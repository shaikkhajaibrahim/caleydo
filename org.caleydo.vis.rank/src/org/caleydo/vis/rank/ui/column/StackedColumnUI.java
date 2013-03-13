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
package org.caleydo.vis.rank.ui.column;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.vis.rank.layout.IRowHeightLayout.ISetHeight;
import org.caleydo.vis.rank.model.ARankColumnModel;
import org.caleydo.vis.rank.model.IRow;
import org.caleydo.vis.rank.model.StackedRankColumnModel;
import org.caleydo.vis.rank.model.mixin.ICollapseableColumnMixin;
import org.caleydo.vis.rank.model.mixin.ICompressColumnMixin;
import org.caleydo.vis.rank.model.mixin.IMultiColumnMixin.MultiFloat;
import org.caleydo.vis.rank.ui.RenderStyle;

/**
 * @author Samuel Gratzl
 *
 */
public class StackedColumnUI extends ACompositeTableColumnUI<StackedRankColumnModel> {

	private final PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			switch (evt.getPropertyName()) {
			case StackedRankColumnModel.PROP_ALIGNMENT:
				onAlignmentChanged();
				break;
			case ICompressColumnMixin.PROP_COMPRESSED:
			case ICollapseableColumnMixin.PROP_COLLAPSED:
				onCompressedChanged();
				break;
			}
		}
	};

	public StackedColumnUI(StackedRankColumnModel model) {
		super(model, 1);
		model.addPropertyChangeListener(StackedRankColumnModel.PROP_ALIGNMENT, listener);
		model.addPropertyChangeListener(ICompressColumnMixin.PROP_COMPRESSED, listener);
		model.addPropertyChangeListener(ICollapseableColumnMixin.PROP_COLLAPSED, listener);
		this.add(0, wrap(model));
	}

	protected void onCompressedChanged() {
		relayout();
		relayoutParent();
	}

	protected void onAlignmentChanged() {
		relayoutChildren();
		relayout();
		repaint();
	}

	@Override
	protected GLElement wrap(ARankColumnModel model) {
		ITableColumnUI ui = ColumnUIs.createBody(model, false);
		return ui.setData(model.getTable().getData(), this);
	}

	@Override
	protected void takeDown() {
		model.removePropertyChangeListener(StackedRankColumnModel.PROP_ALIGNMENT, listener);
		model.removePropertyChangeListener(ICompressColumnMixin.PROP_COMPRESSED, listener);
		model.removePropertyChangeListener(ICollapseableColumnMixin.PROP_COLLAPSED, listener);
		super.takeDown();
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		IGLLayoutElement elem = children.get(0);
		if (model.isCompressed()) {
			elem.setBounds(0, 0, w, h);
			for (IGLLayoutElement child : children.subList(1, children.size()))
				child.hide();
		} else {
			elem.hide();
			super.doLayout(children, w, h);
		}
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		g.decZ().decZ();
		g.color(RenderStyle.COLOR_STACKED_BORDER).lineWidth(RenderStyle.COLOR_STACKED_BORDER_WIDTH);
		g.drawLine(-1, 0, -1, h).drawLine(w - 1, 0, w - 1, h);
		g.incZ().incZ();
		g.lineWidth(1);
		super.renderImpl(g, w, h);
	}

	@Override
	public void layoutRows(ARankColumnModel model, final List<? extends IGLLayoutElement> children, final float w,
			float h) {
		final int combinedAlign = this.model.getAlignment();
		final int index = this.model.indexOf(model);
		if (combinedAlign >= 0 && index != combinedAlign && index >= 0) {
			// moving around
			final float[] weights = new float[this.model.size()];
			for (int i = 0; i < weights.length; ++i)
				weights[i] = this.model.get(i).getWeight();

			IRow selected = this.model.getTable().getSelectedRow();
			final int selectedIndex = (selected == null ? -1 : selected.getIndex());

			ISetHeight setter = new ISetHeight() {
				@Override
				public void set(int at, float y, float h) {
					IGLLayoutElement row = children.get(at);
					IRow data = row.getLayoutDataAs(IRow.class, null);
					float x = getX(combinedAlign, weights, index, data);
					row.setBounds(x, y, w, h);
					row.asElement().setVisibility(at == selectedIndex ? EVisibility.PICKABLE : EVisibility.VISIBLE);
				}
			};
			getRanker(model).layoutRows(setter);
		} else {
			// simple
			getColumnModelParent().layoutRows(model, children, w, h);
		}
	}

	private float getX(int combinedAlign, float[] weights, int index, IRow data) {
		float x = 0;
		MultiFloat vs = model.getSplittedValue(data);
		if (index < combinedAlign) {
			for (int i = index; i < combinedAlign; ++i)
				x += -vs.values[i] + weights[i] - RenderStyle.COLUMN_SPACE;
			x += RenderStyle.COLUMN_SPACE;
		} else {
			for (int i = combinedAlign; i < index; ++i)
				x += vs.values[i] - weights[i] + RenderStyle.COLUMN_SPACE;
			x += RenderStyle.COLUMN_SPACE;
		}
		return x;
	}

	@Override
	public OrderColumnUI getRanker(ARankColumnModel model) {
		return getColumnModelParent().getRanker(model);
	}

	@Override
	public boolean causesReorderingLayouting() {
		return getColumnModelParent().causesReorderingLayouting();
	}

	@Override
	public VAlign getAlignment(ITableColumnUI model) {
		int combinedAlign = this.model.getAlignment();
		if (combinedAlign < 0)
			return VAlign.LEFT;
		int index = this.model.indexOf(model.getModel());
		return (index >= combinedAlign || index < 0) ? VAlign.LEFT : VAlign.RIGHT;
	}

	@Override
	public boolean hasFreeSpace(ITableColumnUI model) {
		int combinedAlign = this.model.getAlignment();
		if (combinedAlign < 0)
			return true;
		int index = this.model.indexOf(model.getModel());
		if (index < 0)
			return true;
		return index >= combinedAlign ? (index == (this.model.size() - 1)) : (index == 0);
	}
}

