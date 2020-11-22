package org.barrelorgandiscovery.extensionsng.scanner;

import java.awt.image.BufferedImage;
import java.io.File;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.bookimage.IFamilyImageSeeker;
import org.barrelorgandiscovery.tools.Disposable;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class OpenCVVideoFamilyImageSeeker implements IFamilyImageSeeker, Disposable {

	private static Logger logger = Logger.getLogger(OpenCVVideoFamilyImageSeeker.class);
	
	private File videoFile;
	
	private VideoCapture videoCapture;
	// CAP_PROP_POS_FRAMES -> pos frame

	private int frameVideoCount;

	private int resizeFactor;
	// take image every XX
	private int imageInterval;

	public OpenCVVideoFamilyImageSeeker(File videoFile, int resizeFactor, int imageinterval) throws Exception {
		Loader.load(opencv_java.class);
		
		assert videoFile != null;
		assert videoFile.exists();
		assert videoFile.isFile();
		assert resizeFactor >= 1;

		this.resizeFactor = resizeFactor;

		assert imageinterval >= 1;
		this.imageInterval = imageinterval;

		videoCapture = new VideoCapture(videoFile.getAbsolutePath());
		if (!videoCapture.isOpened()) {
			throw new Exception("cannot open video");
		}
		// remember the video file, to be able to reopen it
		this.videoFile = videoFile;
		
		logger.info("video information HEIGHT : " +  videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT));
		logger.info("video information WIDTH : " +  videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH));
		

		// get the frame count
		frameVideoCount = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);
		
		videoCapture.set(48, 0);
		videoCapture.set(49, 1); // Videoio.CAP_PROP_ORIENTATION_AUTO 

	}

	@Override
	public int getImageCount() {
		return frameVideoCount / imageInterval;
	}

	@Override
	public BufferedImage loadImage(int imageNumber) throws Exception {
		synchronized (this) {
			videoCapture.set(Videoio.CAP_PROP_POS_FRAMES, imageNumber * imageInterval);
			Mat m = new Mat();

			if (!videoCapture.read(m)) {
				throw new Exception("cannot read image " + imageNumber);
			}

			final BufferedImage b = OpenCVJavaConverter.convertOpenCVToJava(m, resizeFactor);

			return b;
		}
	}


	@Override
	public void dispose() {
		videoCapture.release();
	}

}
