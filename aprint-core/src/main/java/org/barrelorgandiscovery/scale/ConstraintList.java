package org.barrelorgandiscovery.scale;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class ConstraintList implements Serializable,
		Iterable<AbstractScaleConstraint> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2239712002888501590L;

	/**
	 * Mémorisation de la liste des contraintes
	 */

	private static class AbstractScaleConstraintComparator implements
			Comparator<AbstractScaleConstraint>, Serializable {

		
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 8566538767756069933L;

		public int compare(AbstractScaleConstraint o1,
				AbstractScaleConstraint o2) {

			assert o1 != null;
			assert o2 != null;

			int h1 = o1.hashCode();
			int h2 = o2.hashCode();

			if (h1 < h2)
				return 1;

			if (h1 == h2)
				return 0;
			return -1;

		}

	}

	private Set<AbstractScaleConstraint> list = new TreeSet<AbstractScaleConstraint>(
			new AbstractScaleConstraintComparator());

	public Iterator<AbstractScaleConstraint> iterator() {
		return list.iterator();
	}

	public void add(AbstractScaleConstraint cons) {
		list.add(cons);
	}

	public void remove(AbstractScaleConstraint cons) {
		list.remove(cons);
	}

	public int size() {
		return list.size();
	}

	@Override
	public int hashCode() {
		return list.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj.getClass() != getClass())
			return false;

		return list.equals(((ConstraintList) obj).list);
	}

}
