package org.caleydo.core.util.mapping;

import gleem.linalg.Vec3f;

import java.util.ArrayList;
import java.util.Iterator;

import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.collection.IStorage;
import org.caleydo.core.data.collection.SetType;
import org.caleydo.core.data.graph.item.vertex.EPathwayVertexType;
import org.caleydo.core.data.graph.item.vertex.PathwayVertexGraphItem;
import org.caleydo.core.data.graph.item.vertex.PathwayVertexGraphItemRep;
import org.caleydo.core.data.mapping.EGenomeMappingType;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.ILoggerManager.LoggerType;
import org.caleydo.core.manager.data.IGenomeIdManager;
import org.caleydo.core.util.mapping.color.ColorMapping;

/**
 * Class is responsible for mapping gene expression data
 * from one entity to another. 
 * 
 * @author Marc Streit
 *
 */
public abstract class AGenomeMapper {

	protected IGeneralManager generalManager;
	
	protected IGenomeIdManager genomeIdManager;
	
	protected ArrayList<IStorage> alMappingStorage;
	
	protected ColorMapping expressionColorMapping;
	
	/**
	 * Constructor. 
	 * 
	 * @param generalManager
	 */
	public AGenomeMapper(final IGeneralManager generalManager) {
	
		this.generalManager = generalManager;
		alMappingStorage = new ArrayList<IStorage>();
		genomeIdManager = generalManager.getGenomeIdManager();
		
		expressionColorMapping = new ColorMapping(0, 1);
	}
	
	public void setMappingData(final ArrayList<ISet> alSetData) {
		
		if (alSetData == null)
			return;
		
		Iterator<ISet> iterSetData = alSetData.iterator();
		
		while (iterSetData.hasNext())
		{
			ISet tmpSet = iterSetData.next();
			
			if (tmpSet.getSetType().equals(SetType.SET_GENE_EXPRESSION_DATA))
			{
				alMappingStorage.add(tmpSet.getStorageByDimAndIndex(0, 0));
			}
		}	
	}
	
	public final ArrayList<Vec3f> getMappingColorArrayByVertexRep(
			final PathwayVertexGraphItemRep pathwayVertexRep) {
		
		// Do nothing if picked node is invalid.
		if (pathwayVertexRep == null)
		{
			return new ArrayList<Vec3f>();
		}
		
		if (pathwayVertexRep.getPathwayVertexGraphItem().getType().equals(EPathwayVertexType.gene))
		{
			return getMappingColorArrayByGeneVertexRep(pathwayVertexRep);
		}
		else if (pathwayVertexRep.getPathwayVertexGraphItem().getType().equals(EPathwayVertexType.enzyme))
		{
			ArrayList<Vec3f> arMappingColor = new ArrayList<Vec3f>();
			arMappingColor.add(new Vec3f(-1, -1, -1)); // invalid color
			return arMappingColor;
		}
		
		return new ArrayList<Vec3f>();
	}
	
	public ArrayList<Vec3f> getMappingColorArrayByGeneID(
			String sGeneID) {
		
		// Remove prefix ("hsa:")
		sGeneID = sGeneID.substring(4);
		
		ArrayList<Vec3f> arMappingColor = new ArrayList<Vec3f>();
		
		int iGeneID = genomeIdManager.getIdIntFromStringByMapping(sGeneID, 
				EGenomeMappingType.NCBI_GENEID_CODE_2_NCBI_GENEID);
				
		if (iGeneID == -1)
		{	
			arMappingColor.add(new Vec3f(-1, -1, -1)); // invalid color
			return arMappingColor;
		}
		
		int iAccessionID = genomeIdManager.getIdIntFromIntByMapping(iGeneID, 
				EGenomeMappingType.NCBI_GENEID_2_ACCESSION);
	
		if (iAccessionID == -1)
		{	
			arMappingColor.add(new Vec3f(-1, -1, -1)); // invalid color
			return arMappingColor;
		}

		int iExpressionStorageIndex = genomeIdManager.getIdIntFromIntByMapping(iAccessionID,
				EGenomeMappingType.ACCESSION_2_MICROARRAY_EXPRESSION);

		if (iExpressionStorageIndex == -1)
		{	
			arMappingColor.add(new Vec3f(-1, -1, -1)); // invalid color
			return arMappingColor;
		}
		
		// Get rid of 770 internal ID identifier
		iExpressionStorageIndex = (int)(((float)iExpressionStorageIndex - 770.0f) / 1000.0f);

		Iterator<IStorage> iterMappingStorage = alMappingStorage.iterator();
		IStorage refExpressionStorage = null;
		
		while (iterMappingStorage.hasNext())
		{
			//Get expression value by MicroArrayID
			refExpressionStorage = iterMappingStorage.next();
			
			float[] bufferFloatArray = refExpressionStorage.getArrayFloat();
			
			if ( bufferFloatArray == null ) {
				this.generalManager.logMsg("color mapping failed, Storage=[" +
						refExpressionStorage.getLabel() + "][" +
						refExpressionStorage.toString() +
						"] does not contain float[]!",LoggerType.ERROR);
			}
						
			float fExpressionValue = bufferFloatArray[iExpressionStorageIndex];	
			arMappingColor.add(expressionColorMapping.colorMappingLookup(fExpressionValue));
		}
		
		return arMappingColor;
	}
	
	protected abstract ArrayList<Vec3f> getMappingColorArrayByGeneVertexRep(
			final PathwayVertexGraphItemRep pathwayVertexRep);
	
	protected abstract ArrayList<Vec3f> getMappingColorArrayByEnzymeVertex(
			final PathwayVertexGraphItem pathwayVertex);
}
