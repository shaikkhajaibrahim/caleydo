package cerberus.view.gui.opengl.canvas.pathway;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.media.opengl.GL;

import cerberus.data.collection.ISet;
import cerberus.data.pathway.Pathway;
import cerberus.data.pathway.element.PathwayVertex;
import cerberus.data.view.rep.pathway.IPathwayVertexRep;
import cerberus.data.view.rep.pathway.renderstyle.PathwayRenderStyle;
import cerberus.manager.IGeneralManager;
import cerberus.util.mapping.EnzymeToExpressionColorMapper;
import cerberus.util.opengl.GLTextUtils;

/**
 * 
 * @author Marc Streit
 *
 */
public class GLPathwayManager {

	private IGeneralManager refGeneralManager;
	
	public static final float SCALING_FACTOR_X = 0.0025f;
	public static final float SCALING_FACTOR_Y = 0.0025f;
	
	private int iEnzymeNodeDisplayListId = -1;
	private int iCompoundNodeDisplayListId = -1;
	private int iHighlightedEnzymeNodeDisplayListId = -1;
	
	// First 200 IDs are reserved for picking of non pathway objects in the scene
	private int iUniqueObjectPickId = GLCanvasJukeboxPathway3D.FREE_PICKING_ID_RANGE_START;
	
	private PathwayRenderStyle refRenderStyle;

	private boolean bEnableGeneMapping = true;
	
	private HashMap<Integer, IPathwayVertexRep> refHashPickID2VertexRep;
	
	private HashMap<Integer, Integer> refHashPathwayId2DisplayListId;
	
	private EnzymeToExpressionColorMapper enzymeToExpressionColorMapper;
	
	private ArrayList<Integer> iAlSelectedElements;
	
	private HashMap<Integer, ArrayList<Color>> refHashElementId2MappingColorArray;
	
	/**
	 * Constructor.
	 */
	public GLPathwayManager(final IGeneralManager refGeneralManager) {
		
		this.refGeneralManager = refGeneralManager;
		
		refRenderStyle = new PathwayRenderStyle();
		refHashPickID2VertexRep = new HashMap<Integer, IPathwayVertexRep>();
		refHashPathwayId2DisplayListId = new HashMap<Integer, Integer>();
		refHashElementId2MappingColorArray = new HashMap<Integer, ArrayList<Color>>();
	}
	
	public void init(final GL gl, 
			final ArrayList<ISet> alSetData,
			final ArrayList<Integer> iAlSelectedElements) {
		
		buildEnzymeNodeDisplayList(gl);
		buildCompoundNodeDisplayList(gl);
		buildHighlightedEnzymeNodeDisplayList(gl);
		
		this.iAlSelectedElements = iAlSelectedElements;
		
		enzymeToExpressionColorMapper =
			new EnzymeToExpressionColorMapper(refGeneralManager, alSetData);
	}
	
	public void buildPathwayDisplayList(final GL gl, final int iPathwayID) {

		Pathway refTmpPathway = (Pathway)refGeneralManager.getSingelton().getPathwayManager().
			getItem(iPathwayID);
		
		int iVerticesDiplayListId = -1;
		if (refHashPathwayId2DisplayListId.containsKey(iPathwayID))
		{
			// Replace current display list if a display list exists
			iVerticesDiplayListId = refHashPathwayId2DisplayListId.get(iPathwayID);
		}
		else
		{
			// Creating display list for pathways
			iVerticesDiplayListId = gl.glGenLists(1);
			refHashPathwayId2DisplayListId.put(iPathwayID, iVerticesDiplayListId);			
		}
		
		gl.glNewList(iVerticesDiplayListId, GL.GL_COMPILE);	
		extractVertices(gl, refTmpPathway);
		gl.glEndList();
	}

