package org.caleydo.core.view.opengl.canvas.storagebased.listener;

import org.caleydo.core.manager.event.AEvent;
import org.caleydo.core.manager.event.view.keyboard.WrapperKeyEvent;
import org.caleydo.core.view.opengl.canvas.storagebased.GLHierarchicalHeatMap;
import org.caleydo.core.view.opengl.keyboard.GLKeyListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

public class GLHierarchicalHeatMapKeyListener
	extends GLKeyListener<GLHierarchicalHeatMap> {

	private GLHierarchicalHeatMap glHierarchicalHeatMap;

	public GLHierarchicalHeatMapKeyListener(GLHierarchicalHeatMap glHierarchicalHeatMap) {

		this.glHierarchicalHeatMap = glHierarchicalHeatMap;
	}

	@Override
	protected void handleKeyPressedEvent(KeyEvent event) {
		glHierarchicalHeatMap.queueEvent(this, new WrapperKeyEvent(event));

	}

	@Override
	public void handleEvent(AEvent event) {
		WrapperKeyEvent wrapperKeyEvent;
		if (event instanceof WrapperKeyEvent) {
			wrapperKeyEvent = (WrapperKeyEvent) event;
		}
		else
			return;

		KeyEvent keyEvent = wrapperKeyEvent.getKeyEvent();

		switch (keyEvent.keyCode) {
			case SWT.ARROW_UP:

				if (keyEvent.stateMask == SWT.CTRL) {
					glHierarchicalHeatMap.handleArrowUpCtrlPressed();
				}
				else if (keyEvent.stateMask == SWT.ALT) {
					glHierarchicalHeatMap.handleArrowUpAltPressed();
				}
				else {
					glHierarchicalHeatMap.handleArrowUpPressed();
				}

				break;
			case SWT.ARROW_DOWN:

				if (keyEvent.stateMask == SWT.CTRL) {
					glHierarchicalHeatMap.handleArrowDownCtrlPressed();
				}
				else if (keyEvent.stateMask == SWT.ALT) {
					glHierarchicalHeatMap.handleArrowDownAltPressed();
				}
				else {
					glHierarchicalHeatMap.handleArrowDownPressed();
				}

				break;
			case SWT.ARROW_LEFT:

				if (keyEvent.stateMask == SWT.SHIFT) {
					glHierarchicalHeatMap.handleArrowLeftShiftPressed();
				}
				break;
			case SWT.ARROW_RIGHT:

				if (keyEvent.stateMask == SWT.SHIFT) {
					glHierarchicalHeatMap.handleArrowRightShiftPressed();
				}
				break;
		}
	}
}
