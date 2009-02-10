package org.caleydo.core.view.opengl.canvas.storagebased.parcoords;

import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.ANGLUAR_LINE_WIDTH;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.ANGULAR_COLOR;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.ANGULAR_POLYGON_COLOR;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.AXIS_MARKER_WIDTH;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.AXIS_Z;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.DESELECTED_POLYLINE_LINE_WIDTH;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.GATE_BODY_COLOR;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.GATE_TIP_COLOR;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.MOUSE_OVER_POLYLINE_LINE_WIDTH;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.NUMBER_AXIS_MARKERS;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.POLYLINE_MOUSE_OVER_COLOR;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.POLYLINE_NO_OCCLUSION_PREV_COLOR;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.POLYLINE_SELECTED_COLOR;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.SELECTED_POLYLINE_LINE_WIDTH;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.X_AXIS_COLOR;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.X_AXIS_LINE_WIDTH;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.Y_AXIS_COLOR;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.Y_AXIS_LINE_WIDTH;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.Y_AXIS_LOW;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.Y_AXIS_MOUSE_OVER_COLOR;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.Y_AXIS_MOUSE_OVER_LINE_WIDTH;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.Y_AXIS_SELECTED_COLOR;
import static org.caleydo.core.view.opengl.canvas.storagebased.parcoords.ParCoordsRenderStyle.Y_AXIS_SELECTED_LINE_WIDTH;
import static org.caleydo.core.view.opengl.renderstyle.GeneralRenderStyle.getDecimalFormat;
import gleem.linalg.Rotf;
import gleem.linalg.Vec3f;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.management.InvalidAttributeValueException;
import javax.media.opengl.GL;
import org.caleydo.core.data.collection.ESetType;
import org.caleydo.core.data.collection.INominalStorage;
import org.caleydo.core.data.collection.INumericalStorage;
import org.caleydo.core.data.collection.IStorage;
import org.caleydo.core.data.collection.storage.EDataRepresentation;
import org.caleydo.core.data.mapping.EIDType;
import org.caleydo.core.data.mapping.EMappingType;
import org.caleydo.core.data.selection.DeltaEventContainer;
import org.caleydo.core.data.selection.ESelectionCommandType;
import org.caleydo.core.data.selection.ESelectionType;
import org.caleydo.core.data.selection.EVAOperation;
import org.caleydo.core.data.selection.GenericSelectionManager;
import org.caleydo.core.data.selection.ISelectionDelta;
import org.caleydo.core.data.selection.IVirtualArray;
import org.caleydo.core.data.selection.IVirtualArrayDelta;
import org.caleydo.core.data.selection.SelectedElementRep;
import org.caleydo.core.data.selection.SelectionCommand;
import org.caleydo.core.data.selection.SelectionCommandEventContainer;
import org.caleydo.core.data.selection.VADeltaItem;
import org.caleydo.core.data.selection.VirtualArrayDelta;
import org.caleydo.core.manager.event.EEventType;
import org.caleydo.core.manager.event.EMediatorType;
import org.caleydo.core.manager.event.IDListEventContainer;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.manager.id.EManagedObjectType;
import org.caleydo.core.manager.picking.EPickingMode;
import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.manager.picking.Pick;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.preferences.PreferenceConstants;
import org.caleydo.core.util.wii.WiiRemote;
import org.caleydo.core.view.opengl.camera.IViewFrustum;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.remote.IGLCanvasRemoteRendering;
import org.caleydo.core.view.opengl.canvas.storagebased.AStorageBasedView;
import org.caleydo.core.view.opengl.canvas.storagebased.EDataFilterLevel;
import org.caleydo.core.view.opengl.canvas.storagebased.EStorageBasedVAType;
import org.caleydo.core.view.opengl.mouse.PickingJoglMouseListener;
import org.caleydo.core.view.opengl.util.GLCoordinateUtils;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;

/**
 * This class is responsible for rendering the parallel coordinates
 * 
 * @author Alexander Lex (responsible for PC)
 * @author Marc Streit
 */