	private void buildEnzymeNodeDisplayList(final GL gl) {

		// Creating display list for node cube objects
		iEnzymeNodeDisplayListId = gl.glGenLists(1);
		
		float fNodeWidth = refRenderStyle.getEnzymeNodeWidth(true);
		float fNodeHeight = refRenderStyle.getEnzymeNodeHeight(true);
		
		gl.glNewList(iEnzymeNodeDisplayListId, GL.GL_COMPILE);
		fillNodeDisplayList(gl, fNodeWidth, fNodeHeight);		
	    gl.glEndList();
	}
	
	protected void buildHighlightedEnzymeNodeDisplayList(final GL gl) {
		
		// Creating display list for node cube objects
		iHighlightedEnzymeNodeDisplayListId = gl.glGenLists(1);
		
		float fNodeWidth = refRenderStyle.getEnzymeNodeWidth(true);
		float fNodeHeight = refRenderStyle.getEnzymeNodeHeight(true);
		
		gl.glNewList(iHighlightedEnzymeNodeDisplayListId, GL.GL_COMPILE);
		fillNodeDisplayListFrame(gl, fNodeWidth, fNodeHeight);		
	    gl.glEndList();
	}
	
	protected void buildCompoundNodeDisplayList(final GL gl) {

		// Creating display list for node cube objects
		iCompoundNodeDisplayListId = gl.glGenLists(1);
		
		float fNodeWidth = refRenderStyle.getCompoundNodeWidth(true);
		float fNodeHeight = refRenderStyle.getCompoundNodeHeight(true);
		
		gl.glNewList(iCompoundNodeDisplayListId, GL.GL_COMPILE);
		fillNodeDisplayList(gl, fNodeWidth, fNodeHeight);
        gl.glEndList();
	}
	
	private void fillNodeDisplayList(final GL gl, 
			final float fNodeWidth, final float fNodeHeight) {
		
		gl.glBegin(GL.GL_QUADS);
		
        // FRONT FACE
		gl.glNormal3f( 0.0f, 0.0f, 1.0f);	
		// Top Right Of The Quad (Front)
        gl.glVertex3f(-fNodeWidth , -fNodeHeight, 0.015f);		
        // Top Left Of The Quad (Front)
        gl.glVertex3f(fNodeWidth, -fNodeHeight, 0.015f);			
        // Bottom Left Of The Quad (Front)
        gl.glVertex3f(fNodeWidth, fNodeHeight, 0.015f);
		// Bottom Right Of The Quad (Front)
        gl.glVertex3f(-fNodeWidth, fNodeHeight, 0.015f);

        // BACK FACE
        gl.glNormal3f( 0.0f, 0.0f,-1.0f);
        // Bottom Left Of The Quad (Back)
        gl.glVertex3f(fNodeWidth, -fNodeHeight,-0.015f);	
        // Bottom Right Of The Quad (Back)
        gl.glVertex3f(-fNodeWidth, -fNodeHeight,-0.015f);	
        // Top Right Of The Quad (Back)
        gl.glVertex3f(-fNodeWidth, fNodeHeight,-0.015f);			
        // Top Left Of The Quad (Back)
        gl.glVertex3f(fNodeWidth, fNodeHeight,-0.015f);			

		// TOP FACE
        gl.glNormal3f( 0.0f, 1.0f, 0.0f);	
        // Top Right Of The Quad (Top)
        gl.glVertex3f(fNodeWidth, fNodeHeight,-0.015f);	
        // Top Left Of The Quad (Top)
        gl.glVertex3f(-fNodeWidth, fNodeHeight,-0.015f);	
        // Bottom Left Of The Quad (Top)
        gl.glVertex3f(-fNodeWidth, fNodeHeight, 0.015f);
        // Bottom Right Of The Quad (Top)
        gl.glVertex3f(fNodeWidth, fNodeHeight, 0.015f);			

        // BOTTOM FACE
        gl.glNormal3f( 0.0f,-1.0f, 0.0f);	
        // Top Right Of The Quad (Bottom)
        gl.glVertex3f(fNodeWidth, -fNodeHeight, 0.015f);
        // Top Left Of The Quad (Bottom)
        gl.glVertex3f(-fNodeWidth, -fNodeHeight, 0.015f);
        // Bottom Left Of The Quad (Bottom)
        gl.glVertex3f(-fNodeWidth, -fNodeHeight,-0.015f);
        // Bottom Right Of The Quad (Bottom)
        gl.glVertex3f(fNodeWidth, -fNodeHeight,-0.015f);			

        // RIGHT FACE
        gl.glNormal3f( 1.0f, 0.0f, 0.0f);	
        // Top Right Of The Quad (Right)
        gl.glVertex3f(fNodeWidth, fNodeHeight,-0.015f);
        // Top Left Of The Quad (Right)
        gl.glVertex3f(fNodeWidth, fNodeHeight, 0.015f);
        // Bottom Left Of The Quad (Right)
        gl.glVertex3f(fNodeWidth, -fNodeHeight, 0.015f);
        // Bottom Right Of The Quad (Right)
        gl.glVertex3f(fNodeWidth, -fNodeHeight,-0.015f);			
        
        // LEFT FACE
        gl.glNormal3f(-1.0f, 0.0f, 0.0f);	
        // Top Right Of The Quad (Left)
        gl.glVertex3f(-fNodeWidth, fNodeHeight, 0.015f);	
        // Top Left Of The Quad (Left)
        gl.glVertex3f(-fNodeWidth, fNodeHeight,-0.015f);	
        // Bottom Left Of The Quad (Left)
        gl.glVertex3f(-fNodeWidth, -fNodeHeight,-0.015f);	
        // Bottom Right Of The Quad (Left)
        gl.glVertex3f(-fNodeWidth, -fNodeHeight, 0.015f);	
        
        gl.glEnd();
	}
	
