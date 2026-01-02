package org.barrelorgandiscovery.mcp.features;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Number of Pitch Classes
 * 
 * Finds the number of pitch classes that occur at least once in the piece.
 * Enharmonic equivalents are grouped together.
 * 
 * @author APrint Development Team
 */
public class NumberOfPitchClassesFeature extends VirtualBookFeatureExtractor {
	
	public NumberOfPitchClassesFeature() {
		this.code = "P-5";
		this.name = "Number of Pitch Classes";
		this.description = "Number of pitch classes that occur at least once in the piece. Enharmonic equivalents are grouped together.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<NoteInfo> allNotes = context.getAllNotes();
		Set<Integer> pitchClasses = new HashSet<>();
		
		// Collect unique pitch classes
		for (NoteInfo note : allNotes) {
			if (note.getMidiPitch() >= 0 && !note.isPercussion()) {
				int pitchClass = note.getMidiPitch() % 12;
				pitchClasses.add(pitchClass);
			}
		}
		
		return new double[] { pitchClasses.size() };
	}
}

