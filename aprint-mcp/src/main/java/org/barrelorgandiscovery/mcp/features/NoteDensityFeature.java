package org.barrelorgandiscovery.mcp.features;

/**
 * Feature: Note Density
 * 
 * Calculates the average number of notes per second.
 * 
 * @author APrint Development Team
 */
public class NoteDensityFeature extends VirtualBookFeatureExtractor {
	
	public NoteDensityFeature() {
		this.code = "RT-10";
		this.name = "Note Density";
		this.description = "Average number of notes per second.";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		double totalLength = context.getTotalLengthSeconds();
		if (totalLength == 0) {
			return new double[] { 0.0 };
		}
		
		double density = context.getTotalHoleCount() / totalLength;
		return new double[] { density };
	}
}

