package org.barrelorgandiscovery.scale;

import java.io.Serializable;

import org.barrelorgandiscovery.tools.HashCodeUtils;


/**
 * Classe defining a pipe stop that can be controlled by a register
 * 
 * @author Freydiere Patrice
 * 
 */
public class PipeStop implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -1143368191587185306L;

	private String name;

	private boolean isRegisteredControlled;

	public PipeStop(String name, boolean isRegisteredControlled) {
		this.name = name;
		this.isRegisteredControlled = isRegisteredControlled;
	}

	public String getName() {
		return name;
	}

	public boolean isRegisteredControlled() {
		return isRegisteredControlled;
	}

	@Override
	public int hashCode() {
		int aSeed = HashCodeUtils.SEED;
		aSeed = HashCodeUtils.hash(aSeed, isRegisteredControlled);
		aSeed = HashCodeUtils.hash(aSeed, name);
		return aSeed;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (!(obj instanceof PipeStop))
			return false;

		PipeStop pobj = (PipeStop) obj;

		return pobj.name == name
				&& pobj.isRegisteredControlled == isRegisteredControlled;

	}

	@Override
	public String toString() {
		return name + " - " + isRegisteredControlled;
	}

}
