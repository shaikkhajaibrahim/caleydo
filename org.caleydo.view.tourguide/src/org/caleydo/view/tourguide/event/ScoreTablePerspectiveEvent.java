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
package org.caleydo.view.tourguide.event;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.AEvent;

/**
 * Event for opening the vending machine within Stratomex with a specified
 * reference table perspective and the brick column.
 *
 * @author Marc Streit
 *
 */
public class ScoreTablePerspectiveEvent
	extends AEvent {

	/**
	 * Table perspectives that will be used for the scoring. either a single group or a stratification depending on the
	 * type
	 */
	private TablePerspective group;

	private EScoreReferenceMode scoreReferenceMode;

	private TablePerspective stratification;

	public ScoreTablePerspectiveEvent() {

	}

	public ScoreTablePerspectiveEvent(EScoreReferenceMode scoreReferenceMode, TablePerspective stratification) {
		this(scoreReferenceMode, stratification, null);
	}

	public ScoreTablePerspectiveEvent(EScoreReferenceMode scoreReferenceMode, TablePerspective stratification,
			TablePerspective group) {
		this.scoreReferenceMode = scoreReferenceMode;
		this.group = group;
		this.stratification = stratification;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

	public TablePerspective getGroup() {
		return group;
	}

	/**
	 * @return the stratification, see {@link #stratification}
	 */
	public TablePerspective getStratification() {
		return stratification;
	}

	/**
	 * @param scoreReferenceMode setter, see {@link #scoreReferenceMode}
	 */
	public void setScoreReferenceMode(EScoreReferenceMode scoreReferenceMode) {
		this.scoreReferenceMode = scoreReferenceMode;
	}

	/**
	 * @return the scoreReferenceMode, see {@link #scoreReferenceMode}
	 */
	public EScoreReferenceMode getScoreReferenceMode() {
		return scoreReferenceMode;
	}
}
