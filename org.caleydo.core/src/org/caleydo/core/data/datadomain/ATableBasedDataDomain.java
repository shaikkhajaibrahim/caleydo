package org.caleydo.core.data.datadomain;

import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.data.collection.EColumnType;
import org.caleydo.core.data.collection.table.DataTable;
import org.caleydo.core.data.id.IDCategory;
import org.caleydo.core.data.id.IDType;
import org.caleydo.core.data.mapping.IDMappingManager;
import org.caleydo.core.data.mapping.IDMappingManagerRegistry;
import org.caleydo.core.data.perspective.DimensionPerspective;
import org.caleydo.core.data.perspective.PerspectiveInitializationData;
import org.caleydo.core.data.perspective.RecordPerspective;
import org.caleydo.core.data.selection.DimensionSelectionManager;
import org.caleydo.core.data.selection.RecordSelectionManager;
import org.caleydo.core.data.selection.SelectionCommand;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.delta.DeltaConverter;
import org.caleydo.core.data.selection.delta.SelectionDelta;
import org.caleydo.core.data.virtualarray.DimensionVirtualArray;
import org.caleydo.core.data.virtualarray.RecordVirtualArray;
import org.caleydo.core.data.virtualarray.delta.DimensionVADelta;
import org.caleydo.core.data.virtualarray.delta.RecordVADelta;
import org.caleydo.core.data.virtualarray.events.DimensionVADeltaEvent;
import org.caleydo.core.data.virtualarray.events.DimensionVADeltaListener;
import org.caleydo.core.data.virtualarray.events.DimensionVAUpdateEvent;
import org.caleydo.core.data.virtualarray.events.IDimensionChangeHandler;
import org.caleydo.core.data.virtualarray.events.IRecordVADeltaHandler;
import org.caleydo.core.data.virtualarray.events.RecordVADeltaEvent;
import org.caleydo.core.data.virtualarray.events.RecordVADeltaListener;
import org.caleydo.core.data.virtualarray.events.RecordVAUpdateEvent;
import org.caleydo.core.data.virtualarray.events.ReplaceDimensionPerspectiveEvent;
import org.caleydo.core.data.virtualarray.events.ReplaceDimensionPerspectiveListener;
import org.caleydo.core.data.virtualarray.events.ReplaceRecordPerspectiveEvent;
import org.caleydo.core.data.virtualarray.events.ReplaceRecordPerspectiveListener;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.manager.event.EventPublisher;
import org.caleydo.core.manager.event.data.StartClusteringEvent;
import org.caleydo.core.manager.event.view.SelectionCommandEvent;
import org.caleydo.core.manager.event.view.tablebased.SelectionUpdateEvent;
import org.caleydo.core.util.clusterer.ClusterManager;
import org.caleydo.core.util.clusterer.ClusterResult;
import org.caleydo.core.util.clusterer.initialization.ClusterConfiguration;
import org.caleydo.core.util.clusterer.initialization.ClustererType;
import org.caleydo.core.view.opengl.canvas.listener.ForeignSelectionCommandListener;
import org.caleydo.core.view.opengl.canvas.listener.ForeignSelectionUpdateListener;
import org.caleydo.core.view.opengl.canvas.listener.ISelectionCommandHandler;
import org.caleydo.core.view.opengl.canvas.listener.ISelectionUpdateHandler;
import org.caleydo.core.view.opengl.canvas.listener.SelectionCommandListener;
import org.caleydo.core.view.opengl.canvas.listener.SelectionUpdateListener;

