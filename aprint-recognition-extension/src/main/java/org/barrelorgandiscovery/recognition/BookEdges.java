package org.barrelorgandiscovery.recognition;

import java.io.Serializable;

public class BookEdges extends IntArrayHolder implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2315837331579940276L;

	
	public BookEdges() {

	}

	
	public int computeMeanLength() {
		long l = 0;
		int c = 0;
		for (int i = 0; i < getCount(); i++) {
			int[] e = getElement(i);
			int length = e[1];
			if (length > 0) {
				l += length;
				c++;
			}
		}

		return (int) (1.0 * l / c);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < getCount(); i++) {
			int[] e = getElement(i);
			sb.append("").append(e[0]).append(" length :").append(e[1])
					.append("\n");
		}
		return sb.toString();
	}
}
