package org.barrelorgandiscovery.extensionsng.scanner.scan;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
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
import org.barrelorgandiscovery.extensionsng.scanner.PerfoScanFolder;
import org.barrelorgandiscovery.extensionsng.scanner.scan.trigger.ITriggerFactory;
import org.barrelorgandiscovery.extensionsng.scanner.scan.trigger.TimeTrigger;
import org.barrelorgandiscovery.extensionsng.scanner.scan.trigger.Trigger;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.ImageTools;

import com.github.sarxos.webcam.Webcam;
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
			asyncPreviewImage(i);
		});
		initComponents();

	}

	private void asyncPreviewImage(BufferedImage previewImage) {
		SwingUtilities.invokeLater(() -> {
			if (previewImage == null) {
				logger.debug("webcam is not opened");
				return;
			}
			final BufferedImage boxedImage = ImageTools.crop(300, 300, previewImage);
			imageLabel.setIcon(new ImageIcon(boxedImage));
			imageLabel.repaint();
		});
	}

	protected void initComponents() throws Exception {

		InputStream inputStream = getClass().getResourceAsStream("scanphase.jfrm");
		FormPanel fp = new FormPanel(inputStream);

		JLabel lblFolder = fp.getLabel("lblfolder");
		lblFolder.setText(perfoScanFolder.getFolder().getAbsolutePath());

		JLabel lblfolderLabel = fp.getLabel("lblfolderlabel");
		assert lblfolderLabel != null;
		lblfolderLabel.setText("Folder in which the images will be saved");

		btnStart = fp.getButton("btnstart");
		assert btnStart != null;
		btnStart.setText("Start Record");
		btnStart.setToolTipText("Start record the images in the folder");
		btnStart.setIcon(new ImageIcon(ImageTools.loadImage(JScanPanel.class, "krec_record.png")));

		btnStart.addActionListener((e) -> {
			try {
				start();
				updateUIState();
			} catch (Exception ex) {
				logger.error("error starting recording :" + ex.getMessage(), ex);
			}
		});

		btneraseimagefiles = fp.getButton("eraseimagefiles");
		btneraseimagefiles.setText("Erase all imagefiles");
		btneraseimagefiles.setIcon(new ImageIcon(ImageTools.loadImage(JScanPanel.class, "stop.png")));
		btneraseimagefiles.setToolTipText("Erase all images in the folder to restart the record");

		btneraseimagefiles.addActionListener((e) -> {

			// erase all folder images
			perfoScanFolder.deleteAllImages();

			// clean image stack
			imageStack.clearImages();

		});

		btnStop = fp.getButton("btnstop");
		assert btnStop != null;
		btnStop.setText("Stop");
		btnStop.setToolTipText("End record images");

		btnStop.setIcon(new ImageIcon(ImageTools.loadImage(JScanPanel.class, "player_stop.png")));

		btnStop.addActionListener((e) -> {
			try {
				stop();
				updateUIState();
			} catch (Exception ex) {
				logger.error("error stoping recording :" + ex.getMessage(), ex);
			}
		});
		btnStop.setEnabled(false);

		imageLabel = new JLabel();
		fp.getFormAccessor("gridwebcam").replaceBean("webcampreview", imageLabel);
		imageLabel.setAlignmentX(0.5f);
		imageLabel.setAlignmentY(0.5f);
		imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		imageLabel.setVerticalAlignment(SwingConstants.CENTER);

		imageStack = new JImagePreviewStack();
		fp.getFormAccessor("gridpreview").replaceBean("imagespreview", imageStack);

		setLayout(new BorderLayout());
		add(fp, BorderLayout.CENTER);

		// wire events

		scheduledExecutorWebCam = Executors.newSingleThreadScheduledExecutor();
		triggerLivePreview();

	}

	ScheduledFuture<?> scheduleWithFixedDelay = null;

	private void triggerLivePreview() {
		if (scheduleWithFixedDelay == null) {
			logger.debug("start live trigger");
			scheduleWithFixedDelay = scheduledExecutorWebCam.scheduleWithFixedDelay(previewWebCamPictureTake, 0, 100,
					TimeUnit.MILLISECONDS);
		}
	}

	private void stopLivePreview() {
		if (scheduleWithFixedDelay != null) {
			logger.debug("cancel the livepreview");
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

		stopLivePreview();
		logger.debug("start the record");
		trigger = triggerFactory.create(webcam, new IWebCamListener() {

			@Override
			public void imageReceived(BufferedImage image, long timestamp) {
				imageStack.addImage(image);
				asyncPreviewImage(image);

			}
		}, perfoScanFolder);

		trigger.start();

	}

	public boolean isStarted() {
		return trigger == null;
	}

	public void stop() {
		if (trigger != null) {
			try {
				trigger.stop();
			} catch (Exception ex) {
				logger.error("error closing web cam trigger :" + ex.getMessage(), ex);
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
				logger.error("error closing web cam trigger :" + ex.getMessage(), ex);
			}
			trigger = null;
		}

		stopLivePreview();
	}

	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());

		Webcam w = Webcam.getDefault();
		w.open();

		File t = File.createTempFile("testtrigger", ".folder");
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
