package org.barrelorgandiscovery.playsubsystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.instrument.SampleMapping;
import org.barrelorgandiscovery.instrument.sample.SoundSample;
import org.barrelorgandiscovery.playsubsystem.SynthCompiler.SoundRef;


class SoundMapping {
	
	private static Logger logger = Logger.getLogger(SoundMapping.class); 

	List<SoundRef> soundRef = new ArrayList<SoundRef>();

	SoundMapping(IEditableInstrument editableInstrument) throws Exception {
		String[] allRegisters = editableInstrument.getPipeStopGroupsAndRegisterName();
		logger.debug("all registers to read :" + Arrays.asList(allRegisters));
		for (String register: allRegisters) {
			List<SoundSample> pslist = editableInstrument.getSoundSampleList(register);
			if (pslist != null && pslist.size() > 0) {
				for (SoundSample s : pslist) {
					SoundRef sRef = new SoundRef();
					sRef.registerSet = register;
					sRef.soundSample = s;

					SampleMapping m = editableInstrument.getSampleMapping(register, s);
					assert m != null;
					sRef.start = m.getFirstMidiCode();
					sRef.end = m.getLastMidiCode();
					logger.debug("sample mapping :" + m);
					soundRef.add(sRef);
				}
			}
		}
		
	}
	
	SoundRef findFirstSoundRef(int midicode, String registerSet) {
		assert registerSet != null;
		boolean registerSetFound = false;
		for (SoundRef s : soundRef) {
			if (registerSet.equals(s.registerSet)) {
				registerSetFound = true;
				if (midicode <= s.end && midicode >= s.start) {
					return s;
				}
			}
		}
		if (!registerSetFound) {
			logger.warn("register set :" + registerSet + " not found");
		}
		return null;
	}
}