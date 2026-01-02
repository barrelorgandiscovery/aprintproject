package org.barrelorgandiscovery.mcp.features;

import java.util.List;

/**
 * Feature: Amount of Staccato
 * 
 * Calculates the number of notes with a duration less than 0.1 seconds,
 * divided by the total number of notes in the piece.
 * 
 * @author APrint Development Team
 */
public class AmountOfStaccatoFeature extends VirtualBookFeatureExtractor {
	
	public AmountOfStaccatoFeature() {
		this.code = "RT-15";
		this.name = "Amount of Staccato";
		this.description = "Number of notes with a duration less than 0.1 seconds, divided by the total number of notes in the piece.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<Double> durations = context.getNoteDurations();
		
		if (durations.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		// Count notes with duration < 0.1 seconds
		int shortCount = 0;
		for (double duration : durations) {
			if (duration < 0.1) {
				shortCount++;
			}
		}
		
		double fraction = (double) shortCount / durations.size();
		return new double[] { fraction };
	}
}

