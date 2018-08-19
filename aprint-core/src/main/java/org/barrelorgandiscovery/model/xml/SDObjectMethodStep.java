package org.barrelorgandiscovery.model.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.model.ModelValuedParameter;
import org.barrelorgandiscovery.model.ObjectMethodStep;
import org.barrelorgandiscovery.model.steps.book.VirtualBookMultiplexer;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.io.ScaleIO;
import org.w3c.dom.Element;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Step ObjectMethodStep serializer / deserializer
 * 
 * @author pfreydiere
 * 
 */
public class SDObjectMethodStep extends BaseSerDeserHelper<ObjectMethodStep> {

	private static Logger logger = Logger.getLogger(SDObjectMethodStep.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.model.xml.SerDeserHelper#getHelpedClass()
	 */
	public Class getHelpedClass() {

		return ObjectMethodStep.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.model.xml.SerDeserHelper#toXml(java.lang.Object,
	 * org.barrelorgandiscovery.model.xml.XmlSerContext, org.w3c.dom.Element)
	 */
	public void toXml(ObjectMethodStep oms, XmlSerContext ctx, Element element)
			throws Exception {

		addElementValue(element, "class", oms.getObjectClassName());
		addElementValue(element, "method", oms.getMethodName());
		addAttributeValue(element, "id", oms.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.model.xml.SerDeserHelper#fromXml(org.
	 * barrelorgandiscovery.model.xml.XmlSerContext, org.w3c.dom.Element)
	 */
	public ObjectMethodStep fromXml(XmlSerContext ctx, Element object)
			throws Exception {

		String cn = getValue(object, "class");
		String mn = getValue(object, "method");

		ObjectMethodStep objectMethodStep = new ObjectMethodStep(
				Class.forName(cn), mn);
		String readId = getAttribute(object, "id");
		logger.debug("readid : " + readId);
		objectMethodStep.setId(readId);
		return objectMethodStep;

	}

}
