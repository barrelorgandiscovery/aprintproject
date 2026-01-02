package org.barrelorgandiscovery.mcp.features;

import java.util.List;

/**
 * Feature: Melodic Perfect Fifths
 * 
 * Calculates the fraction of melodic intervals that are perfect fifths (7 semitones).
 * 
 * @author APrint Development Team
 */
public class MelodicPerfectFifthsFeature extends VirtualBookFeatureExtractor {
	
	public MelodicPerfectFifthsFeature() {
		this.code = "M-15";
		this.name = "Melodic Perfect Fifths";
		this.description = "Fraction of melodic intervals that are perfect fifths.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<Integer> intervals = MelodicIntervalHelper.calculateMelodicIntervals(context);
		double fraction = MelodicIntervalHelper.fractionOfIntervals(intervals, 7);
		return new double[] { fraction };
	}
}

