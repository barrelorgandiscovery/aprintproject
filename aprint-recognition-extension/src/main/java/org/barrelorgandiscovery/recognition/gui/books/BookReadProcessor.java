package org.barrelorgandiscovery.recognition.gui.books;

import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.barrelorgandiscovery.math.MathVect;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * image with scale tools to render and read the recognition value for the book
 * 
 * @author pfreydiere
 *
 */
public class BookReadProcessor {

	/**
	 * edge range
	 * 
	 * @author use
	 *
	 */
	public static class Extremum {
		double min = java.lang.Double.NaN;
		double max = java.lang.Double.NaN;
	}

	/**
	 * interpolate edges
	 * 
	 * @param offset
	 * @param top
	 * @param bottom
	 * @return
	 */
	public static Extremum getEdges(double offset, List<Point2D.Double> top, List<Point2D.Double> bottom) {
		Extremum e = new Extremum();
		e.min = interpolate(offset, top);
		e.max = interpolate(offset, bottom);
		return e;
	}

	public static int computeMeanWidth(List<Point2D.Double> top, List<Point2D.Double> bottom) {

		double xmin = Double.MAX_VALUE;
		double xmax = Double.MIN_VALUE;

		for (Point2D.Double p : top) {
			if (p.getX() < xmin) {
				xmin = p.getX();
			}

			if (p.getX() > xmax) {
				xmax = p.getX();
			}
		}

		for (Point2D.Double p : bottom) {
			if (p.getX() < xmin) {
				xmin = p.getX();
			}

			if (p.getX() > xmax) {
				xmax = p.getX();
			}
		}

		long width = 0;
		int cpt = 0;
		for (double x = xmin; x < xmax; x += 10) {
			Extremum e = getEdges(x, top, bottom);
			if (!Double.isNaN(e.max) && !Double.isNaN(e.min)) {
				width += Math.abs(e.max - e.min);
				cpt++;
			}
		}

		return (int) (width / cpt);
	}

	/**
	 * interpolate along a line
	 * 
	 * @param offset
	 * @param g
	 * @return
	 */
	private static java.lang.Double interpolate(double offset, List<Point2D.Double> g) {

		if (g == null || g.size() < 2)
			return Double.NaN;

		for (int i = 0; i < g.size() - 1; i++) {

			Point2D.Double p1 = g.get(i);
			Point2D.Double p2 = g.get(i + 1);

			if (offset >= p1.getX() && offset <= p2.getX()) {
				// in range
				// interpolate
				MathVect v = new MathVect(p1, p2);
				double delta = offset - p1.getX();
				MathVect nv = v.scale(delta / (p2.getX() - p1.getX()));
				return nv.plus(p1).getY();
			}

		}
		return Double.NaN;
	}

	public static double ratioPtsPerMm() {
		Toolkit kit = Toolkit.getDefaultToolkit();
		int screendpi = kit.getScreenResolution();
		double pts_par_mm = (screendpi * 1.0) / 25.4;
		return pts_par_mm;
	}

	public static class ReadResultBag {
		public VirtualBook virtualbook;
		public long[] state;
	}

