package org.barrelorgandiscovery.gui.aprint;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.playsubsystem.GervillPlaySubSystemWithRegisterInstruments;

import com.sun.media.sound.AudioSynthesizer;
import com.sun.media.sound.SoftSynthesizer;

public class SequencerTools {

	private static final Logger logger = Logger.getLogger(SequencerTools.class);

	/**
	 * Render sequence using selected or default soundbank into wave audio file.
	 */
	public static void render(Soundbank soundbank, Sequence sequence,
			File audio_file, boolean onlyfirstinstrument) throws Exception {
		try {
			// Find available AudioSynthesizer.
			AudioSynthesizer synth = new SoftSynthesizer(); // findAudioSynthesizer();
			
			if (synth == null) {
				logger.error("No AudioSynthesizer was found!");//$NON-NLS-1$
				throw new Exception("No AudioSynthesizer was found");//$NON-NLS-1$
			}
			
			// synth.open();

			Map<String, Object> infos = GervillPlaySubSystemWithRegisterInstruments.setupInfoProperties();
			
			// Open AudioStream from AudioSynthesizer.
			AudioInputStream stream = synth.openStream(null, infos);

		
			// Load user-selected Soundbank into AudioSynthesizer.
			if (soundbank != null) {

				if (onlyfirstinstrument) {
					Soundbank defaultbank = synth.getDefaultSoundbank();
					if (defaultbank == null) {
						logger.debug("no default bank"); //$NON-NLS-1$
					}

					if (defaultbank != null) {
						synth.loadInstrument(soundbank.getInstruments()[0]);
						
						logger.debug("remap the instrument"); //$NON-NLS-1$

						if (!synth.remapInstrument(
								defaultbank.getInstruments()[0], soundbank
										.getInstruments()[0])) {
							logger.error("fail to remap instrument"); //$NON-NLS-1$
						}
					} 

				} else {
					
					// FIX PF 2010 - Percus ...
					// if unload all instruments, all drums are unloaded from the default soundbank
					
//					Soundbank defsbk = synth.getDefaultSoundbank();
//					if (defsbk != null)
//						synth.unloadAllInstruments(defsbk);
					synth.loadAllInstruments(soundbank);
				}

			}

			
			 Receiver recv = synth.getReceiver();
			
			// reverbe off 
			// Set volume to max and turn reverb off
			 ShortMessage reverb_off = new ShortMessage();
			 reverb_off.setMessage(ShortMessage.CONTROL_CHANGE, 91, 0);
			 recv.send(reverb_off, -1);
			
			
			// Play Sequence into AudioSynthesizer Receiver.
			double total = send(sequence,recv);

			// Calculate how long the WAVE file needs to be.
			long len = (long) (stream.getFormat().getFrameRate() * (total + 4));
			stream = new AudioInputStream(stream, stream.getFormat(), len);

			// Write WAVE file to disk.
			AudioSystem.write(stream, AudioFileFormat.Type.WAVE, audio_file);

			// We are finished, close synthesizer.
			synth.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Find available AudioSynthesizer.
	 */
	private static AudioSynthesizer findAudioSynthesizer()
			throws MidiUnavailableException {
		// First check if default synthesizer is AudioSynthesizer.
		Synthesizer synth = MidiSystem.getSynthesizer();
		if (synth instanceof AudioSynthesizer)
			return (AudioSynthesizer) synth;

		// If default synhtesizer is not AudioSynthesizer, check others.
		Info[] infos = MidiSystem.getMidiDeviceInfo();
		for (int i = 0; i < infos.length; i++) {
			MidiDevice dev = MidiSystem.getMidiDevice(infos[i]);
			if (dev instanceof AudioSynthesizer)
				return (AudioSynthesizer) dev;
		}

		// No AudioSynthesizer was found, return null.
		return null;
	}

	/*
	 * Send entiry MIDI Sequence into Receiver using timestamps.
	 */
	private static double send(Sequence seq, Receiver recv) {
		float divtype = seq.getDivisionType();
		assert (seq.getDivisionType() == Sequence.PPQ);
		Track[] tracks = seq.getTracks();
		int[] trackspos = new int[tracks.length];
		int mpq = 500000;
		int seqres = seq.getResolution();
		long lasttick = 0;
		long curtime = 0;
		while (true) {
			MidiEvent selevent = null;
			int seltrack = -1;
			for (int i = 0; i < tracks.length; i++) {
				int trackpos = trackspos[i];
				Track track = tracks[i];
				if (trackpos < track.size()) {
					MidiEvent event = track.get(trackpos);
					if (selevent == null
							|| event.getTick() < selevent.getTick()) {
						selevent = event;
						seltrack = i;
					}
				}
			}
			if (seltrack == -1)
				break;
			trackspos[seltrack]++;
			long tick = selevent.getTick();
			if (divtype == Sequence.PPQ)
				curtime += ((tick - lasttick) * mpq) / seqres;
			else
				curtime = (long) ((tick * 1000000.0 * divtype) / seqres);
			lasttick = tick;
			MidiMessage msg = selevent.getMessage();
			if (msg instanceof MetaMessage) {
				if (divtype == Sequence.PPQ)
					if (((MetaMessage) msg).getType() == 0x51) {
						byte[] data = ((MetaMessage) msg).getData();
						mpq = ((data[0] & 0xff) << 16)
								| ((data[1] & 0xff) << 8) | (data[2] & 0xff);
					}
			} else {
				if (recv != null)
					recv.send(msg, curtime);
			}
		}
		return curtime / 1000000.0;
	}
}
