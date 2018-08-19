package org.barrelorgandiscovery.gui.aedit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Component layer for showing the registration on the book ...
 * 
 * @author Freydiere Patrice
 * 
 */
public class RegistrationSectionLayer implements VirtualBookComponentLayer,
		ToolTipVirtualBookComponentLayer {

	private static Logger logger = Logger
			.getLogger(RegistrationSectionLayer.class);

	private JVirtualBookComponent vbc;

	public RegistrationSectionLayer() {

	}

	private boolean isVisible = true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayer#draw(java.awt.Graphics,
	 *      org.barrelorgandiscovery.gui.aedit.JVirtualBookComponent)
	 */
	public void draw(Graphics g, JVirtualBookComponent jbookcomponentreference) {

		try {

			if (!isVisible)
				return;

			this.vbc = jbookcomponentreference;

			logger.debug("draw registration section layer");

			VirtualBook virtualBook = jbookcomponentreference.getVirtualBook();
			if (virtualBook == null)
				return;

			Rectangle clipBounds = g.getClipBounds(new Rectangle());

			long start = 0;
			long end = virtualBook.getLength();

			if (clipBounds != null) {

				start = jbookcomponentreference
						.MMToTime(jbookcomponentreference
								.convertScreenXToCarton(clipBounds.x));

				end = jbookcomponentreference
						.MMToTime(jbookcomponentreference
								.convertScreenXToCarton(clipBounds.x
										+ clipBounds.width));

			}

			int indexstart = virtualBook.findSection(start);
			int indexend = virtualBook.findSection(end);

			logger.debug("index start " + indexstart);
			logger.debug("index end " + indexend);

			int min = Math.min(indexstart, indexend);
			int max = Math.max(indexstart, indexend);

			if (min < 0)
				min = 0;

			if (max > virtualBook.getSectionCount() - 1)
				max = virtualBook.getSectionCount() - 1;

			for (int i = min; i <= max; i++) {

				long sectionStart = virtualBook.getSectionStart(i);

				int x = jbookcomponentreference
						.convertCartonToScreenX(jbookcomponentreference
								.timeToMM(sectionStart));
				int y = jbookcomponentreference.convertCartonToScreenY(0);
				int ymax = jbookcomponentreference
						.convertCartonToScreenY(virtualBook.getScale()
								.getWidth());

				Color oldcolor = g.getColor();
				try {
					g.setColor(Color.RED);

					g.drawLine(x, y, x, ymax);

					String[] sectionRegisters = virtualBook
							.getSectionRegisters(i);

					for (int j = 0; j < sectionRegisters.length; j++) {
						g.drawString(sectionRegisters[j], x, y + 20 * (j+1));
					}

				} finally {
					g.setColor(oldcolor);
				}
			}

		} catch (Throwable ex) {
			logger.error("error in displaying layer .. " + ex.getMessage(), ex);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayer#isVisible()
	 */
	public boolean isVisible() {
		return isVisible;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayer#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		isVisible = visible;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.gui.aedit.ToolTipVirtualBookComponentLayer#getToolTipInfo(double,
	 *      double)
	 */
	public String getToolTipInfo(double x, double y) {
		if (!isVisible || vbc == null)
			return null;

		VirtualBook virtualBook = vbc.getVirtualBook();
		if (virtualBook == null)
			return null;

		int findSection = virtualBook.findSection(vbc.MMToTime(x));
		if (findSection == -1) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Registration :");
		String[] registration = virtualBook.getSectionRegisters(findSection);
		for (String r : registration) {
			sb.append(r).append("-");
		}

		return sb.toString();
	}

}
