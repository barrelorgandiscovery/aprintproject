package org.barrelorgandiscovery.listeningconverter;

import java.util.Iterator;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * First version of Virtual Book to Midi Conversion
 * @author use
 *
 */
public class EcouteConverter {

	private static Logger logger = Logger.getLogger(EcouteConverter.class);

	public static Sequence convert(VirtualBook carton1)
			throws InvalidMidiDataException {
		return convert(carton1, 384);
	}

	public static Sequence convert(VirtualBook carton1, int ticksperbeat)
			throws InvalidMidiDataException {
		return convert(carton1, ticksperbeat, 1);
	}

	/**
	 * fonction permettant de transformer un carton virtuel, en sequence midi
	 * pouvant être jouée.
	 * 
	 * les notes se touchant sont fusionnées.
	 * 
	 * @param carton
	 *            le carton virtuel
	 * @return la sequence midi
	 * @throws InvalidMidiDataException
	 */
	public static Sequence convert(VirtualBook carton1, int ticksperbeat,
			int midicanal) throws InvalidMidiDataException {

		/**
		 * Fusion des notes dédoublées
		 */

		long micropertick = (long) (10000.0 / 384.0 * ticksperbeat);

		logger.debug("merge all overlapping holes");
		VirtualBook carton = new VirtualBook(carton1.getScale());
		for (Iterator<Hole> iterator = carton1.getOrderedHolesCopy().iterator(); iterator
				.hasNext();) {
			Hole h = iterator.next();
			carton.addAndMerge(h);
		}

		logger.debug("convert");

		Sequence seq = new Sequence(Sequence.PPQ, ticksperbeat);
		Track track = seq.createTrack();

		logger.debug("micropertick " + micropertick);

		long l = micropertick * seq.getResolution();

		byte[] b = { (byte) ((l >> 16) & 0xFF), (byte) ((l >> 8) & 0xFF),
				(byte) (l & 0xFF) };

		// Ajout du message de tempo ....

		MetaMessage mtm = new MetaMessage();
		mtm.setMessage(0x51, b, b.length);
		MidiEvent mt = new MidiEvent(mtm, 0);

		logger.debug("add a track");
		track.add(mt);

		List<Hole> notes = carton.getOrderedHolesCopy();

		for (int i = 0; i < notes.size(); i++) {
			Hole n = notes.get(i);

			// conversion en note midi ...

			AbstractTrackDef nd = carton.getScale().getTracksDefinition()[n
					.getTrack()];

			if (nd != null) {

				long decalage = 0;

				if (nd instanceof PercussionDef || nd instanceof NoteDef) {
					if (nd instanceof PercussionDef) {
						PercussionDef d = (PercussionDef) nd;

						// on prends en charge le retard !!
						if (!Double.isNaN(d.getRetard())) {
							// on a un retard dans la gamme d'origine ...

							decalage = (long) (d.getRetard()
									/ carton.getScale().getSpeed() * 1000000);
						}

					}

					int notemidi = (nd instanceof NoteDef) ? ((NoteDef) nd)
							.getMidiNote() : ((PercussionDef) nd)
							.getPercussion();
					int canal = (nd instanceof PercussionDef) ? 9 : midicanal;

					if (notemidi != -1 || n.getTimeLength() == 0) {

						long startTick = (n.getTimestamp() + decalage)
								/ micropertick;
						
						long endTick = (n.getTimestamp() + n.getTimeLength() + decalage)
								/ micropertick;
						
						
						if (nd instanceof PercussionDef)
						{
							// force the length for percussions
							endTick = (n.getTimestamp() + decalage + 2000000) / micropertick;
						}
						
						// sanitize
						assert startTick <= endTick;
						if (startTick == endTick)
							continue;
						
						ShortMessage mm = new ShortMessage();

						mm.setMessage(0x90, canal, notemidi, 127);
						MidiEvent me = new MidiEvent(mm, startTick);

						track.add(me);

						
						mm = new ShortMessage();
						mm.setMessage(0x80, canal, notemidi, 127);

						me = new MidiEvent(mm, endTick);
						track.add(me);
					}
				}

			} else {
				System.out.println("track " + n.getTrack()
						+ " cannot be converted");

			}
		}
		return seq;

	}
}
