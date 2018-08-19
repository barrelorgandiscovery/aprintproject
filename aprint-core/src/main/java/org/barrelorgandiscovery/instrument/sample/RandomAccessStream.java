package org.barrelorgandiscovery.instrument.sample;

import java.io.File;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

/**
 * Asynchrone RandomAccess File Reading like .net asynchronous IO :-)
 */
public class RandomAccessStream {

	private ManagedAudioInputStream mais;

	private File workFile = null;
	private RandomAccessFile workRaf = null;
	private long length = 0;

	private static Logger logger = Logger.getLogger(RandomAccessStream.class);

	public RandomAccessStream(ManagedAudioInputStream mais) throws Exception {

		this.mais = new ManagedAudioInputStream(mais);

		// convert Audio Stream to let display it, in an efficient manner
		RandomAccessFile raf = new RandomAccessFile(mais.getFile(), "r");

		this.workFile = mais.getFile();
		this.workRaf = raf;
		this.length = raf.length();
	}

	public File getFile() {
		return this.workFile;
	}

	public void startRead(long start, long length,
			RandomAccessStreamListener listener) {

		logger.debug("start read " + start + "  length : " + length);

		try {
			synchronized (this) {
				workRaf.seek(start);

				long lefttoread = length;

				byte[] buffer = new byte[8192 * 8];
				while (lefttoread > 0) {
					int cpt = workRaf.read(buffer);
					if (cpt == -1)
						break;

					lefttoread -= cpt;
					if (lefttoread > 0 && cpt == buffer.length) {
						listener.dataReceived(buffer);
					} else {
						// must take a subset ...

						int l;

						if (lefttoread > 0) {
							l = cpt;
						} else {
							l = (int) (lefttoread + cpt);
						}

						byte[] b = new byte[l];
						if (logger.isDebugEnabled()) {
							logger.debug("coping " + l + " octet from buffer");
						}
						System.arraycopy(buffer, 0, b, 0, l);
						logger.debug("call datareceived");
						if (listener.dataReceived(b))
							break;

					}
				}
				logger.debug("call endofstream");
				listener.endOfStream();

			}
		} catch (Exception ex) {
			logger.error("startRead", ex);
		}

	}

	public void dispose() {
		try {
			this.mais.close();
		} catch (Exception ex) {
			logger.error("dispose", ex);
		}
	}

	public long getFileLength() {
		return this.length;
	}

}
