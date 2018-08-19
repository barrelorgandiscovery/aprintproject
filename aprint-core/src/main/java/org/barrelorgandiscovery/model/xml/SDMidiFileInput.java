package org.barrelorgandiscovery.model.xml;

import org.barrelorgandiscovery.model.steps.midi.MidiFileInput;
import org.w3c.dom.Element;

public class SDMidiFileInput extends BaseSerDeserHelper<MidiFileInput> {

	@Override
	public Class<MidiFileInput> getHelpedClass() {
		return MidiFileInput.class;
	}

	@Override
	public MidiFileInput fromXml(XmlSerContext ctx, Element object) throws Exception {

		MidiFileInput mfi = new MidiFileInput();
		mfi.setId(getAttribute(object, "id"));
		return mfi;
		
	}

	@Override
	public void toXml(MidiFileInput tms, XmlSerContext ctx, Element element) throws Exception {
		addAttributeValue(element, "id", tms.getId());
	}

}