@XmlType
@XmlRootElement
public abstract class ATableBasedDataDomain
	extends ADataDomain
	implements IRecordVADeltaHandler, IDimensionChangeHandler, ISelectionUpdateHandler,
	ISelectionCommandHandler {

	protected boolean isColumnDimension = true;

	/** The set which is currently loaded and used inside the views for this use case. */
	protected DataTable table;

	protected String recordDenominationSingular = "<not specified>";
	protected String recordDenominationPlural = "<not specified>";

	protected String dimensionDenominationSingular = "<not specified>";
	protected String dimensionDenominationPlural = "<not specified>";

	/**
	 * The id type that should be used if an entity of this data domain should be printed human readable for
	 * records
	 */
	protected IDType humanReadableRecordIDType;
	/** Same as {@link #humanReadableRecordIDType} for dimensions */
	protected IDType humanReadableDimensionIDType;

	/**
	 * The primary mapping type of the record. This type is not determined at run-time but something permanent
	 * like an official gene mapping type like DAVID.
	 */
	protected IDType primaryRecordMappingType;
	/** Same as {@link #primaryDimensionMappingType} for dimensions. */
	protected IDType primaryDimensionMappingType;

	protected IDCategory recordIDCategory;
	protected IDCategory dimensionIDCategory;

	protected IDType recordIDType;
	protected IDType dimensionIDType;

	/** IDType used for {@link Group}s in this dataDomain */
	protected IDType recordGroupIDType;

	protected RecordSelectionManager recordSelectionManager;
	protected DimensionSelectionManager dimensionSelectionManager;
	protected SelectionManager recordGroupSelectionManager;

	/** central {@link EventPublisher} to receive and send events */
	protected EventPublisher eventPublisher = GeneralManager.get().getEventPublisher();

	/**
	 * All recordPerspectiveIDs registered with the DataTable. This variable is syncronous with the keys of
	 * the hashMap of the DatTable.
	 */
	@XmlElement
	private Set<String> recordPerspectiveIDs;

	/** Same as {@link #recordPerspectiveIDs} for dimensions */
	@XmlElement
	private Set<String> dimensionPerspectiveIDs;

	protected IDMappingManager recordIDMappingManager;
	protected IDMappingManager dimensionIDMappingManager;

	private SelectionUpdateListener selectionUpdateListener;
	private SelectionCommandListener selectionCommandListener;
	private StartClusteringListener startClusteringListener;

	private ReplaceDimensionPerspectiveListener replaceDimensionPerspectiveListener;
	private DimensionVADeltaListener dimensionVADeltaListener;
	private ReplaceRecordPerspectiveListener replaceRecordPerspectiveListener;
	private RecordVADeltaListener recordVADeltaListener;

	private AggregateGroupListener aggregateGroupListener;

	/**
	 * Constructor that should be used only for serialization
	 */
	public ATableBasedDataDomain() {
		super();
	}

	public ATableBasedDataDomain(String dataDomainType, String dataDomainID) {
		super(dataDomainType, dataDomainID);
	}

	/**
	 * @return the isColumnDimension, see {@link #isColumnDimension}
	 */
	public boolean isColumnDimension() {
		return isColumnDimension;
	}

	/**
	 * @param isColumnDimension
	 *            setter, see {@link #isColumnDimension}
	 */
	public void setColumnDimension(boolean isColumnDimension) {
		this.isColumnDimension = isColumnDimension;
	}

	@Override
	public void init() {
		super.init();
		assignIDCategories();
		if (recordIDCategory == null || dimensionIDCategory == null) {
			throw new IllegalStateException("A ID category in " + toString()
				+ " was null, recordIDCategory: " + recordIDCategory + ", dimensionIDCategory: "
				+ dimensionIDCategory);
		}
		recordIDMappingManager = IDMappingManagerRegistry.get().getIDMappingManager(recordIDCategory);
		dimensionIDMappingManager = IDMappingManagerRegistry.get().getIDMappingManager(dimensionIDCategory);

		recordIDType =
			IDType.registerType("record_" + dataDomainID + "_" + hashCode(), recordIDCategory,
				EColumnType.INT);
		recordIDType.setInternalType(true);
		dimensionIDType =
			IDType.registerType("dimension_" + dataDomainID + "_" + hashCode(), dimensionIDCategory,
				EColumnType.INT);
		dimensionIDType.setInternalType(true);

		recordGroupIDType =
			IDType.registerType("group_record_" + dataDomainID + "_" + hashCode(), recordIDCategory,
				EColumnType.INT);
		recordGroupIDType.setInternalType(true);

		recordSelectionManager = new RecordSelectionManager(recordIDMappingManager, recordIDType);
		dimensionSelectionManager = new DimensionSelectionManager(dimensionIDMappingManager, dimensionIDType);
		recordGroupSelectionManager = new SelectionManager(recordGroupIDType);

	}

	/**
	 * Assign {@link #recordIDCategory} and {@link #dimensionIDCategory} in the concrete implementing classes.
	 * ID Categories should typically be already existing through the data mapping. Assign the correct types
	 * using {@link IDCategory#getIDCategory(String)}.
	 */
	protected abstract void assignIDCategories();

	/**
	 * Sets the set which is currently loaded and used inside the views for this use case.
	 * 
	 * @param table
	 *            The new set which replaced the currently loaded one.
	 */
	public void setTable(DataTable table) {
		assert (table != null);

		// TODO - do we still need this?
		DataTable oldTable = this.table;
		this.table = table;

		if (oldTable != null) {
			oldTable.destroy();
			oldTable = null;
		}

		recordPerspectiveIDs = table.getRecordPerspectiveIDs();
		dimensionPerspectiveIDs = table.getDimensionPerspectiveIDs();
	}

	/**
	 * Returns the root set which is currently loaded and used inside the views for this use case.
	 * 
	 * @return a data set
	 */
	@XmlTransient
	public DataTable getTable() {
		return table;
	}

	/**
	 * @return the recordIDMappingManager, see {@link #recordIDMappingManager}
	 */
	public IDMappingManager getRecordIDMappingManager() {
		return recordIDMappingManager;
	}

	/**
	 * @return the dimensionIDMappingManager, see {@link #dimensionIDMappingManager}
	 */
	public IDMappingManager getDimensionIDMappingManager() {
		return dimensionIDMappingManager;
	}

	/**
	 * @return the recordIDType, see {@link #recordIDType}
	 */
	public IDType getRecordIDType() {
		return recordIDType;
	}

	/**
	 * @return the dimensionIDType, see {@link #dimensionIDType}
	 */
	public IDType getDimensionIDType() {
		return dimensionIDType;
	}

	/**
	 * Returns
	 * 
	 * @return
	 */
	public IDType getPrimaryRecordMappingType() {
		return primaryRecordMappingType;
	}

	public IDType getPrimaryDimensionMappingType() {
		return dimensionIDType;
	}

	/**
	 * @return the recordIDCategory, see {@link #recordIDCategory}
	 */
	public IDCategory getRecordIDCategory() {
		return recordIDCategory;
	}

	/**
	 * @return the dimensionIDCategory, see {@link #dimensionIDCategory}
	 */
	public IDCategory getDimensionIDCategory() {
		return dimensionIDCategory;
	}

	/**
	 * @return the recordGroupIDType, see {@link #recordGroupIDType}
	 */
	public IDType getRecordGroupIDType() {
		return recordGroupIDType;
	}

	/**
	 * @return the humanReadableRecordIDType, see {@link #humanReadableRecordIDType}
	 */
	public IDType getHumanReadableRecordIDType() {
		return humanReadableRecordIDType;
	}

	/**
	 * @return the humanReadableDimensionIDType, see {@link #humanReadableDimensionIDType}
	 */
	public IDType getHumanReadableDimensionIDType() {
		return humanReadableDimensionIDType;
	}

	/**
	 * Returns a clone of the record selection manager. This is the preferred way to initialize
	 * SelectionManagers.
	 * 
	 * @return a clone of the record selection manager
	 */
	public RecordSelectionManager getRecordSelectionManager() {
		return (RecordSelectionManager) recordSelectionManager.clone();
	}

	/**
	 * Returns a clone of the dimension selection manager. This is the preferred way to initialize
	 * SelectionManagers.
	 * 
	 * @return a clone of the dimension selection manager
	 */
	public DimensionSelectionManager getDimensionSelectionManager() {
		return (DimensionSelectionManager) dimensionSelectionManager.clone();
	}

	/**
	 * Returns a clone of the record group selection manager. This is the preferred way to initialize
	 * SelectionManagers. *
	 * 
	 * @return a clone of the dimension selection manager
	 */
	public SelectionManager getRecordGroupSelectionManager() {
		return recordGroupSelectionManager.clone();
	}

	/**
	 * Returns the virtual array for the type
	 * 
	 * @param recordPerspectiveID
	 *            the type of VA requested
	 * @return
	 */
	public RecordVirtualArray getRecordVA(String recordPerspectiveID) {
		RecordVirtualArray va = table.getRecordPerspective(recordPerspectiveID).getVirtualArray();
		return va;
	}

	/**
	 * Returns the virtual array for the type
	 * 
	 * @param dimensionPerspectiveID
	 *            the type of VA requested
	 * @return
	 */
	public DimensionVirtualArray getDimensionVA(String dimensionPerspectiveID) {
		DimensionVirtualArray va = table.getDimensionPerspective(dimensionPerspectiveID).getVirtualArray();
		return va;
	}

	/**
	 * @return the recordPerspectiveIDs, see {@link #recordPerspectiveIDs}
	 */
	public Set<String> getRecordPerspectiveIDs() {
		return recordPerspectiveIDs;
	}

	/**
	 * @return the dimensionPerspectiveIDs, see {@link #dimensionPerspectiveIDs}
	 */
	public Set<String> getDimensionPerspectiveIDs() {
		return dimensionPerspectiveIDs;
	}

	/**
	 * Initiates clustering based on the parameters passed. Sends out an event to all affected views upon
	 * positive completion to replace their VA.
	 * 
	 * @param tableID
	 *            ID of the set to cluster
	 * @param clusterState
	 */
	public void startClustering(ClusterConfiguration clusterState) {
		// FIXME this should be re-designed so that the clustering is a separate thread and communicates via
		// events
		ClusterManager clusterManager = new ClusterManager(this);
		ClusterResult result = clusterManager.cluster(clusterState);

		// check if clustering failed. If so, we just ignore it.
		if (result == null)
			return;

		if (clusterState.getClustererType() == ClustererType.DIMENSION_CLUSTERING
			|| clusterState.getClustererType() == ClustererType.BI_CLUSTERING) {
			PerspectiveInitializationData dimensionResult = result.getDimensionResult();
			DimensionPerspective dimensionPerspective = clusterState.getTargetDimensionPerspective();
			dimensionPerspective.init(dimensionResult);

			eventPublisher.triggerEvent(new DimensionVAUpdateEvent(dataDomainID, dimensionPerspective
				.getPerspectiveID(), this));
		}

		if (clusterState.getClustererType() == ClustererType.RECORD_CLUSTERING
			|| clusterState.getClustererType() == ClustererType.BI_CLUSTERING) {
			PerspectiveInitializationData recordResult = result.getRecordResult();
			RecordPerspective recordPerspective = clusterState.getTargetRecordPerspective();
			recordPerspective.init(recordResult);

			eventPublisher.triggerEvent(new RecordVAUpdateEvent(dataDomainID, recordPerspective
				.getPerspectiveID(), this));
		}
	}

	/**
	 * Resets the context VA to it's initial state
	 */
	@Deprecated
	public void resetRecordVA(String recordPerspectiveID) {
		table.getRecordPerspective(recordPerspectiveID).reset();
	}

	@Override
	public void handleRecordVADelta(RecordVADelta vaDelta, String info) {
		IDCategory targetCategory = vaDelta.getIDType().getIDCategory();
		if (targetCategory != recordIDCategory)
			return;

		if (targetCategory == recordIDCategory && vaDelta.getIDType() != recordIDType)
			vaDelta = DeltaConverter.convertDelta(recordIDMappingManager, recordIDType, vaDelta);
		RecordPerspective recordData = table.getRecordPerspective(vaDelta.getVAType());
		recordData.setVADelta(vaDelta);

		RecordVAUpdateEvent event =
			new RecordVAUpdateEvent(dataDomainID, recordData.getPerspectiveID(), this);

		eventPublisher.triggerEvent(event);

	}

	@Override
	public void handleDimensionVADelta(DimensionVADelta vaDelta, String info) {
		// FIXME why is here nothing?
		System.out.println("What?");

	}

	@Override
	public void replaceRecordPerspective(String dataDomainID, String recordPerspectiveID,
		PerspectiveInitializationData data) {

		if (dataDomainID != this.dataDomainID) {
			handleForeignRecordVAUpdate(dataDomainID, recordPerspectiveID, data);
			return;
		}

		table.getRecordPerspective(recordPerspectiveID).init(data);

		RecordVAUpdateEvent event = new RecordVAUpdateEvent();
		event.setSender(this);
		event.setDataDomainID(dataDomainID);
		event.setPerspectiveID(recordPerspectiveID);
		eventPublisher.triggerEvent(event);
	}

	@Override
	public void replaceDimensionPerspective(String dataDomainID, String dimensionPerspectiveID,
		PerspectiveInitializationData data) {

		table.getDimensionPerspective(dimensionPerspectiveID).init(data);

		DimensionVAUpdateEvent event = new DimensionVAUpdateEvent();
		event.setDataDomainID(dataDomainID);
		event.setSender(this);
		event.setPerspectiveID(dimensionPerspectiveID);
		eventPublisher.triggerEvent(event);
	}

	@Override
	public void handleSelectionUpdate(SelectionDelta selectionDelta, boolean scrollToSelection, String info) {

		if (recordSelectionManager == null)
			return;

		if (recordIDMappingManager.hasMapping(selectionDelta.getIDType(), recordSelectionManager.getIDType())) {
			recordSelectionManager.setDelta(selectionDelta);
		}
		else if (dimensionIDMappingManager.hasMapping(selectionDelta.getIDType(),
			dimensionSelectionManager.getIDType())) {
			dimensionSelectionManager.setDelta(selectionDelta);
		}

		if (selectionDelta.getIDType() == recordGroupSelectionManager.getIDType()) {
			recordGroupSelectionManager.setDelta(selectionDelta);
		}
	}

	@Override
	public void handleSelectionCommand(IDCategory idCategory, SelectionCommand selectionCommand) {
		// TODO Auto-generated method stub
	}

	/**
	 * This method is called by the {@link ForeignSelectionUpdateListener}, signaling that a selection form
	 * another dataDomain is available. If possible, it is converted to be compatible with the local
	 * dataDomain and then sent out via a {@link SelectionUpdateEvent}.
	 * 
	 * @param dataDomainType
	 *            the type of the dataDomain for which this selectionUpdate is intended
	 * @param delta
	 * @param scrollToSelection
	 * @param info
	 */
	public void handleForeignSelectionUpdate(String dataDomainType, SelectionDelta delta,
		boolean scrollToSelection, String info) {
		// may be interesting to implement in sub-class
	}

	/**
	 * Interface used by {@link ForeignSelectionCommandListener} to signal foreign selection commands. Can be
	 * implemented in concrete classes, has no functionality in base class.
	 */
	public void handleForeignSelectionCommand(String dataDomainType, IDCategory idCategory,
		SelectionCommand selectionCommand) {
		// may be interesting to implement in sub-class
	}

	/**
	 * This method is called if a record VA Update was requested, but the dataDomainType specified was not
	 * this dataDomains type. Concrete handling can only be done in concrete dataDomains.
	 * 
	 * @param tableID
	 * @param dataDomainType
	 * @param vaType
	 * @param data
	 */
	public void handleForeignRecordVAUpdate(String dataDomainType, String vaType,
		PerspectiveInitializationData data) {
		// may be interesting to implement in sub-class
	}

	/**
	 * Returns the denomination for the records. For genetic data for example this would be "Gene"
	 * 
	 * @param capitalized
	 *            if true, the label is returned capitalized, e.g., "Gene", if false it would be "gene"
	 * @param plural
	 *            if true, the label is returned in the plural form of the word, e.g., "genes" instead of the
	 *            singular form, e.g., "gene"
	 * @return the denomination formatted according to the parameters passed
	 */
	public String getRecordDenomination(boolean capitalized, boolean plural) {
		String recordDenomination;
		if (plural)
			recordDenomination = recordDenominationPlural;
		else
			recordDenomination = recordDenominationSingular;

		if (capitalized) {
			// Make first char capitalized
			recordDenomination =
				recordDenomination.substring(0, 1).toUpperCase()
					+ recordDenomination.substring(1, recordDenomination.length());
		}
		return recordDenomination;
	}

	/** Same as {@link #getRecordDenomination(boolean, boolean)} for dimensions. */
	public String getDimensionDenomination(boolean capitalized, boolean plural) {
		String dimensionDenomination;

		if (plural)
			dimensionDenomination = dimensionDenominationPlural;
		else
			dimensionDenomination = dimensionDenominationSingular;

		if (capitalized) {
			// Make first char capitalized
			dimensionDenomination =
				dimensionDenomination.substring(0, 1).toUpperCase()
					+ dimensionDenomination.substring(1, dimensionDenomination.length());
		}
		return dimensionDenomination;
	}

	/**
	 * Get the human readable record label for the id, which is of the {@link #recordIDType}.
	 * 
	 * @param id
	 *            the id to convert to a human readable label
	 * @return the readable label
	 */
	public String getRecordLabel(Object id) {
		return getRecordLabel(recordIDType, id);
	}

	/**
	 * Get the human readable dimension label for the id, which is of the {@link #dimensionIDType}.
	 * 
	 * @param id
	 *            the id to convert to a human readable label
	 * @return the readable label
	 */
	public String getDimensionLabel(Object id) {
		return getDimensionLabel(dimensionIDType, id);
	}

	/**
	 * Get the human readable record label for the id, which is of the type specified.
	 * 
	 * @param idType
	 *            the IDType of the id passed
	 * @param id
	 * @return the readable label
	 */
	public String getRecordLabel(IDType idType, Object id) {
		Set<String> ids = recordIDMappingManager.getIDAsSet(idType, humanReadableRecordIDType, id);
		String label = "No Mapping";
		if (ids != null && ids.size() > 0) {
			label = ids.iterator().next();
		}
		return label;
	}

	/** Same as {@link #getRecordLabel(IDType, Object)} for dimensions */
	public String getDimensionLabel(IDType idType, Object id) {
		Set<String> ids = dimensionIDMappingManager.getIDAsSet(idType, humanReadableDimensionIDType, id);
		String label = "No Mapping";
		if (ids != null && ids.size() > 0) {
			label = ids.iterator().next();
		}
		return label;
	}

	public void aggregateGroups(java.util.Set<Integer> groups) {
		System.out.println("Received command to aggregate experiments, not implemented yet");
	}

	// FIXME CONTEXT MENU
	// /**
	// * A dataDomain may contribute to the context menu. This function returns the recordItemContainer of the
	// * context menu if one was specified. This should be overridden by subclasses if needed.
	// *
	// * @return a context menu item container related to record items
	// */
	// public AItemContainer getRecordItemContainer(IDType idType, int id) {
	// return null;
	// }

	// FIXME CONTEXT MENU
	// /**
	// * A dataDomain may contribute to the context menu. This function returns dataDomain specific
	// * implementations of a context menu for content groups. * @param idType
	// *
	// * @param ids
	// * @return
	// */
	// public AItemContainer getRecordGroupItemContainer(IDType idType, ArrayList<Integer> ids) {
	// return null;
	// }

	@Override
	public int getDataAmount() {
		if (table == null)
			return 0;
		return table.getMetaData().size() * table.getMetaData().depth();
	}

	@Override
	public void registerEventListeners() {

		selectionUpdateListener = new SelectionUpdateListener();
		selectionUpdateListener.setHandler(this);
		selectionUpdateListener.setExclusiveDataDomainID(dataDomainID);
		eventPublisher.addListener(SelectionUpdateEvent.class, selectionUpdateListener);

		selectionCommandListener = new SelectionCommandListener();
		selectionCommandListener.setHandler(this);
		selectionCommandListener.setDataDomainID(dataDomainID);
		eventPublisher.addListener(SelectionCommandEvent.class, selectionCommandListener);

		startClusteringListener = new StartClusteringListener();
		startClusteringListener.setHandler(this);
		startClusteringListener.setDataDomainID(dataDomainID);
		eventPublisher.addListener(StartClusteringEvent.class, startClusteringListener);

		recordVADeltaListener = new RecordVADeltaListener();
		recordVADeltaListener.setHandler(this);
		recordVADeltaListener.setDataDomainID(dataDomainID);
		eventPublisher.addListener(RecordVADeltaEvent.class, recordVADeltaListener);

		replaceRecordPerspectiveListener = new ReplaceRecordPerspectiveListener();
		replaceRecordPerspectiveListener.setHandler(this);
		replaceRecordPerspectiveListener.setDataDomainID(dataDomainID);
		eventPublisher.addListener(ReplaceRecordPerspectiveEvent.class, replaceRecordPerspectiveListener);

		dimensionVADeltaListener = new DimensionVADeltaListener();
		dimensionVADeltaListener.setHandler(this);
		dimensionVADeltaListener.setDataDomainID(dataDomainID);
		eventPublisher.addListener(DimensionVADeltaEvent.class, dimensionVADeltaListener);

		replaceDimensionPerspectiveListener = new ReplaceDimensionPerspectiveListener();
		replaceDimensionPerspectiveListener.setHandler(this);
		replaceDimensionPerspectiveListener.setDataDomainID(dataDomainID);
		eventPublisher.addListener(ReplaceDimensionPerspectiveEvent.class,
			replaceDimensionPerspectiveListener);

		aggregateGroupListener = new AggregateGroupListener();
		aggregateGroupListener.setHandler(this);
		eventPublisher.addListener(AggregateGroupEvent.class, aggregateGroupListener);
	}

	// TODO this is never called!
	@Override
	public void unregisterEventListeners() {

		if (selectionUpdateListener != null) {
			eventPublisher.removeListener(selectionUpdateListener);
			selectionUpdateListener = null;
		}

		if (selectionCommandListener != null) {
			eventPublisher.removeListener(selectionCommandListener);
			selectionCommandListener = null;
		}

		if (startClusteringListener != null) {
			eventPublisher.removeListener(startClusteringListener);
			startClusteringListener = null;
		}

		if (replaceRecordPerspectiveListener != null) {
			eventPublisher.removeListener(replaceRecordPerspectiveListener);
			replaceRecordPerspectiveListener = null;
		}

		if (replaceDimensionPerspectiveListener != null) {
			eventPublisher.removeListener(replaceDimensionPerspectiveListener);
			replaceDimensionPerspectiveListener = null;
		}

		if (recordVADeltaListener != null) {
			eventPublisher.removeListener(recordVADeltaListener);
			recordVADeltaListener = null;
		}

		if (dimensionVADeltaListener != null) {
			eventPublisher.removeListener(dimensionVADeltaListener);
			dimensionVADeltaListener = null;
		}

		if (aggregateGroupListener != null) {
			eventPublisher.removeListener(aggregateGroupListener);
			aggregateGroupListener = null;
		}
	}

}
