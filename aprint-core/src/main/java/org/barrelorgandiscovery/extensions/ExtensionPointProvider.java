package org.barrelorgandiscovery.extensions;

import java.lang.reflect.Array;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * Helper class for providing an extension point in an application development
 * 
 * @author Freydiere Patrice
 * 
 */
public class ExtensionPointProvider {

	private static Logger logger = Logger
			.getLogger(ExtensionPointProvider.class);

	/**
	 * This method get all the entry points in the extensions, of the type c
	 * 
	 * @param <T>
	 * @param c
	 *            the extension type id (interface implemented by the extension
	 *            point)
	 * @param exts
	 *            an array of extension in which entry points are implements
	 * @return an array of extension point of the type c contained in the
	 *         extensions
	 */
	public static <T> T[] getAllPoints(Class<T> c, IExtension[] exts) {

		Vector<T> v = new Vector<T>();

		if (exts != null) {
			for (int i = 0; i < exts.length; i++) {
				IExtension e = exts[i];
				try {

					ExtensionPoint[] pts = e.getExtensionPoints();
					if (pts == null)
						continue;

					for (int j = 0; j < pts.length; j++) {
						ExtensionPoint extensionPoint = pts[j];
						if (extensionPoint == null)
							continue;
						if (extensionPoint.getTypeID() == c) {
							try {

								Object o = extensionPoint.getPoint();
								if (o == null)
									continue;

								if (c.isAssignableFrom(o.getClass())) {
									v.add((T) o);
								}

							} catch (ClassCastException cce) {
								logger
										.error("extension point doesn't have support for interface "
												+ c.getClass());
							}
						}

					}

				} catch (Throwable t) {
					logger.error("cannot get extension points from "
							+ e.getName());
				}
			}
		}

		T[] retvalue = (T[]) Array.newInstance(c, v.size());
		v.copyInto(retvalue);

		return retvalue;
	}

}
