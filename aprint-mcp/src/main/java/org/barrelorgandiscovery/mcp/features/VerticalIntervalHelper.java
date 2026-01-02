package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Helper class for calculating vertical intervals (intervals between simultaneous pitches).
 * 
 * @author APrint Development Team
 */
public class VerticalIntervalHelper {
	
	/**
	 * Calculate vertical interval histogram (weighted by duration).
	 * Returns a map of interval (wrapped to 0-11) -> total duration in microseconds.
	 */
	public static Map<Integer, Long> calculateVerticalIntervalHistogram(VirtualBookFeatureContext context) {
		Map<Integer, Long> histogram = new HashMap<>();
		List<NoteInfo> allNotes = context.getAllNotes();
		
		if (allNotes.isEmpty()) {
			return histogram;
		}
		
		// Create timeline of active notes
		List<TimePoint> timeline = new ArrayList<>();
		for (NoteInfo note : allNotes) {
			if (note.getMidiPitch() >= 0 && !note.isPercussion()) {
				timeline.add(new TimePoint(note.getTimestamp(), note.getMidiPitch(), note.getLength(), true));
				timeline.add(new TimePoint(note.getEndTimestamp(), note.getMidiPitch(), 0, false));
			}
		}
		
		// Sort by timestamp
		timeline.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));
		
		// Calculate vertical intervals at each time point
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
				long timeDelta = point.timestamp - lastTimestamp;
				for (int i = 0; i < pitches.size(); i++) {
					for (int j = i + 1; j < pitches.size(); j++) {
						int interval = Math.abs(pitches.get(j) - pitches.get(i));
						int wrappedInterval = interval % 12; // Wrap to octave
						histogram.put(wrappedInterval, histogram.getOrDefault(wrappedInterval, 0L) + timeDelta);
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
		
		return histogram;
	}
	
	private static class TimePoint {
		final long timestamp;
		final int pitch;
		final long length;
		final boolean isStart;
		
		TimePoint(long timestamp, int pitch, long length, boolean isStart) {
			this.timestamp = timestamp;
			this.pitch = pitch;
			this.length = length;
			this.isStart = isStart;
		}
	}
}

