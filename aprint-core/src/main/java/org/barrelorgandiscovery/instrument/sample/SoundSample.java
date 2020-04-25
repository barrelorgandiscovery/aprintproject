package org.barrelorgandiscovery.instrument.sample;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;

import org.apache.log4j.Logger;


/**
 * A sound sample for the instrument .. the sound sample maintain a reference
 * with a stream for minimizing the memory footprint
 * 
 * @author Freydiere Patrice
 * 
 */
public class SoundSample {

	private static Logger logger = Logger.getLogger(SoundSample.class);

	private String name;
	private int midiRootNote;
	private long loopStart = -1;
	private long loopEnd = -1;
	private float freq;

	private ManagedAudioInputStream ais = null;

	/**
	 * Contruct a sound sample with predefined values
	 * 
	 * @param name
	 *            the name of the sample
	 * @param midiRootNote
	 *            the midi root note
	 * @param wavdata
	 *            the wavdata associated
	 */
	public SoundSample(String name, int midiRootNote,
			ManagedAudioInputStream ais) throws IOException {

		logger.debug("creating a soundsample");

		assert name != null;
		this.name = name;
		this.midiRootNote = midiRootNote;

		try {
			this.ais = new ManagedAudioInputStream(ais); // = new
			// AudioFormat(freq,
			// 16, 1, true,
			// false);
		} catch (Exception ex) {
			logger.error("exception in constructing the sample :"
					+ ex.getMessage(), ex);
			throw new IOException(ex.getMessage());
		}
		ais.mark(0);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMidiRootNote() {
		return midiRootNote;
	}

	public void setMidiRootNote(int midiRootNote) {
		this.midiRootNote = midiRootNote;
	}

	public void setLoopStart(long loopStart) {
		this.loopStart = loopStart;
	}

	public void setLoopEnd(long loopEnd) {
		this.loopEnd = loopEnd;
	}

	/**
	 * return -1 if the sound has no loop
	 * @return
	 */
	public long getLoopStart() {
		return loopStart;
	}

	public long getLoopEnd() {
		return loopEnd;
	}

	public AudioFormat getFomat() {
		return ais.getFormat();
	}

	/**
	 * create a new ManagedStream for the sound, 
	 * managed audio stream contains the sound, 
	 * the stream is ready to play
	 * @return
	 * @throws Exception
	 */
	public ManagedAudioInputStream getManagedAudioInputStream()
			throws Exception {
		ManagedAudioInputStream retvalue = new ManagedAudioInputStream(ais);
		retvalue.reset();
		return retvalue;
	}

	public void dispose() {
		try {
			logger.debug("dispose");

			if (ais != null)
				ais.close();

			ais = null;

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Override
	protected void finalize() throws Throwable {

		dispose();
		super.finalize();
	}

}
