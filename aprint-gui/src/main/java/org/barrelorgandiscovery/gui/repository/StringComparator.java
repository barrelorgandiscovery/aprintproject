package org.barrelorgandiscovery.gui.repository;

import java.util.Comparator;

public class StringComparator implements Comparator<String> {

	public int compare(String o1, String o2) {

		if (o1 == null) {
			if (o2 == null)
				return 0;

			return o1.compareTo(o2);
		}

		return o1.compareTo(o2);
	}

}
