package org.barrelorgandiscovery.model.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.barrelorgandiscovery.model.ModelParameter;

public final class ModelParameterComparator implements
		Comparator<ModelParameter>, Serializable {
	public int compare(ModelParameter o1, ModelParameter o2) {
		return ((Integer) o1.hashCode()).compareTo(o2.hashCode());
	}
}