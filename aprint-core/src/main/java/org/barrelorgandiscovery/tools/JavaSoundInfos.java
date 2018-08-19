package org.barrelorgandiscovery.tools;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.Mixer.Info;

import org.apache.log4j.Logger;

public class JavaSoundInfos {

	private static Logger logger = Logger.getLogger(JavaSoundInfos.class);

	/**
	 * dump SourceDataLines
	 */
	public static void dumpSourceDataLines() {
		Info[] mixerInfo = AudioSystem.getMixerInfo();
		for (int i = 0; i < mixerInfo.length; i++) {
			Info info = mixerInfo[i];

			logger.info("Mixer name :" + info.getName() + ", "
					+ info.getVendor());

			Mixer mixer = AudioSystem.getMixer(info);
			javax.sound.sampled.Line.Info[] sourceLineInfo = mixer
					.getSourceLineInfo();

			for (int j = 0; j < sourceLineInfo.length; j++) {

				if (sourceLineInfo[j] instanceof SourceDataLine.Info) {

					javax.sound.sampled.SourceDataLine.Info linfo = (javax.sound.sampled.SourceDataLine.Info) sourceLineInfo[j];
					logger.info("   SourceDataLine : " + linfo);

					AudioFormat[] formats = linfo.getFormats();
					for (int k = 0; k < formats.length; k++) {
						AudioFormat audioFormat = formats[k];
						logger.info("      format " + k + ":" + audioFormat);
					}

				}
			}

		}
	}

}
