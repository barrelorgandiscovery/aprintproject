package org.barrelorgandiscovery.listeningconverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.instrument.RegisterSoundLink;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.FakeLogger;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.rendering.MusicBoxRendering;
import org.barrelorgandiscovery.virtualbook.rendering.VirtualBookRendering;

import gervill.SoftShortMessage;


/**
 * This class convert the Virtual Book to a Midi compliant Listening
 * 
 * @author Freydiere Patrice
 */
public class VirtualBookToMidiConverter implements MIDIListeningConverter {

	private static Logger logger = new FakeLogger(
			VirtualBookToMidiConverter.class.getSimpleName());

	private int ticksperbeat = 384;

	/**
	 * Association between the register and the midi program
	 */
	private HashMap<String, Integer> soundBankMapping = null;

	private int drumInstrument = -1;
	
	private boolean isMusicBox = false;

	public VirtualBookToMidiConverter(Instrument currentInstrument)
			throws Exception {
		// construct a section-registermapping

		if (currentInstrument == null)
			throw new IllegalArgumentException("current Instrument is null");

		// is music box ?
		assert currentInstrument != null && currentInstrument.getScale() != null;
		VirtualBookRendering rendering = currentInstrument.getScale().getRendering();
		if (rendering != null) {
			if (rendering instanceof MusicBoxRendering) {
				logger.debug("musicbox rendering");
				isMusicBox = true;
			}
		}
		
		
		// Mapping the registers on the correct choosen instrument
		HashMap<String, Integer> registerMappingHash = new HashMap<String, Integer>();
		assert currentInstrument != null;

		RegisterSoundLink registerSoundLink = currentInstrument
				.getRegisterSoundLink();
		assert registerSoundLink != null;

		this.drumInstrument = registerSoundLink.getDrumSoundBank();

		List<String> psn = registerSoundLink
				.getPipeStopGroupNamesInWhichThereAreMappings();
		for (Iterator iterator = psn.iterator(); iterator.hasNext();) {

			String pipestopgroup = (String) iterator.next();
			logger.debug("evaluating instrument for pipestopgroup :"
					+ pipestopgroup);

			List<String> ps = registerSoundLink
					.getPipeStopNamesInWhichThereAreMappings(pipestopgroup);

			for (Iterator iterator2 = ps.iterator(); iterator2.hasNext();) {
				String p = (String) iterator2.next();

				logger.debug("evaluating instrument for pipestop " + p);

				int instrumentNumber = registerSoundLink.getInstrumentNumber(
						pipestopgroup, p);

				String k = pipestopgroup + "-" + p;
				if (logger.isDebugEnabled())
					logger.debug("adding mapping for " + k + " on "
							+ instrumentNumber);
				registerMappingHash.put(k, instrumentNumber);
			}

		}

		this.soundBankMapping = registerMappingHash;

	}

	public VirtualBookToMidiConverter(HashMap<String, Integer> soundBankMapping) {
		this.soundBankMapping = soundBankMapping;
	}

