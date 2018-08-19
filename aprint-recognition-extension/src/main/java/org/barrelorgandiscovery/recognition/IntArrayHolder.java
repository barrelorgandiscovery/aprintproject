package org.barrelorgandiscovery.recognition;

import java.io.Serializable;

public class IntArrayHolder implements Serializable{

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5168110485351322991L;
	private int[][] elements = new int[100][];
	private int count = 0;

	public IntArrayHolder() {
		super();
	}

	/**
	 * Add Edges at the end of the collection
	 * @param x
	 * @param x1
	 */
	public void addElement(int x, int x1) {
	
		if (count >= elements.length) {
			int[][] newedges = new int[elements.length * 2][]; // *2 each time
			System.arraycopy(elements, 0, newedges, 0, elements.length);
			elements = newedges;
		}
	
		elements[count] = new int[] { x, x1 };
		count++;
	}

	/**
	 * Get Edge by reference
	 * @param x
	 * @return
	 */
	public int[] getElement(int x) {
		if (x >= count)
			throw new ArrayIndexOutOfBoundsException();
	
		return elements[x];
	}

	public int getCount() {
		return count;
	}

}