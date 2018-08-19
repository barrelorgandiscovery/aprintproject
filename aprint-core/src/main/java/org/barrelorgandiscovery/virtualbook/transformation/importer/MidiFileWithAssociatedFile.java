package org.barrelorgandiscovery.virtualbook.transformation.importer;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Facade to MidiFile to permit a property Editor to work on it. This Facade
 * conserve a link to a midi File ...
 * 
 * @author Freydiere Patrice
 * 
 */
public class MidiFileWithAssociatedFile extends MidiFile {

	private File filePath;

	private MidiFile innerMidiFile;

	public MidiFileWithAssociatedFile(MidiFile inner, File associatedFile) {
		this.innerMidiFile = inner;
		this.filePath = associatedFile;
	}

	public File getAssociatedFile() {
		return filePath;
	}

	@Override
	public int[] listTracks() {
		return innerMidiFile.listTracks();
	}

	@Override
	public String toString() {
		if (filePath == null)
			return "";
		return filePath.toString();
	}

	@Override
	public void add(int index, MidiAdvancedEvent element) {
		innerMidiFile.add(index, element);
	}

	@Override
	public boolean add(MidiAdvancedEvent o) {
		return innerMidiFile.add(o);
	}

	@Override
	public boolean addAll(Collection<? extends MidiAdvancedEvent> c) {
		return innerMidiFile.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends MidiAdvancedEvent> c) {
		return innerMidiFile.addAll(index, c);
	}

	@Override
	public void clear() {
		innerMidiFile.clear();
	}

	@Override
	public Object clone() {
		return innerMidiFile.clone();
	}

	@Override
	public boolean contains(Object elem) {
		return innerMidiFile.contains(elem);
	}

	@Override
	public void ensureCapacity(int minCapacity) {
		innerMidiFile.ensureCapacity(minCapacity);
	}

	@Override
	public MidiAdvancedEvent get(int index) {
		return innerMidiFile.get(index);
	}

	@Override
	public int indexOf(Object elem) {
		return innerMidiFile.indexOf(elem);
	}

	@Override
	public boolean isEmpty() {
		return innerMidiFile.isEmpty();
	}

	@Override
	public int lastIndexOf(Object elem) {
		return innerMidiFile.lastIndexOf(elem);
	}

	@Override
	public MidiAdvancedEvent remove(int index) {
		return innerMidiFile.remove(index);
	}

	@Override
	public boolean remove(Object o) {
		return innerMidiFile.remove(o);
	}

	@Override
	public MidiAdvancedEvent set(int index, MidiAdvancedEvent element) {
		return innerMidiFile.set(index, element);
	}

	@Override
	public int size() {
		return innerMidiFile.size();
	}

	@Override
	public Object[] toArray() {
		return innerMidiFile.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return innerMidiFile.toArray(a);
	}

	@Override
	public void trimToSize() {
		innerMidiFile.trimToSize();
	}

	@Override
	public boolean equals(Object o) {
		return innerMidiFile.equals(o);
	}

	@Override
	public int hashCode() {
		return innerMidiFile.hashCode();
	}

	@Override
	public Iterator<MidiAdvancedEvent> iterator() {
		return innerMidiFile.iterator();
	}

	@Override
	public ListIterator<MidiAdvancedEvent> listIterator() {
		return innerMidiFile.listIterator();
	}

	@Override
	public ListIterator<MidiAdvancedEvent> listIterator(int index) {
		return innerMidiFile.listIterator(index);
	}

	@Override
	public List<MidiAdvancedEvent> subList(int fromIndex, int toIndex) {
		return innerMidiFile.subList(fromIndex, toIndex);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return innerMidiFile.containsAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return innerMidiFile.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return innerMidiFile.retainAll(c);
	}

}
