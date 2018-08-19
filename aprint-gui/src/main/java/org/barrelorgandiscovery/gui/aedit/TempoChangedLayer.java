package org.barrelorgandiscovery.gui.aedit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.barrelorgandiscovery.virtualbook.AbstractEvent;
import org.barrelorgandiscovery.virtualbook.TempoChangeEvent;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.sigs.ComputedSig;

public class TempoChangedLayer implements VirtualBookComponentLayer {

	private boolean visible = false;

	public void draw(Graphics g, JVirtualBookComponent jbookcomponentreference) {

		if (!visible)
			return;

		VirtualBook vb = jbookcomponentreference.getVirtualBook();
		if (vb == null)
			return;

		// get the current time stamp to display
		Rectangle bounds = g.getClipBounds(new Rectangle());
		if (bounds == null)
			return;

		double offset = jbookcomponentreference
				.convertScreenXToCarton(bounds.x);
		long tsStart = vb.getScale().mmToTime(offset);
		long tLength = vb.getScale().mmToTime(
				jbookcomponentreference.pixelToMm(bounds.width));

		ArrayList<AbstractEvent> evts = vb.findEvents(tsStart, tsStart
				+ tLength, TempoChangeEvent.class);
		if (evts == null)
			return;

		Graphics2D g2d = (Graphics2D) g;
		Color oldC = g2d.getColor();
		try {
			g2d.setColor(Color.green);

			for (Iterator iterator = evts.iterator(); iterator.hasNext();) {
				AbstractEvent abstractEvent = (AbstractEvent) iterator.next();

				TempoChangeEvent tce = (TempoChangeEvent) abstractEvent;

				double sx = vb.getScale().timeToMM(tce.getTimestamp());
				double sy = vb.getScale().getWidth();
				int posx = jbookcomponentreference.convertCartonToScreenX(sx);
				g2d.drawLine(posx, 0, posx,
						jbookcomponentreference.convertCartonToScreenY(sy));
				g2d.drawString("[Tempo Changed] : " + tce.getNoirLength(),
						posx,
						jbookcomponentreference.convertCartonToScreenY(6.0));
			}

		} finally {
			g2d.setColor(oldC);
		}

	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return this.visible;
	}

}
