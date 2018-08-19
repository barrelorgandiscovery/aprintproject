package org.barrelorgandiscovery.model;

import java.io.Serializable;
import java.util.Comparator;

final class ModelStepScheduleComparator implements Comparator<ModelStep>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 507483993505623327L;

	public int compare(ModelStep o1, ModelStep o2) {

		if (o1.schedule < o2.schedule)
			return -1;

		if (o1.schedule > o2.schedule)
			return 1;

		return ((Integer) o1.hashCode()).compareTo(o2.hashCode());
	}
}