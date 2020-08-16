package org.barrelorgandiscovery.playsubsystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Transmitter;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.listeningconverter.VirtualBookToMidiConverter;
import org.barrelorgandiscovery.playsubsystem.prepared.IPreparedCapableSubSystem;
import org.barrelorgandiscovery.playsubsystem.prepared.IPreparedPlaying;
import org.barrelorgandiscovery.playsubsystem.prepared.ISubSystemPlayParameters;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

import gervill.SF2Instrument;
import gervill.SF2Soundbank;
import gervill.SoftSynthesizer;

public class GervillPlaySubSystemWithRegisterInstruments implements
		PlaySubSystem, NeedInstrument, IPreparedCapableSubSystem {

	private static Logger logger = Logger
			.getLogger(GervillPlaySubSystemWithRegisterInstruments.class);

	public GervillPlaySubSystemWithRegisterInstruments() {

	}

	private org.barrelorgandiscovery.instrument.Instrument currentInstrument = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.playsubsystem.NeedInstrument#setCurrentInstrument
	 * (fr.freydierepatrice.instrument.Instrument)
	 */
	public void setCurrentInstrument(
			org.barrelorgandiscovery.instrument.Instrument ins) {
		this.currentInstrument = ins;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.playsubsystem.NeedInstrument#getCurrentInstrument()
	 */
	public org.barrelorgandiscovery.instrument.Instrument getCurrentInstrument() {
		return this.currentInstrument;
	}

	public boolean isPlaying() throws Exception {
		Thread t = currentPlayingThread.get();
		return t != null;
	}

	private Sequencer sequencer = null;

	private Soundbank lastplayedinstrument = null;

	private AtomicReference<Thread> currentPlayingThread = new AtomicReference<Thread>();

	private IPlaySubSystemFeedBackWrapper fb = null;

	/**
	 * The current play owner
	 */
	private Object owner = null;

	private SourceDataLine sourceDataLine;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.playsubsystem.prepared.IPreparedCapableSubSystem
	 * #createParameterInstance()
	 */
	public ISubSystemPlayParameters createParameterInstance() throws Exception {
		GervillPlaySubSystemPreparedParameters p = new GervillPlaySubSystemPreparedParameters();
		if (currentInstrument == null)
			throw new Exception("no current instrument");
		p.instrument = currentInstrument;
		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.playsubsystem.prepared.IPreparedCapableSubSystem
	 * #preparePlaying(org.barrelorgandiscovery.virtualbook.VirtualBook,
	 * org.barrelorgandiscovery.playsubsystem.prepared.ISubSystemPlayParameters)
	 */
	public IPreparedPlaying preparePlaying(VirtualBook transposedVirtualBook,
			ISubSystemPlayParameters params) throws Exception {

		if (params == null
				|| !(params instanceof GervillPlaySubSystemPreparedParameters))
			throw new Exception(
					"bad parameters, must be not null, must be an instance of "
							+ GervillPlaySubSystemPreparedParameters.class
									.getName());
		GervillPlaySubSystemPreparedParameters gp = (GervillPlaySubSystemPreparedParameters) params;
		
		VirtualBookToMidiConverter vbmc = new VirtualBookToMidiConverter(
				gp.instrument);
		
		Sequence seq = vbmc.convert(transposedVirtualBook);

		GervillPlaySubSystemPreparedPlay gpp = new GervillPlaySubSystemPreparedPlay();
		gpp.sequenceToPlay = seq;
		gpp.vbToPlay = transposedVirtualBook;
		gpp.instrument = gp.instrument;

		return gpp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.playsubsystem.prepared.IPreparedCapableSubSystem
	 * #playPrepared(java.lang.Object,
	 * org.barrelorgandiscovery.playsubsystem.prepared.IPreparedPlaying,
	 * org.barrelorgandiscovery.playsubsystem.IPlaySubSystemFeedBack, long)
	 */
	public PlayControl playPrepared(Object owner, IPreparedPlaying pp,
			IPlaySubSystemFeedBack feedBackinterface, final long startAt)
			throws Exception {

		this.owner = owner;

		final IPlaySubSystemFeedBackWrapper feedBack = new IPlaySubSystemFeedBackWrapper(
				feedBackinterface);

		if (!(pp instanceof GervillPlaySubSystemPreparedPlay))
			throw new Exception("preparedplay is not of the proper kind");

		GervillPlaySubSystemPreparedPlay gpp = (GervillPlaySubSystemPreparedPlay) pp;
		assert feedBack != null;

		feedBack.playStarted();

		SoftSynthesizer synth = new SoftSynthesizer();

		logger.info("max Polyphony :" //$NON-NLS-1$
				+ synth.getMaxPolyphony());

		Map<String, Object> infos = setupInfoProperties();

		sourceDataLine = AudioSystem.getSourceDataLine(null);

		synth.open(sourceDataLine, infos);

		logger.info("max Polyphony :" //$NON-NLS-1$
				+ synth.getMaxPolyphony());

		sequencer = MidiSystem.getSequencer(); // open the line implicitly

		final long sequenceLenghtInMicroseconds = gpp.sequenceToPlay
				.getMicrosecondLength();

		sequencer.setSequence(gpp.sequenceToPlay);
		

		if (gpp.instrument != null && lastplayedinstrument != gpp.instrument) {
			// play with custom sound ...

			org.barrelorgandiscovery.instrument.Instrument ins = gpp.instrument;

			if (lastplayedinstrument != null) {
				logger.info("unload all previous instruments");
				synth.unloadAllInstruments(lastplayedinstrument);
			}

			SF2Soundbank soundBank = (SF2Soundbank) ins.openSoundBank();
			if (soundBank == null) {
				throw new Exception("Fail to load instrument"); //$NON-NLS-1$
			}

			if (!synth.isSoundbankSupported(soundBank))
				throw new Exception("unsupported soundBank"); //$NON-NLS-1$

			logger.info("load sound bank");
			if (synth.loadAllInstruments(soundBank) == false) {
				throw new Exception("Fail to load sound ... "); //$NON-NLS-1$
			}

			logger.debug("instruments loaded ..., mapping registers ... "); //$NON-NLS-1$

			SF2Instrument[] instruments = soundBank.getInstruments();
			for (int i = 0; i < instruments.length; i++) {
				SF2Instrument instrument2 = instruments[i];
				String r = instrument2.getName();
				logger.debug("Mapping instrument :" + r);

				int p = i;

				if (!synth.remapInstrument(synth.getDefaultSoundbank()
						.getInstruments()[p], instrument2)) {

					logger.error("fail to remap instrument"); //$NON-NLS-1$

				} else {
					logger.debug("instruments remapped ..."); //$NON-NLS-1$
				}
			}

			lastplayedinstrument = soundBank;

			Soundbank defaultbank = synth.getDefaultSoundbank();
			if (defaultbank == null) {
				logger.debug("no default bank"); //$NON-NLS-1$
			}
			//
			// synth.remapInstrument(
			// synth.getAvailableInstruments()[0], ins
			// .getSoundBank().getInstruments()[0]);

			logger.debug("Liste des transmitters"); //$NON-NLS-1$
			List<Transmitter> trans = sequencer.getTransmitters();
			for (int i = 0; i < trans.size(); i++) {
				logger.debug("Transmitter " + i + " : " //$NON-NLS-1$ //$NON-NLS-2$
						+ trans.toString());
				trans.get(i).close();
			}

			Receiver receiver = synth.getReceiver();

			// reverbe off
			// Set volume to max and turn reverb off
			ShortMessage reverb_off = new ShortMessage();
			reverb_off.setMessage(ShortMessage.CONTROL_CHANGE, 91, 0);
			receiver.send(reverb_off, -1);

			Transmitter transmitter = sequencer.getTransmitter();
			transmitter.setReceiver(receiver);

		}

		sequencer.open();
		sequencer.start();

		fb = feedBack;

		// start At ....

		Thread t = new Thread(new Runnable() {

			public void run() {

				try {

					// adaptative fps
					// by default : 1/25 -> every 40ms

					int timer = 40;

					sequencer.setMicrosecondPosition(startAt);
				
					while (true) {
						// every 100ms we evaluate the position :-)
						Thread.sleep(timer);

						long startnano = System.nanoTime();

						long pos = sequencer.getMicrosecondPosition();

						
						if (pos >= sequenceLenghtInMicroseconds
								|| !sequencer.isRunning() || sequencer.getLoopCount() > 0) {
							try {
								Thread.sleep(1000); // because there might be some delays in the wav output streams
								stop();
							} catch (Throwable t) {
								logger.debug(t.getMessage(), t);
							}
							feedBack.playFinished();
							Thread.sleep(1000);
							break;
						}
						long n = feedBack.informCurrentPlayPosition(pos);

						long nanotime = System.nanoTime() - startnano;

						if (n > 0)
							nanotime += n;

						int credit = (int) (nanotime / 1000000);
						credit *= 2;

						if (credit > timer) {
							timer *= 2;
						} else if (credit / 2 < timer) {
							timer /= 2;
						}

						if (timer < 40)
							timer = 40; // limit the CPU

					}
				} catch(Throwable ex) {
					
				}
				
				finally
				
				
				 {

					sequencer.stop();
					sequencer.close();
					if (sourceDataLine != null)
						sourceDataLine.close();
					sourceDataLine = null;

					GervillPlaySubSystemWithRegisterInstruments.this.owner = null;
				}

			}
		});

		Thread oldOne = currentPlayingThread.getAndSet(t);

		// fb.playStarted();

		if (oldOne != null)
			oldOne.stop();

		t.start();

		return new PlayControl() {

			@Override
			public void setTempo(float newTempo) {
				sequencer.setTempoFactor(newTempo);
			}

			@Override
			public float getTempo() {
				return sequencer.getTempoFactor();
			}
		};

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.playsubsystem.PlaySubSystem#play(java.lang.Object
	 * , org.barrelorgandiscovery.virtualbook.VirtualBook,
	 * org.barrelorgandiscovery.playsubsystem.IPlaySubSystemFeedBack, long)
	 */
	public PlayControl play(Object owner, final VirtualBook transposedCarton,
			final IPlaySubSystemFeedBack feedBack, final long startAt)
			throws Exception {

		logger.debug("play");

		GervillPlaySubSystemPreparedParameters p = (GervillPlaySubSystemPreparedParameters) createParameterInstance();

		IPreparedPlaying preparedPlaying = preparePlaying(transposedCarton, p);

		return playPrepared(owner, preparedPlaying, feedBack, startAt);

	}

	public static Map<String, Object> setupInfoProperties() {

		Map<String, Object> infos = new HashMap<String, Object>();
		// infos.put("interpolation","cubic");
		infos.put("latency", 170_000L); // in ms
		// infos.put("control rate", 22000f);
		infos.put("reverb", Boolean.FALSE);
		infos.put("chorus", Boolean.FALSE);
		infos.put("max polyphony", 256);
		infos.put("midi channels", 64);
		// infos.put("auto gain control", Boolean.FALSE);
		// infos.put("jitter correction", Boolean.FALSE);
		infos.put("large mode", Boolean.TRUE); // slow in some time
		return infos;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.playsubsystem.PlaySubSystem#stop()
	 */
	public void stop() throws Exception {
		Thread oldOne = currentPlayingThread.getAndSet(null);
		if (oldOne != null) {
			try {
			oldOne.stop();
			} catch(Throwable t) {
				
			}
		}
		owner = null;

		if (fb != null) {
			fb.playStopped();
		}

		if (sourceDataLine != null) {
			sourceDataLine.close();
			sourceDataLine = null;
		}
	}

	public Object getOwner() {
		return owner;
	}

}
