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
package org.caleydo.view.radial.toolbar;

import java.util.ArrayList;
import java.util.List;
import org.caleydo.core.gui.toolbar.AToolBarContent;
import org.caleydo.core.gui.toolbar.ActionToolBarContainer;
import org.caleydo.core.gui.toolbar.IToolBarItem;
import org.caleydo.core.gui.toolbar.ToolBarContainer;
import org.caleydo.view.radial.GLRadialHierarchy;
import org.caleydo.view.radial.SerializedRadialHierarchyView;
import org.caleydo.view.radial.actions.ChangeColorModeAction;
import org.caleydo.view.radial.actions.GoBackInHistoryAction;
import org.caleydo.view.radial.actions.GoForthInHistoryAction;

/**
 * ToolBarContent implementation for radial layout specific toolbar items.
 * 
 * @author Christian Partl
 */
public class RadialHierarchyToolBarContent extends AToolBarContent {

	public static final String IMAGE_PATH = "resources/icons/view/radial/radial_color_mapping.png";

	public static final String VIEW_TITLE = "Radial Hierarchy";
	private IToolBarItem depthSlider;

	@Override
	protected List<ToolBarContainer> getToolBarContent() {
		ActionToolBarContainer container = new ActionToolBarContainer();

		container.setImagePath(IMAGE_PATH);
		container.setTitle(VIEW_TITLE);
		List<IToolBarItem> actionList = new ArrayList<IToolBarItem>();
		container.setToolBarItems(actionList);

		SerializedRadialHierarchyView serializedView = (SerializedRadialHierarchyView) getTargetViewData();

		IToolBarItem goBackInHistory = new GoBackInHistoryAction();
		IToolBarItem goForthInHistory = new GoForthInHistoryAction();
		IToolBarItem changeColorMode = new ChangeColorModeAction();
		// IToolBarItem magnifyingGlass = new ToggleMagnifyingGlassAction();
		if (depthSlider == null) {
			depthSlider = new DepthSlider("",
					serializedView.getMaxDisplayedHierarchyDepth());
		}
		actionList.add(goBackInHistory);
		actionList.add(goForthInHistory);
		actionList.add(changeColorMode);
		// actionList.add(magnifyingGlass);
		actionList.add(depthSlider);

		ArrayList<ToolBarContainer> list = new ArrayList<ToolBarContainer>();
		list.add(container);

		return list;
	}

	@Override
	public Class<?> getViewClass() {
		return GLRadialHierarchy.class;
	}

}