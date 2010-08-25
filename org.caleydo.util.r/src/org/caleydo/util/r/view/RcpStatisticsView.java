package org.caleydo.util.r.view;

import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.manager.datadomain.ASetBasedDataDomain;
import org.caleydo.core.manager.datadomain.DataDomainManager;
import org.caleydo.rcp.view.rcp.CaleydoRCPViewPart;
import org.eclipse.swt.widgets.Composite;

public class RcpStatisticsView extends CaleydoRCPViewPart {
	
	private StatisticsView statisticsView;

	@Override
	public void createPartControl(Composite parent) {
		statisticsView = (StatisticsView) GeneralManager.get().getViewGLCanvasManager()
				.createView(StatisticsView.VIEW_ID, -1, "Statistics View");

		ASetBasedDataDomain dataDomain = (ASetBasedDataDomain) DataDomainManager
				.getInstance().getDataDomain(
						determineDataDomain(new SerializedStatisticsView()));
		statisticsView.initViewRCP(parent);
		statisticsView.drawView();

		parentComposite = parent;

		GeneralManager.get().getViewGLCanvasManager().registerItem(statisticsView);
		view = statisticsView;
	}

	@Override
	public void setFocus() {

	}

	@Override
	public void dispose() {
		super.dispose();
		statisticsView.unregisterEventListeners();
		GeneralManager.get().getViewGLCanvasManager()
				.unregisterItem(statisticsView.getID());
	}

	public StatisticsView getTabularDataView() {
		return statisticsView;
	}
}
