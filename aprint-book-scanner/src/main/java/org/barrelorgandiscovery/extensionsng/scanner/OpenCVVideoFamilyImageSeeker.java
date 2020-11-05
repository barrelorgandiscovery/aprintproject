package org.barrelorgandiscovery.extensionsng.scanner;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;

import org.barrelorgandiscovery.tools.Disposable;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class OpenCVVideoFamilyImageSeeker implements IFamilyImageSeeker, Disposable {

	private File videoFile;
	private VideoCapture videoCapture;
	// CAP_PROP_POS_FRAMES -> pos frame

	private int frameVideoCount;

	private int resizeFactor;
	// take image every XX
	private int imageInterval;

	public OpenCVVideoFamilyImageSeeker(File videoFile, int resizeFactor, int imageinterval) throws Exception {
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

		// get the frame count
		frameVideoCount = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);

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
