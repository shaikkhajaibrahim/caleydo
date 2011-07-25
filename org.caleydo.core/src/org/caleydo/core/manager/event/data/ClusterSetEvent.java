package org.caleydo.core.manager.event.data;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.data.collection.table.DataTable;
import org.caleydo.core.manager.event.AEvent;

/**
 * Event that signals that a given set needs to be clustered.
 * 
 * @author Marc Streit
 */
@XmlRootElement
@XmlType
public class ClusterSetEvent
	extends AEvent {

	private ArrayList<DataTable> sets;

	/**
	 * default no-arg constructor
	 */
	public ClusterSetEvent() {
		// nothing to initialize here
	}

	public ClusterSetEvent(ArrayList<DataTable> sets) {
		this.sets = sets;
	}

	public ArrayList<DataTable> setDataTables() {
		return sets;
	}

	public ArrayList<DataTable> getDataTables() {
		return sets;
	}

	@Override
	public boolean checkIntegrity() {
		if (sets == null || sets.size() == 0)
			return false;
		return true;
	}

}
