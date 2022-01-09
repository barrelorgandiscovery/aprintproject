package org.barrelorgandiscovery.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


import org.apache.log4j.Logger;

/**
 * Modeling a model editor step
 * 
 * @author pfreydiere
 * 
 */

public abstract class ModelStep implements Serializable {

	private static Logger logger = Logger.getLogger(ModelStep.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6866672484013245575L;

	/**
	 * ID of the model step
	 */
	private String id;

	/**
	 * label for this model step
	 */
	protected String label;

	/**
	 * Ordre in scheduling
	 */

	int schedule;

	public int getScheduleOrder() {
		return schedule;
	}

	/**
	 * Constructor
	 */
	public ModelStep() {

	}

	/**
	 * Get the parameter list reference
	 * 
	 * @return
	 */
	public abstract ModelParameter[] getAllParametersByRef();

	private static interface IFilterParameters {
		boolean filter(ModelParameter p);
	}

	protected ModelParameter[] filterParameters(IFilterParameters f) {
		ArrayList<ModelParameter> parameters = new ArrayList<ModelParameter>();
		ModelParameter[] pmts = getAllParametersByRef();
		for (int i = 0; i < pmts.length; i++) {
			ModelParameter modelParameter = pmts[i];
			if (f.filter(modelParameter))
				parameters.add(modelParameter);
		}
		return parameters.toArray(new ModelParameter[0]);
	}

	public ModelParameter[] getInputParametersByRef() {
		return filterParameters(new IFilterParameters() {

			public boolean filter(ModelParameter p) {
				return p.isIn();
			}
		});
	}

	public ModelParameter[] getOutputParametersByRef() {
		return filterParameters(new IFilterParameters() {
			public boolean filter(ModelParameter p) {
				return !p.isIn();
			}
		});
	}

	public ModelParameter getParameterByName(String name) {
		ModelParameter[] refp = getAllParametersByRef();
		for (int i = 0; i < refp.length; i++) {
			ModelParameter modelParameter = refp[i];
			if (modelParameter.getName() == null) 
			{
				logger.warn("modelParameter " + modelParameter + " has no name");
			}
			if (modelParameter.getName().equals(name)) {
				return modelParameter;
			}
		}
		return null;
	}

	/**
	 * find model parameter reference using its name
	 * 
	 * @param name
	 *            the modelparameter name
	 * @return null if not found
	 */
	public AbstractParameter findModelParameterRefByName(String name) {
		if (name == null)
			return null;

		AbstractParameter[] parametersByRef = getAllParametersByRef();
		for (int i = 0; i < parametersByRef.length; i++) {
			AbstractParameter modelParameter = parametersByRef[i];
			if (name.equals(modelParameter.getName()))
				return modelParameter;
		}
		return null;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public abstract String getName();

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ModelStep " + getClass().getName()).append("\n");
		sb.append("  " + id + " (" + label + ") , schedule :" + schedule).append("\n");
		return sb.toString();
	}

	public static class ParameterError {
		
		/**
		 * default constructor
		 */
		public ParameterError() {
			
		}
		
		public ParameterError(AbstractParameter parameter, String errorDescription) {
			this.parameter = parameter;
			this.errorDescription = errorDescription;
		}
		
		public AbstractParameter parameter;
		public String errorDescription;
	}

	/**
	 * Internal method to check config values
	 * 
	 * @return
	 */
	protected ParameterError[] validateConfigValues() {

		List<ParameterError> errors = new ArrayList<ModelStep.ParameterError>();
		ModelValuedParameter[] configParams = getConfigureParametersByRef();

		for (int i = 0; i < configParams.length; i++) {
			ModelValuedParameter modelValuedParameter = configParams[i];

			ModelType type = modelValuedParameter.getType();
			assert type != null;

			if (!type.doesValueBelongToThisType(modelValuedParameter.getValue())) {
				ParameterError pe = new ParameterError();
				pe.parameter = modelValuedParameter;
				pe.errorDescription = "Value " + modelValuedParameter.getValue() + " isn't of the type :"
						+ type.getName();
				errors.add(pe);
			}

			if (modelValuedParameter.getValue() == null) {
				ParameterError pe = new ParameterError();
				pe.parameter = modelValuedParameter;
				pe.errorDescription = "Parameter " + modelValuedParameter.getLabel() + " is mandatory";
				errors.add(pe);
			}

		}

		return errors.toArray(new ParameterError[0]);

	}

	/**
	 * Check the given parameters
	 * 
	 * @param parameterValues
	 * @return
	 */
	public ParameterError[] validateParameterValues(Map<String, Object> parameterValues) {

		assert parameterValues != null;

		List<ParameterError> errors = new ArrayList<ModelStep.ParameterError>();

		ModelParameter[] parameters = getAllParametersByRef();
		for (int i = 0; i < parameters.length; i++) {
			ModelParameter modelParameter = parameters[i];
			assert modelParameter != null;

			String name = modelParameter.getName();
			Object object = parameterValues.get(name);

			ModelType type = modelParameter.getType();
			assert type != null;

			if (!type.doesValueBelongToThisType(object)) {
				ParameterError pe = new ParameterError();
				pe.parameter = modelParameter;
				pe.errorDescription = "Value " + object + " isn't of the type :" + type.getName();
				errors.add(pe);
			}

			if (object == null && (!modelParameter.isOptional())) {
				ParameterError pe = new ParameterError();
				pe.parameter = modelParameter;
				pe.errorDescription = "Parameter " + modelParameter.getLabel() + " is mandatory";
				errors.add(pe);
			}

		}

		return errors.toArray(new ParameterError[0]);
	}

	/**
	 * Parametres de configuration, ces paramètres sont construits par référence
	 * dans la construction de l'objet et ne changent pas dans le cycle de vie
	 * de l'objet
	 */
	protected ModelValuedParameter[] configureParameters = new ModelValuedParameter[0];

	/**
	 * retourne les paramètres de configuration
	 * 
	 * @return
	 */
	public ModelValuedParameter[] getConfigureParametersByRef() {
		return configureParameters;
	}

	/**
	 * is the step still in configuration state ? this base implementation check
	 * if the value of the configparameter is not null
	 * 
	 * @return
	 */
	public boolean isConfigured() {
		for (int i = 0; i < configureParameters.length; i++) {
			ModelValuedParameter cp = configureParameters[i];
			if (cp.getValue() == null)
				return false;
		}
		
		ParameterError[] configvalueserrors = validateConfigValues();
		if (configvalueserrors != null && configvalueserrors.length > 0) {
			logger.debug( Arrays.stream( configvalueserrors).map(  e -> e.errorDescription ).reduce("", (i,r) -> i + " " + r )  );
			// there are specific config values errors
			return false;
		}
		
		return true;
	}

	/**
	 * This method adapt parameters from the configuration, this method is
	 * called after config parameters has been adjusted
	 * 
	 * usually this method modify the getInputParametersByRef and
	 * getOutputParametersByRef return
	 */
	public abstract void applyConfig() throws Exception;

	
	/**
	 * Execute the step, for interpret engine
	 * 
	 * @param values
	 *            , input values
	 * @return output values
	 */
	public abstract Map<AbstractParameter, Object> execute(Map<AbstractParameter, Object> inputValues) throws Exception;

	
	
}