	protected void fillNodeDisplayListFrame(final GL gl,
			final float fNodeWidth, final float fNodeHeight) {
	
		gl.glLineWidth(3);
		
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(-fNodeWidth, fNodeHeight, 0.02f);
		gl.glVertex3f(fNodeWidth, fNodeHeight, 0.02f);
		gl.glVertex3f(fNodeWidth, -fNodeHeight, 0.02f);
		gl.glVertex3f(-fNodeWidth, -fNodeHeight, 0.02f);		
        gl.glEnd();
	}
	
	private void extractVertices(final GL gl,
			Pathway refPathwayToExtract) {
		
	    Iterator<PathwayVertex> vertexIterator;
	    PathwayVertex vertex;
	    IPathwayVertexRep vertexRep;
		
        vertexIterator = refPathwayToExtract.getVertexListIterator();
        while (vertexIterator.hasNext())
        {
        	vertex = vertexIterator.next();
        	vertexRep = vertex.getVertexRepByIndex(0);

        	if (vertexRep != null)
        	{
        		createVertex(gl,
        				vertexRep, 
        				refPathwayToExtract);        	
        	}
        }   
	}
	
	private void createVertex(final GL gl, 
			IPathwayVertexRep vertexRep, 
			Pathway refContainingPathway) {
		
		Color tmpNodeColor = null;
		
		float fCanvasXPos = (vertexRep.getXPosition() * SCALING_FACTOR_X);
		float fCanvasYPos =	(vertexRep.getYPosition() * SCALING_FACTOR_Y);
		
		float fNodeWidth = vertexRep.getWidth() / 2.0f * SCALING_FACTOR_X;
		float fNodeHeight = vertexRep.getHeight() / 2.0f * SCALING_FACTOR_Y;
		
		// Create and store unique picking ID for that object
//		if (bPickingRendering)
//		{
			iUniqueObjectPickId++;
			gl.glLoadName(iUniqueObjectPickId);
			refHashPickID2VertexRep.put(iUniqueObjectPickId, vertexRep);			
//		}
		
		String sShapeType = vertexRep.getShapeType();
		
		gl.glTranslatef(fCanvasXPos, -fCanvasYPos, 0);

		// Pathway link
		if (sShapeType.equals("roundrectangle"))
		{		
			if (bEnableGeneMapping)
				tmpNodeColor = refRenderStyle.getPathwayNodeColor(true);
			else
				tmpNodeColor = refRenderStyle.getPathwayNodeColor(false);
				
			gl.glColor4f(tmpNodeColor.getRed() / 255.0f, 
					tmpNodeColor.getGreen() / 255.0f, 
					tmpNodeColor.getBlue() / 255.0f, 1.0f);

			fillNodeDisplayList(gl, fNodeWidth, fNodeHeight);
		}
		// Compound
		else if (sShapeType.equals("circle"))
		{				
			if (bEnableGeneMapping)
				tmpNodeColor = refRenderStyle.getCompoundNodeColor(true);
			else
				tmpNodeColor = refRenderStyle.getCompoundNodeColor(false);
			
			gl.glColor4f(tmpNodeColor.getRed() / 255.0f, 
					tmpNodeColor.getGreen() / 255.0f, 
					tmpNodeColor.getBlue() / 255.0f, 1.0f);
			gl.glCallList(iCompoundNodeDisplayListId);
		}	
		// Enzyme
		else if (sShapeType.equals("rectangle"))
		{	
			// Handle selection highlighting of element
			if (iAlSelectedElements.contains(
					vertexRep.getVertex().getElementId()))
			{
				tmpNodeColor = refRenderStyle.getHighlightedNodeColor();
				gl.glColor4f(tmpNodeColor.getRed() / 255.0f, 
						tmpNodeColor.getGreen() / 255.0f, 
						tmpNodeColor.getBlue() / 255.0f, 1.0f);
				gl.glCallList(iHighlightedEnzymeNodeDisplayListId);
			}
			
			if (bEnableGeneMapping)
			{
				mapExpression(gl, vertexRep.getVertex(), fNodeWidth);
			}
			else
			{
				tmpNodeColor = refRenderStyle.getEnzymeNodeColor(false);
				gl.glColor4f(tmpNodeColor.getRed() / 255.0f, 
					tmpNodeColor.getGreen() / 255.0f, 
					tmpNodeColor.getBlue() / 255.0f, 1.0f);
				
				gl.glCallList(iEnzymeNodeDisplayListId);
			}
		}
		
		gl.glTranslatef(-fCanvasXPos, fCanvasYPos, 0);
		
		// Draw Frame
		
		//gl.glPopName();
	}
	
