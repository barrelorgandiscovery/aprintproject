package org.barrelorgandiscovery.recognition.gui;

import org.barrelorgandiscovery.recognition.IntArrayHolder;

public interface DrawingEdgeListener {

	/**
	 * a polyline has been drawn
	 * 
	 * @param x
	 * @param y
	 */
	void polylineDraw(IntArrayHolder path);

}