	/**
	 * Adding midi events to the array retvalue, it contains the channel and the
	 * note to play
	 * 
	 * @param vb
	 * @param timestamp
	 * @param track
	 * @param retvalue
	 */
	private void addMidiToPlayForTrack(VirtualBook vb, long timestamp,
			int track, ArrayList<int[]> retvalue) {

		logger.debug("for track :" + track + " on timestamp :" + timestamp);

		assert soundBankMapping != null;

		Scale scale = vb.getScale();

		AbstractTrackDef[] tracksDefinition = scale.getTracksDefinition();

		int sectionno = vb.findSection(timestamp);

		String[] sectionRegisters = vb.getSectionRegisters(sectionno);

		AbstractTrackDef abstractTrackDef = tracksDefinition[track];

		if (abstractTrackDef instanceof NoteDef) {

			NoteDef nd = (NoteDef) abstractTrackDef;

			// adding register defined ... only on the registerset defined !!!

			for (String r : sectionRegisters) {

				if (!soundBankMapping.containsKey(r)) {
					logger.error("Error, currentInstrumentMapping :" + r
							+ " does not exist");
					continue;
				}

				logger.debug("register on :" + r);

				String registerSetName = nd.getRegisterSetName();
				if (r.startsWith(registerSetName + "-")) {

					Integer program = soundBankMapping.get(r);
					logger.debug("program for note : " + program);

					int channel = program;
					if (channel >= 9)
						channel += 1;

					int[] p = new int[] { channel, nd.getMidiNote() };

					retvalue.add(p);
					logger.debug("note :" + nd.getMidiNote() + " on channel "
							+ channel + " added");
				}
			}

			// if default sound for the group name exist , adding the rendering
			// ...
			String defaultRegisterforPipeStopGroup = nd.getRegisterSetName()
					+ "-" + IEditableInstrument.DEFAULT_PIPESTOPGROUPNAME;
			if (soundBankMapping.containsKey(defaultRegisterforPipeStopGroup)) {
				logger.debug("adding default sound for :"
						+ defaultRegisterforPipeStopGroup);
				Integer program = soundBankMapping
						.get(defaultRegisterforPipeStopGroup);
				int channel = program;
				if (channel >= 9)
					channel += 1;

				int[] p = new int[] { channel, nd.getMidiNote() };
				retvalue.add(p);
				logger.debug("note :" + nd.getMidiNote() + " on channel "
						+ channel + " added");
			}

		} // if notedef

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.listeningconverter.MIDIListeningConverter#convert
	 * (fr.freydierepatrice.virtualbook.VirtualBook)
	 */
	public Sequence convert(VirtualBook vb) throws Exception {
		/**
		 * Fusion des notes dédoublées
		 */

		long micropertick = (long) (10000.0 / 384.0 * ticksperbeat);

		logger.debug("merge all overlapping holes");

		VirtualBook carton = vb.flattenVirtualBook();

		// in case this is musicbox, we must enlarge the notes time, 
		// to get the hole sound remaining
		
		if (isMusicBox) {
			logger.debug("enlarge all holes");
			
			// 1,5s length
			long minimumLength = 1_500_000;
			
			ArrayList<Hole> newHoles = new ArrayList<>();
			for (Hole h : carton.getHolesCopy()) {
				newHoles.add(h.newHoleWithMinimumLength(minimumLength));
			}
			
			carton.clear();
			carton.addHoles(newHoles);
		}
		
		
		logger.debug("convert");

		Sequence seq = new Sequence(Sequence.PPQ, ticksperbeat);

		logger.debug("adding program to channels ... ");
		assert soundBankMapping != null;

		Track baseTrack = seq.createTrack();

		HashMap<Integer, Queue<Track>> trackReference = new HashMap<Integer, Queue<Track>>();

		ShortMessage shortBaseMessage = createShortMessage();
		shortBaseMessage.setMessage(ShortMessage.PROGRAM_CHANGE, 0, 0, 0);

		baseTrack.add(new MidiEvent(shortBaseMessage, 0));

		if (this.drumInstrument != -1) {

			ShortMessage shortMessageDrum = createShortMessage();
			shortMessageDrum.setMessage(ShortMessage.PROGRAM_CHANGE, 9,
					drumInstrument, 127);
			baseTrack.add(new MidiEvent(shortMessageDrum, 0));
		}

		LinkedList<Track> bt = new LinkedList<Track>();
		bt.add(baseTrack);

		// adding the base track
		trackReference.put(0, bt);

		// creating several tracks for percussions, because
		// some drums are long and the notes must not overlapps

		Queue<Track> drumTracks = new LinkedList<Track>();

		for (int i = 0; i < 50; i++) {
			drumTracks.add(seq.createTrack());
		}

		trackReference.put(9, drumTracks);

		// shortBaseMessage = new ShortMessage();
		// shortBaseMessage.setMessage(ShortMessage.PROGRAM_CHANGE, 0, 0, 0);
		// percussionTrack.add(new MidiEvent(shortBaseMessage, 0));

		for (Iterator itmapping = soundBankMapping.entrySet().iterator(); itmapping
				.hasNext();) {
			Entry<String, Integer> mapping = (Entry<String, Integer>) itmapping
					.next();

			logger.debug("creating tracks for the instrument / register");
			Track track = seq.createTrack();
			Integer program = mapping.getValue();

			int canal = program;

			if (program >= 9) // skip the specific drum channel (midi : 10)
			{
				canal += 1;
			}

			logger.debug("mapping program " + program + " to channel " + canal);

			LinkedList<Track> llr = new LinkedList<Track>();
			llr.add(track);
			trackReference.put(canal, llr);

			ShortMessage shortMessage = createShortMessage();

			// we can go up to 16 channels ... nice to play registers ...

			// throw new Exception("implementation error, too much channels");

			shortMessage.setMessage(ShortMessage.PROGRAM_CHANGE, canal,
					program, 127);

			track.add(new MidiEvent(shortMessage, 0));

		}

		logger.debug("micropertick " + micropertick);

		long l = micropertick * seq.getResolution();

		byte[] b = { (byte) ((l >> 16) & 0xFF), (byte) ((l >> 8) & 0xFF),
				(byte) (l & 0xFF) };

		// Ajout du message de tempo ....

		logger.debug("adding tempo message");
		MetaMessage mtm = new MetaMessage();
		mtm.setMessage(0x51, b, b.length);
		MidiEvent mt = new MidiEvent(mtm, 0);

		logger.debug("add to track");
		baseTrack.add(mt);

		List<Hole> notes = carton.getOrderedHolesCopy();

		for (int i = 0; i < notes.size(); i++) {
			Hole n = notes.get(i);

			// conversion en note midi ...

			AbstractTrackDef nd = carton.getScale().getTracksDefinition()[n
					.getTrack()];

			if (nd != null) {

				long decalage = 0;

				if (nd instanceof PercussionDef) {
					PercussionDef d = (PercussionDef) nd;

					// on prends en charge le retard !!
					if (!Double.isNaN(d.getRetard())) {
						// on a un retard dans la gamme d'origine ...

						decalage = (long) (d.getRetard()
								/ carton.getScale().getSpeed() * 1000000);
					}
				}

				int[][] notesToPlay = new int[][] {};

				if (nd instanceof PercussionDef) {

					if (logger.isDebugEnabled())
						logger.debug("percussion midicode :"
								+ ((PercussionDef) nd).getPercussion());

					notesToPlay = new int[][] { { 9,
							((PercussionDef) nd).getPercussion() } };

				} else if (nd instanceof NoteDef) {

					ArrayList<int[]> ntp = new ArrayList<int[]>();

					// add midi events to the list for playing
					addMidiToPlayForTrack(carton, n.getTimestamp(),
							n.getTrack(), ntp);

					// ajout du default ...
					ntp.add(new int[] { 0, ((NoteDef) nd).getMidiNote() });

					notesToPlay = ntp.toArray(new int[0][]);

				}

				if (notesToPlay.length > 0 && n.getTimeLength() != 0) {

					for (int[] midinote : notesToPlay) {

						long tick = (n.getTimestamp() + decalage)
								/ micropertick;

						if (logger.isDebugEnabled())
							logger.debug("start tick : " + tick);

						int canal = midinote[0];
						if (logger.isDebugEnabled())
							logger.debug("canal :" + canal);
						int notemidi = midinote[1];
						if (logger.isDebugEnabled())
							logger.debug("notemidi :" + notemidi);

						ShortMessage mm = createShortMessage();

						mm.setMessage(0x90, canal, notemidi, 127);
						MidiEvent me = new MidiEvent(mm, tick);

						Queue<Track> trackQueue = trackReference.get(canal);

						if (trackQueue == null)
							throw new Exception(
									"implementation error, null track getted");

						Track t = trackQueue.remove();

						if (t == null)
							throw new Exception(
									"implementation error, null track getted");

						t.add(me);

						// put it at the end of the queue
						trackQueue.offer(t);

						long lengthNote = n.getTimeLength();
						// if (canal == 9) // drum
						// {
						// lengthNote = 1000000;
						// }

						tick = (n.getTimestamp() + lengthNote + decalage)
								/ micropertick;

						if (logger.isDebugEnabled())
							logger.debug("end tick : " + tick);

						mm = createShortMessage();
						mm.setMessage(0x80, canal, notemidi, 127);

						if (logger.isDebugEnabled()) {
							logger.debug("adding note midi " + notemidi
									+ " on channel " + canal);
						}

						me = new MidiEvent(mm, tick);

						if (logger.isDebugEnabled())
							logger.debug("midi note added to sequence");
						t.add(me);

					}
				}
			} else {

				System.out.println("track " + n.getTrack()
						+ " cannot be converted");

			}
		}
		return seq;

	}

	/**
	 * 
	 * create a short message that accept more than 16 channels
	 * 
	 * @return
	 */
	protected ShortMessage createShortMessage() {
		return new SoftShortMessage();
	}

}
