package org.barrelorgandiscovery.gui.ascale;

import org.barrelorgandiscovery.scale.AbstractTrackDef;

public interface ScaleHighlightListener {

	/**
	 * Method called when a track is highlighted
	 * 
	 * @param td
	 */
	void trackIsHighlighted(AbstractTrackDef td);

	/**
	 * Method called when a track is unhighlighted
	 */
	void hightlightReseted();

}
