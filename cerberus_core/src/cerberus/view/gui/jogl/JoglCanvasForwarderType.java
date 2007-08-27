/**
 * 
 */
package cerberus.view.gui.jogl;

/**�
 * Define type of OpenGL canvas forwarder
 * 
 * @see cerberus.view.gui.jogl.JoglCanvasForwarder
 * @see cerberus.manager.IViewGLCanvasManager#setJoglCanvasForwarderType(JoglCanvasForwarderType)
 * @see cerberus.view.gui.swt.jogl.SwtJoglGLCanvasViewRep#SwtJoglGLCanvasViewRep(cerberus.manager.IGeneralManager, int, int, int, String, JoglCanvasForwarderType)
 * @see cerberus.view.gui.swt.jogl.SwtJoglGLCanvasViewRep#initView()
 * 
 * @author Michael Kalkusch
 *
 */
public enum JoglCanvasForwarderType
{
	DEFAULT_FORWARDER(),
	NO_ROTATION_FORWARDER(),
	NO_TRANSLATION_FORWARDER(),
	ONLY_2D_FORWARDER(),
	GLEVENT_LISTENER_FORWARDER(),
	NONE();
}
