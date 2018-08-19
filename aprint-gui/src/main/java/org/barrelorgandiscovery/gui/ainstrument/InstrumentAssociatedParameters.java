package org.barrelorgandiscovery.gui.ainstrument;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.prefs.PrefixedNamePrefsStorage;

/**
 * This class handle parameters associated to an instrument and stored in the
 * users prefs
 * 
 * @author Freydiere Patrice
 * 
 */
public class InstrumentAssociatedParameters {

	private static Logger logger = Logger
			.getLogger(InstrumentAssociatedParameters.class);

	private IPrefsStorage prefs = null;

	public InstrumentAssociatedParameters(IPrefsStorage prefstorage) {
		if (logger.isDebugEnabled())
			logger.debug("InstrumentAssociatedParameters " + prefstorage);
		
		assert prefstorage != null;
		this.prefs = prefstorage;
	}

	public IPrefsStorage getInstrumentPrefsStorage(Instrument instrument) {
		if (logger.isDebugEnabled())
			logger.debug("instrument : " + instrument + " name : "
					+ instrument.getName());
		return new PrefixedNamePrefsStorage(instrument.getName(), prefs);
	}

}
