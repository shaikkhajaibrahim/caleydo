package org.caleydo.view.tabular;

import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.manager.datadomain.DataDomainManager;
import org.caleydo.core.manager.datadomain.IDataDomain;
import org.caleydo.core.manager.datadomain.IDataDomainBasedView;
import org.caleydo.rcp.view.rcp.CaleydoRCPViewPart;
import org.eclipse.swt.widgets.Composite;

public class RcpTabularDataView extends CaleydoRCPViewPart {

	private TabularDataView tabularDataView;

	@Override
	public void createPartControl(Composite parent) {
		tabularDataView = (TabularDataView) GeneralManager.get().getViewGLCanvasManager()
				.createView("org.caleydo.view.tabular", -1);

		parentComposite = parent;

		GeneralManager.get().getViewGLCanvasManager().registerItem(tabularDataView);
		view = tabularDataView;

		if (view instanceof IDataDomainBasedView<?>) {
			String dataDomainType = determineDataDomain(view
					.getSerializableRepresentation());
			((IDataDomainBasedView<IDataDomain>) view).setDataDomain(DataDomainManager
					.get().getDataDomain(dataDomainType));
		}

		tabularDataView.initViewRCP(parent);
		tabularDataView.drawView();
	}

	@Override
	public void setFocus() {

	}

	@Override
	public void dispose() {
		super.dispose();
		tabularDataView.unregisterEventListeners();
		GeneralManager.get().getViewGLCanvasManager()
				.unregisterItem(tabularDataView.getID());
	}

	public TabularDataView getTabularDataView() {
		return tabularDataView;
	}
}
