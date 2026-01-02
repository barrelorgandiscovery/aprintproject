package org.barrelorgandiscovery.mcp.features;

import java.util.Map;

/**
 * Feature: Vertical Perfect Fifths
 * 
 * Calculates the fraction of all wrapped vertical intervals that are perfect fifths (7 semitones).
 * Weighted by how long intervals are held.
 * 
 * @author APrint Development Team
 */
public class VerticalPerfectFifthsFeature extends VirtualBookFeatureExtractor {
	
	public VerticalPerfectFifthsFeature() {
		this.code = "C-19";
		this.name = "Vertical Perfect Fifths";
		this.description = "Fraction of all wrapped vertical intervals that are perfect fifths. This is weighted by how long intervals are held.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		Map<Integer, Long> histogram = VerticalIntervalHelper.calculateVerticalIntervalHistogram(context);
		
		if (histogram.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		// Calculate total duration
		long totalDuration = 0;
		for (Long duration : histogram.values()) {
			totalDuration += duration;
		}
		
		if (totalDuration == 0) {
			return new double[] { 0.0 };
		}
		
		// Get perfect fifths (interval 7)
		long fifthsDuration = histogram.getOrDefault(7, 0L);
		double fraction = (double) fifthsDuration / totalDuration;
		
		return new double[] { fraction };
	}
}

