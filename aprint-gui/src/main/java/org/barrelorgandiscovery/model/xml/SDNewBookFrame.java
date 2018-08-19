package org.barrelorgandiscovery.model.xml;

import org.barrelorgandiscovery.model.steps.impl.NewBookFrame;
import org.w3c.dom.Element;

public class SDNewBookFrame extends BaseSerDeserHelper<NewBookFrame> {

	public Class getHelpedClass() {
		return NewBookFrame.class;
	}

	public void toXml(NewBookFrame tms, XmlSerContext ctx, Element element) throws Exception {

		addElementValue(element, "instrumentName", tms.getInstrumentName());
		addAttributeValue(element, "id", tms.getId());

	}

	public NewBookFrame fromXml(XmlSerContext ctx, Element object) throws Exception {

		NewBookFrame newBookFrame = new NewBookFrame();
		newBookFrame.setId(getAttribute(object, "id"));
		newBookFrame.setInstrumentName(getSubElement(object, "instrumentName").getTextContent());
		newBookFrame.updateConfig();
		return newBookFrame;

	}

}
