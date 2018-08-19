package org.barrelorgandiscovery.virtualbook;

import java.util.List;

public interface VirtualBookSectionManipulation {

	/**
	 * Return the list of markers
	 * 
	 * @return
	 */
	public List<MarkerEvent> listMarkers();

	/**
	 * Return the length of the marker (the length from the marker to the next
	 * one, or the end of the virtualbook)
	 * 
	 * @param marker
	 * @return
	 */
	public long getMarkerLength(MarkerEvent marker);

	/**
	 * Select the marker
	 * 
	 * @param marker
	 * @return
	 */
	public Fragment selectMarker(MarkerEvent marker);

	/**
	 * Make a selection
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public Fragment selectMarkers(MarkerEvent from, MarkerEvent to);

	/**
	 * insert in the book the selection at the given atPosition, this will
	 * extend the virtualbook for inserting the region
	 * 
	 * @param selection the selection
	 * @param atPosition the position
	 */
	public void insertAt(Fragment selection, long atPosition);

	/**
	 * Remove the selection from the book
	 * 
	 * @param selection
	 */
	public void removeFragment(Fragment selection);

}