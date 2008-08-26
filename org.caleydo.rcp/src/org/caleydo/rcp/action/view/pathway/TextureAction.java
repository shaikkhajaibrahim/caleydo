package org.caleydo.rcp.action.view.pathway;

import org.caleydo.core.command.view.rcp.EExternalFlagSetterType;
import org.caleydo.rcp.action.view.AToolBarAction;
import org.eclipse.jface.resource.ImageDescriptor;


public class TextureAction
extends AToolBarAction
{
	public static final String TEXT = "Turn on/off pathway textures";
	public static final String ICON = "resources/icons/general/no_icon_available.png";

	private boolean bEnable = true;
	
	/**
	 * Constructor.
	 */
	public TextureAction(int iViewID)
	{
		super(iViewID);
		
		setText(TEXT);
		setToolTipText(TEXT);
		setImageDescriptor(ImageDescriptor.createFromURL(this.getClass()
				.getClassLoader().getResource(ICON)));
	}
	
	@Override
	public void run()
	{
		super.run();
			
		bEnable = !bEnable;
		
		triggerCmdSExternalFlagSetter(bEnable, EExternalFlagSetterType.PATHWAY_TEXTURES);

	};
}
