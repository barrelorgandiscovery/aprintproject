package org.barrelorgandiscovery.gui.ainstrument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.EditableInstrument;
import org.barrelorgandiscovery.gui.ainstrument.SBCreator.SF2SoundBankResult;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.Scale;

import gervill.SF2Instrument;
import gervill.SF2Soundbank;
import gervill.SoftSynthesizer;

/**
 * Class for playing notes with register specification
 * 
 * @author Freydiere Patrice
 * 
 */
public class SBRegistersPlay {
	private static Logger logger = Logger.getLogger(SBRegistersPlay.class);

	private Synthesizer synth;
	private Receiver receiver;

	private int[][] lastPlayerMidiNotes = null;

	public SBRegistersPlay() throws Exception {

	}

	public void open() throws Exception {
		try {
			logger.debug("open the SBPlayer"); //$NON-NLS-1$
			synth = new SoftSynthesizer();
			receiver = synth.getReceiver();
			synth.open();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw new Exception(ex.getMessage(), ex);
		}
	}

	public void close() throws Exception {
		try {
			logger.debug("open the SBPlayer"); //$NON-NLS-1$
			synth.close();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw new Exception(ex.getMessage(), ex);
		}
	}

	private Soundbank lastplayedinstrument = null;

	private String[] currentPlayedRegisters = null;

	public void setCurrentRegisterGroupRegister(String[] r) {
		if (logger.isDebugEnabled()) {
			if (r == null)
				logger.debug("null registers for playing");
			else
				for (String s : r) {
					logger.debug("current Register used to play :" + s);
				}
		}
		this.currentPlayedRegisters = r;
	}

	private EditableInstrument currentEditableInstrument = null;

	private HashMap<String, Integer> currentInstrumentMapping = null;

	public void changeInstrument(EditableInstrument instrument)
			throws Exception {

		this.currentEditableInstrument = instrument;

		SF2SoundBankResult sb = new SBCreator().createSoundBank(instrument);
		SF2Soundbank soundBank = sb.soundBank;
		currentInstrumentMapping = sb.soundbankMapping;

		assert synth.isOpen();

		if (lastplayedinstrument != null) {
			logger.debug("unload the instruments ..."); //$NON-NLS-1$
			synth.unloadAllInstruments(lastplayedinstrument);
		}

		if (!synth.isSoundbankSupported(soundBank))
			throw new Exception("unsupported soundBank"); //$NON-NLS-1$

		if (synth.loadAllInstruments(soundBank) == false) {
			throw new Exception("Fail to load sound ... "); //$NON-NLS-1$
		}

		logger.debug("instruments loaded ..., mapping registers ... "); //$NON-NLS-1$

		SF2Instrument[] instruments = soundBank.getInstruments();
		for (int i = 0; i < instruments.length; i++) {
			SF2Instrument instrument2 = instruments[i];
			String r = instrument2.getName();
			logger.debug("Mapping instrument :" + r);

			if (!currentInstrumentMapping.containsKey(r)) {
				logger.error("no mapping for instrument " + r);
			} else {
				Integer p = currentInstrumentMapping.get(r);

				if (!synth.remapInstrument(synth.getDefaultSoundbank()
						.getInstruments()[p], instrument2)) {

					logger.error("fail to remap instrument"); //$NON-NLS-1$

				} else {
					logger.debug("instruments remapped ..."); //$NON-NLS-1$
				}

			}

		}

		// program channels ...

		// for (Entry<String, Integer> e : currentInstrumentMapping.entrySet())
		// {
		// Integer program = e.getValue();
		// logger.debug("program change " + e.getKey() + " on channel ... "
		// + program);
		// ShortMessage shortMessage = new ShortMessage();
		//
		// shortMessage.setMessage(ShortMessage.PROGRAM_CHANGE, program,
		// program);
		//
		// logger.debug("sending program change ... ");
		// receiver.send(shortMessage, -1);
		//
		// logger.debug("done");
		// }

		lastplayedinstrument = soundBank;

	}

