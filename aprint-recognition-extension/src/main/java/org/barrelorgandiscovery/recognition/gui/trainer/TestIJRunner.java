package org.barrelorgandiscovery.recognition.gui.trainer;

import trainableSegmentation.WekaSegmentation;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Macro_Runner;

public class TestIJRunner {
	
	public static void main(String[] args) throws Exception {
		
		ImagePlus i = IJ.openImage("C:\\temp\\recognition\\modifiedImage_downsampling.png");
		WekaSegmentation ws = new WekaSegmentation(i);
		ws.loadClassifier("C:\\temp\\recognition\\classifier.model");
		ImagePlus r = ws.applyClassifier(i);
		r.getBufferedImage();
	
		
	}

}
