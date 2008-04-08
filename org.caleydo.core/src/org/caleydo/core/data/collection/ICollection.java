/*
 * Project: GenView
 * 
 * Author: Michael Kalkusch
 * 
 *  creation date: 18-05-2005
 *  
 */
package org.caleydo.core.data.collection;

//import prometheus.data.UniqueManagedInterface;
//import prometheus.data.collection.CollectionCache;

/**
 * Interface for objects with labels and a cache.
 * 
 * @author Michael Kalkusch
 *
 */
public interface ICollection {
	//extends ICollectionCache, IUniqueManagedObject {

	/**
	 * Return a Label of the item.
	 * 
	 * @return label text
	 */
	public String getLabel();
	
	/**
	 * Sets the label for this item.
	 * 
	 * @param setLabel label text
	 */
	public void setLabel( final String setLabel);
	

}
