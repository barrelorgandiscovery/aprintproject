package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Most Common Vertical Interval
 * 
 * Finds the interval in semitones corresponding to the most common vertical interval
 * (interval between simultaneous pitches) in the piece.
 * 
 * @author APrint Development Team
 */
public class MostCommonVerticalIntervalFeature extends VirtualBookFeatureExtractor {
	
	public MostCommonVerticalIntervalFeature() {
		this.code = "C-8";
		this.name = "Most Common Vertical Interval";
		this.description = "The interval in semitones corresponding to the most common vertical interval (interval between simultaneous pitches) in the piece.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<NoteInfo> allNotes = context.getAllNotes();
		
		if (allNotes.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		// Create timeline of active notes
		List<TimePoint> timeline = new ArrayList<>();
		for (NoteInfo note : allNotes) {
			if (note.getMidiPitch() >= 0 && !note.isPercussion()) {
				timeline.add(new TimePoint(note.getTimestamp(), note.getMidiPitch(), true));
				timeline.add(new TimePoint(note.getEndTimestamp(), note.getMidiPitch(), false));
			}
		}
		
		// Sort by timestamp
		timeline.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));
		
		// Calculate vertical intervals at each time point
		Map<Integer, Integer> intervalCounts = new HashMap<>(); // interval -> count
		Map<Integer, Integer> activePitches = new HashMap<>(); // pitch -> count
		long lastTimestamp = -1;
		
		for (TimePoint point : timeline) {
			// If we've moved to a new timestamp, calculate intervals
			if (lastTimestamp >= 0 && point.timestamp != lastTimestamp && activePitches.size() > 1) {
				// Get all active pitches
				List<Integer> pitches = new ArrayList<>();
				for (Map.Entry<Integer, Integer> entry : activePitches.entrySet()) {
					if (entry.getValue() > 0) {
						pitches.add(entry.getKey());
					}
				}
				
				// Calculate all pairwise intervals (wrapped to 0-11)
				for (int i = 0; i < pitches.size(); i++) {
					for (int j = i + 1; j < pitches.size(); j++) {
						int interval = Math.abs(pitches.get(j) - pitches.get(i));
						int wrappedInterval = interval % 12; // Wrap to octave
						intervalCounts.put(wrappedInterval, intervalCounts.getOrDefault(wrappedInterval, 0) + 1);
					}
				}
			}
			
			// Update active pitches
			if (point.isStart) {
				activePitches.put(point.pitch, activePitches.getOrDefault(point.pitch, 0) + 1);
			} else {
				activePitches.put(point.pitch, activePitches.getOrDefault(point.pitch, 1) - 1);
			}
			
			lastTimestamp = point.timestamp;
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
	
	private static class TimePoint {
		final long timestamp;
		final int pitch;
		final boolean isStart;
		
		TimePoint(long timestamp, int pitch, boolean isStart) {
			this.timestamp = timestamp;
			this.pitch = pitch;
			this.isStart = isStart;
		}
	}
}

