package org.caleydo.view.scatterplot.toolbar;

import org.caleydo.core.event.AEvent;
import org.caleydo.core.event.AEventListener;
import org.caleydo.core.event.view.tablebased.InitAxisComboEvent;

/**
 * Listener that reacts to Inititailaize The ComboBoxes For Axis Selection in
 * the Scatterplot
 * 
 * @author Juergen Pillhofer
 */
public class InitYAxisComboListener extends AEventListener<YAxisSelector> {

	@Override
	public void handleEvent(AEvent event) {
		if (event instanceof InitAxisComboEvent) {
			handler.initComboString(((InitAxisComboEvent) event).getAxisNames());
		}
	}

}
