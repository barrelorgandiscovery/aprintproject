package org.barrelorgandiscovery.gui.aprint.instrumentchoice;

import org.barrelorgandiscovery.instrument.Instrument;

public interface IInstrumentChoiceListener {

	/**
	 * Message pour indiquant que l'instrument a changé
	 * 
	 * @param newInstrument
	 */
	public void instrumentChanged(Instrument newInstrument);

}
