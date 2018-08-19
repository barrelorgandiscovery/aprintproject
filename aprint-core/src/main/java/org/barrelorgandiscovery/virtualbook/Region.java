package org.barrelorgandiscovery.virtualbook;

import org.barrelorgandiscovery.tools.HashCodeUtils;

/**
 * a Virual Book region (with a start/end and track interval)
 * 
 * @author Freydiere Patrice
 */
public class Region {
	/**
	 * Start of the region (microseconds)
	 */
	public long start;
	/**
	 * end of the region (microseconds)
	 */
	public long end;
	/**
	 * Beginning track
	 */
	public int beginningtrack;
	public int endtrack;

	/**
	 * Default constructor
	 */
	public Region() {

	}

	/**
	 * Constructor with all the parameters
	 * 
	 * @param start
	 * @param end
	 * @param notedebut
	 * @param notefin
	 */
	public Region(long start, long end, int notedebut, int notefin) {
		this.start = start;
		this.end = end;
		this.beginningtrack = notedebut;
		this.endtrack = notefin;
	}

	public long getWidth()
	{
		return end - start;
	}
	
	public int getHeight(){
		return endtrack - beginningtrack;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = HashCodeUtils.SEED;
		hash = HashCodeUtils.hash(hash, start);
		hash = HashCodeUtils.hash(hash, end);
		hash = HashCodeUtils.hash(hash, beginningtrack);
		hash = HashCodeUtils.hash(hash, endtrack);
		return hash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj.getClass() != getClass())
			return false;

		Region n = (Region) obj;
		return start == n.start && end == n.end
				&& beginningtrack == n.beginningtrack && endtrack == n.endtrack;
	}

}
