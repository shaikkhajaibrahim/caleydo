package org.caleydo.rcp.splashHandlers;

import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.rcp.Application;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.splash.AbstractSplashHandler;

/**
 * Caleydo splash screen with integrated progress bar.
 * 
 * @author Marc Streit
 */
public class ExtensibleSplashHandler
	extends AbstractSplashHandler
{
	private ProgressBar progressBar;
	
	@Override
	public void init(Shell splash)
	{
		// Store the shell
		super.init(splash);

		if (Application.bIsWebstart)
			return;
		
		// Create UI
		createUI();

		// Enter event loop and prevent the RCP application from
		// loading until all work is done
		doEventLoop();
	}

	private void createUI()
	{
		Shell splash = getSplash();
		splash.setLayout(new FillLayout());
		// Force shell to inherit the splash background
		splash.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		// Create the composite
		final Composite composite = new Composite(splash, SWT.NONE);
		
		// Configure layout
		RowLayout layout = new RowLayout(SWT.VERTICAL);
//	    layout.marginLeft = 12;
	    layout.marginTop = 2;
//	    layout.justify = true;
		composite.setLayout(layout);
		
		progressBar = new ProgressBar(composite, SWT.SMOOTH);
		progressBar.setLayoutData(new RowData(getSplash().getSize().x - 5, 20));
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("Loading...");
		label.setForeground(splash.getDisplay().getSystemColor (SWT.COLOR_WHITE));
		label.setLayoutData(new RowData(getSplash().getSize().x - 5, 20));
		
		GeneralManager.get().getSWTGUIManager().setExternalProgressBarAndLabel(
				progressBar, label);
	}
	
	/**
	 * 
	 */
	private void doEventLoop()
	{
		final Shell splash = getSplash();
		if (splash.getDisplay().readAndDispatch() == false)
		{
			splash.getDisplay().sleep();
		}
		
		// Make sure that splash screen remains the active window
		splash.addFocusListener(new FocusListener() 
		{
			public void focusGained(FocusEvent e)
			{
				// nothing to do
			}

			public void focusLost(FocusEvent e)
			{
				splash.forceActive();
			}
		});
	}

	@Override
	public void dispose()
	{		
		if (!Application.bIsWebstart && !Application.bDoExit)
		{
			Application.startCaleydoCore();
		}	
//		super.dispose();
	}
}