	public void renderPathway(final GL gl, final int iPathwayID, boolean bRenderLabels) {
		
		// Creating hierarchical picking names
		// This is the layer of the pathways, therefore we can use the pathway
		// node picking ID
		int iTmpDisplayListID = refHashPathwayId2DisplayListId.get(iPathwayID);
		// FIXME: must be a unique ID!!!
//		gl.glPushName(iTmpDisplayListID);	
		gl.glCallList(iTmpDisplayListID);
//		gl.glPopName();
		
		if (bRenderLabels)
			renderLabels(gl, iPathwayID);
		
	}
	
	private void renderLabels(final GL gl, final int iPathwayID) {

	    Iterator<PathwayVertex> vertexIterator;
	    PathwayVertex vertex;
	    IPathwayVertexRep vertexRep;
		Pathway refTmpPathway = (Pathway)refGeneralManager.getSingelton().getPathwayManager().
			getItem(iPathwayID);
        
		vertexIterator = refTmpPathway.getVertexListIterator();
        while (vertexIterator.hasNext())
        {
        	vertex = vertexIterator.next();
        	vertexRep = vertex.getVertexRepByIndex(0);

        	if (vertexRep != null)
        	{
        		float fNodeWidth = vertexRep.getWidth() / 2.0f * SCALING_FACTOR_X;
        		float fNodeHeight = vertexRep.getHeight() / 2.0f * SCALING_FACTOR_Y;
        		float fCanvasXPos = (vertexRep.getXPosition() * SCALING_FACTOR_X);
        		float fCanvasYPos =	(vertexRep.getYPosition() * SCALING_FACTOR_Y);
 
        		gl.glTranslated(fCanvasXPos - fNodeWidth + 0.01f, -fCanvasYPos - 0.01f, 0);
        		gl.glColor3f(0, 0 , 0);
    			GLTextUtils.renderTextInRegion(gl, vertexRep.getName(), 10, 0, 0, 0.03f, fNodeWidth, fNodeHeight);
    			//GLTextUtils.renderText(gl, vertexRep.getName(), 0, 0, -0.03f);
    			gl.glTranslated(-fCanvasXPos + fNodeWidth - 0.01f, fCanvasYPos + 0.01f, 0);      	
        	}
        }  
	}
	
