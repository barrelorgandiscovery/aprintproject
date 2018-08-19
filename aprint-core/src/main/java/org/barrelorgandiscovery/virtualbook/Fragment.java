package org.barrelorgandiscovery.virtualbook;

import java.io.Serializable;

/**
 * Structure representing a selection in the book (with a start and a end)
 * 
 * @author Freydiere Patrice
 */
public class Fragment implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4747525943210512058L;
	
	public Fragment()
	{
	}

	public Fragment(long start, long length)
	{
		this.start = start; 
		this.length = length;
	}
	
	/**
	 * Start of the selection (in microseconds)
	 */
	public long start;
	/**
	 * Length of the selection (in microseconds)
	 */
	public long length;
	
	@Override
	public String toString() {
		return "Selection(" + start + "," + length + ")";
	}
	
}
