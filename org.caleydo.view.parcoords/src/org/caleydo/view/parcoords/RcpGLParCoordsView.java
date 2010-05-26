package org.caleydo.view.parcoords;

import org.caleydo.core.manager.IDataDomain;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.datadomain.genetic.GeneticDataDomain;
import org.caleydo.rcp.view.rcp.ARcpGLViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

public class RcpGLParCoordsView extends ARcpGLViewPart {

	/**
	 * Constructor.
	 */
	public RcpGLParCoordsView() {
		super();
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);

		if (memento == null) {
			SerializedParallelCoordinatesView serializedView = new SerializedParallelCoordinatesView(
					dataDomain);
			initSerializedView = serializedView;
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		IDataDomain usecase = GeneralManager.get().getUseCase(dataDomain);
		if (usecase != null && usecase instanceof GeneticDataDomain
				&& ((GeneticDataDomain) usecase).isPathwayViewerMode()) {
			MessageBox alert = new MessageBox(new Shell(), SWT.OK);
			alert
					.setMessage("Cannot create parallel coordinates in pathway viewer mode!");
			alert.open();

			dispose();
			return;
		}

		createGLCanvas();
		createGLView(initSerializedView, glCanvas.getID());
	}

	@Override
	public ASerializedView createDefaultSerializedView() {
		SerializedParallelCoordinatesView serializedView = new SerializedParallelCoordinatesView(
				dataDomain);
		return serializedView;
	}

	@Override
	public String getViewGUIID() {
		return GLParallelCoordinates.VIEW_ID;
	}

}