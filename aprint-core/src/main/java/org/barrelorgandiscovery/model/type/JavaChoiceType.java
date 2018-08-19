package org.barrelorgandiscovery.model.type;

import java.io.Serializable;
import java.util.ArrayList;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.model.ModelType;

public class JavaChoiceType implements ModelType, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8765880402007865906L;
	
	private Class[] clazzes;

	public JavaChoiceType(Class[] alternatives) {
		this.clazzes = alternatives;

		assert alternatives.length > 0;

		for (Class c : alternatives) {
			assert c != null;
		}

	}

	public JavaChoiceType(String serializedString) throws Exception {
		assert serializedString != null;

		ArrayList<Class> l = new ArrayList<Class>();

		String[] e = serializedString.split(",");

		for (String s : e) {
			l.add(Class.forName(s));
		}

		this.clazzes = l.toArray(new Class[l.size()]);
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAssignableFrom(ModelType type) {

		if (type == null)
			return false;

		if (type instanceof JavaType) {
			JavaType j = (JavaType) type;
			for (Class c : clazzes) {
				boolean result = c.isAssignableFrom(j.clazz);
				if (result)
					return true;
			}
		}

		return false;

	}

	@Override
	public String getName() {

		return null;
	}

	@Override
	public boolean doesValueBelongToThisType(Object value) {
		for (Class clazz : clazzes) {
			try {
				clazz.cast(value);
				return true;
			} catch (ClassCastException e) {

			}
		}
		return false;
	}

	@Override
	public String serializedForm() throws Exception {
		StringBuilder sb = new StringBuilder();

		for (Class c : clazzes) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(c.getName());
		}

		return sb.toString();
	}
	
	@Override
	public String getLabel() {
		String key = "PRIMITIVETYPE|" + getName();
		return Messages.getString(key);
	}

}
