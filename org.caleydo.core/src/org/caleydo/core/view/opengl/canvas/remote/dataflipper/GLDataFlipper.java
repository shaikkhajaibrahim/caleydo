package org.caleydo.core.view.opengl.canvas.remote.dataflipper;

import gleem.RightTruncPyrMapping;
import gleem.linalg.Rotf;
import gleem.linalg.Vec3f;
import gleem.linalg.open.Transform;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import org.caleydo.core.command.ECommandType;
import org.caleydo.core.command.view.opengl.CmdCreateGLEventListener;
import org.caleydo.core.data.selection.ESelectionType;
import org.caleydo.core.data.selection.EVAOperation;
import org.caleydo.core.manager.ICommandManager;
import org.caleydo.core.manager.IEventPublisher;
import org.caleydo.core.manager.IUseCase;
import org.caleydo.core.manager.IViewManager;
import org.caleydo.core.manager.event.view.ViewActivationEvent;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.manager.id.EManagedObjectType;
import org.caleydo.core.manager.picking.EPickingMode;
import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.manager.picking.Pick;
import org.caleydo.core.manager.specialized.PathwayUseCase;
import org.caleydo.core.manager.specialized.TissueUseCase;
import org.caleydo.core.manager.usecase.EDataDomain;
import org.caleydo.core.manager.view.ConnectedElementRepresentationManager;
import org.caleydo.core.manager.view.RemoteRenderingTransformer;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.system.SystemTime;
import org.caleydo.core.util.system.Time;
import org.caleydo.core.view.opengl.camera.IViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLEventListener;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.GLCaleydoCanvas;
import org.caleydo.core.view.opengl.canvas.remote.AGLConnectionLineRenderer;
import org.caleydo.core.view.opengl.canvas.remote.GLRemoteRendering;
import org.caleydo.core.view.opengl.canvas.remote.IGLRemoteRenderingView;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.util.hierarchy.RemoteElementManager;
import org.caleydo.core.view.opengl.util.hierarchy.RemoteLevelElement;
import org.caleydo.core.view.opengl.util.overlay.infoarea.GLInfoAreaManager;
import org.caleydo.core.view.opengl.util.slerp.SlerpAction;
import org.caleydo.core.view.opengl.util.slerp.SlerpMod;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;
import org.caleydo.core.view.opengl.util.texture.TextureManager;
import org.eclipse.core.runtime.Status;

import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;

