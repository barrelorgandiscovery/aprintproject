package org.barrelorgandiscovery.extensionsng.scanner.merge.tools;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.barrelorgandiscovery.extensionsng.scanner.opencv.TestKeyPoints;
import org.barrelorgandiscovery.math.MathVect;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.xfeatures2d.SURF;

public class BooksImagesCalculation {

	public static List<KeyPointMatch> detectKeyPoint(Mat rm1, Mat rm2) {
		
		long start = System.currentTimeMillis();
		double hessianThreshold = 400;
		
		int nOctaves = 4, nOctaveLayers = 3;
		boolean extended = false, upright = false;
		SURF detector = SURF.create(hessianThreshold, nOctaves, nOctaveLayers, extended, upright);
		MatOfKeyPoint keypointsObject = new MatOfKeyPoint(), keypointsScene = new MatOfKeyPoint();
		Mat descriptors1 = new Mat(), descriptors2 = new Mat();
	
		detector.detectAndCompute(rm1, new Mat(), keypointsObject, descriptors1);
		detector.detectAndCompute(rm2, new Mat(), keypointsScene, descriptors2);
		
		// -- Step 2: Matching descriptor vectors with a FLANN based matcher
		// Since SURF is a floating-point descriptor NORM_L2 is used
		DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
		List<MatOfDMatch> knnMatches = new ArrayList<>();
		matcher.knnMatch(descriptors1, descriptors2, knnMatches, 2);
		
		// -- Filter matches using the Lowe's ratio test
		
		float ratioThresh = 0.75f;
		List<DMatch> listOfGoodMatches = new ArrayList<>();
		for (int i = 0; i < knnMatches.size(); i++) {
			if (knnMatches.get(i).rows() > 1) {
				DMatch[] matches = knnMatches.get(i).toArray();
				if (matches[0].distance < ratioThresh * matches[1].distance) {
					listOfGoodMatches.add(matches[0]);
				}
			}
		}
	
		System.out.println("detect cost :" + (System.currentTimeMillis() - start));
		// Drawing the detected key points
		
		ArrayList<KeyPointMatch> result = new ArrayList<KeyPointMatch>();
		for (DMatch d: listOfGoodMatches ) {
			  List<KeyPoint> listOfKeypointsObject = keypointsObject.toList();
	         List<KeyPoint> listOfKeypointsScene = keypointsScene.toList();
	        
	        for (int i = 0; i < listOfGoodMatches.size(); i++) {
	            //-- Get the keypoints from the good matches
	        	KeyPointMatch keyPointMatch = new KeyPointMatch();
	            
	        	Point firstPoint = listOfKeypointsObject.get(listOfGoodMatches.get(i).queryIdx).pt;
	        	Point second = listOfKeypointsScene.get(listOfGoodMatches.get(i).trainIdx).pt;
				
	        	keyPointMatch.origin = new MathVect(firstPoint.x, firstPoint.y);
	        	keyPointMatch.displacement = new MathVect(second.x, second.y).moins(keyPointMatch.origin);
	        	result.add(keyPointMatch);
	        }
			
		}
		
		
		return result;
	}
	
	public static class KeyPointMatch implements Clusterable{
		public MathVect origin;
		public MathVect displacement;
		
		@Override
		public double[] getPoint() {
			return new double[] { displacement.getX(), displacement.getY()};
		}
	}
}


