package org.barrelorgandiscovery.mcp.features;

/**
 * Base class for extracting features directly from VirtualBook.
 * Similar to MIDIFeatureExtractor in jSymbolic, but works with VirtualBook.
 * 
 * @author APrint Development Team
 */
public abstract class VirtualBookFeatureExtractor {
	
	protected String code;
	protected String name;
	protected String description;
	
	/**
	 * Extract this feature from the given VirtualBook feature context.
	 * 
	 * @param context The pre-processed VirtualBook context
	 * @return The extracted feature value(s) as an array
	 * @throws Exception If the feature cannot be calculated
	 */
	public abstract double[] extractFeature(VirtualBookFeatureContext context) throws Exception;
	
	/**
	 * Get the feature code (unique identifier).
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * Get the feature name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get the feature description.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Get the number of dimensions (values) this feature returns.
	 * Default is 1.
	 */
	public int getDimensions() {
		return 1;
	}
}

