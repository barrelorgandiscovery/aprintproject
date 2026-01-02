package org.barrelorgandiscovery.mcp.features;

/**
 * Feature: Maximum Note Duration
 * 
 * Finds the duration of the longest note in the piece (in seconds).
 * 
 * @author APrint Development Team
 */
public class MaximumNoteDurationFeature extends VirtualBookFeatureExtractor {
	
	public MaximumNoteDurationFeature() {
		this.code = "RT-12";
		this.name = "Maximum Note Duration";
		this.description = "Duration of the longest note in the piece (in seconds).";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		double[] result = new double[1];
		result[0] = context.getMaxNoteDuration();
		return result;
	}
}

