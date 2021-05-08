package org.barrelorgandiscovery.recognition.gui.books;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.bookimage.BookImage;
import org.barrelorgandiscovery.bookimage.ZipBookImage;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
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
import org.barrelorgandiscovery.images.books.tools.BookImageRecognitionTiledImage;
import org.barrelorgandiscovery.images.books.tools.IFileFamilyTiledImage;
import org.barrelorgandiscovery.images.books.tools.RecognitionTiledImage;
import org.barrelorgandiscovery.images.books.tools.StandaloneTiledImage;
import org.barrelorgandiscovery.images.books.tools.TiledImage;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.recognition.gui.bookext.IRecognitionToolWindowCommands;
import org.barrelorgandiscovery.recognition.gui.books.states.EdgesStates;
import org.barrelorgandiscovery.recognition.gui.books.steps.ProcessEdges;
import org.barrelorgandiscovery.recognition.gui.books.steps.StepChooseEdges;
import org.barrelorgandiscovery.recognition.gui.books.steps.StepModelChooseChoice;
import org.barrelorgandiscovery.recognition.gui.books.steps.StepViewAndEditBook;
import org.barrelorgandiscovery.recognition.gui.disks.steps.StepChooseFilesAndInstrument;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.Book;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.INumericImage;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.ImageFileAndInstrument;
import org.barrelorgandiscovery.recognition.messages.Messages;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Factory;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.SerializeTools;
import org.barrelorgandiscovery.tools.URLTools;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * book recognition panel
 * 
 * @author pfreydiere
 * 
 */
public class JBookRecognition extends JPanel {

	private static final int PREFERRED_COMPUTED_HEIGHT = 800;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1203072642221142351L;

	private static Logger logger = Logger.getLogger(JBookRecognition.class);

	public static final String STEP_INSTRUMENTS_AND_IMAGE = "instrumentsAndImage"; //$NON-NLS-1$
	// public static final String STEP_VIEW_AND_EDIT = "view_and_edit";
	// //$NON-NLS-1$
	public static final String STEP_EDGES_RECOGNITION = "edges_recognition"; //$NON-NLS-1$
	// public static final String STEP_CHOOSE_MODEL = "choose_model"; //$NON-NLS-1$

	// public static final int MAX_HEIGHT_WIDTH = 800;

	/**
	 * Wizard component
	 */
	private Wizard wizard;

	private StepChooseFilesAndInstrument stepChooseFilesAndInstrument;

	private Repository2 repository;
	private File serializeFile;

	private IPrefsStorage prefsStorage;
	private APrintNGGeneralServices services;
	private IAPrintWait waitFrame;

	private StepViewAndEditBook stepEdit;

	private StepChooseEdges stepEdges;

	// private StepModelChooseChoice chooseModelStep;

