package org.barrelorgandiscovery.mcp.features;

import java.util.HashMap;
import java.util.Map;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Most Common Pitch
 * 
 * Finds the MIDI pitch that appears most frequently in the virtual book.
 * 
 * @author APrint Development Team
 */
public class MostCommonPitchFeature extends VirtualBookFeatureExtractor {
	
	public MostCommonPitchFeature() {
		this.code = "P-3";
		this.name = "Most Common Pitch";
		this.description = "The MIDI pitch value that appears most frequently in the piece.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		Map<Integer, Integer> pitchCounts = new HashMap<>();
		
		for (NoteInfo note : context.getAllNotes()) {
			if (note.getMidiPitch() >= 0 && !note.isPercussion()) {
				pitchCounts.put(note.getMidiPitch(), 
					pitchCounts.getOrDefault(note.getMidiPitch(), 0) + 1);
			}
		}
		
		if (pitchCounts.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		int mostCommonPitch = 0;
		int maxCount = 0;
		for (Map.Entry<Integer, Integer> entry : pitchCounts.entrySet()) {
			if (entry.getValue() > maxCount) {
				maxCount = entry.getValue();
				mostCommonPitch = entry.getKey();
			}
		}
		
		return new double[] { mostCommonPitch };
	}
}

