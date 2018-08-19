package org.barrelorgandiscovery.gui.aedit;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalToolTipUI;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.Position;
import org.barrelorgandiscovery.virtualbook.Fragment;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * 
 * A Component that can display a virtual book component, with scrollable
 * capabilities
 * 
 * @author Freydiere Patrice
 * 
 */
public class JVirtualBookScrollableComponent extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5042922615958690652L;

	private static Logger logger = Logger.getLogger(JVirtualBookScrollableComponent.class);

	/**
	 * Horizontal scroll
	 */
	private JScrollBar hscroll;

	/**
	 * Vertical Scroll
	 */
	private JScrollBar vscroll;

	/**
	 * Book component
	 */
	private JVirtualBookComponent jvirtualbookcomponent;

	private boolean triggerScrolls = false;

	/**
	 * Constructor
	 */
	public JVirtualBookScrollableComponent() {
		super();
		setLayout(new BorderLayout());
		hscroll = new JScrollBar(JScrollBar.HORIZONTAL);
		vscroll = new JScrollBar(JScrollBar.VERTICAL);

		hscroll.addAdjustmentListener(new AdjustmentListener() {

			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (!triggerScrolls) {
					jvirtualbookcomponent.setXoffset(e.getValue() * increment);
					jvirtualbookcomponent.repaint();
				}
				triggerScrolls = false;
			}
		});

		vscroll.addAdjustmentListener(new AdjustmentListener() {

			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (!triggerScrolls) {
					jvirtualbookcomponent.setYoffset(e.getValue() * increment);
					jvirtualbookcomponent.repaint();
				}
				triggerScrolls = false;
			}
		});

		jvirtualbookcomponent = new JVirtualBookComponent();

		addMouseWheelListener(new MouseWheelListener() {

			public void mouseWheelMoved(MouseWheelEvent e) {
				int rotation = e.getWheelRotation();

				if ((e.getModifiersEx() & MouseWheelEvent.CTRL_DOWN_MASK) != 0) {
					double scale = 2;
					if (rotation != 0) {
						if (rotation < 0) {
							scale = 1 / scale;
						}
						double newscale = jvirtualbookcomponent.getXfactor() * scale;

						double centerX = convertScreenXToCarton(e.getX());
						double centerY = convertScreenYToCarton(e.getY());

						setXfactor(newscale);

						centerAtCartonPosition(centerX, centerY);

					}

				} else if ((e.getModifiersEx() & MouseWheelEvent.SHIFT_DOWN_MASK) != 0) {
					setXoffset((hscroll.getValue() + rotation) * increment);
				} else {

					double offsety = (vscroll.getValue() + rotation) * increment;
					if (offsety < 0)
						offsety = 0;
					setYoffset(offsety);

				}

				jvirtualbookcomponent.repaint();
			}

		});

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				computeIncrementAndUpdateScrollsFromBookAndComponentSize();
			}
		});

		// add(panel);

		add(jvirtualbookcomponent, BorderLayout.CENTER);
		add(hscroll, BorderLayout.SOUTH);
		add(vscroll, BorderLayout.EAST);

		validate();

		setToolTipText(""); // activate the tooltip .. work around

	}

	/**
	 * Increment for moving in the book
	 */
	private double increment = 10;

	private void computeIncrementAndUpdateScrollsFromBookAndComponentSize() {

		VirtualBook vb = jvirtualbookcomponent.getVirtualBook();
		if (vb == null) {
			increment = 10.0;
			return;
		}
		assert vb != null;

		double viewHeight = jvirtualbookcomponent.pixelToMm(getHeight());
		Scale s = vb.getScale();
		double bookWidth = s.getWidth();

		// try to move on 1/10 of the visible size
		increment = viewHeight / 10;

		// update the scrolls

		hscroll.setMaximum((int) (((double) vb.getLength()) / 1000000 * s.getSpeed() / increment));

		vscroll.setMaximum((int) (bookWidth / increment));

		vscroll.setVisibleAmount((int) (viewHeight / increment));

	}

	/**
	 * Construction du composant en ajoutant la couche
	 * 
	 * @param layer
	 *            la couche à ajouter
	 */
	public JVirtualBookScrollableComponent(VirtualBookComponentLayer layer) {
		this();
		jvirtualbookcomponent.addLayer(layer);
	}

	/**
	 * Définit le carton virtuel de travail
	 * 
	 * @param carton
	 */
	public void setVirtualBook(VirtualBook carton) {
		clearSelection();
		jvirtualbookcomponent.setVirtualBook(carton);
		touchBook();
	}

	/**
	 * 
	 * This method is called when a change has been done on the book
	 */
	public void touchBook() {

		// VirtualBook v = jvirtualbookcomponent.getVirtualBook();
		// if (v == null)
		// return;
		//
		// Scale s = v.getScale();
		//
		// hscroll.setMaximum((int) (((double) v.getLength()) / 1000000
		// * s.getSpeed() / increment));
		//
		// vscroll.setMaximum((int) (s.getWidth() / increment));

		computeIncrementAndUpdateScrollsFromBookAndComponentSize();
	}

	/**
	 * Calcule le facteur en x pour faire rentrer la largeur du carton dans le
	 * composant, cette methode redessine la zone ecran
	 */
	public void fitToScreen() {

		jvirtualbookcomponent.fitToComponentSize();
		repaint();

	}

	/**
	 * get the book
	 * 
	 * @return
	 */
	public VirtualBook getVirtualBook() {
		return jvirtualbookcomponent.getVirtualBook();
	}

	// @Override
	// public void setBounds(int x, int y, int width, int height) {
	// super.setBounds(x, y, width, height);
	// panel.setBounds(0, 0, width, height);
	// }

	/**
	 * get the book position from a hit on the screen
	 * 
	 * @param x
	 *            x pixel
	 * @param y
	 *            y pixel
	 * @return the position associated to this hit
	 */
	public Position query(int x, int y) {
		return jvirtualbookcomponent.query(x, y);
	}

	/**
	 * 
	 */
	public Position queryWithExtraMargin(int x, int y) {
		return jvirtualbookcomponent.queryWithExtraMargin(x, y);
	}

	// //////////////////////////////////////////////////////////
	// Gestion de la sélection ...

	/**
	 * add a hole to selection
	 */
	public void addToSelection(Hole n) {
		jvirtualbookcomponent.addToSelection(n);
	}

	/**
	 * remove hole from selection
	 */
	public void clearSelection() {
		jvirtualbookcomponent.clearSelection();
	}

	/**
	 * remove hole from selection
	 * 
	 * @param n
	 */
	public void removeFromSelection(Hole n) {
		jvirtualbookcomponent.removeFromSelection(n);
	}

	/**
	 * Get a selection copy
	 * 
	 * @return
	 */
	public Set<Hole> getSelectionCopy() {
		return jvirtualbookcomponent.getSelectionCopy();
	}

	// ///////////////////////////////////////////////////////////
	// gestion de la selection par blocks

	/**
	 * set the selection block
	 * 
	 * @param start
	 *            timestamp in microseconds
	 * @param length
	 *            length in microseconds
	 */
	public void setBlockSelection(long start, long length) {
		jvirtualbookcomponent.setBlockSelection(start, length);
	}

	/**
	 * clear the block selection
	 */
	public void clearBlockSelection() {
		jvirtualbookcomponent.clearBlockSelection();
	}

	/**
	 * get the block selection
	 * 
	 * @return
	 */
	public Fragment getBlockSelection() {
		return jvirtualbookcomponent.getBlockSelection();
	}

	// ///////////////////////////////////////////////////////////
	// hightlight

	/**
	 * set highlight
	 * 
	 * @param position
	 *            the position
	 */
	public void setHightlight(double position) {
		jvirtualbookcomponent.setHightlight(position);
		touchBook();
	}

	/**
	 * clear the highlight
	 */
	public void clearHightlight() {
		jvirtualbookcomponent.clearHightlight();
	}

	/**
	 * has this component a hightlight ?
	 * 
	 * @return
	 */
	public boolean hasHightlight() {
		return jvirtualbookcomponent.hasHightlight();
	}

	/**
	 * has a selection ?
	 * 
	 * @return
	 */
	public boolean hasSelection() {
		return jvirtualbookcomponent.hasSelection();
	}

	/**
	 * get the hightlight position
	 * 
	 * @return popsition in mm from beginning of the book
	 */
	public double getHightlight() {
		return jvirtualbookcomponent.getHightlight();
	}

	// Gestion des tooltip

	@Override
	public String getToolTipText(MouseEvent event) {
		return jvirtualbookcomponent.getToolTipText(event);
	}

	public void setXfactor(double xfactor) {

		jvirtualbookcomponent.setXfactor(xfactor);

		VirtualBook vb = getVirtualBook();
		if (vb == null)
			return;

		computeIncrementAndUpdateScrollsFromBookAndComponentSize();

		vscroll.revalidate();
		vscroll.repaint();

	}

	public double pixelToMM(int pixels) {
		return jvirtualbookcomponent.pixelToMm(pixels);
	}

	public int MmToPixel(double x) {
		return jvirtualbookcomponent.MmToPixel(x);
	}

	public long MMToTime(double mm) {
		return jvirtualbookcomponent.MMToTime(mm);
	}

	public double timeToMM(long time) {
		return jvirtualbookcomponent.timeToMM(time);
	}

	public double timestampToMM(long timestamp) {
		return jvirtualbookcomponent.timestampToMM(timestamp);
	}

	public double convertScreenXToCarton(int x) {
		return jvirtualbookcomponent.convertScreenXToCarton(x);
	}

	public double convertScreenYToCarton(int y) {
		return jvirtualbookcomponent.convertScreenYToCarton(y);
	}

	public double trackToMM(int track) {
		return jvirtualbookcomponent.trackToMM(track);
	}

	public AffineTransform addCartonTransformAndReturnOldOne(Graphics2D g2d) {
		return jvirtualbookcomponent.addCartonTransformAndReturnOldOne(g2d);
	}

	void checkSelectionHoleAreStillInBook() {
		jvirtualbookcomponent.checkSelectionHoleAreStillInBook();
	}

	public double getXfactor() {
		return jvirtualbookcomponent.getXfactor();
	}

	public void setXoffset(double x) {
		jvirtualbookcomponent.setXoffset(x);
		touchBook();
		triggerScrolls = true;
		hscroll.setValue((int) (x / increment));
		hscroll.revalidate();
	}

	public void setYoffset(double offsety) {
		jvirtualbookcomponent.setYoffset(offsety);
		touchBook();
		triggerScrolls = true;
		vscroll.setValue((int) (offsety / increment));
		vscroll.revalidate();
	}

	public double getXoffset() {
		return jvirtualbookcomponent.getXoffset();
	}

	public double getYoffset() {
		return jvirtualbookcomponent.getYoffset();
	}

	public double getMargin() {
		return jvirtualbookcomponent.getMargin();
	}

	public void setMargin(double margin) {
		jvirtualbookcomponent.setMargin(margin);
	}

	// ///////////////////////////////////////////////////////////////////
	// gestion des layers

	/**
	 * Add a layer
	 */
	public void addLayer(VirtualBookComponentLayer layer) {
		jvirtualbookcomponent.addLayer(layer);
	}

	/**
	 * Get the layer array managed by this component
	 * 
	 * @return the array of layer
	 */
	public VirtualBookComponentLayer[] getLayers() {
		return jvirtualbookcomponent.getLayers();
	}

	/**
	 * Find a layer from it's display name (interface
	 * VirtualBookComponentLayerName)
	 * 
	 * @param name
	 * @return
	 */
	public VirtualBookComponentLayer findLayerByName(String name) {
		return jvirtualbookcomponent.findLayerByName(name);
	}

	/**
	 * Find a layer by it's java class type
	 * 
	 * @param clazz
	 *            the class the layer should derive from
	 * @return the layer or null if not found
	 */
	public VirtualBookComponentLayer findLayerByClass(Class clazz) {
		return jvirtualbookcomponent.findLayerByClass(clazz);
	}

	/**
	 * Add Or Replace the given layer with the same name
	 * 
	 * @param layer
	 */
	public void addOrReplaceLayer(VirtualBookComponentLayer layer) {
		jvirtualbookcomponent.addOrReplaceLayer(layer);
	}

	/**
	 * remove this layer from managed collection
	 * 
	 * @param layer
	 *            the layer to remove
	 */
	public void removeLayer(VirtualBookComponentLayer layer) {
		jvirtualbookcomponent.removeLayer(layer);
	}

	// ////////////////////////////////////////////////////////////////////
	// dynamic

	/**
	 * Get the display time ... in nanos
	 * 
	 * @return get the time benchmark of the display
	 */
	public long getDisplayNanos() {
		return jvirtualbookcomponent.getDisplayNanos();
	}

	/**
	 * set the fast drawing mode for the component
	 */
	public void setUseFastDrawing(boolean fastdrawing) {
		jvirtualbookcomponent.setUseFastDrawing(fastdrawing);
	}

	/**
	 * is this component use a fast drawing
	 */
	public boolean isUseFastDrawing() {
		return jvirtualbookcomponent.isUseFastDrawing();
	}

	// Multi Line ToolTip creation

	@Override
	public JToolTip createToolTip() {
		MultiLineToolTip tip = new MultiLineToolTip();
		tip.setComponent(this);
		return tip;
	}

	class MultiLineToolTip extends JToolTip {
		public MultiLineToolTip() {
			setUI(new MultiLineToolTipUI());
		}
	}

	class MultiLineToolTipUI extends MetalToolTipUI {
		private String[] strs;

		private int maxWidth = 0;

		public void paint(Graphics g, JComponent c) {
			FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(g.getFont());
			Dimension size = c.getSize();
			g.setColor(c.getBackground());
			g.fillRect(0, 0, size.width, size.height);
			g.setColor(c.getForeground());
			if (strs != null) {
				for (int i = 0; i < strs.length; i++) {
					g.drawString(strs[i], 3, (metrics.getHeight()) * (i + 1));
				}
			}
		}

		public Dimension getPreferredSize(JComponent c) {
			FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(c.getFont());
			String tipText = ((JToolTip) c).getTipText();
			if (tipText == null) {
				tipText = "";
			}
			BufferedReader br = new BufferedReader(new StringReader(tipText));
			String line;
			int maxWidth = 0;
			Vector v = new Vector();
			try {
				while ((line = br.readLine()) != null) {
					int width = SwingUtilities.computeStringWidth(metrics, line);
					maxWidth = (maxWidth < width) ? width : maxWidth;
					v.addElement(line);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			int lines = v.size();
			if (lines < 1) {
				strs = null;
				lines = 1;
			} else {
				strs = new String[lines];
				int i = 0;
				for (Enumeration e = v.elements(); e.hasMoreElements(); i++) {
					strs[i] = (String) e.nextElement();
				}
			}
			int height = metrics.getHeight() * lines;
			this.maxWidth = maxWidth;
			return new Dimension(maxWidth + 12, height + 8);
		}
	}

	/**
	 * convert the x coordinate from the book space to the screen space
	 * 
	 * @param x
	 *            the x
	 * @return the converted coordinate
	 */
	public int convertCartonToScreenX(double x) {
		return jvirtualbookcomponent.convertCartonToScreenX(x);
	}

	/**
	 * convert the y coordinate from the book space to the screen space
	 * 
	 * @param y
	 *            the y
	 * @return the converted coordinate
	 */
	public int convertCartonToScreenY(double y) {
		return jvirtualbookcomponent.convertCartonToScreenY(y);
	}

	/**
	 * Center the visual element
	 * 
	 * @param x
	 *            the center in mm
	 * @param y
	 *            the center in mm
	 */
	public void centerAtCartonPosition(double x, double y) {

		// center the zoom,

		int componentwidth = jvirtualbookcomponent.getWidth();
		int componentheight = jvirtualbookcomponent.getHeight();

		double componentMMwidth = pixelToMM(componentwidth);
		double componentMMheight = pixelToMM(componentheight);

		double startxmm = x - componentMMwidth / 2;
		double startymm = y - componentMMheight / 2;

		setXoffset(startxmm);

		setYoffset(startymm);

	}

	/**
	 * Add Listener
	 * 
	 * @param l
	 * @since 2012.6
	 */
	public void addVirtualBookComponentLayersListener(VirtualBookComponentLayersListener l) {
		jvirtualbookcomponent.addVirtualBookComponentLayersListener(l);
	}

	/**
	 * Remove the listener
	 * 
	 * @param l
	 * @Since 2012.6
	 */
	public void removeVirtualBookComponentLayersListener(VirtualBookComponentLayersListener l) {
		jvirtualbookcomponent.removeVirtualBookComponentLayersListener(l);
	}

	public void activatePanOnMiddleButton() {

		class MouseEvents extends MouseAdapter implements MouseMotionListener {

			private boolean pan = false;

			private double origineX;

			private int posx;

			public void mouseMoved(MouseEvent e) {
				if (logger.isDebugEnabled())
					logger.debug("pan move");
			}

			public void mouseDragged(MouseEvent e) {
				logger.debug("mouse dragged :" + e); //$NON-NLS-1$

				if (pan) {
					int x = e.getX();

					int delta = posx - x;

					logger.debug("pan to :"); //$NON-NLS-1$

					setXoffset(origineX + pixelToMM(delta));

					repaint();

				}
			}

			@Override
			public void mousePressed(MouseEvent e) {

				requestFocusInWindow();

				logger.debug("mouse pressed :" + e.getButton()); //$NON-NLS-1$

				if (e.getButton() == MouseEvent.BUTTON2) {
					posx = e.getX();

					origineX = getXoffset();

					pan = true;
					logger.debug("pan started"); //$NON-NLS-1$

					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

				} else {

					// normal click on the book

				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {

				pan = false;
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

				if (e.getButton() == MouseEvent.BUTTON1) {
					repaint();
				}
			}

		}

		// pan on the book
		MouseEvents m = new MouseEvents();
		addMouseListener(m);
		addMouseMotionListener(m);

	}
	
	///////////////////////////////////////////////////////////////
	// transparency

	public void setHoleTransparency(float transparency) {
		jvirtualbookcomponent.setHolesTransparency(transparency);
	}

	public float getHoleTransparency() {
		return jvirtualbookcomponent.getHolesTransparency();
	}

}
