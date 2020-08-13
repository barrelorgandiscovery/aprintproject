package org.barrelorgandiscovery.extensionsng.scanner.scan;

import java.awt.image.BufferedImage;

/**
 * listener to get a new scanned image,
 * 
 * @author pfreydiere
 *
 */
public interface IWebCamListener {

	/**
	 * a new image is received
	 * 
	 * @param image     the buffered image
	 * @param timestamp	the time stamp associated to the snapshot
	 */
	public void imageReceived(BufferedImage image, long timestamp);

}
