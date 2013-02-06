/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander Lex, Christian Partl, Johannes Kepler
 * University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.data.importer.tcga;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import org.caleydo.core.io.ColumnDescription;
import org.caleydo.core.io.DataProcessingDescription;
import org.caleydo.core.io.DataSetDescription;
import org.caleydo.core.io.GroupingParseSpecification;
import org.caleydo.core.io.IDSpecification;
import org.caleydo.core.io.IDTypeParsingRules;
import org.caleydo.core.io.ParsingRule;
import org.caleydo.core.util.clusterer.algorithm.kmeans.KMeansClusterConfiguration;
import org.caleydo.core.util.clusterer.initialization.ClusterConfiguration;
import org.caleydo.core.util.clusterer.initialization.EDistanceMeasure;
import org.caleydo.datadomain.genetic.TCGADefinitions;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

public class TCGADataSetBuilder extends RecursiveTask<DataSetDescription> {

	private static final String CLUSTER_FILE = "outputprefix.expclu.gct";
	private static final int LEVEL = 4;

	private static final long serialVersionUID = 6468622325177694143L;

	private final EDataSetType dataSetType;
	private final String tumorAbbreviation;
	private boolean loadSampledGenes;

	private final FirehoseProvider fileProvider;

	private final String dataSetName;

	private TCGADataSetBuilder(String tumorAbbreviation, EDataSetType datasetType, String dataSetName,
			FirehoseProvider fileProvider, boolean loadSampledGenes) {
		this.tumorAbbreviation = tumorAbbreviation;
		this.dataSetType = datasetType;
		this.fileProvider = fileProvider;
		this.loadSampledGenes = loadSampledGenes;
		this.dataSetName = dataSetName;

	}

	public static ForkJoinTask<DataSetDescription> create(String tumorAbbreviation, EDataSetType datasetType,
			FirehoseProvider fileProvider) {
		return create(tumorAbbreviation, datasetType, fileProvider, true);
	}

	public static ForkJoinTask<DataSetDescription> create(String tumorAbbreviation, EDataSetType datasetType,
			String dataSetName, FirehoseProvider fileProvider) {
		return create(tumorAbbreviation, datasetType, dataSetName, fileProvider, true);
	}

	public static ForkJoinTask<DataSetDescription> create(String tumorAbbreviation, EDataSetType datasetType,
			FirehoseProvider fileProvider, boolean loadSampledGenes) {
		return create(tumorAbbreviation, datasetType, datasetType.getName(), fileProvider, loadSampledGenes);
	}

	public static ForkJoinTask<DataSetDescription> create(String tumorAbbreviation, EDataSetType datasetType,
			String dataSetName, FirehoseProvider fileProvider, boolean loadSampledGenes) {
		return new TCGADataSetBuilder(tumorAbbreviation, datasetType, dataSetName, fileProvider, loadSampledGenes);
	}

	@Override
	public DataSetDescription compute() {
		final IDSpecification sampleID = TCGADefinitions.createIDSpecification(true);

		// TCGA SAMPLE IDs look different for seq data (an "-01" is attached)
		final IDSpecification seqSampleID = TCGADefinitions.createIDSpecification(false);

		final IDSpecification clinicalColumnID = new IDSpecification();
		clinicalColumnID.setIdType("clinical");

		final IDSpecification geneRowID = IDSpecification.createGene();
		final IDSpecification proteinRowID = new IDSpecification("protein", "protein");
		final IDSpecification microRNARowID = new IDSpecification("microRNA", "microRNA");
		final IDSpecification clinicalRowID = new IDSpecification("TCGA_SAMPLE", "TCGA_SAMPLE");

		IDTypeParsingRules clinicalSampleIDTypeParsingRules = new IDTypeParsingRules();
		clinicalSampleIDTypeParsingRules.setSubStringExpression("tcga\\-");
		clinicalSampleIDTypeParsingRules.setToLowerCase(true);
		clinicalRowID.setIdTypeParsingRules(clinicalSampleIDTypeParsingRules);

		switch (dataSetType) {
		case mRNA:
			if (loadSampledGenes) {
				return setUpClusteredMatrixData(geneRowID, sampleID);
			} else {
				return setUpClusteredMatrixData("mRNA_Preprocess_Median", tumorAbbreviation + ".medianexp.txt",
						geneRowID, sampleID);
			}
		case mRNAseq:
			if (loadSampledGenes) {
				return setUpClusteredMatrixData(geneRowID, seqSampleID);
			} else {
				return setUpClusteredMatrixData("mRNAseq_Preprocess", tumorAbbreviation + ".mRNAseq_RPKM_log2.txt",
						geneRowID, seqSampleID);
			}
		case microRNA:
			if (loadSampledGenes) {
				return setUpClusteredMatrixData(microRNARowID, sampleID);
			} else {
				return setUpClusteredMatrixData("miR_Preprocess", tumorAbbreviation + ".miR_expression.txt",
						microRNARowID, sampleID);
			}
		case microRNAseq:
			if (loadSampledGenes) {
				return setUpClusteredMatrixData(microRNARowID, seqSampleID);
			} else {
				return setUpClusteredMatrixData("miRseq_Preprocess", tumorAbbreviation + ".miRseq_RPKM_log2.txt",
						microRNARowID, seqSampleID);
			}
		case methylation:
			return setUpClusteredMatrixData(geneRowID, sampleID);
		case RPPA:
			return setUpClusteredMatrixData(proteinRowID, sampleID);
		case clinical:
			return setUpClinicalData(clinicalRowID, clinicalColumnID);
		case mutation:
			return setUpMutationData(geneRowID, sampleID);
		case copyNumber:
			return setUpCopyNumberData(geneRowID, sampleID);
		}
		throw new IllegalStateException("uknown data set type: " + dataSetType);
	}

