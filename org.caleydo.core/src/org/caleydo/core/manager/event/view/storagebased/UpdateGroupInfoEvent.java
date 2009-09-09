package org.caleydo.core.manager.event.view.storagebased;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.manager.event.AEvent;

/**
 * This event signals a view that a change has occurred that causes a update of group/cluster information.
 * 
 * @author Bernhard Schlegl
 */
@XmlRootElement
@XmlType
public class UpdateGroupInfoEvent
	extends AEvent {

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
