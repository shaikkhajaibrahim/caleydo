package org.geneview.core.view.opengl.canvas.jukebox;

import gleem.linalg.Rotf;
import gleem.linalg.Vec3f;
import gleem.linalg.Vec4f;
import gleem.linalg.open.Transform;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLEventListener;

import org.geneview.core.command.CommandQueueSaxType;
import org.geneview.core.command.data.CmdDataCreateSelectionSetMakro;
import org.geneview.core.command.view.opengl.CmdGlObjectPathway3D;
import org.geneview.core.data.collection.ISet;
import org.geneview.core.data.collection.set.selection.ISetSelection;
import org.geneview.core.data.graph.core.PathwayGraph;
import org.geneview.core.data.graph.item.vertex.PathwayVertexGraphItem;
import org.geneview.core.data.graph.item.vertex.PathwayVertexGraphItemRep;
import org.geneview.core.data.mapping.EGenomeMappingType;
import org.geneview.core.data.view.camera.IViewFrustum;
import org.geneview.core.data.view.camera.ViewFrustumBase.ProjectionMode;
import org.geneview.core.data.view.rep.selection.SelectedElementRep;
import org.geneview.core.manager.IGeneralManager;
import org.geneview.core.manager.ILoggerManager.LoggerType;
import org.geneview.core.manager.event.mediator.IMediatorReceiver;
import org.geneview.core.manager.event.mediator.IMediatorSender;
import org.geneview.core.manager.type.ManagerObjectType;
import org.geneview.core.manager.view.EPickingMode;
import org.geneview.core.manager.view.EPickingType;
import org.geneview.core.manager.view.ESelectionMode;
import org.geneview.core.manager.view.Pick;
import org.geneview.core.util.exception.GeneViewRuntimeException;
import org.geneview.core.util.slerp.SlerpAction;
import org.geneview.core.util.slerp.SlerpMod;
import org.geneview.core.util.system.StringConversionTool;
import org.geneview.core.util.system.SystemTime;
import org.geneview.core.util.system.Time;
import org.geneview.core.view.jogl.mouse.PickingJoglMouseListener;
import org.geneview.core.view.opengl.canvas.AGLCanvasUser;
import org.geneview.core.view.opengl.canvas.parcoords.ESelectionType;
import org.geneview.core.view.opengl.canvas.pathway.GLPathwayManager;
import org.geneview.core.view.opengl.util.EIconTextures;
import org.geneview.core.view.opengl.util.GLIconTextureManager;
import org.geneview.core.view.opengl.util.GLSharedObjects;
import org.geneview.core.view.opengl.util.JukeboxHierarchyLayer;
import org.geneview.core.view.opengl.util.selection.EViewInternalSelectionType;
import org.geneview.util.graph.EGraphItemHierarchy;
import org.geneview.util.graph.EGraphItemProperty;
import org.geneview.util.graph.IGraphItem;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureIO;

/**
 * Implementation of the overall Jukebox setup.
 * It supports the user with the ability to navigate
 * and interact with arbitrary views.
 * 
 * @author Marc Streit
 *
 */
