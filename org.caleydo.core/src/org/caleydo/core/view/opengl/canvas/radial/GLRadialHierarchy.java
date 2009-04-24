package org.caleydo.core.view.opengl.canvas.radial;

import java.util.ArrayList;
import java.util.HashMap;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import org.caleydo.core.data.collection.ESetType;
import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.graph.tree.Tree;
import org.caleydo.core.data.selection.ESelectionType;
import org.caleydo.core.data.selection.EVAOperation;
import org.caleydo.core.manager.id.EManagedObjectType;
import org.caleydo.core.manager.picking.EPickingMode;
import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.manager.picking.Pick;
import org.caleydo.core.util.clusterer.ClusterNode;
import org.caleydo.core.view.opengl.camera.IViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLEventListener;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.remote.IGLCanvasRemoteRendering;
import org.caleydo.core.view.opengl.mouse.PickingMouseListener;
import org.caleydo.core.view.opengl.util.overlay.infoarea.GLInfoAreaManager;
import org.caleydo.core.view.serialize.ASerializedView;
import org.caleydo.core.view.serialize.SerializedDummyView;


/**
 * Rendering the GLHeatMap
 * 
 * @author Alexander Lex
 * @author Marc Streit
 */
public class GLRadialHierarchy
	extends AGLEventListener {

	public static final int DISP_HIER_DEPTH_DEFAULT = 3;

	private int iMaxDisplayedHierarchyDepth;

	private boolean bIsAnimationActive;

	private Tree<PartialDisc> partialDiscTree;
	private HashMap<Integer, PartialDisc> hashPartialDiscs;

	private PartialDisc pdRealRootElement;
	private PartialDisc pdCurrentRootElement;
	private PartialDisc pdCurrentSelectedElement;
	private PartialDisc pdCurrentMouseOverElement;

	private GLU glu;
	private DrawingController drawingController;

	boolean bIsInListMode = false;

	boolean bUseDetailLevel = true;
	ISet set;

	/**
	 * Constructor.
	 * 
	 * @param iViewID
	 * @param iGLCanvasID
	 * @param sLabel
	 * @param viewFrustum
	 */
	public GLRadialHierarchy(ESetType setType, final int iGLCanvasID, final String sLabel,
		final IViewFrustum viewFrustum) {
		super(iGLCanvasID, sLabel, viewFrustum, true);

		viewType = EManagedObjectType.GL_RADIAL_HIERARCHY;

		ArrayList<ESelectionType> alSelectionTypes = new ArrayList<ESelectionType>();
		alSelectionTypes.add(ESelectionType.NORMAL);
		alSelectionTypes.add(ESelectionType.MOUSE_OVER);
		alSelectionTypes.add(ESelectionType.SELECTION);

		hashPartialDiscs = new HashMap<Integer, PartialDisc>();
		partialDiscTree = new Tree<PartialDisc>();
		iMaxDisplayedHierarchyDepth = DISP_HIER_DEPTH_DEFAULT;
		drawingController = new DrawingController(this);
		glu = new GLU();
		bIsAnimationActive = false;
	}

	@Override
	public void init(GL gl) {
		initTestHierarchy();
		gl.glEnable(GL.GL_LINE_SMOOTH);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		if (set == null)
			return;
	}

	@Override
	public void initLocal(GL gl) {

		iGLDisplayListIndexLocal = gl.glGenLists(1);
		iGLDisplayListToCall = iGLDisplayListIndexLocal;
		init(gl);
	}

	@Override
	public void initRemote(final GL gl, final int iRemoteViewID,
		final PickingMouseListener pickingTriggerMouseAdapter,
		final IGLCanvasRemoteRendering remoteRenderingGLCanvas, GLInfoAreaManager infoAreaManager) {

		this.remoteRenderingGLCanvas = remoteRenderingGLCanvas;

		this.pickingTriggerMouseAdapter = pickingTriggerMouseAdapter;

		iGLDisplayListIndexRemote = gl.glGenLists(1);
		iGLDisplayListToCall = iGLDisplayListIndexRemote;
		init(gl);

	}

	private void initTestHierarchy() {

		iMaxDisplayedHierarchyDepth = DISP_HIER_DEPTH_DEFAULT;
		int childID = 0;
		ClusterNode currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		pdRealRootElement =
			new PartialDisc(0, 100, iUniqueID, pickingManager, partialDiscTree, currentClusterNode);
		pdCurrentRootElement = pdRealRootElement;
		hashPartialDiscs.put(0, pdRealRootElement);
		partialDiscTree.setRootNode(pdRealRootElement);

		ArrayList<PartialDisc> children = new ArrayList<PartialDisc>();
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		children.add(new PartialDisc(childID, 10, iUniqueID, pickingManager, partialDiscTree,
			currentClusterNode));
		hashPartialDiscs.put(childID, children.get(0));
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		children.add(new PartialDisc(childID, 40, iUniqueID, pickingManager, partialDiscTree,
			currentClusterNode));
		hashPartialDiscs.put(childID, children.get(1));
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		children.add(new PartialDisc(childID, 10, iUniqueID, pickingManager, partialDiscTree,
			currentClusterNode));
		hashPartialDiscs.put(childID, children.get(2));
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		children.add(new PartialDisc(childID, 15, iUniqueID, pickingManager, partialDiscTree,
			currentClusterNode));
		hashPartialDiscs.put(childID, children.get(3));
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		children.add(new PartialDisc(childID, 25, iUniqueID, pickingManager, partialDiscTree,
			currentClusterNode));
		hashPartialDiscs.put(childID, children.get(4));

		partialDiscTree.addChildren(pdCurrentRootElement, children);

		ArrayList<PartialDisc> ch1 = new ArrayList<PartialDisc>();
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		ch1.add(new PartialDisc(childID, 0.01f, iUniqueID, pickingManager, partialDiscTree,
			currentClusterNode));
		hashPartialDiscs.put(childID, ch1.get(0));
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		ch1
			.add(new PartialDisc(childID, 0.3f, iUniqueID, pickingManager, partialDiscTree,
				currentClusterNode));
		hashPartialDiscs.put(childID, ch1.get(1));
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		ch1
			.add(new PartialDisc(childID, 0.2f, iUniqueID, pickingManager, partialDiscTree,
				currentClusterNode));
		hashPartialDiscs.put(childID, ch1.get(2));
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		ch1.add(new PartialDisc(childID, 9.49f, iUniqueID, pickingManager, partialDiscTree,
			currentClusterNode));
		hashPartialDiscs.put(childID, ch1.get(3));

		partialDiscTree.addChildren(children.get(0), ch1);

		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		PartialDisc pdTemp = new PartialDisc(childID, 0.01f, iUniqueID, pickingManager,	partialDiscTree, currentClusterNode);
		partialDiscTree.addChild(ch1.get(0), pdTemp);
		hashPartialDiscs.put(childID, pdTemp);
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		pdTemp = new PartialDisc(childID, 0.3f, iUniqueID, pickingManager,
			partialDiscTree, currentClusterNode);
		partialDiscTree.addChild(ch1.get(1), pdTemp);
		hashPartialDiscs.put(childID, pdTemp);
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		pdTemp = new PartialDisc(childID, 0.2f, iUniqueID, pickingManager,
			partialDiscTree, currentClusterNode);
		partialDiscTree.addChild(ch1.get(2), pdTemp);
		hashPartialDiscs.put(childID, pdTemp);
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		pdTemp = new PartialDisc(childID, 9.49f, iUniqueID, pickingManager,
			partialDiscTree, currentClusterNode);
		partialDiscTree.addChild(ch1.get(3), pdTemp);
		hashPartialDiscs.put(childID, pdTemp);

		ArrayList<PartialDisc> ch2 = new ArrayList<PartialDisc>();
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		ch2.add(new PartialDisc(childID, 10, iUniqueID, pickingManager, partialDiscTree, currentClusterNode));
		hashPartialDiscs.put(childID, ch2.get(0));
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		ch2.add(new PartialDisc(childID, 5, iUniqueID, pickingManager, partialDiscTree, currentClusterNode));
		hashPartialDiscs.put(childID, ch2.get(1));
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		ch2.add(new PartialDisc(childID, 25, iUniqueID, pickingManager, partialDiscTree, currentClusterNode));
		hashPartialDiscs.put(childID, ch2.get(2));
		
		partialDiscTree.addChildren(children.get(1), ch2);

		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		PartialDisc ch = new PartialDisc(childID, 25, iUniqueID, pickingManager, partialDiscTree, currentClusterNode);
		partialDiscTree.addChild(ch2.get(2), ch);
		hashPartialDiscs.put(childID, ch);

		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		pdTemp = new PartialDisc(childID, 25, iUniqueID, pickingManager, partialDiscTree, currentClusterNode);
		partialDiscTree.addChild(ch, pdTemp);
		hashPartialDiscs.put(childID, pdTemp);

		ArrayList<PartialDisc> ch4 = new ArrayList<PartialDisc>();
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		ch4.add(new PartialDisc(childID, 10, iUniqueID, pickingManager, partialDiscTree, currentClusterNode));
		hashPartialDiscs.put(childID, ch4.get(0));
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		ch4.add(new PartialDisc(childID, 5, iUniqueID, pickingManager, partialDiscTree, currentClusterNode));
		hashPartialDiscs.put(childID, ch4.get(1));
		
		partialDiscTree.addChildren(children.get(3), ch4);

		ArrayList<PartialDisc> ch5 = new ArrayList<PartialDisc>();
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		ch5.add(new PartialDisc(childID, 10, iUniqueID, pickingManager, partialDiscTree, currentClusterNode));
		hashPartialDiscs.put(childID, ch5.get(0));
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		ch5.add(new PartialDisc(childID, 2, iUniqueID, pickingManager, partialDiscTree, currentClusterNode));
		hashPartialDiscs.put(childID, ch5.get(1));
		childID++;
		currentClusterNode = new ClusterNode("Node " + childID, childID, 0.5f, 1);
		ch5.add(new PartialDisc(childID, 13, iUniqueID, pickingManager, partialDiscTree, currentClusterNode));
		hashPartialDiscs.put(childID, ch5.get(2));
		
		partialDiscTree.addChildren(children.get(4), ch5);

	}

	@Override
	public synchronized void setDetailLevel(EDetailLevel detailLevel) {
		if (bUseDetailLevel) {
			super.setDetailLevel(detailLevel);
			// renderStyle.setDetailLevel(detailLevel);
		}

	}

	@Override
	public synchronized void displayLocal(GL gl) {
		pickingManager.handlePicking(iUniqueID, gl);

		if (bIsDisplayListDirtyLocal && !bIsAnimationActive) {
			buildDisplayList(gl, iGLDisplayListIndexLocal);
			bIsDisplayListDirtyLocal = false;
		}
		iGLDisplayListToCall = iGLDisplayListIndexLocal;

		display(gl);
		checkForHits(gl);

		if (eBusyModeState != EBusyModeState.OFF) {
			renderBusyMode(gl);
		}
	}

	@Override
	public synchronized void displayRemote(GL gl) {
		if (bIsDisplayListDirtyRemote && !bIsAnimationActive) {
			buildDisplayList(gl, iGLDisplayListIndexRemote);
			bIsDisplayListDirtyRemote = false;
		}
		iGLDisplayListToCall = iGLDisplayListIndexRemote;

		display(gl);
		checkForHits(gl);

		// pickingTriggerMouseAdapter.resetEvents();
	}

	@Override
	public synchronized void display(GL gl) {

		// render(gl);
		// clipToFrustum(gl);
		//
		if (bIsAnimationActive) {
			float fXCenter = viewFrustum.getWidth() / 2;
			float fYCenter = viewFrustum.getHeight() / 2;
			drawingController.draw(fXCenter, fYCenter, gl, glu);
		}
		else
			gl.glCallList(iGLDisplayListToCall);

		// buildDisplayList(gl, iGLDisplayListIndexRemote);
	}

	private void buildDisplayList(final GL gl, int iGLDisplayListIndex) {

		gl.glNewList(iGLDisplayListIndex, GL.GL_COMPILE);

		float fXCenter = viewFrustum.getWidth() / 2;
		float fYCenter = viewFrustum.getHeight() / 2;

		drawingController.draw(fXCenter, fYCenter, gl, glu);

		// TextRenderer renderer = new TextRenderer(new Font("Courier New", Font.PLAIN, 78), false);
		//		
		// renderer.setColor(0, 0, 0, 1);
		// renderer.begin3DRendering();
		// renderer.draw3D("Hello World!", 0, 0, 0, 0.003f);
		// renderer.end3DRendering();
		// renderer.flush();
		// Rectangle2D rect = renderer.getBounds("Hello World!");
		//		
		// gl.glColor4f(0,0,0,1);
		// gl.glBegin(GL.GL_POLYGON);
		//		
		//		
		// gl.glVertex3f((float)rect.getWidth() * 0.003f, 0, 0);
		// gl.glVertex3f((float)rect.getWidth() * 0.003f, -1, 0);
		// gl.glVertex3f(0, -1, 0);
		// gl.glVertex3f(0, 0, 0);
		// gl.glEnd();

		gl.glEndList();
	}

	private void render(GL gl) {

		// // gl.glDisable(GL.GL_DEPTH_TEST);
		// // gl.glEnable(GL.GL_BLEND);
		// // gl.glBlendFunc(GL.GL_SRC_ALPHA_SATURATE, GL.GL_ONE);
		//
		// zTransform += 1.0;
		// if (zTransform == 360)
		// zTransform = 0;
		// // gl.glOrtho(0.0, 1.0, 0.0, 1.0, -1.0, 1.0);
		// float[] array =
		// new float[] { 1.0f, 0.0f, -5.0f, 1.0f, 1.0f, -5.0f, 0.0f, 1.0f, -5.0f, 0.0f, 0.0f, -5.0f };
		// float[] array2 =
		// new float[] { -1.0f, 0.0f, -2.0f, -1.0f, -1.0f, -2.0f, 0.0f, -1.0f, -2.0f, 0.0f, 0.0f, -2.0f };
		// byte indices[] = { 0, 2, 3, 1 };
		// ByteBuffer indexBuffer = BufferUtil.newByteBuffer(indices.length);
		// indexBuffer.put(indices);
		// FloatBuffer verticesBuffer = BufferUtil.newFloatBuffer(array.length + array2.length);
		// // for(int i = 0; i < array.length; i++)
		// verticesBuffer.put(array);
		// verticesBuffer.put(array2);
		//
		// verticesBuffer.rewind();
		// indexBuffer.rewind();
		//
		// gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		// gl.glVertexPointer(3, GL.GL_FLOAT, 0, verticesBuffer);
		// // GLHelperFunctions.drawAxis(gl);
		//
		// int mode = 0;
		gl.glMatrixMode(GL.GL_MODELVIEW);

		// gl.glLoadIdentity();
		gl.glTranslatef(2.0f, 2.0f, 0.0f);
		// gl.glRotatef(zTransform, 0.0f, 0.0f, 1.0f);
		// glu.gluPartialDisk(x, 1 , 2, 3, 1, 30, 60);
		// gl.glColor4f(0, 1, 0, 0.8f);
		// glu.gluPartialDisk(x, 1 , 2, 3, 1, 110, 30);

		// glu.gluPartialDisk(x, 1 , 2, 10, 1, 180, 60);
		// gl.glPushName(pickingManager.getPickingID(iUniqueID, EPickingType.RADIAL_HIERARCHY_SELECTION,
		// 44));
		// if(selection == 44)
		// gl.glColor4f(1, 1, 0, 1);
		// else
		// gl.glColor4f(1, 0, 0, 0.5f);
		// GLPrimitives.renderPartialDisc(gl, 1, 2, 90, 90);
		//		
		// GLPrimitives.renderPartialDisc(gl, 1, 2, 90, 90);
		// gl.glPopName();
		//		
		// gl.glPushName(pickingManager.getPickingID(iUniqueID, EPickingType.RADIAL_HIERARCHY_SELECTION,
		// 11));
		// if(selection == 11)
		// gl.glColor4f(1, 1, 0, 1);
		// else
		// gl.glColor4f(1, 0, 0, 1);
		// GLPrimitives.renderPartialDisc(gl, 1, 2, 180, 90);
		// GLPrimitives.renderCircle(gl, 1);
		// gl.glPopName();
		//		
		// if(mode == 0)
		// {
		// // gl.glBegin(GL.GL_POLYGON);
		// // gl.glArrayElement(0);
		// // gl.glArrayElement(1);
		// // gl.glArrayElement(2);
		// // gl.glArrayElement(3);
		// // // gl.glVertex3f(5, 4, zTransform);
		// // // gl.glVertex3f(5, 5, zTransform);
		// // // gl.glVertex3f(4, 5, zTransform);
		// // // gl.glVertex3f(4, 4, zTransform);
		// // gl.glEnd();
		// // gl.glBegin(GL.GL_POLYGON);
		// // gl.glArrayElement(4);
		// // gl.glArrayElement(5);
		// // gl.glArrayElement(6);
		// // gl.glArrayElement(7);
		// // gl.glVertex3f(5, 4, zTransform);
		// // gl.glVertex3f(5, 5, zTransform);
		// // gl.glVertex3f(4, 5, zTransform);
		// // gl.glVertex3f(4, 4, zTransform);
		// gl.glEnd();
		// }
		// else if(mode == 1)
		// {
		// gl.glDrawElements(GL.GL_POLYGON, 4, GL.GL_UNSIGNED_BYTE, indexBuffer);
		// }
	}

	@Override
	public String getDetailedInfo() {
		return new String("");
	}

	@Override
	protected void handleEvents(EPickingType ePickingType, EPickingMode pickingMode, int iExternalID,
		Pick pick) {
		if (detailLevel == EDetailLevel.VERY_LOW) {
			pickingManager.flushHits(iUniqueID, ePickingType);
			return;
		}
		switch (ePickingType) {

			case RAD_HIERARCHY_PDISC_SELECTION:

				PartialDisc pdPickedElement = hashPartialDiscs.get(iExternalID);

				switch (pickingMode) {
					case CLICKED:
						if (pdPickedElement != null)
							drawingController.handleClick(pdPickedElement);
						break;

					case MOUSE_OVER:
						if (pdPickedElement != null)
							drawingController.handleMouseOver(pdPickedElement);
						break;

//					case RIGHT_CLICKED:
//						if (pdPickedElement != null)
//							drawingController.handleDoubleClick(pdPickedElement);
//						break;

					default:
						pickingManager.flushHits(iUniqueID, ePickingType);
						return;
				}
				break;
		}

		pickingManager.flushHits(iUniqueID, ePickingType);
	}

	public PartialDisc getRealRootElement() {
		return pdRealRootElement;
	}

	public void setRealRootElement(PartialDisc pdRealRootElement) {
		this.pdRealRootElement = pdRealRootElement;
	}

	public PartialDisc getCurrentRootElement() {
		return pdCurrentRootElement;
	}

	public void setCurrentRootElement(PartialDisc pdCurrentRootElement) {
		this.pdCurrentRootElement = pdCurrentRootElement;
	}

	public PartialDisc getCurrentSelectedElement() {
		return pdCurrentSelectedElement;
	}

	public void setCurrentSelectedElement(PartialDisc pdCurrentSelectedElement) {
		this.pdCurrentSelectedElement = pdCurrentSelectedElement;
	}

	public PartialDisc getCurrentMouseOverElement() {
		return pdCurrentMouseOverElement;
	}

	public void setCurrentMouseOverElement(PartialDisc pdCurrentMouseOverElement) {
		this.pdCurrentMouseOverElement = pdCurrentMouseOverElement;
	}

	public int getMaxDisplayedHierarchyDepth() {
		return iMaxDisplayedHierarchyDepth;
	}

	public void setMaxDisplayedHierarchyDepth(int iMaxDisplayedHierarchyDepth) {
		this.iMaxDisplayedHierarchyDepth = iMaxDisplayedHierarchyDepth;
	}

	public boolean isInListMode() {
		return bIsInListMode;
	}

	public void setAnimationActive(boolean bIsAnimationActive) {
		this.bIsAnimationActive = bIsAnimationActive;
	}

	@Override
	public void broadcastElements(EVAOperation type) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumberOfSelections(ESelectionType selectionType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getShortInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearAllSelections() {
		// TODO Auto-generated method stub

	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedDummyView serializedForm = new SerializedDummyView();
		serializedForm.setViewID(this.getID());
		return serializedForm; 
	}

}
