package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Average Number of Simultaneous Pitch Classes
 * 
 * Calculates the average number of different pitch classes sounding simultaneously.
 * Rests are excluded.
 * 
 * @author APrint Development Team
 */
public class AverageNumberOfSimultaneousPitchClassesFeature extends VirtualBookFeatureExtractor {
	
	public AverageNumberOfSimultaneousPitchClassesFeature() {
		this.code = "C-4";
		this.name = "Average Number of Simultaneous Pitch Classes";
		this.description = "Average number of different pitch classes sounding simultaneously. Rests are excluded.";
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
		
		// Calculate simultaneous pitch classes at each time point
		List<Integer> pitchClassCounts = new ArrayList<>();
		Map<Integer, Integer> activePitches = new HashMap<>(); // pitch -> count
		long lastTimestamp = -1;
		
		for (TimePoint point : timeline) {
			// If we've moved to a new timestamp, record the count
			if (lastTimestamp >= 0 && point.timestamp != lastTimestamp) {
				// Count unique pitch classes
				Set<Integer> pitchClasses = new HashSet<>();
				for (Map.Entry<Integer, Integer> entry : activePitches.entrySet()) {
					if (entry.getValue() > 0) {
						int pitchClass = entry.getKey() % 12;
						pitchClasses.add(pitchClass);
					}
				}
				pitchClassCounts.add(pitchClasses.size());
			}
			
			// Update active pitches
			if (point.isStart) {
				activePitches.put(point.pitch, activePitches.getOrDefault(point.pitch, 0) + 1);
			} else {
				activePitches.put(point.pitch, activePitches.getOrDefault(point.pitch, 1) - 1);
			}
			
			lastTimestamp = point.timestamp;
		}
		
		if (pitchClassCounts.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		// Calculate average
		double sum = 0.0;
		for (Integer count : pitchClassCounts) {
			sum += count;
		}
		double average = sum / pitchClassCounts.size();
		
		return new double[] { average };
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