public class GLCanvasOverallJukebox3D
extends AGLCanvasUser
implements IMediatorReceiver, IMediatorSender 
{
	private static final int MAX_LOADED_VIEWS = 100;
	private static final float SCALING_FACTOR_UNDER_INTERACTION_LAYER = 0.5f;
	private static final float SCALING_FACTOR_TRANSITION_LAYER = 0.05f;
	private static final float SCALING_FACTOR_STACK_LAYER = 0.5f;
	private static final float SCALING_FACTOR_POOL_LAYER = 0.01f;
	
	private static final int SLERP_RANGE = 1000;
	private static final int SLERP_SPEED = 1300;
	
	private JukeboxHierarchyLayer underInteractionLayer;
	private JukeboxHierarchyLayer stackLayer;
	private JukeboxHierarchyLayer poolLayer;
	private JukeboxHierarchyLayer transitionLayer;
	
	private ArrayList<SlerpAction> arSlerpActions;
	private Time time;

	/**
	 * Slerp factor: 0 = source; 1 = destination
	 */
	private int iSlerpFactor = 0;
	
	private GLConnectionLineRenderer glConnectionLineRenderer;
	
	private int iDraggedViewID = -1;
	
	private BucketMouseWheelListener bucketMouseWheelListener;
	
	private int iNavigationMouseOverViewID_left = -1;
	private int iNavigationMouseOverViewID_right = -1;
	private int iNavigationMouseOverViewID_out = -1;
	private int iNavigationMouseOverViewID_in = -1;	
	private int iNavigationMouseOverViewID_lock = -1;
	
	private boolean bEnableNavigationOverlay = false;
	
	// FIXME: should be a singleton
	private GLIconTextureManager glIconTextureManager;
	
	private ArrayList<Integer> iAlUninitializedViewIDs;

	/**
	 * Constructor.
	 * 
	 */
	public GLCanvasOverallJukebox3D(final IGeneralManager generalManager,
			final int iViewId,
			final int iGLCanvasID,
			final String sLabel,
			final IViewFrustum viewFrustum)
	{

		super(generalManager, iViewId, iGLCanvasID, sLabel, viewFrustum);
		
		pickingTriggerMouseAdapter.addGLCanvas(this);
		
		underInteractionLayer = new JukeboxHierarchyLayer(generalManager,
				1, 
				SCALING_FACTOR_UNDER_INTERACTION_LAYER, 
				null);
		
		stackLayer = new JukeboxHierarchyLayer(generalManager,
				4, 
				SCALING_FACTOR_STACK_LAYER, 
				null);
		
		poolLayer = new JukeboxHierarchyLayer(generalManager,
				MAX_LOADED_VIEWS, 
				SCALING_FACTOR_POOL_LAYER, 
				null);
		
		transitionLayer = new JukeboxHierarchyLayer(generalManager,
				1, 
				SCALING_FACTOR_TRANSITION_LAYER, 
				null);
		
		underInteractionLayer.setParentLayer(stackLayer);
		stackLayer.setChildLayer(underInteractionLayer);
		stackLayer.setParentLayer(poolLayer);
		poolLayer.setChildLayer(stackLayer);
		
		Transform transformUnderInteraction = new Transform();
		transformUnderInteraction.setTranslation(new Vec3f(0, 0, 0f));
		transformUnderInteraction.setScale(new Vec3f(
				SCALING_FACTOR_UNDER_INTERACTION_LAYER,
				SCALING_FACTOR_UNDER_INTERACTION_LAYER,
				SCALING_FACTOR_UNDER_INTERACTION_LAYER));
		underInteractionLayer.setTransformByPositionIndex(0, transformUnderInteraction);
		
		Transform transformTransition = new Transform();
		transformTransition.setTranslation(new Vec3f(1.9f, 0, 0.1f));
		transformTransition.setScale(new Vec3f(
				SCALING_FACTOR_TRANSITION_LAYER,
				SCALING_FACTOR_TRANSITION_LAYER,
				SCALING_FACTOR_TRANSITION_LAYER));
		transitionLayer.setTransformByPositionIndex(0, transformTransition);
		
		arSlerpActions = new ArrayList<SlerpAction>();
		
		glConnectionLineRenderer = new GLConnectionLineRenderer(generalManager,
				underInteractionLayer, stackLayer, poolLayer);
		
		bucketMouseWheelListener = new BucketMouseWheelListener(this);
		// Unregister standard mouse wheel listener
		parentGLCanvas.removeMouseWheelListener(pickingTriggerMouseAdapter);
		// Register specialized bucket mouse wheel listener
		parentGLCanvas.addMouseWheelListener(bucketMouseWheelListener);
		
		iAlUninitializedViewIDs = new ArrayList<Integer>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#initLocal(javax.media.opengl.GL)
	 */	
	public void initLocal(final GL gl) 
	{
		init(gl);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#initRemote(javax.media.opengl.GL, int, org.geneview.core.view.opengl.util.JukeboxHierarchyLayer, org.geneview.core.view.jogl.mouse.PickingJoglMouseListener)
	 */
	public void initRemote(final GL gl, 
			final int iRemoteViewID, 
			final JukeboxHierarchyLayer layer,
			final PickingJoglMouseListener pickingTriggerMouseAdapter)
	{		
		this.pickingTriggerMouseAdapter = pickingTriggerMouseAdapter;
		
		init(gl);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#init(javax.media.opengl.GL)
	 */
	public void init(final GL gl) 
	{
		glIconTextureManager = new GLIconTextureManager(gl);
		
	    time = new SystemTime();
	    ((SystemTime) time).rebase();
	    
		retrieveContainedViews(gl);
		
	    buildPoolLayer(gl);
		buildStackLayer(gl);
		
//		float[] fArLightPosition = {-2, 0, 5, 0};
//		float[] fArLight1Position = {2, 0, 5, 0};
//		float[] fArLModelAmbient = {0.1f, 0.1f, 0.1f, 1.0f};
//		float[] fArMatSpecular = {1, 1, 1, 1};
//		float[] fArWhiteLight = {1, 1, 1, 1};
//		float[] fArMatShininess = {50};
//		//float[] fArSpotDirection = {0, 0, -1};
//		gl.glShadeModel(GL.GL_SMOOTH);
//		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, fArMatSpecular, 0);
//		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, fArMatShininess, 0);
//		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION,	fArLightPosition, 0);
//		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, fArWhiteLight, 0);
//		gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, fArWhiteLight, 0);
//		
//		gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION,	fArLight1Position, 0);
//		gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, fArWhiteLight, 0);
//		gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, fArWhiteLight, 0);
//		
//		//gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPOT_DIRECTION, fArSpotDirection, 0);
//		gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, fArLModelAmbient, 0);
//		gl.glEnable(GL.GL_LIGHTING);
//		gl.glEnable(GL.GL_LIGHT0);
//		gl.glEnable(GL.GL_LIGHT1);
//		//gl.glEnable(GL.GL_DEPTH_TEST);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#displayLocal(javax.media.opengl.GL)
	 */
	public void displayLocal(final GL gl) 
	{
		if (pickingTriggerMouseAdapter.wasRightMouseButtonPressed())
		{
			bEnableNavigationOverlay = !bEnableNavigationOverlay;
		}
		
		pickingManager.handlePicking(iUniqueId, gl, true);
//		if(bIsDisplayListDirtyLocal)
//		{
//			buildDisplayList(gl, iGLDisplayListIndexLocal);
//			bIsDisplayListDirtyLocal = false;			
//		}	
		display(gl);
		
		checkForHits(gl);
		
		pickingTriggerMouseAdapter.resetEvents();
//		gl.glCallList(iGLDisplayListIndexLocal);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#displayRemote(javax.media.opengl.GL)
	 */
	public void displayRemote(final GL gl) 
	{
	
//		if(bIsDisplayListDirtyRemote)
//		{
//			buildPathwayDisplayList(gl, iGLDisplayListIndexRemote);
//			bIsDisplayListDirtyRemote = false;
//		}	
		display(gl);
//		gl.glCallList(iGLDisplayListIndexRemote);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#display(javax.media.opengl.GL)
	 */
	public void display(final GL gl) 
	{

//		if (bRebuildVisiblePathwayDisplayLists)
//			rebuildVisiblePathwayDisplayLists(gl);
		
		time.update();

		doSlerpActions(gl);
		
//		gl.glNewList(iGLDisplayListIndex, GL.GL_COMPILE);

		renderLayer(gl, underInteractionLayer);
		
		// If user zooms to the bucket bottom all but the under
		// interaction layer is _not_ rendered.
		if (!bucketMouseWheelListener.isBucketBottomReached())
		{
			renderLayer(gl, transitionLayer);			
			renderLayer(gl, poolLayer);
			renderLayer(gl, stackLayer);
			
			glConnectionLineRenderer.render(gl);
		}
		
//		renderUnderInteractionLayer(gl);
//		gl.glEndList();
		
		// TODO: add dirty flag
//		gl.glCallList(iGLDisplayListIndex);
		
		bucketMouseWheelListener.render();
	}

	private void retrieveContainedViews(final GL gl) 
	{
		Iterator<GLEventListener> iterGLEventListener = 
			generalManager.getSingelton().getViewGLCanvasManager()
			.getAllGLEventListeners().iterator();
		
		while(iterGLEventListener.hasNext())
		{
			AGLCanvasUser tmpGLEventListener = (AGLCanvasUser)iterGLEventListener.next();
			
			if(tmpGLEventListener == this)
				continue;
			
			int iViewId = ((AGLCanvasUser)tmpGLEventListener).getId();
			
			if (underInteractionLayer.containsElement(-1))
			{
				underInteractionLayer.addElement(iViewId);
				underInteractionLayer.setElementVisibilityById(true, iViewId);
				
				tmpGLEventListener.initRemote(gl, iUniqueId, underInteractionLayer, pickingTriggerMouseAdapter);

			}
			else if (stackLayer.containsElement(-1))
			{
				stackLayer.addElement(iViewId);
				stackLayer.setElementVisibilityById(true, iViewId);
				
				tmpGLEventListener.initRemote(gl, iUniqueId, stackLayer, pickingTriggerMouseAdapter);
			}
			else if (poolLayer.containsElement(-1))
			{
				poolLayer.addElement(iViewId);
				poolLayer.setElementVisibilityById(true, iViewId);
				
				tmpGLEventListener.initRemote(gl, iUniqueId, poolLayer, pickingTriggerMouseAdapter);
			}
			
//			pickingTriggerMouseAdapter.addGLCanvas(tmpGLEventListener);
			pickingManager.getPickingID(iUniqueId, EPickingType.VIEW_SELECTION, iViewId);
		}
	}
	
	private void buildStackLayer(final GL gl) 
	{
		
		float fTiltAngleDegree = 90; // degree
		float fTiltAngleRad = Vec3f.convertGrad2Radiant(fTiltAngleDegree);
		
		// TOP BUCKET WALL
		Transform transform = new Transform();
		transform.setTranslation(new Vec3f(0, 8 * SCALING_FACTOR_STACK_LAYER, 0));
		transform.setScale(new Vec3f(SCALING_FACTOR_STACK_LAYER,
				SCALING_FACTOR_STACK_LAYER,
				SCALING_FACTOR_STACK_LAYER));		
		transform.setRotation(new Rotf(new Vec3f(1, 0, 0), fTiltAngleRad));
		stackLayer.setTransformByPositionIndex(0, transform);

		// BOTTOM BUCKET WALL
		transform = new Transform();
		transform.setTranslation(new Vec3f(0, 0, 4));
		transform.setScale(new Vec3f(SCALING_FACTOR_STACK_LAYER,
				SCALING_FACTOR_STACK_LAYER,
				SCALING_FACTOR_STACK_LAYER));		
		transform.setRotation(new Rotf(new Vec3f(-1, 0, 0), fTiltAngleRad));
		stackLayer.setTransformByPositionIndex(2, transform);

		// LEFT BUCKET WALL
		transform = new Transform();
		transform.setTranslation(new Vec3f(0, 0, 4));
		transform.setScale(new Vec3f(SCALING_FACTOR_STACK_LAYER,
				SCALING_FACTOR_STACK_LAYER,
				SCALING_FACTOR_STACK_LAYER));		
		transform.setRotation(new Rotf(new Vec3f(0, 1, 0), fTiltAngleRad));
		stackLayer.setTransformByPositionIndex(1, transform);

		// RIGHT BUCKET WALL
		transform = new Transform();
		transform.setTranslation(new Vec3f(8 * SCALING_FACTOR_STACK_LAYER, 0, 0));
		transform.setScale(new Vec3f(SCALING_FACTOR_STACK_LAYER,
				SCALING_FACTOR_STACK_LAYER,
				SCALING_FACTOR_STACK_LAYER));		
		transform.setRotation(new Rotf(new Vec3f(0, -1f, 0), fTiltAngleRad));
		stackLayer.setTransformByPositionIndex(3, transform);
	}
	
	private void buildPoolLayer(final GL gl) 
	{
		for (int iViewIndex = 0; iViewIndex < poolLayer.getCapacity(); iViewIndex++)
		{		
			Transform transform = new Transform();
			transform.setTranslation(new Vec3f(4.1f, 
					0.1f * iViewIndex, 4));
			transform.setScale(new Vec3f(SCALING_FACTOR_POOL_LAYER,
					SCALING_FACTOR_POOL_LAYER,
					SCALING_FACTOR_POOL_LAYER));	
			poolLayer.setTransformByPositionIndex(iViewIndex, transform);		
		}
	}

		
	private void renderBucketWall(final GL gl) 
	{	
		gl.glColor4f(0.4f, 0.4f, 0.4f, 0.8f);
		gl.glLineWidth(4);
		
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(0, 0, -0.01f);
		gl.glVertex3f(0, 8, -0.01f);
		gl.glVertex3f(8, 8, -0.01f);
		gl.glVertex3f(8, 0, -0.01f);
		gl.glEnd();
		
		gl.glColor4f(0.9f, 0.9f, 0.9f, 0.5f);
		
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(0, 0, -0.01f);
		gl.glVertex3f(0, 8, -0.01f);
		gl.glVertex3f(8, 8, -0.01f);
		gl.glVertex3f(8, 0, -0.01f);
		gl.glEnd();
	}
	
	private void renderLayer(final GL gl, 
			final JukeboxHierarchyLayer layer) 
	{
		Iterator<Integer> iterElementList = layer.getElementList().iterator();
		int iViewId = 0;
		int iLayerPositionIndex = 0;
		
		while(iterElementList.hasNext())
		{		
			iViewId = iterElementList.next();		
			
			// Check if spot in layer is currently empty
			if(iViewId == -1)
			{
				renderEmptyBucketWall(gl, layer, iLayerPositionIndex);
			}
			else
			{
				gl.glPushName(pickingManager.getPickingID(iUniqueId, EPickingType.VIEW_SELECTION, iViewId));
				renderViewByID(gl, iViewId, layer);		
				gl.glPopName();
			}
			
			iLayerPositionIndex++;
		}
	}
		
	private void renderViewByID(final GL gl,
			final int iViewID, 
			final JukeboxHierarchyLayer layer) 
	{
		// Init newly created pathways
		// FIXME: Scheduling problem because of creation in update at arbitrary time spot?
		if (!iAlUninitializedViewIDs.isEmpty())
		{
			Iterator<Integer> iterUninitializedViewIDs = iAlUninitializedViewIDs.iterator();
			while (iterUninitializedViewIDs.hasNext())
			{				
				int iTmpViewID = iterUninitializedViewIDs.next();
				
				if (underInteractionLayer.containsElement(-1))
				{
					underInteractionLayer.addElement(iTmpViewID);
					underInteractionLayer.setElementVisibilityById(true, iTmpViewID);
					
					((AGLCanvasUser)generalManager.getSingelton().getViewGLCanvasManager()
							.getItem(iTmpViewID)).initRemote(gl, iUniqueId, underInteractionLayer, pickingTriggerMouseAdapter);	
				}
				else if (stackLayer.containsElement(-1))
				{
					stackLayer.addElement(iTmpViewID);
					stackLayer.setElementVisibilityById(true, iTmpViewID);
					
					((AGLCanvasUser)generalManager.getSingelton().getViewGLCanvasManager()
							.getItem(iTmpViewID)).initRemote(gl, iUniqueId, stackLayer, pickingTriggerMouseAdapter);	
				}
				else if (poolLayer.containsElement(-1))
				{
					poolLayer.addElement(iTmpViewID);
					poolLayer.setElementVisibilityById(true, iTmpViewID);

					((AGLCanvasUser)generalManager.getSingelton().getViewGLCanvasManager()
							.getItem(iTmpViewID)).initRemote(gl, iUniqueId, poolLayer, pickingTriggerMouseAdapter);	
				}				
				
			}

			iAlUninitializedViewIDs.clear();
		}

		
		// Check if view is visible
		if(!layer.getElementVisibilityById(iViewID))
			return;
		
		AGLCanvasUser tmpCanvasUser = ((AGLCanvasUser) generalManager.getSingelton()
				.getViewGLCanvasManager().getItem(iViewID));
		
		if (tmpCanvasUser == null)
			throw new GeneViewRuntimeException("Cannot render canvas object which is null!");
		
		gl.glPushMatrix();
		
		Transform transform = layer.getTransformByElementId(iViewID);
		Vec3f translation = transform.getTranslation();
		Rotf rot = transform.getRotation();
		Vec3f scale = transform.getScale();		
		Vec3f axis = new Vec3f();
		float fAngle = rot.get(axis);
		
		gl.glTranslatef(translation.x(), translation.y(), translation.z());
		gl.glScalef(scale.x(), scale.y(), scale.z());
		gl.glRotatef(Vec3f.convertRadiant2Grad(fAngle), axis.x(), axis.y(), axis.z() );

		if (!layer.equals(transitionLayer) && !layer.equals(poolLayer))
		{
			renderBucketWall(gl);
		}
		
		// Render transparent plane for picking views without texture (e.g. PC)
		if (layer.equals(poolLayer))
		{
			gl.glColor4f(1,1,1,1);
			
			gl.glBegin(GL.GL_POLYGON);
			gl.glVertex3f(0, 0, -0.01f);
			gl.glVertex3f(0, 8, -0.01f);
			gl.glVertex3f(8, 8, -0.01f);
			gl.glVertex3f(8, 0, -0.01f);
			gl.glEnd();
		}
		
		tmpCanvasUser.displayRemote(gl);
		
		if (layer.equals(stackLayer))
		{
			renderNavigationOverlay(gl, iViewID);
		}
		
		gl.glPopMatrix();
	}
	
	public void renderEmptyBucketWall(final GL gl,
			final JukeboxHierarchyLayer layer,
			final int iLayerPositionIndex)
	{
		gl.glPushMatrix();
		
		Transform transform = layer.getTransformByPositionIndex(iLayerPositionIndex);
		Vec3f translation = transform.getTranslation();
		Rotf rot = transform.getRotation();
		Vec3f scale = transform.getScale();		
		Vec3f axis = new Vec3f();
		float fAngle = rot.get(axis);
		
		gl.glTranslatef(translation.x(), translation.y(), translation.z());
		gl.glScalef(scale.x(), scale.y(), scale.z());
		gl.glRotatef(Vec3f.convertRadiant2Grad(fAngle), axis.x(), axis.y(), axis.z() );

		if (!layer.equals(transitionLayer) && !layer.equals(poolLayer))
		{
			renderBucketWall(gl);
		}
		
		gl.glPopMatrix();
	}
	
	private void renderNavigationOverlay(final GL gl, final int iViewID)
	{
		if (!bEnableNavigationOverlay)
			return;
		
		generalManager.getSingelton().getViewGLCanvasManager().getInfoAreaManager().enable(false);
		glConnectionLineRenderer.enableRendering(false);
		
		EPickingType leftWallPickingType = null;
		EPickingType rightWallPickingType = null;
		EPickingType topWallPickingType = null;
		EPickingType bottomWallPickingType = null;
		
		Vec4f tmpColor_out = new Vec4f(0.9f,0.9f,0.9f,0.9f);
		Vec4f tmpColor_in = new Vec4f(0.9f,0.9f,0.9f,0.9f);
		Vec4f tmpColor_left = new Vec4f(0.9f,0.9f,0.9f,0.9f);
		Vec4f tmpColor_right = new Vec4f(0.9f,0.9f,0.9f,0.9f);
		Vec4f tmpColor_lock = new Vec4f(0.9f,0.9f,0.9f,0.9f);	
		
		Texture textureLock = glIconTextureManager.getIconTexture(EIconTextures.LOCK);
		Texture textureMoveLeft = null;
		Texture textureMoveRight = null;
		Texture textureMoveOut = null;
		Texture textureMoveIn = null;
		
		TextureCoords texCoords = textureLock.getImageTexCoords();
		
		if (iNavigationMouseOverViewID_lock == iViewID)
			tmpColor_lock.set(1, 0.3f, 0.3f, 0.9f);
		
		if (stackLayer.getPositionIndexByElementId(iViewID) == 0) // top
		{
			topWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
			bottomWallPickingType = EPickingType.BUCKET_MOVE_IN_ICON_SELECTION;
			leftWallPickingType = EPickingType.BUCKET_MOVE_LEFT_ICON_SELECTION;
			rightWallPickingType = EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION;
			
			if (iNavigationMouseOverViewID_out == iViewID)
				tmpColor_out.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_in == iViewID)
				tmpColor_in.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_left == iViewID)
				tmpColor_left.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_right == iViewID)
				tmpColor_right.set(1, 0.3f, 0.3f, 0.9f);		
			
			textureMoveIn = glIconTextureManager.getIconTexture(EIconTextures.ARROW_LEFT);
			textureMoveOut = glIconTextureManager.getIconTexture(EIconTextures.ARROW_DOWN);
			textureMoveLeft = glIconTextureManager.getIconTexture(EIconTextures.ARROW_DOWN);
			textureMoveRight = glIconTextureManager.getIconTexture(EIconTextures.ARROW_LEFT);
		}
		else if (stackLayer.getPositionIndexByElementId(iViewID) == 2) // bottom
		{
			topWallPickingType = EPickingType.BUCKET_MOVE_IN_ICON_SELECTION;
			bottomWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
			leftWallPickingType = EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION;
			rightWallPickingType = EPickingType.BUCKET_MOVE_LEFT_ICON_SELECTION;
			
			if (iNavigationMouseOverViewID_out == iViewID)
				tmpColor_in.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_in == iViewID)
				tmpColor_out.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_left == iViewID)
				tmpColor_right.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_right == iViewID)
				tmpColor_left.set(1, 0.3f, 0.3f, 0.9f);
			
			textureMoveIn = glIconTextureManager.getIconTexture(EIconTextures.ARROW_LEFT);
			textureMoveOut = glIconTextureManager.getIconTexture(EIconTextures.ARROW_DOWN);
			textureMoveLeft = glIconTextureManager.getIconTexture(EIconTextures.ARROW_DOWN);
			textureMoveRight = glIconTextureManager.getIconTexture(EIconTextures.ARROW_LEFT);
		}
		else if (stackLayer.getPositionIndexByElementId(iViewID) == 1) // left
		{
			topWallPickingType = EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION;
			bottomWallPickingType = EPickingType.BUCKET_MOVE_LEFT_ICON_SELECTION;
			leftWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
			rightWallPickingType = EPickingType.BUCKET_MOVE_IN_ICON_SELECTION;
			
			if (iNavigationMouseOverViewID_out == iViewID)
				tmpColor_left.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_in == iViewID)
				tmpColor_right.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_left == iViewID)
				tmpColor_in.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_right == iViewID)
				tmpColor_out.set(1, 0.3f, 0.3f, 0.9f);	
			
			textureMoveIn = glIconTextureManager.getIconTexture(EIconTextures.ARROW_LEFT);
			textureMoveOut = glIconTextureManager.getIconTexture(EIconTextures.ARROW_DOWN);
			textureMoveLeft = glIconTextureManager.getIconTexture(EIconTextures.ARROW_DOWN);
			textureMoveRight = glIconTextureManager.getIconTexture(EIconTextures.ARROW_LEFT);
		}
		else if (stackLayer.getPositionIndexByElementId(iViewID) == 3) // right
		{
			topWallPickingType = EPickingType.BUCKET_MOVE_LEFT_ICON_SELECTION;
			bottomWallPickingType = EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION;
			leftWallPickingType = EPickingType.BUCKET_MOVE_IN_ICON_SELECTION;
			rightWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
			
			if (iNavigationMouseOverViewID_out == iViewID)
				tmpColor_right.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_in == iViewID)
				tmpColor_left.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_left == iViewID)
				tmpColor_out.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_right == iViewID)
				tmpColor_in.set(1, 0.3f, 0.3f, 0.9f);
			
			textureMoveIn = glIconTextureManager.getIconTexture(EIconTextures.ARROW_LEFT);
			textureMoveOut = glIconTextureManager.getIconTexture(EIconTextures.ARROW_DOWN);
			textureMoveLeft = glIconTextureManager.getIconTexture(EIconTextures.ARROW_DOWN);
			textureMoveRight = glIconTextureManager.getIconTexture(EIconTextures.ARROW_LEFT);
		}
