package org.caleydo.view.tissuebrowser;

import gleem.linalg.Vec3f;
import gleem.linalg.open.Transform;

import java.util.ArrayList;
import java.util.HashMap;

import javax.media.opengl.GL;

import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.mapping.EIDCategory;
import org.caleydo.core.data.mapping.EIDType;
import org.caleydo.core.data.selection.ContentSelectionManager;
import org.caleydo.core.data.selection.ContentVAType;
import org.caleydo.core.data.selection.delta.ContentVADelta;
import org.caleydo.core.data.selection.delta.ISelectionDelta;
import org.caleydo.core.data.selection.delta.VADeltaItem;
import org.caleydo.core.manager.IDataDomain;
import org.caleydo.core.manager.datadomain.EDataDomain;
import org.caleydo.core.manager.event.data.ReplaceVAEvent;
import org.caleydo.core.manager.event.view.storagebased.ContentVAUpdateEvent;
import org.caleydo.core.manager.event.view.storagebased.SelectionUpdateEvent;
import org.caleydo.core.manager.event.view.storagebased.VirtualArrayUpdateEvent;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.view.opengl.camera.IViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.AGLViewBrowser;
import org.caleydo.core.view.opengl.canvas.GLCaleydoCanvas;
import org.caleydo.core.view.opengl.canvas.listener.ContentVAUpdateListener;
import org.caleydo.core.view.opengl.canvas.listener.IContentVAUpdateHandler;
import org.caleydo.core.view.opengl.canvas.listener.ReplaceContentVAListener;
import org.caleydo.core.view.opengl.canvas.listener.SelectionUpdateListener;
import org.caleydo.core.view.opengl.util.hierarchy.RemoteLevelElement;
import org.caleydo.view.tissue.GLTissue;
import org.caleydo.view.tissue.SerializedTissueView;

