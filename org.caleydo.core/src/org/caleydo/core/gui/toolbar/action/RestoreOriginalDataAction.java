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
package org.caleydo.core.gui.toolbar.action;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.gui.SimpleAction;

public class RestoreOriginalDataAction extends SimpleAction {

	public static final String LABEL = "Restore original data";
	public static final String ICON = "resources/icons/general/restore.png";

	private final String recordPerspectiveID;

	public RestoreOriginalDataAction(String recordPerspectiveID) {
		super(LABEL, ICON);
		this.recordPerspectiveID = recordPerspectiveID;
	}

	@Override
	public void run() {
		super.run();

		for (IDataDomain dataDomain : DataDomainManager.get().getDataDomains()) {
			if (dataDomain instanceof ATableBasedDataDomain)
				((ATableBasedDataDomain) dataDomain).getTable().getDefaultRecordPerspective();
			System.out.println("Reset not implemented" + recordPerspectiveID);
		}
	}
}
