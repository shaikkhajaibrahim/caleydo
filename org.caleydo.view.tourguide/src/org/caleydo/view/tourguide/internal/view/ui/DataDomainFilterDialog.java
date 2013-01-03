package org.caleydo.view.tourguide.internal.view.ui;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.io.gui.dataimport.widget.BooleanCallback;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.view.tourguide.api.query.DataDomainQuery;
import org.caleydo.view.tourguide.api.query.filter.CompareDomainFilter;
import org.caleydo.view.tourguide.api.query.filter.EStringCompareOperator;
import org.caleydo.view.tourguide.api.query.filter.SpecificDataDomainFilter;
import org.caleydo.view.tourguide.internal.TourGuideRenderStyle;
import org.caleydo.view.tourguide.spi.query.filter.IDataDomainFilter;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class DataDomainFilterDialog extends TitleAreaDialog {
	// the root element to populate the viewer with
	private final SpecificDataDomainFilter filter;
	private final DataDomainQuery query;
	private final BooleanCallback hasFilterAfterwardsCallback;

	private List<DataDomainFilterWidget> filters = new ArrayList<>();
	private Group filterContainer;

	public DataDomainFilterDialog(Shell shell, DataDomainQuery query, SpecificDataDomainFilter filter,
			BooleanCallback hasFilterAfterwardsCallback) {
		super(shell);
		this.query = query;
		this.filter = filter;
		this.filters = new ArrayList<>();
		this.hasFilterAfterwardsCallback = hasFilterAfterwardsCallback;
	}

	@Override
	public void create() {
		super.create();
		this.setTitle("Edit Data Domain Filters for " + filter.getDataDomain().getLabel());
		this.setBlockOnOpen(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite c = new Composite((Composite) super.createDialogArea(parent), SWT.NONE);
		c.setLayout(new GridLayout(2, false));
		c.setLayoutData(new GridData(GridData.FILL_BOTH));

		addRowAdder(parent).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 200;
		scrolledComposite.setLayoutData(gridData);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);

		this.filterContainer = new Group(scrolledComposite, SWT.NONE);
		this.filterContainer.setText("Active Filters:");
		filterContainer.setLayout(new GridLayout(4, false));

		for (IDataDomainFilter f : this.filter) {
			if (f instanceof CompareDomainFilter) {
				addFilterRow((CompareDomainFilter) f);
			}
		}

		scrolledComposite.setContent(filterContainer);
		Point point = filterContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		filterContainer.setSize(point);
		scrolledComposite.setMinSize(point);

		return c;
	}

	private void addFilterRow(final CompareDomainFilter f) {
		final DataDomainFilterWidget w = new DataDomainFilterWidget(filterContainer, f);
		this.filters.add(w);
		Button removeRow = new Button(filterContainer, SWT.PUSH);
		removeRow.setImage(GeneralManager.get().getResourceLoader()
				.getImage(filterContainer.getDisplay(), TourGuideRenderStyle.ICON_DELETE_ROW));
		removeRow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onRemoveFilter(w);
			}
		});
	}

	/**
	 * @param parent
	 */
	private Composite addRowAdder(Composite parent) {
		parent = new Composite(parent, SWT.NONE);
		RowLayout r = new RowLayout(SWT.HORIZONTAL);
		r.center = true;
		parent.setLayout(r);

		new Label(parent, SWT.NONE).setText("Add New Filter: ");

		Button addRow = new Button(parent, SWT.PUSH);
		addRow.setImage(GeneralManager.get().getResourceLoader()
				.getImage(parent.getDisplay(), TourGuideRenderStyle.ICON_ADD_ROW));
		addRow.setText("Add Stratification Filter");
		addRow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onAddFilter(true);
			}
		});

		addRow = new Button(parent, SWT.PUSH);
		addRow.setImage(GeneralManager.get().getResourceLoader()
				.getImage(parent.getDisplay(), TourGuideRenderStyle.ICON_ADD_ROW));
		addRow.setText("Add Group Filter");
	    addRow.addSelectionListener(new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
				onAddFilter(false);
	    	}
		});

	    return parent;
	}

	protected void onAddFilter(boolean stratificationFilter) {
		addFilterRow(new CompareDomainFilter(EStringCompareOperator.NOT_EQUAL, "", stratificationFilter));
		this.filterContainer.layout(true);
	}

	protected void onRemoveFilter(DataDomainFilterWidget w) {
		int i = this.filters.indexOf(w);
		this.filters.remove(i);
		Control[] arr = filterContainer.getChildren();
		for (int j = i * 4; j < i * 4 + 4; ++j)
			arr[j].dispose();
		this.filterContainer.layout(true);
	}

	private boolean validate() {
		return true;
	}

	@Override
	protected void okPressed() {
		if (!validate())
			return;
		save();
		super.okPressed();
	}

	private void save() {
		filter.clear();
		for (DataDomainFilterWidget w : this.filters) {
			filter.add(w.save());
		}
		query.updateFilter(filter);
		hasFilterAfterwardsCallback.on(!filter.isEmpty());
	}
}