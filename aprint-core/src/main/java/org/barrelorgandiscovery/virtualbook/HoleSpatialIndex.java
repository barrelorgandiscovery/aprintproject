package org.barrelorgandiscovery.virtualbook;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;

/**
 * Spatial index for speed up spatial search on the virtual book
 * 
 * @author Freydiere Patrice
 * 
 */
public class HoleSpatialIndex implements Serializable {

	/**
	 * index size for indexing holes ...
	 */
	private long indexsize = 3000000;

	/**
	 * Internal structure of the index key are "page", and Holes in the
	 * ArrayList that intersect the page
	 */
	private HashMap<Long, ArrayList<Hole>> index = new HashMap<Long, ArrayList<Hole>>();

	/**
	 * Number of tile for spatial index ...
	 */
	private long tileNumber = 0;
	
	private long maxTileNo = 0;

	/**
	 * Default constructor
	 */
	public HoleSpatialIndex() {

	}

	/**
	 * Add a hole to the index
	 * 
	 * @param h
	 */
	public void add(Hole h) {
		long start = h.getTimestamp();
		long end = start + h.getTimeLength();
		long endTileNumber = getTileNumber(end);
		
		maxTileNo = Math.max(maxTileNo, endTileNumber);
		
		for (long i = getTileNumber(start); i <= endTileNumber; i++) {
			internalGetTile(i).add(h);
		}
	}

	/**
	 * Remove a hole from the spatial index
	 * 
	 * @param h
	 */
	public void remove(Hole h) {
		long start = h.getTimestamp();
		long end = start + h.getTimeLength();
		for (long i = getTileNumber(start); i <= getTileNumber(end); i++) {
			internalGetTile(i).remove(h);
		}
	}

	public long getMaxTile() {

		long max = Long.MIN_VALUE;

		Set<Long> keys = index.keySet();
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			Long l = (Long) iterator.next();
			if (l > max)
				max = l;
		}

		return max;

	}

	public long getMinTile() {

		long min = Long.MAX_VALUE;

		Set<Long> keys = index.keySet();
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			Long l = (Long) iterator.next();
			if (l < min)
				min = l;
		}

		return min;
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
	public List<Hole> getTile(long tile) {
		return Collections.unmodifiableList(internalGetTile(tile));
	}

	/**
	 * @param tile
	 * @return
	 */
	protected ArrayList<Hole> internalGetTile(long tile) {
		ArrayList<Hole> s;
		if (index.containsKey(tile)) {
			s = index.get(tile);
		} else {
			// creating the tile ...
			s = new ArrayList<Hole>();
			index.put(tile, s);

			if (tile >= tileNumber) {
				tileNumber = tile + 1;
			}
		}
		return s;
	}

	/**
	 * find holes giving a HashSet to be filled
	 * 
	 * @param start
	 * @param end
	 * @param r
	 */
	public void find(long start, long end, Collection<Hole> r, HoleFilter f) {

		long endTileNumber = getTileNumber(end);
		if (endTileNumber > maxTileNo)
			endTileNumber = maxTileNo;
		
		// in spatial index, hole may be duplicated, so 
		// we avoid this in using a set for removing the duplications
		Set<Hole> result = new HashSet<>();
		
		long startTileNumber = getTileNumber(start);
		for (long i = startTileNumber; i <= endTileNumber; i++) {
			ArrayList<Hole> s = internalGetTile(i);
			for (Hole h : s) {

				long ts = h.getTimestamp();

				if (!(ts + h.getTimeLength() < start || ts > end)) {
					if (f == null) {
						result.add(h);
					} else {
						if (f.take(h))
							result.add(h);
					}
				}
			}
		}
		
		r.addAll(result);
		
	}

	/**
	 * find holes giving a HashSet to be filled
	 * 
	 * @param start
	 * @param end
	 * @param r
	 */
	public void find(long start, long end, Collection<Hole> r) {
		find(start, end, r, null);
	}

	/**
	 * find holes
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public ArrayList<Hole> find(long start, long end) {

		HashSet<Hole> r = new HashSet<Hole>();
		find(start, end, r);

		return new ArrayList<Hole>(r);
	}

	/**
	 * compute the length of the virtual book
	 * 
	 * @return
	 */
	public long getLength() {

		long length = 0;
		long cpt = tileNumber - 1;
		while (cpt >= 0) {
			// take the tile and dump the holes ...
			ArrayList<Hole> tile = internalGetTile(cpt);
			for (Iterator iterator = tile.iterator(); iterator.hasNext();) {
				Hole hole = (Hole) iterator.next();
				long l = hole.getTimestamp() + hole.getTimeLength();
				if (l > length)
					length = l;
			}

			if (length > 0)
				break;

			cpt--;
		}

		return length;
	}
}
