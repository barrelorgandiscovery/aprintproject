package org.barrelorgandiscovery.virtualbook.transformation.importer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.BinaryUtils;
import org.barrelorgandiscovery.tools.MidiHelper;
import org.barrelorgandiscovery.tools.StreamsTools;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFileReadError.ErrorCode;

/**
 * Class for MidiFile Input / Output
 * 
 * @author use
 * 
 */
public class MidiFileIO {

	/**
	 * Logger
	 */
	private static Logger logger = Logger.getLogger(MidiFileIO.class);

	private static class PrivateTrackedMidiEvent implements
			Comparable<PrivateTrackedMidiEvent> {
		public MidiEvent me;
		public int track;

		public int compareTo(PrivateTrackedMidiEvent o) {

			return comparePrivate(this, o);
		}
	}

	/**
	 * Compare 2 midi files
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	private static int comparePrivate(PrivateTrackedMidiEvent o1,
			PrivateTrackedMidiEvent o2) {

		if (o1 == o2)
			return 0;

		MidiEvent midiEvent1 = o1.me;
		long firstTick = midiEvent1.getTick();
		MidiEvent midiEvent2 = o2.me;
		long secondTick = midiEvent2.getTick();

		int timecompare = ((Long) firstTick).compareTo(secondTick);
		if (timecompare != 0)
			return timecompare;

		int compareTracks = ((Integer) o1.track).compareTo(o2.track);

		if (compareTracks != 0)
			return compareTracks;

		byte[] m1 = midiEvent1.getMessage().getMessage();
		byte[] m2 = midiEvent2.getMessage().getMessage();

		if (m1.length > 1 && m2.length > 1 && isNoteMessage(m1)
				&& isNoteMessage(m2)) {

			int note1 = (extractFirst(m1));
			int note2 = (extractFirst(m2));

			if (note1 != note2)
				return ((Integer) note1).compareTo(note2);

			// compare channel

			int ch1 = extractChannel(m1);
			int ch2 = extractChannel(m2);

			int compareChannel = ((Integer) ch1).compareTo(ch2);
			if (compareChannel != 0)
				return compareChannel;

			int no1 = isNoteOff(m1) ? 0 : 1;
			int no2 = isNoteOff(m2) ? 0 : 1;

			int c = ((Integer) no1).compareTo(no2);
			if (c != 0)
				return c;

			// other ....
			return (((Integer) extractSecond(m1)).compareTo(extractSecond(m2)));

		}

		int h1 = midiEvent1.hashCode();
		int h2 = midiEvent2.hashCode();

		int hashCompare = ((Integer) h1).compareTo(h2);

		if (hashCompare != 0)
			return hashCompare;

		if (!midiEvent1.equals(midiEvent2))
			return -1;

		return 0;

	}

	private static class PrivateTrackedMidiEventComparator implements
			Comparator<PrivateTrackedMidiEvent> {

		public int compare(PrivateTrackedMidiEvent o1,
				PrivateTrackedMidiEvent o2) {
			return comparePrivate(o1, o2);
		}
	}

	/**
	 * Read a midi file from a midi file ... no read error are returned
	 * 
	 * @param file
	 *            the file to read
	 * @return the result
	 * @throws Exception
	 */
	public static MidiFile read(File file) throws Exception {
		return read(new BufferedInputStream(new FileInputStream(file)));
	}

