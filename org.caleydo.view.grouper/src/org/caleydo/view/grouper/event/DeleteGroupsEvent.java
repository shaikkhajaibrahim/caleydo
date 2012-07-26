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
package org.caleydo.view.grouper.event;

import java.util.Set;
import org.caleydo.core.event.AEvent;

public class DeleteGroupsEvent extends AEvent {

	private Set<Integer> setGroupsToDelete;

	public DeleteGroupsEvent(Set<Integer> setGroupsToDelete) {
		this.setGroupsToDelete = setGroupsToDelete;
	}

	@Override
	public boolean checkIntegrity() {
		return setGroupsToDelete != null;
	}

	public Set<Integer> getGroupsToDelete() {
		return setGroupsToDelete;
	}

	public void setGroupsToDelete(Set<Integer> setGroupsToDelete) {
		this.setGroupsToDelete = setGroupsToDelete;
	}

}