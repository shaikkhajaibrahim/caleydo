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
package org.caleydo.core.view.opengl.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL2;

import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.view.opengl.layout.event.LayoutSizeCollisionEvent;

/**
 * BaseClass for layouts which contain nested {@link ElementLayout}s.
 *
 * @author Alexander Lex
 */
public class ALayoutContainer extends ElementLayout implements Iterable<ElementLayout> {
	protected final ILayout layout;

	/**
	 * Flag signaling whether the x-size of the container should be calculated as the sum of it's parts (true), or if
	 * some size indication (either scaled or not scaled) is given (false)
	 */
	protected boolean isXDynamic = false;

	/**
	 * Flag signaling whether the y-size of the container should be calculated as the sum of it's parts (true), or if
	 * some size indication (either scaled or not scaled) is given (false)
	 */
	protected boolean isYDynamic = false;

	private final List<ElementLayout> elements = new ArrayList<ElementLayout>();

	/**
	 * The currently available bottom distance for the layout. Use if only this sub-part of the layout is updated
	 */
	protected float bottom;
	/**
	 * The currently available top distance for the layout. Use if only this sub-part of the layout is updated
	 */
	protected float top;
	/**
	 * The currently available left distance for the layout. Use if only this sub-part of the layout is updated
	 */
	protected float left;
	/**
	 * The currently available right distance for the layout. Use if only this sub-part of the layout is updated
	 */
	protected float right;

	/**
	 * Determines whether the layout elements of this container are rendered in an order according to their priority.
	 */
	protected boolean isPriorityRendereing = false;

	public ALayoutContainer(ILayout layout) {
		super();
		this.layout = layout;
	}

	public ALayoutContainer(String layoutName, ILayout layout) {
		super(layoutName);
		this.layout = layout;
	}

	/**
	 * @return the layout, see {@link #layout}
	 */
	public ILayout getLayout() {
		return layout;
	}

	@Override
	public void render(GL2 gl) {
		super.render(gl);
		if (isHidden)
			return;
		if (isPriorityRendereing) {
			List<ElementLayout> sortedList = new ArrayList<>(elements);
			Collections.sort(sortedList);
			for (ElementLayout l : sortedList)
				l.render(gl);
		} else {
			for (ElementLayout element : elements) {
				element.render(gl);
			}
		}
	}

	/**
	 * Set flag signaling whether the x-size of the container should be calculated as the sum of it's parts (true), or
	 * if some size indication (either scaled or not scaled) is given (false)
	 */
	public void setXDynamic(boolean isXDynamic) {
		this.isXDynamic = isXDynamic;
	}

	/**
	 * Set flag signaling whether the y-size of the container should be calculated as the sum of it's parts (true), or
	 * if some size indication (either scaled or not scaled) is given (false)
	 */
	public void setYDynamic(boolean isYDynamic) {
		this.isYDynamic = isYDynamic;
	}

	/**
	 * Append an element to the container at the end
	 *
	 * @param elementLayout
	 */
	public void append(ElementLayout elementLayout) {
		add(elementLayout);
	}

	/**
	 * Append an element to the container at the end
	 *
	 * @param elementLayout
	 */
	public ALayoutContainer add(ElementLayout elementLayout) {
		elements.add(elementLayout);
		init(elementLayout);
		return this;
	}

	/**
	 * Add an element to the container at the specified index. Subsequent layouts will be shifted to the right.
	 *
	 * @param index
	 * @param elementLayout
	 */
	public void add(int index, ElementLayout elementLayout) {
		elements.add(index, elementLayout);
		init(elementLayout);
	}

	private void init(ElementLayout child) {
		if (layoutManager != null)
			child.setLayoutManager(layoutManager);
	}

	public ElementLayout get(int index) {
		return elements.get(index);
	}

	@Override
	public Iterator<ElementLayout> iterator() {
		return elements.iterator();
	}

	@Override
	public void updateSubLayout() {
		if (isHidden)
			return;
		calculateScales(totalWidth, totalHeight, dynamicSizeUnitsX, dynamicSizeUnitsY);
		updateSpacings();
		calculateTransforms(bottom, left, top, right);
	}

