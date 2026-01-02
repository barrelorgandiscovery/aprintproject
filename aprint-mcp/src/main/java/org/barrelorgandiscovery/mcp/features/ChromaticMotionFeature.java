package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Chromatic Motion
 * 
 * Calculates the fraction of melodic intervals that correspond to a semitone.
 * 
 * @author APrint Development Team
 */
public class ChromaticMotionFeature extends VirtualBookFeatureExtractor {
	
	public ChromaticMotionFeature() {
		this.code = "M-10";
		this.name = "Chromatic Motion";
		this.description = "Fraction of melodic intervals that correspond to a semitone.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<NoteInfo> allNotes = context.getAllNotes();
		List<Integer> intervals = new ArrayList<>();
		
		// Calculate melodic intervals for each track
		Map<Integer, List<NoteInfo>> notesByTrack = context.getNotesByTrack();
		
		for (Map.Entry<Integer, List<NoteInfo>> entry : notesByTrack.entrySet()) {
			List<NoteInfo> trackNotes = new ArrayList<>(entry.getValue());
			
			// Sort by timestamp
			trackNotes.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
			
			// Calculate intervals between consecutive notes
			for (int i = 1; i < trackNotes.size(); i++) {
				NoteInfo prev = trackNotes.get(i - 1);
				NoteInfo curr = trackNotes.get(i);
				
				if (prev.getMidiPitch() >= 0 && curr.getMidiPitch() >= 0 &&
					!prev.isPercussion() && !curr.isPercussion()) {
					int interval = Math.abs(curr.getMidiPitch() - prev.getMidiPitch());
					intervals.add(interval);
				}
			}
		}
		
		if (intervals.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		// Count chromatic intervals (1 semitone)
		int chromaticCount = 0;
		for (Integer interval : intervals) {
			if (interval == 1) {
				chromaticCount++;
			}
		}
		
		double fraction = (double) chromaticCount / intervals.size();
		return new double[] { fraction };
	}
}