	/**
	 * Read a midi file with associated errors
	 * 
	 * @param file
	 *            the file to read
	 * @return the result of the read including errors
	 * @throws Exception
	 */
	public static MidiFileReadResult readWithError(File file) throws Exception {
		return readWithError(new BufferedInputStream(new FileInputStream(file)));
	}

	
	/**
	 * Fix some header problems in midifiles
	 * 
	 * @param content
	 * @return
	 * @throws Exception
	 */
	private static byte[] fixDecapMidiFileIfNecessary(byte[] content)
			throws Exception {

		int header = BinaryUtils.nextOccur(new byte[] { 'M', 'T', 'h', 'd' },
				content, 0);
		if (header != 0)
			throw new Exception("bad file, header not found"); //$NON-NLS-1$

		int cpt = header + 4;

		long format = BinaryUtils.readInt2Bytes(content, header + 8);

		logger.debug("midi file format :" + format); //$NON-NLS-1$

		if (format != 0) {
			return content;
		}

		logger.debug("getting the first track ... "); //$NON-NLS-1$

		int offsetTrack = BinaryUtils.nextOccur(
				new byte[] { 'M', 'T', 'r', 'k' }, content, 0);

		if (offsetTrack == -1)
			throw new Exception("no startup markup for the track ..."); //$NON-NLS-1$

		int endTrack = BinaryUtils.nextOccur(new byte[] { -1, 47, 0 }, content,
				offsetTrack + 1);

		if (endTrack == -1)
			throw new Exception("no markup for the track end ..."); //$NON-NLS-1$

		int tailleTrack = endTrack + 3 - offsetTrack - 8;

		logger.debug("computed size : " + tailleTrack); //$NON-NLS-1$

		long storedTrackSize = BinaryUtils.readInt4bytes(content,
				offsetTrack + 4);
		logger.debug("stored size :" + storedTrackSize); //$NON-NLS-1$

		if (storedTrackSize == 32810) {
			// magic number for the decad midi file

			logger.debug("fix the track length ... "); //$NON-NLS-1$
			BinaryUtils.writeInt4Byte(content, offsetTrack + 4, tailleTrack);

			logger.debug("stored new size :" //$NON-NLS-1$
					+ BinaryUtils.readInt4bytes(content, offsetTrack + 4));

		}

		return content;

	}

	/**
	 * Read the midi file ..
	 * 
	 * @param file
	 *            the file to read
	 * @return the readed MidiFile Object
	 * @throws Exception
	 */
	public static MidiFile read(InputStream is) throws Exception {
		logger.debug("read midi file from stream"); //$NON-NLS-1$
		MidiFileReadResult result = readWithError(is);

		if (logger.isDebugEnabled()) {
			ArrayList<MidiFileReadError> errors = result.errors;
			if (errors != null && errors.size() > 0) {
				logger.warn("errors in the midi files :");
				for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
					MidiFileReadError midiFileReadError = (MidiFileReadError) iterator
							.next();
					logger.warn(midiFileReadError);
				}
			}
		}

