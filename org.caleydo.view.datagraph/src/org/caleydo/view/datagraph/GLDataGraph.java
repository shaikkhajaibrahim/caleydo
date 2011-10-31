package org.caleydo.view.datagraph;

import gleem.linalg.Vec3f;

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
import javax.media.opengl.awt.GLCanvas;

import org.caleydo.core.data.container.DataContainer;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainGraph;
import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.data.datadomain.Edge;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.id.IDCategory;
import org.caleydo.core.data.perspective.DimensionPerspective;
import org.caleydo.core.data.perspective.PerspectiveInitializationData;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.virtualarray.DimensionVirtualArray;
import org.caleydo.core.data.virtualarray.EVAOperation;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.event.data.DimensionGroupsChangedEvent;
import org.caleydo.core.event.data.NewDataDomainEvent;
import org.caleydo.core.event.view.DataDomainsChangedEvent;
import org.caleydo.core.event.view.NewViewEvent;
import org.caleydo.core.event.view.ViewClosedEvent;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.ARcpGLViewPart;
import org.caleydo.core.view.RCPViewInitializationData;
import org.caleydo.core.view.RCPViewManager;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.DetailLevel;
import org.caleydo.core.view.opengl.canvas.listener.IViewCommandHandler;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.core.view.opengl.picking.PickingType;
import org.caleydo.core.view.opengl.util.draganddrop.DragAndDropController;
import org.caleydo.core.view.opengl.util.spline.ConnectionBandRenderer;
import org.caleydo.core.view.opengl.util.text.CaleydoTextRenderer;
import org.caleydo.core.view.opengl.util.texture.TextureManager;
import org.caleydo.view.datagraph.bandlayout.EdgeBandRenderer;
import org.caleydo.view.datagraph.bandlayout.IEdgeRoutingStrategy;
import org.caleydo.view.datagraph.bandlayout.SimpleEdgeRoutingStrategy;
import org.caleydo.view.datagraph.event.AddDataContainerEvent;
import org.caleydo.view.datagraph.event.ApplySpringBasedLayoutEvent;
import org.caleydo.view.datagraph.event.CreateViewFromDataContainerEvent;
import org.caleydo.view.datagraph.event.OpenViewEvent;
import org.caleydo.view.datagraph.listener.AddDataContainerEventListener;
import org.caleydo.view.datagraph.listener.ApplySpringBasedLayoutEventListener;
import org.caleydo.view.datagraph.listener.CreateViewFromDataContainerEventListener;
import org.caleydo.view.datagraph.listener.DataDomainsChangedEventListener;
import org.caleydo.view.datagraph.listener.DimensionGroupsChangedEventListener;
import org.caleydo.view.datagraph.listener.GLDataGraphKeyListener;
import org.caleydo.view.datagraph.listener.NewDataDomainEventListener;
import org.caleydo.view.datagraph.listener.NewViewEventListener;
import org.caleydo.view.datagraph.listener.OpenViewEventListener;
import org.caleydo.view.datagraph.listener.ViewClosedEventListener;
import org.caleydo.view.datagraph.node.ADataNode;
import org.caleydo.view.datagraph.node.IDataGraphNode;
import org.caleydo.view.datagraph.node.NodeCreator;
import org.caleydo.view.datagraph.node.ViewNode;
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
public class GLDataGraph extends AGLView implements IViewCommandHandler {

	public final static String VIEW_TYPE = "org.caleydo.view.datagraph";

	public final static int BOUNDS_SPACING_PIXELS = 10;

	private GLDataGraphKeyListener glKeyListener;
	private boolean useDetailLevel = false;

