package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Average Number of Independent Voices
 * 
 * Calculates the average number of different tracks in which notes are sounded simultaneously.
 * Rests are not included in this calculation.
 * 
 * @author APrint Development Team
 */
public class AverageNumberOfIndependentVoicesFeature extends VirtualBookFeatureExtractor {
	
	public AverageNumberOfIndependentVoicesFeature() {
		this.code = "T-3";
		this.name = "Average Number of Independent Voices";
		this.description = "Average number of different tracks in which notes are sounded simultaneously. Rests are not included in this calculation.";
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
			timeline.add(new TimePoint(note.getTimestamp(), note.getTrack(), true));
			timeline.add(new TimePoint(note.getEndTimestamp(), note.getTrack(), false));
		}
		
		// Sort by timestamp
		timeline.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));
		
		// Calculate simultaneous tracks at each time point
		List<Integer> voiceCounts = new ArrayList<>();
		Map<Integer, Integer> activeTracks = new HashMap<>(); // track -> count
		long lastTimestamp = -1;
		
		for (TimePoint point : timeline) {
			// If we've moved to a new timestamp, record the count
			if (lastTimestamp >= 0 && point.timestamp != lastTimestamp) {
				// Count unique tracks (only if at least one note is sounding)
				Set<Integer> tracks = new HashSet<>();
				for (Map.Entry<Integer, Integer> entry : activeTracks.entrySet()) {
					if (entry.getValue() > 0) {
						tracks.add(entry.getKey());
					}
				}
				if (!tracks.isEmpty()) {
					voiceCounts.add(tracks.size());
				}
			}
			
			// Update active tracks
			if (point.isStart) {
				activeTracks.put(point.track, activeTracks.getOrDefault(point.track, 0) + 1);
			} else {
				activeTracks.put(point.track, activeTracks.getOrDefault(point.track, 1) - 1);
			}
			
			lastTimestamp = point.timestamp;
		}
		
		if (voiceCounts.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		// Calculate average
		double sum = 0.0;
		for (Integer count : voiceCounts) {
			sum += count;
		}
		double average = sum / voiceCounts.size();
		
		return new double[] { average };
	}
	
	private static class TimePoint {
		final long timestamp;
		final int track;
		final boolean isStart;
		
		TimePoint(long timestamp, int track, boolean isStart) {
			this.timestamp = timestamp;
			this.track = track;
			this.isStart = isStart;
		}
	}
}

