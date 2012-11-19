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
package org.caleydo.view.tourguide.data;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.datadomain.ADataDomain;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.data.datadomain.DataDomainOracle;
import org.caleydo.core.data.perspective.table.CategoricalTablePerspectiveCreator;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.DimensionPerspective;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.execution.SafeCallable;
import org.caleydo.view.tourguide.data.filter.CompositeDataDomainFilter;
import org.caleydo.view.tourguide.data.filter.EmptyGroupFilter;
import org.caleydo.view.tourguide.data.filter.GroupNameFilter;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Samuel Gratzl
 *
 */
public class DataDomainQuery implements SafeCallable<Collection<TablePerspective>>,
		Function<Collection<TablePerspective>, Multimap<TablePerspective, Group>> {
	public static final String PROP_SELECTION = "selection";

	private final static CategoricalTablePerspectiveCreator perspectiveCreator = new CategoricalTablePerspectiveCreator();

	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);

	private Set<ATableBasedDataDomain> selection = new HashSet<>();
	private CompositeDataDomainFilter filter = new CompositeDataDomainFilter();

	public DataDomainQuery() {
		filter.add(new EmptyGroupFilter());
		filter.add(new GroupNameFilter("Not Mutated"));
		filter.add(new GroupNameFilter("Normal"));
	}

	public Collection<ATableBasedDataDomain> getSelection() {
		return Collections.unmodifiableCollection(selection);
	}

	@Override
	public Multimap<TablePerspective, Group> apply(Collection<TablePerspective> stratifications) {
		Multimap<TablePerspective, Group> r = ArrayListMultimap.create();
		for (TablePerspective strat : stratifications) {
			for (Group group : strat.getRecordPerspective().getVirtualArray().getGroupList()) {
				if (!filter.apply(Pair.make(strat, group)))
					continue;
				r.put(strat, group);
			}
		}
		return r;
	}

	@Override
	public Collection<TablePerspective> call() {
		Collection<TablePerspective> stratifications = new ArrayList<TablePerspective>();
		for (ATableBasedDataDomain dataDomain : selection) {
			if (DataDomainOracle.isCategoricalDataDomain(dataDomain)) {
				stratifications.addAll(dataDomain.getAllTablePerspectives());
			} else {
				// Take the first non ungrouped dimension perspective
				String dimensionPerspectiveID = null;
				for (String tmpDimensionPerspectiveID : dataDomain.getDimensionPerspectiveIDs()) {
					DimensionPerspective per = dataDomain.getTable().getDimensionPerspective(tmpDimensionPerspectiveID);
					if (isUngrouped(per))
						continue;
					dimensionPerspectiveID = tmpDimensionPerspectiveID;
				}

				Set<String> rowPerspectiveIDs = dataDomain.getRecordPerspectiveIDs();

				// we ignore stratifications with only one group, which is the ungrouped default
				if (rowPerspectiveIDs.size() == 1)
					continue;

				for (String rowPerspectiveID : rowPerspectiveIDs) {
					boolean existsAlready = dataDomain.hasTablePerspective(rowPerspectiveID, dimensionPerspectiveID);

					TablePerspective newTablePerspective = dataDomain.getTablePerspective(rowPerspectiveID,
							dimensionPerspectiveID);

					// We do not want to overwrite the state of already existing
					// public table perspectives.
					if (!existsAlready)
						newTablePerspective.setPrivate(true);

					stratifications.add(newTablePerspective);
				}
			}
		}
		return stratifications;
	}

	private static boolean isUngrouped(DimensionPerspective per) {
		return per.getLabel().contains("Ungrouped");
	}

	private static boolean filterDataDomain(ATableBasedDataDomain dataDomain) {
		return dataDomain.getLabel().toLowerCase().equals("clinical");
	}

	public static List<ATableBasedDataDomain> allDataDomains() {
		List<ATableBasedDataDomain> dataDomains = new ArrayList<>(DataDomainManager.get().getDataDomainsByType(
				ATableBasedDataDomain.class));

		for (Iterator<ATableBasedDataDomain> it = dataDomains.iterator(); it.hasNext();)
			if (filterDataDomain(it.next()))
				it.remove();

		// Sort data domains alphabetically
		Collections.sort(dataDomains, new Comparator<ADataDomain>() {
			@Override
			public int compare(ADataDomain dd1, ADataDomain dd2) {
				return dd1.getLabel().compareTo(dd2.getLabel());
			}
		});
		return dataDomains;
	}

	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String,
	 *      java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String,
	 *      java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(propertyName, listener);
	}

	public void addSelection(ATableBasedDataDomain dataDomain) {
		if (!selection.add(dataDomain))
			return;
		initDataDomain(dataDomain);
		listeners.fireIndexedPropertyChange(PROP_SELECTION, selection.size() - 1, null, dataDomain);
	}

	private static void initDataDomain(ATableBasedDataDomain dataDomain) {
		if (DataDomainOracle.isCategoricalDataDomain(dataDomain))
			perspectiveCreator.createAllTablePerspectives(dataDomain);
	}

	public void removeSelection(ATableBasedDataDomain dataDomain) {
		if (!selection.remove(dataDomain))
			return;
		listeners.fireIndexedPropertyChange(PROP_SELECTION, selection.size(), dataDomain, null);
	}


}
