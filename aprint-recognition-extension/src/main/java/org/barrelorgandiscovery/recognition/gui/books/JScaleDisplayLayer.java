package org.barrelorgandiscovery.recognition.gui.books;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.PipeSetGroupLayer;
import org.barrelorgandiscovery.recognition.gui.books.BookReadProcessor.Extremum;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JDisplay;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JLayer;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JLinesLayer;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.Scale;

/**
 * Display the scale on the image, knowing the book border positions
 * 
 * @author pfreydiere
 *
 */
public class JScaleDisplayLayer extends JLayer {

	private static Logger logger = Logger.getLogger(JScaleDisplayLayer.class);

	private JDisplay display;
	private Scale instrumentScale;
	private JLinesLayer top;
	private JLinesLayer bottom;

	private boolean viewInverted = false;

	public JScaleDisplayLayer(JDisplay display, Scale instrumentScale, JLinesLayer top, JLinesLayer bottom) {
		assert display != null;
		this.display = display;
		// assert instrumentScale != null;
		this.instrumentScale = instrumentScale;
		assert top != null;
		this.top = top;
		assert bottom != null;
		this.bottom = bottom;
	}

	@Override
	public String getName() {
		return null;
	}

	public void setViewInverted(boolean viewInverted) {
		this.viewInverted = viewInverted;
	}

	public boolean isViewInverted() {
		return viewInverted;
	}

	public void setInstrumentScale(Scale instrumentScale) {
		this.instrumentScale = instrumentScale;
		display.repaint();
	}

	public Scale getInstrumentScale() {
		return instrumentScale;
	}

	@Override
	public void drawLayer(Graphics2D g2d) {

		if (top.getGraphics().size() < 2 || bottom.getGraphics().size() < 2)
			return;

		if (instrumentScale == null)
			return;

		Rectangle clipBounds = g2d.getClipBounds();

		List<Double> ptop = BookReadProcessor.toPoint(top.getGraphics());
		List<Double> pbottom = BookReadProcessor.toPoint(bottom.getGraphics());

		// draw arrow
		Extremum e = BookReadProcessor.getEdges(10, ptop, pbottom);
		if (!java.lang.Double.isNaN(e.min) && !java.lang.Double.isNaN(e.max)) {
			g2d.drawLine(10, factor(0.3, e, viewInverted), 10, factor(0, e, viewInverted));
			g2d.drawLine(13, factor(0.1, e, viewInverted), 10, factor(0, e, viewInverted));
			g2d.drawLine(7, factor(0.1, e, viewInverted), 10, factor(0, e, viewInverted));
		}

		if (clipBounds != null) {
			AlphaComposite alphacomposite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.2f);
			for (int i = 0; i < instrumentScale.getTrackNb() + 1; i++) {

				// ratio for positionning the element in scale
				double ratio = (1.0 * instrumentScale.getFirstTrackAxis() + instrumentScale.getIntertrackHeight() * i
						- instrumentScale.getIntertrackHeight() / 2) / instrumentScale.getWidth();
				double ratio2 = (1.0 * instrumentScale.getFirstTrackAxis()
						+ instrumentScale.getIntertrackHeight() * (i + 1) - instrumentScale.getIntertrackHeight() / 2)
						/ instrumentScale.getWidth();

//				if (viewInverted)
//					ratio = 1.0 - ratio;

				// color elements

				double step = clipBounds.getWidth() / 100;
				if (i < instrumentScale.getTrackNb()) {
					
					AbstractTrackDef abstractTrackDef = instrumentScale.getTracksDefinition()[i];
					Color color = PipeSetGroupLayer.getColor(abstractTrackDef);
					Color old = g2d.getColor();
					Composite oldc = g2d.getComposite();
					try {
						g2d.setColor(color);
						
						g2d.setComposite(alphacomposite);
						for (double d = clipBounds.getMinX(); d < clipBounds.getMaxX(); d += step) {

							e = BookReadProcessor.getEdges(d, ptop, pbottom);

							if (!java.lang.Double.isNaN(e.min) && !java.lang.Double.isNaN(e.max)) {

								double y1 = factor(ratio, e, viewInverted);
								double y2 = factor(ratio2, e, viewInverted);

								Rectangle2D.Double r = new Rectangle2D.Double(d, Math.min(y1, y2), step,
										Math.abs(y2 - y1));

								g2d.fill(r);

							}

						}

					} finally {
						g2d.setColor(old);
						g2d.setComposite(oldc);
					}
					
				}

				boolean first = true;
				Path2D.Double p = null;
				// draw borders

				for (double d = clipBounds.getMinX(); d < clipBounds.getMaxX(); d += step) {

					e = BookReadProcessor.getEdges(d, ptop, pbottom);

					if (!java.lang.Double.isNaN(e.min) && !java.lang.Double.isNaN(e.max)) {

						if (p == null) {
							p = new Path2D.Double();
						}

						double y1 = factor(ratio, e, viewInverted);

						if (first) {
							p.moveTo(d, y1);
							first = false;
						} else {
							p.lineTo(d, y1);
						}
					}

				}
				if (p != null)
					g2d.draw(p);

			}
		}
	}

	private int factor(double ratio, Extremum e, boolean inverted) {
		assert ratio >= 0 && ratio <= 1.0;
		if (inverted)
			ratio = 1.0 - ratio;

		return (int) (ratio * (e.max - e.min) + e.min);
	}

	@Override
	public Rectangle2D getExtent() {
		return null;
	}

	@Override
	public String getTooltip(Double position) {
		if (position == null) {
			return null;
		}
		logger.debug("query tooltip at " + position);
		for (int i = 0; i < instrumentScale.getTrackNb(); i++) {

			double baselineRatio = (1.0 * instrumentScale.getFirstTrackAxis()
					+ instrumentScale.getIntertrackHeight() * i - instrumentScale.getIntertrackHeight() / 2)
					/ instrumentScale.getWidth();
			double baselineRatio2 = (1.0 * instrumentScale.getFirstTrackAxis()
					+ instrumentScale.getIntertrackHeight() * (i + 1) - instrumentScale.getIntertrackHeight() / 2)
					/ instrumentScale.getWidth();

			double d = position.x;

			List<Double> ptop = BookReadProcessor.toPoint(top.getGraphics());
			List<Double> pbottom = BookReadProcessor.toPoint(bottom.getGraphics());

			Extremum e = BookReadProcessor.getEdges(d, ptop, pbottom);

			if (!java.lang.Double.isNaN(e.min) && !java.lang.Double.isNaN(e.max)) {

				double y = factor(baselineRatio, e, viewInverted);
				double y2 = factor(baselineRatio2, e, viewInverted);
				double ymin = Math.min(y2, y);
				double ymax = Math.max(y2, y);

				logger.debug(" y :" + ymin + " y2 :" + ymax);
				if (position.y >= ymin && position.y < ymax) {
					AbstractTrackDef td = instrumentScale.getTracksDefinition()[i];
					return "track :" + i + (td != null ? " " + td.toString() : "");
				}

			}

		}
		return null;
	}

}
