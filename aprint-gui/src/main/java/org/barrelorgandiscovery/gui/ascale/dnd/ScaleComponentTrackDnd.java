package org.barrelorgandiscovery.gui.ascale.dnd;

import java.awt.datatransfer.DataFlavor;
import java.io.Serializable;

import org.barrelorgandiscovery.scale.AbstractTrackDef;

/**
 * Class referencing a track drag, this object is for dragging track reference
 * 
 * @author use
 * 
 */
public class ScaleComponentTrackDnd implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3487938253105214755L;

	public static DataFlavor trackDefDataFlavor = new DataFlavor(
			ScaleComponentTrackDnd.class, "TrackDef");

	/**
	 * trackDef
	 */
	private AbstractTrackDef td;

	/**
	 * track index
	 */
	private int pos;

	/**
	 * Constructor,
	 * 
	 * @param trackDef
	 *            the track def
	 * @param trackNumber
	 *            the track number
	 */
	public ScaleComponentTrackDnd(AbstractTrackDef trackDef, int trackNumber) {
		this.td = trackDef;
		this.pos = trackNumber;
	}

	/**
	 * Get the trackdef
	 * 
	 * @return
	 */
	public AbstractTrackDef getTrackDef() {
		return this.td;
	}

	/**
	 * Get the trackdef index in the original component
	 * 
	 * @return
	 */
	public int getTrackNumber() {
		return this.pos;
	}

}
