package org.caleydo.view.pathway;

import gleem.linalg.Vec3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.id.IDCategory;
import org.caleydo.core.data.id.IDType;
import org.caleydo.core.data.id.ManagedObjectType;
import org.caleydo.core.data.mapping.IDMappingManager;
import org.caleydo.core.data.selection.ESelectionCommandType;
import org.caleydo.core.data.selection.SelectedElementRep;
import org.caleydo.core.data.selection.SelectionCommand;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.selection.delta.SelectionDelta;
import org.caleydo.core.data.selection.delta.SelectionDeltaItem;
import org.caleydo.core.data.virtualarray.EVAOperation;
import org.caleydo.core.data.virtualarray.delta.RecordVADelta;
import org.caleydo.core.data.virtualarray.delta.VADeltaItem;
import org.caleydo.core.data.virtualarray.events.RecordVADeltaEvent;
import org.caleydo.core.data.virtualarray.events.RecordVADeltaListener;
import org.caleydo.core.data.virtualarray.events.ReplaceRecordPerspectiveListener;
import org.caleydo.core.event.view.ClearSelectionsEvent;
import org.caleydo.core.event.view.SelectionCommandEvent;
import org.caleydo.core.event.view.SwitchDataRepresentationEvent;
import org.caleydo.core.event.view.pathway.DisableGeneMappingEvent;
import org.caleydo.core.event.view.pathway.EnableGeneMappingEvent;
import org.caleydo.core.event.view.remote.LoadPathwayEvent;
import org.caleydo.core.event.view.tablebased.RedrawViewEvent;
import org.caleydo.core.event.view.tablebased.SelectionUpdateEvent;
import org.caleydo.core.gui.preferences.PreferenceConstants;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.logging.Logger;
import org.caleydo.core.view.ITableBasedDataDomainView;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.DetailLevel;
import org.caleydo.core.view.opengl.canvas.listener.ClearSelectionsListener;
import org.caleydo.core.view.opengl.canvas.listener.ISelectionCommandHandler;
import org.caleydo.core.view.opengl.canvas.listener.ISelectionUpdateHandler;
import org.caleydo.core.view.opengl.canvas.listener.IViewCommandHandler;
import org.caleydo.core.view.opengl.canvas.listener.RedrawViewListener;
import org.caleydo.core.view.opengl.canvas.listener.SelectionCommandListener;
import org.caleydo.core.view.opengl.canvas.listener.SelectionUpdateListener;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.core.view.opengl.picking.PickingType;
import org.caleydo.core.view.vislink.ConnectedElementRepresentationManager;
import org.caleydo.datadomain.genetic.contextmenu.container.GeneRecordContextMenuItemContainer;
import org.caleydo.datadomain.genetic.contextmenu.item.LoadPathwaysByPathwayItem;
import org.caleydo.datadomain.pathway.PathwayDataDomain;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexGraphItem;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexGraphItemRep;
import org.caleydo.datadomain.pathway.manager.PathwayDatabaseType;
import org.caleydo.datadomain.pathway.manager.PathwayItemManager;
import org.caleydo.datadomain.pathway.manager.PathwayManager;
import org.caleydo.util.graph.EGraphItemKind;
import org.caleydo.util.graph.EGraphItemProperty;
import org.caleydo.util.graph.IGraphItem;
import org.caleydo.view.pathway.listener.DisableGeneMappingListener;
import org.caleydo.view.pathway.listener.EnableGeneMappingListener;
import org.caleydo.view.pathway.listener.SwitchDataRepresentationListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;

/**
 * Single OpenGL2 pathway view
 * 
 * @author Marc Streit
 * @author Alexander Lex
 */
