package org.barrelorgandiscovery.extensionsng.perfo.dxf.drawer;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.dxf.canvas.DeviceDrawing;


public abstract class HoleDrawer {

	private static Logger logger = Logger.getLogger(HoleDrawer.class);

	protected DeviceDrawing d;
	private double tailleTrous = Double.NaN;
	private double taillePonts = Double.NaN;
	private double pasdepontsilreste = Double.NaN;

	public HoleDrawer(DeviceDrawing device, double tailleTrous,
			double taillePonts, double pasDePontSilReste) {
		this.d = device;
		this.tailleTrous = tailleTrous;
		this.taillePonts = taillePonts;
		this.pasdepontsilreste = pasDePontSilReste;
	}

	public void drawHole(double ypiste, double halfheight, double x, double endx) {

		double maxhole_length = Double.MAX_VALUE; // 1 cm par défaut

		if (!Double.isNaN(tailleTrous)) {
			maxhole_length = tailleTrous;
		}

		double length = endx - x;

		int nbiterations = (int) (length / maxhole_length);
		if (nbiterations * maxhole_length < length - 0.01) {
			nbiterations++;
		}

		logger.debug("nb de parties pour le trou :" + nbiterations);

		double margin = 0; // 1mm par défaut

		if (!Double.isNaN(taillePonts)) {
			margin = taillePonts / 2;
		}

		double d = x;

		int totaliteration = nbiterations;

		while (nbiterations-- > 0) {

			double end = d + maxhole_length;
			end = Math.min(end, endx);

			if (endx - end < pasdepontsilreste) {
				// traitement du cas d'abandon du dernier pont, s'il
				// reste la longueurmin ...

				// dernier trou à dessiner, en alongeant le trou

				drawPart(ypiste, halfheight, (d == x ? d : d + margin), endx,
						totaliteration > 1, false);

				break;
			}

			drawPart(ypiste, halfheight, (d == x ? d : d + margin),
					(nbiterations == 0 ? end : end - margin), d != x,
					nbiterations > 0);

			d += maxhole_length;
		}

	}

	public abstract void drawPart(double ypiste, double halfheight, double x,
			double endx, boolean pontavant, boolean pontapres);

}
