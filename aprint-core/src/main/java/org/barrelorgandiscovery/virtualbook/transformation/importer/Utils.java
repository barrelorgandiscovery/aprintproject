package org.barrelorgandiscovery.virtualbook.transformation.importer;

import org.barrelorgandiscovery.virtualbook.transformation.LinearTransposition;
import org.barrelorgandiscovery.virtualbook.transformation.importer.processor.MidiNoteGroupIsolator;
import org.barrelorgandiscovery.virtualbook.transformation.importer.processor.NoteTrackIsolator;

/**
 * Utility Class for scripts
 */
public class Utils {

	/**
	 * Extrait les notes appartenant à un track midi spécifique
	 * 
	 * @param track
	 *            le track souhaité
	 * @param group
	 *            les notes midi associées
	 * @return
	 */
	public static MidiEventGroup extractTrack(int track, MidiEventGroup group) {
		MidiNoteGroupIsolator p = new MidiNoteGroupIsolator();
		return p.isole(new NoteTrackIsolator(track), group);
	}

	public static LinearMidiImporter linearToMidiImporter(LinearTransposition trans) {
		LinearMidiImporter li = new LinearMidiImporter(trans.getScaleDestination());
	
		for (int i = 0; i < trans.getScaleSource().getTrackNb(); i++) {
			int[] corresp = trans.getAllCorrespondances(i);
			if (corresp != null) {
				if (i <= 128) {
					for (int j = 0; j < corresp.length; j++) {
						li.mapNote(i, corresp[j]);
					}
	
				} else {
					for (int j = 0; j < corresp.length; j++) {
						li.mapPercussion(i - 128, corresp[j]);
					}
	
				}
			}
		}
		return li;
	}

}
