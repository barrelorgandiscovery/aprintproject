package org.barrelorgandiscovery.virtualbook.rendering;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.InputStream;
import java.io.Serializable;

/**
 * This class define the book appearence
 * 
 * @author Freydiere Patrice
 * 
 */
public class VirtualBookRendering implements Serializable {

	public VirtualBookRendering() {

	}

	public InputStream getBackgroundImage() {
		return getClass().getResourceAsStream("cartontrame.jpg");
	}

	private Color carton_color = new Color(216, 181, 141); // new Color(250,

	// 220, 110);

	public Color getDefaultBookColor() {
		return carton_color;
	}

	private Color shallow = new Color(30,30,30);
	
	/**
	 * This function render a hole
	 * 
	 * @param g2d
	 * @param hole
	 */
	public void renderHole(Graphics2D g2d, int x, int y, int width, int height,
			boolean isSelected) {

		if (!isSelected) {
			g2d.setColor(shallow);
			g2d.fillRoundRect(x, y, width, height, 3, 3);
			g2d.setColor(Color.white);
			g2d.fillRoundRect(x + 2, y + 2, width - 2, height - 2, 3, 3);
		} else {
			g2d.setColor(shallow);
			g2d.fillRoundRect(x, y, width, height, 3, 3);
			g2d.setColor(Color.red);
			g2d.fillRoundRect(x + 2, y + 2, width - 2, height - 2, 3, 3);
		}
	}

	public String getName() {
		return "default";
	}

}
