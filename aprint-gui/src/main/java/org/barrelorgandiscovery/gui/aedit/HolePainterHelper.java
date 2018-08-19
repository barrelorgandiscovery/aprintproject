package org.barrelorgandiscovery.gui.aedit;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Class for drawing the hole, it use a Graphics2D in mm range
 * 
 * @author pfreydiere
 * 
 */
public class HolePainterHelper {

	private VirtualBook virtualbook;
	private Scale gamme;

	public HolePainterHelper(VirtualBook vb) {
		this.virtualbook = vb;
		this.gamme = virtualbook.getScale();
	}

	/**
	 * paint the hole using the current transform of Graphics2D object.
	 * 
	 * @param g
	 * @param n
	 */
	public void paintNote(Graphics2D g, Hole n) {

		assert virtualbook != null;

		double ymm = (n.getTrack() * gamme.getIntertrackHeight()
				+ gamme.getFirstTrackAxis() - gamme.getTrackWidth() / 2);

		double heightmm = gamme.getTrackWidth();

		if (gamme.isPreferredViewedInversed()) {
			ymm = gamme.getWidth() - ymm - heightmm;
		}

		Rectangle2D.Double r = new Rectangle2D.Double(
				((double) n.getTimestamp()) / 1000000 * gamme.getSpeed(), ymm,
				((double) n.getTimeLength()) / 1000000 * gamme.getSpeed(),
				heightmm);

		g.draw(r);

	}
}
