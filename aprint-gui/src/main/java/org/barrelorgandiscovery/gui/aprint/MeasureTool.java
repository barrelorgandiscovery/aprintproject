package org.barrelorgandiscovery.gui.aprint;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import org.barrelorgandiscovery.gui.aedit.JVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayer;

/**
 * Tool for measuring on the book (evaluating distances)
 * 
 * @author Freydiere Patrice
 * 
 */
public class MeasureTool extends Tool {

	private JVirtualBookScrollableComponent vbc;

	private int firstPointX = Integer.MIN_VALUE;
	private int firstPointY = Integer.MIN_VALUE;

	private int secondPointX = Integer.MIN_VALUE;
	private int secondPointY = Integer.MIN_VALUE;

	public MeasureTool(JVirtualBookScrollableComponent vbcomponent) {
		this.vbc = vbcomponent;
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		secondPointX = e.getX();
		secondPointY = e.getY();

		vbc.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		deactivate();
	}

	@Override
	public void unactivated() {
		deactivate();
	}

	protected void deactivate() {
		firstPointX = firstPointY = secondPointX = secondPointY = Integer.MIN_VALUE;
		vbc.repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {

		firstPointX = e.getX();
		firstPointY = e.getY();

	}
	
	@Override
	public void paintElements(Graphics g) {
		
		super.paintElements(g);
	
		Color old = g.getColor();
		try {

			g.setColor(Color.black);

			if (firstPointX != Integer.MIN_VALUE
					&& firstPointY != Integer.MIN_VALUE
					&& secondPointX != Integer.MIN_VALUE
					&& secondPointY != Integer.MIN_VALUE) {

				g.drawLine(firstPointX, firstPointY, secondPointX, secondPointY);

				g.drawLine(firstPointX, firstPointY - 5, firstPointX,
						firstPointY + 5);
				g.drawLine(secondPointX, secondPointY - 5, secondPointX,
						secondPointY + 5);

				// compute distance ...
				double x1 = vbc.convertScreenXToCarton(firstPointX);
				double y1 = vbc.convertScreenXToCarton(firstPointY);

				double x2 = vbc.convertScreenXToCarton(secondPointX);
				double y2 = vbc.convertScreenXToCarton(secondPointY);

				double distance = Math.sqrt(Math.pow((x2 - x1), 2.0)
						+ Math.pow((y2 - y1), 2.0));

				g.drawString(" " + (((int) (distance * 10)) / 10.0) + " mm",
						secondPointX, secondPointY);

			}

		} finally {
			g.setColor(old);
		}

	}

	private boolean visible = true;

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

}
