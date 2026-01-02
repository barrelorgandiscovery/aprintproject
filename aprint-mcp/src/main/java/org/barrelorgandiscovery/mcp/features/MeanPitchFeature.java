package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.List;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Mean Pitch
 * 
 * Calculates the mean MIDI pitch value, averaged across all pitched notes in the piece.
 * Set to 0 if there are no pitched notes.
 * 
 * @author APrint Development Team
 */
public class MeanPitchFeature extends VirtualBookFeatureExtractor {
	
	public MeanPitchFeature() {
		this.code = "P-14";
		this.name = "Mean Pitch";
		this.description = "Mean MIDI pitch value, averaged across all pitched notes in the piece. Set to 0 if there are no pitched notes.";
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
		
		double mean = FeatureMathUtils.mean(pitches);
		return new double[] { mean };
	}
}

