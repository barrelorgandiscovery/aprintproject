package org.barrelorgandiscovery.scale.comparator;

import org.barrelorgandiscovery.scale.Scale;

public class TracksNumberComparator extends ScaleComparator {

	@Override
	public boolean compare(Scale s1, Scale s2) {
		assert s1 != null;
		assert s2 != null;
		
		return s1.getTrackNb() == s2.getTrackNb();
	}

}
