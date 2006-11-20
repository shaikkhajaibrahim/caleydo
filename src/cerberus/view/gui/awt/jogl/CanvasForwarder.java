package cerberus.view.gui.awt.jogl;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLJPanel;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import com.sun.opengl.util.Animator;

import cerberus.view.gui.opengl.IGLCanvasDirector;

/**
 * Gears.java <BR>
 * author: Brian Paul (converted to Java by Ron Cemer and Sven Goethel) <P>
 *
 * This version is equal to Brian Paul's version 1.2 1999/10/21
 */

public class CanvasForwarder 
implements GLEventListener, IJoglMouseListener {

	private final IGLCanvasDirector refGLCanvasDirector;

	private GearsMouse refMouseHandler;

	private float view_rotx = 00.0f, view_roty = 00.0f, view_rotz = 0.0f;

	private float view_x = 0.0f, view_y = 0.0f, view_z = 0.0f;

	public static void main(String[] args) {

		CanvasForwarder refGearsMainRoot = new CanvasForwarder();

		refGearsMainRoot.runMain();

	}

	public CanvasForwarder() {

		refMouseHandler = new GearsMouse(this);

		this.refGLCanvasDirector = null;
	}

	public CanvasForwarder(IGLCanvasDirector refGLCanvasDirector) {

		refMouseHandler = new GearsMouse(this);

		this.refGLCanvasDirector = refGLCanvasDirector;
	}

	public void runMain() {

		CanvasForwarder refmyGrears = new CanvasForwarder();

		Frame frame = new Frame("GL Canvas Forwarder");
		GLCanvas canvas = new GLCanvas();

		canvas.addGLEventListener(refmyGrears);

		frame.add(canvas);
		frame.setSize(300, 300);

		final Animator animator = new Animator(canvas);
		frame.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {

				// Run this on another thread than the AWT event queue to
				// make sure the call to Animator.stop() completes before
				// exiting
				new Thread(new Runnable() {

					public void run() {

						animator.stop();
						System.exit(0);
					}
				}).start();
			}
		});

		frame.setVisible(true);

		animator.start();
	}

	public synchronized void setViewAngles(float fView_RotX, float fView_RotY,
			float fView_RotZ) {

		view_rotx = fView_RotX;
		view_roty = fView_RotY;
		view_rotz = fView_RotZ;
	}

	public synchronized void setTranslation(float fView_X, float fView_Y,
			float fView_Z) {

		view_x = fView_X;
		view_y = fView_Y;
		view_z = fView_Z;
	}

	public void renderTestTriangle(GL gl) {

		/* Clear The Screen And The Depth Buffer */
		gl.glLoadIdentity(); // Reset the current modelview matrix

		gl.glTranslatef(view_x, view_y, view_z - 10.0f);

		gl.glPushMatrix();
		gl.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(view_rotz, 0.0f, 0.0f, 1.0f);

		gl.glBegin(GL.GL_TRIANGLES); // Drawing using triangles
		gl.glColor3f(1.0f, 0.0f, 0.0f); // Set the color to red
		gl.glVertex3f(0.0f, 1.0f, 0.0f); // Top
		gl.glColor3f(0.0f, 1.0f, 0.0f); // Set the color to green
		gl.glVertex3f(-1.0f, -1.0f, 0.0f); // Bottom left
		gl.glColor3f(0.0f, 0.0f, 1.0f); // Set the color to blue
		gl.glVertex3f(1.0f, -1.0f, 0.0f); // Bottom right
		gl.glEnd(); // Finish drawing the triangle

		gl.glPopMatrix();

	}

	public void init(GLAutoDrawable drawable) {

		// Use debug pipeline
		// drawable.setGL(new DebugGL(drawable.getGL()));

		GL gl = drawable.getGL();

		System.out.println("TrinalgeMain.java INIT GL IS: "
				+ gl.getClass().getName());

		//gl.resizeGLScene();                      // Initialize the GL viewport

		gl.glShadeModel(GL.GL_SMOOTH); // Enables Smooth Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Black Background
		gl.glClearDepth(1.0f); // Depth Buffer Setup

		gl.glEnable(GL.GL_DEPTH_TEST); // Enables Depth Testing
		gl.glDepthFunc(GL.GL_LEQUAL); // The Type Of Depth Test To Do

		/* Really Nice Perspective Calculations */
		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

		/**
		 * You must register the MouseMotionListener here!
		 */
		drawable.addMouseListener(this.refMouseHandler);
		drawable.addMouseMotionListener(this.refMouseHandler);

		if (refGLCanvasDirector != null)
		{
			refGLCanvasDirector.initGLCanvasUser(drawable);
		}
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {

		System.out.println("TrinalgeMain.java  RESHAPE GL");

		GL gl = drawable.getGL();

		float h = (float) height / (float) width;

		gl.glMatrixMode(GL.GL_PROJECTION);

		System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
		System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
		System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));

		gl.glLoadIdentity();
		gl.glFrustum(-1.0f, 1.0f, -h, h, 5.0f, 60.0f);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, -40.0f);
	}

	public void display(GLAutoDrawable drawable) {

		//System.err.println("DISPLAY GL    TrinagleMain!");

		GL gl = drawable.getGL();
		if ((drawable instanceof GLJPanel)
				&& !((GLJPanel) drawable).isOpaque()
				&& ((GLJPanel) drawable)
						.shouldPreserveColorBufferIfTranslucent())
		{
			gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		} else
		{
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		}

		/* Clear The Screen And The Depth Buffer */
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity(); // Reset the current modelview matrix

		gl.glTranslatef(view_x, view_y, view_z - 10.0f);

		gl.glPushMatrix();
		gl.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(view_rotz, 0.0f, 0.0f, 1.0f);

		//renderTestTriangle( gl );

		if (refGLCanvasDirector != null)
		{
			refGLCanvasDirector.renderGLCanvasUser(drawable);
		}

		gl.glPopMatrix();

	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {

	}

}
