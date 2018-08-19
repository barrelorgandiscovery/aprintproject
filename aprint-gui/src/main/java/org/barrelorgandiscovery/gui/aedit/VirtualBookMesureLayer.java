package org.barrelorgandiscovery.gui.aedit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import org.barrelorgandiscovery.virtualbook.SigsEvaluator;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.sigs.ComputedSig;

/**
 * Layer for displaying measures
 * 
 * @author use
 * 
 */
public class VirtualBookMesureLayer implements VirtualBookComponentLayer,
		ToolTipVirtualBookComponentLayer {

	private boolean visible = false;

	private JVirtualBookComponent lastComponent;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayer#draw(java
	 * .awt.Graphics, org.barrelorgandiscovery.gui.aedit.JVirtualBookComponent)
	 */
	public void draw(Graphics g, JVirtualBookComponent jbookcomponentreference) {

		if (!visible)
			return;

		this.lastComponent = jbookcomponentreference;

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

		SigsEvaluator se = new SigsEvaluator();

		List<ComputedSig> results = se.computeSigs(vb.getOrderedEventsByRef());
		int current = 0;
		while (current < results.size()
				&& results.get(current).timeStamp < tsStart) {
			current++;
		}

		current--;
		if (current >= results.size())
			return;

		if (results.size() == 0)
			return;

		if (current < 0) {
			current = 0;
		}

		Graphics2D g2d = (Graphics2D) g;
		Color oldC = g2d.getColor();
		try {
			g2d.setColor(Color.black);
			ComputedSig currentSG = results.get(current);
			long timeS = currentSG.timeStamp;
			int currentSig = currentSG.sigNumber;
			while (timeS < tsStart + tLength) {

				double sx = vb.getScale().timeToMM(timeS);
				double sy = vb.getScale().getWidth();
				int posx = jbookcomponentreference.convertCartonToScreenX(sx);
				g2d.drawLine(posx, 0, posx,
						jbookcomponentreference.convertCartonToScreenY(sy));
				g2d.drawString("[" + currentSig + "]", posx,
						jbookcomponentreference.convertCartonToScreenY(6.0));

				timeS = timeS + currentSG.measureLength;
				currentSig++;
				if (current + 1 < results.size()) {
					ComputedSig nextOne = results.get(current + 1);
					if (nextOne.timeStamp < timeS) {
						current++;
						currentSG = nextOne;
						currentSig = currentSG.sigNumber;
						timeS = currentSG.timeStamp;
					}
				}
			}

		} finally {
			g2d.setColor(oldC);
		}
	}

	/**
	 * set the layer is visible
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * return if the layer is visible
	 */
	public boolean isVisible() {
		return visible;
	}

	public String getToolTipInfo(double x, double y) {

		if (lastComponent == null)
			return null;

		// time stamp
		long ts = lastComponent.MMToTime(x);

		VirtualBook vb = lastComponent.getVirtualBook();

		SigsEvaluator se = new SigsEvaluator();

		List<ComputedSig> results = se.computeSigs(vb.getOrderedEventsByRef());
		int current = 0;
		while (current < results.size() 
				&& results.get(current).timeStamp < ts) {
			current++;
		}

		if (--current < results.size() && current >=0) {
			ComputedSig sig = results.get(current);
			
			int ns = sig.sigNumber;
			
			ns += ((ts - sig.timeStamp) / sig.measureLength);
			
			
			return "Measure " + ns ;
		}

		return null;
	}

}
