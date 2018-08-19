package org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.gui.tools.CursorTools;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JDisplay;
import org.barrelorgandiscovery.tools.ImageTools;

public class ZoomTool extends Tool {

	private static Logger logger = Logger.getLogger(ZoomTool.class);

	private JDisplay display;

	private Point2D corner = null;
	private Point2D bottom = null;

	private Cursor customCursor = null;
	private Cursor customCursorOut = null;

	public ZoomTool(JDisplay display) throws Exception {
		assert display != null;
		this.display = display;
		this.customCursor = CursorTools
				.createCursorWithImage(ImageTools.loadImage(getClass().getResource("viewmag.png"))); //$NON-NLS-1$

		customCursorOut = CursorTools
				.createCursorWithImage(ImageTools.loadImage(getClass().getResource("viewmagminus.png")));

	}

	@Override
	public void activated() {
		super.activated();
		display.setCursor(customCursor);
	}

	@Override
	public void unactivated() {

		display.setCursor(Cursor.getDefaultCursor());
		super.unactivated();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		try {
			this.corner = display.getOriginPosition(e.getX(), e.getY());

		} catch (Exception ex) {
			logger.error("error in getting origin position :" + ex.getMessage(), ex); //$NON-NLS-1$
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		try {
			if (corner != null) {
				bottom = display.getOriginPosition(e.getX(), e.getY());
				display.repaint();
			}

			super.mouseDragged(e);
		} catch (Exception ex) {
			logger.error("error in getting origin position :" + ex.getMessage(), ex); //$NON-NLS-1$
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode()==  KeyEvent.VK_SHIFT) {
			display.setCursor(customCursorOut);
		}

		super.keyPressed(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode()==  KeyEvent.VK_SHIFT) {
			display.setCursor(customCursor);
		}
		super.keyReleased(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);

		if (corner != null && bottom != null) {

			Rectangle2D.Double d = createRectangle();
			try {
				if (e.isShiftDown()) {
					d = createRectangle(display.getOriginPosition(0, 0),
							display.getOriginPosition(display.getWidth(), display.getHeight()));
					d = ShapeTools.scale2(d);
				}

				display.zoomTo(d);
				
			} catch (Exception ex) {
				logger.error("error while computing the zoom out rectangle :" //$NON-NLS-1$
						+ ex.getMessage(), ex);
			}
		}

		corner = null;
		bottom = null;

	}

	protected Double createRectangle() {
		return createRectangle(corner, bottom);
	}

	
	
	
	protected Rectangle2D.Double createRectangle(Point2D p1, Point2D p2) {
		
		double xmin = Math.min(p1.getX(), p2.getX());
		double ymin = Math.min(p1.getY(), p2.getY());
		double xmax = Math.max(p1.getX(), p2.getX());
		double ymax = Math.max(p1.getY(), p2.getY());
		
		return new Rectangle2D.Double(xmin, ymin, xmax - xmin, ymax - ymin);
	}

	@Override
	public void paintElements(Graphics g) {
		super.paintElements(g);

		if (corner == null || bottom == null)
			return;

		Graphics2D g2d = (Graphics2D) g;

		g2d.draw(createRectangle());

	}

}
