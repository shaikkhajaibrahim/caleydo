/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *  
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.view.datagraph.layout.edge.rendering;

import java.util.List;
import org.caleydo.core.data.container.DataContainer;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.util.spline.ConnectionBandRenderer;
import org.caleydo.view.datagraph.Edge;
import org.caleydo.view.datagraph.GLDataViewIntegrator;
import org.caleydo.view.datagraph.layout.edge.rendering.connectors.ANodeConnector;
import org.caleydo.view.datagraph.layout.edge.rendering.connectors.BottomSideConnector;
import org.caleydo.view.datagraph.layout.edge.rendering.connectors.TopSideConnector;
import org.caleydo.view.datagraph.node.IDVINode;

public class TwoLayeredEdgeBandRenderer extends AEdgeBandRenderer {

	public TwoLayeredEdgeBandRenderer(Edge edge, GLDataViewIntegrator view) {
		super(edge, view);
	}

	@Override
	protected void determineNodeConnectors(
			Pair<ANodeConnector, ANodeConnector> nodeConnectors, IDVINode leftNode,
			IDVINode rightNode, IDVINode bottomNode, IDVINode topNode,
			List<DataContainer> commonDataContainersNode1,
			List<DataContainer> commonDataContainersNode2,
			ConnectionBandRenderer connectionBandRenderer) {

		ANodeConnector connector1 = new TopSideConnector(bottomNode, pixelGLConverter,
				connectionBandRenderer, viewFrustum, topNode);
		ANodeConnector connector2 = new BottomSideConnector(topNode, pixelGLConverter,
				connectionBandRenderer, viewFrustum, bottomNode);

		nodeConnectors.setFirst(connector1);
		nodeConnectors.setSecond(connector2);

		determineBundleConnectors(nodeConnectors, commonDataContainersNode1,
				commonDataContainersNode2, connectionBandRenderer);

		// if (!commonDataContainersNode1.isEmpty()) {
		//
		// if (node1.showsDataContainers()) {
		//
		// ANodeConnector currentConnector = null;
		//
		// if (node1.isUpsideDown()) {
		// currentConnector = new TopBundleConnector(node1,
		// pixelGLConverter, connectionBandRenderer,
		// commonDataContainersNode1, minBandWidth,
		// maxBandWidth, maxDataAmount);
		// } else {
		// currentConnector = new BottomBundleConnector(node1,
		// pixelGLConverter, connectionBandRenderer,
		// commonDataContainersNode1, minBandWidth,
		// maxBandWidth, maxDataAmount);
		// }
		//
		// if (connector1.getNode() == node1) {
		// connector1 = currentConnector;
		// } else {
		// connector2 = currentConnector;
		// }
		// }
		//
		// if (node2.showsDataContainers()) {
		//
		// ANodeConnector currentConnector = null;
		//
		// if (node2.isUpsideDown()) {
		// currentConnector = new TopBundleConnector(node2,
		// pixelGLConverter, connectionBandRenderer,
		// commonDataContainersNode2, minBandWidth,
		// maxBandWidth, maxDataAmount);
		// } else {
		// currentConnector = new BottomBundleConnector(node2,
		// pixelGLConverter, connectionBandRenderer,
		// commonDataContainersNode2, minBandWidth,
		// maxBandWidth, maxDataAmount);
		// }
		//
		// if (connector1.getNode() == node2) {
		// connector1 = currentConnector;
		// } else {
		// connector2 = currentConnector;
		// }
		// }
		// }

	}

}