//		else if (underInteractionLayer.containsElement(iViewID))
//		{
//			topWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
//			bottomWallPickingType = EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION;
//			leftWallPickingType = EPickingType.BUCKET_MOVE_IN_ICON_SELECTION;
//			rightWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
//		}
		
		gl.glLineWidth(4);
			
		// CENTER - NAVIGATION: LOCK
		gl.glPushName(pickingManager.getPickingID(iUniqueId, 
				EPickingType.BUCKET_LOCK_ICON_SELECTION, 
				iViewID));

		gl.glColor4f(0.5f, 0.5f, 0.5f, 1);
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(2.66f, 2.66f, 0.02f);
		gl.glVertex3f(2.66f, 5.33f, 0.02f);
		gl.glVertex3f(5.33f, 5.33f, 0.02f);
		gl.glVertex3f(5.33f, 2.66f, 0.02f);
		gl.glEnd();
		
		textureLock.enable();
		textureLock.bind();		
		
		gl.glColor4f(tmpColor_lock.x(), tmpColor_lock.y(), tmpColor_lock.z(), tmpColor_lock.w());
		gl.glBegin(GL.GL_POLYGON);
		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		gl.glVertex3f(2.66f, 2.66f, 0.03f); 
		gl.glTexCoord2f(texCoords.left(), texCoords.top());
		gl.glVertex3f(2.66f, 5.33f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.top());
		gl.glVertex3f(5.33f, 5.33f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.bottom()); 
		gl.glVertex3f(5.33f, 2.66f, 0.03f);
		gl.glEnd();
		
		textureLock.disable();
		
		gl.glPopName();	
		
		// BOTTOM - NAVIGATION: MOVE IN
		gl.glPushName(pickingManager.getPickingID(iUniqueId, 
				bottomWallPickingType, 
				iViewID));
		
		gl.glColor4f(0.5f, 0.5f, 0.5f, 1);
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(0, 0, 0.02f);
		gl.glVertex3f(2.66f, 2.66f, 0.02f);
		gl.glVertex3f(5.33f, 2.66f, 0.02f);
		gl.glVertex3f(8, 0, 0.02f);
		gl.glEnd();
		
		gl.glColor4f(tmpColor_in.x(), tmpColor_in.y(), tmpColor_in.z(), tmpColor_in.w());
				
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(0.05f, 0.05f, 0.02f); 
		gl.glVertex3f(2.66f, 2.66f, 0.02f);
		gl.glVertex3f(5.33f, 2.66f, 0.02f);
		gl.glVertex3f(7.95f, 0.02f, 0.02f);
		gl.glEnd();
		
		textureMoveIn.enable();
		textureMoveIn.bind();	
