package org.barrelorgandiscovery.virtualbook.transformation;

import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * transposition of a virtual book object
 * 
 * 
 * @author Freydiere Patrice
 * 
 */
public class Transpositor {

	/**
	 * Transform a virtual book from one scale to another
	 * 
	 * @param carton
	 *            le carton à transposer
	 * @param transposition
	 *            la transposition à opérer
	 * @return le résultat de la transposition
	 */
	public static TranspositionResult transpose(VirtualBook carton,
			AbstractTransposeVirtualBook transposition) {

		// just an indirection ...
		return transposition.transpose(carton);
	}

}