public class GLParallelCoordinates
	extends AStorageBasedView
{

	/**
	 * Flag whether to take measures against occlusion or not
	 */
	private boolean bPreventOcclusion = true;

	// flag whether one array should be a polyline or an axis
	// protected boolean bRenderHorizontally = false;

	// Specify the current input data type for the axis and polylines
	// Is used for meta information, such as captions
	private EIDType eAxisDataType = EIDType.EXPERIMENT_INDEX;

	private EIDType ePolylineDataType = EIDType.EXPRESSION_INDEX;

	private boolean bIsDraggingActive = false;

	private boolean bIsGateMouseOver = false;

	private EPickingType draggedObject;

	private HashMap<Integer, ArrayList<Pair<Float, Float>>> hashGates;

	private HashMap<Integer, ArrayList<Integer>> hashIsGateBlocking;

	private ArrayList<ArrayList<Integer>> alIsAngleBlocking;

	private ArrayList<Float> alAxisSpacing;

	private int iDraggedGateNumber = 0;

	private float fXDefaultTranslation = 0;

	private float fXTranslation = 0;

	private float fYTranslation = 0;

	private float fXTargetTranslation = 0;

	private boolean bIsTranslationActive = false;

	// private boolean bRenderInfoArea = false;
	// private boolean bInfoAreaFirstTime = false;

	private boolean bAngularBrushingSelectPolyline = false;
	private boolean bIsAngularBrushingActive = false;
	private boolean bIsAngularBrushingFirstTime = false;

	private boolean bIsGateDraggingFirstTime = false;
	private boolean bIsAngularDraggingActive = false;

	private boolean bWasAxisMoved = false;
	private boolean bWasAxisDraggedFirstTime = true;
	private float fAxisDraggingOffset;

	private int iMovedAxisPosition = -1;

	private float fGateTopSpacing;
	private float fGateBottomSpacing;

	private Vec3f vecAngularBrushingPoint;

	private float fDefaultAngle = (float) Math.PI / 6;

	private float fCurrentAngle = 0;

	// private boolean bIsLineSelected = false;
	private int iSelectedLineID = -1;

	private Pick linePick;

	private SelectedElementRep elementRep;

	private int iPolylineVAID = 0;
	private int iAxisVAID = 0;

	private GenericSelectionManager polylineSelectionManager;
	private GenericSelectionManager axisSelectionManager;

	protected ParCoordsRenderStyle renderStyle;

	private int iDisplayEveryNthPolyline = 1;

	/**
	 * Constructor.
	 */
	public GLParallelCoordinates(ESetType setType, final int iGLCanvasID, final String sLabel,
			final IViewFrustum viewFrustum)
	{
		super(setType, iGLCanvasID, sLabel, viewFrustum);
		viewType = EManagedObjectType.GL_PARALLEL_COORDINATES;

		renderStyle = new ParCoordsRenderStyle(viewFrustum);
		super.renderStyle = this.renderStyle;

		contentSelectionManager = new GenericSelectionManager.Builder(EIDType.EXPRESSION_INDEX)
				.mappingType(EMappingType.EXPRESSION_INDEX_2_REFSEQ_MRNA_INT,
						EMappingType.REFSEQ_MRNA_INT_2_EXPRESSION_INDEX).externalIDType(
						EIDType.REFSEQ_MRNA_INT).build();

		storageSelectionManager = new GenericSelectionManager.Builder(EIDType.EXPERIMENT_INDEX)
				.build();

		alIsAngleBlocking = new ArrayList<ArrayList<Integer>>();
		alIsAngleBlocking.add(new ArrayList<Integer>());

		alAxisSpacing = new ArrayList<Float>();
		iNumberOfRandomElements = generalManager.getPreferenceStore().getInt(
				PreferenceConstants.PC_NUM_RANDOM_SAMPLING_POINT);
	}

	@Override
	public void initLocal(final GL gl)
	{
		String sLevel = GeneralManager.get().getPreferenceStore().getString(
				PreferenceConstants.DATA_FILTER_LEVEL);
		if (sLevel.equals("complete"))
		{
			dataFilterLevel = EDataFilterLevel.COMPLETE;
		}
		else if (sLevel.equals("only_mapping"))
		{
			dataFilterLevel = EDataFilterLevel.ONLY_MAPPING;
		}
		else if (sLevel.equals("only_context"))
		{
			dataFilterLevel = EDataFilterLevel.ONLY_CONTEXT;
		}
		else
		{
			throw new IllegalStateException("Unknown data filter level");
		}

		generalManager.getEventPublisher().addSender(EMediatorType.PROPAGATION_MEDIATOR, this);
		generalManager.getEventPublisher().addSender(EMediatorType.SELECTION_MEDIATOR, this);
		generalManager.getEventPublisher().addReceiver(EMediatorType.SELECTION_MEDIATOR, this);

		bRenderOnlyContext = false;

		iGLDisplayListIndexLocal = gl.glGenLists(1);
		iGLDisplayListToCall = iGLDisplayListIndexLocal;
		init(gl);

	}

	@Override
	public void initRemote(final GL gl, final int iRemoteViewID,
			final PickingJoglMouseListener pickingTriggerMouseAdapter,
			final IGLCanvasRemoteRendering remoteRenderingGLCanvas)
	{
		bRenderOnlyContext = true;
		dataFilterLevel = EDataFilterLevel.ONLY_CONTEXT;
		// dataFilterLevel = EDataFilterLevel.ONLY_MAPPING;

		this.remoteRenderingGLCanvas = remoteRenderingGLCanvas;

		this.pickingTriggerMouseAdapter = pickingTriggerMouseAdapter;

		iGLDisplayListIndexRemote = gl.glGenLists(1);
		iGLDisplayListToCall = iGLDisplayListIndexRemote;
		init(gl);
		// toggleRenderContext();
	}

	@Override
	public void init(final GL gl)
	{

		initData();

		resetAxisSpacing();

		fXDefaultTranslation = renderStyle.getXSpacing();
		fYTranslation = renderStyle.getBottomSpacing();
	}

	@Override
	public synchronized void displayLocal(final GL gl)
	{
		if (set == null)
			return;

		if (hashGates == null)
		{
			initGates();
		}

		if (bIsTranslationActive)
		{
			doTranslation();
		}

		pickingManager.handlePicking(iUniqueID, gl, true);
		
		if (bIsDisplayListDirtyLocal)
		{
			buildDisplayList(gl, iGLDisplayListIndexLocal);
			bIsDisplayListDirtyLocal = false;
		}
		iGLDisplayListToCall = iGLDisplayListIndexLocal;
		
		checkForHits(gl);
		display(gl);
		
		if (eBusyModeState != EBusyModeState.OFF)
			renderBusyMode(gl);
	}

	@Override
	public synchronized void displayRemote(final GL gl)
	{
		if (set == null)
			return;

		if (hashGates == null)
		{
			initGates();
		}

		if (bIsTranslationActive)
		{
			doTranslation();
		}

		if (bIsDisplayListDirtyRemote)
		{
			buildDisplayList(gl, iGLDisplayListIndexRemote);
			bIsDisplayListDirtyRemote = false;
		}

		iGLDisplayListToCall = iGLDisplayListIndexRemote;

		display(gl);
		checkForHits(gl);
	}

	@Override
	public synchronized void display(final GL gl)
	{

		// focusOnArea();
		// TODO another display list
		clipToFrustum(gl);

		gl.glTranslatef(fXDefaultTranslation + fXTranslation, fYTranslation, 0.0f);

		if (bIsDraggingActive)
		{
			handleGateDragging(gl);
//			if (pickingTriggerMouseAdapter.wasMouseReleased())
//			{
//				System.out.println("Mouse released in view");
//				bIsDraggingActive = false;
//			}
		}

		if (bWasAxisMoved)
		{
			adjustAxisSpacing(gl);
			if (pickingTriggerMouseAdapter.wasMouseReleased())
				bWasAxisMoved = false;
		}

		// if(bRenderInfoArea)
		// infoAreaManager.renderInfoArea(gl, bInfoAreaFirstTime);
		// bInfoAreaFirstTime = false;

		checkUnselection();
		// GLHelperFunctions.drawAxis(gl);
		gl.glCallList(iGLDisplayListToCall);

		if (bIsAngularBrushingActive && iSelectedLineID != -1)
		{
			handleAngularBrushing(gl);
			// if(pickingTriggerMouseAdapter.wasMouseReleased())
			// bIsAngularBrushingActive = false;

		}

		gl.glTranslatef(-fXDefaultTranslation - fXTranslation, -fYTranslation, 0.0f);

		gl.glDisable(GL.GL_STENCIL_TEST);
	}

	/**
	 * Choose whether to render one array as a polyline and every entry across
	 * arrays is an axis or whether the array corresponds to an axis and every
	 * entry across arrays is a polyline
	 */
	public synchronized void renderStorageAsPolyline(boolean bRenderStorageHorizontally)
	{

		if (bRenderStorageHorizontally != this.bRenderStorageHorizontally)
		{
			if (bRenderStorageHorizontally && set.getVA(iContentVAID).size() > 100)
			{
				MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
				messageBox
						.setMessage("Can not show more than 100 axis - reduce polylines to less than 100 first");
				messageBox.open();
				return;
			}

			EIDType eTempType = eAxisDataType;
			eAxisDataType = ePolylineDataType;
			ePolylineDataType = eTempType;
		}

		this.bRenderStorageHorizontally = bRenderStorageHorizontally;
		// bRenderInfoArea = false;

		fXTranslation = 0;
		connectedElementRepresentationManager.clear(EIDType.EXPRESSION_INDEX);
		initContentVariables();

		resetAxisSpacing();
		initGates();

		setDisplayListDirty();
	}

	public synchronized void triggerAngularBrushing()
	{
		bAngularBrushingSelectPolyline = true;
		setDisplayListDirty();
	}

	@Override
	public synchronized void renderContext(boolean bRenderOnlyContext)
	{
		this.bRenderOnlyContext = bRenderOnlyContext;

		if (bRenderOnlyContext)
			iContentVAID = mapVAIDs.get(EStorageBasedVAType.EXTERNAL_SELECTION);
		else
		{
			if (!mapVAIDs.containsKey(EStorageBasedVAType.COMPLETE_SELECTION))
				initCompleteList();

			iContentVAID = mapVAIDs.get(EStorageBasedVAType.COMPLETE_SELECTION);
		}

		contentSelectionManager.setVA(set.getVA(iContentVAID));
		initContentVariables();
		initGates();
		resetSelections();

		setDisplayListDirty();
	}

	/**
	 * Choose whether to take measures against occlusion or not
	 * 
	 * @param bPreventOcclusion
	 */
	public synchronized void preventOcclusion(boolean bPreventOcclusion)
	{
		this.bPreventOcclusion = bPreventOcclusion;
		setDisplayListDirty();
	}

	/**
	 * Reset all selections and deselections
	 */
	@Override
	public synchronized void resetSelections()
	{

		initGates();
		contentSelectionManager.clearSelections();
		storageSelectionManager.clearSelections();

		// bRenderInfoArea = false;
		bIsAngularBrushingActive = false;

		for (ArrayList<Integer> alCurrent : alIsAngleBlocking)
		{
			alCurrent.clear();
		}
		for (ArrayList<Integer> alCurrent : hashIsGateBlocking.values())
		{
			alCurrent.clear();
		}
		setDisplayListDirty();
		connectedElementRepresentationManager.clear(EIDType.EXPRESSION_INDEX);

		triggerEvent(EMediatorType.SELECTION_MEDIATOR,
				new SelectionCommandEventContainer(EIDType.REFSEQ_MRNA_INT,
						new SelectionCommand(ESelectionCommandType.CLEAR_ALL)));
		triggerEvent(EMediatorType.SELECTION_MEDIATOR, new SelectionCommandEventContainer(
				EIDType.EXPERIMENT_INDEX,
				new SelectionCommand(ESelectionCommandType.CLEAR_ALL)));

	}

	@Override
	public synchronized void broadcastElements()
	{
		saveSelection();

		IVirtualArrayDelta delta = contentSelectionManager.getBroadcastVADelta();
		triggerEvent(EMediatorType.PROPAGATION_MEDIATOR,
				new DeltaEventContainer<IVirtualArrayDelta>(delta));
		setDisplayListDirty();
	}

	public synchronized void saveSelection()
	{

		// polylineSelectionManager.moveType(ESelectionType.DESELECTED,
		// ESelectionType.REMOVE);
		polylineSelectionManager.removeElements(ESelectionType.DESELECTED);
		resetSelections();
		setDisplayListDirty();
	}

	/**
	 * Initializes the array lists that contain the data. Must be run at program
	 * start, every time you exchange axis and polylines and every time you
	 * change storages or selections *
	 */
	@Override
	protected void initLists()
	{

		// TODO this needs only to be done if initLists has to be called during
		// runtime, not while initing
		// contentSelectionManager.resetSelectionManager();
		// storageSelectionManager.resetSelectionManager();

		if (bRenderOnlyContext)
			iContentVAID = mapVAIDs.get(EStorageBasedVAType.EXTERNAL_SELECTION);
		else
		{
			if (!mapVAIDs.containsKey(EStorageBasedVAType.COMPLETE_SELECTION))
				initCompleteList();
			iContentVAID = mapVAIDs.get(EStorageBasedVAType.COMPLETE_SELECTION);

		}
		iStorageVAID = mapVAIDs.get(EStorageBasedVAType.STORAGE_SELECTION);

		initContentVariables();

		contentSelectionManager.setVA(set.getVA(iContentVAID));
		storageSelectionManager.setVA(set.getVA(iStorageVAID));
		// iNumberOfEntriesToRender = alContentSelection.size();

		// int iNumberOfAxis = ;

		// // this for loop executes once per polyline
		// for (int iPolyLineCount = 0; iPolyLineCount <
		// iNumberOfPolyLinesToRender; iPolyLineCount++)
		// {
		// polylineSelectionManager.initialAdd(set.getVA(iPolylineVAID).get(
		// iPolyLineCount));
		// }
		//
		// // this for loop executes one per axis
		// for (int iAxisCount = 0; iAxisCount < iNumberOfAxis; iAxisCount++)
		// {
		// axisSelectionManager.initialAdd(set.getVA(iAxisVAID).get(iAxisCount));
		// }

		// initGates();
		hashGates = null;
	}

	/**
	 * Build mapping between polyline/axis and storage/content for virtual
	 * arrays and selection managers
	 */
	private void initContentVariables()
	{
		if (bRenderStorageHorizontally)
		{

			iPolylineVAID = iStorageVAID;
			iAxisVAID = iContentVAID;
			polylineSelectionManager = storageSelectionManager;
			axisSelectionManager = contentSelectionManager;
		}
		else
		{
			iPolylineVAID = iContentVAID;
			iAxisVAID = iStorageVAID;
			polylineSelectionManager = contentSelectionManager;
			axisSelectionManager = storageSelectionManager;
		}
	}

	/**
	 * Initialize the gates. The gate heights are saved in two lists, which
	 * contain the rendering height of the gate
	 */
	private void initGates()
	{
		hashGates = new HashMap<Integer, ArrayList<Pair<Float, Float>>>();
		IVirtualArray va = set.getVA(iAxisVAID);

		hashIsGateBlocking = new HashMap<Integer, ArrayList<Integer>>();

		for (Integer axisID : va)
		{
			ArrayList<Pair<Float, Float>> selections = new ArrayList<Pair<Float, Float>>();
			selections.add(new Pair<Float, Float>((renderStyle.getGateYOffset() - renderStyle
					.getGateTipHeight()), 0.0f));
			hashGates.put(axisID, selections);
			hashIsGateBlocking.put(axisID, new ArrayList<Integer>());
		}
	}

	@Override
	protected void initForAddedElements()
	{
		if (bRenderStorageHorizontally)
			initGates();
	}

	/**
	 * Build polyline display list. Renders coordinate system, polylines and
	 * gates, by calling the render methods
	 * 
	 * @param gl GL context
	 * @param iGLDisplayListIndex the index of the display list
	 */
	private void buildDisplayList(final GL gl, int iGLDisplayListIndex)
	{
		gl.glNewList(iGLDisplayListIndex, GL.GL_COMPILE);

		// if (contentSelectionManager.getNumberOfElements() == 0)
		// {
		// gl.glTranslatef(-fXDefaultTranslation - fXTranslation,
		// -fYTranslation, 0.0f);
		// renderSymbol(gl);
		// gl.glTranslatef(+fXDefaultTranslation + fXTranslation, fYTranslation,
		// 0.0f);
		// }
		// else
		// {

		renderCoordinateSystem(gl);

		// FIXME if uses z buffer fighting to avoid artfacts when tiltet
		if (detailLevel.compareTo(EDetailLevel.LOW) < 1)
		{
			renderPolylines(gl, ESelectionType.MOUSE_OVER);
			renderPolylines(gl, ESelectionType.SELECTION);
			renderPolylines(gl, ESelectionType.DESELECTED);
			renderPolylines(gl, ESelectionType.NORMAL);
		}
		else
		{
			renderPolylines(gl, ESelectionType.DESELECTED);
			renderPolylines(gl, ESelectionType.NORMAL);
			renderPolylines(gl, ESelectionType.MOUSE_OVER);
			renderPolylines(gl, ESelectionType.SELECTION);
		}

		renderGates(gl);

		// }

		gl.glEndList();
	}

	/**
	 * Polyline rendering method. All polylines that are contained in the
	 * polylineSelectionManager and are of the selection type specified in
	 * renderMode
	 * 
	 * @param gl the GL context
	 * @param renderMode the type of selection in the selection manager to
	 *            render
	 */
	@SuppressWarnings("unchecked")
	private void renderPolylines(GL gl, ESelectionType renderMode)
	{

		Set<Integer> setDataToRender = null;
		float fZDepth = 0f;

		if (renderMode == ESelectionType.DESELECTED || renderMode == ESelectionType.NORMAL)
		{
			iDisplayEveryNthPolyline = set.getVA(iContentVAID).size()
					/ iNumberOfRandomElements;
			if (iDisplayEveryNthPolyline == 0)
				iDisplayEveryNthPolyline = 1;
		}

		switch (renderMode)
		{
			case NORMAL:
				setDataToRender = polylineSelectionManager.getElements(renderMode);
				if (detailLevel.compareTo(EDetailLevel.LOW) < 1)
				{
					gl.glColor4fv(renderStyle
							.getPolylineDeselectedOcclusionPrevColor(setDataToRender.size()
									/ iDisplayEveryNthPolyline), 0);
					gl.glLineWidth(ParCoordsRenderStyle.DESELECTED_POLYLINE_LINE_WIDTH);
					fZDepth = -0.001f;
				}
				else
				{
					if (bPreventOcclusion)
						gl.glColor4fv(renderStyle
								.getPolylineOcclusionPrevColor(setDataToRender.size()
										/ iDisplayEveryNthPolyline), 0);
					else
						gl.glColor4fv(POLYLINE_NO_OCCLUSION_PREV_COLOR, 0);

					gl.glLineWidth(ParCoordsRenderStyle.POLYLINE_LINE_WIDTH);
				}
				break;
			case SELECTION:
				setDataToRender = polylineSelectionManager.getElements(renderMode);
				gl.glColor4fv(POLYLINE_SELECTED_COLOR, 0);
				gl.glLineWidth(SELECTED_POLYLINE_LINE_WIDTH);
				fZDepth = 0.002f;
				break;
			case MOUSE_OVER:
				setDataToRender = polylineSelectionManager.getElements(renderMode);
				gl.glColor4fv(POLYLINE_MOUSE_OVER_COLOR, 0);
				gl.glLineWidth(MOUSE_OVER_POLYLINE_LINE_WIDTH);
				fZDepth = 0.02f;
				break;
			case DESELECTED:
				setDataToRender = polylineSelectionManager.getElements(renderMode);
				gl.glColor4fv(renderStyle
						.getPolylineDeselectedOcclusionPrevColor(setDataToRender.size()
								/ iDisplayEveryNthPolyline), 0);
				gl.glLineWidth(DESELECTED_POLYLINE_LINE_WIDTH);
				break;
			default:
				setDataToRender = polylineSelectionManager.getElements(ESelectionType.NORMAL);
		}

		boolean bRenderingSelection = false;

		if (renderMode == ESelectionType.SELECTION || renderMode == ESelectionType.MOUSE_OVER
				&& detailLevel == EDetailLevel.HIGH)
			bRenderingSelection = true;

		Iterator<Integer> dataIterator = setDataToRender.iterator();
		// this loop executes once per polyline
		while (dataIterator.hasNext())
		{
			int iPolyLineID = dataIterator.next();
			if (bUseRandomSampling && (renderMode == ESelectionType.DESELECTED || renderMode == ESelectionType.NORMAL))
			{
				if (iPolyLineID % iDisplayEveryNthPolyline != 0)
					continue;
				// if(!alUseInRandomSampling.get(set.getVA(iPolylineVAID).indexOf(iPolyLineID)))
				// continue;
			}
			if (renderMode != ESelectionType.DESELECTED)
				gl.glPushName(pickingManager.getPickingID(iUniqueID,
						EPickingType.POLYLINE_SELECTION, iPolyLineID));

			if (!bRenderingSelection)
				gl.glBegin(GL.GL_LINE_STRIP);

			IStorage currentStorage = null;

			// decide on which storage to use when array is polyline
			if (bRenderStorageHorizontally)
			{
				int iWhichStorage = iPolyLineID;
				// currentStorage = set.getStorageFromVA(iStorageVAID,
				// iWhichStorage);
				currentStorage = set.get(iWhichStorage);// ,
				// iIndex)iStorageVAID,
				// iWhichStorage);
			}

			float fPreviousXValue = 0;
			float fPreviousYValue = 0;
			float fCurrentXValue = 0;
			float fCurrentYValue = 0;

			// this loop executes once per axis
			for (int iVertexCount = 0; iVertexCount < set.getVA(iAxisVAID).size(); iVertexCount++)
			{
				int iStorageIndex = 0;

				// get the index if array as polyline
				if (bRenderStorageHorizontally)
				{
					iStorageIndex = set.getVA(iContentVAID).get(iVertexCount);
				}
				// get the storage and the storage index for the different cases
				else
				{
					currentStorage = set.getStorageFromVA(iStorageVAID, iVertexCount);
					iStorageIndex = iPolyLineID;
				}

				fCurrentXValue = alAxisSpacing.get(iVertexCount);
				fCurrentYValue = currentStorage.getFloat(EDataRepresentation.NORMALIZED,
						iStorageIndex);
				if (Float.isNaN(fCurrentYValue))
				{
					fCurrentYValue = renderStyle.getNaNYOffset();
				}
				if (iVertexCount != 0)
				{
					if (bRenderingSelection)
						gl.glBegin(GL.GL_LINES);

					gl.glVertex3f(fPreviousXValue, fPreviousYValue
							* renderStyle.getAxisHeight(), fZDepth);
					gl.glVertex3f(fCurrentXValue,
							fCurrentYValue * renderStyle.getAxisHeight(), fZDepth);

					if (bRenderingSelection)
						gl.glEnd();

				}

				if (bRenderingSelection)
				{
					String sRawValue;
					if (currentStorage instanceof INumericalStorage)
					{
						sRawValue = getDecimalFormat().format(
								currentStorage
										.getFloat(EDataRepresentation.RAW, iStorageIndex));

					}
					else if (currentStorage instanceof INominalStorage)
					{
						sRawValue = ((INominalStorage<String>) currentStorage)
								.getRaw(iStorageIndex);
					}
					else
					{
						throw new IllegalStateException("Unknown Storage Type");
					}

					renderBoxedYValues(gl, fCurrentXValue, fCurrentYValue
							* renderStyle.getAxisHeight(), sRawValue, renderMode);
				}

				fPreviousXValue = fCurrentXValue;
				fPreviousYValue = fCurrentYValue;
			}

			if (!bRenderingSelection)
				gl.glEnd();

			if (renderMode != ESelectionType.DESELECTED)
				gl.glPopName();
		}
	}

	/**
	 * Render the coordinate system of the parallel coordinates, including the
	 * axis captions and axis-specific buttons
	 * 
	 * @param gl the gl context
	 * @param iNumberAxis
	 */
	private void renderCoordinateSystem(GL gl)
	{
		IVirtualArray axisVA = set.getVA(iAxisVAID);
		textRenderer.setColor(0, 0, 0, 1);

		int iNumberAxis = set.getVA(iAxisVAID).size();
		// draw X-Axis
		gl.glColor4fv(X_AXIS_COLOR, 0);
		gl.glLineWidth(X_AXIS_LINE_WIDTH);

		gl
				.glPushName(pickingManager.getPickingID(iUniqueID,
						EPickingType.X_AXIS_SELECTION, 1));
		gl.glBegin(GL.GL_LINES);

		gl.glVertex3f(renderStyle.getXAxisStart(), 0.0f, 0.0f);
		gl.glVertex3f(renderStyle.getXAxisEnd(), 0.0f, 0.0f);

		gl.glEnd();
		gl.glPopName();

		// draw all Y-Axis
		Set<Integer> selectedSet = axisSelectionManager.getElements(ESelectionType.SELECTION);
		Set<Integer> mouseOverSet = axisSelectionManager
				.getElements(ESelectionType.MOUSE_OVER);

		int iCount = 0;
		while (iCount < iNumberAxis)
		{
			float fXPosition = alAxisSpacing.get(iCount);
			if (selectedSet.contains(axisVA.get(iCount)))
			{
				gl.glColor4fv(Y_AXIS_SELECTED_COLOR, 0);
				gl.glLineWidth(Y_AXIS_SELECTED_LINE_WIDTH);
			}
			else if (mouseOverSet.contains(axisVA.get(iCount)))
			{
				gl.glColor4fv(Y_AXIS_MOUSE_OVER_COLOR, 0);
				gl.glLineWidth(Y_AXIS_MOUSE_OVER_LINE_WIDTH);
			}
			else
			{
				gl.glColor4fv(Y_AXIS_COLOR, 0);
				gl.glLineWidth(Y_AXIS_LINE_WIDTH);
			}
			gl.glPushName(pickingManager.getPickingID(iUniqueID,
					EPickingType.Y_AXIS_SELECTION, axisVA.get(iCount)));
			gl.glBegin(GL.GL_LINES);
			gl.glVertex3f(fXPosition, Y_AXIS_LOW, AXIS_Z);
			gl.glVertex3f(fXPosition, renderStyle.getAxisHeight(), AXIS_Z);

			// Top marker
			gl.glVertex3f(fXPosition - AXIS_MARKER_WIDTH, renderStyle.getAxisHeight(), AXIS_Z);
			gl.glVertex3f(fXPosition + AXIS_MARKER_WIDTH, renderStyle.getAxisHeight(), AXIS_Z);

			gl.glEnd();
			if (detailLevel != EDetailLevel.HIGH
					|| !renderStyle.isEnoughSpaceForText(iNumberAxis))
			{
				// pop the picking id here when we don't want to include the
				// axis label
				gl.glPopName();
			}
			else
			{

				// markers on axis
				float fMarkerSpacing = renderStyle.getAxisHeight() / (NUMBER_AXIS_MARKERS + 1);
				for (int iInnerCount = 1; iInnerCount <= NUMBER_AXIS_MARKERS; iInnerCount++)
				{
					float fCurrentHeight = fMarkerSpacing * iInnerCount;
					if (iCount == 0)
					{
						if (set.isSetHomogeneous())
						{
							float fNumber = (float) set.getRawForNormalized(fCurrentHeight
									/ renderStyle.getAxisHeight());

							Rectangle2D bounds = textRenderer.getBounds(getDecimalFormat()
									.format(fNumber));
							float fWidth = (float) bounds.getWidth()
									* renderStyle.getSmallFontScalingFactor();
							float fHeightHalf = (float) bounds.getHeight()
									* renderStyle.getSmallFontScalingFactor() / 3;

							renderNumber(getDecimalFormat().format(fNumber), fXPosition
									- fWidth - AXIS_MARKER_WIDTH, fCurrentHeight - fHeightHalf);
						}
						else
						{
							// TODO: storage based access
						}
					}
					gl.glColor3fv(Y_AXIS_COLOR, 0);
					gl.glBegin(GL.GL_LINES);
					gl.glVertex3f(fXPosition - AXIS_MARKER_WIDTH, fCurrentHeight, AXIS_Z);
					gl.glVertex3f(fXPosition + AXIS_MARKER_WIDTH, fCurrentHeight, AXIS_Z);
					gl.glEnd();

				}

				String sAxisLabel = null;
				switch (eAxisDataType)
				{
					// TODO not very generic here
					// case EXPERIMENT:
					// // Labels
					// // sAxisLabel = alDataStorages.get(iCount).getLabel();

					case EXPRESSION_INDEX:
						sAxisLabel = Integer.toString(getRefSeqFromStorageIndex(set.getVA(
								iContentVAID).get(iCount)));
						break;
					default:
						sAxisLabel = set.getStorageFromVA(iStorageVAID, iCount).getLabel();
						break;

				}
				gl.glPushAttrib(GL.GL_CURRENT_BIT | GL.GL_LINE_BIT);
				gl.glTranslatef(fXPosition, renderStyle.getAxisHeight()
						+ renderStyle.getAxisCaptionSpacing(), 0);
				gl.glRotatef(25, 0, 0, 1);
				textRenderer.begin3DRendering();
				textRenderer.draw3D(sAxisLabel, 0, 0, 0, renderStyle
						.getSmallFontScalingFactor());
				textRenderer.end3DRendering();
				gl.glRotatef(-25, 0, 0, 1);
				gl.glTranslatef(-fXPosition, -(renderStyle.getAxisHeight() + renderStyle
						.getAxisCaptionSpacing()), 0);

				if (set.isSetHomogeneous())
				{
					// textRenderer.begin3DRendering();
					//
					// // render values on top and bottom of axis
					//
					// // top
					// String text = getDecimalFormat().format(set.getMax());
					// textRenderer.draw3D(text, fXPosition + 2 *
					// AXIS_MARKER_WIDTH, renderStyle
					// .getAxisHeight(), 0,
					// renderStyle.getSmallFontScalingFactor());
					//
					// // bottom
					// text = getDecimalFormat().format(set.getMin());
					// textRenderer.draw3D(text, fXPosition + 2 *
					// AXIS_MARKER_WIDTH, 0, 0,
					// renderStyle.getSmallFontScalingFactor());
					// textRenderer.end3DRendering();
				}
				else
				{
					// TODO
				}

				gl.glPopAttrib();
				gl.glPopName();
				// render Buttons

				if (selectedSet.contains(axisVA.get(iCount))
						|| mouseOverSet.contains(axisVA.get(iCount)))
				{
					int iNumberOfButtons = 0;
					if (iCount != 0 || iCount != iNumberAxis - 1)
						iNumberOfButtons = 4;
					else
						iNumberOfButtons = 3;

					float fXButtonOrigin = 0;
					float fYButtonOrigin = 0;
					int iPickingID = -1;

					fXButtonOrigin = alAxisSpacing.get(iCount)
							- (iNumberOfButtons * renderStyle.getButtonWidht() + (iNumberOfButtons - 1)
									* renderStyle.getSmallSpacing()) / 2;
					fYButtonOrigin = -renderStyle.getAxisButtonYOffset();

					if (iCount != 0)
					{
						iPickingID = pickingManager.getPickingID(iUniqueID,
								EPickingType.MOVE_AXIS_LEFT, iCount);
						renderButton(gl, fXButtonOrigin, fYButtonOrigin, iPickingID,
								EIconTextures.ARROW_LEFT);
					}

					// remove button
					fXButtonOrigin = fXButtonOrigin + renderStyle.getButtonWidht()
							+ renderStyle.getSmallSpacing();

					iPickingID = pickingManager.getPickingID(iUniqueID,
							EPickingType.REMOVE_AXIS, iCount);
					renderButton(gl, fXButtonOrigin, fYButtonOrigin, iPickingID,
							EIconTextures.REMOVE);

					// duplicate axis button
					fXButtonOrigin = fXButtonOrigin + renderStyle.getButtonWidht()
							+ renderStyle.getSmallSpacing();
					iPickingID = pickingManager.getPickingID(iUniqueID,
							EPickingType.DUPLICATE_AXIS, iCount);
					renderButton(gl, fXButtonOrigin, fYButtonOrigin, iPickingID,
							EIconTextures.DUPLICATE);

					if (iCount != iNumberAxis - 1)
					{
						// right, move right button
						fXButtonOrigin = fXButtonOrigin + renderStyle.getButtonWidht()
								+ renderStyle.getSmallSpacing();
						iPickingID = pickingManager.getPickingID(iUniqueID,
								EPickingType.MOVE_AXIS_RIGHT, iCount);
						renderButton(gl, fXButtonOrigin, fYButtonOrigin, iPickingID,
								EIconTextures.ARROW_RIGHT);
					}
				}
			}
			iCount++;
		}
	}

	/**
	 * Render a Button, at a specified position with a specified picking ID and
	 * a specified texture
	 * 
	 * @param gl the GL context
	 * @param fXButtonOrigin the x origin of the button
	 * @param fYButtonOrigin the y origin of the button
	 * @param iPickingID the picking id to be assigned to the button
	 * @param eIconTextures the texture of the button
	 */
	private void renderButton(GL gl, float fXButtonOrigin, float fYButtonOrigin,
			int iPickingID, EIconTextures eIconTextures)
	{

		Texture tempTexture = iconTextureManager.getIconTexture(gl, eIconTextures);
		tempTexture.enable();
		tempTexture.bind();

		TextureCoords texCoords = tempTexture.getImageTexCoords();

		gl.glPushAttrib(GL.GL_CURRENT_BIT | GL.GL_LINE_BIT);
		gl.glColor4f(1, 1, 1, 1);
		gl.glPushName(iPickingID);
		gl.glBegin(GL.GL_POLYGON);

		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		gl.glVertex3f(fXButtonOrigin, fYButtonOrigin, AXIS_Z);
		gl.glTexCoord2f(texCoords.left(), texCoords.top());
		gl.glVertex3f(fXButtonOrigin, fYButtonOrigin + renderStyle.getButtonWidht(), AXIS_Z);
		gl.glTexCoord2f(texCoords.right(), texCoords.top());
		gl.glVertex3f(fXButtonOrigin + renderStyle.getButtonWidht(), fYButtonOrigin
				+ renderStyle.getButtonWidht(), AXIS_Z);
		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
		gl.glVertex3f(fXButtonOrigin + renderStyle.getButtonWidht(), fYButtonOrigin, AXIS_Z);
		gl.glEnd();
		gl.glPopName();
		gl.glPopAttrib();
		tempTexture.disable();
	}

	/**
	 * Render the symbol of the view instead of the view
	 * 
	 * @param gl
	 */
	private void renderSymbol(GL gl)
	{
		float fXButtonOrigin = 0.33f * renderStyle.getScaling();
		float fYButtonOrigin = 0.33f * renderStyle.getScaling();
		Texture tempTexture = iconTextureManager.getIconTexture(gl,
				EIconTextures.PAR_COORDS_SYMBOL);
		tempTexture.enable();
		tempTexture.bind();

		TextureCoords texCoords = tempTexture.getImageTexCoords();

		gl.glPushAttrib(GL.GL_CURRENT_BIT | GL.GL_LINE_BIT);
		gl.glColor4f(1f, 1, 1, 1f);
		gl.glBegin(GL.GL_POLYGON);

		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		gl.glVertex3f(fXButtonOrigin, fYButtonOrigin, 0.01f);
		gl.glTexCoord2f(texCoords.left(), texCoords.top());
		gl.glVertex3f(fXButtonOrigin, 2 * fYButtonOrigin, 0.01f);
		gl.glTexCoord2f(texCoords.right(), texCoords.top());
		gl.glVertex3f(fXButtonOrigin * 2, 2 * fYButtonOrigin, 0.01f);
		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
		gl.glVertex3f(fXButtonOrigin * 2, fYButtonOrigin, 0.01f);
		gl.glEnd();
		gl.glPopAttrib();
		tempTexture.disable();
	}

	/**
	 * Render the gates and update the fArGateHeights for the
	 * selection/unselection
	 * 
	 * @param gl
	 * @param iNumberAxis
	 */
	private void renderGates(GL gl)
	{
		if (detailLevel != EDetailLevel.HIGH)
			return;

		final float fGateWidth = renderStyle.getGateWidth();
		final float fGateTipHeight = renderStyle.getGateTipHeight();
		int iCount = 0;
		for (int iAxisID : set.getVA(iAxisVAID))
		{
			ArrayList<Pair<Float, Float>> alGatesPerAxis = hashGates.get(iAxisID);
			for (Pair<Float, Float> gate : alGatesPerAxis)
			{
				Float fBottom = gate.getFirst();
				Float fTop = gate.getSecond();

				float[] fArGateColor;
				if ((bIsGateMouseOver || bIsDraggingActive) && iAxisID == iDraggedGateNumber
						&& draggedObject == EPickingType.LOWER_GATE_TIP_SELECTION)
				{
					fArGateColor = POLYLINE_SELECTED_COLOR;
					bIsGateMouseOver = false;
				}
				else
				{
					fArGateColor = GATE_TIP_COLOR;
				}
				gl.glColor4fv(fArGateColor, 0);
				float fCurrentPosition = alAxisSpacing.get(iCount);

				// The tip of the gate (which is pickable)
				gl.glPushName(pickingManager.getPickingID(iUniqueID,
						EPickingType.LOWER_GATE_TIP_SELECTION, iAxisID));
				gl.glBegin(GL.GL_POLYGON);
				// variable
				gl.glVertex3f(fCurrentPosition + fGateWidth, fTop - fGateTipHeight, 0.001f);
				// variable
				gl.glVertex3f(fCurrentPosition, fTop, 0.001f);
				// variable
				gl.glVertex3f(fCurrentPosition - fGateWidth, fTop - fGateTipHeight, 0.001f);
				gl.glEnd();

				// invisible part, for better picking
				gl.glPushAttrib(GL.GL_CURRENT_BIT | GL.GL_LINE_BIT);

				gl.glBegin(GL.GL_POLYGON);
				gl.glColor4f(0, 0, 0, 0f);
				gl
						.glVertex3f(fCurrentPosition + 3 * fGateWidth, fTop - fGateTipHeight,
								0.001f);
				gl
						.glVertex3f(fCurrentPosition + 3 * fGateWidth, fTop + fGateTipHeight,
								0.001f);
				gl
						.glVertex3f(fCurrentPosition - 3 * fGateWidth, fTop + fGateTipHeight,
								0.001f);
				gl
						.glVertex3f(fCurrentPosition - 3 * fGateWidth, fTop - fGateTipHeight,
								0.001f);
				gl.glEnd();
				gl.glPopAttrib();
				gl.glPopName();

				if (detailLevel == EDetailLevel.HIGH)
				{
					if (set.isSetHomogeneous())
					{
						renderBoxedYValues(gl, fCurrentPosition, fTop, getDecimalFormat()
								.format(
										set.getRawForNormalized(fTop
												/ renderStyle.getAxisHeight())),
								ESelectionType.NORMAL);
					}
					else
					{
						// TODO storage based acces
					}

				}
				gl.glColor4fv(GATE_BODY_COLOR, 0);
				// The body of the gate
				gl.glPushName(pickingManager.getPickingID(iUniqueID,
						EPickingType.LOWER_GATE_BODY_SELECTION, iAxisID));
				gl.glBegin(GL.GL_POLYGON);
				// bottom
				gl
						.glVertex3f(fCurrentPosition - fGateWidth, fBottom + fGateTipHeight,
								0.0001f);
				// constant
				gl
						.glVertex3f(fCurrentPosition + fGateWidth, fBottom + fGateTipHeight,
								0.0001f);
				// top
				gl.glVertex3f(fCurrentPosition + fGateWidth, fTop - fGateTipHeight, 0.0001f);
				// top
				gl.glVertex3f(fCurrentPosition - fGateWidth, fTop - fGateTipHeight, 0.0001f);
				gl.glEnd();
				gl.glPopName();

				gl.glPushName(pickingManager.getPickingID(iUniqueID,
						EPickingType.LOWER_GATE_BOTTOM_SELECTION, iAxisID));

				if ((bIsGateMouseOver || bIsDraggingActive) && iAxisID == iDraggedGateNumber
						&& draggedObject == EPickingType.LOWER_GATE_BOTTOM_SELECTION)
				{
					fArGateColor = POLYLINE_SELECTED_COLOR;
					bIsGateMouseOver = false;
				}
				else
				{
					fArGateColor = GATE_TIP_COLOR;
				}
				gl.glColor4fv(fArGateColor, 0);
				// The bottom of the gate
				gl.glBegin(GL.GL_POLYGON);
				// variable
				gl.glVertex3f(fCurrentPosition + fGateWidth, fBottom + fGateTipHeight, 0.001f);
				// variable
				gl.glVertex3f(fCurrentPosition, fBottom, 0.001f);
				// variable
				gl.glVertex3f(fCurrentPosition - fGateWidth, fBottom + fGateTipHeight, 0.001f);
				gl.glEnd();

				// invisible part, for better picking
				gl.glPushAttrib(GL.GL_CURRENT_BIT | GL.GL_LINE_BIT);

				gl.glBegin(GL.GL_POLYGON);
				gl.glColor4f(0, 0, 0, 0.0f);
				gl.glVertex3f(fCurrentPosition + 3 * fGateWidth, fBottom, 0.001f);
				gl.glVertex3f(fCurrentPosition + 3 * fGateWidth, fBottom + fGateTipHeight,
						0.001f);
				gl.glVertex3f(fCurrentPosition - 3 * fGateWidth, fBottom + fGateTipHeight,
						0.001f);
				gl.glVertex3f(fCurrentPosition - 3 * fGateWidth, fBottom, 0.001f);
				gl.glEnd();
				gl.glPopAttrib();

				gl.glPopName();

				if (detailLevel == EDetailLevel.HIGH)
				{
					if (set.isSetHomogeneous())
					{
						float fValue = (float) set.getRawForNormalized(fBottom
								/ renderStyle.getAxisHeight());
						if (fValue > set.getMin())
							renderBoxedYValues(gl, fCurrentPosition, fBottom,
									getDecimalFormat().format(fValue), ESelectionType.NORMAL);
					}
					else
					{
						// TODO storage based access
					}
				}
				iCount++;
			}
		}
	}

	/**
	 * Render the captions on the axis
	 * 
	 * @param gl
	 * @param fXOrigin
	 * @param fYOrigin
	 * @param renderMode
	 */
	private void renderBoxedYValues(GL gl, float fXOrigin, float fYOrigin, String sRawValue,
			ESelectionType renderMode)
	{

		// don't render values that are below the y axis
		if (fYOrigin < 0)
			return;

		gl.glPushAttrib(GL.GL_CURRENT_BIT | GL.GL_LINE_BIT);
		gl.glLineWidth(Y_AXIS_LINE_WIDTH);
		gl.glColor4fv(Y_AXIS_COLOR, 0);

		Rectangle2D tempRectangle = textRenderer.getBounds(sRawValue);
		float fSmallSpacing = renderStyle.getVerySmallSpacing();
		float fBackPlaneWidth = (float) tempRectangle.getWidth()
				* renderStyle.getSmallFontScalingFactor();// +
		// 2
		// *
		// fSmallSpacing;
		float fBackPlaneHeight = (float) tempRectangle.getHeight()
				* renderStyle.getSmallFontScalingFactor();// +
		// 2
		// *
		// fSmallSpacing;
		float fXTextOrigin = fXOrigin + 2 * AXIS_MARKER_WIDTH;
		float fYTextOrigin = fYOrigin;

		gl.glColor4f(1f, 1f, 1f, 0.8f);
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(fXTextOrigin - fSmallSpacing, fYTextOrigin - fSmallSpacing, 0.03f);
		gl.glVertex3f(fXTextOrigin + fBackPlaneWidth, fYTextOrigin - fSmallSpacing, 0.03f);
		gl.glVertex3f(fXTextOrigin + fBackPlaneWidth, fYTextOrigin + fBackPlaneHeight, 0.03f);
		gl.glVertex3f(fXTextOrigin - fSmallSpacing, fYTextOrigin + fBackPlaneHeight, 0.03f);
		gl.glEnd();

		renderNumber(sRawValue, fXTextOrigin, fYTextOrigin);
		gl.glPopAttrib();
	}

	private void renderNumber(String sRawValue, float fXOrigin, float fYOrigin)
	{
		textRenderer.begin3DRendering();

		// String text = "";
		// if (Float.isNaN(fRawValue))
		// text = "NaN";
		// else
		// text = getDecimalFormat().format(fRawValue);

		textRenderer.draw3D(sRawValue, fXOrigin, fYOrigin, 0.031f, renderStyle
				.getSmallFontScalingFactor());
		textRenderer.end3DRendering();
	}

	/**
	 * Renders the gates and updates their values
	 * 
	 * @param gl
	 */
	private void handleGateDragging(GL gl)
	{

		// bIsDisplayListDirtyLocal = true;
		// bIsDisplayListDirtyRemote = true;
		Point currentPoint = pickingTriggerMouseAdapter.getPickedPoint();

		float[] fArTargetWorldCoordinates = GLCoordinateUtils
				.convertWindowCoordinatesToWorldCoordinates(gl, currentPoint.x, currentPoint.y);

		float height = fArTargetWorldCoordinates[1];

		// todo only valid for one gate

		Pair<Float, Float> gate = hashGates.get(iDraggedGateNumber).get(0);

		float fTop = gate.getSecond();
		float fBottom = gate.getFirst();

		if (bIsGateDraggingFirstTime)
		{
			fGateTopSpacing = fTop - height;
			fGateBottomSpacing = height - fBottom;
			bIsGateDraggingFirstTime = false;
		}
		float fTipUpperLimit = renderStyle.getAxisHeight();
		float fTipLowerLimit = fBottom + 2 * renderStyle.getGateTipHeight();
		float fBottomLowerLimit = renderStyle.getGateMinimumValue()
				- renderStyle.getGateTipHeight();
		float fBottomUpperLimit = fTop - 2 * renderStyle.getGateTipHeight();

		if (draggedObject == EPickingType.LOWER_GATE_TIP_SELECTION)
		{
			gate.setSecond(height);
		}
		else if (draggedObject == EPickingType.LOWER_GATE_BOTTOM_SELECTION)
		{
			gate.setFirst(height);
		}
		else if (draggedObject == EPickingType.LOWER_GATE_BODY_SELECTION)
		{
			gate.setSecond(height + fGateTopSpacing);
			gate.setFirst(height - fGateBottomSpacing);

		}

		if (fTop > fTipUpperLimit)
			gate.setSecond(fTipUpperLimit);
		if (fTop < fTipLowerLimit)
			gate.setSecond(fTipLowerLimit);
		if (fBottom > fBottomUpperLimit)
			gate.setFirst(fBottomUpperLimit);
		if (fBottom < fBottomLowerLimit)
			gate.setFirst(fBottomLowerLimit);

		bIsDisplayListDirtyLocal = true;
		bIsDisplayListDirtyRemote = true;

//		System.out.println("Handle dragging");
		
		if (pickingTriggerMouseAdapter.wasMouseReleased())
		{
			bIsDraggingActive = false;
			System.out.println("Mouse released in view");
		}

	}

	/**
	 * Unselect all lines that are deselected with the gates
	 * 
	 * @param iAxisNumber
	 */
	// TODO revise
	private void handleGateUnselection(int iAxisNumber)
	{
		IVirtualArray axisVA = set.getVA(iAxisVAID);
		int iAxisID = axisVA.get(iAxisNumber);
		ArrayList<Integer> alCurrentGateBlocks = hashIsGateBlocking.get(iAxisID);
		alCurrentGateBlocks.clear();

		float fCurrentValue = -1;
		for (int iPolylineIndex : set.getVA(iPolylineVAID))
		{
			if (bRenderStorageHorizontally)
			{
				fCurrentValue = set.get(iPolylineIndex).getFloatVA(
						EDataRepresentation.NORMALIZED, iAxisNumber, iContentVAID);
			}
			else
			{
				fCurrentValue = set.getStorageFromVA(iStorageVAID, iAxisNumber).getFloat(
						EDataRepresentation.NORMALIZED, iPolylineIndex);
			}

			if (Float.isNaN(fCurrentValue))
				fCurrentValue = renderStyle.getNaNYOffset();
			for (Pair<Float, Float> gate : hashGates.get(iAxisID))
			{

				if (fCurrentValue <= (gate.getSecond() - 0.0000000001f)
						/ renderStyle.getAxisHeight()
						&& fCurrentValue >= gate.getFirst() / renderStyle.getAxisHeight())
				{
					alCurrentGateBlocks.add(iPolylineIndex);
				}
			}
		}

	}

	@Override
	protected void reactOnExternalSelection()
	{
		// TODO check if necessary to do both
		checkUnselection();
		resetAxisSpacing();
		// initGates();
	}

	@Override
	protected void reactOnVAChanges(IVirtualArrayDelta delta)
	{
		if (delta.getIDType() == eAxisDataType)
		{

			IVirtualArray axisVA = set.getVA(iAxisVAID);
			for (VADeltaItem item : delta)
			{
				int iElement = axisVA.get(item.getIndex());
				if (item.getType() == EVAOperation.REMOVE)
				{
					resetAxisSpacing();
					if (axisVA.containsElement(iElement) == 1)
						hashGates.remove(iElement);
				}
				else if (item.getType() == EVAOperation.REMOVE_ELEMENT)
				{
					resetAxisSpacing();
					hashGates.remove(item.getPrimaryID());
				}
			}
		}

	}

	// TODO: revise this, not very performance friendly, especially the clearing
	// of the DESELECTED
	private void checkUnselection()
	{
		if (hashGates == null)
		{
			initGates();
		}

		for (int iCount = 0; iCount < hashGates.size(); iCount++)
		{
			handleGateUnselection(iCount);
		}
		HashMap<Integer, Boolean> hashDeselectedPolylines = new HashMap<Integer, Boolean>();

		for (ArrayList<Integer> alCurrent : hashIsGateBlocking.values())
		{
			for (Integer iCurrent : alCurrent)
			{
				hashDeselectedPolylines.put(iCurrent, true);
			}
		}

		for (ArrayList<Integer> alCurrent : alIsAngleBlocking)
		{
			for (Integer iCurrent : alCurrent)
			{
				hashDeselectedPolylines.put(iCurrent, true);
			}
		}

		polylineSelectionManager.clearSelection(ESelectionType.DESELECTED);

		for (int iCurrent : hashDeselectedPolylines.keySet())
		{
			polylineSelectionManager.addToType(ESelectionType.DESELECTED, iCurrent);
		}
	}

	@Override
	protected void handleEvents(final EPickingType ePickingType,
			final EPickingMode ePickingMode, final int iExternalID, final Pick pick)
	{
		if (detailLevel == EDetailLevel.VERY_LOW || bIsDraggingActive || bWasAxisMoved)
		{
			pickingManager.flushHits(iUniqueID, ePickingType);
			return;
		}
		ESelectionType eSelectionType;
		switch (ePickingType)
		{
			case POLYLINE_SELECTION:
				switch (ePickingMode)
				{
					case DOUBLE_CLICKED:
						IDListEventContainer<Integer> idListEventContainer = new IDListEventContainer<Integer>(
								EEventType.LOAD_PATHWAY_BY_GENE, EIDType.REFSEQ_MRNA_INT);
						idListEventContainer.addID(getRefSeqFromStorageIndex(iExternalID));
						triggerEvent(EMediatorType.SELECTION_MEDIATOR, idListEventContainer);
						// intentionally no break

					case CLICKED:
						eSelectionType = ESelectionType.SELECTION;
						if (bAngularBrushingSelectPolyline)
						{
							bAngularBrushingSelectPolyline = false;
							bIsAngularBrushingActive = true;
							iSelectedLineID = iExternalID;
							linePick = pick;
							bIsAngularBrushingFirstTime = true;
						}

						break;
					case MOUSE_OVER:
						eSelectionType = ESelectionType.MOUSE_OVER;
						break;

					default:
						pickingManager.flushHits(iUniqueID, ePickingType);
						return;

				}
				connectedElementRepresentationManager.clear(ePolylineDataType);
				
				if (polylineSelectionManager.checkStatus(eSelectionType, iExternalID))
					break;
				
				polylineSelectionManager.clearSelection(eSelectionType);

				if (ePolylineDataType == EIDType.EXPRESSION_INDEX)
				{
					// Resolve multiple spotting on chip and add all to the
					// selection manager.
					Integer iRefSeqID = idMappingManager.getID(
							EMappingType.EXPRESSION_INDEX_2_REFSEQ_MRNA_INT, iExternalID);
					for (Object iExpressionIndex : idMappingManager.getMultiID(
							EMappingType.REFSEQ_MRNA_INT_2_EXPRESSION_INDEX, iRefSeqID))
					{
						contentSelectionManager.addToType(eSelectionType,
								(Integer) iExpressionIndex);
					}
				}
				else
				{
					polylineSelectionManager.addToType(eSelectionType, iExternalID);
				}

				polylineSelectionManager.addConnectionID(generalManager.getIDManager()
						.createID(EManagedObjectType.CONNECTION), iExternalID);

				if (ePolylineDataType == EIDType.EXPRESSION_INDEX
						&& !bAngularBrushingSelectPolyline)
				{
					triggerEvent(EMediatorType.SELECTION_MEDIATOR,
							new SelectionCommandEventContainer(EIDType.REFSEQ_MRNA_INT,
									new SelectionCommand(ESelectionCommandType.CLEAR,
											eSelectionType)));
					ISelectionDelta selectionDelta = contentSelectionManager.getDelta();
					handleConnectedElementRep(selectionDelta);
					triggerEvent(EMediatorType.SELECTION_MEDIATOR,
							new DeltaEventContainer<ISelectionDelta>(selectionDelta));

				}

				setDisplayListDirty();
				pickingManager.flushHits(iUniqueID, ePickingType);
				break;

			case X_AXIS_SELECTION:
				pickingManager.flushHits(iUniqueID, ePickingType);
				break;
			case Y_AXIS_SELECTION:

				switch (ePickingMode)
				{
					case CLICKED:
						eSelectionType = ESelectionType.SELECTION;
						break;

					case MOUSE_OVER:
						eSelectionType = ESelectionType.MOUSE_OVER;
						break;

					default:
						pickingManager.flushHits(iUniqueID, ePickingType);
						return;

				}

				axisSelectionManager.clearSelection(eSelectionType);
				axisSelectionManager.addToType(eSelectionType, iExternalID);

				axisSelectionManager.addConnectionID(generalManager.getIDManager().createID(
						EManagedObjectType.CONNECTION), iExternalID);

				connectedElementRepresentationManager.clear(eAxisDataType);

				// triggerSelectionUpdate(EMediatorType.SELECTION_MEDIATOR,
				// axisSelectionManager
				// .getDelta(), null);

			
				triggerEvent(EMediatorType.SELECTION_MEDIATOR,
						new SelectionCommandEventContainer(eAxisDataType,
								new SelectionCommand(ESelectionCommandType.CLEAR,
										eSelectionType)));
				ISelectionDelta selectionDelta = axisSelectionManager.getDelta();
				if (eAxisDataType == EIDType.EXPRESSION_INDEX)
				{
					handleConnectedElementRep(selectionDelta);
				}
				triggerEvent(EMediatorType.SELECTION_MEDIATOR,
						new DeltaEventContainer<ISelectionDelta>(selectionDelta));

				rePosition(iExternalID);
				setDisplayListDirty();
				pickingManager.flushHits(iUniqueID, ePickingType);
				break;
			case LOWER_GATE_TIP_SELECTION:
				switch (ePickingMode)
				{
					case MOUSE_OVER:
						bIsGateMouseOver = true;
						iDraggedGateNumber = iExternalID;
						draggedObject = EPickingType.LOWER_GATE_TIP_SELECTION;
						setDisplayListDirty();
						
//						System.out.println("Lower gate mouse over");
						break;
					case CLICKED:
						bIsDraggingActive = true;
						draggedObject = EPickingType.LOWER_GATE_TIP_SELECTION;
						iDraggedGateNumber = iExternalID;
						
						System.out.println("Lower gate top click");
						break;
					// case DRAGGED:
					// bIsDraggingActive = true;
					// draggedObject = EPickingType.LOWER_GATE_TIP_SELECTION;
					// iDraggedGateNumber = iExternalID;
					// break;

				}
				pickingManager.flushHits(iUniqueID, ePickingType);
				break;
			case LOWER_GATE_BOTTOM_SELECTION:
				switch (ePickingMode)
				{
					case MOUSE_OVER:
						bIsGateMouseOver = true;
						iDraggedGateNumber = iExternalID;
						draggedObject = EPickingType.LOWER_GATE_BOTTOM_SELECTION;
						setDisplayListDirty();
						
						System.out.println("Lower gate mouse over");
						break;
					case CLICKED:
						bIsDraggingActive = true;
						draggedObject = EPickingType.LOWER_GATE_BOTTOM_SELECTION;
						iDraggedGateNumber = iExternalID;

						System.out.println("Lower gate clicked");
						break;
					// case DRAGGED:
					// bIsDraggingActive = true;
					// draggedObject = EPickingType.LOWER_GATE_BOTTOM_SELECTION;
					// iDraggedGateNumber = iExternalID;
					// break;
				}
				pickingManager.flushHits(iUniqueID, ePickingType);
				break;

			case LOWER_GATE_BODY_SELECTION:
				switch (ePickingMode)
				{
					case MOUSE_OVER:
						bIsGateMouseOver = true;
						iDraggedGateNumber = iExternalID;
						draggedObject = EPickingType.LOWER_GATE_BODY_SELECTION;
						setDisplayListDirty();
						break;
					case CLICKED:
						bIsDraggingActive = true;
						bIsGateDraggingFirstTime = true;
						draggedObject = EPickingType.LOWER_GATE_BODY_SELECTION;
						iDraggedGateNumber = iExternalID;
						break;
					// case DRAGGED:
					// bIsDraggingActive = true;
					// draggedObject = EPickingType.LOWER_GATE_BODY_SELECTION;
					// iDraggedGateNumber = iExternalID;
					// break;
				}
				pickingManager.flushHits(iUniqueID, ePickingType);
				break;
			case PC_ICON_SELECTION:
				switch (ePickingMode)
				{
					case CLICKED:

						break;
				}

				pickingManager.flushHits(iUniqueID, EPickingType.PC_ICON_SELECTION);
				break;
			case REMOVE_AXIS:
				IVirtualArray axisVA = set.getVA(iAxisVAID);
				switch (ePickingMode)
				{
					case CLICKED:
						if (axisVA.containsElement(axisVA.get(iExternalID)) == 1)
							hashGates.remove(axisVA.get(iExternalID));
						axisVA.remove(iExternalID);

						IVirtualArrayDelta vaDelta = new VirtualArrayDelta(
								EIDType.EXPERIMENT_INDEX);
						vaDelta.add(VADeltaItem.remove(iExternalID));
						generalManager.getEventPublisher().triggerEvent(
								EMediatorType.SELECTION_MEDIATOR, this,
								new DeltaEventContainer<IVirtualArrayDelta>(vaDelta));
						setDisplayListDirty();
						resetAxisSpacing();
						break;
				}
				pickingManager.flushHits(iUniqueID, EPickingType.REMOVE_AXIS);
				break;
			case MOVE_AXIS_LEFT:
				switch (ePickingMode)
				{
					case CLICKED:
						if (iExternalID > 0)
						{
							bWasAxisMoved = true;
							bWasAxisDraggedFirstTime = true;
							iMovedAxisPosition = iExternalID;

							setDisplayListDirty();

						}
						break;
				}
				pickingManager.flushHits(iUniqueID, EPickingType.MOVE_AXIS_LEFT);
				break;
			case MOVE_AXIS_RIGHT:

				switch (ePickingMode)
				{
					case CLICKED:
						resetAxisSpacing();
						setDisplayListDirty();

						break;
				}
				pickingManager.flushHits(iUniqueID, EPickingType.MOVE_AXIS_RIGHT);
				break;
			case DUPLICATE_AXIS:
				switch (ePickingMode)
				{
					case CLICKED:
						if (iExternalID > 0)
						{
							set.getVA(iAxisVAID).copy(iExternalID);
							IVirtualArrayDelta vaDelta = new VirtualArrayDelta(
									EIDType.EXPERIMENT_INDEX);
							vaDelta.add(VADeltaItem.copy(iExternalID));
							generalManager.getEventPublisher().triggerEvent(
									EMediatorType.SELECTION_MEDIATOR, this,
									new DeltaEventContainer<IVirtualArrayDelta>(vaDelta));
							setDisplayListDirty();
							// resetSelections();
							// initGates();
							resetAxisSpacing();
							break;
						}
				}
				pickingManager.flushHits(iUniqueID, EPickingType.DUPLICATE_AXIS);
				break;
			case ANGULAR_UPPER:
				switch (ePickingMode)
				{
					case CLICKED:
						bIsAngularDraggingActive = true;
					case DRAGGED:
						bIsAngularDraggingActive = true;
				}
				pickingManager.flushHits(iUniqueID, EPickingType.ANGULAR_UPPER);
				break;

			case ANGULAR_LOWER:
				switch (ePickingMode)
				{
					case CLICKED:
						bIsAngularDraggingActive = true;
					case DRAGGED:
						bIsAngularDraggingActive = true;
				}
				break;
		}
	}

	@Override
	protected SelectedElementRep createElementRep(EIDType idType, int iStorageIndex)
			throws InvalidAttributeValueException
	{
		// TODO only for one element atm

		float fXValue = 0;
		float fYValue = 0;

		if ((bRenderStorageHorizontally && idType == EIDType.EXPRESSION_INDEX)
				|| (!bRenderStorageHorizontally && idType == EIDType.EXPERIMENT_INDEX))
		{
			fXValue = set.getVA(iAxisVAID).indexOf(iStorageIndex)
					* renderStyle.getAxisSpacing(set.getVA(iAxisVAID).size());
			fXValue = fXValue + renderStyle.getXSpacing();
			fYValue = renderStyle.getBottomSpacing();
		}
		else
		{

			fXValue = renderStyle.getXSpacing() + fXTranslation;
			// get the value on the leftmost axis
			fYValue = set.getStorageFromVA(iStorageVAID, 0).getFloat(
					EDataRepresentation.NORMALIZED, iStorageIndex);

			if (Float.isNaN(fYValue))
			{
				fYValue = renderStyle.getNaNYOffset() * renderStyle.getAxisHeight()
						+ renderStyle.getBottomSpacing();
			}
			else
			{
				fYValue = fYValue * renderStyle.getAxisHeight()
						+ renderStyle.getBottomSpacing();
			}
		}

		SelectedElementRep elementRep = new SelectedElementRep(idType, iUniqueID, fXValue,
				fYValue, 0.0f);
		return elementRep;
	}

	@Override
	public synchronized String getShortInfo()
	{
		int iNumLines = set.getVA(iContentVAID).size();
		String message = "Parallel Coordinates (" + iNumLines + " genes / "
				+ set.getVA(iStorageVAID).size() + " experiments)";
		if (iDisplayEveryNthPolyline != 1)
			message += ", rendering a sample of " + (int) iNumLines / iDisplayEveryNthPolyline;
		return message;

	}

	@Override
	public synchronized String getDetailedInfo()
	{
		StringBuffer sInfoText = new StringBuffer();
		sInfoText.append("<b>Type:</b> Parallel Coordinates\n");
		sInfoText.append(set.getVA(iPolylineVAID).size() + " Genes as polylines and "
				+ set.getVA(iAxisVAID).size() + " experiments as axis.\n");

		if (bRenderOnlyContext)
		{
			sInfoText
					.append("Showing only genes which occur in one of the other views in focus\n");
		}
		else
		{
			if (bUseRandomSampling)
			{
				sInfoText.append("Random sampling active, sample size: "
						+ iNumberOfRandomElements + "\n");
			}
			else
			{
				sInfoText.append("Random sampling inactive\n");
			}

			if (dataFilterLevel == EDataFilterLevel.COMPLETE)
				sInfoText.append("Showing all Genes in the dataset\n");
			else if (dataFilterLevel == EDataFilterLevel.ONLY_MAPPING)
				sInfoText.append("Showing all Genes that have a known DAVID ID mapping\n");
			else if (dataFilterLevel == EDataFilterLevel.ONLY_CONTEXT)
				sInfoText
						.append("Showing all genes that are contained in any of the KEGG or Biocarta Pathways\n");
		}

		return sInfoText.toString();
	}

	/**
	 * Re-position a view centered on a element, specified by the element ID
	 * 
	 * @param iElementID the ID of the element that should be in the center
	 */
	protected void rePosition(int iElementID)
	{

		// IVirtualArray virtualArray;
		// if (bRenderStorageHorizontally)
		// virtualArray = set.getVA(iContentVAID);
		// else
		// virtualArray = set.getVA(iStorageVAID);
		//
		// float fCurrentPosition =
		// virtualArray.indexOf(iElementID) * fAxisSpacing +
		// renderStyle.getXSpacing();
		//
		// float fFrustumLength = viewFrustum.getRight() -
		// viewFrustum.getLeft();
		// float fLength = (virtualArray.size() - 1) * fAxisSpacing;
		//
		// fXTargetTranslation = -(fCurrentPosition - fFrustumLength / 2);
		//
		// if (-fXTargetTranslation > fLength - fFrustumLength)
		// fXTargetTranslation = -(fLength - fFrustumLength + 2 *
		// renderStyle.getXSpacing());
		// else if (fXTargetTranslation > 0)
		// fXTargetTranslation = 0;
		// else if (-fXTargetTranslation < -fXTranslation + fFrustumLength / 2
		// - renderStyle.getXSpacing()
		// && -fXTargetTranslation > -fXTranslation - fFrustumLength / 2
		// + renderStyle.getXSpacing())
		// {
		// fXTargetTranslation = fXTranslation;
		// return;
		// }
		//
		// bIsTranslationActive = true;
	}

	// TODO
	private void doTranslation()
	{

		float fDelta = 0;
		if (fXTargetTranslation < fXTranslation - 0.3)
		{

			fDelta = -0.3f;

		}
		else if (fXTargetTranslation > fXTranslation + 0.3)
		{
			fDelta = 0.3f;
		}
		else
		{
			fDelta = fXTargetTranslation - fXTranslation;
			bIsTranslationActive = false;
		}

		if (elementRep != null)
		{
			ArrayList<Vec3f> alPoints = elementRep.getPoints();
			for (Vec3f currentPoint : alPoints)
			{
				currentPoint.setX(currentPoint.x() + fDelta);
			}
		}

		fXTranslation += fDelta;
	}

	// TODO
	private void handleAngularBrushing(final GL gl)
	{

		if (bIsAngularBrushingFirstTime)
		{
			fCurrentAngle = fDefaultAngle;
			Point currentPoint = linePick.getPickedPoint();
			float[] fArPoint = GLCoordinateUtils.convertWindowCoordinatesToWorldCoordinates(
					gl, currentPoint.x, currentPoint.y);
			vecAngularBrushingPoint = new Vec3f(fArPoint[0], fArPoint[1], 0.01f);
			bIsAngularBrushingFirstTime = false;

		}
		alIsAngleBlocking.get(0).clear();

		int iPosition = 0;

		for (int iCount = 0; iCount < alAxisSpacing.size() - 1; iCount++)
		{
			if (vecAngularBrushingPoint.x() > alAxisSpacing.get(iCount)
					&& vecAngularBrushingPoint.x() < alAxisSpacing.get(iCount + 1))
				iPosition = iCount;
		}

		int iAxisLeftIndex;
		int iAxisRightIndex;

		iAxisLeftIndex = set.getVA(iAxisVAID).get(iPosition);
		iAxisRightIndex = set.getVA(iAxisVAID).get(iPosition + 1);

		Vec3f vecLeftPoint = new Vec3f(0, 0, 0);
		Vec3f vecRightPoint = new Vec3f(0, 0, 0);

		if (bRenderStorageHorizontally)
		{
			vecLeftPoint.setY(set.get(iSelectedLineID).getFloat(
					EDataRepresentation.NORMALIZED, iAxisLeftIndex)
					* renderStyle.getAxisHeight());
			vecRightPoint.setY(set.get(iSelectedLineID).getFloat(
					EDataRepresentation.NORMALIZED, iAxisRightIndex)
					* renderStyle.getAxisHeight());
		}
		else
		{
			vecLeftPoint.setY(set.get(iAxisLeftIndex).getFloat(EDataRepresentation.NORMALIZED,
					iSelectedLineID)
					* renderStyle.getAxisHeight());
			vecRightPoint.setY(set.get(iAxisRightIndex).getFloat(
					EDataRepresentation.NORMALIZED, iSelectedLineID)
					* renderStyle.getAxisHeight());
		}

		vecLeftPoint.setX(alAxisSpacing.get(iPosition));
		vecRightPoint.setX(alAxisSpacing.get(iPosition + 1));

		Vec3f vecDirectional = vecRightPoint.minus(vecLeftPoint);
		float fLength = vecDirectional.length();
		vecDirectional.normalize();

		Vec3f vecTriangleOrigin = vecLeftPoint.addScaled(fLength / 4, vecDirectional);

		Vec3f vecTriangleLimit = vecLeftPoint.addScaled(fLength / 4 * 3, vecDirectional);

		Rotf rotf = new Rotf();

		Vec3f vecCenterLine = vecTriangleLimit.minus(vecTriangleOrigin);
		float fLegLength = vecCenterLine.length();

		if (bIsAngularDraggingActive)
		{
			Point pickedPoint = pickingTriggerMouseAdapter.getPickedPoint();
			float fArPoint[] = GLCoordinateUtils.convertWindowCoordinatesToWorldCoordinates(
					gl, pickedPoint.x, pickedPoint.y);
			Vec3f vecPickedPoint = new Vec3f(fArPoint[0], fArPoint[1], 0.01f);
			Vec3f vecTempLine = vecPickedPoint.minus(vecTriangleOrigin);

			fCurrentAngle = getAngle(vecTempLine, vecCenterLine);

			bIsDisplayListDirtyLocal = true;
			bIsDisplayListDirtyRemote = true;
		}

		rotf.set(new Vec3f(0, 0, 1), fCurrentAngle);

		Vec3f vecUpperPoint = rotf.rotateVector(vecCenterLine);
		rotf.set(new Vec3f(0, 0, 1), -fCurrentAngle);
		Vec3f vecLowerPoint = rotf.rotateVector(vecCenterLine);

		vecUpperPoint.add(vecTriangleOrigin);
		vecLowerPoint.add(vecTriangleOrigin);

		gl.glColor4fv(ANGULAR_COLOR, 0);
		gl.glLineWidth(ANGLUAR_LINE_WIDTH);

		gl.glPushName(pickingManager.getPickingID(iUniqueID, EPickingType.ANGULAR_UPPER,
				iPosition));
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3f(vecTriangleOrigin.x(), vecTriangleOrigin.y(),
				vecTriangleOrigin.z() + 0.02f);
		gl.glVertex3f(vecUpperPoint.x(), vecUpperPoint.y(), vecUpperPoint.z() + 0.02f);
		gl.glEnd();
		gl.glPopName();

		gl.glPushName(pickingManager.getPickingID(iUniqueID, EPickingType.ANGULAR_UPPER,
				iPosition));
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3f(vecTriangleOrigin.x(), vecTriangleOrigin.y(),
				vecTriangleOrigin.z() + 0.02f);
		gl.glVertex3f(vecLowerPoint.x(), vecLowerPoint.y(), vecLowerPoint.z() + 0.02f);
		gl.glEnd();
		gl.glPopName();

		// draw angle polygon

		gl.glColor4fv(ANGULAR_POLYGON_COLOR, 0);
		// gl.glColor4f(1, 0, 0, 0.5f);
		gl.glBegin(GL.GL_POLYGON);
		rotf.set(new Vec3f(0, 0, 1), -fCurrentAngle / 10);
		Vec3f tempVector = vecCenterLine.copy();
		gl.glVertex3f(vecTriangleOrigin.x(), vecTriangleOrigin.y(),
				vecTriangleOrigin.z() + 0.02f);

		for (int iCount = 0; iCount <= 10; iCount++)
		{
			Vec3f vecPoint = tempVector.copy();
			vecPoint.normalize();
			vecPoint.scale(fLegLength);
			gl.glVertex3f(vecTriangleOrigin.x() + vecPoint.x(), vecTriangleOrigin.y()
					+ vecPoint.y(), vecTriangleOrigin.z() + vecPoint.z() + 0.02f);
			tempVector = rotf.rotateVector(tempVector);
		}
		gl.glEnd();

		gl.glBegin(GL.GL_POLYGON);
		rotf.set(new Vec3f(0, 0, 1), fCurrentAngle / 10);
		tempVector = vecCenterLine.copy();

		gl.glVertex3f(vecTriangleOrigin.x(), vecTriangleOrigin.y(),
				vecTriangleOrigin.z() + 0.02f);
		for (int iCount = 0; iCount <= 10; iCount++)
		{
			Vec3f vecPoint = tempVector.copy();
			vecPoint.normalize();
			vecPoint.scale(fLegLength);
			gl.glVertex3f(vecTriangleOrigin.x() + vecPoint.x(), vecTriangleOrigin.y()
					+ vecPoint.y(), vecTriangleOrigin.z() + vecPoint.z() + 0.02f);

			tempVector = rotf.rotateVector(tempVector);

		}

		// gl.glVertex3f(vecUpperPoint.x(), vecUpperPoint.y(), vecUpperPoint.z()
		// + 0.02f);
		gl.glEnd();

		// check selection

		for (Integer iCurrent : set.getVA(iPolylineVAID))
		{
			if (bRenderStorageHorizontally)
			{
				vecLeftPoint.setY(set.get(iCurrent).getFloat(EDataRepresentation.NORMALIZED,
						iAxisLeftIndex)
						* renderStyle.getAxisHeight());
				vecRightPoint.setY(set.get(iCurrent).getFloat(EDataRepresentation.NORMALIZED,
						iAxisRightIndex)
						* renderStyle.getAxisHeight());
			}
			else
			{
				vecLeftPoint.setY(set.get(iAxisLeftIndex).getFloat(
						EDataRepresentation.NORMALIZED, iCurrent)
						* renderStyle.getAxisHeight());
				vecRightPoint.setY(set.get(iAxisRightIndex).getFloat(
						EDataRepresentation.NORMALIZED, iCurrent)
						* renderStyle.getAxisHeight());
			}

			vecLeftPoint.setX(alAxisSpacing.get(iPosition));
			vecRightPoint.setX(alAxisSpacing.get(iPosition + 1));

			// Vec3f vecCompareLine = vecLeftPoint.minus(vecRightPoint);
			Vec3f vecCompareLine = vecRightPoint.minus(vecLeftPoint);
			float fCompareAngle = getAngle(vecCompareLine, vecCenterLine);

			if (fCompareAngle > fCurrentAngle || fCompareAngle < -fCurrentAngle)
			// !(fCompareAngle < fAngle && fCompareAngle < -fAngle))
			{
				// contentSelectionManager.addToType(EViewInternalSelectionType
				// .DESELECTED, iCurrent);
				alIsAngleBlocking.get(0).add(iCurrent);
			}
			// else
			// {
			// // TODO combinations
			// //contentSelectionManager.addToType(EViewInternalSelectionType.
			// NORMAL, iCurrent);
			// }

		}

		if (pickingTriggerMouseAdapter.wasMouseReleased())
		{
			bIsAngularDraggingActive = false;
			// bIsAngularBrushingActive = false;
			
			System.out.println("Mouse released in view");
		}

	}

	private float getAngle(final Vec3f vecOne, final Vec3f vecTwo)
	{
		Vec3f vecNewOne = vecOne.copy();
		Vec3f vecNewTwo = vecTwo.copy();

		vecNewOne.normalize();
		vecNewTwo.normalize();
		float fTmp = vecNewOne.dot(vecNewTwo);
		return (float) Math.acos(fTmp);
	}

	@Override
	public void changeOrientation(boolean defaultOrientation)
	{
		renderStorageAsPolyline(defaultOrientation);
	}

	@Override
	public boolean isInDefaultOrientation()
	{
		return bRenderStorageHorizontally;
	}

	private void adjustAxisSpacing(GL gl)
	{
		IVirtualArray axisVA = set.getVA(iAxisVAID);

		Point currentPoint = pickingTriggerMouseAdapter.getPickedPoint();

		float[] fArTargetWorldCoordinates = GLCoordinateUtils
				.convertWindowCoordinatesToWorldCoordinates(gl, currentPoint.x, currentPoint.y);

		float fWidth = fArTargetWorldCoordinates[0] - fXTranslation - fXDefaultTranslation;

		if (bWasAxisDraggedFirstTime)
		{
			// adjust from the actually clicked point to the center of the axis
			fAxisDraggingOffset = fWidth - alAxisSpacing.get(iMovedAxisPosition);
			bWasAxisDraggedFirstTime = false;
		}

		fWidth -= fAxisDraggingOffset;

		if (fWidth < renderStyle.getXAxisStart())
			fWidth = renderStyle.getXAxisStart();
		if (fWidth > renderStyle.getXAxisEnd())
			fWidth = renderStyle.getXAxisEnd();

		if (iMovedAxisPosition > 0 && fWidth < alAxisSpacing.get(iMovedAxisPosition - 1))
		{
			// switch axis to the left
			axisVA.moveLeft(iMovedAxisPosition);
			alAxisSpacing.remove(iMovedAxisPosition);
			alAxisSpacing.add(iMovedAxisPosition - 1, fWidth);

			IVirtualArrayDelta vaDelta = new VirtualArrayDelta(EIDType.EXPERIMENT_INDEX);
			vaDelta.add(VADeltaItem.moveLeft(iMovedAxisPosition));
			generalManager.getEventPublisher().triggerEvent(EMediatorType.SELECTION_MEDIATOR,
					this, new DeltaEventContainer<IVirtualArrayDelta>(vaDelta));
			iMovedAxisPosition--;
		}
		else if (iMovedAxisPosition < axisVA.size() - 1
				&& fWidth > alAxisSpacing.get(iMovedAxisPosition + 1))
		{
			// switch axis to the right
			axisVA.moveRight(iMovedAxisPosition);
			alAxisSpacing.remove(iMovedAxisPosition);
			alAxisSpacing.add(iMovedAxisPosition + 1, fWidth);

			IVirtualArrayDelta vaDelta = new VirtualArrayDelta(EIDType.EXPERIMENT_INDEX);
			vaDelta.add(VADeltaItem.moveRight(iMovedAxisPosition));
			generalManager.getEventPublisher().triggerEvent(EMediatorType.SELECTION_MEDIATOR,
					this, new DeltaEventContainer<IVirtualArrayDelta>(vaDelta));
			iMovedAxisPosition++;

		}
		else
		{
			alAxisSpacing.set(iMovedAxisPosition, fWidth);
		}
		setDisplayListDirty();

	}

	private void focusOnArea()
	{
		if (!generalManager.isWiiModeActive())
			return;

		WiiRemote wii = generalManager.getWiiRemote();

		float fXWiiPosition = wii.getCurrentSmoothHeadPosition()[0] + 1f;

		// we assume that this is far right, and -fMax is far left
		float fMaxX = 2;

		if (fXWiiPosition > fMaxX)
			fXWiiPosition = fMaxX;
		else if (fXWiiPosition < -fMaxX)
			fXWiiPosition = -fMaxX;

		// now we normalize to 0 to 1
		fXWiiPosition = (fXWiiPosition + fMaxX) / (2 * fMaxX);

		fXWiiPosition *= renderStyle.getWidthOfCoordinateSystem();
		int iAxisNumber = 0;
		for (int iCount = 0; iCount < alAxisSpacing.size() - 1; iCount++)
		{
			if (alAxisSpacing.get(iCount) < fXWiiPosition
					&& alAxisSpacing.get(iCount + 1) > fXWiiPosition)
			{
				if ((fXWiiPosition - alAxisSpacing.get(iCount)) < (alAxisSpacing.get(iCount) - fXWiiPosition))
					iAxisNumber = iCount;
				else
					iAxisNumber = iCount + 1;

				break;
			}
		}

		int iNumberOfAxis = set.getVA(iAxisVAID).size();

		float fOriginalAxisSpacing = renderStyle.getAxisSpacing(iNumberOfAxis);

		float fFocusAxisSpacing = 2 * fOriginalAxisSpacing;

		float fReducedSpacing = (renderStyle.getWidthOfCoordinateSystem() - 2 * fFocusAxisSpacing)
				/ (iNumberOfAxis - 3);

		float fCurrentX = 0;
		alAxisSpacing.clear();
		for (int iCount = 0; iCount < iNumberOfAxis; iCount++)
		{
			alAxisSpacing.add(fCurrentX);
			if (iCount + 1 == iAxisNumber || iCount == iAxisNumber)
				fCurrentX += fFocusAxisSpacing;
			else
				fCurrentX += fReducedSpacing;
		}

		setDisplayListDirty();
	}

	public void resetAxisSpacing()
	{
		alAxisSpacing.clear();
		int iNumAxis = set.sizeVA(iAxisVAID);
		float fInitAxisSpacing = renderStyle.getAxisSpacing(iNumAxis);
		for (int iCount = 0; iCount < iNumAxis; iCount++)
		{
			alAxisSpacing.add(fInitAxisSpacing * iCount);
		}
		setDisplayListDirty();
	}

}
