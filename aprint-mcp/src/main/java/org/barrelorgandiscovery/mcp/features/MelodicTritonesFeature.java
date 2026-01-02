package org.barrelorgandiscovery.mcp.features;

import java.util.List;

/**
 * Feature: Melodic Tritones
 * 
 * Calculates the fraction of melodic intervals that are tritones (6 semitones).
 * 
 * @author APrint Development Team
 */
public class MelodicTritonesFeature extends VirtualBookFeatureExtractor {
	
	public MelodicTritonesFeature() {
		this.code = "M-14";
		this.name = "Melodic Tritones";
		this.description = "Fraction of melodic intervals that are tritones.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<Integer> intervals = MelodicIntervalHelper.calculateMelodicIntervals(context);
		double fraction = MelodicIntervalHelper.fractionOfIntervals(intervals, 6);
		return new double[] { fraction };
	}
}

