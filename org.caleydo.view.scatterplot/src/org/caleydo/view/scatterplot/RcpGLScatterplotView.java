package org.caleydo.view.scatterplot;

import org.caleydo.core.manager.IDataDomain;
import org.caleydo.core.manager.datadomain.EDataDomain;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.datadomain.genetic.GeneticDataDomain;
import org.caleydo.rcp.view.rcp.ARcpGLViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class RcpGLScatterplotView extends ARcpGLViewPart {

	// FIXME: check if it is ok to overwrite
	private EDataDomain dataDomain;

	/**
	 * Constructor.
	 */
	public RcpGLScatterplotView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		IDataDomain usecase = GeneralManager.get().getUseCase(dataDomain);
		if (usecase != null && usecase instanceof GeneticDataDomain
				&& ((GeneticDataDomain) usecase).isPathwayViewerMode()) {
			MessageBox alert = new MessageBox(new Shell(), SWT.OK);
			alert
					.setMessage("Cannot create scatterplot in pathway viewer mode!");
			alert.open();

			dispose();
			return;
		}

		createGLCanvas();
		createGLView(initSerializedView, glCanvas.getID());
	}

	@Override
	public ASerializedView createDefaultSerializedView() {
		SerializedScatterplotView serializedView = new SerializedScatterplotView(
				dataDomain);
		return serializedView;
	}

	@Override
	public String getViewGUIID() {
		return GLScatterPlot.VIEW_ID;
	}

}