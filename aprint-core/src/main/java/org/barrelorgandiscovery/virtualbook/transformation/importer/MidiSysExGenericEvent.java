package org.barrelorgandiscovery.virtualbook.transformation.importer;

public class MidiSysExGenericEvent extends MidiGenericEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5324686180319141581L;

	public MidiSysExGenericEvent(long timestamp, byte[] datas) {
		super(timestamp, datas);
		
	}

}
