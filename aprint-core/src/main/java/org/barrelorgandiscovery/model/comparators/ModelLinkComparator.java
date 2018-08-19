package org.barrelorgandiscovery.model.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.barrelorgandiscovery.model.ModelLink;

public final class ModelLinkComparator implements Comparator<ModelLink>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5143654757966524809L;

	public int compare(ModelLink o1, ModelLink o2) {
		return ((Integer) o1.hashCode()).compareTo(o2.hashCode());
	}
}