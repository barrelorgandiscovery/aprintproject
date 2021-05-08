package org.barrelorgandiscovery.extensionsng.scanner;

import java.awt.image.BufferedImage;
import java.io.File;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.bookimage.IFamilyImageSeeker;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.ImageTools;
import org.bytedeco.ffmpeg.ffmpeg;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

public class FFMpegVideoFamilyImageSeeker implements IFamilyImageSeeker, Disposable {

	private static Logger logger = Logger.getLogger(FFMpegVideoFamilyImageSeeker.class);

	int resizeFactor;
	int imageInterval;

	File videoFile;

	FFmpegFrameGrabber grabber;

	int frameVideoCount;

	Java2DFrameConverter converter = new Java2DFrameConverter();

	int cpt = 0;

	public FFMpegVideoFamilyImageSeeker(File videoFile, int resizeFactor, int imageinterval) throws Exception {
		Loader.load(ffmpeg.class);

		assert videoFile != null;
		assert videoFile.exists();
		assert videoFile.isFile();
		assert resizeFactor >= 1;

		this.resizeFactor = resizeFactor;

		assert imageinterval >= 1;
		this.imageInterval = imageinterval;

		// remember the video file, to be able to reopen it
		this.videoFile = videoFile;

		restart();

		logger.info("video information HEIGHT : " + grabber.getImageHeight());
		logger.info("video information WIDTH : " + grabber.getImageWidth());

		// get the frame count

		Frame f;
		do {
			f = grabber.grab();
			if (f != null && f.image != null) {
				cpt++;
			}

		} while (f != null);

		frameVideoCount = cpt;
		
	}

	private void restart() throws org.bytedeco.javacv.FrameGrabber.Exception {
		dispose();
		grabber = new FFmpegFrameGrabber(videoFile.getAbsolutePath()); // for the first camera
		// grabber.setFormat("video4linux2");
		grabber.start();
	}

	@Override
	public void dispose() {
		if (grabber != null) {
			try {
				grabber.close();
				grabber.release();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
		grabber = null;
	}

	@Override
	public int getImageCount() {
		return frameVideoCount / imageInterval;
	}

	@Override
	public synchronized BufferedImage  loadImage(int imageNumber) throws Exception {

		grabber.setVideoFrameNumber(imageNumber * imageInterval);

		Frame f;
		f = grabber.grab();

		BufferedImage bi = converter.convert(f);
		// rescale
		
		
		return ImageTools.crop(bi.getWidth()/ this.resizeFactor, bi.getHeight()/ this.resizeFactor, bi);

	}

}
