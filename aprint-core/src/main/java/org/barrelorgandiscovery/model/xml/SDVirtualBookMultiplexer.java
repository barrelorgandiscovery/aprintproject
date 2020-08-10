package org.barrelorgandiscovery.model.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.barrelorgandiscovery.model.ModelValuedParameter;
import org.barrelorgandiscovery.model.steps.book.VirtualBookMultiplexer;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.io.ScaleIO;
import org.w3c.dom.Element;

/**
 * Step VirtualBookConstructor serializer / deserializer
 * 
 * @author pfreydiere
 * 
 */
public class SDVirtualBookMultiplexer extends BaseSerDeserHelper<VirtualBookMultiplexer> {

	public Class getHelpedClass() {
		return VirtualBookMultiplexer.class;
	}

	public void toXml(VirtualBookMultiplexer object, XmlSerContext ctx, Element element) throws Exception {

		ModelValuedParameter cfParameter = object.getConfigureParametersByRef()[0];

		Scale v = (Scale) cfParameter.getValue();
		if (v != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ScaleIO.writeGamme(v, baos);
			Encoder encoder = Base64.getEncoder();
			String stringV = new String(encoder.encode(baos.toByteArray()));
			addElementValue(element, "scale", stringV);
		}

		addAttributeValue(element, "id", object.getId());

	}

	public VirtualBookMultiplexer fromXml(XmlSerContext ctx, Element object) throws Exception {

		VirtualBookMultiplexer vbc = new VirtualBookMultiplexer();
		ModelValuedParameter modelValuedParameter = vbc.getConfigureParametersByRef()[0];

		Element escale = getSubElement(object, "scale");
		if (escale != null) {
			String v = escale.getTextContent();
			Decoder decoder = Base64.getDecoder();
			ByteArrayInputStream bais = new ByteArrayInputStream(decoder.decode(v));

			modelValuedParameter.setValue(ScaleIO.readGamme(bais));
			vbc.applyConfig(); // adjust parameters
		}

		vbc.setId(getAttribute(object, "id"));

		// adjust the parameters
		vbc.applyConfig();

		return vbc;
	}

}
