/**
 * 
 */
package org.barrelorgandiscovery.virtualbook.transformation;

import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Interface describing a virtual book transposition
 * 
 * @author Freydiere Patrice
 */
public abstract class AbstractTransposeVirtualBook extends
		AbstractTransformation {

	/**
	 * Get the source scale of the transposition
	 * 
	 * @return
	 */
	public abstract Scale getScaleSource();

	/**
	 * transpose the virtualbook
	 * 
	 * @param cartontotranspose
	 *            le carton à transposer
	 * @return le carton transposé
	 */
	public abstract TranspositionResult transpose(VirtualBook cartontotranspose);

}
