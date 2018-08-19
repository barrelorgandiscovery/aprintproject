package org.barrelorgandiscovery.editableinstrument;

import java.util.Map;

import org.barrelorgandiscovery.instrument.Instrument;

public interface EditableInstrumentManager {

	

	/**
	 * List all the editable instruments in the manager ...
	 * 
	 * @return
	 * @throws Exception
	 */
	String[] listEditableInstruments() throws Exception;

	/**
	 * delete the editable instrument
	 * 
	 * @param name
	 *            the name of the editable instrument ...
	 * @throws Exception
	 */
	void deleteEditableInstrument(String name) throws Exception;

	/**
	 * Save the editable instrument
	 * 
	 * @param instrument
	 *            the instrument to save ...
	 * @throws Exception
	 */
	void saveEditableInstrument(IEditableInstrument instrument)
			throws Exception;

	/**
	 * Load the editable instrument
	 * 
	 * @param name
	 *            the name of the editable instrument ...
	 * @return
	 * @throws Exception
	 */
	IEditableInstrument loadEditableInstrument(String name) throws Exception;

	/**
	 * Add listener
	 * 
	 * @param listener
	 */
	void addListener(EditableInstrumentManagerListener listener);

	/**
	 * Remove listener ...
	 * 
	 * @param listener
	 */
	void removeListener(EditableInstrumentManagerListener listener);

	/**
	 * Get the editable instrument digest
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	String getEditableInstrumentDigest(String name) throws Exception;

	/**
	 * Get all the editable instrument digest
	 * 
	 * @return
	 * @throws Exception
	 */
	Map<String, String> getAllEditableInstrumentDigests() throws Exception;

}
