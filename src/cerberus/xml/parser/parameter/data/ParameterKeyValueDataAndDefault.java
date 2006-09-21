/**
 * 
 */
package cerberus.xml.parser.parameter.data;

import java.util.Enumeration;
import java.util.Hashtable;

import cerberus.xml.parser.parameter.IParameterKeyValuePair;

/**
 * @author java
 *
 */
public final class ParameterKeyValueDataAndDefault < T > implements IParameterKeyValuePair<T>
{

	private Hashtable < String, T > hashKey2Generic;
	
	private Hashtable < String, T > hashKey2DefaultValue;
	
	/**
	 * 
	 */
	public ParameterKeyValueDataAndDefault()
	{
		hashKey2Generic = new Hashtable < String, T > ();
		
		hashKey2DefaultValue = new Hashtable < String, T > ();
	}

	/* (non-Javadoc)
	 * @see cerberus.xml.parser.parameter.IParameterKeyValueDefaultvalue#getValue(java.lang.String)
	 */
	public T getValue( final String key ) {
		return hashKey2Generic.get( key );
	}
	
	
	/* (non-Javadoc)
	 * @see cerberus.xml.parser.parameter.IParameterKeyValueDefaultvalue#getDefaultValue(java.lang.String)
	 */
	public T getDefaultValue( final String key ) {
		return hashKey2DefaultValue.get( key );
	}
	
	/* (non-Javadoc)
	 * @see cerberus.xml.parser.parameter.IParameterKeyValueDefaultvalue#getValueOrDefault(java.lang.String)
	 */
	public T getValueOrDefault( final String key ) {
		T buffer = hashKey2Generic.get( key );
		
		if ( buffer == null ) 
		{
			return hashKey2DefaultValue.get( key );
		}
		
		return buffer;
	}
	
	/* (non-Javadoc)
	 * @see cerberus.xml.parser.parameter.IParameterKeyValueDefaultvalue#setValue(java.lang.String, T)
	 */
	public void setValue( final String key,
			final T value ) {
		hashKey2Generic.put( key, value );
	}
	
	public void setValueAndDefaultValue(final String key, 
			final T value,
			final T defaultValue) {
		hashKey2Generic.put( key, value );
		hashKey2DefaultValue.put( key, defaultValue );
	}
	
	/* (non-Javadoc)
	 * @see cerberus.xml.parser.parameter.IParameterKeyValueDefaultvalue#setDefaultValue(java.lang.String, T)
	 */
	public void setDefaultValue( final String key,
			final T value ) {
		hashKey2DefaultValue.put( key, value );
		hashKey2Generic.put( key, value );
	}
	
	
	/* (non-Javadoc)
	 * @see cerberus.xml.parser.parameter.IParameterKeyValueDefaultvalue#clear()
	 */
	public void clear() {		
		synchronized( getClass() ) {
			hashKey2DefaultValue.clear();
			hashKey2Generic.clear();
		}
	}
	
	/* (non-Javadoc)
	 * @see cerberus.xml.parser.parameter.IParameterKeyValueDefaultvalue#containsValue(java.lang.String)
	 */
	public boolean containsValue( final String key ) {
		return hashKey2Generic.containsKey( key );
	}
	
	/* (non-Javadoc)
	 * @see cerberus.xml.parser.parameter.IParameterKeyValueDefaultvalue#containsDefaultValue(java.lang.String)
	 */
	public boolean containsDefaultValue( final String key ) {
		return hashKey2DefaultValue.containsKey( key );
	}
	
	/* (non-Javadoc)
	 * @see cerberus.xml.parser.parameter.IParameterKeyValueDefaultvalue#containsValueAndDefaultValue(java.lang.String)
	 */
	public boolean containsValueAndDefaultValue( final String key ) {
		if ( (hashKey2Generic.containsKey( key ))&&
				(hashKey2DefaultValue.containsKey( key )) ) {
			return true;
		}
		return false;
	}

	/*
	 *  (non-Javadoc)
	 * @see cerberus.xml.parser.parameter.IParameterKeyValuePair#size()
	 */
	public int size()
	{
		return this.hashKey2Generic.size();
	}

	/*
	 *  (non-Javadoc)
	 * @see cerberus.xml.parser.parameter.IParameterKeyValuePair#isEmpty()
	 */
	public boolean isEmpty()
	{
		return this.hashKey2Generic.isEmpty();
	}
	
	public String toString() {
		
		if ( this.isEmpty() ) {
			return "-";
		}
		
		StringBuffer strBuffer = new StringBuffer();
		
		Enumeration <String> iter = hashKey2Generic.keys();
		
		/* special case for first element... */
		if ( iter.hasMoreElements() ) 
		{
			strBuffer.append( iter.nextElement() );
		}
		
		while ( iter.hasMoreElements() )
		{
			strBuffer.append( " " );
			strBuffer.append( iter.nextElement() );
		}
		
		return strBuffer.toString();
	}

}
