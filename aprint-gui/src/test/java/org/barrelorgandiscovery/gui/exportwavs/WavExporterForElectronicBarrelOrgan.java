package org.barrelorgandiscovery.gui.exportwavs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.Instrument;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Patch;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Track;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JPanel;

import org.apache.commons.io.input.RandomAccessFileInputStream;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.StreamsTools;

import gervill.ModelPatch;
import gervill.SoftSynthesizer;

/**
 * export sf2 sound font instrument into Wav elements
 * 
 * @author pfreydiere
 *
 */
public class WavExporterForElectronicBarrelOrgan extends JPanel {

	private static Logger logger = Logger.getLogger(WavExporterForElectronicBarrelOrgan.class);

	public WavExporterForElectronicBarrelOrgan() {

	}

	public static Map<String, Object> setupInfoProperties() {

		Map<String, Object> infos = new HashMap<String, Object>();
//		infos.put("interpolation","cubic");
//		infos.put("latency", 10L); // in ms
//		// infos.put("control rate", 22000f);
//		infos.put("reverb", Boolean.FALSE);
//		infos.put("chorus", Boolean.FALSE);
//		infos.put("max polyphony", 256);
//		infos.put("midi channels", 64);
//		// infos.put("auto gain control", Boolean.FALSE);
//		// infos.put("jitter correction", Boolean.FALSE);
//		infos.put("large mode", Boolean.TRUE); // slow in some time
		return infos;

	}

