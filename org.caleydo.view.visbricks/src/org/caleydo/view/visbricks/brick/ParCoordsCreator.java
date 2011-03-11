package org.caleydo.view.visbricks.brick;

import javax.media.opengl.GL2;

import org.caleydo.core.data.virtualarray.ContentVirtualArray;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.view.opengl.camera.ECameraProjectionMode;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.DetailLevel;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.view.parcoords.GLParallelCoordinates;

public class ParCoordsCreator implements IRemoteViewCreator {

	@Override
	public AGLView createRemoteView(GLBrick remoteRenderingView, GL2 gl,
			GLMouseListener glMouseListener) {
		
		GLParallelCoordinates parCoords = (GLParallelCoordinates) GeneralManager
				.get()
				.getViewGLCanvasManager()
				.createGLView(
						GLParallelCoordinates.class,
						remoteRenderingView.getParentGLCanvas(),

						new ViewFrustum(ECameraProjectionMode.ORTHOGRAPHIC, 0,
								1, 0, 1, -1, 1));

		parCoords.setRemoteRenderingGLView(remoteRenderingView);
		parCoords.setSet(remoteRenderingView.getSet());
		parCoords.setDataDomain(remoteRenderingView.getDataDomain());
		parCoords.initialize();
		parCoords.initRemote(gl, remoteRenderingView, glMouseListener);
		parCoords.setDetailLevel(DetailLevel.LOW);
		ContentVirtualArray contentVA = remoteRenderingView.getContentVA();
		if (contentVA != null)
			parCoords.setContentVA(contentVA);

		return parCoords;
	}

}
