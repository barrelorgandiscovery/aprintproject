package org.barrelorgandiscovery.gui.aedit;

public interface ToolTipVirtualBookComponentLayer {

	/**
	 * Get an info associated to a layer
	 * 
	 * @param x the x position in mm
	 * @param y the y position in mm
	 * @return null if none
	 */
	String getToolTipInfo(double x, double y);

}
