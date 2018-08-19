package org.barrelorgandiscovery.instrument;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.LineParser;


public class InstrumentParser implements LineParser {

	private static final Logger logger = Logger
			.getLogger(InstrumentParser.class);

	private String name;
	private String patchstream;
	private String imagestream;
	private String gammename;
	private String instrumentDescriptionUrl;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.tools.LineParser#lineParsed(java.lang.String,
	 *      java.lang.String, int)
	 */
	public void lineParsed(String key, String value, int line) throws Exception {

		if (key == null || "".equals(key))
			return;

		logger.debug("read " + key + " -> " + value); //$NON-NLS-1$ //$NON-NLS-2$
		if ("name".equals(key)) { //$NON-NLS-1$
			logger.debug("instrument name " + value); //$NON-NLS-1$
			name = value;
		} else if ("gamme".equals(key)) { //$NON-NLS-1$
			logger.debug("gamme " + value); //$NON-NLS-1$
			gammename = value;
		} else if ("patch".equals(key)) { //$NON-NLS-1$
			logger.debug("patch " + value); //$NON-NLS-1$
			patchstream = value;
		} else if ("picture".equals(key)) { //$NON-NLS-1$
			logger.debug("picture stream " + value); //$NON-NLS-1$
			imagestream = value;
		} else if ("url".equals(key)) //$NON-NLS-1$
		{
			logger.debug("url associated to the picture"); //$NON-NLS-1$
			instrumentDescriptionUrl = value;
		}
	}

	public String getName() {
		return name;
	}

	public String getPatchstream() {
		return patchstream;
	}

	public String getPicture() {
		return imagestream;
	}

	public String getGammeName() {
		return gammename;
	}

	public String getInstrumentDescriptionUrl() {
		return instrumentDescriptionUrl;
	}

}
