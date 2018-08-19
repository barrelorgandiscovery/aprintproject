package org.barrelorgandiscovery.gui.aedit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * layer for displaying distance on the book
 * 
 * @author use
 * 
 */
public class DistanceLayer implements VirtualBookComponentLayer,
		VirtualBookComponentLayerName {

	private static Logger logger = Logger.getLogger(DistanceLayer.class);

	public DistanceLayer() {

	}

	@Override
	public String getDisplayName() {
		return "Distance";
	}

	private boolean visible = true;

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public void draw(Graphics g, JVirtualBookComponent jbookcomponentreference) {

		try {

			Graphics2D g2d = (Graphics2D) g;

			if (!visible)
				return;

			// logger.debug("draw registration section layer");

			VirtualBook virtualBook = jbookcomponentreference.getVirtualBook();
			if (virtualBook == null)
				return;

			Rectangle clipBounds = g.getClipBounds(new Rectangle());

			double start = 0;
			double end = virtualBook.getLength();

			if (clipBounds != null) {

				start = jbookcomponentreference
						.convertScreenXToCarton(clipBounds.x);

				end = jbookcomponentreference
						.convertScreenXToCarton(clipBounds.x + clipBounds.width);

			}

			// logger.debug("start :" + start);
			// logger.debug("end :" + end);

			// setup all meters with all 20 cm
			int startMeter = (int) Math.floor(start / 1000);
			int endMeter = (int) Math.ceil(end / 1000);

			Font f = g.getFont();
			try {

				// Calcule de la meilleure taille de font pour les
				// pistes ...
				FontMetrics fm = g.getFontMetrics();
				int fontheight = fm.getHeight();

				int entrepistepixel = jbookcomponentreference
						.MmToPixel(virtualBook.getScale().getTrackWidth());

				double correctfontsize = (double) entrepistepixel / fontheight;

				Font newfont = f.deriveFont(
						(float) (f.getSize() * correctfontsize * 2.5))
						.deriveFont(Font.ITALIC + Font.BOLD);
				g.setFont(newfont);
				Color oldcolor = g.getColor();
				try {

					for (double i = startMeter; i < endMeter + 1; i += 0.05) {

						String sDistance = String.format("%1$2.1f m", i);

						int x = jbookcomponentreference
								.convertCartonToScreenX(i * 1000);
						int y = jbookcomponentreference
								.convertCartonToScreenY(0);
						int ymax = jbookcomponentreference
								.convertCartonToScreenY(virtualBook.getScale()
										.getWidth());

						BasicStroke dashed = new BasicStroke(0f,
								BasicStroke.CAP_SQUARE, BasicStroke.CAP_SQUARE,
								0f, new float[] { 9 }, 0f);
						BasicStroke plainFat = new BasicStroke(3.0f);

						Stroke olds = g2d.getStroke();
						try {
							if (Math.abs(i - new Double((int) i)) < 1e-10) {
								// integer part
								g2d.setStroke(plainFat);

							} else {
								g2d.setStroke(dashed);
							}

							g.setColor(Color.BLACK);

							g.drawLine(x, y, x, ymax);
							if ( ((int)(Math.round(i * 100))) % 10 == 0) {
								g.drawString(sDistance,
										x + entrepistepixel / 2, y
												+ entrepistepixel);
							}
						} finally {
							g2d.setStroke(olds);
						}

					}
				} catch (Exception ex) {
					logger.error("error in drawing the element");
				} finally {
					g.setColor(oldcolor);
				}

			} finally {
				g.setFont(f);
			}

		} catch (Throwable ex) {
			logger.error("error in displaying layer .. " + ex.getMessage(), ex);
		}

	}

}
