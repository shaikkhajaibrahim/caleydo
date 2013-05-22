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
package org.caleydo.vis.rank.ui;

import static org.caleydo.vis.rank.ui.RenderStyle.HIST_HEIGHT;
import static org.caleydo.vis.rank.ui.RenderStyle.LABEL_HEIGHT;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.vis.rank.config.IRankTableUIConfig;
import org.caleydo.vis.rank.model.ARankColumnModel;
import org.caleydo.vis.rank.model.RankTableModel;
import org.caleydo.vis.rank.ui.column.ACompositeHeaderUI;
import org.caleydo.vis.rank.ui.column.ColumnUIs;

/**
 * a visualzation of the table header row, in HTML it would be the thead section
 *
 * @author Samuel Gratzl
 *
 */
public final class TableHeaderUI extends ACompositeHeaderUI {
	private final RankTableModel table;
	private boolean isCompact = false;

	public TableHeaderUI(RankTableModel table, IRankTableUIConfig config) {
		super(config, 0);
		this.table = table;
		this.table.addPropertyChangeListener(RankTableModel.PROP_COLUMNS, childrenChanged);
		setLayoutData(table);
		setSize(-1, (HIST_HEIGHT + LABEL_HEIGHT * 2) * 1);
		this.init(table.getColumns());
	}

	/**
	 * @return the isCompact, see {@link #isCompact}
	 */
	public boolean isCompact() {
		return isCompact;
	}

	/**
	 * @param isCompact
	 *            setter, see {@link isCompact}
	 */
	public void setCompact(boolean isCompact) {
		this.isCompact = isCompact;
	}

	@Override
	protected void setHasThick(boolean hasThick) {
		super.setHasThick(hasThick);
		setSize(-1, (HIST_HEIGHT + LABEL_HEIGHT) * 1 + (hasThick ? THICK_HEIGHT : 0));
	}

	@Override
	protected GLElement wrapImpl(ARankColumnModel model) {
		return ColumnUIs.createHeader(model, config, true);
	}

	@Override
	protected void takeDown() {
		this.table.removePropertyChangeListener(RankTableModel.PROP_COLUMNS, childrenChanged);
		super.takeDown();
	}

	@Override
	protected float getChildWidth(int i, ARankColumnModel model) {
		return model.getWidth();
	}

	@Override
	public boolean canMoveHere(int index, ARankColumnModel model, boolean clone) {
		return table.isMoveAble(model, index, clone);
	}

	@Override
	public void moveHere(int index, ARankColumnModel model, boolean clone) {
		assert canMoveHere(index, model, clone);
		this.table.move(model, index, clone);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		if (!isCompact) {
			g.incZ(-0.5f);
			g.color(RenderStyle.COLOR_BACKGROUND_EVEN);
			g.fillRect(0, h - RenderStyle.HIST_HEIGHT, w, RenderStyle.HIST_HEIGHT);
			g.incZ(0.5f);
		}
		super.renderImpl(g, w, h);
	}
}


