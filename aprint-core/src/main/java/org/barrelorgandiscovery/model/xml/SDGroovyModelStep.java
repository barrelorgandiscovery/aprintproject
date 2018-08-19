package org.barrelorgandiscovery.model.xml;

import org.barrelorgandiscovery.model.steps.scripts.GroovyScriptModelStep;
import org.w3c.dom.Element;

public class SDGroovyModelStep extends BaseSerDeserHelper<GroovyScriptModelStep> {

	private static final String SCRIPT_ELEMENT = "script";

	@Override
	public Class<GroovyScriptModelStep> getHelpedClass() {
		return GroovyScriptModelStep.class;
	}

	@Override
	public GroovyScriptModelStep fromXml(XmlSerContext ctx, Element element) throws Exception {

		GroovyScriptModelStep mfi = new GroovyScriptModelStep();
		mfi.setId(getAttribute(element, "id"));
		// read script content
		String scriptContent = getValue(element,SCRIPT_ELEMENT);
		mfi.setScriptContent(scriptContent);
		
		if (scriptContent != null && !scriptContent.trim().isEmpty()) {
			// get inner parameters
			mfi.compileScript();
			mfi.applyConfig();
		}
		
		return mfi;
		
	}

	@Override
	public void toXml(GroovyScriptModelStep tms, XmlSerContext ctx, Element element) throws Exception {
		addAttributeValue(element, "id", tms.getId());
		
		addElementValue(element, SCRIPT_ELEMENT, tms.getScriptContent());
		
	}

}
