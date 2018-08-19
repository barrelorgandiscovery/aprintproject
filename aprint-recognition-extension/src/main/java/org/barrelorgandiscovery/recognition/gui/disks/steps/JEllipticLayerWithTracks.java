package org.barrelorgandiscovery.recognition.gui.disks.steps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D.Double;

import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JEllipticLayer;
import org.barrelorgandiscovery.recognition.math.EllipseParameters;
import org.barrelorgandiscovery.scale.Scale;

/**
 * layer adding the display of the borders of tracks
 * 
 * @author pfreydiere
 * 
 */
public class JEllipticLayerWithTracks extends JEllipticLayer {

	/**
	 * instrument scale
	 */
	private Scale scale = null;

	public JEllipticLayerWithTracks() {
		super();
	}

	public void setScale(Scale scale) {
		this.scale = scale;
	}

	@Override
	public void drawLayer(Graphics2D g2d) {
		super.drawLayer(g2d);

		EllipseParameters params = getCurrentEllipseParameters();
		if (params != null && scale != null) {
			// draw the borders of tracks

			Color oldc = g2d.getColor();
			try {
				g2d.setColor(Color.black);

				// first track low line
				double distance = scale.getFirstTrackAxis()
						- scale.getIntertrackHeight() / 2;
				for (int i = 0; i < scale.getTrackNb() + 1; i++) {
					EllipseParameters workingCopy = params.copy();
					workingCopy.a = workingCopy.a / scale.getWidth() * distance;
					workingCopy.b = workingCopy.b / scale.getWidth() * distance;

					Double shape = this.constructEllipseShape(workingCopy);
					g2d.draw(shape);
					distance += scale.getIntertrackHeight();

				}

			} finally {
				g2d.setColor(oldc);
			}

		}

	}

}
