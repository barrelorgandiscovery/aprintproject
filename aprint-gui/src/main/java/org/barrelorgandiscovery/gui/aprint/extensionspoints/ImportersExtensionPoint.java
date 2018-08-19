package org.barrelorgandiscovery.gui.aprint.extensionspoints;

import java.util.ArrayList;

import org.barrelorgandiscovery.extensions.IExtensionPoint;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractMidiImporter;


/**
 * This extension point permit the extension to implement a custom midi Reading
 * transformation
 * 
 * @author Freydiere Patrice
 * 
 */
public interface ImportersExtensionPoint extends IExtensionPoint{

	/**
	 * Get the MidiImporter associated to an instrument scale
	 * 
	 * @param destinationscale
	 *            the destination scale
	 * @return an array list of midi importers
	 */
	public ArrayList<AbstractMidiImporter> getExtensionImporterInstance(
			Scale destinationscale);

}
