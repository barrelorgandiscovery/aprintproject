package org.barrelorgandiscovery.issues;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Spatial index for issues for speeding search
 * 
 * @author Freydiere Patrice
 * 
 */
public class IssueSpatialIndex {

	/**
	 * index size
	 */
	private long indexsize = 10000000;

	/**
	 * Internal structure of the index
	 */
	private HashMap<Long, Set<AbstractSpatialIssue>> index = new HashMap<Long, Set<AbstractSpatialIssue>>();

	/**
	 * Default constructor
	 */
	public IssueSpatialIndex() {

	}

	/**
	 * Add a hole to the index
	 * 
	 * @param h
	 */
	public void add(AbstractSpatialIssue h) {
		long start = h.getExtent().start;
		long end = h.getExtent().end;
		for (long i = getTileNumber(start); i <= getTileNumber(end); i++) {
			getTile(i).add(h);
		}
	}

	/**
	 * Remove a hole from the spatial index
	 * 
	 * @param h
	 */
	public void remove(AbstractSpatialIssue h) {
		long start = h.getExtent().start;
		long end = h.getExtent().end;
		for (long i = getTileNumber(start); i <= getTileNumber(end); i++) {
			getTile(i).remove(h);
		}
	}

	/**
	 * compute the tile number
	 */
	private long getTileNumber(long timestamp) {
		return timestamp / indexsize;
	}

	/**
	 * get the tile from its number
	 * 
	 * @param tile
	 * @return
	 */
	private Set<AbstractSpatialIssue> getTile(long tile) {

		Set<AbstractSpatialIssue> s;
		if (index.containsKey(tile)) {
			s = index.get(tile);
		} else {
			s = new TreeSet<AbstractSpatialIssue>();
			index.put(tile, s);
		}
		return s;
	}

	/**
	 * find holes
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public Set<AbstractSpatialIssue> find(long start, long end) {

		TreeSet<AbstractSpatialIssue> r = new TreeSet<AbstractSpatialIssue>();

		for (long i = getTileNumber(start); i <= getTileNumber(end); i++) {
			Set<AbstractSpatialIssue> s = getTile(i);
			for (AbstractSpatialIssue h : s) {
				if (!(h.getExtent().end < start || h.getExtent().start > end))
					r.add(h);
			}
		}
		return r;
	}
}
