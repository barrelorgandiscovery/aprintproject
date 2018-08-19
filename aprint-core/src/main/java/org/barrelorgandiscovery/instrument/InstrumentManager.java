package org.barrelorgandiscovery.instrument;

import org.barrelorgandiscovery.scale.Scale;

/**
 * Instrument manager
 * 
 * @author Freydiere Patrice
 * 
 */
public interface InstrumentManager {

	/**
	 * Get an instrument by name
	 * 
	 * @param name
	 * @return
	 */
	Instrument getInstrument(String name);

	/**
	 * Get the instruments associated to a scale
	 * 
	 * @param scale
	 * @return
	 */
	Instrument[] getInstrument(Scale scale);

	/**
	 * Get All instruments
	 * 
	 * @return
	 */
	Instrument[] listInstruments();

	/**
	 * Save an instrument
	 * 
	 * @param instrument
	 * @throws Exception
	 */
	void saveInstrument(Instrument instrument) throws Exception;

	/**
	 * delete an instrument
	 * 
	 * @param instrument
	 * @throws Exception
	 */
	void deleteInstrument(Instrument instrument) throws Exception;

}
