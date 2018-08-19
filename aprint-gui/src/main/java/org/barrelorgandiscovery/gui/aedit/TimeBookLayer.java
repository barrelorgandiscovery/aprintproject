package org.barrelorgandiscovery.gui.aedit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * This implementation of the layer put the time in the component (with minutes /
 * secondes)
 * 
 * @author Freydiere Patrice
 * 
 */
public class TimeBookLayer implements VirtualBookComponentLayer {

	/**
	 * Constructor
	 */
	public TimeBookLayer() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.gui.aedit.VirtualBookComponentLayer#draw(java.awt.Graphics,
	 *      fr.freydierepatrice.gui.aedit.JVirtualBookComponent)
	 */
	public void draw(Graphics g, JVirtualBookComponent jcarton) {

		// Récupération de l'étendue visualisée ...
		Rectangle r = g.getClipBounds(new Rectangle());

		if (jcarton.getVirtualBook() == null)
			return;

		long starttimestamp = jcarton.MMToTime(jcarton
				.convertScreenXToCarton(r.x));
		long endtimestamp = jcarton.MMToTime(jcarton.convertScreenXToCarton(r.x
				+ r.width));

		int startindex = (int) starttimestamp / 1000000;
		int stopindex = (int) endtimestamp / 1000000;

		Color lastcolor = g.getColor();
		g.setColor(Color.blue);
		for (int i = startindex; i <= stopindex; i++) {

			int x = jcarton.convertCartonToScreenX(jcarton
					.timestampToMM(i * 1000000));
			g.drawLine(x, r.y, x, r.y + r.height);

			// dessin du temp
			g.drawString("" + (i / 60) + ":" + (i % 60), x, jcarton
					.convertCartonToScreenY(5.0));

		}
		g.setColor(lastcolor);
	}

	private boolean visible = true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.gui.aedit.VirtualBookComponentLayer#isVisible()
	 */
	public boolean isVisible() {
		return visible;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.gui.aedit.VirtualBookComponentLayer#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

}
