package org.barrelorgandiscovery.mcp.features;

import java.util.List;

/**
 * Feature: Melodic Octaves
 * 
 * Calculates the fraction of melodic intervals that are octaves (12 semitones).
 * 
 * @author APrint Development Team
 */
public class MelodicOctavesFeature extends VirtualBookFeatureExtractor {
	
	public MelodicOctavesFeature() {
		this.code = "M-18";
		this.name = "Melodic Octaves";
		this.description = "Fraction of melodic intervals that are octaves.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<Integer> intervals = MelodicIntervalHelper.calculateMelodicIntervals(context);
		double fraction = MelodicIntervalHelper.fractionOfIntervals(intervals, 12);
		return new double[] { fraction };
	}
}

