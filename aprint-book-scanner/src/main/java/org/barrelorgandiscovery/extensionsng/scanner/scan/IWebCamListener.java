package org.barrelorgandiscovery.extensionsng.scanner.scan;

import java.awt.image.BufferedImage;

public interface IWebCamListener {

  public void imageReceived(BufferedImage image, long timestamp);

}
