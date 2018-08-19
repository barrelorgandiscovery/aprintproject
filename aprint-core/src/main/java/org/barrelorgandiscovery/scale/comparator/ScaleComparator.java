package org.barrelorgandiscovery.scale.comparator;

import org.barrelorgandiscovery.scale.Scale;

/**
 * compare two scales
 * 
 * @author pfreydiere
 *
 */
public abstract class ScaleComparator {

	/**
	 * compare two scale and return if they are equivalents
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public abstract boolean compare(Scale s1, Scale s2);

}
