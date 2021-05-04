package org.barrelorgandiscovery.scale;

import java.io.Serializable;

/**
 * Contraintes associées à l'instrument .. à la gamme ...
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
