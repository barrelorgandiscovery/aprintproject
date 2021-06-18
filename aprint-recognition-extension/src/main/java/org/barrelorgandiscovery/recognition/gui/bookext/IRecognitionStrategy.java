package org.barrelorgandiscovery.recognition.gui.bookext;

import java.awt.image.BufferedImage;

public interface IRecognitionStrategy {
	
	BufferedImage apply(BufferedImage image);

}
