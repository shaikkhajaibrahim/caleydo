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
package org.caleydo.vis.rank.ui.detail;

import java.awt.Color;

import javax.media.opengl.GL2;

import org.caleydo.core.util.format.Formatter;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.vis.rank.model.IRow;
import org.caleydo.vis.rank.model.mixin.IMappedColumnMixin;
import org.caleydo.vis.rank.model.mixin.IRankColumnModel;
import org.caleydo.vis.rank.model.mixin.IRankableColumnMixin;
import org.caleydo.vis.rank.ui.IColumnRenderInfo;

/**
 * a simple {@link IGLRenderer} for rendering a score bar
 *
 * @author Samuel Gratzl
 *
 */
public class ScoreBarElement extends ValueElement {
	protected final IRankableColumnMixin model;

	public ScoreBarElement(IRankableColumnMixin model) {
		this.model = model;
		setVisibility(EVisibility.VISIBLE);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		final IRow r = getLayoutDataAs(IRow.class, null); // current row
		float v = model.applyPrimitive(r);
		boolean inferred = model.isValueInferred(r);
		renderValue(g, w, h, r, v, inferred, false, model.getColor(), null);
	}

	@Override
	protected String getTooltip() {
		final IRow r = getLayoutDataAs(IRow.class, null); // current row
		float v = model.applyPrimitive(r);
		if (Float.isNaN(v) || v < 0)
			return null;
		boolean inferred = model.isValueInferred(r);
		return getText(r, model, v, inferred);
	}

	protected void renderValue(GLGraphics g, float w, float h, final IRow r, float v, boolean inferred, boolean align,
			Color color, Color collapseColor) {
		if (Float.isNaN(v) || v <= 0)
			return;
		if (getRenderInfo().isCollapsed()) {
			// if collapsed use a brightness encoding
			if (collapseColor == null)
				g.color(1 - v, 1 - v, 1 - v, 1);
			else {
				float[] rgb = collapseColor.getColorComponents(null);
				g.color(rgb[0], rgb[1], rgb[2], v);
			}
			g.fillRect(0, 1, w - 2, h - 2);
			if (inferred) {
				g.gl.glLineStipple(4, (short) 0xAAAA);
				g.gl.glEnable(GL2.GL_LINE_STIPPLE);
				g.color(0, 0, 0, .5f).drawRect(1, 2, w - 4, h - 4);
				g.gl.glDisable(GL2.GL_LINE_STIPPLE);
			}
		} else {
			// score bar
			g.color(color).fillRect(0, 1, w * v, h - 2);
			if (inferred) {
				g.gl.glLineStipple(1, (short) 0xAAAA);
				g.gl.glEnable(GL2.GL_LINE_STIPPLE);
				g.color(0, 0, 0, .5f).drawRect(1, h * 0.1f + 1, w * v - 1, h * 0.8f - 2);
				g.gl.glDisable(GL2.GL_LINE_STIPPLE);
			}

			if (model.getTable().getSelectedRow() == r) { // is selected, render the value
				renderText(g, w, h, r, v, inferred);
			}
		}
	}

	protected void renderText(GLGraphics g, float w, float h, final IRow r, float v, boolean inferred) {
		String text = getText(r, model, v, inferred);
		float hi = getTextHeight(h);
		renderLabel(g, (h - hi) * 0.5f, w, hi, text, v);
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		if (getVisibility() != EVisibility.PICKABLE)
			return;
		IColumnRenderInfo renderInfo = getRenderInfo();
		if ((renderInfo.hasFreeSpace() && renderInfo.getAlignment() == VAlign.LEFT)) {
			g.fillRect(0, 0, w, h);
		} else {
			final IRow r = getLayoutDataAs(IRow.class, null); // current row
			float v = model.applyPrimitive(r);
			if (!Float.isNaN(v) && v > 0)
				g.fillRect(0, 0, w * v, h);
		}
	}

	protected static String getText(final IRow r, IRankColumnModel model, float v, boolean inferred) {
		String text = (model instanceof IMappedColumnMixin) ? ((IMappedColumnMixin) model).getRawValue(r) : Formatter
				.formatNumber(v);
		return text + (inferred ? "*" : "");
	}

	static float getTextHeight(float h) {
		float hi = Math.min(h * 0.45f, 12);
		return hi;
	}

	protected final IColumnRenderInfo getRenderInfo() {
		return (IColumnRenderInfo) getParent();
	}

	protected void renderLabel(GLGraphics g, float y, float w, float h, String text, float v) {
		if (h < 5)
			return;
		float tw = g.text.getTextWidth(text, h);
		boolean hasFreeSpace = getRenderInfo().hasFreeSpace();

		VAlign alignment = getRenderInfo().getAlignment();
		float space = (hasFreeSpace && alignment == VAlign.LEFT) ? w : (v * w) - 2;
		if (tw < space)
			g.drawText(text, 2, y, space, h, VAlign.LEFT);
	}
}