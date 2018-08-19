package org.barrelorgandiscovery.virtualbook.transformation.importer;

import java.util.ArrayList;

import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Bag for getting conversion + associated problems
 * 
 * @author use
 * 
 */
public class MidiConversionResult {

	/**
	 * The result
	 */
	public VirtualBook virtualbook;

	/**
	 * The associated problems linked to the conversion
	 */
	public ArrayList<MidiConversionProblem> issues;

	
	
}
