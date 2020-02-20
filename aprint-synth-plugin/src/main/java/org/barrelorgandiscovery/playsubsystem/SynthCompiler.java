package org.barrelorgandiscovery.playsubsystem;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.instrument.sample.SoundSample;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * This class compile the virtualbook into synth play events
 * 
 * @author pfreydiere
 */
class SynthCompiler {

	private static Logger logger = Logger.getLogger(SynthCompiler.class);

	static class SoundRef {
		String registerSet;
		SoundSample soundSample;
		long soundId;
		int start;
		int end;
	}

	public static class SynthEvent {
		public SoundRef soundRef;
		public int playMidiCode;
		// ticks and, the value of ticks can be dynamically handled.
		public long start;
		public long end;
	}

	private SoundMapping soundMapping;

	public SynthCompiler(SoundMapping soundMapping) {
		assert soundMapping != null;
		this.soundMapping = soundMapping;
	}

	private String[] addDefaults(String[] registers) {
		ArrayList<String> l = new ArrayList<String>(Arrays.asList(registers));
		return l.toArray(new String[l.size()]);
	}

	public String[] getSectionRegisterAt(VirtualBook vb, long timestamp) {
		int count = vb.getSectionCount();
		int cpt = count - 1;
		while (cpt >= 0) {
			if (timestamp > vb.getSectionStart(cpt)) {
				return addDefaults(vb.getSectionRegisters(cpt));
			}
			cpt--;
		}
		return addDefaults(new String[0]);
	}

	public SynthEvent[] compile(VirtualBook vb) throws Exception {
		assert vb != null;
		ArrayList<SynthEvent> synthEvents = new ArrayList<>();

		Scale s = vb.getScale();
		AbstractTrackDef[] tdefs = s.getTracksDefinition();
		ArrayList<Hole> holes = vb.getOrderedHolesCopy();

		for (Hole h : holes) {

			final int track = h.getTrack();
			assert track >= 0 && track < tdefs.length;
			final AbstractTrackDef def = tdefs[track];
			if (def instanceof NoteDef) {
				NoteDef nd = (NoteDef) def;
				int midiCode = nd.getMidiNote();

				// mid point
				String[] registersToActivate = getSectionRegisterAt(vb, h.getTimestamp() + h.getTimeLength() / 2);

				// add variable registers
				for (String registers : registersToActivate) {
					String registerSet = registers;
					logger.debug("register to activate :" + registerSet);
					SoundRef sref = soundMapping.findFirstSoundRef(midiCode, registerSet);
					if (sref == null) {
						// cannot find mapping
						logger.warn("mapping not found for note :" + midiCode + " and registerset " + registerSet);
						continue;
					}

					// construct event
					SynthEvent synthEvent = new SynthEvent();
					synthEvent.start = h.getTimestamp();
					synthEvent.end = (h.getTimestamp() + h.getTimeLength());
					synthEvent.soundRef = sref;
					synthEvent.playMidiCode = midiCode;
					synthEvents.add(synthEvent);
				}

				// add static registers
				String registerSetName = nd.getRegisterSetName();
				if (registerSetName != null && !registerSetName.isEmpty()) {

					registerSetName += "-DEFAULT";

					SoundRef sref = soundMapping.findFirstSoundRef(midiCode, registerSetName);
					if (sref == null) {
						// cannot find mapping
						logger.warn("no mapping not found for note :" + midiCode + " and registerset " + registerSetName);
						
					} else {

						SynthEvent synthEvent = new SynthEvent();
						synthEvent.start = h.getTimestamp();
						synthEvent.end = (h.getTimestamp() + h.getTimeLength());
						synthEvent.soundRef = sref;
						synthEvent.playMidiCode = midiCode;
						synthEvents.add(synthEvent);
					}
				}
				// add default
				SoundRef sref = soundMapping.findFirstSoundRef(midiCode, "DEFAULT");
				if (sref == null) {
					// cannot find mapping
					logger.warn("mapping not found for note :" + midiCode + " and registerset " + "DEFAULT");
					
				} else {

					SynthEvent synthEvent = new SynthEvent();
					synthEvent.start = h.getTimestamp();
					synthEvent.end = (h.getTimestamp() + h.getTimeLength());
					synthEvent.soundRef = sref;
					synthEvent.playMidiCode = midiCode;
					synthEvents.add(synthEvent);
				}
			}
		}
		return synthEvents.toArray(new SynthEvent[] {});
	}

}
