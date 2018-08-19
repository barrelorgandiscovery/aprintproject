package org.barrelorgandiscovery.gui.aedit.snapping;

import java.awt.Graphics2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Iterator;

import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.AbstractEvent;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class EventSnappingEnvironment implements ISnappingEnvironment {

	private JVirtualBookScrollableComponent vbc;
	private Class abstractEventClass;

	private int tolerance = 3; // 3 mm

	public EventSnappingEnvironment(JVirtualBookScrollableComponent vbc,
			Class abstractEventClass) {
		assert vbc != null;
		assert abstractEventClass != null;
		this.vbc = vbc;
		this.abstractEventClass = abstractEventClass;
	}

	public String getName() {
		return "Events";
	}

	public boolean snapPosition(Double position) {

		if (position == null)
			return false;

		VirtualBook vb = vbc.getVirtualBook();
		if (vb == null)
			return false;

		Scale scale = vb.getScale();

		long timex = scale.mmToTime(position.x);
		ArrayList<AbstractEvent> events = vb.findEvents(
				timex - scale.mmToTime(tolerance),
				timex + scale.mmToTime(tolerance), abstractEventClass);

		AbstractEvent current = null;

		// take the nearest
		for (Iterator iterator = events.iterator(); iterator.hasNext();) {
			AbstractEvent abstractEvent = (AbstractEvent) iterator.next();
			if (current == null)
				current = abstractEvent;

			if (Math.abs(current.getTimestamp() - timex) > Math
					.abs(abstractEvent.getTimestamp() - timex)) {
				current = abstractEvent;
			}
		}

		if (current != null) {
			position.x = scale.timeToMM(current.getTimestamp());
			return true; // position modified
		}

		return false;
	}

	public void drawFeedBack(Graphics2D g) {

	}

}
