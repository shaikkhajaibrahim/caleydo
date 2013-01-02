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
package org.caleydo.core.data.perspective.variable;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;

/**
 * Implementation of {@link AVariablePerspective} for records.
 *
 * @author Alexander Lex
 */
@XmlRootElement
public class RecordPerspective extends ARecordPerspective {

	public RecordPerspective() {
	}

	public RecordPerspective(ATableBasedDataDomain dataDomain) {
		super(dataDomain);
	}

	@Override
	protected void init() {
		super.init();
		idType = getDataDomain().getRecordIDType();
	}

	@Override
	public ATableBasedDataDomain getDataDomain() {
		return (ATableBasedDataDomain)super.getDataDomain();
	}

	@Override
	protected String getElementLabel(Integer id) {
		return getDataDomain().getRecordLabel(id);
	}

	@Override
	protected List<Integer> getIDList() {
		return getDataDomain().getTable().getRowIDList();
	}
}
