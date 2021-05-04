package org.barrelorgandiscovery.recognition.gui.bookext;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.bookimage.BookImage;
import org.barrelorgandiscovery.bookimage.ZipBookImage;
import org.barrelorgandiscovery.gui.aedit.CreationTool;
import org.barrelorgandiscovery.gui.aedit.CurrentToolChanged;
import org.barrelorgandiscovery.gui.aedit.GlobalVirtualBookUndoOperation;
import org.barrelorgandiscovery.gui.aedit.ImageAndHolesVisualizationLayer;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
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
import org.barrelorgandiscovery.ui.tools.VFSTools;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ShapeRoi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import trainableSegmentation.WekaSegmentation;
import weka.core.Instances;

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

	JSlider sldtransparencytraining;

	JSlider sldResultTransparency;

	JButton btnlaunchrecognition;

	JButton btnloadbookimage;

	JButton btncloseimage;

	JButton btnvalidaterecognition;

	JLabel lblprogress;

	ScheduledExecutorService processingGuiLabel = Executors.newSingleThreadScheduledExecutor();

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
					refreshTrainingLabels();
					virtualBookComponent.repaint();
				}
			});
		}
	}

	private void refreshTrainingLabels() {
		Scale scale = virtualBookComponent.getVirtualBook().getScale();

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
				bookRegionDisplay.setHoles(null);

				virtualBookComponent.repaint();
				
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

	JLabel lblholecpt;
	JLabel lblbookcpt;

	JCheckBox chkdisplaylayer;

	protected void initComponents() throws Exception {

		// init layers
		bookRegionDisplay.setHolesColor(Color.red);
		holeRegionDisplay.setHolesColor(Color.green);
		recognitionDisplay.setHolesColor(Color.blue);

		InputStream resourceAsStream = getClass().getResourceAsStream("toolspanel.jfrm"); //$NON-NLS-1$
		assert resourceAsStream != null;
		FormPanel fp = new FormPanel(resourceAsStream);

		FormAccessor fa = fp.getFormAccessor();

		// labels
		JLabel lblexecute = fa.getLabel("lblexecute");// $NON-NLS-1$ //$NON-NLS-1$
		lblexecute.setText(Messages.getString("JRecognitionVirtualBookPanel.502")); //$NON-NLS-1$
		JLabel lblprogresslabel = fa.getLabel("lblprogresslabel");// $NON-NLS-1$ //$NON-NLS-1$
		lblprogresslabel.setText(Messages.getString("JRecognitionVirtualBookPanel.504")); //$NON-NLS-1$

		JLabel lbltools = fa.getLabel("lbltools");// $NON-NLS-1$ //$NON-NLS-1$
		assert lbltools != null;
		lbltools.setText(Messages.getString("JRecognitionVirtualBookPanel.9")); //$NON-NLS-1$

		JLabel lblResultTransparency = fa.getLabel("lblResultTransparency");// $NON-NLS-1$ //$NON-NLS-1$
		assert lblResultTransparency != null;

		lblResultTransparency.setText(Messages.getString("JRecognitionVirtualBookPanel.11")); //$NON-NLS-1$

		//
		chkdisplaylayer = fa.getCheckBox("chkdisplaylayer"); //$NON-NLS-1$
		chkdisplaylayer.getModel().setSelected(true);
		chkdisplaylayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean selected = chkdisplaylayer.getModel().isSelected();
				bookRegionDisplay.setVisible(selected);
				holeRegionDisplay.setVisible(selected);
				recognitionDisplay.setVisible(selected);
				backgroundBook.setVisible(selected);
				virtualBookComponent.repaint();
			}
		});
		chkdisplaylayer.setText(Messages.getString("JRecognitionVirtualBookPanel.505")); //$NON-NLS-1$

		holeCreationToolbtn = (JToggleButton) fa.getButton("toolcreatehole"); // $NON-NLS-1$ //$NON-NLS-1$
		assert holeCreationToolbtn != null;
		holeCreationToolbtn.addActionListener((e) -> {
			try {
				if (holeTool == null) {
					holeTool = new LayerHoleAdd(virtualBookComponent, holeRegionDisplay);
				}
				virtualBookComponent.setCurrentTool(holeTool);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		});

		holeCreationToolbtn.setText("");// $NON-NLS-1$ //$NON-NLS-1$
		holeCreationToolbtn.setToolTipText(Messages.getString("JRecognitionVirtualBookPanel.14")); //$NON-NLS-1$
		holeCreationToolbtn.setIcon(new ImageIcon(
				ImageTools.loadImage(JRecognitionVirtualBookPanel.class.getResource("pencil_green.png"))));// $NON-NLS-1$ //$NON-NLS-1$

		lblholecpt = fa.getLabel("lblholecpt");//$NON-NLS-1$

		bookCreationToolbtn = (JToggleButton) fa.getButton("toolcreatebook");// $NON-NLS-1$ //$NON-NLS-1$
		URL resource = JRecognitionVirtualBookPanel.class.getResource("pencil_red.png");// $NON-NLS-1$ //$NON-NLS-1$
		BufferedImage pencilImage = ImageTools.loadImage(resource);
		bookCreationToolbtn.setIcon(new ImageIcon(pencilImage));// $NON-NLS-1$

		assert bookCreationToolbtn != null;
		bookCreationToolbtn.addActionListener((e) -> {
			try {
				if (bookTool == null)
					bookTool = new LayerHoleAdd(virtualBookComponent, bookRegionDisplay);
				virtualBookComponent.setCurrentTool(bookTool);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		});
		bookCreationToolbtn.setText(""); //$NON-NLS-1$
		bookCreationToolbtn.setToolTipText(Messages.getString("JRecognitionVirtualBookPanel.20")); // $NON-NL)S-1$ //$NON-NLS-1$

		lblbookcpt = fa.getLabel("lblbookcpt"); //$NON-NLS-1$

		sldtransparencytraining = (JSlider) fa.getComponentByName("sldtransparencytraining");// $NON-NLS-1$ //$NON-NLS-1$
		sldtransparencytraining.setToolTipText(Messages.getString("JRecognitionVirtualBookPanel.22")); //$NON-NLS-1$
		sldtransparencytraining.addChangeListener((e) -> {

			int transparencyValue = sldtransparencytraining.getValue();
			float t = transparencyValue / 100.0f;

			// add transparency
			recognitionDisplay.setOpacity(t);
			backgroundBook.setOpacity(1.0f - t);
			virtualBookComponent.repaint();
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
				virtualBookComponent.repaint();
			}

		});

		AbstractButton lblstoprecognition = fa.getButton("lblstoprecognition");// $NON-NLS-1$ //$NON-NLS-1$
		lblstoprecognition.setText(Messages.getString("JRecognitionVirtualBookPanel.507")); //$NON-NLS-1$
		lblstoprecognition.setIcon(new ImageIcon(getClass().getResource("fileclose.png")));// $NON-NLS-1$ //$NON-NLS-1$
		lblstoprecognition.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeBackGroundThread();
			}

		});

		btnlaunchrecognition = (JButton) fa.getButton("btnlaunchrecognition");// $NON-NLS-1$ //$NON-NLS-1$
		btnlaunchrecognition.setText("");// $NON-NLS-1$ //$NON-NLS-1$

		btnlaunchrecognition.setIcon(new ImageIcon(
				ImageTools.loadImage(JRecognitionVirtualBookPanel.class.getResource("kaboodleloop.png"))));// $NON-NLS-1$ //$NON-NLS-1$
		btnlaunchrecognition.setToolTipText(Messages.getString("JRecognitionVirtualBookPanel.28")); //$NON-NLS-1$
		btnlaunchrecognition.addActionListener((e) -> {
			try {
				launchRecognition();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		});

		btnloadbookimage = (JButton) fa.getButton("btnloadbookimage");// $NON-NLS-1$ //$NON-NLS-1$
		assert btnloadbookimage != null;
		btnloadbookimage.setText(Messages.getString("JRecognitionVirtualBookPanel.30")); //$NON-NLS-1$

		btnloadbookimage.setAction(new SetBackGroundAction(Messages.getString("JRecognitionVirtualBookPanel.32"))); //$NON-NLS-1$

		btncloseimage = (JButton) fa.getButton("btnremovelayer");//$NON-NLS-1$
		btncloseimage.setAction(new RemoveImageAction(Messages.getString("JRecognitionVirtualBookPanel.509"))); //$NON-NLS-1$

		btnvalidaterecognition = (JButton) fa.getButton("btnvalidaterecognition");// $NON-NLS-1$ //$NON-NLS-1$
		btnvalidaterecognition.setText(Messages.getString("JRecognitionVirtualBookPanel.34")); //$NON-NLS-1$

		Border btnBorderGetResult = btnvalidaterecognition.getBorder();
		CompoundBorder tb = (CompoundBorder) btnBorderGetResult;
		((TitledBorder) tb.getOutsideBorder()).setTitle(Messages.getString("JRecognitionVirtualBookPanel.10027")); //$NON-NLS-1$

		btnvalidaterecognition.addActionListener((l) -> {

			logger.debug("get the hole copy"); //$NON-NLS-1$
			ArrayList<Hole> recognizedHoles = recognitionDisplay.getHoles();
			if (recognizedHoles == null) {
				return;
			}

			virtualBookComponent.startEventTransaction();
			try {
				VirtualBook vb = virtualBookComponent.getVirtualBook();
				Scale scale = vb.getScale();

				List<Hole> filteredHoles = recognizedHoles.stream()
						.filter((h) -> scale.timeToMM(h.getTimeLength()) > 3.0).collect(Collectors.toList());

				UndoStack us = virtualBookComponent.getUndoStack();
				if (us != null) {
					us.push(new GlobalVirtualBookUndoOperation(vb,
							Messages.getString("JRecognitionVirtualBookPanel.36"), virtualBookComponent)); //$NON-NLS-1$
				}

				vb.clear();

				vb.addHole(filteredHoles);
			} finally {
				virtualBookComponent.endEventTransaction();
			}

		});

		btnvalidaterecognition.setIcon(new ImageIcon(getClass().getResource("editcopy.png"))); //$NON-NLS-1$

		lblprogress = fa.getLabel("lblprogress"); //$NON-NLS-1$
		assert lblprogress != null;
		lblprogress.setText(Messages.getString("JRecognitionVirtualBookPanel.38")); //$NON-NLS-1$

		changeTrainingExampleLabel(0, 0);

		setLayout(new BorderLayout());
		add(fp, BorderLayout.CENTER);

	}

	private void changeTrainingExampleLabel(int hole, int book) {
		lblholecpt.setText("" + hole); //$NON-NLS-1$
		lblbookcpt.setText("" + book); //$NON-NLS-1$
	}

	private JEditableVirtualBookComponent virtualBookComponent;

	public void setVirtualBookComponent(JVirtualBookScrollableComponent virtualBookComponent) {
		this.virtualBookComponent = (JEditableVirtualBookComponent) virtualBookComponent;

		sldResultTransparency.setValue(1);
		sldtransparencytraining.setValue(1);

		this.virtualBookComponent.addCurrentToolChangedListener(new CurrentToolChanged() {
			@Override
			public void currentToolChanged(Tool oldTool, Tool newTool) {
				// un select the tools
				if (oldTool == holeTool) {
					holeCreationToolbtn.setSelected(false);
				}
				if (oldTool == bookTool) {
					bookCreationToolbtn.setSelected(false);
				}

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

	private static int BOOK_CLASS = 0;
	private static int HOLES_CLASS = 1;

	private BackgroundTileImageProcessingThread<Void> backGroundThread;

	private void launchRecognition() throws Exception {

		ArrayList<Hole> bookHoles = bookRegionDisplay.getHoles();
		ArrayList<Hole> holesHoles = holeRegionDisplay.getHoles();

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
		}

		else {
			throw new Exception("unsupported image object format"); //$NON-NLS-1$
		}

		final IFileFamilyTiledImage tiledImage = ti;

		tiledImage.setCurrentImageFamilyDisplay(REC_INLINE_FAMILY);

		recognitionDisplay.setTiledBackgroundimage(tiledImage);

		Instances instances = null;

		assert bookHoles != null;
		assert holesHoles != null;

		for (Hole h : bookHoles) {
			instances = constructTrainingSet(instances, h, BOOK_CLASS);
		}

		for (Hole h : holesHoles) {
			instances = constructTrainingSet(instances, h, HOLES_CLASS);
		}

		if (instances == null) {
			logger.info("no training set, no recognition launched"); //$NON-NLS-1$
			return;
		}

		WekaSegmentation ws = new WekaSegmentation();
		updateSegmentationParameters(ws);
		ws.setLoadedTrainingData(instances);

		ws.doClassBalance();
		// ws.selectAttributes();

		ws.trainClassifier();

		if (backGroundThread != null) {
			backGroundThread.cancel();
			backGroundThread = null;
		}

		recognitionDisplay.setHoles(null);

		class Priority implements Comparator<Integer> {
			private int center;

			Priority(int center) {
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

				backGroundThread.sortProcessingQueue(new Priority((min + max) / 2));
			}

			@Override
			public void errorInProcessingTile(String errormsg) {
				logger.error(errormsg);
			}
		}, 1);
		backGroundThread.start(new TileProcessing<Void>() {
			@Override
			public Void process(int index, BufferedImage image) throws Exception {
				ClassLoader old = Thread.currentThread().getContextClassLoader();

				BufferedImage bi = imageToRecognize.loadImage(index);
				if (bi == null) {
					logger.error("erroneous index, image number " + index + " cannot be loaded in " + imageToRecognize);
					return null;
				}

				ImagePlus ip = new ImagePlus();
				ip.setImage(bi);
				ImagePlus result = ws.applyClassifier(ip);

				ImageProcessor processor = result.getProcessor();
				processor.multiply(250);

				ByteProcessor bp = processor.convertToByteProcessor(true);

				ImagePlus binary = new ImagePlus("", bp); //$NON-NLS-1$

				String fileName = tiledImage.constructImagePath(index, REC_INLINE_FAMILY).getAbsolutePath();
				logger.debug("saving " + fileName); //$NON-NLS-1$
				IJ.save(binary, fileName);

				Scale scale = virtualBookComponent.getVirtualBook().getScale();
				ReadResultBag readResult = BookReadProcessor.readResult2(binary.getBufferedImage(),
						index * bi.getWidth(), scale.mmToTime(scale.getWidth() / imageToRecognize.getHeight()), scale,
						null, false, 150);

				ArrayList<Hole> holes = recognitionDisplay.getHoles();
				if (holes == null) {
					holes = new ArrayList<Hole>();
				}

				holes.addAll(readResult.virtualbook.getHolesCopy());

				recognitionDisplay.setHoles(holes);

				return null;

			}
		});
	}

	private void updateSegmentationParameters(WekaSegmentation ws) {

		// LinearRegression lr = new LinearRegression();
//		Logistic l = new Logistic();
//		l.setUseConjugateGradientDescent(true);
//		
//
//		MultiClassClassifier mcc = new MultiClassClassifier();
//		mcc.setClassifier(l);
//		mcc.setLogLossDecoding(true);
//		mcc.setNumDecimalPlaces(6);
//		mcc.setUsePairwiseCoupling(true);
//		
//		ws.setClassifier(mcc);
//		

		// ws.updateClassifier(200, 6, 10);

		boolean[] enabledFeatures = ws.getEnabledFeatures();
//		for (int i = 0 ; i < enabledFeatures.length ; i ++) {
//			enabledFeatures[i] = true;
//		}
		/*
		 * enabledFeatures[FeatureStack.DERIVATIVES] = false;
		 * 
		 * enabledFeatures[FeatureStack.GABOR] = true;
		 * enabledFeatures[FeatureStack.LIPSCHITZ] = true; //
		 * enabledFeatures[FeatureStack.HESSIAN] = true;
		 * enabledFeatures[FeatureStack.NEIGHBORS] = true;
		 * enabledFeatures[FeatureStack.GAUSSIAN] = true;
		 * 
		 * enabledFeatures[FeatureStack.VARIANCE] = true;
		 * enabledFeatures[FeatureStack.STRUCTURE] = true;
		 * 
		 * ws.setEnabledFeatures(enabledFeatures);
		 */
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

	private Instances constructTrainingSet(Instances instances, Hole hole, int classNo) throws Exception {
		Scale scale = virtualBookComponent.getVirtualBook().getScale();

		IFamilyImageSeekerTiledImage zi = getBackgroundImage();

		int height = zi.getHeight();

		double mm = scale.timeToMM(hole.getTimestamp());

		double startPercent = (scale.getFirstTrackAxis() + hole.getTrack() * scale.getIntertrackHeight()
				- scale.getTrackWidth() / 2) / scale.getWidth();
		double endPercent = (scale.getFirstTrackAxis() + hole.getTrack() * scale.getIntertrackHeight()
				+ scale.getTrackWidth() / 2) / scale.getWidth();

		// fix acquisition when the scale is revert
		if (scale.isPreferredViewedInversed()) {
			startPercent = 1.0 - startPercent;
			endPercent = 1.0 - endPercent;
		}

		if (startPercent > endPercent) {
			double tmp = startPercent;
			startPercent = endPercent;
			endPercent = tmp;
		}

		int startTrackPixel = (int) (startPercent * height);
		int endTrackPixel = (int) (endPercent * height);

		int startPixel = (int) (mm / scale.getWidth() * height);
		int endPixel = (int) (scale.timeToMM(hole.getTimestamp() + hole.getTimeLength()) / scale.getWidth() * height);

		int imageTileWidth = zi.getTileWidth();

		int startImageIndex = startPixel / imageTileWidth;
		int endImageIndex = endPixel / imageTileWidth;
		assert startImageIndex <= endImageIndex;

		Instances trainingInstances = instances;
		for (int i = startImageIndex; i <= endImageIndex; i++) {
			logger.debug("loading image :" + i); //$NON-NLS-1$
			BufferedImage bi = zi.loadImage(startImageIndex);

			int offsetStart = startPixel - (i * imageTileWidth);
			int offsetEnd = endPixel - (i * imageTileWidth);

			Rectangle r = new Rectangle(offsetStart, startTrackPixel, offsetEnd - offsetStart,
					endTrackPixel - startTrackPixel);

			ImagePlus ip = new ImagePlus();
			ip.setImage(bi);

			WekaSegmentation ws = new WekaSegmentation(ip);
			updateSegmentationParameters(ws);

			ws.addExample(classNo, new ShapeRoi(r), 1);

			Instances t = ws.createTrainingInstances();
			if (t != null) {
				if (trainingInstances == null) {
					trainingInstances = t;
				} else {
					ws.mergeDataInPlace(trainingInstances, t);
				}
			}
		}

		return trainingInstances;
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
