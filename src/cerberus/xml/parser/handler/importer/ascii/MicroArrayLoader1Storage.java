/**
 * 
 */
package cerberus.xml.parser.handler.importer.ascii;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import cerberus.data.collection.ISet;
import cerberus.data.collection.IStorage;
import cerberus.data.collection.IVirtualArray;
import cerberus.data.collection.StorageType;
import cerberus.data.collection.parser.CollectionSelectionSaxParserHandler;
import cerberus.data.collection.parser.ParserTokenHandler;
import cerberus.data.collection.virtualarray.VirtualArrayThreadSingleBlock;
import cerberus.manager.IGeneralManager;
import cerberus.manager.ILoggerManager.LoggerType;
import cerberus.xml.parser.ISaxParserHandler;
import cerberus.xml.parser.handler.importer.ascii.AMicroArrayLoader;


/**
 * @author kalkusch
 *
 */
public class MicroArrayLoader1Storage 
extends AMicroArrayLoader {

	private LinkedList<Integer> LLInteger = null;
	
	private LinkedList<Float> LLFloat = null;
	
	private LinkedList<String> LLString = null;
	
	/**
	 * Reference to the current DataStorage.
	 */
	private IStorage refDataStorage;
	
	/**
	 * 
	 */
	private IVirtualArray refImportDataOverrideSelection;
	
	
	/**
	 * 
	 * @param setGeneralManager
	 */
	public MicroArrayLoader1Storage(IGeneralManager setGeneralManager) {
		super( setGeneralManager );
	}
	
	/**
	 * 
	 */
	public MicroArrayLoader1Storage(IGeneralManager setGeneralManager,
			String setFileName) {

		super(setGeneralManager, setFileName); 
		// TODO Auto-generated constructor stub
	}
	
	
	
	protected void allocateStorageBufferForTokenPattern( ) {
		
		if ( LLInteger == null ) {
			LLInteger = new LinkedList<Integer>(); 
		}
		if ( LLFloat == null ) {
			LLFloat = new LinkedList<Float>(); 
		}
		if ( LLString == null ) {
			LLString = new LinkedList<String>(); 
		}
		
	}
	
	
	
	
	/**
	 * Assign a ISet to write the data to.
	 * 
	 * @param refUseSet target set.
	 */
	public final void setTargetSet(ISet refUseSet) {
		this.refImportDataToSet = refUseSet;
	}
	
	/**
	 * Removes all data structures.
	 * 
	 * @see cerberus.xml.parser.IParserObject#destroy()
	 */
	public final void destroy() {
		
		LLInteger.clear();	
		LLFloat.clear();		
		LLString.clear();
		
		alTokenPattern.clear();
		alTokenTargetToParserTokenType.clear();		
		
		LLInteger = null;		
		LLFloat = null;		
		LLString = null;
		
		alTokenPattern = null;
		alTokenTargetToParserTokenType = null;
		
		iIndexPerArray = null;
	}

	@Override
	protected boolean loadDataParseFile(BufferedReader brFile,
			final int iNumberOfLinesInFile ) 
		throws IOException {

		/**
		 * Consistency check: is a Set defined?
		 */
		
		if ( refImportDataToSet == null ) {
			if ( refDataStorage == null ) {
				assert false: "No reference to IStorage was set!";
			
				return false;
			}
			assert false : "deprecated call! need to assign a ISet!";
		}
		else {
			/* refImportDataToSet != null */
			refDataStorage = 
				refImportDataToSet.getStorageByDimAndIndex(0,0);
			refImportDataOverrideSelection = 
				refImportDataToSet.getSelectionByDimAndIndex(0,0);
		}
		
		
		 Vector <String> vecBufferText = new Vector<String>(10);
		    StringBuffer strLineBuffer = new StringBuffer();
		    
			String sLine;
			
			
		    while ( ((sLine = brFile.readLine()) != null)&&
		    		( iLineInFile <= iStopParsingAtLine) )  
		    {
		    	
				//index = sLine.indexOf(" ");
				// 				h_oestat.put( line.substring(0, index), 
				// 					      line.substring( index+1, 
				// 							      line.indexOf("(", index+1) != -1 ? 
				// 							      line.indexOf("(", index+1) : line.length());
		    	
		    	//TODO: remove next lines...
				/*			
		    	if ( sLine.length() > 0 ) 
				    {
						System.out.println( iLineInFile + 
								": " + sLine);
				    }
				*/
				
				if( iLineInFile > this.iStartParsingAtLine ){
					
					boolean bMaintainLoop = true;
					StringTokenizer strTokenText = new StringTokenizer(sLine,"\"");
					
					int iStringIndex = 0;
					
					strLineBuffer.setLength(0);
					vecBufferText.clear();
					
					int iCountTokens = strTokenText.countTokens();
					
					if ( (iCountTokens % 2) == 0 ) {						
						strTokenText = new StringTokenizer (sLine.replace( "\"\"", "\" \"") , "\"");
						//System.out.println("Substitute [\"\"] ==> [\" \"] in line " + iLineInFile );
					}
					
					/**
					 * are there any tokens containing " 
					 */
					if ( iCountTokens > 1) {
						strLineBuffer.append( strTokenText.nextToken() );
						
						boolean bToggle_Buffer = true;
						
						while ( strTokenText.hasMoreTokens() ) {
							String sBuffer = strTokenText.nextToken().trim();
							
							if ( bToggle_Buffer ) {
								vecBufferText.addElement( sBuffer );
								bToggle_Buffer = false;
							}
							else {
								strLineBuffer.append( sBuffer );
								bToggle_Buffer = true;
							}
						}
					} 
					else {
						strLineBuffer.append( sLine );
					}
					
					
					StringTokenizer strToken = new StringTokenizer( new String(strLineBuffer) );
					ListIterator <ParserTokenHandler> iterPerLine = 
						alTokenTargetToParserTokenType.listIterator();
					
					while (( strToken.hasMoreTokens() )&&(bMaintainLoop)) {
						String sTokenObject = strToken.nextToken();

						try {
							ParserTokenHandler bufferIter = iterPerLine.next();
						
							//switch ( iterPerLine.next().getType() ) {
							switch ( bufferIter.getType() ) {
								//case SKIP: do nothing, only consume current token.
								case ABORT:
									bMaintainLoop = false;
									break;
								case INT:
									LLInteger.add( new Integer(sTokenObject) );
									break;
								case FLOAT:
									LLFloat.add( new Float(sTokenObject) );
									break;
								case STRING:	
									LLString.add( vecBufferText.get(iStringIndex) );	
									iStringIndex++;
									break;
								case SKIP:
									break;
								default:
									System.err.println("Unknown label");
									
							} // end switch
						
						} catch ( NoSuchElementException  nsee) {
							/* no ABORT was set. 
							 * since no more tokens are in ParserTokenHandler skip rest of line..*/
							bMaintainLoop = false;
						}
					
					} // end of: while (( strToken.hasMoreTokens() )&&(bMaintainLoop)) {
					
					
					iLineInFile_CurrentDataIndex++;
					
					
				} // end of: if( iLineInFile > this.iHeaderLinesSize) {			
//				else {
//					System.out.println( 
//							" (" + Integer.toString( iLineInFile ) + "/ 0 /" +
//							Integer.toString( iHeaderLinesSize ) + "): " + sLine );					
//				}
				
				iLineInFile++;
				
			
		    } // end: while ((sLine = brFile.readLine()) != null) { 
		    
		refGeneralManager.getSingelton().getLoggerManager().logMsg("  parsed #" + 
				this.iLineInFile_CurrentDataIndex + "  [" + 			
				this.iStartParsingAtLine + " -> " +
				this.iStopParsingAtLine +  "] stoped at line #" +
				(this.iLineInFile-1),
				LoggerType.VERBOSE );
		
		return true;
	}

	@Override
	protected boolean copyDataToInternalDataStructures() {

		   /**
	     * Copy valued to refStorage...
	     */
	    		   
	    refImportDataToSet.setLabel("microarray loader set " + this.getFileName() );
	    refDataStorage.setLabel( "microarray loader storage " + this.getFileName() );
	    
	    /*
	     * notify storage cacheId of changed data...
	     */
	    refDataStorage.setCacheId( refDataStorage.getCacheId() + 1);
	    
	    refDataStorage.setSize(StorageType.INT,1);
	    refDataStorage.setSize(StorageType.FLOAT,1);
	    refDataStorage.setSize(StorageType.STRING,1);
	    
	    if ( LLInteger.size() > 1) {
		    Iterator<Integer> iter_I = LLInteger.iterator();		    
		    int[] intBuffer = new int[LLInteger.size()];		    
		    for ( int i=0; iter_I.hasNext() ;i++ ) {
		    	intBuffer[i]=iter_I.next().intValue();
		    }
		    refDataStorage.setArrayInt( intBuffer );
		    
		    refImportDataOverrideSelection.setLabel("import INTEGER");
		    refImportDataOverrideSelection.setOffset( 0 );
		    refImportDataOverrideSelection.setLength( LLInteger.size() );
		    
		    /*
		     * notify selection cacheId of changed data...
		     */
		    refImportDataOverrideSelection.setCacheId(
		    		refImportDataOverrideSelection.getCacheId() + 1 );
		    
		    refImportDataToSet.setStorageByDimAndIndex(
		    		refDataStorage,0,0);
		    refImportDataToSet.setSelectionByDimAndIndex(
		    		refImportDataOverrideSelection,0,0);
	    }
	    
	    if ( LLFloat.size() > 1) {
		    Iterator<Float> iter_F = LLFloat.iterator();		    
		    float[] floatBuffer = new float[LLFloat.size()];		    
		    for ( int i=0; iter_F.hasNext() ;i++ ) {
		    	floatBuffer[i]=iter_F.next().floatValue();
		    }
		    refDataStorage.setArrayFloat( floatBuffer );
		    
		    IVirtualArray selFloat = 
		    	new VirtualArrayThreadSingleBlock(1,null,null);
		    selFloat.setLabel("import FLOAT");
		    selFloat.setLength( LLFloat.size() );
		    
		    refImportDataToSet.setStorageByDimAndIndex(
		    		refDataStorage,0,1);
		    refImportDataToSet.setSelectionByDimAndIndex(
		    		selFloat,0,1);
	    }
	    
	    if ( LLString.size() > 1) {
		    Iterator<String> iter_S = LLString.iterator();		    
		    String[] stringBuffer = new String[LLString.size()];		    
		    for ( int i=0; iter_S.hasNext() ;i++ ) {
		    	stringBuffer[i]=iter_S.next();
		    }
		    refDataStorage.setArrayString( stringBuffer );
		    
		    IVirtualArray selFloat = 
		    	new VirtualArrayThreadSingleBlock(1, refGeneralManager, null);
		    selFloat.setLabel("import STRING");
		    selFloat.setLength( LLString.size() );
		    
		    refImportDataToSet.setStorageByDimAndIndex(
		    		refDataStorage,0,2);
		    refImportDataToSet.setSelectionByDimAndIndex(
		    		selFloat,0,2);
	    }
	    
	    //TODO: test if cacheId concept works fine...
	    
	    /*
	     * update cacheId of set by calling getCacheId() ...
	     */
	    refImportDataToSet.getCacheId();
	    
		return true;
	}
	
	
	public final boolean setMementoXML_usingHandler( 
			final ISaxParserHandler refSaxHandler ) {
		
		try {
			CollectionSelectionSaxParserHandler handler = 
				(CollectionSelectionSaxParserHandler) refSaxHandler;
			
			setFileName( handler.getXML_MicroArray_FileName() );
			
			int [] iLinkToIdList = 
				handler.getXML_RLE_Random_LookupTable();

			if ( iLinkToIdList.length < 1 ) {
				throw new RuntimeException("MicroArrayLoader1Storage.setMementoXML_usingHandler() failed. need <DataComponentItemDetails type=RandomLookup> tag.");
			}
			try {
				refDataStorage= (IStorage) refGeneralManager.getItem( iLinkToIdList[0] );
				
				setTokenPattern( handler.getXML_MicroArray_TokenPattern().trim() );
				//setTokenPattern( "SKIP;SKIP;SKIP;STRING;STRING;INT;INT;ABORT" );
								
				//loadData();
			}
			catch (NullPointerException npe) {
				refDataStorage = null;
			}
			
			return true;
			
		} catch (NullPointerException npe) {
			
			return false;
		}
	}

	/**
	 * Init data structues. Use this to reset the stat also!
	 * 
	 * @see cerberus.xml.parser.IParserObject#init()
	 */
	public void init() {
		iLineInFile = 1;
		iLineInFile_CurrentDataIndex = 0;
		
		bRequiredSizeOfReadableLines = false;
	}

}
