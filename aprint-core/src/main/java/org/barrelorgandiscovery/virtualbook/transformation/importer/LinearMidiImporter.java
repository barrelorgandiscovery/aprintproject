package org.barrelorgandiscovery.virtualbook.transformation.importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.MidiHelper;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.SignatureEvent;
import org.barrelorgandiscovery.virtualbook.TempoChangeEvent;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class LinearMidiImporter implements MidiImporter {

	private static Logger logger = Logger.getLogger(LinearMidiImporter.class);

	private Scale destinationgamme;

	public LinearMidiImporter(Scale destinationgamme) {
		assert destinationgamme != null;
		this.destinationgamme = destinationgamme;
	}

	// ///////////////////////////////////////////////////////
	// méthodes de définition de la transposition

	/**
	 * map a midinote to a track number
	 * 
	 * @param midinote
	 *            the midicode
	 * @param the
	 *            track number
	 */
	public void mapNote(int midinote, int pisteno) {
		addMapping(mappingnote, midinote, pisteno);
	}

	/**
	 * map a midinote as string to a
	 * 
	 * @param midinote
	 *            the midinode "A4" for example
	 * @param pisteno
	 *            the track number
	 * @throws Exception
	 */
	public void mapNote(String midinote, int pisteno) throws Exception {
		addMapping(mappingnote, MidiHelper.midiCode(midinote), pisteno);
	}

	/**
	 * Map a midi drum to a track
	 * 
	 * @param midipercussion
	 * @param pisteno
	 */
	public void mapPercussion(int midipercussion, int pisteno) {
		addMapping(mappingpercussion, midipercussion, pisteno);
	}

	/**
	 * Map a midi note as a percussion that is not in channel 9
	 * 
	 * @param midinote
	 * @param pisteno
	 */
	public void mapNoteForPercussion(int midinote, int pisteno) {
		addMapping(mappingpercussion, midinote, pisteno);
		notesMappedForPercussion.add(midinote);
	}

	/**
	 * Mapping des notes
	 */
	private HashMap<Integer, Integer[]> mappingnote = new HashMap<Integer, Integer[]>();

	/**
	 * Mapping des percussions
	 */
	private HashMap<Integer, Integer[]> mappingpercussion = new HashMap<Integer, Integer[]>();

	/**
	 * Notes that not belong to drum channel, that must be treated as percussion
	 * with delay of fixed elements
	 */
	private Set<Integer> notesMappedForPercussion = new TreeSet<Integer>();

	private void addMapping(HashMap<Integer, Integer[]> mapping, int i, int j) {
		Integer[] r = mapping.get(Integer.valueOf(i));
		if (r == null)
			r = new Integer[0];

		Integer[] n = new Integer[r.length + 1];
		for (int k = 0; k < r.length; k++) {
			n[k] = r[k];
		}

		// old code (back port)
		// Arrays.copyOf(r, r.length + 1);

		n[r.length] = Integer.valueOf(j);

		mapping.put(Integer.valueOf(i), n);
	}

	private void shiftMapping(HashMap<Integer, Integer[]> mapping, int i) {
		Integer[] r = mapping.get(Integer.valueOf(i));
		if (r == null || r.length == 0)
			return;

		Integer first = r[0];
		for (int j = 1; j < r.length; j++) {
			r[j - 1] = r[j];
		}

		r[r.length - 1] = first;

		mapping.put(Integer.valueOf(i), r);
	}

	private int getFirst(HashMap<Integer, Integer[]> mapping, int i) {
		Integer[] r = mapping.get(Integer.valueOf(i));
		assert r != null;
		assert r.length > 0;
		return r[0];
	}

	private boolean hasMapping(HashMap<Integer, Integer[]> mapping, int i) {
		return mapping.get(Integer.valueOf(i)) != null;
	}

	public MidiConversionResult convert(MidiEventGroup midigroup) {

		logger.debug("convert");

		MidiConversionResult retvalue = new MidiConversionResult();

		VirtualBook cv = new VirtualBook(destinationgamme);
		ArrayList<MidiConversionProblem> issues = new ArrayList<MidiConversionProblem>();

		for (MidiAdvancedEvent e : midigroup) {
			if (e instanceof MidiNote) {
				MidiNote mn = (MidiNote) e;

				if (mn.getChannel() == 9
						|| notesMappedForPercussion.contains(Integer.valueOf(mn
								.getMidiNote()))) {

					// notes handled as percussions
					logger.debug("Percussion " + mn); //$NON-NLS-1$
					if (hasMapping(mappingpercussion, mn.getMidiNote())) {

						int piste = getFirst(mappingpercussion,
								mn.getMidiNote());

						// if the piste no is not a percussion def ... we can't
						// shift with properties

						long timestamp = mn.getTimeStamp();

						long longueur = mn.getLength();

						AbstractTrackDef abstractTrackDef = destinationgamme
								.getTracksDefinition()[piste];
						if (abstractTrackDef instanceof PercussionDef) {
							PercussionDef pd = (PercussionDef) abstractTrackDef;

							if (!Double.isNaN(pd.getRetard())) {
								// on a un retard dans la gamme d'origine ...

								long decalage = destinationgamme.mmToTime(pd
										.getRetard());
								timestamp -= decalage; // on corrige le tire
							}

							if (!Double.isNaN(pd.getLength())) {
								// on a une longueur imposée pour la percussion
								long calculatedlength = destinationgamme
										.mmToTime(pd.getLength());
								longueur = calculatedlength;
							}
						} else {
							logger.warn("error track "
									+ piste
									+ " is not a percussion def, so we can't adjust the properties for delay and fixed length");
							issues.add(new MidiDrumTransformationIsNotProperlyDefined(
									piste));
						}

						// Création du trou ... en prenant en compte le retard
						Hole h = new Hole(piste, timestamp, longueur);
						cv.addHole(h);
						shiftMapping(mappingpercussion, mn.getMidiNote());

					} else {
						logger.debug("percussion :" + mn + " has no mapping"); //$NON-NLS-1$ //$NON-NLS-2$
						issues.add(new MidiMappingNotFound(mn));
					}

				} else {
					logger.debug("Note " + mn); //$NON-NLS-1$
					if (hasMapping(mappingnote, mn.getMidiNote())) {
						// Création du trou ...
						Hole h = new Hole(getFirst(mappingnote,
								mn.getMidiNote()), mn.getTimeStamp(),
								mn.getLength());
						cv.addHole(h);
						shiftMapping(mappingnote, mn.getMidiNote());

					} else {
						logger.debug("note :" + mn + " has no mapping"); //$NON-NLS-1$ //$NON-NLS-2$
						issues.add(new MidiMappingNotFound(mn));
					}
				}

			} else if (e instanceof MidiSignatureEvent) {

				MidiSignatureEvent sigEvent = (MidiSignatureEvent) e;

				SignatureEvent vbSigEvent = new SignatureEvent(
						sigEvent.getTimeStamp(), sigEvent.getNumerator(),
						sigEvent.getDenominator());

				cv.addEvent(vbSigEvent);

			} else if (e instanceof MidiTempoEvent) {
				MidiTempoEvent tevent = (MidiTempoEvent) e;

				TempoChangeEvent tce = new TempoChangeEvent(
						tevent.getTimeStamp(), tevent.getBeatLength());

				cv.addEvent(tce);

			} else {
				logger.warn("event " + e + " not treated"); //$NON-NLS-1$ //$NON-NLS-2$
				issues.add(new MidiUncoveredEvent(e));
			}
		}

		retvalue.virtualbook = cv;
		retvalue.issues = null;

		if (issues.size() > 0) {
			retvalue.issues = issues;
		}
		return retvalue;
	}

	public Integer[] getNoteMapping(int midiNote) {
		return mappingnote.get(midiNote);
	}

	public Integer[] getPercussionMapping(int midipercussion) {
		return mappingpercussion.get(midipercussion);
	}

}
