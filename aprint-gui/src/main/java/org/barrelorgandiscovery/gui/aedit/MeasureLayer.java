package org.barrelorgandiscovery.gui.aedit;

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Display the measures in mm on the book
 * 
 * @author Freydiere Patrice
 */
public class MeasureLayer implements VirtualBookComponentLayer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.aedit.ComponentCartonLayer#draw(java.awt.Graphics,
	 *      fr.freydierepatrice.aedit.JComponentCarton)
	 */
	public void draw(Graphics g, JVirtualBookComponent jcarton) {

		// Récupération de l'étendue à afficher ...
		Rectangle rect = g.getClipBounds(new Rectangle());

		double start = jcarton.convertScreenXToCarton(rect.x);
		double end = jcarton.convertScreenXToCarton(rect.x + rect.width);

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
