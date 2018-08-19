package org.barrelorgandiscovery.gui.aprint;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.aedit.Tool;

/**
 * Pan Tool
 * 
 * @author use
 * 
 */
public class PanTool extends Tool {

	private static Logger logger = Logger.getLogger(PanTool.class);

	private boolean pan = false;

	private double origineX;

	private int posx;

	private JVirtualBookScrollableComponent vbc;

	public PanTool(JVirtualBookScrollableComponent vbc) {
		this.vbc = vbc;
	}

	public void mouseMoved(MouseEvent e) {

	}

	public void mouseDragged(MouseEvent e) {
		logger.debug("mouse dragged :" + e); //$NON-NLS-1$

		if (pan) {
			int x = e.getX();
			int y = e.getY();

			int delta = posx - x;

			logger.debug("pan to :"); //$NON-NLS-1$

			vbc.setXoffset(origineX + vbc.pixelToMM(delta));

			vbc.repaint();

		}
	}

	@Override
	public void mousePressed(MouseEvent e) {

		vbc.requestFocusInWindow();

		logger.debug("mouse pressed :" + e.getButton()); //$NON-NLS-1$

		if (e.getButton() == MouseEvent.BUTTON1) {
			posx = e.getX();

			origineX = vbc.getXoffset();

			pan = true;
			logger.debug("pan started"); //$NON-NLS-1$

		} else {

			// normal click on the book

		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {

		pan = false;

		if (e.getButton() == MouseEvent.BUTTON1) {

			// récupération du click sur le carton ...
			vbc.setHightlight(vbc.convertScreenXToCarton(e.getX()));

			vbc.repaint();
		}
	}

	@Override
	public void activated() {
		vbc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

	}

	@Override
	public void unactivated() {
		vbc.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

}
