package org.barrelorgandiscovery.recognition.gui.bookext;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.bookimage.BookImage;
import org.barrelorgandiscovery.bookimage.ZipBookImage;
import org.barrelorgandiscovery.gui.aedit.CreationTool;
import org.barrelorgandiscovery.gui.aedit.CurrentToolChanged;
import org.barrelorgandiscovery.gui.aedit.GlobalVirtualBookUndoOperation;
import org.barrelorgandiscovery.gui.aedit.HolesDisplayOverlayLayer;
import org.barrelorgandiscovery.gui.aedit.ImageAndHolesVisualizationLayer;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.aedit.RectSelectTool;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.gui.aedit.UndoStack;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
import org.barrelorgandiscovery.images.books.tools.BookImageRecognitionTiledImage;
import org.barrelorgandiscovery.images.books.tools.IFamilyImageSeekerTiledImage;
import org.barrelorgandiscovery.images.books.tools.IFileFamilyTiledImage;
import org.barrelorgandiscovery.images.books.tools.ITiledImage;
import org.barrelorgandiscovery.images.books.tools.RecognitionTiledImage;
import org.barrelorgandiscovery.images.books.tools.StandaloneRecognitionTiledImage;
import org.barrelorgandiscovery.images.books.tools.StandaloneTiledImage;
import org.barrelorgandiscovery.recognition.gui.books.BackgroundTileImageProcessingThread;
import org.barrelorgandiscovery.recognition.gui.books.BackgroundTileImageProcessingThread.TileProcessing;
import org.barrelorgandiscovery.recognition.gui.books.BackgroundTileImageProcessingThread.TiledProcessedListener;
import org.barrelorgandiscovery.recognition.gui.books.BookReadProcessor;
import org.barrelorgandiscovery.recognition.gui.books.BookReadProcessor.ReadResultBag;
import org.barrelorgandiscovery.recognition.messages.Messages;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.SwingUtils;
import org.barrelorgandiscovery.ui.tools.VFSTools;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;

public class JRecognitionVirtualBookPanel extends JPanel implements Disposable, IRecognitionToolWindowCommands {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1619815934643072673L;

	private static Logger logger = Logger.getLogger(JRecognitionVirtualBookPanel.class);

	final String REC_INLINE_FAMILY = "recognized_inline"; //$NON-NLS-1$

	public JRecognitionVirtualBookPanel() throws Exception {
		initComponents();
	}

	JToggleButton holeCreationToolbtn;
	JToggleButton bookCreationToolbtn;

	// create button on rect shapes
	JToggleButton rectHoleCreationToolbtn;
	JToggleButton rectbookCreationToolbtn;

	ImageAndHolesVisualizationLayer backgroundBook = new ImageAndHolesVisualizationLayer();

	/**
	 * display the result of the recognitiion
	 */
	ImageAndHolesVisualizationLayer recognitionDisplay = new ImageAndHolesVisualizationLayer();

	/**
	 * display the user input for the model
	 */
	ImageAndHolesVisualizationLayer bookRegionDisplay = new ImageAndHolesVisualizationLayer();
	ImageAndHolesVisualizationLayer holeRegionDisplay = new ImageAndHolesVisualizationLayer();

	/**
	 * Book notes overlay (in yellow to have a better view of the holes (on top of
	 * other layers)
	 */
	HolesDisplayOverlayLayer bookNotesOverlay = new HolesDisplayOverlayLayer();

	WekaRecognitionStrategy wekaRecognitionStrategy;
	IRecognitionStrategy thresholdStrategy = new IRecognitionStrategy() {
		@Override
		public BufferedImage apply(BufferedImage image) {
			try {
				ColorProcessor colorProcessor = new ColorProcessor(image);
				ByteProcessor convertToByteProcessor = colorProcessor.convertToByteProcessor(true);
				// convertToByteProcessor.invertLut();
				// convertToByteProcessor.applyLut();
				convertToByteProcessor.threshold(Integer.parseInt("" + spnthreshold.getValue()));
				// convertToByteProcessor.autoThreshold();

				return convertToByteProcessor.getBufferedImage();
			} catch (Throwable t) {
				logger.error(t.getMessage(), t);
				throw new RuntimeException(t.getMessage(), t);
			}
		}
	};

	IRecognitionStrategy current = wekaRecognitionStrategy;

