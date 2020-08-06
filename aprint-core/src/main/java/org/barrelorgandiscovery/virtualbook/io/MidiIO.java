package org.barrelorgandiscovery.virtualbook.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.issues.AbstractIssue;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.SignatureEvent;
import org.barrelorgandiscovery.virtualbook.TempoChangeEvent;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.transformation.importer.AbstractMidiEventVisitor;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiControlChange;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFile;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFileIO;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFileReadError;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFileReadResult;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiGenericEvent;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiNote;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiProgramChange;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiSignatureEvent;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiTempoEvent;

public class MidiIO {

	private static Logger logger = Logger.getLogger(MidiIO.class);

	private static class MidiVBVisitor extends AbstractMidiEventVisitor {

		private VirtualBook vb;

		public MidiVBVisitor(VirtualBook vb) {
			this.vb = vb;
		}

		@Override
		public void visit(MidiGenericEvent midiGenericEvent) throws Exception {
			logger.warn("event : " + midiGenericEvent + " not added");
		}

		@Override
		public void visit(MidiNote mn) throws Exception {

			int midicode = mn.getMidiNote();
			if (mn.getChannel() == 9) {
				// percussion, on augmente de 128 le midicode car c'est
				// encodé comme cela sur la gamme midi ....
				midicode += 128;
			}

			vb.addHole(new Hole(midicode, mn.getTimeStamp(), mn.getLength()));

		}

		@Override
		public void visit(MidiSignatureEvent midiSignatureEvent) throws Exception {

			vb.addEvent(new SignatureEvent(midiSignatureEvent.getTimeStamp(), midiSignatureEvent.getNumerator(),
					midiSignatureEvent.getDenominator()));

		}

		@Override
		public void visit(MidiTempoEvent midiTempoEvent) throws Exception {

			vb.addEvent(new TempoChangeEvent(midiTempoEvent.getTimeStamp(), midiTempoEvent.getBeatLength()));

		}

		@Override
		public void visit(MidiControlChange midiControlChangeEvent) throws Exception {
			// Nothing

		}

		@Override
		public void visit(MidiProgramChange midiProgramChange) throws Exception {
			// Nothing

		}

	}

	/**
	 * read midi stream and returned the analyzed midi object
	 * 
	 * @param midiinputstream
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public static MidiIOResult readCartonWithError(InputStream midiinputstream, String filename) throws Exception {
		// Lecture des sequences
		VirtualBook c = new VirtualBook(Scale.getGammeMidiInstance());

		// définition du nom du carton, provenant du nom du fichier midi
		c.setName(filename);

		MidiFileReadResult readWithError = MidiFileIO.readWithError(midiinputstream);

		MidiFile midifileread = readWithError.midiFile;

		MidiVBVisitor midiVBVisitor = new MidiVBVisitor(c);

		midifileread.visitEvents(midiVBVisitor);

		// c is filled by the events

		MidiIOResult result = new MidiIOResult();
		result.virtualBook = c;
		result.issues = new ArrayList<AbstractIssue>();

		for (Iterator iterator = readWithError.errors.iterator(); iterator.hasNext();) {
			MidiFileReadError e = (MidiFileReadError) iterator.next();
			if (e == null)
				continue;
			result.issues.add(e.toIssue());

		}

		return result;

	}

	public static MidiIOResult readCartonWithError(File midifile) throws Exception {
		assert midifile != null;
		return readCartonWithError(new FileInputStream(midifile), midifile.getName());
	}

	/**
	 * new function for reading the midi files ...
	 * 
	 * @param midifile
	 * @return
	 * @throws MidiIOException
	 * @throws InvalidMidiDataException
	 * @throws IOException
	 */
	public static VirtualBook readCarton(File midifile) throws Exception {
		MidiIOResult readCartonWithError = readCartonWithError(midifile);
		return readCartonWithError.virtualBook;
	}

	/**
	 * read midi file from input stream
	 * 
	 * @param midiInputStream
	 * @return
	 * @throws Exception
	 */
	public static VirtualBook readCarton(InputStream midiInputStream, String filename) throws Exception {
		MidiIOResult readCartonWithError = readCartonWithError(midiInputStream, filename);
		return readCartonWithError.virtualBook;
	}

