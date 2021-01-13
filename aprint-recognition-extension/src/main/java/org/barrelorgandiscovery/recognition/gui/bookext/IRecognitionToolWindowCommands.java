package org.barrelorgandiscovery.recognition.gui.bookext;

import org.barrelorgandiscovery.extensions.IExtensionPoint;
import org.barrelorgandiscovery.images.books.tools.ITiledImage;

public interface IRecognitionToolWindowCommands extends IExtensionPoint {

	public void setTiledImage(ITiledImage tiledImage);
	
	
}
