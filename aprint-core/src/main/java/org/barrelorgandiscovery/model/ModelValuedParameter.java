package org.barrelorgandiscovery.model;

import java.io.Serializable;

/**
 * Model Value Parameter, evaluate in config stage
 * 
 * @author pfreydiere
 */
public class ModelValuedParameter extends AbstractParameter {

	private Serializable value;

	public ModelValuedParameter() {
		super();
	}

	public void setValue(Serializable value) {
		this.value = value;
	}

	public Serializable getValue() {
		return this.value;
	}


	@Override
	public void visit(ModelParameterVisitor visitor) {
		visitor.visit(this);
	}

}
