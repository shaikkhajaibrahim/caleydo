package org.caleydo.rcp.core.bridge;

import org.caleydo.core.manager.event.AEvent;
import org.caleydo.core.manager.event.AEventListener;
import org.caleydo.core.manager.event.data.ClusterSetEvent;

public class ClusterSetListener extends AEventListener<RCPBridge> {

	@Override
	public void handleEvent(AEvent event) {

		if (event instanceof ClusterSetEvent) {
			ClusterSetEvent clusterSetEvent = (ClusterSetEvent) event;
			handler.clusterSet(clusterSetEvent.getSets());
		}
	}
}
