package org.barrelorgandiscovery.gui.aedit.snapping;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 * Snapping Environment
 * 
 * @author use
 * 
 */
public interface ISnappingEnvironment {

	/**
	 * Get name of the snapping environment
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Snap Position
	 * 
	 * @param position, in mm in the book space
	 * 
	 * @return true if the position has been changed, false otherwise
	 */
	public boolean snapPosition(Point2D.Double position);

	/**
	 * Draw the feed back for snapping
	 * 
	 * @param g
	 */
	public void drawFeedBack(Graphics2D g);

}
