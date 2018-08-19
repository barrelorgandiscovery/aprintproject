package org.barrelorgandiscovery.scale;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.HashCodeUtils;
import org.barrelorgandiscovery.tools.StringTools;

/**
 * class for selecting a drum
 * 
 * @author Freydiere Patrice
 * 
 */
public class ReferencedPercussion {

	private int midicode;
	private String namecode;

	public ReferencedPercussion(int midicode, String namecode) {
		this.midicode = midicode;
		this.namecode = namecode;
	}

	public int getMidicode() {
		return midicode;
	}

	public String getNamecode() {
		return namecode;
	}

	/**
	 * Get a localized name of the drum
	 * 
	 * @param p
	 * @return
	 */
	public static String getLocalizedDrumLabel(ReferencedPercussion p) {
		return Messages.getString("Drum." + p.getNamecode());
	}

	@Override
	public String toString() {
		return "ReferencedPercussion :" + midicode + " -> " + namecode;
	}
	
	@Override
	public int hashCode() {
	
		int seed = 31;
		seed = HashCodeUtils.hash(seed, midicode);
		seed = HashCodeUtils.hash(seed, namecode);
		
		return seed;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null)
			return false;
		
		if (obj.getClass() != getClass())
			return false;
		
		ReferencedPercussion p = (ReferencedPercussion)obj;
		
		if (p.midicode != midicode)
			return false;
		
		if (StringTools.compare(p.namecode, namecode) != 0)
			return false;

		return true;
	}
	
	

}
