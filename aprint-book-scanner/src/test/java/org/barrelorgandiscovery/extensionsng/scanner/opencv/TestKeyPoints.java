package org.barrelorgandiscovery.extensionsng.scanner.opencv;

import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.barrelorgandiscovery.extensionsng.scanner.merge.tools.BooksImagesDisplacementsCalculation;
import org.barrelorgandiscovery.extensionsng.scanner.merge.tools.BooksImagesDisplacementsCalculation.DisplacementCalculationResult;
import org.barrelorgandiscovery.extensionsng.scanner.merge.tools.BooksImagesDisplacementsCalculation.KeyPointMatch;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class TestKeyPoints {

//	public static void main(String[] args) throws Exception {
//
//		JFrame f = new JFrame();
//
//		Loader.load(opencv_java.class);
//
//		VideoCapture v = new VideoCapture(
//				"C:\\projets\\APrint\\contributions\\plf\\2020-10_Essais saisie video\\carton ancien avec fond blanc.mp4");
//
//		int frameVideoCount = (int) v.get(Videoio.CAP_PROP_FRAME_COUNT);
//
//		Mat m1 = new Mat();
//		v.set(Videoio.CAP_PROP_POS_FRAMES, 1000);
//		v.read(m1);
//
//		Mat rm1 = new Mat();
//		Imgproc.resize(m1, rm1, new Size(m1.width() / 2, m1.height() / 2));
//
//		v.set(Videoio.CAP_PROP_POS_FRAMES, 1005);
//		Mat m2 = new Mat();
//		v.read(m2);
//		Mat rm2 = new Mat();
//		Imgproc.resize(m2, rm2, new Size(m2.width() / 2, m2.height() / 2));
//		DisplacementCalculationResult detectKeyPoint = BooksImagesDisplacementsCalculation.detectKeyPoint(rm1, rm2,
//				null);
//		List<KeyPointMatch> goodMatches = detectKeyPoint.movingKeyPointMatches;
//		
//		// knn the displacement vector
//		KMeansPlusPlusClusterer<KeyPointMatch> kpm = new KMeansPlusPlusClusterer<KeyPointMatch>(10, -1,
//				new DistanceMeasure() {
//
//					@Override
//					public double compute(double[] a, double[] b) throws DimensionMismatchException {
//						return Math.abs(Math.atan2(a[1], a[0]) - Math.atan2(b[1], b[0]));
//
//					}
//				});
//		List<CentroidCluster<KeyPointMatch>> resultClusterDisplacement = kpm.cluster(goodMatches);
//
//		System.out.println(resultClusterDisplacement);
//
//		Mat dst = new Mat();
//
//		Mat imgMatches = new Mat();
////		Features2d.drawMatches(rm1, keypointsObject, rm2, keypointsScene, goodMatches, imgMatches, Scalar.all(-1),
////				Scalar.all(-1), new MatOfByte(), Features2d.DrawMatchesFlags_NOT_DRAW_SINGLE_POINTS);
//
////		HighGui.imshow("Feature Detection", dst2);
////		HighGui.imshow("img1", rm1);
//		HighGui.imshow("img2", imgMatches);
//
//		HighGui.waitKey();
//
//		// fdetector.write("test.keypoints");
//
//	}

}
