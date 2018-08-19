package org.barrelorgandiscovery.gui.aedit;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalToolTipUI;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.ascale.ScaleComponent;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Fragment;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.Position;
import org.barrelorgandiscovery.virtualbook.Region;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.VirtualBookMetadata;
import org.barrelorgandiscovery.virtualbook.rendering.VirtualBookRendering;

/**
 * Swing component for displaying the virtual book
 * 
 * @author Freydiere Patrice
 * 
 */
public class JVirtualBookComponent extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7063042825130386273L;

	private static Logger logger = Logger.getLogger(JVirtualBookComponent.class);

	/**
	 * Virtual Book
	 */
	private VirtualBook virtualbook = null;

	/**
	 * Liste des couches supplémentaires à afficher dans le composant
	 */
	private ArrayList<VirtualBookComponentLayer> layers = new ArrayList<VirtualBookComponentLayer>();

	/**
	 * offset d'affichage de la vue du carton en mm
	 */
	private double xoffset = 0;

	/**
	 * offset d'affichage de la vue du carton en mm
	 */
	private double yoffset = 0;

	/**
	 * Facteur d'échelle d'affichage du carton (en x et y)
	 */
	private double xfactor = 1.0;

	/**
	 * Marge pour l'affichage du carton (en mm)
	 */
	private double margin = 2.0;

	/**
	 * pas des règles
	 */
	private long rules = 1000000; // every seconds

	/**
	 * position a mettre en surbrillance (en mm par rapport au debut du carton)
	 */
	private double hightlight = -1;

	/**
	 * Début de la sélection, ou -1 s'il n'y a pas de sélection
	 */
	private long selstart = -1;

	/**
	 * Longueur de la sélection
	 */
	private long sellength = 0;

	private Color guide_color = new Color(200, 200, 200);

	private HashSet<Hole> selection = new HashSet<Hole>();

	private TexturePaint cartontrame = null;

	private boolean useFastDrawing = false;

	private VirtualBookRendering rendering = null;

	private RenderingHints renderHints = null;

	private boolean displayTracksLimits = true;

	private int screendpi;

	private HashSet<Hole> currentlyDisplayedSet = new HashSet<Hole>();

	private float transparency = 0.0f;

	public void setHolesTransparency(float transparency) {
		this.transparency = transparency;
	}

	public float getHolesTransparency() {
		return transparency;
	}

	public boolean isDisplayTracksLimits() {
		return displayTracksLimits;
	}

	public void setDisplayTracksLimits(boolean displayTracksLimits) {
		this.displayTracksLimits = displayTracksLimits;
	}

	public JVirtualBookComponent() {
		super();

		Toolkit kit = Toolkit.getDefaultToolkit();
		screendpi = kit.getScreenResolution();

		try {

		} catch (Exception ex) {
			logger.error("creating images", ex); //$NON-NLS-1$
		}
		renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

	}

	public VirtualBook getVirtualBook() {
		return virtualbook;
	}

	public void setVirtualBook(VirtualBook virtualbook) {

		this.virtualbook = virtualbook;

		if (this.virtualbook != null) {
			this.rendering = virtualbook.getScale().getRendering();
			if (this.rendering == null)
				this.rendering = new VirtualBookRendering();

			try {
				BufferedImage bi = ImageIO.read(rendering.getBackgroundImage());
				cartontrame = new TexturePaint(bi, new Rectangle2D.Double(0, 0, bi.getWidth(), bi.getHeight()));
			} catch (Exception ex) {
				logger.error("setcarton", ex); //$NON-NLS-1$
				cartontrame = null;
			}
		} else {
			this.rendering = null;
		}
	}

	public boolean hasCarton() {
		return virtualbook != null;
	}

	// /////////////////////////////////////////////////////////////
	// routines de dessin

	private void paintNote(Graphics g, Hole n, boolean isselected) {

		assert virtualbook != null;

		Scale gamme = virtualbook.getScale();

		int debut = convertCartonToScreenX(((double) n.getTimestamp()) / 1000000 * gamme.getSpeed());
		int width = MmToPixel(((double) n.getTimeLength()) / 1000000 * gamme.getSpeed());

		double ymm = (n.getTrack() * gamme.getIntertrackHeight() + gamme.getFirstTrackAxis()
				- gamme.getTrackWidth() / 2);

		double heightmm = gamme.getTrackWidth();

		if (gamme.isPreferredViewedInversed()) {
			ymm = gamme.getWidth() - ymm - heightmm;
		}

		int y = convertCartonToScreenY(ymm);
		int height = MmToPixel(heightmm);

		rendering.renderHole((Graphics2D) g, debut, y, width, height, isselected);

	}

	private AtomicLong displaynanos = new AtomicLong(-1);

	/**
	 * Get the display time ... in nanos, used for debugging purpose to optimize
	 * the displays
	 * 
	 * @return get the time benchmark of the display
	 */
	public long getDisplayNanos() {
		return displaynanos.get();
	}

	public AffineTransform addCartonTransformAndReturnOldOne(Graphics2D g2d) {
		AffineTransform old = g2d.getTransform();

		AffineTransform t = AffineTransform.getTranslateInstance(-getXoffset() + getMargin(),
				-getYoffset() + getMargin());
		double f = 10 / pixelToMm(10);
		AffineTransform scale = AffineTransform.getScaleInstance(f, f);
		scale.concatenate(t);
		AffineTransform clone = (AffineTransform) old.clone();
		clone.concatenate(scale);
		g2d.setTransform(clone);

		return old;

	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		paintTheComponent((Graphics2D) g);

	}

	/**
	 * @param g2d
	 * @param partiecartonaffichee
	 */
	protected void paintCover(Graphics2D g2d, Rectangle2D partiecartonaffichee) {
		if (partiecartonaffichee.getX() <= 0) {
			VirtualBookMetadata metadata = virtualbook.getMetadata();
			if (metadata != null) {

				int screenBookWidth = MmToPixel(virtualbook.getScale().getWidth());

				Image c = metadata.getCoverForDisplay(screenBookWidth);
				if (c != null) {

					int imagewidth = c.getWidth(null);
					int imageheight = c.getHeight(null);

					int originX = convertCartonToScreenX(0);
					int originY = convertCartonToScreenY(0);

					// screenBookWidth = image width

					double screenImageHeight = (1.0 * imageheight / (imagewidth) * screenBookWidth);

					AffineTransform t = AffineTransform.getTranslateInstance(1.0 * originX - screenImageHeight,
							1.0 * originY + screenBookWidth);
					t.concatenate(AffineTransform.getScaleInstance((1.0 * screenBookWidth) / imagewidth,
							(1.0 * screenBookWidth) / imagewidth));
					t.concatenate(AffineTransform.getRotateInstance(-Math.PI / 2));

					g2d.drawImage(c, t, null);

				}

			}

		}
	}

	/**
	 * convert timestamp to mm distance
	 * 
	 * @param timestamp
	 *            the timestamp
	 * @return the mm distance from the start of the book
	 */
	public double timestampToMM(long timestamp) {
		return timeToMM(timestamp);
	}

	/**
	 * convert a time in microseconds in mm
	 * 
	 * @param time
	 *            the time
	 * @return the mm distance representing the time
	 */
	public double timeToMM(long time) {
		return ((double) time) / 1000000 * virtualbook.getScale().getSpeed();
	}

	/**
	 * get the time associated to mm distance...
	 * 
	 * @param mm
	 *            distance in mm
	 * @return the time in microseconds
	 */
	public long MMToTime(double mm) {
		return (long) ((mm / virtualbook.getScale().getSpeed()) * 1000000);
	}

	public Position queryWithExtraMargin(int x, int y) {

		if (virtualbook == null)
			return null;

		int piste;
		Scale gamme = virtualbook.getScale();

		if (gamme.isPreferredViewedInversed()) {
			piste = (int) (((gamme.getWidth() - convertScreenYToCarton(y)) - gamme.getFirstTrackAxis()
					+ gamme.getIntertrackHeight() / 2) / gamme.getIntertrackHeight());
		} else {
			piste = (int) ((convertScreenYToCarton(y) - gamme.getFirstTrackAxis() + gamme.getIntertrackHeight() / 2)
					/ gamme.getIntertrackHeight());
		}

		long pos = (long) (convertScreenXToCarton(x) / gamme.getSpeed() * 1000000);

		Position p = new Position();
		p.track = piste;
		p.position = pos;

		return p;

	}

	/**
	 * Cette fonction retourne la position du carton clickée dans le composant,
	 * tiens compte de l'orientation du carton dans le composant
	 * 
	 * @param x
	 *            la position x écran (en pixel)
	 * @param y
	 *            la position y écran (en pixel)
	 * @return un objet Position si celle ci a été trouvé, ou null sinon
	 */
	public Position query(int x, int y) {

		Position position = queryWithExtraMargin(x, y);
		if (position == null)
			return null;

		assert position != null;
		int track = position.track;
		Scale gamme = virtualbook.getScale();

		if (track < 0 || track >= gamme.getTrackNb())
			return null;

		return position;
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		if (virtualbook == null)
			return null;

		StringBuffer toolTip = new StringBuffer();

		Position pos = query(event.getX(), event.getY());
		if (pos != null) {
			int piste = pos.track;
			AbstractTrackDef nd = virtualbook.getScale().getTracksDefinition()[piste];
			toolTip.append(Messages.getString("JVirtualBookComponent.3") + " " + (piste + 1) + " : " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
																										// //$NON-NLS-4$
					+ ScaleComponent.getTrackLibelle(nd));
		}

		// look for every layer info ....

		double cartonx = convertScreenXToCarton(event.getX());
		double cartony = convertScreenYToCarton(event.getY());

		for (Iterator iterator = layers.iterator(); iterator.hasNext();) {
			VirtualBookComponentLayer l = (VirtualBookComponentLayer) iterator.next();
			if (l instanceof ToolTipVirtualBookComponentLayer) {
				ToolTipVirtualBookComponentLayer tl = (ToolTipVirtualBookComponentLayer) l;

				String toolTipInfo = tl.getToolTipInfo(cartonx, cartony);
				if (toolTipInfo != null && toolTipInfo.length() > 0) {
					toolTip.append("\n").append(toolTipInfo);
				}

			}
		}

		if (toolTip.length() > 0)
			return toolTip.toString();

		return null;
	}

	// //////////////////////////////////////////////////////////
	// fonctions de manipulation de la sélection

	/**
	 * clear selection
	 */
	public void clearSelection() {
		selection.clear();
	}

	/**
	 * has a selection ?
	 * 
	 * @return
	 */
	public boolean hasSelection() {
		return !selection.isEmpty();
	}

	/**
	 * add hole to selection
	 * 
	 * @param n
	 */
	public void addToSelection(Hole n) {
		selection.add(n);
	}

	/**
	 * remove from selection
	 * 
	 * @param n
	 */
	public void removeFromSelection(Hole n) {
		selection.remove(n);
	}

	void checkSelectionHoleAreStillInBook() {
		VirtualBook vb = getVirtualBook();
		if (vb == null) {
			clearSelection();
			return;
		}

		ArrayList<Hole> orderedHolesCopy = vb.getOrderedHolesCopy();
		TreeSet<Hole> bookHoles = new TreeSet<Hole>(orderedHolesCopy);
		selection.retainAll(bookHoles);
	}

	/**
	 * Get a copy of the selection
	 * 
	 * @return
	 */
	public Set<Hole> getSelectionCopy() {
		return new HashSet<Hole>(selection);
	}

	/**
	 * Cette fonction regarde si une note est sélectionnée
	 * 
	 * @param n
	 *            la note
	 * @return
	 */
	private boolean isSelected(Hole n) {
		if (selection.contains(n))
			return true;
		return false;
	}

	// //////////////////////////////////////////////////////////////////
	// fonction de sélection

	/**
	 * définit la sélection affichée
	 * 
	 * @param start
	 *            début de la sélection (en microsecondes)
	 * @param length
	 *            longueur de la sélection (en microsecondes)
	 */
	public void setBlockSelection(long start, long length) {
		selstart = start;
		sellength = length;
	}

	public void clearBlockSelection() {
		selstart = -1;
		sellength = 0;
	}

	/**
	 * get the selection
	 * 
	 * @return the selection objet or null if there is none
	 */
	public Fragment getBlockSelection() {
		if (selstart == -1 || sellength < 0)
			return null;
		Fragment s = new Fragment();
		s.start = selstart;
		s.length = sellength;
		return s;
	}

	// ///////////////////////////////////////////////////////////
	// gestion de l'échelle de visualisation du carton

	/*
	 * public double getXscale() { return xscale; }
	 * 
	 * public void setXscale(double xscale) { this.xscale = xscale; }
	 * 
	 * public double getYscale() { return yscale; }
	 * 
	 * public void setYscale(double yscale) { this.yscale = yscale; }
	 */

	/**
	 * Défini la position du curseur (en mm par rapport au début du carton)
	 * 
	 * @param position
	 */
	public void setHightlight(double position) {
		hightlight = position;

		int tmp = convertCartonToScreenX(position);
		if (tmp < 0 || tmp > getBounds().width) {
			setXoffset(position);
		}
		repaint();
	}

	/**
	 * Supprime l'affichage du curseur
	 */
	public void clearHightlight() {
		hightlight = -1;
	}

	/**
	 * ask in an hightlight is defined
	 * 
	 * @return true if defined
	 */
	public boolean hasHightlight() {
		return hightlight != -1;
	}

	/**
	 * get the Hightlight position (in mm) from the start
	 * 
	 * @return get the Hightlight position (in mm) from the start
	 */
	public double getHightlight() {
		return hightlight;
	}

	// ///////////////////////////////////////////////////////////////////
	// outils de conversion de coordonnées

	/**
	 * convert the x coordinate from the book space to the screen space
	 * 
	 * @param x
	 *            the x
	 * @return the converted coordinate
	 */
	public int convertCartonToScreenX(double x) {
		double d = -xoffset + margin + x;
		return MmToPixel(d);
	}

	/**
	 * convert the y coordinate from the book space to the screen space
	 * 
	 * @param y
	 *            the y
	 * @return the converted coordinate
	 */
	public int convertCartonToScreenY(double y) {
		double d = -yoffset + margin + y;
		return MmToPixel(d);
	}

	/**
	 * convert the x coordinate from the screen space to the book space
	 * 
	 * @param x
	 *            the x
	 * @return the converted coordinate
	 */
	public double convertScreenXToCarton(int x) {
		double xmm = pixelToMm(x);

		return -margin + xoffset + xmm;
	}

	/**
	 * convert the y coordinate from the screen space to the book space
	 * 
	 * @param y
	 *            the y
	 * @return the converted coordinate
	 */
	public double convertScreenYToCarton(int y) {
		double ymm = pixelToMm(y);
		return -margin + yoffset + ymm;
	}

	/**
	 * 
	 * convert a pixel to mm distance
	 * 
	 * 
	 * @param x
	 *            the number of pixels
	 * @return the distance in mm
	 */
	public double pixelToMm(int x) {
		Toolkit kit = Toolkit.getDefaultToolkit();
		int screendpi = kit.getScreenResolution();
		double pts_par_mm = (screendpi * 1.0) / 25.4;
		return ((double) x * xfactor) / pts_par_mm;
	}

	/**
	 * Convert a mm distance to pixel
	 * 
	 * @param x
	 *            the mm distance
	 * @return the converted pixels number
	 */
	public int MmToPixel(double x) {

		double pts_par_mm = (screendpi * 1.0) / 25.4;
		return (int) (x / xfactor * pts_par_mm);
	}

	/**
	 * get the track axis in mm, this take into account the book orientation
	 */
	public double trackToMM(int track) {

		Scale s = virtualbook.getScale();

		if (s.isPreferredViewedInversed()) {
			return s.getWidth() - s.getFirstTrackAxis() - (track * s.getIntertrackHeight());
		} else {
			return s.getFirstTrackAxis() + (track * s.getIntertrackHeight());
		}

	}

	/**
	 * get the X factory
	 * 
	 * @return xfactor
	 */
	public double getXfactor() {
		return xfactor;
	}

	/**
	 * set the X factory
	 * 
	 * @param xfactor
	 *            the xfactor
	 * 
	 */
	public void setXfactor(double xfactor) {
		this.xfactor = xfactor;
	}

	public double getXoffset() {
		return xoffset;
	}

	public void setXoffset(double xoffset) {
		this.xoffset = xoffset;
	}

	public double getYoffset() {
		return yoffset;
	}

	public void setYoffset(double yoffset) {
		this.yoffset = yoffset;
	}

	public double getMargin() {
		return this.margin;
	}

	public void setMargin(double margin) {
		this.margin = margin;
	}

	// Gestion des couches ...

	/**
	 * Add Layer in the component
	 */
	public void addLayer(VirtualBookComponentLayer layer) {
		if (layer != null) {
			layers.add(layer);
			fireLayerAdded(layer);
		}
		repaint();
	}

	/**
	 * Add or replace layer with a displayname (supporting
	 * VirtualBookComponentLayerName)
	 * 
	 * @param layer
	 */
	public void addOrReplaceLayer(VirtualBookComponentLayer layer) {

		if (layer instanceof VirtualBookComponentLayerName) {
			VirtualBookComponentLayer oldlayer = findLayerByName(
					((VirtualBookComponentLayerName) layer).getDisplayName());
			if (oldlayer != null)
				removeLayer(oldlayer);
		}
		addLayer(layer);
	}

	/**
	 * Return the layer by its name, the layer must implements the
	 * VirtualBookComponentLayerName
	 * 
	 * @param name
	 * @return the layer , or null if not found
	 */
	public VirtualBookComponentLayer findLayerByName(String name) {
		if (name == null)
			return null;

		VirtualBookComponentLayer[] layers = getLayers();
		for (int i = 0; i < layers.length; i++) {
			VirtualBookComponentLayer virtualBookComponentLayer = layers[i];
			if (virtualBookComponentLayer instanceof VirtualBookComponentLayerName) {
				VirtualBookComponentLayerName vbn = (VirtualBookComponentLayerName) virtualBookComponentLayer;
				if (name.equalsIgnoreCase(vbn.getDisplayName()))
					return virtualBookComponentLayer;
			}
		}

		return null;

	}

	/**
	 * Find the first layer by its java class
	 * 
	 * @param className
	 * @return the layer associated to this class, or null if not found
	 */
	public VirtualBookComponentLayer findLayerByClass(Class className) {
		if (className == null)
			return null;

		VirtualBookComponentLayer[] layers = getLayers();
		for (int i = 0; i < layers.length; i++) {
			VirtualBookComponentLayer virtualBookComponentLayer = layers[i];
			if (virtualBookComponentLayer instanceof VirtualBookComponentLayerName) {
				VirtualBookComponentLayerName vbn = (VirtualBookComponentLayerName) virtualBookComponentLayer;
				if (className.isAssignableFrom(vbn.getClass()))
					return virtualBookComponentLayer;
			}
		}

		return null;

	}

	/**
	 * Get All component Layer
	 * 
	 * @return
	 */
	public VirtualBookComponentLayer[] getLayers() {
		VirtualBookComponentLayer[] lyrs = new VirtualBookComponentLayer[layers.size()];
		lyrs = layers.toArray(lyrs);
		return lyrs;
	}

	/**
	 * remove the given layer from the component
	 * 
	 * @param layer
	 */
	public void removeLayer(VirtualBookComponentLayer layer) {
		if (layer == null)
			return;
		layers.remove(layer);
		fireLayerRemoved(layer);
	}

	public void fitToComponentSize() {
		if (virtualbook == null)
			return;

		logger.debug("fitToComponentSize"); //$NON-NLS-1$

		Dimension r = getSize();

		if (r != null && r.getHeight() == 0 && r.getWidth() == 0) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					fitToComponentSize();
				}
			});
		} else {

			Toolkit kit = Toolkit.getDefaultToolkit();
			int screendpi = kit.getScreenResolution();
			double pts_par_mm = (screendpi * 1.0) / 25.4;
			double sizeinmm = ((double) r.height) / pts_par_mm;

			logger.debug("size of the component" + r); //$NON-NLS-1$
			logger.debug("size in mm of the height of the component " + sizeinmm); //$NON-NLS-1$

			double nicefactor = 1.0;
			double bookWidthWithMargins = virtualbook.getScale().getWidth() + 2 * margin;
			double newfactor = (bookWidthWithMargins / (sizeinmm * nicefactor));
			logger.debug("redefine XFactor " + newfactor); //$NON-NLS-1$
			setXfactor(newfactor);
			setYoffset(0);

			repaint();
		}
	}

	public boolean isUseFastDrawing() {
		return useFastDrawing;
	}

	public void setUseFastDrawing(boolean useFastDrawing) {
		this.useFastDrawing = useFastDrawing;
	}

	// Multi Line ToolTip creation

	@Override
	public JToolTip createToolTip() {
		MultiLineToolTip tip = new MultiLineToolTip();
		tip.setComponent(this);
		return tip;
	}

	class MultiLineToolTip extends JToolTip {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8052611852002309194L;

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
			return new Dimension(maxWidth + 6, height + 4);
		}
	}

	private Vector<VirtualBookComponentLayersListener> layersListener = new Vector<VirtualBookComponentLayersListener>();

	/**
	 * Fire layer added
	 * 
	 * @param layer
	 * @since 2012.6
	 */
	protected void fireLayerAdded(VirtualBookComponentLayer layer) {
		for (Iterator iterator = layersListener.iterator(); iterator.hasNext();) {
			VirtualBookComponentLayersListener l = (VirtualBookComponentLayersListener) iterator.next();
			try {
				l.layerAdded(layer);
			} catch (Throwable t) {
				logger.error("error in sending layerAdded :" + t.getMessage(), t);
			}
		}
		fireLayersChanged();
	}

	/**
	 * Fire the event Layer Removed
	 * 
	 * @param layer
	 * @since 2012.6
	 */
	protected void fireLayerRemoved(VirtualBookComponentLayer layer) {
		for (Iterator iterator = layersListener.iterator(); iterator.hasNext();) {
			VirtualBookComponentLayersListener l = (VirtualBookComponentLayersListener) iterator.next();
			try {
				l.layerRemoved(layer);
			} catch (Throwable t) {
				logger.error("error in sending layerRemoved :" + t.getMessage(), t);
			}
		}
		fireLayersChanged();
	}

	/**
	 * Fire the event layers changed
	 * 
	 * @since 2012.6
	 */
	protected void fireLayersChanged() {
		for (Iterator iterator = layersListener.iterator(); iterator.hasNext();) {
			VirtualBookComponentLayersListener l = (VirtualBookComponentLayersListener) iterator.next();
			try {
				l.layersChanged();
			} catch (Throwable t) {
				logger.error("error in sending layersChanged :" + t.getMessage(), t);
			}
		}
	}

	/**
	 * Add Listener
	 * 
	 * @param l
	 * @since 2012.6
	 */
	public void addVirtualBookComponentLayersListener(VirtualBookComponentLayersListener l) {
		if (l != null)
			layersListener.add(l);
	}

	/**
	 * Remove the listener
	 * 
	 * @param l
	 * @Since 2012.6
	 */
	public void removeVirtualBookComponentLayersListener(VirtualBookComponentLayersListener l) {
		if (l != null)
			layersListener.remove(l);
	}

	protected void paintTheComponent(Graphics2D g) {

		if (!hasCarton())
			return;

		g.setRenderingHints(renderHints);

		long start = System.nanoTime();

		g.setPaintMode();
		Color lastcolor = g.getColor();

		// récupération de la zone à afficher
		Rectangle rect = g.getClipBounds(new Rectangle());

		// Correction d'un bug de réaffichage
		rect.x -= 1;
		rect.y -= 1;
		rect.width += 2;
		rect.height += 2;

		// conversion de cette zone dans l'espace carton
		Rectangle2D partiecartonaffichee = new Rectangle2D.Double(convertScreenXToCarton(rect.x),
				convertScreenYToCarton(rect.y), pixelToMm(rect.width), pixelToMm(rect.height));

		Scale gamme = virtualbook.getScale();
		Rectangle2D totalitecarton = new Rectangle2D.Double(0, 0, timestampToMM(virtualbook.getLength()),
				gamme.getWidth());

		Rectangle2D cartonaffiche = totalitecarton.createIntersection(partiecartonaffichee);

		// couleur carton ...

		g.setColor(rendering.getDefaultBookColor());

		Paint lastpaint = g.getPaint();
		try {

			if (cartontrame != null && !useFastDrawing) {
				g.setPaint(cartontrame);
			}

			g.fillRect(convertCartonToScreenX(cartonaffiche.getX()), convertCartonToScreenY(cartonaffiche.getY()),
					MmToPixel(cartonaffiche.getWidth()), MmToPixel(cartonaffiche.getHeight()));

		} finally {
			g.setPaint(lastpaint);
		}

		// Draw Cover ..

		paintCover(g, partiecartonaffichee);

		// Affichage des différentes couches supplémentaires (background)

		for (Iterator<VirtualBookComponentLayer> itl = layers.iterator(); itl.hasNext();) {
			VirtualBookComponentLayer layer = null;
			try {
				layer = itl.next();

				if (layer.isVisible()) {
					if (layer instanceof VirtualBookComponentBackgroundLayer) {
						VirtualBookComponentBackgroundLayer vbcl = (VirtualBookComponentBackgroundLayer) layer;
						vbcl.drawBackground(g, this);
					}
				}
			} catch (Throwable t) {
				logger.error("error in displaying background layer :" + (layer != null ? layer.toString() : "") + " "
						+ t.getMessage(), t);
			}
		}

		Composite oldComposite = g.getComposite();
		try {

			if (transparency != 0.0f) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, transparency));
			}

			// draw holes

			int firsttrack;
			int lasttrack;

			if (gamme.isPreferredViewedInversed()) {

				firsttrack = (int) (((gamme.getWidth() - cartonaffiche.getY()) - gamme.getFirstTrackAxis()
						+ gamme.getIntertrackHeight() / 2) / gamme.getIntertrackHeight());

				lasttrack = (int) (((gamme.getWidth() - (cartonaffiche.getY() + cartonaffiche.getHeight()))
						- gamme.getFirstTrackAxis() + gamme.getIntertrackHeight() / 2) / gamme.getIntertrackHeight());

				int tmp = lasttrack;
				lasttrack = firsttrack;
				firsttrack = tmp;

			} else {
				firsttrack = (int) ((cartonaffiche.getY() - gamme.getFirstTrackAxis() + gamme.getIntertrackHeight() / 2)
						/ gamme.getIntertrackHeight());

				lasttrack = (int) ((cartonaffiche.getY() + cartonaffiche.getHeight() - gamme.getFirstTrackAxis()
						+ gamme.getIntertrackHeight() / 2) / gamme.getIntertrackHeight());
			}

			g.setColor(guide_color);

			if (firsttrack < 0)
				firsttrack = 0;

			if (lasttrack > gamme.getTrackNb() - 1)
				lasttrack = gamme.getTrackNb() - 1;

			if (displayTracksLimits) {

				int xorigin = convertCartonToScreenX(0);
				int xdebut = rect.x;
				if (xorigin > xdebut)
					xdebut = xorigin;

				for (int i = firsttrack; i <= lasttrack; i++) {

					int yscreen;

					if (gamme.isPreferredViewedInversed()) {
						yscreen = convertCartonToScreenY(gamme.getWidth() - (i * gamme.getIntertrackHeight()
								+ gamme.getFirstTrackAxis() - gamme.getIntertrackHeight() / 2));
					} else {
						yscreen = convertCartonToScreenY(i * gamme.getIntertrackHeight() + gamme.getFirstTrackAxis()
								- gamme.getIntertrackHeight() / 2);
					}
					g.drawLine(xdebut, yscreen, xdebut + rect.width, yscreen);
				}
				// borne haute
				int yscreen;

				if (gamme.isPreferredViewedInversed()) {
					yscreen = convertCartonToScreenY(gamme.getWidth() - (lasttrack * gamme.getIntertrackHeight()
							+ gamme.getFirstTrackAxis() + gamme.getIntertrackHeight() / 2));

				} else {
					yscreen = convertCartonToScreenY(lasttrack * gamme.getIntertrackHeight() + gamme.getFirstTrackAxis()
							+ gamme.getIntertrackHeight() / 2);
				}

				g.drawLine(xdebut, yscreen, xdebut + rect.width, yscreen);

			}

			g.setColor(Color.black);

			// dessin des éléments ...
			// recherche des notes ...
			Region r = new Region();

			r.start = (long) (cartonaffiche.getX() / gamme.getSpeed() * 1000000);
			r.end = (long) ((cartonaffiche.getX() + cartonaffiche.getWidth()) / gamme.getSpeed() * 1000000);

			r.beginningtrack = firsttrack;
			r.endtrack = lasttrack;

			long startperffind = System.nanoTime();
			currentlyDisplayedSet.clear();

			virtualbook.findHoles(r.start, r.end - r.start, r.beginningtrack, r.endtrack, currentlyDisplayedSet);
			// logger.debug("PERF find :" + ((System.nanoTime() -
			// startperffind))
			// + " ns");

			Iterator<Hole> it = currentlyDisplayedSet.iterator();
			while (it.hasNext()) {
				Hole n = it.next();
				paintNote(g, n, false);
			}

			// draw selection
			for (Hole selh : selection) {
				if (selh.intersect(r)) {
					paintNote(g, selh, true);
				}
			}

			// Affichage des différentes couches supplémentaires

			for (Iterator<VirtualBookComponentLayer> itl = layers.iterator(); itl.hasNext();) {
				VirtualBookComponentLayer layer = null;
				try {
					layer = itl.next();
					if (layer.isVisible())
						layer.draw(g, this);
				} catch (Throwable t) {
					logger.error("error in displaying layer :" + (layer != null ? layer.toString() : "") + " "
							+ t.getMessage(), t);
				}
			}

			//
			// // dessin des règles
			// g.setColor(Color.red);
			//
			// long s = ((long) r.start) / rules * rules;
			// while (s < r.end) {
			// g.drawLine((int) ((s - position) * xscale), rect.y,
			// (int) ((s - position) * xscale), rect.y + rect.height);
			// s += rules;
			// }
			//
			// dessin de la sélection

			if (selstart > 0) {
				g.setXORMode(new Color(128, 128, 128));

				int length = MmToPixel(timeToMM(sellength));

				if (length == 0)
					length = 1;
				g.fillRect(MmToPixel(timeToMM(selstart) - xoffset), rect.y, length, rect.height);
			}

		} finally {
			g.setComposite(oldComposite);
		}

		//
		// dessin du hightlight

		int tmph = convertCartonToScreenX(hightlight);

		if (tmph <= (rect.x + rect.width) && tmph >= rect.x) {
			g.setColor(Color.blue);
			g.drawLine(tmph, rect.y, tmph, rect.y + rect.height);
		}

		// restauration de l'ancienne couleur
		g.setColor(lastcolor);

		displaynanos.set(System.nanoTime() - start);
		// if (logger.isDebugEnabled())
		// logger.debug("paint " + displaynanos + " ns");

	}
}
