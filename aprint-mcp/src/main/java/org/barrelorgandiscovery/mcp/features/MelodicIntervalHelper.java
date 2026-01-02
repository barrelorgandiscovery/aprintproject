package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Helper class for calculating melodic intervals from VirtualBook data.
 * 
 * @author APrint Development Team
 */
public class MelodicIntervalHelper {
	
	/**
	 * Calculate all melodic intervals (intervals between consecutive notes on the same track).
	 * Returns a list of interval sizes in semitones.
	 */
	public static List<Integer> calculateMelodicIntervals(VirtualBookFeatureContext context) {
		List<Integer> intervals = new ArrayList<>();
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
		
		return intervals;
	}
	
	/**
	 * Calculate the fraction of intervals that match specific sizes.
	 */
	public static double fractionOfIntervals(List<Integer> intervals, int... targetSizes) {
		if (intervals.isEmpty()) {
			return 0.0;
		}
		
		int count = 0;
		for (Integer interval : intervals) {
			for (int target : targetSizes) {
				if (interval == target) {
					count++;
					break;
				}
			}
		}
		
		return (double) count / intervals.size();
	}
}