	/**
	 * read image content
	 * 
	 * @param bi
	 * @param startpixel
	 * @param instrumentScale
	 * @param top
	 * @param bottom
	 * @return
	 */
	public static ReadResultBag readResult2(BufferedImage bi, int startpixel, double pixelTimeValueInBook,
			Scale instrumentScale, long[] optionalPreviousState, boolean matchblack, int decision_threshold) {

		assert decision_threshold >= 0 && decision_threshold <= 255;
		
		VirtualBook vb = new VirtualBook(instrumentScale);

		long[] state = optionalPreviousState;
		if (state == null) {
			state = new long[instrumentScale.getTrackNb()];
		}

		// for each row,
		for (int x = startpixel; x < startpixel + bi.getWidth(); x++) {

			// for each track
			for (int k = 0; k < instrumentScale.getTrackNb(); k++) {

				// ratio position
				double start = (1.0d * instrumentScale.getFirstTrackAxis() + k * instrumentScale.getIntertrackHeight()
						- instrumentScale.getIntertrackHeight() / 2) / instrumentScale.getWidth();
				double end = (1.0d * instrumentScale.getFirstTrackAxis() + k * instrumentScale.getIntertrackHeight()
						+ instrumentScale.getIntertrackHeight() / 2) / instrumentScale.getWidth();

				// read the track

				double h = bi.getHeight();
				assert h >= 0;

				long v = 0;
				int cpt = 0;
				// for each
				for (int y = (int) (start * h); y <= (int) (end * h); y++) {

					int rgb = bi.getRGB(x - startpixel, y);
					int rvalue = (rgb & 0xFF0000) >> 16;
					rvalue = rvalue & 0xFF;
					v += rvalue;
					cpt++;
				}
				assert v >=0;
				
				boolean readstate = false;
				double threshold = v * 1.0 / cpt;
				if (matchblack) {
					threshold = 255 - threshold;
				}

				
				if (threshold > decision_threshold) {
					readstate = true;
				}

				// System.out.println(x + " " + k + " " + threshold + " -> "
				// + readstate);

				if (state[k] > 0) {
					if (!readstate) {
						// switched off ...
						Hole hole = new Hole(k, (long) (pixelTimeValueInBook * state[k]),
								(long) (pixelTimeValueInBook * (x - state[k])));
						vb.addHole(hole);
						// reset
						state[k] = 0;

					}

				} else if (state[k] == 0) {
					if (readstate) {
						state[k] = x;
					}
				}
			}
		}

		// close opened elements
		for (int k = 0; k < instrumentScale.getTrackNb(); k++) {
			long s = state[k];
			if (s > 0) {
				Hole hole = new Hole(k, (long) (pixelTimeValueInBook * s),
						(long) (pixelTimeValueInBook * (startpixel + bi.getWidth() - s)));
				vb.addHole(hole);
				// reset
				state[k] = 0;
			}
		}

		ReadResultBag result = new ReadResultBag();
		result.virtualbook = vb;
		result.state = state;

		return result;

	}

	/**
	 * read image content
	 * 
	 * @param bi
	 * @param startpixel
	 * @param instrumentScale
	 * @param top
	 * @param bottom
	 * @return
	 */
	public static VirtualBook readResult_old(BufferedImage bi, int startpixel, double pixelTimeValue,
			Scale instrumentScale, List<Point2D.Double> top, List<Point2D.Double> bottom) {

		VirtualBook vb = new VirtualBook(instrumentScale);

		long[] state = new long[instrumentScale.getTrackNb()];

		// for each row,
		for (int x = startpixel; x < startpixel + bi.getWidth(); x++) {

			Extremum e = getEdges(x, top, bottom);

			if (!Double.isNaN(e.min) && !Double.isNaN(e.max)) {

				// for each track
				for (int k = 0; k < instrumentScale.getTrackNb(); k++) {

					// ratio position
					double start = (1.0d * instrumentScale.getFirstTrackAxis()
							+ k * instrumentScale.getIntertrackHeight() - instrumentScale.getIntertrackHeight() / 2)
							/ instrumentScale.getWidth();
					double end = (1.0d * instrumentScale.getFirstTrackAxis() + k * instrumentScale.getIntertrackHeight()
							+ instrumentScale.getIntertrackHeight() / 2) / instrumentScale.getWidth();

					// read the track

					double h = e.max - e.min;
					assert h >= 0;

					long v = 0;
					int cpt = 0;
					// for each
					for (int y = (int) (start * h + e.min); y <= (int) (end * h + e.min); y++) {

						int rgb = bi.getRGB(x - startpixel, y);
						int rvalue = (rgb & 0xFF0000) >> 16;

						v += rvalue;
						cpt++;
					}

					boolean readstate = false;
					double threshold = v * 1.0 / cpt;
					if (threshold > 128) {
						readstate = true;
					}

					// System.out.println(x + " " + k + " " + threshold + " -> "
					// + readstate);

					if (state[k] > 0) {
						if (!readstate) {
							// switched off ...
							Hole hole = new Hole(k, (long) (pixelTimeValue * state[k]),
									(long) (pixelTimeValue * (x - state[k])));
							vb.addHole(hole);
							// reset
							state[k] = 0;

						}

					} else if (state[k] == 0) {
						if (readstate) {
							state[k] = x;
						}
					}
				}
			}

		}

		// close opened elements
		for (int k = 0; k < instrumentScale.getTrackNb(); k++) {
			long s = state[k];
			if (s > 0) {
				Hole hole = new Hole(k, (long) (pixelTimeValue * s),
						(long) (pixelTimeValue * (startpixel + bi.getWidth() - s)));
				vb.addHole(hole);
				// reset
				state[k] = 0;
			}
		}

		return vb;

	}

