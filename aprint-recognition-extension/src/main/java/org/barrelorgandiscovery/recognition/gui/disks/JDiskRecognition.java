package org.barrelorgandiscovery.recognition.gui.disks;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprintng.APrintNGGeneralServices;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookInternalFrame;
import org.barrelorgandiscovery.gui.aprintng.IAPrintWait;
import org.barrelorgandiscovery.gui.wizard.FinishedListener;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepBeforeChanged;
import org.barrelorgandiscovery.gui.wizard.StepChanged;
import org.barrelorgandiscovery.gui.wizard.Wizard;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.images.books.tools.StandaloneTiledImage;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.prefs.DummyPrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.recognition.gui.bookext.IRecognitionToolWindowCommands;
import org.barrelorgandiscovery.recognition.gui.disks.steps.JDiskTracksCorrectedLayer;
import org.barrelorgandiscovery.recognition.gui.disks.steps.StepChooseFilesAndInstrument;
import org.barrelorgandiscovery.recognition.gui.disks.steps.StepChooseOrientationAndBeginning;
import org.barrelorgandiscovery.recognition.gui.disks.steps.StepChooseOrientationAndBeginning.AngleAndOrientation;
import org.barrelorgandiscovery.recognition.gui.disks.steps.StepMatchCenter;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.INumericImage;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.ImageFileAndInstrument;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.PointsAndEllipseParameters;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JEllipticLayer;
import org.barrelorgandiscovery.recognition.math.EllipseParameters;
import org.barrelorgandiscovery.recognition.math.MathVect;
import org.barrelorgandiscovery.recognition.messages.Messages;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Factory;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.SerializeTools;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.VirtualBookMetadata;

/**
 * Component for disk recognition
 * 
 * @author pfreydiere
 * 
 */
public class JDiskRecognition extends JPanel implements Disposable {

// 	private static final String STEP_VIEW_AND_EDIT = "view_and_edit"; //$NON-NLS-1$

	public static final String STEP_SELECT_DISK_CONTOUR = "selectDiskContour"; //$NON-NLS-1$

	public static final String STEP_SELECT_DISK_CENTER = "selectDiskCenter"; //$NON-NLS-1$

	public static final String STEP_INSTRUMENTS_AND_IMAGE = "instrumentsAndImage"; //$NON-NLS-1$

	public static final String STEP_CHOOSE_BEGINNING = "chooseBeginningAndOrientation"; //$NON-NLS-1$

	private static Logger logger = Logger.getLogger(JDiskRecognition.class);

	/**
	 * instrument repository
	 */
	private Repository2 repository;

	/**
	 * Wizard component
	 */
	private Wizard wizard;

	private File serializeFile;

	private StepChooseFilesAndInstrument stepChooseFilesAndInstrument;

	private IPrefsStorage prefsStorage;

	private APrintNGGeneralServices services;

// 	private StepViewAndEditDisk viewAndEditStep;

	private IAPrintWait waitFrame;

	private StepChooseOrientationAndBeginning chooseOrientationStep;

	private StepMatchCenter chooseContourStep;

	private StepMatchCenter chooseCenterStep;

