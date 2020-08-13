package org.barrelorgandiscovery.extensionsng.perfo.cad.drawer;


import org.barrelorgandiscovery.extensionsng.perfo.cad.canvas.DeviceDrawing;

import com.vividsolutions.jts.geom.Coordinate;

public class RondHoleDrawer extends HoleDrawer {

	public RondHoleDrawer(DeviceDrawing device, double tailleTrous,
			double taillePonts, double pasDePontSilReste) {
		super(device, tailleTrous, taillePonts, pasDePontSilReste);
	}

	@Override
	public void drawPart(double ypiste, double halfheight, double x,
			double endx, boolean pontavant, boolean pontapres) {

		double xstart = x + halfheight;

		d.moveTo(xstart, ypiste + halfheight);

		xstart = endx - halfheight;

		d.drawTo(xstart, ypiste + halfheight);

		// dessin d'un arc
		d.drawArrondi(new Coordinate(xstart, ypiste + halfheight),
				new Coordinate(xstart, ypiste - halfheight), -halfheight);

		xstart = x + halfheight;

		d.drawTo(xstart, ypiste - halfheight);

		// dessin d'un arc
		d.drawArrondi(new Coordinate(xstart, ypiste - halfheight),
				new Coordinate(xstart, ypiste + halfheight), -halfheight);

		d.flushLine();
	}

}
