package org.barrelorgandiscovery.extensions;

public class SimpleExtensionPoint implements ExtensionPoint {

	@SuppressWarnings("unchecked")
	private Class c;
	private Object o;

	@SuppressWarnings("unchecked")
	public SimpleExtensionPoint(Class c, Object o) throws Exception {
		this.c = c;
		this.o = o;

		if (!c.isAssignableFrom(o.getClass()))
			throw new Exception("object " + o + " does not implement "
					+ c.getName());

	}

	public Object getPoint() {
		return o;
	}

	@SuppressWarnings("unchecked")
	public Class getTypeID() {
		return c;
	}

}