	/**
	 * old function for reading a midi file ...
	 * 
	 * @param midifile la référence au fichier midi lu ...
	 * @return un objet carton virtuel
	 * @throws MidiIOException
	 */
	public static VirtualBook readCarton_old(File midifile)
			throws MidiIOException, InvalidMidiDataException, IOException {

		// Lecture des sequences
		VirtualBook c = new VirtualBook(Scale.getGammeMidiInstance());

		// définition du nom du carton, provenant du nom du fichier midi
		c.setName(midifile.getName());

		logger.debug("Reading sequence"); //$NON-NLS-1$
		Sequence seq = MidiSystem.getSequence(midifile);

		if (seq.getDivisionType() != Sequence.PPQ) {
			throw new MidiIOException("invalid tempo notification"); //$NON-NLS-1$
		}

		logger.debug("Resolution " + seq.getResolution()); //$NON-NLS-1$

		Track[] tracks = seq.getTracks();

		logger.debug("tracks length " + tracks.length); //$NON-NLS-1$

		// on fusionne tous les track dans un seul track,
		// pour résoudre les problèmes de tempo dans la lecture

		Sequence lecture = new Sequence(Sequence.PPQ, seq.getResolution());
		Track t = lecture.createTrack();
		for (int i = 0; i < tracks.length; i++) {
			Track origin = tracks[i];
			for (int j = 0; j < origin.size(); j++)
				t.add(origin.get(j));
		}

		// t contient la fusion de tous les evenements de tous les tracks ....

		// calc time resolution ...

		int resolution = seq.getResolution();

		// resolution pour un tempo à 60 bpm
		double micropertick = (double) (1000000.0 / resolution); // en micro
		// seconds

		// Lecture des tracks ..
		Track track = t;

		long currenttime = 0;

		long lasttempotime = 0;
		long lasttempotick = 0;

		long[] alltracks = new long[256];
		for (int k = 0; k < alltracks.length; k++)
			alltracks[k] = -1; // initialisation ...

		for (int j = 0; j < track.size(); j++) {
			MidiEvent me = track.get(j);
			currenttime = lasttempotime + (long) ((me.getTick() - lasttempotick) * micropertick);
			// Récupération des messages de notes
			byte[] message = me.getMessage().getMessage();
			if (message.length > 0) {
				// on a des éléments dans le message ...

				int canal = message[0] & 0x0F;

				if ((message[0] >> 4 & 0x0F) == 8) {
					// System.out.println("Note Off");

					int note = (message[1] & 0xFF);
					if (canal == 9)
						note += 128;

					if (alltracks[note] != -1) {
						c.addHole(new Hole(note, alltracks[note], currenttime - alltracks[note]));
						alltracks[note] = -1;
					}

				} else if ((message[0] >> 4 & 0x0F) == 9) {

					// note on
					int note = (message[1] & 0xFF);
					if (canal == 9)
						note += 128;

					if ((message[2] & 0xFF) == 0) {
						// note on but velocity is equals 0
						// so this is a note off, same as above
						if (alltracks[note] != -1) {
							c.addHole(new Hole(note, alltracks[note], currenttime - alltracks[note]));
							alltracks[note] = -1;
						}
					} else {
						// note on
						alltracks[note] = currenttime;
					}

				} else if ((message[0] & 0xFF) == 0xFF) {
					// tempo
					if ((message[1] & 0xFF) == 0x51) {

						long l = ((message[3] & 0xFF) << 16 | (message[4] & 0xFF) << 8 | (message[5] & 0xFF))
								& 0xFFFFFF;
						micropertick = (l * 1.0) / resolution;

						lasttempotime = currenttime;
						lasttempotick = me.getTick();

						c.addEvent(new TempoChangeEvent(currenttime, l));

						logger.debug("tempo change " + micropertick); //$NON-NLS-1$
					} else if ((message[1] & 0xFF) == 0x58) {
						// Traitement du time signature ...

						byte numerateur = message[3];
						byte denominateur = message[4];

						logger.debug("signature change " + numerateur + "/" //$NON-NLS-1$ //$NON-NLS-2$
								+ denominateur);

						c.addEvent(new SignatureEvent(currenttime, numerateur, denominateur));

					}

					else {
						logger.debug("Message non traité " + me.getMessage() //$NON-NLS-1$
								+ " service " + message[1]); //$NON-NLS-1$
					}
				}

			} // message.length > 0

		}

		return c;
	}

	public static void main(String[] args) {
		try {

			BasicConfigurator.configure(new LF5Appender());

			VirtualBook c =

					MidiIO.readCarton(new File(
							"C:\\Documents and Settings\\Freydiere Patrice\\workspace\\APrint\\fichier midi test\\Les flots du danube (2007 08 08) .mid")); //$NON-NLS-1$

			System.out.println(c.toString());

		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

}
