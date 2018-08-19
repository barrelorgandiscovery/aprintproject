package org.barrelorgandiscovery.model.xml;

import org.w3c.dom.Element;

/**
 * Interface for ser/deser elements
 * 
 * @author use
 * 
 * @param <T>
 */
public interface SerDeserHelper<T> {

	Class<T> getHelpedClass();

	void toXml(T object, XmlSerContext ctx, Element element) throws Exception;

	T fromXml(XmlSerContext ctx, Element object) throws Exception;

}
