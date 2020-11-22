package org.barrelorgandiscovery.extensionsng.scanner.merge;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.bookimage.IFamilyImageSeeker;
import org.barrelorgandiscovery.extensionsng.scanner.OpenCVJavaConverter;
import org.barrelorgandiscovery.extensionsng.scanner.merge.tools.BooksImagesDisplacementsCalculation;
import org.barrelorgandiscovery.extensionsng.scanner.merge.tools.BooksImagesDisplacementsCalculation.DisplacementCalculationResult;
import org.barrelorgandiscovery.extensionsng.scanner.merge.tools.BooksImagesDisplacementsCalculation.KeyPointMatch;
import org.barrelorgandiscovery.math.MathVect;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class AutomaticDetectionComputing extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8439979578567718889L;

	private static Logger logger = Logger.getLogger(AutomaticDetectionComputing.class);

	private JScannerMergePanel mergePanel;

	public AutomaticDetectionComputing(JScannerMergePanel mergePanel) {
		assert mergePanel != null;
		this.mergePanel = mergePanel;
	}

	@Override

	public void actionPerformed(ActionEvent e) {
		try {

			int reductedFactor = 1;
			int current = mergePanel.currentResultImageSlider.getValue();
			int next = current + 1;
			if (next >= mergePanel.currentResultImageSlider.getMaximum()) {
				JMessageBox.showMessage(mergePanel, "cannot compute parmeters for the latest image");
				return;
			}
			IFamilyImageSeeker seekerImage = mergePanel.perfoScanFolder;

			ImageBookMergeModel model = mergePanel.model;

			MathVect origin = new MathVect(model.origin.x, model.origin.y);
			MathVect orientationAndWidthPoint = new MathVect(model.pointforAngleAndImageWidth.x,
					model.pointforAngleAndImageWidth.y);

			MathVect orientationFilter = orientationAndWidthPoint.moins(origin).moins();

			List<CentroidCluster<KeyPointMatch>> resultClusterDisplacement = AnalyzeDisplacementTools.computeDisplacement(seekerImage, current,
					next, reductedFactor, orientationFilter);

			if (resultClusterDisplacement.size() <= 0) {
				JMessageBox.showMessage(this, "cannot classify image");
				return;
			}

			resultClusterDisplacement.sort(new Comparator<CentroidCluster<KeyPointMatch>>() {
				public int compare(CentroidCluster<KeyPointMatch> a, CentroidCluster<KeyPointMatch> b) {
					return -Integer.compare(a.getPoints().size(), b.getPoints().size());
				}
			});

			displayResultOnConsole(resultClusterDisplacement);

			CentroidCluster<KeyPointMatch> centroidCluster = resultClusterDisplacement.get(0);
			DoublePoint center = (DoublePoint) centroidCluster.getCenter();

			MathVect displacement = new MathVect(center.getPoint()[0], center.getPoint()[1]);

			// mergePanel.movingResult.setImageToDisplay(displacementResult.displacementVectorsImage);

			mergePanel.overlappixelsspinner.setValue(displacement.norme() * reductedFactor);
			mergePanel.repaint();

		} catch (Exception ex) {
			logger.error("error in saving parameters :" + ex.getMessage(), ex);
			JMessageBox.showError(this, ex);
		}
	}

	

	private void displayResultOnConsole(List<CentroidCluster<KeyPointMatch>> resultClusterDisplacement) {
		for (CentroidCluster<KeyPointMatch> classified : resultClusterDisplacement) {
			System.out.println(
					"" + Arrays.asList(classified.getCenter().getPoint()[1]) + " " + classified.getPoints().size());
		}
	}

}
