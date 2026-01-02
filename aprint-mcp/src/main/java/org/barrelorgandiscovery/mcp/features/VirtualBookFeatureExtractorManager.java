package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Manager for extracting features from VirtualBook.
 * 
 * This class coordinates the extraction of multiple features from a VirtualBook,
 * similar to how jSymbolic processes MIDI files.
 * 
 * @author APrint Development Team
 */
public class VirtualBookFeatureExtractorManager {
	
	private final List<VirtualBookFeatureExtractor> extractors;
	
	public VirtualBookFeatureExtractorManager() {
		this.extractors = new ArrayList<>();
		registerDefaultExtractors();
	}
	
	/**
	 * Register all default feature extractors.
	 */
	private void registerDefaultExtractors() {
		// Basic rhythm features
		extractors.add(new TotalNumberOfNotesFeature());
		extractors.add(new DurationInSecondsFeature());
		extractors.add(new AverageNoteDurationFeature());
		extractors.add(new MinimumNoteDurationFeature());
		extractors.add(new MaximumNoteDurationFeature());
		extractors.add(new VariabilityOfNoteDurationsFeature());
		extractors.add(new RhythmicVariabilityFeature());
		
		// Note density features
		extractors.add(new NoteDensityFeature());
		extractors.add(new NoteDensityVariabilityFeature());
		
		// Time between attacks features
		extractors.add(new AverageTimeBetweenAttacksFeature());
		extractors.add(new VariabilityOfTimeBetweenAttacksFeature());
		
		// Pitch features
		extractors.add(new PitchRangeFeature());
		extractors.add(new NumberOfPitchesFeature());
		extractors.add(new MeanPitchFeature());
		extractors.add(new PitchVariabilityFeature());
		extractors.add(new MostCommonPitchFeature());
		extractors.add(new FirstPitchFeature());
		extractors.add(new LastPitchFeature());
		
		// Pitch class features
		extractors.add(new FirstPitchClassFeature());
		extractors.add(new LastPitchClassFeature());
		extractors.add(new MeanPitchClassFeature());
		extractors.add(new MostCommonPitchClassFeature());
		extractors.add(new NumberOfPitchClassesFeature());
		extractors.add(new PitchClassVariabilityFeature());
		
		// Melodic features
		extractors.add(new MeanMelodicIntervalFeature());
		extractors.add(new MostCommonMelodicIntervalFeature());
		extractors.add(new StepwiseMotionFeature());
		extractors.add(new ChromaticMotionFeature());
		extractors.add(new RepeatedNotesFeature());
		extractors.add(new MelodicThirdsFeature());
		extractors.add(new MelodicPerfectFourthsFeature());
		extractors.add(new MelodicPerfectFifthsFeature());
		extractors.add(new MelodicOctavesFeature());
		extractors.add(new MelodicSixthsFeature());
		extractors.add(new MelodicSeventhsFeature());
		extractors.add(new MelodicTritonesFeature());
		extractors.add(new MelodicLargeIntervalsFeature());
		extractors.add(new DirectionOfMelodicMotionFeature());
		
		// Rhythm articulation features
		extractors.add(new AmountOfStaccatoFeature());
		
		// Texture/harmony features
		extractors.add(new PolyphonicFractionFeature());
		extractors.add(new AverageNumberOfSimultaneousPitchesFeature());
		extractors.add(new VariabilityOfNumberOfSimultaneousPitchesFeature());
		extractors.add(new AverageNumberOfSimultaneousPitchClassesFeature());
		extractors.add(new MaximumNumberOfIndependentVoicesFeature());
		extractors.add(new AverageNumberOfIndependentVoicesFeature());
		extractors.add(new MostCommonVerticalIntervalFeature());
		extractors.add(new VerticalPerfectFifthsFeature());
		extractors.add(new VerticalOctavesFeature());
	}
	
	/**
	 * Extract all features from a VirtualBook.
	 * 
	 * @param virtualBook The virtual book to analyze
	 * @return Map of feature names to their values
	 */
	public Map<String, Object> extractAllFeatures(VirtualBook virtualBook) {
		Map<String, Object> features = new HashMap<>();
		
		// Create feature context
		VirtualBookFeatureContext context = new VirtualBookFeatureContext(virtualBook);
		
		// Extract each feature
		for (VirtualBookFeatureExtractor extractor : extractors) {
			try {
				double[] values = extractor.extractFeature(context);
				
				if (values.length == 1) {
					features.put(extractor.getName(), values[0]);
				} else {
					// Multi-dimensional feature
					List<Double> valueList = new ArrayList<>();
					for (double v : values) {
						valueList.add(v);
					}
					features.put(extractor.getName(), valueList);
				}
			} catch (Exception e) {
				// Log error but continue with other features
				features.put(extractor.getName() + "_ERROR", e.getMessage());
			}
		}
		
		return features;
	}
	
	/**
	 * Get all registered extractors.
	 */
	public List<VirtualBookFeatureExtractor> getExtractors() {
		return new ArrayList<>(extractors);
	}
}

