package org.barrelorgandiscovery.gui.aedit.markers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Iterator;

import org.barrelorgandiscovery.gui.aedit.JVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayer;
import org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayerName;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.AbstractEvent;
import org.barrelorgandiscovery.virtualbook.MarkerEvent;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * layer to display layers.
 * @author pfreydiere
 *
 */
public class MarkerLayer implements VirtualBookComponentLayer,
		VirtualBookComponentLayerName {

	public MarkerLayer() {
	}

	public String getDisplayName() {
		return "Display Markers";
	}

	public void draw(Graphics g, JVirtualBookComponent jbookcomponentreference) {

		if (!visible)
			return;

		if (jbookcomponentreference == null)
			return;

		if (!(g instanceof Graphics2D))
			return;

		Graphics2D g2d = (Graphics2D) g;
		Stroke oldStroke = g2d.getStroke();
		try {

			Color oldcolor = g2d.getColor();
			try {
				g2d.setColor(Color.GREEN);
				g2d.setStroke(new BasicStroke(3));

				// Récupération de l'étendue à afficher ...
				Rectangle rect = g.getClipBounds(new Rectangle());

				double start = jbookcomponentreference
						.convertScreenXToCarton(rect.x);
				double end = jbookcomponentreference
						.convertScreenXToCarton(rect.x + rect.width);

				VirtualBook vb = jbookcomponentreference.getVirtualBook();
				if (vb == null)
					return;

				Scale scale = vb.getScale();

				long starttime = vb.getScale().mmToTime(start);
				long endtime = vb.getScale().mmToTime(end);

				ArrayList<AbstractEvent> events = vb.findEvents(starttime,
						endtime, MarkerEvent.class);
				for (Iterator iterator = events.iterator(); iterator.hasNext();) {
					MarkerEvent mEvent = (MarkerEvent) iterator.next();

					long ts = mEvent.getTimestamp();
					String markerName = mEvent.getMarkerName();

					// draw the line for the markers

					int x = jbookcomponentreference.convertCartonToScreenX(scale.timeToMM(ts));
					int y1 = jbookcomponentreference.convertCartonToScreenY(0);
					int y2 = jbookcomponentreference.convertCartonToScreenY(scale.getWidth());
					g2d.drawLine(x, y1, x, y2);

					// draw the marker text

					g2d.drawString(markerName, x, y1 + 20);

				}
			} finally {
				g2d.setColor(oldcolor);
			}
		} finally {
			g2d.setStroke(oldStroke);
		}
	}

	/**
	 * is the layer visible
	 */
	private boolean visible = true;

	/**
	 * set visible or not the layer
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * is the layer visible ?
	 */
	public boolean isVisible() {
		return visible;
	}

}
