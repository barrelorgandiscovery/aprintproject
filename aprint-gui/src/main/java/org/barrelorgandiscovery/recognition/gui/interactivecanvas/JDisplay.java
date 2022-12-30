package org.barrelorgandiscovery.recognition.gui.interactivecanvas;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.barrelorgandiscovery.gui.aedit.CurrentToolChanged;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools.PanTool;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools.ZoomTool;
import org.barrelorgandiscovery.tools.ImageTools;

/**
 * Component for display an image, and add informations on top of it
 *
 * @author pfreydiere
 */
public class JDisplay extends JComponent implements CurrentToolChangedAware {

	private static Logger logger = Logger.getLogger(JDisplay.class);

	private class InnerLayerChangedListener implements LayerChangedListener {
		public void layerContentChanged() {
			repaint();
		}

		public void layerSelectionChanged() {
			repaint();
		}
	}

	private InnerLayerChangedListener refresh = new InnerLayerChangedListener();

	private Tool defaultTool = null;

	private Tool panTool = null;

	public JDisplay() {
		super();
		setToolTipText(""); // activate tooltips

		try {
			this.defaultTool = new PanTool(this);
			this.panTool = new PanTool(this);
		} catch (Exception ex) {
			logger.error("error instanciating the default tool :" + ex.getMessage(), ex);
		}

		// Adding tools events
		addMouseListener(new MouseListener() {

			public void mouseReleased(MouseEvent e) {
				if ((e.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) != 0) {
					panTool.mouseReleased(e);
				} else {
					if (currentTool != null) {
						currentTool.mouseReleased(e);
					} else {
						if (defaultTool != null) {
							defaultTool.mouseReleased(e);
						}
					}
				}
			}

			public void mousePressed(MouseEvent e) {
				if ((e.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) != 0) {
					panTool.mousePressed(e);
				} else {

					if (currentTool != null) {
						currentTool.mousePressed(e);
					} else {
						if (defaultTool != null) {
							defaultTool.mousePressed(e);
						}
					}
				}
			}

			public void mouseExited(MouseEvent e) {
				if ((e.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) != 0) {
					panTool.mouseExited(e);
				} else {

					if (currentTool != null) {
						currentTool.mouseExited(e);
					} else {
						if (defaultTool != null) {
							defaultTool.mouseExited(e);
						}
					}
				}
			}

			public void mouseEntered(MouseEvent e) {
				if ((e.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) != 0) {
					panTool.mouseEnter(e);
				} else {

					if (currentTool != null) {
						currentTool.mouseEnter(e);
					} else {
						if (defaultTool != null) {
							defaultTool.mouseEnter(e);
						}
					}
				}
				requestFocus();
			}

			public void mouseClicked(MouseEvent e) {
				if ((e.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) != 0) {
					panTool.mouseClicked(e);
				} else {

					if (currentTool != null) {
						currentTool.mouseClicked(e);
					} else {
						if (defaultTool != null) {
							defaultTool.mouseClicked(e);
						}
					}
				}
			}
		});

		addMouseMotionListener(new MouseMotionListener() {

			public void mouseMoved(MouseEvent e) {
				if ((e.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) != 0) {
					panTool.mouseMoved(e);
				} else {

					if (currentTool != null) {
						currentTool.mouseMoved(e);
					} else {
						if (defaultTool != null) {
							defaultTool.mouseMoved(e);
						}
					}
				}
			}

			public void mouseDragged(MouseEvent e) {
				if ((e.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) != 0) {
					panTool.mouseDragged(e);
				} else {

					if (currentTool != null) {
						currentTool.mouseDragged(e);
					} else {
						if (defaultTool != null) {
							defaultTool.mouseDragged(e);
						}
					}
				}
			}
		});

		addMouseWheelListener(new MouseWheelListener() {

			public void mouseWheelMoved(MouseWheelEvent e) {
				if ((e.getButton() == MouseEvent.BUTTON2)) {
					panTool.mouseWheel(e);
				} else {

					if (currentTool != null) {
						currentTool.mouseWheel(e);
					} else {
						if (defaultTool != null) {
							defaultTool.mouseWheel(e);
						}
					}
				}
			}
		});

		addMouseWheelListener(new MouseWheelListener() {

			public void mouseWheelMoved(MouseWheelEvent e) {
				try {

					double factor = (1 + (Math.abs(e.getPreciseWheelRotation()) + 1.0) / 20);
					if (e.getPreciseWheelRotation() > 0)
						factor = 1 / factor;

					scaleAndCenterOnScreenCoordinates(factor, e.getX(), e.getY());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				if (currentTool != null) {
					currentTool.keyTyped(e);
				} else {
					if (defaultTool != null) {
						defaultTool.keyTyped(e);
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (currentTool != null) {
					currentTool.keyReleased(e);
				} else {
					if (defaultTool != null) {
						defaultTool.keyReleased(e);
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (currentTool != null) {
					currentTool.keyPressed(e);
				} else {
					if (defaultTool != null) {
						defaultTool.keyPressed(e);
					}
				}
			}
		});
	}

	private AffineTransform currentTransform = AffineTransform.getRotateInstance(0);

	@Override
	protected void paintComponent(Graphics g) {

		super.paintComponent(g);

		if (!(g instanceof Graphics2D))
			return;

		Graphics2D g2d = (Graphics2D) g;
		AffineTransform old = g2d.getTransform();
		try {
			AffineTransform olftclone = (AffineTransform) old.clone();
			olftclone.concatenate(currentTransform);
			g2d.setTransform(olftclone);
			/*
			 * RenderingHints hints = g2d.getRenderingHints();
			 * hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			 * hints.put(RenderingHints.KEY_INTERPOLATION,
			 * RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			 * 
			 * g2d.setRenderingHints(hints);
			 */
			for (JLayer layer : layers) {
				try {
					layer.drawLayer(g2d);
				} catch (Exception ex) {
					logger.error("error in drawing layer " + layer, ex);
				}
			}

			// paint tool elements
			if (currentTool != null) {
				currentTool.paintElements(g);
			}

		} finally {
			g2d.setTransform(old);
		}
	}

	/**
	 * get the original position from x/y screen position
	 *
	 * @param screenx
	 * @param screeny
	 * @return
	 * @throws Exception
	 */
	public Point2D getOriginPosition(int screenx, int screeny) throws Exception {
		Point2D.Double ptSrc = new Point2D.Double(screenx, screeny);
		return currentTransform.inverseTransform(ptSrc, null);
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		try {
			// convert position
			Point2D.Double p = (Point2D.Double) getOriginPosition(event.getX(), event.getY());

			for (JLayer l : layers) {
				if (logger.isDebugEnabled()) {
					logger.debug("query layer " + l);
				}
				String text = l.getTooltip(p);
				if (text != null) {
					logger.debug("returning " + text);
					return text;
				}
			}

		} catch (Exception ex) {
			logger.debug("error in getToolTipText :" + ex.getMessage(), ex);
		}
		logger.debug("no tooltips");
		return null;
	}

	/**
	 * @param centerPoint
	 * @throws Exception
	 */
	public void panTo(Point2D centerPoint) throws Exception {

		assert centerPoint != null;

		AffineTransform t = currentTransform;
		AffineTransform reverse = t.createInverse();

		int w = getWidth();
		int h = getHeight();

		Point2D r = reverse.transform(new Point2D.Double(w / 2, h / 2), null);

		t.concatenate(
				AffineTransform.getTranslateInstance(centerPoint.getX() - r.getX(), centerPoint.getY() - r.getY()));

		repaint();
	}

	private IDisplayViewListener displayViewListener = null;

	public void setDisplayViewListener(IDisplayViewListener displayViewListener) {
		this.displayViewListener = displayViewListener;
	}

	protected void informTransformChanged() {
		if (displayViewListener != null) {
			try {
				displayViewListener.currentViewChanged(currentTransform);
			} catch (Exception ex) {
				logger.error("error in informTransformChanged :" + ex.getMessage(), ex);
			}
		}
	}

	/**
	 * zoom to a specific image position in the original coordinates
	 *
	 * @param rectangle
	 */
	public void zoomTo(Rectangle2D.Double rectangle) {
		assert rectangle != null;

		// src coordinate
		double centerX = rectangle.getCenterX();
		double centerY = rectangle.getCenterY();

		int height = getHeight();

		double newFactor = height / rectangle.height;

		AffineTransform t = AffineTransform.getScaleInstance(newFactor, newFactor);

		t.concatenate(AffineTransform.getTranslateInstance(-centerX + getWidth() / 2 / newFactor,
				-centerY + getHeight() / 2 / newFactor));

		currentTransform = t;
		repaint();
	}

	public double getCurrentFactor() {
		return currentTransform.getScaleX();
	}

	/**
	 * get the pixel size in the original space
	 *
	 * @return
	 * @throws Exception
	 */
	public double getPixelSize() throws Exception {

		Point2D ptSrc = new Point2D.Double(0, 0);
		Point2D ptSrc2 = new Point2D.Double(0, 1);
		Point2D d1 = currentTransform.inverseTransform(ptSrc, null);
		Point2D d2 = currentTransform.inverseTransform(ptSrc2, null);

		return d1.distance(d2);
	}

	/**
	 * apply a scale factor, and recenter ont the screen coordinate
	 *
	 * @param factor
	 * @param screenx
	 * @param screeny
	 * @throws Exception
	 */
	public void scaleAndCenterOnScreenCoordinates(double factor, int screenx, int screeny) throws Exception {

		Point2D ptSrc = new Point2D.Double(screenx, screeny);
		Point2D origin = currentTransform.inverseTransform(ptSrc, null);

		// dest image coordinate
		// deviens le centre dans la nouvelle transformation

		AffineTransform newTransform = new AffineTransform(currentTransform);
		newTransform.scale(factor, factor);

		Point2D uncorrectedDestination = newTransform.transform(origin, null);

		// manage translation for centering the image
		AffineTransform t = new AffineTransform();
		t.setToTranslation(getWidth() / 2 - uncorrectedDestination.getX(),
				getHeight() / 2 - uncorrectedDestination.getY());
		newTransform.preConcatenate(t);

		currentTransform = newTransform;
		repaint();
		informTransformChanged();
	}

	public Rectangle2D.Double convertToOriginalImagePosition(Rectangle2D.Double rect) throws Exception {
		assert rect != null;
		Point2D.Double topleft = new Point2D.Double(rect.x, rect.y);
		Point2D.Double bottomright = new Point2D.Double(rect.x + rect.width, rect.y + rect.height);

		Point2D.Double originTopleft = new Point2D.Double();
		Point2D.Double originBottomRight = new Point2D.Double();

		currentTransform.inverseTransform(topleft, originTopleft);
		currentTransform.inverseTransform(bottomright, originBottomRight);

		return new Rectangle2D.Double(originTopleft.x, originTopleft.y, originBottomRight.x - originTopleft.x,
				originBottomRight.y - originTopleft.y);
	}

	public static void main(String[] args) throws Exception {

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					BasicConfigurator.configure(new ConsoleAppender(new PatternLayout()));
					Logger.getRootLogger().setLevel(Level.DEBUG);

					JFrame test = new JFrame();
					final JDisplay id = new JDisplay();
					id.setToolTipText("");

					JImageDisplayLayer jImageDisplayLayer = new JImageDisplayLayer();

					File f = new File(
							"C:\\projets\\APrint\\contributions\\gérard\\2014_09_Reconnaissance\\numerisation\\DSCN2825.JPG");
					if (!f.exists())
						throw new Exception("file " + f.getAbsolutePath() + " not found");
					BufferedImage bi = ImageTools.loadImage(f.toURL());

					jImageDisplayLayer.setImageToDisplay(bi);

					id.addLayer(jImageDisplayLayer);

					test.getContentPane().add(id);

					test.setSize(500, 500);
					test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					test.setVisible(true);

					JEllipticLayer sl = new JEllipticLayer();
					id.addLayer(sl);

					// id.setCurrentTool(new CreatePointTool(id, sl));
					id.setCurrentTool(new ZoomTool(id));

					id.fit();

					id.zoomTo(new Rectangle2D.Double(1600, 1600, 300, 500));

				} catch (Exception ex) {
					ex.printStackTrace(System.err);
				}
			}
		});
	}

	// Tools management

	private Tool currentTool;

	/**
	 * change the current tool
	 *
	 * @param t the tool to set, null for remove
	 */
	public void setCurrentTool(Tool t) {
		if (currentTool != null)
			currentTool.unactivated();

		Tool oldTool = currentTool;

		currentTool = t;
		if (currentTool != null)
			t.activated();

		fireToolChanged(oldTool, t);
	}

	/**
	 * return the current tool
	 *
	 * @return
	 */
	public Tool getCurrentTool() {
		return this.currentTool;
	}

	/** set the current tool to null */
	public void resetCurrentTool() {
		setCurrentTool(null);
	}

	public void fit() {

		try {
			Rectangle2D extent = null;

			for (JLayer layer : layers) {

				Rectangle2D layerExtent = layer.getExtent();
				if (extent == null) {
					extent = layerExtent;
				} else {
					if (layerExtent != null)
						Rectangle2D.union(layerExtent, extent, extent);
				}
			}

			int width = getWidth();
			int height = getHeight();

			if (extent == null) {
				logger.warn("null extent .. cannot fit");
				return;
			}

			if (width == 0 || height == 0) {
				logger.warn("no width or no height");
				return;
			}

			double rw = extent.getWidth() / width;
			double rh = extent.getHeight() / height;

			double r = 1 / Math.max(rw, rh);

			currentTransform = AffineTransform.getScaleInstance(r, r);

			repaint();

		} catch (Exception ex) {
			logger.error("error in fitting " + ex.getMessage(), ex);
		}
	}

	// layers management

	private List<JLayer> layers = new ArrayList<JLayer>();

	public void addLayer(JLayer layer) {
		if (layer != null) {
			layers.add(layer);
			layer.addLayerChangedListener(refresh);
		}
	}

	/**
	 * remove the layer.
	 * 
	 * @param layer
	 */
	public void removeLayer(JLayer layer) {
		layers.remove(layer);
		layer.removeLayerChangedListener(refresh);
	}

	/**
	 * listener for current tool change
	 */
	private Vector<CurrentToolChanged> listeners = new Vector<CurrentToolChanged>();

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.recognition.gui.interactivecanvas.
	 * CurrentToolChangedAware#addCurrentToolChangedListener(org.
	 * barrelorgandiscovery.gui.aedit.CurrentToolChanged)
	 */
	public void addCurrentToolChangedListener(CurrentToolChanged listener) {
		if (listener != null)
			listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.recognition.gui.interactivecanvas.
	 * CurrentToolChangedAware#removeCurrentToolChangedListener(org.
	 * barrelorgandiscovery.gui.aedit.CurrentToolChanged)
	 */
	public void removeCurrentToolChangedListener(CurrentToolChanged listener) {
		if (listener != null)
			listeners.remove(listener);
	}

	/**
	 * called for firing the event for tool change.
	 * 
	 * @param oldTool
	 * @param newTool
	 */
	protected void fireToolChanged(Tool oldTool, Tool newTool) {
		for (CurrentToolChanged ctc : listeners) {
			try {
				ctc.currentToolChanged(oldTool, newTool);
			} catch (Exception ex) {
				logger.error("error in changing current tool :" + ex.getMessage(), ex);
			}
		}
	}

	/**
	 * get the affinetranform for display.
	 * 
	 * @return
	 */
	public AffineTransform getTransformRef() {
		return currentTransform;
	}

	/**
	 * define the transform ref.
	 * 
	 * @param transform
	 */
	public void setTransformRef(AffineTransform transform) {
		currentTransform = transform;
	}

	/**
	 * define the default tool.
	 * 
	 * @param t
	 */
	public void setDefaultTool(Tool t) {
		if (this.defaultTool != null)
			this.defaultTool.unactivated();

		this.defaultTool = t;
		if (this.defaultTool != null)
			t.activated();
	}

	/**
	 * getting the default tool.
	 * 
	 * @return
	 */
	public Tool getDefaultTool() {
		return defaultTool;
	}

}
