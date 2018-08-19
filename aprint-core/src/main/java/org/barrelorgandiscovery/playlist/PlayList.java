package org.barrelorgandiscovery.playlist;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

/**
 * Play list object, manage a non synchronized list of VirtualBook Ref with a
 * current element
 * 
 * non synchronized because, we want to take advantage of the collection fmk,
 * and we want to take advantage of observable lists for GUI model
 * 
 * @author pfreydiere
 * 
 */
public class PlayList extends ArrayList<VirtualBookRef> {

	
	/**
	 * serial to store the object or clone it by storage
	 */
	private static final long serialVersionUID = -6050689491060029305L;

	/**
	 * in case of playing, manage the currently played element
	 */
	private transient int currentElement = -1;

	/**
	 * Get the current virtualbook ref
	 * 
	 * @return
	 */
	public IVirtualBookRef currentVirtualBook() {
		if (currentElement >= 0 && currentElement < size()) {
			return get(currentElement);
		}
		return null;
	}

	public int currentindex() {
		// get the current index
		return currentElement;
	}

	/**
	 * return the new element, or null if none
	 */
	public void moveNext() {
		currentElement++;
	}

	/**
	 * Reset the current virtualbook ref
	 */
	public void moveFirst() {
		currentElement = 0;
	}
	
	/**
	 * set selected element
	 * @param index
	 */
	public void setCurrent(int index)
	{
		currentElement = index;
	}

}
