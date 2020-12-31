package org.barrelorgandiscovery.model.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.barrelorgandiscovery.tools.Base64Tools;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class BaseSerDeserHelper<T> implements SerDeserHelper<T> {

	protected void addAttributeValue(Element e, String name, String value) {
		if (value != null) {
			e.setAttribute(name, value);
		} else {
			e.removeAttribute(name);
		}
	}

	protected void addAttributeValue(Element e, String name, boolean value) {
		e.setAttribute(name, "" + value);
	}

	// Helper method for constructing the result
	protected void addElementValue(Element e, String name, String value) {
		if (value == null) {
			value = "";
		}
		Element v = e.getOwnerDocument().createElement(name);
		v.setTextContent(value);
		e.appendChild(v);
	}

	protected void addElementValue(Element e, String name, boolean value) {
		Element v = e.getOwnerDocument().createElement(name);
		v.setTextContent("" + value);
		e.appendChild(v);
	}

	protected Element getSubElement(Element e, String name) {
		NodeList elements = e.getElementsByTagName(name);
		if (elements.getLength() < 1)
			return null;

		return (Element) elements.item(0);
	}

	protected String getValue(Element e, String name) throws Exception {
		NodeList se = e.getElementsByTagName(name);
		if (se.getLength() < 1) {
			throw new Exception("cannot find element \"" + name + "\"");
		}
		Element nv = (Element) se.item(0);
		return nv.getTextContent();
	}

	protected boolean getBool(Element e, String name) throws Exception {
		return Boolean.getBoolean(getValue(e, name));
	}

	protected String getAttribute(Element e, String name) throws Exception {
		String av = e.getAttribute(name);
		if (av == null)
			throw new Exception("null attribute " + name);

		return av;
	}

	protected boolean getAttributeBool(Element e, String name) throws Exception {
		String av = getAttribute(e, name);

		return "true".equalsIgnoreCase(av);
	}

	protected void addSerializableValue(Element e, String name, Serializable value) throws Exception {

		String r = "";
		if (value != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(value);
			oos.close();

			r = Base64Tools.encode(baos.toByteArray());
		}
		addElementValue(e, name, r);
	}

	protected Serializable getSerializableValue(Element e, String name) throws Exception {

		String sv = getValue(e, name);
		if (sv == null || "".equals(sv)) {
			return null;
		}
		// otherwise, decode the content

		byte[] b = Base64Tools.decode(sv);

		return (Serializable) new ObjectInputStream(new ByteArrayInputStream(b)).readObject();
	}
}
