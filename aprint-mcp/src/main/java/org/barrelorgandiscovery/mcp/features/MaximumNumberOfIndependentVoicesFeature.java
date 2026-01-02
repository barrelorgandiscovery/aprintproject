package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Maximum Number of Independent Voices
 * 
 * Finds the maximum number of different tracks in which notes are sounded simultaneously.
 * 
 * @author APrint Development Team
 */
public class MaximumNumberOfIndependentVoicesFeature extends VirtualBookFeatureExtractor {
	
	public MaximumNumberOfIndependentVoicesFeature() {
		this.code = "T-2";
		this.name = "Maximum Number of Independent Voices";
		this.description = "Maximum number of different tracks in which notes are sounded simultaneously.";
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
		int maxVoices = 0;
		Map<Integer, Integer> activeTracks = new HashMap<>(); // track -> count
		long lastTimestamp = -1;
		
		for (TimePoint point : timeline) {
			// If we've moved to a new timestamp, check the count
			if (lastTimestamp >= 0 && point.timestamp != lastTimestamp) {
				// Count unique tracks
				Set<Integer> tracks = new HashSet<>();
				for (Map.Entry<Integer, Integer> entry : activeTracks.entrySet()) {
					if (entry.getValue() > 0) {
						tracks.add(entry.getKey());
					}
				}
				maxVoices = Math.max(maxVoices, tracks.size());
			}
			
			// Update active tracks
			if (point.isStart) {
				activeTracks.put(point.track, activeTracks.getOrDefault(point.track, 0) + 1);
			} else {
				activeTracks.put(point.track, activeTracks.getOrDefault(point.track, 1) - 1);
			}
			
			lastTimestamp = point.timestamp;
		}
		
		// Check final state
		Set<Integer> tracks = new HashSet<>();
		for (Map.Entry<Integer, Integer> entry : activeTracks.entrySet()) {
			if (entry.getValue() > 0) {
				tracks.add(entry.getKey());
			}
		}
		maxVoices = Math.max(maxVoices, tracks.size());
		
		return new double[] { maxVoices };
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

