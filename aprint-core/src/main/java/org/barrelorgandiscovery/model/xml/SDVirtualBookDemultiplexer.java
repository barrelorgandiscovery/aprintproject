package org.barrelorgandiscovery.model.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.barrelorgandiscovery.model.ModelValuedParameter;
import org.barrelorgandiscovery.model.steps.book.VirtualBookMultiplexer;
import org.barrelorgandiscovery.model.steps.book.VirtualBookDemultiplexer;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.io.ScaleIO;
import org.w3c.dom.Element;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Step VirtualBookConstructor serializer / deserializer
 * 
 * @author pfreydiere
 * 
 */
public class SDVirtualBookDemultiplexer extends
		BaseSerDeserHelper<VirtualBookDemultiplexer> {

	public Class getHelpedClass() {
		return VirtualBookDemultiplexer.class;
	}

	public void toXml(VirtualBookDemultiplexer object, XmlSerContext ctx,
			Element element) throws Exception {

		ModelValuedParameter cfParameter = object.getConfigureParametersByRef()[0];

		Scale v = (Scale) cfParameter.getValue();
		if (v != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ScaleIO.writeGamme(v, baos);
			String stringV = new BASE64Encoder().encode(baos.toByteArray());
			addElementValue(element, "scale", stringV);
		}
		
		addAttributeValue(element, "id", object.getId());

	}

	public VirtualBookDemultiplexer fromXml(XmlSerContext ctx, Element object)
			throws Exception {

		VirtualBookDemultiplexer vbc = new VirtualBookDemultiplexer();
		ModelValuedParameter modelValuedParameter = vbc
				.getConfigureParametersByRef()[0];

		Element escale = getSubElement(object, "scale");
		if (escale != null) {
			String v = escale.getTextContent();
			ByteArrayInputStream bais = new ByteArrayInputStream(
					new BASE64Decoder().decodeBuffer(v));

			modelValuedParameter.setValue(ScaleIO.readGamme(bais));
			vbc.applyConfig(); // adjust parameters
		}

		vbc.setId(getAttribute(object, "id"));
		
		// adjust the parameters
		vbc.applyConfig();
		
		return vbc;
	}

}
