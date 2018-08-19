package org.barrelorgandiscovery.instrument;

import java.util.ArrayList;

import org.apache.log4j.Logger;


public class InstrumentParserV1 extends InstrumentParser {

	private static final Logger logger = Logger
			.getLogger(InstrumentParserV1.class);

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
		} else if (key.startsWith("registerpatch.")) {
			logger.debug("get the registerpatch definition");
			String[] elements = value.split(",");
			String pipestopset = elements[0];
			String pipestop = elements[1];
			String instrumentNumber = elements[2];

			logger.debug("pipestopset " + pipestopset);
			logger.debug("pipestop " + pipestop);
			logger.debug("instrument number " + instrumentNumber);

			RegisterLinkDef rld = new RegisterLinkDef(pipestopset, pipestop,
					Integer.parseInt(instrumentNumber));
			registerLinks.add(rld);

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

	private ArrayList<RegisterLinkDef> registerLinks = new ArrayList<RegisterLinkDef>();

	public ArrayList<RegisterLinkDef> getRegisterLinks() {
		return registerLinks;
	}

}
