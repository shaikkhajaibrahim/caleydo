/*
 * Project: GenView
 * 
 * Author: Michael Kalkusch
 * 
 *  creation date: 18-05-2005
 *  
 */
package cerberus.manager.dcomponent;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Iterator;

import cerberus.manager.IDistComponentManager;
import cerberus.manager.IGeneralManager;
import cerberus.manager.base.AAbstractManager;
import cerberus.manager.data.ICollectionManager;
import cerberus.manager.type.ManagerType;
import cerberus.manager.type.ManagerObjectType;
import cerberus.manager.singelton.SingeltonManager;

import cerberus.net.dwt.DNetEventComponentInterface;
import cerberus.net.dwt.base.DGuiComponentType;

import cerberus.net.dwt.swing.component.DButton;
import cerberus.net.dwt.swing.component.DPanel;

import cerberus.util.exception.CerberusRuntimeException;

//import cerberus.net.dwt.swing.DViewHistogram;

/**
 * @author Michael Kalkusch
 *
 */
public class DComponentSwingFactoryManager 
 extends AAbstractManager
 implements IDistComponentManager {
	
	protected Vector<DNetEventComponentInterface> vecGuiComponents;
	
	protected Hashtable<Integer,DNetEventComponentInterface> hashGuiIndexLookup;
	
	private int iCurrentUniqueComponentId;
	
	protected IGeneralManager refGeneralManager = null;
	
	/**
	 * 
	 */
	public DComponentSwingFactoryManager(IGeneralManager setGeneralManager) {
		
		super( setGeneralManager, 
				IGeneralManager.iUniqueId_TypeOffset_GuiComponent);
		
		assert setGeneralManager != null: "DComponentSwingFactoryManager.DComponentSwingFactoryManager() init with null-pointer.";
		
		refGeneralManager = setGeneralManager;
		
		vecGuiComponents = new Vector<DNetEventComponentInterface> ();
		hashGuiIndexLookup = new Hashtable<Integer,DNetEventComponentInterface> ();
		
		iCurrentUniqueComponentId = 
			ICollectionManager.calculateId( 
					IGeneralManager.iUniqueId_TypeOffset_GuiComponent, 
					setGeneralManager );
		
		refGeneralManager.getSingelton().setDComponentManager( this );
	}


	/* (non-Javadoc)
	 * @see cerberus.data.manager.DComponentManager#createSet(cerberus.net.dwt.DNetEventType)
	 */
	public DNetEventComponentInterface createSet( final DGuiComponentType useSetType) {
		
		DNetEventComponentInterface refResult = null;

		
		switch ( useSetType ) {
			case BUTTON:
				refResult = new DButton();
				break;
			case PANEL:
				refResult = new DPanel();
				break;
			default:
				throw new CerberusRuntimeException("The type " + useSetType.toString() + " is not supported yet.");
				//return null;
		}
		
		final int iNewUniqueId = this.createNewId( ManagerObjectType.MEMENTO );
		refResult.setId( iNewUniqueId );
		
		vecGuiComponents.add( refResult );
		hashGuiIndexLookup.put( iNewUniqueId, refResult );
		
		return refResult;
	}


	/* (non-Javadoc)
	 * @see cerberus.data.manager.DComponentManager#deleteSet(int)
	 */
	public boolean deleteSet(int iNetEventId) {
		try {
			DNetEventComponentInterface removeItem = 
				hashGuiIndexLookup.get( new Integer(iNetEventId) );
			vecGuiComponents.remove( removeItem );
			
			hashGuiIndexLookup.remove( new Integer(iNetEventId) );
		}
		catch (NullPointerException ne) {
			return false;
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.manager.DComponentManager#getItemSet(int)
	 * @see cerberus.data.manager.GeneralManager#getItem(int)
	 */
	public DNetEventComponentInterface getItemSet( final int iNetEventId) {
		try {
			return hashGuiIndexLookup.get( new Integer(iNetEventId) );
		}
		catch (NullPointerException ne) {
			return null;
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.manager.GeneralManager#getItem(int)
	 */
	public final Object getItem( final int iItemId) {
		return getItemSet(iItemId);
	}

	/* (non-Javadoc)
	 * @see cerberus.data.manager.DComponentManager#getAllSetItems()
	 */
	public Iterator<DNetEventComponentInterface> getIteratorComponents() {
		return this.vecGuiComponents.iterator();
	}

	/* (non-Javadoc)
	 * @see cerberus.data.manager.GeneralManager#hasItem(int)
	 */
	public boolean hasItem( final int iItemId) {
		return hashGuiIndexLookup.containsKey( new Integer( iItemId) );
	}

	/* (non-Javadoc)
	 * @see cerberus.data.manager.GeneralManager#size()
	 */
	public int size() {
		return this.vecGuiComponents.size();
	}

	/* (non-Javadoc)
	 * @see cerberus.data.manager.GeneralManager#getManagerType()
	 */
	public ManagerObjectType getManagerType() {
		return ManagerObjectType.GUI_COMPONENT;
	}
	
	public boolean unregisterItem( final int iItemId,
			final ManagerObjectType type  ) {
		
		assert false:"not done yet";
		return false;
	}

	public boolean registerItem( final Object registerItem, 
			final int iItemId , 
			final ManagerObjectType type ) {
		
		
		assert false:"not done yet";
		return false;
	}

}
