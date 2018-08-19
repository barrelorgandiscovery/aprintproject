package org.barrelorgandiscovery.model.type;

import java.util.ArrayList;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.model.ModelType;

/**
 * alternative type definition, A or B
 * 
 * @author pfreydiere
 *
 */
public class CompositeType implements ModelType {

	private static final String INFO_SEPARATOR = ";";

	private static final String INFO_PREFIX = "info:";

	private TypeSerDeser typeserdeser = new TypeSerDeser();

	private ModelType[] typeList;

	private String name;
	private String description;

	public CompositeType(String encodedSerializedForm) throws Exception {

		String[] encoded = encodedSerializedForm.split(",");
		ArrayList<ModelType> ret = new ArrayList<>();
		for (String s : encoded) {
			if (s == null)
				continue;
			if (s.isEmpty())
				continue;

			if (s.startsWith(INFO_PREFIX)) {
				s = s.substring(5);
				String[] nameDescription = s.split(INFO_SEPARATOR);
				name = nameDescription[0];
				description = nameDescription[1];
			} else {
				ret.add(typeserdeser.deserialize(s));
			}
		}
		
		typeList = ret.toArray(new ModelType[ret.size()]);

	}

	public CompositeType(ModelType[] typeList, String name, String description) {
		this.typeList = typeList;
		this.name = name;
		this.description = description;

	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isAssignableFrom(ModelType type) {
		for (ModelType t : typeList) {
			if (t.isAssignableFrom(type))
				return true;
		}
		return false;
	}

	@Override
	public boolean doesValueBelongToThisType(Object value) {
		for (ModelType t : typeList) {
			if (t.doesValueBelongToThisType(value))
				return true;
		}
		return false;
	}

	@Override
	public String serializedForm() throws Exception {

		StringBuilder sb = new StringBuilder();
		
		sb.append(INFO_PREFIX);
		sb.append(name);
		sb.append(INFO_SEPARATOR);
		sb.append(description);
		
		for (ModelType m : typeList) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(typeserdeser.serialize(m));
		}

		return sb.toString();
	}

	/**
	 * search the model type associated to the given value, return null if not
	 * found
	 * 
	 * @param value
	 * @return
	 */
	public ModelType getBelongingType(Object value) {
		for (ModelType t : typeList) {
			if (t.doesValueBelongToThisType(value))
				return t;
		}
		return null;
	}
	
	@Override
	public String getLabel() {
		String key = "PRIMITIVETYPE|" + getName();
		return Messages.getString(key);
	}

}
