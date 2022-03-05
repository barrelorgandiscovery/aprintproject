package org.barrelorgandiscovery.recognition.gui.bookext;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import ij.ImagePlus;
import ij.Prefs;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import trainableSegmentation.WekaSegmentation;

public class WekaWithPreTrainedModel implements IRecognitionStrategy {

	WekaSegmentation ws = null;

	byte[] model = null;

	public WekaWithPreTrainedModel(byte[] modelRead) {
		assert modelRead != null;
		this.model = modelRead;
	}

	@Override
	public BufferedImage apply(BufferedImage image) {

		ImagePlus ip = new ImagePlus();
		ip.setImage(image);

		Prefs.setThreads(1);

		// model input stream

		ws = new WekaSegmentation(ip);

		assert model != null;
		ws.loadClassifier(new ByteArrayInputStream(model));

		ImagePlus result = ws.applyClassifier(ip);

		ImageProcessor processor = result.getProcessor();
		processor.multiply(250);

		ByteProcessor bp = processor.convertToByteProcessor(true);

		ImagePlus binary = new ImagePlus("", bp); //$NON-NLS-1$

		return binary.getBufferedImage();
	}

}
