package org.barrelorgandiscovery.virtualbook.transformation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.Scale;


/**
 * utility class for writing transposition to disk
 * 
 * @author Freydiere Patrice
 * 
 */
public class TranspositionIO {

	private static Logger logger = Logger.getLogger(TranspositionIO.class);

	/**
	 * Create a defaut translation for a scale, the default translation is
	 * builtin ...
	 * 
	 * @param g
	 * @return
	 */
	public static LinearTransposition createDefaultMidiTransposition(
			Scale g) throws TranspositionException {

		assert g != null;

		LinearTransposition lt = new LinearTransposition(Scale
				.getGammeMidiInstance(), g, "Midi vers " + g.getName(), true, true);

		AbstractTrackDef[] td = g.getTracksDefinition();
		for (int i = 0; i < td.length; i++) {
			AbstractTrackDef t = td[i];
			if (t != null) {
				if (t instanceof NoteDef) {
					NoteDef n = (NoteDef) t;
					lt.setCorrespondance(n.getMidiNote(), i);

				} else if (t instanceof PercussionDef) {
					PercussionDef p = (PercussionDef) t;
					lt.setCorrespondance(128 + p.getPercussion(), i);
				}
			}
		}

		return lt;
	}

	private static void writeKeyValue(Writer w, String key, String value)
			throws IOException {
		w.write(key + "=" + value + "\n");
	}

	public static void writeLinearTransposition(LinearTransposition lt,
			File fichier) throws IOException {

		FileOutputStream fileOutputStream = new FileOutputStream(fichier);
		try {
			writeLinearTransposition(lt, fileOutputStream);
		} finally {
			fileOutputStream.close();
		}
	}

	/**
	 * Ecriture de la transposition dans le fichier spécifié en paramètres
	 * 
	 * @param lt
	 * @param fichier
	 */
	public static void writeLinearTransposition(LinearTransposition lt,
			OutputStream stream) throws IOException {

		// création du fichier ...
		Writer w = new OutputStreamWriter(stream);
		try {

			Scale gsource = lt.getScaleSource();

			w.write("# Fichier de transposition midi ->  " + gsource.getName()
					+ "\n");

			w.write("# transposition description \n");

			writeKeyValue(w, "name", lt.getName());

			writeKeyValue(w, "gammesource", lt.getScaleSource().getName());

			writeKeyValue(w, "gammedestination", lt.getScaleDestination()
					.getName());

			for (int i = 0; i < gsource.getTrackNb(); i++) {

				logger.debug("source track :" + i);
				int[] ai = lt.getAllCorrespondances(i);
				logger.debug("correspondance :" + ai);
				if (ai != null && ai.length > 0) {
					// Ecriture des éléments ....

					String r = "";
					for (int j = 0; j < ai.length; j++) {
						if (r.length() > 0)
							r += ",";

						r += "" + ai[j];
					}

					writeKeyValue(w, "" + i, r);

				}

			}

		} finally {
			w.close();
		}

	}

}