	public JDiskRecognition(Repository2 repository, IPrefsStorage prefsStorage, APrintNGGeneralServices services,
			IAPrintWait waitFrame) throws Exception {

		assert repository != null;
		this.repository = repository;
		this.prefsStorage = prefsStorage;
		this.services = services;
		this.waitFrame = waitFrame;

		initComponent();

		assert wizard != null;
		wizard.setStepChanged(new StepChanged() {

			public void currentStepChanged(int stepNo, Serializable state) {
				try {
					logger.debug("saving the state ..."); //$NON-NLS-1$
					if (serializeFile != null) {
						SerializeTools.save(wizard.getPagesStates(), JDiskRecognition.this.serializeFile);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		wizard.setStepBeforeChanged(new StepBeforeChanged() {

			@Override
			public boolean beforeStepChanged(Step oldStep, Step newStep, Wizard wizard) throws Exception {
				if (oldStep == stepChooseFilesAndInstrument) {
					INumericImage numericImage = (INumericImage) wizard.getCurrentWizardState();
					if (numericImage != null) {

						File imageFile = numericImage.getImageFile();
						if (imageFile != null && imageFile.exists()) {
							serializeFile = new File(imageFile.getParentFile(), imageFile.getName() + ".ser");
						}

						if (serializeFile.exists()) {
							try {
								Serializable s = (Serializable) SerializeTools.load(serializeFile);
								// loaded

								wizard.reloadStatesIfPossible(s,
										new Step[] { chooseCenterStep, chooseContourStep, chooseOrientationStep });

							} catch (Exception ex) {
								logger.error("error reading the saved state :" + ex.getMessage(), ex); //$NON-NLS-1$
							}
						}

					}
				}
				return true;
			}
		});

		// wizard.reinit(s);

		wizard.setFinishedListener(new FinishedListener() {

			@Override
			public void finished(WizardStates ser) {
				try {

					ImageFileAndInstrument imageAndInstrument = ser.getInPreviousStates(chooseOrientationStep,
							ImageFileAndInstrument.class);

					BufferedImage source = ImageTools.loadImage(imageAndInstrument.diskFile);

					BufferedImage croppedImage = ImageTools.crop(500, 500, source);

					String currentInstrumentName = imageAndInstrument.instrumentName;
					Instrument instrument = repository.getInstrument(currentInstrumentName);

					// get the perimeter parameters

					PointsAndEllipseParameters perimeterParameters = ser.getInPreviousStates(chooseOrientationStep,
							PointsAndEllipseParameters.class);

					PointsAndEllipseParameters centerParameters = ser.getInPreviousStates(chooseOrientationStep,
							PointsAndEllipseParameters.class);

					EllipseParameters ep = perimeterParameters.ellipseParameters;

					double mean_radius = (ep.a + ep.b) / 2;
					Scale scale = instrument.getScale();

					// compute angle for start

					AngleAndOrientation angleAndOrientation = ser.getInPreviousStates(chooseOrientationStep,
							AngleAndOrientation.class);

					// vector centre -> point
					MathVect v = new MathVect(
							new Point2D.Double(centerParameters.ellipseParameters.centre.x,
									centerParameters.ellipseParameters.centre.y),
							new Point2D.Double(angleAndOrientation.pointForAngle.getCenterX(),
									angleAndOrientation.pointForAngle.getCenterY()));

					double angleOrigine = v.angleOrigine();

					double resolution_factor = 0.6; // ???? @@@

					logger.debug("origin angle :" + angleOrigine); //$NON-NLS-1$
					BufferedImage correctedImage = DiskImageTools.createCorrectedImage(source,
							centerParameters.ellipseParameters.centre, ep, angleOrigine,
							(int) (2 * Math.PI * mean_radius * resolution_factor),
							(int) (mean_radius * resolution_factor));

					VirtualBook virtualBook = new VirtualBook(instrument.getScale());
					// adjust virtualbook
					VirtualBookMetadata metadata = virtualBook.getMetadata();
					if (metadata == null)
						metadata = new VirtualBookMetadata();

					metadata.setCover(croppedImage);
					virtualBook.setMetadata(metadata);

					APrintNGVirtualBookFrame newframe = services.newVirtualBook(virtualBook, instrument);

					APrintNGVirtualBookInternalFrame i = (APrintNGVirtualBookInternalFrame) newframe;
					
					IRecognitionToolWindowCommands[] extensionPointRecognition = i.getExtensionPoints(IRecognitionToolWindowCommands.class);
					if (extensionPointRecognition == null || extensionPointRecognition.length != 1) {
						throw new Exception("no toolwindoww found");
					}
						
					i.toggleDirty();

					extensionPointRecognition[0].setTiledImage(new StandaloneTiledImage(correctedImage));

				} catch (Exception ex) {
					logger.error("error in finishing the wizard :" + ex.getMessage(), //$NON-NLS-1$
							ex);
					BugReporter.sendBugReport();
					JMessageBox.showError(waitFrame, ex);
					
				}

			}
		});

	}

	/**
	 * internal method for creating the components
	 * 
	 * @throws Exception
	 */
	private void initComponent() throws Exception {

		ArrayList<Step> steps = new ArrayList<Step>();

		stepChooseFilesAndInstrument = new StepChooseFilesAndInstrument(STEP_INSTRUMENTS_AND_IMAGE, null, repository,
				prefsStorage);
		steps.add(stepChooseFilesAndInstrument);

		stepChooseFilesAndInstrument.setDetails(Messages.getString("JDiskRecognition.0")); //$NON-NLS-1$

		// step 2, match the center of disk
		JEllipticLayer centerEllipticLayer = new JEllipticLayer();
		centerEllipticLayer.setEllipseDrawingColor(Color.red);
		chooseCenterStep = new StepMatchCenter(STEP_SELECT_DISK_CENTER, stepChooseFilesAndInstrument,
				Messages.getString("JDiskRecognition.7"), centerEllipticLayer, //$NON-NLS-1$
				repository);
		chooseCenterStep.setDetails(Messages.getString("JDiskRecognition.1") //$NON-NLS-1$
				+ Messages.getString("JDiskRecognition.2")); //$NON-NLS-1$
		steps.add(chooseCenterStep);

		// step 2, match the perimeter of the disk
		JDiskTracksCorrectedLayer ellipseLayerWithTracks = new JDiskTracksCorrectedLayer();
		ellipseLayerWithTracks.setEllipseDrawingColor(Color.red);
		chooseContourStep = new StepMatchCenter(STEP_SELECT_DISK_CONTOUR, chooseCenterStep,
				Messages.getString("JDiskRecognition.8"), //$NON-NLS-1$
				ellipseLayerWithTracks, repository);
		chooseContourStep.setDetails(Messages.getString("JDiskRecognition.3")); //$NON-NLS-1$
		steps.add(chooseContourStep);

		chooseOrientationStep = new StepChooseOrientationAndBeginning(STEP_CHOOSE_BEGINNING, chooseContourStep,
				Messages.getString("JDiskRecognition.9"));
		chooseOrientationStep.setDetails(Messages.getString("JDiskRecognition.4")); //$NON-NLS-1$
		steps.add(chooseOrientationStep);

		wizard = new Wizard(steps, null);
		setLayout(new BorderLayout());
		add(wizard, BorderLayout.CENTER);

	}

	public void setImageFile(File imageFile) throws Exception {
		stepChooseFilesAndInstrument.setImageFile(imageFile);
		stepChooseFilesAndInstrument.fitImage();
	}

	/**
	 * return the wizard
	 * 
	 * @return
	 */
	public Wizard getWizard() {
		return wizard;
	}

	/**
	 * test routine
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		SwingUtilities.invokeAndWait(new Runnable() {

			public void run() {

				try {
					File testFile = new File("c:\\temp\\test6.ser"); //$NON-NLS-1$

					BasicConfigurator.configure(new ConsoleAppender(new PatternLayout()));

					APrintProperties aPrintProperties = new APrintProperties(false);
					Repository2 rep = Repository2Factory.create(new Properties(), aPrintProperties);

					JFrame f = new JFrame();
					f.setSize(800, 500);
					f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

					JDiskRecognition dr = new JDiskRecognition(rep, new DummyPrefsStorage(), null, null);

					f.getContentPane().add(dr);

					f.setVisible(true);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

	}

	@Override
	public void dispose() {
		if (wizard != null) {
			wizard.dispose();
			wizard = null;
		}
	}

}
