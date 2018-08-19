package org.barrelorgandiscovery.scale.io;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.ScaleException;
import org.barrelorgandiscovery.tools.LineParser;
import org.barrelorgandiscovery.tools.MidiHelper;


public class ScaleParser implements LineParser {

	private static final Logger logger = Logger.getLogger(ScaleParser.class);

	private int version = 0;

	public ScaleParser(int version) {
		this.version = version;
	}

	private String name = ""; //$NON-NLS-1$

	private double width = Double.NaN;

	private int nbpistes = 0;

	private AbstractTrackDef[] notemidi;

	private double entrepiste = Double.NaN;

	private double firsttrack = Double.NaN;

	private double largeurpiste = Double.NaN;

	private double speed = Double.NaN;

	private String currentclassification = null;

	private String contact = null;

	private String state = null;
	

	public void lineParsed(String key, String value, int line) throws Exception {
		try {

			if ("width".equals(key)) { //$NON-NLS-1$
				width = Double.parseDouble(value);
			} else if ("name".equals(key)) { //$NON-NLS-1$
				name = value;
			} else if ("intertrack".equals(key)) { //$NON-NLS-1$
				entrepiste = Double.parseDouble(value);
			} else if ("firsttrackoffset".equals(key)) { //$NON-NLS-1$
				firsttrack = Double.parseDouble(value);
			} else if ("perforationwidth".equals(key)) { //$NON-NLS-1$
				largeurpiste = Double.parseDouble(value);
			} else if ("speed".equals(key)) { //$NON-NLS-1$
				speed = Double.parseDouble(value);
			} else if ("tracknumber".equals(key)) { //$NON-NLS-1$
				nbpistes = Integer.parseInt(value);
				notemidi = new AbstractTrackDef[nbpistes];
				for (int i = 0; i < notemidi.length; i++)
					notemidi[i] = null; // pas de correspondance midi
			} else if ("version".equals(key)) { //$NON-NLS-1$
				logger.debug("version " + value); //$NON-NLS-1$
			} else if ("classification".equals(key)) { //$NON-NLS-1$
				if ("".equals(value)) //$NON-NLS-1$
					value = null;
				currentclassification = value;
			} else if ("contact".equals(key)) { //$NON-NLS-1$
				if ("".equals(value)) //$NON-NLS-1$
					value = null;

				this.contact = value;

			} else if ("state".equals(key)) { //$NON-NLS-1$
				if ("".equals(value)) //$NON-NLS-1$
					value = null;

				this.state = value;

			} else {
				// c'est une note, on lit la note ...
				int nopiste = Integer.parseInt(key);

				AbstractTrackDef d = null;
				// on regarde si la définition est une percussion
				if (value.toLowerCase().startsWith("p")) { //$NON-NLS-1$

					// on decoupe les éléments de la ligne (pour la lecture du
					// retard et de la longueur)

					String[] values = value.split(","); //$NON-NLS-1$

					// c'est une percussion
					// on lit le nombre acollé au "P"
					String nopercu = values[0].substring(1);
					int codepercu = Integer.parseInt(nopercu);

					double retard = Double.NaN;
					double longueur = Double.NaN;

					if (values.length > 1) {
						retard = Double.parseDouble(values[1]);
					}

					if (values.length > 2) {
						longueur = Double.parseDouble(values[2]);
					}

					notemidi[nopiste] = new PercussionDef(codepercu, retard,
							longueur);

				} else {
					// c'est une note
					int codenotemidi = MidiHelper.midiCode(value);

//					if (version == 0)
//						codenotemidi -= 12; // dans la version 0, il y a un
//					// problème d'octave
//					// non c'est moi qui merde !!!

					NoteDef nd = new NoteDef(codenotemidi,
							currentclassification);
					notemidi[nopiste] = nd;
				}

				logger.debug(nopiste + "-" + (d != null ? d.toString() : "")); //$NON-NLS-1$ //$NON-NLS-2$

			}
		} catch (Exception ex) {
			throw new ScaleException("Erreur ligne " + line, ex); //$NON-NLS-1$
		}
	}

	public double getEntrepiste() {
		return entrepiste;
	}

	public double getFirsttrack() {
		return firsttrack;
	}

	public double getLargeurpiste() {
		return largeurpiste;
	}

	public int getNbpistes() {
		return nbpistes;
	}

	public AbstractTrackDef[] getNotemidi() {
		return notemidi;
	}

	public double getWidth() {
		return width;
	}

	public String getName() {
		return name;
	}

	public double getVitesse() {
		return speed;
	}

	public String getState() {
		return this.state;
	}

	public String getContact() {
		return this.contact;
	}
}
