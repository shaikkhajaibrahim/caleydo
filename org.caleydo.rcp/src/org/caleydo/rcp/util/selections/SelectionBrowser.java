package org.caleydo.rcp.util.selections;

import java.util.ArrayList;

import org.caleydo.core.data.mapping.EIDCategory;
import org.caleydo.core.data.selection.ContentSelectionManager;
import org.caleydo.core.data.selection.ContentVAType;
import org.caleydo.core.data.selection.ContentVirtualArray;
import org.caleydo.core.data.selection.ESelectionCommandType;
import org.caleydo.core.data.selection.SelectionCommand;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.selection.StorageSelectionManager;
import org.caleydo.core.data.selection.delta.ContentVADelta;
import org.caleydo.core.data.selection.delta.ISelectionDelta;
import org.caleydo.core.manager.IEventPublisher;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.IUseCase;
import org.caleydo.core.manager.event.AEvent;
import org.caleydo.core.manager.event.AEventListener;
import org.caleydo.core.manager.event.IListenerOwner;
import org.caleydo.core.manager.event.view.ClearSelectionsEvent;
import org.caleydo.core.manager.event.view.SelectionCommandEvent;
import org.caleydo.core.manager.event.view.storagebased.RedrawViewEvent;
import org.caleydo.core.manager.event.view.storagebased.SelectionUpdateEvent;
import org.caleydo.core.manager.event.view.storagebased.VirtualArrayUpdateEvent;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.manager.usecase.EDataDomain;
import org.caleydo.core.view.opengl.canvas.listener.ClearSelectionsListener;
import org.caleydo.core.view.opengl.canvas.listener.ContentVAUpdateListener;
import org.caleydo.core.view.opengl.canvas.listener.IContentVAUpdateHandler;
import org.caleydo.core.view.opengl.canvas.listener.ISelectionCommandHandler;
import org.caleydo.core.view.opengl.canvas.listener.ISelectionUpdateHandler;
import org.caleydo.core.view.opengl.canvas.listener.IViewCommandHandler;
import org.caleydo.core.view.opengl.canvas.listener.RedrawViewListener;
import org.caleydo.core.view.opengl.canvas.listener.SelectionCommandListener;
import org.caleydo.core.view.opengl.canvas.listener.SelectionUpdateListener;
import org.caleydo.rcp.util.info.listener.InfoAreaUpdateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

/**
 * Info area that is located in the side-bar. It shows the current view and the current selection (in a tree).
 * 
 * @author Marc Streit
 * @author Alexander Lex
 */
