package org.barrelorgandiscovery.extensionsng.perfo.ng.controlling;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayer;

/**
 * layer for displaying machine position, and status
 * 
 * @author pfreydiere
 * 
 */
public class MachinePositionLayer implements VirtualBookComponentLayer {

	public MachinePositionLayer() {

	}

	private double x = 0;
	private double y = 0;

	private boolean visible = true;

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	/**
	 * Define the machine position BEWARE, the X and Y are those of the machine
	 * AXIS, then inverted from the screen
	 * 
	 * @param x
	 * @param y
	 */
	public void setMachinePosition(double x, double y) {
		this.x = x;
		this.y = y;
	}

	private String status;

	public void setStatus(String status) {
		this.status = status;
	}

	public void draw(
			java.awt.Graphics g,
			org.barrelorgandiscovery.gui.aedit.JVirtualBookComponent jbookcomponentreference) {

		if (!isVisible()) {
			return;
		}

		Graphics2D g2d = (Graphics2D) g;
		Color old = g2d.getColor();
		try {
			g2d.setColor(Color.MAGENTA);
			Stroke os = g2d.getStroke();
			try {
				g2d.setStroke(new BasicStroke(3.0f));

				// /////////////////////////////////////////////////////////////
				// draw origin cross

				int xorigin = jbookcomponentreference.convertCartonToScreenX(0);
				int yorigin = jbookcomponentreference.convertCartonToScreenY(0);

				int originsize = 30;

				g2d.drawArc(xorigin - originsize, yorigin - originsize,
						2 * originsize, 2 * originsize, 0, 360);

				g2d.drawLine((int) (xorigin + 0.5 * originsize), yorigin,
						(int) (xorigin + 2 * originsize), yorigin);
				
				g2d.drawLine((int) (xorigin - 0.5 * originsize), yorigin,
						(int) (xorigin - 2 * originsize), yorigin);
				
				g2d.drawLine(xorigin , (int) (yorigin + 0.5 * originsize), xorigin,
						(int) (yorigin + 2 * originsize));
				
				g2d.drawLine(xorigin , (int) (yorigin - 0.5 * originsize), xorigin,
						(int) (yorigin - 2 * originsize));
				
				
			} finally {
				g2d.setStroke(os);
			}

			g2d.setColor(Color.black);

			int xint = jbookcomponentreference.convertCartonToScreenX(y);
			int yint = jbookcomponentreference.convertCartonToScreenY(x);

			g2d.drawLine(xint - 10, yint, xint - 2, yint);
			g2d.drawLine(xint + 2, yint, xint + 10, yint);
			g2d.drawLine(xint, yint - 10, xint, yint - 2);
			g2d.drawLine(xint, yint + 2, xint, yint + 10);
			
			g2d.drawArc(xint - 10, yint - 10, 20, 20, 0, 360);

			if (status != null) {
				g2d.drawString(status, xint + 10, yint + 10);
			}

		} finally {
			g2d.setColor(old);
		}

	};

	public double getY() {
		return y;
	}

	public double getX() {
		return x;
	}

}
