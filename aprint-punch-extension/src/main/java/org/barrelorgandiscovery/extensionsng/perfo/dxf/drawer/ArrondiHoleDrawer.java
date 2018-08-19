package org.barrelorgandiscovery.extensionsng.perfo.dxf.drawer;


import org.barrelorgandiscovery.extensionsng.perfo.dxf.canvas.DeviceDrawing;

import com.vividsolutions.jts.geom.Coordinate;

public class ArrondiHoleDrawer extends HoleDrawer {

	private double arrondi = 0.5;

	public ArrondiHoleDrawer(DeviceDrawing device, double tailleTrous,
			double taillePonts, double pasDePontSilReste) {
		super(device, tailleTrous, taillePonts, pasDePontSilReste);
	}

	@Override
	public void drawPart(double ypiste, double halfheight, double x,
			double endx, boolean p, boolean p2) {

		double xstart = x;

		boolean pontavant = false;
		boolean pontapres = false;
		
		if (!pontavant) {
			xstart += arrondi;
		}
		d.moveTo(xstart, ypiste + halfheight);

		xstart = endx;
		if (!pontapres) {
			// pas de pont
			xstart = endx - arrondi;

		}
		d.drawTo(xstart, ypiste + halfheight);

		if (!pontapres) {

			// dessin d'un arc
			d.drawArrondi(new Coordinate(xstart, ypiste + halfheight),
					new Coordinate(xstart, ypiste - halfheight), -arrondi);
		} else {
			d.drawTo(xstart, ypiste - halfheight);
		}

		xstart = x;
		if (!pontavant) {
			xstart = x + arrondi;
		}

		d.drawTo(xstart, ypiste - halfheight);

		if (!pontavant) {
			// dessin d'un arc
			d.drawArrondi(new Coordinate(xstart, ypiste - halfheight),
					new Coordinate(xstart, ypiste + halfheight), -arrondi);
		} else {
			d.drawTo(xstart, ypiste + halfheight);
		}

	}

}
