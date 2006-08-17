/**
 * 
 */
package cerberus.xml.parser.manager;

import org.xml.sax.ContentHandler;

import cerberus.manager.IGeneralManager;
import cerberus.xml.parser.handler.IXmlParserHandler;


/**
 * @author kalkusch
 *
 */
public interface IXmlParserManager 
extends ContentHandler
{

	/**
	 * Returns the reference to the prometheus.app.SingeltonManager.
	 * 
	 * Note: Do not forget to set the reference to the SingeltonManager inside the constructor.
	 *
	 * @param handler register handler to an opening tag.
	 * @param bOpeningTagExistsOnlyOnce Defines if tag may be present several times. If TURE closing tag unregistes handler and calls destroyHandler()
	 *
	 * @return reference to SingeltonManager
	 */
	public IGeneralManager getManager();
	
	/**
	 * Register a SaxHandler by its opening Tag.
	 * 	 
	 * @see cerberus.xml.parser.handler.IXmlParserHandler#initHandler()
	 * 
	 * @param handler register handler to an opening tag.
	 * @param sOpeningAndClosingTag defines opening and closing tag tiggering the handler to become active.
	 * 
	 * @return TRUE if Handler could be register and FALSE if either handler or its associated opening Tag was already registered.
	 */
	public boolean registerSaxHandler( final IXmlParserHandler handler,
			final boolean bOpeningTagExistsOnlyOnce );		
	
	
	/**
	 * Calls cerberus.xml.parser.base.IXmlParserHandler#destroyHandler()
	 * 
	 * @see cerberus.xml.parser.handler.IXmlParserHandler#destroyHandler()
	 * 
	 * @param handler handel, that should be unregistered.
	 * 
	 * @return TRUE if handle was removed.
	 */
	public boolean unregisterSaxHandler( final IXmlParserHandler handler );	
	
	/**
	 * Unregister a Handler by its String
	 * 
	 * @param sOpeningAndClosingTag tag to identify handler.
	 * 
	 * @return TRUE if handle was unregistered.
	 */
	public boolean unregisterSaxHandler( final String sActivationXmlTag );	
	
	
	/**
	 * Callback called by cerberus.xml.parser.handler.IXmlParserHandler if clasing tag is read in endElement()
	 * 
	 * @see cerberus.xml.parser.handler.IXmlParserHandler
	 * @see orl.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 * 
	 * @param handler calling handler, that just read its closing tag
	 * 
	 */
	public void sectionFinishedByHandler(IXmlParserHandler handler);
}
