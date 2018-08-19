package org.barrelorgandiscovery.model.xml;

import org.barrelorgandiscovery.model.ModelParameter;
import org.barrelorgandiscovery.model.ModelType;
import org.barrelorgandiscovery.model.type.TypeSerDeser;
import org.w3c.dom.Element;

/**
 * Class for XML serialization / deserialization
 * 
 * @author use
 * 
 */
public class SDModelParameter extends BaseSerDeserHelper<ModelParameter> {

	public Class<ModelParameter> getHelpedClass() {
		return ModelParameter.class;
	}

	public void toXml(ModelParameter o, XmlSerContext ctx, Element e) throws Exception {

		addAttributeValue(e, "label", o.getLabel());
		addAttributeValue(e, "name", o.getName());
		addAttributeValue(e, "id", o.getId());
		
		ModelType t = o.getType();
		if (t == null) throw new Exception("Model Parameter is null, invalid implementation");
		addElementValue(e, "type", new TypeSerDeser().serialize(t));

		addAttributeValue(e, "optional", o.isOptional());
		addAttributeValue(e, "in", o.isIn());

	}

	public ModelParameter fromXml(XmlSerContext ctx, Element e) throws Exception {

		ModelParameter mp = new ModelParameter();
		mp.setLabel(getAttribute(e, "label"));
		mp.setName(getAttribute(e, "name"));
		mp.setId(getAttribute(e, "id"));
		mp.setType(new TypeSerDeser().deserialize(getValue(e, "type")));
		mp.setOptional(getAttributeBool(e, "optional"));
		mp.setIn(getAttributeBool(e, "in"));

		return mp;
	}
}
