package org.barrelorgandiscovery.recognition;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.StringTokenizer;

/**
 * PPM Image Reader,
 * 
 * @author pfreydiere
 * 
 */
public class PPMImageReader {

	private LineNumberReader reader;
	private String type;
	private int width;
	private int height;
	private int maxColor;

	public PPMImageReader(File file) throws Exception {
		reader = new LineNumberReader(new InputStreamReader(
				new FileInputStream(file)));
		type = readNext();
		if (type == null)
			throw new Exception("no magic number in the header");

		if (!"P3".equals(type))
			throw new Exception("unsupported image " + type);

		width = Integer.parseInt(readNext());
		height = Integer.parseInt(readNext());
		maxColor = Integer.parseInt(readNext());

		assert maxColor <= 255;

	}

	public String getType() {
		return this.type;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private String readNext() throws Exception {
		String line = null;
		do {
			line = reader.readLine();
			if (line != null) {
				int indexOfCommentChar = line.indexOf('#');
				if (indexOfCommentChar != -1)
					line = line.substring(0, indexOfCommentChar);
				line = line.trim();
			}
		} while (line != null && "".equals(line));

		return line;
	}

	public Color readPixelColor() throws Exception {

		String colorLine = readNext();
		if (colorLine == null)
			return null;

		colorLine = colorLine.trim();
		StringTokenizer st = new StringTokenizer(colorLine, " ");
		int r = Integer.parseInt(st.nextToken());
		int g = Integer.parseInt(st.nextToken());
		int b = Integer.parseInt(st.nextToken());

		return new Color(r, g, b);
	}

	public void close() throws Exception {
		reader.close();
	}

	public BufferedImage readFullImage() throws Exception {
		BufferedImage bi = new BufferedImage(getWidth(), getHeight(),
				BufferedImage.TYPE_INT_RGB);
		for (int j = 0; j < getHeight(); j++) {
			for (int i = 0; i < getWidth(); i++) {

				Color c = readPixelColor();
				bi.setRGB(i, j,
						c.getRed() << 16 | c.getGreen() << 8 | c.getBlue());
			}
		}
		return bi;
	}

}
