package org.barrelorgandiscovery.extensionsng.perfo.cad.canvas;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.barrelorgandiscovery.extensionsng.perfo.cad.math.Line;
import org.barrelorgandiscovery.extensionsng.perfo.cad.math.Vect;
import org.barrelorgandiscovery.scale.Scale;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * base class for drawing device (screen, or export)
 * 
 * @author pfreydiere
 *
 */
public abstract class DeviceDrawing {

	/**
	 * class for interpolation
	 * 
	 * @author use
	 *
	 */
	private class Interpolator {

		double from;
		double to;
		double total;

		public Interpolator(double from, double to, double total) {
			this.from = from;
			this.to = to;
			this.total = total;
		}

		public double interpole(double step) {
			if (Math.abs(total) < 1e-30) {
				return from;
			}

			return (to - from) / total * step + from;
		}
	}

	private List<Coordinate> currentLine = null;

	/**
	 * current layer
	 */
	private String currentLayer;

	/**
	 * define the current layer for drawing
	 * 
	 * @param layer
	 */
	public void setCurrentLayer(String layer) {
		this.currentLayer = layer;
	}

	public String getCurrentLayer() {
		return this.currentLayer;
	}

	public void startGroup() {

	}

	public void endGroup() {
		flushLine();

	}

	Coordinate computeCircularPos(double xcenter, double ycenter, double radius, double angle) {
		double x = xcenter + radius * Math.cos(angle);
		double y = ycenter + radius * Math.sin(angle);
		return new Coordinate(x, y);
	}

	public void drawArc(double xcenter, double ycenter, double radius, double firstArcAngle, double lastArcAngle) {

		assert radius >= 0;

		int steps = 10;
		for (int i = 0; i <= steps; i++) {
			drawTo(computeCircularPos(xcenter, ycenter, radius,
					firstArcAngle + (i * (lastArcAngle - firstArcAngle)) / steps));
		}

	}

	public void drawArrondi(Coordinate p1, Coordinate p2, double arrondi) {
		Vect milieu = new Vect(p1, p2).scale(0.5);
		Vect d = milieu.orthogonal().moins().orthoNorme().scale(arrondi);
		Coordinate ptD = milieu.plus(d).plus(p1);

		Line l1 = lineMilieuOthrogonal(p1, ptD);
		Line l2 = lineMilieuOthrogonal(ptD, p2);
		Coordinate centreCercle = l1.intersect(l2);

		double rayon = centreCercle.distance(p1);
		Vect v1 = new Vect(centreCercle, p1);
		double angle1 = v1.angleOrigine();
		Vect v2 = new Vect(centreCercle, p2);
		double angle2 = v2.angleOrigine();

		double angle = angle2 - angle1;
		if (angle1 < 0 && angle2 > 0 && v1.vectorielZ(new Vect(centreCercle, ptD)) < 0) {
			angle2 = angle2 - 2 * Math.PI;
		}

		drawArc(centreCercle.x, centreCercle.y, rayon, angle1, angle2);

	}

	/**
	 * draw line from .. to
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public void drawLine(double x1, double y1, double x2, double y2) {

		LineString lineString = new LineString(new Coordinate[] { new Coordinate(x1, y1), new Coordinate(x2, y2) },
				new PrecisionModel(PrecisionModel.FLOATING), 0);

		addObject(lineString);

	}

	/**
	 * interpole a double
	 * 
	 * @param from
	 * @param to
	 * @param step
	 * @param total
	 * @return
	 */
	private double interpole(double from, double to, double step, double total) {
		return (to - from) / total * step + from;
	}

	/**
	 * compute the line length
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	private double lineLength(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}

	/**
	 * draw dotspace + dotline
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param dotLength
	 * @param dotSpace
	 */
	public void drawDottedLines(double x1, double y1, double x2, double y2, double dotLength, double dotSpace) {

		double length = lineLength(x1, y1, x2, y2);
		if (dotSpace > length) {

			// draw a full line

		} else {
			Interpolator xi = new Interpolator(x1, x2, length);
			Interpolator yi = new Interpolator(y1, y2, length);

			if (dotLength + dotSpace > length) {
				// only draw part of the dot length
				drawLine(xi.interpole(dotSpace), yi.interpole(dotSpace), x1, y2);
			} else {
				double totalDotLength = dotLength + dotSpace;
				int nb = (int) (length / totalDotLength);
				for (int i = 0; i < nb; i++) {
					drawLine(xi.interpole(i * totalDotLength + dotSpace), yi.interpole(i * totalDotLength + dotSpace),
							xi.interpole((i + 1) * totalDotLength), yi.interpole((i + 1) * totalDotLength));
				}

				// last draw
				drawDottedLines(xi.interpole(nb * totalDotLength), yi.interpole(nb * totalDotLength), x2, y2, dotLength,
						dotSpace);

			}

		}

	}

