package org.barrelorgandiscovery.scale;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.io.ScaleIO;
import org.barrelorgandiscovery.tools.StringTools;
import org.barrelorgandiscovery.tools.streamstorage.StreamStorage;


/**
 * Storage Manager for scales
 * 
 * @author Freydiere Patrice
 * 
 */
public class StorageScaleManager implements ScaleManager {

	private static Logger logger = Logger.getLogger(StorageScaleManager.class);

	/**
	 * liste des gammes présentes dans le répertoire de gammes
	 */
	private HashMap<String, Scale> scales = new HashMap<String, Scale>();

	private StreamStorage iss;

	public StorageScaleManager(StreamStorage iss) {

		// Lecture des gammes contenues dans le répertoire

		String[] listefichiersgamme = iss.listStreams("gamme"); //$NON-NLS-1$

		for (int i = 0; i < listefichiersgamme.length; i++) {

			String f = listefichiersgamme[i];

			// Lecture du fichier de gamme
			System.out.println(Messages.getString("StorageScaleManager.1") + f); //$NON-NLS-1$

			try {
				Scale g = ScaleIO.readGamme(iss.openStream(f));

				// Mémorisation de la Gamme ...
				scales.put(g.getName(), g);

			} catch (ScaleException ex) {
				System.err.println(Messages.getString("StorageScaleManager.2") //$NON-NLS-1$
						+ f);
				ex.printStackTrace(System.err);
				logger.error(ex);
			} catch (IOException ex) {
				System.err.println(Messages.getString("StorageScaleManager.3") //$NON-NLS-1$
						+ f);
				ex.printStackTrace(System.err);
				logger.error(ex);
			}
		}

		// ajout de la gamme midi ... toujours presente dans la collection ...
		Scale midi = Scale.getGammeMidiInstance();
		scales.put(midi.getName(), midi);

		this.iss = iss;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.gamme.GammeManager#getNames()
	 */
	public String[] getScaleNames() {
		Vector<String> v = new Vector<String>();
		Iterator<String> it = scales.keySet().iterator();

		while (it.hasNext()) {
			String name = it.next();
			v.add(name);
		}

		// Liste les clefs des gammes ...
		String[] retvalue = new String[v.size()];
		v.copyInto(retvalue);
		return retvalue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.gamme.GammeManager#getGamme(java.lang.String)
	 */
	public Scale getScale(String name) {
		if (scales.containsKey(name))
			return scales.get(name);
		return null; // non trouvée
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.scale.ScaleManager#deleteScale(fr.freydierepatrice.scale.Scale)
	 */
	public void deleteScale(Scale scale) throws Exception {

		String streamname = StringTools.convertToPhysicalNameWithEndingHashCode(scale.getName());

		iss.deleteStream(streamname, "scale"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.scale.ScaleManager#saveScale(fr.freydierepatrice.scale.Scale)
	 */
	public void saveScale(Scale scale) throws Exception {

		String streamname = StringTools.convertToPhysicalNameWithEndingHashCode(scale.getName());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ScaleIO.writeGamme(scale, baos);
		baos.close();
		iss.saveStream(streamname, "gamme", new ByteArrayInputStream(baos //$NON-NLS-1$
				.toByteArray()));

		// ok no error in saving ...
		scales.put(scale.getName(), scale);

	}
}
