package org.barrelorgandiscovery.mcp.features;

/**
 * Feature: Duration in Seconds
 * 
 * Calculates the total duration of the piece in seconds.
 * 
 * @author APrint Development Team
 */
public class DurationInSecondsFeature extends VirtualBookFeatureExtractor {
	
	public DurationInSecondsFeature() {
		this.code = "RT-1";
		this.name = "Duration in Seconds";
		this.description = "Total duration of the piece in seconds.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		double[] result = new double[1];
		result[0] = context.getTotalLengthSeconds();
		return result;
	}
}

