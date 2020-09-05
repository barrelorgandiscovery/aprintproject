package org.barrelorgandiscovery.recognition.gui.books.steps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.CreationTool;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.UndoStack;
import org.barrelorgandiscovery.gui.aedit.snapping.HolesSnappingEnvironnement;
import org.barrelorgandiscovery.gui.aprintng.APrintNGGeneralServices;
import org.barrelorgandiscovery.gui.aprintng.IAPrintWait;
import org.barrelorgandiscovery.gui.aprintng.WrappingLayout;
import org.barrelorgandiscovery.gui.wizard.BasePanelStep;
import org.barrelorgandiscovery.gui.wizard.Step;
import org.barrelorgandiscovery.gui.wizard.StepStatusChangedListener;
import org.barrelorgandiscovery.gui.wizard.WizardStates;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.recognition.ImageAndHolesVisualizationLayer;
import org.barrelorgandiscovery.recognition.gui.books.BackgroundTileImageProcessingThread;
import org.barrelorgandiscovery.recognition.gui.books.BookReadProcessor;
import org.barrelorgandiscovery.recognition.gui.books.BookReadProcessor.ReadResultBag;
import org.barrelorgandiscovery.recognition.gui.books.RecognitionTiledImage;
import org.barrelorgandiscovery.recognition.gui.books.states.EdgesStates;
import org.barrelorgandiscovery.recognition.gui.booktoolbar.JVBToolingToolbar;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.Book;
import org.barrelorgandiscovery.recognition.gui.disks.steps.states.ImageFileAndInstrument;
import org.barrelorgandiscovery.recognition.gui.tools.ScaleHolesTool;
import org.barrelorgandiscovery.recognition.messages.Messages;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO;

/**
 * this step view the corrected book, and edit the book
 * 
 * @author use
 * 
 */
public class StepViewAndEditBook extends BasePanelStep implements Disposable {

	private static Logger logger = Logger.getLogger(StepViewAndEditBook.class);

	private Repository2 repository;

	// visual component
	private JEditableVirtualBookComponent editableVirtualbookComponent;

	private ImageAndHolesVisualizationLayer imageBackgroundShowing;

	private IAPrintWait waitFrame;

	private APrintNGGeneralServices services;

	private BackgroundTileImageProcessingThread<Void> backProcessing;
	private BackgroundTileImageProcessingThread<Void> backProcessingView;

	private Book currentState = null;

	private JVBToolingToolbar toolbar;
	private JToolBar recognitionToolbar;

	private int meanBookWidthInImage;

	private Model model;

	private EdgesStates edgesState;

	private RecognitionTiledImage tiRenormedModel;

	private JSlider decisionThreshold;

	private boolean invertedReference;

	public StepViewAndEditBook(String id, Step parent, Repository2 repository, IAPrintWait wizardFrame,
			APrintNGGeneralServices services) throws Exception {
		super(id, parent);
		this.waitFrame = wizardFrame;
		this.repository = repository;
		this.services = services;
		initComponents();

	}

