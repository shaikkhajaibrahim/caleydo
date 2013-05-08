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
package org.caleydo.view.tourguide.internal.view.specific;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.tourguide.api.query.EDataDomainQueryMode;
import org.caleydo.view.tourguide.internal.model.ADataDomainQuery;
import org.caleydo.view.tourguide.internal.model.AScoreRow;
import org.caleydo.view.tourguide.internal.model.CategoricalDataDomainQuery;
import org.caleydo.view.tourguide.internal.model.StratificationDataDomainQuery;
import org.caleydo.view.tourguide.internal.view.col.DataDomainRankColumnModel;
import org.caleydo.view.tourguide.internal.view.col.IAddToStratomex;
import org.caleydo.view.tourguide.internal.view.col.SizeRankColumnModel;
import org.caleydo.vis.rank.model.IRow;
import org.caleydo.vis.rank.model.RankTableModel;
import org.caleydo.vis.rank.model.StringRankColumnModel;

import com.google.common.base.Function;

/**
 * @author Samuel Gratzl
 *
 */
public class NumericalSpecifics implements IDataDomainQueryModeSpecfics {

	@Override
	public Iterable<? extends ADataDomainQuery> createDataDomainQueries() {
		List<ADataDomainQuery> r = new ArrayList<>();
		for (IDataDomain dd : EDataDomainQueryMode.NUMERICAL.getAllDataDomains()) {
			r.add(createFor(dd));
		}
		return r;
	}

	@Override
	public void addDefaultColumns(RankTableModel table, IAddToStratomex add2Stratomex) {
		table.add(new DataDomainRankColumnModel(add2Stratomex).setWidth(80).setCollapsed(true));
		final StringRankColumnModel base = new StringRankColumnModel(GLRenderers.drawText("Name"),
				StringRankColumnModel.DEFAULT);
		table.add(base);
		base.setWidth(150);
		base.orderByMe();
		table.add(new SizeRankColumnModel("#Elements", new Function<IRow, Integer>() {
			@Override
			public Integer apply(IRow in) {
				return ((AScoreRow) in).size();
			}
		}).setWidth(75));
	}

	@Override
	public Iterable<? extends ADataDomainQuery> createDataDomainQuery(IDataDomain dd) {
		return Collections.singleton(createFor(dd));
	}

	private static ADataDomainQuery createFor(IDataDomain dd) {
		if (DataSupportDefinitions.categoricalTables.apply(dd))
			return new CategoricalDataDomainQuery((ATableBasedDataDomain) dd);
		return new StratificationDataDomainQuery((ATableBasedDataDomain) dd);
	}
}
