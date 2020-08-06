package org.barrelorgandiscovery.extensionsng.perfo.cad.drawer;

import org.barrelorgandiscovery.extensionsng.perfo.cad.canvas.DeviceDrawing;


public class RectangularHoleDrawer extends HoleDrawer {

	public RectangularHoleDrawer(DeviceDrawing device, double tailleTrous,
			double taillePonts, double pasDePontSilReste) {
		super(device, tailleTrous, taillePonts, pasDePontSilReste);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void drawPart(double ypiste, double halfheight, double x,
			double endx, boolean pontavant, boolean pontapres) {
		d.drawRectangleHole(ypiste, halfheight, x, endx);
	}

}
