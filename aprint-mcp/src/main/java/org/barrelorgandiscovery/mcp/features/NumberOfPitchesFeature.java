package org.barrelorgandiscovery.mcp.features;

/**
 * Feature: Number of Pitches
 * 
 * Calculates the number of unique MIDI pitches used in the virtual book.
 * 
 * @author APrint Development Team
 */
public class NumberOfPitchesFeature extends VirtualBookFeatureExtractor {
	
	public NumberOfPitchesFeature() {
		this.code = "P-2";
		this.name = "Number of Pitches";
		this.description = "The number of unique MIDI pitches used in the piece.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		double[] result = new double[1];
		result[0] = context.getUniquePitchCount();
		return result;
	}
}

