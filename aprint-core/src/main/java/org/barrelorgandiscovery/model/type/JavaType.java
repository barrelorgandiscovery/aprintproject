package org.barrelorgandiscovery.model.type;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.model.ModelType;

public class JavaType implements ModelType, Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1242834661119722847L;

	Class clazz;

	private static final Map<String, Class> primitives = new HashMap<String, Class>();
	static {
		primitives.put("int", int.class);

	}

	public JavaType(Class clazz) {
		assert clazz != null;
		this.clazz = clazz;
	}
	
	public JavaType(String serializedString) throws Exception {
		assert serializedString != null;

		Class<?> clazz = null;

		if (primitives.containsKey(serializedString))
			clazz = primitives.get(serializedString);
		else
			clazz = Class.forName(serializedString);
		
		this.clazz = clazz;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.model.ModelType#getDescription()
	 */
	public String getDescription() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.model.ModelType#isAssignableFrom(org.
	 * barrelorgandiscovery.model.ModelType)
	 */
	public boolean isAssignableFrom(ModelType type) {

		if (type == null)
			return false;

		if (type instanceof JavaType) {
			JavaType j = (JavaType) type;
			return clazz.isAssignableFrom(j.clazz);
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.model.ModelType#getName()
	 */
	public String getName() {
		return clazz.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.model.ModelType#doesValueBelongToThisType(java
	 * .lang.Object)
	 */
	public boolean doesValueBelongToThisType(Object value) {
		try {
			clazz.cast(value);
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

	public String serializedForm() {
		return this.clazz.getName();
	}

	public Class getTargetedJavaType() {
		return this.clazz;
	}
	
	public String getLabel() {
		String key = "PRIMITIVETYPE|" + clazz.getSimpleName();
		return Messages.getString(key);
	}
	
	/**
	 * this is technical string
	 */
	@Override
	public String toString() {
		return getLabel();
	}

}
