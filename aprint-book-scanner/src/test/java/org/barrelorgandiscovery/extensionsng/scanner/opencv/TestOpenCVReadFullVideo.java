package org.barrelorgandiscovery.extensionsng.scanner.opencv;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class TestOpenCVReadFullVideo {

	// result : read in 36130 ms
 //video size: 229 Mb
	
	public static void main(String[] args) throws Exception {
		Loader.load(opencv_java.class);

		VideoCapture vcapture = new VideoCapture();

		vcapture.open(
				"C:\\projets\\APrint\\contributions\\plf\\2020-10_Essais saisie video\\carton ancien avec fond blanc.mp4");

		boolean opened = vcapture.isOpened();
		System.out.println("opened " + opened);
		if (!opened) {
			return;
		}
		
		Mat m = new Mat();
		
		long start = System.currentTimeMillis();
		while(vcapture.read(m)) {
			
		}
		
		System.out.println("read in " + (System.currentTimeMillis()- start) + " ms");
	}
	
}