	private void addMidiToPlayForTrack(int track, ArrayList<int[]> retvalue) {

		assert currentInstrumentMapping != null;

		Scale scale = currentEditableInstrument.getScale();

		AbstractTrackDef[] tracksDefinition = scale.getTracksDefinition();

		AbstractTrackDef abstractTrackDef = tracksDefinition[track];
		if (abstractTrackDef instanceof NoteDef) {

			NoteDef nd = (NoteDef) abstractTrackDef;

			for (String r : currentPlayedRegisters) {
				if (!currentInstrumentMapping.containsKey(r)) {
					logger.error("Error, currentInstrumentMapping :" + r
							+ " does not exist");
					continue;
				}

				Integer program = currentInstrumentMapping.get(r);

				int[] p = new int[] { program, nd.getMidiNote() };

				retvalue.add(p);
				logger.debug("note :" + nd.getMidiNote() + " on channel "
						+ program + " added");

			}

		}

	}

	public void playTracks(int[] track) {

		assert synth.isOpen();

		try {

			logger.debug("play track :" + track); //$NON-NLS-1$

			if (lastPlayerMidiNotes != null) {
				stopNote();
			}

			ArrayList<int[]> midiToPlay = new ArrayList<int[]>();

			for (int i = 0; i < track.length; i++) {
				int t = track[i];
				addMidiToPlayForTrack(t, midiToPlay);
			}

			assert lastPlayerMidiNotes == null;

			lastPlayerMidiNotes = midiToPlay.toArray(new int[0][]);

			for (int i = 0; i < lastPlayerMidiNotes.length; i++) {
				int[] cm = lastPlayerMidiNotes[i];

				ShortMessage shortMessage = new ShortMessage();
				shortMessage.setMessage(ShortMessage.PROGRAM_CHANGE, 0, cm[0],
						127);
				receiver.send(shortMessage, -1);

				ShortMessage shortMessage2 = new ShortMessage();
				shortMessage2.setMessage(ShortMessage.NOTE_ON, 0, cm[1], 127);
				receiver.send(shortMessage2, -1);
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	public void stopNote() {

		assert synth.isOpen();

		logger.debug("stop Note"); //$NON-NLS-1$

		try {

			if (lastPlayerMidiNotes == null)
				return;

			for (int i = 0; i < lastPlayerMidiNotes.length; i++) {
				int[] cm = lastPlayerMidiNotes[i];

				ShortMessage shortMessage = new ShortMessage();
				shortMessage.setMessage(ShortMessage.NOTE_OFF, cm[0], cm[1],
						127);
				receiver.send(shortMessage, -1);
			}

			lastPlayerMidiNotes = null;

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	public void playSequence(Sequence seq) throws Exception {
		assert synth.isOpen();

		logger.debug("get Sequencer");

		Sequencer sequencer = MidiSystem.getSequencer();

		sequencer.setSequence(seq);

		sequencer.open();

		List<Transmitter> transmiters = sequencer.getTransmitters();
		for (Iterator iterator = transmiters.iterator(); iterator.hasNext();) {
			Transmitter transmitter = (Transmitter) iterator.next();
			transmitter.close();
		}

		Transmitter transmitter = sequencer.getTransmitter();
		transmitter.setReceiver(receiver);
		try {

			sequencer.start();

			logger.debug("start playing");

			while (sequencer.isRunning()) {
				Thread.sleep(100);
			}

			logger.debug("end playing ... ");
		} finally {
			transmitter.close();
		}

		sequencer.stop();

		sequencer.close();

	}

	public EditableInstrument getCurrentEditableInstrument() {
		return currentEditableInstrument;
	}

	public HashMap<String, Integer> getCurrentInstrumentMapping() {
		return currentInstrumentMapping;
	}

}
