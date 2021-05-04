package org.barrelorgandiscovery.gui.ainstrument;

import org.barrelorgandiscovery.instrument.Instrument;

/**
 * Listener Interface for listening selected instrument
 * 
 * @author use
 */
public interface InstrumentSelectedListener {

	/**
	 * called when an instrument is selected
	 * @param ins
	 */
	void instrumentSelected(Instrument ins);
	
	/**
	 * called when an instrument is double clicked
	 * @param ins
	 */
	void instrumentDoubleClicked(Instrument ins);

}