//		texCoords = textureMoveIn.getImageTexCoords();
//		gl.glColor4f(1,0.3f,0.3f,0.9f);
		gl.glBegin(GL.GL_POLYGON);
		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		gl.glVertex3f(2.66f, 0.05f, 0.03f); 
		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
		gl.glVertex3f(2.66f, 2.66f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.top());
		gl.glVertex3f(5.33f, 2.66f, 0.03f);
		gl.glTexCoord2f(texCoords.left(), texCoords.top()); 
		gl.glVertex3f(5.33f, 0.05f, 0.03f);
		gl.glEnd();		
		
		textureMoveIn.disable();
		
		gl.glPopName();
		
		// RIGHT - NAVIGATION: MOVE RIGHT
		gl.glPushName(pickingManager.getPickingID(iUniqueId, 
				rightWallPickingType, 
				iViewID));
		
		gl.glColor4f(0.5f, 0.5f, 0.5f, 1);
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(8, 0, 0.02f);
		gl.glVertex3f(5.33f, 2.66f, 0.02f);
		gl.glVertex3f(5.33f, 5.33f, 0.02f);
		gl.glVertex3f(8, 8, 0.02f);
		gl.glEnd();
		
		gl.glColor4f(tmpColor_right.x(), tmpColor_right.y(), tmpColor_right.z(), tmpColor_right.w());
		
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(7.95f, 0.05f, 0.02f);
		gl.glVertex3f(5.33f, 2.66f, 0.02f);
		gl.glVertex3f(5.33f, 5.33f, 0.02f);
		gl.glVertex3f(7.95f, 7.95f, 0.02f);
		gl.glEnd();
		
		textureMoveRight.enable();
		textureMoveRight.bind();
		
