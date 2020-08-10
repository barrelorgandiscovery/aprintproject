package org.barrelorgandiscovery.extensionsng.perfo.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayer;
import org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayerName;
import org.barrelorgandiscovery.optimizers.model.CutLine;
import org.barrelorgandiscovery.optimizers.model.Extent;
import org.barrelorgandiscovery.optimizers.model.GroupedCutLine;
import org.barrelorgandiscovery.optimizers.model.OptimizedObject;
import org.barrelorgandiscovery.optimizers.model.Punch;
import org.barrelorgandiscovery.scale.Scale;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.quadtree.Quadtree;

/**
 * Layer for displaying the punch and lazer cut vision of a book
 * 
 * @author pfreydiere
 */
public class PunchLayer implements VirtualBookComponentLayer, VirtualBookComponentLayerName {

	private static Logger logger = Logger.getLogger(PunchLayer.class);

	private OptimizedObject[] punches = null;

	// quadTree
	private Quadtree quadTree = new Quadtree();

	/**
	 * this linked the Optimized object to command index
	 */
	private HashMap<OptimizedObject, Integer> indexToInteger = null;

	private boolean visible = true;

	private int origin = 0;

	public final static int ORIGIN_LEFT_UPPER = 0;
	public final static int ORIGIN_LEFT_MIDDLE = 1;
	public final static int ORIGIN_CENTER = 2;

	public PunchLayer() {

	}

	public void setOrigin(int origin) {
		this.origin = origin;
	}

	public int getOrigin() {
		return this.origin;
	}

	public void draw(Graphics g, JVirtualBookComponent jcarton) {

		// Dessin des punches !!
		if (!visible)
			return;

		OptimizedObject[] localpunches = punches;

		if (localpunches == null || jcarton.getVirtualBook() == null)
			return;

		Graphics2D g2d = (Graphics2D) g;
		Composite oldcomposite = g2d.getComposite();
		Stroke oldstroke = g2d.getStroke();
		try {

			Composite transparent = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.2f);

			// Composite mediumtransparent =
			// AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.6f);

			// Stroke swidth = new BasicStroke(2.0f);

			g.setColor(Color.BLUE);

			Rectangle rect = g.getClipBounds();

			Scale scale = jcarton.getVirtualBook().getScale();
			boolean isViewInverted = scale.isPreferredViewedInversed();

			Rectangle2D.Double rectcarton;

			if (isViewInverted) {
				rectcarton = new Rectangle2D.Double(jcarton.convertScreenXToCarton(rect.x),
						jcarton.convertScreenYToCarton(rect.y), jcarton.pixelToMm(rect.width),
						jcarton.pixelToMm(rect.height));
			} else {
				rectcarton = new Rectangle2D.Double(jcarton.convertScreenXToCarton(rect.x),
						scale.getWidth() - jcarton.convertScreenYToCarton(rect.y), jcarton.pixelToMm(rect.width),
						jcarton.pixelToMm(rect.height));
			}

			List<OptimizedObject> listOfOptimizedObjectsIntersectingRectangle = searchForPunches(rectcarton);

			drawOptimizedObjects(g, jcarton, localpunches, g2d, transparent, rect,
					listOfOptimizedObjectsIntersectingRectangle);

		} finally

