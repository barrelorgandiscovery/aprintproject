package org.barrelorgandiscovery.gui.aprintng;

import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Interface describing the external objects of a book frame
 * 
 * @author pfreydiere
 * 
 * @since 2011.6
 * 
 */
public interface APrintNGVirtualBookFrame {

	/**
	 * Return the reference of the virtualbook associated to the frame
	 * 
	 * @return
	 */
	VirtualBook getVirtualBook();

	/**
	 * Return the pianoroll component, this gives access to the book component
	 * 
	 * @return
	 */
	JVirtualBookScrollableComponent getPianoRoll();

	/**
	 * Return the current instrument
	 * 
	 * @return
	 */
	Instrument getCurrentInstrument();

	/**
	 * start play the virtual book
	 */
	void play() throws Exception;

	/**
	 * Stop play the virtual book
	 */
	void stop() throws Exception;
	
	/**
	 * Get the frame wait interface, permitting to freeze the screen during large operations
	 * @return
	 */
	IAPrintWait getWaitInterface();
	
	/**
	 * Get the owner for dialog
	 * @return
	 */
	Object getOwnerForDialog();
	
	
	
}
