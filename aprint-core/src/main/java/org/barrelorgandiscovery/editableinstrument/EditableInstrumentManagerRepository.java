package org.barrelorgandiscovery.editableinstrument;

/**
 * Interface for getting the editableInstrumentManager associated to an instrument.
 * one can get the editable name, and editableInstrumentManager, and edit/load them
 * 
 * @author Freydiere Patrice
 * 
 */
public interface EditableInstrumentManagerRepository {

	/**
	 * Get the editableInstrumentManager
	 * 
	 * @return
	 */
	public abstract EditableInstrumentManager getEditableInstrumentManager();

	/**
	 * resolve the editable instrument name given tje instrument name
	 */
	public abstract String findAssociatedEditableInstrumentName(
			String instrumentname);

}