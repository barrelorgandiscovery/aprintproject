package org.barrelorgandiscovery.images.books.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * this class implements access to part of an image, using a decomposed image in
 * tiles, tiles are saved into a subfolder, with named conventions
 * 
 * @author pfreydiere
 *
 */
public class TiledImage implements ITiledImage, IFileBasedTiledImage {

	private File imagePath;
	private File outputRecognitionProject;
	private int maxHeight;

	public TiledImage(File imagePath, File outputRecognitionProject) throws Exception {
		this(imagePath, outputRecognitionProject, (int) readImageSize(new FileInputStream(imagePath)).getHeight());
	}

	public TiledImage(File imagePath, File outputRecognitionProject, int maxHeight) throws Exception {
		assert imagePath != null;

		this.imagePath = imagePath;
		this.maxHeight = maxHeight;

		computeMetrics();

		if (outputRecognitionProject != null) {
			setTiledImageDirectory(outputRecognitionProject);
		}

	}

	protected void setTiledImageDirectory(File tileImageDirectory) {
		assert tileImageDirectory != null;
		assert tileImageDirectory.exists();
		assert tileImageDirectory.isDirectory();

		this.outputRecognitionProject = tileImageDirectory;
	}

	private double whratio;

	private int imagecount;

	private Dimension fullImageDimension;

	protected File getImagePath() {
		return imagePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.recognition.gui.books.tools.ITiledImage#getWidth()
	 */
	@Override
	public int getWidth() {
		return imagecount * maxHeight;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.recognition.gui.books.tools.ITiledImage#getHeight()
	 */
	@Override
	public int getHeight() {
		return maxHeight;
	}

	private void computeMetrics() throws Exception {

		fullImageDimension = null;
		// read full image size
		FileInputStream fis = new FileInputStream(imagePath);
		try {
			fullImageDimension = readImageSize(fis);
		} finally {
			fis.close();
		}

		if (fullImageDimension == null)
			throw new Exception("cannot read image dimension");

		// compute the number of images

		whratio = 1.0 * fullImageDimension.getWidth() / fullImageDimension.getHeight();
		imagecount = (int) Math.ceil(whratio);

	}

	public void constructTiles() throws Exception {

		assert fullImageDimension != null;

		java.awt.Image loadImage = null;

		if (maxHeight != (int) fullImageDimension.getHeight()) {
			loadImage = Toolkit.getDefaultToolkit().createImage(imagePath.toURL())
					.getScaledInstance((int) (whratio * maxHeight), maxHeight, 0);
			// load image
			JLabel l = new JLabel(new ImageIcon(loadImage));
			MediaTracker mt = new MediaTracker(l);
			mt.waitForAll();
		} else {
			loadImage = ImageIO.read(imagePath);
		}

		for (int i = 0; i < imagecount; i++) {

			BufferedImage part = new BufferedImage(maxHeight, maxHeight, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = part.createGraphics();
			try {
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, maxHeight, maxHeight);
				g.drawImage(loadImage, 0, 0, maxHeight, maxHeight, maxHeight * i, 0, maxHeight * (i + 1), maxHeight,
						null);
			} finally {
				g.dispose();
			}

			ImageIO.write(part, "JPEG", constructImagePath(i, null));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.recognition.gui.books.tools.ITiledImage#
	 * getImageCount()
	 */
	@Override
	public int getImageCount() {
		return imagecount;
	}

	public File constructImagePath(int i, String suffix) {
		return new File(outputRecognitionProject, "" + i + (suffix == null ? "" : "_" + suffix) + ".jpg");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.recognition.gui.books.tools.ITiledImage#getImagePath
	 * (int)
	 */
	@Override
	public File getImagePath(int i) {
		return constructImagePath(i, null);
	}

	public static Dimension readImageSize(InputStream is) throws Exception {
		try (ImageInputStream in = ImageIO.createImageInputStream(is)) {
			final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
			if (readers.hasNext()) {
				ImageReader reader = readers.next();
				try {
					reader.setInput(in);
					int height = reader.getHeight(0);
					int width = reader.getWidth(0);
					return new Dimension(width, height);
				} finally {
					reader.dispose();
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.recognition.gui.books.tools.ITiledImage#
	 * subTileDimension(int)
	 */
	@Override
	public Rectangle2D.Double subTileDimension(int index) {
		return new Rectangle2D.Double(maxHeight * index, 0, maxHeight, maxHeight);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.recognition.gui.books.tools.ITiledImage#subTiles(
	 * java.awt.geom.Rectangle2D.Double)
	 */
	@Override
	public int[] subTiles(Rectangle2D.Double viewport) {
		if (viewport == null)
			return new int[0];

		if (viewport.getY() + viewport.getHeight() < 0)
			return new int[0];

		if (viewport.getY() > maxHeight)
			return new int[0];

		int start = (int) Math.floor(viewport.getX() / maxHeight);
		int end = (int) Math.ceil((viewport.getX() + viewport.getWidth()) / maxHeight);

		start = Math.max(start, 0);
		end = Math.min(end, imagecount);

		if (end - start < 0) {
			return new int[0];
		}

		int[] a = new int[end - start];
		for (int i = start; i < end; i++) {
			a[i - start] = i;
		}
		return a;
	}

}
