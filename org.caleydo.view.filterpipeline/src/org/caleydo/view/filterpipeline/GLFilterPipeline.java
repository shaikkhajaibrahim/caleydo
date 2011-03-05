package org.caleydo.view.filterpipeline;

import gleem.linalg.Vec2f;
import java.awt.Font;
import java.util.LinkedList;
import java.util.List;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import org.caleydo.core.data.collection.EStorageType;
import org.caleydo.core.data.filter.ContentMetaOrFilter;
import org.caleydo.core.data.filter.Filter;
import org.caleydo.core.data.filter.event.FilterUpdatedEvent;
import org.caleydo.core.data.filter.event.ReEvaluateContentFilterListEvent;
import org.caleydo.core.data.filter.event.ReEvaluateStorageFilterListEvent;
import org.caleydo.core.data.mapping.IDCategory;
import org.caleydo.core.data.mapping.IDType;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.selection.delta.ISelectionDelta;
import org.caleydo.core.data.virtualarray.EVAOperation;
import org.caleydo.core.data.virtualarray.IVirtualArray;
import org.caleydo.core.manager.datadomain.ASetBasedDataDomain;
import org.caleydo.core.manager.datadomain.DataDomainManager;
import org.caleydo.core.manager.event.view.filterpipeline.SetFilterTypeEvent;
import org.caleydo.core.manager.event.view.filterpipeline.SetFilterTypeEvent.FilterType;
import org.caleydo.core.manager.picking.EPickingMode;
import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.manager.picking.Pick;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.logging.Logger;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.DetailLevel;
import org.caleydo.core.view.opengl.canvas.GLCaleydoCanvas;
import org.caleydo.core.view.opengl.canvas.listener.ISelectionUpdateHandler;
import org.caleydo.core.view.opengl.canvas.listener.IViewCommandHandler;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.util.GLCoordinateUtils;
import org.caleydo.core.view.opengl.util.draganddrop.DragAndDropController;
import org.caleydo.core.view.opengl.util.text.CaleydoTextRenderer;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;
import org.caleydo.view.filterpipeline.listener.FilterUpdateListener;
import org.caleydo.view.filterpipeline.listener.ReEvaluateFilterListener;
import org.caleydo.view.filterpipeline.listener.SetFilterTypeListener;
import org.caleydo.view.filterpipeline.renderstyle.FilterPipelineRenderStyle;
import org.caleydo.view.filterpipeline.representation.Background;
import org.caleydo.view.filterpipeline.representation.FilterMenu;
import org.caleydo.view.filterpipeline.representation.FilterRepresentation;
import org.caleydo.view.filterpipeline.representation.FilterRepresentationMetaOr;
import org.caleydo.view.filterpipeline.representation.FilterRepresentationMetaOrAdvanced;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;


/**
 * TODO
 * 
 * @author Thomas Geymayer
 */

