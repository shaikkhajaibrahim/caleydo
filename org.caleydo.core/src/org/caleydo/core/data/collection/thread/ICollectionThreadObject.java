package org.caleydo.core.data.collection.thread;

import org.caleydo.core.data.collection.ICollectionCache;
import org.caleydo.core.data.collection.ICollection;
import org.caleydo.core.data.collection.thread.lock.ICollectionLock;
import org.caleydo.core.data.IUniqueManagedObject;

public interface ICollectionThreadObject 
	extends ICollectionCache, 
	ICollection, 
	IUniqueManagedObject {

	/** 
	 * 
	 * @see prometheus.data.collection.thread.ICollectionLock#getReadToken()
	 */
	public abstract boolean getReadToken();

	/**
	 *  
	 * @see prometheus.data.collection.thread.ICollectionLock#getReadTokenWait()
	 */
	public abstract boolean getReadTokenWait();

	/**
	 *  
	 * @see prometheus.data.collection.thread.ICollectionLock#returnReadToken()
	 */
	public abstract void returnReadToken();

	/**
	 * 
	 * @see prometheus.data.collection.thread.ICollectionLock#returnWriteToken()
	 */
	public abstract void returnWriteToken();

	/**
	 * 
	 * @see prometheus.data.collection.thread.ICollectionLock#getWriteToken()
	 */
	public abstract boolean getWriteToken();

	/**
	 * @see prometheus.data.collection.thread.ICollectionLock#getCollectionLock()
	 */
	public abstract ICollectionLock getCollectionLock();

	/**
	 * @see prometheus.data.collection.thread.ICollectionLock#setCollectionLock(prometheus.data.collection.thread.ICollectionLock)
	 */
	public abstract boolean setCollectionLock(
			final ICollectionLock setCollectionLock);

	
//	/*
//	 *  (non-Javadoc)
//	 * @see prometheus.data.collection.CollectionInterface#getCacheId()
//	 */
//	public abstract int getCacheId();
//
//	/*
//	 *  (non-Javadoc)
//	 * @see prometheus.data.collection.CollectionInterface#setCacheId(int)
//	 */
//	public abstract void setCacheId(final int iCacheId);
//
//	/*
//	 *  (non-Javadoc)
//	 * @see prometheus.data.collection.CollectionInterface#hasCacheChanged(int)
//	 */
//	public abstract boolean hasCacheChanged(final int iCompareCacheId);

}