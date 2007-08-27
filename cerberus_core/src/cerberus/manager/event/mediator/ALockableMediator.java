/*
 * Project: GenView
 *  
 */

package cerberus.manager.event.mediator;

import cerberus.manager.IEventPublisher;
import cerberus.manager.event.mediator.MediatorUpdateType;
import cerberus.util.exception.GeneViewRuntimeException;
import cerberus.util.exception.GeneViewRuntimeExceptionType;

/**
 * Abstract class for the mediator that belongs to the event mechanism.
 * 
 * @see cerberus.manager.event.mediator.IMediator
 * 
 * @author Micheal Kalkusch
 * @author Marc Streit
 */
public abstract class ALockableMediator 
extends ALockableMediatorReceiver {
	
	protected final IEventPublisher refEventPublisher;
	
	private final MediatorUpdateType mediatorUpdateType;
		
	public final int iMediatorId;
	
	/**
	 * 
	 * @param iMediatorId
	 * @param mediatorUpdateType if ==NULL, MediatorUpdateType.MEDIATOR_DEFAULT is used as default 
	 */
	protected ALockableMediator(final IEventPublisher refEventPublisher,
			int iMediatorId,
			final MediatorUpdateType mediatorUpdateType) {
		
		super();
		
		this.refEventPublisher = refEventPublisher;
		this.iMediatorId = iMediatorId;
		
		if ( mediatorUpdateType == null ) 
		{
			this.mediatorUpdateType = MediatorUpdateType.MEDIATOR_DEFAULT;
		}
		else 
		{
			this.mediatorUpdateType = mediatorUpdateType;
		}
	}

	public final MediatorUpdateType getMediatorUpdateTypeType() {
		return mediatorUpdateType;
	}
	
	/**
	 * Implement cleanup inside this function.
	 * 
	 * @see cerberus.manager.event.mediator.ALockableMediator#destroyMediator(IEventPublisher)
	 * 
	 * @param sender callling object 
	 */
	protected abstract void destroyMediatorDerivedObject( 
			final IEventPublisher sender );

	
	/**
	 * Test if caller is creator and calls destroyMediatorObject(IMediatorSender).
	 * 
	 * @see cerberus.manager.event.mediator.IMediator#destroyMediator()
	 * @see cerberus.manager.event.mediator.ALockableMediator#destroyMediatorDerivedObject(IMediatorSender)
	 */
	public final void destroyMediator( final IEventPublisher sender )
	{
		if ( ! refEventPublisher.equals(sender)) {
			throw new GeneViewRuntimeException("IMediator.destroyMediator() may only be callled by its creator!");
		}

		destroyMediatorDerivedObject( sender );
	}
	

	/**
	 * @see cerberus.data.IUniqueObject#getId()
	 */
	public final int getId() {

		return iMediatorId;
	}

	/**
	 * Since the MediatorId is final this method must not be called.
	 * 
	 * @see cerberus.data.IUniqueObject#setId(int)
	 */
	public final void setId(int isetId) {
		
		throw new GeneViewRuntimeException("setId() must not be called.",
				GeneViewRuntimeExceptionType.OBSERVER);
		
		//this.iMediatorId = isetId;
		
	}

}
