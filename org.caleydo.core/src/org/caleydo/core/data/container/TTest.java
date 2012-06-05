/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *  
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
/**
 * 
 */
package org.caleydo.core.data.container;

import java.util.ArrayList;
import java.util.HashMap;

import org.caleydo.core.data.collection.table.DataTable;

/**
 * @author alexsb
 */
public class TTest {

	HashMap<DataContainer, ArrayList<Double>> dataContainerToTwoSidedTTestResult =
		new HashMap<DataContainer, ArrayList<Double>>();

	double[] oneSidedTTestResult;

	// DataContainer container;

	public void setTwoSiddedTTestResult(DataContainer set, ArrayList<Double> resultVector) {
		dataContainerToTwoSidedTTestResult.put(set, resultVector);
	}

	public ArrayList<Double> getTwoSidedTTestResult(DataContainer dataContainer) {

		return dataContainerToTwoSidedTTestResult.get(dataContainer);
	}

	public Double getTwoSidedTTestResult(DataTable dataContainer, Integer recordID) {

		return dataContainerToTwoSidedTTestResult.get(dataContainer).get(recordID);
	}

	public void setOneSiddedTTestResult(double[] pValueVector) {
		oneSidedTTestResult = pValueVector;
	}

	public double[] getOneSidedTTestResult() {
		return oneSidedTTestResult;
	}

	public HashMap<DataContainer, ArrayList<Double>> getAllTwoSidedTTestResults() {
		return dataContainerToTwoSidedTTestResult;
	}

}
