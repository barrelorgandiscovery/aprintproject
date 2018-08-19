package org.barrelorgandiscovery.virtualbook;

/**
 * Interface for filtering holes in the virtual book
 * 
 * @author use
 * 
 */
public interface HoleFilter {
	
	/**
	 * Method to implement to say this hole can be taken
	 * 
	 * @param h
	 * @return
	 */
	boolean take(Hole h);
}