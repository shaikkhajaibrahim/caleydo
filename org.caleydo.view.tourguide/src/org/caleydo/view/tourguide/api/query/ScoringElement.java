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
package org.caleydo.view.tourguide.api.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.ARecordPerspective;
import org.caleydo.core.data.virtualarray.RecordVirtualArray;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.ILabelProvider;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.view.tourguide.api.score.CollapseScore;
import org.caleydo.view.tourguide.internal.compute.CachedIDTypeMapper;
import org.caleydo.view.tourguide.spi.compute.ICompositeScore;
import org.caleydo.view.tourguide.spi.score.IGroupScore;
import org.caleydo.view.tourguide.spi.score.IScore;
import org.caleydo.view.tourguide.spi.score.IStratificationScore;

/**
 * @author Samuel Gratzl
 *
 */
public final class ScoringElement implements ILabelProvider {
	private final TablePerspective perspective;
	private final ARecordPerspective stratification;
	private final Group group;
	/**
	 * collapse scores have different scores depending on the current scoring element, this map stores their selections
	 */
	private final Map<IScore, IScore> collapseSelections;

	public ScoringElement(ARecordPerspective stratification, TablePerspective perspective,
			Map<IScore, IScore> collapseSelections) {
		this(stratification, null, perspective, collapseSelections);
	}

	public ScoringElement(ARecordPerspective stratification, Group group, TablePerspective perspective,
			Map<IScore, IScore> collapseSelections) {
		this.stratification = stratification;
		this.perspective = perspective;
		this.group = group;
		this.collapseSelections = collapseSelections;
	}

	public IScore getSelected(CollapseScore collapseScore) {
		return collapseSelections == null ? null : collapseSelections.get(collapseScore);
	}

	@Override
	public String getLabel() {
		String label = stratification.getLabel();
		if (group != null)
			label += ": " + group.getLabel();
		return label;
	}

	@Override
	public String getProviderName() {
		return stratification.getProviderName();
	}

	public IDataDomain getDataDomain() {
		return stratification.getDataDomain();
	}

	public ARecordPerspective getStratification() {
		return stratification;
	}

	public TablePerspective getPerspective() {
		return perspective;
	}

	public Group getGroup() {
		return group;
	}

	/**
	 * returns the list of row ids that intersects all the relevant visible columns based on this stratifaction and
	 * group
	 *
	 * @param pair
	 *            containing the ids and the type in which the ids are
	 * @return
	 */
	public Pair<Collection<Integer>, IDType> getIntersection(Collection<IScore> visibleColumns) {
		// select nearest score
		Collection<IStratificationScore> relevant = filterRelevantColumns(visibleColumns);

		IDType target = stratification.getIdType();
		for (IStratificationScore elem : relevant) {
			IDType type = elem.getStratification().getIdType();
			if (!target.getIDCategory().equals(type.getIDCategory()))
				continue;
			if (!target.equals(type))
				target = target.getIDCategory().getPrimaryMappingType();
		}

		CachedIDTypeMapper mapper = new CachedIDTypeMapper();

		// compute the intersection of all
		IDType source = stratification.getIdType();

		RecordVirtualArray va = stratification.getVirtualArray();
		Collection<Integer> ids = (group == null) ? va.getIDs() : va.getIDsOfGroup(group.getGroupIndex());

		if (!relevant.isEmpty()) {
			Collection<Integer> intersection = new ArrayList<>(mapper.get(source, target).apply(ids));
			for (IStratificationScore score : relevant) {
				va = score.getStratification().getVirtualArray();
				Group g = (score instanceof IGroupScore) ? ((IGroupScore) score).getGroup() : null;
				ids = (g == null) ? va.getIDs() : va.getIDsOfGroup(g.getGroupIndex());
				Set<Integer> mapped = mapper.get(score.getStratification().getIdType(), target)
						.apply(ids);
				for (Iterator<Integer> it = intersection.iterator(); it.hasNext();) {
					if (!mapped.contains(it.next())) // not part of
						it.remove();
				}
			}
			ids = intersection;
		}
		return Pair.make(ids, target);
	}

	private Set<IStratificationScore> filterRelevantColumns(Collection<IScore> columns) {
		Set<IStratificationScore> relevant = new HashSet<>();
		for (IScore score : columns) {
			if (score instanceof CollapseScore)
				score = getSelected((CollapseScore) score);
			if (score instanceof IStratificationScore)
				relevant.add((IStratificationScore) score);
			if (score instanceof ICompositeScore) {
				relevant.addAll(filterRelevantColumns(((ICompositeScore) score).getChildren()));
			}
		}
		return relevant;
	}
}
