package org.barrelorgandiscovery.extensionsng.scanner.scan.trigger;

import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.bookimage.PerfoScanFolder;
import org.barrelorgandiscovery.extensionsng.scanner.scan.IWebCamListener;
import org.barrelorgandiscovery.extensionsng.scanner.scan.WebCamPictureTake;

import com.github.sarxos.webcam.Webcam;

public abstract class Trigger {

	private static Logger logger = Logger.getLogger(Trigger.class);

	private Webcam webcam;

	protected WebCamPictureTake webCamPictureTaker;

	private PerfoScanFolder psf;

	private IWebCamListener listener;

	public Trigger(Webcam webcam, IWebCamListener listener, PerfoScanFolder psf) {
		this.listener = listener;
		this.webcam = webcam;
		assert this.webcam.isOpen();
		webCamPictureTaker = new WebCamPictureTake(webcam, new IWebCamListener() {
			@Override
			public void imageReceived(BufferedImage image, long timestamp) {
				// add image
				try {
					logger.debug("take a new image");//$NON-NLS-1$
					psf.addNewImage(image);
				} catch (Exception ex) {
					logger.error("error saving picture :" + ex.getMessage(), ex); //$NON-NLS-1$
				}
				// relaunch
				if (listener != null) {
					listener.imageReceived(image, timestamp);
				}
			}
		});
		this.psf = psf;
	}

	public abstract void start() throws Exception;

	public abstract void stop() throws Exception;

	protected void takePicture() {
		webCamPictureTaker.run();
	}
}
