package org.barrelorgandiscovery.gui.aprint.extensionspoints;

import org.barrelorgandiscovery.extensions.IExtensionPoint;
import org.barrelorgandiscovery.instrument.Instrument;

/**
 * Inform for the current instrument
 * 
 * @author Freydiere Patrice
 * 
 */
public interface InformCurrentInstrumentExtensionPoint extends IExtensionPoint {

	void informCurrentInstrument(Instrument instrument);

}
