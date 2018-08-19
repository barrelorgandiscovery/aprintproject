package org.barrelorgandiscovery.virtualbook.rendering;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.InputStream;

public class PaperBookRendering extends VirtualBookRendering {

	public PaperBookRendering() {
	}

	@Override
	public InputStream getBackgroundImage() {
		return getClass().getResourceAsStream("blankbg.jpg");
	}

	@Override
	public Color getDefaultBookColor() {
		return Color.WHITE;
	}

	/**
	 * This function render a hole
	 * 
	 * @param g2d
	 * @param hole
	 */
	public void renderHole(Graphics2D g2d, int x, int y, int width, int height,
			boolean isSelected) {

		if (!isSelected) {
			g2d.setColor(Color.GRAY);
		} else {
			g2d.setColor(Color.red);
		}

		double continueRatio = 0.3; // height * 0.3

		if (width < height * 2) {
			g2d.fillRoundRect(x, y, width, height, height, height);
		} else {

			int i = (int)((1+continueRatio) * height);
			g2d.fillRoundRect(x, y, (int) (1.5 * height), height, height,
					height);

			while (i <= width) {

				if ((width - i) < (2 * height)) {
					
					// en to draw ...

					int lwidth = (width - i);
					if (lwidth < height) {
						lwidth = height;
						i = (width - height);
					}
					
					g2d.fillRoundRect(i + x, y, lwidth, height, height, height);

					break;

				} else {

					g2d.fillRoundRect(i + x, y, height, height, height, height);

				}

				i += (int) (height * (1.0+continueRatio));
			}
		}

		// g2d.setColor(Color.black);
		// g2d.fillRoundRect(x + 1, y + 1, width - 1, height - 1, height,
		// height);

	}

	@Override
	public String getName() {
		return "paper";
	}

}