public class GLPathway extends AGLView implements ITableBasedDataDomainView,
		ISelectionUpdateHandler, IViewCommandHandler, ISelectionCommandHandler {

	public final static String VIEW_TYPE = "org.caleydo.view.pathway";

	private ATableBasedDataDomain dataDomain;
	protected PathwayDataDomain pathwayDataDomain;
	private PathwayGraph pathway;

	private boolean bEnablePathwayTexture = true;

	private PathwayManager pathwayManager;
	private PathwayItemManager pathwayItemManager;

	private GLPathwayContentCreator gLPathwayContentCreator;

	private ConnectedElementRepresentationManager connectedElementRepresentationManager;

	private SelectionManager selectionManager;

	/**
	 * Own texture manager is needed for each GL2 context, because textures
	 * cannot be bound to multiple GL2 contexts.
	 */
	private HashMap<GL, GLPathwayTextureManager> hashGLcontext2TextureManager;

	private Vec3f vecScaling;
	private Vec3f vecTranslation;

	int iCurrentDimensionIndex = -1;

	protected EnableGeneMappingListener enableGeneMappingListener;
	protected DisableGeneMappingListener disableGeneMappingListener;

	protected SelectionUpdateListener selectionUpdateListener;
	protected RecordVADeltaListener virtualArrayUpdateListener;

	protected ReplaceRecordPerspectiveListener replaceVirtualArrayListener;

	protected RedrawViewListener redrawViewListener;
	protected ClearSelectionsListener clearSelectionsListener;

	protected SelectionCommandListener selectionCommandListener;

	protected SwitchDataRepresentationListener switchDataRepresentationListener;

	/**
	 * Constructor.
	 */
	public GLPathway(GLCanvas glCanvas, Composite parentComposite, ViewFrustum viewFrustum) {

		super(glCanvas, parentComposite, viewFrustum);
		viewType = VIEW_TYPE;

		pathwayManager = PathwayManager.get();
		pathwayItemManager = PathwayItemManager.get();

		hashGLcontext2TextureManager = new HashMap<GL, GLPathwayTextureManager>();
		// hashPathwayContainingSelectedVertex2VertexCount = new
		// HashMap<Integer, Integer>();

		connectedElementRepresentationManager = generalManager.getViewManager()
				.getConnectedElementRepresentationManager();

		vecScaling = new Vec3f(1, 1, 1);
		vecTranslation = new Vec3f(0, 0, 0);
		gLPathwayContentCreator = new GLPathwayContentCreator(viewFrustum, this);

	}

	public void setPathway(final PathwayGraph pathway) {
		// Unregister former pathway in visibility list
		if (pathway != null) {
			pathwayManager.setPathwayVisibilityState(pathway, false);
		}

		this.pathway = pathway;
	}

	public void setPathway(final int iPathwayID) {

		setPathway(pathwayManager.getItem(iPathwayID));
	}

	public PathwayGraph getPathway() {

		return pathway;
	}

	@Override
	public void initLocal(final GL2 gl) {
		iGLDisplayListIndexLocal = gl.glGenLists(1);
		iGLDisplayListToCall = iGLDisplayListIndexLocal;
		init(gl);
		// TODO: individual toolboxrenderer
	}

	@Override
	public void initRemote(final GL2 gl, final AGLView glParentView,
			final GLMouseListener glMouseListener) {

		this.glMouseListener = glMouseListener;

		iGLDisplayListIndexRemote = gl.glGenLists(1);
		iGLDisplayListToCall = iGLDisplayListIndexRemote;
		init(gl);
	}

	@Override
	public void init(final GL2 gl) {
		// Check if pathway exists or if it's already loaded
		if (pathway == null || !pathwayManager.hasItem(pathway.getID()))
			return;

		initPathwayData(gl);
	}

	@Override
	public void displayLocal(final GL2 gl) {

		// Check if pathway exists or if it's already loaded
		if (pathway == null || !pathwayManager.hasItem(pathway.getID()))
			return;

		// FIXME - check if already initialized with dirty flag
		initPathwayData(gl);

		pickingManager.handlePicking(this, gl);
		if (bIsDisplayListDirtyLocal) {
			rebuildPathwayDisplayList(gl, iGLDisplayListIndexLocal);
			bIsDisplayListDirtyLocal = false;
		}
		iGLDisplayListToCall = iGLDisplayListIndexLocal;
		display(gl);
	}

	@Override
	public void displayRemote(final GL2 gl) {
		// // Check if pathway exists or if it is already loaded
		// // FIXME: not good because check in every rendered frame
		// if (!generalManager.getPathwayManager().hasItem(pathway.getID()))
		// return;

		if (bIsDisplayListDirtyRemote) {
			calculatePathwayScaling(gl, pathway);
			rebuildPathwayDisplayList(gl, iGLDisplayListIndexRemote);
			bIsDisplayListDirtyRemote = false;
		}
		iGLDisplayListToCall = iGLDisplayListIndexRemote;

		display(gl);

	}

	@Override
	public void display(final GL2 gl) {
		// processEvents();
		checkForHits(gl);

		// GLHelperFunctions.drawViewFrustum(gl, viewFrustum);

		// TODO: also put this in global DL
		renderPathway(gl, pathway);

		gl.glCallList(iGLDisplayListToCall);
	}

	protected void initPathwayData(final GL2 gl) {
		// Initialize all elements in selection manager
		// Iterator<IGraphItem> iterPathwayVertexGraphItem =
		// pathway.getAllItemsByKind(
		// EGraphItemKind.NODE).iterator();
		// PathwayVertexGraphItemRep tmpPathwayVertexGraphItemRep = null;
		// while (iterPathwayVertexGraphItem.hasNext()) {
		// tmpPathwayVertexGraphItemRep = (PathwayVertexGraphItemRep)
		// iterPathwayVertexGraphItem
		// .next();
		// selectionManager.initialAdd(tmpPathwayVertexGraphItemRep.getId());
		// }

		gLPathwayContentCreator.init(gl, selectionManager);

		// Create new pathway manager for GL2 context
		if (!hashGLcontext2TextureManager.containsKey(gl)) {
			hashGLcontext2TextureManager.put(gl, new GLPathwayTextureManager());
		}

		calculatePathwayScaling(gl, pathway);
		pathwayManager.setPathwayVisibilityState(pathway, true);

		// gLPathwayContentCreator.buildPathwayDisplayList(gl, this,
		// iPathwayID);
	}

	private void renderPathway(final GL2 gl, final PathwayGraph pathway) {

		gl.glPushMatrix();
		// GLHelperFunctions.drawPointAt(gl, new Vec3f(0,0,0));
		gl.glTranslatef(vecTranslation.x(), vecTranslation.y(), vecTranslation.z());
		gl.glScalef(vecScaling.x(), vecScaling.y(), vecScaling.z());

		if (bEnablePathwayTexture) {
			float fPathwayTransparency = 1.0f;

			hashGLcontext2TextureManager.get(gl).renderPathway(gl, this, pathway,
					fPathwayTransparency, false);
		}

		float tmp = PathwayRenderStyle.SCALING_FACTOR_Y * pathway.getHeight();

		// Pathway texture height is subtracted from Y to align pathways to
		// front level
		gl.glTranslatef(0, tmp, 0);

		// FIXME: after view plugin reorganization
		// if (glRemoteRenderingView instanceof IGLRemoteRenderingBucketView
		// && ((IGLRemoteRenderingBucketView)
		// glRemoteRenderingView).getBucketMouseWheelListener() != null) {
		// // if
		// //
		// (remoteRenderingGLCanvas.getHierarchyLayerByGLEventListenerId(uniqueID)
		// // .getLevel().equals(EHierarchyLevel.UNDER_INTERACTION)
		// // &&
		// //
		// remoteRenderingGLCanvas.getBucketMouseWheelListener().isZoomedIn())
		// if (detailLevel == EDetailLevel.HIGH) {
		// gLPathwayContentCreator.renderPathway(gl, pathway, true);
		// }
		// else {
		// gLPathwayContentCreator.renderPathway(gl, pathway, false);
		// }
		// }
		// else {
		gLPathwayContentCreator.renderPathway(gl, pathway, false);
		// }

		gl.glTranslatef(0, -tmp, 0);

		gl.glScalef(1 / vecScaling.x(), 1 / vecScaling.y(), 1 / vecScaling.z());
		gl.glTranslatef(-vecTranslation.x(), -vecTranslation.y(), -vecTranslation.z());

		gl.glPopMatrix();
	}

	private void rebuildPathwayDisplayList(final GL2 gl, int iGLDisplayListIndex) {
		gLPathwayContentCreator.buildPathwayDisplayList(gl, this, pathway);

		// gl.glNewList(iGLDisplayListIndex, GL2.GL_COMPILE);
		// renderPathwayName(gl);
		// gl.glEndList();
	}

	@Override
	public void handleSelectionUpdate(SelectionDelta selectionDelta,
			boolean scrollToSelection, String info) {

		if (pathway == null)
			return;

		if (selectionDelta.getIDType() == dataDomain.getDimensionIDType()) {
			for (SelectionDeltaItem item : selectionDelta.getAllItems()) {
				if (item.getSelectionType() == SelectionType.MOUSE_OVER
						&& !item.isRemove()) {
					iCurrentDimensionIndex = item.getID();
					System.out.println(item);
					break;
				}
			}
			setDisplayListDirty();

		} else if (selectionDelta.getIDType().getIDCategory() == dataDomain
				.getRecordIDCategory()) {

			SelectionDelta resolvedDelta = resolveExternalSelectionDelta(selectionDelta);
			selectionManager.setDelta(resolvedDelta);

			setDisplayListDirty();

			int iPathwayHeight = pathway.getHeight();
			for (SelectionDeltaItem item : resolvedDelta) {
				if (item.getSelectionType() != SelectionType.MOUSE_OVER
						&& item.getSelectionType() != SelectionType.SELECTION) {
					continue;
				}

				PathwayVertexGraphItemRep vertexRep = (PathwayVertexGraphItemRep) pathwayItemManager
						.getItem(item.getID());

				int viewID = uniqueID;
				// If rendered remote (hierarchical heat map) - use the remote
				// view ID
				// if (glRemoteRenderingView != null && glRemoteRenderingView
				// instanceof AGLViewBrowser)
				// viewID = glRemoteRenderingView.getID();

				SelectedElementRep elementRep = new SelectedElementRep(
						dataDomain.getRecordIDType(), viewID, vertexRep.getXOrigin()
								* PathwayRenderStyle.SCALING_FACTOR_X * vecScaling.x()
								+ vecTranslation.x(),
						(iPathwayHeight - vertexRep.getYOrigin())
								* PathwayRenderStyle.SCALING_FACTOR_Y * vecScaling.y()
								+ vecTranslation.y(), 0);

				for (Integer iConnectionID : item.getConnectionIDs()) {
					connectedElementRepresentationManager.addSelection(iConnectionID,
							elementRep, item.getSelectionType());
				}
			}
		}
	}

	private ArrayList<Integer> getExpressionIndicesFromPathwayVertexGraphItemRep(
			int iPathwayVertexGraphItemRepID) {

		ArrayList<Integer> alExpressionIndex = new ArrayList<Integer>();

		for (IGraphItem pathwayVertexGraphItem : pathwayItemManager.getItem(
				iPathwayVertexGraphItemRepID).getAllItemsByProp(
				EGraphItemProperty.ALIAS_PARENT)) {
			int davidID = pathwayItemManager
					.getDavidIdByPathwayVertexGraphItem((PathwayVertexGraphItem) pathwayVertexGraphItem);

			if (davidID == -1) {
				continue;
			}

			// Set<Integer> DataTableRefSeq =
			// idMappingManager.getID(EIDType.DAVID,
			// EIDType.REFSEQ_MRNA_INT, iDavidID);
			//
			// if (DataTableRefSeq == null) {
			// generalManager.getLogger().log(
			// new Status(IStatus.ERROR, GeneralManager.PLUGIN_ID,
			// "No RefSeq IDs found for David: " + iDavidID));
			// continue;
			// }

			// for (Integer iDavid : DataTableRefSeq) {

			Set<Integer> DataTableExpressionIndex = contentIDMappingManager.getIDAsSet(
					IDType.getIDType("DAVID"), dataDomain.getRecordIDType(), davidID);
			if (DataTableExpressionIndex == null)
				continue;
			alExpressionIndex.addAll(DataTableExpressionIndex);
			// }
		}

		return alExpressionIndex;
	}

	private SelectionDelta createExternalSelectionDelta(SelectionDelta selectionDelta) {
		SelectionDelta newSelectionDelta = new SelectionDelta(
				dataDomain.getRecordIDType());

		for (SelectionDeltaItem item : selectionDelta) {
			for (Integer expressionIndex : getExpressionIndicesFromPathwayVertexGraphItemRep(item
					.getID())) {

				SelectionDeltaItem newItem = newSelectionDelta.addSelection(
						expressionIndex, item.getSelectionType());
				newItem.setRemove(item.isRemove());

				for (Integer iConnectionID : item.getConnectionIDs()) {
					newSelectionDelta.addConnectionID(expressionIndex, iConnectionID);
				}
			}
		}

		return newSelectionDelta;
	}

	private SelectionDelta resolveExternalSelectionDelta(SelectionDelta selectionDelta) {

		SelectionDelta newSelectionDelta = new SelectionDelta(
				pathwayDataDomain.getPrimaryIDType());

		PathwayVertexGraphItem pathwayVertexGraphItem;

		IDMappingManager idMappingManager = pathwayDataDomain.getGeneIDMappingManager();

		for (SelectionDeltaItem item : selectionDelta) {

			// FIXME: Due to new mapping system, a mapping involving expression
			// index can return a Set of
			// values, depending on the IDType that has been specified when
			// loading expression data.
			// Possibly a different handling of the Set is required.
			Set<Integer> tableIDs = idMappingManager.getIDAsSet(
					selectionDelta.getIDType(), pathwayDataDomain.getDavidIDType(),
					item.getID());

			if (tableIDs == null || tableIDs.isEmpty()) {
				continue;
				// throw new
				// IllegalStateException("Cannot resolve RefSeq ID to David ID.");
			}
			Integer iDavidID = (Integer) tableIDs.toArray()[0];

			pathwayVertexGraphItem = pathwayItemManager
					.getPathwayVertexGraphItemByDavidId(iDavidID);

			// Ignore David IDs that do not exist in any pathway
			if (pathwayVertexGraphItem == null) {
				continue;
			}

			// Convert DAVID ID to pathway graph item representation ID
			for (IGraphItem tmpGraphItemRep : pathwayVertexGraphItem
					.getAllItemsByProp(EGraphItemProperty.ALIAS_CHILD)) {
				if (!pathway.containsItem(tmpGraphItemRep)) {
					continue;
				}

				SelectionDeltaItem newItem = newSelectionDelta.addSelection(
						tmpGraphItemRep.getId(), item.getSelectionType());
				newItem.setRemove(item.isRemove());
				for (int iConnectionID : item.getConnectionIDs()) {
					newItem.addConnectionID(iConnectionID);
				}
			}
		}

		return newSelectionDelta;
	}

	private void calculatePathwayScaling(final GL2 gl, final PathwayGraph pathway) {

		if (hashGLcontext2TextureManager.get(gl) == null)
			return;

		// // Missing power of two texture GL2 extension workaround
		// PathwayGraph tmpPathwayGraph =
		// (PathwayGraph)generalManager.getPathwayManager().getItem(iPathwayId);
		// ImageIcon img = new ImageIcon(generalManager.getPathwayManager()
		// .getPathwayDatabaseByType(tmpPathwayGraph.getType()).getImagePath()
		// + tmpPathwayGraph.getImageLink());
		// int iImageWidth = img.getIconWidth();
		// int iImageHeight = img.getIconHeight();
		// tmpPathwayGraph.setWidth(iImageWidth);
		// tmpPathwayGraph.setHeight(iImageHeight);
		// img = null;

		float fPathwayScalingFactor = 0;
		float fPadding = 0.98f;

		if (pathway.getType().equals(PathwayDatabaseType.BIOCARTA)) {
			fPathwayScalingFactor = 5;
		} else {
			fPathwayScalingFactor = 3.2f;
		}

		int iImageWidth = pathway.getWidth();
		int iImageHeight = pathway.getHeight();

		if (iImageWidth == -1 || iImageHeight == -1) {
			Logger.log(new Status(IStatus.ERROR, this.toString(),
					"Problem because pathway texture width or height is invalid!"));
		}

		float fTmpPathwayWidth = iImageWidth * PathwayRenderStyle.SCALING_FACTOR_X
				* fPathwayScalingFactor;
		float fTmpPathwayHeight = iImageHeight * PathwayRenderStyle.SCALING_FACTOR_Y
				* fPathwayScalingFactor;

		float pathwayAspectRatio = fTmpPathwayWidth / fTmpPathwayHeight;
		float viewFrustumWidth = viewFrustum.getRight() - viewFrustum.getLeft();
		float viewFrustumHeight = viewFrustum.getTop() - viewFrustum.getBottom();
		float viewFrustumAspectRatio = viewFrustumWidth / viewFrustumHeight;
		boolean pathwayFitsViewFrustum = true;

		if (viewFrustumAspectRatio < pathwayAspectRatio
				&& fTmpPathwayWidth > viewFrustumWidth) {

			// if (fTmpPathwayWidth > viewFrustum.getRight() -
			// viewFrustum.getLeft()
			// && fTmpPathwayWidth > fTmpPathwayHeight) {
			vecScaling.setX((viewFrustum.getRight() - viewFrustum.getLeft())
					/ (iImageWidth * PathwayRenderStyle.SCALING_FACTOR_X) * fPadding);
			vecScaling.setY(vecScaling.x());

			vecTranslation
					.set((viewFrustum.getRight() - viewFrustum.getLeft() - iImageWidth
							* PathwayRenderStyle.SCALING_FACTOR_X * vecScaling.x()) / 2.0f,
							(viewFrustum.getTop() - viewFrustum.getBottom() - iImageHeight
									* PathwayRenderStyle.SCALING_FACTOR_Y
									* vecScaling.y()) / 2.0f, 0);
			pathwayFitsViewFrustum = false;
		}
		if (viewFrustumAspectRatio >= pathwayAspectRatio
				&& fTmpPathwayHeight > viewFrustumHeight) {
			//
			// else if (fTmpPathwayHeight > viewFrustum.getTop()
			// - viewFrustum.getBottom()) {
			vecScaling.setY((viewFrustum.getTop() - viewFrustum.getBottom())
					/ (iImageHeight * PathwayRenderStyle.SCALING_FACTOR_Y) * fPadding);
			vecScaling.setX(vecScaling.y());

			vecTranslation
					.set((viewFrustum.getRight() - viewFrustum.getLeft() - iImageWidth
							* PathwayRenderStyle.SCALING_FACTOR_X * vecScaling.x()) / 2.0f,
							(viewFrustum.getTop() - viewFrustum.getBottom() - iImageHeight
									* PathwayRenderStyle.SCALING_FACTOR_Y
									* vecScaling.y()) / 2.0f, 0);
			pathwayFitsViewFrustum = false;

		} // else {

		if (pathwayFitsViewFrustum) {
			vecScaling.set(fPathwayScalingFactor, fPathwayScalingFactor, 1f);

			vecTranslation.set((viewFrustum.getRight() - viewFrustum.getLeft()) / 2.0f
					- fTmpPathwayWidth / 2.0f,
					(viewFrustum.getTop() - viewFrustum.getBottom()) / 2.0f
							- fTmpPathwayHeight / 2.0f, 0);
		}
	}

	public void setMappingRowCount(final int iMappingRowCount) {
		gLPathwayContentCreator.setMappingRowCount(iMappingRowCount);
	}

	public void enableGeneMapping(final boolean bEnableMapping) {
		gLPathwayContentCreator.enableGeneMapping(bEnableMapping);
		setDisplayListDirty();
	}

	public void enablePathwayTextures(final boolean bEnablePathwayTexture) {
		gLPathwayContentCreator.enableEdgeRendering(!bEnablePathwayTexture);
		setDisplayListDirty();

		this.bEnablePathwayTexture = bEnablePathwayTexture;
	}

	public void enableNeighborhood(final boolean bEnableNeighborhood) {
		setDisplayListDirty();

		gLPathwayContentCreator.enableNeighborhood(bEnableNeighborhood);
	}

	public void enableIdenticalNodeHighlighting(
			final boolean bEnableIdenticalNodeHighlighting) {
		setDisplayListDirty();

		gLPathwayContentCreator
				.enableIdenticalNodeHighlighting(bEnableIdenticalNodeHighlighting);
	}

	public void enableAnnotation(final boolean bEnableAnnotation) {
		gLPathwayContentCreator.enableAnnotation(bEnableAnnotation);
	}

	@Override
	protected void handlePickingEvents(PickingType pickingType, PickingMode pickingMode,
			int externalID, Pick pick) {
		if (detailLevel == DetailLevel.VERY_LOW) {
			return;
		}

		switch (pickingType) {
		case PATHWAY_ELEMENT_SELECTION:

			SelectionType selectionType;

			PathwayVertexGraphItemRep tmpVertexGraphItemRep = (PathwayVertexGraphItemRep) pathwayItemManager
					.getItem(externalID);

			setDisplayListDirty();

			switch (pickingMode) {
			case DOUBLE_CLICKED:
				// same behavior as for single click except that
				// pathways are also loaded
				selectionType = SelectionType.SELECTION;

				// Load embedded pathway
				if (tmpVertexGraphItemRep.getType() == EPathwayVertexType.map) {
					PathwayGraph pathway = pathwayManager.searchPathwayByName(
							tmpVertexGraphItemRep.getName(), PathwayDatabaseType.KEGG);

					if (pathway != null) {
						LoadPathwayEvent event = new LoadPathwayEvent();
						event.setSender(this);
						event.setPathwayID(pathway.getID());
						event.setDataDomainID(dataDomain.getDataDomainID());
						eventPublisher.triggerEvent(event);
					}
				} else {

					// // Load pathways
					// for (IGraphItem pathwayVertexGraphItem :
					// tmpVertexGraphItemRep
					// .getAllItemsByProp(EGraphItemProperty.ALIAS_CHILD))
					// {
					//
					// LoadPathwaysByGeneEvent loadPathwaysByGeneEvent =
					// new LoadPathwaysByGeneEvent();
					// loadPathwaysByGeneEvent.setSender(this);
					// loadPathwaysByGeneEvent.setGeneID(pathwayVertexGraphItem.getId());
					// loadPathwaysByGeneEvent.setIdType(EIDType.PATHWAY_VERTEX);
					// generalManager.getEventPublisher().triggerEvent(loadPathwaysByGeneEvent);
					//
					// }
				}
				break;

			case CLICKED:
				selectionType = SelectionType.SELECTION;
				break;
			case MOUSE_OVER:
				selectionType = SelectionType.MOUSE_OVER;
				break;
			case RIGHT_CLICKED:
				selectionType = SelectionType.SELECTION;

				if (tmpVertexGraphItemRep.getType() == EPathwayVertexType.map) {

					LoadPathwaysByPathwayItem menuItem = new LoadPathwaysByPathwayItem(
							pathwayManager.searchPathwayByName(
									tmpVertexGraphItemRep.getName(),
									PathwayDatabaseType.KEGG),
							dataDomain.getDataDomainID());
					contextMenuCreator.addContextMenuItem(menuItem);

				} else if (tmpVertexGraphItemRep.getType() == EPathwayVertexType.gene) {
					for (IGraphItem pathwayVertexGraphItem : tmpVertexGraphItemRep
							.getAllItemsByProp(EGraphItemProperty.ALIAS_PARENT)) {

						GeneRecordContextMenuItemContainer contexMenuItemContainer = new GeneRecordContextMenuItemContainer();
						contexMenuItemContainer
								.setDataDomain((ATableBasedDataDomain) dataDomain);
						contexMenuItemContainer
								.setData(
										pathwayDataDomain.getDavidIDType(),
										pathwayItemManager
												.getDavidIdByPathwayVertexGraphItem((PathwayVertexGraphItem) pathwayVertexGraphItem));
						contextMenuCreator
								.addContextMenuItemContainer(contexMenuItemContainer);
					}
				} else {
					// do nothing if the type is neither a gene nor an
					// embedded pathway
					break;
				}

			default:
				return;
			}

			if (selectionManager.checkStatus(selectionType, externalID)) {
				break;
			}

			selectionManager.clearSelection(selectionType);

			SelectionCommand command = new SelectionCommand(ESelectionCommandType.CLEAR,
					selectionType);
			sendSelectionCommandEvent(dataDomain.getRecordIDType(), command);

			// Add new vertex to internal selection manager
			selectionManager.addToType(selectionType, tmpVertexGraphItemRep.getId());

			int iConnectionID = generalManager.getIDCreator().createID(
					ManagedObjectType.CONNECTION);
			selectionManager
					.addConnectionID(iConnectionID, tmpVertexGraphItemRep.getId());
			connectedElementRepresentationManager.clear(dataDomain.getRecordIDType(),
					selectionType);
			// gLPathwayContentCreator
			// .performIdenticalNodeHighlighting(selectionType);

			createConnectionLines(selectionType, iConnectionID);

			SelectionDelta selectionDelta = createExternalSelectionDelta(selectionManager
					.getDelta());
			SelectionUpdateEvent event = new SelectionUpdateEvent();
			event.setSender(this);
			event.setDataDomainID(dataDomain.getDataDomainID());
			event.setSelectionDelta((SelectionDelta) selectionDelta);
			event.setInfo(getShortInfoLocal());

			eventPublisher.triggerEvent(event);

			break;
		}
	}

	private void createConnectionLines(SelectionType selectionType, int iConnectionID) {
		// check in preferences if we should draw connection lines for mouse
		// over
		if (!connectedElementRepresentationManager
				.isSelectionTypeRenderedWithVisuaLinks(selectionType))
			return;
		// check for selections
		if (!generalManager.getPreferenceStore().getBoolean(
				PreferenceConstants.VISUAL_LINKS_FOR_SELECTIONS)
				&& selectionType == SelectionType.SELECTION)
			return;

		PathwayVertexGraphItemRep tmpPathwayVertexGraphItemRep;
		int iPathwayHeight = pathway.getHeight();

		int viewID = uniqueID;
		// If rendered remote (hierarchical heat map) - use the remote view ID
		// if (glRemoteRenderingView != null && glRemoteRenderingView instanceof
		// AGLViewBrowser)
		// viewID = glRemoteRenderingView.getID();

		for (int iVertexRepID : selectionManager.getElements(selectionType)) {
			tmpPathwayVertexGraphItemRep = pathwayItemManager
					.getPathwayVertexRep(iVertexRepID);

			SelectedElementRep elementRep = new SelectedElementRep(
					dataDomain.getRecordIDType(), viewID,
					tmpPathwayVertexGraphItemRep.getXOrigin()
							* PathwayRenderStyle.SCALING_FACTOR_X * vecScaling.x()
							+ vecTranslation.x(),
					(iPathwayHeight - tmpPathwayVertexGraphItemRep.getYOrigin())
							* PathwayRenderStyle.SCALING_FACTOR_Y * vecScaling.y()
							+ vecTranslation.y(), 0);

			// for (Integer iConnectionID : selectionManager
			// .getConnectionForElementID(iVertexRepID))
			// {
			connectedElementRepresentationManager.addSelection(iConnectionID, elementRep,
					selectionType);
			// }
		}
		// }
	}

	@Override
	public void broadcastElements(EVAOperation type) {

		RecordVADelta delta = new RecordVADelta(recordPerspectiveID,
				pathwayDataDomain.getDavidIDType());

		for (IGraphItem tmpPathwayVertexGraphItemRep : pathway
				.getAllItemsByKind(EGraphItemKind.NODE)) {
			for (IGraphItem tmpPathwayVertexGraphItem : tmpPathwayVertexGraphItemRep
					.getAllItemsByProp(EGraphItemProperty.ALIAS_PARENT)) {
				int iDavidID = pathwayItemManager
						.getDavidIdByPathwayVertexGraphItem((PathwayVertexGraphItem) tmpPathwayVertexGraphItem);

				if (iDavidID == -1 || iDavidID == 0) {
					// generalManager.getLogger().log(
					// new Status(Status.WARNING, GeneralManager.PLUGIN_ID,
					// "Invalid David Gene ID."));
					continue;
				}

				// Set<Integer> DataTableRefSeq =
				// idMappingManager.getID(EIDType.DAVID,
				// EIDType.REFSEQ_MRNA_INT, iDavidID);
				//
				// if (DataTableRefSeq == null) {
				//
				// generalManager.getLogger().log(
				// new Status(IStatus.ERROR, GeneralManager.PLUGIN_ID,
				// "No RefSeq IDs found for David: " + iDavidID));
				// continue;
				// }

				// for (Object iRefSeqID : DataTableRefSeq) {
				delta.add(VADeltaItem.create(type, (Integer) iDavidID));
				// }
			}
		}

		RecordVADeltaEvent virtualArrayDeltaEvent = new RecordVADeltaEvent();
		virtualArrayDeltaEvent.setSender(this);
		virtualArrayDeltaEvent.setDataDomainID(dataDomain.getDataDomainID());
		virtualArrayDeltaEvent.setVirtualArrayDelta(delta);
		virtualArrayDeltaEvent.setInfo(getShortInfoLocal());
		eventPublisher.triggerEvent(virtualArrayDeltaEvent);
	}

	@Override
	public String getShortInfo() {
		return pathway.getTitle() + " (" + pathway.getType().getName() + ")";
	}

	@Override
	public String getDetailedInfo() {

		if (isRenderedRemote())
			return (((AGLView) getRemoteRenderingGLView()).getDetailedInfo());

		StringBuffer sInfoText = new StringBuffer();

		sInfoText.append("<b>Pathway</b>\n\n<b>Name:</b> " + pathway.getTitle()
				+ "\n<b>Type:</b> " + pathway.getType().getName());

		// generalManager.getSWTGUIManager().setExternalRCPStatusLineMessage(
		// pathway.getType().getName() + " Pathway: " + sPathwayTitle);

		return sInfoText.toString();
	}

	@Override
	public void initData() {
		connectedElementRepresentationManager.clear(dataDomain.getRecordIDType());
		iCurrentDimensionIndex = -1;
		super.initData();

	}

	@Override
	public int getNumberOfSelections(SelectionType SelectionType) {
		return selectionManager.getElements(SelectionType).size();
	}

	@Override
	public void handleRedrawView() {
		setDisplayListDirty();
	}

	@Override
	public void handleUpdateView() {
		setDisplayListDirty();
	}

	@Override
	public void handleClearSelections() {
		clearAllSelections();
		setDisplayListDirty();
	}

	@Override
	public void destroy() {
		pathwayManager.setPathwayVisibilityState(pathway, false);

		super.destroy();
	}

	@Override
	public void clearAllSelections() {
		selectionManager.clearSelections();
	}

	@Override
	public void registerEventListeners() {
		super.registerEventListeners();

		enableGeneMappingListener = new EnableGeneMappingListener();
		enableGeneMappingListener.setHandler(this);
		eventPublisher.addListener(EnableGeneMappingEvent.class,
				enableGeneMappingListener);

		disableGeneMappingListener = new DisableGeneMappingListener();
		disableGeneMappingListener.setHandler(this);
		eventPublisher.addListener(DisableGeneMappingEvent.class,
				disableGeneMappingListener);

		selectionUpdateListener = new SelectionUpdateListener();
		selectionUpdateListener.setHandler(this);
		selectionUpdateListener.setExclusiveDataDomainID(dataDomain.getDataDomainID());
		eventPublisher.addListener(SelectionUpdateEvent.class, selectionUpdateListener);

		// virtualArrayUpdateListener = new RecordVAUpdateListener();
		// virtualArrayUpdateListener.setHandler(this);
		// eventPublisher.addListener(VirtualArrayUpdateEvent.class,
		// virtualArrayUpdateListener);

		redrawViewListener = new RedrawViewListener();
		redrawViewListener.setHandler(this);
		eventPublisher.addListener(RedrawViewEvent.class, redrawViewListener);

		clearSelectionsListener = new ClearSelectionsListener();
		clearSelectionsListener.setHandler(this);
		clearSelectionsListener.setDataDomainID(dataDomain.getDataDomainID());
		eventPublisher.addListener(ClearSelectionsEvent.class, clearSelectionsListener);

		selectionCommandListener = new SelectionCommandListener();
		selectionCommandListener.setHandler(this);
		selectionCommandListener.setDataDomainID(dataDomain.getDataDomainID());
		eventPublisher.addListener(SelectionCommandEvent.class, selectionCommandListener);

		// replaceVirtualArrayListener = new ReplaceRecordVAListener();
		// replaceVirtualArrayListener.setHandler(this);
		// eventPublisher.addListener(ReplaceVAEvent.class,
		// replaceVirtualArrayListener);

		switchDataRepresentationListener = new SwitchDataRepresentationListener();
		switchDataRepresentationListener.setHandler(this);
		eventPublisher.addListener(SwitchDataRepresentationEvent.class,
				switchDataRepresentationListener);
	}

	@Override
	public void unregisterEventListeners() {
		super.unregisterEventListeners();

		if (enableGeneMappingListener != null) {
			eventPublisher.removeListener(EnableGeneMappingEvent.class,
					enableGeneMappingListener);
			enableGeneMappingListener = null;
		}
		if (disableGeneMappingListener != null) {
			eventPublisher.removeListener(DisableGeneMappingEvent.class,
					disableGeneMappingListener);
			disableGeneMappingListener = null;
		}
		if (selectionUpdateListener != null) {
			eventPublisher.removeListener(selectionUpdateListener);
			selectionUpdateListener = null;
		}
		if (virtualArrayUpdateListener != null) {
			eventPublisher.removeListener(virtualArrayUpdateListener);
			virtualArrayUpdateListener = null;
		}
		if (selectionCommandListener != null) {
			eventPublisher.removeListener(selectionCommandListener);
			selectionCommandListener = null;
		}

		if (replaceVirtualArrayListener != null) {
			eventPublisher.removeListener(replaceVirtualArrayListener);
			replaceVirtualArrayListener = null;
		}

		if (switchDataRepresentationListener != null) {
			eventPublisher.removeListener(switchDataRepresentationListener);
			switchDataRepresentationListener = null;
		}
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedPathwayView serializedForm = new SerializedPathwayView(
				pathwayDataDomain.getDataDomainID());
		serializedForm.setViewID(this.getID());

		if (pathway != null)
			serializedForm.setPathwayID(pathway.getID());

		return serializedForm;
	}

	@Override
	public void handleSelectionCommand(IDCategory category,
			SelectionCommand selectionCommand) {
		if (dataDomain.getRecordIDCategory() == category)
			selectionManager.executeSelectionCommand(selectionCommand);

	}

	public PathwayDataDomain getPathwayDataDomain() {
		return pathwayDataDomain;
	}

	public void setPathwayDataDomain(PathwayDataDomain dataDomain) {
		this.pathwayDataDomain = dataDomain;
	}

	@Override
	public int getMinPixelHeight() {
		return 60;
	}

	@Override
	public int getMinPixelWidth() {
		float aspectRatio = (float) pathway.getWidth() / (float) pathway.getHeight();
		return (int) (60.0f * aspectRatio);
	}

	@Override
	public boolean isDataView() {
		return true;
	}

	public void switchDataRepresentation() {
		gLPathwayContentCreator.switchDataRepresentation();
		setDisplayListDirty();
	}

	public void setRecordPerspectiveID(String recordPerspectiveID) {
		this.recordPerspectiveID = recordPerspectiveID;

	}

	public void setDimensionPerspectiveID(String dimensionPerspectiveID) {
		this.dimensionPerspectiveID = dimensionPerspectiveID;
	}

	@Override
	public void setDataDomain(ATableBasedDataDomain dataDomain) {
		this.dataDomain = dataDomain;
		selectionManager = dataDomain.getRecordSelectionManager();
	}

	@Override
	public ATableBasedDataDomain getDataDomain() {
		return dataDomain;
	}
	
	
	
}