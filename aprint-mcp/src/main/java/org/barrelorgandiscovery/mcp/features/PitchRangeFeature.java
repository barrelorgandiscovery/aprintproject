package org.barrelorgandiscovery.mcp.features;

/**
 * Feature: Pitch Range
 * 
 * Calculates the range of MIDI pitches used in the virtual book
 * (difference between highest and lowest pitch).
 * 
 * @author APrint Development Team
 */
public class PitchRangeFeature extends VirtualBookFeatureExtractor {
	
	public PitchRangeFeature() {
		this.code = "P-1";
		this.name = "Pitch Range";
		this.description = "The range of MIDI pitches used (difference between highest and lowest pitch).";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		double[] result = new double[1];
		result[0] = context.getPitchRange();
		return result;
	}
}

