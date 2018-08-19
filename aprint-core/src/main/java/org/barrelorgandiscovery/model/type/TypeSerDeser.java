package org.barrelorgandiscovery.model.type;

import java.lang.reflect.Constructor;

import org.barrelorgandiscovery.model.ModelType;


public class TypeSerDeser {

	public static char FIELDS_SEPARATOR = '|';
	
	
	/**
	 * Serialize the model type
	 * 
	 * @param type
	 * @return
	 */
	public String serialize(ModelType type) throws Exception {
		assert type != null;
		return type.getClass().getName() + FIELDS_SEPARATOR + type.serializedForm();
	}

	/**
	 * Deser the Model Type
	 * 
	 * @param serialisedForm
	 * @return
	 * @throws Exception
	 */
	public ModelType deserialize(String serialisedForm) throws Exception {
		assert serialisedForm != null;
		int index = serialisedForm.indexOf(FIELDS_SEPARATOR);
		if (index == -1)
			throw new Exception("bad serialized form");

		String className = serialisedForm.substring(0, index);
		String serialized = serialisedForm.substring(index + 1);

		Class typeClazz = Class.forName(className);

		Constructor cons = typeClazz.getConstructor(String.class);
		return (ModelType) cons.newInstance(serialized);
	}

}
