package org.geneview.core.data.graph.item.edge;

import org.geneview.util.graph.EGraphItemKind;
import org.geneview.util.graph.item.GraphItem;


public class PathwayReactionEdgeGraphItem 
extends GraphItem {
	
	final String sReactionId;
	
	final EPathwayReactionEdgeType type;
	
	public PathwayReactionEdgeGraphItem(
			final int iId,
			final String sReactionId,
			final String sType) {
		
		super(iId, EGraphItemKind.EDGE);

		this.sReactionId = sReactionId;
		
		type = EPathwayReactionEdgeType.valueOf(sType);
	}

	public EPathwayReactionEdgeType getType() {
		
		return type;
	}
	
	public String getReactionId() {
		
		return sReactionId;
	}
}