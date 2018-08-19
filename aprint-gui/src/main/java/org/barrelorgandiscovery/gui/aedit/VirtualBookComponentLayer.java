package org.barrelorgandiscovery.gui.aedit;

import java.awt.Graphics;

/**
 * This interface define the methods to implement a "layer" that is displayed in
 * a virtual book component
 * 
 * @author Freydiere Patrice
 * 
 */
public interface VirtualBookComponentLayer {

	/**
	 * ask for a draw in the component
	 * 
	 * @param g
	 *            the graphic reference for drawing with Java2D
	 * @param jbookcomponentreference
	 *            the VirtualBookComponent reference
	 */
	public void draw(Graphics g, JVirtualBookComponent jbookcomponentreference);

	/**
	 * define if the layer is visible or not
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible);

	/**
	 * ask if the layer is visible
	 * 
	 * @return
	 */
	public boolean isVisible();
	

}
