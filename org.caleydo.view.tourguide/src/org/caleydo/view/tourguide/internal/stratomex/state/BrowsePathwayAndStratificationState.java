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
package org.caleydo.view.tourguide.internal.stratomex.state;

import javax.media.opengl.GL2;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout.AForwardingRenderer;
import org.caleydo.core.view.opengl.layout.ALayoutRenderer;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.view.stratomex.brick.configurer.PathwayDataConfigurer;
import org.caleydo.view.stratomex.tourguide.event.UpdatePathwayPreviewEvent;
import org.caleydo.view.tourguide.api.state.BrowsePathwayState;
import org.caleydo.view.tourguide.api.state.ISelectReaction;
import org.caleydo.view.tourguide.api.state.ISelectStratificationState;

/**
 * @author Samuel Gratzl
 *
 */
public class BrowsePathwayAndStratificationState extends BrowsePathwayState implements ISelectStratificationState {

	private PathwayGraph pathway;

	public BrowsePathwayAndStratificationState() {
		super("Select a pathway in the Tour Guide and select a strafication to refer to.");
	}

	@Override
	public void onEnter() {

		super.onEnter();
	}

	@Override
	public void onUpdate(UpdatePathwayPreviewEvent event, ISelectReaction adapter) {
		pathway = event.getPathway();
		if (underlying == null) {
			// adapter.replaceTemplate(create)
			// adapter.replaceTemplate();
			// TODO show a custom block with the pathway in it
			// TODO show a custom view with
		} else {
			show(adapter);
		}
	}

	private void show(ISelectReaction adapter) {
		if (underlying == null || pathway == null)
			return;
		TablePerspective tablePerspective = asPerspective(underlying, pathway);
		adapter.replaceTemplate(tablePerspective, new PathwayDataConfigurer());
	}

	@Override
	public boolean apply(TablePerspective tablePerspective) {
		return true;
	}

	@Override
	public void select(TablePerspective tablePerspective, ISelectReaction reactions) {
		setUnderlying(tablePerspective.getRecordPerspective());
		show(reactions);
	}

	private static class PreviewRenderer extends AForwardingRenderer {
		public PreviewRenderer(ALayoutRenderer renderer) {
			super(renderer);
		}
		@Override
		public void setLimits(float x, float y) {
			super.setLimits(x, y);
			currentRenderer.setLimits(x, y);
		}

		@Override
		protected void renderContent(GL2 gl) {
			super.renderContent(gl);
		}
	}
}