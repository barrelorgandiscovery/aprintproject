package org.barrelorgandiscovery.tools;

/**
 * Class for specify the profiling condition
 * 
 * @author Freydiere Patrice
 * 
 */
public class ProfilingCondition {

	/**
	 * Is the application currently profiling ?
	 * 
	 * @return
	 */
	public static boolean isProfiling() {
		return System.getProperty("profiling") != null;
	}

}
