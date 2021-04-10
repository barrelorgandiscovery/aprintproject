package org.barrelorgandiscovery.extensionsng.scanner.opencv;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.bytedeco.opencv.global.opencv_video;
import org.bytedeco.opencv.global.opencv_videoio;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class TestOpenCVReadFullVideo {
//
//	// result : read in 36130 ms
// //video size: 229 Mb
//	
//	public static void main(String[] args) throws Exception {
//		
//		Loader.load(opencv_java.class);
//		Loader.load(opencv_video.class);
//		Loader.load(opencv_videoio.class);
//	
//		
//
//		VideoCapture vcapture = new VideoCapture();
//
//		vcapture.open(
//				"/home/use/tmp/Cartonneufavecfondnoir.mp4");
//
//		boolean opened = vcapture.isOpened();
//		System.out.println("opened " + opened);
//		if (!opened) {
//			return;
//		}
//		
//		Mat m = new Mat();
//		
//		long start = System.currentTimeMillis();
//		while(vcapture.read(m)) {
//			
//		}
//		
//		System.out.println("read in " + (System.currentTimeMillis()- start) + " ms");
//	}
//	
}
