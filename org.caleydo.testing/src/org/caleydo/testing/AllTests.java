package org.caleydo.testing;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.caleydo.testing.collection.VirtualArrayTester;

public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for org.caleydo.testing.command.data.filter");
		// $JUnit-BEGIN$
		// suite.addTestSuite(CmdDataFiterMinMaxTest.class);
		// suite.addTestSuite(CmdDataFilterMathTest.class);
//		suite.addTestSuite(NominalStringCCollectionTest.class);
		// $JUnit-END$
		suite.addTestSuite(VirtualArrayTester.class);
		return suite;
	}

}