public class GLFilterPipeline
	extends AGLView
	implements IViewCommandHandler, ISelectionUpdateHandler, IRadialMenuListener
{

	public final static String VIEW_ID = "org.caleydo.view.filterpipeline";

	private FilterPipelineRenderStyle renderStyle;
	private DragAndDropController dragAndDropController;
	private SelectionManager selectionManager;	
	private List<FilterItem<?>> filterList = new LinkedList<FilterItem<?>>();
	
	private FilterUpdateListener filterUpdateListener;
	private SetFilterTypeListener setFilterTypeListener;
	private ReEvaluateFilterListener reEvaluateFilterListener;
	
	private ASetBasedDataDomain dataDomain;
	private FilterType filterType = FilterType.CONTENT;
	
	/**
	 * First filter to be displayed. All filters before are hidden and the
	 * height of the first filter shall fill the whole view.
	 */
	private int firstFilter = 0;
	
	/**
	 * The filtered items of this filter will be ignored, so that we can
	 * see what the filter pipeline would look like without this filter.
	 * 
	 * Set to -1 if no filter should be ignored.
	 */
	private int ignoredFilter = -1;
	
	/**
	 * The filter which should be showed in full size, which showing all
	 * filtered items, even those which don't arrive as input because they
	 * have been filtered before.
	 * 
	 * Set to -1 if no filter should be showed full sized.
	 */
	private int fullSizedFilter = -1;
	
	/**
	 * The size a filter should have in OpenGL units. The x value is absolute,
	 * the y value is per 100 elements
	 */
	private Vec2f filterSize = null;
	
	private boolean pipelineNeedsUpdate = true;
	private Vec2f mousePosition = new Vec2f();
	
	private Background background = null;
	private RadialMenu radialMenu = null;
	private FilterMenu filterMenu = null;

	private boolean bControlPressed = false;

	/**
	 * Constructor.
	 * 
	 * @param glCanvas
	 * @param sLabel
	 * @param viewFrustum
	 */
	public GLFilterPipeline(GLCaleydoCanvas glCanvas, final ViewFrustum viewFrustum)
	{
		super(glCanvas, viewFrustum, true);

		viewType = GLFilterPipeline.VIEW_ID;
		dataDomain =
			(ASetBasedDataDomain) DataDomainManager.get().getDataDomain
			(
				"org.caleydo.datadomain.genetic"
			);
		
		dragAndDropController = new DragAndDropController(this);
		glKeyListener = new GLFilterPipelineKeyListener(this);
	}

	@Override
	public void init(GL2 gl)
	{
		// renderStyle = new GeneralRenderStyle(viewFrustum);
		renderStyle = new FilterPipelineRenderStyle(viewFrustum);
		selectionManager =
			new SelectionManager
			(
				IDType.registerType
				(
					"filter_" + hashCode(),
					IDCategory.registerCategory("filter"),
					EStorageType.INT
				)
			);

		super.renderStyle = renderStyle;
		detailLevel = DetailLevel.HIGH;
		
		background = new Background(iUniqueID, pickingManager, renderStyle);
		radialMenu = new RadialMenu
		(
			this,
			textureManager.getIconTexture(gl, EIconTextures.FILTER_PIPELINE_MENU_ITEM)
		);
		radialMenu.addEntry( null );
		radialMenu.addEntry( null );
		radialMenu.addEntry( textureManager.getIconTexture(gl, EIconTextures.FILTER_PIPELINE_DELETE) );
		radialMenu.addEntry( textureManager.getIconTexture(gl, EIconTextures.FILTER_PIPELINE_EDIT) );
		
		filterMenu = new FilterMenu(renderStyle, pickingManager, iUniqueID);
		
		if( textRenderer != null )
			textRenderer.dispose();
		textRenderer = new CaleydoTextRenderer(new Font("Arial", Font.PLAIN, 20), true);
		textRenderer.setColor(0, 0, 0, 1);
	}

	@Override
	public void initLocal(GL2 gl)
	{
		// Register keyboard listener to GL2 canvas
		parentGLCanvas.getParentComposite().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				parentGLCanvas.getParentComposite().addKeyListener(glKeyListener);
			}
		});
		
		init(gl);
	}

	@Override
	public void initRemote(final GL2 gl, final AGLView glParentView,
			final GLMouseListener glMouseListener)
	{
		// Register keyboard listener to GL2 canvas
		glParentView.getParentGLCanvas().getParentComposite().getDisplay()
				.asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						glParentView.getParentGLCanvas().getParentComposite()
								.addKeyListener(glKeyListener);
					}
				});

		this.glMouseListener = glMouseListener;

		iGLDisplayListIndexRemote = gl.glGenLists(1);
		iGLDisplayListToCall = iGLDisplayListIndexRemote;
		init(gl);
	}

	@Override
	public void displayLocal(GL2 gl)
	{
		pickingManager.handlePicking(this, gl);
		//glMouseListener = getParentGLCanvas().getGLMouseListener();

		display(gl);
		checkForHits(gl);
	}

	@Override
	public void displayRemote(GL2 gl)
	{
		display(gl);
		checkForHits(gl);
	}
	
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		super.reshape(drawable, x, y, width, height);
		
		updateFilterSize();
	}

	@Override
	public void display(GL2 gl)
	{
		// ---------------------------------------------------------------------
		// move...
		// ---------------------------------------------------------------------
		
		if( pipelineNeedsUpdate )
			updateFilterPipeline();
		
		if( glMouseListener.wasMouseReleased() )
			radialMenu.handleMouseReleased();
		
		updateMousePosition(gl);
		
		radialMenu.handleDragging(mousePosition);
		
		// ---------------------------------------------------------------------
		// render...
		// ---------------------------------------------------------------------
		
		background.render(gl, textRenderer);
		
		// filter
		if( !filterList.isEmpty() )
		{
			// display an arrow to show hidden filters
			if( firstFilter > 0 )
				displayCollapseArrow(gl, firstFilter - 1, 0.4f);
			
			for (FilterItem<?> filter : filterList)
			{
				if( filter.getId() < firstFilter )
					// skip hidden filters
					continue;
				
				if( filter.getId() == firstFilter )
				{
					// show input for first filter
					textRenderer.renderText
					(
						gl,
						""+filter.getInput().size(),
						filter.getRepresentation().getPosition().x(),
						filter.getRepresentation().getPosition().y()
						+ filter.getRepresentation().getHeightLeft()
						+ 0.05f,
						0.9f,
						0.007f,
						20
					);
				}
				else
				{
					displayCollapseArrow
					(
						gl,
						filter.getId(),
						filter.getRepresentation().getPosition().x() - 0.15f
					);
				}
				
				filter.getRepresentation().updateSelections(selectionManager);
				filter.render(gl, textRenderer);
			}

			filterMenu.render(gl, textRenderer);
			radialMenu.render(gl);
		}
		
		// call after all other rendering because it calls the onDrag methods
		// which need alpha blending...
		dragAndDropController.handleDragging(gl, glMouseListener);
	}
	
	private void displayCollapseArrow(GL2 gl, int id, float left)
	{
		int iPickingID =
			pickingManager.getPickingID
			(
				iUniqueID,
				EPickingType.FILTERPIPE_START_ARROW,
				id
			);
		float bottom = 0.025f;
		float halfSize = 0.075f;

		gl.glPushAttrib(GL2.GL_COLOR_BUFFER_BIT);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		Texture arrowTexture =
			textureManager.getIconTexture(gl, EIconTextures.HEAT_MAP_ARROW);
		arrowTexture.enable();
		arrowTexture.bind();
		TextureCoords texCoords = arrowTexture.getImageTexCoords();
		
		gl.glPushName(iPickingID);
		
		gl.glMatrixMode(GL2.GL_MODELVIEW_MATRIX);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
		gl.glTranslatef(left + halfSize, bottom + halfSize, 0.001f);
		gl.glRotatef(id <= firstFilter ? -90 : 90, 0, 0, 1);
		
		gl.glBegin(GL2.GL_QUADS);
		{
			gl.glColor3f(0.9f,1f,0.9f);
	
			gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
			gl.glVertex2f(-halfSize, -halfSize);
			
			gl.glTexCoord2f(texCoords.left(), texCoords.top());
			gl.glVertex2f(-halfSize, halfSize);
	
			gl.glTexCoord2f(texCoords.right(), texCoords.top());
			gl.glVertex2f(halfSize, halfSize);
			
			gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
			gl.glVertex2f(halfSize, -halfSize);
		}
		gl.glEnd();
		
		gl.glPopMatrix();
		gl.glPopName();

		arrowTexture.disable();
		
		gl.glPopAttrib();
	}

	@Override
	public String getShortInfo()
	{
		return "Filterpipeline "+filterType;
	}

	@Override
	public String getDetailedInfo()
	{
		return "Filterpipeline "+filterType;
	}

	@Override
	protected void handlePickingEvents(EPickingType pickingType, EPickingMode pickingMode,
			int iExternalID, Pick pick)
	{
		switch(pickingMode)
		{
			case CLICKED:
				dragAndDropController.clearDraggables();
				break;
			case MOUSE_OVER:
				filterMenu.handleClearMouseOver();
				break;
		}

		switch(pickingType)
		{
			// -----------------------------------------------------------------
			case FILTERPIPE_FILTER:
				switch(pickingMode)
				{
					case MOUSE_OVER:
						// remove old mouse over
						selectionManager.clearSelection(SelectionType.MOUSE_OVER);
						selectionManager.addToType
						(
							SelectionType.MOUSE_OVER, iExternalID
						);
						filterMenu.setFilter(filterList.get(iExternalID));
						break;
					case CLICKED:
						if( !bControlPressed )
							selectionManager.clearSelection(SelectionType.SELECTION);

						// Toggle add/remove element to selection
						if( selectionManager.checkStatus(SelectionType.SELECTION, iExternalID) )
						{
							selectionManager.removeFromType
							(
								SelectionType.SELECTION, iExternalID
							);
						}
						else
						{
							selectionManager.addToType
							(
								SelectionType.SELECTION, iExternalID
							);
						}
						
						dragAndDropController.setDraggingStartPosition(pick.getPickedPoint());
						dragAndDropController.addDraggable(filterList.get(iExternalID).getRepresentation());
						break;
					case RIGHT_CLICKED:
						radialMenu.show(iExternalID, mousePosition);
						break;
					case DRAGGED:
						if( dragAndDropController.hasDraggables() )
						{
							if( glMouseListener.wasRightMouseButtonPressed() )
								dragAndDropController.clearDraggables();
							else if( !dragAndDropController.isDragging() )
								dragAndDropController.startDragging();
						}
						dragAndDropController.setDropArea(filterList.get(iExternalID));
						break;
				}
				break;
			// -----------------------------------------------------------------
			case FILTERPIPE_SUB_FILTER:
				switch(pickingMode)
				{
					case MOUSE_OVER:
						filterMenu.handleIconMouseOver(iExternalID);
						break;
				}
				break;
			// -----------------------------------------------------------------
			case FILTERPIPE_START_ARROW:
				switch(pickingMode)
				{
					case CLICKED:
						firstFilter = iExternalID;
						updateFilterSize();
						// break; Fall through...
					case MOUSE_OVER:
						// reset all mouse over actions
						selectionManager.clearSelection(SelectionType.MOUSE_OVER);
						filterMenu.setFilter(null);
						break;
				}
				break;				
			// -----------------------------------------------------------------
			case FILTERPIPE_BACKGROUND:
				switch(pickingMode)
				{
					case CLICKED:
						if( !bControlPressed )
							selectionManager.clearSelection(SelectionType.SELECTION);
						// break; Fall through...
					case MOUSE_OVER:
						// reset all mouse over actions
						selectionManager.clearSelection(SelectionType.MOUSE_OVER);
						filterMenu.setFilter(null);
						break;
					case DRAGGED:
						dragAndDropController.setDropArea(background);
						break;
				}
				break;
		}
	}
	
	@Override
	public void handleRadialMenuSelection(int externalId, int selection)
	{
		fullSizedFilter = -1;
		ignoredFilter = -1;

		if( externalId >= filterList.size() || externalId < 0 )
			return;
		
		FilterItem<?> filter = filterList.get(externalId);
		
		switch(selection)
		{
			case 2: // left
				filter.triggerRemove();
				selectionManager.removeFromType
				(
					SelectionType.SELECTION, externalId
				);
				break;
			case 3: // down
				try
				{
					filter.showDetailsDialog();
				}
				catch (Exception e)
				{
					System.out.println("Failed to show details dialog: "+e);
				}
				break;
		}
	}
	
	@Override
	public void handleRadialMenuHover(int externalId, int selection)
	{
		ignoredFilter = -1;
		fullSizedFilter = -1;

		switch(selection)
		{
			case 0:
				fullSizedFilter = externalId;
				break;
			case 2: // remove
				ignoredFilter = externalId;
				break;
		}
	}
	
	private void updateMousePosition(GL2 gl)
	{
		try
		{
			float windowCoords[] =
				GLCoordinateUtils.convertWindowCoordinatesToWorldCoordinates
				(
					gl,
					glMouseListener.getPickedPoint().x,
					glMouseListener.getPickedPoint().y
				);

			mousePosition.set(windowCoords[0], windowCoords[1]);
		}
		catch(Exception e)
		{
			//System.out.println("Failed to get mouse position: "+e);
		}
	}
	
	private void updateFilterSize()
	{
		if( filterList.isEmpty() )
			return;

		// ensure at least one valid filter is shown
		if( firstFilter >= filterList.size() )
			firstFilter = filterList.size() - 1;
		else if( firstFilter < 0 )
			firstFilter = 0;

		filterSize =
			new Vec2f
			(
				(viewFrustum.getWidth() - 0.5f) / (filterList.size() - firstFilter),

				// 100 elements will be high exactly 1 unit. So we need to scale
				// it that the largest (== first) filter fits.		
				(viewFrustum.getHeight() - 0.4f)
			    / (filterList.get(firstFilter).getInput().size()/100.f)
			);
		
		Vec2f filterPosition = new Vec2f(0.4f, renderStyle.FILTER_SPACING_BOTTOM),
		      width = new Vec2f(filterSize.x(), 0);
		
		for (FilterItem<?> filter : filterList)
		{
			if( filter.getId() < firstFilter )
				continue;
			
			filter.getRepresentation().setPosition(filterPosition);
			filter.getRepresentation().setSize(filterSize);
			
			filterPosition.add(width);
		}
		
		background.setFilterList(filterList, firstFilter);
	}

	@Override
	public ASerializedView getSerializableRepresentation()
	{
		SerializedFilterPipelineView serializedForm = new SerializedFilterPipelineView();
		serializedForm.setViewID(this.getID());
		return serializedForm;
	}

	@Override
	public String toString()
	{
		return getClass().getCanonicalName();
	}

	@Override
	public void registerEventListeners()
	{
		filterUpdateListener = new FilterUpdateListener();
		filterUpdateListener.setHandler(this);
		eventPublisher.addListener(FilterUpdatedEvent.class, filterUpdateListener);
		
		reEvaluateFilterListener = new ReEvaluateFilterListener();
		reEvaluateFilterListener.setHandler(this);
		eventPublisher.addListener(ReEvaluateContentFilterListEvent.class, reEvaluateFilterListener);
		eventPublisher.addListener(ReEvaluateStorageFilterListEvent.class, reEvaluateFilterListener);
		
		setFilterTypeListener = new SetFilterTypeListener();
		setFilterTypeListener.setHandler(this);
		eventPublisher.addListener(SetFilterTypeEvent.class, setFilterTypeListener);
	}

	@Override
	public void unregisterEventListeners()
	{
		if( filterUpdateListener != null )
		{
			eventPublisher.removeListener(filterUpdateListener);
			filterUpdateListener = null;
		}
		
		if( reEvaluateFilterListener != null )
		{
			eventPublisher.removeListener(reEvaluateFilterListener);
			reEvaluateFilterListener = null;
		}
		
		if( setFilterTypeListener != null )
		{
			eventPublisher.removeListener(setFilterTypeListener);
			setFilterTypeListener = null;
		}
	}

	/**
	 * Rebuild the filterpipeline
	 */
	public void updateFilterPipeline()
	{
		pipelineNeedsUpdate = false;

		Logger.log
		(
			new Status(IStatus.INFO, this.toString(),
			"Filterupdate: filterType="+filterType)
		);
		
		filterList.clear();
		int filterID = 0;
		
		for( Filter<?> filter :
				 filterType == FilterType.CONTENT
				   ? dataDomain.getContentFilterManager().getFilterPipe()
				   : dataDomain.getStorageFilterManager().getFilterPipe()
		   )
		{
			FilterItem<?> filterItem =
				new FilterItem(filterID++, filter, pickingManager, iUniqueID);
			
			if( filter instanceof ContentMetaOrFilter )
				filterItem.setRepresentation
				(
					new FilterRepresentationMetaOrAdvanced(renderStyle, pickingManager, iUniqueID)
				);
			else
				filterItem.setRepresentation
				(
					new FilterRepresentation(renderStyle, pickingManager, iUniqueID)
				);
			
			filterList.add(filterItem);
		}

		// TODO move to separate function...
		IVirtualArray<?,?,?> currentVA =
			filterType == FilterType.CONTENT
			   ? dataDomain.getContentFilterManager().getBaseVA().clone()
			   : dataDomain.getStorageFilterManager().getBaseVA().clone();

		for (FilterItem<?> filter : filterList)
		{
			// filter items
			filter.setInput(currentVA);
			currentVA = filter.getOutput().clone();
		}
		
		updateFilterSize();
	}

	public void handleSetFilterTypeEvent(FilterType type)
	{
		if( filterType == type )
			return;
		
		filterType = type;
		updateFilterPipeline();
	}
	
	public void handleReEvaluateFilter(FilterType type)
	{
		if( filterType == type )
			updateFilterPipeline();
	}

	@Override
	public void handleSelectionUpdate(ISelectionDelta selectionDelta,
			boolean scrollToSelection, String info)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void handleRedrawView()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void handleUpdateView()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void handleClearSelections()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void clearAllSelections()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void broadcastElements(EVAOperation type)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public int getNumberOfSelections(SelectionType SelectionType)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public void setControlPressed(boolean state)
	{
		bControlPressed  = state;		
	}

}
