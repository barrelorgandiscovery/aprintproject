package org.barrelorgandiscovery.playsubsystem;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.listeningconverter.EcouteConverter;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

import com.sun.media.sound.SoftSynthesizer;


@Deprecated
public class GervillPlaySubSystem implements PlaySubSystem, NeedInstrument {

	private static Logger logger = Logger.getLogger(GervillPlaySubSystem.class);

	public GervillPlaySubSystem() {

	}

	private org.barrelorgandiscovery.instrument.Instrument currentInstrument = null;

	/* (non-Javadoc)
	 * @see fr.freydierepatrice.playsubsystem.NeedInstrument#setCurrentInstrument(fr.freydierepatrice.instrument.Instrument)
	 */
	public void setCurrentInstrument(
			org.barrelorgandiscovery.instrument.Instrument ins) {
		this.currentInstrument = ins;
	}

	/* (non-Javadoc)
	 * @see fr.freydierepatrice.playsubsystem.NeedInstrument#getCurrentInstrument()
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

	private IPlaySubSystemFeedBack fb = null;

	/**
	 * The current play owner
	 */
	private Object owner = null;
	
	public PlayControl play(Object owner, final VirtualBook transposedCarton,
			final IPlaySubSystemFeedBack feedBack, final long startAt)
			throws Exception {

		this.owner = owner;
		
		sequencer = MidiSystem.getSequencer();

		Sequence seq = EcouteConverter.convert(transposedCarton);

		sequencer.setSequence(seq);
		

		if (currentInstrument != null) {
			// play with custom sound ...

			org.barrelorgandiscovery.instrument.Instrument ins = currentInstrument;

			Synthesizer synth = new SoftSynthesizer();

			logger.info("max Polyphony :" //$NON-NLS-1$
					+ synth.getMaxPolyphony());

			synth.open();

			if (lastplayedinstrument != null) {
				synth.unloadAllInstruments(lastplayedinstrument);
			}

			Soundbank sb = ins.openSoundBank();
			if (sb == null) {
				throw new Exception("Fail to load instrument"); //$NON-NLS-1$
			}

			if (synth.loadAllInstruments(sb) == false) {
				throw new Exception("Fail to load sound ... "); //$NON-NLS-1$
			}

			lastplayedinstrument = sb;

			logger.debug("IsSoundbankSupported " //$NON-NLS-1$
					+ synth.isSoundbankSupported(sb));

			Instrument[] listavailable = synth.getLoadedInstruments();
			logger.debug("loaded instruments " //$NON-NLS-1$
					+ listavailable.length);

			if (logger.isDebugEnabled()) {
				for (int i = 0; i < listavailable.length; i++) {
					logger.debug("Instrument " //$NON-NLS-1$
							+ listavailable[i].getName());
					logger.debug("bank " //$NON-NLS-1$
							+ listavailable[i].getPatch().getBank());
					logger.debug("program " //$NON-NLS-1$
							+ listavailable[i].getPatch().getProgram());

				}

			}

			Soundbank defaultbank = synth.getDefaultSoundbank();
			if (defaultbank == null) {
				logger.debug("no default bank"); //$NON-NLS-1$
			}

			if (defaultbank != null) {

				logger.debug("remap the instrument"); //$NON-NLS-1$

				if (!synth.remapInstrument(defaultbank.getInstruments()[0], sb
						.getInstruments()[0])) {
					logger.error("fail to remap instrument"); //$NON-NLS-1$

				}

				logger.debug("bank - " //$NON-NLS-1$
						+ defaultbank.getInstruments()[0].getPatch().getBank());
				logger.debug("program - " //$NON-NLS-1$
						+ defaultbank.getInstruments()[0].getPatch()
								.getProgram());
			} else {
				throw new Exception(Messages.getString("APrint.72")); //$NON-NLS-1$

			}

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
					
				} catch (Throwable ex) {

					sequencer.stop();
					sequencer.close();
					GervillPlaySubSystem.this.owner = null;

				}

			}
		});

		Thread oldOne = currentPlayingThread.getAndSet(t);
		
		fb.playStarted();
		
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

	public void stop() throws Exception {
		Thread oldOne = currentPlayingThread.getAndSet(null);
		if (oldOne != null) {
			oldOne.stop();
			
		}

		owner = null;
		
		if (fb != null)
			fb.playStopped();
	}

	public Object getOwner() {
		return owner;
	}
	
}
