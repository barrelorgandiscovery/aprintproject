package org.barrelorgandiscovery.recognition.gui.disks.steps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.JobEvent;
import org.barrelorgandiscovery.gui.aedit.CreationTool;
import org.barrelorgandiscovery.gui.aedit.ImageAndHolesVisualizationLayer;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.UndoStack;
import org.barrelorgandiscovery.gui.aedit.snapping.HolesSnappingEnvironnement;
import org.barrelorgandiscovery.gui.aprintng.APrintNGGeneralServices;
import org.barrelorgandiscovery.gui.aprintng.IAPrintWait;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.recognition.ImageHoleReader;
import org.barrelorgandiscovery.recognition.gui.booktoolbar.JVBToolingToolbar;
import org.barrelorgandiscovery.recognition.gui.disks.DiskImageTools;
import org.barrelorgandiscovery.recognition.gui.disks.steps.StepChooseOrientationAndBeginning.AngleAndOrientation;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.Book;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.ImageFileAndInstrument;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.PointsAndEllipsisParameters;
import org.barrelorgandiscovery.recognition.math.EllipseParameters;
import org.barrelorgandiscovery.recognition.math.MathVect;
import org.barrelorgandiscovery.recognition.messages.Messages;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import trainableSegmentation.WekaSegmentation;

/**
 * this step view the corrected book, and edit the book
 * 
 * @author pfreydiere
 * 
 */
public class StepViewAndEditDisk extends BasePanelStep {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4566233634571293392L;

	private static Logger logger = Logger.getLogger(StepViewAndEditDisk.class);

	private Repository2 repository;

	private JEditableVirtualBookComponent editableVirtualbookComponent;

	private ImageAndHolesVisualizationLayer iv;

	private IAPrintWait waitFrame;

	private APrintNGGeneralServices services;

	public StepViewAndEditDisk(String id, Step parent, Repository2 repository, IAPrintWait wizardFrame,
			APrintNGGeneralServices services) throws Exception {
		super(id, parent);
		this.waitFrame = wizardFrame;
		this.repository = repository;
		this.services = services;
		initComponents();
	}

