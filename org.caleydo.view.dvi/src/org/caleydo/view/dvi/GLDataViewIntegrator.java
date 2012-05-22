/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *  
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.view.dvi;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;

import org.caleydo.core.data.container.DataContainer;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.datadomain.graph.DataDomainGraph;
import org.caleydo.core.data.perspective.DimensionPerspective;
import org.caleydo.core.data.perspective.PerspectiveInitializationData;
import org.caleydo.core.data.perspective.RecordPerspective;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.virtualarray.DimensionVirtualArray;
import org.caleydo.core.data.virtualarray.EVAOperation;
import org.caleydo.core.data.virtualarray.RecordVirtualArray;
import org.caleydo.core.data.virtualarray.events.DimensionVAUpdateEvent;
import org.caleydo.core.data.virtualarray.events.RecordVAUpdateEvent;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.event.MinSizeAppliedEvent;
import org.caleydo.core.event.SetMinViewSizeEvent;
import org.caleydo.core.event.data.DataDomainUpdateEvent;
import org.caleydo.core.event.data.NewDataDomainEvent;
import org.caleydo.core.event.view.DataContainersChangedEvent;
import org.caleydo.core.event.view.NewViewEvent;
import org.caleydo.core.event.view.ViewClosedEvent;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.logging.Logger;
import org.caleydo.core.view.ARcpGLViewPart;
import org.caleydo.core.view.RCPViewInitializationData;
import org.caleydo.core.view.RCPViewManager;
import org.caleydo.core.view.listener.AddDataContainersEvent;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.listener.IViewCommandHandler;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.core.view.opengl.picking.PickingType;
import org.caleydo.core.view.opengl.util.draganddrop.DragAndDropController;
import org.caleydo.core.view.opengl.util.spline.ConnectionBandRenderer;
import org.caleydo.core.view.opengl.util.text.CaleydoTextRenderer;
import org.caleydo.view.dvi.event.AddDataContainerEvent;
import org.caleydo.view.dvi.event.ApplySpecificGraphLayoutEvent;
import org.caleydo.view.dvi.event.CreateViewFromDataContainerEvent;
import org.caleydo.view.dvi.event.OpenViewEvent;
import org.caleydo.view.dvi.event.ShowDataConnectionsEvent;
import org.caleydo.view.dvi.layout.AGraphLayout;
import org.caleydo.view.dvi.layout.TwoLayeredGraphLayout;
import org.caleydo.view.dvi.layout.edge.rendering.AEdgeRenderer;
import org.caleydo.view.dvi.listener.AddDataContainerEventListener;
import org.caleydo.view.dvi.listener.ApplySpecificGraphLayoutEventListener;
import org.caleydo.view.dvi.listener.CreateViewFromDataContainerEventListener;
import org.caleydo.view.dvi.listener.DataContainersCangedListener;
import org.caleydo.view.dvi.listener.DimensionGroupsChangedEventListener;
import org.caleydo.view.dvi.listener.DimensionVAUpdateEventListener;
import org.caleydo.view.dvi.listener.GLDVIKeyListener;
import org.caleydo.view.dvi.listener.MinSizeAppliedEventListener;
import org.caleydo.view.dvi.listener.NewDataDomainEventListener;
import org.caleydo.view.dvi.listener.NewViewEventListener;
import org.caleydo.view.dvi.listener.OpenViewEventListener;
import org.caleydo.view.dvi.listener.RecordVAUpdateEventListener;
import org.caleydo.view.dvi.listener.ShowDataConnectionsEventListener;
import org.caleydo.view.dvi.listener.ViewClosedEventListener;
import org.caleydo.view.dvi.node.ADataNode;
import org.caleydo.view.dvi.node.IDVINode;
import org.caleydo.view.dvi.node.NodeCreator;
import org.caleydo.view.dvi.node.ViewNode;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * This class is responsible for rendering the radial hierarchy and receiving
 * user events and events from other views.
 * 
 * @author Christian Partl
 */
public class GLDataViewIntegrator extends AGLView implements IViewCommandHandler {
	public static String VIEW_TYPE = "org.caleydo.view.dvi";

	public static String VIEW_NAME = "Data-View Integrator";

	public final static int BOUNDS_SPACING_PIXELS = 10;

	private GLDVIKeyListener glKeyListener;
	private boolean useDetailLevel = false;

	private Graph dataGraph;
	private AGraphLayout graphLayout;
	private int maxNodeWidthPixels;
	private int maxNodeHeightPixels;
	private DragAndDropController dragAndDropController;
	private boolean applyAutomaticLayout;
	private int lastNodeID = 0;
	private Set<ADataNode> dataNodes;
	private Set<ViewNode> viewNodes;
	private Map<IDataDomain, Set<ViewNode>> viewNodesOfDataDomains;
	private Map<IDataDomain, ADataNode> dataNodesOfDataDomains;
	private ConnectionBandRenderer connectionBandRenderer;

	private int maxDataAmount = Integer.MIN_VALUE;
	private boolean nodePositionsUpdated = false;

