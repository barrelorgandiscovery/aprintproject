package org.barrelorgandiscovery.recognition.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.recognition.BookEdges;
import org.barrelorgandiscovery.recognition.IntArrayHolder;

public class JEdgeDrawingComponent extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7355609070475453687L;

	private static Logger logger = Logger
			.getLogger(JEdgeDrawingComponent.class);

	private int lastx = -1, lasty = -1;

	private AffineTransform currentTransform = AffineTransform
			.getScaleInstance(0.2, 0.2);

	public JEdgeDrawingComponent() {

		addMouseWheelListener(new MouseWheelListener() {

			public void mouseWheelMoved(MouseWheelEvent e) {

				int rotation = e.getWheelRotation();
				float sgn = Math.signum(rotation);

				double[] dest = new double[2];
				try {
					currentTransform.inverseTransform(new double[] { e.getX(),
							e.getY() }, 0, dest, 0, 1);

					double deplx = dest[0];
					double deply =  dest[1];

					double factor = rotation / 1.15;
					if (factor < 0)
						factor = 1 / -(factor);

					logger.debug("factor : " + factor);
					
					
					currentTransform.concatenate(AffineTransform
							.getScaleInstance(factor, factor));

					Point2D.Double destPt = new Point2D.Double();
					currentTransform.transform(new Point2D.Double(deplx, deply), destPt);
				
					currentTransform.concatenate(AffineTransform
							.getTranslateInstance(-(destPt.getX() - getWidth()/2),  -(destPt.getY() - getHeight()/2)));
					
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}

				repaint();

			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {

				double[] dest = new double[2];
				try {
					currentTransform.inverseTransform(new double[] { e.getX(),
							e.getY() }, 0, dest, 0, 1);

					lastx = (int) dest[0];
					lasty = (int) dest[1];
					repaint();
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}

				repaint();
			}

		});

		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {

				if (currentPath != null) {

					if (e.getClickCount() == 2) {
						fireDrawingEdgeListener(currentPath);
						currentPath = new IntArrayHolder();
						return;
					}

					double[] dest = new double[2];
					try {
						currentTransform.inverseTransform(
								new double[] { e.getX(), e.getY() }, 0, dest,
								0, 1);

						currentPath.addElement((int) dest[0], (int) dest[1]);
						repaint();
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
				}

			}

		});

	}

	private BufferedImage image = null;

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public BufferedImage getImage() {
		return image;
	}

	private BookEdges edges;

	public void setEdges(BookEdges edges) {
		this.edges = edges;
	}

	public BookEdges getEdges() {
		return this.edges;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setTransform(currentTransform);

		

		if (image != null) {
			g.drawImage(image, 0, 0, null);
		}

		if (currentPath != null) {
			int count = currentPath.getCount();
			int[] x = new int[count + 1];
			int[] y = new int[count + 1];
			for (int i = 0; i < count; i++) {
				int[] e = currentPath.getElement(i);
				x[i] = e[0];
				y[i] = e[1];
			}
			y[count] = lasty;
			x[count] = lastx;

			g.drawPolyline(x, y, count + 1);
		}

		if (edges != null) {
			g.setColor(Color.RED);
			for (int i = 0; i < edges.getCount(); i++) {
				int[] el = edges.getElement(i);

				g.drawRect(i, el[0], 1, 1);
				g.drawRect(i, el[0] + el[1], 1, 1);

			}
		}

	}

	// ///////////////////////////////////////////////
	// Listener support

	private Vector<DrawingEdgeListener> drawingEdgeListeners = new Vector<DrawingEdgeListener>();

	public void addDrawingEdgeListener(DrawingEdgeListener listener) {
		if (listener != null)
			drawingEdgeListeners.add(listener);
	}

	public void removeDrawingEdgeListener(DrawingEdgeListener listener) {
		drawingEdgeListeners.remove(listener);
	}

	protected void fireDrawingEdgeListener(IntArrayHolder path) {
		for (DrawingEdgeListener listener : drawingEdgeListeners) {
			listener.polylineDraw(path);
		}
	}

	// Drawing support

	private IntArrayHolder currentPath = new IntArrayHolder();

	public void startDrawing() {
		this.currentPath = new IntArrayHolder();
	}

	public void endDrawing() {
		if (currentPath != null) {
			fireDrawingEdgeListener(currentPath);
		}

		currentPath = null;
	}

}
