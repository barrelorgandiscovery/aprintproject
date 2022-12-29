package org.barrelorgandiscovery.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.model.annotations.ParameterName;
import org.barrelorgandiscovery.model.annotations.ParameterOut;
import org.barrelorgandiscovery.model.annotations.ProcessorMethod;
import org.barrelorgandiscovery.model.type.JavaType;

/**
 * Step calling an object method name
 * 
 * @author use
 * 
 */
public class ObjectMethodStep extends ModelStep {

	private static Logger logger = Logger.getLogger(ObjectMethodStep.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 5763493793568063675L;

	private String objectClassName;

	private ModelParameter[] constructorParameter;
	private ModelParameter[] outParams;

	/**
	 * Model parameters
	 */
	private ModelParameter[] mparams; // models parameters

	/**
	 * Reference to the called Method
	 */
	private Class _clazz;
	private String methodName;

	public ObjectMethodStep(Class c, String methodName) throws Exception {

		assert c != null;
		assert methodName != null;

		this.objectClassName = c.getName();
		this._clazz = c;
		this.methodName = methodName;

		// detect parameters from constructor and selected method

		createModelStepFromClass();

	}

	@Override
	public ModelParameter[] getAllParametersByRef() {
		List<ModelParameter> ret = new ArrayList<ModelParameter>();
		if (constructorParameter != null)
			ret.addAll(Arrays.asList(constructorParameter));
		if (mparams != null)
			ret.addAll(Arrays.asList(mparams));
		if (outParams != null)
			ret.addAll(Arrays.asList(outParams));
		return ret.toArray(new ModelParameter[0]);
	}

	/*
	 * stepName
	 */
	private String name;

	@Override
	public String getName() {
		return this.name;
	}

	protected void setName(String processorName) {
		this.name = processorName;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString()).append("\n");
		sb.append("ObjectMethodStep : " + this.getId() + "/" + this.getLabel() + "-" + objectClassName);

		AbstractParameter[] parametersByRef = getAllParametersByRef();
		if (parametersByRef != null) {
			for (int i = 0; i < parametersByRef.length; i++) {
				AbstractParameter modelParameter = parametersByRef[i];
				sb.append(modelParameter.toString()).append("\n");
			}
		}
		return sb.toString();
	}

	@Override
	public void applyConfig() throws Exception {
		// nothing to do, no config
	}

	@Override
	public Map<AbstractParameter, Object> execute(Map<AbstractParameter, Object> values) throws Exception {

		assert values != null;

		// instanciate
		Object newInstance = getConstructor().newInstance(toCallArray(constructorParameter, values));

		Map<AbstractParameter, Object> h = new HashMap<AbstractParameter, Object>();
		assert outParams != null && outParams.length == 1;

		Object result = getMethod().invoke(newInstance, toCallArray(mparams, values));
		h.put(outParams[0], result);

		return h;
	}

	private Object[] toCallArray(ModelParameter[] parameters, Map<AbstractParameter, Object> values) {
		Object[] rets = new Object[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			ModelParameter p = parameters[i];
			assert p != null;
			Object associatedValue = values.get(p);
			if (logger.isDebugEnabled() && associatedValue == null) {
				logger.debug("no value for parameter :" + p);
			}
			rets[i] = associatedValue;
		}
		return rets;
	}

	/**
	 * Retrieve the class name
	 * 
	 * @return
	 */
	public String getObjectClassName() {
		return objectClassName;
	}

	/**
	 * Retrieved the method name for this processor
	 * 
	 * @return
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * Find a method by it's name, if multiple method have the same name, the
	 * first one is taken
	 * 
	 * @param clazz
	 * @param methodName
	 * @return
	 */
	private Method findMethodByName(Class clazz, String methodName) {
		Method[] mthds = clazz.getMethods();
		for (int i = 0; i < mthds.length; i++) {
			Method method = mthds[i];
			if (method.getName().equals(methodName))
				return method;
		}
		return null;
	}

	/**
	 * Create a modelStep from introspecting a class
	 */
	private void createModelStepFromClass() throws Exception {

		// create the constructor parameters
		ArrayList<ModelParameter> constructorParameters = createModelParameters(getConstructor().getParameterTypes(),
				getConstructor().getParameterAnnotations());

		this.constructorParameter = constructorParameters.toArray(new ModelParameter[0]);

		// find method

		Method method = getMethod();
		if (method == null)
			throw new Exception("method " + methodName + " not found in class " + _clazz);

		this.name = method.getName();

		String processorMethodName = getModelProcessorMethodAnnotationName(method.getAnnotations());

		setName(processorMethodName);

		ArrayList<ModelParameter> createModelParameters = createModelParameters(method.getParameterTypes(),
				method.getParameterAnnotations());

		this.mparams = createModelParameters.toArray(new ModelParameter[0]);

		// handling out parameter

		String outParamName = getModelParameterOutName(method.getAnnotations());
		if (outParamName == null) {
			throw new Exception("Method " + method.getName() + " ha no named output parameter");
		}

		// out parameter
		ModelParameter p = new ModelParameter();
		p.setName(outParamName);
		p.setType(new JavaType(method.getReturnType()));
		p.setIn(false);
		p.setOptional(false);
		p.setStep(this);

		outParams = new ModelParameter[] { p };

	}

	private Method getMethod() {
		return findMethodByName(_clazz, methodName);
	}

	private Constructor getConstructor() throws Exception {
		Constructor[] constructors = this._clazz.getConstructors();

		if (constructors.length > 1) {
			logger.warn("too many constructors");
			throw new Exception("too many constructors");
		}

		return constructors[0];
	}

	private ArrayList<ModelParameter> createModelParameters(Class[] parameterTypes, Annotation[][] annotations) {
		ArrayList<ModelParameter> ret = new ArrayList<ModelParameter>();
		for (int i = 0; i < parameterTypes.length; i++) {
			Class classParameter = parameterTypes[i];
			ModelParameter p = new ModelParameter();
			String name = getModelAnnotationName(annotations[i]);
			if (name == null) {
				name = classParameter.getSimpleName();
			}
			p.setName(name);
			p.setType(new JavaType(classParameter));
			p.setIn(true);
			p.setOptional(false);
			p.setStep(this);
			ret.add(p);
		}
		return ret;
	}

	private String getModelAnnotationName(Annotation[] ann) {
		for (int i = 0; i < ann.length; i++) {
			Annotation annotation = ann[i];
			if (annotation.annotationType().isAssignableFrom(ParameterName.class)) {
				ParameterName pn = (ParameterName) annotation;
				return pn.name();
			}
		}

		return null;
	}

	String getModelProcessorMethodAnnotationName(Annotation[] ann) {
		for (int i = 0; i < ann.length; i++) {
			Annotation annotation = ann[i];
			if (annotation.annotationType().isAssignableFrom(ProcessorMethod.class)) {
				ProcessorMethod pn = (ProcessorMethod) annotation;
				return pn.labelName();
			}
		}

		return null;
	}

	String getModelParameterOutName(Annotation[] ann) {
		for (int i = 0; i < ann.length; i++) {
			Annotation annotation = ann[i];
			if (annotation.annotationType().isAssignableFrom(ParameterOut.class)) {
				ParameterOut pn = (ParameterOut) annotation;
				return pn.name();
			}
		}

		return null;
	}

}