	private NewViewEventListener newViewEventListener;
	private NewDataDomainEventListener newDataDomainEventListener;
	private ViewClosedEventListener viewClosedEventListener;
	private DataContainersCangedListener dataContainersChangedListener;
	private DimensionGroupsChangedEventListener dimensionGroupsChangedEventListener;
	private AddDataContainerEventListener addDataContainerEventListener;
	private OpenViewEventListener openViewEventListener;
	private CreateViewFromDataContainerEventListener createViewFromDataContainerEventListener;
	private ApplySpecificGraphLayoutEventListener applySpecificGraphLayoutEventListener;
	private MinSizeAppliedEventListener minSizeAppliedEventListener;
	private ShowDataConnectionsEventListener showDataConnectionsEventListener;
	private RecordVAUpdateEventListener recordVAUpdateEventListener;
	private DimensionVAUpdateEventListener dimensionVAUpdateEventListener;

	private IDVINode currentMouseOverNode;

	private NodeCreator nodeCreator;

	private int minViewHeightPixels;
	private int minViewWidthPixels;

	private boolean isMinSizeApplied = false;
	private boolean waitForMinSizeApplication = false;
	private boolean isRendered = false;
	private boolean showDataConnections = false;

	private boolean isVendingMachineMode = false;

	/**
	 * Constructor.
	 */
	public GLDataViewIntegrator(GLCanvas glCanvas, Composite parentComposite,
			ViewFrustum viewFrustum) {

		super(glCanvas, parentComposite, viewFrustum, VIEW_TYPE, VIEW_NAME);

		connectionBandRenderer = new ConnectionBandRenderer();
		VIEW_TYPE = GLDataViewIntegrator.VIEW_TYPE;

		glKeyListener = new GLDVIKeyListener();
		// graphLayout = new ForceDirectedGraphLayout(this, dataGraph);
		dragAndDropController = new DragAndDropController(this);
		dataNodes = new HashSet<ADataNode>();
		viewNodes = new HashSet<ViewNode>();
		viewNodesOfDataDomains = new HashMap<IDataDomain, Set<ViewNode>>();
		dataNodesOfDataDomains = new HashMap<IDataDomain, ADataNode>();
		nodeCreator = new NodeCreator();
	}

