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
package org.caleydo.view.tourguide.internal.external;

import java.util.Map;

import org.caleydo.view.tourguide.internal.score.ExternalGroupLabelScore;

public class ExternalGroupLabelScoreParser extends AExternalScoreParser<GroupLabelParseSpecification, String> {

	public ExternalGroupLabelScoreParser(GroupLabelParseSpecification spec) {
		super(spec);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.caleydo.view.tourguide.data.load.AExternalScoreParser#extractID(java.lang.String)
	 */
	@Override
	protected String extractID(String originalID) {
		return originalID;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.caleydo.view.tourguide.data.load.AExternalScoreParser#addScore(java.lang.String, boolean, java.util.Map)
	 */
	@Override
	protected ExternalGroupLabelScore createScore(String label, boolean isRank, Map<String, Float> scores) {
		return new ExternalGroupLabelScore(label, spec.getPerspectiveKey(), isRank, scores);
	}

}