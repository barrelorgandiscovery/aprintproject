package org.barrelorgandiscovery.tools;

import java.awt.*;
import javax.swing.*;

/**
 * Class getted from internet, contribution by Nathan Pruett, for JButton with a
 * background image.
 * 
 */
public class JImageButton extends JButton {

	Image backgroundImage;

	public JImageButton() {
		super();
	}

	public JImageButton(Action a) {
		super(a);
	}

	public JImageButton(Icon icon) {
		super(icon);
	}

	public JImageButton(String text) {
		super(text);
	}

	public JImageButton(String text, Icon icon) {
		super(text, icon);
	}

	public void setBackgroundImage(Image image) {
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(image, 0);
		try {
			mt.waitForAll();
			backgroundImage = image;
		} catch (InterruptedException x) {
			System.err
					.println("Specified background image could not be loaded.");
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Color saved = g.getColor();
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(saved);

		if (backgroundImage != null) {
			int imageX = (getWidth() - backgroundImage.getWidth(this)) / 2;
			int imageY = (getHeight() - backgroundImage.getHeight(this)) / 2;
			g.drawImage(backgroundImage, imageX, imageY, this);
		}

		if (!getText().equals("")) {
			g.drawString(super.getText(), getWidth() / 2, getHeight() / 2);
		}

		if (getIcon() != null) {
			Icon icon = getIcon();
			icon.paintIcon(this, g, 10, 10);
		}
	}

	public Dimension getPreferredSize() {
		Dimension oldSize = super.getPreferredSize();
		Dimension newSize = new Dimension();
		Dimension returnSize = new Dimension();

		if (backgroundImage != null) {
			newSize.width = backgroundImage.getWidth(this) + 1;
			newSize.height = backgroundImage.getHeight(this) + 1;
		}

		if (oldSize.height > newSize.height) {
			returnSize.height = oldSize.height;
		} else {
			returnSize.height = newSize.height;
		}

		if (oldSize.width > newSize.width) {
			returnSize.width = oldSize.width;
		} else {
			returnSize.width = newSize.width;
		}

		return (returnSize);
	}
}
