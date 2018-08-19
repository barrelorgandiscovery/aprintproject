package org.barrelorgandiscovery.instrument.sample;

/**
 * Mapping of a sample on a key range ...
 * 
 * @author Freydiere Patrice
 */
public class SoundRegion {

	private SoundSample sample = null;
	private int keystart;
	private int keyend;

	public SoundRegion(SoundSample sample, int keystart, int keyend) {
		assert sample != null;
		this.sample = sample;
		this.keystart = keystart;
		this.keyend = keyend;
	}

	public SoundSample getSample() {
		return sample;
	}

	public int getKeyStart() {
		return keystart;
	}

	public int getKeyEnd() {
		return keyend;
	}

}
