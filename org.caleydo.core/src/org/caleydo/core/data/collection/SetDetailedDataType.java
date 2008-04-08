/*
 * Project: GenView
 * 
 * Author: Michael Kalkusch
 * 
 *  creation date: 18-05-2005
 *  
 */
package org.caleydo.core.data.collection;

import org.caleydo.core.util.ICaleydoDefaultType;

/**
 * Defines different types of sets containing data.
 * 
 * @author Michael Kalkusch
 * 
 * @see org.caleydo.core.data.collection.SetDataType;
 * @see org.caleydo.core.data.collection.SetType;
 *
 */
public enum SetDetailedDataType 
implements ICaleydoDefaultType <SetDetailedDataType> {
	
	/** raw data without any tag */
	RAW_DATA(),
	
	/** Pathway related data */
	PATHWAY_DATA(),
	
	/** gene expression data */
	GENE_EXPRESSION_DATA(),
	
	/** not specified  */
	NONE();
	
	
//	private final boolean bRawDataType;
//	
//	private SetDataType setDataType;
	
	/**
	 * Default Constructor
	 */
	private SetDetailedDataType() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.util.ICaleydoDefaultType#getTypeDefault()
	 */
	public SetDetailedDataType getTypeDefault() {

		return SetDetailedDataType.RAW_DATA;
	}
	

	public static SetDetailedDataType getDefault() {

		return SetDetailedDataType.RAW_DATA;
	}
	
//	/**
//	 * TRUE if it is a RAW_DATA type, FALSE else
//	 */
//	public boolean isDataType() {
//		return bRawDataType;
//	}
//	
//	public void setDataType( final SetDataType setType) {
//		assert setType != SetDataType.SET_DATATYPE_NONE : "Can not set no data type";
//		
//		setDataType = setType;
//	}
//	
//	public SetDataType getDataType() {
//		return setDataType;
//	}
}
