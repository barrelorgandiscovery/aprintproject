package org.barrelorgandiscovery.virtualbook.transformation.importer;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Modélisation d'un fichier midi
 * 
 * @author Freydiere Patrice
 */
public class MidiFile extends MidiEventGroup {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5620023445769491997L;

	public MidiFile() {
		super();
	}

	/**
	 * Watch for every note, to know in which track it is stored
	 * 
	 * @return
	 */
	public int[] listTracks() {
		Set<Integer> s = new TreeSet<Integer>();

		// recherche la liste des tracks utilisés ...
		for (MidiAdvancedEvent e : this) {
			if (e instanceof MidiNote) {
				MidiNote mn = (MidiNote) e;
				s.add(Integer.valueOf(mn.getTrack()));
			}
		}

		int[] retvalue = new int[s.size()];
		int cpt = 0;
		for (Integer i : s) {
			retvalue[cpt++] = i.intValue();
		}

		return retvalue;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("MidiFile \n");
		for (MidiAdvancedEvent e : this) {
			sb.append(e.toString()).append("\n");
		}
		return sb.toString();
	}

	/**
	 * Visitor pattern for the midi events
	 * 
	 * @param visitor
	 * @throws Exception
	 */
	public void visitEvents(AbstractMidiEventVisitor visitor) throws Exception {
		for (MidiAdvancedEvent e : this) {
			e.visit(visitor);
		}
	}

}
