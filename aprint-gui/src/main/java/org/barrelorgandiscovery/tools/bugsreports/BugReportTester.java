package org.barrelorgandiscovery.tools.bugsreports;

import org.apache.log4j.Logger;

public class BugReportTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		BugReporter.init("TestApplication"); //$NON-NLS-1$
		Logger logger = Logger.getLogger(BugReportTester.class);
		
		logger.debug("hello world"); //$NON-NLS-1$
		
		BugReporter.sendBugReport();
		
	}

}
