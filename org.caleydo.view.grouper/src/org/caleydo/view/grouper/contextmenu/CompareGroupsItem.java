package org.caleydo.view.grouper.contextmenu;

import java.util.ArrayList;

import org.caleydo.core.data.collection.table.DataTable;
import org.caleydo.core.manager.event.view.OpenMatchmakerViewEvent;
import org.caleydo.core.view.opengl.util.overlay.contextmenu.AContextMenuItem;

public class CompareGroupsItem extends AContextMenuItem {

	public CompareGroupsItem(ArrayList<DataTable> setsToCompare) {
		super();

		setText("Compare Groups in Matchmaker");

		OpenMatchmakerViewEvent openViewEvent = new OpenMatchmakerViewEvent();
		openViewEvent.setViewType("org.caleydo.view.matchmaker");
		openViewEvent.setSender(this);
		openViewEvent.setDataTablesToCompare(setsToCompare);
		registerEvent(openViewEvent);
	}
}