		{
			g2d.setComposite(oldcomposite);
			g2d.setStroke(oldstroke);
		}
	}

	private void drawOptimizedObjects(Graphics g, JVirtualBookComponent jcarton, OptimizedObject[] localpunches,
			Graphics2D g2d, Composite transparent, Rectangle rect, Collection<OptimizedObject> subSet) {

		Scale scale = jcarton.getVirtualBook().getScale();
		boolean isPreferredViewInverted = scale.isPreferredViewedInversed();

		for (Iterator<OptimizedObject> iterator = subSet.iterator(); iterator.hasNext();) {
			OptimizedObject current = iterator.next();

			int i = indexToInteger.get(current);

			if (current instanceof Punch) {

				Punch currentPunch = (Punch) current;
				int x = jcarton.convertCartonToScreenX(currentPunch.x);
				int y = jcarton.convertCartonToScreenY(
						(isPreferredViewInverted ? scale.getWidth() - currentPunch.y : currentPunch.y));

				// if (rectcarton.contains(p.x, p.y)) {

				if (punchType == 0) // square ...
				{
					if (!Double.isNaN(punchHeight) && !Double.isNaN(punchWidth)) {

						// dessin du rectangle ...
						int width = jcarton.MmToPixel(punchWidth);
						int height = jcarton.MmToPixel(punchHeight);
						Color old = g.getColor();
						try {

							Paint oldpaint = g2d.getPaint();
							// Stroke olds = g2d.getStroke();

							// Composite current = g2d.getComposite();

							// g2d.setComposite(mediumtransparent);
							// g2d.setStroke(swidth);

							int ys = y;
							int xs = x;

							switch (origin) {
							case ORIGIN_LEFT_MIDDLE:
								ys = y - height / 2;
								break;
							case ORIGIN_CENTER:
								ys = y - height / 2;
								xs = x - width / 2;
								break;
							}

							g2d.setPaint(Color.gray);
							g.fillRect(xs, ys, width, height);
							g.setColor(Color.black);
							g.drawRect(xs, ys, width, height);

							g2d.setPaint(oldpaint);

						} finally {
							g.setColor(old);
						}

					} else {
						g.drawRect(x, y, 2, 2);
					}
				} else {
					logger.debug("punch type not supported ...");
				}

				// draw lines between elements

			} else if (current instanceof CutLine) {

				CutLine cl = (CutLine) current;

				drawCutLine(g, jcarton, g2d, scale, isPreferredViewInverted, cl);
			} else if (current instanceof GroupedCutLine) {

				for (CutLine l : ((GroupedCutLine) current).getLinesByRefs()) {
					drawCutLine(g, jcarton, g2d, scale, isPreferredViewInverted, l);
				}

			}

			if (i < localpunches.length - 1) {
				OptimizedObject nextOne = localpunches[i + 1];

				int lastX = jcarton.convertCartonToScreenX(current.lastX());
				int lastY = jcarton.convertCartonToScreenY(
						(isPreferredViewInverted ? scale.getWidth() - current.lastY() : current.lastY()));

				int x2 = jcarton.convertCartonToScreenX(nextOne.firstX());
				int y2 = jcarton.convertCartonToScreenY(
						(isPreferredViewInverted ? scale.getWidth() - nextOne.firstY() : nextOne.firstY()));

				if (rect.intersectsLine(lastX, lastY, x2, y2)) {
					// g2d.setComposite(transparent);
					g.drawLine(lastX, lastY, x2, y2);
					// g2d.setComposite(oldcomposite);
				}

			}

			if (i > 1) {
				OptimizedObject previousOne = localpunches[i - 1];

				int firstX = jcarton.convertCartonToScreenX(current.firstX());
				int firstY = jcarton.convertCartonToScreenY(
						(isPreferredViewInverted ? scale.getWidth() - current.firstY() : current.firstY()));

				int x2 = jcarton.convertCartonToScreenX(previousOne.lastX());
				int y2 = jcarton.convertCartonToScreenY(
						(isPreferredViewInverted ? scale.getWidth() - previousOne.lastY() : previousOne.lastY()));

				if (rect.intersectsLine(firstX, firstY, x2, y2)) {
					// g2d.setComposite(transparent);
					g.drawLine(firstX, firstY, x2, y2);
					// g2d.setComposite(oldcomposite);
				}
			}

		}
	}

	private void drawCutLine(Graphics g, JVirtualBookComponent jcarton, Graphics2D g2d, Scale scale,
			boolean isPreferredViewInverted, CutLine cl) {
		int x1 = jcarton.convertCartonToScreenX(cl.x1);
		int y1 = jcarton.convertCartonToScreenY((isPreferredViewInverted ? scale.getWidth() - cl.y1 : cl.y1));

		int x2 = jcarton.convertCartonToScreenX(cl.x2);
		int y2 = jcarton.convertCartonToScreenY((isPreferredViewInverted ? scale.getWidth() - cl.y2 : cl.y2));

		Color old = g.getColor();
		try {
			g.setColor(Color.MAGENTA);
			g.drawLine(x1, y1, x2, y2);

		} finally {
			g2d.setColor(old);
		}
	}

	/**
	 * rectangle is in the book absolute reference
	 * @param rectcarton
	 * @return
	 */
	private List<OptimizedObject> searchForPunches(Rectangle2D.Double rectcarton) {

		Envelope searchEnvelope = new Envelope(rectcarton.x, rectcarton.x + rectcarton.width, rectcarton.y,
				rectcarton.y + rectcarton.height);
		return (List<OptimizedObject>) quadTree.queryAll();
		// return (List<OptimizedObject>) quadTree.query(searchEnvelope);
	}

	/**
	 * set the punches to display
	 * 
	 * @param optimizedObject
	 */
	public void setOptimizedObject(OptimizedObject[] optimizedObject) {

		if (optimizedObject == null) {
			this.quadTree = null;
			this.indexToInteger = null;

		} else {

			this.quadTree = new Quadtree();

			this.indexToInteger = new HashMap<OptimizedObject, Integer>();

		
			for (int i = 0; i < optimizedObject.length; i++) {
				OptimizedObject optimizedo = optimizedObject[i];
				if (optimizedo == null)
					continue;
				Extent extent = optimizedo.getExtent();
				assert extent != null;

				double y1 =  extent.ymin;

				double y2 = extent.ymax;

				Envelope env = new Envelope(extent.xmin, extent.xmax, Math.min(y1, y2), Math.max(y1, y2));

				this.quadTree.insert(env, optimizedo);
				this.indexToInteger.put(optimizedo, i);
			}

		}

		this.punches = optimizedObject;

	}

	private double punchWidth = Double.NaN;
	private double punchHeight = Double.NaN;
	private int punchType = 0; // square

	public double getPunchWidth() {
		return punchWidth;
	}

	public void setPunchWidth(double punchWidth) {
		this.punchWidth = punchWidth;
	}

	public double getPunchHeight() {
		return punchHeight;
	}

	public void setPunchHeight(double punchHeight) {
		this.punchHeight = punchHeight;
	}

	public int getPunchType() {
		return punchType;
	}

	public void setPunchType(int punchType) {
		this.punchType = punchType;
	}

	public OptimizedObject[] getPunch() {
		return this.punches;
	}

	//////////////////////////////////////////////////////////////////////////////

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	private String displayName;

	public void setLayerName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

}
