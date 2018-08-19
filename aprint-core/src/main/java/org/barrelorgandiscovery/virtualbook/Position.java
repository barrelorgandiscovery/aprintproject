package org.barrelorgandiscovery.virtualbook;

/**
 * Bag for a position in the Book
 * 
 * @author Freydiere Patrice
 */
public class Position {

	/**
	 * the track
	 */
	public int track;

	/**
	 * position in microseconds from the beginning of the book
	 */
	public long position;

	@Override
	public String toString() {
		return super.toString() + " piste : " + track + " position :"
				+ position;
	}

}
