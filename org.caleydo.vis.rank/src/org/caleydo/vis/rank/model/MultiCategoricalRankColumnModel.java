/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.vis.rank.model;

import gleem.linalg.Vec2f;

import java.beans.PropertyChangeListener;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.ISWTLayer.ISWTLayerRunnable;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.vis.rank.internal.event.FilterEvent;
import org.caleydo.vis.rank.internal.ui.CatFilterDalog;
import org.caleydo.vis.rank.model.mixin.IFilterColumnMixin;
import org.caleydo.vis.rank.model.mixin.IGrabRemainingHorizontalSpace;
import org.caleydo.vis.rank.model.mixin.IRankableColumnMixin;
import org.caleydo.vis.rank.ui.GLPropertyChangeListeners;
import org.caleydo.vis.rank.ui.IColumnRenderInfo;
import org.caleydo.vis.rank.ui.detail.ValueElement;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multiset;

/**
 * multiple categories for one element model
 *
 * @author Samuel Gratzl
 *
 */
public class MultiCategoricalRankColumnModel<CATEGORY_TYPE extends Comparable<CATEGORY_TYPE>> extends
		ABasicFilterableRankColumnModel implements IFilterColumnMixin, IGrabRemainingHorizontalSpace,
		IRankableColumnMixin {
	private final Function<IRow, Set<CATEGORY_TYPE>> data;
	private final Set<CATEGORY_TYPE> selection = new HashSet<>();
	private final Map<CATEGORY_TYPE, String> metaData;

	public MultiCategoricalRankColumnModel(IGLRenderer header, final Function<IRow, Set<CATEGORY_TYPE>> data,
			Map<CATEGORY_TYPE, String> metaData) {
		this(header, data, metaData, Color.GRAY, new Color(.95f, .95f, .95f));
	}

	public static MultiCategoricalRankColumnModel<String> createSimple(IGLRenderer header,
			final Function<IRow, Set<String>> data, Collection<String> items) {
		Map<String, String> map = new TreeMap<>();
		for (String s : items)
			map.put(s, s);
		return new MultiCategoricalRankColumnModel<>(header, data, map);
	}

	public MultiCategoricalRankColumnModel(IGLRenderer header, final Function<IRow, Set<CATEGORY_TYPE>> data,
			Map<CATEGORY_TYPE, String> metaData, Color color, Color bgColor) {
		super(color, bgColor);
		setHeaderRenderer(header);
		this.data = data;
		this.metaData = metaData;
		this.selection.addAll(metaData.keySet());
	}

	public MultiCategoricalRankColumnModel(MultiCategoricalRankColumnModel<CATEGORY_TYPE> copy) {
		super(copy);
		setHeaderRenderer(getHeaderRenderer());
		this.data = copy.data;
		this.metaData = copy.metaData;
		this.selection.addAll(copy.selection);
	}

	@Override
	public MultiCategoricalRankColumnModel<CATEGORY_TYPE> clone() {
		return new MultiCategoricalRankColumnModel<>(this);
	}

	public Map<CATEGORY_TYPE, String> getMetaData() {
		return metaData;
	}

	@Override
	public String getValue(IRow row) {
		Set<CATEGORY_TYPE> value = getCatValue(row);
		if (value == null || value.isEmpty())
			return "";
		return StringUtils.join(Iterators.transform(value.iterator(), Functions.forMap(metaData, null)), ',');
	}

	@Override
	public GLElement createSummary(boolean interactive) {
		return new MyElement(interactive);
	}

	@Override
	public ValueElement createValue() {
		return new MyValueElement();
	}

	@Override
	public final void editFilter(final GLElement summary, IGLElementContext context) {
		final Vec2f location = summary.getAbsoluteLocation();
		context.getSWTLayer().run(new ISWTLayerRunnable() {
			@Override
			public void run(Display display, Composite canvas) {
				Point loc = canvas.toDisplay((int) location.x(), (int) location.y());
				CatFilterDalog<CATEGORY_TYPE> dialog = new CatFilterDalog<>(canvas.getShell(), getTitle(), summary,
						metaData, selection, isGlobalFilter, getTable().hasSnapshots(), loc);
				dialog.open();
			}
		});
	}

	protected void setFilter(Collection<CATEGORY_TYPE> filter, boolean isGlobalFilter) {
		invalidAllFilter();
		Set<CATEGORY_TYPE> bak = new HashSet<>(this.selection);
		this.selection.clear();
		this.selection.addAll(filter);
		if (this.selection.equals(bak)) {
			setGlobalFilter(isGlobalFilter);
		} else {
			this.isGlobalFilter = isGlobalFilter;
			propertySupport.firePropertyChange(PROP_FILTER, bak, this.selection);
		}
	}

	@Override
	public boolean isFiltered() {
		return selection.size() < metaData.size();
	}

	public Set<CATEGORY_TYPE> getCatValue(IRow row) {
		return data.apply(row);
	}

	@Override
	public int compare(IRow o1, IRow o2) {
		Set<CATEGORY_TYPE> t1 = getCatValue(o1);
		Set<CATEGORY_TYPE> t2 = getCatValue(o2);
		if (Objects.equal(t1, t2))
			return 0;
		if ((t1 != null) != (t2 != null))
			return t1 == null ? 1 : -1;
		assert t1 != null && t2 != null;
		// idea: as comparable sort their values, decreasing and compare them
		Iterator<CATEGORY_TYPE> ita = new TreeSet<>(t1).iterator();
		Iterator<CATEGORY_TYPE> itb = new TreeSet<>(t2).iterator();
		int c;
		while (ita.hasNext() && itb.hasNext()) {
			CATEGORY_TYPE a = ita.next();
			CATEGORY_TYPE b = itb.next();
			if ((c = a.compareTo(b)) != 0)
				return c;
		}
		// one is maybe longer than the other
		if (ita.hasNext() == itb.hasNext())
			return 0;
		return ita.hasNext() ? 1 : -1; // the longer the bigger
	}

	@Override
	public void orderByMe() {
		parent.orderBy(this);
	}

	@Override
	protected void updateMask(BitSet todo, List<IRow> data, BitSet mask) {
		for (int i = todo.nextSetBit(0); i >= 0; i = todo.nextSetBit(i + 1)) {
			Set<CATEGORY_TYPE> v = this.data.apply(data.get(i));
			mask.set(i, v == null ? false : Iterables.any(v, Predicates.in(selection)));
		}
	}

	/**
	 * @return
	 */
	public Multiset<CATEGORY_TYPE> getHist() {
		Multiset<CATEGORY_TYPE> hist = HashMultiset.create(metaData.size());
		for (IRow r : getMyRanker()) {
			Set<CATEGORY_TYPE> vs = getCatValue(r);
			if (vs == null) // TODO nan
				continue;
			hist.addAll(vs);
		}
		return hist;
	}

	private class MyElement extends GLElement {
		private final PropertyChangeListener repaintListner = GLPropertyChangeListeners.repaintOnEvent(this);

		public MyElement(boolean interactive) {
			setzDelta(0.25f);
			if (!interactive)
				setVisibility(EVisibility.VISIBLE);
		}

		@Override
		protected void init(IGLElementContext context) {
			super.init(context);
			addPropertyChangeListener(PROP_FILTER, repaintListner);
		}

		@Override
		protected void takeDown() {
			removePropertyChangeListener(PROP_FILTER, repaintListner);
			super.takeDown();
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			super.renderImpl(g, w, h);
			if (((IColumnRenderInfo) getParent()).isCollapsed())
				return;
			g.drawText("Filter:", 4, 2, w - 4, 12);
			String t = "<None>";
			if (isFiltered())
				t = selection.size() + " out of " + metaData.size();
			g.drawText(t, 4, 18, w - 4, 12);
		}

		@SuppressWarnings("unchecked")
		@ListenTo(sendToMe = true)
		private void onSetFilter(FilterEvent event) {
			setFilter((Collection<CATEGORY_TYPE>) event.getFilter(), event.isFilterGlobally());
		}
	}

	class MyValueElement extends ValueElement {
		public MyValueElement() {
			setVisibility(EVisibility.VISIBLE);
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			if (h < 5)
				return;
			String info = getTooltip();
			if (info == null)
				return;
			float hi = Math.min(h, 18);
			if (!(((IColumnRenderInfo) getParent()).isCollapsed())) {
				g.drawText(info, 1, 1 + (h - hi) * 0.5f, w - 2, hi - 5);
			}
		}

		@Override
		public String getTooltip() {
			return getValue(getLayoutDataAs(IRow.class, null));
		}
	}

}
