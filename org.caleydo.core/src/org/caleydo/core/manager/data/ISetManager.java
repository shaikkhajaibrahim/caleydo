/*
 * Project: GenView
 * 
 * Author: Michael Kalkusch
 * 
 *  creation date: 18-05-2005
 *  
 */
package org.caleydo.core.manager.data;

import java.util.Collection;

import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.collection.SetType;
import org.caleydo.core.manager.IGeneralManager;


/**
 * Manges all ISet's.
 * 
 * Note: the ISetManager must register itself to the singelton prometheus.app.SingeltonManager
 * 
 * @author Michael Kalkusch
 *
 */
public interface ISetManager
extends IGeneralManager
{
	
	public ISet createSet( final SetType setType );
	
	public boolean deleteSet( ISet deleteSet );
	
	public boolean deleteSet( final int iItemId );
	
	public ISet getItemSet( final int iItemId );
	
	public Collection<ISet> getAllSetItems();
	
	/**
	 * Initialize data structures prior using this manager.
	 *
	 */
	public void initManager();
	
}