	private DataSetDescription setUpClusteredMatrixData(IDSpecification rowIDSpecification,
			IDSpecification sampleIDSpecification) {
		return setUpClusteredMatrixData(dataSetType.getTCGAAbbr() + "_Clustering_CNMF", CLUSTER_FILE,
				rowIDSpecification, sampleIDSpecification);
	}

	private DataSetDescription setUpClusteredMatrixData(String matrixArchiveName, String matrixFileName,
			IDSpecification rowIDSpecification, IDSpecification columnIDSpecification) {
		String cnmfArchiveName = dataSetType.getTCGAAbbr() + "_Clustering_CNMF";
		String hierarchicalArchiveName = dataSetType.getTCGAAbbr() + "_Clustering_Consensus";

		File matrixFile = fileProvider.extractAnalysisRunFile(matrixFileName, matrixArchiveName, LEVEL);
		if (matrixFile == null)
			return null;
		File cnmfGroupingFile = fileProvider.extractAnalysisRunFile("cnmf.membership.txt", cnmfArchiveName, LEVEL);
		if (cnmfGroupingFile == null)
			return null;

		DataSetDescription dataSet = new DataSetDescription();
		dataSet.setDataSetName(dataSetName);
		dataSet.setColor(dataSetType.getColor());
		dataSet.setDataSourcePath(matrixFile.getPath());
		if (loadSampledGenes) {
			// the gct files have 3 header lines and are centered<
			dataSet.setNumberOfHeaderLines(3);
			dataSet.setDataCenter(0d);
		} else {
			// the files with all the genes have the ids in the first row, then a row with "signal" and then the data
			dataSet.setNumberOfHeaderLines(2);
			dataSet.setRowOfColumnIDs(0);

		}

		ParsingRule parsingRule = new ParsingRule();
		parsingRule.setFromColumn(2);
		parsingRule.setParseUntilEnd(true);
		parsingRule.setColumnDescripton(new ColumnDescription("FLOAT", ColumnDescription.CONTINUOUS));
		dataSet.addParsingRule(parsingRule);
		dataSet.setTransposeMatrix(true);

		dataSet.setRowIDSpecification(rowIDSpecification);

		dataSet.setColumnIDSpecification(columnIDSpecification);

		GroupingParseSpecification firehoseCnmfClustering = new GroupingParseSpecification(cnmfGroupingFile.getPath());
		firehoseCnmfClustering.setContainsColumnIDs(false);
		firehoseCnmfClustering.setRowIDSpecification(columnIDSpecification);
		firehoseCnmfClustering.setGroupingName("CNMF Clustering");
		dataSet.addColumnGroupingSpecification(firehoseCnmfClustering);

		try {
			File hierarchicalGroupingFile = fileProvider.extractAnalysisRunFile(tumorAbbreviation + ".allclusters.txt",
					hierarchicalArchiveName, LEVEL);
			if (hierarchicalGroupingFile == null) {
				hierarchicalGroupingFile = fileProvider.extractAnalysisRunFile(tumorAbbreviation
						+ "-TP.allclusters.txt", hierarchicalArchiveName, LEVEL);
			}
			if (hierarchicalGroupingFile == null)
				throw new IllegalStateException("can't extract: " + tumorAbbreviation + ".allclusters.txt");
			GroupingParseSpecification firehoseHierarchicalClustering = new GroupingParseSpecification(
					hierarchicalGroupingFile.getPath());
			firehoseHierarchicalClustering.setContainsColumnIDs(false);
			firehoseHierarchicalClustering.setRowIDSpecification(columnIDSpecification);
			firehoseHierarchicalClustering.setGroupingName("Hierarchical Clustering");
			dataSet.addColumnGroupingSpecification(firehoseHierarchicalClustering);
		} catch (RuntimeException e) {
			System.err.println("can't extract hierarchical information " + e.getMessage());
		}

		DataProcessingDescription dataProcessingDescription = new DataProcessingDescription();
		ClusterConfiguration clusterConfiguration = new ClusterConfiguration();
		clusterConfiguration.setDistanceMeasure(EDistanceMeasure.EUCLIDEAN_DISTANCE);
		KMeansClusterConfiguration kMeansAlgo = new KMeansClusterConfiguration();
		kMeansAlgo.setNumberOfClusters(5);
		clusterConfiguration.setClusterAlgorithmConfiguration(kMeansAlgo);
		dataProcessingDescription.addRowClusterConfiguration(clusterConfiguration);
		dataSet.setDataProcessingDescription(dataProcessingDescription);

		if (loadSampledGenes) {
			// here we turn on sampling to 1500
			dataProcessingDescription.setNrRowsInSample(1500);
		}

		return dataSet;
	}

