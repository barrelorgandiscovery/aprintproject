package org.barrelorgandiscovery.tools;

import java.net.URL;
import java.util.Arrays;

public class URLTools {

	public static URL getParentURL(URL url) throws Exception {
		assert url != null;

		String urlString = url.toExternalForm();
		String[] e = urlString.split("/");

		String[] parentDecomposition = Arrays.copyOfRange(e, 0, e.length - 2);

		String joined = String.join("/", parentDecomposition);

		return new URL(joined);
	}
	
	

}