public class GLDataFlipper
	extends AGLEventListener
	implements IGLRemoteRenderingView {

	private static final int SLERP_RANGE = 1000;
	private static final int SLERP_SPEED = 1400;

	private static final int MAX_SIDE_VIEWS = 10;

	private ArrayList<ASerializedView> newViews;

	private ArrayList<AGLEventListener> containedGLViews;

	private RemoteLevelElement focusElement;
	private ArrayList<RemoteLevelElement> stackElementsLeft;
	private ArrayList<RemoteLevelElement> stackElementsRight;

	protected AGLConnectionLineRenderer glConnectionLineRenderer;

	/**
	 * Transformation utility object to transform and project view related coordinates
	 */
	protected RemoteRenderingTransformer selectionTransformer;

	private GLInfoAreaManager infoAreaManager;

	private TextRenderer textRenderer;

	private TextureManager textureManager;

	private ArrayList<SlerpAction> arSlerpActions;

	private Time time;

	private RemoteLevelElement lastPickedRemoteLevelElement;
	private int iLastPickedViewID;

	/**
	 * Slerp factor: 0 = source; 1 = destination
	 */
	private int iSlerpFactor = 0;

	private boolean focusZoom = false;

	/**
	 * Constructor.
	 */
	public GLDataFlipper(GLCaleydoCanvas glCanvas, final String sLabel, final IViewFrustum viewFrustum) {

		super(glCanvas, sLabel, viewFrustum, true);

		viewType = EManagedObjectType.GL_DATA_FLIPPER;

		// // Unregister standard mouse wheel listener
		// parentGLCanvas.removeMouseWheelListener(glMouseListener);
		// // Register specialized bucket mouse wheel listener
		// parentGLCanvas.addMouseWheelListener(bucketMouseWheelListener);
		// // parentGLCanvas.addMouseListener(bucketMouseWheelListener);

		textRenderer = new TextRenderer(new Font("Arial", Font.PLAIN, 24), false);
		textureManager = new TextureManager();
		arSlerpActions = new ArrayList<SlerpAction>();

		glMouseListener.addGLCanvas(this);

		newViews = new ArrayList<ASerializedView>();
		containedGLViews = new ArrayList<AGLEventListener>();
		stackElementsRight = new ArrayList<RemoteLevelElement>();
		stackElementsLeft = new ArrayList<RemoteLevelElement>();

		// TODO: Move to render style
		Transform transform = new Transform();
		transform.setTranslation(new Vec3f(-0.1f, 0.28f, 4));// -1.7f, -1.5f,
		// 4));
		transform.setScale(new Vec3f(1 / 2.5f, 1 / 2.5f, 1 / 2.5f));
		// transform.setTranslation(new Vec3f(-1.95f, -1.4f, 0));
		// transform.setScale(new Vec3f(1 / 1.15f, 1 / 1.15f, 1 / 1.1f));

		focusElement = new RemoteLevelElement(null);
		focusElement.setTransform(transform);
		RemoteElementManager.get().registerItem(focusElement);

		for (int iSideViewsIndex = 1; iSideViewsIndex <= MAX_SIDE_VIEWS; iSideViewsIndex++) {
			RemoteLevelElement newElement = new RemoteLevelElement(null);
			transform = new Transform();
			transform.setTranslation(new Vec3f(-2.1f - iSideViewsIndex / 1.8f + 1.5f, -1.25f + 1.5f, 4f));
			transform.setScale(new Vec3f(1 / 2.4f, 1 / 2.4f, 1 / 2.4f));
			transform.setRotation(new Rotf(new Vec3f(0, 1, 0), Vec3f.convertGrad2Radiant(96)));
			newElement.setTransform(transform);
			stackElementsLeft.add(newElement);
			RemoteElementManager.get().registerItem(newElement);

			newElement = new RemoteLevelElement(null);
			transform = new Transform();
			transform.setTranslation(new Vec3f(3.15f + iSideViewsIndex / 1.8f + 1.5f, -1.55f + 1.5f, -1f));
			transform.setScale(new Vec3f(1 / 1.95f, 1 / 1.95f, 1 / 2f));
			transform.setRotation(new Rotf(new Vec3f(0, -1, 0), Vec3f.convertGrad2Radiant(96)));
			newElement.setTransform(transform);
			stackElementsRight.add(newElement);
			RemoteElementManager.get().registerItem(newElement);
		}

		glConnectionLineRenderer =
			new GLConnectionLineRendererDataFlipper(focusElement, stackElementsLeft, stackElementsRight);

		// FIXME: remove when alex is ready with use case changes
		generalManager.addUseCase(new PathwayUseCase());
		generalManager.addUseCase(new TissueUseCase());
		generalManager.getUseCase(EDataDomain.PATHWAY_DATA).setSet(
			generalManager.getUseCase(EDataDomain.GENETIC_DATA).getSet());
	}

	@Override
	public void initLocal(final GL gl) {
		// iGLDisplayList = gl.glGenLists(1);

		ArrayList<RemoteLevelElement> remoteLevelElementWhiteList = new ArrayList<RemoteLevelElement>();
		remoteLevelElementWhiteList.add(focusElement);
		remoteLevelElementWhiteList.add(stackElementsLeft.get(0));
		remoteLevelElementWhiteList.add(stackElementsRight.get(0));
		selectionTransformer = new RemoteRenderingTransformer(iUniqueID, remoteLevelElementWhiteList);

		init(gl);
	}

	@Override
	public void initRemote(final GL gl, final AGLEventListener glParentView,
		final GLMouseListener glMouseListener, GLInfoAreaManager infoAreaManager) {

		throw new IllegalStateException("Not implemented to be rendered remote");
	}

	@Override
	public void init(final GL gl) {
		gl.glClearColor(0.5f, 0.5f, 0.5f, 1f);

		time = new SystemTime();
		((SystemTime) time).rebase();

		infoAreaManager = new GLInfoAreaManager();
		infoAreaManager.initInfoInPlace(viewFrustum);

		if (glConnectionLineRenderer != null) {
			glConnectionLineRenderer.init(gl);
		}

	}

	@Override
	public void displayLocal(final GL gl) {

		pickingManager.handlePicking(this, gl);

		display(gl);

		if (eBusyModeState != EBusyModeState.OFF) {
			renderBusyMode(gl);
		}

		checkForHits(gl);

		ConnectedElementRepresentationManager cerm =
			GeneralManager.get().getViewGLCanvasManager().getConnectedElementRepresentationManager();
		cerm.doViewRelatedTransformation(gl, selectionTransformer);
		
		// gl.glCallList(iGLDisplayListIndexLocal);
	}

	@Override
	public void displayRemote(final GL gl) {
		display(gl);
	}

	@Override
	public void display(final GL gl) {

		time.update();
		processEvents();

		// gl.glCallList(iGLDisplayList);

		doSlerpActions(gl);
		initNewView(gl);

		// if (focusZoom) {
		renderHandles(gl);
		renderDataViewIcons(gl, EDataDomain.CLINICAL_DATA);
		renderDataViewIcons(gl, EDataDomain.TISSUE_DATA);
		renderDataViewIcons(gl, EDataDomain.GENETIC_DATA);
		renderDataViewIcons(gl, EDataDomain.PATHWAY_DATA);

		for (RemoteLevelElement element : stackElementsLeft) {
			renderRemoteLevelElement(gl, element);
		}

		for (RemoteLevelElement element : stackElementsRight) {
			renderRemoteLevelElement(gl, element);
		}
		// }

		renderRemoteLevelElement(gl, focusElement);

		if (glConnectionLineRenderer != null && arSlerpActions.isEmpty()) {
			glConnectionLineRenderer.render(gl);
		}

		float fZTranslation = 0;
		fZTranslation = 4f;

		gl.glTranslatef(0, 0, fZTranslation);
		contextMenu.render(gl, this);
		gl.glTranslatef(0, 0, -fZTranslation);
	}

	private void renderRemoteLevelElement(final GL gl, RemoteLevelElement element) {

		if (element.getContainedElementID() == -1)
			return;

		int iViewID = element.getContainedElementID();

		gl.glPushName(pickingManager.getPickingID(iUniqueID, EPickingType.REMOTE_LEVEL_ELEMENT, element
			.getID()));
		gl.glPushName(pickingManager.getPickingID(iUniqueID, EPickingType.VIEW_SELECTION, iViewID));

		AGLEventListener glEventListener =
			generalManager.getViewGLCanvasManager().getGLEventListener(iViewID);

		if (glEventListener == null) {
			generalManager.getLogger().log(
				new Status(Status.WARNING, GeneralManager.PLUGIN_ID,
					"Remote level element is null and cannot be rendered!"));
			return;
		}

		gl.glPushMatrix();

		Transform transform = element.getTransform();
		Vec3f translation = transform.getTranslation();
		Rotf rot = transform.getRotation();
		Vec3f scale = transform.getScale();
		Vec3f axis = new Vec3f();
		float fAngle = rot.get(axis);

		if (glEventListener instanceof GLRemoteRendering) {

			gl.glTranslatef(translation.x() - 1.5f, translation.y() - 1.5f, translation.z());
			gl.glScalef(scale.x(), scale.y(), scale.z());
			renderBucketWall(gl, true);
			gl.glScalef(1 / scale.x(), 1 / scale.y(), 1 / scale.z());
			gl.glTranslatef(-translation.x() + 1.5f, -translation.y() + 1.5f, -translation.z());

			gl.glTranslatef(translation.x() + 0.14f, translation.y() - 0.09f, translation.z() + 2);
			gl.glRotatef(Vec3f.convertRadiant2Grad(fAngle), axis.x(), axis.y(), axis.z());
			gl.glScalef(scale.x(), scale.y(), scale.z());
		}
		else {
			gl.glTranslatef(translation.x() - 1.5f, translation.y() - 1.5f, translation.z());
			gl.glRotatef(Vec3f.convertRadiant2Grad(fAngle), axis.x(), axis.y(), axis.z());
			gl.glScalef(scale.x(), scale.y(), scale.z());

			renderBucketWall(gl, true);
		}

		glEventListener.displayRemote(gl);

		gl.glPopMatrix();

		gl.glPopName();
		gl.glPopName();
	}

	/**
	 * Adds new remote-rendered-views that have been queued for displaying to this view. Only one view is
	 * taken from the list and added for remote rendering per call to this method.
	 * 
	 * @param GL
	 */
	private void initNewView(GL gl) {

		// if(arSlerpActions.isEmpty())
		// {
		if (!newViews.isEmpty()) {
			ASerializedView serView = newViews.remove(0);
			AGLEventListener view = createView(gl, serView);

			// addSlerpActionForView(gl, view);

			// TODO: remove when activating slerp
			view.initRemote(gl, this, glMouseListener, infoAreaManager);
			// view.getViewFrustum().considerAspectRatio(true);

			containedGLViews.add(view);

			if (focusElement.isFree()) {
				focusElement.setContainedElementID(view.getID());
				view.setRemoteLevelElement(focusElement);
				view.setDetailLevel(EDetailLevel.HIGH);
			}
			else {

				if (newViews.size() % 2 == 0) {

					Iterator<RemoteLevelElement> iter = stackElementsLeft.iterator();
					while (iter.hasNext()) {
						RemoteLevelElement element = iter.next();
						if (element.isFree()) {
							element.setContainedElementID(view.getID());
							view.setRemoteLevelElement(element);
							view.setDetailLevel(EDetailLevel.LOW);
							break;
						}
					}
				}
				else {
					Iterator<RemoteLevelElement> iter = stackElementsRight.iterator();
					while (iter.hasNext()) {
						RemoteLevelElement element = iter.next();
						if (element.isFree()) {
							element.setContainedElementID(view.getID());
							view.setRemoteLevelElement(element);
							view.setDetailLevel(EDetailLevel.LOW);
							break;
						}
					}
				}
			}

			if (newViews.isEmpty()) {
				triggerToolBarUpdate();
				enableUserInteraction();
			}
		}
	}

	/**
	 * Triggers a toolbar update by sending an event similar to the view activation
	 * 
	 * @TODO: Move to remote rendering base class
	 */
	private void triggerToolBarUpdate() {

		ViewActivationEvent viewActivationEvent = new ViewActivationEvent();
		viewActivationEvent.setSender(this);
		List<AGLEventListener> views = getRemoteRenderedViews();

		List<Integer> viewIDs = new ArrayList<Integer>();
		viewIDs.add(getID());
		for (AGLEventListener view : views) {
			viewIDs.add(view.getID());
		}

		viewActivationEvent.setViewIDs(viewIDs);

		IEventPublisher eventPublisher = GeneralManager.get().getEventPublisher();
		eventPublisher.triggerEvent(viewActivationEvent);
	}

	@Override
	public List<AGLEventListener> getRemoteRenderedViews() {
		return containedGLViews;
	}

	@Override
	public void initFromSerializableRepresentation(ASerializedView ser) {
		// resetView(false);

		SerializedDataFlipperView serializedView = (SerializedDataFlipperView) ser;
		newViews.addAll(serializedView.getInitialContainedViews());

		setDisplayListDirty();
	}

	/**
	 * Creates and initializes a new view based on its serialized form. The view is already added to the list
	 * of event receivers and senders.
	 * 
	 * @param gl
	 * @param serView
	 *            serialized form of the view to create
	 * @return the created view ready to be used within the application
	 */
	private AGLEventListener createView(GL gl, ASerializedView serView) {

		ICommandManager commandManager = generalManager.getCommandManager();
		ECommandType cmdType = serView.getCreationCommandType();
		CmdCreateGLEventListener cmdView =
			(CmdCreateGLEventListener) commandManager.createCommandByType(cmdType);
		cmdView.setAttributesFromSerializedForm(serView);
		cmdView.doCommand();

		AGLEventListener glView = cmdView.getCreatedObject();
		glView.setRemoteRenderingGLView(this);

		return glView;
	}

	/**
	 * Disables picking and enables busy mode
	 */
	public void disableUserInteraction() {
		IViewManager canvasManager = generalManager.getViewGLCanvasManager();
		canvasManager.getPickingManager().enablePicking(false);
		canvasManager.requestBusyMode(this);
	}

	/**
	 * Enables picking and disables busy mode
	 */
	public void enableUserInteraction() {
		IViewManager canvasManager = generalManager.getViewGLCanvasManager();
		canvasManager.getPickingManager().enablePicking(true);
		canvasManager.releaseBusyMode(this);
	}

	private void doSlerpActions(final GL gl) {
		if (arSlerpActions.isEmpty())
			return;

		// SlerpAction tmpSlerpAction = arSlerpActions.get(0);

		if (iSlerpFactor == 0) {

			for (SlerpAction tmpSlerpAction : arSlerpActions) {
				tmpSlerpAction.start();
			}
		}

		if (iSlerpFactor < SLERP_RANGE) {
			// Makes animation rendering CPU speed independent
			iSlerpFactor += SLERP_SPEED * time.deltaT();

			if (iSlerpFactor > SLERP_RANGE) {
				iSlerpFactor = SLERP_RANGE;
			}
		}

		for (SlerpAction tmpSlerpAction : arSlerpActions) {
			slerpView(gl, tmpSlerpAction);
		}

		// Check if slerp action is finished
		if (iSlerpFactor >= SLERP_RANGE) {

			// // Finish in reverse order - otherwise the target ID would
			// overwrite the next
			// for (int iSlerpIndex = arSlerpActions.size() - 1; iSlerpIndex >=
			// 0; iSlerpIndex--) {
			// arSlerpActions.get(iSlerpIndex).finished();
			// }

			for (SlerpAction tmpSlerpAction : arSlerpActions) {
				tmpSlerpAction.finished();

				updateViewDetailLevels(tmpSlerpAction.getDestinationRemoteLevelElement());
			}

			arSlerpActions.clear();
			iSlerpFactor = 0;

			// Trigger chain move when selected view has not reached the focus
			// position
			if (iLastPickedViewID != focusElement.getContainedElementID())
				chainMove(lastPickedRemoteLevelElement);
		}
	}

	private void slerpView(final GL gl, SlerpAction slerpAction) {
		int iViewID = slerpAction.getElementId();

		if (iViewID == -1)
			return;

		SlerpMod slerpMod = new SlerpMod();

		if (iSlerpFactor == 0) {
			slerpMod.playSlerpSound();
		}

		Transform transform =
			slerpMod.interpolate(slerpAction.getOriginRemoteLevelElement().getTransform(), slerpAction
				.getDestinationRemoteLevelElement().getTransform(), (float) iSlerpFactor / SLERP_RANGE);

		gl.glPushMatrix();

		slerpMod.applySlerp(gl, transform, true, true);

		renderBucketWall(gl, true);
		generalManager.getViewGLCanvasManager().getGLEventListener(iViewID).displayRemote(gl);

		gl.glPopMatrix();

		// // Check if slerp action is finished
		// if (iSlerpFactor >= SLERP_RANGE) {
		// // arSlerpActions.remove(slerpAction);
		// arSlerpActions.removeAll();
		//
		// iSlerpFactor = 0;
		//			
		// slerpAction.finished();
		//
		// // RemoteLevelElement destinationElement =
		// slerpAction.getDestinationRemoteLevelElement();
		//
		// // updateViewDetailLevels(destinationElement);
		// // bUpdateOffScreenTextures = true;
		// }

		// // After last slerp action is done the line connections are turned on
		// // again
		// if (arSlerpActions.isEmpty()) {
		// if (glConnectionLineRenderer != null) {
		// glConnectionLineRenderer.enableRendering(true);
		// }
		//
		// generalManager.getViewGLCanvasManager().getInfoAreaManager().enable(!bEnableNavigationOverlay);
		// generalManager.getViewGLCanvasManager().getConnectedElementRepresentationManager().clearTransformedConnections();
		// }
	}

	@Override
	public void broadcastElements(EVAOperation type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearAllSelections() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDetailedInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfSelections(ESelectionType eSelectionType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getShortInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void handlePickingEvents(EPickingType pickingType, EPickingMode pickingMode, int iExternalID,
		Pick pick) {

		switch (pickingType) {

			case VIEW_SELECTION:
				switch (pickingMode) {
					case MOUSE_OVER:

						setDisplayListDirty();
						break;

					case CLICKED:
						break;
					case RIGHT_CLICKED:
						contextMenu.setLocation(pick.getPickedPoint(), getParentGLCanvas().getWidth(),
							getParentGLCanvas().getHeight());
						contextMenu.setMasterGLView(this);
						break;

				}
				// infoAreaManager.setData(iExternalID, EIDType.EXPRESSION_INDEX, pick.getPickedPoint(),
				// 0.3f);// pick.getDepth());
				break;

			case REMOTE_LEVEL_ELEMENT:
				switch (pickingMode) {
					case CLICKED:
						// Check if other slerp action is currently running
						if (iSlerpFactor > 0 && iSlerpFactor < SLERP_RANGE) {
							break;
						}

						// glConnectionLineRenderer.enableRendering(true);

						arSlerpActions.clear();
						lastPickedRemoteLevelElement = RemoteElementManager.get().getItem(iExternalID);
						iLastPickedViewID = lastPickedRemoteLevelElement.getContainedElementID();
						chainMove(lastPickedRemoteLevelElement);

						break;
					case MOUSE_OVER:

						break;
				}
				break;

			case BUCKET_DRAG_ICON_SELECTION:

				switch (pickingMode) {
					case CLICKED:

						break;
				}
		}
	}

	private void chainMove(RemoteLevelElement selectedElement) {
		// Chain slerping to the right
		if (stackElementsLeft.contains(selectedElement)) {

			for (int iElementIndex = stackElementsLeft.size(); iElementIndex >= 0; iElementIndex--) {

				if (iElementIndex < (MAX_SIDE_VIEWS - 1)) {
					arSlerpActions.add(new SlerpAction(stackElementsLeft.get(iElementIndex + 1),
						stackElementsLeft.get(iElementIndex)));
				}

				if (iElementIndex == 0) {
					arSlerpActions.add(new SlerpAction(stackElementsLeft.get(iElementIndex), focusElement));
				}
			}

			arSlerpActions.add(new SlerpAction(focusElement, stackElementsRight.get(0)));

			for (int iElementIndex = 0; iElementIndex < stackElementsRight.size(); iElementIndex++) {

				if (iElementIndex < (MAX_SIDE_VIEWS - 1)) {
					// if (!remoteLevelElementsRight.get(iElementIndex +
					// 1).isFree()) {
					arSlerpActions.add(new SlerpAction(stackElementsRight.get(iElementIndex),
						stackElementsRight.get(iElementIndex + 1)));
					// }
				}
			}
		}
		// Chain slerping to the left
		else if (stackElementsRight.contains(selectedElement)) {

			for (int iElementIndex = 0; iElementIndex < stackElementsRight.size(); iElementIndex++) {

				if (iElementIndex < (MAX_SIDE_VIEWS - 1)) {
					arSlerpActions.add(new SlerpAction(stackElementsRight.get(iElementIndex + 1),
						stackElementsRight.get(iElementIndex)));
				}

				if (iElementIndex == 0) {
					arSlerpActions.add(new SlerpAction(stackElementsRight.get(iElementIndex), focusElement));
				}
			}

			arSlerpActions.add(new SlerpAction(focusElement, stackElementsLeft.get(0)));

			for (int iElementIndex = 0; iElementIndex < stackElementsLeft.size(); iElementIndex++) {

				if (iElementIndex < (MAX_SIDE_VIEWS - 1)) {
					// if (!remoteLevelElementsLeft.get(iElementIndex +
					// 1).isFree()) {
					arSlerpActions.add(new SlerpAction(stackElementsLeft.get(iElementIndex),
						stackElementsLeft.get(iElementIndex + 1)));
					// }
				}
			}
		}
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedDataFlipperView serializedForm = new SerializedDataFlipperView(dataDomain);
		serializedForm.setViewID(this.getID());

		// IViewManager viewManager = generalManager.getViewGLCanvasManager();

		// ArrayList<ASerializedView> remoteViews =
		// new ArrayList<ASerializedView>(focusLevel.getAllElements().size());
		// for (RemoteLevelElement rle : focusLevel.getAllElements()) {
		// if (rle.getContainedElementID() != -1) {
		// AGLEventListener remoteView = viewManager.getGLEventListener(rle.getContainedElementID());
		// remoteViews.add(remoteView.getSerializableRepresentation());
		// }
		// }
		// serializedForm.setFocusViews(remoteViews);
		//
		// remoteViews = new ArrayList<ASerializedView>(stackLevel.getAllElements().size());
		// for (RemoteLevelElement rle : stackLevel.getAllElements()) {
		// if (rle.getContainedElementID() != -1) {
		// AGLEventListener remoteView = viewManager.getGLEventListener(rle.getContainedElementID());
		// remoteViews.add(remoteView.getSerializableRepresentation());
		// }
		// }
		// serializedForm.setStackViews(remoteViews);

		return serializedForm;
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		super.reshape(drawable, x, y, width, height);

		AGLEventListener glView =
			generalManager.getViewGLCanvasManager().getGLEventListener(focusElement.getContainedElementID());

		if (glView == null)
			return;

		// IViewFrustum frustum = glView.getViewFrustum();
		// frustum.setTop(8*fAspectRatio);
		// glView.reshape(drawable, x, y, width, height);
	}

	private void renderDataViewIcons(final GL gl, EDataDomain dataDomain) {

		IUseCase useCase = GeneralManager.get().getUseCase(dataDomain);
		ArrayList<EManagedObjectType> possibleViews = useCase.getPossibleViews();

		EIconTextures dataIcon = null;
		float fXPos = 0.5f;

		if (dataDomain == EDataDomain.CLINICAL_DATA) {
			dataIcon = EIconTextures.DATA_FLIPPER_DATA_ICON_PATIENT;
			fXPos += -3f;
		}
		else if (dataDomain == EDataDomain.TISSUE_DATA) {
			dataIcon = EIconTextures.DATA_FLIPPER_DATA_ICON_TISSUE;
			fXPos += -1.5f;
		}
		else if (dataDomain == EDataDomain.GENETIC_DATA) {
			dataIcon = EIconTextures.DATA_FLIPPER_DATA_ICON_GENE_EXPRESSION;
			fXPos += -0f;
		}
		else if (dataDomain == EDataDomain.PATHWAY_DATA) {
			dataIcon = EIconTextures.DATA_FLIPPER_DATA_ICON_PATHWAY;
			fXPos += 1.5f;
		}

		float fViewIconWidth = 0.12f;
		gl.glTranslatef(fXPos, -2.07f, 4);

		// Data background
		textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_DATA_ICON_BACKGROUND, new Vec3f(0, 0, 0),
			new Vec3f(0.51f, 0, 0), new Vec3f(0.51f, 0.3f, 0), new Vec3f(0, 0.3f, 0), 1, 1, 1, 1);

		gl.glTranslatef(0, 0.31f, 0);

		// First view background
		textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_VIEW_ICON_BACKGROUND_ROUNDED, new Vec3f(
			fViewIconWidth, 0.0f, 0), new Vec3f(0.0f, 0.0f, 0), new Vec3f(0.0f, fViewIconWidth, 0),
			new Vec3f(fViewIconWidth, fViewIconWidth, 0), 1, 1, 1, 1);

		gl.glTranslatef(fViewIconWidth + 0.01f, 0, 0);

		// Second view background
		textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_VIEW_ICON_BACKGROUND_SQUARE, new Vec3f(
			fViewIconWidth, 0.0f, 0), new Vec3f(0.0f, 0.0f, 0), new Vec3f(0.0f, fViewIconWidth, 0),
			new Vec3f(fViewIconWidth, fViewIconWidth, 0), 1, 1, 1, 1);

		gl.glTranslatef(fViewIconWidth + 0.01f, 0, 0);

		// Third view background
		textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_VIEW_ICON_BACKGROUND_SQUARE, new Vec3f(
			fViewIconWidth, 0.0f, 0), new Vec3f(0.0f, 0.0f, 0), new Vec3f(0.0f, fViewIconWidth, 0),
			new Vec3f(fViewIconWidth, fViewIconWidth, 0), 1, 1, 1, 1);

		gl.glTranslatef(fViewIconWidth + 0.01f, 0, 0);

		// Forth view background
		textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_VIEW_ICON_BACKGROUND_ROUNDED, new Vec3f(
			0, 0.0f, 0), new Vec3f(fViewIconWidth, 0.0f, 0), new Vec3f(fViewIconWidth, fViewIconWidth, 0),
			new Vec3f(0, fViewIconWidth, 0), 1, 1, 1, 1);
		gl.glTranslatef(-3 * fViewIconWidth - 0.03f, -0.31f, 0);

		for (int iViewIndex = 0; iViewIndex < possibleViews.size(); iViewIndex++) {

			EIconTextures iconTextureType;
			EManagedObjectType viewType = possibleViews.get(iViewIndex);
			switch (viewType) {
				case GL_HIER_HEAT_MAP:
					iconTextureType = EIconTextures.HEAT_MAP_ICON;
					break;
				case GL_PARALLEL_COORDINATES:
					iconTextureType = EIconTextures.PAR_COORDS_ICON;
					break;
				case GL_GLYPH:
					iconTextureType = EIconTextures.GLYPH_ICON;
					break;
				case GL_PATHWAY:
				case GL_PATHWAY_VIEW_BROWSER:
					iconTextureType = EIconTextures.PATHWAY_ICON;
					break;
				case GL_TISSUE:
				case GL_TISSUE_VIEW_BROWSER:
					iconTextureType = EIconTextures.TISSUE_SAMPLE;
					break;
				default:
					iconTextureType = EIconTextures.LOCK;
					break;
			}

			RemoteLevelElement element = findElementContainingView(dataDomain, viewType);

			if (element != null && arSlerpActions.isEmpty()) {

				float fHorizontalConnStart = 0;
				float fHorizontalConnStop = 0;
				float fHorizontalConnHeight = 0;
				float fPipeWidth = 0.05f;

				// gl.glTranslatef(fXPos, -2.6f, 3);
				Transform transform = element.getTransform();
				Vec3f translation = transform.getTranslation();

				// if (element == focusElement) {
				//				
				// textureManager.renderTexture(gl,
				// EIconTextures.DATA_FLIPPER_CONNECTION_STRAIGHT,
				// new Vec3f(0.05f, 0.43f, 0.0f), new Vec3f(fPipeWidth, 0.43f,
				// 0.0f), new Vec3f(fPipeWidth,
				// 0.85f, 0.0f), new Vec3f(0.05f, 0.85f, 0.0f), 1, 1, 1, 1);
				// }
				// else
				if (element == stackElementsLeft.get(0)) {
					// // LEFT first
					gl.glTranslatef(-fXPos - 1.56f + translation.x(), 0.47f + translation.y(), translation
						.z() * 0);
					textureManager
						.renderTexture(gl, EIconTextures.DATA_FLIPPER_CONNECTION_STRAIGHT, new Vec3f(0.0f,
							0.0f + fPipeWidth, 0.0f), new Vec3f(fPipeWidth, 0.0f + fPipeWidth, 0.0f),
							new Vec3f(fPipeWidth, 0.1f, 0.0f), new Vec3f(0.0f, 0.1f, 0.0f), 1, 1, 1, 1);
					gl.glTranslatef(0, -0.2f, 0);

					gl.glTranslatef(fXPos + 1.56f - translation.x(), -0.47f - translation.y() + 0.2f,
						-translation.z() * 0);
					//					
					fHorizontalConnStart = -fXPos + translation.x() - 1.56f + fPipeWidth;
					fHorizontalConnHeight = 0.67f;
				}
				else if (element == stackElementsLeft.get(1)) {
					// // LEFT second
					gl.glTranslatef(-fXPos - 1.53f + translation.x(), 0.34f + translation.y(), translation
						.z() * 0);
					textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_CONNECTION_STRAIGHT,
						new Vec3f(0.0f, 0.0f + fPipeWidth, 0.0f), new Vec3f(fPipeWidth, 0.0f + fPipeWidth,
							0.0f), new Vec3f(fPipeWidth, 0.23f, 0.0f), new Vec3f(0.0f, 0.23f, 0.0f), 1, 1, 1,
						1);
					gl.glTranslatef(0, -0.2f, 0);

					gl.glTranslatef(fXPos + 1.53f - translation.x(), -0.34f - translation.y() + 0.2f,
						-translation.z() * 0);
					//				
					fHorizontalConnStart = -fXPos + translation.x() - 1.53f + fPipeWidth;
					fHorizontalConnHeight = 0.54f;
				}
				else if (element == stackElementsRight.get(0)) {
					// RIGHT first
					gl.glTranslatef(-fXPos - 2.53f + translation.x(), 0.76f + translation.y(), translation
						.z() * 0);
					textureManager
						.renderTexture(gl, EIconTextures.DATA_FLIPPER_CONNECTION_STRAIGHT, new Vec3f(0.0f,
							0.0f + fPipeWidth, 0.0f), new Vec3f(fPipeWidth, 0.0f + fPipeWidth, 0.0f),
							new Vec3f(fPipeWidth, 0.1f, 0.0f), new Vec3f(0.0f, 0.1f, 0.0f), 1, 1, 1, 1);
					gl.glTranslatef(0, -0.2f, 0);

					gl.glTranslatef(fXPos + 2.53f - translation.x(), -0.76f - translation.y() + 0.2f,
						-translation.z() * 0);
					//				
					fHorizontalConnStart = -fXPos + translation.x() - 2.53f;
					fHorizontalConnHeight = 0.67f;
				}
				else if (element == stackElementsRight.get(1)) {
					// RIGHT second
					gl.glTranslatef(-fXPos - 2.63f + translation.x(), 0.64f + translation.y(), translation
						.z() * 0);
					textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_CONNECTION_STRAIGHT,
						new Vec3f(0.0f, 0.0f + fPipeWidth, 0.0f), new Vec3f(fPipeWidth, 0.0f + fPipeWidth,
							0.0f), new Vec3f(fPipeWidth, 0.23f, 0.0f), new Vec3f(0.0f, 0.23f, 0.0f), 1, 1, 1,
						1);
					gl.glTranslatef(0, -0.2f, 0);

					gl.glTranslatef(fXPos + 2.63f - translation.x(), -0.64f - translation.y() + 0.2f,
						-translation.z() * 0);
					//				
					fHorizontalConnStart = -fXPos + translation.x() - 2.63f;
					fHorizontalConnHeight = 0.54f;
				}

				if (element == focusElement
					|| (stackElementsLeft.contains(element) && stackElementsLeft.indexOf(element) < 2)
					|| (stackElementsRight.contains(element) && stackElementsRight.indexOf(element) < 2)) {
					float fPipeHeight = 0.11f;

					if (fHorizontalConnHeight > 0.6)
						fPipeHeight = 0.24f;

					switch (iViewIndex) {
						case 0:

							if (element == focusElement) {

								textureManager.renderTexture(gl,
									EIconTextures.DATA_FLIPPER_CONNECTION_STRAIGHT, new Vec3f(0.04f, 0.43f,
										0.0f), new Vec3f(0.04f + fPipeWidth, 0.43f, 0.0f), new Vec3f(
										0.04f + fPipeWidth, 0.85f, 0.0f), new Vec3f(0.04f, 0.85f, 0.0f), 1,
									1, 1, 1);

								// Special case when focus element is last view on the right stack
								if (stackElementsRight.get(0).isFree()) {
									textureManager.renderTexture(gl,
										EIconTextures.DATA_FLIPPER_CONNECTION_CORNER, new Vec3f(
											0.04f - fPipeWidth, 0.85f, 0.0f), new Vec3f(0.04f + fPipeWidth,
											0.85f, 0.0f), new Vec3f(0.04f + fPipeWidth,
											0.85f + fPipeWidth + 0.05f, 0.0f), new Vec3f(0.04f - fPipeWidth,
											0.85f + fPipeWidth + 0.05f, 0.0f), 1, 1, 1, 1);

									textureManager.renderTexture(gl,
										EIconTextures.DATA_FLIPPER_CONNECTION_STRAIGHT, new Vec3f(
											fHorizontalConnStart - 0.4f, 0.85f + fPipeWidth, 0.0f),
										new Vec3f(0.04f - fPipeWidth, 0.85f + fPipeWidth, 0.0f), new Vec3f(
											0.04f - fPipeWidth, 0.85f + fPipeWidth + 0.05f, 0.0f), new Vec3f(
											fHorizontalConnStart - 0.4f, 0.85f + fPipeWidth + 0.05f, 0.0f),
										1, 1, 1, 1);
								}
								// Special case when focus element is last view on the right stack
								else if (stackElementsLeft.get(0).isFree()) {
									textureManager.renderTexture(gl,
										EIconTextures.DATA_FLIPPER_CONNECTION_CORNER, new Vec3f(
											0.04f + 2*fPipeWidth, 0.85f, 0.0f), new Vec3f(0.04f,
											0.85f, 0.0f), new Vec3f(0.04f,
											0.85f + fPipeWidth + 0.05f, 0.0f), new Vec3f(0.04f + 2*fPipeWidth,
											0.85f + fPipeWidth + 0.05f, 0.0f), 1, 1, 1, 1);

									textureManager.renderTexture(gl,
										EIconTextures.DATA_FLIPPER_CONNECTION_STRAIGHT, new Vec3f(
											fHorizontalConnStart + 0.9f, 0.85f + fPipeWidth, 0.0f),
										new Vec3f(0.04f + 2*fPipeWidth, 0.85f + fPipeWidth, 0.0f), new Vec3f(
											0.04f + 2*fPipeWidth, 0.85f + fPipeWidth + 0.05f, 0.0f), new Vec3f(
											fHorizontalConnStart + 0.9f, 0.85f + fPipeWidth + 0.05f, 0.0f),
										1, 1, 1, 1);
								}
							}
							else {
								gl.glTranslatef(0.032f, 0.43f, 0);
								textureManager.renderTexture(gl,
									EIconTextures.DATA_FLIPPER_CONNECTION_STRAIGHT, new Vec3f(0.0f, 0.0f,
										0.0f), new Vec3f(fPipeWidth, 0.0f, 0.0f), new Vec3f(fPipeWidth,
										fPipeHeight - fPipeWidth, 0.0f), new Vec3f(0.0f, fPipeHeight
										- fPipeWidth, 0.0f), 1, 1, 1, 1);
								gl.glTranslatef(-0.032f, -0.43f, 0);

								if (stackElementsLeft.contains(element))
									fHorizontalConnStop = 0.03f;
								else
									fHorizontalConnStop = 0.08f;
							}
							break;
						case 1:
							if (element == focusElement) {

								// textureManager.renderTexture(gl,
								// EIconTextures.DATA_FLIPPER_CONNECTION_STRAIGHT, new Vec3f(0.16f, 0.43f,
								// 0.0f), new Vec3f(0.16f + fPipeWidth, 0.43f, 0.0f), new Vec3f(
								// 0.16f + fPipeWidth, 0.85f, 0.0f), new Vec3f(0.16f, 0.85f, 0.0f), 1,
								// 1, 1, 1);
							}
							else {
								gl.glTranslatef(0.17f, 0.43f, 0);
								textureManager.renderTexture(gl,
									EIconTextures.DATA_FLIPPER_CONNECTION_STRAIGHT, new Vec3f(0.0f, 0.0f,
										0.0f), new Vec3f(fPipeWidth, 0.0f, 0.0f), new Vec3f(fPipeWidth,
										fPipeHeight - fPipeWidth, 0.0f), new Vec3f(0.0f, fPipeHeight
										- fPipeWidth, 0.0f), 1, 1, 1, 1);

								gl.glTranslatef(-0.17f, -0.43f, 0);

								if (stackElementsLeft.contains(element))
									fHorizontalConnStop = 0.17f;
								else
									fHorizontalConnStop = 0.22f;
							}
							break;
						case 2:
							// TODO
							break;
						case 3:
							// TODO
							break;
					}

					if (element != focusElement) {
						if (fHorizontalConnStart < fHorizontalConnStop) {
							textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_CONNECTION_STRAIGHT,
								new Vec3f(fHorizontalConnStart + fPipeWidth, fHorizontalConnHeight, 0.0f),
								new Vec3f(fHorizontalConnStop - fPipeWidth, fHorizontalConnHeight, 0.0f),
								new Vec3f(fHorizontalConnStop - fPipeWidth, fHorizontalConnHeight + 0.05f,
									0.0f), new Vec3f(fHorizontalConnStart + fPipeWidth,
									fHorizontalConnHeight + 0.05f, 0.0f), 1, 1, 1, 1);
						}
						else {
							textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_CONNECTION_STRAIGHT,
								new Vec3f(fHorizontalConnStart - fPipeWidth, fHorizontalConnHeight, 0.0f),
								new Vec3f(fHorizontalConnStop + fPipeWidth, fHorizontalConnHeight, 0.0f),
								new Vec3f(fHorizontalConnStop + fPipeWidth, fHorizontalConnHeight + 0.05f,
									0.0f), new Vec3f(fHorizontalConnStart - fPipeWidth,
									fHorizontalConnHeight + 0.05f, 0.0f), 1, 1, 1, 1);
						}

						// ROUND CORNERS near views
						if (fHorizontalConnStart > fHorizontalConnStop) {
							textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_CONNECTION_CORNER,
								new Vec3f(fHorizontalConnStart - fPipeWidth, fHorizontalConnHeight
									+ fPipeWidth + 0.05f, 0.0f), new Vec3f(fHorizontalConnStart + fPipeWidth,
									fHorizontalConnHeight + fPipeWidth + 0.05f, 0.0f), new Vec3f(
									fHorizontalConnStart + fPipeWidth, fHorizontalConnHeight, 0.0f),
								new Vec3f(fHorizontalConnStart - fPipeWidth, fHorizontalConnHeight, 0.0f), 1,
								1, 1, 1);
						}
						else {
							textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_CONNECTION_CORNER,
								new Vec3f(fHorizontalConnStart + fPipeWidth, fHorizontalConnHeight
									+ fPipeWidth + 0.05f, 0.0f), new Vec3f(fHorizontalConnStart - fPipeWidth,
									fHorizontalConnHeight + fPipeWidth + 0.05f, 0.0f), new Vec3f(
									fHorizontalConnStart - fPipeWidth, fHorizontalConnHeight, 0.0f),
								new Vec3f(fHorizontalConnStart + fPipeWidth, fHorizontalConnHeight, 0.0f), 1,
								1, 1, 1);
						}

						// ROUND CORNERS near data sets
						if (fHorizontalConnStart > fHorizontalConnStop) {
							textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_CONNECTION_CORNER,
								new Vec3f(fHorizontalConnStop + fPipeWidth, fHorizontalConnHeight
									- fPipeWidth, 0.0f), new Vec3f(fHorizontalConnStop - fPipeWidth,
									fHorizontalConnHeight - fPipeWidth, 0.0f), new Vec3f(fHorizontalConnStop
									- fPipeWidth, fHorizontalConnHeight + 0.05f, 0.0f), new Vec3f(
									fHorizontalConnStop + fPipeWidth, fHorizontalConnHeight + 0.05f, 0.0f),
								1, 1, 1, 1);
						}
						else {
							textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_CONNECTION_CORNER,
								new Vec3f(fHorizontalConnStop - fPipeWidth, fHorizontalConnHeight
									- fPipeWidth, 0.0f), new Vec3f(fHorizontalConnStop + fPipeWidth,
									fHorizontalConnHeight - fPipeWidth, 0.0f), new Vec3f(fHorizontalConnStop
									+ fPipeWidth, fHorizontalConnHeight + 0.05f, 0.0f), new Vec3f(
									fHorizontalConnStop - fPipeWidth, fHorizontalConnHeight + 0.05f, 0.0f),
								1, 1, 1, 1);
						}
					}
				}
			}

			float fIconBackgroundGray = 1;
			if (element == null)
				fIconBackgroundGray = 0.6f;

			if (element != null)
				gl.glPushName(pickingManager.getPickingID(iUniqueID, EPickingType.REMOTE_LEVEL_ELEMENT,
					element.getID()));

			float fIconPadding = 0.015f;
			gl.glTranslatef(0, 0, 0.001f);
			switch (iViewIndex) {
				case 0:
					// Data icon
					textureManager.renderTexture(gl, dataIcon, new Vec3f(0f, 0.02f, 0.01f), new Vec3f(0.5f,
						0.02f, 0.01f), new Vec3f(0.5f, 0.28f, 0.01f), new Vec3f(0.0f, 0.28f, 0.01f), 1, 1, 1,
						1);

					// First view icon
					gl.glTranslatef(0, 0.31f, 0);
					textureManager.renderTexture(gl, iconTextureType, new Vec3f(
						fViewIconWidth - fIconPadding, fIconPadding, 0), new Vec3f(fIconPadding,
						fIconPadding, 0), new Vec3f(fIconPadding, fViewIconWidth - fIconPadding, 0),
						new Vec3f(fViewIconWidth - fIconPadding, fViewIconWidth - fIconPadding, 0),
						fIconBackgroundGray, fIconBackgroundGray, fIconBackgroundGray, 1);
					gl.glTranslatef(0, -0.31f, 0);

					break;
				case 1:
					// Second view icon
					gl.glTranslatef(0.13f, 0.31f, 0);
					textureManager.renderTexture(gl, iconTextureType, new Vec3f(
						fViewIconWidth - fIconPadding, fIconPadding, 0), new Vec3f(fIconPadding,
						fIconPadding, 0), new Vec3f(fIconPadding, fViewIconWidth - fIconPadding, 0),
						new Vec3f(fViewIconWidth - fIconPadding, fViewIconWidth - fIconPadding, 0),
						fIconBackgroundGray, fIconBackgroundGray, fIconBackgroundGray, 1);
					gl.glTranslatef(-0.13f, -0.31f, 0);
					break;
				case 2:
					// Third view icon
					gl.glTranslatef(0.26f, 0.31f, 0);
					textureManager.renderTexture(gl, iconTextureType, new Vec3f(
						fViewIconWidth - fIconPadding, fIconPadding, 0), new Vec3f(fIconPadding,
						fIconPadding, 0), new Vec3f(fIconPadding, fViewIconWidth - fIconPadding, 0),
						new Vec3f(fViewIconWidth - fIconPadding, fViewIconWidth - fIconPadding, 0),
						fIconBackgroundGray, fIconBackgroundGray, fIconBackgroundGray, 1);
					gl.glTranslatef(-0.26f, -0.31f, 0);
					break;
				case 3:
					// Forth view icon
					gl.glTranslatef(0.39f, 0.31f, 0);
					textureManager.renderTexture(gl, iconTextureType, new Vec3f(
						fViewIconWidth - fIconPadding, fIconPadding, 0), new Vec3f(fIconPadding,
						fIconPadding, 0), new Vec3f(fIconPadding, fViewIconWidth - fIconPadding, 0),
						new Vec3f(fViewIconWidth - fIconPadding, fViewIconWidth - fIconPadding, 0),
						fIconBackgroundGray, fIconBackgroundGray, fIconBackgroundGray, 1);
					gl.glTranslatef(-0.39f, -0.31f, 0);
					break;
			}

			if (element != null)
				gl.glPopName();
			gl.glTranslatef(0, 0, -0.001f);

		}
		gl.glTranslatef(-fXPos, 2.07f, -4);
	}

	private RemoteLevelElement findElementContainingView(EDataDomain dataDomain, EManagedObjectType viewType) {

		for (AGLEventListener glView : containedGLViews) {
			if (glView.getViewType() == viewType && glView.getDataDomain() == dataDomain) {
				if (focusElement.getContainedElementID() == glView.getID())
					return focusElement;

				for (RemoteLevelElement element : stackElementsLeft) {
					if (element.getContainedElementID() == glView.getID())
						return element;
				}

				for (RemoteLevelElement element : stackElementsRight) {
					if (element.getContainedElementID() == glView.getID())
						return element;
				}
			}
		}

		return null;
	}

	// FIXME: method copied from bucket
	private void renderHandles(final GL gl) {

		// Bucket center (focus)
		RemoteLevelElement element = focusElement;
		if (element.getContainedElementID() != -1) {

			Transform transform;
			Vec3f translation;
			// Vec3f scale;

			float fYCorrection = 0f;

			transform = element.getTransform();
			translation = transform.getTranslation();
			// scale = transform.getScale();

			gl.glTranslatef(translation.x() - 1.5f, translation.y() - 0.225f - 0.075f + fYCorrection,
				translation.z() + 0.001f);

			// gl.glScalef(scale.x() * 4, scale.y() * 4, 1);
			renderNavigationHandleBar(gl, element, 3.2f, 0.075f, false, 2);
			// gl.glScalef(1 / (scale.x() * 4), 1 / (scale.y() * 4), 1);

			gl.glTranslatef(-translation.x() + 1.5f, -translation.y() + 0.225f + 0.075f - fYCorrection,
				-translation.z() - 0.001f);
		}

		// Left first
		element = stackElementsLeft.get(0);
		if (element.getContainedElementID() != -1) {

			gl.glTranslatef(-0.64f, -1.25f, 4.02f);
			gl.glRotatef(90, 0, 0, 1);
			renderNavigationHandleBar(gl, element, 3.33f, 0.075f, false, 2);
			gl.glRotatef(-90, 0, 0, 1);
			gl.glTranslatef(0.64f, 1.25f, -4.02f);
		}

		// Left second
		element = stackElementsLeft.get(1);
		if (element.getContainedElementID() != -1) {

			gl.glTranslatef(-1.17f, -1.25f, 4.02f);
			gl.glRotatef(90, 0, 0, 1);
			renderNavigationHandleBar(gl, element, 3.32f, 0.075f, false, 2);
			gl.glRotatef(-90, 0, 0, 1);
			gl.glTranslatef(1.17f, 1.25f, -4.02f);
		}

		// Right first
		element = stackElementsRight.get(0);
		if (element.getContainedElementID() != -1) {
			gl.glTranslatef(0.65f, 2.08f, 4.02f);
			gl.glRotatef(-90, 0, 0, 1);
			renderNavigationHandleBar(gl, element, 3.34f, 0.075f, false, 2);
			gl.glRotatef(90, 0, 0, 1);
			gl.glTranslatef(-0.65f, -2.08f, -4.02f);
		}

		// Right second
		element = stackElementsRight.get(1);
		if (element.getContainedElementID() != -1) {
			gl.glTranslatef(1.1f, 2.08f, 4.02f);
			gl.glRotatef(-90, 0, 0, 1);
			renderNavigationHandleBar(gl, element, 3.34f, 0.075f, false, 2);
			gl.glRotatef(90, 0, 0, 1);
			gl.glTranslatef(-1.1f, -2.08f, -4.02f);
		}
	}

	// private void renderViewConnectionPipes(final GL gl, RemoteLevelElement element) {
	//
	// textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_CONNECTION_STRAIGHT, new Vec3f(0, 0, 0),
	// new Vec3f(0.63f, 0, 0), new Vec3f(0.63f, 0.46f, 0), new Vec3f(0, 0.46f, 0), 1, 1, 1, 1);
	//
	// textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_CONNECTION_CORNER, new Vec3f(0, 0, 0),
	// new Vec3f(0.63f, 0, 0), new Vec3f(0.63f, 0.46f, 0), new Vec3f(0, 0.46f, 0), 1, 1, 1, 1);
	//
	// }

	// FIXME: method copied from bucket
	// private void renderViewTitleBar(final GL gl, RemoteLevelElement element,
	// float fHandleWidth,
	// float fHandleHeight, boolean bUpsideDown, float fScalingFactor,
	// EOrientation eOrientation) {
	//
	// // if (eOrientation == EOrientation.LEFT) {
	// // gl.glBegin(GL.GL_POLYGON);
	// // gl.glVertex3f(-fHandleHeight, fHandleHeight, 0);
	// // gl.glVertex3f(0, fHandleHeight, 0);
	// // gl.glVertex3f(0, fHandleWidth - fHandleHeight, 0);
	// // gl.glVertex3f(-fHandleHeight, fHandleWidth - fHandleHeight, 0);
	// // gl.glEnd();
	// //
	// // // Render icons
	// // gl.glTranslatef(-fHandleHeight, 0, 0);
	// // renderSingleHandle(gl, element.getID(),
	// EPickingType.BUCKET_DRAG_ICON_SELECTION,
	// // EIconTextures.NAVIGATION_DRAG_VIEW, fHandleHeight, fHandleHeight,
	// eOrientation);
	// // gl.glTranslatef(0, fHandleWidth - fHandleWidth, 0);
	// //// if (bUpsideDown) {
	// //// gl.glRotatef(180, 1, 0, 0);
	// //// gl.glTranslatef(0, fHandleHeight, 0);
	// //// }
	// //// renderSingleHandle(gl, element.getID(),
	// EPickingType.BUCKET_LOCK_ICON_SELECTION,
	// //// EIconTextures.NAVIGATION_LOCK_VIEW, fHandleHeight, fHandleHeight);
	// //// if (bUpsideDown) {
	// //// gl.glTranslatef(0, -fHandleHeight, 0);
	// //// gl.glRotatef(-180, 1, 0, 0);
	// //// }
	// //// gl.glTranslatef(0, -fHandleWidth, 0);
	// //// renderSingleHandle(gl, element.getID(),
	// EPickingType.BUCKET_REMOVE_ICON_SELECTION,
	// //// EIconTextures.NAVIGATION_REMOVE_VIEW, fHandleHeight, fHandleHeight);
	// // gl.glTranslatef(fHandleHeight, -fHandleWidth + fHandleWidth, 0);
	// //
	// // }
	//
	// if (eOrientation == EOrientation.LEFT)
	// gl.glRotatef(90, 0, 0, 1);
	//		
	// // Render icons
	// // gl.glTranslatef(0, fHandleWidth + fHandleHeight, 0);
	// // renderSingleHandle(gl, element.getID(),
	// EPickingType.BUCKET_DRAG_ICON_SELECTION,
	// // EIconTextures.NAVIGATION_DRAG_VIEW, fHandleHeight, fHandleHeight,
	// eOrientation);
	// // gl.glTranslatef(0, -fHandleWidth - fHandleHeight, 0);
	// // gl.glTranslatef(fHandleWidth - 2 * fHandleHeight, 0, 0);
	//		
	// // if (bUpsideDown) {
	// // gl.glRotatef(180, 1, 0, 0);
	// // gl.glTranslatef(0, fHandleHeight, 0);
	// // }
	// // renderSingleHandle(gl, element.getID(),
	// EPickingType.BUCKET_LOCK_ICON_SELECTION,
	// // EIconTextures.NAVIGATION_LOCK_VIEW, fHandleHeight, fHandleHeight,
	// eOrientation);
	// // if (bUpsideDown) {
	// // gl.glTranslatef(0, -fHandleHeight, 0);
	// // gl.glRotatef(-180, 1, 0, 0);
	// // }
	// // gl.glTranslatef(fHandleHeight, 0, 0);
	// // renderSingleHandle(gl, element.getID(),
	// EPickingType.BUCKET_REMOVE_ICON_SELECTION,
	// // EIconTextures.NAVIGATION_REMOVE_VIEW, fHandleHeight, fHandleHeight,
	// eOrientation);
	// // gl.glTranslatef(-fHandleWidth + fHandleHeight, -fHandleWidth -
	// fHandleHeight, 0);
	// //
	// // // Render background (also draggable)
	// //
	// // gl.glPushName(pickingManager.getPickingID(iUniqueID,
	// EPickingType.BUCKET_DRAG_ICON_SELECTION,
	// element
	// // .getID()));
	// gl.glColor3f(0.25f, 0.25f, 0.25f);
	// //
	// //// if (eOrientation == EOrientation.TOP) {
	// gl.glBegin(GL.GL_POLYGON);
	// gl.glVertex3f(0 + fHandleHeight, fHandleWidth + fHandleHeight, 0);
	// gl.glVertex3f(fHandleWidth - 2 * fHandleHeight, fHandleWidth +
	// fHandleHeight, 0);
	// gl.glVertex3f(fHandleWidth - 2 * fHandleHeight, fHandleWidth, 0);
	// gl.glVertex3f(0 + fHandleHeight, fHandleWidth, 0);
	// gl.glEnd();
	// //// }
	// // gl.glPopName();
	// //
	// if (eOrientation == EOrientation.LEFT)
	// gl.glRotatef(-90, 0, 0, 1);
	//
	// //
	// // // Render view information
	// // String sText =
	// //
	// generalManager.getViewGLCanvasManager().getGLEventListener(element.getContainedElementID())
	// // .getShortInfo();
	// //
	// // int iMaxChars = 50;
	// // if (sText.length() > iMaxChars) {
	// // sText = sText.subSequence(0, iMaxChars - 3) + "...";
	// // }
	// //
	// // float fTextScalingFactor = 0.0027f;
	// //
	// // if (bUpsideDown) {
	// // gl.glRotatef(180, 1, 0, 0);
	// // gl.glTranslatef(0, -4 - fHandleHeight, 0);
	// // }
	// //
	// // textRenderer.setColor(0.7f, 0.7f, 0.7f, 1);
	// // textRenderer.begin3DRendering();
	// // textRenderer.draw3D(sText, fHandleWidth / fScalingFactor
	// // - (float) textRenderer.getBounds(sText).getWidth() / 2f *
	// fTextScalingFactor, fHandleWidth + .02f,
	// // 0f, fTextScalingFactor);
	// // textRenderer.end3DRendering();
	// //
	// // if (bUpsideDown) {
	// // gl.glTranslatef(0, 4 + fHandleHeight, 0);
	// // gl.glRotatef(-180, 1, 0, 0);
	// // }
	// }

	private void renderNavigationHandleBar(final GL gl, RemoteLevelElement element, float fHandleWidth,
		float fHandleHeight, boolean bUpsideDown, float fScalingFactor) {

		// Render icons
		gl.glTranslatef(0, 2 + fHandleHeight, 0);
		renderSingleHandle(gl, element.getID(), EPickingType.BUCKET_DRAG_ICON_SELECTION,
			EIconTextures.NAVIGATION_DRAG_VIEW, fHandleHeight, fHandleHeight);
		gl.glTranslatef(fHandleWidth - 2 * fHandleHeight, 0, 0);
		if (bUpsideDown) {
			gl.glRotatef(180, 1, 0, 0);
			gl.glTranslatef(0, fHandleHeight, 0);
		}
		renderSingleHandle(gl, element.getID(), EPickingType.BUCKET_LOCK_ICON_SELECTION,
			EIconTextures.NAVIGATION_LOCK_VIEW, fHandleHeight, fHandleHeight);
		if (bUpsideDown) {
			gl.glTranslatef(0, -fHandleHeight, 0);
			gl.glRotatef(-180, 1, 0, 0);
		}
		gl.glTranslatef(fHandleHeight, 0, 0);
		renderSingleHandle(gl, element.getID(), EPickingType.BUCKET_REMOVE_ICON_SELECTION,
			EIconTextures.NAVIGATION_REMOVE_VIEW, fHandleHeight, fHandleHeight);
		gl.glTranslatef(-fHandleWidth + fHandleHeight, -2 - fHandleHeight, 0);

		// Render background (also draggable)
		gl.glPushName(pickingManager.getPickingID(iUniqueID, EPickingType.BUCKET_DRAG_ICON_SELECTION, element
			.getID()));
		gl.glColor3f(0.25f, 0.25f, 0.25f);
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(0 + fHandleHeight, 2 + fHandleHeight, 0);
		gl.glVertex3f(fHandleWidth - 2 * fHandleHeight, 2 + fHandleHeight, 0);
		gl.glVertex3f(fHandleWidth - 2 * fHandleHeight, 2, 0);
		gl.glVertex3f(0 + fHandleHeight, 2, 0);
		gl.glEnd();

		gl.glPopName();

		// Render view information
		String sText =
			generalManager.getViewGLCanvasManager().getGLEventListener(element.getContainedElementID())
				.getShortInfo();

		int iMaxChars = 50;
		if (sText.length() > iMaxChars) {
			sText = sText.subSequence(0, iMaxChars - 3) + "...";
		}

		float fTextScalingFactor = 0.0027f;

		if (bUpsideDown) {
			gl.glRotatef(180, 1, 0, 0);
			gl.glTranslatef(0, -4 - fHandleHeight, 0);
		}

		textRenderer.setColor(0.7f, 0.7f, 0.7f, 1);
		textRenderer.begin3DRendering();
		textRenderer.draw3D(sText, fHandleWidth / fScalingFactor
			- (float) textRenderer.getBounds(sText).getWidth() / 2f * fTextScalingFactor, 2.02f, 0f,
			fTextScalingFactor);
		textRenderer.end3DRendering();

		if (bUpsideDown) {
			gl.glTranslatef(0, 4 + fHandleHeight, 0);
			gl.glRotatef(-180, 1, 0, 0);
		}
	}

	// FIXME: method copied from bucket
	private void renderSingleHandle(final GL gl, int iRemoteLevelElementID, EPickingType ePickingType,
		EIconTextures eIconTexture, float fWidth, float fHeight) {

		gl.glPushName(pickingManager.getPickingID(iUniqueID, ePickingType, iRemoteLevelElementID));

		Texture tempTexture = textureManager.getIconTexture(gl, eIconTexture);
		tempTexture.enable();
		tempTexture.bind();

		TextureCoords texCoords = tempTexture.getImageTexCoords();
		gl.glColor3f(1, 1, 1);
		gl.glBegin(GL.GL_POLYGON);

		// if (eOrientation == EOrientation.TOP) {
		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		gl.glVertex3f(0, -fHeight, 0f);
		gl.glTexCoord2f(texCoords.left(), texCoords.top());
		gl.glVertex3f(0, 0, 0f);
		gl.glTexCoord2f(texCoords.right(), texCoords.top());
		gl.glVertex3f(fWidth, 0, 0f);
		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
		gl.glVertex3f(fWidth, -fHeight, 0f);
		gl.glEnd();
		// }
		// else if (eOrientation == EOrientation.LEFT) {
		// gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		// gl.glVertex3f(0, -fHeight, 0f);
		// gl.glTexCoord2f(texCoords.left(), texCoords.top());
		// gl.glVertex3f(0, 0, 0f);
		// gl.glTexCoord2f(texCoords.right(), texCoords.top());
		// gl.glVertex3f(fWidth, 0, 0f);
		// gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
		// gl.glVertex3f(fWidth, -fHeight, 0f);
		// gl.glEnd();
		// }

		tempTexture.disable();

		gl.glPopName();
	}

	// FIXME: method copied from bucket
	public void renderBucketWall(final GL gl, boolean bRenderBorder) {

		gl.glLineWidth(2);

		// Highlight potential view drop destination
		// if (dragAndDrop.isDragActionRunning() && element.getID() ==
		// iMouseOverObjectID) {
		// gl.glLineWidth(5);
		// }
		// gl.glColor4f(0.2f, 0.2f, 0.2f, 1);
		// gl.glBegin(GL.GL_LINE_LOOP);
		// gl.glVertex3f(0, 0, 0.01f);
		// gl.glVertex3f(0, 8, 0.01f);
		// gl.glVertex3f(8, 8, 0.01f);
		// gl.glVertex3f(8, 0, 0.01f);
		// gl.glEnd();
		// }

		// if (arSlerpActions.isEmpty()) {
		gl.glColor4f(1f, 1f, 1f, 1.0f); // normal mode
		// }
		// else {
		// gl.glColor4f(1f, 1f, 1f, 0.3f);
		// }

		if (!newViews.isEmpty()) {
			gl.glColor4f(1f, 1f, 1f, 0.3f);
		}

		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(0, 0, -0.01f);
		gl.glVertex3f(0, 8, -0.01f);
		gl.glVertex3f(8, 8, -0.01f);
		gl.glVertex3f(8, 0, -0.01f);
		gl.glEnd();

		if (!bRenderBorder)
			return;

		gl.glColor4f(0.4f, 0.4f, 0.4f, 1f);
		gl.glLineWidth(1f);
	}

	private void updateViewDetailLevels(RemoteLevelElement element) {
		if (element.getContainedElementID() == -1)
			return;

		AGLEventListener glActiveSubView =
			GeneralManager.get().getViewGLCanvasManager().getGLEventListener(element.getContainedElementID());

		glActiveSubView.setRemoteLevelElement(element);

		// Update detail level of moved view when slerp action is finished;
		if (element == focusElement) {
			glActiveSubView.setDetailLevel(EDetailLevel.HIGH);
		}
		else {
			glActiveSubView.setDetailLevel(EDetailLevel.LOW);
		}
	}
}
