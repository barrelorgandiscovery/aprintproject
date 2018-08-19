package org.barrelorgandiscovery.gui.aedit.snapping;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class HolesSnappingEnvironnement implements ISnappingEnvironment {

	private static Logger logger = Logger
			.getLogger(HolesSnappingEnvironnement.class);

	private JVirtualBookScrollableComponent vbc;

	private double tolerance = 1; // mm

	public HolesSnappingEnvironnement(JVirtualBookScrollableComponent c) {
		this.vbc = c;
	}

	public boolean snapPosition(final Double position) {

		boolean snapped = false;

		if (logger.isDebugEnabled())
			logger.debug("position to snap : " + position.x + " ," + position.y);

		VirtualBook virtualBook = vbc.getVirtualBook();

		long start = vbc.MMToTime(position.x - tolerance);
		long length = vbc.MMToTime(2 * tolerance);

		ArrayList<Hole> result = virtualBook.findHoles(start, length);

		if (logger.isDebugEnabled())
			logger.debug("" + result.size() + " holes found");

		ArrayList<Point2D.Double> r = new ArrayList<Double>();

		for (Hole h : result) {
			r.add(new Double(vbc.timestampToMM(h.getTimestamp()), vbc
					.trackToMM(h.getTrack())));
			r.add(new Double(vbc.timestampToMM(h.getTimestamp()
					+ h.getTimeLength()), vbc.trackToMM(h.getTrack())));
		}

		if (logger.isDebugEnabled()) {
			logger.debug("candidats found :" + r.size());
		}

		TreeSet<Double> ts = new TreeSet<Double>(new Comparator<Double>() {
			public int compare(Double o1, Double o2) {

				return (Math.abs(position.x - o1.x) < Math.abs(position.x
						- o2.x) ? -1 : 1);

			}
		});
		ts.addAll(r);

		if (!ts.isEmpty()) {
			Double firstPoint = ts.first();

			if (logger.isDebugEnabled())
				logger.debug("position to snap : " + firstPoint.x + " ,"
						+ firstPoint.y);

			if (Math.abs(firstPoint.x - position.x) < tolerance) {
				position.x = firstPoint.x;
				if (logger.isDebugEnabled())
					logger.debug("point snapped to " + position.x);
				snapped = true;
			}
		}

		return snapped;
	}

	public String getName() {
		return "Hole Snapping";
	}

	public void drawFeedBack(Graphics2D g) {
		// TODO Auto-generated method stub

	}

}
