package org.barrelorgandiscovery.scale;

import org.barrelorgandiscovery.tools.LineParser;

/**
 * Parser for reading the version number in a property file
 * 
 * @author Freydiere Patrice
 * 
 */
public class VersionParser implements LineParser {

	/*
	 * (non-Javadoc)
	 * @see org.barrelorgandiscovery.tools.LineParser#lineParsed(java.lang.String, java.lang.String, int)
	 */
	public void lineParsed(String key, String value, int line) throws Exception {

		if (key != null && value != null)
		{
			if ("version".equals(key)) //$NON-NLS-1$
			{
				version = value;
			}
		}
	}

	private String version = null;

	/**
	 * get the result version after the parsing
	 * @return
	 */
	public String getVersion() {
		return version;
	}

}
