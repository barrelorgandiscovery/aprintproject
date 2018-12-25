package org.barrelorgandiscovery.extensionsng.scanner.scan.trigger;

import org.barrelorgandiscovery.extensionsng.scanner.PerfoScanFolder;
import org.barrelorgandiscovery.extensionsng.scanner.scan.IWebCamListener;

import com.github.sarxos.webcam.Webcam;

public interface ITriggerFactory {

	/**
	 * factory for a trigger
	 *
	 * @param webcam the opened wecam
	 * @param psf
	 * @return
	 * @throws Exception
	 */
	public Trigger create(Webcam webcam, IWebCamListener listener, PerfoScanFolder psf) throws Exception;
}
