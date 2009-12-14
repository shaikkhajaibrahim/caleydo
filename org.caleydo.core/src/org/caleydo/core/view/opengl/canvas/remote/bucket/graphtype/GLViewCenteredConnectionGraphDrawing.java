package org.caleydo.core.view.opengl.canvas.remote.bucket.graphtype;

import gleem.linalg.Vec3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.media.opengl.GL;

import org.caleydo.core.data.mapping.EIDType;
import org.caleydo.core.view.opengl.canvas.remote.bucket.GraphDrawingUtils;
import org.caleydo.core.view.opengl.util.hierarchy.RemoteLevel;
import org.caleydo.core.view.opengl.util.vislink.VisLinkScene;

/**
 * Specialized connection line renderer for bucket view.
 * 
 * @author Alexander Lex
 * @author Marc Streit
 */
public class GLViewCenteredConnectionGraphDrawing
	extends GraphDrawingUtils {

	protected RemoteLevel focusLevel;
	protected RemoteLevel stackLevel;

	/**
	 * Constructor.
	 * 
	 * @param focusLevel
	 * @param stackLevel
	 */
	public GLViewCenteredConnectionGraphDrawing(final RemoteLevel focusLevel, final RemoteLevel stackLevel) {

		super(focusLevel, stackLevel);

		this.focusLevel = focusLevel;
		this.stackLevel = stackLevel;
	}

	protected void renderLineBundling(final GL gl, EIDType idType, float[] fArColor) {
		Set<Integer> keySet = hashIDTypeToViewToPointLists.get(idType).keySet();
		HashMap<Integer, Vec3f> hashViewToCenterPoint = new HashMap<Integer, Vec3f>();
		
		for (Integer iKey : keySet) {
			hashViewToCenterPoint.put(iKey, calculateCenter(hashIDTypeToViewToPointLists.get(idType).get(iKey)));
		}
		Vec3f activeViewBundlingPoint = new Vec3f();
		Vec3f vecCenter = calculateCenter(hashViewToCenterPoint.values());
		ArrayList<ArrayList<ArrayList<Vec3f>>> connectionLinesAllViews = new ArrayList<ArrayList<ArrayList<Vec3f>>>(4);
		ArrayList<ArrayList<Vec3f>> connectionLinesActiveView = new ArrayList<ArrayList<Vec3f>>();
		//ArrayList<ArrayList<Vec3f>> bundlingToCenterLinesActiveView = new ArrayList<ArrayList<Vec3f>>();
		ArrayList<ArrayList<Vec3f>> bundlingToCenterLinesOtherViews = new ArrayList<ArrayList<Vec3f>>();
		ArrayList<ArrayList<Vec3f>> connectionLinesOtherViews = new ArrayList<ArrayList<Vec3f>>();
		
		

		for (Integer iKey : keySet) {
			Vec3f vecViewBundlingPoint = calculateBundlingPoint(hashViewToCenterPoint.get(iKey), vecCenter);
			if(iKey.equals(activeViewID))
				activeViewBundlingPoint = vecViewBundlingPoint;
			ArrayList<Vec3f> pointsToDepthSort = new ArrayList<Vec3f>();
			

			for (ArrayList<Vec3f> alCurrentPoints : hashIDTypeToViewToPointLists.get(idType).get(iKey)) {
				if (alCurrentPoints.size() > 1) {
					renderPlanes(gl, vecViewBundlingPoint, alCurrentPoints);
				}
				else
					pointsToDepthSort.add(alCurrentPoints.get(0));
			}
//			for(Vec3f currentPoint : depthSort(pointsToDepthSort))				
//				renderLine(gl, vecViewBundlingPoint, currentPoint, 0, hashViewToCenterPoint.get(iKey), fArColor);
			
			for(Vec3f currentPoint : depthSort(pointsToDepthSort)) {
				if(activeViewID != -1 && iKey == activeViewID)
					connectionLinesActiveView.add( createControlPoints( vecViewBundlingPoint, currentPoint, hashViewToCenterPoint.get(iKey) ) );
				else
					connectionLinesOtherViews.add( createControlPoints( vecViewBundlingPoint, currentPoint, hashViewToCenterPoint.get(iKey) ) );
			}
			
//			renderLine(gl, vecViewBundlingPoint, vecCenter, 0, fArColor);

/*			ArrayList<Vec3f> bundlingToCenter = new ArrayList<Vec3f>(2);
			bundlingToCenter.add(vecViewBundlingPoint);
			bundlingToCenter.add(vecCenter);
*/
			if (activeViewID != -1 && iKey != activeViewID)
				bundlingToCenterLinesOtherViews.add(createControlPoints(activeViewBundlingPoint, vecViewBundlingPoint, vecCenter));
		}
		
		connectionLinesAllViews.add(connectionLinesActiveView);
		//connectionLinesAllViews.add(bundlingToCenterLinesActiveView);
		connectionLinesAllViews.add(bundlingToCenterLinesOtherViews);
		connectionLinesAllViews.add(connectionLinesOtherViews);
		
		VisLinkScene visLinkScene = new VisLinkScene(connectionLinesAllViews);
		visLinkScene.renderLines(gl);
	}
}