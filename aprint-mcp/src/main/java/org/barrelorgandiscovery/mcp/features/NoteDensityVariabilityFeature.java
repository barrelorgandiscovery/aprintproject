package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.List;

import org.barrelorgandiscovery.mcp.features.VirtualBookFeatureContext.NoteInfo;

/**
 * Feature: Note Density Variability
 * 
 * Calculates the standard deviation of note density across time windows.
 * Measures how much the note density varies throughout the piece.
 * 
 * @author APrint Development Team
 */
public class NoteDensityVariabilityFeature extends VirtualBookFeatureExtractor {
	
	public NoteDensityVariabilityFeature() {
		this.code = "RT-9";
		this.name = "Note Density Variability";
		this.description = "Standard deviation of note density across time windows. Measures how much the note density varies throughout the piece.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		// Calculate note density in time windows
		long totalLength = context.getTotalLength();
		if (totalLength == 0) {
			return new double[] { 0.0 };
		}
		
		// Use 1-second windows
		long windowSize = 1_000_000; // 1 second in microseconds
		int numWindows = (int) (totalLength / windowSize) + 1;
		
		List<Double> densities = new ArrayList<>();
		List<NoteInfo> allNotes = context.getAllNotes();
		
		for (int i = 0; i < numWindows; i++) {
			long windowStart = i * windowSize;
			long windowEnd = windowStart + windowSize;
			
			int notesInWindow = 0;
			for (NoteInfo note : allNotes) {
				if (note.getTimestamp() >= windowStart && note.getTimestamp() < windowEnd) {
					notesInWindow++;
				}
			}
			
			double density = notesInWindow / 1.0; // notes per second
			densities.add(density);
		}
		
		if (densities.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		double stdDev = FeatureMathUtils.standardDeviation(densities);
		return new double[] { stdDev };
	}
}