	JSlider sldtransparencytraining;

	JSlider sldResultTransparency;

	JButton btnlaunchrecognition;

	JButton btnloadbookimage;

	JButton btncloseimage;

	JButton btnvalidaterecognition;

	JLabel lblprogress;

	ScheduledExecutorService processingGuiLabel = Executors.newSingleThreadScheduledExecutor();

	/**
	 * add hole in layer, for recognition
	 *
	 */
	private class LayerHoleAdd extends CreationTool {
		public LayerHoleAdd(JEditableVirtualBookComponent virtualBookComponent, ImageAndHolesVisualizationLayer layer)
				throws Exception {
			super(virtualBookComponent, null, null, new CreationTool.CreationToolAction() {
				@Override
				public void handleAction(Hole n, boolean isRemove) {
					ArrayList<Hole> holes = layer.getHoles();
					if (holes == null) {
						holes = new ArrayList<>();
					}
					holes.add(n);
					layer.setHoles(holes);
					layer.setVisible(true);
					refreshTrainingLabels();
					assert virtualBookComponent != null;
					virtualBookComponent.repaint();
				}
			});
		}
	}

	// create select tool
	/**
	 * select rect in book, for recognition
	 *
	 */
	private class RectHoleAdd extends RectSelectTool {
		public RectHoleAdd(JEditableVirtualBookComponent virtualBookComponent, ImageAndHolesVisualizationLayer layer)
				throws Exception {
			super(virtualBookComponent, null, new RectSelectTool.RectSelectToolListener() {

				@Override
				public void rectDrawn(double xmin, double ymin, double xmax, double ymax) {
					double w = xmax - xmin;
					double h = ymax - ymin;
					if (w < 0 || h < 0) {
						// skip
						return;
					}
					layer.setVisible(true);
					layer.getAdditionalShapes().add(new Rectangle2D.Double(xmin, ymin, w, h));
					virtualBookComponent.repaint();
				}
			});

		}

	}

	private void refreshTrainingLabels() {
		Scale scale = virtualBookComponent.getVirtualBook().getScale();

		// compute the number of elements in training samples
		int b = 0;
		if (bookRegionDisplay.getHoles() != null) {
			b = (int) bookRegionDisplay.getHoles().stream().mapToDouble((h) -> scale.timeToMM(h.getTimeLength())).sum();
		}
		int h = 0;
		if (holeRegionDisplay.getHoles() != null) {
			h = (int) holeRegionDisplay.getHoles().stream().mapToDouble((hole) -> scale.timeToMM(hole.getTimeLength()))
					.sum();
		}
		changeTrainingExampleLabel(h, b);
	}

