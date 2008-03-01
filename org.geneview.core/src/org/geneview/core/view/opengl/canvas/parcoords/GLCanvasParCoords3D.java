package org.geneview.core.view.opengl.canvas.parcoords;


import gleem.linalg.Vec3f;

import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Set;

import javax.media.opengl.GL;
import javax.media.opengl.GLException;

import org.geneview.core.data.collection.ISet;
import org.geneview.core.data.collection.IStorage;
import org.geneview.core.data.collection.SetType;
import org.geneview.core.data.collection.set.selection.ISetSelection;
import org.geneview.core.data.mapping.EGenomeMappingType;
import org.geneview.core.data.view.camera.IViewFrustum;
import org.geneview.core.data.view.rep.renderstyle.ParCoordsRenderStyle;
import org.geneview.core.data.view.rep.selection.SelectedElementRep;
import org.geneview.core.manager.IGeneralManager;
import org.geneview.core.manager.ILoggerManager.LoggerType;
import org.geneview.core.manager.data.IGenomeIdManager;
import org.geneview.core.manager.event.mediator.IMediatorReceiver;
import org.geneview.core.manager.event.mediator.IMediatorSender;
import org.geneview.core.manager.view.EPickingMode;
import org.geneview.core.manager.view.EPickingType;
import org.geneview.core.manager.view.ESelectionMode;
import org.geneview.core.manager.view.Pick;
import org.geneview.core.util.exception.GeneViewRuntimeException;
import org.geneview.core.util.exception.GeneViewRuntimeExceptionType;
import org.geneview.core.view.jogl.mouse.PickingJoglMouseListener;
import org.geneview.core.view.opengl.canvas.AGLCanvasUser;
import org.geneview.core.view.opengl.util.EIconTextures;
import org.geneview.core.view.opengl.util.GLCoordinateUtils;
import org.geneview.core.view.opengl.util.GLInfoAreaManager;
import org.geneview.core.view.opengl.util.JukeboxHierarchyLayer;

import com.sun.media.sound.AlawCodec;
import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureIO;

/**
 * 
 * @author Alexander Lex (responsible for PC)
 * @author Marc Streit
 * 
 * This class is responsible for rendering the parallel coordinates
 *
 */
