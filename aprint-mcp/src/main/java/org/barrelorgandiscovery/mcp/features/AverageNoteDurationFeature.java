package org.barrelorgandiscovery.mcp.features;

/**
 * Feature: Average Note Duration
 * 
 * Calculates the average duration of notes in seconds.
 * 
 * @author APrint Development Team
 */
public class AverageNoteDurationFeature extends VirtualBookFeatureExtractor {
	
	public AverageNoteDurationFeature() {
		this.code = "RT-13";
		this.name = "Average Note Duration";
		this.description = "Average duration of notes (in seconds).";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		double[] result = new double[1];
		result[0] = context.getAverageNoteDuration();
		return result;
	}
}

