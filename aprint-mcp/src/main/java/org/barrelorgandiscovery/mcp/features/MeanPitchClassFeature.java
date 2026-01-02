package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.List;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Mean Pitch Class
 * 
 * Calculates the mean pitch class value, averaged across all pitched notes.
 * A value of 0 corresponds to C, and pitches increase chromatically by semitone.
 * 
 * @author APrint Development Team
 */
public class MeanPitchClassFeature extends VirtualBookFeatureExtractor {
	
	public MeanPitchClassFeature() {
		this.code = "P-15";
		this.name = "Mean Pitch Class";
		this.description = "Mean pitch class value, averaged across all pitched notes in the piece. A value of 0 corresponds to C, and pitches increase chromatically by semitone.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<NoteInfo> allNotes = context.getAllNotes();
		List<Double> pitchClasses = new ArrayList<>();
		
		for (NoteInfo note : allNotes) {
			if (note.getMidiPitch() >= 0 && !note.isPercussion()) {
				int pitchClass = note.getMidiPitch() % 12;
				pitchClasses.add((double) pitchClass);
			}
		}
		
		if (pitchClasses.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		double mean = FeatureMathUtils.mean(pitchClasses);
		return new double[] { mean };
	}
}

