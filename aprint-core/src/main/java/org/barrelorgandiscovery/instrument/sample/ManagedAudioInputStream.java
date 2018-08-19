package org.barrelorgandiscovery.instrument.sample;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.StreamsTools;

/**
 * AudioInputStream that manage a reference counter on a temporary file
 * 
 * @author Freydiere Patrice
 * 
 */
public class ManagedAudioInputStream extends AudioInputStream {

	private static class RandomAccessFileInputStream extends InputStream {

		private RandomAccessFile raf;

		public RandomAccessFileInputStream(File file) throws IOException {
			raf = new RandomAccessFile(file, "r");
		}

		@Override
		public int read() throws IOException {
			return raf.read();
		}

		@Override
		public int read(byte[] b) throws IOException {
			return raf.read(b);
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return raf.read(b, off, len);
		}

		@Override
		public boolean markSupported() {
			return true;
		}

		private long markedPos = 0;

		@Override
		public synchronized void mark(int readlimit) {
			try {
				markedPos = raf.getFilePointer();
			} catch (IOException ex) {
				logger.error(ex.getMessage(), ex);
			}
		}

		@Override
		public synchronized void reset() throws IOException {
			raf.seek(markedPos);
		}

		@Override
		public void close() throws IOException {
			raf.close();
		}
	}

	private static Logger logger = Logger
			.getLogger(ManagedAudioInputStream.class);

	private AudioInputStream ais = null;

	/**
	 * This property is set if the stream reference an other stream ...
	 */
	private ManagedAudioInputStream ref = null;

	private File tempFileToDelete = null;
	private RandomAccessFileInputStream randomAccessFileInputStream = null;

	private AtomicInteger usingCounter = null;

	/**
	 * Copy the file as a managed audio stream ...
	 * 
	 * 
	 * @throws Exception
	 */
	public ManagedAudioInputStream(File file, AudioFormat format,
			long frameLength) throws Exception {
		super(new FileInputStream(file), format, frameLength);

		memorizeOpenCreateStreamLocation();
		
		usingCounter = new AtomicInteger(1);

		File tempFile = File.createTempFile("tempwav", ".tmp");
		FileOutputStream fos = new FileOutputStream(tempFile);
		try {
			StreamsTools.copyStream(new BufferedInputStream(
					new FileInputStream(file)), fos);

			this.tempFileToDelete = tempFile;

		} finally {
			fos.close();
		}
		randomAccessFileInputStream = new RandomAccessFileInputStream(tempFile);
		this.ais = new AudioInputStream(randomAccessFileInputStream,
				super.getFormat(), frameLength);

		this.ref = null;
		this.ais.mark(0);

	}

	/**
	 * class used for specifying the stream is non managed
	 */
	public static class NonManagedInputStream {
		private AudioInputStream ais;

		public NonManagedInputStream(AudioInputStream ais) {
			this.ais = ais;
		}
	}

	/**
	 * Copy the stream in a temporary file for creating the managed stream
	 * 
	 * @param ais
	 * @throws Exception
	 */
	public ManagedAudioInputStream(NonManagedInputStream nmis) throws Exception {
		super(nmis.ais, nmis.ais.getFormat(), nmis.ais.getFrameLength());

		memorizeOpenCreateStreamLocation();
		
		usingCounter = new AtomicInteger(1);

		logger.debug("creating a new Managed Audio Stream");

		File tempFile = File.createTempFile("tempwav", ".tmp");
		FileOutputStream fos = new FileOutputStream(tempFile);
		try {
			StreamsTools.copyStream(nmis.ais, fos);

			this.tempFileToDelete = tempFile;

		} finally {
			fos.close();
		}
		randomAccessFileInputStream = new RandomAccessFileInputStream(tempFile);
		this.ais = new AudioInputStream(randomAccessFileInputStream,
				nmis.ais.getFormat(), nmis.ais.getFrameLength());

		this.ref = null;
		this.ais.mark(0);

	}

	protected File getFile() {
		if (ref != null)
			return ref.getFile();

		return tempFileToDelete;
	}

