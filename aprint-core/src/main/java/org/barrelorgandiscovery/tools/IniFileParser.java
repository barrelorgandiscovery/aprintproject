package org.barrelorgandiscovery.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.log4j.Logger;

public class IniFileParser {

	private static final Logger logger = Logger.getLogger(IniFileParser.class);

	private LineParser ref = null;

	private BufferedReader reader = null;

	/**
	 * Constructeur
	 * 
	 * @param reader
	 *            le fichier ou stream à lire
	 * @param parser
	 *            le parser d'évènements
	 */
	public IniFileParser(Reader reader, LineParser parser) {
		super();

		this.reader = new BufferedReader(reader);
		this.ref = parser;

	}

	/**
	 * Parse un fichier ini en invoquant le parser lorsqu'une ligne est lue
	 * 
	 * @throws IOException
	 */
	public void parse() throws IOException, Exception {

		String line = null;
		int cpt = 0;
		while ((line = reader.readLine()) != null) {
			// On enlève les commentaires
			cpt++;
			int comment = line.indexOf(';');
			if (comment != -1) {

				line = line.substring(0, comment);
			}

			int e = line.indexOf('=');
			if (e != -1) {
				String begin = line.substring(0, e);
				String end = line.substring(e + 1);
				String key = begin.trim();
				String value = end.trim();
				logger.debug("parse line " + cpt + " " + key + "=" + value);
				ref.lineParsed(key, value, cpt);
			}

		}
	}

}
