package org.barrelorgandiscovery.mcp.features;

import java.util.List;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: First Pitch Class
 * 
 * Finds the pitch class of the first note in the piece. If there are multiple notes
 * with simultaneous attacks at the beginning, the one with the lowest pitch is selected.
 * A value of 0 corresponds to C, and pitches increase chromatically by semitone.
 * 
 * @author APrint Development Team
 */
public class FirstPitchClassFeature extends VirtualBookFeatureExtractor {
	
	public FirstPitchClassFeature() {
		this.code = "P-35";
		this.name = "First Pitch Class";
		this.description = "The pitch class of the first note in the piece. If there are multiple notes with simultaneous attacks at the beginning, the one with the lowest pitch is selected. A value of 0 corresponds to C.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<NoteInfo> allNotes = context.getAllNotes();
		
		if (allNotes.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		// Find the earliest timestamp
		long earliestTimestamp = Long.MAX_VALUE;
		for (NoteInfo note : allNotes) {
			if (note.getMidiPitch() >= 0 && !note.isPercussion()) {
				earliestTimestamp = Math.min(earliestTimestamp, note.getTimestamp());
			}
		}
		
		if (earliestTimestamp == Long.MAX_VALUE) {
			return new double[] { 0.0 };
		}
		
		// Find the lowest pitch among notes starting at the earliest timestamp
		int lowestPitch = 127;
		for (NoteInfo note : allNotes) {
			if (note.getMidiPitch() >= 0 && !note.isPercussion() && 
				note.getTimestamp() == earliestTimestamp) {
				lowestPitch = Math.min(lowestPitch, note.getMidiPitch());
			}
		}
		
		// Pitch class = pitch % 12
		int pitchClass = lowestPitch % 12;
		return new double[] { pitchClass };
	}
}

