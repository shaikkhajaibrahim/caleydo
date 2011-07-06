package org.caleydo.view.heatmap.uncertainty;

import java.util.ArrayList;
import java.util.List;

import javax.management.InvalidAttributeValueException;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.collection.set.Set;
import org.caleydo.core.data.mapping.IDType;
import org.caleydo.core.data.selection.SelectedElementRep;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.selection.delta.ISelectionDelta;
import org.caleydo.core.data.virtualarray.ContentVirtualArray;
import org.caleydo.core.data.virtualarray.EVAOperation;
import org.caleydo.core.data.virtualarray.delta.ContentVADelta;
import org.caleydo.core.data.virtualarray.group.ContentGroupList;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.manager.picking.EPickingMode;
import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.manager.picking.Pick;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.mapping.color.ColorMapper;
import org.caleydo.core.util.mapping.color.ColorMappingManager;
import org.caleydo.core.util.mapping.color.EColorMappingType;
import org.caleydo.core.view.opengl.camera.ECameraProjectionMode;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.AStorageBasedView;
import org.caleydo.core.view.opengl.canvas.DetailLevel;
import org.caleydo.core.view.opengl.canvas.GLCaleydoCanvas;
import org.caleydo.core.view.opengl.canvas.listener.ISelectionUpdateHandler;
import org.caleydo.core.view.opengl.canvas.listener.IViewCommandHandler;
import org.caleydo.core.view.opengl.canvas.remote.IGLRemoteRenderingView;
import org.caleydo.core.view.opengl.layout.Column;
import org.caleydo.core.view.opengl.layout.ElementLayout;
import org.caleydo.core.view.opengl.layout.LayoutManager;
import org.caleydo.core.view.opengl.layout.LayoutTemplate;
import org.caleydo.core.view.opengl.layout.Row;
import org.caleydo.core.view.opengl.layout.util.ViewLayoutRenderer;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.view.heatmap.HeatMapRenderStyle;
import org.caleydo.view.heatmap.heatmap.GLHeatMap;
import org.caleydo.view.heatmap.heatmap.renderer.OverviewDetailConnectorRenderer;
import org.caleydo.view.heatmap.heatmap.template.UncertaintyDetailHeatMapTemplate;

/**
 * Uncertainty heat map view.
 * 
 * @author Marc Streit
 * @author Alexander Lex
 * @author Clemens Holzhüter
 */