	@Override
	public void init(GL2 gl) {

		displayListIndex = gl.glGenLists(1);

		// Register keyboard listener to GL2 canvas
		parentComposite.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				parentComposite.addKeyListener(glKeyListener);
			}
		});

		textRenderer = new CaleydoTextRenderer(24);
		dataGraph = new Graph();
		graphLayout = new TwoLayeredGraphLayout(this, dataGraph);

		DataDomainGraph dataDomainGraph = DataDomainManager.get().getDataDomainGraph();

		for (IDataDomain dataDomain : dataDomainGraph.getDataDomains()) {
			addDataDomain(dataDomain);
		}

		// Set<String> allowedViewTypes = new HashSet<String>();
		// // TODO: Maybe add to AView isMetaView() instead?
		// allowedViewTypes.add("org.caleydo.view.parcoords");
		// allowedViewTypes.add("org.caleydo.view.heatmap");
		// allowedViewTypes.add("org.caleydo.view.heatmap.hierarchical");
		// allowedViewTypes.add("org.caleydo.view.visbricks");
		// allowedViewTypes.add("org.caleydo.view.scatterplot");
		// allowedViewTypes.add("org.caleydo.view.tabular");
		// allowedViewTypes.add("org.caleydo.view.bucket");

		Collection<AGLView> views = GeneralManager.get().getViewManager().getAllGLViews();

		for (AGLView view : views) {
			addView(view);
		}

		applyAutomaticLayout = true;
	}

	@Override
	public void initLocal(GL2 gl) {
		init(gl);
	}

	@Override
	public void initRemote(final GL2 gl, final AGLView glParentView,
			final GLMouseListener glMouseListener) {
		this.glMouseListener = glMouseListener;
		init(gl);
		pixelGLConverter = glParentView.getPixelGLConverter();
	}

	@Override
	public void setDetailLevel(EDetailLevel detailLevel) {
		if (useDetailLevel) {
			super.setDetailLevel(detailLevel);
			// renderStyle.setDetailLevel(detailLevel);
		}

	}

	@Override
	public void displayLocal(GL2 gl) {

		if (!lazyMode)
			pickingManager.handlePicking(this, gl);

		display(gl);

		if (busyState != EBusyState.OFF) {
			renderBusyMode(gl);
		}
	}

	@Override
	public void displayRemote(GL2 gl) {
		display(gl);
	}

	@Override
	public void display(GL2 gl) {

		if (!isRendered) {
			maxNodeWidthPixels = Integer.MIN_VALUE;
			maxNodeHeightPixels = Integer.MIN_VALUE;

			for (IDVINode node : dataGraph.getNodes()) {
				node.recalculateNodeSize();
				if (node.getHeightPixels() > maxNodeHeightPixels)
					maxNodeHeightPixels = node.getHeightPixels();

				if (node.getWidthPixels() > maxNodeWidthPixels)
					maxNodeWidthPixels = node.getWidthPixels();
			}
		}

		if (isDisplayListDirty && !(!isMinSizeApplied && waitForMinSizeApplication)) {
			buildDisplayList(gl, displayListIndex);
			isDisplayListDirty = false;
			waitForMinSizeApplication = false;
		}
		gl.glCallList(displayListIndex);

		if (!lazyMode)
			checkForHits(gl);

		dragAndDropController.handleDragging(gl, glMouseListener);

		isRendered = true;
	}

	/**
	 * Builds the display list for a given display list index.
	 * 
	 * @param gl
	 *            Instance of GL2.
	 * @param iGLDisplayListIndex
	 *            Index of the display list.
	 */
	private void buildDisplayList(final GL2 gl, int iGLDisplayListIndex) {
		gl.glNewList(iGLDisplayListIndex, GL2.GL_COMPILE);

		Rectangle2D drawingArea = calculateGraphDrawingArea();
		
		if (applyAutomaticLayout) {
			for (IDVINode node : dataGraph.getNodes()) {
				node.setCustomPosition(false);
			}
			for (Edge edge : dataGraph.getAllEdges()) {
				AEdgeRenderer edgeRenderer = graphLayout
						.getLayoutSpecificEdgeRenderer(edge);
				edge.setEdgeRenderer(edgeRenderer);
			}
			// graphLayout.setGraph(dataGraph);

			graphLayout.clearNodePositions();
			graphLayout.layout(drawingArea);
			updateMinWindowSize(true);
		} else {

		}
		for (IDVINode node : dataGraph.getNodes()) {
			// Point2D position = graphLayout.getNodePosition(node);

			node.render(gl);
			// float relativePosX = (float) position.getX() / drawingAreaWidth;
			// float relativePosY = (float) position.getY() / drawingAreaHeight;
			// relativeNodePositions.put(node, new Pair<Float,
			// Float>(relativePosX,
			// relativePosY));
		}

		renderEdges(gl);

		gl.glEndList();

		// gl.glMatrixMode(GL2.GL_MODELVIEW);
		// gl.glPushMatrix();
		// gl.glTranslatef(2, 2, 2);
		// pixelGLConverter.getGLWidthForCurrentGLTransform(gl);
		// gl.glPopMatrix();

		applyAutomaticLayout = false;
		nodePositionsUpdated = false;

	}
	
	public Rectangle2D calculateGraphDrawingArea() {
		int drawingAreaWidth = pixelGLConverter.getPixelWidthForGLWidth(viewFrustum
				.getWidth()) - 2 * BOUNDS_SPACING_PIXELS;
		int drawingAreaHeight = pixelGLConverter.getPixelHeightForGLHeight(viewFrustum
				.getHeight()) - 2 * BOUNDS_SPACING_PIXELS;

		Rectangle2D drawingArea = new Rectangle();

		drawingArea.setFrame(BOUNDS_SPACING_PIXELS, BOUNDS_SPACING_PIXELS, drawingAreaWidth,
				drawingAreaHeight);
		
		return drawingArea;
	}

	public void updateMinWindowSize(boolean waitForMinSizeApplication) {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;

		for (IDVINode node : dataGraph.getNodes()) {
			Point2D position = graphLayout.getNodePosition(node);

			if (position.getX() - node.getWidthPixels() / 2.0f < minX) {
				minX = (int) (position.getX() - node.getWidthPixels() / 2.0f);
			}
			if (position.getX() + node.getWidthPixels() / 2.0f > maxX) {
				maxX = (int) (position.getX() + node.getWidthPixels() / 2.0f);
			}

			if (position.getY() - node.getHeightPixels() / 2.0f < minY) {
				minY = (int) (position.getY() - node.getHeightPixels() / 2.0f);
			}
			if (position.getY() + node.getHeightPixels() / 2.0f > maxY) {
				maxY = (int) (position.getY() + node.getHeightPixels() / 2.0f);
			}
		}

		// if (!dragAndDropController.isDragging()) {
		int minWidth = maxX - minX + 2 * BOUNDS_SPACING_PIXELS;
		int minHeight = maxY - minY + 2 * BOUNDS_SPACING_PIXELS;

		if (minWidth > minViewWidthPixels + 2 || minWidth < minViewWidthPixels - 2
				|| minHeight > minViewHeightPixels + 2
				|| minHeight < minViewHeightPixels - 2) {

			minViewWidthPixels = minWidth;
			minViewHeightPixels = minHeight;

			this.waitForMinSizeApplication = waitForMinSizeApplication;
			isMinSizeApplied = false;

			EventPublisher eventPublisher = GeneralManager.get().getEventPublisher();
			SetMinViewSizeEvent event = new SetMinViewSizeEvent();
			event.setMinViewSize(minViewWidthPixels, minViewHeightPixels);
			event.setView(this);
			eventPublisher.triggerEvent(event);
			// parentComposite.getParent();
		}
		// }
	}

	private void renderEdges(GL2 gl) {

		calcMaxDataAmount();

		connectionBandRenderer.init(gl);

		List<Edge> bandConnectedNodes = new ArrayList<Edge>();

		for (Edge edge : dataGraph.getAllEdges()) {

			// Works because there are no edges between view nodes
			if ((edge.getNode1() instanceof ViewNode)
					|| (edge.getNode2() instanceof ViewNode)) {
				// Render later transparent in foreground
				bandConnectedNodes.add(edge);
			} else {
				renderEdge(gl, edge, connectionBandRenderer);
			}
		}

		for (Edge edge : bandConnectedNodes) {
			renderEdge(gl, edge, connectionBandRenderer);
		}

	}

	private void renderEdge(GL2 gl, Edge edge,
			ConnectionBandRenderer connectionBandRenderer) {
		boolean highlight = false;
		if (edge.getNode1() == currentMouseOverNode
				|| edge.getNode2() == currentMouseOverNode) {
			highlight = true;
		}
		edge.getEdgeRenderer().renderEdge(gl, connectionBandRenderer, highlight);
	}

	private void calcMaxDataAmount() {
		for (ADataNode dataNode : dataNodes) {
			if (maxDataAmount < dataNode.getDataDomain().getDataAmount())
				maxDataAmount = dataNode.getDataDomain().getDataAmount();
		}
	}

	@Override
	protected void handlePickingEvents(PickingType pickingType, PickingMode pickingMode,
			int externalID, Pick pick) {
		if (detailLevel == EDetailLevel.VERY_LOW) {
			return;
		}

	}

	@Override
	public void broadcastElements(EVAOperation type) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumberOfSelections(SelectionType selectionType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedDVIView serializedForm = new SerializedDVIView();
		serializedForm.setViewID(this.getID());
		return serializedForm;
	}

	@Override
	public void initFromSerializableRepresentation(ASerializedView ser) {

	}

	@Override
	public void registerEventListeners() {
		super.registerEventListeners();

		newViewEventListener = new NewViewEventListener();
		newViewEventListener.setHandler(this);
		eventPublisher.addListener(NewViewEvent.class, newViewEventListener);

		viewClosedEventListener = new ViewClosedEventListener();
		viewClosedEventListener.setHandler(this);
		eventPublisher.addListener(ViewClosedEvent.class, viewClosedEventListener);

		dataContainersChangedListener = new DataContainersCangedListener();
		dataContainersChangedListener.setHandler(this);
		eventPublisher.addListener(DataContainersChangedEvent.class,
				dataContainersChangedListener);

		dimensionGroupsChangedEventListener = new DimensionGroupsChangedEventListener();
		dimensionGroupsChangedEventListener.setHandler(this);
		eventPublisher.addListener(DataDomainUpdateEvent.class,
				dimensionGroupsChangedEventListener);

		newDataDomainEventListener = new NewDataDomainEventListener();
		newDataDomainEventListener.setHandler(this);
		eventPublisher.addListener(NewDataDomainEvent.class, newDataDomainEventListener);

		addDataContainerEventListener = new AddDataContainerEventListener();
		addDataContainerEventListener.setHandler(this);
		eventPublisher.addListener(AddDataContainerEvent.class,
				addDataContainerEventListener);

		openViewEventListener = new OpenViewEventListener();
		openViewEventListener.setHandler(this);
		eventPublisher.addListener(OpenViewEvent.class, openViewEventListener);

		createViewFromDataContainerEventListener = new CreateViewFromDataContainerEventListener();
		createViewFromDataContainerEventListener.setHandler(this);
		eventPublisher.addListener(CreateViewFromDataContainerEvent.class,
				createViewFromDataContainerEventListener);

		applySpecificGraphLayoutEventListener = new ApplySpecificGraphLayoutEventListener();
		applySpecificGraphLayoutEventListener.setHandler(this);
		eventPublisher.addListener(ApplySpecificGraphLayoutEvent.class,
				applySpecificGraphLayoutEventListener);

		minSizeAppliedEventListener = new MinSizeAppliedEventListener();
		minSizeAppliedEventListener.setHandler(this);
		eventPublisher
				.addListener(MinSizeAppliedEvent.class, minSizeAppliedEventListener);

		showDataConnectionsEventListener = new ShowDataConnectionsEventListener();
		showDataConnectionsEventListener.setHandler(this);
		eventPublisher.addListener(ShowDataConnectionsEvent.class,
				showDataConnectionsEventListener);

		recordVAUpdateEventListener = new RecordVAUpdateEventListener();
		recordVAUpdateEventListener.setHandler(this);
		eventPublisher
				.addListener(RecordVAUpdateEvent.class, recordVAUpdateEventListener);

		dimensionVAUpdateEventListener = new DimensionVAUpdateEventListener();
		dimensionVAUpdateEventListener.setHandler(this);
		eventPublisher.addListener(DimensionVAUpdateEvent.class,
				dimensionVAUpdateEventListener);
	}

	@Override
	public void unregisterEventListeners() {
		super.unregisterEventListeners();

		if (newViewEventListener != null) {
			eventPublisher.removeListener(newViewEventListener);
			newViewEventListener = null;
		}

		if (viewClosedEventListener != null) {
			eventPublisher.removeListener(viewClosedEventListener);
			viewClosedEventListener = null;
		}

		if (dataContainersChangedListener != null) {
			eventPublisher.removeListener(dataContainersChangedListener);
			dataContainersChangedListener = null;
		}

		if (dimensionGroupsChangedEventListener != null) {
			eventPublisher.removeListener(dimensionGroupsChangedEventListener);
			dimensionGroupsChangedEventListener = null;
		}

		if (newDataDomainEventListener != null) {
			eventPublisher.removeListener(newDataDomainEventListener);
			newDataDomainEventListener = null;
		}

		if (addDataContainerEventListener != null) {
			eventPublisher.removeListener(addDataContainerEventListener);
			addDataContainerEventListener = null;
		}

		if (openViewEventListener != null) {
			eventPublisher.removeListener(openViewEventListener);
			openViewEventListener = null;
		}

		if (createViewFromDataContainerEventListener != null) {
			eventPublisher.removeListener(createViewFromDataContainerEventListener);
			createViewFromDataContainerEventListener = null;
		}

		if (applySpecificGraphLayoutEventListener != null) {
			eventPublisher.removeListener(applySpecificGraphLayoutEventListener);
			applySpecificGraphLayoutEventListener = null;
		}

		if (minSizeAppliedEventListener != null) {
			eventPublisher.removeListener(minSizeAppliedEventListener);
			minSizeAppliedEventListener = null;
		}

		if (showDataConnectionsEventListener != null) {
			eventPublisher.removeListener(showDataConnectionsEventListener);
			showDataConnectionsEventListener = null;
		}

		if (recordVAUpdateEventListener != null) {
			eventPublisher.removeListener(recordVAUpdateEventListener);
			recordVAUpdateEventListener = null;
		}

		if (dimensionVAUpdateEventListener != null) {
			eventPublisher.removeListener(dimensionVAUpdateEventListener);
			dimensionVAUpdateEventListener = null;
		}
	}

	@Override
	public void handleClearSelections() {
	}

	@Override
	public void handleRedrawView() {
		setDisplayListDirty();
	}

	public void setApplyAutomaticLayout(boolean applyAutomaticLayout) {
		this.applyAutomaticLayout = applyAutomaticLayout;

		if (applyAutomaticLayout) {
			minViewWidthPixels = 10;
			minViewHeightPixels = 10;

			isMinSizeApplied = false;
			waitForMinSizeApplication = true;

			EventPublisher eventPublisher = GeneralManager.get().getEventPublisher();
			SetMinViewSizeEvent event = new SetMinViewSizeEvent();
			event.setMinViewSize(minViewWidthPixels, minViewHeightPixels);
			event.setView(this);
			eventPublisher.triggerEvent(event);
		}
	}

	public void addView(AGLView view) {
		if (!view.isRenderedRemote() && view.isDataView()) {

			ViewNode node = nodeCreator.createViewNode(graphLayout, this,
					dragAndDropController, lastNodeID++, view);
			dataGraph.addNode(node);
			viewNodes.add(node);
			Set<IDataDomain> dataDomains = view.getDataDomains();
			if (dataDomains != null && !dataDomains.isEmpty()) {
				node.setDataDomains(dataDomains);
				for (IDataDomain dataDomain : dataDomains) {
					Set<ViewNode> viewNodes = viewNodesOfDataDomains.get(dataDomain);
					if (viewNodes == null) {
						viewNodes = new HashSet<ViewNode>();
					}
					viewNodes.add(node);
					viewNodesOfDataDomains.put(dataDomain, viewNodes);
					ADataNode dataNode = dataNodesOfDataDomains.get(dataDomain);
					if (dataNode != null) {
						Edge edge = dataGraph.addEdge(dataNode, node);
						AEdgeRenderer edgeRenderer = graphLayout
								.getLayoutSpecificEdgeRenderer(edge);
						edge.setEdgeRenderer(edgeRenderer);
						dataNode.update();
					}
				}
			}
			applyAutomaticLayout = true;
			setDisplayListDirty();
		}
	}

	public void removeView(AGLView view) {

		ViewNode viewNode = null;
		for (ViewNode node : viewNodes) {
			if (node.getRepresentedView() == view) {
				viewNode = node;
				break;
			}
		}

		if (viewNode == null)
			return;

		Set<IDataDomain> dataDomains = viewNode.getDataDomains();

		if (dataDomains != null) {
			for (IDataDomain dataDomain : dataDomains) {
				Set<ViewNode> viewNodes = viewNodesOfDataDomains.get(dataDomain);
				if (viewNodes != null) {
					viewNodes.remove(viewNode);
				}
			}
		}

		dataGraph.removeNode(viewNode);
		viewNodes.remove(viewNode);
		viewNode.destroy();
		// applyAutomaticLayout = true;
		setDisplayListDirty();
	}

	public void updateView(AGLView view) {

		if (view.isRenderedRemote())
			return;

		ViewNode viewNode = null;
		for (ViewNode node : viewNodes) {
			if (node.getRepresentedView() == view) {
				viewNode = node;
				break;
			}
		}

		if (viewNode == null)
			return;

		Set<IDataDomain> dataDomainsOfView = view.getDataDomains();
		if (dataDomainsOfView != null) {
			viewNode.setDataDomains(dataDomainsOfView);

			for (IDataDomain dataDomain : dataNodesOfDataDomains.keySet()) {

				Set<ViewNode> viewNodes = viewNodesOfDataDomains.get(dataDomain);
				if (dataDomainsOfView.contains(dataDomain)) {
					if (viewNodes == null) {
						viewNodes = new HashSet<ViewNode>();
					}
					viewNodes.add(viewNode);
					viewNodesOfDataDomains.put(dataDomain, viewNodes);
					ADataNode dataNode = dataNodesOfDataDomains.get(dataDomain);
					if (dataNode != null) {
						Edge edge = dataGraph.addEdge(dataNode, viewNode);
						AEdgeRenderer edgeRenderer = graphLayout
								.getLayoutSpecificEdgeRenderer(edge);
						edge.setEdgeRenderer(edgeRenderer);
					}
				} else {
					if (viewNodes != null) {
						viewNodes.remove(viewNode);
					}
					ADataNode dataNode = dataNodesOfDataDomains.get(dataDomain);
					if (dataNode != null) {
						dataGraph.removeEdge(dataNode, viewNode);
					}
				}
			}

			viewNode.update();
		}
	}

	public void updateDataDomain(IDataDomain dataDomain) {
		ADataNode dataNode = dataNodesOfDataDomains.get(dataDomain);
		if (dataNode != null) {
			dataNode.update();
			setDisplayListDirty();
		}
	}

	public void addDataDomain(IDataDomain dataDomain) {
		ADataNode dataNode = null;
		boolean nodeAdded = false;
		for (ADataNode node : dataNodes) {
			if (node.getDataDomain() == dataDomain) {
				dataNode = node;
				nodeAdded = true;
				break;
			}
		}
		if (!nodeAdded) {
			dataNode = nodeCreator.createDataNode(graphLayout, this,
					dragAndDropController, lastNodeID++, dataDomain);
			if (dataNode == null)
				return;
			dataGraph.addNode(dataNode);
			dataNodes.add(dataNode);
			dataNodesOfDataDomains.put(dataNode.getDataDomain(), dataNode);
		}

		DataDomainGraph dataDomainGraph = DataDomainManager.get().getDataDomainGraph();

		Set<IDataDomain> neighbors = dataDomainGraph.getNeighboursOf(dataDomain);

		for (IDataDomain neighborDataDomain : neighbors) {
			nodeAdded = false;
			for (ADataNode node : dataNodes) {
				if (node.getDataDomain() == neighborDataDomain) {
					Edge edge = dataGraph.addEdge(dataNode, node);
					AEdgeRenderer edgeRenderer = graphLayout
							.getLayoutSpecificEdgeRenderer(edge);
					edge.setEdgeRenderer(edgeRenderer);
					nodeAdded = true;
					break;
				}
			}
			if (!nodeAdded) {
				ADataNode node = nodeCreator.createDataNode(graphLayout, this,
						dragAndDropController, lastNodeID++, neighborDataDomain);
				if (node == null)
					return;
				dataGraph.addNode(node);
				dataNodes.add(node);
				dataNodesOfDataDomains.put(node.getDataDomain(), node);
				Edge edge = dataGraph.addEdge(dataNode, node);
				AEdgeRenderer edgeRenderer = graphLayout
						.getLayoutSpecificEdgeRenderer(edge);
				edge.setEdgeRenderer(edgeRenderer);
			}
		}

		applyAutomaticLayout = true;
		setDisplayListDirty();
	}

	// public TextureManager getTextureManager() {
	// return textureManager;
	// }

	public int getMaxDataAmount() {
		return maxDataAmount;
	}

	public String getEdgeLabel(ADataNode node1, ADataNode node2) {

		DataDomainGraph dataDomainGraph = DataDomainManager.get().getDataDomainGraph();

		Set<org.caleydo.core.data.datadomain.graph.Edge> edges = dataDomainGraph
				.getEdges(node1.getDataDomain(), node2.getDataDomain());

		StringBuffer stringBuffer = new StringBuffer();

		Iterator<org.caleydo.core.data.datadomain.graph.Edge> iterator = edges.iterator();
		while (iterator.hasNext()) {
			org.caleydo.core.data.datadomain.graph.Edge e = iterator.next();
			IDCategory category = e.getIdCategory();
			if (category != null) {
				stringBuffer.append(e.getIdCategory().getCategoryName());
			} else {
				stringBuffer.append("Unknown Mapping");
			}
			if (iterator.hasNext()) {
				stringBuffer.append(", ");
			}
		}

		return stringBuffer.toString();
	}

	// public void createDataContainer(ATableBasedDataDomain dataDomain,
	// String recordPerspectiveID, String dimensionPerspectiveID,
	// boolean createDimensionPerspective, DimensionVirtualArray dimensionVA,
	// Group group) {
	//
	// DimensionPerspective dimensionPerspective = null;
	//
	// if (createDimensionPerspective) {
	// dimensionPerspective = new DimensionPerspective(dataDomain);
	// List<Integer> indices = dimensionVA.getSubList(group.getStartIndex(),
	// group.getEndIndex() + 1);
	// PerspectiveInitializationData data = new PerspectiveInitializationData();
	// data.setData(indices);
	// dimensionPerspective.init(data);
	// // TODO: Shall we really set it private?
	// dimensionPerspective.setPrivate(true);
	// group.setPerspectiveID(dimensionPerspective.getID());
	// dataDomain.getTable().registerDimensionPerspective(dimensionPerspective);
	// } else {
	// dimensionPerspective = dataDomain.getTable().getDimensionPerspective(
	// dimensionPerspectiveID);
	// }
	//
	// // FIXME: This should only be a datacontainer in the future
	// TableBasedDimensionGroupData data = new
	// TableBasedDimensionGroupData(dataDomain,
	// dataDomain.getTable().getRecordPerspective(recordPerspectiveID),
	// dimensionPerspective);
	// dataDomain.addDimensionGroup(data);
	//
	//
	// }

	/**
	 * FIXME:
	 * DOKU!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !!!!!!!!!!!
	 * 
	 * @param dataDomain
	 * @param recordPerspectiveID
	 * @param createRecordPerspective
	 * @param recordVA
	 * @param recordGroup
	 * @param dimensionPerspectiveID
	 * @param createDimensionPerspective
	 * @param dimensionVA
	 * @param dimensionGroup
	 */
	public void createDataContainer(final ATableBasedDataDomain dataDomain,
			final String recordPerspectiveID, final boolean createRecordPerspective,
			final RecordVirtualArray recordVA, final Group recordGroup,
			final String dimensionPerspectiveID,
			final boolean createDimensionPerspective,
			final DimensionVirtualArray dimensionVA, final Group dimensionGroup) {

		final String recordPerspectiveLabel = (createRecordPerspective) ? (recordGroup
				.getLabel()) : dataDomain.getTable()
				.getRecordPerspective(recordPerspectiveID).getLabel();

		final String dimensionPerspectiveLabel = (createDimensionPerspective) ? (dimensionGroup
				.getLabel()) : dataDomain.getTable()
				.getDimensionPerspective(dimensionPerspectiveID).getLabel();

		parentComposite.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {

				IInputValidator validator = new IInputValidator() {
					@Override
					public String isValid(String newText) {
						if (newText.equalsIgnoreCase(""))
							return "Please enter a name for the data container.";
						else
							return null;
					}
				};

				InputDialog dialog = new InputDialog(new Shell(),
						"Create Data Container", "Name", dataDomain.getLabel() + " - "
								+ recordPerspectiveLabel + "/"
								+ dimensionPerspectiveLabel, validator);

				String currentDimensionPerspeciveID = dimensionPerspectiveID;
				String currentRecordPerspeciveID = recordPerspectiveID;

				if (dialog.open() == Window.OK) {
					DimensionPerspective dimensionPerspective = null;

					if (createDimensionPerspective) {
						dimensionPerspective = new DimensionPerspective(dataDomain);
						List<Integer> indices = dimensionVA.getIDsOfGroup(dimensionGroup
								.getGroupIndex());
						PerspectiveInitializationData data = new PerspectiveInitializationData();
						data.setData(indices);
						dimensionPerspective.init(data);
						dimensionPerspective.setLabel(dimensionPerspectiveLabel, true);
						// TODO: Shall we really set it private?
						dimensionPerspective.setPrivate(true);
						dimensionGroup.setPerspectiveID(dimensionPerspective.getID());
						dataDomain.getTable().registerDimensionPerspective(
								dimensionPerspective);
						currentDimensionPerspeciveID = dimensionPerspective.getID();
					} else {
						dimensionPerspective = dataDomain.getTable()
								.getDimensionPerspective(dimensionPerspectiveID);
					}

					RecordPerspective recordPerspective = null;

					if (createRecordPerspective) {
						recordPerspective = new RecordPerspective(dataDomain);
						List<Integer> indices = recordVA.getIDsOfGroup(recordGroup
								.getGroupIndex());
						PerspectiveInitializationData data = new PerspectiveInitializationData();
						data.setData(indices);
						recordPerspective.init(data);
						recordPerspective.setLabel(recordPerspectiveLabel, true);
						// TODO: Shall we really set it private?
						recordPerspective.setPrivate(true);
						recordGroup.setPerspectiveID(recordPerspective.getID());
						dataDomain.getTable()
								.registerRecordPerspective(recordPerspective);
						currentRecordPerspeciveID = recordPerspective.getID();
					} else {
						recordPerspective = dataDomain.getTable().getRecordPerspective(
								recordPerspectiveID);
					}

					DataContainer dataContainer = dataDomain.getDataContainer(
							currentRecordPerspeciveID, currentDimensionPerspeciveID);
					dataContainer.setLabel(dialog.getValue(), false);
					dataContainer.setPrivate(false);

					if (isRenderedRemote()) {
						AddDataContainersEvent event = new AddDataContainersEvent(
								dataContainer);
						event.setReceiver((AGLView) getRemoteRenderingGLView());
						event.setSender(this);
						GeneralManager.get().getEventPublisher().triggerEvent(event);
					}
				}
			}
		});
	}

	public void openView(AGLView view) {
		final ARcpGLViewPart viewPart = GeneralManager.get().getViewManager()
				.getViewPartFromView(view);

		parentComposite.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.activate(viewPart);
			}
		});

	}

	public void createView(final String viewType, final IDataDomain dataDomain,
			final DataContainer dataContainer) {

		parentComposite.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {

				try {

					String secondaryID = UUID.randomUUID().toString();
					RCPViewInitializationData rcpViewInitData = new RCPViewInitializationData();
					rcpViewInitData.setDataDomainID(dataDomain.getDataDomainID());
					rcpViewInitData.setDataContainer(dataContainer);
					RCPViewManager.get().addRCPView(secondaryID, rcpViewInitData);

					if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
						PlatformUI
								.getWorkbench()
								.getActiveWorkbenchWindow()
								.getActivePage()
								.showView(viewType, secondaryID,
										IWorkbenchPage.VIEW_ACTIVATE);

					}
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	public void setCurrentMouseOverNode(IDVINode currentMouseOverNode) {
		this.currentMouseOverNode = currentMouseOverNode;
	}

	public IDVINode getCurrentMouseOverNode() {
		return currentMouseOverNode;
	}

	public void applyGraphLayout(Class<? extends AGraphLayout> graphLayoutClass) {

		try {
			graphLayout = graphLayoutClass.getConstructor(GLDataViewIntegrator.class,
					Graph.class).newInstance(this, dataGraph);

			for (IDVINode node : dataGraph.getNodes()) {
				node.setGraphLayout(graphLayout);
			}
		} catch (Exception e) {
			Logger.log(new Status(Status.ERROR, this.toString(),
					"Failed to create Graph Layout", e));
		}

		setApplyAutomaticLayout(true);
		setDisplayListDirty();
	}

	public boolean isNodePositionsUpdated() {
		return nodePositionsUpdated;
	}

	public void setNodePositionsUpdated(boolean nodePositionsUpdated) {
		this.nodePositionsUpdated = nodePositionsUpdated;
	}

	public boolean isMinSizeApplied() {
		return isMinSizeApplied;
	}

	public void setMinSizeApplied(boolean isMinSizeApplied) {
		this.isMinSizeApplied = isMinSizeApplied;
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		super.reshape(drawable, x, y, width, height);

		if (!waitForMinSizeApplication && isRendered) {

			Rectangle2D drawingArea = calculateGraphDrawingArea();
			
			graphLayout.applyIncrementalLayout(drawingArea);

//			for (IDVINode node : dataGraph.getNodes()) {
//				Pair<Float, Float> relativePosition = relativeNodePositions.get(node);
//				graphLayout.setNodePosition(node,
//						new Point2D.Double(
//								relativePosition.getFirst() * drawingAreaWidth,
//								relativePosition.getSecond() * drawingAreaHeight));
//			}

			// updateMinWindowSize(true);
		}
	}

	public ADataNode getDataNode(IDataDomain dataDomain) {
		return dataNodesOfDataDomains.get(dataDomain);
	}

	public Set<ViewNode> getViewNodes() {
		return viewNodes;
	}

	public AGraphLayout getGraphLayout() {
		return graphLayout;
	}

	public Collection<IDVINode> getAllNodes() {
		return dataGraph.getNodes();
	}

	public void showDataConnections(boolean showDataConnections) {
		this.showDataConnections = showDataConnections;
		setDisplayListDirty();
	}

	public boolean isShowDataConnections() {
		return showDataConnections;
	}

	/**
	 * @param isVendingMachineMode
	 *            setter, see {@link #isVendingMachineMode}
	 */
	public void setVendingMachineMode(boolean isVendingMachineMode) {
		this.isVendingMachineMode = isVendingMachineMode;
	}

	/**
	 * @return the isVendingMachineMode, see {@link #isVendingMachineMode}
	 */
	public boolean isVendingMachineMode() {
		return isVendingMachineMode;
	}
}
