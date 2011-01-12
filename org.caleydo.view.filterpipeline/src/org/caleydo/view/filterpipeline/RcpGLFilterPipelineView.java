package org.caleydo.view.filterpipeline;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.manager.event.AEvent;
import org.caleydo.core.manager.event.AEventListener;
import org.caleydo.core.manager.event.IListenerOwner;
import org.caleydo.rcp.view.rcp.ARcpGLViewPart;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 * TODO: DOCUMENT ME!
 * 
 * @author <INSERT_YOUR_NAME>
 */
public class RcpGLFilterPipelineView
	extends ARcpGLViewPart
	implements IListenerOwner
{
	public static final String VIEW_ID = "org.caleydo.view.filterpipeline";

	/**
	 * Constructor.
	 */
	public RcpGLFilterPipelineView()
	{
		super();

		try
		{
			viewContext = JAXBContext.newInstance(SerializedFilterPipelineView.class);
		}
		catch (JAXBException ex)
		{
			throw new RuntimeException("Could not create JAXBContext", ex);
		}

		eventPublisher = GeneralManager.get().getEventPublisher();
		registerEventListeners();
	}

	@Override
	public void createPartControl(Composite parent)
	{
		super.createPartControl(parent);

		createGLCanvas();
		view = new GLFilterPipeline(glCanvas, serializedView.getViewFrustum());
		view.initFromSerializableRepresentation(serializedView);
		view.initialize();
		createPartControlGL();
	}

	@Override
	public void createDefaultSerializedView()
	{
		serializedView = new SerializedFilterPipelineView();
		determineDataDomain(serializedView);
	}

	@Override
	public String getViewGUIID()
	{
		return GLFilterPipeline.VIEW_ID;
	}

	@Override
	public void queueEvent(final AEventListener<? extends IListenerOwner> listener,
			final AEvent event)
	{
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				listener.handleEvent(event);
			}
		});
	}

	@Override
	public void registerEventListeners()
	{
		
	}

	@Override
	public void unregisterEventListeners()
	{
		
	}

}