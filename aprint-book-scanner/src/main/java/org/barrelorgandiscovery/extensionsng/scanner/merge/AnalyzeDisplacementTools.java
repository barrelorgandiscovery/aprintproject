package org.barrelorgandiscovery.extensionsng.scanner.merge;

import java.awt.image.BufferedImage;
import java.util.List;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.bookimage.IFamilyImageSeeker;
import org.barrelorgandiscovery.extensionsng.scanner.OpenCVJavaConverter;
import org.barrelorgandiscovery.extensionsng.scanner.merge.tools.BooksImagesDisplacementsCalculation;
import org.barrelorgandiscovery.extensionsng.scanner.merge.tools.BooksImagesDisplacementsCalculation.DisplacementCalculationResult;
import org.barrelorgandiscovery.extensionsng.scanner.merge.tools.BooksImagesDisplacementsCalculation.KeyPointMatch;
import org.barrelorgandiscovery.math.MathVect;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

/**
 * static Libraries
 * @author pfreydiere
 *
 */
public class AnalyzeDisplacementTools {

	
	private static Logger logger = Logger.getLogger(AnalyzeDisplacementTools.class);
	
	/**
	 * compute displacement for 2 frames
	 * 
	 * @param seekerImage
	 * @param current
	 * @param next
	 * @param reductedFactor
	 * @param orientationFilter
	 * @return
	 * @throws Exception
	 */
	public static List<CentroidCluster<KeyPointMatch>> computeDisplacement(IFamilyImageSeeker seekerImage, int current,
			int next, int reductedFactor, MathVect orientationFilter) throws Exception {

		logger.debug("loading image " + current);

		BufferedImage i1 = seekerImage.loadImage(current);
		logger.debug("loading image " + next);

		BufferedImage i2 = seekerImage.loadImage(next);

		Mat m1 = new Mat();
		OpenCVJavaConverter.toOpenCV(i1, m1);
		
		
		Mat rm1 = new Mat();
		Imgproc.resize(m1, rm1, new Size(m1.width() / reductedFactor, m1.height() / reductedFactor));
		
		Mat m2 = new Mat();
		OpenCVJavaConverter.toOpenCV(i2, m2);
		Mat rm2 = new Mat();
		Imgproc.resize(m2, rm2, new Size(m2.width() / reductedFactor, m2.height() / reductedFactor));

		logger.debug("launch key point detection");

		DisplacementCalculationResult displacementResult = BooksImagesDisplacementsCalculation.detectKeyPoint(rm1, rm2,
				orientationFilter);

		List<KeyPointMatch> goodMatches = displacementResult.movingKeyPointMatches;
		if (goodMatches.size() <= 4) {
			throw new Exception("cannot find moving points");
		}

		// knn the displacement vector, based on distance
		KMeansPlusPlusClusterer<KeyPointMatch> kpm = new KMeansPlusPlusClusterer<KeyPointMatch>(4, 1000,
				new DistanceMeasure() {
					@Override
					public double compute(double[] a, double[] b) throws DimensionMismatchException {
						return Math.abs(Math.sqrt(a[1] * a[1] + a[0] * a[0]) - Math.sqrt(b[1] * b[1] + b[0] * b[0]));

						// return Math.abs(Math.atan2(a[1], a[0]) - Math.atan2(b[1], b[0]));
					}
				});
		
		List<CentroidCluster<KeyPointMatch>> resultClusterDisplacement = kpm.cluster(goodMatches);

		return resultClusterDisplacement;

	}
}
