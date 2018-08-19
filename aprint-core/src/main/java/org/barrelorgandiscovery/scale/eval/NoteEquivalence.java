package org.barrelorgandiscovery.scale.eval;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.Diff;
import org.barrelorgandiscovery.tools.Difference;

public class NoteEquivalence {

	private static Logger logger = Logger.getLogger(NoteEquivalence.class);

	/**
	 * find the equivalence, this method return the number of mismatch
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public int findEquivalence(Scale s1, Scale s2) throws Exception {

		Set<Note> l1 = noteDefToMidiCodes(s1);
		int cpt = 0;
		for (Iterator iterator = l1.iterator(); iterator.hasNext();) {
			Note integer = (Note) iterator.next();
			logger.debug("l1(" + (cpt++) + ") :" + integer);
		}

		Set<Note> l2 = noteDefToMidiCodes(s2);
		cpt = 0;
		for (Iterator iterator = l2.iterator(); iterator.hasNext();) {
			Note integer = (Note) iterator.next();
			logger.debug("l2(" + (cpt++) + ") :" + integer);
		}

		computeDifference(l1, l2);
		return 0;

	}

	/**
	 * 
	 * @param l1
	 * @param l2
	 */
	private void computeDifference(Set<Note> l1, Set<Note> l2) {
		Diff<Note> diff = new Diff<Note>(new ArrayList<Note>(l1),
				new ArrayList<Note>(l2));

		List<Difference> ld = diff.diff();
		for (Iterator iterator = ld.iterator(); iterator.hasNext();) {
			Difference difference = (Difference) iterator.next();
			logger.debug("Difference :" + difference);
		}
	}

	/**
	 * convert the notedef tracks to midicodes
	 * 
	 * @param s
	 * @return
	 */
	private Set<Note> noteDefToMidiCodes(Scale s) {

		Set<Note> midicodes = new TreeSet<Note>();
		AbstractTrackDef[] tds = s.getTracksDefinition();
		for (int i = 0; i < tds.length; i++) {
			AbstractTrackDef abstractTrackDef = tds[i];
			if (abstractTrackDef instanceof NoteDef) {
				NoteDef nd = (NoteDef) abstractTrackDef;
				midicodes.add(new Note(nd.getMidiNote(), nd
						.getRegisterSetName()));
			}
		}

		return midicodes;
	}

	public static void main(String[] args) {
		// test method ...

	}

}
