package org.barrelorgandiscovery.editableinstrument;

import java.util.HashMap;

import org.barrelorgandiscovery.instrument.sample.SoundSample;


public interface SoundSampleListListener {

	void hashChanged(HashMap<String, SoundSample> hash);

	void soundSampleRemoved(SoundSample sampleRemoved, String pipeStopGroup);

	void soundSampleAdded(SoundSample sampleAdded, String pipeStopGroup);

}
