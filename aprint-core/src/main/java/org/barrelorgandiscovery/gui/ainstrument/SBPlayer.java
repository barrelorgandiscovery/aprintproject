package org.barrelorgandiscovery.gui.ainstrument;

import java.util.Map;

import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.AudioSystem;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.playsubsystem.GervillPlaySubSystemWithRegisterInstruments;

import gervill.SoftSynthesizer;

/**
 * MultiThreaded Utility class for playing notes where a soundbank can be
 * changed
 * 
 * @author Freydiere Patrice
 * 
 */
public class SBPlayer {

	private static Logger logger = Logger.getLogger(SBPlayer.class);

	private SoftSynthesizer synth;
	private Receiver receiver;

	public SBPlayer() throws Exception {

	}

	public void open() throws Exception {
		try {
			logger.debug("open the SBPlayer"); //$NON-NLS-1$
			synth = new SoftSynthesizer();
			
			receiver = synth.getReceiver();

			Map<String, Object> infos = GervillPlaySubSystemWithRegisterInstruments
					.setupInfoProperties();

			
			synth.open(null, infos); 

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

	public void changeCurrentSoundBank(Soundbank soundBank) throws Exception {

		if (synth == null) {
			logger.info("no synth");
			return;
		}
		
		assert synth != null;
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

		logger.debug("instruments loaded ..."); //$NON-NLS-1$

		if (!synth.remapInstrument(
				synth.getDefaultSoundbank().getInstruments()[0],
				soundBank.getInstruments()[0])) {
			logger.error("fail to remap instrument"); //$NON-NLS-1$

		} else {
			logger.debug("instruments remapped ..."); //$NON-NLS-1$
		}

		lastplayedinstrument = soundBank;

	}

	private int lastPlayedNote = -1;

	public void playNote(int midicode) {

		assert synth.isOpen();

		logger.debug("play note :" + midicode); //$NON-NLS-1$

		if (lastPlayedNote != -1)
			stopNote();

		try {

			ShortMessage shortMessage = new ShortMessage();
			shortMessage.setMessage(ShortMessage.NOTE_ON, 0, midicode, 127);
			receiver.send(shortMessage, -1);

			lastPlayedNote = midicode;

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	public int getCurrentPlayedNote() {
		return lastPlayedNote;
	}

	public void stopNote() {
		assert synth.isOpen();

		logger.debug("stop Note"); //$NON-NLS-1$

		try {

			if (lastPlayedNote == -1)
				return;

			ShortMessage shortMessage = new ShortMessage();
			shortMessage.setMessage(ShortMessage.NOTE_OFF, 0, lastPlayedNote,
					127);
			receiver.send(shortMessage, -1);
			lastPlayedNote = -1;

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

}
