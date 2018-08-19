package org.barrelorgandiscovery.model.steps.scripts;

import java.util.Collection;
import java.util.Map;

import org.barrelorgandiscovery.model.ModelParameter;
import org.barrelorgandiscovery.model.ModelType;
import org.barrelorgandiscovery.model.type.GenericSimpleType;
import org.barrelorgandiscovery.model.type.JavaType;
import org.barrelorgandiscovery.virtualbook.Hole;

/**
 * base class for model script, provide helpers for implementing model scripts
 * 
 * @author pfreydiere
 *
 */
public abstract class ModelGroovyScript {

	/**
	 * configure parameters
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract ModelParameter[] configureParameters() throws Exception;

	/**
	 * create a hole type
	 * 
	 * @return
	 */
	protected ModelType newHolesType() {
		return new GenericSimpleType(Collection.class, new Class[] { Hole.class });
	}
	
	/**
	 * create a simple java type
	 * @param clazz
	 * @return
	 */
	protected ModelType newJavaType(Class clazz) {
		return new JavaType(clazz);
	}

	/**
	 * create a new parameter
	 * 
	 * @param isIn
	 * @param name
	 * @param type
	 * @return
	 */
	protected ModelParameter newParameter(boolean isIn, String name, ModelType type) {
		ModelParameter p = new ModelParameter();
		p.setName(name);
		p.setIn(isIn);
		p.setType(type);
		return p;
	}

	/**
	 * execute the step
	 * 
	 * @param parameterValues
	 * @return output parameters values
	 * @throws Exception
	 */
	public abstract Map<String, Object> execute(Map<String, Object> parameterValues) throws Exception;

}
