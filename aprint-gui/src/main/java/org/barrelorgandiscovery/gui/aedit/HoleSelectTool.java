package org.barrelorgandiscovery.gui.aedit;

import java.awt.Color;
import java.awt.Graphics;
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
public class HoleSelectTool extends Tool {

	private static final Logger logger = Logger.getLogger(HoleSelectTool.class);

	private JVirtualBookScrollableComponent c;

	private long positionstart = -1;

	private int pistestart = -1;

	private UndoStack us;

	private int x = -1;
	private int y = -1;

	private int xend = -1;
	private int yend = -1;

	public HoleSelectTool(JVirtualBookScrollableComponent c, UndoStack us) {
		this.c = c;
		this.us = us;

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
		Point2D.Double d = new Point2D.Double(
				c.convertScreenXToCarton(e.getX()), c.convertScreenYToCarton(e
						.getY()));

		// se.snapPosition(d);

		// c.setHightlight(d.x);
		// c.repaint();
	}

	/**
	 * Recupère la position et borne les pistes, pour se caler sur les bords
	 * 
	 * @return
	 */
	private Position limitedPositionQuery(int x, int y) {
		Position p = c.queryWithExtraMargin(x, y);

		if (p == null)
			return null;

		int trackNb = c.getVirtualBook().getScale().getTrackNb();
		if (p.track >= trackNb) {
			p.track = trackNb - 1;
		}

		if (p.track < 0) {
			p.track = 0;
		}
		
		return p;
	}

	@Override
	public void mousePressed(MouseEvent e) {

		this.x = e.getX();
		this.y = e.getY();

		Position p = limitedPositionQuery(x, y);
		if (p == null)
			return;

		positionstart = p.position;
		pistestart = p.track;

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
		Position p = limitedPositionQuery(e.getX(), e.getY());
		if (p == null)
			return;

		Point2D.Double d = new Point2D.Double(
				c.convertScreenXToCarton(e.getX()), c.convertScreenYToCarton(e
						.getY()));

		Point2D.Double ps = new Point2D.Double(c.timestampToMM(positionstart),
				pistestart);

		long first = c.MMToTime(d.x);
		long second = c.MMToTime(ps.x);
		
		long length = Math.abs(first - second);
		first = Math.min(first, second);
		
		
		List<Hole> selectHoles = c.getVirtualBook().findHoles(
				first, length, pistestart, p.track);

		if (e.getButton() == MouseEvent.BUTTON1) {

			if ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0) {
				// add to selection
				for (Iterator iterator = selectHoles.iterator(); iterator
						.hasNext();) {
					Hole hole = (Hole) iterator.next();
					c.addToSelection(hole);
				}

			} else if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0) {
				// remove from selection

				for (Iterator iterator = selectHoles.iterator(); iterator
						.hasNext();) {
					Hole hole = (Hole) iterator.next();
					c.removeFromSelection(hole);
				}

			} else {

				c.clearSelection();
				for (Iterator iterator = selectHoles.iterator(); iterator
						.hasNext();) {
					Hole hole = (Hole) iterator.next();
					c.addToSelection(hole);
				}
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
