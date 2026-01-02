package org.barrelorgandiscovery.mcp.features;

/**
 * Feature: Average Time Between Attacks
 * 
 * Calculates the average time between note attacks in seconds.
 * 
 * @author APrint Development Team
 */
public class AverageTimeBetweenAttacksFeature extends VirtualBookFeatureExtractor {
	
	public AverageTimeBetweenAttacksFeature() {
		this.code = "RT-20";
		this.name = "Average Time Between Attacks";
		this.description = "Average time between note attacks (in seconds).";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		double[] result = new double[1];
		result[0] = context.getAverageTimeBetweenAttacks();
		return result;
	}
}

