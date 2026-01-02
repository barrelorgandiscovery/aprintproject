package org.barrelorgandiscovery.mcp.features;

import java.util.List;

/**
 * Feature: Melodic Thirds
 * 
 * Calculates the fraction of melodic intervals that are major or minor thirds (3 or 4 semitones).
 * 
 * @author APrint Development Team
 */
public class MelodicThirdsFeature extends VirtualBookFeatureExtractor {
	
	public MelodicThirdsFeature() {
		this.code = "M-12";
		this.name = "Melodic Thirds";
		this.description = "Fraction of melodic intervals that are major or minor thirds.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<Integer> intervals = MelodicIntervalHelper.calculateMelodicIntervals(context);
		double fraction = MelodicIntervalHelper.fractionOfIntervals(intervals, 3, 4);
		return new double[] { fraction };
	}
}

