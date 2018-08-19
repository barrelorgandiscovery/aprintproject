package org.barrelorgandiscovery.instrument.sample;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.barrelorgandiscovery.instrument.sample.metadata.SoundSampleMetadata;
import org.barrelorgandiscovery.tools.StreamsTools;

/**
 * Sound Sample Input/Output Object
 * 
 * @author use
 * 
 */
public class SoundSampleIO {

	public static final String SOUNDSAMPLEEXTENSION = "soundsample";
	
	
	/**
	 * save a sample in a stream
	 * 
	 * @param s
	 * @param metadata
	 *            the soundsample metadata
	 * @param os
	 * @throws Exception
	 */
	public void saveSample(SoundSample s, SoundSampleMetadata metadata,
			OutputStream os) throws Exception {

		ObjectOutputStream oos = new ObjectOutputStream(os);

		SoundSampleMetadata m = metadata;
		if (m == null) {
			m = new SoundSampleMetadata();
			m.setName(s.getName());
			m.setRootKey(s.getMidiRootNote());
			m.setLoopStart(s.getLoopStart());
			m.setLoopEnd(s.getLoopEnd());
		}

		m.writeExternal(oos);

		AudioSystem.write(s.getManagedAudioInputStream(), Type.WAVE, oos);

	}

	/**
	 * read a sample from a stream
	 * 
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public SoundSample readSample(InputStream is) throws Exception {

		ObjectInputStream dis = new ObjectInputStream(is);
		SoundSampleMetadata m = new SoundSampleMetadata();
		m.readExternal(dis);

		// write in a temp file the wav informations
		File f = File.createTempFile("tmpsample", "tmpss");
		FileOutputStream fos = new FileOutputStream(f);
		StreamsTools.copyStream(dis, fos);
		fos.close();

		AudioInputStream ais = AudioSystem.getAudioInputStream(f);

		ManagedAudioInputStream mais = new ManagedAudioInputStream(
				new ManagedAudioInputStream.NonManagedInputStream(ais));

		SoundSample ss = new SoundSample(m.getName(), m.getRootKey(), mais);
		// get loop informations from the sound metadata informations
		ss.setLoopStart(m.getLoopStart());
		ss.setLoopEnd(m.getLoopEnd());

		return ss;
	}

	/**
	 * Read metadata informations about a sample
	 * 
	 * @return
	 * @throws Exception
	 */
	public SoundSampleMetadata readSoundSampleMetadata(InputStream is)
			throws Exception {
		ObjectInputStream dis = new ObjectInputStream(is);
		SoundSampleMetadata m = new SoundSampleMetadata();
		m.readExternal(dis);
		return m;

	}

}
