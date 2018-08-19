package org.barrelorgandiscovery.gui.aedit;

import java.awt.Graphics;

/**
 * Interface for displaying elements under the holes on the book eg : displaying
 * an image
 * 
 * @author pfreydiere
 * 
 */
public interface VirtualBookComponentBackgroundLayer extends
		VirtualBookComponentLayer {

	/**
	 * Method called when the background is drawn
	 * 
	 * @param g
	 *            the graphic context
	 * @param component
	 *            the component
	 */
	void drawBackground(Graphics g, JVirtualBookComponent component);

}
