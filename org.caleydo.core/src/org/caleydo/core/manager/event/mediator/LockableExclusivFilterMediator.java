/**
 * 
 */
package org.caleydo.core.manager.event.mediator;

import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.manager.IGeneralManager;

/**
 * Exclusively accept one SelectionSet and blocks all other SelectionSet's when calling
 * org.caleydo.core.manager.event.mediator.IMediator#updateReceiverSelection(Object, ISet).
 * 
 * @see org.caleydo.core.manager.event.mediator.IMediator#updateReceiverSelection(Object, ISet)
 * 
 * @author Michael Kalkusch
 *
 */
public class LockableExclusivFilterMediator 
extends LockableMediator {

	protected ISet refExclusiveSet;
	
	/**
	 * @param refEventPublisher
	 * @param iMediatorId
	 */
	public LockableExclusivFilterMediator(final IGeneralManager generalManager,
			int iMediatorId,
			final ISet setExclusiveSelectionSet) {

		super(generalManager, iMediatorId, MediatorUpdateType.MEDIATOR_FILTER_ONLY_SET);
		
		this.refExclusiveSet = setExclusiveSelectionSet;
	}
	
	public ISet getExclusiveSet() {
		return this.refExclusiveSet;
	}
	
	public void setExclusiveSet(ISet setExclusiveSelectionSet) {
		this.refExclusiveSet = setExclusiveSelectionSet;
	}
	
	/**
	 * The update is only forwareded if the exclusiveSelectionSet (internal)
	 * is equal tio the calling selectionSet.
	 * 
	 * @see org.caleydo.core.manager.event.mediator.LockableMediator#updateReceiverSpecialMediator(java.lang.Object, org.caleydo.core.data.collection.ISet)
	 */
	public final void updateReceiverSpecialMediator(Object eventTrigger,
			ISet updatedSet) {
		
		assert updatedSet != null : "can not handle selectionSet null-pointer";
		
		if (refExclusiveSet == updatedSet) {
			super.updateReceiver(eventTrigger, updatedSet);
		}		
	}

}
