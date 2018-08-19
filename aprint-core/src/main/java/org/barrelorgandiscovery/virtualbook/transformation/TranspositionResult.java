package org.barrelorgandiscovery.virtualbook.transformation;

import java.util.ArrayList;

import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;


/**
 * Define the result of a transposition
 * 
 * @author Freydiere Patrice
 * 
 */
public class TranspositionResult {

	/**
	 * result virtualbook
	 */
	public VirtualBook virtualbook;

	/**
	 * non transposed holes
	 */
	public ArrayList<Hole> untransposedholes = new ArrayList<Hole>();

}
