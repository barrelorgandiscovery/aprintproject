package org.barrelorgandiscovery.gui.aprint.instrumentchoice;

import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.repository.Repository2;

public interface IInstrumentChoice {

	public abstract void setRepository(Repository2 newrep);

	/**
	 * Recharge la liste des instruments
	 */
	public abstract void reloadInstruments();

	/**
	 * Get the instrument Filter
	 * 
	 * @return
	 */
	public abstract String getInstrumentFilter();

	/**
	 * Set instrument Filter
	 */
	public abstract void setInstrumentFilter(String filter);

	/**
	 * Retourne l'instrument sélectionné
	 * 
	 * @return
	 */
	public abstract Instrument getCurrentInstrument();

	/**
	 * Select the given instrument if found
	 * 
	 * @param instrumentName
	 * @return true if found, false otherwise
	 */
	public abstract boolean selectInstrument(String instrumentName);

}