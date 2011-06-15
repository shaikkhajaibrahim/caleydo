package org.caleydo.core.manager.datadomain;

import java.util.HashSet;
import java.util.Set;

import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.manager.mapping.IDMappingManager;
import org.jgrapht.graph.SimpleGraph;

/**
 * @author Alexander Lex
 */
public class DataDomainGraph {
	SimpleGraph<IDataDomain, Edge> dataDomainGraph;

	public static final String CLINICAL = "org.caleydo.datadomain.clinical";
	public static final String TISSUE = "org.caleydo.datadomain.tissue";
	public static final String GENETIC = "org.caleydo.datadomain.genetic";
	public static final String PATHWAY = "org.caleydo.datadomain.pathway";
	public static final String ORGAN = "org.caleydo.datadomain.organ";

	public DataDomainGraph() {
		EdgeFactory edgeFactory = new EdgeFactory();
		dataDomainGraph = new SimpleGraph<IDataDomain, Edge>(edgeFactory);

		// initDataDomainGraph();
	}

	// public void initDataDomainGraph() {
	// dataDomainGraph.addVertex(CLINICAL);
	// dataDomainGraph.addVertex(TISSUE);
	// dataDomainGraph.addVertex(GENETIC);
	// dataDomainGraph.addVertex(PATHWAY);
	// dataDomainGraph.addVertex(ORGAN);
	//
	// dataDomainGraph.addEdge(CLINICAL, GENETIC);
	// dataDomainGraph.addEdge(CLINICAL, TISSUE);
	// dataDomainGraph.addEdge(CLINICAL, ORGAN);
	// dataDomainGraph.addEdge(GENETIC, PATHWAY);
	// dataDomainGraph.addEdge(TISSUE, GENETIC);
	// }

	public void addDataDomain(IDataDomain dataDomain) {
		if (dataDomainGraph.containsVertex(dataDomain))
			return;

		dataDomainGraph.addVertex(dataDomain);

		IDMappingManager idMappingManager = GeneralManager.get().getIDMappingManager();

		//FIXME: This is not generic at all, move the IDTypes of the DataDomains into IDataDomain 
		for (IDataDomain vertex : dataDomainGraph.vertexSet()) {
			if (vertex != dataDomain) {
				boolean mappingExists = false;

				if (dataDomain instanceof ASetBasedDataDomain && vertex instanceof ASetBasedDataDomain) {
					ASetBasedDataDomain setBasedDataDomain = (ASetBasedDataDomain) dataDomain;
					ASetBasedDataDomain setBasedVertex = (ASetBasedDataDomain) vertex;

					// TODO: Also mapping between content and storage?
					boolean hasContentMapping =
						idMappingManager.hasMapping(setBasedDataDomain.getPrimaryContentMappingType(),
							setBasedVertex.getPrimaryContentMappingType());
					boolean hasStorageMapping =
						idMappingManager.hasMapping(setBasedDataDomain.getPrimaryStorageMappingType(),
							setBasedVertex.getPrimaryStorageMappingType());
					if (hasContentMapping || hasStorageMapping) {
						mappingExists = true;
					}
				}

				if ((dataDomain.getDataDomainType().startsWith(CLINICAL) && vertex.getDataDomainType()
					.startsWith(TISSUE))
					|| (vertex.getDataDomainType().startsWith(CLINICAL) && dataDomain.getDataDomainType()
						.startsWith(TISSUE))) {
					mappingExists = true;
				}
				if ((dataDomain.getDataDomainType().startsWith(CLINICAL) && vertex.getDataDomainType()
					.startsWith(ORGAN))
					|| (vertex.getDataDomainType().startsWith(CLINICAL) && dataDomain.getDataDomainType()
						.startsWith(ORGAN))) {
					mappingExists = true;
				}

				if ((dataDomain.getDataDomainType().startsWith(GENETIC) && vertex.getDataDomainType()
					.startsWith(PATHWAY))
					|| (vertex.getDataDomainType().startsWith(GENETIC) && dataDomain.getDataDomainType()
						.startsWith(PATHWAY))) {
					mappingExists = true;
				}

				if ((dataDomain.getDataDomainType().startsWith(GENETIC) && vertex.getDataDomainType()
					.startsWith(TISSUE))
					|| (vertex.getDataDomainType().startsWith(GENETIC) && dataDomain.getDataDomainType()
						.startsWith(TISSUE))) {
					mappingExists = true;
				}

				if (dataDomain.getDataDomainType().startsWith(PATHWAY)
					&& vertex.getDataDomainType().startsWith(PATHWAY))
					mappingExists = true;

				if (mappingExists) {
					dataDomainGraph.addEdge(dataDomain, vertex);
				}

			}
		}

	}

	public Set<IDataDomain> getNeighboursOf(IDataDomain vertex) {
		Set<Edge> edges = dataDomainGraph.edgesOf(vertex);
		Set<IDataDomain> vertices = new HashSet<IDataDomain>();
		for (Edge edge : edges) {
			vertices.add(edge.getOtherSideOf(vertex));
		}

		return vertices;

	}

	public SimpleGraph<IDataDomain, Edge> getGraph() {
		return dataDomainGraph;
	}

	public static void main(String args[]) {
		DataDomainGraph graph = new DataDomainGraph();

		// System.out.println(graph.dataDomainGraph.edgesOf(CLINICAL));

		// System.out.println(graph.getNeighboursOf(GENETIC));

	}
}
