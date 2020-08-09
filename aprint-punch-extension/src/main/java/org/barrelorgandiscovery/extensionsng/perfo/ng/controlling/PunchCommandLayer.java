package org.barrelorgandiscovery.extensionsng.perfo.ng.controlling;

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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.CommandVisitor;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.CutToCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.HomingCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.XYCommand;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayer;
import org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayerName;
import org.barrelorgandiscovery.scale.Scale;

public class PunchCommandLayer implements VirtualBookComponentLayer,
		VirtualBookComponentLayerName {

	private static Logger logger = Logger.getLogger(PunchCommandLayer.class);

	private PunchPlan punchPlan = null;

	private TreeSet<XYCommand> index = null;

	private HashMap<XYCommand, Integer> indexToInteger = null;

	private boolean visible = true;

	/**
	 * current position in the punch display
	 */
	private Integer currentPos = null;

	private int origin = ORIGIN_CENTER;

	public final static int ORIGIN_LEFT_UPPER = 0;
	public final static int ORIGIN_LEFT_MIDDLE = 1;
	public final static int ORIGIN_CENTER = 2;

	// used for refresh and locate the user
	private JVirtualBookScrollableComponent comp;

	public PunchCommandLayer(JVirtualBookScrollableComponent comp) {
		this.comp = comp;
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

		PunchPlan localpunches = punchPlan;

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
			SortedSet<XYCommand> subSet = searchForXYCommands(rectcarton);

			drawPunches(g, jcarton, localpunches, g2d, transparent, rect,
					subSet);
		} finally {
			g2d.setComposite(oldcomposite);
			g2d.setStroke(oldstroke);
		}
	}

	private void drawPunches(Graphics g, JVirtualBookComponent jcarton,
			PunchPlan localpunches, Graphics2D g2d, Composite transparent,
			Rectangle rect, SortedSet<XYCommand> subSet) {

		Scale scale = jcarton.getVirtualBook().getScale();
		boolean isPreferredViewInverted = scale.isPreferredViewedInversed();

		for (Iterator iterator = subSet.iterator(); iterator.hasNext();) {
			XYCommand p = (XYCommand) iterator.next();

			int i = indexToInteger.get(p);

			int x = jcarton.convertCartonToScreenX(p.getX());
			int y = jcarton
					.convertCartonToScreenY((isPreferredViewInverted ? scale
							.getWidth() - p.getY() : p.getY()));

			Color c = Color.blue;

			if (currentPos != null) {
				if (currentPos >= i)
					// display in yellow the done part
					c = Color.yellow;
			}

			g.setColor(c);

			if (punchType == 0) // square ...
			{
				Color old = g.getColor();
				try {

					Paint oldpaint = g2d.getPaint();
					Stroke olds = g2d.getStroke();
					Composite current = g2d.getComposite();
					if (p instanceof PunchCommand) {

						if (!Double.isNaN(punchHeight)
								&& !Double.isNaN(punchWidth)) {

							// dessin du rectangle ...
							int width = jcarton.MmToPixel(punchWidth);
							int height = jcarton.MmToPixel(punchHeight);

							g.setColor(c);

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

							g2d.setPaint(c);
							// g.fillRect(xs, ys, width, height);
							// g.setColor(Color.black);
							g.drawRect(xs, ys, width, height);

							g2d.setPaint(oldpaint);

							// g2d.setStroke(olds);
							// g2d.setComposite(current);

						}

					}
					g.drawRect(x, y, 2, 2);

				} finally {
					g.setColor(old);
				}
			} else {
				logger.debug("punch type not supported ...");
			}


			if (i >= 1) {

				// previous command
				XYCommand p1 = (XYCommand) punchPlan.getCommandsByRef().get(
						i - 1 );

				int x2 = jcarton.convertCartonToScreenX(p1.getX());
				int y2 = jcarton
						.convertCartonToScreenY((isPreferredViewInverted ? scale
								.getWidth() - p1.getY()
								: p1.getY()));
				if (rect.intersectsLine(x, y, x2, y2)) {
					// g2d.setComposite(transparent);
					g.drawLine(x, y, x2, y2);
					// g2d.setComposite(oldcomposite);
				}
			}

		}
	}

	private SortedSet<XYCommand> searchForXYCommands(
			Rectangle2D.Double rectcarton) {
		SortedSet<XYCommand> subSet = index.subSet(new DisplacementCommand(
				rectcarton.x, 0), new DisplacementCommand(rectcarton.x
				+ rectcarton.width, 0));
		return subSet;
	}

	/**
	 * set the punches to display
	 * 
	 * @param punches
	 */
	public void setPunchPlan(PunchPlan punchPlan) throws Exception {

		if (punchPlan == null) {
			this.index = null;
			this.indexToInteger = null;

		} else {

			this.index = new TreeSet<XYCommand>(new Comparator<XYCommand>() {
				public int compare(XYCommand o1, XYCommand o2) {

					int c = Double.compare(o1.getX(), o2.getX());
					if (c != 0)
						return c;
					return Double.compare(o1.getY(), o2.getY());
				}
			});

			this.indexToInteger = new HashMap<XYCommand, Integer>();

			final AtomicInteger i = new AtomicInteger(0);

			CommandVisitor v = new CommandVisitor() {

				@Override
				public void visit(int idx,DisplacementCommand displacementCommand)
						throws Exception {
					index.add(displacementCommand);
					indexToInteger.put(displacementCommand, i.getAndAdd(1));
				}

				@Override
				public void visit(int idx,PunchCommand punchCommand) throws Exception {
					index.add(punchCommand);
					indexToInteger.put( punchCommand, i.getAndAdd(1));
				}
				
				@Override
				public void visit(int idx, CutToCommand cutToCommand) throws Exception {
					index.add(cutToCommand);
					indexToInteger.put( cutToCommand, i.getAndAdd(1));
				}
				
				
				@Override
				public void visit(int idx,HomingCommand command) throws Exception {
					
				}
				
			};

			v.visit(punchPlan);
		}

		this.punchPlan = punchPlan;

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

	public PunchPlan getPunchPlan() {
		return this.punchPlan;
	}

	// ////////////////////////////////////////////////////////////////////////////

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

	public void setCurrentPos(Integer currentPos) {

		Integer old = this.currentPos;
		this.currentPos = currentPos;

		if (currentPos != old) {
			// changed
			comp.repaint();
		}
	}

	public Integer getCurrentPos() {
		return currentPos;
	}

}
