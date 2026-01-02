package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.List;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Pitch Class Variability
 * 
 * Calculates the standard deviation of the pitch classes of all pitched notes.
 * Provides a measure of how close the pitch classes are to the mean pitch class.
 * 
 * @author APrint Development Team
 */
public class PitchClassVariabilityFeature extends VirtualBookFeatureExtractor {
	
	public PitchClassVariabilityFeature() {
		this.code = "P-25";
		this.name = "Pitch Class Variability";
		this.description = "Standard deviation of the pitch classes (where 0 corresponds to C, 1 to C#/Db, etc.) of all pitched notes in the piece. Provides a measure of how close the pitch classes as a whole are to the mean pitch class.";
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
		
		double stdDev = FeatureMathUtils.standardDeviation(pitchClasses);
		return new double[] { stdDev };
	}
}

