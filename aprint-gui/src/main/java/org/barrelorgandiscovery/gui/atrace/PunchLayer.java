package org.barrelorgandiscovery.gui.atrace;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayer;
import org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayerName;
import org.barrelorgandiscovery.scale.Scale;

/**
 * Layer for displaying the punch vision of a book
 * 
 * @author Freydiere Patrice
 */
public class PunchLayer implements VirtualBookComponentLayer, VirtualBookComponentLayerName {

	private static Logger logger = Logger.getLogger(PunchLayer.class);

	private Punch[] punches = null;

	private TreeSet<Punch> index = null;
	
	private HashMap<Punch, Integer> indexToInteger = null;

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

		Punch[] localpunches = punches;

		if (localpunches == null || jcarton.getVirtualBook() == null)
			return;

		Graphics2D g2d = (Graphics2D) g;
		Composite oldcomposite = g2d.getComposite();
		Stroke oldstroke = g2d.getStroke();
		try {

			Composite transparent = AlphaComposite.getInstance(
					AlphaComposite.SRC_ATOP, 0.2f);

			Composite mediumtransparent = AlphaComposite.getInstance(
					AlphaComposite.SRC_ATOP, 0.6f);

			Stroke swidth = new BasicStroke(2.0f);

			g.setColor(Color.BLUE);

			Rectangle rect = g.getClipBounds();

			Scale scale = jcarton.getVirtualBook().getScale();
			boolean isViewInverted = scale.isPreferredViewedInversed();

			Rectangle2D.Double rectcarton;

			if (isViewInverted) {
				rectcarton = new Rectangle2D.Double(
						jcarton.convertScreenXToCarton(rect.x),
						jcarton.convertScreenYToCarton(rect.y),
						jcarton.pixelToMm(rect.width),
						jcarton.pixelToMm(rect.height));
			} else {
				rectcarton = new Rectangle2D.Double(
						jcarton.convertScreenXToCarton(rect.x),
						scale.getWidth()
								- jcarton.convertScreenYToCarton(rect.y),
						jcarton.pixelToMm(rect.width),
						jcarton.pixelToMm(rect.height));
			}
			SortedSet<Punch> subSet = searchForPunches(rectcarton);

			drawPunches(g, jcarton, localpunches, g2d, transparent, rect,
					subSet);
		} finally {
			g2d.setComposite(oldcomposite);
			g2d.setStroke(oldstroke);
		}
	}

	private void drawPunches(Graphics g, JVirtualBookComponent jcarton,
			Punch[] localpunches, Graphics2D g2d, Composite transparent,
			Rectangle rect, SortedSet<Punch> subSet) {

		Scale scale = jcarton.getVirtualBook().getScale();
		boolean isPreferredViewInverted = scale.isPreferredViewedInversed();

		for (Iterator iterator = subSet.iterator(); iterator.hasNext();) {
			Punch p = (Punch) iterator.next();

			int i = indexToInteger.get(p);

			int x = jcarton.convertCartonToScreenX(p.x);
			int y = jcarton
					.convertCartonToScreenY((isPreferredViewInverted ? scale
							.getWidth() - p.y : p.y));

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
						Stroke olds = g2d.getStroke();
						Composite current = g2d.getComposite();

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

						// g2d.setStroke(olds);
						// g2d.setComposite(current);

					} finally {
						g.setColor(old);
					}

				} else {
					g.drawRect(x, y, 2, 2);
				}
			} else {
				logger.debug("punch type not supported ...");
			}

			// }

			if (i < localpunches.length - 1) {
				Punch p1 = localpunches[i + 1];

				int x2 = jcarton.convertCartonToScreenX(p1.x);
				int y2 = jcarton
						.convertCartonToScreenY((isPreferredViewInverted ? scale
								.getWidth() - p1.y
								: p1.y));

				if (rect.intersectsLine(x, y, x2, y2)) {
					// g2d.setComposite(transparent);
					g.drawLine(x, y, x2, y2);
					// g2d.setComposite(oldcomposite);
				}

			}

			if (i > 1) {
				Punch p1 = localpunches[i - 1];

				int x2 = jcarton.convertCartonToScreenX(p1.x);
				int y2 = jcarton
						.convertCartonToScreenY((isPreferredViewInverted ? scale
								.getWidth() - p1.y
								: p1.y));
				if (rect.intersectsLine(x, y, x2, y2)) {
					// g2d.setComposite(transparent);
					g.drawLine(x, y, x2, y2);
					// g2d.setComposite(oldcomposite);
				}
			}

		}
	}

	private SortedSet<Punch> searchForPunches(Rectangle2D.Double rectcarton) {
		SortedSet<Punch> subSet = index.subSet(new Punch(rectcarton.x, 0),
				new Punch(rectcarton.x + rectcarton.width, 0));
		return subSet;
	}

	/**
	 * set the punches to display
	 * @param punches
	 */
	public void setPunch(Punch[] punches) {

		if (punches == null) {
			this.index = null;
			this.indexToInteger = null;

		} else {
			this.index = new TreeSet<Punch>(new Comparator<Punch>() {
				public int compare(Punch o1, Punch o2) {

					int c = Double.compare(o1.x, o2.x);
					if (c != 0)
						return c;
					return Double.compare(o1.y, o2.y);
				}
			});
			this.indexToInteger = new HashMap<Punch, Integer>();

			for (int i = 0; i < punches.length; i++) {
				if (punches[i] == null)
					continue;
				this.index.add(punches[i]);
				this.indexToInteger.put(punches[i], i);
			}

		}

		this.punches = punches;
		
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

	public Punch[] getPunch() {
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
	
	public void setLayerName(String displayName)
	{
		this.displayName = displayName;
	}
	
	@Override
	public String getDisplayName() {
		return displayName;
	}

}