	/**
	 * Dessine une ligne pointillee, en faisant en sorte que les pointilles soient
	 * bien
	 * 
	 * @param x1
	 * @param y2
	 * @param x2
	 * @param y2
	 * @param dotLength
	 * @param dotSpace
	 */
	public void drawImprovedDottedLines(double x1, double y1, double x2, double y2, double dotLength, double dotSpace) {

		double totalDotLength = dotLength + dotSpace;

		double length = lineLength(x1, y1, x2, y2);

		// recalc the dot length for having drawing at both side
		double nbofdot = length / totalDotLength;
		double floor = Math.ceil(nbofdot);

		double wishedLength = floor * totalDotLength + dotSpace;
		double newDotLength = dotLength / length * wishedLength;
		double newDotSpace = dotSpace / length * wishedLength;

		drawDottedLines(x1, y1, x2, y2, newDotLength, newDotSpace);

	}

	/**
	 * put the dash inside the tracks boundaries
	 * 
	 * @param x
	 * @param device
	 * @param bookscale
	 */
	public void drawDottedLinesAccordinglyToBook(double x, DeviceDrawing device, Scale bookscale) {

		double first = bookscale.getFirstTrackAxis();
		double dash_size = bookscale.getTrackWidth() / 2;
		boolean inverted = !bookscale.isPreferredViewedInversed();

		for (int i = 0; i < bookscale.getTrackNb(); i++) {

			double shift = -bookscale.getTrackWidth() / 4;
			double y1 = first + i * bookscale.getIntertrackHeight() + shift;
			if (inverted) {
				y1 = bookscale.getWidth() - y1;
			}

			device.moveTo(x, y1);
			device.drawTo(x, y1 + (inverted ? -dash_size : dash_size));
		}

	}

	/**
	 * draw a rectangle hole
	 * 
	 * @param ypiste
	 * @param halfheight
	 * @param x
	 * @param endx
	 */
	public void drawRectangleHole(double ypiste, double halfheight, double x, double endx) {

		moveTo(x, ypiste - halfheight);
		drawTo(endx, ypiste - halfheight);
		drawTo(endx, ypiste + halfheight);
		drawTo(x, ypiste + halfheight);
		drawTo(x, ypiste - halfheight);
	}

	public void drawTo(Coordinate c) {
		if (currentLine == null)
			throw new RuntimeException("invalid draw, line is not started");
		assert c != null;
		currentLine.add(c);
	}

	public void drawTo(double x, double y) {
		drawTo(new Coordinate(x, y));
	}

	public void flushLine() {
		if (currentLine == null)
			return;

		if (currentLine.size() <= 1)
			throw new RuntimeException("invalid draw, not enough points");

		LineString lineString = new LineString(currentLine.toArray(new Coordinate[0]),
				new PrecisionModel(PrecisionModel.FLOATING), 0);
		addObject(lineString);

		currentLine = null;
	}

	protected abstract void addObject(Geometry g);

	private Line lineMilieuOthrogonal(Coordinate p1, Coordinate p2) {
		Vect milieu = new Vect(p1, p2).scale(0.5);
		Coordinate ptMilieu = milieu.plus(p1);
		return new Line(ptMilieu, milieu.orthogonal().moins());
	}

	public void moveTo(Coordinate c) {
		assert c != null;
		if (currentLine != null)
			flushLine();
		assert currentLine == null;

		currentLine = new ArrayList<Coordinate>();
		currentLine.add(c);
	}

	public void moveTo(double x, double y) {
		moveTo(new Coordinate(x, y));
	}

	/**
	 * draw an arrow
	 * 
	 * @param vector      vector for the arrow
	 * @param arroworigin origin of the arrow
	 * @param width       lengths of the arrow borders
	 */
	public void drawArrow(Vect vector, Coordinate arroworigin, double width) {

		Coordinate end = vector.plus(arroworigin);

		Vect v = vector.orthoNorme().scale(width);
		Vect inverted = v.rotateOrigin(Math.PI);

		Vect o1 = inverted.rotateOrigin(Math.PI / 180.0 * 15);
		Vect o2 = inverted.rotateOrigin(-Math.PI / 180.0 * 15);

		// draw the main line
		moveTo(arroworigin);
		drawTo(end);

		moveTo(end);
		drawTo(o1.plus(end));

		moveTo(end);
		drawTo(o2.plus(end));

	}

	/**
	 * write previously drawn content to a file
	 * 
	 * @param file
	 * @param layers
	 * @throws Exception
	 */
	public abstract void write(File file, String[] layers) throws Exception;

	/**
	 * write to output stream
	 * 
	 * @param outStream
	 * @param layers
	 * @throws Exception
	 */
	public abstract void write(OutputStream outStream, String[] layers) throws Exception;

	/**
	 * does the export needs to ignore the reference ? some export needs to not
	 * rotation the book
	 * 
	 * @return
	 */
	public abstract boolean ignoreReference();

}
