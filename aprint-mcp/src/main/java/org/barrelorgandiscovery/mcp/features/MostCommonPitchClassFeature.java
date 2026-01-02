package org.barrelorgandiscovery.mcp.features;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Most Common Pitch Class
 * 
 * Finds the pitch class that occurs most frequently. A value of 0 corresponds to C.
 * 
 * @author APrint Development Team
 */
public class MostCommonPitchClassFeature extends VirtualBookFeatureExtractor {
	
	public MostCommonPitchClassFeature() {
		this.code = "P-17";
		this.name = "Most Common Pitch Class";
		this.description = "The pitch class that occurs most frequently compared to other pitch classes. A value of 0 corresponds to C.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<NoteInfo> allNotes = context.getAllNotes();
		Map<Integer, Integer> pitchClassCounts = new HashMap<>();
		
		// Count occurrences of each pitch class
		for (NoteInfo note : allNotes) {
			if (note.getMidiPitch() >= 0 && !note.isPercussion()) {
				int pitchClass = note.getMidiPitch() % 12;
				pitchClassCounts.put(pitchClass, pitchClassCounts.getOrDefault(pitchClass, 0) + 1);
			}
		}
		
		if (pitchClassCounts.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		// Find most common pitch class
		int mostCommonPitchClass = 0;
		int maxCount = 0;
		for (Map.Entry<Integer, Integer> entry : pitchClassCounts.entrySet()) {
			if (entry.getValue() > maxCount) {
				maxCount = entry.getValue();
				mostCommonPitchClass = entry.getKey();
			}
		}
		
		return new double[] { mostCommonPitchClass };
	}
}

