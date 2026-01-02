package org.barrelorgandiscovery.mcp.features;

import java.util.List;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Last Pitch
 * 
 * Finds the MIDI pitch value of the last note in the piece.
 * 
 * @author APrint Development Team
 */
public class LastPitchFeature extends VirtualBookFeatureExtractor {
	
	public LastPitchFeature() {
		this.code = "P-35";
		this.name = "Last Pitch";
		this.description = "The MIDI pitch value of the last note in the piece.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<NoteInfo> allNotes = context.getAllNotes();
		
		if (allNotes.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		// Find the latest timestamp
		long latestTimestamp = 0;
		for (NoteInfo note : allNotes) {
			if (note.getMidiPitch() >= 0 && !note.isPercussion()) {
				latestTimestamp = Math.max(latestTimestamp, note.getTimestamp());
			}
		}
		
		// Find any pitch at the latest timestamp (or the first one found)
		for (NoteInfo note : allNotes) {
			if (note.getMidiPitch() >= 0 && !note.isPercussion() && 
				note.getTimestamp() == latestTimestamp) {
				return new double[] { note.getMidiPitch() };
			}
		}
		
		return new double[] { 0.0 };
	}
}

