package org.caleydo.core.view.opengl.util.vislink;

import gleem.linalg.Vec3f;

import java.util.ArrayList;

import javax.media.opengl.GL;


import org.caleydo.core.view.opengl.renderstyle.ConnectionLineRenderStyle;

//import org.caleydo.core.view.opengl.canvas.listener.ISelectionUpdateHandler;
//import org.caleydo.core.data.selection.delta.ISelectionDelta;
//import org.caleydo.core.manager.IEventPublisher;
//import org.caleydo.core.manager.event.AEvent;
//import org.caleydo.core.manager.event.AEventListener;
//import org.caleydo.core.manager.event.IListenerOwner;
//import org.caleydo.core.manager.event.view.storagebased.SelectionUpdateEvent;
//import org.caleydo.core.manager.general.GeneralManager;
//
//import org.caleydo.core.view.opengl.canvas.listener.ISelectionUpdateHandler;

/**
 * This class provides higher level features to VisLinks such as halo and animation.
 * To provide this features, more context is needed than a single line. This
 * class needs to know all connection lines that should be displayed on the screen.
 *  
 * @author Oliver Pimas
 * @version 2009-10-21
 *
 */

public class VisLinkEnvironment { 
//	implements ISelectionUpdateHandler {
	
//	private int activeViewID;
	
//	ArrayList<ArrayList<ArrayList<Vec3f>>> connectionLinesAllViews;
//	ArrayList<ArrayList<Vec3f>> bundlingToCenterLines;
//	ArrayList<ArrayList<Vec3f>> connectionLinesActiveView;
	ArrayList<ArrayList<ArrayList<Vec3f>>> connectionLinesAllViews;
//	private static int stage;
	private static boolean animationFinished = false;
	
	private static long animationStartTime = -1;
	private static int NUMBER_OF_SEGMENTS = 30;
	
	private EVisLinkStyleType style;
//	private EVisLinkStyleType style = EVisLinkStyleType.STANDARD_VISLINK;
//	private EVisLinkStyleType style = EVisLinkStyleType.SHADOW_VISLINK;
//	private EVisLinkStyleType style = EVisLinkStyleType.HALO_VISLINK;
	
//	protected static VLSelectionUpdateListener selectionUpdateListener = null;
	
	
//	/**
//	 * Constructor
//	 * @param connectionLines connection lines from objects to bundling points of the non active views
//	 * @param bundlingToCenterLines the lines connecting the bundling points and the center
//	 * @param connectionLinesActiveView connection lines of the current view
//	 */
//	public VisLinkEnvironment(ArrayList<ArrayList<ArrayList<Vec3f>>> connectionLinesAllViews, ArrayList<ArrayList<Vec3f>> bundlingToCenterLines, ArrayList<ArrayList<Vec3f>> connectionLinesActiveView) {
//		this.connectionLinesAllViews = connectionLinesAllViews;
//		this.bundlingToCenterLines = bundlingToCenterLines;
//		this.connectionLinesActiveView = connectionLinesActiveView;
////		if(selectionUpdateListener == null)
////			registerEventListeners();
//	}
	
