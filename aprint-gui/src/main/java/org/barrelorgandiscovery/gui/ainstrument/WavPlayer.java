package org.barrelorgandiscovery.gui.ainstrument;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.instrument.sample.ManagedAudioInputStream;


/**
 * Class permitting to play a wav / sound in asynchronous way...
 * 
 * @author Freydiere Patrice
 * 
 */
public class WavPlayer {

	private static Logger logger = Logger.getLogger(WavPlayer.class);

	private boolean isCancelled = false;
	private boolean isPlaying = false;

	public WavPlayer() {

	}

	/**
	 * Blocking method to Play the AudioStream and fire events if the listener
	 * is not null
	 * 
	 * @param as
	 *            the audiostream
	 * @param listener
	 *            listeners for the events
	 * @throws Exception
	 */
	public void playSound(ManagedAudioInputStream as, WavPlayerListener listener)
			throws Exception {
		try {

			if (as == null)
				return;

			synchronized (this) {
				if (isPlaying)
					throw new Exception("Wav player is currently playing"); //$NON-NLS-1$
				isPlaying = true;
			}
			final WavPlayerListener currentlistener = listener;

			SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(as
					.getFormat());

			sourceDataLine.open(as.getFormat());
			sourceDataLine.start();
			synchronized (this) {
				if (currentlistener != null)
					currentlistener.startPlaying();
			}

			try {
				byte[] buffer = new byte[10000];
				int cpt = 0;
				while (((cpt = as.read(buffer)) != -1) && !isCancelled) {
					sourceDataLine.write(buffer, 0, cpt);
					if (currentlistener != null) {
						currentlistener.playStateChanged(sourceDataLine
								.getLongFramePosition());
					}
				}

				// Flushing the line ...
				int left = 5;
				while (left-- > 0) {
					Thread.sleep(200);
					if (currentlistener != null) {
						currentlistener.playStateChanged(sourceDataLine
								.getLongFramePosition());
					}
				}
				synchronized (this) {
					if (currentlistener != null)
						currentlistener.playStopped();
				}

			} finally {
				sourceDataLine.stop();
				synchronized (this) {
					this.isPlaying = false;
					this.isCancelled = false;
				}
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Play the sample using the loops
	 * 
	 * @param ais
	 * @param listener
	 * @param loopPProvider
	 */
	public void playSoundWithLoops(ManagedAudioInputStream as,
			WavPlayerListener listener, LoopParameterProvider loopPProvider) {

		try {

			if (as == null || loopPProvider == null) {
				logger.debug("bad parameters"); //$NON-NLS-1$
				return;
			}

			long currentPos = 0;
			as.reset();

			synchronized (this) {
				if (isPlaying)
					throw new Exception("Wav player is currently playing"); //$NON-NLS-1$
				isPlaying = true;
			}

			final WavPlayerListener currentlistener = listener;

			SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(as
					.getFormat());

			sourceDataLine.open(as.getFormat());
			sourceDataLine.start();
			synchronized (this) {
				if (currentlistener != null)
					currentlistener.startPlaying();
			}

			try {

				byte[] buffer = new byte[100000];
			
				while (!isCancelled) {
					long end = loopPProvider.getEndLoop();
					if (end <= 0)
						break;

					long toread = end - currentPos;

					if (toread <= as.getFormat().getFrameSize()) {
						logger.debug("reset position .... "); //$NON-NLS-1$
						// reset position
						currentPos = loopPProvider.getStartLoop();
						if (currentPos <= 0)
							break;

						as.reset();
						as.skip(currentPos);
						toread = end - currentPos;
					}

					long bytesToRead = Math.min(toread, buffer.length);
					int read = as.read(buffer, 0, (int) bytesToRead);

					currentPos += read;
					sourceDataLine.write(buffer, 0, read);
					if (currentlistener != null) {
						currentlistener.playStateChanged(currentPos);
					}

				}

				// Flushing the line ...
				int left = 5;
				while (left-- > 0) {
					Thread.sleep(200);
					if (currentlistener != null) {
						currentlistener.playStateChanged(sourceDataLine
								.getLongFramePosition());
					}
				}
				synchronized (this) {
					if (currentlistener != null)
						currentlistener.playStopped();
				}

			} finally {
				sourceDataLine.stop();
				synchronized (this) {
					this.isPlaying = false;
					this.isCancelled = false;
				}
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

	}

	public void cancelPlay() {
		synchronized (this) {
			isCancelled = true;
		}
	}

	public boolean isPlaying() {
		synchronized (this) {
			return this.isPlaying;
		}
	}

}
