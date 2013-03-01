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
package org.caleydo.vis.rank.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.caleydo.vis.rank.config.IRankTableConfig;
import org.caleydo.vis.rank.model.mixin.IFilterColumnMixin;
import org.caleydo.vis.rank.model.mixin.IMappedColumnMixin;
import org.caleydo.vis.rank.model.mixin.IRankColumnModel;
import org.caleydo.vis.rank.model.mixin.IRankableColumnMixin;

import com.google.common.collect.Iterators;
import com.jogamp.common.util.IntIntHashMap;

/**
 * basic model abstraction of a ranked list
 *
 * @author Samuel Gratzl
 *
 */
public class RankTableModel implements Iterable<IRow>, IRankColumnParent {
	public static final String PROP_SELECTED_ROW = "selectedRow";
	public static final String PROP_ORDER = "order";
	public static final String PROP_DATA = "data";
	public static final String PROP_COLUMNS = "columns";
	public static final String PROP_POOL = "pool";
	public static final String PROP_INVALID = "invalid";
	public static final String PROP_REGISTER = "register";

	private final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

	/**
	 * current visible columns
	 */
	private List<ARankColumnModel> columns = new ArrayList<>();
	/**
	 * current hidden columns
	 */
	private List<ARankColumnModel> pool = new ArrayList<>(2);

