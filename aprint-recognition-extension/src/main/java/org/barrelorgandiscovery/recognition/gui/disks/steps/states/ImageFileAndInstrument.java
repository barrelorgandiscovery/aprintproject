package org.barrelorgandiscovery.recognition.gui.disks.steps.states;

import java.io.File;
import java.io.Serializable;

public class ImageFileAndInstrument implements Serializable, INumericImage, IInstrumentName {

	public String instrumentName;

	public File diskFile;

	public File getImageFile() {
		return diskFile;
	}

	@Override
	public String getInstrumentName() {
		return instrumentName;
	}

}
