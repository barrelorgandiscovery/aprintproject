package org.barrelorgandiscovery.gui.aedit;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * This class define a tool used in the virtual book component, a tool is a
 * class that takes all the main events of the component
 * 
 * @author Freydiere Patrice
 */
public abstract class Tool implements KeyListener {

	/**
	 * Signal the tool is activated, if we have something to do before
	 */
	public void activated() {

	}

	/**
	 * signal the tool is beeing unactivated
	 */
	public void unactivated() {

	}

	/**
	 * Mouse moved event
	 */
	public void mouseMoved(MouseEvent e) {

	}

	/**
	 * Mouse pressed event
	 */
	public void mousePressed(MouseEvent e) {

	}

	/**
	 * Mouse Released event
	 */
	public void mouseReleased(MouseEvent e) {

	}

	/**
	 * mouse dragged event
	 */
	public void mouseDragged(MouseEvent e) {

	}

	/**
	 * mouse clicked event
	 * 
	 * @param e
	 */
	public void mouseClicked(MouseEvent e) {

	}

	/**
	 * mouse enter event
	 */
	public void mouseEnter(MouseEvent e) {

	}

	/**
	 * mouse exited event
	 * 
	 * @param e
	 */
	public void mouseExited(MouseEvent e) {

	}

	/**
	 * mouse wheel
	 * 
	 * @param e
	 */
	public void mouseWheel(MouseWheelEvent e) {

	}

	/**
	 * Paint a feed back from tool
	 * 
	 * @param g
	 *            the graphic context
	 */
	public void paintElements(Graphics g) {

	}

	public void keyPressed(KeyEvent e) {

	}

	public void keyReleased(KeyEvent e) {

	}

	public void keyTyped(KeyEvent e) {

	}

}