//		gl.glColor4f(0,1,0,1);
		gl.glBegin(GL.GL_POLYGON);
		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		gl.glVertex3f(7.95f, 2.66f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
		gl.glVertex3f(5.33f, 2.66f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.top());
		gl.glVertex3f(5.33f, 5.33f, 0.03f);
		gl.glTexCoord2f(texCoords.left(), texCoords.top()); 
		gl.glVertex3f(7.95f, 5.33f, 0.03f);
		gl.glEnd();		
		
		textureMoveRight.disable();
		
		gl.glPopName();
		
		// LEFT - NAVIGATION: MOVE LEFT
		gl.glPushName(pickingManager.getPickingID(iUniqueId, 
				leftWallPickingType, 
				iViewID));
		
		gl.glColor4f(0.5f, 0.5f, 0.5f, 1);
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(0, 0, 0.02f);
		gl.glVertex3f(0, 8, 0.02f);
		gl.glVertex3f(2.66f, 5.33f, 0.02f);
		gl.glVertex3f(2.66f, 2.66f, 0.02f);
		gl.glEnd();
		
		gl.glColor4f(tmpColor_left.x(), tmpColor_left.y(), tmpColor_left.z(), tmpColor_left.w());
		
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(0.05f, 0.05f, 0.02f);
		gl.glVertex3f(0.05f, 7.95f, 0.02f);
		gl.glVertex3f(2.66f, 5.33f, 0.02f);
		gl.glVertex3f(2.66f, 2.66f, 0.02f);
		gl.glEnd();
		
		textureMoveLeft.enable();
		textureMoveLeft.bind();
		
//		gl.glColor4f(0,1,0,1);
		gl.glBegin(GL.GL_POLYGON);
		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		gl.glVertex3f(0.05f, 2.66f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
		gl.glVertex3f(0.05f, 5.33f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.top());
		gl.glVertex3f(2.66f, 5.33f, 0.03f);
		gl.glTexCoord2f(texCoords.left(), texCoords.top()); 
		gl.glVertex3f(2.66f, 2.66f, 0.03f);
		gl.glEnd();		
		
		textureMoveLeft.disable();
		
		gl.glPopName();
		
		// TOP - NAVIGATION: MOVE OUT
		gl.glPushName(pickingManager.getPickingID(iUniqueId, 
				topWallPickingType, 
				iViewID));
		
		gl.glColor4f(0.5f, 0.5f, 0.5f, 1);
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(0, 8, 0.02f);
		gl.glVertex3f(8, 8, 0.02f);
		gl.glVertex3f(5.33f, 5.33f, 0.02f);
		gl.glVertex3f(2.66f, 5.33f, 0.02f);
		gl.glEnd();
		
		gl.glColor4f(tmpColor_out.x(), tmpColor_out.y(), tmpColor_out.z(), tmpColor_out.w());
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(0.05f, 7.95f, 0.02f);
		gl.glVertex3f(7.95f, 7.95f, 0.02f);
		gl.glVertex3f(5.33f, 5.33f, 0.02f);
		gl.glVertex3f(2.66f, 5.33f, 0.02f);
		gl.glEnd();
		
		textureMoveOut.enable();
		textureMoveOut.bind();
		
//		gl.glColor4f(0,1,0,1);
		gl.glBegin(GL.GL_POLYGON);
		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		gl.glVertex3f(2.66f, 7.95f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
		gl.glVertex3f(5.33f, 7.95f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.top());
		gl.glVertex3f(5.33f, 5.33f, 0.03f);
		gl.glTexCoord2f(texCoords.left(), texCoords.top()); 
		gl.glVertex3f(2.66f, 5.33f, 0.03f);
		gl.glEnd();		
		
		textureMoveOut.disable();
		
		gl.glPopName();
	}
	
	private void doSlerpActions(final GL gl) 
	{
		if (arSlerpActions.isEmpty())
			return;
		
		SlerpAction tmpSlerpAction = arSlerpActions.get(0);
		
		if (iSlerpFactor == 0)
		{
			tmpSlerpAction.start();
			
//			tmpSlerpAction.getOriginHierarchyLayer().setElementVisibilityById(false, 
//					tmpSlerpAction.getOriginHierarchyLayer().getElementIdByPositionIndex(
//							tmpSlerpAction.getOriginPosIndex()));
			
			// Update layer in toolbox renderer
			((AGLCanvasUser)generalManager.getSingelton().getViewGLCanvasManager().getItem(
					tmpSlerpAction.getElementId())).getToolboxRenderer()
						.updateLayer(tmpSlerpAction.getDestinationHierarchyLayer());
		}
		
		if (iSlerpFactor < SLERP_RANGE)
		{
			// Makes animation rendering speed independent
			iSlerpFactor += SLERP_SPEED * time.deltaT();
			
			if (iSlerpFactor > SLERP_RANGE)
				iSlerpFactor = SLERP_RANGE;
		}
		
		slerpView(gl, tmpSlerpAction);
	}
	
	private void slerpView(final GL gl, SlerpAction slerpAction) 
	{

		int iViewId = slerpAction.getElementId();
		SlerpMod slerpMod = new SlerpMod();
		
		if ((iSlerpFactor == 0))
		{	
			slerpMod.playSlerpSound();
		}
		
		Transform transform = slerpMod.interpolate(slerpAction
				.getOriginHierarchyLayer().getTransformByPositionIndex(
						slerpAction.getOriginPosIndex()), slerpAction
				.getDestinationHierarchyLayer().getTransformByPositionIndex(
						slerpAction.getDestinationPosIndex()),
				(float)iSlerpFactor / SLERP_RANGE);

		gl.glPushMatrix();
		
		slerpMod.applySlerp(gl, transform);
		
		((AGLCanvasUser) generalManager.getSingelton()
				.getViewGLCanvasManager().getItem(iViewId)).displayRemote(gl);

		gl.glPopMatrix();

		if (iSlerpFactor >= SLERP_RANGE)
		{
			arSlerpActions.remove(slerpAction);
			
//			if (!slerpAction.getOriginHierarchyLayer().equals(slerpAction.getDestinationHierarchyLayer()))
//			{
				// Remove view from origin layer after slerping
//				slerpAction.getOriginHierarchyLayer().removeElement(iViewId);
//			}
				
			slerpAction.getDestinationHierarchyLayer()
				.setElementVisibilityById(true, iViewId);		
			
			iSlerpFactor = 0;
		}
		
		// After last slerp action is done the line connections are turned on again
		if (arSlerpActions.isEmpty())
			glConnectionLineRenderer.enableRendering(true);
	}
	
	private void loadViewToUnderInteractionLayer(final int iViewID) 
	{
		generalManager.getSingelton().logMsg(this.getClass().getSimpleName()
								+ ": loadPathwayToUnderInteractionPosition(): View with ID "
								+ iViewID + " is under interaction.", LoggerType.VERBOSE);

//		// Check if pathway is already under interaction
//		if (underInteractionLayer.containsElement(iViewID))
//			return;
				
		// Check if other slerp action is currently running
		if (iSlerpFactor > 0 && iSlerpFactor < SLERP_RANGE)
			return;

		arSlerpActions.clear();
		
		glConnectionLineRenderer.enableRendering(false);

//		// Check if under interaction layer is free
//		if (underInteractionLayer.getElementList().isEmpty())
//		{
//			// Slerp directly from pool to under interaction layer
//			SlerpAction slerpAction = new SlerpAction(iViewId, stackLayer, false);
//			arSlerpActions.add(slerpAction);
//		}
//		else
//		{

//		// Check if stack layer has a free spot to switch out view under interaction
//		if (stackLayer.containsElement(-1))
//		{
//			// Slerp current view back to layered view
//			if (!underInteractionLayer.getElementList().isEmpty())
//			{
//				// Slerp selected view to under interaction transition position
//				SlerpAction slerpActionTransition = new SlerpAction(
//						iViewID, poolLayer, transitionLayer);	
//				arSlerpActions.add(slerpActionTransition);
//				
//				// Slerp under interaction view to free spot in stack
//				SlerpAction reverseSlerpAction = new SlerpAction(
//						underInteractionLayer.getElementIdByPositionIndex(0),
//						underInteractionLayer, stackLayer);
//				arSlerpActions.add(reverseSlerpAction);	
//				
//				// Slerp selected view from transition position to under interaction position
//				SlerpAction slerpAction = new SlerpAction(
//						iViewID, transitionLayer, underInteractionLayer);	
//				arSlerpActions.add(slerpAction);	
//			}
//		}
//		else
//		{
			// Check if view is already loaded in the stack layer
			if (stackLayer.containsElement(iViewID))
			{
				// Slerp selected view to under interaction transition position
				SlerpAction slerpActionTransition = new SlerpAction(
						iViewID, stackLayer, transitionLayer);	
				arSlerpActions.add(slerpActionTransition);
				
				// Slerp under interaction view to free spot in stack
				SlerpAction reverseSlerpAction = new SlerpAction(
						underInteractionLayer.getElementIdByPositionIndex(0),
						underInteractionLayer, stackLayer);
				arSlerpActions.add(reverseSlerpAction);	
				
				// Slerp selected view from transition position to under interaction position
				SlerpAction slerpAction = new SlerpAction(
						iViewID, transitionLayer, underInteractionLayer);	
				arSlerpActions.add(slerpAction);
			}
			else
			{
				// Slerp selected view to under interaction transition position
				SlerpAction slerpActionTransition = new SlerpAction(
						iViewID, poolLayer, transitionLayer);	
				arSlerpActions.add(slerpActionTransition);
				
				// Slerp view from stack to pool
				SlerpAction reverseSlerpAction = new SlerpAction(
						stackLayer.getElementIdByPositionIndex(stackLayer.getNextPositionIndex()),
						stackLayer, true);
				arSlerpActions.add(reverseSlerpAction);		
				
				// Slerp under interaction view to free spot in stack
				SlerpAction reverseSlerpAction2 = new SlerpAction(
						underInteractionLayer.getElementIdByPositionIndex(0),
						underInteractionLayer, true);
				arSlerpActions.add(reverseSlerpAction2);		
				
				// Slerp selected view from transition position to under interaction position
				SlerpAction slerpAction = new SlerpAction(
						iViewID, transitionLayer, underInteractionLayer);	
				arSlerpActions.add(slerpAction);	
			}
//		}
//		
		iSlerpFactor = 0;
		
//		// Slerp current pathway back to layered view
//		if (!underInteractionLayer.getElementList().isEmpty())
//		{
//			SlerpAction reverseSlerpAction = new SlerpAction(
//					underInteractionLayer.getElementIdByPositionIndex(0),
//					underInteractionLayer, true);
//
//			arSlerpActions.add(reverseSlerpAction);
//		}
//
//		SlerpAction slerpAction;
//
//		// Prevent slerp action if pathway is already in layered view
//		if (!stackLayer.containsElement(iViewId))
//		{
//			// Slerp to layered pathway view
//			slerpAction = new SlerpAction(iViewId, poolLayer, false);
//
//			arSlerpActions.add(slerpAction);
//		}
//
//		// Slerp from layered to under interaction position
//		slerpAction = new SlerpAction(iViewId, stackLayer, false);
//
//		arSlerpActions.add(slerpAction);
//		iSlerpFactor = 0;
//
////		bRebuildVisiblePathwayDisplayLists = true;
//		//selectedVertex = null;
	}

	@Override
	public void updateReceiver(Object eventTrigger) 
	{

		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateReceiver(Object eventTrigger, ISet updatedSet) 
	{
		generalManager.getSingelton().logMsg(
				this.getClass().getSimpleName()
						+ " ("+iUniqueId+"): updateReceiver(Object eventTrigger, ISet updatedSet): Update called by "
						+ eventTrigger.getClass().getSimpleName()+" ("+((AGLCanvasUser)eventTrigger).getId()+")",
				LoggerType.VERBOSE);
		
		ISetSelection refSetSelection = (ISetSelection) updatedSet;

		refSetSelection.getReadToken();
		ArrayList<Integer> iAlSelection = refSetSelection.getSelectionIdArray();
		ArrayList<Integer> iAlSelectionGroup = refSetSelection.getGroupArray();
		ArrayList<IGraphItem> alPathwayVertexGraphItem = new ArrayList<IGraphItem>();
		
		for (int iSelectionIndex = 0; iSelectionIndex < iAlSelection.size(); iSelectionIndex++)
		{			
			int iAccessionID = iAlSelection.get(iSelectionIndex);
			
			if (iAlSelectionGroup.get(iSelectionIndex) != 2)
				continue;
			
			String sAccessionCode = generalManager.getSingelton().getGenomeIdManager()
				.getIdStringFromIntByMapping(iAccessionID, EGenomeMappingType.ACCESSION_2_ACCESSION_CODE);
		
			System.out.println("Accession Code: " +sAccessionCode);
								
			int iNCBIGeneID = generalManager.getSingelton().getGenomeIdManager()
				.getIdIntFromIntByMapping(iAccessionID, EGenomeMappingType.ACCESSION_2_NCBI_GENEID);

			String sNCBIGeneIDCode = generalManager.getSingelton().getGenomeIdManager()
				.getIdStringFromIntByMapping(iNCBIGeneID, EGenomeMappingType.NCBI_GENEID_2_NCBI_GENEID_CODE);
		
			int iNCBIGeneIDCode = StringConversionTool.convertStringToInt(sNCBIGeneIDCode, -1);
			
			PathwayVertexGraphItem tmpPathwayVertexGraphItem = 
				((PathwayVertexGraphItem)generalManager.getSingelton().getPathwayItemManager().getItem(
					generalManager.getSingelton().getPathwayItemManager().getPathwayVertexGraphItemIdByNCBIGeneId(iNCBIGeneIDCode)));
		
			alPathwayVertexGraphItem.add(tmpPathwayVertexGraphItem);
		}
		
		if (!alPathwayVertexGraphItem.isEmpty())
			loadDependentPathways(alPathwayVertexGraphItem);
		
	}
	
	public void loadDependentPathways(final List<IGraphItem> alVertex) {

		// Remove pathways from stacked layer view
//		poolLayer.removeAllElements();
		
		Iterator<IGraphItem> iterPathwayGraphItem = alVertex.iterator();
		Iterator<IGraphItem> iterIdenticalPathwayGraphItemRep = null;
		
		IGraphItem pathwayGraphItem;
		int iPathwayID = 0;
		
		while (iterPathwayGraphItem.hasNext())
		{
			pathwayGraphItem = iterPathwayGraphItem.next();
			
			iterIdenticalPathwayGraphItemRep = 
				pathwayGraphItem.getAllItemsByProp(EGraphItemProperty.ALIAS_CHILD).iterator();

			while (iterIdenticalPathwayGraphItemRep.hasNext())
			{
				iPathwayID = ((PathwayGraph)iterIdenticalPathwayGraphItemRep.next()
						.getAllGraphByType(EGraphItemHierarchy.GRAPH_PARENT).toArray()[0]).getId();

				// Check if pathway is already loaded in bucket
				if (generalManager.getSingelton().getPathwayManager().isPathwayVisible(iPathwayID))
					continue;
							
				ArrayList<Integer> iArSetIDs = new ArrayList<Integer>();
				iArSetIDs.add(35101);
				iArSetIDs.add(36101);
				iArSetIDs.add(37101);
				iArSetIDs.add(39101);
				
				// Create new selection set
				int iSelectionSetID = generalManager.getSingelton().getSetManager().createId(ManagerObjectType.SET);
				CmdDataCreateSelectionSetMakro selectedSetCmd = (CmdDataCreateSelectionSetMakro) generalManager.getSingelton().getCommandManager()
					.createCommandByType(CommandQueueSaxType.CREATE_SET_SELECTION_MAKRO);
				selectedSetCmd.setAttributes(iSelectionSetID);
				selectedSetCmd.doCommand();
				
				iArSetIDs.add(iSelectionSetID);
				
				int iGeneratedViewID = generalManager.getSingelton().getViewGLCanvasManager().createId(ManagerObjectType.VIEW);
				
				// Create Pathway3D view
				CmdGlObjectPathway3D cmdPathway = (CmdGlObjectPathway3D) generalManager.getSingelton()
					.getCommandManager().createCommandByType(CommandQueueSaxType.CREATE_GL_PATHWAY_3D);
				
				cmdPathway.setAttributes(iGeneratedViewID, iPathwayID, iArSetIDs, 
						ProjectionMode.ORTHOGRAPHIC, -4, 4, 4, -4, -20, 20);
					
				cmdPathway.doCommand();
				
				iAlUninitializedViewIDs.add(iGeneratedViewID);
				
//				// Slerp to layered pathway view
//				SlerpAction slerpAction = new SlerpAction(iPathwayId,
//						poolLayer, false);
//		
//				arSlerpActions.add(slerpAction);
			}
			
			iSlerpFactor = 0;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#handleEvents(org.geneview.core.manager.view.EPickingType, org.geneview.core.manager.view.EPickingMode, int, org.geneview.core.manager.view.Pick)
	 */
	protected void handleEvents(EPickingType pickingType,
			EPickingMode pickingMode, int iExternalID, Pick pick) 
	{
		switch (pickingType)
		{
		case VIEW_SELECTION:
			switch (pickingMode)
			{
			case MOUSE_OVER:
				
				generalManager.getSingelton().getViewGLCanvasManager().getInfoAreaManager()
					.setDataAboutView(iExternalID);
				
				break;
		
			case CLICKED:			
				
				generalManager.getSingelton().getViewGLCanvasManager().getInfoAreaManager()
					.setDataAboutView(iExternalID);
				
				if (poolLayer.containsElement(iExternalID))// || stackLayer.containsElement(iViewID))
				{
					loadViewToUnderInteractionLayer(iExternalID);
				}
				
				break;
			}
		
		pickingManager.flushHits(iUniqueId, EPickingType.VIEW_SELECTION);
			
		break;
		
		case BUCKET_LOCK_ICON_SELECTION:
			switch (pickingMode)
			{
			case CLICKED:

				break;
				
			case MOUSE_OVER:
				
				iNavigationMouseOverViewID_lock = iExternalID;
				iNavigationMouseOverViewID_left = -1;
				iNavigationMouseOverViewID_right = -1;
				iNavigationMouseOverViewID_out = -1;
				iNavigationMouseOverViewID_in = -1;
				
				break;
			}
			
		pickingManager.flushHits(iUniqueId, EPickingType.BUCKET_LOCK_ICON_SELECTION);	
			
		break;
		
		case BUCKET_MOVE_IN_ICON_SELECTION:
			switch (pickingMode)
			{
			case CLICKED:
				loadViewToUnderInteractionLayer(iExternalID);							
				bEnableNavigationOverlay = false;
				break;
				
			case MOUSE_OVER:
				
				iNavigationMouseOverViewID_left = -1;
				iNavigationMouseOverViewID_right = -1;
				iNavigationMouseOverViewID_out = -1;
				iNavigationMouseOverViewID_in = iExternalID;
				iNavigationMouseOverViewID_lock = -1;
				
				break;
			}
			
		pickingManager.flushHits(iUniqueId, EPickingType.BUCKET_MOVE_IN_ICON_SELECTION);
		
		break;
		
		case BUCKET_MOVE_OUT_ICON_SELECTION:
			switch (pickingMode)
			{
			case CLICKED:
				
				// Check if other slerp action is currently running
				if (iSlerpFactor > 0 && iSlerpFactor < SLERP_RANGE)
					break;

				arSlerpActions.clear();
				
				glConnectionLineRenderer.enableRendering(false);
				
				SlerpAction slerpActionTransition = new SlerpAction(
						iExternalID, stackLayer, poolLayer);	
				arSlerpActions.add(slerpActionTransition);
				
				bEnableNavigationOverlay = false;

				break;
				
			case MOUSE_OVER:
				
				iNavigationMouseOverViewID_left = -1;
				iNavigationMouseOverViewID_right = -1;
				iNavigationMouseOverViewID_out = iExternalID;
				iNavigationMouseOverViewID_in = -1;
				iNavigationMouseOverViewID_lock = -1;
				
				break;
			}

		pickingManager.flushHits(iUniqueId, EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION);	
			
		break;
		
		case BUCKET_MOVE_LEFT_ICON_SELECTION:
			switch (pickingMode)
			{
			case CLICKED:
				// Check if other slerp action is currently running
				if (iSlerpFactor > 0 && iSlerpFactor < SLERP_RANGE)
					break;

				arSlerpActions.clear();
				
				glConnectionLineRenderer.enableRendering(false);
				
				int iDestinationPosIndex = stackLayer.getPositionIndexByElementId(iExternalID);
				
				if (iDestinationPosIndex == 3)
					iDestinationPosIndex = 0;
				else
					iDestinationPosIndex++;
				
				if(stackLayer.getElementIdByPositionIndex(iDestinationPosIndex) == -1)
				{
					SlerpAction slerpAction= new SlerpAction(iExternalID, 
							stackLayer, stackLayer, stackLayer.getPositionIndexByElementId(-1));	
					arSlerpActions.add(slerpAction);
				}
				else
				{	
					SlerpAction slerpActionTransition = new SlerpAction(
							iExternalID, stackLayer, transitionLayer);	
					arSlerpActions.add(slerpActionTransition);
					
					SlerpAction slerpAction= new SlerpAction(
							stackLayer.getElementIdByPositionIndex(iDestinationPosIndex), 
							stackLayer, stackLayer, stackLayer.getPositionIndexByElementId(iExternalID));	
					arSlerpActions.add(slerpAction);
					
					SlerpAction slerpActionTransitionReverse= new SlerpAction(
							iExternalID, transitionLayer, stackLayer, iDestinationPosIndex);	
					arSlerpActions.add(slerpActionTransitionReverse);
				}
				
				bEnableNavigationOverlay = false;
				
				break;
				
			case MOUSE_OVER:
				
				iNavigationMouseOverViewID_left = iExternalID;
				iNavigationMouseOverViewID_right = -1;
				iNavigationMouseOverViewID_out = -1;
				iNavigationMouseOverViewID_in = -1;
				iNavigationMouseOverViewID_lock = -1;
				
				break;
			}
			
		pickingManager.flushHits(iUniqueId, EPickingType.BUCKET_MOVE_LEFT_ICON_SELECTION);	
		
		break;
		
		case BUCKET_MOVE_RIGHT_ICON_SELECTION:
			switch (pickingMode)
			{
			case CLICKED:
				// Check if other slerp action is currently running
				if (iSlerpFactor > 0 && iSlerpFactor < SLERP_RANGE)
					break;

				arSlerpActions.clear();
				
				glConnectionLineRenderer.enableRendering(false);
				
				int iDestinationPosIndex = stackLayer.getPositionIndexByElementId(iExternalID);
				
				if (iDestinationPosIndex == 0)
					iDestinationPosIndex = 3;
				else
					iDestinationPosIndex--;
				
				if(stackLayer.getElementIdByPositionIndex(iDestinationPosIndex) == -1)
				{
					SlerpAction slerpAction= new SlerpAction(iExternalID, 
							stackLayer, stackLayer, stackLayer.getPositionIndexByElementId(-1));	
					arSlerpActions.add(slerpAction);
				}
				else
				{
					SlerpAction slerpActionTransition = new SlerpAction(
							iExternalID, stackLayer, transitionLayer);	
					arSlerpActions.add(slerpActionTransition);
					
					SlerpAction slerpAction= new SlerpAction(
							stackLayer.getElementIdByPositionIndex(iDestinationPosIndex), 
							stackLayer, stackLayer, stackLayer.getPositionIndexByElementId(iExternalID));	
					arSlerpActions.add(slerpAction);
					
					SlerpAction slerpActionTransitionReverse= new SlerpAction(
							iExternalID, transitionLayer, stackLayer, iDestinationPosIndex);	
					arSlerpActions.add(slerpActionTransitionReverse);	
				}	
					
				bEnableNavigationOverlay = false;				
					
				break;
			
			case MOUSE_OVER:
				
				iNavigationMouseOverViewID_left = -1;
				iNavigationMouseOverViewID_right = iExternalID;
				iNavigationMouseOverViewID_out = -1;
				iNavigationMouseOverViewID_in = -1;
				iNavigationMouseOverViewID_lock = -1;
			
				break;
			}
		
			break;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#getInfo()
	 */
	public ArrayList<String> getInfo()
	{	
		ArrayList<String> sAlInfo = new ArrayList<String>();
		sAlInfo.add("No info available!");
		return sAlInfo;
	}
}
