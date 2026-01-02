package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.List;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Pitch Variability
 * 
 * Calculates the standard deviation of the MIDI pitches of all pitched notes in the piece.
 * Provides a measure of how close the pitches as a whole are to the mean pitch.
 * 
 * @author APrint Development Team
 */
public class PitchVariabilityFeature extends VirtualBookFeatureExtractor {
	
	public PitchVariabilityFeature() {
		this.code = "P-24";
		this.name = "Pitch Variability";
		this.description = "Standard deviation of the MIDI pitches of all pitched notes in the piece. Provides a measure of how close the pitches as a whole are to the mean pitch.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<NoteInfo> allNotes = context.getAllNotes();
		List<Double> pitches = new ArrayList<>();
		
		for (NoteInfo note : allNotes) {
			if (note.getMidiPitch() >= 0 && !note.isPercussion()) {
				pitches.add((double) note.getMidiPitch());
			}
		}
		
		if (pitches.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		double stdDev = FeatureMathUtils.standardDeviation(pitches);
		return new double[] { stdDev };
	}
}

