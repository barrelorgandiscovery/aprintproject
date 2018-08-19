package org.barrelorgandiscovery.virtualbook;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparators for events
 * 
 * @author use
 * 
 */
public class AbstractEventComparator implements Comparator<AbstractEvent>, Serializable {

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(AbstractEvent o1, AbstractEvent o2) {

		if (o1 == null) {
			if (o2 == null)
				return 0; // equals

			return -1;
		}

		// o1 != null
		if (o2 == null)
			return 1; // o2 less than o1

		// o1 != null && o2 != null
		long ts1 = o1.getTimestamp();
		long ts2 = o2.getTimestamp();

		int cmp = ((Long) ts1).compareTo(ts2);

		if (cmp != 0)
			return cmp;

		// same timestamp

		if (o1.equals(o2))
			return 0;

		
		if (o1 instanceof TempoChangeEvent && o2 instanceof SignatureEvent) {
			return 1;
		}

		if (o1 instanceof SignatureEvent && o2 instanceof TempoChangeEvent) {
			return -1;
		}

		return ((Integer) (o1.hashCode())).compareTo(o2.hashCode());

	}

}
