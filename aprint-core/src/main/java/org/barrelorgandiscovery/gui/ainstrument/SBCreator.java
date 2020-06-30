package org.barrelorgandiscovery.gui.ainstrument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sound.sampled.AudioFormat;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.instrument.SampleMapping;
import org.barrelorgandiscovery.instrument.sample.ManagedAudioInputStream;
import org.barrelorgandiscovery.instrument.sample.SoundSample;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.StreamsTools;

import com.sun.media.sound.ModelByteBuffer;
import com.sun.media.sound.ModelPatch;
import com.sun.media.sound.SF2Instrument;
import com.sun.media.sound.SF2InstrumentRegion;
import com.sun.media.sound.SF2Layer;
import com.sun.media.sound.SF2LayerRegion;
import com.sun.media.sound.SF2Region;
import com.sun.media.sound.SF2Sample;
import com.sun.media.sound.SF2Soundbank;

public class SBCreator {

	public static final String DRUMSINSTRUMENTNAME = "DRUMS";
	private static Logger logger = Logger.getLogger(SBCreator.class);

	public SF2Soundbank createSimpleSoundBank(SampleMapping[] mapping)
			throws Exception {

		SF2Soundbank sb = new SF2Soundbank();
		sb.setName("sb"); //$NON-NLS-1$

		SF2Instrument ins = createInstrument(mapping, sb, false);

		sb.addInstrument(ins);

		return sb;

	}

	/**
	 * Sound bank result for the instruments
	 * 
	 * @author Freydiere Patrice
	 * 
	 */
	public static class SF2SoundBankResult {
		public SF2Soundbank soundBank;
		public HashMap<String, Integer> soundbankMapping;
		public Integer percussionDrum;

	}

	public SF2SoundBankResult createSoundBank(IEditableInstrument instrument)
			throws Exception {

		logger.debug("createSoundBank for instrument :" + instrument);

		SF2SoundBankResult r = new SF2SoundBankResult();

		SF2Soundbank sb = new SF2Soundbank();
		sb.setName("sb"); //$NON-NLS-1$

		r.soundBank = sb;

		int program = 0;
		
		
		HashMap<String, Integer> sm = new HashMap<String, Integer>();
		r.soundbankMapping = sm;

		String[] pipeStopGroupsAndRegisters = instrument
				.getPipeStopGroupsAndRegisterName();
		for (String pipeStopGroupAndRegister : pipeStopGroupsAndRegisters) {
			logger.debug("treat pipeStopGroupAndRegister :"
					+ pipeStopGroupAndRegister);

			List<SoundSample> soundSampleList = instrument
					.getSoundSampleList(pipeStopGroupAndRegister);

			logger.debug("SoundSample List :" + soundSampleList.size());

			ArrayList<SampleMapping> mapping = new ArrayList<SampleMapping>();
			for (SoundSample s : soundSampleList) {
				logger.debug("adding mapping :" + s.getName());
				mapping.add(instrument.getSampleMapping(
						pipeStopGroupAndRegister, s));
			}

			logger.debug("creating instrument in sound bank");
			SF2Instrument ins = createInstrument(pipeStopGroupAndRegister,
					program, mapping.toArray(new SampleMapping[0]), sb, false);

			sm.put(pipeStopGroupAndRegister, program);

			logger.debug("adding instrument ... ");

			sb.addInstrument(ins);

			program++;

		}

		// Adding drums
		{

			logger.debug("adding drum instrument in sound bank if has sample mapping for drums");

			// collect the drum sounds

			Scale s = instrument.getScale();

			ArrayList<SampleMapping> drumsMapping = new ArrayList<SampleMapping>();

			PercussionDef[] plist = s.findUniquePercussionDefs();
			for (int i = 0; i < plist.length; i++) {
				PercussionDef percussionDef = plist[i];

				SoundSample mpdrum = instrument
						.getPercussionSoundSample(percussionDef);
				
				if (mpdrum != null) {
					int mcode = percussionDef.getPercussion();
					mpdrum.setMidiRootNote(percussionDef.getPercussion());
					drumsMapping.add(new SampleMapping(mpdrum, mcode, mcode));
				}
			}

			if (drumsMapping.size() > 0) {
				
				logger.debug("instrument has percussion instrument ...");
				
				SF2Instrument ins = createInstrument(DRUMSINSTRUMENTNAME,
						program, drumsMapping.toArray(new SampleMapping[0]), sb, true);
				
				
				
				sm.put(DRUMSINSTRUMENTNAME, program);

				logger.debug("drums added to the instrument " + program);

				logger.debug("adding instrument ... ");

				sb.addInstrument(ins);

				r.percussionDrum = program;
			}

		}

		return r;
	}

