package org.barrelorgandiscovery.gui.repository2;

import java.awt.Component;
import java.awt.Frame;

import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprintng.IAPrintWait;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.repository.Repository2;

/**
 * Interface for all actions
 * 
 * @author use
 * 
 */
public interface CurrentRepositoryInformations {

	/**
	 * Frame Owner
	 * 
	 * @return
	 */
	Frame getOwner();

	/**
	 * wait interface ...
	 * 
	 * @return
	 */
	IAPrintWait getWaitInterface();

	/**
	 * properties
	 * 
	 * @return
	 */
	APrintProperties getAPrintProperties();

	/**
	 * Instrument associated
	 * 
	 * @return
	 */
	Instrument getCurrentInstrument();

	/**
	 * Repository associated to the current instrument
	 * 
	 * @return
	 */
	Repository2 getCurrentInstrumentRepository2();

	/**
	 * Editable Repository associated to current instrument
	 * 
	 * @return
	 */
	EditableInstrumentManagerRepository getCurrentInstrumentEditableInstrumentManagerRepository();

	/**
	 * Repository Associated
	 * 
	 * @return
	 */
	Repository2 getCurrentRepository2();

	EditableInstrumentManagerRepository getCurrentEditableInstrumentManagerRepository();

}
