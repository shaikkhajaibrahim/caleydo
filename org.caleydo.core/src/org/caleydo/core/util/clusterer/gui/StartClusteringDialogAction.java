package org.caleydo.core.util.clusterer.gui;

import org.caleydo.core.data.collection.table.DataTable;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.manager.datadomain.ATableBasedDataDomain;
import org.caleydo.core.manager.event.view.browser.ChangeURLEvent;
import org.caleydo.core.util.clusterer.ClusterState;
import org.caleydo.core.util.clusterer.EClustererAlgo;
import org.caleydo.core.util.clusterer.EClustererType;
import org.caleydo.core.util.clusterer.EDistanceMeasure;
import org.caleydo.core.util.clusterer.ETreeClustererAlgo;
import org.caleydo.data.loader.ResourceLoader;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Action responsible for starting clustering
 * 
 * @author Bernhard Schlegl
 */
public class StartClusteringDialogAction
	extends Action
	implements ActionFactory.IWorkbenchAction {

	public final static String ID = "org.caleydo.core.util.clusterer.gui.StartClusteringAction";
	public static final String TEXT = "Clustering";
	public static final String ICON = "resources/icons/view/storagebased/clustering.png";

	private Composite parentComposite;

	private String clusterType;
	private String distmeasure;
	private String treeClusterAlgo;
	private int iClusterCntGenes = 5;
	private int iClusterCntExperiments = 5;
	private float fclusterFactorGenes = 1f;
	private float fclusterFactorExperiments = 1f;

	private String[] sArTypeOptions = { "DETERMINED_DEPENDING_ON_USE_CASE", "Experiment", "Both dimensions" };
	private String[] sArDistOptions = { "Euclidean distance", "Manhattan distance", "Chebyshev distance",
			"Pearson correlation" };
	private String[] sArDistOptionsWeka = { "Euclidean distance", "Manhattan distance" };// ,"Chebyshev distance"};
	private String[] sArTreeClusterer = { "Complete Linkage", "Average Linkage", "Single Linkage" };

	private ClusterState clusterState = new ClusterState();

	private TabItem treeClusteringTab;
	private TabItem affinityPropagationTab;
	private TabItem kMeansTab;
	private TabItem cobwebTab;
	private OtherClusterersTab othersTab;

	private Text clusterFactorGenes = null;
	private Text clusterFactorExperiments = null;

	/**
	 * Constructor.
	 */
	public StartClusteringDialogAction(final Composite parentComposite, ATableBasedDataDomain dataDomain) {
		super(TEXT);
		setId(ID);
		setToolTipText(TEXT);
		setImageDescriptor(ImageDescriptor.createFromImage(new ResourceLoader().getImage(PlatformUI
			.getWorkbench().getDisplay(), ICON)));

		this.parentComposite = parentComposite;

		sArTypeOptions[0] = dataDomain.getContentName(true, false);
	}

	@Override
	public void run() {

		createGUI();

	}

	private void createGUI() {

		Composite composite = new Composite(parentComposite, SWT.OK);
		// composite.setLayout(new FillLayout(SWT.VERTICAL));

		final TabFolder tabFolder = new TabFolder(composite, SWT.BORDER);

		composite.addHelpListener(new HelpListener() {

			@Override
			public void helpRequested(HelpEvent e) {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView("org.caleydo.view.browser");

					final String URL_HELP_CLUSTERING =
						"http://www.caleydo.org/help/gene_expression.html#Clustering";
					ChangeURLEvent changeURLEvent = new ChangeURLEvent();
					changeURLEvent.setSender(this);
					changeURLEvent.setUrl(URL_HELP_CLUSTERING);
					GeneralManager.get().getEventPublisher().triggerEvent(changeURLEvent);
				}
				catch (PartInitException partInitException) {
				}
			}
		});

		createTreeClusteringTab(tabFolder);
		createAffinityPropagationTab(tabFolder);
		createKMeansTab(tabFolder);
		createCobwebTab(tabFolder);
		othersTab = new OtherClusterersTab(tabFolder);

		Button helpButton = new Button(composite, SWT.PUSH);
		helpButton.setText("Help");
		helpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView("org.caleydo.view.browser");

					String stHelp = "http://www.caleydo.org/help/gene_expression.html#Cobweb";

					ChangeURLEvent changeURLEvent = new ChangeURLEvent();
					changeURLEvent.setSender(this);
					changeURLEvent.setUrl(stHelp);
					GeneralManager.get().getEventPublisher().triggerEvent(changeURLEvent);
				}
				catch (PartInitException e1) {
					e1.printStackTrace();
				}
			}
		});

		// set default value for cluster algo
		clusterState.setClustererAlgo(EClustererAlgo.TREE_CLUSTERER);

		tabFolder.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if (((TabItem) e.item) == treeClusteringTab) {
					clusterState.setClustererAlgo(EClustererAlgo.TREE_CLUSTERER);
				}
				else if (((TabItem) e.item) == affinityPropagationTab) {
					clusterState.setClustererAlgo(EClustererAlgo.AFFINITY_PROPAGATION);
				}
				else if (((TabItem) e.item) == kMeansTab) {
					clusterState.setClustererAlgo(EClustererAlgo.KMEANS_CLUSTERER);
				}
				else if (((TabItem) e.item) == cobwebTab) {
					clusterState.setClustererAlgo(EClustererAlgo.COBWEB_CLUSTERER);
				}
				else if (((TabItem) e.item) == othersTab.getTab()) {
					clusterState.setClustererAlgo(EClustererAlgo.OTHER);
				}
				else
					throw new IllegalStateException("Not implemented!");
			}
		});

		tabFolder.pack();
		composite.pack();
	}

	private void createCobwebTab(TabFolder tabFolder) {
		cobwebTab = new TabItem(tabFolder, SWT.NONE);
		cobwebTab.setText("Cobweb");

		Composite composite = new Composite(tabFolder, SWT.NONE);
		cobwebTab.setControl(composite);
		composite.setLayout(new GridLayout(1, false));

		Group clusterDimensionGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		clusterDimensionGroup.setText("Cluster:");
		clusterDimensionGroup.setLayout(new GridLayout(1, false));

		final Combo clusterTypeCombo = new Combo(clusterDimensionGroup, SWT.DROP_DOWN);
		clusterTypeCombo.setItems(sArTypeOptions);
		clusterTypeCombo.setEnabled(true);
		clusterTypeCombo.select(0);
		clusterType = sArTypeOptions[0];
		clusterTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clusterType = clusterTypeCombo.getText();
			}
		});
	}

	private void createKMeansTab(TabFolder tabFolder) {
		kMeansTab = new TabItem(tabFolder, SWT.NONE);
		kMeansTab.setText("KMeans");

		Composite composite = new Composite(tabFolder, SWT.NONE);
		kMeansTab.setControl(composite);
		composite.setLayout(new GridLayout(1, false));

		Group clusterDimensionGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		clusterDimensionGroup.setText("Cluster:");
		clusterDimensionGroup.setLayout(new GridLayout(1, false));

		final Combo clusterTypeCombo = new Combo(clusterDimensionGroup, SWT.DROP_DOWN);
		clusterTypeCombo.setItems(sArTypeOptions);
		clusterTypeCombo.select(0);
		clusterType = sArTypeOptions[0];

		ModifyListener listenerIntGenes = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				valueChangedInt((Text) e.widget, true);
			}
		};
		ModifyListener listenerIntExperiments = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				valueChangedInt((Text) e.widget, false);
			}
		};

		Group distanceMeasureGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		distanceMeasureGroup.setText("Distance measure:");
		distanceMeasureGroup.setLayout(new GridLayout(1, false));

		final Combo distMeasureCombo = new Combo(distanceMeasureGroup, SWT.DROP_DOWN);
		distMeasureCombo.setItems(sArDistOptionsWeka);
		distMeasureCombo.setEnabled(true);
		distMeasureCombo.select(0);
		distmeasure = sArDistOptionsWeka[0];
		distMeasureCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				distmeasure = distMeasureCombo.getText();
			}
		});

		final Label lblClusterCntGenes = new Label(composite, SWT.SHADOW_ETCHED_IN);
		lblClusterCntGenes.setText("Number clusters for clustering genes");
		lblClusterCntGenes.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		final Text clusterCntGenes = new Text(composite, SWT.SHADOW_ETCHED_IN);
		clusterCntGenes.addModifyListener(listenerIntGenes);
		clusterCntGenes.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		clusterCntGenes.setText("5");
		clusterCntGenes
			.setToolTipText("Positive integer value. Range: 1 up to the number of samples in data set");

		final Label lblClusterCntExperiments = new Label(composite, SWT.SHADOW_ETCHED_IN);
		lblClusterCntExperiments.setText("Number clusters for clustering experiments");
		lblClusterCntExperiments.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		final Text clusterCntExperiments = new Text(composite, SWT.SHADOW_ETCHED_IN);
		clusterCntExperiments.addModifyListener(listenerIntExperiments);
		clusterCntExperiments.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		clusterCntExperiments.setText("5");
		clusterCntExperiments
			.setToolTipText("Positive integer value. Range: 1 up to the number of samples in data set");
		clusterCntExperiments.setEnabled(false);

		clusterTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clusterType = clusterTypeCombo.getText();
				if (clusterType.equals(sArTypeOptions[0])) {
					clusterCntGenes.setEnabled(true);
					clusterCntExperiments.setEnabled(false);
				}
				else if (clusterType.equals(sArTypeOptions[1])) {
					clusterCntGenes.setEnabled(false);
					clusterCntExperiments.setEnabled(true);
				}
				else {
					clusterCntGenes.setEnabled(true);
					clusterCntExperiments.setEnabled(true);
				}
			}
		});

	}

	private void createAffinityPropagationTab(TabFolder tabFolder) {
		affinityPropagationTab = new TabItem(tabFolder, SWT.NONE);
		affinityPropagationTab.setText("Affinity Propagation");

		Composite composite = new Composite(tabFolder, SWT.NONE);
		affinityPropagationTab.setControl(composite);
		composite.setLayout(new GridLayout(1, false));

		Group clusterDimensionGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		clusterDimensionGroup.setText("Cluster:");
		clusterDimensionGroup.setLayout(new GridLayout(1, false));

		final Combo clusterTypeCombo = new Combo(clusterDimensionGroup, SWT.DROP_DOWN);
		clusterTypeCombo.setItems(sArTypeOptions);
		clusterTypeCombo.select(0);
		clusterType = sArTypeOptions[0];

		Group distanceMeasureGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		distanceMeasureGroup.setText("Distance measure:");
		distanceMeasureGroup.setLayout(new GridLayout(1, false));

		final Combo distMeasureCombo = new Combo(distanceMeasureGroup, SWT.DROP_DOWN);
		distMeasureCombo.setItems(sArDistOptions);
		distMeasureCombo.setEnabled(true);
		distMeasureCombo.select(0);
		distmeasure = sArDistOptions[0];
		distMeasureCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				distmeasure = distMeasureCombo.getText();
			}
		});

		ModifyListener listenerFloatGenes = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				valueChangedFloat((Text) e.widget, true);
			}
		};

		ModifyListener listenerFloatExperiments = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				valueChangedFloat((Text) e.widget, false);
			}
		};

		final Label lblClusterFactorGenes = new Label(composite, SWT.SHADOW_ETCHED_IN);
		lblClusterFactorGenes.setText("Factor for clustering genes");
		lblClusterFactorGenes.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		clusterFactorGenes = new Text(composite, SWT.SHADOW_ETCHED_IN);
		clusterFactorGenes.addModifyListener(listenerFloatGenes);
		clusterFactorGenes.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		clusterFactorGenes.setText("1.0");
		clusterFactorGenes
			.setToolTipText("Float value. Range: 1 up to 10. The bigger the value the less clusters will be formed");

		final Label lblClusterFactorExperiments = new Label(composite, SWT.SHADOW_ETCHED_IN);
		lblClusterFactorExperiments.setText("Factor for clustering experiments");
		lblClusterFactorExperiments.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		clusterFactorExperiments = new Text(composite, SWT.SHADOW_ETCHED_IN);
		clusterFactorExperiments.addModifyListener(listenerFloatExperiments);
		clusterFactorExperiments.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		clusterFactorExperiments.setText("1.0");
		clusterFactorExperiments
			.setToolTipText("Float value. Range: 1 up to 10. The bigger the value the less clusters will be formed");
		clusterFactorExperiments.setEnabled(false);

		clusterTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clusterType = clusterTypeCombo.getText();
				if (clusterType.equals(sArTypeOptions[0])) {
					clusterFactorGenes.setEnabled(true);
					clusterFactorExperiments.setEnabled(false);
				}
				else if (clusterType.equals(sArTypeOptions[1])) {
					clusterFactorGenes.setEnabled(false);
					clusterFactorExperiments.setEnabled(true);
				}
				else {
					clusterFactorGenes.setEnabled(true);
					clusterFactorExperiments.setEnabled(true);
				}

			}
		});

	}

	private void createTreeClusteringTab(TabFolder tabFolder) {
		treeClusteringTab = new TabItem(tabFolder, SWT.NONE);
		treeClusteringTab.setText("Tree Clusterer");

		Composite composite = new Composite(tabFolder, SWT.NONE);
		treeClusteringTab.setControl(composite);
		composite.setLayout(new GridLayout(1, false));

		Group clusterDimensionGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		clusterDimensionGroup.setText("Cluster:");
		clusterDimensionGroup.setLayout(new GridLayout(2, false));

		Composite clusterComposite = new Composite(clusterDimensionGroup, SWT.NONE);
		clusterComposite.setLayout(new RowLayout());

		final Combo clusterTypeCombo = new Combo(clusterComposite, SWT.DROP_DOWN);
		clusterTypeCombo.setItems(sArTypeOptions);
		clusterTypeCombo.select(0);
		clusterType = sArTypeOptions[0];
		clusterTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clusterType = clusterTypeCombo.getText();
			}
		});

		final Combo treeClustererCombo = new Combo(clusterComposite, SWT.DROP_DOWN);
		treeClustererCombo.setItems(sArTreeClusterer);
		treeClustererCombo.select(0);
		treeClusterAlgo = sArTreeClusterer[0];
		treeClustererCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				treeClusterAlgo = treeClustererCombo.getText();
			}
		});

		Group distanceMeasureGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		distanceMeasureGroup.setText("Distance measure:");
		distanceMeasureGroup.setLayout(new GridLayout(1, false));

		final Combo distMeasureCombo = new Combo(distanceMeasureGroup, SWT.DROP_DOWN);
		distMeasureCombo.setItems(sArDistOptions);
		distMeasureCombo.setEnabled(true);
		distMeasureCombo.select(0);
		distmeasure = sArDistOptions[0];
		distMeasureCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				distmeasure = distMeasureCombo.getText();
			}
		});

	}

	private void valueChangedInt(Text text, boolean bGeneFactor) {
		if (!text.isFocusControl())
			return;

		int temp = 0;

		try {
			temp = Integer.parseInt(text.getText());
			if (temp > 0) {
				if (bGeneFactor == true)
					iClusterCntGenes = temp;
				else
					iClusterCntExperiments = temp;
			}
			else {
				Shell shell = new Shell();
				MessageBox messageBox = new MessageBox(shell, SWT.OK);
				messageBox.setText("Start Clustering");
				messageBox.setMessage("Number of clusters must be positive");
				messageBox.open();
			}
		}
		catch (NumberFormatException e) {
			System.out.println("Invalid input");
		}

	}

	private void valueChangedFloat(Text text, boolean bGeneFactor) {
		if (!text.isFocusControl())
			return;

		float temp = 0;

		try {
			temp = Float.parseFloat(text.getText());
			if (temp >= 1f && temp <= 10) {
				if (bGeneFactor == true)
					fclusterFactorGenes = temp;
				else
					fclusterFactorExperiments = temp;
			}
			else {
				Shell shell = new Shell();
				MessageBox messageBox = new MessageBox(shell, SWT.OK);
				messageBox.setText("Start Clustering");
				messageBox.setMessage("Factor for affinity propagation has to be between 1.0 and 10.0");
				messageBox.open();
			}
		}
		catch (NumberFormatException e) {
			System.out.println("Invalid input");
		}

	}

	public void execute(boolean cancelPressed) {

		if (cancelPressed) {
			clusterState = null;
			return;
		}

		if (clusterType.equals(sArTypeOptions[0]))
			clusterState.setClustererType(EClustererType.CONTENT_CLUSTERING);
		else if (clusterType.equals(sArTypeOptions[1]))
			clusterState.setClustererType(EClustererType.STORAGE_CLUSTERING);
		else if (clusterType.equals(sArTypeOptions[2]))
			clusterState.setClustererType(EClustererType.BI_CLUSTERING);

		if (distmeasure.equals(sArDistOptions[0]))
			clusterState.setDistanceMeasure(EDistanceMeasure.EUCLIDEAN_DISTANCE);
		else if (distmeasure.equals(sArDistOptions[1]))
			clusterState.setDistanceMeasure(EDistanceMeasure.MANHATTAHN_DISTANCE);
		else if (distmeasure.equals(sArDistOptions[2]))
			clusterState.setDistanceMeasure(EDistanceMeasure.CHEBYSHEV_DISTANCE);
		else if (distmeasure.equals(sArDistOptions[3]))
			clusterState.setDistanceMeasure(EDistanceMeasure.PEARSON_CORRELATION);

		if (treeClusterAlgo.equals(sArTreeClusterer[0]))
			clusterState.setTreeClustererAlgo(ETreeClustererAlgo.COMPLETE_LINKAGE);
		else if (treeClusterAlgo.equals(sArTreeClusterer[1]))
			clusterState.setTreeClustererAlgo(ETreeClustererAlgo.AVERAGE_LINKAGE);
		else if (treeClusterAlgo.equals(sArTreeClusterer[2]))
			clusterState.setTreeClustererAlgo(ETreeClustererAlgo.SINGLE_LINKAGE);

		clusterState.setAffinityPropClusterFactorGenes(fclusterFactorGenes);
		clusterState.setAffinityPropClusterFactorExperiments(fclusterFactorExperiments);
		clusterState.setKMeansClusterCntGenes(iClusterCntGenes);
		clusterState.setKMeansClusterCntExperiments(iClusterCntExperiments);

		if (clusterState.getClustererAlgo().equals(EClustererAlgo.OTHER))
			clusterState = othersTab.getClusterState();

		// by default we use the main VAs for clustering
		clusterState.setContentVAType(DataTable.RECORD);
		clusterState.setStorageVAType(DataTable.DIMENSION);

		ClusteringProgressBar progressBar =
			new ClusteringProgressBar(clusterState.getClustererAlgo(), clusterState.getClustererType());
		progressBar.run();

	}

	@Override
	public void dispose() {
	}

	public ClusterState getClusterState() {
		return clusterState;
	}

}
