package org.barrelorgandiscovery.mcp.features;

import java.util.Map;

/**
 * Feature: Vertical Octaves
 * 
 * Calculates the fraction of all wrapped vertical intervals that are octaves (0 semitones, wrapped).
 * Weighted by how long intervals are held.
 * 
 * @author APrint Development Team
 */
public class VerticalOctavesFeature extends VirtualBookFeatureExtractor {
	
	public VerticalOctavesFeature() {
		this.code = "C-22";
		this.name = "Vertical Octaves";
		this.description = "Fraction of all wrapped vertical intervals that are octaves. This is weighted by how long intervals are held.";
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
		
		// Get octaves (interval 0, wrapped)
		long octavesDuration = histogram.getOrDefault(0, 0L);
		double fraction = (double) octavesDuration / totalDuration;
		
		return new double[] { fraction };
	}
}

