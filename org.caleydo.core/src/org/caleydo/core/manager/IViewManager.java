package org.caleydo.core.manager;

import java.util.Collection;

import javax.media.opengl.GLCanvas;

import org.caleydo.core.command.ECommandType;
import org.caleydo.core.manager.execution.DisplayLoopExecution;
import org.caleydo.core.manager.id.EManagedObjectType;
import org.caleydo.core.manager.picking.PickingManager;
import org.caleydo.core.manager.view.ConnectedElementRepresentationManager;
import org.caleydo.core.view.IView;
import org.caleydo.core.view.opengl.camera.IViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLEventListener;
import org.caleydo.core.view.opengl.canvas.GLCaleydoCanvas;
import org.caleydo.core.view.opengl.util.overlay.infoarea.GLInfoAreaManager;
import org.eclipse.swt.widgets.Composite;

/**
 * Make SWT Views and JOGL GLCanvas addressable by ID and provide ground for XML bootstrapping of GLCanvas.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 */
public interface IViewManager
	extends IManager<IView> {
	public IView createView(final EManagedObjectType useViewType, final int iParentContainerId,
		final String sLabel);

	public AGLEventListener createGLEventListener(ECommandType type, GLCaleydoCanvas glCanvas, String sLabel,
		IViewFrustum viewFrustum);

	public IView createGLView(final EManagedObjectType type, final int iParentContainerID, final String sLabel);

	public Collection<GLCaleydoCanvas> getAllGLCanvasUsers();

	public Collection<AGLEventListener> getAllGLEventListeners();

	public boolean registerGLCanvas(final GLCaleydoCanvas glCanvas);

	public boolean unregisterGLCanvas(final GLCaleydoCanvas glCanvas);

	public void registerGLEventListenerByGLCanvas(final GLCaleydoCanvas glCanvas,
		final AGLEventListener gLEventListener);

	public void unregisterGLEventListener(final AGLEventListener glEventListener);

	/**
	 * Remove canvas from animator. Therefore the canvas is not rendered anymore.
	 */
	public void registerGLCanvasToAnimator(final GLCanvas glCanvas);

	/**
	 * Add canvas to animator. Therefore the canvas is rendered by the animator loop.
	 */
	public void unregisterGLCanvasFromAnimator(final GLCaleydoCanvas glCanvas);

	/**
	 * Get the PickingManager which is responsible for system wide picking
	 * 
	 * @return the PickingManager
	 */
	public PickingManager getPickingManager();

	public ConnectedElementRepresentationManager getConnectedElementRepresentationManager();

	public GLInfoAreaManager getInfoAreaManager();

	public void startAnimator();

	public void stopAnimator();

	/**
	 * Removes all views, canvas and GL event listeners
	 */
	public void cleanup();

	public GLCaleydoCanvas getCanvas(int iItemID);

	public AGLEventListener getGLEventListener(int iItemID);

	public void setActiveSWTView(Composite composite);

	public Composite getActiveSWTView();

	/**
	 * Requests busy mode for the application. This method should be called whenever a process needs to stop
	 * any user interaction with the application, e.g. when starting up or when loading multiple pathways.
	 * Usually this should result disabling user events and showing a loading screen animation.
	 * 
	 * @param requestInstance
	 *            object that wants to request busy mode
	 */
	public void requestBusyMode(Object requestInstance);

	/**
	 * Releases a previously requested busy mode. Releases are only performed by passing the originally
	 * requesting object to this method.
	 * 
	 * @param requestInstance
	 *            the object that requested the busy mode
	 */
	public void releaseBusyMode(Object requestInstance);

	/**
	 * Retrieves the {@link DisplayLoopExecution} related to the {@link IViewManager}'s display loop.
	 * 
	 * @return {@link DisplayLoopExecution} for executing code in the display loop
	 */
	public DisplayLoopExecution getDisplayLoopExecution();
}
