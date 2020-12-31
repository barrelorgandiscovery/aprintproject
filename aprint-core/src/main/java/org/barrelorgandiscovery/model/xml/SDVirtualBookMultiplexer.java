package org.barrelorgandiscovery.model.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.model.ModelValuedParameter;
import org.barrelorgandiscovery.model.steps.book.VirtualBookMultiplexer;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.io.ScaleIO;
import org.barrelorgandiscovery.tools.Base64Tools;
import org.w3c.dom.Element;

/**
 * Step VirtualBookConstructor serializer / deserializer
 * 
 * @author pfreydiere
 * 
 */
public class SDVirtualBookMultiplexer extends BaseSerDeserHelper<VirtualBookMultiplexer> {

	private static Logger logger = Logger.getLogger(SDVirtualBookMultiplexer.class);

	public Class getHelpedClass() {
		return VirtualBookMultiplexer.class;
	}

	public void toXml(VirtualBookMultiplexer object, XmlSerContext ctx, Element element) throws Exception {

		ModelValuedParameter cfParameter = object.getConfigureParametersByRef()[0];

		Scale v = (Scale) cfParameter.getValue();
		if (v != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ScaleIO.writeGamme(v, baos);
			String stringV = Base64Tools.encode(baos.toByteArray());
			addElementValue(element, "scale", stringV);
		}

		addAttributeValue(element, "id", object.getId());

	}

	public VirtualBookMultiplexer fromXml(XmlSerContext ctx, Element object) throws Exception {

		VirtualBookMultiplexer vbc = new VirtualBookMultiplexer();
		ModelValuedParameter modelValuedParameter = vbc.getConfigureParametersByRef()[0];

		vbc.setId(getAttribute(object, "id"));
		Element escale = getSubElement(object, "scale");

		if (escale != null) {
			String v = escale.getTextContent();
			
			try {
				byte[] decodedTextContent = Base64Tools.decode(v);
				ByteArrayInputStream bais = new ByteArrayInputStream(decodedTextContent);

				modelValuedParameter.setValue(ScaleIO.readGamme(bais));
				vbc.applyConfig(); // adjust parameters
			} catch (Exception ex) {
				logger.error("error on reading scale definition, " + ex.getMessage() + " for " + object, ex);
			}
		}

		// adjust the parameters
		vbc.applyConfig();

		return vbc;
	}

}
