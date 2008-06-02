package org.caleydo.core.parser.ascii.lookuptable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.caleydo.core.data.mapping.EGenomeMappingType;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.data.genome.IGenomeIdMap;


/**
 * Loads a lookup table mapping an ID to another.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 */
public class LookupTableHashMapLoader 
extends ALookupTableLoader
implements ILookupTableLoader {
	
	protected IGenomeIdMap genomeIdMap;
	
	/**
	 * Constructor.
	 * 
	 * @param setGeneralManager
	 * @param setFileName
	 */
	public LookupTableHashMapLoader(final IGeneralManager generalManager,
			final String sFileName,
			final EGenomeMappingType genomeIdType,
			final LookupTableLoaderProxy lookupTableLoaderProxy ) {

		super(generalManager, sFileName, genomeIdType, lookupTableLoaderProxy);
	}


	/* (non-Javadoc)
	 * @see org.caleydo.core.parser.ascii.lookuptable.ILookupTableLoader#loadDataParseFileLUT(java.io.BufferedReader, int)
	 */
	public int loadDataParseFileLUT(BufferedReader brFile,
			int iNumberOfLinesInFile ) throws IOException {

		String sLine;
		
		int iLineInFile = 1;
		int iStartParsingAtLine = lookupTableLoaderProxy.getStartParsingAtLine();
		int iStopParsingAtLine  = lookupTableLoaderProxy.getStopParsingAtLine();		
		String sOuterTokenSeperator = lookupTableLoaderProxy.getTokenSeperator();
		
	    while ( ((sLine = brFile.readLine()) != null)&&
	    		( iLineInFile <= iStopParsingAtLine) )  
	    {
	    	/**
	    	 * Start parsing if current line 
	    	 * iLineInFile is larger than iStartParsingAtLine ..
	    	 */
			if( iLineInFile > iStartParsingAtLine )
			{
				
				boolean bMaintainLoop = true;
				StringTokenizer strTokenText = new StringTokenizer(sLine, sOuterTokenSeperator );
				
				// Read all tokens
				while (( strTokenText.hasMoreTokens() )&& bMaintainLoop) 
				{
					// Expect two Integer values in one row!
					try {
						String buffer = strTokenText.nextToken();
						
						if  (strTokenText.hasMoreTokens()) 
						{
							// Special case for creating indexing of storages
							if (currentGenomeIdType.equals(EGenomeMappingType.DAVID_2_EXPRESSION_STORAGE_ID))
								genomeIdMap.put(buffer, Integer.toString(iLineInFile-iStartParsingAtLine));
							else
								genomeIdMap.put(buffer, strTokenText.nextToken());
							
							break;
						}
						else
						{
//							generalManager.logMsg(
//									"(Key,Value) [" +
//									buffer + ", ?? ] value is missing (ignore key-value pair) in line " +
//									iLineInFile,
//									LoggerType.FULL);
						}
					} catch ( NoSuchElementException  nsee) {
						/* no ABORT was set. 
						 * since no more tokens are in ParserTokenHandler skip rest of line..*/
						bMaintainLoop = false;
						
						//reset return value to indicate error
						iStopParsingAtLine = -1;
						
					} catch ( NullPointerException npe ) {
						bMaintainLoop = false;
						
						//reset return value to indicate error
						iStopParsingAtLine = 1;
						
						System.out.println( "LookupTableHashMapLoader NullPointerException! " + npe.toString() );
						npe.printStackTrace();
						
					}
				
				} // end of: while (( strToken.hasMoreTokens() )&&(bMaintainLoop)) {
				
				
				lookupTableLoaderProxy.progressBarStoredIncrement();
				
			} // end of: if( iLineInFile > this.iHeaderLinesSize) {			
			
			iLineInFile++;
			
	    } // end: while ((sLine = brFile.readLine()) != null) { 
	 
		return iLineInFile - iStartParsingAtLine;
	}
	
	public final void setHashMap( final IGenomeIdMap setHashMap,
			final EGenomeMappingType type) {
		
		assert type == currentGenomeIdType : "must use same type as in constructor!";
		
		if ( type.isMultiMap() )
		{
			assert false : "setHashMap() must not call MultiMap via setHashMap()";
			return;
		}
		
		genomeIdMap = setHashMap;		
	}


	/**
	 * Write back data to IGenomeIdManager
	 * @see org.caleydo.core.parser.ascii.lookuptable.ILookupTableLoader#wirteBackMapToGenomeManager()
	 * @see org.caleydo.core.manager.data.IGenomeIdManager
	 */
	public void wirteBackMapToGenomeIdManager() {
		
		genomeIdManager.setMapByType(currentGenomeIdType, genomeIdMap);
	}
}
