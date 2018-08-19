package org.barrelorgandiscovery.model.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.barrelorgandiscovery.model.ModelStep;

public final class ModelStepComparator implements Comparator<ModelStep>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5920559345048776196L;

	public int compare(ModelStep o1, ModelStep o2) {
		return ((Integer) o1.hashCode()).compareTo(o2.hashCode());
	}
}