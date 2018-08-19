package org.barrelorgandiscovery.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.barrelorgandiscovery.model.type.JavaType;

/**
 * This class handle a fictive step for assigning parameter model
 * 
 * @author pfreydiere
 */
public class TerminalParameterModelStep extends ModelStep implements SinkSource {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4415987886275860126L;

	/**
	 * Handled parameter value
	 */
	protected ModelParameter parameter = null;

	/**
	 * Nom du paramètre
	 */
	private String name;

	/**
	 * Type du paramètre
	 */
	private ModelType type;

	/**
	 * Type de paramètre entrée (true), sortie (false)
	 */
	private boolean inputParameter;

	/**
	 * Default constructor
	 */
	public TerminalParameterModelStep() {

		ModelParameter mp = new ModelParameter();

		name = "input";
		label = "Input";
		type = new JavaType(String.class);
		inputParameter = true;
		value = "";

		parameter = mp;
		refreshParameterFromConfiguration();

	}

	/**
	 * Constructor
	 * 
	 * @param entryParameter
	 *            is the terminal element an input or output ?
	 * @param type
	 * @param name
	 * @param label
	 */
	public TerminalParameterModelStep(boolean entryParameter, ModelType type, String name, String label,
			Serializable value) {
		super();
		this.label = label;
		this.name = name;
		this.type = type;
		this.value = value;
		this.inputParameter = entryParameter;

		ModelParameter mp = new ModelParameter();

		this.parameter = mp;
		refreshParameterFromConfiguration();
	}

	public ModelType getModelType() {
		return parameter.getType();
	}

	@Override
	public ModelParameter[] getAllParametersByRef() {
		// return the parameter
		return new ModelParameter[] { this.parameter };
	}

	@Override
	public String getName() {
		return parameter.getName();
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(ModelType type) {
		this.type = type;
	}

	public void setInput(boolean entry) {
		this.inputParameter = entry;
	}

	/**
	 * Display name or label
	 */
	public String getLabel() {
		return parameter.getLabel();
	}

	/**
	 * Boolean, mean source
	 * 
	 * @return
	 */
	public boolean isInput() {
		return inputParameter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.model.ModelStep#execute(java.util.Map)
	 */
	@Override
	public Map<AbstractParameter, Object> execute(Map<AbstractParameter, Object> values) throws Exception {
		Map<AbstractParameter, Object> ret = new HashMap<AbstractParameter, Object>();

		if (inputParameter) {
			ret.put(parameter, value);
		} else {
			assert values.size() == 1;
			Object valueretained = values.values().iterator().next();
			ret.put(parameter, valueretained);
			this.value = (Serializable) valueretained;
		}

		return ret;
	}

	@Override
	public void applyConfig() throws Exception {

		// redefine the parameter definition, from configuration
		refreshParameterFromConfiguration();
	}

	/**
	 * rafraichit le paramètre , en fonction de la configuration
	 */
	protected void refreshParameterFromConfiguration() {
		parameter.setName(this.name);
		parameter.setLabel(this.name);
		parameter.setOptional(true);
		parameter.setType(this.type);
		parameter.setIn(!inputParameter);
		parameter.setStep(this);
	}

	@Override
	public boolean isSink() {
		return !inputParameter;
	}

	/**
	 * Terminal Value, not necessarely serializable
	 */
	private transient Object value;

	public Object getValue() {
		return this.value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return super.toString() + "    associated value :" + value + "(" + type + ")";
	}

}
