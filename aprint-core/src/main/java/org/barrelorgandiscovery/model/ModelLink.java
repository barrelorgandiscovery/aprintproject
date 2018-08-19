package org.barrelorgandiscovery.model;

import java.io.Serializable;

/**
 * Link between parameters of the model
 * 
 * @author use
 * 
 */
public class ModelLink implements Serializable {

	/**
	 * from model parameter
	 */
	private AbstractParameter from;

	/**
	 * To model parameter
	 */
	private AbstractParameter to;

	/**
	 * internal link id
	 */
	private String id = null;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public AbstractParameter getFrom() {
		return from;
	}

	public void setFrom(AbstractParameter from) {
		this.from = from;
	}

	public AbstractParameter getTo() {
		return to;
	}

	public void setTo(AbstractParameter to) {
		this.to = to;
	}

	@Override
	public String toString() {
		return "ModelLink : from (" + getFrom() + ") -> (" + getTo() + ")";
	}


}
