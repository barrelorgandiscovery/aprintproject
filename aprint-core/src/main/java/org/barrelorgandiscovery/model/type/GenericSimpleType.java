package org.barrelorgandiscovery.model.type;

import java.io.Serializable;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.model.ModelType;

/**
 * 
 * 
 * @author pfreydiere
 *
 */
public class GenericSimpleType implements ModelType, Serializable {

	private Class genericType;
	private Class[] components;

	/**
	 * constructor
	 * 
	 * @param genericType
	 * @param components
	 */
	public GenericSimpleType(Class genericType, Class[] components) {
		this.genericType = genericType;
		this.components = components;
		for (Class c : components) {
			assert c != null;
		}
	}

	/**
	 * unserialized contructor
	 * 
	 * @param serializedForm
	 * @throws Exception
	 */
	public GenericSimpleType(String serializedForm) throws Exception {
		Pattern p = Pattern.compile("(.*)[<](.*)[>]");
		Matcher m = p.matcher(serializedForm);
		if (!m.matches())
			throw new Exception("cannot parse serialized form " + serializedForm);

		genericType = Class.forName(m.group(1));
		String[] elements = m.group(2).split(",");
		Class[] result = new Class[elements.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Class.forName(elements[i]);
		}
		this.components = result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.model.ModelType#getDescription()
	 */
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.model.ModelType#isAssignableFrom(org.
	 * barrelorgandiscovery.model.ModelType)
	 */
	@Override
	public boolean isAssignableFrom(ModelType type) {

		if (type.getClass() != getClass()) {
			return false; // not same definition
		}

		assert type instanceof GenericSimpleType;
		GenericSimpleType g = (GenericSimpleType) type;

		if (!genericType.isAssignableFrom(g.genericType)) {
			return false;
		}

		if (components.length != g.components.length)
			return false;

		for (int i = 0; i < components.length; i++) {
			if (!(components[i].isAssignableFrom(g.components[i])))
				return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.model.ModelType#getName()
	 */
	@Override
	public String getName() {

		String content = Arrays.stream(components).reduce("", (acc, c) -> {
			String s = acc;
			if (s.length() > 0)
				s += ",";
			return s + c.getName();
		}, (s1, s2) -> {
			return s1 + "," + s2;
		});

		return genericType.getName() + "<" + content + ">";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.model.ModelType#doesValueBelongToThisType(java.
	 * lang.Object)
	 */
	@Override
	public boolean doesValueBelongToThisType(Object value) {

		if (value == null)
			return true;

		if (genericType.isAssignableFrom(value.getClass())) {
			return true;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.model.ModelType#serializedForm()
	 */
	@Override
	public String serializedForm() throws Exception {
		return getName();
	}
	
	@Override
	public String getLabel() {
		String key = "PRIMITIVETYPE|" + getName();
		return Messages.getString(key);
	}

	@Override
	public String toString() {
		return getName();
	}

}