	public ManagedAudioInputStream(ManagedAudioInputStream mais)
			throws Exception {
		super(mais, mais.getFormat(), mais.getFrameLength());

		memorizeOpenCreateStreamLocation();
		
		this.ais = mais.getRoot().ais;
		this.ref = mais;
		int currentRefCounter = incrementInternalCounter();

		if (logger.isDebugEnabled()) {
			ManagedAudioInputStream root = getRoot();
			try {
				throw new Exception("StackTrace");
			} catch (Exception ex) {
				logger.debug("new ms "
						+ (root.tempFileToDelete == null ? "null"
								: root.tempFileToDelete.getName()), ex);
			}
		}

		logger.debug("current ref counter :" + currentRefCounter);
		ais.reset();
	}
	
	private void memorizeOpenCreateStreamLocation()
	{
		try {
			throw new Exception("internalCreate");
		} catch (Throwable t) {
			this.createdStreamSource = t;
		}
	}

	private int incrementInternalCounter() {
		
		ManagedAudioInputStream r = getRoot();
		
		assert r != null;
		
		if (ref != null)
			return r.incrementInternalCounter();

		return r.usingCounter.incrementAndGet();
	}

	@Override
	public int available() throws IOException {
		return ais.available();
	}

	private boolean isClosed = false;

	private Throwable closedSource = null;
	
	private Throwable createdStreamSource = null;

	protected void internalClose() throws IOException {

		try {
			throw new Exception("internalClose");
		} catch (Throwable t) {
			this.closedSource = t;
		}

		ManagedAudioInputStream r = getRoot();

		int counter = r.usingCounter.decrementAndGet();

		logger.debug("release reference :" + counter);
		if (counter < 0)
			throw new IOException("implementation error ....");

		if (counter <= 0) {
			// closing resources ...
			

			if (r.randomAccessFileInputStream == null) {
				logger.debug("stream already closed");
				return;
			}
			r.randomAccessFileInputStream.close();
			isClosed = true;

			if (r.tempFileToDelete != null) {
				logger.debug("removing " + r.tempFileToDelete.getAbsolutePath()
						+ ":" + r.tempFileToDelete.delete());
			}

			r.ais = null;
			r.tempFileToDelete = null;
			r.randomAccessFileInputStream = null;

		}
		
		isClosed = true;

	}

	private ManagedAudioInputStream getRoot() {
		if (ref != null)
			return ref.getRoot();

		return this;
	}

	@Override
	public void close() throws IOException {

		if (logger.isDebugEnabled()) {
			ManagedAudioInputStream root = getRoot();
			try {
				throw new Exception("StackTrace");
			} catch (Exception ex) {
				logger.debug("closing stream "
						+ (root.tempFileToDelete == null ? "null"
								: root.tempFileToDelete.getName()), ex);
			}
		}

		if (isClosed) {
			logger.error("Stream Already Closed");
			return;
		}

		internalClose();

	}

	@Override
	public AudioFormat getFormat() {
		return ais.getFormat();
	}

	@Override
	public long getFrameLength() {
		return ais.getFrameLength();
	}

	@Override
	public void mark(int readlimit) {
		ais.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return ais.markSupported();
	}

	private void checkNotClosed() throws IOException {
		if (isClosed) {
			if (closedSource != null)
				logger.error("Stream has already been closed at ", closedSource);

			if (createdStreamSource != null)
				logger.error("Stream has been created at ", createdStreamSource);
			
			throw new IOException("stream has been closed");
		}
	}

	@Override
	public int read() throws IOException {

		checkNotClosed();
		return ais.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		checkNotClosed();
		return ais.read(b, off, len);
	}

	@Override
	public int read(byte[] b) throws IOException {
		checkNotClosed();
		return ais.read(b);
	}

	@Override
	public void reset() throws IOException {
		// checkNotClosed();
		ais.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		checkNotClosed();
		return ais.skip(n);
	}

	@Override
	protected void finalize() throws Throwable {

		if (!isClosed) {
			logger.warn("closing an unclosed stream");
			internalClose();
		}

		super.finalize();
	}

}