	@Override
	public String toString() {
		return ("Container " + super.toString() + " with " + elements.size() + " elements. height: " + sizeScaledY
				+ ", width: " + sizeScaledX);
	}

	public int size() {
		return elements.size();
	}

	public boolean isEmpty() {
		return elements.isEmpty();
	}

	public void clear() {
		elements.clear();
	}

	public boolean remove(ElementLayout elementLayout) {
		return elements.remove(elementLayout);
	}

	public ElementLayout remove(int index) {
		return elements.remove(index);
	}

	public int indexOf(ElementLayout child) {
		return elements.indexOf(child);
	}

	// --------------------- End of Public Interface ---------------------

	protected void calculateTransforms(float bottom, float left, float top, float right) {
		this.bottom = bottom;
		this.left = left;
		this.top = top;
		this.right = right;
		layout.calculateTransforms(this, bottom, left, top, right);
	}

	@Override
	public float getUnscalableElementWidth() {
		if (isHidden)
			return 0;
		if (!isXDynamic)
			return super.getUnscalableElementWidth();
		return layout.getUnscalableElementWidth(this);
	}

	@Override
	public float getUnscalableElementHeight() {
		if (isHidden)
			return 0;
		if (!isYDynamic)
			return super.getUnscalableElementHeight();
		return layout.getUnscalableElementHeight(this);
	}

	@Override
	void calculateScales(float totalWidth, float totalHeight, Integer numberOfDynamicSizeUnitsX,
			Integer numberOfDynamicSizeUnitsY) {
		if (isHidden)
			return;
		super.calculateScales(totalWidth, totalHeight, numberOfDynamicSizeUnitsX, numberOfDynamicSizeUnitsY);
		layout.calculateScales(this, totalWidth, totalHeight, numberOfDynamicSizeUnitsX, numberOfDynamicSizeUnitsY);
	}

	/**
	 * @return the dynamicSizeUnitsX, see {@link #dynamicSizeUnitsX}
	 */
	@Override
	int getDynamicSizeUnitsX() {
		return layout.getDynamicSizeUnitsX(this);
	}

	/**
	 * @return the dynamicSizeUnitsY, see {@link #dynamicSizeUnitsY}
	 */
	@Override
	int getDynamicSizeUnitsY() {
		return layout.getDynamicSizeUnitsY(this);
	}

	@Override
	void setRenderingDirty() {
		if (isHidden)
			return;
		super.setRenderingDirty();
		for (ElementLayout element : elements) {
			element.setRenderingDirty();
		}
	}

	@Override
	protected void updateSpacings() {
		if (isHidden)
			return;
		super.updateSpacings();
		for (ElementLayout element : elements) {
			element.updateSpacings();
		}
	}

	@Override
	void setLayoutManager(LayoutManager layoutManager) {
		super.setLayoutManager(layoutManager);
		for (ElementLayout element : elements) {
			element.setLayoutManager(layoutManager);
		}
	}

	@Override
	public void destroy(GL2 gl) {
		super.destroy(gl);

		for (ElementLayout elementLayout : elements) {
			elementLayout.destroy(gl);
		}
		elements.clear();
	}

	/**
	 * @return True, if this layout container renders its elements in an order (concerning the time) according to their
	 *         priority, false otherwise.
	 */
	public boolean isPriorityRendereing() {
		return isPriorityRendereing;
	}

	/**
	 * Sets whether this layout container renders its elements in an order (concerning the time) according to their
	 * priority.
	 *
	 * @param isPriorityRendereing
	 */
	public void setPriorityRendereing(boolean isPriorityRendereing) {
		this.isPriorityRendereing = isPriorityRendereing;
	}

	/**
	 * @param abs
	 */
	public void triggerLayoutCollision(float toBigBy) {
		if (managingClassID != -1 && layoutID != -1) {
			LayoutSizeCollisionEvent event = new LayoutSizeCollisionEvent();
			event.setToBigBy(toBigBy);
			event.tableIDs(managingClassID, layoutID);
			GeneralManager.get().getEventPublisher().triggerEvent(event);
		}
	}
}
