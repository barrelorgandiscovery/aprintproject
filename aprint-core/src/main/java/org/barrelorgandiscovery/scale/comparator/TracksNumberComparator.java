package org.barrelorgandiscovery.scale.comparator;

import org.barrelorgandiscovery.scale.Scale;

/**
 * only compare the number of tracks in the scale (and not all the tracks definitions, measures)
 * @author pfreydiere
 *	
 */
public class TracksNumberComparator extends ScaleComparator {

	@Override
	public boolean compare(Scale s1, Scale s2) {
		assert s1 != null;
		assert s2 != null;
		
		return s1.getTrackNb() == s2.getTrackNb();
	}

}
