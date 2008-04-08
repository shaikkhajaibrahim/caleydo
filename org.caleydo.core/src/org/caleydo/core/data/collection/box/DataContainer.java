/**
 * 
 */
package org.caleydo.core.data.collection.box;

import java.util.Vector;
//import java.util.Iterator;

//import org.caleydo.core.data.collection.IVirtualArray;
import org.caleydo.core.data.collection.ISet;
//import org.caleydo.core.data.collection.IStorage;

import org.caleydo.core.data.collection.thread.ICollectionThreadItem;
import org.caleydo.core.data.collection.thread.impl.AThreadItem;
import org.caleydo.core.data.collection.thread.lock.ICollectionLock;


/**
 * Container or box holding several ISet's.
 * Has it's own unique id and a label.
 * 
 * @author Michael Kalkusch
 *
 */
public class DataContainer extends AThreadItem implements
		ICollectionThreadItem {

	/**
	 * Label for this DataContainer.
	 * Label must not be unique.
	 * Default label is it's unique ID
	 */
	protected String sMetaBoxLabel;
	
	/**
	 * Define a group of org.caleydo.core.data.collection.ISet objects.
	 * Each ISet may have a label stored in org.caleydo.core.data.collection.box.DataContainer#vecLabelOfSet
	 * 
	 * @see org.caleydo.core.data.collection.box.DataContainer#vecLabelOfSet
	 */
	protected Vector <ISet> vecSet;
	
	/**
	 * Define a label for each set
	 * 
	 * @see org.caleydo.core.data.collection.box.DataContainer#vecSet
	 */
	protected Vector <String> vecLabelOfSet;
		
	
	/**
	 * @param iSetCollectionId
	 */
	public DataContainer(int iSetCollectionId) {
		super(iSetCollectionId);
		
		sMetaBoxLabel = Integer.toString( iSetCollectionId );
	}

	/**
	 * @param iSetCollectionId
	 * @param setCollectionLock
	 */
	public DataContainer(int iSetCollectionId, ICollectionLock setCollectionLock) {
		super(iSetCollectionId, setCollectionLock);
		
		sMetaBoxLabel = Integer.toString( iSetCollectionId );
	}

	/**
	 * Get Label.
	 * Label is not an unique name.
	 * 
	 * @return label of DataContainer
	 */
	public final String getMetaBoxLabel() {
		return sMetaBoxLabel;
	}
	
	/**
	 * ISet Label of DataContainer. 
	 * Must not be an unique name.
	 * 
	 * @param setMetaBoxLabel set new label name
	 */
	public final void getMetaBoxLabel( String setMetaBoxLabel ) {
		this.sMetaBoxLabel = setMetaBoxLabel;
	}
	
	public void addSet( ISet addSet, String label ) {
		
	}
	
	public void removeSet( ISet addSet ) {
		
	}
	
	/**
	 * @see org.caleydo.core.data.collection.box.DataContainer#addSet(ISet, String)
	 * 
	 * @param iUniqueSetId
	 * @param label
	 */
	public void addItem( int iUniqueSetId, String label ) {
		
	}
	
	/**
	 * 
	 * @see org.caleydo.core.data.collection.box.DataContainer#addItem(int, String)
	 * @see org.caleydo.core.data.collection.box.DataContainer#addSet(ISet, String)
	 * 
	 * @param iUniqueSetId
	 */
	public void removeItem( int iUniqueSetId ) {
		
	}
	
	/**
	 * Swap position of two ISet'S in a Box.
	 * 
	 * @param iFromUniqueSetId Addess first ISet
	 * @param iToUniqueSetId address second ISet
	 */
	public void swapItem( int iFromUniqueSetId, int iToUniqueSetId ) {
		
	}
	
	/**
	 * Remove all ISet objects from Box.
	 * 
	 * @see org.caleydo.core.data.collection.box.DataContainer#addItem(int, String)
	 * @see org.caleydo.core.data.collection.box.DataContainer#addSet(ISet, String)
	 * 
	 *
	 */
	public void clearBox( ) {
		
	}
	
}
