package org.barrelorgandiscovery.playsubsystem;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.sound.sampled.AudioFormat;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManager;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.instrument.sample.ManagedAudioInputStream;
import org.barrelorgandiscovery.instrument.sample.SoundSample;
import org.barrelorgandiscovery.playsubsystem.SynthCompiler.SoundRef;
import org.barrelorgandiscovery.playsubsystem.SynthCompiler.SynthEvent;
import org.barrelorgandiscovery.tools.MidiHelper;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.rendering.MusicBoxRendering;

import net.frett27.synthetizer.Synthetizer;
import net.frett27.synthetizer.Synthetizer.BufferPrepare;
import net.frett27.synthetizer.Synthetizer.BufferPrepareFacilities;

public class SynthPlaySubSystem implements PlaySubSystem, NeedInstrument {

	private static Logger logger = Logger.getLogger(SynthPlaySubSystem.class);

	private Object owner;
	
	private IPlaySubSystemFeedBack feedback;

	private EditableInstrumentManagerRepository editableInstrumentManagerRepository;

	private SoundMapping mapping;

	private Instrument currentInstrument;

	private AtomicBoolean canceled = new AtomicBoolean(false);

	ExecutorService es = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setPriority(Thread.MAX_PRIORITY);
			return t;
		}
	});

	private Thread currentRunning;

	public SynthPlaySubSystem(EditableInstrumentManagerRepository editableInstrumentManagerRepository) {
		assert editableInstrumentManagerRepository != null;
		this.editableInstrumentManagerRepository = editableInstrumentManagerRepository;

	}

	@Override
	public void setCurrentInstrument(Instrument ins) {
		currentInstrument = ins;
		try {
			EditableInstrumentManager em = editableInstrumentManagerRepository.getEditableInstrumentManager();
			String associatedIns = editableInstrumentManagerRepository
					.findAssociatedEditableInstrumentName(ins.getName());
			logger.debug("associated ins :" + associatedIns);
			IEditableInstrument editableInstrument = em.loadEditableInstrument(associatedIns);

			
			changeSoundMapping(editableInstrument);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	private void changeSoundMapping(IEditableInstrument editableInstrument) throws Exception {
		
		mapping = new SoundMapping(editableInstrument);
	}
	
	@Override
	public PlayControl play(Object owner, VirtualBook vb, IPlaySubSystemFeedBack feedBack, long pos) throws Exception {
		this.owner = owner;

		this.feedback = feedBack;

		SynthCompiler synthCompiler = new SynthCompiler(mapping);
		final SynthEvent[] compiled = synthCompiler.compile(vb);

		AtomicLong startTime = new AtomicLong(0);
		AtomicInteger index = new AtomicInteger(0);
		
		Synthetizer synthetizer = new Synthetizer();
		
		final AtomicBoolean ended = new AtomicBoolean(false);
		synthetizer.defaultBufferSize = 2_000;

		synthetizer.setPrepareBufferCallBack(new BufferPrepare() {

			@Override
			public void prepareBuffer(BufferPrepareFacilities synth, long startBufferTime, long stopBufferTime) {
				try {
					
					while (index.get() < compiled.length && compiled[index.get()].start + startTime.get() <= stopBufferTime) {

						// consume
						SynthEvent ev = compiled[index.get()];

						if (ev.start + startTime.get() >= startBufferTime) {
							// add the start event
							long voiceId = synth.play(ev.start + startTime.get(), ev.soundRef.soundId,
									(float) MidiHelper.hertz(ev.playMidiCode));
							// add the planned stop
							synth.stop(ev.end + startTime.get(), voiceId);

						}
						// next
						index.incrementAndGet();
					}

					// System.out.println(eventIndex.get());
					if (stopBufferTime > compiled[compiled.length - 1].end + 10_000_000) {
						ended.set(true);
					}
					
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		});

		boolean isMusicBox = currentInstrument.getScale().getRendering() instanceof MusicBoxRendering;
		
		for (SoundRef soundRef : mapping.soundRef) {
			SoundSample soundSample = soundRef.soundSample;
			float[] m = loadSound(soundSample);

			long soundid = synthetizer.loadSample(m, soundSample.getFomat().getSampleRate(),
					(float) MidiHelper.hertz(soundSample.getMidiRootNote()),
					soundSample.getLoopStart() != -1,
					(int)soundSample.getLoopStart(), (int)soundSample.getLoopEnd(),
					
					
					isMusicBox);
			soundRef.soundId = soundid;
		}

		canceled.set(false);
		feedback.playStarted();
		currentRunning = new Thread(() -> {
			try {
				synthetizer.open();
				
				Thread.sleep(1000);
				startTime.set(synthetizer.getEstimateTime());
				
				try {
					
					while (!ended.get()) {
						Thread.sleep(20);
						
						feedback.informCurrentPlayPosition(synthetizer.getEstimateTime() - startTime.get());

						if (canceled.get()) {
							return;
						}
					}

					// Thread.sleep(10_000);
				} catch (Throwable t) {
					t.printStackTrace();
				}

				finally {

					synthetizer.close();
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		});
		currentRunning.start();

		// no control returned
		return new PlayControl() {

			float t;

			@Override
			public void setTempo(float newTempo) {
				t = newTempo;
			}

			@Override
			public float getTempo() {
				return t;
			}
		};
	}

	@Override
	public Object getOwner() {
		return owner;
	}

	@Override
	public boolean isPlaying() throws Exception {
		return currentRunning != null && currentRunning.isAlive();
	}

	@Override
	public void stop() throws Exception {
		canceled.set(true);
		feedback.playStopped();
	}

	public static float[] loadSound(SoundSample s) throws Exception {
		AudioFormat audioFormat = s.getFomat();
		System.out.println(audioFormat);
		ManagedAudioInputStream mis = s.getManagedAudioInputStream();
		System.out.println("frame length :" + mis.getFrameLength());
		int length = (int) mis.getFrameLength();

		float[] memorySound = new float[length];
		byte[] buffer = new byte[2 * 8192 * 4];
		int cpt = 0;
		int readFrames;
		while ( (readFrames =  mis.read(buffer)) != -1) {
			for (int i = 0 ; i < readFrames / 2 ; i ++) {			
				byte b1 = buffer[i * 2];
				byte b2 = buffer[i * 2 + 1];
	
				float v = (float) (((1.0 * b1 + 1.0 * 128 * b2)) / (Short.MAX_VALUE * 1.0));
				// System.out.println(v);
				memorySound[cpt++] = v;
			}
		}
		System.out.println("sample read :" + cpt);
		return memorySound;
	}

}
