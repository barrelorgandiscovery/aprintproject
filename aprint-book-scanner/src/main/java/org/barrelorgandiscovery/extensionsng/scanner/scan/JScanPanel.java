package org.barrelorgandiscovery.extensionsng.scanner.scan;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.bookimage.PerfoScanFolder;
import org.barrelorgandiscovery.extensionsng.scanner.Messages;
import org.barrelorgandiscovery.extensionsng.scanner.scan.trigger.ITriggerFactory;
import org.barrelorgandiscovery.extensionsng.scanner.scan.trigger.TimeTrigger;
import org.barrelorgandiscovery.extensionsng.scanner.scan.trigger.Trigger;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.ImageTools;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDevice;
import com.jeta.forms.components.panel.FormPanel;

public class JScanPanel extends JPanel implements Disposable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7948210921186182119L;

	private static Logger logger = Logger.getLogger(JScanPanel.class);

	File outputFolder;

	JImagePreviewStack imageStack;

	JLabel imageLabel;

	WebCamPictureTake previewWebCamPictureTake;

	private ScheduledExecutorService scheduledExecutorWebCam;

	private PerfoScanFolder perfoScanFolder;

	private ITriggerFactory triggerFactory;

	private Webcam webcam;

	public JScanPanel(Webcam webCam, ITriggerFactory snapshotTriggerFactory, PerfoScanFolder pf) throws Exception {
		assert webCam != null;
		this.webcam = webCam;
		assert pf != null;
		this.perfoScanFolder = pf;

		assert snapshotTriggerFactory != null;
		this.triggerFactory = snapshotTriggerFactory;

		this.previewWebCamPictureTake = new WebCamPictureTake(webCam, (i, t) -> {
			try {
				asyncPreviewImage(i);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		});
		initComponents();

	}

	private void asyncPreviewImage(BufferedImage previewImage) throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			if (previewImage == null) {
				logger.debug("webcam is not opened");//$NON-NLS-1$
				return;
			}
			final BufferedImage boxedImage = ImageTools.crop(300, 300, previewImage);
			imageLabel.setIcon(new ImageIcon(boxedImage));
			imageLabel.repaint();
		});
	}

	protected void initComponents() throws Exception {

		InputStream inputStream = getClass().getResourceAsStream("scanphase.jfrm");//$NON-NLS-1$
		FormPanel fp = new FormPanel(inputStream);

		JLabel lblFolder = fp.getLabel("lblfolder");//$NON-NLS-1$
		lblFolder.setText(perfoScanFolder.getFolder().getAbsolutePath());

		JLabel lblfolderLabel = fp.getLabel("lblfolderlabel");//$NON-NLS-1$
		assert lblfolderLabel != null;
		lblfolderLabel.setText(Messages.getString("JScanPanel.0")); //$NON-NLS-1$

		btnStart = fp.getButton("btnstart");//$NON-NLS-1$
		assert btnStart != null;
		btnStart.setText(Messages.getString("JScanPanel.1")); //$NON-NLS-1$
		btnStart.setToolTipText(Messages.getString("JScanPanel.2")); //$NON-NLS-1$
		btnStart.setIcon(new ImageIcon(ImageTools.loadImage(JScanPanel.class, "krec_record.png")));//$NON-NLS-1$

		btnStart.addActionListener((e) -> {
			try {
				start();
				updateUIState();
			} catch (Exception ex) {
				logger.error("error starting recording :" + ex.getMessage(), ex);//$NON-NLS-1$
			}
		});

		btneraseimagefiles = fp.getButton("eraseimagefiles");//$NON-NLS-1$
		btneraseimagefiles.setText(Messages.getString("JScanPanel.3")); //$NON-NLS-1$
		btneraseimagefiles.setIcon(new ImageIcon(ImageTools.loadImage(JScanPanel.class, "stop.png")));//$NON-NLS-1$
		btneraseimagefiles.setToolTipText(Messages.getString("JScanPanel.4")); //$NON-NLS-1$

		btneraseimagefiles.addActionListener((e) -> {

			// erase all folder images
			perfoScanFolder.deleteAllImages();

			// clean image stack
			imageStack.clearImages();

		});

		btnStop = fp.getButton("btnstop");//$NON-NLS-1$
		assert btnStop != null;
		btnStop.setText(Messages.getString("JScanPanel.5")); //$NON-NLS-1$
		btnStop.setToolTipText(Messages.getString("JScanPanel.6")); //$NON-NLS-1$

		btnStop.setIcon(new ImageIcon(ImageTools.loadImage(JScanPanel.class, "player_stop.png")));//$NON-NLS-1$

		btnStop.addActionListener((e) -> {
			try {
				stop();
				updateUIState();
			} catch (Exception ex) {
				logger.error("error stoping recording :" + ex.getMessage(), ex);//$NON-NLS-1$
			}
		});
		btnStop.setEnabled(false);

		imageLabel = new JLabel();
		fp.getFormAccessor("gridwebcam")//$NON-NLS-1$
				.replaceBean("webcampreview", imageLabel);//$NON-NLS-1$
		imageLabel.setAlignmentX(0.5f);
		imageLabel.setAlignmentY(0.5f);
		imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		imageLabel.setVerticalAlignment(SwingConstants.CENTER);

		imageStack = new JImagePreviewStack();
		fp.getFormAccessor("gridpreview")//$NON-NLS-1$
				.replaceBean("imagespreview", imageStack);//$NON-NLS-1$

		setLayout(new BorderLayout());
		add(fp, BorderLayout.CENTER);

		// wire events

		scheduledExecutorWebCam = Executors.newSingleThreadScheduledExecutor();
		triggerLivePreview();

	}

	ScheduledFuture<?> scheduleWithFixedDelay = null;

	private void triggerLivePreview() {
		if (scheduleWithFixedDelay == null) {
			logger.debug("start live trigger"); //$NON-NLS-1$
			scheduleWithFixedDelay = scheduledExecutorWebCam.scheduleWithFixedDelay(previewWebCamPictureTake, 0, 100,
					TimeUnit.MILLISECONDS);
		}
	}

	private void stopLivePreview() {
		if (scheduleWithFixedDelay != null) {
			logger.debug("cancel the livepreview"); //$NON-NLS-1$
			scheduleWithFixedDelay.cancel(true);
			scheduleWithFixedDelay = null;
		}
	}

	private void updateUIState() {
		btnStop.setEnabled(trigger != null);
		btnStart.setEnabled(trigger == null);
		btneraseimagefiles.setEnabled(btnStart.isEnabled());
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	// manage the acquisition life cycle

	private Trigger trigger = null;

	private AbstractButton btneraseimagefiles;

	private AbstractButton btnStart;

	private AbstractButton btnStop;

	public void start() throws Exception {

		try {
			stopLivePreview();
			Thread.sleep(1000);
			logger.debug("start the record");//$NON-NLS-1$

			if (trigger != null) {
				try {
					trigger.stop();
				} catch (Throwable t) {
				}
				trigger = null;
			}

			trigger = triggerFactory.create(webcam, new IWebCamListener() {

				@Override
				public void imageReceived(BufferedImage image, long timestamp) {

					try {
						logger.debug("add image"); //$NON-NLS-1$
						SwingUtilities.invokeAndWait(() -> {
							imageStack.addImage(image);

						});
						asyncPreviewImage(image);
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
				}
			}, perfoScanFolder);

			trigger.start();

		} catch (Throwable t) {
			logger.error("error in starting trigger :" + t.getMessage(), t);
		}
	}

	public boolean isStarted() {
		return trigger == null;
	}

	public void stop() {
		if (trigger != null) {
			try {
				trigger.stop();
			} catch (Exception ex) {
				logger.error("error closing web cam trigger :" + ex.getMessage(), ex);//$NON-NLS-1$
			}
			trigger = null;
		}
		triggerLivePreview();
	}

	@Override
	public void dispose() {
		if (trigger != null) {
			try {
				trigger.stop();
			} catch (Exception ex) {
				logger.error("error closing web cam trigger :" + ex.getMessage(), ex);//$NON-NLS-1$
			}
			trigger = null;
		}

		stopLivePreview();
	}

	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());

		List<Webcam> list = Webcam.getWebcams(10000);
		System.out.println(list);

		Webcam w = list.get(2);
		WebcamDevice dev = w.getDevice();
		dev.setResolution(new Dimension(3672, 2856));
		System.out.println(Arrays.asList(dev.getResolutions()));
		// dev.open();

		Dimension[] sizes = w.getCustomViewSizes();
		System.out.println(Arrays.asList(sizes));

		w.open();

		File t = File.createTempFile("testtrigger", //$NON-NLS-1$
				".folder");//$NON-NLS-1$
		t.delete();
		t.mkdirs();

		PerfoScanFolder pf = new PerfoScanFolder(t);

		ITriggerFactory tf = new ITriggerFactory() {
			@Override
			public Trigger create(Webcam webcam, IWebCamListener listener, PerfoScanFolder psf) throws Exception {
				return new TimeTrigger(webcam, listener, psf, 2);
			}
		};

		JScanPanel scanPanel = new JScanPanel(w, tf, pf);

		f.getContentPane().add(scanPanel, BorderLayout.CENTER);
		f.setSize(800, 600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);

	}

}
