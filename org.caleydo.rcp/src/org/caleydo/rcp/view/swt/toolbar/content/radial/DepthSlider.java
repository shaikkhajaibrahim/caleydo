package org.caleydo.rcp.view.swt.toolbar.content.radial;

import org.caleydo.core.manager.event.AEvent;
import org.caleydo.core.manager.event.AEventListener;
import org.caleydo.core.manager.event.IListenerOwner;
import org.caleydo.core.manager.event.view.radial.SetMaxDisplayedHierarchyDepthEvent;
import org.caleydo.core.manager.event.view.radial.UpdateDepthSliderPositionEvent;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.rcp.view.swt.toolbar.content.IToolBarItem;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.PlatformUI;

public class DepthSlider
	extends ControlContribution 
	implements IToolBarItem, IListenerOwner{
	
//	private RadialSliderBox radialSliderBox;
	
	private Slider slider;
	private Listener listener;
	private UpdateDepthSliderPositionListener updateSliderPositionListener;
	private static int iSelection = 5;
	
	public DepthSlider(String str) {
		super(str);
	}

	@Override
	protected Control createControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = layout.marginWidth = layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		
//		Label label = new Label(composite, SWT.NULL);
//		label.setText("Displayed depth");
		
		slider = new Slider(composite, SWT.HORIZONTAL);
		slider.setValues(iSelection, 2, 20, 1, 1, 1);
		slider.setLayoutData(new GridData(130, 20));
//		slider.setSize(130, 20);

		listener = new Listener() {
			public void handleEvent(Event event) {
				SetMaxDisplayedHierarchyDepthEvent setMaxDisplayedHierarchyDepthEvent =
					new SetMaxDisplayedHierarchyDepthEvent();
				setMaxDisplayedHierarchyDepthEvent.setSender(this);
				setMaxDisplayedHierarchyDepthEvent.setMaxDisplayedHierarchyDepth(slider.getSelection());
				GeneralManager.get().getEventPublisher().triggerEvent(setMaxDisplayedHierarchyDepthEvent);
				iSelection = slider.getSelection();
			}
		};

		slider.addListener(SWT.Selection, listener);

		updateSliderPositionListener = new UpdateDepthSliderPositionListener();
		updateSliderPositionListener.setHandler(this);
		GeneralManager.get().getEventPublisher().addListener(UpdateDepthSliderPositionEvent.class,
			updateSliderPositionListener);
		return composite;
	}
	
	public void setSliderPosition(int iPosition) {
		iSelection = iPosition;
		slider.setSelection(iPosition);
	}

	@Override
	public void queueEvent(final AEventListener<? extends IListenerOwner> listener, final AEvent event) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				listener.handleEvent(event);
			}
		});
	}
}
