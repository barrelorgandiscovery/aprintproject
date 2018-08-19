package org.barrelorgandiscovery.scale.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * Parser of a midiboek file
 * @author use
 *
 */
public class MidiBoekFileParser {

	private static final Logger logger = Logger
			.getLogger(MidiBoekFileParser.class);

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
	public MidiBoekFileParser(Reader reader, LineParser parser) {
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

			String[] params = line.split(" "); //$NON-NLS-1$
			Vector<String> v = new Vector<String>();
			for (String s : params) {
				String tmp = s.trim();
				if (!"".equals(tmp)) //$NON-NLS-1$
					v.add(tmp);
			}

			if (v.size() > 0) {
				String cmd = v.get(0);
				v.remove(0);
				String[] retvalue = new String[v.size()];
				v.copyInto(retvalue);
				if (logger.isDebugEnabled())
				{
					
					StringBuffer sb = new StringBuffer();
					for (String s : retvalue)
					{
						sb.append("Params :" + s + " \n"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					
					
					logger.debug("send to parser " + cmd + " PARAMS " + sb.toString()); //$NON-NLS-1$ //$NON-NLS-2$
				}
				this.ref.lineParsed(cmd, retvalue);
			}
		}
	}

}
