package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Mean Melodic Interval
 * 
 * Calculates the mean average (in semitones) of the intervals involved in each
 * of the melodic intervals in the piece.
 * 
 * @author APrint Development Team
 */
public class MeanMelodicIntervalFeature extends VirtualBookFeatureExtractor {
	
	public MeanMelodicIntervalFeature() {
		this.code = "M-3";
		this.name = "Mean Melodic Interval";
		this.description = "Mean average (in semitones) of the intervals involved in each of the melodic intervals in the piece.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<NoteInfo> allNotes = context.getAllNotes();
		List<Double> melodicIntervals = new ArrayList<>();
		
		// Calculate melodic intervals for each track
		Map<Integer, List<NoteInfo>> notesByTrack = context.getNotesByTrack();
		
		for (Map.Entry<Integer, List<NoteInfo>> entry : notesByTrack.entrySet()) {
			List<NoteInfo> trackNotes = entry.getValue();
			
			// Sort by timestamp
			trackNotes = new ArrayList<>(trackNotes);
			trackNotes.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
			
			// Calculate intervals between consecutive notes
			for (int i = 1; i < trackNotes.size(); i++) {
				NoteInfo prev = trackNotes.get(i - 1);
				NoteInfo curr = trackNotes.get(i);
				
				if (prev.getMidiPitch() >= 0 && curr.getMidiPitch() >= 0 &&
					!prev.isPercussion() && !curr.isPercussion()) {
					int interval = Math.abs(curr.getMidiPitch() - prev.getMidiPitch());
					melodicIntervals.add((double) interval);
				}
			}
		}
		
		if (melodicIntervals.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		double mean = FeatureMathUtils.mean(melodicIntervals);
		return new double[] { mean };
	}
}

