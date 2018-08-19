package org.barrelorgandiscovery.tools;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class ReadOnlySet<E> implements Set<E> {

	private Set<E> inner;

	public ReadOnlySet(Set<E> inner) {
		this.inner = inner;
	}

	public int size() {
		return inner.size();
	}

	public boolean isEmpty() {
		return inner.isEmpty();
	}

	public boolean contains(Object o) {
		return inner.contains(o);
	}

	public Iterator<E> iterator() {
		return inner.iterator();
	}

	public Object[] toArray() {
		return inner.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return inner.toArray(a);
	}

	public boolean add(E o) {
		throw new UnsupportedOperationException();

	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean containsAll(Collection<?> c) {
		return inner.containsAll(c);
	}

	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();

	}

	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();

	}

}
