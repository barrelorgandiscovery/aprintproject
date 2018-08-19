package org.barrelorgandiscovery.playsubsystem;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;
import javax.sound.midi.MidiDevice.Info;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.listeningconverter.EcouteConverter;
import org.barrelorgandiscovery.listeningconverter.MIDIListeningConverter;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class MidiDevicePlaySubSystem implements PlaySubSystem,
		NeedMidiListeningConverter {

	private static Logger logger = Logger
			.getLogger(MidiDevicePlaySubSystem.class);

	public MidiDevicePlaySubSystem() {

	}

	public boolean isPlaying() throws Exception {

		return false;
	}

	private Sequencer sequencer = null;

	private MidiDevice midiDevice = null;

	private AtomicReference<Thread> currentPlayingThread = 
		new AtomicReference<Thread>();

	private IPlaySubSystemFeedBackWrapper fb = null;

	private Info midiDeviceName;

	/**
	 * The current play owner
	 */
	private Object owner = null;

	public void setCurrentMidiDevice(Info mididevice) {
		this.midiDeviceName = mididevice;
	}

	public Info getCurrentMidiDevice()
	{
		return this.midiDeviceName;
	}
	
	/**
	 * Midi canal used for the play
	 */
	private int midiCanal = 0;

	/**
	 * set the midi canal used for the play
	 * @param midiCanal
	 */
	public void setMidiCanal(int midiCanal) {
		this.midiCanal = midiCanal;
	}

	/**
	 * Get the midi canal
	 * @return
	 */
	public int getMidiCanal() {
		return midiCanal;
	}

	/*
	 * (non-Javadoc)
	 * @see org.barrelorgandiscovery.playsubsystem.PlaySubSystem#play(java.lang.Object, org.barrelorgandiscovery.virtualbook.VirtualBook, org.barrelorgandiscovery.playsubsystem.IPlaySubSystemFeedBack, long)
	 */
	public PlayControl play(Object owner, final VirtualBook transposedCarton,
			final IPlaySubSystemFeedBack feedBackinterface, final long startAt)
			throws Exception {

		this.owner = owner;
		
		this.fb = new IPlaySubSystemFeedBackWrapper(feedBackinterface);

		Info selectedInfo = null;
		Info[] infos = MidiSystem.getMidiDeviceInfo();

		if (logger.isDebugEnabled()) {
			for (int i = 0; i < infos.length; i++) {

				Info info = infos[i];
				logger.debug("midi device :\"" + info.getName() + "\"");
			}
		}

		for (int i = 0; i < infos.length; i++) {

			Info info = infos[i];
			
			if (selectedInfo == null)
				selectedInfo = info;

			logger.debug(info.getName() + " - " + info.getDescription());
			if (info.equals(midiDeviceName)) {
				
				logger.debug("found midi device ...");
				selectedInfo = info;
				break;
			}
		}

		if (selectedInfo == null)
			throw new Exception("Device " + midiDeviceName + " not found");

		logger.debug("current one :" + selectedInfo.getName());

		MidiDevice md = MidiSystem.getMidiDevice(selectedInfo);
	
		
		md.open();
		try {

			sequencer = MidiSystem.getSequencer();

			Sequence seq = convertToMidiSequence(transposedCarton);

			final long sequenceLenghtInMicroseconds = seq
			.getMicrosecondLength();
			
			sequencer.setSequence(seq);

			logger.debug("Liste des transmitters"); //$NON-NLS-1$
			List<Transmitter> trans = sequencer.getTransmitters();
			for (int i = 0; i < trans.size(); i++) {
				logger.debug("Transmitter " + i + " : " //$NON-NLS-1$ //$NON-NLS-2$
						+ trans.toString());
				trans.get(i).close();
			}

			Receiver receiver = md.getReceiver();
			Transmitter transmitter = sequencer.getTransmitter();
			transmitter.setReceiver(receiver);

			logger.debug("opening sequencer");

			sequencer.open();
			sequencer.start();

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

							long n = fb.informCurrentPlayPosition(pos);

							
							if (pos >= sequenceLenghtInMicroseconds - 100) {
								stop();
								fb.playFinished();
							}
							
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
						MidiDevicePlaySubSystem.this.owner = null;

					}
				}
			});

			this.midiDevice = md;

			Thread oldOne = currentPlayingThread.getAndSet(t);

			fb.playStarted();

			if (oldOne != null)
				oldOne.stop();

			t.start();

			logger.debug("tread started");
			
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

		} catch (Exception ex) {
			logger.error("Exception :" + ex.getMessage(), ex);
			md.close();
			throw new Exception(ex.getMessage(), ex);
		}

	}

	protected Sequence convertToMidiSequence(final VirtualBook transposedCarton)
			throws Exception {

		if (currentConverter != null) {
			return currentConverter.convert(transposedCarton);
		}

		return EcouteConverter.convert(transposedCarton, 384, midiCanal);

	}

	public void stop() throws Exception {
		Thread oldOne = currentPlayingThread.getAndSet(null);
		if (oldOne != null) {
			oldOne.stop();
		}

		this.owner = null;

		if (this.midiDevice != null) {
			try {
				midiDevice.close();
			} catch (Exception ex) {
				logger.warn("Exception when closing the mididevice");
			}
			midiDevice = null;
		}

		if (fb != null)
			fb.playStopped();

	}

	public Object getOwner() {
		return owner;
	}

	private MIDIListeningConverter currentConverter = null;

	public void setCurrentMidiListeningConverter(
			MIDIListeningConverter converter) {
		logger.debug("setting converter :" + converter);
		this.currentConverter = converter;

	}

	public MIDIListeningConverter getCurrentMidiListeningConverter()
	{
		return this.currentConverter;
	}
	
	public boolean isSupportMidiListeningConverter() {
		
		return true;
	}
}