	private SF2Instrument createInstrument(SampleMapping[] mapping,
			SF2Soundbank sb, boolean isPercussion) throws Exception, IOException {
		return createInstrument("DEFAULT", 0, mapping, sb, isPercussion);
	}

	private SF2Instrument createInstrument(String instrumentname, int program,
			SampleMapping[] mapping, SF2Soundbank sb, boolean isPercussion) throws Exception,
			IOException {

		logger.debug("creating instrument in " + sb.toString()
				+ " with instrumentname " + instrumentname + " on program "
				+ program);

		

		SF2Instrument ins = new SF2Instrument(sb);
		ins.setName(instrumentname); //$NON-NLS-1$
		
		String layername = instrumentname;
		
		SF2Layer layer = new SF2Layer(sb);

		// Création d'un patch ...
		ModelPatch p = new ModelPatch(isPercussion ? 128 : 0, program, isPercussion); // bank, program ...
		ins.setPatch(p);

		List<SF2InstrumentRegion> listInstrumentRegions = ins.getRegions();

		layer.setName(layername); //$NON-NLS-1$

		for (int i = 0; i < mapping.length; i++) {
			SampleMapping sampleMapping = mapping[i];

			if (sampleMapping == null)
				continue;

			SF2Sample sample = new SF2Sample(sb);

			logger.debug("creating sample mapping " + sampleMapping.getSoundSample().getName()); //$NON-NLS-1$

			sample.setName(sampleMapping.getSoundSample().getName());

			AudioFormat fomat = sampleMapping.getSoundSample().getFomat();
			sample.setSampleRate((long) fomat.getSampleRate());

			sample.setOriginalPitch(1);
			long loopStart = sampleMapping.getSoundSample().getLoopStart();
			long loopEnd = sampleMapping.getSoundSample().getLoopEnd();
			if (loopStart != -1 && loopEnd != -1) {
				logger.debug("setting loop"); //$NON-NLS-1$
				sample.setStartLoop(loopStart);
				sample.setEndLoop(loopEnd);
			}

			// reading the sample content ....
			ManagedAudioInputStream audioInputStream = sampleMapping
					.getSoundSample().getManagedAudioInputStream();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			StreamsTools.copyStream(audioInputStream, baos);

			sample.setData(new ModelByteBuffer(baos.toByteArray()));

			SF2LayerRegion lr = new SF2LayerRegion();

			lr.putBytes(SF2Region.GENERATOR_KEYRANGE, new byte[] {
					(byte) sampleMapping.getFirstMidiCode(),
					(byte) sampleMapping.getLastMidiCode() });
			lr.putInteger(SF2Region.GENERATOR_OVERRIDINGROOTKEY, sampleMapping
					.getSoundSample().getMidiRootNote());
			lr.putInteger(SF2Region.GENERATOR_SAMPLEMODES, 1);
			
			/**
			 * added for smooth the attacks of the instruments
			 */
			lr.putShort(SF2Region.GENERATOR_ATTACKVOLENV, (short)-7000); // -12000 par défaut
			
		
			
			// lr.putInteger(SF2Region.GENERATOR_INITIALATTENUATION, -100);

//			
//			lr.putShort(  SF2Region.GENERATOR_SUSTAINVOLENV,(short) -100);
//			lr.putShort(  SF2Region.GENERATOR_DECAYVOLENV,(short) -100);
//			
//			
			
			lr.setSample(sample);
			
			layer.getRegions().add(lr);
		
		
			
			sb.addResource(sample);
			
		}
		
		SF2InstrumentRegion instrumentRegion = new SF2InstrumentRegion();
		
		instrumentRegion.setLayer(layer);
		
		listInstrumentRegions.add(instrumentRegion);
		
		sb.addResource(layer);
		return ins;
	}
}