public class GLCanvasParCoords3D 
extends AGLCanvasUser
implements IMediatorReceiver, IMediatorSender 
{
	
	private enum RenderMode
	{
		NORMAL,
		SELECTION,
		MOUSE_OVER,
		DESELECTED
	}	
		
	private float fAxisSpacing = 0;
		
	private int iGLDisplayListIndexLocal;
	private int iGLDisplayListIndexRemote;
	private int iGLDisplayListToCall = 0;
		
	// flag whether one array should be a polyline or an axis
	private boolean bRenderArrayAsPolyline = false;
	// flag whether the whole data or the selection should be rendered
	private boolean bRenderSelection = true;
	// flag whether to take measures against occlusion or not
	private boolean bPreventOcclusion = true;
	
	private EInputDataTypes eAxisDataType = EInputDataTypes.EXPERIMENTS;
	private EInputDataTypes ePolylineDataType = EInputDataTypes.GENES;
	
	private boolean bIsDisplayListDirtyLocal = true;
	private boolean bIsDisplayListDirtyRemote = true;
	
	private boolean bIsDraggingActive = false;
	private EPickingType draggedObject;
		
	private int iNumberOfAxis = 0;
	private float[] fArGateTipHeight;
	private float[] fArGateBottomHeight;
	private int iDraggedGateNumber = 0;
	
	private float fScaling = 0;
	private float fXTranslation = 0;
	private float fYTranslation = 0;
	
	private ParCoordsRenderStyle renderStyle;
	
	private PolylineSelectionManager polyLineSelectionManager;
	
	private TextRenderer textRenderer;
	
	private GLInfoAreaManager infoAreaManager; 
	
	//protected HashMap <ESelectionType, ISetSelection> hashSetSelection;
	
	private boolean bRenderInfoArea = false;
	private boolean bInfoAreaFirstTime = false;
	
	private IGenomeIdManager IDManager;
	
	private ESelectionType eWhichContentSelection = ESelectionType.EXTERNAL_SELECTION;
	private ESelectionType eWhichStorageSelection = ESelectionType.STORAGE_SELECTION;
	private ArrayList<Integer> alContentSelection;
	private ArrayList<Integer> alStorageSelection;
	
	
	ArrayList<IStorage> alDataStorages;
	
	DecimalFormat decimalFormat;
	
	EnumMap<EIconTextures, Texture> mapIconTextures;
	EnumMap<ESelectionType, ArrayList<Integer>> mapSelections;
	//Texture texture;
	
	/**
	 * Constructor.
	 * 
	 */
	public GLCanvasParCoords3D(final IGeneralManager generalManager,
			final int iViewId,
			final int iGLCanvasID,
			final String sLabel,
			final IViewFrustum viewFrustum) 
	{
		super(generalManager, iViewId, iGLCanvasID, sLabel, viewFrustum);
	
		alDataStorages = new ArrayList<IStorage>();
		renderStyle = new ParCoordsRenderStyle();		
		polyLineSelectionManager = new PolylineSelectionManager();	
		
		textRenderer = new TextRenderer(new Font("Arial",
				Font.BOLD, 16), false);
		
		infoAreaManager = new GLInfoAreaManager(generalManager);
		
		decimalFormat = new DecimalFormat("#####.##");
		IDManager = generalManager.getSingelton().getGenomeIdManager();
		
		mapIconTextures = new EnumMap<EIconTextures, Texture>(EIconTextures.class);
		mapSelections = new EnumMap<ESelectionType, ArrayList<Integer>>(ESelectionType.class);
		//hashSetSelection = new HashMap<ESelectionType, ISetSelection>();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#initLocal(javax.media.opengl.GL)
	 */	
	public void initLocal(final GL gl)
	{
		iGLDisplayListIndexLocal = gl.glGenLists(1);
		iGLDisplayListToCall = iGLDisplayListIndexLocal;
		init(gl);
		
		toolboxRenderer = new GLParCoordsToolboxRenderer(generalManager, iUniqueId, new Vec3f (0, 0, 0), true);
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
		toolboxRenderer = new GLParCoordsToolboxRenderer(generalManager,
				iUniqueId, iRemoteViewID, new Vec3f (0, 0, 0), layer, true);
		
		this.pickingTriggerMouseAdapter = pickingTriggerMouseAdapter;
	
		iGLDisplayListIndexRemote = gl.glGenLists(1);	
		iGLDisplayListToCall = iGLDisplayListIndexRemote;
		init(gl);		
	}

	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#init(javax.media.opengl.GL)
	 */
	public void init(final GL gl) 
	{
		
		for(EIconTextures eIconTextures : EIconTextures.values())
		{
			try
			{
				Texture tempTexture = TextureIO.newTexture(TextureIO.newTextureData(
						new File(eIconTextures.getFileName()), true, "PNG"));
				mapIconTextures.put(eIconTextures, tempTexture);
			} catch (GLException e)
			{
				e.printStackTrace();
			} catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}	
		}
		
		// initialize selection to an empty array with 
		ISetSelection tmpSelection = alSetSelection.get(0);		
		// TODO: only for tests, should be {}
		int[] iArTmpSelectionIDs = {3, 4, 5, 6, 7, 9, 12};
		tmpSelection.setSelectionIdArray(iArTmpSelectionIDs);
		initSelections();
		initPolyLineLists();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#displayLocal(javax.media.opengl.GL)
	 */
	public void displayLocal(final GL gl) 
	{		
		pickingManager.handlePicking(iUniqueId, gl, true);
		if(bIsDisplayListDirtyLocal)
		{
			buildPolyLineDisplayList(gl, iGLDisplayListIndexLocal);
			bIsDisplayListDirtyLocal = false;			
		}	
		iGLDisplayListToCall = iGLDisplayListIndexLocal;
		display(gl);
		
		checkForHits(gl);		
		
		pickingTriggerMouseAdapter.resetEvents();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#displayRemote(javax.media.opengl.GL)
	 */
	public void displayRemote(final GL gl) 
	{		
		if(bIsDisplayListDirtyRemote)
		{
			buildPolyLineDisplayList(gl, iGLDisplayListIndexRemote);
			bIsDisplayListDirtyRemote = false;
		}	
		iGLDisplayListToCall = iGLDisplayListIndexRemote;
		display(gl);
		
		checkForHits(gl);
		pickingTriggerMouseAdapter.resetEvents();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#display(javax.media.opengl.GL)
	 */
	public void display(final GL gl) 
	{	
		// FIXME: scaling here not nice, operations are not in display lists
		if(bIsDraggingActive)
		{			
			gl.glTranslatef(fXTranslation, fYTranslation, 0.0f);
			gl.glScalef(fScaling, fScaling, 1.0f);
			handleDragging(gl);
	
			gl.glScalef(1/fScaling, 1/fScaling, 1.0f);
			gl.glTranslatef(-fXTranslation, -fYTranslation, 0.0f);			
		}
		if(bRenderInfoArea)
		{
			gl.glTranslatef(fXTranslation, fYTranslation, 0.0f);
			gl.glScalef(fScaling, fScaling, 1.0f);
			
			infoAreaManager.renderInfoArea(gl, bInfoAreaFirstTime);
			bInfoAreaFirstTime = false;
		
			gl.glScalef(1/fScaling, 1/fScaling, 1.0f);
			gl.glTranslatef(-fXTranslation, -fYTranslation, 0.0f);
		}

		gl.glCallList(iGLDisplayListToCall);
		toolboxRenderer.render(gl);	
	}
		
	/**
	 * Choose whether to render one array as a polyline and every entry across arrays is an axis 
	 * or whether the array corresponds to an axis and every entry across arrays is a polyline
	 *  
	 * @param bRenderArrayAsPolyline if true array contents make up a polyline, else array is an axis
	 */
	public void renderArrayAsPolyline(boolean bRenderArrayAsPolyline)
	{
		this.bRenderArrayAsPolyline = bRenderArrayAsPolyline;
		EInputDataTypes eTempType = eAxisDataType;
		eAxisDataType = ePolylineDataType;
		ePolylineDataType = eTempType;
		initPolyLineLists();
	}
	
	/**
	 * Choose whether to render just the selection or all data
	 * 
	 * @param bRenderSelection if true renders only the selection, else renders everything in the data
	 */
	public void renderSelection(boolean bRenderSelection)
	{
		this.bRenderSelection = bRenderSelection;
		if(bRenderSelection)
		{
			eWhichContentSelection = ESelectionType.EXTERNAL_SELECTION;
		}
		else
			eWhichContentSelection = ESelectionType.COMPLETE_SELECTION;
		initPolyLineLists();
	}
	
	/**
	 * Choose whether to take measures against occlusion or not
	 * 
	 * @param bPreventOcclusion
	 */
	public void preventOcclusion(boolean bPreventOcclusion)
	{
		this.bPreventOcclusion = bPreventOcclusion;
	}
	
	/**
	 * Reset all selections and deselections
	 */
	public void resetSelections()
	{
		for(int iCount = 0; iCount < fArGateTipHeight.length; iCount++)
		{
			fArGateTipHeight[iCount] = 0;
			fArGateBottomHeight[iCount] = ParCoordsRenderStyle.GATE_NEGATIVE_Y_OFFSET -
				ParCoordsRenderStyle.GATE_TIP_HEIGHT;
		}
		polyLineSelectionManager.clearDeselection();
		polyLineSelectionManager.clearMouseOver();
		polyLineSelectionManager.clearSelection();
		
		bRenderInfoArea = false;
	}
	
	public void refresh()
	{
		initPolyLineLists();
		bIsDisplayListDirtyLocal = true;
		bIsDisplayListDirtyRemote = true;
		bRenderInfoArea = false;
	}
	
	
	private void initSelections()
	{
		// TODO: check if I only get in here once
		alDataStorages.clear();
		
		
		if (alSetData == null)
			return;
		
		if (alSetSelection == null)
			return;				
				
		
		Iterator<ISet> iterSetData = alSetData.iterator();
		while (iterSetData.hasNext())
		{
			ISet tmpSet = iterSetData.next();
						
			if (tmpSet.getSetType().equals(SetType.SET_GENE_EXPRESSION_DATA))
			{
				alDataStorages.add(tmpSet.getStorageByDimAndIndex(0, 0));
			}
		}	
		
		ArrayList<Integer> alTempList = new ArrayList<Integer>();
		int[] iArTemp = alSetSelection.get(0).getSelectionIdArray();
		for(int iCount = 0; iCount < iArTemp.length; iCount++)
		{
			alTempList.add(iArTemp[iCount]);
		}
		
		mapSelections.put(ESelectionType.EXTERNAL_SELECTION, alTempList);

		//int iStorageLength = alDataStorages.get(0).getArrayFloat().length;
		int iStorageLength = 1000;
		alTempList = new ArrayList<Integer>(iStorageLength);
		// initialize full list
		for(int iCount = 0; iCount < iStorageLength; iCount++)
		{
			alTempList.add(iCount);
		}
		
		mapSelections.put(ESelectionType.COMPLETE_SELECTION, alTempList);
		
		alTempList = new ArrayList<Integer>();
		
		for(int iCount = 0; iCount < alDataStorages.size(); iCount++)
		{
			alTempList.add(iCount);
		}
		
		mapSelections.put(ESelectionType.STORAGE_SELECTION, alTempList);
	}
	
	
	/**
	 * Initializes the array lists that contain the data. 
	 * Must be run at program start, 
	 * every time you exchange axis and polylines and
	 * every time you change storages or selections
	 */
	private void initPolyLineLists()
	{						
		polyLineSelectionManager.clearAll();
		
		int iNumberOfEntriesToRender = 0;		

		alContentSelection = mapSelections.get(eWhichContentSelection);
		alStorageSelection = mapSelections.get(eWhichStorageSelection);
		iNumberOfEntriesToRender = alContentSelection.size();
	
		int iNumberOfPolyLinesToRender = 0;		
		
		// if true one array corresponds to one polyline, number of arrays is number of polylines
		if (bRenderArrayAsPolyline)
		{			
			iNumberOfPolyLinesToRender = alStorageSelection.size();
			iNumberOfAxis = iNumberOfEntriesToRender;			
		}
		// render polylines across storages - first element of storage 1 to n makes up polyline
		else
		{						
			iNumberOfPolyLinesToRender = iNumberOfEntriesToRender;
			iNumberOfAxis = alStorageSelection.size();
		}
		
		fArGateTipHeight = new float[iNumberOfAxis];
		fArGateBottomHeight = new float[iNumberOfAxis];
		
		for(int iCount = 0; iCount < fArGateTipHeight.length; iCount++)
		{
			fArGateTipHeight[iCount] = 0;
			fArGateBottomHeight[iCount] = ParCoordsRenderStyle.GATE_NEGATIVE_Y_OFFSET - ParCoordsRenderStyle.GATE_TIP_HEIGHT;
		}
			
		// this for loop executes once per polyline
		for (int iPolyLineCount = 0; iPolyLineCount < iNumberOfPolyLinesToRender; iPolyLineCount++)
		{	
			if(bRenderArrayAsPolyline)
				polyLineSelectionManager.initialAdd(alStorageSelection.get(iPolyLineCount));
			else
				polyLineSelectionManager.initialAdd(alContentSelection.get(iPolyLineCount));
		}
		
		fScaling = 2.5f;
		
		fXTranslation = (viewFrustum.getRight() - viewFrustum.getLeft()
				-(iNumberOfAxis-1)*fAxisSpacing*fScaling)/2.0f;
		fYTranslation = (viewFrustum.getTop() - viewFrustum.getBottom() - fScaling)/2.0f;
	
		fAxisSpacing = renderStyle.getAxisSpacing(iNumberOfAxis);
		
	}
	

	
	private void buildPolyLineDisplayList(final GL gl, int iGLDisplayListIndex)
	{		
		gl.glNewList(iGLDisplayListIndex, GL.GL_COMPILE);

		gl.glTranslatef(fXTranslation, fYTranslation, 0.0f);
		gl.glScalef(fScaling, fScaling, 1.0f);
		
		renderCoordinateSystem(gl, iNumberOfAxis);	
		
		renderPolylines(gl, RenderMode.DESELECTED);
		renderPolylines(gl, RenderMode.NORMAL);
		renderPolylines(gl, RenderMode.MOUSE_OVER);
		renderPolylines(gl, RenderMode.SELECTION);		
		
		renderGates(gl, iNumberOfAxis);				
		
		gl.glScalef(1/fScaling, 1/fScaling, 1.0f);
		gl.glTranslatef(-fXTranslation, -fYTranslation, 0.0f);		
		gl.glEndList();
	}
	
	private void renderPolylines(GL gl, RenderMode renderMode)
	{				
		
		Set<Integer> setDataToRender = null;
		float fZDepth = 0.0f;
		
		switch (renderMode)
		{
			case NORMAL:
				setDataToRender = polyLineSelectionManager.getNormalPolylines();
				if(bPreventOcclusion)				
					gl.glColor4fv(renderStyle.
							getPolylineOcclusionPrevColor(setDataToRender.size()), 0);									
				else
					gl.glColor4fv(ParCoordsRenderStyle.POLYLINE_NO_OCCLUSION_PREV_COLOR, 0);
				
				gl.glLineWidth(ParCoordsRenderStyle.POLYLINE_LINE_WIDTH);
				break;
			case SELECTION:	
				setDataToRender = polyLineSelectionManager.getSelectedPolylines();
				gl.glColor4fv(ParCoordsRenderStyle.POLYLINE_SELECTED_COLOR, 0);
				gl.glLineWidth(ParCoordsRenderStyle.SELECTED_POLYLINE_LINE_WIDTH);
				break;
			case MOUSE_OVER:
				setDataToRender = polyLineSelectionManager.getMouseOverPolylines();
				gl.glColor4fv(ParCoordsRenderStyle.POLYLINE_MOUSE_OVER_COLOR, 0);
				gl.glLineWidth(ParCoordsRenderStyle.MOUSE_OVER_POLYLINE_LINE_WIDTH);
				break;
			case DESELECTED:	
				setDataToRender = polyLineSelectionManager.getDeselectedPolylines();				
				gl.glColor4fv(renderStyle.
						getPolylineDeselectedOcclusionPrevColor(setDataToRender.size()),
						0);
				gl.glLineWidth(ParCoordsRenderStyle.DESELECTED_POLYLINE_LINE_WIDTH);
				break;
			default:
				setDataToRender = polyLineSelectionManager.getNormalPolylines();
		}
		
		Iterator<Integer> dataIterator = setDataToRender.iterator();
		// this for loop executes once per polyline
		while(dataIterator.hasNext())
		{
			int iPolyLineID = dataIterator.next();
			if(renderMode != RenderMode.DESELECTED)
				gl.glPushName(pickingManager.getPickingID(iUniqueId, EPickingType.POLYLINE_SELECTION, iPolyLineID));
			
			IStorage currentStorage = null;
			
			// decide on which storage to use when array is polyline
			if(bRenderArrayAsPolyline)
			{
				int iWhichStorage = iPolyLineID;				
				//currentStorage = alDataStorages.get(alStorageSelection.get(iWhichStorage));
				currentStorage = alDataStorages.get(iWhichStorage);
			}
			
			float fPreviousXValue = 0;
			float fPreviousYValue = 0;
			float fCurrentXValue = 0;
			float fCurrentYValue = 0;

			// this loop executes once per axis
			for (int iVertricesCount = 0; iVertricesCount < iNumberOfAxis; iVertricesCount++)
			{
				int iStorageIndex = 0;
				
				// get the index if array as polyline
				if (bRenderArrayAsPolyline)
				{
					iStorageIndex = alContentSelection.get(iVertricesCount);
				}
				// get the storage and the storage index for the different cases				
				else
				{				
					currentStorage = alDataStorages.get(alStorageSelection.get(iVertricesCount));					
					iStorageIndex = iPolyLineID;					
				}			
								
				fCurrentXValue = iVertricesCount * fAxisSpacing;
				fCurrentYValue = currentStorage.getArrayFloat()[iStorageIndex];
				if(iVertricesCount != 0)
				{
					gl.glBegin(GL.GL_LINES);
					gl.glVertex3f(fPreviousXValue, fPreviousYValue, fZDepth);
					gl.glVertex3f(fCurrentXValue, fCurrentYValue, fZDepth);	
					gl.glEnd();
				}
				
				if(renderMode == RenderMode.SELECTION || renderMode == RenderMode.MOUSE_OVER)
				{
					renderYValues(gl, fCurrentXValue, fCurrentYValue, renderMode);					
				}				
				
				fPreviousXValue = fCurrentXValue;
				fPreviousYValue = fCurrentYValue;
			}						
			
			if(renderMode != RenderMode.DESELECTED)
				gl.glPopName();			
		}		
	}
	

	private void renderCoordinateSystem(GL gl, final int iNumberAxis)
	{
		textRenderer.setColor(0, 0, 0, 1);

		// draw X-Axis
		gl.glColor4fv(ParCoordsRenderStyle.X_AXIS_COLOR, 0);
				
		gl.glLineWidth(ParCoordsRenderStyle.X_AXIS_LINE_WIDTH);
		
		gl.glPushName(pickingManager.getPickingID(iUniqueId, EPickingType.X_AXIS_SELECTION, 1));
		gl.glBegin(GL.GL_LINES);		
	
		gl.glVertex3f(-0.1f, 0.0f, 0.0f); 
		gl.glVertex3f(((iNumberAxis-1) * fAxisSpacing)+0.1f, 0.0f, 0.0f);
			
		gl.glEnd();
		gl.glPopName();
		
		// draw all Y-Axis

		gl.glColor4fv(ParCoordsRenderStyle.Y_AXIS_COLOR, 0);
			
		gl.glLineWidth(ParCoordsRenderStyle.Y_AXIS_LINE_WIDTH);			
		
		int iCount = 0;
		while (iCount < iNumberAxis)
		{
			gl.glPushName(pickingManager.getPickingID(iUniqueId, EPickingType.Y_AXIS_SELECTION, iCount));
			gl.glBegin(GL.GL_LINES);
			gl.glVertex3f(iCount * fAxisSpacing, 
					ParCoordsRenderStyle.Y_AXIS_LOW, 
					ParCoordsRenderStyle.AXIS_Z);
			gl.glVertex3f(iCount * fAxisSpacing, 
					ParCoordsRenderStyle.MAX_HEIGHT,
					ParCoordsRenderStyle.AXIS_Z);
			gl.glVertex3f(iCount * fAxisSpacing - ParCoordsRenderStyle.AXIS_MARKER_WIDTH,
					ParCoordsRenderStyle.MAX_HEIGHT, 
					ParCoordsRenderStyle.AXIS_Z);
			gl.glVertex3f(iCount * fAxisSpacing + ParCoordsRenderStyle.AXIS_MARKER_WIDTH,
					ParCoordsRenderStyle.MAX_HEIGHT, 
					ParCoordsRenderStyle.AXIS_Z);			
			gl.glEnd();				
			gl.glPopName();
			
			
			String sAxisLabel = null;
			switch (eAxisDataType) 
			{
			case EXPERIMENTS:
				sAxisLabel = "Exp." + iCount;
				sAxisLabel = alSetData.get(alStorageSelection.get(iCount)).getLabel();
				break;
			case GENES:				
				sAxisLabel = getAccessionNumberFromStorageIndex(alContentSelection.get(iCount));
				break;
			default:
				sAxisLabel = "No Label";
			}
			gl.glPushAttrib(GL.GL_CURRENT_BIT);
			gl.glRotatef(90, 0, 0, 1);
			textRenderer.begin3DRendering();	
			textRenderer.draw3D(sAxisLabel, ParCoordsRenderStyle.MAX_HEIGHT + 0.01f, - iCount * fAxisSpacing, 0, ParCoordsRenderStyle.SMALL_FONT_SCALING_FACTOR);
			textRenderer.end3DRendering();
			gl.glRotatef(-90, 0, 0, 1);
			
			textRenderer.begin3DRendering();
			// TODO: set this to real values once we have more than normalized values
			textRenderer.draw3D(String.valueOf(ParCoordsRenderStyle.MAX_HEIGHT),
								iCount * fAxisSpacing + 2 * ParCoordsRenderStyle.AXIS_MARKER_WIDTH,
								ParCoordsRenderStyle.MAX_HEIGHT, 0, 0.002f);
			textRenderer.end3DRendering();
			gl.glPopAttrib();
			
			// render Buttons
			
			int iNumberOfButtons = 0;
			if(iCount != 0 || iCount != iNumberAxis-1)			
				iNumberOfButtons = 4;			
			else
				iNumberOfButtons = 3;
		
			float fXButtonOrigin = 0;
			float fYButtonOrigin = 0;
			int iPickingID = -1;
			
			fXButtonOrigin = iCount * fAxisSpacing - 
				(iNumberOfButtons * ParCoordsRenderStyle.AXIS_BUTTON_WIDTH +
				(iNumberOfButtons - 1) * ParCoordsRenderStyle.AXIS_BUTTONS_X_SPACING) / 2;
			fYButtonOrigin = -ParCoordsRenderStyle.AXIS_BUTTONS_Y_OFFSET;
			
			if(iCount != 0)
			{				
				iPickingID = pickingManager.getPickingID(iUniqueId, EPickingType.MOVE_AXIS_LEFT, iCount);
				renderButton(gl, fXButtonOrigin, fYButtonOrigin, iPickingID, EIconTextures.MOVE_AXIS_LEFT);
			}			
		
			// remove button
			fXButtonOrigin = fXButtonOrigin + ParCoordsRenderStyle.AXIS_BUTTON_WIDTH +
				ParCoordsRenderStyle.AXIS_BUTTONS_X_SPACING;
			
			iPickingID = pickingManager.getPickingID(iUniqueId, EPickingType.REMOVE_AXIS, iCount);			
			renderButton(gl, fXButtonOrigin, fYButtonOrigin, iPickingID, EIconTextures.REMOVE_AXIS);
			
			// duplicate axis button
			fXButtonOrigin = fXButtonOrigin + ParCoordsRenderStyle.AXIS_BUTTON_WIDTH +
			ParCoordsRenderStyle.AXIS_BUTTONS_X_SPACING;
			iPickingID = pickingManager.getPickingID(iUniqueId, EPickingType.DUPLICATE_AXIS, iCount);			
			renderButton(gl, fXButtonOrigin, fYButtonOrigin, iPickingID, EIconTextures.DUPLICATE_AXIS);	
		
			if(iCount != iNumberAxis-1)
			{
				// right, move right button
				fXButtonOrigin = fXButtonOrigin + ParCoordsRenderStyle.AXIS_BUTTON_WIDTH +
					ParCoordsRenderStyle.AXIS_BUTTONS_X_SPACING;				
				iPickingID = pickingManager.getPickingID(iUniqueId, EPickingType.MOVE_AXIS_RIGHT, iCount);
				renderButton(gl, fXButtonOrigin, fYButtonOrigin, iPickingID, EIconTextures.MOVE_AXIS_RIGHT);		
			}
			iCount++;
		}				
	}
	
	private void renderButton(GL gl, float fXButtonOrigin, 
				float fYButtonOrigin, 
				int iPickingID, 
				EIconTextures eIconTextures)
	{	
		
		Texture tempTexture = mapIconTextures.get(eIconTextures);
		tempTexture.enable();
		tempTexture.bind();
		
		TextureCoords texCoords = tempTexture.getImageTexCoords();
		
		gl.glPushAttrib(GL.GL_CURRENT_BIT);
		gl.glColor4f(1, 1, 1, 1);
		gl.glPushName(iPickingID);
		gl.glBegin(GL.GL_POLYGON);		
		
		gl.glTexCoord2f(texCoords.left(), texCoords.bottom()); 
		gl.glVertex3f(fXButtonOrigin, 
				fYButtonOrigin, 
				ParCoordsRenderStyle.AXIS_Z);
		gl.glTexCoord2f(texCoords.left(), texCoords.top()); 
		gl.glVertex3f(fXButtonOrigin, 
				fYButtonOrigin + ParCoordsRenderStyle.AXIS_BUTTON_WIDTH,
				ParCoordsRenderStyle.AXIS_Z);
		gl.glTexCoord2f(texCoords.right(), texCoords.top()); 
		gl.glVertex3f(fXButtonOrigin + ParCoordsRenderStyle.AXIS_BUTTON_WIDTH,
				fYButtonOrigin + ParCoordsRenderStyle.AXIS_BUTTON_WIDTH,
				ParCoordsRenderStyle.AXIS_Z);
		gl.glTexCoord2f(texCoords.right(), texCoords.bottom()); 
		gl.glVertex3f(fXButtonOrigin + ParCoordsRenderStyle.AXIS_BUTTON_WIDTH,
				fYButtonOrigin,
				ParCoordsRenderStyle.AXIS_Z);
		gl.glEnd();	
		gl.glPopName();
		gl.glPopAttrib();
		tempTexture.disable();
	}
	
	private void renderGates(GL gl, int iNumberAxis)
	{		
		gl.glColor4fv(ParCoordsRenderStyle.GATE_COLOR, 0);
		
		int iCount = 0;
		while (iCount < iNumberAxis)
		{			
			float fCurrentPosition = iCount * fAxisSpacing;
			
			// The tip of the gate (which is pickable)
			gl.glPushName(pickingManager.getPickingID(iUniqueId, 
					EPickingType.LOWER_GATE_TIP_SELECTION, iCount));
			gl.glBegin(GL.GL_POLYGON);
			// variable
			gl.glVertex3f(fCurrentPosition + ParCoordsRenderStyle.GATE_WIDTH,
						fArGateTipHeight[iCount] - ParCoordsRenderStyle.GATE_TIP_HEIGHT,
						0.001f);
			// variable
			gl.glVertex3f(fCurrentPosition,
						fArGateTipHeight[iCount],
						0.001f);			
			// variable
			gl.glVertex3f(fCurrentPosition - ParCoordsRenderStyle.GATE_WIDTH,
						fArGateTipHeight[iCount] - ParCoordsRenderStyle.GATE_TIP_HEIGHT,
						0.001f);
			gl.glEnd();
			gl.glPopName();
			
			renderYValues(gl, fCurrentPosition, fArGateTipHeight[iCount], RenderMode.NORMAL);
			
			
			// The body of the gate
			gl.glPushName(pickingManager.getPickingID(iUniqueId, EPickingType.LOWER_GATE_BODY_SELECTION, iCount));
			gl.glBegin(GL.GL_POLYGON);
			// bottom
			gl.glVertex3f(fCurrentPosition - ParCoordsRenderStyle.GATE_WIDTH,
						fArGateBottomHeight[iCount] + ParCoordsRenderStyle.GATE_TIP_HEIGHT,
						0.0001f);
			// constant
			gl.glVertex3f(fCurrentPosition + ParCoordsRenderStyle.GATE_WIDTH,
						fArGateBottomHeight[iCount] + ParCoordsRenderStyle.GATE_TIP_HEIGHT,
						0.0001f);
			// top
			gl.glVertex3f(fCurrentPosition + ParCoordsRenderStyle.GATE_WIDTH,
						fArGateTipHeight[iCount] - ParCoordsRenderStyle.GATE_TIP_HEIGHT,
						0.0001f);			
			// top
			gl.glVertex3f(fCurrentPosition - ParCoordsRenderStyle.GATE_WIDTH,
						fArGateTipHeight[iCount] - ParCoordsRenderStyle.GATE_TIP_HEIGHT,
						0.0001f);
			gl.glEnd();
			gl.glPopName();
			
			gl.glPushName(pickingManager.getPickingID(iUniqueId, EPickingType.LOWER_GATE_BOTTOM_SELECTION, iCount));	
			// The bottom of the gate 
			gl.glBegin(GL.GL_POLYGON);
			// variable
			gl.glVertex3f(fCurrentPosition + ParCoordsRenderStyle.GATE_WIDTH,
						fArGateBottomHeight[iCount] + ParCoordsRenderStyle.GATE_TIP_HEIGHT,
						0.001f);
			// variable
			gl.glVertex3f(fCurrentPosition,
						fArGateBottomHeight[iCount],
						0.001f);			
			// variable
			gl.glVertex3f(fCurrentPosition - ParCoordsRenderStyle.GATE_WIDTH,
						fArGateBottomHeight[iCount] + ParCoordsRenderStyle.GATE_TIP_HEIGHT,
						0.001f);
			gl.glEnd();
			gl.glPopName();
			
			renderYValues(gl, fCurrentPosition, fArGateBottomHeight[iCount], RenderMode.NORMAL);			
			
			iCount++;
		}
	}
	
	private void renderYValues(GL gl, float fXOrigin, float fYOrigin, RenderMode renderMode)
	{
		// don't render values that are below the y axis
		if(fYOrigin < 0)
			return;
		
		gl.glPushAttrib(GL.GL_CURRENT_BIT);
		gl.glLineWidth(ParCoordsRenderStyle.Y_AXIS_LINE_WIDTH);
		gl.glColor4fv(ParCoordsRenderStyle.Y_AXIS_COLOR, 0);
		
		Rectangle2D tempRectangle = textRenderer.getBounds(decimalFormat.format(fYOrigin));
		float fBackPlaneWidth = (float)tempRectangle.getWidth() * ParCoordsRenderStyle.SMALL_FONT_SCALING_FACTOR;
		float fBackPlaneHeight = (float)tempRectangle.getHeight() * ParCoordsRenderStyle.SMALL_FONT_SCALING_FACTOR;
		float fXTextOrigin = fXOrigin + 2 * ParCoordsRenderStyle.AXIS_MARKER_WIDTH;
		float fYTextOrigin = fYOrigin;
		
		gl.glColor4f(0.8f, 0.8f, 0.8f, 0.5f);
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(fXTextOrigin, fYTextOrigin, 0.002f);
		gl.glVertex3f(fXTextOrigin + fBackPlaneWidth, fYTextOrigin, 0.002f);
		gl.glVertex3f(fXTextOrigin + fBackPlaneWidth, fYTextOrigin + fBackPlaneHeight, 0.002f);
		gl.glVertex3f(fXTextOrigin, fYTextOrigin + fBackPlaneHeight, 0.002f);
		gl.glEnd();
		
		textRenderer.begin3DRendering();
		// TODO: set this to real values once we have more than normalized values
		textRenderer.draw3D(decimalFormat.format(fYOrigin),
							fXTextOrigin,
							fYTextOrigin,
							0.0021f, ParCoordsRenderStyle.SMALL_FONT_SCALING_FACTOR);
		textRenderer.end3DRendering();
		gl.glPopAttrib();

	}	

	private void handleDragging(GL gl)
	{	
		Point currentPoint = pickingTriggerMouseAdapter.getPickedPoint();
		
		float[] fArTargetWorldCoordinates = GLCoordinateUtils.
			convertWindowCoordinatesToWorldCoordinates(gl, currentPoint.x, currentPoint.y);	
		
		float height = fArTargetWorldCoordinates[1];
		if (draggedObject == EPickingType.LOWER_GATE_TIP_SELECTION)
		{
			float fLowerLimit = fArGateBottomHeight[iDraggedGateNumber] + 2 * ParCoordsRenderStyle.GATE_TIP_HEIGHT;
			
			if (height > 1)
			{
				height = 1;
			}
			else if (height < 0)
			{
				height = 0;
			}				
			else if (height < fLowerLimit)
			{
				height = fLowerLimit;
			}
			
			fArGateTipHeight[iDraggedGateNumber] = height;
		}
		else if (draggedObject == EPickingType.LOWER_GATE_BOTTOM_SELECTION)
		{
			float fLowerLimit = ParCoordsRenderStyle.GATE_NEGATIVE_Y_OFFSET - ParCoordsRenderStyle.GATE_TIP_HEIGHT;
			float fUpperLimit = fArGateTipHeight[iDraggedGateNumber] - 2 * ParCoordsRenderStyle.GATE_TIP_HEIGHT;
			
			if (height > 1 - ParCoordsRenderStyle.GATE_TIP_HEIGHT)
			{
					height = 1 - ParCoordsRenderStyle.GATE_TIP_HEIGHT;
			}			
			else if (height < fLowerLimit)
			{
				height = ParCoordsRenderStyle.GATE_NEGATIVE_Y_OFFSET - ParCoordsRenderStyle.GATE_TIP_HEIGHT;
			}
			else if (height > fUpperLimit)
			{
				height = fUpperLimit;
			}
			
			fArGateBottomHeight[iDraggedGateNumber] =  height;
		}
		else if (draggedObject == EPickingType.LOWER_GATE_BODY_SELECTION)
		{
			
		}
		
		bIsDisplayListDirtyLocal = true;
		bIsDisplayListDirtyRemote = true;		
	
		if(pickingTriggerMouseAdapter.wasMouseReleased())
		{
			bIsDraggingActive = false;
		}
		handleUnselection(iDraggedGateNumber);		
	}
	
	/**
	 * Unselect all lines that are deselected with the gates
	 * @param iAxisNumber
	 */
	private void handleUnselection(int iAxisNumber)
	{	
		IStorage currentStorage = null;
		
		// for every polyline
		for (int iPolylineCount = 0; iPolylineCount < polyLineSelectionManager.getNumberOfPolylines(); iPolylineCount++)
		{	
			int iStorageIndex = 0;
			
			// get the index if array as polyline
			if (bRenderArrayAsPolyline)
			{
				currentStorage = alDataStorages.get(alStorageSelection.get(iPolylineCount));

				iStorageIndex = alContentSelection.get(iAxisNumber);
			}
			// get the storage and the storage index for the different cases				
			else
			{
				iStorageIndex = alContentSelection.get(iPolylineCount);			
				currentStorage = alDataStorages.get(alStorageSelection.get(iAxisNumber));						
			}							
			float fCurrentValue = currentStorage.getArrayFloat()[iStorageIndex];
			if(fCurrentValue < fArGateTipHeight[iAxisNumber] 
			                                    && fCurrentValue > fArGateBottomHeight[iAxisNumber])
			{	
				if(polyLineSelectionManager.isPolylineSelected(iPolylineCount))
					bRenderInfoArea = false;
				
				if(bRenderArrayAsPolyline)
					polyLineSelectionManager.addDeselection(alStorageSelection.get(iPolylineCount));
				else
					polyLineSelectionManager.addDeselection(alContentSelection.get(iPolylineCount));
			}
			else
			{
				boolean bIsBlocked = false;
				
				// every axis
				for (int iLocalAxisCount = 0; iLocalAxisCount < iNumberOfAxis; iLocalAxisCount++)
				{					
					int iLocalStorageIndex = 0;
					if(bRenderArrayAsPolyline)
					{
						if(!bRenderSelection)					
							iLocalStorageIndex = iLocalAxisCount;	
						else
							iLocalStorageIndex = alContentSelection.get(iLocalAxisCount);
						
						fCurrentValue = currentStorage.getArrayFloat()[iLocalStorageIndex];
						if(fCurrentValue < fArGateTipHeight[iLocalAxisCount] 
						                                    && fCurrentValue > fArGateBottomHeight[iLocalAxisCount])
						{						
							bIsBlocked = true;
							break;
						}			
					}
					else
					{					
						iLocalStorageIndex = alContentSelection.get(iPolylineCount);
						fCurrentValue = alDataStorages.get(alStorageSelection.get(iLocalAxisCount)).getArrayFloat()[iLocalStorageIndex];
						if(fCurrentValue < fArGateTipHeight[iLocalAxisCount] 
						                                    && fCurrentValue > fArGateBottomHeight[iLocalAxisCount])
						{
							bIsBlocked = true;
							break;
						}						
					}							
				}
				if (!bIsBlocked)
				{
					if(bRenderArrayAsPolyline)
						polyLineSelectionManager.removeDeselection(alStorageSelection.get(iPolylineCount));
					else
						polyLineSelectionManager.removeDeselection(alContentSelection.get(iPolylineCount));
				}				
			}
		}		
	} 
	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#handleEvents(org.geneview.core.manager.view.EPickingType, org.geneview.core.manager.view.EPickingMode, int, org.geneview.core.manager.view.Pick)
	 */
	protected void handleEvents(final EPickingType ePickingType, 
			final EPickingMode ePickingMode, 
			final int iExternalID,
			final Pick pick)
	{
		switch (ePickingType)
		{
		case POLYLINE_SELECTION:
			switch (ePickingMode)
			{						
				case CLICKED:						
					polyLineSelectionManager.clearSelection();							
					polyLineSelectionManager.addSelection(iExternalID);
					
					if (ePolylineDataType == EInputDataTypes.GENES)
					{
						
						int iAccessionID = getAccesionIDFromStorageIndex(iExternalID);								
						
						infoAreaManager.setData(iAccessionID, 
								ePolylineDataType, pick.getPickedPoint());
						bRenderInfoArea = true;
						bInfoAreaFirstTime = true;								
						
						if (iAccessionID == -1)
							break;
						
						// Write currently selected vertex to selection set
						// and trigger update event
						int[] iArTmpSelectionId = new int[1];
						int[] iArTmpDepth = new int[1];
						iArTmpSelectionId[0] = iAccessionID;
						iArTmpDepth[0] = 0;
						alSetSelection.get(0).getWriteToken();
						alSetSelection.get(0).updateSelectionSet(iUniqueId, 
								iArTmpSelectionId, iArTmpDepth, new int[0]);
						alSetSelection.get(0).returnWriteToken();
					}
					bIsDisplayListDirtyLocal = true;
					bIsDisplayListDirtyRemote = true;
					break;	
				case MOUSE_OVER:

					polyLineSelectionManager.clearMouseOver();
					polyLineSelectionManager.addMouseOver(iExternalID);
					bIsDisplayListDirtyLocal = true;
					bIsDisplayListDirtyRemote = true;
					break;					
				default:
				
			}
			pickingManager.flushHits(iUniqueId, ePickingType);
			break;
			
		case X_AXIS_SELECTION:
			pickingManager.flushHits(iUniqueId, ePickingType);
			break;
		case Y_AXIS_SELECTION:
			pickingManager.flushHits(iUniqueId, ePickingType);
			break;
		case LOWER_GATE_TIP_SELECTION:
			switch (ePickingMode)
			{
			case CLICKED:						
				System.out.println("Gate Selected");
				//bIsDisplayListDirty = true;
				break;
			case DRAGGED:							
				bIsDraggingActive = true;
				draggedObject = EPickingType.LOWER_GATE_TIP_SELECTION;
				iDraggedGateNumber = iExternalID;
				break;
			}
			pickingManager.flushHits(iUniqueId, ePickingType);
			break;
		case LOWER_GATE_BOTTOM_SELECTION:
			switch (ePickingMode)
			{
				case CLICKED:						
					System.out.println("Gate Selected");
					//bIsDisplayListDirty = true;
					break;
				case DRAGGED:
					System.out.println("Gate Dragged");
				
				
					bIsDraggingActive = true;
					draggedObject = EPickingType.LOWER_GATE_BOTTOM_SELECTION;
					iDraggedGateNumber = iExternalID;
					break;
				default:
					// do nothing
			}
			pickingManager.flushHits(iUniqueId, ePickingType);
		case PC_ICON_SELECTION:	
			switch (ePickingMode)
			{						
				case CLICKED:	
					if(iExternalID == EIconIDs.TOGGLE_RENDER_ARRAY_AS_POLYLINE.ordinal())
					{							
						if (bRenderArrayAsPolyline == true)
							renderArrayAsPolyline(false);
						else
							renderArrayAsPolyline(true);
					}
					else if(iExternalID == EIconIDs.TOGGLE_PREVENT_OCCLUSION.ordinal())
					{
						if (bPreventOcclusion == true)
							preventOcclusion(false);
						else
							preventOcclusion(true);
					}
					else if(iExternalID == EIconIDs.TOGGLE_RENDER_SELECTION.ordinal())
					{
						if (bRenderSelection == true)
							renderSelection(false);
						else
							renderSelection(true);
					}
					else if(iExternalID == EIconIDs.RESET_SELECTIONS.ordinal())
					{
						resetSelections();
					}							
					
					bIsDisplayListDirtyLocal = true;
					bIsDisplayListDirtyRemote = true;
					break;
				default:
					// do nothing
			}
		
			pickingManager.flushHits(iUniqueId, EPickingType.PC_ICON_SELECTION);
			break;
		case REMOVE_AXIS:
			switch (ePickingMode)
			{						
				case CLICKED:	
					//int iSelection = 0;
					if(bRenderArrayAsPolyline)
					{
						alContentSelection.remove(iExternalID);	
					}
					else
					{
						alStorageSelection.remove(iExternalID);															
					}
					refresh();
					break;
				default:
					// do nothing
			}		
			pickingManager.flushHits(iUniqueId, EPickingType.REMOVE_AXIS);
			break;
		case MOVE_AXIS_LEFT:
			switch (ePickingMode)
			{						
				case CLICKED:	
					
					ArrayList<Integer> alSelection;
					if(bRenderArrayAsPolyline)							
						alSelection = alContentSelection;							
					else						
						alSelection = alStorageSelection;								
					
					if (iExternalID > 0 && iExternalID < alSelection.size())
					{
						int iTemp = alSelection.get(iExternalID - 1);
						alSelection.set(iExternalID - 1, alSelection.get(iExternalID));
						alSelection.set(iExternalID, iTemp);
						refresh();
					}

					break;
				default:
					// do nothing
			}
			pickingManager.flushHits(iUniqueId, EPickingType.MOVE_AXIS_LEFT);
			break;
		case MOVE_AXIS_RIGHT:	
				
			switch (ePickingMode)
			{						
				case CLICKED:	
					ArrayList<Integer> alSelection;
					if(bRenderArrayAsPolyline)							
						alSelection = alContentSelection;							
					else						
						alSelection = alStorageSelection;
					
					if (iExternalID >= 0 && iExternalID < alSelection.size()-1)
					{
						int iTemp = alSelection.get(iExternalID + 1);
						alSelection.set(iExternalID+1, alSelection.get(iExternalID));
						alSelection.set(iExternalID, iTemp);
						refresh();
					}						
					break;
				default:
					// do nothing
			}			
			pickingManager.flushHits(iUniqueId, EPickingType.MOVE_AXIS_RIGHT);
			break;
		case DUPLICATE_AXIS:			
			switch (ePickingMode)
			{						
				case CLICKED:	
					ArrayList<Integer> alSelection;
					if(bRenderArrayAsPolyline)							
						alSelection = alContentSelection;							
					else						
						alSelection = alStorageSelection;
					
					if (iExternalID >= 0 && iExternalID < alSelection.size()-1)
					{
						alSelection.add(iExternalID+1, alSelection.get(iExternalID));
						refresh();
					}						
					break;
				default:
					// do nothing
			}		
			pickingManager.flushHits(iUniqueId, EPickingType.DUPLICATE_AXIS);			
			break;			
		default:
			// do nothing		
		
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geneview.core.manager.event.mediator.IMediatorReceiver#updateReceiver(java.lang.Object,
	 *      org.geneview.core.data.collection.ISet)
	 */
	public void updateReceiver(Object eventTrigger, ISet updatedSet) 
	{
		
		generalManager.getSingelton().logMsg(
				this.getClass().getSimpleName()
						+ ": updateReceiver(Object eventTrigger, ISet updatedSet): Update called by "
						+ eventTrigger.getClass().getSimpleName(),
				LoggerType.VERBOSE);
		
		ISetSelection refSetSelection = (ISetSelection) updatedSet;

		refSetSelection.getReadToken();
		// contains all genes in center pathway (not yet)
		int[] iArSelection = refSetSelection.getSelectionIdArray();

		// contains type - 0 for not selected 1 for selected
		int[] iArGroup = refSetSelection.getGroupArray();
		// iterate here		
		int[] iArSelectionStorageIndices = convertAccessionToExpressionIndices(iArSelection);
		iArSelectionStorageIndices = cleanSelection(iArSelectionStorageIndices);
		setSelection(iArSelectionStorageIndices);
		
		int iSelectedAccessionID = 0;
		int iSelectedStorageIndex = 0;
		
		for(int iSelectionCount = 0; iSelectionCount < iArSelectionStorageIndices.length;  iSelectionCount++)
		{
			// TODO: set this to 1 resp. later to a enum as soon as I get real data
			if(iArGroup[iSelectionCount] == 0)
			{
				iSelectedAccessionID = iArSelection[iSelectionCount];
				iSelectedStorageIndex = iArSelectionStorageIndices[iSelectionCount];
				
				String sAccessionCode = generalManager.getSingelton().getGenomeIdManager()
					.getIdStringFromIntByMapping(iSelectedAccessionID, EGenomeMappingType.ACCESSION_2_ACCESSION_CODE);
			
				System.out.println("Accession Code: " +sAccessionCode);			
				System.out.println("Expression stroage index: " +iSelectedStorageIndex);
				
				if (iSelectedStorageIndex >= 0)
				{						
					if(!bRenderArrayAsPolyline)
					{
				
						//			if (alSelectedPolylines.contains(iExpressionStorageIndex))
						//			{
						polyLineSelectionManager.clearSelection();
						polyLineSelectionManager.addSelection(iSelectedStorageIndex);	
						bIsDisplayListDirtyLocal = true;
						bIsDisplayListDirtyRemote = true;
						//			}
							
						Iterator<ISet> iterSetData = alSetData.iterator();
						while (iterSetData.hasNext())
						{
							ISet tmpSet = iterSetData.next();
								
							if (tmpSet.getSetType().equals(SetType.SET_GENE_EXPRESSION_DATA))
							{
									alDataStorages.add(tmpSet.getStorageByDimAndIndex(0, 0));
							}
						}	
							
						float fYValue = alDataStorages.get(0).getArrayFloat()[iSelectedStorageIndex];
						
						generalManager.getSingelton().getViewGLCanvasManager().getSelectionManager()
							.modifySelection(iSelectedAccessionID, new SelectedElementRep(iUniqueId, 0.0f, fYValue), ESelectionMode.AddPick);
					}
					else
					{
						System.out.println("Highlighting for Axis not implemented yet");
						generalManager.getSingelton().getViewGLCanvasManager().getSelectionManager()
						.modifySelection(iSelectedAccessionID, new SelectedElementRep(iUniqueId, 0.0f, 0), ESelectionMode.AddPick);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#updateReceiver(java.lang.Object)
	 */
	public void updateReceiver(Object eventTrigger) {

		generalManager.getSingelton().logMsg(
				this.getClass().getSimpleName()
						+ ": updateReceiver(Object eventTrigger): Update called by "
						+ eventTrigger.getClass().getSimpleName(),
				LoggerType.VERBOSE);
	}
	
	protected int[] cleanSelection(int[] iArSelection)
	{
	
		for (int iCount = 0; iCount < iArSelection.length; iCount++)
		{
			if(iArSelection[iCount] == -1)
				continue;		
			
			iArSelection[iCount] = iArSelection[iCount] / 1000;	
			System.out.println("Storageindexalex: " + iArSelection[iCount]);
		}		
		
		return iArSelection;
		
	}
	
	protected void setSelection(int[] iArSelection)
	{
	
		alSetSelection.get(0).setSelectionIdArray(iArSelection);
		initPolyLineLists();
	}
	
	protected int[] convertAccessionToExpressionIndices(int[] iArSelection)
	{
		int[] iArSelectionStorageIndices = new int[iArSelection.length];
		
		for(int iCount = 0; iCount < iArSelection.length; iCount++)
		{
			iArSelectionStorageIndices[iCount] = generalManager.getSingelton().getGenomeIdManager()
				.getIdIntFromIntByMapping(iArSelection[iCount], EGenomeMappingType.ACCESSION_2_MICROARRAY_EXPRESSION);
		}		
		return iArSelectionStorageIndices;
	}
	
	private int getAccesionIDFromStorageIndex(int index)
	{
		int iAccessionID = IDManager.getIdIntFromIntByMapping(index*1000+770, 
				EGenomeMappingType.MICROARRAY_EXPRESSION_2_ACCESSION);
		return iAccessionID;
	}
	
	private String getAccessionNumberFromStorageIndex(int index)
	{
			
		// Convert expression storage ID to accession ID
		int iAccessionID = getAccesionIDFromStorageIndex(index);
		String sAccessionNumber = IDManager.getIdStringFromIntByMapping(iAccessionID, EGenomeMappingType.ACCESSION_2_ACCESSION_CODE);
		if(sAccessionNumber == "")
			return "Unkonwn Gene";
		else
			return sAccessionNumber;		
	}	
}
