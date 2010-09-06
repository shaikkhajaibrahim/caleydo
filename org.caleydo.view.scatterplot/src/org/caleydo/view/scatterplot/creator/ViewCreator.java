package org.caleydo.view.scatterplot.creator;

import java.util.ArrayList;

import org.caleydo.core.manager.datadomain.DataDomainManager;
import org.caleydo.core.manager.view.creator.AGLViewCreator;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.GLCaleydoCanvas;
import org.caleydo.view.scatterplot.GLScatterPlot;
import org.caleydo.view.scatterplot.SerializedScatterplotView;
import org.caleydo.view.scatterplot.toolbar.ScatterplotToolBarContent;

public class ViewCreator extends AGLViewCreator {

	public ViewCreator() {
		super(GLScatterPlot.VIEW_ID);
	}

	@Override
	public AGLView createGLView(GLCaleydoCanvas glCanvas, ViewFrustum viewFrustum) {

		return new GLScatterPlot(glCanvas, viewFrustum);
	}

	@Override
	public ASerializedView createSerializedView() {

		return new SerializedScatterplotView();
	}

	@Override
	public Object createToolBarContent() {
		return new ScatterplotToolBarContent();
	}

	@Override
	protected void registerDataDomains() {
		ArrayList<String> dataDomainTypes = new ArrayList<String>();
		dataDomainTypes.add("org.caleydo.datadomain.genetic");
		dataDomainTypes.add("org.caleydo.datadomain.generic");
		dataDomainTypes.add("org.caleydo.datadomain.clinical");

		DataDomainManager
				.get()
				.getAssociationManager()
				.registerDatadomainTypeViewTypeAssociation(dataDomainTypes,
						GLScatterPlot.VIEW_ID);
	}
}
