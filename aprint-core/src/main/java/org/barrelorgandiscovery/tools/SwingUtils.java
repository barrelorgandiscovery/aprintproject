package org.barrelorgandiscovery.tools;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

public class SwingUtils {

	/**
	 * Centre une fenetre
	 * 
	 * @param frame
	 */
	public static void center(JFrame frame) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Point center = ge.getCenterPoint();
		Rectangle bounds = ge.getMaximumWindowBounds();
		int w = Math.max(bounds.width / 2, Math.min(frame.getWidth(), bounds.width));
		int h = Math.max(bounds.height / 2, Math.min(frame.getHeight(), bounds.height));
		int x = center.x - w / 2, y = center.y - h / 2;
		frame.setBounds(x, y, w, h);
		if (w == bounds.width && h == bounds.height)
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.validate();
	}

	public static void center(JInternalFrame frame, Rectangle bounds) {

		Point center = new Point((int) bounds.getCenterX(), (int) bounds.getCenterY());
		int w = Math.max(bounds.width / 2, Math.min(frame.getWidth(), bounds.width));
		int h = Math.max(bounds.height / 2, Math.min(frame.getHeight(), bounds.height));
		int x = center.x - w / 2, y = center.y - h / 2;
		frame.setBounds(x, y, w, h);

		frame.validate();
	}

	/**
	 * Centre un dialog
	 * 
	 * @param frame
	 */
	public static void center(JDialog frame) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Point center = ge.getCenterPoint();
		Rectangle bounds = frame.getBounds();
		int w = Math.max(bounds.width / 2, Math.min(frame.getWidth(), bounds.width));
		int h = Math.max(bounds.height / 2, Math.min(frame.getHeight(), bounds.height));
		int x = center.x - w / 2, y = center.y - h / 2;

		frame.setLocation(x, y);
		frame.validate();
	}

	/**
	 * permet la désactivation/activation d'un ensemble de composans
	 * 
	 * @param comp
	 * @param enable
	 */
	public static void recurseSetEnable(Component comp, boolean enable) {
		if (comp == null)
			return;

		comp.setEnabled(enable);
		if (comp instanceof JComponent) {
			JComponent c = (JComponent) comp;
			for (int i = 0; i < c.getComponentCount(); i++) {
				Component child = c.getComponent(i);
				if (child instanceof JComponent) {
					recurseSetEnable((JComponent) child, enable);
				}
			}
		}
	}
}
