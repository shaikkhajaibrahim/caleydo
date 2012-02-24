package org.caleydo.data.importer.jkubioinfo.startup;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.caleydo.core.data.collection.table.LoadDataParameters;
import org.caleydo.core.data.datadomain.DataDomainConfiguration;

/**
 * Description of a data set generated by Firehose for data types such as mRNA, miR or methylation data.
 * 
 * @author Nils Gehlenborg
 * @author Marc Streit
 * @author Alexander Lex
 */
@XmlType
@XmlRootElement
public class DataSetMetaInfo {

	private String dataPath;
	/** could be empty */
	private String groupingPath;

	/** A second grouping, might be empty */
	private String externalGroupingPath;
	private String name;
	/** could be empty */
	private String colorScheme;
	private String dataDomainType;
	private DataDomainConfiguration dataDomainConfiguration;
	private int column;

	private LoadDataParameters loadDataParameters;

	private boolean runClusteringOnRows = false;
	private boolean createGeneSamples = false;

	public String getDataPath() {
		return dataPath;
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	public String getGroupingPath() {
		return groupingPath;
	}

	public void setGroupingPath(String groupingPath) {
		this.groupingPath = groupingPath;
	}

	/**
	 * @param externalGroupingPath
	 *            setter, see {@link #externalGroupingPath}
	 */
	public void setExternalGroupingPath(String externalGroupingPath) {
		this.externalGroupingPath = externalGroupingPath;
		this.column = column;
	}

	/**
	 * @return the externalGroupingPath, see {@link #externalGroupingPath}
	 */
	public String getExternalGroupingPath() {
		return externalGroupingPath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColorScheme() {
		return colorScheme;
	}

	public void setColorScheme(String colorScheme) {
		this.colorScheme = colorScheme;
	}

	/**
	 * @param dataDomainType
	 *            setter, see {@link #dataDomainType}
	 */
	public void setDataDomainType(String dataDomainType) {
		this.dataDomainType = dataDomainType;
	}

	/**
	 * @return the dataDomainType, see {@link #dataDomainType}
	 */
	public String getDataDomainType() {
		return dataDomainType;
	}

	/**
	 * @param dataDomainConfiguration
	 *            setter, see {@link #dataDomainConfiguration}
	 */
	public void setDataDomainConfiguration(DataDomainConfiguration dataDomainConfiguration) {
		this.dataDomainConfiguration = dataDomainConfiguration;
	}

	/**
	 * @return the dataDomainConfiguration, see {@link #dataDomainConfiguration}
	 */
	public DataDomainConfiguration getDataDomainConfiguration() {
		return dataDomainConfiguration;
	}

	/**
	 * @param loadDataParameters
	 *            setter, see {@link #loadDataParameters}
	 */
	public void setLoadDataParameters(LoadDataParameters loadDataParameters) {
		this.loadDataParameters = loadDataParameters;
	}

	/**
	 * @return the loadDataParameters, see {@link #loadDataParameters}
	 */
	public LoadDataParameters getLoadDataParameters() {
		return loadDataParameters;
	}

	/**
	 * @param runClusteringOnRows
	 *            setter, see {@link #runClusteringOnRows}
	 */
	public void setRunClusteringOnRows(boolean runClusteringOnRows) {
		this.runClusteringOnRows = runClusteringOnRows;
	}

	/**
	 * @return the runClusteringOnRows, see {@link #runClusteringOnRows}
	 */
	public boolean isRunClusteringOnRows() {
		return runClusteringOnRows;
	}

	/**
	 * @param createGeneSamples
	 *            setter, see {@link #createGeneSamples}
	 */
	public void setCreateGeneSamples(boolean createGeneSamples) {
		this.createGeneSamples = createGeneSamples;
	}

	/**
	 * @return the createGeneSamples, see {@link #createGeneSamples}
	 */
	public boolean isCreateGeneSamples() {
		return createGeneSamples;
	}
	
	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	

	@Override
	public String toString() {
		return name;
	}
}