	public static void renderAllInstrumentsInSoundBank(File sf2instrument, File outputFolder) throws Exception {
		assert sf2instrument != null;
		assert sf2instrument.isFile();

		// Chargement de l'instrument ...

		try (RandomAccessFileInputStream instrumentStream = new RandomAccessFileInputStream(
				new RandomAccessFile(sf2instrument, "r"))) {

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			StreamsTools.copyStream(instrumentStream, baos);

			ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());

			Soundbank sb = MidiSystem.getSoundbank(bis);

			Instrument[] instruments = sb.getInstruments();

			for (int i = 0; i < instruments.length; i++) {
				Instrument instrument2 = instruments[i];
				String r = instrument2.getName();
				//
				System.out.println("" + i + "   Instrument :" + r);

				File subfolder = new File(outputFolder, r);

				if (!subfolder.exists())
				{
					subfolder.mkdirs();	
					System.out.println("rendering " + subfolder);
					renderAllNotesInWavFolder(sb, subfolder, i);
				}
			}

		}

	}

	public static void renderAllNotesInWavFolder(Soundbank sb, File outputFolder, int noInstrument) throws Exception {

		// int noInstrument = 1;

		for (int note = 0; note < 127; note++) {
			renderNote(outputFolder, sb, noInstrument, note);
		}

	}

	public static void renderNote(File outputFolder, Soundbank sb, int noInstrument, int note) throws Exception {

		// Find available AudioSynthesizer.
		SoftSynthesizer synth = new SoftSynthesizer(); // findAudioSynthesizer();

		// Open AudioStream from AudioSynthesizer.
		AudioInputStream stream = synth.openStream(null, setupInfoProperties());

		if (!synth.loadAllInstruments(sb)) {
			throw new Exception("instrument not loaded");
		}

//		for (int i = 0; i < instruments.length; i++) {
//			Instrument instrument2 = instruments[i];
//			String r = instrument2.getName();
//
//			System.out.println("" + i + "   Instrument :" + r);
//		}

		Sequence sequence = new Sequence(Sequence.PPQ, 128);
		Track track = sequence.createTrack();

		Instrument instrument = sb.getInstruments()[noInstrument];
		Patch patch = instrument.getPatch();
		
		ModelPatch mp = (ModelPatch)patch;
		
		
		int bank = patch.getBank();
		// System.out.println("bank : " + bank);
		int program = patch.getProgram();
		// System.out.println("program : " + program);

//		ShortMessage ctrlChangeMessage = new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, 0, bank / 127);
//		track.add(new MidiEvent(ctrlChangeMessage, 0));
//		ShortMessage ctrlChangeMessage2 = new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, 0x20, bank & 127);
//		track.add(new MidiEvent(ctrlChangeMessage2, 0));
//		

//		ShortMessage programChangeMessage = new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, program, 0);
//
//		track.add(new MidiEvent(programChangeMessage, 0));

		
		int channel = mp.isPercussion() ? 9 : 0;	
		if (mp.isPercussion()) {
			System.out.println("percussion");
		}
		
		synth.getChannels()[channel].programChange(/* bank, */ program);

		
		ShortMessage shortMessage = new ShortMessage(ShortMessage.NOTE_ON, channel, note, 127);
		track.add(new MidiEvent(shortMessage, 1L));

		ShortMessage endNoteMessage = new ShortMessage(ShortMessage.NOTE_OFF, channel, note, 127);
		track.add(new MidiEvent(endNoteMessage, 10000L/4));

		try (FileOutputStream os = new FileOutputStream(new File(outputFolder, "DEFAULT_" + note + ".WAV"))) {

			Receiver recv = synth.getReceiver();

			// reverbe off
//			// Set volume to max and turn reverb off
//			ShortMessage reverb_off = new ShortMessage();
//			reverb_off.setMessage(ShortMessage.CONTROL_CHANGE, 91, 0);
//			recv.send(reverb_off, -1);

			// Play Sequence into AudioSynthesizer Receiver.
			double total = send(sequence, recv);

			// Calculate how long the WAVE file needs to be.
			long len = (long) (stream.getFormat().getFrameRate() * (total + 4));
			stream = new AudioInputStream(stream, stream.getFormat(), len);

			// Write WAVE file to disk.
			AudioSystem.write(stream, AudioFileFormat.Type.WAVE, os);

			// We are finished, close synthesizer.
			synth.close();

			// SequencerTools.render(sb, sequence, os, false);

		}
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
					if (selevent == null || event.getTick() < selevent.getTick()) {
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
						mpq = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
					}
			} else {
				if (recv != null)
					recv.send(msg, curtime);
			}
		}
		return curtime / 1000000.0;
	}

	/**
	 * main procedure
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

//		File outFolder = new File("/home/use/tmp/outputsound");
//		outFolder.mkdirs();
//		File insf2 = new File(
//				"/home/use/projets/2022-02_Orgue_Electronique/work/sons/arachno-soundfont-10-sf2/Arachno SoundFont - Version 1.0.sf2");

		 File insf2 = new File(
		 "/home/use/projets/2022-02_Orgue_Electronique/work/sons/bibliotheque_sons/SoniMusicae-Diato-sf2/Sonimusicae-diato-sf2/Diato.sf2");

		// File insf2 = new
		// File("/home/use/projets/2022-02_Orgue_Electronique/work/sons/SoniMusicae-Blanchet1720-sf2/Soni
		// Musicae-Blanchet1720-sf2/Blanchet-1720.sf2");

		// String sbpath = "GeneralUser GS MuseScore v1.442.sf2";
		File outFolder = new File("/home/use/tmp/outputdiato");
		outFolder.mkdirs();
//		File insf2 = new File(
//				"/home/use/projets/2022-02_Orgue_Electronique/work/sons/bibliotheque_sons/GeneralUser_GS_1.442-MuseScore/GeneralUser GS 1.442 MuseScore/"
//						+ sbpath);

		// 97 -> plantage SoftLinearResampler2, : -19532

//		
//		
//		String sbpath = "42TypeOdin.sf2";
//		File outFolder = new File("/home/use/projets/2022-02_Orgue_Electronique/work/sons/Instrument-Flute/sbflutes");
//		outFolder.mkdirs();
//		File insf2 = new File(
//				"/home/use/projets/2022-02_Orgue_Electronique/work/sons/Instrument-Flute/sbflutes/" + sbpath);
//		

		assert insf2.exists();
		renderAllInstrumentsInSoundBank(insf2, outFolder);

	}

}
