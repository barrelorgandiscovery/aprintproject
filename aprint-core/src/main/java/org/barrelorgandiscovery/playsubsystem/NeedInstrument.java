package org.barrelorgandiscovery.playsubsystem;

/**
 * Interface indicate that an instrument is needed for the play subsystem
 * 
 * @author Freydiere Patrice
 * 
 */
public interface NeedInstrument {

	public abstract void setCurrentInstrument(
			org.barrelorgandiscovery.instrument.Instrument ins);

}