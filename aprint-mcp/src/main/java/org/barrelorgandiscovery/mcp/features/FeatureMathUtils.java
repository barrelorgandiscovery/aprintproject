package org.barrelorgandiscovery.mcp.features;

import java.util.Collection;
import java.util.List;

/**
 * Utility class for mathematical and statistical operations used in feature extraction.
 * 
 * @author APrint Development Team
 */
public class FeatureMathUtils {
	
	/**
	 * Calculate the mean (average) of a collection of numbers.
	 */
	public static double mean(Collection<Double> values) {
		if (values == null || values.isEmpty()) {
			return 0.0;
		}
		double sum = 0.0;
		for (Double value : values) {
			sum += value;
		}
		return sum / values.size();
	}
	
	/**
	 * Calculate the mean of an array of doubles.
	 */
	public static double mean(double[] values) {
		if (values == null || values.length == 0) {
			return 0.0;
		}
		double sum = 0.0;
		for (double value : values) {
			sum += value;
		}
		return sum / values.length;
	}
	
	/**
	 * Calculate the standard deviation of a collection of numbers.
	 */
	public static double standardDeviation(Collection<Double> values) {
		if (values == null || values.isEmpty()) {
			return 0.0;
		}
		double mean = mean(values);
		double sumSquaredDiffs = 0.0;
		for (Double value : values) {
			double diff = value - mean;
			sumSquaredDiffs += diff * diff;
		}
		double variance = sumSquaredDiffs / values.size();
		return Math.sqrt(variance);
	}
	
	/**
	 * Calculate the standard deviation of an array of doubles.
	 */
	public static double standardDeviation(double[] values) {
		if (values == null || values.length == 0) {
			return 0.0;
		}
		double mean = mean(values);
		double sumSquaredDiffs = 0.0;
		for (double value : values) {
			double diff = value - mean;
			sumSquaredDiffs += diff * diff;
		}
		double variance = sumSquaredDiffs / values.length;
		return Math.sqrt(variance);
	}
	
	/**
	 * Calculate the variance of a collection of numbers.
	 */
	public static double variance(Collection<Double> values) {
		if (values == null || values.isEmpty()) {
			return 0.0;
		}
		double mean = mean(values);
		double sumSquaredDiffs = 0.0;
		for (Double value : values) {
			double diff = value - mean;
			sumSquaredDiffs += diff * diff;
		}
		return sumSquaredDiffs / values.size();
	}
	
	/**
	 * Calculate the minimum value in a collection.
	 */
	public static double min(Collection<Double> values) {
		if (values == null || values.isEmpty()) {
			return 0.0;
		}
		double min = Double.MAX_VALUE;
		for (Double value : values) {
			if (value < min) {
				min = value;
			}
		}
		return min == Double.MAX_VALUE ? 0.0 : min;
	}
	
	/**
	 * Calculate the maximum value in a collection.
	 */
	public static double max(Collection<Double> values) {
		if (values == null || values.isEmpty()) {
			return 0.0;
		}
		double max = Double.MIN_VALUE;
		for (Double value : values) {
			if (value > max) {
				max = value;
			}
		}
		return max == Double.MIN_VALUE ? 0.0 : max;
	}
	
	/**
	 * Calculate the median of a sorted list.
	 */
	public static double median(List<Double> sortedValues) {
		if (sortedValues == null || sortedValues.isEmpty()) {
			return 0.0;
		}
		int size = sortedValues.size();
		if (size % 2 == 0) {
			return (sortedValues.get(size / 2 - 1) + sortedValues.get(size / 2)) / 2.0;
		} else {
			return sortedValues.get(size / 2);
		}
	}
	
	/**
	 * Calculate skewness (third moment about the mean).
	 */
	public static double skewness(Collection<Double> values) {
		if (values == null || values.size() < 3) {
			return 0.0;
		}
		double mean = mean(values);
		double stdDev = standardDeviation(values);
		if (stdDev == 0.0) {
			return 0.0;
		}
		
		double sumCubedDiffs = 0.0;
		for (Double value : values) {
			double diff = (value - mean) / stdDev;
			sumCubedDiffs += diff * diff * diff;
		}
		return sumCubedDiffs / values.size();
	}
	
	/**
	 * Calculate kurtosis (fourth moment about the mean).
	 */
	public static double kurtosis(Collection<Double> values) {
		if (values == null || values.size() < 4) {
			return 0.0;
		}
		double mean = mean(values);
		double stdDev = standardDeviation(values);
		if (stdDev == 0.0) {
			return 0.0;
		}
		
		double sumFourthDiffs = 0.0;
		for (Double value : values) {
			double diff = (value - mean) / stdDev;
			sumFourthDiffs += diff * diff * diff * diff;
		}
		return (sumFourthDiffs / values.size()) - 3.0; // Excess kurtosis
	}
	
	/**
	 * Get the index of the smallest value in an array.
	 */
	public static int indexOfSmallest(double[] values) {
		if (values == null || values.length == 0) {
			return -1;
		}
		int minIndex = 0;
		for (int i = 1; i < values.length; i++) {
			if (values[i] < values[minIndex]) {
				minIndex = i;
			}
		}
		return minIndex;
	}
	
	/**
	 * Get the index of the largest value in an array.
	 */
	public static int indexOfLargest(double[] values) {
		if (values == null || values.length == 0) {
			return -1;
		}
		int maxIndex = 0;
		for (int i = 1; i < values.length; i++) {
			if (values[i] > values[maxIndex]) {
				maxIndex = i;
			}
		}
		return maxIndex;
	}
}