public class SelectionBrowser
	implements ISelectionUpdateHandler, IContentVAUpdateHandler, ISelectionCommandHandler,
	IViewCommandHandler {

	IGeneralManager generalManager = null;
	IEventPublisher eventPublisher = null;
	
	ContentSelectionManager contentSelectionManager;
	StorageSelectionManager storageSelectionManager;
	
	private Tree selectionTree;
	private TreeItem contentTree;

	
	private Label lblTest;
	private Button btnAdd;
	private Button btnSub;;
	private Composite parentComposite;

	protected SelectionUpdateListener selectionUpdateListener;
	protected ContentVAUpdateListener virtualArrayUpdateListener;
	protected SelectionCommandListener selectionCommandListener;

	protected RedrawViewListener redrawViewListener;
	protected ClearSelectionsListener clearSelectionsListener;
	protected InfoAreaUpdateListener infoAreaUpdateListener;

	/**
	 * Constructor.
	 */
	public SelectionBrowser() {
		generalManager = GeneralManager.get();
		eventPublisher = generalManager.getEventPublisher();
		
		ContentVAType contentVAType = ContentVAType.CONTENT;				
		IUseCase useCase = generalManager.getUseCase(EDataDomain.GENETIC_DATA);
		
		ContentVirtualArray contentVA= useCase.getContentVA(contentVAType);
		contentSelectionManager = useCase.getContentSelectionManager();
		contentSelectionManager.setVA(contentVA);
		
					
			
		registerEventListeners();
	}

	public Control createControl(final Composite parent) {

		parentComposite = parent;
		selectionTree = new Tree(parent, SWT.NULL  | SWT.MULTI );
		
		btnAdd = new Button(parent, SWT.WRAP);
		btnAdd.setText("Add");
		
		btnSub = new Button(parent, SWT.WRAP);
		btnSub.setText("Del");
		
		btnAdd.addSelectionListener(new SelectionListener() {

		      public void widgetSelected(SelectionEvent event) {
		    	  lblTest.setText("Add Clicked!");
		      }

		      public void widgetDefaultSelected(SelectionEvent event) {
		    	  lblTest.setText("Add Default Clicked!");
		      }
		    });
									
		btnSub.addSelectionListener(new SelectionListener() {

			 public void widgetSelected(SelectionEvent event) {
		    	  lblTest.setText("Sub Clicked!");
		      }

		      public void widgetDefaultSelected(SelectionEvent event) {
		    	  lblTest.setText("Sub Default Clicked!");
		      }
		    });
		
		
		
		lblTest = new Label(parent, SWT.WRAP);
		lblTest.setAlignment(SWT.CENTER);		
		lblTest.setText("This is a Test");
	
		
		GridData gridData = new GridData(GridData.FILL_BOTH);

	
		gridData.minimumWidth = 145;
		gridData.widthHint = 145;
		gridData.minimumHeight = 82;
		gridData.heightHint = 82;		

		lblTest.setLayoutData(gridData);
		
		
		
		
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.minimumHeight = 62;
		gridData.heightHint = 156;
		
		if (System.getProperty("os.name").contains("Win")) {
			// In windows the list needs more space because of no multi line
			// support
			gridData.widthHint = 145;
			gridData.minimumWidth = 145;
		}

		

		selectionTree.setLayoutData(gridData);
		
		contentTree = new TreeItem(selectionTree, SWT.NONE);
		contentTree.setExpanded(true);
		contentTree.setData(-1);
		contentTree.setText("Content Selections");
		
		
updateContentTree();
		
		return parent;
	}

	private void updateContentTree(){
		ArrayList<SelectionType> sTypes = contentSelectionManager.getSelectionTypes();
		Color color=null;
		contentTree.removeAll(); 
		for (SelectionType tmpSelectionType : sTypes) 
		{
								
		if (SelectionType.isDefaultType(tmpSelectionType) || tmpSelectionType == SelectionType.DESELECTED)
				continue;
		
		TreeItem item = new TreeItem(contentTree, SWT.NONE);
		
		float[] fArColor = tmpSelectionType.getColor();
		
		color =
			new Color(parentComposite.getDisplay(), (int) (fArColor[0] * 255),
				(int) (fArColor[1] * 255), (int) (fArColor[2] * 255));

		item.setText(tmpSelectionType.toString());
		item.setBackground(color);		
		item.setData(tmpSelectionType.hashCode());
		item.setData("selection_type", tmpSelectionType);

		contentTree.setExpanded(true);
		
		
		}
		
		
		
	}
	
	
	@Override
	public void handleSelectionUpdate(final ISelectionDelta selectionDelta, final boolean scrollToSelection,
		final String info) {
		
		
		parentComposite.getDisplay().asyncExec(new Runnable() {
			public void run() {

				updateContentTree();
//				String tmpString="";
//				if (info != null) {
//					
//					Color color=null;
//					for (SelectionDeltaItem selectionItem : selectionDelta) {
//						
//						SelectionType currentSelection =selectionItem.getSelectionType();
//						
//						int i=contentSelectionManager.getNumberOfElements(currentSelection);
//						
//						tmpString=currentSelection.toString()+"("+i+")";
//						
//						float[] fArColor = currentSelection.getColor();
//						
//						color =
//							new Color(parentComposite.getDisplay(), (int) (fArColor[0] * 255),
//								(int) (fArColor[1] * 255), (int) (fArColor[2] * 255));
//
//						
//
//					}
//					lblTest.setText(tmpString);
//					lblTest.setBackground(color);
//				}
//			
		}
	
		});
	}

	@Override
	public void handleSelectionCommand(EIDCategory category, final SelectionCommand selectionCommand) {
		
		if (parentComposite.isDisposed())
			return;

		parentComposite.getDisplay().asyncExec(new Runnable() {
			public void run() {
				ESelectionCommandType cmdType;
				

				
		
				
				lblTest.setText("Reseted Selection");

				cmdType = selectionCommand.getSelectionCommandType();
				
				
				
				if (cmdType == ESelectionCommandType.RESET || cmdType == ESelectionCommandType.CLEAR_ALL) {
					lblTest.setText("Reseted Selection");
				}
//				else if (cmdType == ESelectionCommandType.CLEAR) {
//					lblViewInfoContent.setText("Cleared current Selection");
//					}

				
			}
		});

	}

	@Override
	public void handleRedrawView() {
		// nothing to do here
	}

	@Override
	public void handleUpdateView() {
		// nothing to do here
	}

	@Override
	public void handleClearSelections() {

	}

	/**
	 * handling method for updates about the info text displayed in the this info-area
	 * 
	 * @param info
	 *            short-info of the sender to display
	 */
	public void handleInfoAreaUpdate(final String info) {
		parentComposite.getDisplay().asyncExec(new Runnable() {
			public void run() {
				lblTest.setText("joschi");
			}
		});
		

	}

	/**
	 * Registers the listeners for this view to the event system. To release the allocated resources
	 * unregisterEventListeners() has to be called.
	 */
	public void registerEventListeners() {
		selectionUpdateListener = new SelectionUpdateListener();
		selectionUpdateListener.setHandler(this);
		eventPublisher.addListener(SelectionUpdateEvent.class, selectionUpdateListener);

		virtualArrayUpdateListener = new ContentVAUpdateListener();
		virtualArrayUpdateListener.setHandler(this);
		eventPublisher.addListener(VirtualArrayUpdateEvent.class, virtualArrayUpdateListener);

		selectionCommandListener = new SelectionCommandListener();
		selectionCommandListener.setHandler(this);
		eventPublisher.addListener(SelectionCommandEvent.class, selectionCommandListener);

		redrawViewListener = new RedrawViewListener();
		redrawViewListener.setHandler(this);
		eventPublisher.addListener(RedrawViewEvent.class, redrawViewListener);

		clearSelectionsListener = new ClearSelectionsListener();
		clearSelectionsListener.setHandler(this);
		eventPublisher.addListener(ClearSelectionsEvent.class, clearSelectionsListener);
	}

	/**
	 * Unregisters the listeners for this view from the event system. To release the allocated resources
	 * unregisterEventListenrs() has to be called.
	 */
	public void unregisterEventListeners() {
		if (selectionUpdateListener != null) {
			eventPublisher.removeListener(selectionUpdateListener);
			selectionUpdateListener = null;
		}
		if (virtualArrayUpdateListener != null) {
			eventPublisher.removeListener(virtualArrayUpdateListener);
			virtualArrayUpdateListener = null;
		}
		if (selectionCommandListener != null) {
			eventPublisher.removeListener(selectionCommandListener);
			selectionCommandListener = null;
		}
		if (redrawViewListener != null) {
			eventPublisher.removeListener(redrawViewListener);
			redrawViewListener = null;
		}
		if (clearSelectionsListener != null) {
			eventPublisher.removeListener(clearSelectionsListener);
			clearSelectionsListener = null;
		}
	}

	public void dispose() {
		unregisterEventListeners();
	}

	@Override
	public synchronized void queueEvent(final AEventListener<? extends IListenerOwner> listener,
		final AEvent event) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				listener.handleEvent(event);
			}
		});
	}

	@Override
	public void handleContentVAUpdate(ContentVADelta vaDelta, final String info) {
	
	}

	@Override
	public void replaceContentVA(EIDCategory idCategory, ContentVAType vaType) {
		// TODO Auto-generated method stub
	}

}