		return result.midiFile;

	}

	/**
	 * read the midi file and send the result
	 * 
	 * @param is
	 *            the stream to read
	 * @return
	 * @throws Exception
	 */
	public static MidiFileReadResult readWithError(InputStream is)
			throws Exception {

		ArrayList<MidiFileReadError> errors = new ArrayList<MidiFileReadError>();

		logger.debug("reading midi stream"); //$NON-NLS-1$
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StreamsTools.copyStream(is, baos);
		byte[] content = baos.toByteArray();

		fixDecapMidiFileIfNecessary(content);

		ByteArrayInputStream bais = new ByteArrayInputStream(content);

		logger.debug("Reading sequence"); //$NON-NLS-1$
		Sequence seq = MidiSystem.getSequence(bais);

		if (seq.getDivisionType() != Sequence.PPQ) {
			throw new Exception("invalid tempo notification"); //$NON-NLS-1$
		}

		logger.debug("Resolution " + seq.getResolution()); //$NON-NLS-1$

		// Comparator<PrivateTrackedMidiEvent> comparator = new
		// PrivateTrackedMidiEventComparator();

		// ListOrderedSet listOrderedSet = new ListOrderedSet();
		// Set<PrivateTrackedMidiEvent> allsortedevents =
		// SetUtils.orderedSet(listOrderedSet);
		//

		Collection<PrivateTrackedMidiEvent> allevents = new ArrayList<PrivateTrackedMidiEvent>();

		Track[] tracks = seq.getTracks();

		// on fusionne tous les track dans un seul track,
		// pour résoudre les problèmes de tempo dans la lecture

		logger.debug("MergeAllEvents, tracks to analyse :" + tracks.length); //$NON-NLS-1$
		mergeAllEvents(allevents, tracks);

		// t contient la fusion de tous les evenements de tous les tracks ....

		// calc time resolution ...

		// Création de l'objet midi associé
		MidiFile midifile = new MidiFile();

		logger.debug("processAllEvents"); //$NON-NLS-1$
		long[][][] alltracks = processEvents(midifile, seq, allevents, errors);

		// sanity check

		for (int k = 0; k < alltracks.length; k++)
			for (int m = 0; m < 16; m++)
				// channel
				for (int l = 0; l < alltracks.length; l++)
					if (alltracks[k][m][l] != -1)
						logger.warn("note " + l + " on track " + k + " not ended"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		MidiFileReadResult midiFileReadResult = new MidiFileReadResult();
		midiFileReadResult.midiFile = midifile;
		midiFileReadResult.errors = errors;

		return midiFileReadResult;
	}

	private static long[][][] processEvents(MidiFile midifile, Sequence seq,
			Collection<PrivateTrackedMidiEvent> allevents,
			List<MidiFileReadError> errors) {
		int ticksPerBeat = seq.getResolution();

		// resolution pour un tempo à 60 bpm
		double micropertick = (double) (1000000.0 / ticksPerBeat); // en micro
		// seconds

		long currenttime = 0;

		long lasttempotime = 0;
		long lasttempotick = 0;

		// tableau contenant l'état des différentes notes ....
		// le premier index contient le track, le second, la note
		long[][][] alltracks = new long[128][16][128];
		Integer[][][] velocityOn = new Integer[128][16][128];
		

		for (int k = 0; k < alltracks.length; k++)
			for (int m = 0; m < 16; m++)
				for (int l = 0; l < alltracks.length; l++) {
					alltracks[k][m][l] = -1; // initialisation ...
					velocityOn[k][m][l] = null;
				}

		logger.debug("iterate on the privateTrackedMidiEvents"); //$NON-NLS-1$
		for (Iterator<PrivateTrackedMidiEvent> it = allevents.iterator(); it
				.hasNext();) {

			PrivateTrackedMidiEvent pt = it.next();

			MidiEvent me = pt.me;

			logger.debug("-- treat midievent " + me.getTick() + " -> " + me); //$NON-NLS-1$

			// if (logger.isDebugEnabled())
			// logger.debug("treat event " + me);

			int notetrack = pt.track;

			currenttime = lasttempotime
					+ (long) ((me.getTick() - lasttempotick) * micropertick);

			// Récupération des messages de notes
			byte[] message = me.getMessage().getMessage();
			if (message.length > 0) {
				// on a des éléments dans le message ...

				int canal = extractChannel(message);

				int cmd = extractCommand(message);
				if (cmd == 8) {
					// System.out.println("Note Off");

					int note = (extractFirst(message));
					int velOff = extractSecond(message);

					if (logger.isDebugEnabled())
						logger.debug("Time :" + currenttime + "Note Off :" //$NON-NLS-1$ //$NON-NLS-2$
								+ note
								+ ",Tick :" + me.getTick() + ", Channel :" + canal + " track :" //$NON-NLS-1$ //$NON-NLS-2$
								+ notetrack);

					if (alltracks[notetrack][canal][note] == -1) {
						logger.warn("note " + note + " not beginned on track " //$NON-NLS-1$ //$NON-NLS-2$
								+ notetrack + " skipped"); //$NON-NLS-1$

						MidiFileReadError e = new MidiFileReadError();
						e.code = ErrorCode.BAD_NOTE_END;
						e.description = Messages.getString("MidiFileIO.100") //$NON-NLS-1$
								+ MidiHelper.localizedMidiLibelle(note);
						e.timeStamp = currenttime;
						errors.add(e);

					} else {
						assert alltracks[notetrack][canal][note] != -1;

						assert currenttime - alltracks[notetrack][canal][note] >= 0;

						Integer velOn = velocityOn[notetrack][canal][note];
						
						MidiNote m = new MidiNote(
								alltracks[notetrack][canal][note], currenttime
										- alltracks[notetrack][canal][note],
								note, notetrack, canal, velOn, velOff);

						
						if (logger.isDebugEnabled())
							logger.debug("adding MidiNote :" + m); //$NON-NLS-1$

						if (m.getLength() > 0) {
							assert m.getLength() != 0;

							midifile.add(m);
						} else {
							logger.warn("0 length note, stripped");
						}
						
						alltracks[notetrack][canal][note] = -1;
						velocityOn[notetrack][canal][note] = 127; // reset
					}

				} else if (cmd == 9) {

					// note on
					int note = (extractFirst(message));
					
					int velOnInMidi = extractSecond(message); 

					if (velOnInMidi == 0) {
						// note on but velocity is equals 0
						// so this is a note off, same as above
						if (logger.isDebugEnabled())
							logger.debug("Time :" + currenttime + " Note Off :" //$NON-NLS-1$ //$NON-NLS-2$
									+ note
									+ " from vel 0 "
									+ ",Tick :" + me.getTick() + " track :" + notetrack); //$NON-NLS-1$

						Integer velOff = null;
						Integer velOn = velocityOn[notetrack][canal][note];
						
						if (alltracks[notetrack][canal][note] != -1) {

							assert currenttime
									- alltracks[notetrack][canal][note] >= 0;
									
							MidiNote m = new MidiNote(
									alltracks[notetrack][canal][note],
									currenttime
											- alltracks[notetrack][canal][note],
									note, notetrack, canal, velOn, velOff);
							
							midifile.add(m);
							assert m.getLength() != 0;
							alltracks[notetrack][canal][note] = -1;
							velocityOn[notetrack][canal][note] = null;
							
						} else {

							MidiFileReadError e = new MidiFileReadError();

							e.description = Messages
									.getString("MidiFileIO.101") //$NON-NLS-1$
									+ MidiHelper.localizedMidiLibelle(note);
							e.timeStamp = currenttime;

							e.code = ErrorCode.BAD_NOTE_START;

							errors.add(e);
							logger.warn("bad midi, note " + note //$NON-NLS-1$
									+ " not beginned"); //$NON-NLS-1$
						}
					} else {
						
						assert velOnInMidi > 0;
						
						// note on
						if (logger.isDebugEnabled())
							logger.debug("Time :" + currenttime + " Note On :" //$NON-NLS-1$ //$NON-NLS-2$
									+ note
									+ " channel "
									+ canal
									+ ",Tick :" + me.getTick() + " track :" + notetrack); //$NON-NLS-1$

						// si la note est déjà active, on la ferme pour en
						// creer une autre
						if (alltracks[notetrack][canal][note] != -1) {

							assert currenttime
									- alltracks[notetrack][canal][note] >= 0;

							logger.debug("already active, create the note");
							
							Integer velOn = velocityOn[notetrack][canal][note];

							MidiNote m = new MidiNote(
									alltracks[notetrack][canal][note],
									currenttime
											- alltracks[notetrack][canal][note],
									note, notetrack, canal, velOn, null); // no informations about the closing
							
							midifile.add(m);

						}
						alltracks[notetrack][canal][note] = currenttime;
						velocityOn[notetrack][canal][note] = velOnInMidi;

					}

				} else if (cmd == 11) {
					// control change

					midifile.add(new MidiControlChange(currenttime, canal,
							(int) message[1], (int) message[2]));

				} else if (cmd == 12) {
					// control change

					midifile.add(new MidiProgramChange(currenttime, canal,
							(int) message[1]));

				} else if ((message[0] & 0xFF) == 0xFF) {

					// tempo
					if ((extractFirst(message)) == 0x51) {

						long l = ((message[3] & 0xFF) << 16
								| (message[4] & 0xFF) << 8 | (message[5] & 0xFF)) & 0xFFFFFF;

						if (logger.isDebugEnabled())
							logger.debug("new micro per tick :" + micropertick);

						micropertick = (l * 1.0) / ticksPerBeat;

						lasttempotime = currenttime;
						lasttempotick = me.getTick();

						midifile.add(new MidiTempoEvent(currenttime, l));

						if (logger.isDebugEnabled())
							logger.debug("Time :" + currenttime //$NON-NLS-1$
									+ ",Tick :" + me.getTick() + " tempo change " + micropertick); //$NON-NLS-1$

					} else if ((extractFirst(message)) == 0x58) {
						// Traitement du time signature ...

						byte numerateur = message[3];
						byte denominateur = message[4];

						if (logger.isDebugEnabled())
							logger.debug("signature change " + numerateur + "/" //$NON-NLS-1$ //$NON-NLS-2$
									+ denominateur);

						midifile.add(new MidiSignatureEvent(currenttime,
								numerateur, (int) Math.pow(2, denominateur)));

					} else {
						logger.debug("add generic message");
						addGenericMessage(midifile, currenttime, me, message);
					}
				} else {
					logger.debug("add generic message");
					addGenericMessage(midifile, currenttime, me, message);
				}

			} // message.length > 0

		}
		return alltracks;
	}

	private static int extractChannel(byte[] message) {
		return message[0] & 0x0F;
	}

	/**
	 * @param midifile
	 * @param currenttime
	 * @param me
	 * @param message
	 */
	private static void addGenericMessage(MidiFile midifile, long currenttime,
			MidiEvent me, byte[] message) {
		logger.warn("Message non traité " + me.getMessage() //$NON-NLS-1$
				+ " service " + message[1]); //$NON-NLS-1$

		if (me.getMessage() instanceof MetaMessage) {
			MetaMessage metaMessage = (MetaMessage) me.getMessage();

			midifile.add(new MidiMetaGenericEvent(currenttime, metaMessage
					.getType(), metaMessage.getData()));

		} else if (me.getMessage() instanceof SysexMessage) {

			SysexMessage sysexMessage = (SysexMessage) me.getMessage();

			midifile.add(new MidiSysExGenericEvent(currenttime, sysexMessage
					.getMessage()));

		} else {

			midifile.add(new MidiGenericEvent(currenttime, message));
		}
	}

	/**
	 * test if the message is a note on or note off message
	 */
	private static boolean isNoteMessage(byte[] message) {
		return (extractCommand(message) == 8) || extractCommand(message) == 9;
	}

	/**
	 * @param message
	 * @return
	 */
	private static boolean isNoteOff(byte[] message) {
		assert isNoteMessage(message);

		int cmd = extractCommand(message);
		if (cmd == 8)
			return true;

		int vel = extractSecond(message);

		if (cmd == 9 && vel == 0)
			return true; // note off

		return false;
	}

	/**
	 * @param message
	 * @return
	 */
	private static int extractFirst(byte[] message) {
		return message[1] & 0xFF;
	}

	/**
	 * @param message
	 * @return
	 */
	private static int extractSecond(byte[] message) {
		return (message[2] & 0xFF);
	}

	/**
	 * @param message
	 * @return
	 */
	private static int extractCommand(byte[] message) {
		return (message[0] >> 4 & 0x0F);
	}

	private static void mergeAllEvents(
			Collection<PrivateTrackedMidiEvent> allevents, Track[] tracks) {

		ArrayList<PrivateTrackedMidiEvent> array = new ArrayList<PrivateTrackedMidiEvent>(
				10000);

		PrivateTrackedMidiEvent[] array2 = mergeConvertToArray(tracks, array);

		mergeSortAllEvents(array2);

		mergeAddArrayToCollection(allevents, array2);

	}

	private final static void mergeSortAllEvents(
			PrivateTrackedMidiEvent[] array2) {
		Arrays.sort(array2, new PrivateTrackedMidiEventComparator());
	}

	private static void mergeAddArrayToCollection(
			Collection<PrivateTrackedMidiEvent> allevents,
			PrivateTrackedMidiEvent[] array2) {
		for (int i = 0; i < array2.length; i++) {
			PrivateTrackedMidiEvent privateTrackedMidiEvent = array2[i];
			allevents.add(privateTrackedMidiEvent);
		}
	}

	private static PrivateTrackedMidiEvent[] mergeConvertToArray(
			Track[] tracks, ArrayList<PrivateTrackedMidiEvent> array) {

		logger.debug("merge Convert to Array"); //$NON-NLS-1$

		for (int i = 0; i < tracks.length; i++) {
			Track origin = tracks[i];

			logger.debug("merge track " + origin); //$NON-NLS-1$
			for (int j = 0; j < origin.size(); j++) {
				MidiEvent event = origin.get(j);

				PrivateTrackedMidiEvent pt = new PrivateTrackedMidiEvent();
				pt.me = event;
				pt.track = i;

				array.add(pt);
			}
		}

		PrivateTrackedMidiEvent[] array2 = array
				.toArray(new PrivateTrackedMidiEvent[0]);
		return array2;

	}

	public static void write_midi_0(MidiFile f, File outFile) throws Exception {
		FileOutputStream fileOutputStream = new FileOutputStream(outFile);
		try {
			write_midi_0(f, fileOutputStream);
		} finally {
			fileOutputStream.close();
		}
	}

	public static void write_midi_0(MidiFile f, OutputStream outStream)
			throws Exception {

		Sequence seq = createSequence(f);

		logger.debug("writing sequence"); //$NON-NLS-1$
		writeMidi(seq, 0, outStream);
		logger.debug("written"); //$NON-NLS-1$

	} // write_midi_0

	/**
	 * Create a midi sequence from aprint midifile object
	 * 
	 * @param f
	 * @return
	 * @throws InvalidMidiDataException
	 * @throws Exception
	 */
	public static Sequence createSequence(MidiFile f)
			throws InvalidMidiDataException, Exception {

		int ticksperbeat = 384;

		int bpm = 120;
		
		long l = 60_000_000L / bpm;
		
		long micropertick = (long) ( 60_000_000L / bpm  / ticksperbeat);
		logger.debug("micropertick " + micropertick); //$NON-NLS-1$
		
		logger.debug("convert"); //$NON-NLS-1$

		Sequence seq = new Sequence(Sequence.PPQ, ticksperbeat);
		Track track = seq.createTrack();

		logger.debug("micropertick " + micropertick); //$NON-NLS-1$

		byte[] b = { (byte) ((l >> 16) & 0xFF), (byte) ((l >> 8) & 0xFF),
				(byte) (l & 0xFF) };

		// Ajout du message de tempo ....

		MetaMessage mtm = new MetaMessage();
		mtm.setMessage(0x51, b, b.length);
		MidiEvent mt = new MidiEvent(mtm, 0);

		logger.debug("add a track for midi 0"); //$NON-NLS-1$
		track.add(mt);

		TreeSet<MidiAdvancedEvent> ts = new TreeSet<MidiAdvancedEvent>(
				new MidiAdvancedEventTimestampComparator());
		ts.addAll(f);

		for (MidiAdvancedEvent e : ts) {
			if (e instanceof MidiNote) {
				MidiNote mn = (MidiNote) e;

				if (mn.getMidiNote() != -1 && mn.getLength() != 0) {

					long tick = (mn.getTimeStamp() /* + decalage */)
							/ micropertick;
					ShortMessage mm = new ShortMessage();

					int vel = 127;
					if (mn.getVelocityOn() != null) {
						vel = mn.getVelocityOn();
					}
					mm.setMessage(0x90, mn.getChannel(), mn.getMidiNote(), vel);
					MidiEvent me = new MidiEvent(mm, tick);

					track.add(me);

					tick = (mn.getTimeStamp() + mn.getLength()) / micropertick;
					mm = new ShortMessage();
					
					int velOff = 127;
					if (mn.getVelocityOn() != null) {
						velOff = mn.getVelocityOn();
					}
					mm.setMessage(0x80, mn.getChannel(), mn.getMidiNote(), velOff);

					me = new MidiEvent(mm, tick);
					track.add(me);
				}
			} else if (e instanceof MidiControlChange) {

				MidiControlChange mcc = (MidiControlChange) e;
				long tick = (mcc.getTimeStamp() /* + decalage */)
						/ micropertick;

				ShortMessage sm = new ShortMessage();
				sm.setMessage(11 << 4, mcc.getChannel(), mcc.getControl(),
						mcc.getValue());

				MidiEvent me = new MidiEvent(sm, tick);
				track.add(me);

			} else if (e instanceof MidiProgramChange) {

				MidiProgramChange mcc = (MidiProgramChange) e;
				long tick = (mcc.getTimeStamp() /* + decalage */)
						/ micropertick;

				ShortMessage sm = new ShortMessage();
				sm.setMessage(12 << 4, mcc.getChannel(), mcc.getProgram(), 0);

				MidiEvent me = new MidiEvent(sm, tick);
				track.add(me);

			} else if (e instanceof MidiTempoEvent) {

				// Nothing to do, we don't support tempo change

			} else if (e instanceof MidiSignatureEvent) {

				// Nothing to do, we don't support tempo change

			} else if (e instanceof MidiGenericEvent) {

				MidiGenericEvent mge = (MidiGenericEvent) e;
				long tick = (mge.getTimeStamp() /* + decalage */)
						/ micropertick;

				byte[] data = mge.getData();

				if (e instanceof MidiMetaGenericEvent) {

					MidiMetaGenericEvent mmge = (MidiMetaGenericEvent) e;

					MetaMessage mm = new MetaMessage();
					mm.setMessage(mmge.getType(), mmge.getData(),
							mmge.getData().length);

					MidiEvent me = new MidiEvent(mm, tick);
					track.add(me);

				} else if (e instanceof MidiSysExGenericEvent) {

					MidiSysExGenericEvent msege = (MidiSysExGenericEvent) e;
					SysexMessage mm = new SysexMessage();
					mm.setMessage(msege.getData(), msege.getData().length);

					MidiEvent me = new MidiEvent(mm, tick);
					track.add(me);

				} else if (data.length <= 3 && data.length >= 2) {

					if (data[0] == -1) {
						// unsupported event ..
						throw new Exception("unsupported event");
					} else {

						ShortMessage sm = new ShortMessage();

						int d1 = data[1];
						int d2 = 0;

						if (data.length > 2)
							d2 = data[2];

						sm.setMessage(data[0], d1, d2);
						MidiEvent me = new MidiEvent(sm, tick);
						track.add(me);
					}
				} else {

					int status = data[0] & 0xFF;

					if (status == 0xF0 || status == 0xF7) {
						SysexMessage mm = new SysexMessage();
						mm.setMessage(data, data.length);
						MidiEvent me = new MidiEvent(mm, tick);
						track.add(me);
					} else {
						StringBuffer sb = new StringBuffer();
						for (int i = 0; i < data.length; i++) {
							sb.append(data[i]).append("-"); //$NON-NLS-1$
						}
						throw new Exception(
								"unsupported generic message with length " //$NON-NLS-1$
										+ data.length + "--> " + sb.toString()); //$NON-NLS-1$
						// unsupported message
					}

					//
					// StringBuffer sb = new StringBuffer();
					// for (int i = 0; i < data.length; i++) {
					//						sb.append(data[i]).append("-"); //$NON-NLS-1$
					// }
					//
					// throw new Exception(
					//							"unsupported generic message with length " //$NON-NLS-1$
					//									+ data.length + "--> " + sb.toString()); //$NON-NLS-1$
				}

			} else {
				throw new Exception("unsupported midi event :" + e); //$NON-NLS-1$
			}
		}
		return seq;
	}

	public static void writeMidi(Sequence seq, int type, OutputStream stream)
			throws Exception {

		if (seq == null)
			throw new Exception("null sequence passed to the midiwrite"); //$NON-NLS-1$

		MidiSystem.write(seq, type, stream);
		// new StandardMidiFileWriter().write(seq, type, stream);
	}

}
