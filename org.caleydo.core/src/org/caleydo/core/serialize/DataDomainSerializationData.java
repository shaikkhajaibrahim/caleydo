package org.caleydo.core.serialize;

import java.util.HashMap;

import org.caleydo.core.data.virtualarray.RecordVirtualArray;
import org.caleydo.core.data.virtualarray.DimensionVirtualArray;
import org.caleydo.core.manager.datadomain.ATableBasedDataDomain;

/**
 * Bean that holds the initialization data for new started caleydo application. Used to store and restore
 * project or to sync remote clients.
 * 
 * @author Werner Puff
 * @author Alexander Lex
 */
public class DataDomainSerializationData {

	/** defines the type of usage of the application */
	private ATableBasedDataDomain dataDomain;

	/** content of the set file the application is based on, only used to sync remote clients */
	private byte[] setFileContent;

	/** gene cluster information, only used to sync remote clients */
	private String geneClusterTree;

	/** experiment cluster information, only used to sync remote clients */
	private String experimentClusterTree;

	/** virtual arrays of this application stored in relation with their their-key */
	private HashMap<String, RecordVirtualArray> recordVAMap;
	private HashMap<String, DimensionVirtualArray> dimensionVAMap;

	public ATableBasedDataDomain getDataDomain() {
		return dataDomain;
	}

	public void setDataDomain(ATableBasedDataDomain dataDomain) {
		this.dataDomain = dataDomain;
	}

	public byte[] getDataTableFileContent() {
		return setFileContent;
	}

	public void setDataTableFileContent(byte[] setFileContent) {
		this.setFileContent = setFileContent;
	}

	public HashMap<String, RecordVirtualArray> getRecordVAMap() {
		return recordVAMap;
	}

	public void setRecordVAMap(HashMap<String, RecordVirtualArray> recordVAMap) {
		this.recordVAMap = recordVAMap;
	}

	public HashMap<String, DimensionVirtualArray> getDimensionVAMap() {
		return dimensionVAMap;
	}

	public void setDimensionVAMap(HashMap<String, DimensionVirtualArray> dimensionVAMap) {
		this.dimensionVAMap = dimensionVAMap;
	}

	public String getGeneClusterTree() {
		return geneClusterTree;
	}

	public void setGeneClusterTree(String geneClusterTree) {
		this.geneClusterTree = geneClusterTree;
	}

	public String getExperimentClusterTree() {
		return experimentClusterTree;
	}

	public void setExperimentClusterTree(String experimentClusterTree) {
		this.experimentClusterTree = experimentClusterTree;
	}
}