	private final PropertyChangeListener resort = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			dirtyOrder = true;
			fireInvalid();
		}
	};
	private final PropertyChangeListener refilter = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			dirtyFilter = true;
			fireInvalid();
		}
	};

	/**
	 * settings
	 */
	private final IRankTableConfig config;
	/**
	 * the data of this table, not data can only be ADDED not removed, if you want to disable the use the
	 * {@link #dataMask}
	 */
	private final List<IRow> data = new ArrayList<>();
	/**
	 * mask selecting a subset of the data
	 */
	private BitSet dataMask;

	private int selectedRank = -1;
	private IRow selectedRow = null;

	private boolean dirtyFilter = true;
	/**
	 * current filter to select data
	 */
	private BitSet filter;

	private boolean dirtyOrder = true;
	/**
	 * currently column used for ordering
	 */
	private IRankableColumnMixin orderBy;
	/**
	 * current order
	 *
	 * <pre>
	 * order[i] = data index
	 * </pre>
	 */
	private int[] order;
	private IntIntHashMap exaequoOffsets = new IntIntHashMap();

	/**
	 *
	 */
	public RankTableModel(IRankTableConfig config) {
		this.config = config;
	}

	/**
	 * adds a collection of new data items to this table
	 *
	 * @param rows
	 */
	public void addData(Collection<? extends IRow> rows) {
		if (rows == null || rows.isEmpty())
			return;
		int s = this.data.size();
		for (IRow r : rows)
			r.setIndex(s++);
		this.data.addAll(rows);
		propertySupport.fireIndexedPropertyChange(PROP_DATA, s, null, rows);
		dirtyFilter = true;
		fireInvalid();
	}

	protected void fireInvalid() {
		propertySupport.firePropertyChange(PROP_INVALID, false, true);
	}

	/**
	 * sets the data mask to filter the {@link #data}
	 *
	 * @param dataMask
	 */
	public void setDataMask(BitSet dataMask) {
		if (Objects.equals(dataMask, this.dataMask))
			return;
		boolean change = true;
		if (this.dataMask != null && dataMask != null) {
			this.dataMask.xor(dataMask);
			if (getDataSize() < this.dataMask.size())
				this.dataMask.clear(getDataSize(), this.dataMask.size());
			change = !this.dataMask.isEmpty(); // same data subset
		}
		this.dataMask = (BitSet) dataMask.clone();
		if (change) {
			dirtyFilter = true;
			fireInvalid();
		}
	}

	/**
	 * add and registered a new column to this table
	 *
	 * @param col
	 */
	public void addColumn(ARankColumnModel col) {
		setup(col);
		add(col);
	}

	/**
	 * see {@link #addColumn(ARankColumnModel)} but append the column to the given parent
	 *
	 * @param parent
	 * @param col
	 */
	public void addColumnTo(ACompositeRankColumnModel parent, ARankColumnModel col) {
		setup(col);
		parent.add(col);
	}

	private void add(ARankColumnModel col) {
		add(columns.size(), col);
	}

	private void add(int index, ARankColumnModel col) {
		col.setParent(this);
		this.columns.add(index, col); // intelligent positioning
		propertySupport.fireIndexedPropertyChange(PROP_COLUMNS, index, null, col);
		checkOrderChanges(index, col);
	}

	/**
	 * are the current changes, e.g. moving triggers changes in the filtering or ordering?
	 *
	 * @param index
	 * @param col
	 */
	private void checkOrderChanges(int index, ARankColumnModel col) {
		if (col instanceof IFilterColumnMixin && ((IFilterColumnMixin) col).isFiltered()) { // filter elements changed
			dirtyFilter = true;
			fireInvalid();
			return;
		}
		if (findFirstRankable() != orderBy) { // order by changed
			dirtyOrder = true;
			fireInvalid();
			return;
		}

	}

	@Override
	public final void move(ARankColumnModel model, int to) {
		int from = this.columns.indexOf(model);
		if (model.getParent() == this && from >= 0) { // move within the same parent
			if (from == to)
				return;
			columns.add(to, model);
			columns.remove(from < to ? from : from + 1);
			propertySupport.fireIndexedPropertyChange(PROP_COLUMNS, to, from, model);
			checkOrderChanges(to, model);
		} else {
			model.getParent().detach(model);
			add(to, model);
		}
	}

	@Override
	public boolean isMoveAble(ARankColumnModel model, int index) {
		return config.isMoveAble(model) && model.getParent().isHideAble(model);
	}

	@Override
	public void replace(ARankColumnModel from, ARankColumnModel to) {
		int i = this.columns.indexOf(from);
		columns.set(i, to);
		to.setParent(this);
		propertySupport.fireIndexedPropertyChange(PROP_COLUMNS, i, from, to);
		from.setParent(null);
		checkOrderChanges(i, to);
	}

	@Override
	public void detach(ARankColumnModel model) {
		remove(model);
		removeFromPool(model);
		checkOrderChanges(-1, model);
	}

	/**
	 * @return
	 */
	public ACompositeRankColumnModel createCombined() {
		ACompositeRankColumnModel new_ = config.createNewCombined();
		setup(new_);
		return new_;
	}


	public boolean isCombineAble(ARankColumnModel model, ARankColumnModel with) {
		if (model == with)
			return false;
		if (model.getParent() == with || with.getParent() == model) // already children
			return false;
		if (!with.getParent().isHideAble(with)) // b must be hide able
			return false;
		return config.isCombineAble(model, with);
	}

	private void setup(ARankColumnModel col) {
		col.init(this);
		if (col instanceof StackedRankColumnModel)
			col.addPropertyChangeListener(ARankColumnModel.PROP_WEIGHT, resort);
		col.addPropertyChangeListener(IMappedColumnMixin.PROP_MAPPING, refilter);
		col.addPropertyChangeListener(IFilterColumnMixin.PROP_FILTER, refilter);
		if (col instanceof ACompositeRankColumnModel) {
			for (ARankColumnModel child : ((ACompositeRankColumnModel) col))
				setup(child);
		}
		propertySupport.firePropertyChange(PROP_REGISTER, null, col);
	}

	private void takeDown(ARankColumnModel col) {
		col.takeDown(this);
		col.removePropertyChangeListener(ARankColumnModel.PROP_WEIGHT, resort);
		col.removePropertyChangeListener(IMappedColumnMixin.PROP_MAPPING, refilter);
		col.removePropertyChangeListener(IFilterColumnMixin.PROP_FILTER, refilter);
		if (col instanceof ACompositeRankColumnModel) {
			for (ARankColumnModel child : ((ACompositeRankColumnModel) col))
				takeDown(child);
		}
		propertySupport.firePropertyChange(PROP_REGISTER, col, null);
	}

	private void remove(ARankColumnModel model) {
		int index = columns.indexOf(model);
		if (index < 0)
			return;
		columns.remove(model);
		propertySupport.fireIndexedPropertyChange(PROP_COLUMNS, index, model, null);
		model.setParent(null);
		checkOrderChanges(index, model);
	}

	public boolean destroy(ARankColumnModel col) {
		removeFromPool(col);
		takeDown(col);
		return true;
	}

	/**
	 * @param model
	 */
	void addToPool(ARankColumnModel model) {
		int bak = pool.size();
		this.pool.add(model);
		model.setParent(this);
		ARankColumnModel.uncollapse(model);
		propertySupport.fireIndexedPropertyChange(PROP_POOL, bak, null, model);
		checkOrderChanges(-1, model);
	}

	void removeFromPool(ARankColumnModel model) {
		int index = pool.indexOf(model);
		if (index < 0)
			return;
		pool.remove(index);
		propertySupport.fireIndexedPropertyChange(PROP_POOL, index, model, null);
		model.setParent(null);
	}

	@Override
	public boolean hide(ARankColumnModel model) {
		remove(model);
		if (config.isDestroyOnHide()) {
			destroy(model);
		} else
			addToPool(model);
		return true;
	}

	@Override
	public boolean isDestroyAble(ARankColumnModel model) {
		return pool.contains(model); // just elements in the pool
	}

	@Override
	public final boolean isCollapseAble(ARankColumnModel model) {
		return config.isDefaultCollapseAble();
	}

	@Override
	public boolean isHideAble(ARankColumnModel model) {
		return config.isDefaultHideAble();
	}

	@Override
	public RankTableModel getTable() {
		return this;
	}

	@Override
	public void explode(ACompositeRankColumnModel model) {
		int index = this.columns.indexOf(model);
		List<ARankColumnModel> children = model.getChildren();
		for (ARankColumnModel child : children)
			child.setParent(this);
		getTable().destroy(model);
		this.columns.set(index, children.get(0));
		propertySupport.fireIndexedPropertyChange(PROP_COLUMNS, index, model, children.get(0));
		if (children.size() > 1) {
			this.columns.addAll(index + 1, children.subList(1, children.size()));
			propertySupport.fireIndexedPropertyChange(PROP_COLUMNS, index + 1, null,
					children.subList(1, children.size()));
		}
		checkOrderChanges(index, children.get(0));
	}

	private IRankableColumnMixin findFirstRankable() {
		for (ARankColumnModel col : this.columns) {
			if (col instanceof IRankableColumnMixin)
				return (IRankableColumnMixin) col;
		}
		return null;
	}

	/**
	 * @return the columns, see {@link #columns}
	 */
	public List<ARankColumnModel> getColumns() {
		return Collections.unmodifiableList(columns);
	}

	/**
	 * @return the pool, see {@link #pool}
	 */
	public List<ARankColumnModel> getPool() {
		return Collections.unmodifiableList(pool);
	}

	/**
	 * finds all columns, by flatten combined columns
	 *
	 * @return
	 */
	public Iterator<ARankColumnModel> findAllColumns() {
		Collection<ARankColumnModel> c = new ArrayList<>();
		findAllColumns(c, this.columns);
		return c.iterator();
	}

	private void findAllColumns(Collection<ARankColumnModel> c, Iterable<ARankColumnModel> cols) {
		for (ARankColumnModel col : cols) {
			if (col instanceof ACompositeRankColumnModel) {
				findAllColumns(c, (ACompositeRankColumnModel) col);
			} else
				c.add(col);
		}
	}

	private Iterator<IFilterColumnMixin> findAllFiltered() {
		return Iterators.filter(findAllColumns(), IFilterColumnMixin.class);
	}


	public int size() {
		if (!dirtyOrder && order != null)
			return order.length;
		if (!dirtyFilter && filter != null)
			return filter.cardinality();
		checkOrder();
		return order.length;
	}

	public BitSet getFilter() {
		filter();
		return filter;
	}

	/**
	 * performs filtering
	 */
	private void filter() {
		if (!dirtyFilter)
			return;
		dirtyFilter = false;
		// System.out.println("filter");
		// start with data mask
		if (dataMask != null)
			filter = (BitSet) dataMask.clone();
		else {
			filter = new BitSet(data.size());
			filter.set(0, data.size());
		}

		for (Iterator<IFilterColumnMixin> it = findAllFiltered(); it.hasNext();) {
			it.next().filter(data, filter);
		}

		// selected not visible anymore?
		if (selectedRow != null && !filter.get(selectedRow.getIndex()))
			setSelectedRow(-1);

		dirtyOrder = true;
		order();
	}

	private void checkOrder() {
		if (dirtyFilter) {
			filter();
		} else
			order();
	}

	/**
	 * sorts the current data
	 */
	private void order() {
		if (!dirtyOrder)
			return;
		dirtyOrder = false;
		// System.out.println("sort");
		int[] bak = order;

		// by what
		orderBy = findFirstRankable();
		exaequoOffsets.clear();
		if (orderBy == null) {
			int rank = 0; // natural order
			for (int i = 0; i < data.size(); ++i) {
				if (!filter.get(i)) {
					data.get(i).setRank(-1);
				} else {
					data.get(i).setRank(rank);
					exaequoOffsets.put(rank, -rank);
					rank++;
				}
			}
			order = new int[rank];
			for(int i = 0; i < order.length; ++i)
				order[i] = i;
		} else {
			List<IntFloat> tmp = new ArrayList<>(data.size());
			for (int i = 0; i < data.size(); ++i) {
				if (!filter.get(i)) {
					data.get(i).setRank(-1);
				} else {
					tmp.add(new IntFloat(i, orderBy.getValue(data.get(i))));
				}
			}
			Collections.sort(tmp);

			order = new int[tmp.size()];
			int offset = 0;
			float last = Float.NaN;
			for (int i = 0; i < tmp.size(); ++i) {
				IntFloat pair = tmp.get(i);
				order[i] = pair.id;
				data.get(order[i]).setRank(i);
				if (last == pair.value) {
					offset++;
					exaequoOffsets.put(i, offset);
				} else {
					offset = 0;
				}
				last = pair.value;
			}
		}
		if (!Arrays.equals(bak, order))
			propertySupport.firePropertyChange(PROP_ORDER, bak, order);
	}

	private static class IntFloat implements Comparable<IntFloat> {
		private final int id;
		private final float value;

		public IntFloat(int id, float value) {
			this.id = id;
			this.value = value;
		}

		@Override
		public int compareTo(IntFloat o) {
			return -Float.compare(value, o.value);
		}
	}

	/**
	 * @param selectedRow
	 *            setter, see {@link selectedRow}
	 */
	public void setSelectedRow(int selectedRank) {
		checkOrder();
		if (selectedRank >= order.length)
			selectedRank = order.length - 1;
		else if (selectedRank < 0) {
			selectedRank = -1;
		}
		if (this.selectedRank == selectedRank)
			return;
		this.selectedRank = selectedRank;
		propertySupport.firePropertyChange(PROP_SELECTED_ROW, this.selectedRow, this.selectedRow = get(selectedRank));
	}

	/**
	 * returns the element at the given rank
	 *
	 * @param rank
	 * @return
	 */
	public IRow get(int rank) {
		if (rank < 0)
			return null;
		checkOrder();
		return data.get(order[rank]);
	}

	public int getVisualRank(IRow row) {
		int r = row.getRank();
		if (r < 0)
			return -1;
		if (exaequoOffsets.containsKey(r)) {
			return r - exaequoOffsets.get(r) + 1;
		}
		return r + 1;
	}

	public void selectNextRow() {
		if (selectedRow == null)
			setSelectedRow(0);
		else
			setSelectedRow(selectedRow.getRank() + 1);
	}

	public void selectPreviousRow() {
		if (selectedRow == null)
			return;
		setSelectedRow(selectedRow.getRank() - 1);
	}

	/**
	 * @return the selectedRow, see {@link #selectedRow}
	 */
	public IRow getSelectedRow() {
		return selectedRow;
	}

	public final void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}

	public final void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(propertyName, listener);
	}

	public final void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}

	public final void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(propertyName, listener);
	}

	public List<IRow> getData() {
		return Collections.unmodifiableList(this.data);
	}

	public int getDataSize() {
		return this.data.size();
	}

	/**
	 * @return
	 */
	public int[] getOrder() {
		checkOrder();
		return order;
	}

	@Override
	public Iterator<IRow> iterator() {
		checkOrder();
		return new Iterator<IRow>() {
			int cursor = 0;

			@Override
			public boolean hasNext() {
				return cursor < order.length;
			}

			@Override
			public IRow next() {
				return data.get(order[cursor++]);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * @return the config, see {@link #config}
	 */
	public IRankTableConfig getConfig() {
		return config;
	}

	/**
	 * @return
	 */
	public int getSelectedRank() {
		return selectedRank;
	}

	@Override
	public IRankColumnModel getParent() {
		return null;
	}
}
