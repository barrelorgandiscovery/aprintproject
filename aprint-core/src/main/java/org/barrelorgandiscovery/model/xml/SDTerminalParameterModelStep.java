package org.barrelorgandiscovery.model.xml;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.model.TerminalParameterModelStep;
import org.barrelorgandiscovery.model.type.TypeSerDeser;
import org.w3c.dom.Element;

public class SDTerminalParameterModelStep extends BaseSerDeserHelper<TerminalParameterModelStep> {

	private static Logger logger = Logger.getLogger(SDTerminalParameterModelStep.class);
	
	public Class getHelpedClass() {
		return TerminalParameterModelStep.class;
	}

	public void toXml(TerminalParameterModelStep tms, XmlSerContext ctx, Element element) throws Exception {

		addAttributeValue(element, "in", tms.isInput());
		addAttributeValue(element, "name", tms.getName());
		addAttributeValue(element, "label", tms.getLabel());

		// add Type
		addElementValue(element, "type", new TypeSerDeser().serialize(tms.getModelType()));

		// add stored Value
		Object serialization = null;
		
		if (tms.getValue() instanceof Serializable) {
			serialization = tms.getValue();
		} else {
			logger.warn("value " + tms.getValue() + " is not serializable");
		}
		addSerializableValue(element, "value", (Serializable) serialization);
		
		addAttributeValue(element, "id", tms.getId());

	}

	public TerminalParameterModelStep fromXml(XmlSerContext ctx, Element object) throws Exception {

		TerminalParameterModelStep terminalParameterModelStep = new TerminalParameterModelStep(
				getAttributeBool(object, "in"), new TypeSerDeser().deserialize(getValue(object, "type")),
				getAttribute(object, "name"), getAttribute(object, "label"), 
				getSerializableValue(object, "value"));

		terminalParameterModelStep.setId(getAttribute(object, "id"));
		return terminalParameterModelStep;

	}

}
