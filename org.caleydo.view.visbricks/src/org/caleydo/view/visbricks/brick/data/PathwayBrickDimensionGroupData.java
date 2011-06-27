package org.caleydo.view.visbricks.brick.data;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.data.virtualarray.ContentVirtualArray;
import org.caleydo.core.data.virtualarray.ISegmentData;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.manager.datadomain.ASetBasedDataDomain;
import org.caleydo.core.manager.datadomain.IDataDomain;
import org.caleydo.datadomain.pathway.data.PathwayDimensionGroupData;
import org.caleydo.datadomain.pathway.data.PathwaySegmentData;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.view.visbricks.brick.layout.IBrickConfigurer;
import org.caleydo.view.visbricks.brick.layout.PathwayDataConfigurer;

public class PathwayBrickDimensionGroupData implements IBrickDimensionGroupData {

	private PathwayDimensionGroupData dimensionGroupData;
	private PathwayDataConfigurer pathwayDataConfigurer;

	public PathwayBrickDimensionGroupData(
			PathwayDimensionGroupData dimensionGroupData) {
		this.dimensionGroupData = dimensionGroupData;
		pathwayDataConfigurer = new PathwayDataConfigurer();
	}

	@Override
	public ContentVirtualArray getSummaryBrickVA() {
		return dimensionGroupData.getSummaryVA();
	}

	@Override
	public ArrayList<ContentVirtualArray> getSegmentBrickVAs() {

		return dimensionGroupData.getSegmentVAs();
	}

	@Override
	public IDataDomain getDataDomain() {
		return dimensionGroupData.getDataDomain();
	}

	@Override
	public IBrickConfigurer getBrickConfigurer() {
		return pathwayDataConfigurer;
	}

	public ArrayList<PathwayGraph> getPathways() {
		return dimensionGroupData.getPathways();
	}

	@Override
	public ArrayList<Group> getGroups() {
		return dimensionGroupData.getGroups();
	}

	public ASetBasedDataDomain getMappingDataDomain() {
		return dimensionGroupData.getMappingDataDomain();
	}

	@Override
	public List<IBrickData> getSegmentBrickData() {

		List<ISegmentData> segmentData = dimensionGroupData.getSegmentData();

		List<IBrickData> segmentBrickData = new ArrayList<IBrickData>();

		for (ISegmentData data : segmentData) {
			segmentBrickData
					.add(new PathwayBrickData((PathwaySegmentData) data));
		}

		return segmentBrickData;
	}

	@Override
	public IBrickData getSummaryBrickData() {
		PathwaySegmentData tempSegmentData = new PathwaySegmentData(
				getDataDomain(), getMappingDataDomain(), getSummaryBrickVA(),
				new Group(), null, dimensionGroupData);
		return new PathwayBrickData(tempSegmentData);
	}

	@Override
	public IBrickSortingStrategy getDefaultSortingStrategy() {
		return new AlphabeticalDataLabelSortingStrategy();
	}

	@Override
	public int getID() {
		return dimensionGroupData.getID();
	}

}
