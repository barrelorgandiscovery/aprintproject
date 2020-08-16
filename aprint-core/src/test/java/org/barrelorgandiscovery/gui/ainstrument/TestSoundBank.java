package org.barrelorgandiscovery.gui.ainstrument;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.barrelorgandiscovery.instrument.SampleMapping;
import org.barrelorgandiscovery.instrument.sample.ManagedAudioInputStream;
import org.barrelorgandiscovery.instrument.sample.SoundSample;
import org.barrelorgandiscovery.tools.MidiHelper;
import org.junit.Test;

import com.sun.media.sound.SF2Soundbank;

public class TestSoundBank {

	private ManagedAudioInputStream createSin() throws Exception {

		File f = File.createTempFile("sin", ".wav");
		System.out.println("creating file " + f.getAbsolutePath());

		final double sampleRate = 44100.0;
		final double frequency = 440;
		final double amplitude = 1.0;
		final double seconds = 2.0;
		final double twoPiF = 2 * Math.PI * frequency;
		
		float[] buffer = new float[(int) (seconds * sampleRate)];

		for (int sample = 0; sample < buffer.length; sample++) {
			double time = sample / sampleRate;
			buffer[sample] = (float) (amplitude * Math.sin(twoPiF * time));
		}

		final byte[] byteBuffer = new byte[buffer.length * 2];

		int bufferIndex = 0;
		for (int i = 0; i < byteBuffer.length; i++) {
			final int x = (int) (buffer[bufferIndex++] * 32767.0);

			byteBuffer[i++] = (byte) x;
			byteBuffer[i] = (byte) (x >>> 8);
		}

		
		final boolean bigEndian = false;
		final boolean signed = true;

		final int bits = 16;
		final int channels = 1;

		AudioFormat format = new AudioFormat((float) sampleRate, bits, channels, signed, bigEndian);
		ByteArrayInputStream bais = new ByteArrayInputStream(byteBuffer);
		AudioInputStream audioInputStream = new AudioInputStream(bais, format, buffer.length);
		AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, f);
		audioInputStream.close();
		
		return new ManagedAudioInputStream(f, format, byteBuffer.length );
		

	}
	
	@Test
	public void testMidiNote() {
		System.out.println(MidiHelper.hertz(69));
	}

	// @Test
	public void testSoundBankCreation() throws Exception {
		SBCreator sb = new SBCreator();
		
		ManagedAudioInputStream m = createSin();
		
		
		SoundSample s = new SoundSample("sin", 69, m);
		
		
		SampleMapping sample = new SampleMapping(s, 0, 127);
		
			
		SF2Soundbank b = sb.createSimpleSoundBank(new SampleMapping[] {sample});
		
		SBPlayer sbPlayer = new SBPlayer();
		sbPlayer.open();
		sbPlayer.changeCurrentSoundBank(b);
		
		
		
		sbPlayer.playNote(69);
		sbPlayer.playNote(MidiHelper.midiCode("C5"));
		
		
		Thread.sleep(3000);
		sbPlayer.close();
	}

}
