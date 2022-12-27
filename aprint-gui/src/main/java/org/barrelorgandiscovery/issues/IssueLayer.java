package org.barrelorgandiscovery.issues;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.ToolTipVirtualBookComponentLayer;
import org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayer;
import org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayerName;
import org.barrelorgandiscovery.gui.issues.JIssuePresenter;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class IssueLayer implements VirtualBookComponentLayer,
		ToolTipVirtualBookComponentLayer, VirtualBookComponentLayerName {

	private static final Logger logger = Logger.getLogger(IssueLayer.class);

	private IssueCollection issueCollection = null;

	public IssueLayer() {
	}

	private static Color colorissue = Color.yellow;

	public void draw(Graphics g, JVirtualBookComponent jcarton) {

		Graphics2D g2d = (Graphics2D) g;

		AffineTransform oldtransform = g2d.getTransform();
		try {

			Stroke oldStroke = g2d.getStroke();
			try {

				AffineTransform t = new AffineTransform(oldtransform);

				Toolkit kit = Toolkit.getDefaultToolkit();
				int screendpi = kit.getScreenResolution();
				double pts_par_mm = (screendpi * 1.0) / 25.4;

				AffineTransform st = AffineTransform.getScaleInstance(
						pts_par_mm / (jcarton.getXfactor()), pts_par_mm
								/ (jcarton.getXfactor()));

				t.concatenate(st);

				AffineTransform f = AffineTransform.getTranslateInstance(
						-jcarton.getXoffset() + jcarton.getMargin(),
						-jcarton.getYoffset() + jcarton.getMargin());

				t.concatenate(f);

				g2d.setTransform(t);

				g2d.setStroke(new BasicStroke(0.5f));
				g2d.setPaint(Color.red);

				if (compiledIssues != null) {
					for (Iterator iterator = compiledIssues.entrySet()
							.iterator(); iterator.hasNext();) {
						Entry<AbstractSpatialIssue, IssueRepresentation> e = (Entry<AbstractSpatialIssue, IssueRepresentation>) iterator
								.next();

						IssueRepresentation r = e.getValue();
						if (r != null) {
							ArrayList<Shape> arr = r.issueShape;
							for (Iterator iterator2 = arr.iterator(); iterator2
									.hasNext();) {
								Shape shape = (Shape) iterator2.next();
								g2d.draw(shape);
							}
						}

					}

					if (selectedIssues != null) {

						g2d.setStroke(new BasicStroke(1.5f));
						g2d.setPaint(Color.red);

						for (int i = 0; i < selectedIssues.length; i++) {
							AbstractIssue abstractIssue = selectedIssues[i];
							if (abstractIssue instanceof AbstractSpatialIssue) {
								IssueRepresentation issueRepresentation = compiledIssues
										.get(abstractIssue);

								ArrayList<Shape> rs = issueRepresentation.issueShape;
								for (Iterator iterator = rs.iterator(); iterator
										.hasNext();) {
									Shape shape = (Shape) iterator.next();
									g2d.draw(shape);
								}

							}
						}

					}

				}

			} finally {
				g2d.setStroke(oldStroke);
			}

		} finally {
			g2d.setTransform(oldtransform);
		}

	}


	private class IssueRepresentation {
		public ArrayList<Shape> issueShape;
	}

	/**
	 * convert a time in microseconds in mm
	 * 
	 * @param time
	 *            the time
	 * @return the mm distance representing the time
	 */
	private double timeToMM(Scale scale, long time) {
		return ((double) time) / 1000000 * scale.getSpeed();
	}

	private Map<AbstractSpatialIssue, IssueRepresentation> compiledIssues = null;

	private Map<AbstractSpatialIssue, IssueRepresentation> compileIssues(
			VirtualBook vb, IssueCollection c) {
		if (c == null || vb == null)
			return new HashMap<AbstractSpatialIssue, IssueRepresentation>();

		logger.debug("compile issues");

		Scale scale = vb.getScale();

		Map<AbstractSpatialIssue, IssueRepresentation> retvalue = new HashMap<AbstractSpatialIssue, IssueRepresentation>();

		for (Iterator iterator = c.iterator(); iterator.hasNext();) {
			AbstractIssue ai = (AbstractIssue) iterator.next();

			if (ai instanceof AbstractSpatialIssue) {

				AbstractSpatialIssue asi = (AbstractSpatialIssue) ai;
				ArrayList<Shape> gc = new ArrayList<Shape>();

				if (ai instanceof IssueHole) {

					IssueHole ih = (IssueHole) ai;
					Hole[] holes = ih.getNotes();

					if (holes != null && holes.length > 0) {
						// dessin des problemes ...

						for (int j = 0; j < holes.length; j++) {
							Hole h = holes[j];

							double x = timeToMM(scale, h.getTimestamp());

							double y;

							double d = scale.getFirstTrackAxis()
									+ scale.getIntertrackHeight()
									* h.getTrack()
									- scale.getIntertrackHeight() / 2.0;

							if (scale.isPreferredViewedInversed()) {

								y = scale.getWidth() - (d)
										- scale.getIntertrackHeight();

							} else {

								y = d;
							}

							Ellipse2D.Double e = new Ellipse2D.Double(x, y,
									timeToMM(scale, h.getTimeLength()),
									scale.getTrackWidth());

							gc.add(e);

						}

					}

				} else if (ai instanceof IssueRegion) {

					IssueRegion ir = (IssueRegion) ai;

					double xstart = timeToMM(scale, ir.getStart());
					double xend = timeToMM(scale,
							ir.getStart() + ir.getLength());

					double ystart;
					double yend;

					if (scale.isPreferredViewedInversed()) {

						yend = scale.getWidth()
								- (scale.getFirstTrackAxis()
										+ scale.getIntertrackHeight()
										* ir.getBegintrack() - scale
										.getIntertrackHeight() / 2);

						ystart = scale.getWidth()
								- (scale.getFirstTrackAxis()
										+ scale.getIntertrackHeight()
										* ir.getEndtrack() + scale
										.getIntertrackHeight() / 2);
					} else {
						ystart = scale.getFirstTrackAxis()
								+ scale.getIntertrackHeight()
								* ir.getBegintrack()
								- scale.getIntertrackHeight() / 2;

						yend = scale.getFirstTrackAxis()
								+ scale.getIntertrackHeight()
								* ir.getEndtrack()
								+ scale.getIntertrackHeight() / 2;
					}

					Rectangle2D.Double r2d = new Rectangle2D.Double(xstart,
							Math.min(ystart, yend), xend - xstart,
							Math.abs(ystart - yend));

					gc.add(r2d);

				} else if (ai instanceof IssueMissing) {

					IssueMissing im = (IssueMissing) ai;

					double xstart = timeToMM(scale, im.getStart());
					double xend = timeToMM(scale,
							im.getStart() + im.getLength());

					// int ypos = jcarton.convertCartonToScreenY(gamme
					// .getWidth());

					double ypos = scale.getWidth();

					int issueposition = im.getInterpolatePos();
					if (issueposition >= 0) {
						if (scale.isPreferredViewedInversed()) {
							ypos = scale.getWidth()
									- (scale.getFirstTrackAxis() + issueposition
											* scale.getIntertrackHeight());
						} else {

							ypos = scale.getFirstTrackAxis() + issueposition
									* scale.getIntertrackHeight();
						}
					}

					Rectangle2D.Double r2d = new Rectangle2D.Double(xstart,
							ypos, xend - xstart, 5);

					gc.add(r2d);

				} else {
					logger.debug("issue " + ai + " not drawn"); //$NON-NLS-1$ //$NON-NLS-2$
				}

				IssueRepresentation r = new IssueRepresentation();
				r.issueShape = gc;

				retvalue.put(asi, r);

			}

		}

		return retvalue;

	}

	public String getToolTipInfo(double x, double y) {

		if (!isVisible())
			return null;

		if (compiledIssues != null) {
			for (Iterator iterator = compiledIssues.entrySet().iterator(); iterator
					.hasNext();) {
				Entry<AbstractSpatialIssue, IssueRepresentation> e = (Entry<AbstractSpatialIssue, IssueRepresentation>) iterator
						.next();

				IssueRepresentation value = e.getValue();
				ArrayList<Shape> arrayList = value.issueShape;
				if (arrayList != null) {
					for (Iterator iterator2 = arrayList.iterator(); iterator2
							.hasNext();) {
						Shape shape = (Shape) iterator2.next();
						if (shape.contains(x, y)) {

							return JIssuePresenter.issueToString(e.getKey());
						}

					}
				}

			}

		}

		return null;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// Gestion des problemes ...

	public IssueCollection getIssueCollection() {
		return issueCollection;
	}

	public void setIssueCollection(IssueCollection issueCollection,
			VirtualBook vb) {

		if (issueCollection == null || vb == null) {
			compiledIssues = null;
			this.issueCollection = null;
			resetSelectedIssues();
			fireIssueCollectionChanged(issueCollection);
			return;
		}

		Map<AbstractSpatialIssue, IssueRepresentation> c = compileIssues(vb,
				issueCollection);
		this.compiledIssues = c;
		this.issueCollection = issueCollection;
		resetSelectedIssues();
		fireIssueCollectionChanged(issueCollection);
	}

	// Manage error Selection ...

	private AbstractIssue[] selectedIssues = null;

	public void resetSelectedIssues() {
		selectedIssues = null;
	}

	public void setSelectedIssues(AbstractIssue[] sel) {
		selectedIssues = sel;
	}

	public AbstractIssue[] getSelectedIssues() {
		return selectedIssues;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// Gestion de la visibilite des couches ...

	private boolean visible = true;

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	private Vector<IssueCollectionListener> issueCollectionListener = new Vector<IssueCollectionListener>();

	public void addIssueCollectionListener(IssueCollectionListener listener) {
		if (listener == null)
			return;
		issueCollectionListener.add(listener);
	}

	public void removeIssueCollectionListener(IssueCollectionListener listener) {
		issueCollectionListener.remove(listener);
	}

	protected void fireIssueCollectionChanged(IssueCollection ic) {

		for (Iterator iterator = issueCollectionListener.iterator(); iterator
				.hasNext();) {
			IssueCollectionListener c = (IssueCollectionListener) iterator
					.next();
			try {
				c.issuesChanged(ic);
			} catch (Throwable t) {
				logger.error("error in firing event ..." + t.getMessage(), t);
			}

		}
		logger.debug("done !");

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
