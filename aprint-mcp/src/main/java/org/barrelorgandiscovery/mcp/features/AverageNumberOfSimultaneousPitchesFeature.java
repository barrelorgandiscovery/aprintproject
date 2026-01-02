package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Average Number of Simultaneous Pitches
 * 
 * Calculates the average number of pitches sounding simultaneously.
 * Rests are excluded. Unisons are excluded, but octave multiples are included.
 * 
 * @author APrint Development Team
 */
public class AverageNumberOfSimultaneousPitchesFeature extends VirtualBookFeatureExtractor {
	
	public AverageNumberOfSimultaneousPitchesFeature() {
		this.code = "C-6";
		this.name = "Average Number of Simultaneous Pitches";
		this.description = "Average number of pitches sounding simultaneously. Rests are excluded. Unisons are excluded, but octave multiples are included.";
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
		
		// Calculate simultaneous pitches at each time point
		List<Integer> simultaneousCounts = new ArrayList<>();
		Map<Integer, Integer> activePitches = new HashMap<>(); // pitch -> count
		long lastTimestamp = -1;
		
		for (TimePoint point : timeline) {
			// If we've moved to a new timestamp, record the count
			if (lastTimestamp >= 0 && point.timestamp != lastTimestamp) {
				// Count unique pitches (excluding unisons)
				int uniquePitches = 0;
				for (Integer pitch : activePitches.keySet()) {
					if (activePitches.get(pitch) > 0) {
						uniquePitches++;
					}
				}
				simultaneousCounts.add(uniquePitches);
			}
			
			// Update active pitches
			if (point.isStart) {
				activePitches.put(point.pitch, activePitches.getOrDefault(point.pitch, 0) + 1);
			} else {
				activePitches.put(point.pitch, activePitches.getOrDefault(point.pitch, 1) - 1);
			}
			
			lastTimestamp = point.timestamp;
		}
		
		if (simultaneousCounts.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		// Calculate average
		double sum = 0.0;
		for (Integer count : simultaneousCounts) {
			sum += count;
		}
		double average = sum / simultaneousCounts.size();
		
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

