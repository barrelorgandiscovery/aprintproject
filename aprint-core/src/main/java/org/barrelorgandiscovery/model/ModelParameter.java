package org.barrelorgandiscovery.model;

/**
 * Describe a Model Parameter
 * 
 * @author pfreydiere
 * 
 */

public class ModelParameter extends AbstractParameter {

	/**
	 * Is the parameter optional (for input), outputs are optional
	 */
	private boolean optional = false;

	/**
	 * Is it an input parameter
	 */
	private boolean in;

	public ModelParameter() {

	}

	/**
	 * is the parameter optional ?
	 * 
	 * @return
	 */

	public boolean isOptional() {
		return optional;
	}

	/**
	 * Define if the parameter is optional
	 * 
	 * @param optional
	 */
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	/**
	 * Is an input parameter ?, else this is an output parameter
	 * 
	 * @return
	 */

	public boolean isIn() {
		return in;
	}

	public boolean isOut() {
		return !in;
	}

	public void setIn(boolean in) {
		this.in = in;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("\n\t");
		sb.append("name : " + getName());
		sb.append("\n\t");
		sb.append("label :" + getLabel());
		sb.append("\n\t");
		sb.append("type : " + getType());
		sb.append("\n\t");
		sb.append("isIn: " + isIn());
		sb.append("\n\t");
		sb.append("Optional : " + isOptional());
		sb.append("\n\t");
		return sb.toString();
	}

	@Override
	public void visit(ModelParameterVisitor visitor) {
		visitor.visit(this);
	}
}