	private DataSetDescription setUpMutationData(IDSpecification rowIDSpecification,
			IDSpecification sampleIDSpecification) {

		int startColumn = 8;
		File mutationFile = fileProvider.extractAnalysisRunFile(tumorAbbreviation + ".per_gene.mutation_counts.txt",
				"Mutation_Significance", LEVEL);

		if (mutationFile == null)
			mutationFile = fileProvider.extractAnalysisRunFile(tumorAbbreviation + ".per_gene.mutation_counts.txt",
					"MutSigRun2.0", LEVEL);

		if (mutationFile == null) {
			File maf = fileProvider.extractAnalysisRunFile(tumorAbbreviation + "-TP.final_analysis_set.maf",
					"MutSigNozzleReport2.0", LEVEL);
			if (maf != null) {
				mutationFile = parseMAF(maf);
				startColumn = 1;
			}
		}

		if (mutationFile == null)
			return null;
		DataSetDescription dataSet = new DataSetDescription();
		dataSet.setDataSetName(dataSetName);
		dataSet.setColor(dataSetType.getColor());
		dataSet.setDataSourcePath(mutationFile.getPath());
		dataSet.setNumberOfHeaderLines(1);
		dataSet.setMax(1.f);

		ParsingRule parsingRule = new ParsingRule();
		parsingRule.setFromColumn(startColumn);
		parsingRule.setParseUntilEnd(true);
		parsingRule.setColumnDescripton(new ColumnDescription("FLOAT", ColumnDescription.NOMINAL));
		dataSet.addParsingRule(parsingRule);
		dataSet.setTransposeMatrix(true);

		// IDSpecification mutationSampleIDSpecification = new
		// IDSpecification();
		// mutationSampleIDSpecification.setIdCategory("TCGA_SAMPLE");
		// mutationSampleIDSpecification.setIdType("TCGA_SAMPLE");

		// Mutation uses a different ID convention, the source looks like this:
		// OV_20_0990
		// IDTypeParsingRules mutationSampleIDTypeParsingRules = new
		// IDTypeParsingRules();
		// mutationSampleIDTypeParsingRules.setReplacementExpression("-",
		// "\\_");
		// mutationSampleIDTypeParsingRules.setSubStringExpression("^[a-z]+\\-");
		// mutationSampleIDTypeParsingRules.setToLowerCase(true);
		// mutationSampleIDSpecification
		// .setIdTypeParsingRules(mutationSampleIDTypeParsingRules);
		dataSet.setColumnIDSpecification(sampleIDSpecification);
		dataSet.setRowIDSpecification(rowIDSpecification);

		return dataSet;
	}

