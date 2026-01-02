package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Direction of Melodic Motion
 * 
 * Calculates the fraction of melodic intervals that are rising in pitch.
 * Set to zero if no rising or falling melodic intervals are found.
 * 
 * @author APrint Development Team
 */
public class DirectionOfMelodicMotionFeature extends VirtualBookFeatureExtractor {
	
	public DirectionOfMelodicMotionFeature() {
		this.code = "M-22";
		this.name = "Direction of Melodic Motion";
		this.description = "Fraction of melodic intervals that are rising in pitch. Set to zero if no rising or falling melodic intervals are found.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<NoteInfo> allNotes = context.getAllNotes();
		int risingCount = 0;
		int fallingCount = 0;
		
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
					int interval = curr.getMidiPitch() - prev.getMidiPitch();
					if (interval > 0) {
						risingCount++;
					} else if (interval < 0) {
						fallingCount++;
					}
				}
			}
		}
		
		if (risingCount + fallingCount == 0) {
			return new double[] { 0.0 };
		}
		
		double fraction = (double) risingCount / (risingCount + fallingCount);
		return new double[] { fraction };
	}
}

