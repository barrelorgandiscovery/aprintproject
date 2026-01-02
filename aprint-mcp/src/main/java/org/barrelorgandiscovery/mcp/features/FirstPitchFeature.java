package org.barrelorgandiscovery.mcp.features;

import java.util.List;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: First Pitch
 * 
 * Finds the MIDI pitch value of the first note in the piece.
 * If there are multiple notes with simultaneous attacks, the lowest pitch is selected.
 * 
 * @author APrint Development Team
 */
public class FirstPitchFeature extends VirtualBookFeatureExtractor {
	
	public FirstPitchFeature() {
		this.code = "P-34";
		this.name = "First Pitch";
		this.description = "The MIDI pitch value of the first note in the piece. " +
			"If there are multiple notes with simultaneous attacks, the lowest pitch is selected.";
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
		
		return new double[] { lowestPitch };
	}
}

