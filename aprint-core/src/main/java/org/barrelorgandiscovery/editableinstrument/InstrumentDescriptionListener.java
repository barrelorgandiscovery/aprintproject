package org.barrelorgandiscovery.editableinstrument;

import java.awt.Image;

/**
 * interface for MVC for instruments
 * 
 * @author Freydiere Patrice
 * 
 */
public interface InstrumentDescriptionListener {

	void instrumentNameChanged(String newName);

	void instrumentDescriptionChanged(String description);

	void instrumentImageChanged(Image newImage);

}
