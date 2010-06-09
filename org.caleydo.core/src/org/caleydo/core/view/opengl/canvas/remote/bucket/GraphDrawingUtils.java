package org.caleydo.core.view.opengl.canvas.remote.bucket;

import gleem.linalg.Vec3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import javax.media.opengl.GL;

import org.caleydo.core.data.mapping.EIDType;
import org.caleydo.core.data.selection.SelectedElementRep;
import org.caleydo.core.manager.IViewManager;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.manager.view.ConnectionMap;
import org.caleydo.core.manager.view.SelectedElementRepList;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.remote.AGLConnectionLineRenderer;
import org.caleydo.core.view.opengl.canvas.storagebased.heatmap.GLHeatMap;
import org.caleydo.core.view.opengl.canvas.storagebased.parallelcoordinates.GLParallelCoordinates;
import org.caleydo.core.view.opengl.renderstyle.ConnectionLineRenderStyle;
import org.caleydo.core.view.opengl.util.hierarchy.RemoteLevel;
import org.caleydo.core.view.opengl.util.hierarchy.RemoteLevelElement;

/**
 * Abstract class which provides tools needed for rendering different graphs in bucket view
 * 
 * @author Alexander Lex
 * @author Marc Streit
 */
public abstract class GraphDrawingUtils
	extends AGLConnectionLineRenderer {

	protected final static char HEATMAP = 1;
	protected final static char PARCOORDS = 2;
	protected final static char PATHWAY = 3;
	protected Vec3f vecCenter = new Vec3f();
	protected ArrayList<float[]> colors;
	
	protected RemoteLevel focusLevel;
	protected RemoteLevel stackLevel;
	/**
	 * Constructor.
	 * 
	 * @param focusLevel
	 * @param stackLevel
	 */
	public GraphDrawingUtils(final RemoteLevel focusLevel, final RemoteLevel stackLevel) {

		super();
		this.focusLevel = focusLevel;
		this.stackLevel = stackLevel;
		this.colors = new ArrayList<float[]>();
		
	}

	@Override
	protected void renderConnectionLines(final GL gl) {
		IViewManager viewGLCanvasManager = GeneralManager.get().getViewGLCanvasManager();
		//System.out.println("ids:" + connectedElementRepManager.getCanvasConnectionsByType());
		for (Entry<EIDType, ConnectionMap> typeConnections : connectedElementRepManager
			.getTransformedConnectionsByType().entrySet()) {
			ArrayList<ArrayList<Vec3f>> alPointLists = null;

			EIDType idType = typeConnections.getKey();

			HashMap<Integer, ArrayList<ArrayList<Vec3f>>> viewToPointList =
				hashIDTypeToViewToPointLists.get(idType);

			if (viewToPointList == null) {
				viewToPointList = new HashMap<Integer, ArrayList<ArrayList<Vec3f>>>();
				hashIDTypeToViewToPointLists.put(idType, viewToPointList);
			}
			//!!!!Multiple Connection Lines!!!!
			/*
			int count = 0;
			if (typeConnections.getValue().entrySet().size() > colors.size())
				colors.add(new float[]{(float)Math.random(), (float)Math.random(), (float)Math.random(), 1f});
			*/
			for (Entry<Integer, SelectedElementRepList> connections : typeConnections.getValue().entrySet()) {
				//!!!!Multiple Connection Lines!!!!
				/*HashMap<Integer, ArrayList<ArrayList<Vec3f>>> viewToPointList =
					hashIDTypeToViewToPointLists.get(idType);

				if (viewToPointList == null) {
					viewToPointList = new HashMap<Integer, ArrayList<ArrayList<Vec3f>>>();
					hashIDTypeToViewToPointLists.put(idType, viewToPointList);
				}
				*/
				for (SelectedElementRep selectedElementRep : connections.getValue()) {

					if (selectedElementRep.getIDType() != idType)
						throw new IllegalStateException(
							"Current ID Type does not match the selected elemen rep's");

					AGLView glView =
						viewGLCanvasManager.getGLEventListener(selectedElementRep.getSourceViewID());

					if (glView == null) {
						// TODO: investigate! view must not be null here.
						// GeneralManager.get().getLogger().log(Level.WARNING,
						// "View in connection line manager is null!");
						continue;
					}

					RemoteLevelElement remoteLevelElement = glView.getRemoteLevelElement();
					if (remoteLevelElement == null) {
						// ignore views that are not rendered remote
						continue;
					}

					RemoteLevel activeLevel = remoteLevelElement.getRemoteLevel();

					if (activeLevel == stackLevel || activeLevel == focusLevel) {
						int viewID = selectedElementRep.getSourceViewID();

						alPointLists = hashIDTypeToViewToPointLists.get(idType).get(viewID);
						if (alPointLists == null) {
							alPointLists = new ArrayList<ArrayList<Vec3f>>();
							viewToPointList.put(viewID, alPointLists);
						}

						alPointLists.add(selectedElementRep.getPoints());
					}
				}

			}
			if (viewToPointList.size() > 1) {
				renderLineBundling(gl, idType, new float[] { 0, 0, 0 });
				/*if (count == typeConnections.getValue().entrySet().size()-1)
					renderLineBundling(gl, idType, ConnectionLineRenderStyle.CONNECTION_LINE_COLOR, false);
				else if (count >= colors.size())
					return;
				else
					renderLineBundling(gl, idType, colors.get(count), true);
				*/
				hashIDTypeToViewToPointLists.clear();
			//count++;

			}
		}
	}

	protected abstract void renderLineBundling(final GL gl, EIDType idType, float[] fArColor);
	//!!!!Multiple Connection Lines!!!!
	//protected abstract void renderLineBundling(final GL gl, EIDType idType, float[] fArColor, boolean transparancy);
//	protected void renderLineBundling(final GL gl, EIDType idType, float[] fArColor) {
//		Set<Integer> keySet = hashIDTypeToViewToPointLists.get(idType).keySet();
//		HashMap<Integer, Vec3f> hashViewToCenterPoint = new HashMap<Integer, Vec3f>();
//
//		for (Integer iKey : keySet) {
//			hashViewToCenterPoint.put(iKey, calculateCenter(hashIDTypeToViewToPointLists.get(idType)
//				.get(iKey)));
//		}
//
//		Vec3f vecCenter = calculateCenter(hashViewToCenterPoint.values());
//
//		for (Integer iKey : keySet) {
//			Vec3f vecViewBundlingPoint = calculateBundlingPoint(hashViewToCenterPoint.get(iKey), vecCenter);
//
//			for (ArrayList<Vec3f> alCurrentPoints : hashIDTypeToViewToPointLists.get(idType).get(iKey)) {
//				if (alCurrentPoints.size() > 1) {
//					renderPlanes(gl, vecViewBundlingPoint, alCurrentPoints);
//				}
//				else {
//					renderLine(gl, vecViewBundlingPoint, alCurrentPoints.get(0), 0, hashViewToCenterPoint
//						.get(iKey), fArColor);
//				}
//			}
//
//			renderLine(gl, vecViewBundlingPoint, vecCenter, 0, fArColor);
//		}
//	}
	
	protected ArrayList<Vec3f> createControlPoints(Vec3f vecSrcPoint, Vec3f vecDstPoint, Vec3f vecViewCenterPoint) {
		ArrayList<Vec3f> controlPoints = new ArrayList<Vec3f>(3);
		controlPoints.add(vecDstPoint);
		controlPoints.add(calculateBundlingPoint(vecSrcPoint, vecViewCenterPoint));
		controlPoints.add(vecSrcPoint);
		return controlPoints;
	}

	protected Vec3f calculateBundlingPoint(Vec3f vecViewCenter, Vec3f vecCenter) {
		Vec3f vecDirection = new Vec3f();
		vecDirection = vecCenter.minus(vecViewCenter);
		float fLength = vecDirection.length();
		vecDirection.normalize();

		Vec3f vecViewBundlingPoint = new Vec3f();
		// Vec3f vecDestBundingPoint = new Vec3f();

		vecViewBundlingPoint = vecViewCenter.copy();
		vecDirection.scale(fLength / 1.7f);
		vecViewBundlingPoint.add(vecDirection);
		return vecViewBundlingPoint;
	}

	protected void renderPlanes(final GL gl, final Vec3f vecPoint, final ArrayList<Vec3f> alPoints) {

		gl.glColor4f(0.3f, 0.3f, 0.3f, 1f);// 0.6f);
		gl.glLineWidth(2 + 4);
		gl.glBegin(GL.GL_LINES);
		for (Vec3f vecCurrent : alPoints) {
			gl.glVertex3f(vecPoint.x(), vecPoint.y(), vecPoint.z() - 0.001f);
			gl.glVertex3f(vecCurrent.x(), vecCurrent.y(), vecCurrent.z() - 0.001f);
		}
		// gl.glVertex3f(vecPoint.x(), vecPoint.y(), vecPoint.z());
		// gl.glVertex3f(alPoints.get(0).x(), alPoints.get(0).y(),
		// alPoints.get(0).z());
		//		
		// gl.glVertex3f(vecPoint.x(), vecPoint.y(), vecPoint.z());
		// gl.glVertex3f(alPoints.get(alPoints.size()-1).x(),
		// alPoints.get(alPoints.size()-1).y(), alPoints.get(0).z());
		gl.glEnd();

		// gl.glColor4fv(ConnectionLineRenderStyle.CONNECTION_LINE_COLOR_1, 0);

		gl.glColor4fv(ConnectionLineRenderStyle.CONNECTION_LINE_COLOR, 0);

		gl.glLineWidth(2);
		gl.glBegin(GL.GL_LINES);
		for (Vec3f vecCurrent : alPoints) {
			gl.glVertex3f(vecPoint.x(), vecPoint.y(), vecPoint.z());
			gl.glVertex3f(vecCurrent.x(), vecCurrent.y(), vecCurrent.z());
		}
		// gl.glVertex3f(vecPoint.x(), vecPoint.y(), vecPoint.z());
		// gl.glVertex3f(alPoints.get(0).x(), alPoints.get(0).y(),
		// alPoints.get(0).z());
		//		
		// gl.glVertex3f(vecPoint.x(), vecPoint.y(), vecPoint.z());
		// gl.glVertex3f(alPoints.get(alPoints.size()-1).x(),
		// alPoints.get(alPoints.size()-1).y(), alPoints.get(0).z());

		gl.glEnd();

		gl.glColor4fv(ConnectionLineRenderStyle.CONNECTION_AREA_COLOR, 0);
		gl.glBegin(GL.GL_TRIANGLE_FAN);
		gl.glVertex3f(vecPoint.x(), vecPoint.y(), vecPoint.z());
		for (Vec3f vecCurrent : alPoints) {

			gl.glVertex3f(vecCurrent.x(), vecCurrent.y(), vecCurrent.z());
		}
		gl.glEnd();
	}

	/**
	 * Render curved connection lines.
	 * 
	 * @param gl
	 * @param vecSrcPoint
	 * @param vecDestPoint
	 * @param iNumberOfLines
	 * @param vecViewCenterPoint
	 * @param fArColor
	 */
//	private void renderLine(final GL gl, final Vec3f vecSrcPoint, final Vec3f vecDestPoint,
//		final int iNumberOfLines, Vec3f vecViewCenterPoint, float[] fArColor) {
//		Vec3f[] arSplinePoints = new Vec3f[3];
//
//		arSplinePoints[0] = vecSrcPoint.copy();
//		arSplinePoints[1] = calculateBundlingPoint(vecSrcPoint, vecViewCenterPoint);
//		arSplinePoints[2] = vecDestPoint.copy();
//
//		FloatBuffer splinePoints = FloatBuffer.allocate(8 * 3);
//		// float[] fArPoints =
//		// {1,2,-1,0,1,2,2,0,0,3,3,1,2,3,-2,1,3,1,1,3,0,2,-1,-1};
//		float[] fArPoints =
//			{ arSplinePoints[0].x(), arSplinePoints[0].y(), arSplinePoints[0].z(), arSplinePoints[1].x(),
//					arSplinePoints[1].y(), arSplinePoints[1].z(), arSplinePoints[2].x(),
//					arSplinePoints[2].y(), arSplinePoints[2].z() };
//		splinePoints.put(fArPoints);
//		splinePoints.rewind();
//
//		gl.glMap1f(GL.GL_MAP1_VERTEX_3, 0.0f, 1.0f, 3, 3, splinePoints);
//
//		// Line shadow
//		gl.glColor4fv(ConnectionLineRenderStyle.CONNECTION_LINE_SHADOW_COLOR, 0);
//		// gl.glColor4f(28/255f, 122/255f, 254/255f, 1f);
//		gl.glLineWidth(ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH + 2);
//		gl.glBegin(GL.GL_LINE_STRIP);
//		for (int i = 0; i <= 10; i++) {
//			gl.glEvalCoord1f((float) i / 10);
//		}
//		gl.glEnd();
//
//		// gl.glColor4fv(fArColor, 0);
//		// Point to mask artefacts
//		gl.glColor4fv(ConnectionLineRenderStyle.CONNECTION_LINE_COLOR, 0);
//		// gl.glColor4f(254/255f, 160/255f, 28/255f, 1f);
//
//		gl.glPointSize(ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH - 0.5f);
//		gl.glBegin(GL.GL_POINTS);
//		for (int i = 0; i <= 10; i++) {
//			gl.glEvalCoord1f((float) i / 10);
//		}
//		gl.glEnd();
//
//		// The spline
//		gl.glLineWidth(ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH);
//
//		gl.glBegin(GL.GL_LINE_STRIP);
//		for (int i = 0; i <= 10; i++) {
//			gl.glEvalCoord1f((float) i / 10);
//		}
//		gl.glEnd();
//	}

	// private void renderLine(final GL gl, final Vec3f vecSrcPoint, final Vec3f
	// vecDestPoint,
	// final int iNumberOfLines, Vec3f vecViewCenterPoint)
	// {
	// Vec3f[] arSplinePoints = new Vec3f[3];
	//		
	// // Vec3f vecDirection = new Vec3f();
	// // vecDirection = vecCenter.minus(vecViewCenter);
	// // float fLength = vecDirection.length();
	// // vecDirection.normalize();
	// //
	// // Vec3f vecViewBundlingPoint2 = new Vec3f();
	// // // Vec3f vecDestBundingPoint = new Vec3f();
	// //
	// // vecViewBundlingPoint = vecViewCenter.copy();
	// // vecDirection.scale(fLength / 3);
	// // vecViewBundlingPoint.add(vecDirection);
	//		
	// arSplinePoints[0] = vecSrcPoint.copy();
	// arSplinePoints[1] = calculateBundlingPoint(vecSrcPoint,
	// vecViewCenterPoint);
	// arSplinePoints[2] = vecDestPoint.copy();
	//		
	// // FIXME: Do not create spline in every render frame
	// Spline3D spline = new Spline3D(arSplinePoints, 0.001f, 0.01f);
	//		
	// // // Line shadow
	// // gl.glColor4f(0.3f, 0.3f, 0.3f, 1);// , 0.6f);
	// // gl.glLineWidth(ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH +
	// iNumberOfLines + 4);
	// // gl.glBegin(GL.GL_LINES);
	// // gl.glVertex3f(vecSrcPoint.x(), vecSrcPoint.y(), vecSrcPoint.z() -
	// 0.001f);
	// // gl.glVertex3f(vecDestPoint.x(), vecDestPoint.y(), vecDestPoint.z() -
	// 0.001f);
	// // gl.glEnd();
	//
	// gl.glColor4fv(ConnectionLineRenderStyle.CONNECTION_LINE_COLOR, 0);
	// gl.glLineWidth(ConnectionLineRenderStyle.CONNECTION_LINE_WIDTH +
	// iNumberOfLines);
	// gl.glBegin(GL.GL_LINES);
	//
	// for (int i=0; i<(arSplinePoints.length-1)*10; i++)
	// {
	// Vec3f vec = spline.getPositionAt((float)i / 10);
	// gl.glVertex3f(vec.x(), vec.y(), vec.z());
	// vec = spline.getPositionAt(((float)i+1) / 10);
	// gl.glVertex3f(vec.x(), vec.y(), vec.z());
	// }
	// // gl.glVertex3f(vecSrcPoint.x(), vecSrcPoint.y(), vecSrcPoint.z());
	// // gl.glVertex3f(vecDestPoint.x(), vecDestPoint.y(), vecDestPoint.z());
	//
	// gl.glEnd();
	// }

	protected Vec3f calculateCenter(ArrayList<ArrayList<Vec3f>> alPointLists) {
		Vec3f vecCenterPoint = new Vec3f(0, 0, 0);

		int iCount = 0;
		for (ArrayList<Vec3f> currentList : alPointLists) {
			for (Vec3f vecCurrent : currentList) {
				vecCenterPoint.add(vecCurrent);
				iCount++;
			}

		}
		vecCenterPoint.scale(1.0f / iCount);
		return vecCenterPoint;
	}

	protected Vec3f calculateCenter(Collection<Vec3f> pointCollection) {

		Vec3f vecCenterPoint = new Vec3f(0, 0, 0);

		int iCount = 0;

		for (Vec3f vecCurrent : pointCollection) {
			vecCenterPoint.add(vecCurrent);
			iCount++;
		}

		vecCenterPoint.scale(1.0f / iCount);
		return vecCenterPoint;
	}
	
	/** Helper method to find the id of heatmap or parallel coordinates
	 * 
	 * @return returns the id of the view if one has been found
	 */
	protected int getSpecialViewID(char type){
		if (focusLevel.getElementByPositionIndex(0).getGLView() != null){
			if ((type == HEATMAP) && (focusLevel.getElementByPositionIndex(0).getGLView() instanceof GLHeatMap))
				return focusLevel.getElementByPositionIndex(0).getGLView().getID();
			else if ((type == PARCOORDS) && (focusLevel.getElementByPositionIndex(0).getGLView() instanceof GLParallelCoordinates))
				return focusLevel.getElementByPositionIndex(0).getGLView().getID();
			else {
				for (int stack = 0; stack < stackLevel.getCapacity(); stack++) {
					if (stackLevel.getElementByPositionIndex(stack).getGLView() != null){
						if ((type == HEATMAP) && (stackLevel.getElementByPositionIndex(stack).getGLView() instanceof GLHeatMap))
							return stackLevel.getElementByPositionIndex(stack).getGLView().getID();
						else if ((type == PARCOORDS) && (stackLevel.getElementByPositionIndex(stack).getGLView() instanceof GLParallelCoordinates))
							return stackLevel.getElementByPositionIndex(stack).getGLView().getID();
					}
				}
			}
		}
		else {
			for (int stack = 0; stack < stackLevel.getCapacity(); stack++) {
				if (stackLevel.getElementByPositionIndex(stack).getGLView() != null){
					if ((type == HEATMAP) && (stackLevel.getElementByPositionIndex(stack).getGLView() instanceof GLHeatMap))
						return stackLevel.getElementByPositionIndex(stack).getGLView().getID();
					else if ((type == PARCOORDS) && (stackLevel.getElementByPositionIndex(stack).getGLView() instanceof GLParallelCoordinates))
						return stackLevel.getElementByPositionIndex(stack).getGLView().getID();
				}
			}		
		}
		return 0;
	}
	
	
	/**
	 * selects optimal points of views which have a set of points to choose from (atm this especially concerns HeatMap and Parallel Coordinates)
	 * @param idType
	 * @return returns a {@link HashMap} that contains the local center points
	 */
	protected HashMap<Integer, Vec3f> getOptimalDynamicPoints(EIDType idType) {
			
			Set<Integer> keySet = hashIDTypeToViewToPointLists.get(idType).keySet();
			ArrayList<ArrayList<Vec3f>> heatMapPoints = new ArrayList<ArrayList<Vec3f>>();
			ArrayList<ArrayList<Vec3f>> parCoordsPoints = new ArrayList<ArrayList<Vec3f>>();
			HashMap<Integer, Vec3f> hashViewToCenterPoint = new HashMap<Integer, Vec3f>();
			
			int heatMapID = getSpecialViewID(HEATMAP);
			int parCoordID = getSpecialViewID(PARCOORDS);

			for (Integer iKey : keySet) {
				if (iKey.equals(heatMapID))
					heatMapPoints = hashIDTypeToViewToPointLists.get(idType).get(iKey);
				else if (iKey.equals(parCoordID))
					parCoordsPoints = hashIDTypeToViewToPointLists.get(idType).get(iKey);
				else
					hashViewToCenterPoint.put(iKey, calculateCenter(hashIDTypeToViewToPointLists.get(idType).get(iKey)));
			}
			if ((heatMapID < 0) && (parCoordID < 0)){
				vecCenter = calculateCenter(hashViewToCenterPoint.values());
				return hashViewToCenterPoint;
			}
			
			ArrayList<ArrayList<Vec3f>> pointsList = new ArrayList<ArrayList<Vec3f>>();
			ArrayList<ArrayList<ArrayList<Vec3f>>> multiplePointsList = new ArrayList<ArrayList<ArrayList<Vec3f>>>();
			if ((heatMapPoints.size() < 7) && (parCoordsPoints.size() < 4)){
				pointsList = calculateOptimalSinglePoints(heatMapPoints, heatMapID, parCoordsPoints, parCoordID, hashViewToCenterPoint);
				if (pointsList == null)
					return null;
			}
			else {
				multiplePointsList = calculateOptimalMultiplePoints(heatMapPoints, heatMapID, parCoordsPoints, parCoordID, hashViewToCenterPoint);
				if (multiplePointsList == null)
					return null;
			}
					
			if (pointsList.size() >0){
				ArrayList<ArrayList<Vec3f>> tempArray = new ArrayList<ArrayList<Vec3f>>();
				if ((pointsList.size() == 2) && (pointsList.get(1).size() == 0)){
					tempArray.add(pointsList.get(0));
					hashIDTypeToViewToPointLists.get(idType).put(heatMapID, tempArray);
					hashViewToCenterPoint.put(heatMapID, pointsList.get(0).get(0));
				}
				else if (pointsList.size() == 2){
					tempArray.add(pointsList.get(0));
					hashIDTypeToViewToPointLists.get(idType).put(heatMapID, tempArray);
					hashViewToCenterPoint.put(heatMapID, pointsList.get(0).get(0));
					tempArray = new ArrayList<ArrayList<Vec3f>>();		
					tempArray.add(pointsList.get(1));
					hashIDTypeToViewToPointLists.get(idType).remove(parCoordID);
					hashIDTypeToViewToPointLists.get(idType).put(parCoordID, tempArray);
					hashViewToCenterPoint.put(parCoordID, pointsList.get(1).get(0));
				}
				else {
					tempArray.add(pointsList.get(0));
					hashIDTypeToViewToPointLists.get(idType).put(parCoordID, tempArray);
					hashViewToCenterPoint.put(parCoordID, pointsList.get(0).get(0));
					
				}
			}
			else if (multiplePointsList.size() > 0){
				if ((multiplePointsList.size() == 2) && (multiplePointsList.get(1).size() == 0)){
					hashIDTypeToViewToPointLists.get(idType).put(heatMapID, multiplePointsList.get(0));
					hashViewToCenterPoint.put(heatMapID, calculateCenter(multiplePointsList.get(0)));
				}
				else if (multiplePointsList.size() == 2){
					hashIDTypeToViewToPointLists.get(idType).put(heatMapID, multiplePointsList.get(0));
					hashViewToCenterPoint.put(heatMapID, calculateCenter(multiplePointsList.get(0)));
					hashIDTypeToViewToPointLists.get(idType).put(parCoordID, multiplePointsList.get(1));
					hashViewToCenterPoint.put(parCoordID, calculateCenter(multiplePointsList.get(1)));
				}
				else {
					hashIDTypeToViewToPointLists.get(idType).put(parCoordID,multiplePointsList.get(0));
					hashViewToCenterPoint.put(parCoordID, calculateCenter(multiplePointsList.get(0)));
					
				}				
			}
		return hashViewToCenterPoint;
	}

	protected abstract ArrayList<ArrayList<Vec3f>> calculateOptimalSinglePoints(
		ArrayList<ArrayList<Vec3f>> heatMapPoints, int heatMapID,
		ArrayList<ArrayList<Vec3f>> parCoordsPoints, int parCoordID,
		HashMap<Integer, Vec3f> hashViewToCenterPoint);

	protected abstract ArrayList<ArrayList<ArrayList<Vec3f>>> calculateOptimalMultiplePoints(
		ArrayList<ArrayList<Vec3f>> heatMapPoints, int heatMapID,
		ArrayList<ArrayList<Vec3f>> parCoordsPoints, int parCoordID,
		HashMap<Integer, Vec3f> hashViewToCenterPoint);

}