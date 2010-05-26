package org.caleydo.view.heatmap.dendrogram;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.manager.datadomain.EDataDomain;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.view.opengl.camera.EProjectionMode;
import org.caleydo.core.view.opengl.camera.ViewFrustum;

/**
 * Serialized form of a heatmap-view.
 * 
 * @author Werner Puff
 */
@XmlRootElement
@XmlType
public class SerializedDendogramVerticalView extends ASerializedView {

	public static final String GUI_ID = "org.caleydo.rcp.views.opengl.GLDendrogramVerticalView";

	/**
	 * Default constructor with default initialization
	 */
	public SerializedDendogramVerticalView() {
	}

	public SerializedDendogramVerticalView(EDataDomain dataDomain) {
		super(dataDomain);
	}

	@Override
	public ViewFrustum getViewFrustum() {
		ViewFrustum viewFrustum = new ViewFrustum(EProjectionMode.ORTHOGRAPHIC,
				0, 8, 0, 8, -20, 20);
		return viewFrustum;
	}

	@Override
	public String getViewType() {
		return GUI_ID;
	}

}
