package org.barrelorgandiscovery.scale.comparator;

import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.MidiHelper;

/**
 * Compare the tracks number and notes equivalence
 * 
 * @author use
 * 
 */
public class TracksAndPositionedNoteComparator extends TracksNumberComparator {

	@Override
	public boolean compare(Scale s1, Scale s2) {
		assert s1 != null;
		assert s2 != null;

		AbstractTrackDef[] td1 = s1.getTracksDefinition();
		AbstractTrackDef[] td2 = s2.getTracksDefinition();

		for (int i = 0; i < td1.length; i++) {
			if (td1[i] != null) {
				if (td2.length > i && td2[i] != null) {

					if (td1[i] instanceof NoteDef) {

						if (td2[i] instanceof NoteDef) {

							NoteDef nd1 = (NoteDef) td1[i];
							NoteDef nd2 = (NoteDef) td2[i];

							// si pas les mêmes notes, indépendamment de la hauteur
							if (!MidiHelper.getMidiNote(nd1.getMidiNote())
									.equals(MidiHelper.getMidiNote(nd2.getMidiNote()))) {
								return false;
							}
						} else {
							// td2 pas notedef, alors qu'il devrai
							return false;
						}
					}
				}
			}

		}

		return true;
	}
}
