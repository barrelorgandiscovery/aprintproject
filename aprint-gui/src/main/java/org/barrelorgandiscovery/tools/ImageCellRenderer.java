package org.barrelorgandiscovery.tools;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

public class ImageCellRenderer extends DefaultCellRenderer {

	public ImageCellRenderer() {

	}

	@Override
	protected String convertToString(Object value) {
		return "";
	}

	@Override
	protected Icon convertToIcon(Object value) {
		if (value == null)
			return null;

		try {
			Image im = ImageTools.crop(30, 30, (Image) value);
			return new ImageIcon(im);
		} catch (Exception ex) {
			return new ImageIcon(new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB));
		}
	}
}