	private void initComponents() throws Exception {

		editableVirtualbookComponent = new JEditableVirtualBookComponent();

		imageBackgroundShowing = new ImageAndHolesVisualizationLayer();

		editableVirtualbookComponent.addLayer(imageBackgroundShowing);

		decisionThreshold = new JSlider(10, 245);
		decisionThreshold.setValue(150); // default

		UndoStack us = new UndoStack();

		// tool for editing on the book
		HolesSnappingEnvironnement snappingEnvironment = new HolesSnappingEnvironnement(editableVirtualbookComponent);
		editableVirtualbookComponent
				.setCurrentTool(new CreationTool(editableVirtualbookComponent, new UndoStack(), snappingEnvironment));

		setLayout(new BorderLayout());
		add(editableVirtualbookComponent, BorderLayout.CENTER);

		JPanel toolbarPanel = new JPanel();
		toolbarPanel.setLayout(new WrappingLayout());

		recognitionToolbar = new JToolBar();

		toolbar = new JVBToolingToolbar(editableVirtualbookComponent, us, snappingEnvironment);

		toolbarPanel.add(toolbar);
		toolbarPanel.add(recognitionToolbar);

		add(toolbarPanel, BorderLayout.NORTH);

		// add save image informations button

		JButton autoRecognition = new JButton(Messages.getString("StepViewAndEditDisk.1")); //$NON-NLS-1$
		autoRecognition.setIcon(ImageTools.loadIcon(StepViewAndEditBook.class, "auto.png")); //$NON-NLS-1$
		autoRecognition.setToolTipText(Messages.getString("StepViewAndEditDisk.3")); //$NON-NLS-1$

		autoRecognition.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				try {

					if (editableVirtualbookComponent.getVirtualBook().getOrderedHolesCopy().size() > 0) {
						if (JOptionPane.showConfirmDialog((Component) StepViewAndEditBook.this,
								Messages.getString("StepViewAndEditDisk.4")) != JOptionPane.YES_OPTION) { // $NON-NLS-1$
							return;
						}

					}

					assert tiRenormedModel != null;
					RecognitionTiledImage rec = tiRenormedModel; // (RecognitionTiledImage)
																	// iv.getTiledBackgroundimage();

					RecognitionTiledImage recognized = new RecognitionTiledImage(rec);
					recognized.setCurrentImageFamilyDisplay(model.getName());

					VirtualBook virtualBook = editableVirtualbookComponent.getVirtualBook();

					virtualBook.clear();

					long[] state = new long[virtualBook.getScale().getTrackNb()];

					final int d = decisionThreshold.getValue();

					assert d >= 0 && d <= 255;

					// read all then tiles images
					for (int i = 0; i < recognized.getImageCount(); i++) {

						try {
							File f = rec.getImagePath(i);
							if (!f.exists())
								continue;

							BufferedImage bi = ImageTools.loadImage(f.toURL());

							Scale scale = editableVirtualbookComponent.getVirtualBook().getScale();

							double pixelSize = 1.0 * scale.getWidth() / meanBookWidthInImage; // mm
																								// per
																								// pixel

							double pixeltime = scale.mmToTime(1000000) / 1000000.0; // mm
																					// by
																					// micros
							// double factor = 1 -
							// 1/BookReadProcessor.ratioPtsPerMm() ;

//							double factor = 0.6d / 1.00427d / ((1803 + 3.4) / 1803); // magix
//																						// number
//																						// ???

							double factor = 1.0;

							ReadResultBag result = BookReadProcessor.readResult2(bi, rec.getHeight() * i,
									pixeltime * pixelSize * factor, scale, state, true, d);

							VirtualBook v = result.virtualbook;
							state = result.state;

							editableVirtualbookComponent.getVirtualBook().addHole(v.getHolesCopy());

						} catch (Exception ex) {
							logger.error("error in holes recognition :" + ex.getMessage(), ex);
						}
					}

					editableVirtualbookComponent.repaint();

				} catch (Exception ex) {
					logger.error("error in recognition : " + ex.getMessage(), ex);
				}
			}

		});

		// for debug purpose
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
					ImageIO.write(imageBackgroundShowing.getBackgroundimage(), "PNG", //$NON-NLS-1$
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

		recognitionToolbar.add(new JLabel("Transparency :"));

		JSlider sl = new JSlider(JSlider.HORIZONTAL, 10, 90, 50);
		sl.setToolTipText("select transparency");
		sl.setMaximumSize(new Dimension(100, 30));
		sl.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {

				float floatValue = 1.0f * sl.getValue() / 100;

				editableVirtualbookComponent.setHoleTransparency(floatValue);
				editableVirtualbookComponent.repaint();
			}
		});
		recognitionToolbar.add(sl);

		recognitionToolbar.addSeparator(new Dimension(50, 10));
		recognitionToolbar.add(autoRecognition);
		recognitionToolbar.add(decisionThreshold);

		toolbar.addSeparator();
		JToggleButton b = toolbar.addTool(new ScaleHolesTool(editableVirtualbookComponent));
		b.setText("Adjust Holes Scale");

	}

	public String getLabel() {
		return "Recognize the holes, and edit the book";
	}

	public void activate(Serializable state, WizardStates allStepsStates, StepStatusChangedListener stepListener)
			throws Exception {

		if (state != null) {
			currentState = (Book) state;
		}

		if (currentState == null) {
			currentState = new Book();
		}

		// 1 - Get the instrument and scale
		ImageFileAndInstrument imageAndInstrument = allStepsStates.getPreviousStateImplementing(this,
				ImageFileAndInstrument.class);

		assert imageAndInstrument != null;

		model = allStepsStates.getPreviousStateImplementing(this, Model.class);

		File imageDiskFile = imageAndInstrument.diskFile;

		final RecognitionTiledImage origin = new RecognitionTiledImage(imageDiskFile);
		// origin.setCurrentImageFamilyDisplay(model.getName());

		RecognitionTiledImage originModel = new RecognitionTiledImage(imageDiskFile);
		originModel.setCurrentImageFamilyDisplay(model.getName());

		// renormalized
		tiRenormedModel = new RecognitionTiledImage(imageDiskFile);
		tiRenormedModel.setCurrentImageFamilyDisplay("renormed_model");

		// view the renormalized model
		final RecognitionTiledImage tiView = new RecognitionTiledImage(imageDiskFile);
		tiView.setCurrentImageFamilyDisplay("renormed");

		edgesState = allStepsStates.getPreviousStateImplementing(this, EdgesStates.class);
		meanBookWidthInImage = BookReadProcessor.computeMeanWidth(edgesState.top, edgesState.bottom);

		String currentInstrumentName = imageAndInstrument.instrumentName;
		Instrument instrument = repository.getInstrument(currentInstrumentName);

		imageBackgroundShowing.setFlipHorizontallyTheImage(instrument.getScale().isPreferredViewedInversed());

		///////////////////////////////////////////////////////////////////////
		// model processing
		{
			BackgroundTileImageProcessingThread<Void> t = new BackgroundTileImageProcessingThread<>(originModel,
					new BackgroundTileImageProcessingThread.TiledProcessedListener() {
						@Override
						public <T> void tileProcessed(int index, T result) {

							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									editableVirtualbookComponent.repaint();
								}
							});

						}

						@Override
						public void errorInProcessingTile(String errormsg) {
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									JOptionPane.showMessageDialog(null, errormsg);
								}

							});
						}

					}, 1);

			if (backProcessing != null) {
				backProcessing.cancel();
			}

			backProcessing = t;

			t.start(new BackgroundTileImageProcessingThread.TileProcessing<Void>() {
				@Override
				public Void process(int index, BufferedImage tile) throws Exception {

					File output = tiRenormedModel.constructImagePath(index,
							tiRenormedModel.getCurrentImageFamilyDisplay());

					BufferedImage result = BookReadProcessor.correctImage(tile, index * originModel.getHeight(),
							originModel.getHeight(), edgesState.top, edgesState.bottom, originModel.getHeight(),
							edgesState.viewInverted != instrument.getScale().isPreferredViewedInversed());

					ImageIO.write(result, "JPEG", output);

					return null;
				}
			});
		}

		////////////////////////////////////////////////////////////////////////////
		// viewing processing
		{

			BackgroundTileImageProcessingThread<Void> t2 = new BackgroundTileImageProcessingThread<>(origin,
					new BackgroundTileImageProcessingThread.TiledProcessedListener() {
						@Override
						public <T> void tileProcessed(int index, T result) {

							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									editableVirtualbookComponent.repaint();
								}
							});

						}

						@Override
						public void errorInProcessingTile(String errormsg) {
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									JOptionPane.showMessageDialog(null, errormsg);
								}

							});
						}
					}, 1);

			if (backProcessingView != null) {
				backProcessingView.cancel();
			}

			backProcessingView = t2;

			t2.start(new BackgroundTileImageProcessingThread.TileProcessing<Void>() {
				@Override
				public Void process(int index, BufferedImage tile) throws Exception {

					File output = tiView.constructImagePath(index, tiView.getCurrentImageFamilyDisplay());

					BufferedImage result = BookReadProcessor.correctImage(tile, index * origin.getHeight(),
							origin.getHeight(), edgesState.top, edgesState.bottom, origin.getHeight(),
							edgesState.viewInverted ^ instrument.getScale().isPreferredViewedInversed());

					ImageIO.write(result, "JPEG", output);

					return null;
				}
			});

		}

		currentState.instrumentName = currentInstrumentName;

		// get the perimeter parameters

		if (currentState.virtualbook == null) {
			currentState.virtualbook = new VirtualBook(instrument.getScale());
		}

		editableVirtualbookComponent.setVirtualBook(currentState.virtualbook);
		imageBackgroundShowing.setTiledBackgroundimage(tiView);
		editableVirtualbookComponent.touchBook();
		editableVirtualbookComponent.repaint();

	}

	public Serializable unActivateAndGetSavedState() throws Exception {

		currentState.virtualbook = editableVirtualbookComponent.getVirtualBook();

		if (backProcessing != null) {
			backProcessing.cancel();
			backProcessing = null;
		}

		return currentState;
	}

	public boolean isStepCompleted() {
		return true;
	}

	@Override
	public void dispose() {
		if (backProcessing != null) {
			backProcessing.dispose();
			backProcessing = null;
		}

		if (backProcessingView != null) {
			backProcessingView.dispose();
			backProcessingView = null;
		}
	}

}
