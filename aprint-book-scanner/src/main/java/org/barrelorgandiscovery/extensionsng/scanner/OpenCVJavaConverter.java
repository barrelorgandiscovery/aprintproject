package org.barrelorgandiscovery.extensionsng.scanner;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.barrelorgandiscovery.tools.ImageTools;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.bytedeco.opencv.global.opencv_core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class OpenCVJavaConverter {

	public static BufferedImage convertOpenCVToJava(Mat m, int resizeFactor) {

		Mat d = new Mat();

		Imgproc.cvtColor(m, d, Imgproc.COLOR_RGB2RGBA, 0);

		// Imgproc.resize(m, d, new Size(m.width() / resizeFactor, m.height() /
		// resizeFactor));

		BufferedImage gray = new BufferedImage(d.width(), d.height(), BufferedImage.TYPE_INT_RGB);
		byte[] rgbcv = new byte[4 * d.height() * d.width()];

		// Get the BufferedImage's backing array and copy the pixels directly into it

		DataBufferInt bdata = (DataBufferInt) gray.getRaster().getDataBuffer();
		int[] data = bdata.getData();

		d.get(0, 0, rgbcv);

		// change image alignment
		for (int i = 0; i < data.length; i++) {
			int r = rgbcv[i * 4] & 0xFF;
			int g = rgbcv[i * 4 + 1] & 0xFF;
			int b = rgbcv[i * 4 + 2] & 0xFF;

			int value = r | g << 8 | b << 16;
			data[i] = value;
		}
		final BufferedImage b = gray;

		return b;
	}

	public static void toOpenCV(BufferedImage bi, Mat dest) {
		// Get the BufferedImage's backing array and copy the pixels directly into it

		if (bi.getType() == BufferedImage.TYPE_INT_RGB || bi.getType() == BufferedImage.TYPE_INT_ARGB) {
			// assert bi.getType() == BufferedImage.TYPE_INT_RGB;
			int[] data = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();

			byte[] buffer = new byte[3];
			dest.create(bi.getHeight(), bi.getWidth(), opencv_core.CV_8UC3);
			for (int i = 0; i < data.length; i++) {
				final int r = i / bi.getWidth();
				final int c = i % bi.getWidth();

				int v = data[i];
				buffer[0] = (byte) (v & 0xFF);
				buffer[1] = (byte) ((v >> 8) & 0xFF);
				buffer[2] = (byte) ((v >> 16) & 0xFF);

				dest.put(r, c, buffer);

			}
		} else if (bi.getType() == BufferedImage.TYPE_3BYTE_BGR) {
			byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
			dest.create(bi.getHeight(), bi.getWidth(), opencv_core.CV_8UC3);
			dest.put(0, 0, data);
			
		} else {
			throw new RuntimeException("unsupported image type :" + bi.getType());
		}

	}

	public static void main(String[] args) throws Exception {

		Loader.load(opencv_java.class);
		BufferedImage bi = ImageTools.loadImageAndCrop(new File(
				"C:\\projets\\APrint\\contributions\\plf\\2019-07-19_scan_video_popcorn_52\\exportedfile-00001.png"),
				200, 200);
		Mat dest = new Mat();
		toOpenCV(bi, dest);

		// HighGui.imshow("img2", dest);
		// HighGui.waitKey();

		BufferedImage bit = convertOpenCVToJava(dest, 1);

		displayImage(bit);

	}

	public static void displayImage(Image bit) {
		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(new JLabel(new ImageIcon(bit)));
		f.setSize(200, 400);
		f.setVisible(true);
	}

}
