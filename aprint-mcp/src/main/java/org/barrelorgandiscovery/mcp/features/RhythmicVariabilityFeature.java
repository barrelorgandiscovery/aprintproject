package org.barrelorgandiscovery.mcp.features;

import java.util.List;

/**
 * Feature: Rhythmic Variability
 * 
 * Calculates the standard deviation of note durations, indicating
 * how much variation there is in the rhythmic values.
 * 
 * @author APrint Development Team
 */
public class RhythmicVariabilityFeature extends VirtualBookFeatureExtractor {
	
	public RhythmicVariabilityFeature() {
		this.code = "RT-27";
		this.name = "Rhythmic Variability";
		this.description = "Standard deviation of note durations, indicating rhythmic variability.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<Double> durations = context.getNoteDurations();
		
		if (durations.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		// Calculate standard deviation using utility
		double stdDev = FeatureMathUtils.standardDeviation(durations);
		
		return new double[] { stdDev };
	}
}

