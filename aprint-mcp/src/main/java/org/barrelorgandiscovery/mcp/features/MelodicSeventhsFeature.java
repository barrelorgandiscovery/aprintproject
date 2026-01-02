package org.barrelorgandiscovery.mcp.features;

import java.util.List;

/**
 * Feature: Melodic Sevenths
 * 
 * Calculates the fraction of melodic intervals that are major or minor sevenths (10 or 11 semitones).
 * 
 * @author APrint Development Team
 */
public class MelodicSeventhsFeature extends VirtualBookFeatureExtractor {
	
	public MelodicSeventhsFeature() {
		this.code = "M-17";
		this.name = "Melodic Sevenths";
		this.description = "Fraction of melodic intervals that are major or minor sevenths.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<Integer> intervals = MelodicIntervalHelper.calculateMelodicIntervals(context);
		double fraction = MelodicIntervalHelper.fractionOfIntervals(intervals, 10, 11);
		return new double[] { fraction };
	}
}

