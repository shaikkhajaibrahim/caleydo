/**
 * 
 */
package cerberus.xml.parser.handler.command;


//import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
//import org.xml.sax.helpers.DefaultHandler;

import cerberus.command.CommandType;
import cerberus.command.ICommand;
import cerberus.command.queue.ICommandQueue;
import cerberus.manager.ICommandManager;
import cerberus.manager.IGeneralManager;
import cerberus.manager.ILoggerManager.LoggerType;
//import cerberus.manager.IMenuManager;
//import cerberus.util.exception.CerberusRuntimeException;
import cerberus.xml.parser.command.CommandQueueSaxType;
//import cerberus.xml.parser.ACerberusDefaultSaxHandler;
import cerberus.xml.parser.handler.AXmlParserHandler;
import cerberus.xml.parser.handler.IXmlParserHandler;
import cerberus.xml.parser.handler.SXmlParserHandler;
import cerberus.xml.parser.manager.IXmlParserManager;
import cerberus.xml.parser.parameter.IParameterHandler;
import cerberus.xml.parser.parameter.IParameterHandler.ParameterHandlerType;
import cerberus.xml.parser.parameter.ParameterHandler;


/**
 * Create Menus in Frames from XML file.
 * 
 * @author java
 *
 */
public class CommandSaxHandler 
extends AXmlParserHandler
implements IXmlParserHandler 
{
	
	/* XML Tags */		
	private final String sTag_Command = 
		CommandQueueSaxType.TAG_CMD.getXmlKey();
	
	private final String sTag_CommandQueue = 
		CommandQueueSaxType.TAG_CMD_QUEUE.getXmlKey();
	/* END: XML Tags */
	
	private final ICommandManager refCommandManager;
	
	/**
	 * Since the opening tag is handled by the extenal handler
	 * this fal is set to true by default.
	 */
	private boolean bCommandBuffer_isActive = false;
	
	private boolean bCommandQueue_isActive = false;

	
	protected ICommandQueue refCommandQueueIter = null;


	/**
	 * <Application >
	 *  <CommandBuffer>
	 *    <Cmd />
	 *    <Cmd />
	 *  </CommandBuffer>
	 * </Application>
	 */
	public CommandSaxHandler( final IGeneralManager setGeneralManager,
			final IXmlParserManager refXmlParserManager ) {
		
		super( setGeneralManager, refXmlParserManager );
		
		setXmlActivationTag( "CommandBuffer" );
		
		refCommandManager = 
			refGeneralManager.getSingelton().getCommandManager();

		assert refCommandManager != null : "ICommandManager was not created by ISingelton!";
	}

	
	/**
	 * 
	 * Read values of class: iCurrentFrameId
	 * 
	 * @param attrs
	 * @param bIsExternalFrame
	 */
	protected ICommand readCommandData( final Attributes attrs, boolean bIsExternalFrame ) {
		
		ICommand lastCommand = null;
		
		IParameterHandler phAttributes =
			new ParameterHandler();
	
		try 
		{
			/* create new Frame */
			phAttributes.setValueBySaxAttributes( attrs, 
					CommandQueueSaxType.TAG_PROCESS.getXmlKey(), 
					CommandQueueSaxType.TAG_PROCESS.getDefault(),
					ParameterHandlerType.STRING );
			
			phAttributes.setValueBySaxAttributes( attrs, 
					CommandQueueSaxType.TAG_LABEL.getXmlKey(), 
					CommandQueueSaxType.TAG_LABEL.getDefault(),
					ParameterHandlerType.STRING );
			
			phAttributes.setValueBySaxAttributes( attrs,
					CommandQueueSaxType.TAG_CMD_ID.getXmlKey(),
					CommandQueueSaxType.TAG_CMD_ID.getDefault(),
					ParameterHandlerType.INT );
			
			phAttributes.setValueBySaxAttributes( attrs,
					CommandQueueSaxType.TAG_TARGET_ID.getXmlKey(),
					CommandQueueSaxType.TAG_TARGET_ID.getDefault(),
					ParameterHandlerType.INT );
			
			phAttributes.setValueBySaxAttributes( attrs,
					CommandQueueSaxType.TAG_MEMENTO_ID.getXmlKey(),
					CommandQueueSaxType.TAG_MEMENTO_ID.getDefault(),
					ParameterHandlerType.INT );
			
			phAttributes.setValueBySaxAttributes( attrs,
					CommandQueueSaxType.TAG_TYPE.getXmlKey(), 
					CommandQueueSaxType.TAG_TYPE.getDefault(),
					ParameterHandlerType.STRING );

			phAttributes.setValueBySaxAttributes( attrs,
					CommandQueueSaxType.TAG_PARENT.getXmlKey(), 
					CommandQueueSaxType.TAG_PARENT.getDefault(),
					ParameterHandlerType.INT );	
			
			phAttributes.setValueBySaxAttributes( attrs,
					CommandQueueSaxType.TAG_ATTRIBUTE1.getXmlKey(), 
					CommandQueueSaxType.TAG_ATTRIBUTE1.getDefault(),
					ParameterHandlerType.STRING );	
			
			phAttributes.setValueBySaxAttributes( attrs,
					CommandQueueSaxType.TAG_ATTRIBUTE2.getXmlKey(), 
					CommandQueueSaxType.TAG_ATTRIBUTE2.getDefault(),
					ParameterHandlerType.STRING );	
			
			phAttributes.setValueBySaxAttributes( attrs,
					CommandQueueSaxType.TAG_ATTRIBUTE3.getXmlKey(), 
					CommandQueueSaxType.TAG_ATTRIBUTE3.getDefault(),
					ParameterHandlerType.STRING );	
				
			phAttributes.setValueBySaxAttributes( attrs,
					CommandQueueSaxType.TAG_DETAIL.getXmlKey(), 
					CommandQueueSaxType.TAG_DETAIL.getDefault(),
					ParameterHandlerType.STRING );										 

			refGeneralManager.getSingelton().getLoggerManager().logMsg(
					"XML-TAG= " +  phAttributes.getValueString( 
					CommandQueueSaxType.TAG_LABEL.getXmlKey() ),
					LoggerType.VERBOSE );
				
			lastCommand = refCommandManager.createCommand( phAttributes );
			
		}
		catch ( Exception e) 
		{
			refGeneralManager.getSingelton().getLoggerManager().logMsg(
					"CommandSaxHandler.readCommandData(" +
					attrs.toString() + ") ERROR while parsing " + 
					lastCommand.toString() + " error=" + e.toString(),
					LoggerType.STATUS );
					
			return null;
		}
			
		try 
		{
			if ( lastCommand != null )
			{
				String sData_Cmd_process = phAttributes.getValueString( 
						CommandQueueSaxType.TAG_PROCESS.getXmlKey() );
				
				if (sData_Cmd_process.equals( CommandQueueSaxType.RUN_CMD_NOW.toString() ))
				{				
					refGeneralManager.getSingelton().getLoggerManager().logMsg("do command: " + 
						lastCommand.toString(),
						LoggerType.VERBOSE );
					lastCommand.doCommand();
				}
			}
			
			return lastCommand;
			
		}
		catch ( Exception e) 
		{
			refGeneralManager.getSingelton().getLoggerManager().logMsg(
					"CommandSaxHandler.readCommandData(" +
					attrs.toString() + ")\n  ERROR while executing command " + e.toString(),
					LoggerType.STATUS );
					
			
			return null;
		}
	}
	
	/**
	 * 
	 * Read values of class: iCurrentFrameId
	 * @param attrs
	 * @param bIsExternalFrame
	 */
	protected void readCommandQueueData( final Attributes attrs, boolean bIsExternalFrame ) {
		
		ICommand lastCommand = null;
		
		String sData_Queue_process = 
			CommandQueueSaxType.RUN_QUEUE_ON_DEMAND.toString();
		String sData_Queue_type = 
			CommandQueueSaxType.COMMAND_QUEUE_RUN.toString();
		
		int iData_Queue_CmdId;
		int iData_Queue_CmdQueueId;
		int iData_Queue_ThreadPool_Id = -1;					
		int iData_Queue_ThreadPool_Wait_Id = -1;
		
		
		try 
		{
			
			
			/* create new Frame */
			sData_Queue_process = 
				SXmlParserHandler.assignStringValue( attrs, 
					CommandQueueSaxType.RUN_QUEUE_ON_DEMAND.getXmlKey(), 
					CommandQueueSaxType.RUN_QUEUE_ON_DEMAND.toString() );
			
			iData_Queue_CmdId = 
				SXmlParserHandler.assignIntValueIfValid( attrs, 
					CommandQueueSaxType.CMD_ID.getXmlKey(),
					-1  );
			
			iData_Queue_CmdQueueId = 
				SXmlParserHandler.assignIntValueIfValid( attrs, 
					CommandQueueSaxType.CMDQUEUE_ID.getXmlKey(),
					-1  );
			
			sData_Queue_type = 
				SXmlParserHandler.assignStringValue( attrs,
					CommandQueueSaxType.COMMAND_QUEUE_RUN.getXmlKey(), 
					CommandQueueSaxType.COMMAND_QUEUE_RUN.toString() );		
			
			iData_Queue_ThreadPool_Id = 
				SXmlParserHandler.assignIntValueIfValid( attrs, 
					CommandQueueSaxType.CMD_THREAD_POOL_ID.getXmlKey(),
					-1  );
						
			iData_Queue_ThreadPool_Wait_Id = 
				SXmlParserHandler.assignIntValueIfValid( attrs, 
					CommandQueueSaxType.CMD_THREAD_POOL_WAIT_ID.getXmlKey(),
					-1  );
			
			lastCommand = refCommandManager.createCommandQueue( 
					sData_Queue_type,
					sData_Queue_process,
					iData_Queue_CmdId,
					iData_Queue_CmdQueueId,
					iData_Queue_ThreadPool_Id,
					-iData_Queue_ThreadPool_Wait_Id );
				
		}
		catch ( Exception e) 
		{
			System.err.println("CommandSaxHandler::readCommandQueueData() ERROR while parsing " + e.toString() );
		}
		
		
		
		if ( sData_Queue_type.equals( CommandType.COMMAND_QUEUE_RUN.toString() )) {
			
			if ( sData_Queue_process.equals( "RUN_QUEUE" )) {
				lastCommand.doCommand();
				
				refCommandQueueIter = null;
			}
			
		} 
		else if ( sData_Queue_type.equals( CommandType.COMMAND_QUEUE_OPEN.toString() )) {
			
			refCommandQueueIter = (ICommandQueue) lastCommand;
			
		}

//			throw new CerberusRuntimeException( "can not create command from [" +
//					attrs.toString() + "]");
	}
	
	
	/**
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes attrs) throws SAXException {
		
		String eName = ("".equals(localName)) ? qName : localName;
		
		if (null != eName) {
				
				if (eName.equals(sOpeningTag)) {
					/* <sFrameStateTag> */
					if ( bCommandBuffer_isActive ) {
						throw new SAXException ( "<" + sOpeningTag + "> already opened!");
					} else {
						bCommandBuffer_isActive = true;
						return;
					}
					
				} //end: if (eName.equals(sFrameStateTag)) {
				else if (eName.equals(sTag_Command)) {
					
					
					
					if ( bCommandBuffer_isActive ) {						
						/**
						 * <CommandBuffer>
						 *   ... 
						 *  <Cmd ...> 
						 */
						
						if  ( bCommandQueue_isActive ) {
							/**
							 * <CommandBuffer>
							 * ...
							 * <CmdQueue> <br>
							 *  ...
							 * <Cmd ...>
							 */
							
							//readCommandQueueData( attrs, true );
							ICommand lastCommand = 
								readCommandData( attrs, true );
							
							if ( lastCommand != null ) {
								refCommandQueueIter.addCmdToQueue( lastCommand );
							} 
							else 
							{
								refGeneralManager.getSingelton().getLoggerManager().logMsg(
										"CommandQueue: no Command to add. skip it.");
							}
							
							
														
						} else {
							/**
							 * <CommandBuffer>
							 * ...
							 * <Cmd ...>
							 */
							
							//readCommandQueueData( attrs, true );
							ICommand lastCommand = readCommandData( attrs, true );
							
							if ( lastCommand == null ) 
							{
								refGeneralManager.getSingelton().getLoggerManager().logMsg(
										"Command: can not execute command due to error while parsing. skip it.");
							}
							
						

							
						}
						

						
					}  //if ( bCommandBuffer_isActive ) {
					else 
					{ 
						throw new SAXException ( "<"+ sTag_Command + "> opens without <" + 
								sOpeningTag + "> being opened!");
					}
				}
				else if (eName.equals( sTag_Command )) {
					
					/**
					 *  <CmdQueue ...> 
					 */
					if ( bCommandBuffer_isActive ) {
						
						if ( bCommandQueue_isActive ) {
							throw new SAXException ( "<"+ sTag_CommandQueue + "> opens inside a <" + 
									sTag_CommandQueue + "> block!");
						}
						
						bCommandQueue_isActive = true;
						
						readCommandQueueData( attrs, true );
						

						
					} else {
						throw new SAXException ( "<"+ sTag_Command + "> opens without <" + 
								sOpeningTag + "> being opened!");
					}
					
				}
		}
	}

	/**
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		
			
			String eName = ("".equals(localName)) ? qName : localName;
		
			if (null != eName) {
				if (eName.equals(sOpeningTag)) {	
					
					/* </CommandBuffer> */
					if ( bCommandBuffer_isActive ) {
						bCommandBuffer_isActive = false;
						
						/**
						 * section (xml block) finished, call callback function from IXmlParserManager
						 */
						refXmlParserManager.sectionFinishedByHandler( this );
						
						return;
					} else {
						throw new SAXException ( "<" + sOpeningTag + "> was already closed.");
					}	
					
				} 
				else if (eName.equals(sTag_Command)) {	
					
					/* </cmd> */
					if ( ! bCommandBuffer_isActive ) {
						throw new SAXException ( "<" + sTag_Command + "> opens without " + 
								sOpeningTag + " being opened.");
					}											
					
				}
				else if (eName.equals( sTag_CommandQueue )) {
					
					/**
					 *  </CmdQueue ...> 
					 */
					if ( bCommandBuffer_isActive ) {
						
						bCommandQueue_isActive = false;
					} else {
						throw new SAXException ( "<" + sTag_CommandQueue + "> opens without " + 
								sOpeningTag + " being opened.");
					}
				}
				
				// end:else if (eName.equals(...)) {	
			} //end: if (null != eName) {
			
	}

	public void characters(char[] buf, int offset, int len) throws SAXException {
	
	}


	public String createXML( final Object frame, final String sIndent ) {
		String result = sIndent;		

		
//		if ( frame.getClass().equals( SwingJoglJFrame.class )) {
//			SwingJoglJFrame jframe = (SwingJoglJFrame) frame;
//			
////			result += "<" + sMenuTag;
////			
////			iCurrentFrameId = jframe.getId();
////			dim = jframe.getSize();
////			location = jframe.getLocation();			
////			bIsVisible = jframe.isVisible();
////			sTypeName = jframe.getFrameType().getTypeNameForXML();
////			sTitle = jframe.getTitle();
////			sName = jframe.getName();
////			sClosingTag = ">\n";
//			
//		} else if ( frame.getClass().equals( SwingJoglJInternalFrame.class )) {
//			SwingJoglJInternalFrame jiframe = (SwingJoglJInternalFrame) frame;
//			
////			result += "<" + sInternalFrameTag;
////			
////			iCurrentFrameId = jiframe.getId();
////			dim = jiframe.getSize();
////			location = jiframe.getLocation();			
////			bIsVisible = jiframe.isVisible();
////			sTitle = jiframe.getTitle();
////			sTypeName = jiframe.getFrameType().getTypeNameForXML();
////			sName = jiframe.getName();
////			sClosingTag = "> </" + sInternalFrameTag + ">\n";
//			
//		} else {
//			throw new RuntimeException("Can not create XML string from class [" +
//					frame.getClass().getName() + "] ;only support SwingJoglJFrame and SwingJoglJInternalFrame");
//		}
//		
////		result +=          " " + sMenuKey_objectId + sArgumentBegin + Integer.toString(iData_MenuId);
//		result += sArgumentEnd + sMenuKey_processType + sArgumentBegin + Integer.toString(this.iData_TargetFrameId);
//		result += sArgumentEnd + sMenuKey_commandId + sArgumentBegin + Integer.toString(this.iData_CommandId);
////		result += sArgumentEnd + sMenuKey_parentMenuId + sArgumentBegin + Integer.toString(this.iData_MenuParentId);;
//		result += sArgumentEnd + sCmdKey_type + sArgumentBegin + sData_MenuType;
//		
////		result += sArgumentEnd + sMenuKey_enabled + sArgumentBegin + Boolean.toString(bData_EnableMenu);	
//		result += sArgumentEnd + sMenuKey_memento + sArgumentBegin + sData_MenuMemento;
//		
////		result += sArgumentEnd + sMenuKey_title + sArgumentBegin + sData_MenuTitle;
//		result += sArgumentEnd + sMenuKey_details + sArgumentBegin + sData_MenuTooltip;					
//				
//		
		return result;
	}

		
	public void initHandler() {
		refGeneralManager.getSingelton().getLoggerManager().logMsg(
				"CommandSaxHandler.initHandler()",
				LoggerType.STATUS );
	}
	
	
	/**
	 * Cleanup called by Mananger after Handler is not used any more. 
	 */
	public void destroyHandler() {
		
		if ( refCommandQueueIter != null ) {
			refCommandQueueIter.destroy();
			refCommandQueueIter = null;
		}
		
		refGeneralManager.getSingelton().getLoggerManager().logMsg(
				"CommandSaxHandler.destroyHandler() free memory!",
				LoggerType.STATUS );
	}
	
}
