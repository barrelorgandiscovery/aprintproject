package org.barrelorgandiscovery.model;

import java.io.Serializable;

public abstract class AbstractParameter implements Serializable {

	/**
	 * Parameter name
	 */
	private String name;
	/**
	 * Parameter label
	 */
	private String label;
	/**
	 * Parameter Class
	 */
	private ModelType type;
	/**
	 * Associated step
	 */
	private ModelStep step;

	/**
	 * id associated to parameter
	 */
	private String id = null;

	/**
	 * Return the parameter name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Define the parameter name
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	public AbstractParameter() {
		super();
	}

	/**
	 * Get the parameter id
	 * 
	 * @return
	 */

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Get parameter label
	 * 
	 * @return
	 */

	public String getLabel() {
		if (label == null)
			return name; // default
		return label;
	}

	/**
	 * the the parameter label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Get the parameter type
	 * 
	 * @return
	 */

	public ModelType getType() {
		return type;
	}

	public void setType(ModelType type) {
		assert type != null;
		this.type = type;
	}

	/**
	 * Get the associated parameter step (a parameter is always associated to a
	 * model step)
	 * 
	 * @return
	 */
	public ModelStep getStep() {
		return step;
	}

	public void setStep(ModelStep step) {
		this.step = step;
	}

	/**
	 * Visitor
	 * 
	 * @param visitor
	 */
	public abstract void visit(ModelParameterVisitor visitor);

}