	public void mapExpression(final GL gl, 
			final PathwayVertex pathwayVertex, 
			final float fNodeWidth) {
		
		ArrayList<Color> alMappingColor;
		
		// Check if vertex is already mapped 
		if (refHashElementId2MappingColorArray.containsKey(pathwayVertex.getElementId()))
		{
			// Load existing mapping
			alMappingColor = refHashElementId2MappingColorArray.get(pathwayVertex.getElementId());
		}
		else
		{
			// Request mapping
			alMappingColor = enzymeToExpressionColorMapper.getMappingColorArrayByVertex(pathwayVertex);
			refHashElementId2MappingColorArray.put((Integer)pathwayVertex.getElementId(), alMappingColor);
		}
		
		drawMapping(gl, alMappingColor, fNodeWidth);
	}
	
	public void mapExpressionByGeneId(final GL gl, 
			String sGeneID, final float fNodeWidth) {

		drawMapping(gl, 
				enzymeToExpressionColorMapper.getMappingColorArrayByGeneID(sGeneID),
				fNodeWidth);
	}
	
	private void drawMapping(final GL gl,
			final ArrayList<Color> alMappingColor,
			final float fNodeWidth) {
		
		// Factor indicates how often the enzyme needs to be split
		// so that all genes can be mapped.
		int iSplitFactor = alMappingColor.size();
		Color tmpNodeColor = null;
		
		gl.glPushMatrix();
		
		if (iSplitFactor > 1)
		{
			gl.glTranslatef(-fNodeWidth, 0.0f, 0.0f);
			gl.glTranslatef(fNodeWidth * 2.0f / (iSplitFactor * 2.0f), 0.0f, 0.0f);
		}		
		for (int iSplitIndex = 0; iSplitIndex < iSplitFactor; iSplitIndex++)
		{
			tmpNodeColor = alMappingColor.get(iSplitIndex);
			
			// Check if the mapping gave back a valid color
			if (!tmpNodeColor.equals(Color.BLACK))
			{
				gl.glColor4f(tmpNodeColor.getRed() / 255.0f, 
						tmpNodeColor.getGreen() / 255.0f, 
						tmpNodeColor.getBlue() / 255.0f, 1.0f);
			
			}
			// Take the default color
			else
			{
				//gl.glColor4f(0.53f, 0.81f, 1.0f, 1.0f); // ligth blue
				tmpNodeColor = refRenderStyle.getEnzymeNodeColor(bEnableGeneMapping);
			}
			
			gl.glColor4f(tmpNodeColor.getRed() / 255.0f, 
					tmpNodeColor.getGreen() / 255.0f, 
					tmpNodeColor.getBlue() / 255.0f, 1.0f);
			
			gl.glScalef(1.0f / iSplitFactor, 1.0f, 1.0f);
			gl.glCallList(iEnzymeNodeDisplayListId);
			gl.glScalef(iSplitFactor, 1.0f, 1.0f);

			if (iSplitFactor > 1)
			{
				gl.glTranslatef(fNodeWidth * 2.0f / iSplitFactor, 0.0f, 0.0f);
			}
		}

		gl.glPopMatrix();

	}
	
	public IPathwayVertexRep getVertexRepByPickID(int iPickID) {
		
		return refHashPickID2VertexRep.get(iPickID);
	}
	
	public void clearOldPickingIDs() {
		
		refHashPickID2VertexRep.clear();
		iUniqueObjectPickId = GLCanvasJukeboxPathway3D.FREE_PICKING_ID_RANGE_START;
	}
}
