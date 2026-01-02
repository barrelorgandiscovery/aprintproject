package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.List;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Polyphonic Fraction
 * 
 * Calculates the fraction of time during which multiple notes are sounding simultaneously.
 * 
 * @author APrint Development Team
 */
public class PolyphonicFractionFeature extends VirtualBookFeatureExtractor {
	
	public PolyphonicFractionFeature() {
		this.code = "T-1";
		this.name = "Polyphonic Fraction";
		this.description = "Fraction of time during which multiple notes are sounding simultaneously.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<NoteInfo> allNotes = context.getAllNotes();
		
		if (allNotes.isEmpty() || context.getTotalLengthSeconds() == 0) {
			return new double[] { 0.0 };
		}
		
		// Create a timeline of active notes
		List<TimePoint> timeline = new ArrayList<>();
		for (NoteInfo note : allNotes) {
			timeline.add(new TimePoint(note.getTimestamp(), true));
			timeline.add(new TimePoint(note.getEndTimestamp(), false));
		}
		
		// Sort by timestamp
		timeline.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));
		
		// Calculate polyphonic time (time when 2+ notes are active simultaneously)
		// All timestamps are in microseconds
		long polyphonicTime = 0;
		int activeNotes = 0;
		long lastTimestamp = 0;
		
		for (TimePoint point : timeline) {
			// If we had multiple notes active in the previous interval, add that time
			if (activeNotes > 1 && lastTimestamp > 0) {
				polyphonicTime += point.timestamp - lastTimestamp;
			}
			
			// Update active note count
			if (point.isStart) {
				activeNotes++;
			} else {
				activeNotes--;
			}
			
			lastTimestamp = point.timestamp;
		}
		
		// Both polyphonicTime and totalLength are in microseconds, so division gives fraction
		long totalLength = context.getTotalLength();
		double polyphonicFraction = totalLength > 0 ? (double) polyphonicTime / totalLength : 0.0;
		
		return new double[] { polyphonicFraction };
	}
	
	private static class TimePoint {
		final long timestamp;
		final boolean isStart;
		
		TimePoint(long timestamp, boolean isStart) {
			this.timestamp = timestamp;
			this.isStart = isStart;
		}
	}
}

