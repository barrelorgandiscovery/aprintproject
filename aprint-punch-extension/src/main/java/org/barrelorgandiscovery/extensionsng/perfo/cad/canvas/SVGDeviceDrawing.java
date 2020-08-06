package org.barrelorgandiscovery.extensionsng.perfo.cad.canvas;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

/**
 * thisClass export drawing into SVG file format
 * 
 * @author pfreydiere
 *
 */
public class SVGDeviceDrawing extends DeviceDrawing {

	private Map<String, List<Geometry>> content = new HashMap<>();

	private SVGGraphics2D device;

	public SVGDeviceDrawing(double width, double height) {
		device = new SVGGraphics2D(toSVGUnit(width), toSVGUnit(height));
	}

	/**
	 * convert to svg graphics space
	 * 
	 * @param x
	 * @return
	 */
	int toSVGUnit(double x) {
		return (int) (x * 10);
	}

	/**
	 * draw geometry
	 * @param g the geometry
	 */
	private void draw(Geometry g) {

		if (!(g instanceof LineString)) {
			throw new RuntimeException("unsupported geometry " + g);
		}

		LineString ls = (LineString) g;
		Coordinate[] coordinates = ls.getCoordinates();

		if (coordinates.length == 0) {
			return;
		}
		assert coordinates.length > 0;

		Path2D.Double path = new Path2D.Double();
		path.moveTo(coordinates[0].x, coordinates[0].y);
		for (int i = 1; i < coordinates.length; i++) {
			path.lineTo(coordinates[i].x, coordinates[i].y);
		}
		device.draw(path);
	}

	/*
	 * (non-Javadoc)
	 * @see org.barrelorgandiscovery.extensionsng.perfo.cad.canvas.DeviceDrawing#addObject(com.vividsolutions.jts.geom.Geometry)
	 */
	@Override
	protected void addObject(Geometry g) {
		draw(g);
	}

	/*
	 * (non-Javadoc)
	 * @see org.barrelorgandiscovery.extensionsng.perfo.cad.canvas.DeviceDrawing#write(java.io.File, java.lang.String[])
	 */
	public void write(File file, String[] layers) throws Exception {
		SVGUtils.writeToSVG(file, device.getSVGElement());
	}

}