public class GLUncertaintyHeatMap extends AStorageBasedView implements
		IViewCommandHandler, ISelectionUpdateHandler, IGLRemoteRenderingView {

	public final static String VIEW_ID = "org.caleydo.view.heatmap.uncertainty";
	public final static float[] light = {0.0f, 0.0f, 0.0f, 0.95f};
	public final static float[] dark = {0.0f, 0.0f, 0.0f, 0.15f};
	
	private HeatMapRenderStyle renderStyle;

	private OverviewRenderer overviewHeatMap;
	private GLHeatMap detailHeatMap;

	private LayoutManager templateRenderer;
	private LayoutTemplate template;

	private Row baseRow;
	private Column overviewLayout;
	private ElementLayout detailLayout;
	private ElementLayout overviewDetailConnectorLayout;

	private ColorMapper colorMapper = ColorMappingManager.get().getColorMapping(
			EColorMappingType.GENE_EXPRESSION);

	/**
	 * Constructor.
	 * 
	 * @param glCanvas
	 * @param label
	 * @param viewFrustum
	 */
	public GLUncertaintyHeatMap(GLCaleydoCanvas glCanvas, final ViewFrustum viewFrustum) {
		super(glCanvas, viewFrustum);
		viewType = GLUncertaintyHeatMap.VIEW_ID;
	}

	@Override
	public void init(GL2 gl) {

		templateRenderer = new LayoutManager(this.viewFrustum);
		if (template == null)
			template = new LayoutTemplate();

		templateRenderer.setTemplate(template);

		baseRow = new Row("baseRow");
		template.setBaseElementLayout(baseRow);

		overviewLayout = new Column("overviewLayout");
		overviewLayout.setDebug(false);
		overviewLayout.setPixelGLConverter(parentGLCanvas.getPixelGLConverter());
		overviewLayout.setPixelSizeX(60);

		overviewDetailConnectorLayout = new Column("overviewDetailConnectorLayout");
		overviewDetailConnectorLayout.setDebug(false);
		overviewDetailConnectorLayout.setPixelGLConverter(parentGLCanvas.getPixelGLConverter());
		overviewDetailConnectorLayout.setPixelSizeX(100);	
		
		detailLayout = new ElementLayout("detailLayout");
		detailLayout.setDebug(false);

		baseRow.append(overviewLayout);
		baseRow.append(overviewDetailConnectorLayout);
		baseRow.append(detailLayout);

		super.renderStyle = renderStyle;
		detailLevel = DetailLevel.HIGH;

		createOverviewHeatMap(gl);
		createDetailHeatMap(gl);
		
		OverviewDetailConnectorRenderer overviewDetailConnectorRenderer = new OverviewDetailConnectorRenderer(overviewHeatMap, detailHeatMap);
		overviewDetailConnectorLayout.setRenderer(overviewDetailConnectorRenderer);

		templateRenderer.updateLayout();
	}

	@Override
	public void initLocal(GL2 gl) {
		init(gl);
	}

	@Override
	public void initRemote(final GL2 gl, final AGLView glParentView,
			final GLMouseListener glMouseListener) {

		// Register keyboard listener to GL2 canvas
		glParentView.getParentGLCanvas().getParentComposite().getDisplay()
				.asyncExec(new Runnable() {
					@Override
					public void run() {
						glParentView.getParentGLCanvas().getParentComposite()
								.addKeyListener(glKeyListener);
					}
				});

		this.glMouseListener = glMouseListener;

		iGLDisplayListIndexRemote = gl.glGenLists(1);
		iGLDisplayListToCall = iGLDisplayListIndexRemote;
		init(gl);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		super.reshape(drawable, x, y, width, height);
		templateRenderer.updateLayout();
	}

	/**
	 * Create embedded heat map
	 * 
	 * @param overviewLayout2
	 * 
	 * @param
	 */
	private void createOverviewHeatMap(GL2 gl) {

		overviewHeatMap = new OverviewRenderer(this, overviewLayout);
		overviewLayout.setRenderer(overviewHeatMap);
		overviewHeatMap.init();
	}

	private void createDetailHeatMap(GL2 gl) {
		detailHeatMap = (GLHeatMap) GeneralManager
				.get()
				.getViewGLCanvasManager()
				.createGLView(
						GLHeatMap.class,
						this.getParentGLCanvas(),
						new ViewFrustum(ECameraProjectionMode.ORTHOGRAPHIC, 0, 1, 0, 1,
								-1, 1));

		detailHeatMap.setDataDomain(dataDomain);
		detailHeatMap.setRemoteRenderingGLView(this);
		detailHeatMap.setSet(set);
		detailHeatMap.setRenderTemplate(new UncertaintyDetailHeatMapTemplate(
				detailHeatMap));
		detailHeatMap.initialize();
		detailHeatMap.initRemote(gl, this, glMouseListener);

		if (contentVA != null)
			detailHeatMap.setContentVA(contentVA);

		ViewLayoutRenderer detailHeatMapLayoutRenderer = new ViewLayoutRenderer(
				detailHeatMap);

		detailLayout.setRenderer(detailHeatMapLayoutRenderer);
	}

	@Override
	public void displayLocal(GL2 gl) {

		pickingManager.handlePicking(this, gl);
		display(gl);
		checkForHits(gl);

		// if (overviewHeatMap != null)
		// overviewHeatMap.processEvents();
	}

	@Override
	public void displayRemote(GL2 gl) {
		throw new IllegalStateException("This view cannot be rendered remotely!");
	}

	@Override
	public void display(GL2 gl) {
		baseRow.render(gl);
		// overviewHeatMap.displayRemote(gl);
	}

	@Override
	public String getShortInfo() {

		return "LayoutTemplate Caleydo View";
	}

	@Override
	public String getDetailedInfo() {
		return "LayoutTemplate Caleydo View";

	}

	@Override
	protected void handlePickingEvents(EPickingType pickingType,
			EPickingMode pickingMode, int externalID, Pick pick) {

		switch (pickingType) {

		// handling the groups/clusters of genes
		case HEAT_MAP_CLUSTER_GROUP:
			switch (pickingMode) {
			case CLICKED:
				
				overviewHeatMap.setSelectedGroup(externalID);
				ContentGroupList groupList = contentVA.getGroupList();
				
				// null if data set is not clustered
				if (groupList == null)
					break;
				
				ArrayList<Integer> clusterElements = contentVA.getIDsOfGroup(groupList.get(externalID).getID());
				ContentVirtualArray clusterVA = new ContentVirtualArray(Set.CONTENT,
						clusterElements);
				detailHeatMap.setContentVA(clusterVA);
				
				setDisplayListDirty();
				break;

			case DRAGGED:
				break;

			case MOUSE_OVER:
				break;
			}
			break;
		}
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedUncertaintyHeatMapView serializedForm = new SerializedUncertaintyHeatMapView();
		serializedForm.setViewID(this.getID());
		return serializedForm;
	}

	@Override
	public String toString() {
		return "TODO: ADD INFO THAT APPEARS IN THE LOG";
	}

	@Override
	public void registerEventListeners() {
		super.registerEventListeners();

	}

	@Override
	public void unregisterEventListeners() {
		super.unregisterEventListeners();

	}

	@Override
	public void handleSelectionUpdate(ISelectionDelta selectionDelta,
			boolean scrollToSelection, String info) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleRedrawView() {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleUpdateView() {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleClearSelections() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearAllSelections() {
		// TODO Auto-generated method stub

	}

	@Override
	public void broadcastElements(EVAOperation type) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumberOfSelections(SelectionType SelectionType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<AGLView> getRemoteRenderedViews() {
		// ArrayList<AGLView> remoteRenderedViews = new ArrayList<AGLView>();
		// remoteRenderedViews.add(overviewHeatMap);
		// return remoteRenderedViews;
		return null;
	}

	@Override
	public void renderContext(boolean bRenderContext) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void reactOnContentVAChanges(ContentVADelta delta) {

		super.reactOnContentVAChanges(delta);
		
		setDisplayListDirty();
		overviewHeatMap.init();
	}
	
	@Override
	protected void initLists() {

		if (bRenderOnlyContext)
			contentVAType = ISet.CONTENT_CONTEXT;
		else
			contentVAType = ISet.CONTENT;

		contentVA = dataDomain.getContentVA(contentVAType);
		storageVA = dataDomain.getStorageVA(storageVAType);

		// In case of importing group info
		// if (set.isGeneClusterInfo())
		// contentVA.setGroupList(set.getContentGroupList());
		// if (set.isExperimentClusterInfo())
		// storageVA.setGroupList(set.getStorageGroupList());

		contentSelectionManager.setVA(contentVA);
		storageSelectionManager.setVA(storageVA);

		setDisplayListDirty();
	}

	@Override
	protected ArrayList<SelectedElementRep> createElementRep(IDType idType, int id)
			throws InvalidAttributeValueException {
		// TODO Auto-generated method stub
		return null;
	}

	public ColorMapper getColorMapper() {
		return colorMapper;
	}

	@Override
	public void replaceContentVA(int setID, String dataDomainType, String vaType) {

		super.replaceContentVA(setID, dataDomainType, vaType);

		overviewHeatMap.init();
	}
}