	private Graph<IDataGraphNode> dataGraph;
	private AGraphLayout graphLayout;
	private int maxNodeWidthPixels;
	private int maxNodeHeightPixels;
	private DragAndDropController dragAndDropController;
	private boolean applyAutomaticLayout;
	private Map<IDataGraphNode, Pair<Float, Float>> relativeNodePositions;
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
	private DataDomainsChangedEventListener dataDomainsChangedEventListener;
	private DimensionGroupsChangedEventListener dimensionGroupsChangedEventListener;
	private AddDataContainerEventListener addDataContainerEventListener;
	private OpenViewEventListener openViewEventListener;
	private CreateViewFromDataContainerEventListener createViewFromDataContainerEventListener;
	private ApplySpringBasedLayoutEventListener applySpringBasedLayoutEventListener;

	private IDataGraphNode currentMouseOverNode;

	private NodeCreator nodeCreator;

	/**
	 * Constructor.
	 */
	public GLDataGraph(GLCanvas glCanvas, Composite parentComposite,
			ViewFrustum viewFrustum) {

		super(glCanvas, parentComposite, viewFrustum);

		connectionBandRenderer = new ConnectionBandRenderer();
		viewType = GLDataGraph.VIEW_TYPE;
		glKeyListener = new GLDataGraphKeyListener();
		dataGraph = new Graph<IDataGraphNode>();
		graphLayout = new BipartiteGraphLayout(this, dataGraph);
		// graphLayout = new ForceDirectedGraphLayout(this, dataGraph);
		relativeNodePositions = new HashMap<IDataGraphNode, Pair<Float, Float>>();
		dragAndDropController = new DragAndDropController(this);
		dataNodes = new HashSet<ADataNode>();
		viewNodes = new HashSet<ViewNode>();
		viewNodesOfDataDomains = new HashMap<IDataDomain, Set<ViewNode>>();
		dataNodesOfDataDomains = new HashMap<IDataDomain, ADataNode>();
		nodeCreator = new NodeCreator();

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

		maxNodeWidthPixels = Integer.MIN_VALUE;
		maxNodeHeightPixels = Integer.MIN_VALUE;

		for (IDataGraphNode node : dataGraph.getNodes()) {
			if (node.getHeightPixels() > maxNodeHeightPixels)
				maxNodeHeightPixels = node.getHeightPixels();

			if (node.getWidthPixels() > maxNodeWidthPixels)
				maxNodeWidthPixels = node.getWidthPixels();
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
	}

	@Override
	public void setDetailLevel(DetailLevel detailLevel) {
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

		if (isDisplayListDirty) {
			buildDisplayList(gl, displayListIndex);
			isDisplayListDirty = false;
		}
		gl.glCallList(displayListIndex);

		dragAndDropController.handleDragging(gl, glMouseListener);

		if (!lazyMode)
			checkForHits(gl);
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

		int drawingAreaWidth = pixelGLConverter.getPixelWidthForGLWidth(viewFrustum
				.getWidth()) - 2 * BOUNDS_SPACING_PIXELS;
		int drawingAreaHeight = pixelGLConverter.getPixelHeightForGLHeight(viewFrustum
				.getHeight()) - 2 * BOUNDS_SPACING_PIXELS;
		if (applyAutomaticLayout) {
			// graphLayout.setGraph(dataGraph);
			Rectangle2D rect = new Rectangle();

			rect.setFrame(BOUNDS_SPACING_PIXELS, BOUNDS_SPACING_PIXELS, drawingAreaWidth,
					drawingAreaHeight);
			graphLayout.clearNodePositions();
			graphLayout.layout(rect);
		} else {
			if (!dragAndDropController.isDragging() && !nodePositionsUpdated) {
				for (IDataGraphNode node : dataGraph.getNodes()) {
					Pair<Float, Float> relativePosition = relativeNodePositions.get(node);
					graphLayout.setNodePosition(node,
							new Point2D.Double(relativePosition.getFirst()
									* drawingAreaWidth, relativePosition.getSecond()
									* drawingAreaHeight));
				}
			}
		}

		for (IDataGraphNode node : dataGraph.getNodes()) {
			Point2D position = graphLayout.getNodePosition(node);
			float relativePosX = (float) position.getX() / drawingAreaWidth;
			float relativePosY = (float) position.getY() / drawingAreaHeight;
			relativeNodePositions.put(node, new Pair<Float, Float>(relativePosX,
					relativePosY));

			node.render(gl);

		}
		renderEdges(gl);

		gl.glEndList();

		applyAutomaticLayout = false;
		nodePositionsUpdated = false;

	}

	private void renderEdges(GL2 gl) {

		List<Pair<IDataGraphNode, IDataGraphNode>> bandConnectedNodes = new ArrayList<Pair<IDataGraphNode, IDataGraphNode>>();

		for (Pair<IDataGraphNode, IDataGraphNode> edge : dataGraph.getAllEdges()) {

			// Works because there are no edges between view nodes
			if ((edge.getFirst() instanceof ViewNode)
					|| (edge.getSecond() instanceof ViewNode)) {
				// Render later transparent in foreground
				bandConnectedNodes.add(edge);
			} else {

				boolean renderEdgeLabels = false;
				if (edge.getFirst() == currentMouseOverNode
						|| edge.getSecond() == currentMouseOverNode) {
					renderEdgeLabels = true;
				}

				gl.glPushAttrib(GL2.GL_LINE_BIT | GL2.GL_COLOR_BUFFER_BIT);
				gl.glColor3f(0.6f, 0.6f, 0.6f);
				gl.glLineWidth(2);
				gl.glEnable(GL2.GL_LINE_STIPPLE);
				gl.glLineStipple(3, (short) 127);

				// gl.glBegin(GL2.GL_LINES);
				Point2D position1 = edge.getFirst().getPosition();
				Point2D position2 = edge.getSecond().getPosition();

				List<Point2D> edgePoints = new ArrayList<Point2D>();
				edgePoints.add(position1);
				edgePoints.add(position2);

				IEdgeRoutingStrategy routingStrategy = new SimpleEdgeRoutingStrategy(
						dataGraph);
				routingStrategy.createEdge(edgePoints);

				edgePoints.add(0, position1);
				edgePoints.add(position2);

				connectionBandRenderer.init(gl);

				if (renderEdgeLabels) {
					renderLabeledCurve(gl, edgePoints, edge);
				} else {
					gl.glPushMatrix();
					gl.glTranslatef(0, 0, -0.1f);
					connectionBandRenderer.renderInterpolatedCurve(gl, edgePoints);
					gl.glPopMatrix();
				}
				gl.glPopAttrib();
			}
		}

		calcMaxDataAmount();

		for (Pair<IDataGraphNode, IDataGraphNode> edge : bandConnectedNodes) {
			renderConnectionBands(gl, edge.getFirst(), edge.getSecond());
		}

	}

	private void renderLabeledCurve(GL2 gl, List<Point2D> edgePoints,
			Pair<IDataGraphNode, IDataGraphNode> edge) {
		gl.glPushMatrix();
		gl.glTranslatef(0, 0, -0.1f);
		List<Vec3f> curvePoints = connectionBandRenderer.calcInterpolatedCurve(gl,
				edgePoints);

		Vec3f startPoint = curvePoints.get(0);
		Vec3f endPoint = curvePoints.get(curvePoints.size() - 1);
		Vec3f centerPoint = startPoint;
		float distanceDelta = centerPoint.minus(endPoint).lengthSquared();

		gl.glBegin(GL2.GL_LINE_STRIP);
		for (Vec3f point : curvePoints) {
			gl.glVertex3f(point.x(), point.y(), point.z());
			float distanceStart = point.minus(startPoint).lengthSquared();
			float dinstanceEnd = point.minus(endPoint).lengthSquared();
			float currentDistanceDelta = Math.abs(distanceStart - dinstanceEnd);
			if (currentDistanceDelta < distanceDelta) {
				distanceDelta = currentDistanceDelta;
				centerPoint = point;
			}
		}
		gl.glEnd();

		gl.glPopMatrix();

		ADataNode node1 = (ADataNode) edge.getFirst();
		ADataNode node2 = (ADataNode) edge.getSecond();

		DataDomainGraph dataDomainGraph = DataDomainManager.get().getDataDomainGraph();

		Set<Edge> edges = dataDomainGraph.getEdges(node1.getDataDomain(),
				node2.getDataDomain());

		StringBuffer stringBuffer = new StringBuffer();

		Iterator<Edge> iterator = edges.iterator();
		while (iterator.hasNext()) {
			Edge e = iterator.next();
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

		String edgeLabel = stringBuffer.toString();

		float height = pixelGLConverter.getGLHeightForPixelHeight(14);
		float requiredWidth = textRenderer.getRequiredTextWidth(edgeLabel, height);

		textRenderer.renderTextInBounds(gl, edgeLabel, centerPoint.x()
				- (requiredWidth / 2.0f), centerPoint.y() - (height / 2.0f),
				centerPoint.z() + 0.1f, requiredWidth, height);
	}

	private void renderConnectionBands(GL2 gl, IDataGraphNode node1, IDataGraphNode node2) {

		EdgeBandRenderer bandRenderer = new EdgeBandRenderer(node1, node2,
				pixelGLConverter, viewFrustum, maxDataAmount);

		bandRenderer
				.renderEdgeBand(
						gl,
						new SimpleEdgeRoutingStrategy(dataGraph),
						(node1 == getCurrentMouseOverNode() || node2 == getCurrentMouseOverNode()));
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
		if (detailLevel == DetailLevel.VERY_LOW) {
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
		SerializedDataGraphView serializedForm = new SerializedDataGraphView();
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

		dataDomainsChangedEventListener = new DataDomainsChangedEventListener();
		dataDomainsChangedEventListener.setHandler(this);
		eventPublisher.addListener(DataDomainsChangedEvent.class,
				dataDomainsChangedEventListener);

		dimensionGroupsChangedEventListener = new DimensionGroupsChangedEventListener();
		dimensionGroupsChangedEventListener.setHandler(this);
		eventPublisher.addListener(DimensionGroupsChangedEvent.class,
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

		applySpringBasedLayoutEventListener = new ApplySpringBasedLayoutEventListener();
		applySpringBasedLayoutEventListener.setHandler(this);
		eventPublisher.addListener(ApplySpringBasedLayoutEvent.class,
				applySpringBasedLayoutEventListener);
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

		if (dataDomainsChangedEventListener != null) {
			eventPublisher.removeListener(dataDomainsChangedEventListener);
			dataDomainsChangedEventListener = null;
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

		if (applySpringBasedLayoutEventListener != null) {
			eventPublisher.removeListener(applySpringBasedLayoutEventListener);
			applySpringBasedLayoutEventListener = null;
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
						dataGraph.addEdge(dataNode, node);
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

		Set<IDataDomain> dataDomains = view.getDataDomains();
		if (dataDomains != null && !dataDomains.isEmpty()) {
			viewNode.setDataDomains(dataDomains);
			for (IDataDomain dataDomain : dataDomains) {
				Set<ViewNode> viewNodes = viewNodesOfDataDomains.get(dataDomain);
				if (viewNodes == null) {
					viewNodes = new HashSet<ViewNode>();
				}
				viewNodes.add(viewNode);
				viewNodesOfDataDomains.put(dataDomain, viewNodes);
				ADataNode dataNode = dataNodesOfDataDomains.get(dataDomain);
				if (dataNode != null) {
					dataGraph.addEdge(dataNode, viewNode);
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
					dataGraph.addEdge(dataNode, node);
					nodeAdded = true;
					break;
				}
			}
			if (!nodeAdded) {
				ADataNode node = nodeCreator.createDataNode(graphLayout, this,
						dragAndDropController, lastNodeID++, neighborDataDomain);
				dataGraph.addNode(node);
				dataNodes.add(node);
				dataNodesOfDataDomains.put(node.getDataDomain(), node);
				dataGraph.addEdge(dataNode, node);
			}
		}

		applyAutomaticLayout = true;
		setDisplayListDirty();
	}

	public TextureManager getTextureManager() {
		return textureManager;
	}

	public int getMaxDataAmount() {
		return maxDataAmount;
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

	public void createDataContainer(final ATableBasedDataDomain dataDomain,
			final String recordPerspectiveID, final String dimensionPerspectiveID,
			final boolean createDimensionPerspective,
			final DimensionVirtualArray dimensionVA, final Group group) {

		final String recordPerspectiveLabel = dataDomain.getTable()
				.getRecordPerspective(recordPerspectiveID).getLabel();

		final String dimensionPerspeciveLabel = (createDimensionPerspective) ? (group
				.getClusterNode().getLabel()) : dataDomain.getTable()
				.getDimensionPerspective(dimensionPerspectiveID).getLabel();

		parentComposite.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {

				IInputValidator validator = new IInputValidator() {
					public String isValid(String newText) {
						if (newText.equalsIgnoreCase(""))
							return "Please enter a name for the data container.";
						else
							return null;
					}
				};

				InputDialog dialog = new InputDialog(new Shell(),
						"Create Data Container", "Name", dimensionPerspeciveLabel + "/"
								+ recordPerspectiveLabel, validator);

				String currentdimensionPerspeciveID = dimensionPerspectiveID;

				if (dialog.open() == Window.OK) {
					DimensionPerspective dimensionPerspective = null;

					if (createDimensionPerspective) {
						dimensionPerspective = new DimensionPerspective(dataDomain);
						List<Integer> indices = dimensionVA.getSubList(
								group.getStartIndex(), group.getEndIndex() + 1);
						PerspectiveInitializationData data = new PerspectiveInitializationData();
						data.setData(indices);
						dimensionPerspective.init(data);
						dimensionPerspective.setLabel(dimensionPerspeciveLabel);
						// TODO: Shall we really set it private?
						dimensionPerspective.setPrivate(true);
						group.setPerspectiveID(dimensionPerspective.getID());
						dataDomain.getTable().registerDimensionPerspective(
								dimensionPerspective);
						currentdimensionPerspeciveID = dimensionPerspective.getID();
					} else {
						dimensionPerspective = dataDomain.getTable()
								.getDimensionPerspective(dimensionPerspectiveID);
					}

					DataContainer dataContainer = dataDomain.getDataContainer(
							recordPerspectiveID, currentdimensionPerspeciveID);
					dataContainer.setLabel(dialog.getValue());

					// DataContainer dataContainer = new
					// DataContainer(dataDomain,
					// dataDomain.getTable().getRecordPerspective(
					// recordPerspectiveID), dimensionPerspective);

					// FIXME: This should only be a datacontainer in the future

					// dataDomain.addDimensionGroup(data);
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
			DataContainer dataContainer) {

		parentComposite.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {

				try {

					String secondaryID = UUID.randomUUID().toString();
					RCPViewInitializationData rcpViewInitData = new RCPViewInitializationData();
					rcpViewInitData.setDataDomainID(dataDomain.getDataDomainID());
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

	public void setCurrentMouseOverNode(IDataGraphNode currentMouseOverNode) {
		this.currentMouseOverNode = currentMouseOverNode;
	}

	public IDataGraphNode getCurrentMouseOverNode() {
		return currentMouseOverNode;
	}

	public void applySpringBasedLayout() {
		// TODO: Choose correct layout
		setApplyAutomaticLayout(true);
		setDisplayListDirty();
	}

	public boolean isNodePositionsUpdated() {
		return nodePositionsUpdated;
	}

	public void setNodePositionsUpdated(boolean nodePositionsUpdated) {
		this.nodePositionsUpdated = nodePositionsUpdated;
	}

	@Override
	public void handleUpdateView() {
		setDisplayListDirty();
	}
}
