package org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.gui.tools.CursorTools;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JDisplay;
import org.barrelorgandiscovery.tools.ImageTools;

public class PanTool extends Tool {

	private static Logger logger = Logger.getLogger(PanTool.class);

	private int snappedX = -1;
	private int snappedY = -1;

	private JDisplay display;

	private Cursor customCursor;

	public PanTool(JDisplay display) throws Exception {
		this.display = display;

		customCursor = CursorTools.createCursorWithImage(ImageTools.loadImage(getClass().getResource("hand.png")));

	}

	@Override
	public void mousePressed(MouseEvent e) {

		super.mousePressed(e);

		snappedX = e.getX();
		snappedY = e.getY();

	}

	@Override
	public void mouseReleased(MouseEvent e) {

		if (snappedX != -1) {

			updatePos(e);

			snappedX = -1;
		}

		super.mouseReleased(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		super.mouseDragged(e);

		if (snappedX != -1) {

			updatePos(e);

			snappedX = e.getX();
			snappedY = e.getY();
		}

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
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);

		if (snappedX != -1) {

			updatePos(e);

			snappedX = -1;
		}

	}

	private void updatePos(MouseEvent e) {
		int deltax = e.getX() - snappedX;
		int deltay = e.getY() - snappedY;
		try {
			Point2D pt2 = display.getOriginPosition(display.getWidth() / 2 + deltax, display.getHeight() / 2 + deltay);

			display.panTo(pt2);

		} catch (Exception ex) {
			logger.debug(ex.getMessage(), ex);
		}
	}

	@Override
	public void paintElements(Graphics g) {
		super.paintElements(g);
	}

}
