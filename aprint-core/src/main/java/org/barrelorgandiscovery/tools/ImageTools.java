package org.barrelorgandiscovery.tools;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * Image Tool Class
 * 
 * @author Freydiere Patrice
 * 
 */
public class ImageTools {

	/**
	 * Load image and reduce it
	 * 
	 * @param imageUrl  the image url
	 * @param maxwidth
	 * @param maxheight
	 * @return the read buffered image
	 * @throws Exception
	 */
	public static BufferedImage loadImageAndCrop(URL imageUrl, int maxwidth, int maxheight) throws Exception {
		Image image = Toolkit.getDefaultToolkit().createImage(imageUrl);

		JLabel l = new JLabel();
		l.setIcon(new ImageIcon(image));
		MediaTracker mt = new MediaTracker(l);
		mt.waitForAll();

		BufferedImage bi = crop(maxwidth, maxheight, image);

		return bi;
	}

	/**
	 * read image stream and crop (warn this method load all the image content in
	 * memory)
	 * 
	 * @param imageFile
	 * @param maxwidth
	 * @param maxheight
	 * @return
	 * @throws Exception
	 */
	public static BufferedImage loadImageAndCrop(InputStream imageFile, int maxwidth, int maxheight) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StreamsTools.copyStream(imageFile, baos);
		Image image = Toolkit.getDefaultToolkit().createImage(baos.toByteArray());
		JLabel l = new JLabel();
		l.setIcon(new ImageIcon(image));
		MediaTracker mt = new MediaTracker(l);
		mt.waitForAll();

		BufferedImage bi = crop(maxwidth, maxheight, image);

		return bi;
	}

	public static BufferedImage loadImageAndCrop(File imageFile, int maxwidth, int maxheight) throws Exception {
		Image image = Toolkit.getDefaultToolkit().createImage(imageFile.getAbsolutePath());

		JLabel l = new JLabel();
		l.setIcon(new ImageIcon(image));
		MediaTracker mt = new MediaTracker(l);
		mt.waitForAll();

		BufferedImage bi = crop(maxwidth, maxheight, image);

		return bi;

	}

	public static BufferedImage crop(int maxwidth, int maxheight, Image image) {
		return crop(maxwidth, maxheight, image, true);
	}

	public static BufferedImage crop(int maxwidth, int maxheight, Image image, boolean quality) {
		int width = image.getWidth(null);
		int height = image.getHeight(null);

		double ratio = (1.0 * width) / height;

		int newwidth;
		int newheight;

		if (ratio > 1) {
			// width is longer than height
			// width is dimensionning
			newwidth = maxwidth;
			newheight = (int) (newwidth / ratio);
		} else {
			newheight = maxheight;
			newwidth = (int) (ratio * newheight);
		}

		BufferedImage bi = new BufferedImage(newwidth, newheight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = bi.createGraphics();
		try {

			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

			g.drawImage(image, 0, 0, newwidth, newheight, null);
		} finally {
			g.dispose();
		}
		return bi;
	}

	/**
	 * Load an image associated to a class ressource
	 * 
	 * @param associatedClass the associated class
	 * @param resourcename    the resource name
	 * @return the image
	 * @throws Exception an exception is raised if the resource is not found or if
	 *                   there is a probleme loading the resource
	 */
	public static BufferedImage loadImage(Class associatedClass, String resourcename) throws Exception {
		if (associatedClass == null || resourcename == null) {
			throw new Exception("null parameters passed");
		}

		URL resourceUrl = associatedClass.getResource(resourcename);
		if (resourceUrl == null)
			throw new Exception("resource " + resourcename + " not found associated to " + associatedClass);

		return loadImage(resourceUrl);
	}

	/**
	 * Load an image associated to a class ressource
	 * 
	 * @param associatedClass the associated class
	 * @param resourcename    the resource name
	 * @return the image
	 * @throws Exception an exception is raised if the resource is not found or if
	 *                   there is a probleme loading the resource
	 */
	public static BufferedImage loadImageIfExists(Class associatedClass, String resourcename) throws Exception {
		if (associatedClass == null || resourcename == null) {
			throw new Exception("null parameters passed");
		}

		URL resourceUrl = associatedClass.getResource(resourcename);
		if (resourceUrl == null)
			return null;

		assert resourceUrl != null;
		return loadImage(resourceUrl);
	}

	/**
	 * load an icon
	 * 
	 * @param associatedClass
	 * @param resourceName
	 * @return
	 * @throws Exception
	 */
	public static ImageIcon loadIcon(Class associatedClass, String resourceName) throws Exception {
		BufferedImage image = loadImage(associatedClass, resourceName);
		if (image != null) {
			return new ImageIcon(image);
		}

		return null;
	}

	/**
	 * load icon if exists
	 * 
	 * @param associatedClass
	 * @param resourceName
	 * @return
	 * @throws Exception
	 */
	public static ImageIcon loadIconIfExists(Class associatedClass, String resourceName) throws Exception {
		BufferedImage image = loadImageIfExists(associatedClass, resourceName);
		if (image != null) {
			return new ImageIcon(image);
		}
		return null;
	}

	public static BufferedImage loadImage(URL url) throws Exception {
		if (url == null) {
			throw new Exception("null image url passed for loading the image");
		}
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image image = kit.createImage(url);
		return loadImage(image);
	}

	public static BufferedImage loadImage(Image image) throws Exception {

		if (image == null)
			return null;

		JLabel l = new JLabel();
		l.setIcon(new ImageIcon(image));
		MediaTracker mt = new MediaTracker(l);
		mt.waitForAll();
		int width = image.getWidth(null);
		int height = image.getHeight(null);

		if (width <= -1 || height <= -1) {
			throw new Exception("image cannot be loaded, width or height incorrect");
		}

		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = bi.createGraphics();
		try {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
			g.setBackground(new Color(255, 255, 255, 0));
			g.clearRect(0, 0, width, height);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

			g.drawImage(image, 0, 0, width, height, null);
		} finally {
			g.dispose();
		}

		return bi;
	}

}
