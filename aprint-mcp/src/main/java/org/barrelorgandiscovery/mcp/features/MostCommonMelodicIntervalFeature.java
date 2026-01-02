package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Most Common Melodic Interval
 * 
 * Finds the number of semitones corresponding to the most frequently occurring
 * melodic interval.
 * 
 * @author APrint Development Team
 */
public class MostCommonMelodicIntervalFeature extends VirtualBookFeatureExtractor {
	
	public MostCommonMelodicIntervalFeature() {
		this.code = "M-2";
		this.name = "Most Common Melodic Interval";
		this.description = "Number of semitones corresponding to the most frequently occurring melodic interval.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<NoteInfo> allNotes = context.getAllNotes();
		Map<Integer, Integer> intervalCounts = new HashMap<>();
		
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
					intervalCounts.put(interval, intervalCounts.getOrDefault(interval, 0) + 1);
				}
			}
		}
		
		if (intervalCounts.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		// Find most common interval
		int mostCommonInterval = 0;
		int maxCount = 0;
		for (Map.Entry<Integer, Integer> entry : intervalCounts.entrySet()) {
			if (entry.getValue() > maxCount) {
				maxCount = entry.getValue();
				mostCommonInterval = entry.getKey();
			}
		}
		
		return new double[] { mostCommonInterval };
	}
}

