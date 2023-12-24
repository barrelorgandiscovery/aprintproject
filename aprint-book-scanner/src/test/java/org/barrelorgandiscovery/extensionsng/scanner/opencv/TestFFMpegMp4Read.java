package org.barrelorgandiscovery.extensionsng.scanner.opencv;

import java.awt.image.BufferedImage;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

public class TestFFMpegMp4Read {

	// @Test
	public void test() throws Exception {

		FrameGrabber grabber = new FFmpegFrameGrabber("/home/use/tmp/Cartonneufavecfondnoir.mp4"); // for the first
																									// camera
		// grabber.setFormat("video4linux2");
		// grabber.setFormat("mp4");
		grabber.start();
		System.out.println(grabber.getImageWidth());
		System.out.println(grabber.getImageHeight());

		int frame = grabber.getFrameNumber();
		System.out.println(frame);

		Java2DFrameConverter converter = new Java2DFrameConverter();

		int cpt = 0;
		Frame f = null;
		do {
			f = grabber.grab();

			// System.out.println(f.image);
			if (f != null) {
				BufferedImage bi = converter.convert(f);
				cpt++;
			/*	if (bi != null) {
					JFrame jf = new JFrame();
					jf.getContentPane().setLayout(new BorderLayout());
					JLabel label = new JLabel();
					label.setIcon(new ImageIcon(bi));
					jf.getContentPane().add(label);
					jf.setVisible(true);
					while (true) {
						Thread.sleep(1000);
					}
				} */
				
			}

			System.out.println(cpt);
		} while (f != null);

	}
}