	private File parseMAF(File maf) {
		File out = new File(maf.getParentFile(), "P" + maf.getName());
		if (out.exists())
			return out;
		final String TAB = "\t";

		try {
			List<String> lines = Files.readAllLines(maf.toPath(), Charset.defaultCharset());
			List<String> header = Arrays.asList(lines.get(0).split(TAB));
			lines = lines.subList(1, lines.size());
			int geneIndex = header.indexOf("Hugo_Symbol");
			int sampleIndex = header.indexOf("Tumor_Sample_Barcode");
			// gene x sample x mutated
			Table<String, String, Boolean> mutated = TreeBasedTable.create();
			for (String line : lines) {
				String[] columns = line.split(TAB);
				mutated.put(columns[geneIndex], columns[sampleIndex], Boolean.TRUE);
			}

			PrintWriter w = new PrintWriter(out);
			w.append("Hugo_Symbol");
			for (String sample : mutated.columnKeySet()) {
				w.append(TAB).append(sample);
			}
			w.println();
			for (String gene : mutated.rowKeySet()) {
				w.append(gene);
				for (String sample : mutated.columnKeySet()) {
					w.append(TAB).append(mutated.contains(gene, sample) ? "1" : "0");
				}
				w.println();
			}
			w.close();
			return out;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private DataSetDescription setUpCopyNumberData(IDSpecification rwoIDSpecification,
			IDSpecification sampleIDSpecification) {
		File copyNumberFile = fileProvider.extractAnalysisRunFile("all_thresholded.by_genes.txt", "CopyNumber_Gistic2",
				LEVEL);
		if (copyNumberFile == null)
			return null;

		DataSetDescription dataSet = new DataSetDescription();
		dataSet.setDataSetName(dataSetName);
		dataSet.setColor(dataSetType.getColor());
		dataSet.setDataSourcePath(copyNumberFile.getPath());
		dataSet.setNumberOfHeaderLines(1);

		ParsingRule parsingRule = new ParsingRule();
		parsingRule.setFromColumn(3);
		parsingRule.setParseUntilEnd(true);
		parsingRule.setColumnDescripton(new ColumnDescription("FLOAT", ColumnDescription.ORDINAL));
		dataSet.addParsingRule(parsingRule);
		dataSet.setTransposeMatrix(true);

		dataSet.setRowIDSpecification(rwoIDSpecification);
		dataSet.setColumnIDSpecification(sampleIDSpecification);

		return dataSet;
	}

	private DataSetDescription setUpClinicalData(IDSpecification rowIdSpecification,
			IDSpecification columnIdSpecification) {
		File clinicalFile = fileProvider.extractDataRunFile(tumorAbbreviation + ".clin.merged.picked.txt",
				"Clinical_Pick_Tier1", LEVEL);
		if (clinicalFile == null)
			return null;

		File out = new File(clinicalFile.getParentFile(), "T" + clinicalFile.getName());
		transposeCSV(clinicalFile.getPath(), out.getPath());

		DataSetDescription dataSet = new DataSetDescription();
		dataSet.setDataSetName(dataSetName);
		dataSet.setColor(dataSetType.getColor());
		dataSet.setDataHomogeneous(false);
		dataSet.setDataSourcePath(out.getPath());
		dataSet.setNumberOfHeaderLines(1);

		ParsingRule parsingRule = new ParsingRule();
		parsingRule.setFromColumn(2);
		parsingRule.setToColumn(4);
		parsingRule.setColumnDescripton(new ColumnDescription());
		dataSet.addParsingRule(parsingRule);

		dataSet.setRowIDSpecification(rowIdSpecification);
		dataSet.setColumnIDSpecification(columnIdSpecification);

		return dataSet;
	}

	private static void transposeCSV(String fileName, String fileNameOut) {
		File in = new File(fileName);
		File out = new File(fileNameOut);
		if (out.exists())
			return;

		List<String> data;
		try {
			data = Files.readAllLines(in.toPath(), Charset.defaultCharset());
		} catch (IOException e2) {
			e2.printStackTrace();
			return;
		}
		// split into parts
		String[][] parts = new String[data.size()][];
		int maxCol = -1;
		for (int i = 0; i < data.size(); ++i) {
			parts[i] = data.get(i).split("\t");
			if (parts[i].length > maxCol)
				maxCol = parts[i].length;
		}
		data = null;

		try (BufferedWriter writer = Files.newBufferedWriter(new File(fileNameOut).toPath(), Charset.defaultCharset())) {
			for (int c = 0; c < maxCol; ++c) {
				for (int i = 0; i < parts.length; ++i) {
					if (i > 0)
						writer.append('\t');
					String[] p = parts[i];
					if (p.length >= c)
						writer.append(p[c]);
				}
				writer.newLine();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
