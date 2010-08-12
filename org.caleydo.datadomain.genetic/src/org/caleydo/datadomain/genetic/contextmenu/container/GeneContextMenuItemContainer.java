package org.caleydo.datadomain.genetic.contextmenu.container;

import org.caleydo.core.data.mapping.IDType;
import org.caleydo.core.manager.datadomain.DataDomainManager;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.view.opengl.util.overlay.contextmenu.AItemContainer;
import org.caleydo.core.view.opengl.util.overlay.contextmenu.item.BookmarkItem;
import org.caleydo.datadomain.genetic.GeneticDataDomain;
import org.caleydo.datadomain.genetic.contextmenu.item.LoadPathwaysByGeneItem;
import org.caleydo.datadomain.genetic.contextmenu.item.ShowPathwaysByGeneItem;

/**
 * Implementation of AItemContainer for Genes. By passing a RefSeq int code all
 * relevant context menu items are constructed automatically
 * 
 * @author Alexander Lex
 */
public class GeneContextMenuItemContainer extends AItemContainer {

	/**
	 * Constructor.
	 */
	public GeneContextMenuItemContainer() {
		super();

	}

	public void setID(IDType idType, int id) {
		Integer davidID = GeneralManager.get().getIDMappingManager()
				.getID(idType, dataDomain.getPrimaryContentMappingType(), id);
		if (davidID == null)
			return;
		createMenuContent(davidID);
	}

	private void createMenuContent(int davidID) {
		GeneralManager generalManager = GeneralManager.get();

		String sGeneSymbol = generalManager.getIDMappingManager().getID(
				dataDomain.getPrimaryContentMappingType(),
				((GeneticDataDomain) (DataDomainManager.getInstance()
						.getDataDomain(GeneticDataDomain.dataDomainType)))
						.getHumanReadableContentIDType(), davidID);
		if (sGeneSymbol == "" || sGeneSymbol == null)
			sGeneSymbol = "Unkonwn Gene";
		addHeading(sGeneSymbol);

		if (GeneralManager.get().getPathwayManager().isPathwayLoadingFinished()) {
			LoadPathwaysByGeneItem loadPathwaysByGeneItem = new LoadPathwaysByGeneItem();
			loadPathwaysByGeneItem.setDavid(dataDomain.getPrimaryContentMappingType(),
					davidID);
			addContextMenuItem(loadPathwaysByGeneItem);

			ShowPathwaysByGeneItem showPathwaysByGeneItem = new ShowPathwaysByGeneItem();
			showPathwaysByGeneItem.setDavid(dataDomain.getPrimaryContentMappingType(),
					davidID);
			addContextMenuItem(showPathwaysByGeneItem);
		}

		BookmarkItem addToListItem = new BookmarkItem(
				dataDomain.getPrimaryContentMappingType(), davidID);

		addContextMenuItem(addToListItem);
	}
}
