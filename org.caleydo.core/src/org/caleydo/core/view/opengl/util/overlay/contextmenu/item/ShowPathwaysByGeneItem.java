package org.caleydo.core.view.opengl.util.overlay.contextmenu.item;

import java.util.Set;

import org.caleydo.core.data.graph.pathway.core.PathwayGraph;
import org.caleydo.core.data.mapping.EIDType;
import org.caleydo.core.manager.event.view.remote.LoadPathwaysByGeneEvent;
import org.caleydo.core.manager.specialized.genetic.GeneticIDMappingHelper;
import org.caleydo.core.view.opengl.util.overlay.contextmenu.AContextMenuItem;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;

/**
 * <p>
 * Item for showing all pathways that contain a specific gene in a sub menu, where these pathways can be
 * loaded individually. The sub-pathways can either be specified manually or the convenience method
 * {@link ShowPathwaysByGeneItem#setRefSeqInt(int)} can be used, which creates the sub-menus automatically.
 * </p>
 * <p>
 * Text and icon have default values but can be overriden.
 * </p>
 * 
 * @author Alexander Lex
 */
public class ShowPathwaysByGeneItem
	extends AContextMenuItem {

	/**
	 * Constructor which sets the default values for icon and text
	 */
	public ShowPathwaysByGeneItem() {
		super();
		setIconTexture(EIconTextures.LOAD_DEPENDING_PATHWAYS);
		setText("Pathways");
	}

	/**
	 * Convenience method that automatically creates a {@link LoadPathwaysByGeneEvent} based on a RefSeqInt
	 * 
	 * @param iRefSeq
	 *            the int code associated with a refseq
	 */
	public void setRefSeqInt(int iRefSeqID) {
		
		Set<PathwayGraph> pathwayGraphs = GeneticIDMappingHelper.get().getPathwayGraphsByGeneID(EIDType.REFSEQ_MRNA_INT, iRefSeqID);

		for (PathwayGraph pathwayGraph : pathwayGraphs) {
			addSubItem(new LoadPathwaysByPathwayIDItem(pathwayGraph.getID()));
		}
	}

}
