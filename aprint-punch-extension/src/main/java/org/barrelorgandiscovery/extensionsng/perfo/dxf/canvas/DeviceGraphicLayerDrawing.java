package org.barrelorgandiscovery.extensionsng.perfo.dxf.canvas;

import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.GraphicsLayer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class DeviceGraphicLayerDrawing extends DeviceDrawing {

	private static Logger logger = Logger
			.getLogger(DeviceGraphicLayerDrawing.class);

	private GraphicsLayer gl;
	private double scalewidth;

	public DeviceGraphicLayerDrawing(GraphicsLayer gl, double scalewidth) {
		this.gl = gl;
		this.scalewidth = scalewidth;
	}

	@Override
	protected void addObject(Geometry g) {

		if (g instanceof LineString) {
			LineString l = (LineString) g;
			Coordinate[] coords = l.getCoordinates();

			if (coords.length == 2) {
				gl.add(
				new Line2D.Float((float) coords[0].x,
						(float) (scalewidth - coords[0].y),
						(float) coords[1].x, (float) (scalewidth - coords[1].y))
				);

			} else {

				GeneralPath gp = null;

				if (coords.length > 0) {
					gp = new GeneralPath();
					gp.moveTo((float) coords[0].x,
							(float) (scalewidth - coords[0].y));
					for (int i = 1; i < coords.length; i++) {
						gp.lineTo((float) coords[i].x,
								(float) (scalewidth - coords[i].y));
					}

					gl.add(gp);
				}
			}
		} else {
			logger.error("error in adding geometry " + g
					+ ", this geometry is not supported in "
					+ getClass().getName());
		}

	}
}
