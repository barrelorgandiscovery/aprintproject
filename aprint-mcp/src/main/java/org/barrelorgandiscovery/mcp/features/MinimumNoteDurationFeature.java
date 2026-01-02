package org.barrelorgandiscovery.mcp.features;

/**
 * Feature: Minimum Note Duration
 * 
 * Finds the duration of the shortest note in the piece (in seconds).
 * Set to 0 if there are no notes.
 * 
 * @author APrint Development Team
 */
public class MinimumNoteDurationFeature extends VirtualBookFeatureExtractor {
	
	public MinimumNoteDurationFeature() {
		this.code = "RT-11";
		this.name = "Minimum Note Duration";
		this.description = "Duration of the shortest note in the piece (in seconds). Set to 0 if there are no notes.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		double[] result = new double[1];
		result[0] = context.getMinNoteDuration();
		return result;
	}
}