	@Override
	public void setTiledImage(ITiledImage tiledImage) {

		try {

			// from num, tiledImage are coming with RENORMED FAMILY

			if (tiledImage instanceof BookImageRecognitionTiledImage) {

				BookImageRecognitionTiledImage bri = (BookImageRecognitionTiledImage) tiledImage;
				backgroundBook.setTiledBackgroundimage(tiledImage);

				BookImageRecognitionTiledImage b = new BookImageRecognitionTiledImage(bri.getUnderlyingZipBookImage());
				b.setCurrentImageFamilyDisplay(REC_INLINE_FAMILY);
				recognitionDisplay.setTiledBackgroundimage(b);

			} else if (tiledImage instanceof ZipBookImage) {
				ZipBookImage zbi = (ZipBookImage) tiledImage;
				backgroundBook.setTiledBackgroundimage(zbi);

				BookImageRecognitionTiledImage b = new BookImageRecognitionTiledImage(zbi);
				b.setCurrentImageFamilyDisplay(REC_INLINE_FAMILY);
				recognitionDisplay.setTiledBackgroundimage(b);

			} else if (tiledImage instanceof StandaloneTiledImage) {

				backgroundBook.setTiledBackgroundimage(tiledImage);

				StandaloneRecognitionTiledImage b = new StandaloneRecognitionTiledImage(
						(StandaloneTiledImage) tiledImage);
				b.setCurrentImageFamilyDisplay(REC_INLINE_FAMILY);
				recognitionDisplay.setTiledBackgroundimage(b);

			} else if (tiledImage instanceof RecognitionTiledImage) {

				backgroundBook.setTiledBackgroundimage(tiledImage);

				RecognitionTiledImage b = new RecognitionTiledImage((RecognitionTiledImage) tiledImage);
				b.setCurrentImageFamilyDisplay(REC_INLINE_FAMILY);
				recognitionDisplay.setTiledBackgroundimage(b);

			} else {
				throw new RuntimeException("unsupported tiledImage format :" + tiledImage); //$NON-NLS-1$
			}

			backgroundBook.setVisible(true);

		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	/** undisplay the elements **/
	private class RemoveImageAction extends AbstractAction {

		public RemoveImageAction(String name) {
			super(name, new ImageIcon(JRecognitionVirtualBookPanel.class.getResource("fileclose.png")));//$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				recognitionDisplay.setTiledBackgroundimage(null);
				backgroundBook.setTiledBackgroundimage(null);
				backgroundBook.setBackgroundimage(null);

				holeRegionDisplay.setHoles(null);
				holeRegionDisplay.getAdditionalShapes().clear();
				bookRegionDisplay.setHoles(null);
				bookRegionDisplay.getAdditionalShapes().clear();

				if (virtualBookComponent != null) {
					virtualBookComponent.repaint();
				}

			} catch (Throwable t) {
				logger.error(t.getMessage(), t);
			}
		}

	}

	private class SetBackGroundAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6972644892906915997L;

		public SetBackGroundAction(String name) {
			super(name, new ImageIcon(JRecognitionVirtualBookPanel.class.getResource("fileopen.png")));//$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {

				APrintFileChooser aPrintFileChooser = new APrintFileChooser();
				aPrintFileChooser.addFileFilter(new VFSFileNameExtensionFilter(
						Messages.getString("JRecognitionVirtualBookPanel.1"), new String[] { "png", "jpg", "jpeg" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

				aPrintFileChooser.addFileFilter(new VFSFileNameExtensionFilter("Book image", //$NON-NLS-1$
						new String[] { BookImage.BOOKIMAGE_EXTENSION_WITHOUT_DOT }));

				int returnedValue = aPrintFileChooser.showOpenDialog(virtualBookComponent);
				if (returnedValue == APrintFileChooser.APPROVE_OPTION) {
					AbstractFileObject selectedFile = aPrintFileChooser.getSelectedFile();
					if (selectedFile != null) {
						File f = VFSTools.convertToFile(selectedFile);

						if (f.getName().toLowerCase().endsWith(BookImage.BOOKIMAGE_EXTENSION)) {

							ZipBookImage zbook = new ZipBookImage(f);
							setTiledImage(zbook);

						} else {
							logger.error("cannot display image " + f); //$NON-NLS-1$
						}
					}
					ensureLayersVisible();
				}

			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	/**
	 * visual layer for holes definition in training set
	 */
	LayerHoleAdd holeTool;

	/**
	 * visual layer for book surface definition in training set
	 */
	LayerHoleAdd bookTool;

	RectHoleAdd rectHoleTool;
	RectHoleAdd rectBookTool;

	JLabel lblholecpt;
	JLabel lblbookcpt;

	JCheckBox chkdisplaylayer;

	JRadioButton rbthrshold;
	JRadioButton rbIA;

	private boolean isInitializing = true;

	HashMap<Tool, JToggleButton> toolButtonAssociation = new HashMap<>();

	protected void initComponents() throws Exception {

		// init layers
		bookRegionDisplay.setHolesColor(Color.red);
		holeRegionDisplay.setHolesColor(Color.green);
		recognitionDisplay.setHolesColor(Color.blue);

		// hole overlay
		bookNotesOverlay.setHolesColor(Color.yellow);
		bookNotesOverlay.setHolesStroke(new BasicStroke(1f));
		bookNotesOverlay.setOpacity(1.0f);
		bookNotesOverlay.setVisible(false);

		InputStream resourceAsStream = getClass().getResourceAsStream("toolspanel.jfrm"); //$NON-NLS-1$
		assert resourceAsStream != null;
		FormPanel fp = new FormPanel(resourceAsStream);

		FormAccessor fa = fp.getFormAccessor();

		// labels
		JLabel lblexecute = fa.getLabel("lblexecute");// $NON-NLS-1$ //$NON-NLS-1$
		lblexecute.setText(Messages.getString("JRecognitionVirtualBookPanel.502")); //$NON-NLS-1$

		JLabel lbltools = fa.getLabel("lbltools");// $NON-NLS-1$ //$NON-NLS-1$
		assert lbltools != null;
		lbltools.setText(Messages.getString("JRecognitionVirtualBookPanel.9")); //$NON-NLS-1$

		JLabel lblResultTransparency = fa.getLabel("lblResultTransparency");// $NON-NLS-1$ //$NON-NLS-1$
		assert lblResultTransparency != null;

		lblResultTransparency.setText(Messages.getString("JRecognitionVirtualBookPanel.11")); //$NON-NLS-1$

		ButtonGroup buttonGroup = new ButtonGroup();

		rbthrshold = fa.getRadioButton("rbthrshold");
		rbthrshold.setText("Simple Color Threshold Model");
		assert rbthrshold != null;
		buttonGroup.add(rbthrshold);

		rbIA = fa.getRadioButton("rbIA");
		rbIA.setText("Machine Learning Recognition Model");

		assert rbIA != null;
		buttonGroup.add(rbIA);

		// both forms
		final Component iaform = fa.getComponentByName("iaform");
		final Component thrsholdform = fa.getComponentByName("thrsholdform");

		ItemListener rbitemListener = (r) -> {
			try {
				SwingUtils.recurseSetEnable(iaform, rbIA.isSelected());
				SwingUtils.recurseSetEnable(thrsholdform, rbthrshold.isSelected());

				if (rbIA.isSelected()) {
					current = wekaRecognitionStrategy;
				} else {
					current = thresholdStrategy;
				}

			} catch (Throwable t) {
				logger.error(t.getMessage(), t);
			}
		};

		JLabel labelThreshold = fa.getLabel("lblThreshold");//$NON-NLS-1$
		labelThreshold.setText("Choose grey threshold");

		rbthrshold.addItemListener(rbitemListener);
		rbIA.addItemListener(rbitemListener);

		rbIA.setSelected(true);

		//
		chkdisplaylayer = fa.getCheckBox("chkdisplaylayer"); //$NON-NLS-1$

		chkdisplaylayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean selected = chkdisplaylayer.getModel().isSelected();
				bookRegionDisplay.setVisible(selected);
				holeRegionDisplay.setVisible(selected);
				recognitionDisplay.setVisible(selected);
				backgroundBook.setVisible(selected);
				bookNotesOverlay.setVisible(selected);
				if (virtualBookComponent != null) {
					virtualBookComponent.repaint();
				}
				chkdisplaylayer.repaint();
			}
		});
		chkdisplaylayer.setText(Messages.getString("JRecognitionVirtualBookPanel.505")); //$NON-NLS-1$
		chkdisplaylayer.getModel().setSelected(false);

		holeCreationToolbtn = (JToggleButton) fa.getButton("toolcreatehole"); // $NON-NLS-1$ //$NON-NLS-1$
		assert holeCreationToolbtn != null;
		holeCreationToolbtn.addActionListener((e) -> {
			try {
				if (holeTool == null) {
					holeTool = new LayerHoleAdd(virtualBookComponent, holeRegionDisplay);
					toolButtonAssociation.put(holeTool, holeCreationToolbtn);
				}
				virtualBookComponent.setCurrentTool(holeTool);
				ensureLayersVisible();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		});

		holeCreationToolbtn.setText("");// $NON-NLS-1$ //$NON-NLS-1$
		holeCreationToolbtn.setToolTipText(Messages.getString("JRecognitionVirtualBookPanel.14")); //$NON-NLS-1$
		holeCreationToolbtn.setIcon(new ImageIcon(
				ImageTools.loadImage(JRecognitionVirtualBookPanel.class.getResource("pencil_green.png"))));// $NON-NLS-1$ //$NON-NLS-1$

		lblholecpt = fa.getLabel("lblholecpt");//$NON-NLS-1$

		bookCreationToolbtn = (JToggleButton) fa.getButton("toolcreatebook");// $NON-NLS-1$
		URL resource = JRecognitionVirtualBookPanel.class.getResource("pencil_red.png");// $NON-NLS-1$ //$NON-NLS-1$
		BufferedImage pencilImage = ImageTools.loadImage(resource);
		bookCreationToolbtn.setIcon(new ImageIcon(pencilImage));// $NON-NLS-1$

		assert bookCreationToolbtn != null;
		bookCreationToolbtn.addActionListener((e) -> {
			try {
				if (bookTool == null) {
					bookTool = new LayerHoleAdd(virtualBookComponent, bookRegionDisplay);
					toolButtonAssociation.put(bookTool, bookCreationToolbtn);
				}
				virtualBookComponent.setCurrentTool(bookTool);
				ensureLayersVisible();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		});
		bookCreationToolbtn.setText(""); //$NON-NLS-1$
		bookCreationToolbtn.setToolTipText(Messages.getString("JRecognitionVirtualBookPanel.20")); // $NON-NL)S-1$ //$NON-NLS-1$

		////////////////////////////////////////////////////////////////////////////////
		// rect

		rectbookCreationToolbtn = (JToggleButton) fa.getButton("recttoolcreatebook");
		assert rectbookCreationToolbtn != null;
		rectbookCreationToolbtn.addActionListener((e) -> {
			try {
				if (rectBookTool == null) {
					rectBookTool = new RectHoleAdd(virtualBookComponent, bookRegionDisplay);
					toolButtonAssociation.put(rectBookTool, rectbookCreationToolbtn);
				}
				virtualBookComponent.setCurrentTool(rectBookTool);
				ensureLayersVisible();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		});

		rectHoleCreationToolbtn = (JToggleButton) fa.getButton("recttoolcreatehole");
		assert rectHoleCreationToolbtn != null;
		rectHoleCreationToolbtn.addActionListener((e) -> {
			try {
				if (rectHoleTool == null) {
					rectHoleTool = new RectHoleAdd(virtualBookComponent, holeRegionDisplay);
					toolButtonAssociation.put(rectHoleTool, rectHoleCreationToolbtn);
				}
				virtualBookComponent.setCurrentTool(rectHoleTool);
				ensureLayersVisible();

			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}

		});

		//////////////////////////////////////////////////////////////////////

		lblbookcpt = fa.getLabel("lblbookcpt"); //$NON-NLS-1$

		sldtransparencytraining = (JSlider) fa.getComponentByName("sldtransparencytraining");// $NON-NLS-1$ //$NON-NLS-1$
		sldtransparencytraining.setToolTipText(Messages.getString("JRecognitionVirtualBookPanel.22")); //$NON-NLS-1$
		sldtransparencytraining.addChangeListener((e) -> {

			int transparencyValue = sldtransparencytraining.getValue();
			float t = transparencyValue / 100.0f;

			ensureLayersVisible();
			// add transparency
			recognitionDisplay.setOpacity(t);
			backgroundBook.setOpacity(1.0f - t);
			if (virtualBookComponent != null) {
				virtualBookComponent.repaint();
			}
		});

		sldResultTransparency = (JSlider) fa.getComponentByName("sliderTransparency"); //$NON-NLS-1$
		assert sldResultTransparency != null;
		sldResultTransparency.setToolTipText(Messages.getString("JRecognitionVirtualBookPanel.24")); //$NON-NLS-1$
		sldResultTransparency.addChangeListener((e) -> {

			int transparencyValue = sldResultTransparency.getValue();
			float t = (1.0f - transparencyValue / 100.0f);

			// add transparency
			if (virtualBookComponent != null) {
				virtualBookComponent.setHoleTransparency(t);
				ensureLayersVisible();

			}

		});

		AbstractButton lblstoprecognition = fa.getButton("lblstoprecognition");// $NON-NLS-1$ //$NON-NLS-1$
		lblstoprecognition.setText(Messages.getString("JRecognitionVirtualBookPanel.507")); //$NON-NLS-1$
		lblstoprecognition.setIcon(new ImageIcon(getClass().getResource("fileclose.png")));// $NON-NLS-1$ //$NON-NLS-1$
		lblstoprecognition.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					closeBackGroundThread();
				} catch (Throwable t) {
					logger.debug("error in closing thread " + t.getMessage(), t);
				}
				if (virtualBookComponent != null) {
					virtualBookComponent.repaint();
				}
			}

		});

		btnlaunchrecognition = (JButton) fa.getButton("btnlaunchrecognition");// $NON-NLS-1$ //$NON-NLS-1$
		btnlaunchrecognition.setText("");// $NON-NLS-1$ //$NON-NLS-1$

		btnlaunchrecognition.setIcon(new ImageIcon(
				ImageTools.loadImage(JRecognitionVirtualBookPanel.class.getResource("kaboodleloop.png"))));// $NON-NLS-1$ //$NON-NLS-1$
		btnlaunchrecognition.setToolTipText(Messages.getString("JRecognitionVirtualBookPanel.28")); //$NON-NLS-1$
		btnlaunchrecognition.addActionListener((e) -> {
			try {
				ensureLayersVisible();
				launchRecognition();
			} catch (Exception ex) {

				logger.error(ex.getMessage(), ex);
				JMessageBox.showError(this, ex);
			}
		});

		btnloadbookimage = (JButton) fa.getButton("btnloadbookimage");// $NON-NLS-1$ //$NON-NLS-1$
		assert btnloadbookimage != null;
		btnloadbookimage.setText(Messages.getString("JRecognitionVirtualBookPanel.30")); //$NON-NLS-1$

		btnloadbookimage.setAction(new SetBackGroundAction(Messages.getString("JRecognitionVirtualBookPanel.32"))); //$NON-NLS-1$
		btnloadbookimage.addActionListener((e) -> {
			ensureLayersVisible();
		});

		btncloseimage = (JButton) fa.getButton("btnremovelayer");//$NON-NLS-1$
		btncloseimage.setAction(new RemoveImageAction(Messages.getString("JRecognitionVirtualBookPanel.509"))); //$NON-NLS-1$

		btnvalidaterecognition = (JButton) fa.getButton("btnvalidaterecognition");// $NON-NLS-1$ //$NON-NLS-1$
		btnvalidaterecognition.setText(Messages.getString("JRecognitionVirtualBookPanel.34")); //$NON-NLS-1$

		JLabel lbldisplay = fa.getLabel("lbldisplay");
		lbldisplay.setText("Layer Display Settings");

		JLabel lblgetResult = fa.getLabel("lblgetResult");
		lblgetResult.setText("Recognition Result into VirtualBook");

		JLabel lblminwidth = fa.getLabel("lblminwidth");
		lblminwidth.setText("Min recognition hole (mm)");

		spnminwidth = fa.getSpinner("spnminwidth");
		spnminwidth.setModel(new SpinnerNumberModel(3.0f, 0.0f, 10.0f, 0.1f));

		btnvalidaterecognition.addActionListener((l) -> {

			ensureLayersVisible();

			logger.debug("get the hole copy"); //$NON-NLS-1$
			ArrayList<Hole> recognizedHoles = recognitionDisplay.getHoles();
			if (recognizedHoles == null) {
				return;
			}

			virtualBookComponent.startEventTransaction();
			try {

				double min = Double.parseDouble("" + this.spnminwidth.getValue());

				VirtualBook vb = virtualBookComponent.getVirtualBook();
				Scale scale = vb.getScale();

				List<Hole> filteredHoles = recognizedHoles.stream()
						.filter((h) -> scale.timeToMM(h.getTimeLength()) >= min).collect(Collectors.toList());

				UndoStack us = virtualBookComponent.getUndoStack();
				if (us != null) {
					us.push(new GlobalVirtualBookUndoOperation(vb,
							Messages.getString("JRecognitionVirtualBookPanel.36"), virtualBookComponent)); //$NON-NLS-1$
				}

				vb.clear();

				vb.addHole(filteredHoles);
			} catch (Throwable t) {
				logger.error(t.getMessage(), t);
				JMessageBox.showError(this, t);
			} finally {
				virtualBookComponent.endEventTransaction();
			}

		});

		btnvalidaterecognition.setIcon(new ImageIcon(getClass().getResource("editcopy.png"))); //$NON-NLS-1$

		lblprogress = fa.getLabel("lblprogress"); //$NON-NLS-1$
		assert lblprogress != null;
		lblprogress.setText(Messages.getString("JRecognitionVirtualBookPanel.38")); //$NON-NLS-1$

		changeTrainingExampleLabel(0, 0);

		spnthreshold = fa.getSpinner("spnthreshold");
		spnthreshold.setModel(new SpinnerNumberModel(127, 0, 255, 5));

		setLayout(new BorderLayout());
		add(fp, BorderLayout.CENTER);

		// on event change, permit to not display the layers
		isInitializing = false;

	}

	/**
	 * ensure the layers are visible
	 */
	private void ensureLayersVisible() {
		if (!isInitializing && virtualBookComponent != null) {
			chkdisplaylayer.getModel().setSelected(true);
			bookNotesOverlay.setVisible(true);
			virtualBookComponent.repaint();
		}
	}

	private void changeTrainingExampleLabel(int hole, int book) {
		lblholecpt.setText("" + hole); //$NON-NLS-1$
		lblbookcpt.setText("" + book); //$NON-NLS-1$
	}

	private JEditableVirtualBookComponent virtualBookComponent;

	/**
	 * setup the virtualbook component
	 * 
	 * @param virtualBookComponent
	 */
	public void setVirtualBookComponent(JVirtualBookScrollableComponent virtualBookComponent) {

		sldResultTransparency.setValue(1);
		sldtransparencytraining.setValue(1);

		this.virtualBookComponent = (JEditableVirtualBookComponent) virtualBookComponent;

		this.virtualBookComponent.addCurrentToolChangedListener(new CurrentToolChanged() {

			void resetAllBut(Tool newTool) {

				toolButtonAssociation.keySet().stream().forEach((tool) -> {
					if (tool != newTool) {
						toolButtonAssociation.get(tool).setSelected(false);
					}

				});

			}

			@Override
			public void currentToolChanged(Tool oldTool, Tool newTool) {
				// un select the tools
				resetAllBut(newTool);

				logger.debug("current tool :" + newTool + " old one :" + oldTool); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		// init feedback

		this.processingGuiLabel.scheduleAtFixedRate(() -> {
			BackgroundTileImageProcessingThread b = backGroundThread;

			SwingUtilities.invokeLater(() -> {
				if (b == null) {
					lblprogress.setText(Messages.getString("JRecognitionVirtualBookPanel.41")); //$NON-NLS-1$
				} else {
					lblprogress.setText("" + (int) (b.currentProgress() * 100) + " %"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			});
		}, 5, 5, TimeUnit.SECONDS);

	}

	public JVirtualBookScrollableComponent getVirtualBookComponent() {
		return virtualBookComponent;
	}

	private BackgroundTileImageProcessingThread<Void> backGroundThread;

	private JSpinner spnminwidth;

	private JSpinner spnthreshold;

	List<Rectangle2D.Double> convertToRectangleDouble(List<Shape> s) {
		ArrayList<java.awt.geom.Rectangle2D.Double> result = new ArrayList<Rectangle2D.Double>();
		if (s == null) {
			return result;
		}

		for (Shape shape : s) {
			if (!(shape instanceof Rectangle2D.Double)) {
				logger.warn("implementation error, some zones are not of the proper type");
				continue;
			}
			result.add((Rectangle2D.Double) shape);
		}
		return result;
	}

	private void launchRecognition() throws Exception {

		ArrayList<Hole> bookHoles = bookRegionDisplay.getHoles();
		ArrayList<Hole> holesHoles = holeRegionDisplay.getHoles();

		List<Rectangle2D.Double> freeBookRects = convertToRectangleDouble(bookRegionDisplay.getAdditionalShapes());
		List<Rectangle2D.Double> freeHoleRects = convertToRectangleDouble(holeRegionDisplay.getAdditionalShapes());

		// classify holes into training set
		IFamilyImageSeekerTiledImage imageToRecognize = (IFamilyImageSeekerTiledImage) getBackgroundImage();

		IFileFamilyTiledImage ti = null;

		// create output tiled image

		if (imageToRecognize instanceof BookImage) {
			ti = new BookImageRecognitionTiledImage((ZipBookImage) imageToRecognize);
		} else if (imageToRecognize instanceof RecognitionTiledImage) {
			ti = new RecognitionTiledImage((RecognitionTiledImage) imageToRecognize);
		} else if (imageToRecognize instanceof StandaloneTiledImage) {
			ti = new StandaloneRecognitionTiledImage((StandaloneTiledImage) imageToRecognize);
		} else if (imageToRecognize instanceof BookImageRecognitionTiledImage) {
			ti = new BookImageRecognitionTiledImage(
					((BookImageRecognitionTiledImage) imageToRecognize).getUnderlyingZipBookImage());
		} else {
			throw new Exception("unsupported image object format"); //$NON-NLS-1$
		}

		final IFileFamilyTiledImage tiledImage = ti;

		tiledImage.setCurrentImageFamilyDisplay(REC_INLINE_FAMILY);

		recognitionDisplay.setTiledBackgroundimage(tiledImage);

		if (rbIA.isSelected()) {

			logger.debug("AI, strategy");

			wekaRecognitionStrategy = new WekaRecognitionStrategy(virtualBookComponent.getVirtualBook().getScale());

			wekaRecognitionStrategy.train(bookHoles, freeBookRects, holesHoles, freeHoleRects, imageToRecognize);
			current = wekaRecognitionStrategy;

		} else {
			logger.debug("threshold strategy");
			current = thresholdStrategy;
		}

		if (backGroundThread != null) {
			backGroundThread.cancel();
			backGroundThread = null;
		}

		// clear
		recognitionDisplay.setHoles(null);

		/**
		 * ordering the task queue depending on the user point of view
		 * 
		 * @author pfreydiere
		 *
		 */
		class PriorityImageComparator implements Comparator<Integer> {
			private int center;

			PriorityImageComparator(int center) {
				this.center = center;
			}

			@Override
			public int compare(Integer o1, Integer o2) {
				int d1 = Math.abs(o1 - center);
				int d2 = Math.abs(o2 - center);
				return Integer.compare(d1, d2);
			}
		}

		backGroundThread = new BackgroundTileImageProcessingThread<>(tiledImage, new TiledProcessedListener() {

			@Override
			public <T> void tileProcessed(int index, T parameter) {

				SwingUtilities.invokeLater(() -> {
					assert virtualBookComponent != null;
					virtualBookComponent.repaint();
				});

				// change tile computing priority
				int[] visibleTiles = backgroundBook.getCurrentVisibleTiles();

				int min = Integer.MAX_VALUE;
				int max = Integer.MIN_VALUE;
				for (int i = 0; i < visibleTiles.length; i++) {
					min = Math.min(visibleTiles[i], min);
					max = Math.max(visibleTiles[i], max);
				}

				backGroundThread.sortProcessingQueue(new PriorityImageComparator((min + max) / 2));
			}

			@Override
			public void errorInProcessingTile(String errormsg) {
				logger.error(errormsg);
			}
		}, 1); // one thread

		backGroundThread.start(new TileProcessing<Void>() {
			@Override
			public Void process(int index, BufferedImage image) throws Exception {

				logger.debug("loading image " + index);
				BufferedImage bi = imageToRecognize.loadImage(index);
				if (bi == null) {
					logger.error("erroneous index, image number " + index + " cannot be loaded in " + imageToRecognize);
					return null;
				}

				BufferedImage binaryresult = current.apply(bi);

				String fileName = tiledImage.constructImagePath(index, REC_INLINE_FAMILY).getAbsolutePath();
				logger.debug("saving " + fileName); //$NON-NLS-1$
				IJ.save(new ImagePlus("", binaryresult), fileName);

				Scale scale = virtualBookComponent.getVirtualBook().getScale();

				ReadResultBag readResult = BookReadProcessor.readResult2(binaryresult, index * bi.getWidth(),
						scale.mmToTime(scale.getWidth() / imageToRecognize.getHeight()), scale, null, false, 150);

				ArrayList<Hole> holes = recognitionDisplay.getHoles();
				if (holes == null) {
					holes = new ArrayList<Hole>();
				}

				holes.addAll(readResult.virtualbook.getHolesCopy());
				// display results
				recognitionDisplay.setHoles(holes);

				return null;

			}
		});
	}

	IFamilyImageSeekerTiledImage getBackgroundImage() {

		ITiledImage t = backgroundBook.getTiledBackgroundimage();

		if (t == null) {
			// reset
			recognitionDisplay.setTiledBackgroundimage(null);
			return null;
		}

		assert t instanceof IFamilyImageSeekerTiledImage;

		return (IFamilyImageSeekerTiledImage) t;
	}

	private void closeBackGroundThread() {
		// close background thread
		BackgroundTileImageProcessingThread<Void> b = backGroundThread;
		if (b != null) {
			b.dispose();
		}
		backGroundThread = null;

	}

	@Override
	public void dispose() {
		closeBackGroundThread();

		List<Runnable> sutdown = processingGuiLabel.shutdownNow();
		this.processingGuiLabel = null;
	}

}
