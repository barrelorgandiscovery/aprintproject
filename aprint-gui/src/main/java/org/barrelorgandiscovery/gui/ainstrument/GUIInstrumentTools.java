package org.barrelorgandiscovery.gui.ainstrument;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.ProgressIndicator;
import org.barrelorgandiscovery.instrument.sample.ManagedAudioInputStream;
import org.barrelorgandiscovery.instrument.sample.SoundSample;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.StreamsTools;

public class GUIInstrumentTools {

	private static Logger logger = Logger.getLogger(GUIInstrumentTools.class);

	public static AudioFormat TARGET_FORMAT = new AudioFormat(44000.0f, 16, 1, true, false);

	/**
	 * loadwav file from stream
	 * 
	 * @param wavStream the stream
	 * @param name
	 * @return the sound sample
	 * @throws Exception
	 */
	public static SoundSample loadWavFile(InputStream wavStream, String name) throws Exception {
		assert wavStream != null;
		assert name != null;
		assert !name.isEmpty();
		try {

			// perhaps a conversion to be done ... OK Done !!

			AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(wavStream));

			logger.debug("convert wav ..."); //$NON-NLS-1$
			AudioFormat f = ais.getFormat();
			System.out.println(Messages.getString("JPatchEditor.11") + f); //$NON-NLS-1$

			AudioInputStream targetInputStream = AudioSystem.getAudioInputStream(TARGET_FORMAT, ais);

			SoundSample ss;
			File tempFile = File.createTempFile("tmp", "wav"); //$NON-NLS-1$ //$NON-NLS-2$

			StreamsTools.copyStream(targetInputStream, new FileOutputStream(tempFile));

			// AudioSystem.write(targetInputStream, Type.WAVE, tempFile);

			ss = new SoundSample(name, -1, new ManagedAudioInputStream(tempFile, TARGET_FORMAT, tempFile.length()));

			return ss;

		} catch (Exception ex) {
			logger.error("loadWavFile", ex); //$NON-NLS-1$
			throw new Exception(ex.getMessage(), ex);
		}
	}

	/**
	 * load wav from file,
	 * 
	 * @param wavFile
	 * @return the sound sample
	 * @throws Exception
	 */
	public static SoundSample loadWavFile(File wavFile) throws Exception {
		assert wavFile != null;
		assert wavFile.exists();
		FileInputStream stream = new FileInputStream(wavFile);
		return loadWavFile(stream, wavFile.getName());
	}

	/**
	 * Adjust the volume ...
	 * 
	 * @param ais
	 * @param factor
	 * @return the managed audio input stream
	 * @throws Exception
	 */
	public static ManagedAudioInputStream adjust(AudioInputStream ais, float factor, ProgressIndicator pi)
			throws Exception {

		File newCroppedFile = File.createTempFile("adjusttmp", "tmp"); //$NON-NLS-1$ //$NON-NLS-2$

		logger.debug("new adjust temporary file :" + newCroppedFile.getAbsolutePath());

		OutputStream fos = new BufferedOutputStream(new FileOutputStream(newCroppedFile), 100000);
		try {
			byte[] chunk = new byte[10000];

			int cpt;
			while ((cpt = ais.read(chunk, 0, (int) chunk.length)) != -1) {

				if (pi != null) {
					pi.progress(0.0, "" + cpt + " read");
				}

				for (int i = 0; i < cpt; i++) {

					byte b1 = chunk[i];

					i++;
					if (i >= chunk.length)
						break;

					byte b2 = chunk[i];

					int y = (b1 + 256 * (int) b2);

					int newy = (int) ((double) y * factor);
					if (newy > Short.MAX_VALUE) {
						newy = Short.MAX_VALUE;
					}

					if (newy < Short.MIN_VALUE) {
						newy = Short.MIN_VALUE;
					}

					fos.write(newy);
					fos.write(newy >> 8);

				}

			}
		} finally {
			fos.close();
		}
		ManagedAudioInputStream mas = new ManagedAudioInputStream(newCroppedFile, ais.getFormat(),
				newCroppedFile.length());

		return mas;

	}

	/**
	 * Crop the current section ...
	 * 
	 * @param start the crop start in time
	 * @param end   the end of the crop
	 * @return the cropped audio stream
	 * @throws Exception
	 */
	public static ManagedAudioInputStream crop(AudioInputStream ais, long start, long end) throws Exception {
		logger.debug("crop " + start + "->" + end); //$NON-NLS-1$ //$NON-NLS-2$

		File newCroppedFile = File.createTempFile("croptmp", "tmp"); //$NON-NLS-1$ //$NON-NLS-2$
		// newCroppedFile.deleteOnExit();

		OutputStream fos = new BufferedOutputStream(new FileOutputStream(newCroppedFile), 100000);
		try {

			// go to the start of the stream ...

			logger.debug("seek to the right position ..."); //$NON-NLS-1$

			byte[] frame = new byte[10000];
			long bytetoskip = start * 2;

			while (bytetoskip > 0) {
				long readbytes = bytetoskip;
				if (bytetoskip > frame.length) {
					readbytes = frame.length;
				}

				if (readbytes == 0)
					break;

				logger.debug("" + readbytes + " bytes to skip"); //$NON-NLS-1$ //$NON-NLS-2$

				int cpt = ais.read(frame, 0, (int) readbytes);

				logger.debug("" + cpt + " bytes skipped"); //$NON-NLS-1$ //$NON-NLS-2$

				if (cpt <= 0) // == because reading an integral number of
					// frames
					break;

				bytetoskip -= cpt;
			}

			logger.debug("ok at the right place ... copy elements ... "); //$NON-NLS-1$

			byte[] buffer = new byte[10000];
			long cpt = end * 2 - start * 2;
			while (cpt > 0) {
				int m = Math.min(buffer.length, (int) cpt);
				int readedbytes = ais.read(buffer, 0, m);
				if (readedbytes > 0) {
					fos.write(buffer, 0, readedbytes);
					cpt -= readedbytes;
				} else {
					break;
				}
			}

			logger.debug("end of copying the file ... "); //$NON-NLS-1$
		} finally {
			fos.close();
		}

		// AudioInputStream mas = new AudioInputStream(new BufferedInputStream(
		// new FileInputStream(newCroppedFile)), ais.getFormat(),
		// newCroppedFile.length());

		ManagedAudioInputStream mas = new ManagedAudioInputStream(newCroppedFile, ais.getFormat(),
				newCroppedFile.length());

		return mas;

	}

}
