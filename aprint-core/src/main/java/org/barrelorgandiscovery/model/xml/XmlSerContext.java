package org.barrelorgandiscovery.model.xml;

import java.util.LinkedHashMap;

import org.w3c.dom.Element;

public class XmlSerContext {

	private LinkedHashMap<Class, SerDeserHelper> helpers = new LinkedHashMap<Class, SerDeserHelper>();

	public void register(SerDeserHelper h) {
		helpers.put(h.getHelpedClass(), h);
	}

	public Element toXML(Object o, Element parent) throws Exception {

		Class clazz = o.getClass();

		SerDeserHelper h = getSerDeserHelper(clazz);

		Element current = parent.getOwnerDocument().createElement(
				clazz.getName());

		h.toXml(o, this, current);
		parent.appendChild(current);

		return current;
	}

	/**
	 * Serialize in XML
	 * 
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	protected SerDeserHelper getSerDeserHelper(Class clazz) throws Exception {
		SerDeserHelper h = helpers.get(clazz);
		if (h == null)
			throw new Exception("no SerDeser object for " + clazz.getName());
		return h;
	}

	/**
	 * 
	 * @param e
	 * @return
	 * @throws Exception
	 */
	public Object fromXML(Element e) throws Exception {

		String localName = e.getNodeName();
		Class c = Class.forName(localName);

		SerDeserHelper h = getSerDeserHelper(c);

		return h.fromXml(this, e);

	}

}
