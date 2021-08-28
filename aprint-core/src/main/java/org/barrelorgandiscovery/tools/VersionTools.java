package org.barrelorgandiscovery.tools;

import java.net.URL;
import java.util.Properties;

public class VersionTools {

	public static String getVersion() throws Exception {
		
		URL url = VersionTools.class.getClassLoader().getResource(
				"aprintversion.properties");

		if (url == null)
			return null;

		Properties p = new Properties();
		p.load(url.openStream());

		return p.getProperty("version");

	}
	
	public static String getMainVersion() throws Exception {
		
		String version = getVersion();
		if (version == null)
			return null;
		
		int i = version.indexOf(".");
		if (i == -1) {
			return null;
		}
		String main = version.substring(0, i);
		
		return main;
	}

}
