package org.barrelorgandiscovery.scale;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Vector;

public class ReferencedPercussionList {

	private static String[] correspondancelist = new String[] {
			"35 Acoustic Bass Drum", "59 Ride Cymbal 2", "36 Bass Drum 1", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"60 Hi Bongo", "37 Side Stick", "61	Low Bongo", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"38 Acoustic Snare", "62 Mute Hi Conga", "39	Hand Clap", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"63 Open Hi Conga", "40	Electric Snare", "64 Low Conga", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"41 Low Floor Tom", "65	High Timbale", "42 Closed Hi-Hat", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"66 Low Timbale", "43 High Floor Tom", "67 High Agogo", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"44 Pedal Hi-Hat", "68 Low Agogo", "45 Low Tom", "69 Cabasa", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"46 Open Hi-Hat", "70 Maracas", "47	Low-Mid Tom", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"71 Short Whistle", "48	Hi-Mid Tom", "72 Long Whistle", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"49 Crash Cymbal 1", "73 Short Guiro", "50 High Tom", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"74 Long Guiro", "51 Ride Cymbal 1", "75 Claves", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"52 Chinese Cymbal", "76 Hi Wood Block", "53 Ride Bell", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"77 Low Wood Block", "54 Tambourine", "78 Mute Cuica", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"55 Splash Cymbal", "79 Open Cuica", "56 Cowbell", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"80 Mute Triangle", "57	Crash Cymbal 2", "81 Open Triangle", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"58 Vibraslap" }; //$NON-NLS-1$

	/**
	 * retourne la liste des percussions référencées
	 * 
	 * @return
	 */
	public static ReferencedPercussion[] getReferencedPercussion() {

		Vector<ReferencedPercussion> p = new Vector<ReferencedPercussion>();

		for (int i = 0; i < correspondancelist.length; i++) {
			String stringpercu = correspondancelist[i];

			int midicode = Integer.parseInt(stringpercu.substring(0, 2));
			String englishlabel = stringpercu.substring(3);
			String name = englishlabel.toUpperCase().replace(" ", "_"); //$NON-NLS-1$ //$NON-NLS-2$

			p.add(new ReferencedPercussion(midicode, name));

		}

		ReferencedPercussion[] retvalue = new ReferencedPercussion[p.size()];
		p.copyInto(retvalue);

		return retvalue;

	}

	/**
	 * Fill the percussion translation in the message file ...
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		// open the messages.properties

		Properties p = new Properties();
		File messagefile = new File(
				"src/fr/freydierepatrice/messages/messages.properties");
		FileInputStream fis = new FileInputStream(messagefile);
		try {
			p.load(fis);
		} finally {
			fis.close();
		}

		for (int i = 0; i < correspondancelist.length; i++) {
			String stringpercu = correspondancelist[i];

			int midicode = Integer.parseInt(stringpercu.substring(0, 2));

			String englishlabel = stringpercu.substring(3);
			String name = englishlabel.toUpperCase().replace(" ", "_"); //$NON-NLS-1$ //$NON-NLS-2$

			p.setProperty("Drum." + name, englishlabel);

		}

		p.list(System.out);

		FileOutputStream fos = new FileOutputStream(messagefile);
		p.save(fos, "Default messages");

		fos.close();
	}

	/**
	 * Search the midi code associated to the name
	 * 
	 * @param name
	 * @return
	 */
	public static int findReferencePercussionByName(String name) {
		ReferencedPercussion[] l = getReferencedPercussion();
		for (int i = 0; i < l.length; i++) {
			ReferencedPercussion referencedPercussion = l[i];
			if (referencedPercussion.getNamecode().equalsIgnoreCase(name))
				return referencedPercussion.getMidicode();
		}
		return -1;
	}

	/**
	 * 
	 * Search for a drum from the midi code
	 * 
	 * @param midicode
	 *            midi code
	 * @return null if not found
	 */
	public static ReferencedPercussion findReferencedPercussionByMidiCode(
			int midicode) {
		ReferencedPercussion[] l = getReferencedPercussion();
		for (int i = 0; i < l.length; i++) {
			ReferencedPercussion referencedPercussion = l[i];
			if (referencedPercussion.getMidicode() == midicode)
				return referencedPercussion;
		}
		return null;
	}

}
