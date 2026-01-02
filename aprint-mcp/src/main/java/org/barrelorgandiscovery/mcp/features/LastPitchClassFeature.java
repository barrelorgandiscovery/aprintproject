package org.barrelorgandiscovery.mcp.features;

import java.util.List;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Last Pitch Class
 * 
 * Finds the pitch class of the last note in the piece. If there are multiple notes
 * with simultaneous attacks at the end, the one with the lowest pitch is selected.
 * A value of 0 corresponds to C.
 * 
 * @author APrint Development Team
 */
public class LastPitchClassFeature extends VirtualBookFeatureExtractor {
	
	public LastPitchClassFeature() {
		this.code = "P-37";
		this.name = "Last Pitch Class";
		this.description = "The pitch class of the last note in the piece. If there are multiple notes with simultaneous attacks at the end, the one with the lowest pitch is selected. A value of 0 corresponds to C.";
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
		
		// Find the lowest pitch among notes starting at the latest timestamp
		int lowestPitch = 127;
		for (NoteInfo note : allNotes) {
			if (note.getMidiPitch() >= 0 && !note.isPercussion() && 
				note.getTimestamp() == latestTimestamp) {
				lowestPitch = Math.min(lowestPitch, note.getMidiPitch());
			}
		}
		
		if (lowestPitch == 127) {
			return new double[] { 0.0 };
		}
		
		// Pitch class = pitch % 12
		int pitchClass = lowestPitch % 12;
		return new double[] { pitchClass };
	}
}

