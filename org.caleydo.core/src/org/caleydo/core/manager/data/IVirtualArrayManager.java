/*
 * Project: GenView
 * 
 * Author: Michael Kalkusch
 * 
 *  creation date: 18-05-2005
 *  
 */
package org.caleydo.core.manager.data;

import java.util.Vector;

import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.type.ManagerObjectType;

import org.caleydo.core.data.collection.IVirtualArray;

/**
 * Manage all IVirtualArray's.
 * 
 * Note: the ISelectionManager must register itself to the singelton prometheus.app.SingeltonManager
 * 
 * @author Michael Kalkusch
 *
 */
public interface IVirtualArrayManager
extends IGeneralManager
{
	
	public IVirtualArray createVirtualArray( final ManagerObjectType useSelectionType );
	
	public boolean deleteVirtualArray( IVirtualArray deleteSelection );
	
	public boolean deleteVirtualArray( final int iItemId  );
	
	public IVirtualArray getItemVirtualArray( final int iItemId );
	
	public IVirtualArray[] getAllVirtualArrayItems();
	
	public Vector<IVirtualArray> getAllVirtualArrayItemsVector();
	
	//public ManagerObjectType getManagerType();
	
}
