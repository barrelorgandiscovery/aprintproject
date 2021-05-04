package org.barrelorgandiscovery.extensionsng.scanner.scan;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;

import com.github.sarxos.webcam.Webcam;

/**
 * runnable for getting the images
 * @author pfreydiere
 *
 */
public class WebCamPictureTake implements Runnable {

	private static Logger logger = Logger.getLogger(WebCamPictureTake.class);

	private Webcam webCam;
	private IWebCamListener listener;

	public WebCamPictureTake(Webcam webCam, IWebCamListener listener) {
		this.webCam = webCam;
		this.listener = listener;
	}

	@Override
	public synchronized void run() {
		logger.debug("take picture");//$NON-NLS-1$
		final BufferedImage picture = webCam.getDevice().getImage();
		try {

			if (listener != null) {
				listener.imageReceived(picture, System.nanoTime() / 1_000_000);
			}

		} catch (Exception ex) {
			logger.error("error while showing the image " + ex.getMessage(), ex);//$NON-NLS-1$
		}
	}
}
