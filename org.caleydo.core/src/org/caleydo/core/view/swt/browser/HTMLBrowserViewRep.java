package org.caleydo.core.view.swt.browser;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import org.caleydo.core.view.AView;
import org.caleydo.core.view.IView;
import org.caleydo.core.view.ViewType;
import org.caleydo.core.view.opengl.util.EIconTextures;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Simple HTML browser.
 * 
 * @author Marc Streit
 */
public class HTMLBrowserViewRep
	extends AView
	implements IView
{
	public final static String CALEYDO_HOME = "http://www.caleydo.org";

	protected Browser browser;

	protected String sUrl = CALEYDO_HOME;

	protected Text textURL;

	protected IDExtractionLocationListener idExtractionLocationListener;
	
	private ToolItem goButton;
	private ToolItem homeButton;
	private ToolItem backButton;
	private ToolItem stopButton;

	/**
	 * Constructor.
	 */
	public HTMLBrowserViewRep(final int iParentContainerId, final String sLabel)
	{
		super(iParentContainerId, sLabel, ViewType.SWT_HTML_BROWSER);
	}
	
	@Override
	protected void initViewSwtComposite(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,true));
		
		Composite browserBarComposite = new Composite(composite, SWT.NONE);
		browserBarComposite.setLayout(new GridLayout(2, false));
		
		ToolBar toolbar = new ToolBar(browserBarComposite, SWT.NONE);
		GridData data = new GridData(GridData.FILL_VERTICAL);
//		toolbar.setLayoutData(data);
		
		goButton = new ToolItem(toolbar, SWT.PUSH);

		if (getClass().getClassLoader().getResourceAsStream(
				EIconTextures.BROWSER_REFRESH_IMAGE.getFileName()) != null)
		{
			goButton.setImage(new Image(parent.getDisplay(), 
					getClass().getClassLoader().getResourceAsStream(
							EIconTextures.BROWSER_REFRESH_IMAGE.getFileName())));
		}
		else
		{
			goButton.setImage(new Image(parent.getDisplay(), 
					EIconTextures.BROWSER_REFRESH_IMAGE.getFileName()));
		}

		backButton = new ToolItem(toolbar, SWT.PUSH);
		if (generalManager.getClass().getClassLoader().getResourceAsStream(
				EIconTextures.BROWSER_BACK_IMAGE.getFileName()) != null)
		{
			backButton.setImage(new Image(parent.getDisplay(), 
					this.getClass().getClassLoader().getResourceAsStream(
							EIconTextures.BROWSER_BACK_IMAGE.getFileName())));
		}
		else
		{
			backButton.setImage(new Image(parent.getDisplay(), 
					EIconTextures.BROWSER_BACK_IMAGE.getFileName()));
		}

		stopButton = new ToolItem(toolbar, SWT.PUSH);
		if (generalManager.getClass().getClassLoader().getResourceAsStream(
				EIconTextures.BROWSER_STOP_IMAGE.getFileName()) != null)
		{
			stopButton.setImage(new Image(parent.getDisplay(), 
					this.getClass().getClassLoader().getResourceAsStream(
							EIconTextures.BROWSER_STOP_IMAGE.getFileName())));
		}
		else
		{
			stopButton.setImage(new Image(parent.getDisplay(), 
					EIconTextures.BROWSER_STOP_IMAGE.getFileName()));
		}

		homeButton = new ToolItem(toolbar, SWT.PUSH);

		if (getClass().getClassLoader().getResourceAsStream(
				EIconTextures.BROWSER_HOME_IMAGE.getFileName()) != null)
		{
			homeButton.setImage(new Image(parent.getDisplay(), 
					getClass().getClassLoader().getResourceAsStream(
							EIconTextures.BROWSER_HOME_IMAGE.getFileName())));
		}
		else
		{
			homeButton.setImage(new Image(parent.getDisplay(), 
					EIconTextures.BROWSER_HOME_IMAGE.getFileName()));
		}
		
		textURL = new Text(browserBarComposite, SWT.BORDER);
		
		if (checkInternetConnection())
			textURL.setText(sUrl);
		
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = 15;
		textURL.setLayoutData(data);

		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.heightHint = 45;
		browserBarComposite.setLayoutData(data);

		Listener listener = new Listener()
		{
			public void handleEvent(Event event)
			{
				if (!checkInternetConnection())
					return;
				
				ToolItem item = (ToolItem) event.widget;
				if (item.equals(backButton))
				{
					browser.back();
				}
				else if (item.equals(stopButton))
				{
					browser.stop();
				}
				else if (item.equals(goButton))
				{
					sUrl = textURL.getText();
				}
				else if (item.equals(homeButton))
				{
					sUrl = "www.caleydo.org";
					textURL.setText(CALEYDO_HOME);
					browser.setUrl(sUrl);
				}
			}
		};

		goButton.addListener(SWT.Selection, listener);
		backButton.addListener(SWT.Selection, listener);
		stopButton.addListener(SWT.Selection, listener);
		homeButton.addListener(SWT.Selection, listener);

		textURL.addListener(SWT.DefaultSelection, new Listener()
		{
			public void handleEvent(Event e)
			{
				sUrl = textURL.getText();
				drawView();
			}
		});

		parent.getDisplay().addFilter(SWT.FocusIn, new Listener()
		{

			public void handleEvent(Event event)
			{

				if (!(event.widget.getClass().equals(this.getClass())))
					return;
			}
		});

		browser = new Browser(composite, SWT.BORDER);

		idExtractionLocationListener = new IDExtractionLocationListener(browser, iUniqueID, -1);
		browser.addLocationListener(idExtractionLocationListener);

		data = new GridData();
		browser.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,true));
	}

	public void drawView()
	{
		generalManager.getLogger().log(Level.INFO, "Load " + sUrl);

		try
		{
			parent.getDisplay().asyncExec(new Runnable()
			{

				public void run()
				{
					if (!checkInternetConnection())
						return;
					
					textURL.setText(sUrl);
					browser.setUrl(sUrl);
					// browser.refresh();
				}
			});
		}
		catch (SWTException swte)
		{
			generalManager.getLogger().log(Level.SEVERE, "Error while loading " + sUrl);
		}
	}

	public void setUrl(String sUrl)
	{
		this.sUrl = sUrl;

		idExtractionLocationListener.updateSkipNextChangeEvent(true);
		drawView();
	}
	
	
	protected boolean checkInternetConnection()
	{
		 // Check internet connection
		try
		{
			InetAddress.getByName("www.google.at");

		}
		catch (UnknownHostException e)
		{
			textURL.setText("No internet connection available!");
			return false;
		}
		
		return true;
	}
}