	/**
	 * undistord the image
	 * 
	 * @param bi
	 * @param startpixel
	 * @param outHeight
	 * @param top
	 * @param bottom
	 * @return
	 */
	public static BufferedImage correctImage(BufferedImage bi, int startpixel, int outHeight, List<Point2D.Double> top,
			List<Point2D.Double> bottom, int vectorheight, boolean inverted) {

		BufferedImage out = new BufferedImage(bi.getWidth(), outHeight, BufferedImage.TYPE_INT_RGB);

		// for each x
		for (int x = 0; x < bi.getWidth(); x++) {
			Extremum e = getEdges(startpixel + x, top, bottom);
			if (!Double.isNaN(e.min) && !Double.isNaN(e.max)) {
				for (int y = 0; y < outHeight; y++) { // for every y in dest

					double f = 1.0d * y / outHeight; // ratio for y position

					if (inverted) {
						f = 1.0 - f;
					}

					// f=1.0-f;
					double resolution_factor = 1;// 96.0*2/200; // FIXME

					double whole = e.max - e.min;
					//
					// double sy = whole * (
					// (instrumentScale.getFirstTrackAxis() -
					// instrumentScale.getIntertrackHeight() / 2) /
					// instrumentScale.getWidth());
					// double ey = whole *( (instrumentScale.getFirstTrackAxis()
					// + instrumentScale.getIntertrackHeight() / 2) +
					// instrumentScale.getTrackNb() *
					// instrumentScale.getIntertrackHeight() ) /
					// instrumentScale.getWidth();
					//
					double r = (e.min + whole * f) / vectorheight / resolution_factor;

					int computedy = (int) (r * outHeight); // Math.ceil

					int v = 0;
					
					if (computedy >= outHeight || computedy < 0) {
						System.out.println("impl error :" + computedy);
					} else {
						v = bi.getRGB(x, computedy);
					}
					
					out.setRGB(x, y, v);
				}
			}
		}
		return out;
	}

	public static List<Point2D.Double> toPoint(Collection<Rectangle2D.Double> col) {
		if (col == null)
			return null;

		ArrayList<Point2D.Double> a = new ArrayList<Point2D.Double>();
		for (Rectangle2D.Double r : col) {
			a.add(new Point2D.Double(r.getX() + 5, r.getY() + 5));
		}
		return a;
	}

	public static List<Rectangle2D.Double> fromPoint(Collection<Point2D.Double> col) {
		if (col == null)
			return null;

		ArrayList<Rectangle2D.Double> a = new ArrayList<Rectangle2D.Double>();
		for (Point2D.Double r : col) {
			a.add(new Rectangle2D.Double(r.getX() - 5, r.getY() - 5, 10, 10));
		}
		return a;
	}
	
}
