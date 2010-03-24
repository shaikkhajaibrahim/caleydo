package daemon;

import Ice.Current;
import VIS.InteractionEvent;
import VIS.MouseOverCollaboratorSelectionEvent;
import VIS.VisManagerIPrx;
import VIS._VisManagerIDisp;

public class VisLinkManagerIceInterface extends _VisManagerIDisp{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	VisLinkManager manager; 
	
	VisManagerIPrx appPrx; 

	public VisLinkManagerIceInterface(VisLinkManager manager) {
		this.manager = manager;
	}
	
	public VisManagerIPrx getProxy() {
		return appPrx;
	}


	public void setProxy(VisManagerIPrx appPrx) {
		this.appPrx = appPrx;
	}


	public void reportMouseOverCollaboratorSelectionEvent(MouseOverCollaboratorSelectionEvent event){
		System.out.println("Receiving mouse over collaborator selection event for owner " + event.ownerPointerId); 
	}


	public void reportEvent(InteractionEvent event, Current current) {
		System.out.println("Receiving interaction event for pointer " + event.pointerId); 
		switch(event.eventType){
		case MouseOverCollaboratorSelection: 
			this.reportMouseOverCollaboratorSelectionEvent((MouseOverCollaboratorSelectionEvent)event); 
			break; 
		default:
			break; 
		}
	}

}