	/**
	 * Constructor
	 * @param connectionLinesAllViews Connection lines of the scene
	 */
	public VisLinkEnvironment(ArrayList<ArrayList<ArrayList<Vec3f>>> connectionLinesAllViews) {
		this.connectionLinesAllViews = connectionLinesAllViews;
		this.style = ConnectionLineRenderStyle.CONNECTION_LINE_STYLE;
	}
	
	
//	public void registerEventListeners() {
//		IEventPublisher eventPublisher = GeneralManager.get().getEventPublisher();
//		
//		selectionUpdateListener = new VLSelectionUpdateListener();
//		selectionUpdateListener.setHandler(this);
//		eventPublisher.addListener(SelectionUpdateEvent.class, selectionUpdateListener);
//	}
	
//	public void unregisterEventListeners() {
//		IEventPublisher eventPublisher = GeneralManager.get().getEventPublisher();
//		
//		if (selectionUpdateListener != null) {
//			eventPublisher.removeListener(selectionUpdateListener);
//			selectionUpdateListener = null;
//		}
//	}
	
//	public void setActiveViewID(int id) {
//		activeViewID = id;
//	}
	
	
	/**
	 * Renders the connection line of the scene.
	 * @param gl The GL-object
	 */
	public void renderLines(final GL gl) {		
//		callRenderLine(gl);		
		if(ConnectionLineRenderStyle.ANIMATION)
			callRenderAnimatedPolygonLine(gl);
		else
			callRenderPolygonLine(gl);		
	}
	
	
	/**
	 * Renders the scene with simple connection lines (visual links).
	 * No halo-effect or animation available.
	 * @param gl The GL-object
	 */
	protected void callRenderLine(final GL gl) {
		for (ArrayList<ArrayList<Vec3f>> currentView : connectionLinesAllViews)
			for(ArrayList<Vec3f> currentLine : currentView) 
				VisLink.renderLine(gl, currentLine, 0, 10, true);
	}
//	protected void callRenderLine(final GL gl) {
//		for (ArrayList<ArrayList<Vec3f>> currentView : connectionLinesAllView) {
//			for(ArrayList<Vec3f> currentLine : currentView) {
//				VisLink.renderLine(gl, currentLine, 0, 10, true);
//			}
//		}
//		for(ArrayList<Vec3f> currentLine : connectionLinesActiveView)
//			VisLink.renderLine(gl, currentLine, 0, 10, true);
//		for(ArrayList<Vec3f> currentLine : bundlingToCenterLines)
//			VisLink.renderLine(gl, currentLine, 0, 10, true);
//	}	
	
	
	/**
	 * Renders the scene with polygonal connection lines (visual links)
	 * @param gl The GL-object
	 */
	protected void callRenderPolygonLine(final GL gl) {
				
		float width = 0.0f;
		float color[] = new float[4];
		
//		int antiAliasingQuality = ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY;
		int antiAliasingQuality = 1;
				
		if( (style == EVisLinkStyleType.SHADOW_VISLINK) || (style == EVisLinkStyleType.HALO_VISLINK) ) {
			
			//clear stencil buffer
			gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
			
			//set parameters
			if(style == EVisLinkStyleType.SHADOW_VISLINK) {
				width = ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH * ConnectionLineRenderStyle.CONNECTION_LINE_SHADOW_WIDTH_FACTOR;
				color = ConnectionLineRenderStyle.CONNECTION_LINE_SHADOW_COLOR;
				antiAliasingQuality = 1;
			}
			if(style == EVisLinkStyleType.HALO_VISLINK) {
				width = ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH * ConnectionLineRenderStyle.CONNECTION_LINE_HALO_WIDTH_FACTOR;
				color[0] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[0];
				color[1] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[1];
				color[2] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[2];
				color[3] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[3] / 1.5f;
				antiAliasingQuality = 5;
			}
			
			//draw shadow oder halo
			for(ArrayList<ArrayList<Vec3f>> currentView : connectionLinesAllViews)
				for(ArrayList<Vec3f> currentLine : currentView) {
					if(currentLine.size() >= 2) {
						VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
						enableStencilBuffer(gl);
						visLink.drawPolygonLine(gl, width, color, antiAliasingQuality);
						disableStencilBuffer(gl);
					}
				}		
		}
		
		// background (halo or shadow) done, render frontline
		gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
		
		width = ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH;
		color = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR;
		antiAliasingQuality = 1;

		for(ArrayList<ArrayList<Vec3f>> currentView : connectionLinesAllViews)
			for(ArrayList<Vec3f> currentLine : currentView) {
				if(currentLine.size() >= 2) {
					VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
					enableStencilBuffer(gl);
					visLink.drawPolygonLine(gl, width, color, antiAliasingQuality);
					disableStencilBuffer(gl);
				}
			}		
	}
//	protected void callRenderPolygonLine(final GL gl) {
//		
//		gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
//		
//		float width = 0.0f;
//		float color[] = new float[4];
//				
//		if( (style == EVisLinkStyleType.SHADOW_VISLINK) || (style == EVisLinkStyleType.HALO_VISLINK) ) {
//			
//			//set parameters
//			if(style == EVisLinkStyleType.SHADOW_VISLINK) {
//				width = ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH * ConnectionLineRenderStyle.CONNECTION_LINE_SHADOW_WIDTH_FACTOR;
//				color = ConnectionLineRenderStyle.CONNECTION_LINE_SHADOW_COLOR;
//			}
//			if(style == EVisLinkStyleType.HALO_VISLINK) {
//				width = ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH * ConnectionLineRenderStyle.CONNECTION_LINE_HALO_WIDTH_FACTOR;
//				color[0] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[0];
//				color[1] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[1];
//				color[2] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[2];
//				color[3] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[3] / 1.5f;
//			}
//			
//			//draw shadow oder halo
//			for(ArrayList<Vec3f> currentLine : connectionLinesActiveView) {
//				if(currentLine.size() >= 2) {
//					VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
//					enableStencilBuffer(gl);
//					visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY);
//					disableStencilBuffer(gl);
//				}
//				else
//					throw new IllegalArgumentException( "Need at least two points to render a line!" );
//			}
//			for(ArrayList<Vec3f> currentLine : bundlingToCenterLines) {
//				if(currentLine.size() >= 2) {
//					VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
//					enableStencilBuffer(gl);
//					visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY);
//					disableStencilBuffer(gl);
//				}
//			}
//			for(ArrayList<ArrayList<Vec3f>> currentView : connectionLinesAllViews)
//				for(ArrayList<Vec3f> currentLine : currentView) {
//					if(currentLine.size() >= 2) {
//						VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
//						enableStencilBuffer(gl);
//						visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY);
//						disableStencilBuffer(gl);
//					}
//				}		
//		}
//		
//		// background (halo or shadow) done, render frontline
//		width = ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH;
//		color = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR;
//		
//		for(ArrayList<Vec3f> currentLine : connectionLinesActiveView) {
//			if(currentLine.size() >= 2) {
//				VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
//				visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY);
//			}
//			else
//				throw new IllegalArgumentException( "Need at least two points to render a line!" );
//		}
//		for(ArrayList<Vec3f> currentLine : bundlingToCenterLines) {
//			if(currentLine.size() >= 2) {
//				VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
//				visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY);
//			}
//		}
//		for(ArrayList<ArrayList<Vec3f>> currentView : connectionLinesAllViews)
//			for(ArrayList<Vec3f> currentLine : currentView) {
//				if(currentLine.size() >= 2) {
//					VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
//					visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY);
//				}
//			}		
//	}
	

	
//	/**
//	 * Renders the scene with animated polygonal connection lines (visual links)
//	 * @param gl The GL-object
//	 */
//	protected void callRenderAnimatedPolygonLine(final GL gl) {
//		
//		gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
//		
//		long numberOfSegmentsToDraw = numberOfSegmentsToDraw();
//		float width = 0.0f;
//		float color[] = new float[4];
//				
//		if( (style == EVisLinkStyleType.SHADOW_VISLINK) || (style == EVisLinkStyleType.HALO_VISLINK) ) {
//			//set parameters
//			if(style == EVisLinkStyleType.SHADOW_VISLINK) {
//				width = ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH * ConnectionLineRenderStyle.CONNECTION_LINE_SHADOW_WIDTH_FACTOR;
//				color = ConnectionLineRenderStyle.CONNECTION_LINE_SHADOW_COLOR;
//			}
//			if(style == EVisLinkStyleType.HALO_VISLINK) {
//				width = ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH * ConnectionLineRenderStyle.CONNECTION_LINE_HALO_WIDTH_FACTOR;
//				color[0] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[0];
//				color[1] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[1];
//				color[2] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[2];
//				color[3] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[3] / 1.5f;
//			}
//			
//			//draw shadow oder halo
//			for(ArrayList<Vec3f> currentLine : connectionLinesActiveView) {
//				if(currentLine.size() >= 2) {
//					VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
//					if (numberOfSegmentsToDraw >= visLink.numberOfSegments()) {
//						activeViewAnimationFinished = true;
//						numberOfSegmentsToDraw = numberOfSegmentsToDraw();
//					}
//					enableStencilBuffer(gl);
//					if(!activeViewAnimationFinished)
//						visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY, numberOfSegmentsToDraw);
//					else
//						visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY);
//					disableStencilBuffer(gl);
//				}
//				else
//					throw new IllegalArgumentException( "Need at least two points to render a line!" );
//			}
//			if(activeViewAnimationFinished) {
//				for(ArrayList<Vec3f> currentLine : bundlingToCenterLines) {
//					if(currentLine.size() >= 2) {
//						VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
//						enableStencilBuffer(gl);
//						if(!animationFinished)
//							visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY, numberOfSegmentsToDraw);
//						else
//							visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY);
//						disableStencilBuffer(gl);
//					}
//				}
//				for(ArrayList<ArrayList<Vec3f>> currentView : connectionLinesAllViews)
//					for(ArrayList<Vec3f> currentLine : currentView) {
//						if(currentLine.size() >= 2) {
//							VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
//							if (numberOfSegmentsToDraw >= visLink.numberOfSegments()) {
//								animationFinished = true;
//							}
//							enableStencilBuffer(gl);
//							if(!animationFinished)
//								visLink.drawPolygonLineReverse(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY, numberOfSegmentsToDraw);
//							else
//								visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY);
//							disableStencilBuffer(gl);
//						}
//					}
//			}			
//		}
//		
//		// background (halo or shadow) done, render frontline
//		width = ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH;
//		color = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR;
//		
//		for(ArrayList<Vec3f> currentLine : connectionLinesActiveView) {
//			if(currentLine.size() >= 2) {
//				VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
//				if (numberOfSegmentsToDraw >= visLink.numberOfSegments()) {
//					activeViewAnimationFinished = true;
//					numberOfSegmentsToDraw = numberOfSegmentsToDraw();
//				}
//				if(!activeViewAnimationFinished)
//					visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY, numberOfSegmentsToDraw);
//				else
//					visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY);
//			}
//			else
//				throw new IllegalArgumentException( "Need at least two points to render a line!" );
//		}
//		if(activeViewAnimationFinished) {
//			for(ArrayList<Vec3f> currentLine : bundlingToCenterLines) {
//				if(currentLine.size() >= 2) {
//					VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
//					if(!animationFinished)
//						visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY, numberOfSegmentsToDraw);
//					else
//						visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY);
//				}
//			}
//			for(ArrayList<ArrayList<Vec3f>> currentView : connectionLinesAllViews)
//				for(ArrayList<Vec3f> currentLine : currentView) {
//					if(currentLine.size() >= 2) {
//						VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
//						if (numberOfSegmentsToDraw >= visLink.numberOfSegments()) {
//							animationFinished = true;
//						}
//						if(!animationFinished)
//							visLink.drawPolygonLineReverse(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY, numberOfSegmentsToDraw);
//						else
//							visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY);
//					}
//				}
//		}		
//	}

	
	/**
	 * Enables the stencil buffer
	 * @param gl The GL-object
	 */
	protected void enableStencilBuffer(GL gl) {
		gl.glStencilFunc(GL.GL_NOTEQUAL, 0x1, 0x1);
		gl.glStencilOp(GL.GL_KEEP, GL.GL_REPLACE, GL.GL_REPLACE);
		gl.glEnable(GL.GL_STENCIL_TEST);
	}
	
	
	/**
	 * Disables the stencil buffer
	 * @param gl The GL-object
	 */
	protected void disableStencilBuffer(GL gl) {
		gl.glDisable(GL.GL_STENCIL_TEST);
	}
	
	
	/**
	 * Resets the animation
	 * @param time the time the animation started
	 */
	public static void resetAnimation(long time) {
		animationStartTime = time;
		animationFinished = false;
//		stage = 0;
	}
	
	
	/**
	 * Calculates the number of segments to be drawn for animation 
	 * @return number of segments to be drawn
	 */
	public long numberOfSegmentsToDraw() {
		long animationSpeed = ConnectionLineRenderStyle.ANIMATION_SPEED_IN_MILLIS / NUMBER_OF_SEGMENTS;
		return (System.currentTimeMillis() - animationStartTime) / animationSpeed;
	}
//	public long numberOfSegmentsToDraw() {
//		long animationSpeed = ConnectionLineRenderStyle.ANIMATION_SPEED_IN_MILLIS / NUMBER_OF_SEGMENTS;
//		long segments = (System.currentTimeMillis() - animationStartTime) / animationSpeed;
//		if(activeViewAnimationFinished)
//			segments -= NUMBER_OF_SEGMENTS;
//		if(bundlingToCenterAnimationFinished)
//			segments -= NUMBER_OF_SEGMENTS;
//		if(segments < 0)
//			segments = 0;
//		return (segments > NUMBER_OF_SEGMENTS ) ? NUMBER_OF_SEGMENTS : segments;
//	}
	
//	public long numberOfSegmentsToDraw() {
//		long animationSpeed = ConnectionLineRenderStyle.ANIMATION_SPEED_IN_MILLIS / NUMBER_OF_SEGMENTS;
//		long segments = (System.currentTimeMillis() - animationStartTime) / animationSpeed;
//		segments -= (NUMBER_OF_SEGMENTS*stage);
//		if(segments < 0)
//			segments = 0;
//		return (segments > NUMBER_OF_SEGMENTS ) ? NUMBER_OF_SEGMENTS : segments;
//	}

	
	/**
	 * Renders the scene with animated polygonal connection lines (visual links)
	 * @param gl The GL-object
	 */
	protected void callRenderAnimatedPolygonLine(final GL gl) {
		
		long numberOfSegmentsToDraw = numberOfSegmentsToDraw();
		int localStage = 0;
		while(numberOfSegmentsToDraw > NUMBER_OF_SEGMENTS){
			localStage++;
			numberOfSegmentsToDraw -= NUMBER_OF_SEGMENTS;
		}
		if(localStage > numberOfStages() ) {
			animationFinished = true;
			localStage = numberOfStages();
		}
//		System.out.println("numberOfSegmentsToDraw=" + numberOfSegmentsToDraw + "   localStage=" + localStage);
		
		float width = 0.0f;
		float color[] = new float[4];
		
//		int antiAliasingQuality = ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY;
		int antiAliasingQuality = 1;
				
		if( (style == EVisLinkStyleType.SHADOW_VISLINK) || (style == EVisLinkStyleType.HALO_VISLINK) ) {
			
			//clear stencil buffer
			gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
			
			//set parameters
			if(style == EVisLinkStyleType.SHADOW_VISLINK) {
				width = ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH * ConnectionLineRenderStyle.CONNECTION_LINE_SHADOW_WIDTH_FACTOR;
				color = ConnectionLineRenderStyle.CONNECTION_LINE_SHADOW_COLOR;
				antiAliasingQuality = 1;
			}
			if(style == EVisLinkStyleType.HALO_VISLINK) {
				width = ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH * ConnectionLineRenderStyle.CONNECTION_LINE_HALO_WIDTH_FACTOR;
				color[0] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[0];
				color[1] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[1];
				color[2] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[2];
				color[3] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[3] / 1.5f;
				antiAliasingQuality = 5;
			}
			
			//draw shadow oder halo
			for(int i = 0; i <= localStage; i++) {
				ArrayList<ArrayList<Vec3f>> currentStage = connectionLinesAllViews.get(i);
				for(ArrayList<Vec3f> currentLine : currentStage) {
					if(currentLine.size() >= 2) {
						VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
						enableStencilBuffer(gl);
						if(i == localStage && !animationFinished) {
							if(i < 2)
								visLink.drawPolygonLine(gl, width, color, antiAliasingQuality, numberOfSegmentsToDraw);
							else
								visLink.drawPolygonLineReverse(gl, width, color, antiAliasingQuality, numberOfSegmentsToDraw);
						}
						else
							visLink.drawPolygonLine(gl, width, color, antiAliasingQuality);
						disableStencilBuffer(gl);
					}
				}
			}			
		}
		
		// background (halo or shadow) done, render frontline
		width = ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH;
		color = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR;
		antiAliasingQuality = 1;
		
		for(int i = 0; i <= localStage; i++) {
			ArrayList<ArrayList<Vec3f>> currentStage = connectionLinesAllViews.get(i);
			for(ArrayList<Vec3f> currentLine : currentStage) {
				if(currentLine.size() >= 2) {
					VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
					if(i == localStage && !animationFinished) {
						if(i < 2)
							visLink.drawPolygonLine(gl, width, color, antiAliasingQuality, numberOfSegmentsToDraw);
						else
							visLink.drawPolygonLineReverse(gl, width, color, antiAliasingQuality, numberOfSegmentsToDraw);
					}
					else
						visLink.drawPolygonLine(gl, width, color, antiAliasingQuality);
				}
			}
		}		
	}
	
	
	/**
	 * Returns the number of stages for animation.
	 * @return Number of stages for animation
	 */
	protected int numberOfStages() {
		return connectionLinesAllViews.size() - 1;
	}
	
//	protected void callRenderAnimatedPolygonLine(final GL gl) {
//		
//		gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
//		
//		long numberOfSegmentsToDraw = numberOfSegmentsToDraw();
//		boolean currentStageFinished = false;
//		float width = 0.0f;
//		float color[] = new float[4];
//				
//		if( (style == EVisLinkStyleType.SHADOW_VISLINK) || (style == EVisLinkStyleType.HALO_VISLINK) ) {
//			//set parameters
//			if(style == EVisLinkStyleType.SHADOW_VISLINK) {
//				width = ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH * ConnectionLineRenderStyle.CONNECTION_LINE_SHADOW_WIDTH_FACTOR;
//				color = ConnectionLineRenderStyle.CONNECTION_LINE_SHADOW_COLOR;
//			}
//			if(style == EVisLinkStyleType.HALO_VISLINK) {
//				width = ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH * ConnectionLineRenderStyle.CONNECTION_LINE_HALO_WIDTH_FACTOR;
//				color[0] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[0];
//				color[1] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[1];
//				color[2] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[2];
//				color[3] = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR[3] / 1.5f;
//			}
//			
//			//draw shadow oder halo
//			for(int i = 0; i <= stage; i++) {
//				currentStageFinished = false;
//				ArrayList<ArrayList<Vec3f>> currentStage = connectionLinesAllViews.get(i);
//				for(ArrayList<Vec3f> currentLine : currentStage) {
//					if(currentLine.size() >= 2) {
//						VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
//						if ( (numberOfSegmentsToDraw >= visLink.numberOfSegments()) && !currentStageFinished) {
//							currentStageFinished = true;
//							if(stage < (connectionLinesAllViews.size()-1)) {
//								stage++;
//								numberOfSegmentsToDraw = numberOfSegmentsToDraw();
//							}
//							else
//								animationFinished = true;
//						}
//						enableStencilBuffer(gl);
//						if(i == stage && !animationFinished) {
//							if(i < 2)
//								visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY, numberOfSegmentsToDraw);
//							else
//								visLink.drawPolygonLineReverse(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY, numberOfSegmentsToDraw);
//						}
//						else
//							visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY);
//						disableStencilBuffer(gl);
//					}
//				}
//			}			
//		}
//		
//		// background (halo or shadow) done, render frontline
//		width = ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH;
//		color = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR;
//		
//		for(int i = 0; i <= stage; i++) {
//			currentStageFinished = false;
//			ArrayList<ArrayList<Vec3f>> currentStage = connectionLinesAllViews.get(i);
//			for(ArrayList<Vec3f> currentLine : currentStage) {
//				if(currentLine.size() >= 2) {
//					VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
//					if ( (numberOfSegmentsToDraw >= visLink.numberOfSegments()) && !currentStageFinished) {
//						currentStageFinished = true;
//						if(stage < (connectionLinesAllViews.size()-1)) {
//							stage++;
//							numberOfSegmentsToDraw = numberOfSegmentsToDraw();
//						}
//						else
//							animationFinished = true;
//					}
//					if(i == stage && !animationFinished) {
//						if(i < 2)
//							visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY, numberOfSegmentsToDraw);
//						else
//							visLink.drawPolygonLineReverse(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY, numberOfSegmentsToDraw);
//					}
//					else
//						visLink.drawPolygonLine(gl, width, color, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY);
//				}
//			}
//		}		
//	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
//the following is outdated...
	
//	protected void callRenderAnimatedPolygonLineProgressive(final GL gl) {
//	gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
//	
//	for(ArrayList<Vec3f> currentLine : connectionLinesActiveView) {
//		if(currentLine.size() >= 2) {
//			VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
//			long numberOfSegmentsToDraw = numberOfSegmentsToDraw();
//			if (numberOfSegmentsToDraw >= visLink.numberOfSegments())
//				activeViewAnimationFinished = true;
//			enableStencilBuffer(gl);
//			if(!activeViewAnimationFinished)
//				visLink.renderPolygonLine(gl, ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH, ConnectionLineRenderStyle.CONNECTION_LINE_COLOR, style, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY, numberOfSegmentsToDraw);
//			else
//				visLink.renderPolygonLine(gl, ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH, ConnectionLineRenderStyle.CONNECTION_LINE_COLOR, style, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY);
//			disableStencilBuffer(gl);
//		}
//		else
//			throw new IllegalArgumentException( "Need at least two points to render a line!" );
//	}
//	if(activeViewAnimationFinished) {
//		for(ArrayList<Vec3f> currentLine : bundlingToCenterLines) {
//			if(currentLine.size() >= 2) {
//				VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
//				enableStencilBuffer(gl);
//				visLink.renderPolygonLine(gl, ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH, ConnectionLineRenderStyle.CONNECTION_LINE_COLOR, style, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY);
//				disableStencilBuffer(gl);
//			}
//		}
//		for(ArrayList<ArrayList<Vec3f>> currentView : connectionLinesAllViews)
//			for(ArrayList<Vec3f> currentLine : currentView) {
//				if(currentLine.size() >= 2) {
//					VisLink visLink = new VisLink(currentLine, 0, NUMBER_OF_SEGMENTS);
//					long numberOfSegmentsToDraw = numberOfSegmentsToDraw();
//					if (numberOfSegmentsToDraw >= visLink.numberOfSegments())
//						animationFinished = true;
//					enableStencilBuffer(gl);
//					if(!animationFinished)
//						visLink.renderPolygonLineReverse(gl, ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH, ConnectionLineRenderStyle.CONNECTION_LINE_COLOR, style, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY, numberOfSegmentsToDraw);
//					else
//						visLink.renderPolygonLine(gl, ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH, ConnectionLineRenderStyle.CONNECTION_LINE_COLOR, style, ConnectionLineRenderStyle.LINE_ANTI_ALIASING_QUALITY);
//					disableStencilBuffer(gl);
//				}
//			}
//	}
//}
	
//	/**
//	 * Checks if the animation is finished (all segments are drawn)
//	 * 
//	 * @return true if animation is finished, false otherwise
//	 */
//	public boolean isAnimationFinished() {
//		return animationFinished;
//	}

	
}
