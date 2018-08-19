package org.barrelorgandiscovery.scale;

import java.io.Serializable;

/**
 * Contraintes associ�es � l'instrument .. � la gamme ...
 * 
 * @author Freydiere Patrice
 * 
 */
public abstract class AbstractScaleConstraint implements Serializable {

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

}
