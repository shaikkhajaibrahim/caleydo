/**
 * 
 */
package cerberus.command.base;

import cerberus.command.ICommand;
import cerberus.manager.IGeneralManager;
import cerberus.xml.parser.command.CommandQueueSaxType;
import cerberus.xml.parser.parameter.IParameterHandler;
import cerberus.command.base.ACmdCreate_IdTargetLabel;
import cerberus.util.system.StringConversionTool;

/**
 * @author java
 *
 */
public abstract class ACmdCreate_IdTargetLabelAttr 
	extends ACmdCreate_IdTargetLabel
		implements ICommand
{

	protected String sAttribute1;
	
	protected String sAttribute2;
	
	/**
	 * @param refGeneralManager
	 * @param refParameterHandler
	 */
	protected ACmdCreate_IdTargetLabelAttr(final IGeneralManager refGeneralManager,
			final IParameterHandler refParameterHandler)
	{
		super(refGeneralManager, refParameterHandler);
		
		setAttributesBaseParent( refParameterHandler );
	}


	/**
	 * Note: This methode does not call setAttributesBase(IParameterHandler) internal.
	 * 
	 * @see cerberus.command.base.ACmdCreate_IdTargetLabel#setAttributesBase(IParameterHandler)
	 * 
	 * @param refParameterHandler
	 */
	protected final void setAttributesBaseParent( final IParameterHandler refParameterHandler ) {
		
		sAttribute1 = refParameterHandler.getValueString( 
				CommandQueueSaxType.TAG_ATTRIBUTE1.getXmlKey() );
		
		sAttribute2 = refParameterHandler.getValueString( 
				CommandQueueSaxType.TAG_ATTRIBUTE2.getXmlKey() );
	}

}
