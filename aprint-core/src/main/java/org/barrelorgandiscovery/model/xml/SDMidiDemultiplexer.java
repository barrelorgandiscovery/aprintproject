package org.barrelorgandiscovery.model.xml;

import org.barrelorgandiscovery.model.ModelValuedParameter;
import org.barrelorgandiscovery.model.steps.midi.MidiDemultiplexer;
import org.barrelorgandiscovery.tools.SerializeTools;
import org.w3c.dom.Element;

public class SDMidiDemultiplexer extends BaseSerDeserHelper<MidiDemultiplexer> {
	
	@Override
	public Class<MidiDemultiplexer> getHelpedClass() {
		return MidiDemultiplexer.class;
	}

	@Override
	public MidiDemultiplexer fromXml(XmlSerContext ctx, Element object) throws Exception {

		MidiDemultiplexer demux = new MidiDemultiplexer();

		ModelValuedParameter[] cfParameter = demux.getConfigureParametersByRef();
		for (ModelValuedParameter p : cfParameter) {

			Element escale = getSubElement(object, p.getName());
			if (escale != null) {
				String v = escale.getTextContent();

				if (v != null && !v.isEmpty()) {
					p.setValue(SerializeTools.loadBase64(v));
				}
			}

		}

		demux.setId(getAttribute(object, "id"));

		// adjust the parameters
		demux.applyConfig();

		return demux;
	}

	@Override
	public void toXml(MidiDemultiplexer object, XmlSerContext ctx, Element element) throws Exception {

		ModelValuedParameter[] cfParameter = object.getConfigureParametersByRef();

		for (ModelValuedParameter p : cfParameter) {
			if (p.getValue() != null) { // save value if not null
				String stringV = SerializeTools.saveBase64(p.getValue());
				String name = p.getName();
				addElementValue(element, name, stringV);
			}
		}

		addAttributeValue(element, "id", object.getId());

	}
}
