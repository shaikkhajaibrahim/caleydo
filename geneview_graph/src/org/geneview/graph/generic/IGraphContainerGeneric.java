package org.geneview.graph.generic;

import java.util.Collection;

import org.geneview.graph.generic.EGraphItemHierarchy;
import org.geneview.graph.EGraphItemProperty;

public interface IGraphContainerGeneric <GraphItem> {

	/* (non-Javadoc)
	 * @see org.geneview.graph.AGraphObject#addGraphEdge()
	 */
	public void addGraphItemByType(GraphItem item,
			EGraphItemProperty prop, 
			EGraphItemHierarchy type);

	/* (non-Javadoc)
	 * @see org.geneview.graph.AGraphObject#addGraphEdge()
	 */
	public boolean removeGraphItemByType(GraphItem item,
			EGraphItemHierarchy type);

	/**
	 * Get all GraphItem registered to item using type.
	 * 
	 * @param item
	 * @param type
	 * @return Collection of GraphItems
	 */
	public Collection<GraphItem> getAllGraphItemByType(EGraphItemHierarchy type);

	/**
	 * Get all GraphItems linked to one item by using a property prop.
	 * 
	 * @param item
	 * @param prop
	 * @return
	 */
	public Collection<GraphItem> getAllGraphItemByProperty(EGraphItemProperty prop);
	
	public boolean containsGraphItemByType(GraphItem item,
			EGraphItemHierarchy type);

	public boolean containsGraphItemByProp(GraphItem item,
			EGraphItemProperty prop);
	
//	/* ------------------- */
//	/* ---  GRAPH DATA --- */
//	/* ------------------- */
//	
//	/* (non-Javadoc)
//	 * @see org.geneview.graph.AGraphObject#addGraphEdge()
//	 */
//	public boolean addGraphDataById(GraphData data, int id);
//
//	/* (non-Javadoc)
//	 * @see org.geneview.graph.AGraphObject#addGraphEdge()
//	 */
//	public void setGraphDataById(GraphData data, int id);
//
//	public GraphData getGraphDataById(int id);
//
//	public GraphData removeGraphDataById(int id);
//
//	/* (non-Javadoc)
//	 * @see org.geneview.graph.IGraphObject#getAllData()
//	 */
//	public Collection<GraphData> getAllGraphDataCopy();
//
//	/* (non-Javadoc)
//	 * @see org.geneview.graph.IGraphObject#removeAllData()
//	 */
//	public void removeAllData();
//
//	public void removeAllGraphItem();

	
	/* ----------------------------- */
	/* ---  GRAPH HIERARCHY DATA --- */
	/* ----------------------------- */
	
//	public boolean addGraph(GraphParent parent, EGraphItemHierarchy type);
//
//	public void setGraph(GraphParent parent, EGraphItemHierarchy type);
//
//	public boolean removeGraphAsType(GraphParent parent, EGraphItemHierarchy type);
//
//	/**
//	 * Remove this graph from all instances no matter if parent, child or neighbor.
//	 * 
//	 * @param parent
//	 * @return
//	 */
//	public boolean removeGraph(GraphParent parent);
//	
//	/** 
//	 * removes all references to parent graphs, children graphs, and neighborhood graphs. 
//	 * */
//	public boolean removeAllGraphs();
//	
//	public boolean containsGraph(GraphParent parent);
//	
//	public boolean containsGraphAsType(GraphParent parent, EGraphItemHierarchy type);
//
//	public Collection <EGraphItemHierarchy> containsGraphAnyType(GraphParent parent);

}
