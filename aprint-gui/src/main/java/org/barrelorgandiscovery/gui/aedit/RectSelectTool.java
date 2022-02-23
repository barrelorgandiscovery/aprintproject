package org.barrelorgandiscovery.gui.aedit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.Position;

/**
 * Tool for creating rect zones
 * 
 * @author pfreydiere
 */
public class RectSelectTool extends Tool {

	private static final Logger logger = Logger.getLogger(RectSelectTool.class);

	public static interface RectSelectToolListener {
		public void rectDrawn(double xmin, double ymin, double xmax, double ymax);
	}

	private JVirtualBookScrollableComponent c;

	private UndoStack us;

	private int x = -1;
	private int y = -1;

	private int xend = -1;
	private int yend = -1;

	private RectSelectToolListener listener;

	public RectSelectTool(JVirtualBookScrollableComponent c, UndoStack us) {
		this(c, us, null);
	}

	public RectSelectTool(JVirtualBookScrollableComponent c, UndoStack us, RectSelectToolListener listener) {
		this.c = c;
		this.us = us;
		this.listener = listener;
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		logger.debug("dragged");

		
		if (!(x == -1 || y == -1)) {

			xend = e.getX();
			yend = e.getY();

			c.repaint();
		}

		
	}

	@Override
	public void mousePressed(MouseEvent e) {

		this.x = e.getX();
		this.y = e.getY();

	}

	@Override
	public void mouseReleased(MouseEvent e) {

		Point2D.Double currentEndPos = new Point2D.Double(c.convertScreenXToCarton(e.getX()),
				c.convertScreenYToCarton(e.getY()));

		Point2D.Double posStart = new Point2D.Double(c.convertScreenXToCarton(x), c.convertScreenYToCarton(y));

		if (e.getButton() == MouseEvent.BUTTON1) {

			if (listener != null) {
				listener.rectDrawn(posStart.x, posStart.y, currentEndPos.x, currentEndPos.y);
			}

		}

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