	private void initComponents() throws Exception {

		editableVirtualbookComponent = new JEditableVirtualBookComponent();

		iv = new ImageAndHolesVisualizationLayer();

		editableVirtualbookComponent.addLayer(iv);

		UndoStack us = new UndoStack();

		// tool for editing on the book
		HolesSnappingEnvironnement snappingEnvironment = new HolesSnappingEnvironnement(editableVirtualbookComponent);
		editableVirtualbookComponent
				.setCurrentTool(new CreationTool(editableVirtualbookComponent, new UndoStack(), snappingEnvironment));

		setLayout(new BorderLayout());
		add(editableVirtualbookComponent, BorderLayout.CENTER);

		toolbar = new JVBToolingToolbar(editableVirtualbookComponent, us, snappingEnvironment);
		add(toolbar, BorderLayout.NORTH);

		// add save image informations button

		toolbar.addSeparator();

		JButton autoRecognition = new JButton(Messages.getString("StepViewAndEditDisk.1")); //$NON-NLS-1$
		autoRecognition.setIcon(ImageTools.loadIcon(StepViewAndEditDisk.class, "auto.png")); //$NON-NLS-1$
		autoRecognition.setToolTipText(Messages.getString("StepViewAndEditDisk.3")); //$NON-NLS-1$

		autoRecognition.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				if (editableVirtualbookComponent.getVirtualBook().getOrderedHolesCopy().size() > 0) {

					if (JOptionPane.showConfirmDialog((Component) waitFrame,
							Messages.getString("StepViewAndEditDisk.4")) != JOptionPane.YES_OPTION) { //$NON-NLS-1$
						return;
					}

				}

				//
				// JMessageBox
				// .showMessage(waitFrame,
				// "Recognition will be launched, it might take a minute, please wait ...");

				Callable<List<Hole>> f = new Callable<List<Hole>>() {
					@Override
					public List<Hole> call() throws Exception {
						try {
							ImagePlus input = new ImagePlus();
							try {

								input.setImage(iv.getBackgroundimage());

								WekaSegmentation ws = new WekaSegmentation(input);
								if (!ws.loadClassifier(
										StepViewAndEditDisk.class.getResourceAsStream("classifier.model"))) { //$NON-NLS-1$
									throw new Exception(Messages.getString("StepViewAndEditDisk.6")); //$NON-NLS-1$
								}

								if (waitFrame != null)
									waitFrame.infiniteChangeText(Messages.getString("StepViewAndEditDisk.7")); //$NON-NLS-1$

								ImagePlus r = ws.applyClassifier(input);

								if (waitFrame != null)
									waitFrame.infiniteChangeText(Messages.getString("StepViewAndEditDisk.8")); //$NON-NLS-1$

								ImageProcessor processor = r.getProcessor();
								processor.setAutoThreshold(ij.process.ImageProcessor.RED_LUT, 0);

								ByteProcessor bp = processor.convertToByteProcessor();

								bp.erode();
								bp.erode();

								bp.dilate();
								bp.dilate();

								ImagePlus opened = new ImagePlus("", bp); //$NON-NLS-1$

								if (waitFrame != null)
									waitFrame.infiniteChangeText(Messages.getString("StepViewAndEditDisk.10")); //$NON-NLS-1$

								List<Hole> holes = ImageHoleReader.readHoles(opened.getBufferedImage(),
										currentState.virtualbook.getScale());

								// read holes
								return holes;

							} finally {
								input.close();
							}

						} catch (Exception ex) {
							logger.error("error in recognition " + ex.getMessage(), //$NON-NLS-1$
									ex);

							throw ex;

						}

					}
				};

				AsyncJobsManager ajm = services.getAsyncJobs();

				if (waitFrame != null)
					waitFrame.infiniteStartWait(Messages.getString("StepViewAndEditDisk.12")); //$NON-NLS-1$

				ajm.submitAndExecuteJob(f, new JobEvent() {

					@Override
					public void jobFinished(Object result) {

						if (waitFrame != null)
							waitFrame.infiniteEndWait();

						List<Hole> holes = (List<Hole>) result;

						if (holes == null)
							return;

						// clear all holes
						editableVirtualbookComponent.getVirtualBook().clear();

						// add the recogined holes
						editableVirtualbookComponent.getVirtualBook().addHole(holes);

						// repaint the output
						editableVirtualbookComponent.repaint();

						JMessageBox.showMessage(waitFrame, Messages.getString("StepViewAndEditDisk.13")); //$NON-NLS-1$

					}

					@Override
					public void jobError(Throwable ex) {

						BugReporter.sendBugReport();

						// repaint the output
						editableVirtualbookComponent.repaint();

						if (waitFrame != null)
							waitFrame.infiniteEndWait();

						JMessageBox.showError(waitFrame, ex);

					}

					@Override
					public void jobAborted() {

						// repaint the output
						editableVirtualbookComponent.repaint();

						if (waitFrame != null)
							waitFrame.infiniteEndWait();

					}
				});

			}

		});

		toolbar.add(autoRecognition);

		toolbar.addSeparator();

		JButton saveImageInformationAndVirtualBook = new JButton(Messages.getString("StepViewAndEditDisk.14")); //$NON-NLS-1$

		// toolbar.add(saveImageInformationAndVirtualBook);

		saveImageInformationAndVirtualBook.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {

					assert currentState != null;
					assert currentState.instrumentName != null;

					File c = new File("c:\\temp\\recognition"); //$NON-NLS-1$
					c.mkdirs();

					logger.debug("saving image"); //$NON-NLS-1$
					ImageIO.write(iv.getBackgroundimage(), "PNG", //$NON-NLS-1$
							new File(c, "modifiedImage.png")); //$NON-NLS-1$

					VirtualBook vb = editableVirtualbookComponent.getVirtualBook();
					FileOutputStream fos = new FileOutputStream(new File(c, "virtualBook.book")); //$NON-NLS-1$
					try {
						VirtualBookXmlIO.write(fos, vb, currentState.instrumentName);
					} finally {
						fos.close();
					}
				} catch (Exception ex) {
					logger.error("Error in saving informations :" //$NON-NLS-1$
							+ ex.getMessage(), ex);
					JMessageBox.showError(this, ex);
				}

			}

		});

	}

	public String getLabel() {
		return Messages.getString("StepViewAndEditDisk.0"); //$NON-NLS-1$
	}

	private Book currentState = null;

	private JVBToolingToolbar toolbar;

	public void activate(Serializable state, WizardStates allStepsStates, StepStatusChangedListener stepListener)
			throws Exception {

		if (state != null) {
			currentState = (Book) state;
		}

		if (currentState == null) {
			currentState = new Book();
		}

		ImageFileAndInstrument imageAndInstrument = allStepsStates.getPreviousStateImplementing(this,
				ImageFileAndInstrument.class);

		BufferedImage source = ImageTools.loadImage(imageAndInstrument.diskFile);

		String currentInstrumentName = imageAndInstrument.instrumentName;
		Instrument instrument = repository.getInstrument(currentInstrumentName);

		currentState.instrumentName = currentInstrumentName;

		// get the perimeter parameters

		PointsAndEllipsisParameters perimeterParameters = allStepsStates.getPreviousStateImplementing(getParentStep(),
				PointsAndEllipsisParameters.class);
		logger.debug("parent step :" + getParentStep().getId()); //$NON-NLS-1$

		PointsAndEllipsisParameters centerParameters = allStepsStates
				.getPreviousStateImplementing(getParentStep().getParentStep(), PointsAndEllipsisParameters.class);
		logger.debug("parent, parent step :" + getParentStep().getParentStep()); //$NON-NLS-1$

		EllipseParameters ep = perimeterParameters.outerEllipseParameters;

		double mean_radius = (ep.a + ep.b) / 2;
		Scale scale = instrument.getScale();

		// compute angle for start

		AngleAndOrientation angleAndOrientation = allStepsStates.getPreviousStateImplementing(this,
				AngleAndOrientation.class);

		// vector centre -> point
		MathVect v = new MathVect(
				new Point2D.Double(centerParameters.outerEllipseParameters.centre.x,
						centerParameters.outerEllipseParameters.centre.y),
				new Point2D.Double(angleAndOrientation.pointForAngle.getCenterX(),
						angleAndOrientation.pointForAngle.getCenterY()));

		double angleOrigine = v.angleOrigine();

		double resolution_factor = 0.6; // ???? @@@

		logger.debug("origin angle :" + angleOrigine); //$NON-NLS-1$
		BufferedImage correctedImage = DiskImageTools.createCorrectedImage(source,
				centerParameters.outerEllipseParameters.centre, ep, angleOrigine,
				(int) (2 * Math.PI * mean_radius * resolution_factor), (int) (mean_radius * resolution_factor));

		if (currentState.virtualbook == null) {
			currentState.virtualbook = new VirtualBook(scale);
		}

		editableVirtualbookComponent.setVirtualBook(currentState.virtualbook);
		iv.setBackgroundimage(correctedImage);
		editableVirtualbookComponent.touchBook();
		editableVirtualbookComponent.repaint();

	}

	public Serializable unActivateAndGetSavedState() throws Exception {

		currentState.virtualbook = editableVirtualbookComponent.getVirtualBook();

		return currentState;
	}

	public boolean isStepCompleted() {
		return true;
	}

}
