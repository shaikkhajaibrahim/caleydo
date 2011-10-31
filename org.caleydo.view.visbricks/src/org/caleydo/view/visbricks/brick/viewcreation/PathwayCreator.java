package org.caleydo.view.visbricks.brick.viewcreation;

import java.util.Collection;

import javax.media.opengl.GL2;

import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.view.opengl.camera.CameraProjectionMode;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.datadomain.pathway.PathwayDataDomain;
import org.caleydo.datadomain.pathway.data.PathwayDataContainer;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.manager.PathwayManager;
import org.caleydo.view.pathway.GLPathway;
import org.caleydo.view.pathway.SerializedPathwayView;
import org.caleydo.view.visbricks.brick.GLBrick;

/**
 * Creator for a remote rendered {@link GLPathway}.
 * 
 * @author Christian Partl
 * 
 */
public class PathwayCreator implements IRemoteViewCreator {

	@Override
	public AGLView createRemoteView(GLBrick remoteRenderingView, GL2 gl,
			GLMouseListener glMouseListener) {

		Collection<PathwayGraph> pathways = PathwayManager.get().getAllItems();

		PathwayGraph pathway = ((PathwayDataContainer) (remoteRenderingView.getDataContainer()))
				.getPathway();

		if (pathway == null) {
			for (PathwayGraph p : pathways) {
				pathway = p;
				break;
			}
		}

		SerializedPathwayView serPathway = new SerializedPathwayView();
		serPathway.setDataDomainID("org.caleydo.datadomain.pathway");
		serPathway.setPathwayID(pathway.getID());

		GLPathway pathwayView = (GLPathway) GeneralManager
				.get()
				.getViewManager()
				.createGLView(
						GLPathway.class,
						remoteRenderingView.getParentGLCanvas(),
						remoteRenderingView.getParentComposite(),
						new ViewFrustum(CameraProjectionMode.ORTHOGRAPHIC, 0, 1, 0, 1,
								-1, 1));

		pathwayView.setPathway(pathway);
		// FIXME: DataDomainByType may be not appropriate
//		pathwayView.setPathwayDataDomain((PathwayDataDomain) (DataDomainManager.get()
//				.getDataDomainByType(serPathway.getDataDomainID())));
		pathwayView.enablePathwayTextures(true);
		pathwayView.enableNeighborhood(true);
		pathwayView.enableGeneMapping(true);
		pathwayView.setRemoteRenderingGLView(remoteRenderingView);
		// pathway.setTable(remoteRenderingView.getTable());
		// parCoords.setDataDomain(remoteRenderingView.getDataDomain());
		pathwayView.initialize();
		pathwayView.initRemote(gl, remoteRenderingView, glMouseListener);
		// parCoords.setDetailLevel(DetailLevel.LOW);
		// ContentVirtualArray recordVA = remoteRenderingView.getRecordVA();
		// if (recordVA != null)
		// parCoords.setRecordVA(recordVA);

		return pathwayView;
	}

}
