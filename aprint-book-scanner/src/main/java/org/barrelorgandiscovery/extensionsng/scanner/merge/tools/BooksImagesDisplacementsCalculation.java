package org.barrelorgandiscovery.extensionsng.scanner.merge.tools;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.barrelorgandiscovery.math.MathVect;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.xfeatures2d.SURF;

public class BooksImagesDisplacementsCalculation {

	public static class DisplacementCalculationResult {
		public List<KeyPointMatch> movingKeyPointMatches;
		public List<KeyPointMatch> fixedKeyPointMatches;
		public BufferedImage displacementVectorsImage;
	}

	/**
	 * detect image displacement, and return informations
	 * 
	 * @param rm1
	 * @param rm2
	 * @param userCartonVector
	 * @return
	 */
	public static DisplacementCalculationResult detectKeyPoint(Mat rm1, Mat rm2, MathVect userCartonVector) {

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

		// create image

		BufferedImage displacementImage = new BufferedImage(rm1.width(), rm1.height(), BufferedImage.TYPE_INT_RGB);
		Graphics g = displacementImage.getGraphics();
		try {
			ArrayList<KeyPointMatch> result = new ArrayList<KeyPointMatch>();
			ArrayList<KeyPointMatch> fixedPoints = new ArrayList<KeyPointMatch>();
			
			
			for (DMatch d : listOfGoodMatches) {
				List<KeyPoint> listOfKeypointsObject = keypointsObject.toList();
				List<KeyPoint> listOfKeypointsScene = keypointsScene.toList();

				for (int i = 0; i < listOfGoodMatches.size(); i++) {
					// -- Get the keypoints from the good matches
					KeyPointMatch keyPointMatch = new KeyPointMatch();

					Point firstPoint = listOfKeypointsObject.get(listOfGoodMatches.get(i).queryIdx).pt;
					Point second = listOfKeypointsScene.get(listOfGoodMatches.get(i).trainIdx).pt;

					keyPointMatch.origin = new MathVect(firstPoint.x, firstPoint.y);
					keyPointMatch.displacement = new MathVect(second.x, second.y).moins(keyPointMatch.origin);

					double displacementVectorNorm = keyPointMatch.displacement.norme();
					double THRESHOLD_DISPLACEMENT = 15.0;
					Color c = Color.blue; // no displacement
					if (displacementVectorNorm > THRESHOLD_DISPLACEMENT) {
						c = Color.red;
						// filter non colinear ones

						if (userCartonVector != null) {
							double colinearity = keyPointMatch.displacement.angle(userCartonVector);
							double angleThreashold = 5.0 / 180.0 * Math.PI;
							if (Math.abs(colinearity) > angleThreashold) {
								// skip
								
								continue;
							}
						}
					}
					g.setColor(c);

					int x = (int) firstPoint.x;
					int y = (int) firstPoint.y;
					int x1 = (int) second.x;
					int y1 = (int) second.y;
					g.drawLine(x, y, x1, y1);

					if (displacementVectorNorm <= THRESHOLD_DISPLACEMENT) {
						// don't add fixed point into the result
						fixedPoints.add(keyPointMatch);
						continue;
					}

					result.add(keyPointMatch);
				}

			}
			DisplacementCalculationResult r = new DisplacementCalculationResult();
			r.movingKeyPointMatches = result;
			r.displacementVectorsImage = displacementImage;
			r.fixedKeyPointMatches = fixedPoints;

			return r;
		} finally {
			g.dispose();
		}

	}

	/**
	 * key point
	 * 
	 * @author pfreydiere
	 *
	 */
	public static class KeyPointMatch implements Clusterable {
		public MathVect origin;
		public MathVect displacement;

		@Override
		public double[] getPoint() {
			return new double[] { displacement.getX(), displacement.getY() };
		}
	}
}
