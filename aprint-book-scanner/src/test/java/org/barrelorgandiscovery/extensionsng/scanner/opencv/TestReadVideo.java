package org.barrelorgandiscovery.extensionsng.scanner.opencv;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;

public class TestReadVideo {
	
	public static void main(String[] args) throws Exception {
		Loader.load(opencv_java.class);

		VideoCapture vcapture = new VideoCapture();

		vcapture.open(
				"C:\\projets\\APrint\\contributions\\plf\\2020-10_Essais saisie video\\videos 45 limonaire\\Baisers gloutons.mp4");

		boolean opened = vcapture.isOpened();
		System.out.println("opened " + opened);
		if (!opened) {
			return;
		}
		
		Mat m = new Mat();
		
		long start = System.currentTimeMillis();
		boolean mvideo = vcapture.read(m);
			
		HighGui.imshow("img2", m);

		HighGui.waitKey();
		
		
		System.out.println("read in " + (System.currentTimeMillis()- start) + " ms");
	}
}
