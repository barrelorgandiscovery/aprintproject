package org.barrelorgandiscovery.mcp.features;

/**
 * Feature: Total Number of Notes
 * 
 * Calculates the total number of holes/notes in the virtual book.
 * 
 * @author APrint Development Team
 */
public class TotalNumberOfNotesFeature extends VirtualBookFeatureExtractor {
	
	public TotalNumberOfNotesFeature() {
		this.code = "R-9";
		this.name = "Total Number of Notes";
		this.description = "Total number of notes (holes) in the virtual book.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		double[] result = new double[1];
		result[0] = context.getTotalHoleCount();
		return result;
	}
}

