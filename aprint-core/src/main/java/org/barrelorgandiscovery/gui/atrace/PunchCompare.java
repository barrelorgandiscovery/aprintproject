package org.barrelorgandiscovery.gui.atrace;

import java.util.Comparator;

public class PunchCompare implements Comparator<Punch> {

	@Override
	public int compare(Punch o1, Punch o2) {
	
		int i = Double.compare(o1.x, o2.x);
		if (i != 0) {
			return i;
		}
		
		return Double.compare(o1.y, o2.y);
		
	}

}
