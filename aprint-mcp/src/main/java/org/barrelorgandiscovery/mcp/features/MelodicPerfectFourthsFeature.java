package org.barrelorgandiscovery.mcp.features;

import java.util.List;

/**
 * Feature: Melodic Perfect Fourths
 * 
 * Calculates the fraction of melodic intervals that are perfect fourths (5 semitones).
 * 
 * @author APrint Development Team
 */
public class MelodicPerfectFourthsFeature extends VirtualBookFeatureExtractor {
	
	public MelodicPerfectFourthsFeature() {
		this.code = "M-13";
		this.name = "Melodic Perfect Fourths";
		this.description = "Fraction of melodic intervals that are perfect fourths.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<Integer> intervals = MelodicIntervalHelper.calculateMelodicIntervals(context);
		double fraction = MelodicIntervalHelper.fractionOfIntervals(intervals, 5);
		return new double[] { fraction };
	}
}

