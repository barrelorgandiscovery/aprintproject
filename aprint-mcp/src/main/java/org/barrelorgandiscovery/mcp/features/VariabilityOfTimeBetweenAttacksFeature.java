package org.barrelorgandiscovery.mcp.features;

import java.util.List;

/**
 * Feature: Variability of Time Between Attacks
 * 
 * Calculates the standard deviation of time between note attacks.
 * 
 * @author APrint Development Team
 */
public class VariabilityOfTimeBetweenAttacksFeature extends VirtualBookFeatureExtractor {
	
	public VariabilityOfTimeBetweenAttacksFeature() {
		this.code = "RT-21";
		this.name = "Variability of Time Between Attacks";
		this.description = "Standard deviation of time between note attacks (in seconds).";
	}
	
	@Override
	public double[] extractFeature(VirtualBookFeatureContext context) throws Exception {
		List<Double> timeBetweenAttacks = context.getTimeBetweenAttacks();
		
		if (timeBetweenAttacks.isEmpty()) {
			return new double[] { 0.0 };
		}
		
		double stdDev = FeatureMathUtils.standardDeviation(timeBetweenAttacks);
		return new double[] { stdDev };
	}
}