public class GLTissueViewBrowser extends AGLViewBrowser implements
		IContentVAUpdateHandler {

	public final static String VIEW_ID = "org.caleydo.view.tissuebrowser";

	private static final int VIEW_THRESHOLD = 20;

	private HashMap<Integer, String> mapExperimentToTexturePath;

	private ContentSelectionManager experiementSelectionManager;

	private SelectionUpdateListener selectionUpdateListener;
	private ContentVAUpdateListener virtualArrayUpdateListener;
	private ReplaceContentVAListener replaceVirtualArrayListener;

	private EIDType primaryIDType = EIDType.EXPERIMENT_INDEX;

	private ArrayList<SerializedTissueView> allTissueViews;

	private boolean poolLeft = true;

	public GLTissueViewBrowser(GLCaleydoCanvas glCanvas, String sLabel,
			IViewFrustum viewFrustum) {
		super(glCanvas, sLabel, viewFrustum);

		viewType = VIEW_ID;
		mapExperimentToTexturePath = new HashMap<Integer, String>();
	}

	@Override
	public void setUseCase(IDataDomain useCase) {
		super.setUseCase(useCase);
		contentVA = GeneralManager.get().getUseCase(EDataDomain.CLINICAL_DATA)
				.getContentVA(ContentVAType.CONTENT);
		experiementSelectionManager = new ContentSelectionManager(primaryIDType);
		experiementSelectionManager.setVA(contentVA);

		addInitialViews();
	}

	@Override
	protected void addInitialViews() {

		newViews.clear();
		allTissueViews = new ArrayList<SerializedTissueView>();

		ISet geneticSet = generalManager.getUseCase(EDataDomain.GENETIC_DATA)
				.getSet();
		for (int experimentIndex = 0; experimentIndex < 2; experimentIndex++) { //TODO: replace 2 with geneticSet.size()

			generalManager.getViewGLCanvasManager().createGLView(
					"org.caleydo.view.tissue", parentGLCanvas, "", viewFrustum);

			mapExperimentToTexturePath.put(experimentIndex,
					"data/tissue/breast_" + experimentIndex % 24 + ".jpg");

			SerializedTissueView tissue = new SerializedTissueView();
			tissue.setDataDomain(EDataDomain.TISSUE_DATA);
			tissue.setTexturePath(mapExperimentToTexturePath
					.get(experimentIndex));
			//tissue.setLabel(geneticSet.get(experimentIndex).getLabel());
			tissue.setExperimentIndex(experimentIndex);

			allTissueViews.add(tissue);
		}
		
		for (SerializedTissueView serTissue : allTissueViews) {
			newViews.add(serTissue);
		}
	}

	private void updateViews() {

		if (contentVA.size() > VIEW_THRESHOLD)
			return;

		newViews.clear();

		clearRemoteLevel(focusLevel);
		clearRemoteLevel(poolLevel);
		clearRemoteLevel(transitionLevel);

		for (Integer experimentIndex : contentVA) {
			newViews.add(allTissueViews.get(experimentIndex));
		}
	}

	@Override
	protected AGLView createView(GL gl, ASerializedView serView) {

		AGLView glView = super.createView(gl, serView);

		((GLTissue) glView).setTexturePath(((SerializedTissueView) serView)
				.getTexturePath());
		glView.setLabel(((SerializedTissueView) serView).getLabel());
		((GLTissue) glView).setExperimentIndex(((SerializedTissueView) serView)
				.getExperimentIndex());
		return glView;
	}

	@Override
	protected void initFocusLevel() {

		float xOffset = 1.5f;
		if (!poolLeft)
			xOffset = 0.1f;

		Transform transform = new Transform();
		transform.setTranslation(new Vec3f(xOffset, 1.3f, 0));
		transform.setScale(new Vec3f(0.8f, 0.8f, 1));

		focusLevel.getElementByPositionIndex(0).setTransform(transform);
	}

	@Override
	protected void initPoolLevel(int iSelectedRemoteLevelElementID) {
		Transform transform;

		float xOffset = 6.6f;
		if (poolLeft)
			xOffset = 0.1f;

		float fScalingFactorPoolLevel = 0.05f;
		float fSelectedScaling = 1;
		float fYAdd = 8f;

		int iRemoteLevelElementIndex = 0;
		for (RemoteLevelElement element : poolLevel.getAllElements()) {

			if (element.getID() == iSelectedRemoteLevelElementID) {
				fSelectedScaling = 1.8f;
				fYAdd -= 0.6f * fSelectedScaling;
			} else {
				fSelectedScaling = 1;
				fYAdd -= 0.5f * fSelectedScaling;
			}

			transform = new Transform();
			transform.setTranslation(new Vec3f(xOffset, fYAdd, 0));
			transform.setScale(new Vec3f(fScalingFactorPoolLevel
					* fSelectedScaling, fScalingFactorPoolLevel
					* fSelectedScaling, fScalingFactorPoolLevel
					* fSelectedScaling));

			poolLevel.getElementByPositionIndex(iRemoteLevelElementIndex)
					.setTransform(transform);
			iRemoteLevelElementIndex++;
		}
	}

	@Override
	protected void initExternalSelectionLevel() {

		float fScalingFactorSelectionLevel = 1;
		Transform transform = new Transform();
		transform.setTranslation(new Vec3f(1, -2.01f, 0));
		transform.setScale(new Vec3f(fScalingFactorSelectionLevel,
				fScalingFactorSelectionLevel, fScalingFactorSelectionLevel));

		externalSelectionLevel.getElementByPositionIndex(0).setTransform(
				transform);
	}

	@Override
	protected void initTransitionLevel() {

		Transform transform = new Transform();
		transform.setTranslation(new Vec3f(1.5f, 1.3f, 0));
		transform.setScale(new Vec3f(0.8f, 0.8f, 1));

		transitionLevel.getElementByPositionIndex(0).setTransform(transform);

	}

	@Override
	protected void initSpawnLevel() {

		float fScalingFactorSpawnLevel = 0.05f;
		Transform transform = new Transform();
		transform.setTranslation(new Vec3f(6.5f, 5, -0.2f));
		transform.setScale(new Vec3f(fScalingFactorSpawnLevel,
				fScalingFactorSpawnLevel, fScalingFactorSpawnLevel));

		spawnLevel.getElementByPositionIndex(0).setTransform(transform);
	}

	@Override
	public String getShortInfo() {
		return "Tissue Browser";
	}

	@Override
	public String getDetailedInfo() {
		StringBuffer sInfoText = new StringBuffer();
		sInfoText.append("Tissue Browser");
		return sInfoText.toString();
	}

	// /**
	// * This method generates only valid associations for Asslaber dataset!
	// */
	// private void generateTissuePatientConnection() {
	//
	// ClinicalUseCase clinicalUseCase =
	// (ClinicalUseCase) generalManager.getUseCase(EDataDomain.CLINICAL_DATA);
	// ISet clinicalSet = clinicalUseCase.getSet();
	//
	// if (clinicalSet.get(0) == null)
	// return;
	//
	// for (int index = 0; index < clinicalSet.depth(); index++) {
	//
	// mapExperimentToTexturePath.put(index, "data/tissue/breast_" + index % 24
	// + ".jpg");
	// }
	//
	// // for (Integer vaID : clinicalUseCase.getVA(EVAType.CONTENT))
	// // {
	// // set.getStorageFromVA(
	// // }
	// //
	// // for (IStorage storage : set) {
	// // String experiment = storage.getLabel();
	// // mapExperimentToTexturePath.put(experiment, )
	// // }
	//
	// }

	@Override
	public void handleSelectionUpdate(ISelectionDelta selectionDelta,
			boolean scrollToSelection, String info) {
		if (selectionDelta.getIDType() == primaryIDType) {
			experiementSelectionManager.setDelta(selectionDelta);
		}
	}

	/**
	 * FIXME: should be moved to a bucket-mediator registers the event-listeners
	 * to the event framework
	 */
	@Override
	public void registerEventListeners() {
		super.registerEventListeners();

		selectionUpdateListener = new SelectionUpdateListener();
		selectionUpdateListener.setHandler(this);
		eventPublisher.addListener(SelectionUpdateEvent.class,
				selectionUpdateListener);

		virtualArrayUpdateListener = new ContentVAUpdateListener();
		virtualArrayUpdateListener.setHandler(this);
		eventPublisher.addListener(VirtualArrayUpdateEvent.class,
				virtualArrayUpdateListener);

		replaceVirtualArrayListener = new ReplaceContentVAListener();
		replaceVirtualArrayListener.setHandler(this);
		eventPublisher.addListener(ReplaceVAEvent.class,
				replaceVirtualArrayListener);

	}

	/**
	 * FIXME: should be moved to a bucket-mediator registers the event-listeners
	 * to the event framework
	 */
	@Override
	public void unregisterEventListeners() {

		super.unregisterEventListeners();

		if (selectionUpdateListener != null) {
			eventPublisher.removeListener(selectionUpdateListener);
			selectionUpdateListener = null;
		}

		if (virtualArrayUpdateListener != null) {
			eventPublisher.removeListener(virtualArrayUpdateListener);
			virtualArrayUpdateListener = null;
		}

		if (replaceVirtualArrayListener != null) {
			eventPublisher.removeListener(replaceVirtualArrayListener);
			replaceVirtualArrayListener = null;
		}

	}

	public ContentSelectionManager getSelectionManager() {
		return experiementSelectionManager;
	}

	public void setPoolSide(boolean poolLeft) {
		this.poolLeft = poolLeft;
	}

	@Override
	protected void removeSelection(int iElementID) {

		experiementSelectionManager.remove(iElementID, false);
		ContentVADelta vaDelta = new ContentVADelta(ContentVAType.CONTENT,
				EIDType.EXPERIMENT_INDEX);
		vaDelta.add(VADeltaItem.removeElement(iElementID));

		ContentVAUpdateEvent virtualArrayUpdateEvent = new ContentVAUpdateEvent();
		virtualArrayUpdateEvent.setSender(this);
		virtualArrayUpdateEvent.setVirtualArrayDelta(vaDelta);
		virtualArrayUpdateEvent.setInfo(getShortInfo());
		eventPublisher.triggerEvent(virtualArrayUpdateEvent);
	}

	@Override
	public void handleContentVAUpdate(ContentVADelta vaDelta, String info) {
		if (vaDelta.getIDType() == primaryIDType) {
			experiementSelectionManager.setVADelta(vaDelta);
		}
	}

	@Override
	public void replaceContentVA(int setID, EIDCategory idCategory, ContentVAType vaType) {
		if (idCategory != EIDCategory.EXPERIMENT)
			return;

		IDataDomain clinicalUseCase = GeneralManager.get().getUseCase(
				EDataDomain.CLINICAL_DATA);

		String primaryVAType = clinicalUseCase
				.getVATypeForIDCategory(idCategory);
		if (primaryVAType == null)
			return;

		contentVA = clinicalUseCase.getContentVA(vaType);
		// contentSelectionManager.setVA(contentVA);

		initData();
		updateViews();
	}
}
