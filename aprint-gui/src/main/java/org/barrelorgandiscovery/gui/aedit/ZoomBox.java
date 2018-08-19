package org.barrelorgandiscovery.gui.aedit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.Position;

/**
 * Tool for selecting holes in the book
 * 
 * @author Freydiere Patrice
 */
public class ZoomBox extends Tool {

	private static final Logger logger = Logger.getLogger(ZoomBox.class);

	private JVirtualBookScrollableComponent c;

	private int x = -1;
	private int y = -1;

	private int xend = -1;
	private int yend = -1;

	public ZoomBox(JVirtualBookScrollableComponent c) {
		this.c = c;
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		logger.debug("dragged");

		Position p = c.query(e.getX(), e.getY());

		if (!(x == -1 || y == -1)) {

			xend = e.getX();
			yend = e.getY();

			c.repaint();
		}

		if (p == null)
			return;

	}

	@Override
	public void mousePressed(MouseEvent e) {

		this.x = e.getX();
		this.y = e.getY();

	}

	@Override
	public void mouseReleased(MouseEvent e) {

		// zoom factor

		// make a box

		double width = Math.abs(x - xend);
		double height = Math.abs(y - yend);
		double boxRatio = width / height;
		Point2D.Double center = new Point2D.Double(c.convertScreenXToCarton((int) ((x + xend) / 2.0)),
				c.convertScreenYToCarton((int) ((y + yend) / 2.0)));

		double viewRatio = 1.0 * c.getWidth() / c.getHeight();

		double pwidth = c.pixelToMM((int) width);
		double pheight = c.pixelToMM((int) height);

		double factor = 1.0;

		double viewHeight = c.pixelToMM(c.getHeight());
		double viewWidth = c.pixelToMM(c.getWidth());

		if (boxRatio > viewRatio) {
			// width driven
			factor = pwidth / viewWidth * c.getXfactor();

		} else {

			// height driven
			factor = pheight / viewHeight * c.getXfactor();
			pwidth = pheight * viewRatio;

		}

		factor = factor * 1.1; // enlarge a bit

		double newXOffset = center.x - pwidth / 2.0;
		double newYOffset = center.y - pwidth / viewRatio / 2.0;

		c.setXfactor(factor);
		c.setXoffset(newXOffset);
		c.setYoffset(newYOffset);

		x = y = xend = yend = -1;

		c.repaint();

	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	@Override
	public void paintElements(Graphics g) {

		logger.debug("paint");

		if (x == -1 || y == -1 || xend == -1 || yend == -1)
			return;

		Color color = g.getColor();
		try {

			int startx = Math.min(x, xend);
			int starty = Math.min(y, yend);
			int width = Math.abs(xend - x);
			int height = Math.abs(yend - y);

			g.setColor(Color.black);
			g.drawRect(startx, starty, width, height);
		} finally {
			g.setColor(color);
		}
	}

}
