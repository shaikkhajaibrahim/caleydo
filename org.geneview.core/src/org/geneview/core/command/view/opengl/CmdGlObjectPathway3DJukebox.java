package org.geneview.core.command.view.opengl;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.geneview.core.command.CommandQueueSaxType;
import org.geneview.core.command.view.opengl.ACmdGLObjectPathway3D;
import org.geneview.core.manager.ICommandManager;
import org.geneview.core.manager.IGeneralManager;
import org.geneview.core.parser.parameter.IParameterHandler;
import org.geneview.core.parser.parameter.IParameterHandler.ParameterHandlerType;
import org.geneview.core.util.exception.GeneViewRuntimeException;
import org.geneview.core.util.system.StringConversionTool;
import org.geneview.core.view.opengl.canvas.pathway.GLCanvasJukeboxPathway3D;

/**
 * @author Michael Kalkusch
 * @author Marc Streit
 *
 */
public class CmdGlObjectPathway3DJukebox 
extends ACmdGLObjectPathway3D {

	protected ArrayList<Integer> iArSetIDs;
	
	protected float [] fResolution;
	
	private int iMappingRowCount = -1;
	
	
	/**
	 * Constructor.
	 * 
	 */
	public CmdGlObjectPathway3DJukebox(
			final IGeneralManager refGeneralManager,
			final ICommandManager refCommandManager,
			final CommandQueueSaxType refCommandQueueSaxType)
	{
		super(refGeneralManager, 
				refCommandManager,
				refCommandQueueSaxType);
				
		iArSetIDs = new ArrayList<Integer>();

		localManagerObjectType = CommandQueueSaxType.CREATE_GL_JUKEBOX_PATHWAY_3D;
	}

	public void setParameterHandler( final IParameterHandler refParameterHandler ) {
	
		super.setParameterHandler(refParameterHandler);

		// Read SET IDs (Data and Selection) 
		String sPathwaySets = "";
		refParameterHandler.setValueAndTypeAndDefault("sPathwaySets",
				refParameterHandler.getValueString( 
						CommandQueueSaxType.TAG_DETAIL.getXmlKey() ),
				IParameterHandler.ParameterHandlerType.STRING,
				"-1");
		
		sPathwaySets = refParameterHandler.getValueString("sPathwaySets");
		
		StringTokenizer setToken = new StringTokenizer(
				sPathwaySets,
				IGeneralManager.sDelimiter_Parser_DataItems);

		while (setToken.hasMoreTokens())
		{
			iArSetIDs.add(StringConversionTool.convertStringToInt(
					setToken.nextToken(), -1));
		}
		
		fResolution = 
			StringConversionTool.convertStringToFloatArrayVariableLength(
					sAttribute3);
		
		refParameterHandler.setValueAndTypeAndDefault("mapping_row_count", 
				refParameterHandler.getValueString(CommandQueueSaxType.TAG_ATTRIBUTE1.getXmlKey()), 
				ParameterHandlerType.INT, "1");
		
		iMappingRowCount = refParameterHandler.getValueInt("mapping_row_count");
		
		setParameterHandler_DetailsPathway3D();	
	}

	@Override
	public void doCommandPart() throws GeneViewRuntimeException {
		
		GLCanvasJukeboxPathway3D canvas = 
			(GLCanvasJukeboxPathway3D) openGLCanvasUser;		
		
		canvas.setOriginRotation(cameraOrigin, cameraRotation);
		
		int[] iArTmp = new int[iArSetIDs.size()];
		for(int index = 0; index < iArSetIDs.size(); index++)
			iArTmp[index] = iArSetIDs.get(index);
		
		canvas.addSetId(iArTmp);
		canvas.setTextureTransparency(fSetTransparencyValue);
		canvas.setMappingRowCount(iMappingRowCount);
	}

	@Override
	public void undoCommandPart() throws GeneViewRuntimeException {

		GLCanvasJukeboxPathway3D canvas = 
			(GLCanvasJukeboxPathway3D) openGLCanvasUser;
		
		canvas.destroyGLCanvas();
		canvas = null;
	}
}
