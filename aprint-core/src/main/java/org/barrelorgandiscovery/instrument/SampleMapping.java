package org.barrelorgandiscovery.instrument;

import org.barrelorgandiscovery.instrument.sample.SoundSample;

/**
 * link with midinotes and soundsample
 * 
 * @author Freydiere Patrice
 * 
 */
public class SampleMapping {

	private SoundSample soundSample;
	private int firstMidiCode;
	private int lastMidiCode;

	public SampleMapping(SoundSample soundsample, int firstmidicode,
			int lastmidicode) {
		this.soundSample = soundsample;
		this.firstMidiCode = firstmidicode;
		this.lastMidiCode = lastmidicode;
	}

	public SoundSample getSoundSample() {
		return soundSample;
	}

	public int getFirstMidiCode() {
		return firstMidiCode;
	}

	public int getLastMidiCode() {
		return lastMidiCode;
	}

	public void setFirstMidiCode(int firstMidiCode) {
		this.firstMidiCode = firstMidiCode;
	}

	public void setLastMidiCode(int lastMidiCode) {
		this.lastMidiCode = lastMidiCode;
	}

	public void setSoundSample(SoundSample soundSample) {
		this.soundSample = soundSample;
	}

	@Override
	public String toString() {
		return "SampleMapping : " + soundSample + " , " + this.firstMidiCode
				+ " -> " + this.lastMidiCode;
	}

}
