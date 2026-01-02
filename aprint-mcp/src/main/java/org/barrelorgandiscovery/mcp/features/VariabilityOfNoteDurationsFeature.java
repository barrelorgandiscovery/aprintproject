package org.barrelorgandiscovery.mcp.features;

import java.util.List;

/**
 * Feature: Variability of Note Durations
 * 
 * Calculates the standard deviation of note durations (in seconds).
 * 
 * @author APrint Development Team
 */
public class VariabilityOfNoteDurationsFeature extends VirtualBookFeatureExtractor {
	
	public VariabilityOfNoteDurationsFeature() {
		this.code = "RT-14";
		this.name = "Variability of Note Durations";
		this.description = "Standard deviation of note durations (in seconds).";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<Double> durations = context.getNoteDurations();
		
		if (durations.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		double stdDev = FeatureMathUtils.standardDeviation(durations);
		return new double[] { stdDev };
	}
}