	public JBookRecognition(Repository2 repository, IPrefsStorage prefsStorage, APrintNGGeneralServices services,
			IAPrintWait waitFrame) throws Exception {

		assert repository != null;
		this.repository = repository;
		this.serializeFile = null;
		this.prefsStorage = prefsStorage;
		this.services = services;
		this.waitFrame = waitFrame;

		// initialize components
		initComponent();

		wizard.setStepChanged(new StepChanged() {

			public void currentStepChanged(int stepNo, Serializable state) {
				try {
					logger.debug("saving the state ..."); //$NON-NLS-1$

					OutputStream outStream = new FileOutputStream(JBookRecognition.this.serializeFile);
					try {
						SerializeTools.save(wizard.getPagesStates(), outStream);
					} finally {
						outStream.close();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		wizard.setStepBeforeChanged(new StepBeforeChanged() {

			@Override
			public boolean beforeStepChanged(Step oldStep, Step newStep, Wizard wizard) throws Exception {

				logger.debug("before step changed"); //$NON-NLS-1$
				if (oldStep == stepChooseFilesAndInstrument) {
					INumericImage numericImage = (INumericImage) wizard.getCurrentWizardState();
					if (numericImage != null) {
						File imageFile = numericImage.getImageFile();
						if (imageFile != null) {
							serializeFile = new File(imageFile.getAbsolutePath() + ".ser"); //$NON-NLS-1$
						}

						try {
							InputStream is = new FileInputStream(serializeFile);
							try {
								Serializable s = (Serializable) SerializeTools.load(is);
								// loaded
								wizard.reloadStatesIfPossible(s, new Step[] { stepEdges });
							} finally {
								is.close();
							}
						} catch (Exception ex) {
							logger.debug(ex.getMessage(), ex);
						}

						if (!imageFile.getName().endsWith(BookImage.BOOKIMAGE_EXTENSION)) {

							// this is an image file

							try (InputStream fis = new FileInputStream(imageFile)) {
								Dimension d = TiledImage.readImageSize(fis);
								if (d.getHeight() > 800) {
									int result = JOptionPane.showConfirmDialog(wizard,
											"Height dimension is larger than 800 pixels \n "
													+ "do you want to reduce the size to be more efficient ?",
											"Reduce image size", JOptionPane.YES_NO_OPTION);

									if (result == JOptionPane.YES_OPTION) {

										int newwidth = d.width * 800 / d.height;

										// move to jpeg ...
										String rootName = imageFile.getName();

										int extensionpoint = rootName.lastIndexOf('.');
										if (extensionpoint != -1) {
											rootName = rootName.substring(0, extensionpoint);
											rootName += ".jpg"; //$NON-NLS-1$
										}

										File newFileName = new File(imageFile.getParentFile(), "rescaled_" + rootName);//$NON-NLS-1$

										rescaleImageFile(imageFile, newwidth, newFileName);
										stepChooseFilesAndInstrument.setImageFile(newFileName);
										logger.debug("image resized"); //$NON-NLS-1$
									}

								}
							}
						} else {
							assert imageFile.getName().endsWith(BookImage.BOOKIMAGE_EXTENSION);

						}
					}
				}
				return true;
			}

			private void rescaleImageFile(File imageFile, int newwidth, File newFileName)
					throws Exception {
				Image loadImage = Toolkit.getDefaultToolkit().createImage(imageFile.toURL()).getScaledInstance(newwidth,
						PREFERRED_COMPUTED_HEIGHT, 0);
				// load image
				JLabel l = new JLabel(new ImageIcon(loadImage));
				MediaTracker mt = new MediaTracker(l);
				mt.waitForAll();

				BufferedImage newImage = new BufferedImage(loadImage.getWidth(null), loadImage.getHeight(null),
						BufferedImage.TYPE_INT_RGB);
				Graphics2D g = newImage.createGraphics();
				try {
					g.drawImage(loadImage, 0, 0, newwidth, PREFERRED_COMPUTED_HEIGHT, null);
				} finally {
					g.dispose();
				}

				OutputStream outStream = new FileOutputStream(newFileName);
				try {
					ImageTools.saveJpeg(newImage, outStream); //$NON-NLS-1$
				} finally {
					outStream.close();
				}
			}
		});

		// wizard.reinit(s);
	}

	private void initComponent() throws Exception {
		ArrayList<Step> steps = new ArrayList<Step>();

		// 1 - Choose instrument and Image
		stepChooseFilesAndInstrument = new StepChooseFilesAndInstrument(STEP_INSTRUMENTS_AND_IMAGE, null, repository,
				prefsStorage);
		steps.add(stepChooseFilesAndInstrument);
		stepChooseFilesAndInstrument.setDetails(Messages.getString("JDiskRecognition.0")); //$NON-NLS-1$

//		chooseModelStep = new StepModelChooseChoice(STEP_CHOOSE_MODEL, stepChooseFilesAndInstrument);
//		steps.add(chooseModelStep);

		stepEdges = new StepChooseEdges(STEP_EDGES_RECOGNITION, stepChooseFilesAndInstrument, repository);
		steps.add(stepEdges);

//		stepEdit = new StepViewAndEditBook(STEP_VIEW_AND_EDIT, stepEdges, repository, waitFrame, services);
//		steps.add(stepEdit);

		// create wizard
		wizard = new Wizard(steps, null);
		setLayout(new BorderLayout());
		add(wizard, BorderLayout.CENTER);

		// finished conditions, and last steps
		wizard.setFinishedListener(new FinishedListener() {
			public void finished(WizardStates ser) {
				try {

					//
					ImageFileAndInstrument dins = (ImageFileAndInstrument) ser.getState(STEP_INSTRUMENTS_AND_IMAGE);
					Instrument ins = repository.getInstrument(dins.getInstrumentName());

					File imageFile = dins.diskFile;
					assert imageFile != null;

					IFileFamilyTiledImage origin = null;
					IFileFamilyTiledImage tiView = null;

					if (imageFile.getName().endsWith(BookImage.BOOKIMAGE_EXTENSION)) {
						origin = new BookImageRecognitionTiledImage(new ZipBookImage(imageFile));
						tiView = new BookImageRecognitionTiledImage(new ZipBookImage(imageFile));
					} else {
						origin = new RecognitionTiledImage(imageFile);
						tiView = new RecognitionTiledImage(imageFile);
					}

					// view the renormalized model

					tiView.setCurrentImageFamilyDisplay("renormed");

					// adjust virtualbook
					VirtualBook vb = new VirtualBook(ins.getScale());

					APrintNGVirtualBookFrame newframe = services.newVirtualBook(vb, ins);

					APrintNGVirtualBookInternalFrame i = (APrintNGVirtualBookInternalFrame) newframe;
					i.toggleDirty();

					EdgesStates edgesState = (EdgesStates) ser.getState(stepEdges);

					ProcessEdges processEdges = new ProcessEdges();
					JVirtualBookScrollableComponent pianoRoll = i.getPianoRoll();

					processEdges.processImageEdges(origin, edgesState, vb.getScale(), tiView);
					
					IRecognitionToolWindowCommands[] extensionPointRecognition = i.getExtensionPoints(IRecognitionToolWindowCommands.class);
					if (extensionPointRecognition == null || extensionPointRecognition.length != 1) {
						throw new Exception("no toolwindoww found");
					}
					
					extensionPointRecognition[0].setTiledImage(tiView);

					// JMessageBox.showMessage(waitFrame, "the book is ready to play and edit");
					// //$NON-NLS-1$

				} catch (Exception ex) {
					logger.error("error in finishing the wizard :" + ex.getMessage(), //$NON-NLS-1$
							ex);
					BugReporter.sendBugReport();
					JMessageBox.showError(waitFrame, ex);
				}

			}
		});
	}

	public static void main(String[] args) throws Exception {

		SwingUtilities.invokeAndWait(new Runnable() {

			public void run() {

				try {

					BasicConfigurator.configure(new LF5Appender());

					APrintProperties aPrintProperties = new APrintProperties("aprintstudio", false);
					System.out.println(aPrintProperties.getAprintFolder());
					Repository2 rep = Repository2Factory.create(new Properties(), aPrintProperties);

					JFrame f = new JFrame();
					f.setSize(800, 500);
					f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

					// for testing it is useful
					FilePrefsStorage prefs = new FilePrefsStorage(new File("c:\\temp\\prefsTest.properties"));
					prefs.load();

					JBookRecognition dr = new JBookRecognition(rep, prefs, null, null);

					f.getContentPane().add(dr);

					f.setVisible(true);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

	}

}
