package org.barrelorgandiscovery.mcp.features;

import java.util.List;

/**
 * Feature: Melodic Sixths
 * 
 * Calculates the fraction of melodic intervals that are major or minor sixths (8 or 9 semitones).
 * 
 * @author APrint Development Team
 */
public class MelodicSixthsFeature extends VirtualBookFeatureExtractor {
	
	public MelodicSixthsFeature() {
		this.code = "M-16";
		this.name = "Melodic Sixths";
		this.description = "Fraction of melodic intervals that are major or minor sixths.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<Integer> intervals = MelodicIntervalHelper.calculateMelodicIntervals(context);
		double fraction = MelodicIntervalHelper.fractionOfIntervals(intervals, 8, 9);
		return new double[] { fraction };
	}
}

