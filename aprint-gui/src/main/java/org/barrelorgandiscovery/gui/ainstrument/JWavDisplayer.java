package org.barrelorgandiscovery.gui.ainstrument;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.apache.log4j.lf5.util.StreamUtils;
import org.barrelorgandiscovery.instrument.sample.ManagedAudioInputStream;
import org.barrelorgandiscovery.instrument.sample.RandomAccessStream;
import org.barrelorgandiscovery.instrument.sample.RandomAccessStreamListener;
import org.barrelorgandiscovery.tools.Disposable;

/**
 * Class for displaying a wav file / inputStream ...
 * 
 * well, a bit "raw" will be refactored when having time opportunities ...
 * 
 * @author Freydiere Patrice
 * 
 */
public class JWavDisplayer extends JComponent implements Disposable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7492161501276055337L;

	private static Logger logger = Logger.getLogger(JWavDisplayer.class);

	private Executor executor = Executors.newCachedThreadPool();

	private Image waitImage = null;

	public JWavDisplayer() {
		super();

		URL resource = getClass().getResource("cd.png");
		if (resource == null)
			throw new RuntimeException("cd.png not found");
		waitImage = java.awt.Toolkit.getDefaultToolkit().createImage(resource);

		// scale change in the component
		addMouseWheelListener(new MouseWheelListener() {

			public void mouseWheelMoved(MouseWheelEvent e) {

				int wheelRotation = e.getWheelRotation();
				int x = e.getX();

				long timelocation = getPosFromScreen(x);

				double s = getScale();
				if (wheelRotation < 0) {
					s /= Math.abs(wheelRotation - 1);
				} else {
					s *= Math.abs(wheelRotation + 1);
				}

				long newstart = timelocation
						- (long) (1.0 * getWidth() / 2 * s);

				logger.debug("mousewheel modification , new scale :" + s); //$NON-NLS-1$
				logger.debug("mousewheel modification , new start :" //$NON-NLS-1$
						+ newstart);

				if (newstart < 0)
					newstart = 0;

				currentStart = newstart;
				currentscale = s;

				launchNewSampleView();
				relaunchLoopViewIfValid();

			}
		});

		class SampleMouseMotion implements MouseListener, MouseMotionListener {
			private int clickedX = -1;
			private long associatedX = -1;

			public void mouseDragged(MouseEvent e) {

				if (clickedX != -1) {

					logger.debug("moved");
					int delta = e.getX() - clickedX;

					currentStart = associatedX - (long) (delta * currentscale);
					if (logger.isDebugEnabled()) {
						logger.debug("start :" + (currentStart));
					}
					repaint();
				}

			}

			public void mouseMoved(MouseEvent e) {

			}

			public void mouseClicked(MouseEvent e) {

			}

			public void mousePressed(MouseEvent e) {

				if ((e.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) != 0) {
					logger.debug("start moving");
					clickedX = e.getX();
					associatedX = currentStart;

				}

			}

			public void mouseReleased(MouseEvent e) {

				if (clickedX != -1) { // in case the move has not been started
					logger.debug("stop moving");
					clickedX = -1;
					setStart(currentStart); // for reloading the image
				}
			}

			public void mouseEntered(MouseEvent e) {

			}

			public void mouseExited(MouseEvent e) {

			}

		}

		SampleMouseMotion s = new SampleMouseMotion();
		addMouseMotionListener(s);
		addMouseListener(s);

	}

	private AtomicReference<RandomAccessStream> atomicReferenceRas = new AtomicReference<RandomAccessStream>();

	private ManagedAudioInputStream mais;

	public void displayAudioInputStream(ManagedAudioInputStream ais)
			throws Exception {

		if (ais == null) {
			RandomAccessStream ras = atomicReferenceRas.getAndSet(null);

			if (ras != null) {
				ras.dispose();
			}

			if (this.mais != null) {
				this.mais.close();
				this.mais = null;
			}

			sampleImageToShow.set(null);
			loopImageToShow.set(null);
			resetLoopParameters();

			repaint();
			return;
		}

		mais = new ManagedAudioInputStream(ais);

		File tempAISFile = createTempFile();
		tempAISFile.deleteOnExit(); // clean up when the JVM exit ...

		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(tempAISFile, false));

		AudioInputStream convertedAudioStream = AudioSystem
				.getAudioInputStream(new AudioFormat(44000.0f, 16, 1, true,
						false), ais);

		StreamUtils.copy(convertedAudioStream, bos);
		bos.close();

		RandomAccessStream ras = atomicReferenceRas
				.getAndSet(new RandomAccessStream(ais));

		if (ras != null) {
			ras.dispose();
			ras.getFile().delete();
		}

		launchNewSampleView();
	}

	private File createTempFile() throws IOException {
		return File.createTempFile("working_sample", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * get a copy of the audio stream
	 * 
	 * @return
	 * @throws Exception
	 */
	public ManagedAudioInputStream getAudioStream() throws Exception {

		RandomAccessStream ras = atomicReferenceRas.get();

		if (ras == null)
			return null;

		return new ManagedAudioInputStream(mais);
	}

	/**
	 * Ajust the volume of this sample ... the sample is then modified
	 * 
	 * @param factor
	 * @throws Exception
	 */
	public void adjust(float factor) throws Exception {
		logger.debug("adjust gain ...");

		mais.reset();
		ManagedAudioInputStream mas = GUIInstrumentTools.adjust(mais, factor,
				null);

		logger.debug("adjust completed");

		visualReplaceStream(mas);

	}

	/**
	 * Crop the current section ...
	 * 
	 * @param start
	 * @param end
	 * @throws Exception
	 */
	public void crop(long start, long end) throws Exception {
		logger.debug("crop " + start + "->" + end); //$NON-NLS-1$ //$NON-NLS-2$

		RandomAccessStream randomAccessStream = atomicReferenceRas.get();

		if (randomAccessStream == null)
			return;

		File newCroppedFile = createTempFile();
		// newCroppedFile.deleteOnExit();

		FileOutputStream fos = new FileOutputStream(newCroppedFile);

		// go to the start of the stream ...

		RandomAccessFile raf = new RandomAccessFile(
				randomAccessStream.getFile(), "r"); //$NON-NLS-1$
		raf.seek(start * 2);

		byte[] buffer = new byte[10000];
		long cpt = end * 2 - start * 2;
		while (cpt > 0) {
			int m = Math.min(buffer.length, (int) cpt);
			int readedbytes = raf.read(buffer, 0, m);
			if (readedbytes != -1) {
				fos.write(buffer, 0, readedbytes);
				cpt -= readedbytes;
			} else {
				break;
			}
		}

		raf.close();

		mais.reset();
		ManagedAudioInputStream mas = GUIInstrumentTools.crop(mais, start, end);

		visualReplaceStream(mas);

	}

	private void visualReplaceStream(ManagedAudioInputStream mas)
			throws Exception {
		RandomAccessStream ras = atomicReferenceRas
				.getAndSet(new RandomAccessStream(mas));

		this.mais = mas;

		if (ras != null) {
			ras.dispose();
			ras.getFile().delete();
		}

		launchNewSampleView();
	}

	/**
	 * the start sample
	 */
	private long currentStart = 0;

	/**
	 * sample number per pixel
	 */
	private double currentscale = 100.0;

	private void launchNewSampleView() {

		RandomAccessStream randomAccessStream = this.atomicReferenceRas.get();

		if (randomAccessStream == null)
			return;

		int width = getWidth();
		int height = getHeight();

		if (width <= 0 || height <= 0)
			return;

		logger.debug("launch new View, width : " + width + " height :" //$NON-NLS-1$ //$NON-NLS-2$
				+ height);

		final SampleViewProcessor c = new SampleViewProcessor(
				randomAccessStream, width, height, currentStart, currentscale);

		SampleViewProcessor old = referenceViewClass.getAndSet(c);
		if (old != null)
			old.abort();

		// sampleImageToShow.set(null);
		repaint();

		executor.execute(new Runnable() {
			public void run() {
				c.run();
			}
		});

	}

	/**
	 * Launch the execution of the loop view
	 */
	private void launchNewLoopView() {

		RandomAccessStream randomAccessStream = this.atomicReferenceRas.get();

		if (randomAccessStream == null)
			return;

		int width = getWidth();
		int height = getHeight();

		if (width <= 0 || height <= 0)
			return;

		logger.debug("launch new Loop View, width : " + width + " height :" //$NON-NLS-1$ //$NON-NLS-2$
				+ height);

		final LoopViewProcessor c = new LoopViewProcessor(randomAccessStream,
				width, height, currentStart, currentStartLoopPos,
				currentEndLoopPos, currentscale);

		LoopViewProcessor old = referenceLoopViewProcessor.getAndSet(c);
		if (old != null)
			old.abort();

		executor.execute(new Runnable() {
			public void run() {
				c.run();
			}
		});

	}

	/**
	 * a View of the sample view
	 * 
	 * @author use
	 * 
	 */
	private static class SampleView {
		/**
		 * associated image
		 */
		public BufferedImage image = null;
		/**
		 * the scale of the image
		 */
		public double scale;
		/**
		 * the sample start
		 */
		public long start;
	}

	/**
	 * Processor
	 */
	private AtomicReference<SampleViewProcessor> referenceViewClass = new AtomicReference<SampleViewProcessor>();
	private AtomicReference<LoopViewProcessor> referenceLoopViewProcessor = new AtomicReference<LoopViewProcessor>();

	private class LoopViewProcessor implements RandomAccessStreamListener {

		RandomAccessStream ras;
		int width;
		int height;
		long start;
		long startlooppos;
		long endlooppos;
		double scale;

		private long lastposx = 0;
		private int lastposy = 0;

		private boolean isRunning = true;

		LoopViewProcessor(RandomAccessStream ras, int width, int height,
				long start, long startlooppos, long endlooppos, double scale) {
			this.ras = ras;
			this.width = width;
			this.height = height;
			this.start = start;
			this.startlooppos = startlooppos;
			this.endlooppos = endlooppos;
			this.scale = scale;
		}

		private BufferedImage bi = null;
		private Graphics g = null;

		private boolean isAborted;

		private boolean isfirst = true;

		public void run() {
			if (isAborted)
				return;

			long startview = start;
			long endview = start + (long) (width * scale);

			long startloopviewrange = endlooppos;
			long endloopviewrange = endlooppos + (endlooppos - startlooppos);

			if (endloopviewrange < startview || startloopviewrange > endview) {
				signalNewLoopImage(null);
				abort();
				return;
			}

			// range to read and display ...
			long displayreadstart = Math.max(startloopviewrange, startview);
			long displayreadend = Math.min(endloopviewrange, endview);

			this.lastposx = displayreadstart - start;

			this.bi = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);
			this.g = bi.createGraphics();
			g.setPaintMode();
			g.setColor(Color.PINK);

			long length = displayreadend - displayreadstart;

			ras.startRead((startlooppos + (displayreadstart - endlooppos)) * 2,
					length * 2, this);

		}

		public boolean dataReceived(byte[] chunk) {

			if (logger.isDebugEnabled())
				logger.debug("chunk received " + chunk.length); //$NON-NLS-1$

			for (int i = 0; i < chunk.length; i++) {

				int b1 = chunk[i];

				i++;
				if (i >= chunk.length)
					break;

				int b2 = chunk[i];

				int y = (int) ((1.0 * Short.MAX_VALUE / 2.0 - (1.0 * b1 + 1.0 * 128 * b2)) / (Short.MAX_VALUE * 1.0 / height));

				int x = (int) lastposx + 1;

				if (!isfirst) {
					g.drawLine((int) (lastposx / scale), lastposy,
							(int) (x / scale), y);

				}
				isfirst = false;
				lastposx++;
				lastposy = y;

				if (isAborted) {
					return false;
				}
			}

			return isAborted;
		}

		public void endOfStream() {
			g.dispose();
			isRunning = false;

			SampleView v = new SampleView();
			v.scale = this.scale;
			v.start = this.start;
			v.image = this.bi;

			signalNewLoopImage(v);
		}

		public void abort() {
			this.isAborted = true;
		}

		public boolean isRunning() {
			if (isAborted)
				return false;

			return isRunning;
		}

	}

	private static BasicStroke axisStroke = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {
					10.0f, 5.0f, 2.0f }, 0.0f);

	private class SampleViewProcessor implements RandomAccessStreamListener {

		private boolean isAborted = false;

		private int height;
		private int width;
		private RandomAccessStream ras;
		private long start;
		private double scale;

		/**
		 * number of chunk after flushing to the screen, to show progress
		 */
		private int flushScreenChunks = 3;

		private int currentScreenChunkToFlush = flushScreenChunks;

		private boolean isRunning = true;

		SampleViewProcessor(RandomAccessStream ras, int width, int height,
				long start, double scale) {
			this.ras = ras;
			this.width = width;
			this.height = height;
			this.start = start;
			this.scale = scale;
		}

		private BufferedImage bi = null;
		private Graphics g = null;

		public void run() {
			if (isAborted)
				return;

			this.bi = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);
			this.g = bi.createGraphics();
			g.setPaintMode();

			// draw axis line

			Graphics2D g2d = (Graphics2D) g;

			g2d.setColor(Color.LIGHT_GRAY);
			Stroke oldstroke = g2d.getStroke();
			try {
				g2d.setStroke(axisStroke);

				g.drawLine(0, height / 2, width, height / 2);

				g.drawLine(0, 3 * height / 4, width, 3 * height / 4);

				g.drawLine(0, height / 4, width, height / 4);

			} finally {
				g2d.setStroke(oldstroke);
			}

			g.setColor(Color.DARK_GRAY);
			// a lot less performant when alpha channel is activated ...
			// Graphics2D g2d = (Graphics2D) g;
			// g2d.setComposite(AlphaComposite.getInstance(
			// AlphaComposite.SRC_OVER, 0.7f));

			long length = (long) (1.0 * width * scale);

			ras.startRead(start * 2, length * 2, this);

		}

		public void abort() {
			this.isAborted = true;
		}

		private int lastposx = 0;
		private int lastposy = 0;
		private boolean isfirst = true;

		public boolean dataReceived(byte[] chunk) {

			if (logger.isDebugEnabled())
				logger.debug("chunk received " + chunk.length); //$NON-NLS-1$

			for (int i = 0; i < chunk.length; i++) {

				int b1 = chunk[i];

				i++;
				if (i >= chunk.length)
					break;

				int b2 = chunk[i];

				int y = (int) ((1.0 * Short.MAX_VALUE / 2.0 - (1.0 * b1 + 1.0 * 128 * b2)) / (Short.MAX_VALUE * 1.0 / height));

				int x = lastposx + 1;

				if (!isfirst) {
					g.drawLine((int) (lastposx / scale), lastposy,
							(int) (x / scale), y);

				}
				isfirst = false;
				lastposx++;
				lastposy = y;

				if (isAborted)
					return isAborted;

			}

			if (currentScreenChunkToFlush-- < 0) {
				currentScreenChunkToFlush = flushScreenChunks;

				// flush the view

				SampleView v = new SampleView();
				v.scale = this.scale;
				v.start = this.start;

				v.image = this.bi;

				signalNewSampleImage(v);
			}

			return isAborted;
		}

		public void endOfStream() {
			g.dispose();
			isRunning = false;

			SampleView v = new SampleView();
			v.scale = this.scale;
			v.start = this.start;
			v.image = this.bi;

			signalNewSampleImage(v);
		}

		public boolean isRunning() {
			if (isAborted)
				return false;

			return isRunning;
		}

	}

	private AtomicReference<SampleView> sampleImageToShow = new AtomicReference<SampleView>(
			null);

	private void signalNewSampleImage(SampleView bi) {
		logger.debug("signalNewImage"); //$NON-NLS-1$
		this.sampleImageToShow.set(bi);
		repaint();
	}

	private AtomicReference<SampleView> loopImageToShow = new AtomicReference<SampleView>(
			null);

	private void signalNewLoopImage(SampleView bi) {
		logger.debug("signalNewLoopImage"); //$NON-NLS-1$
		this.loopImageToShow.set(bi);
		repaint();
	}

	/**
	 * Compute the x
	 * 
	 * @param s
	 * @return
	 */
	private int getScreenXPos(long s) {
		if (sampleImageToShow == null)
			return -1;

		return (int) ((s - currentStart) / currentscale);

	}

	public long getPosFromScreen(int x) {
		return (long) (x * currentscale) + currentStart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		SampleView svimage = this.sampleImageToShow.get();
		if (svimage != null) {
			double ratio = svimage.scale / currentscale;

			int xpos = getScreenXPos(svimage.start);

			BufferedImage sampleimage = svimage.image;
			g.drawImage(sampleimage, xpos, 0,
					(int) (sampleimage.getWidth() * ratio),
					sampleimage.getHeight(), null);
		}

		SampleView loopimage = this.loopImageToShow.get();
		if (loopimage != null) {
			double ratio = currentscale / loopimage.scale;

			int xpos = (int) ((loopimage.start - currentStart) * ratio);

			BufferedImage sampleimage = loopimage.image;
			g.drawImage(sampleimage, xpos, 0,
					(int) (sampleimage.getWidth() * ratio),
					sampleimage.getHeight(), null);

			g.drawImage(sampleimage, 0, 0, null);

		}
		if (isRunning())
			g.drawImage(waitImage, 0, 0, this);

		if (svimage != null) {
			BufferedImage bi = svimage.image;
			drawVerticalLine(g, bi, currentStartLoopPos, Color.BLACK);
			drawVerticalLine(g, bi, currentEndLoopPos, Color.RED);
			drawVerticalLine(g, bi, hightlight, Color.YELLOW);
		}

		drawSelection(g);

	}

	private void drawSelection(Graphics g) {
		if (hasSelection()) {
			int start = getScreenXPos(selectionStart);
			int end = getScreenXPos(selectionEnd);

			g.setXORMode(Color.gray);

			g.fillRect(start, 0, end - start, g.getClipBounds().height);

			g.setPaintMode();

		}
	}

	private void drawVerticalLine(Graphics g, BufferedImage image, long pos,
			Color color) {
		int startpos = getScreenXPos(pos);
		if (pos != -1 && startpos != -1 && image != null) {
			g.setColor(color);
			g.drawLine(startpos, 0, startpos, image.getHeight());
		}
	}

	private long currentStartLoopPos = -1;
	private long currentEndLoopPos = -1;

	private long hightlight = -1;

	public void clearHightLight() {
		this.hightlight = -1;
		repaint();
	}

	public void setHightLight(long pos) {
		this.hightlight = pos;
		repaint();
	}

	public void setCurrentStartLoopPos(long currentStartLoopPos) {
		this.currentStartLoopPos = currentStartLoopPos;
		relaunchLoopViewIfValid();
		repaint();
	}

	/**
	 * Return the current start loop pos (or -1) if not defined
	 * 
	 * @return
	 */
	public long getCurrentStartLoopPos() {
		return currentStartLoopPos;
	}

	public void setCurrentEndLoopPos(long currentEndLoopPos) {
		this.currentEndLoopPos = currentEndLoopPos;
		relaunchLoopViewIfValid();
		repaint();
	}

	/**
	 * Reset the current loop parameters
	 */
	public void resetLoopParameters() {
		this.currentEndLoopPos = -1;
		this.currentStartLoopPos = -1;
		relaunchLoopViewIfValid();
		repaint();
	}

	/**
	 * return the current endloop pos, -1 if none defined
	 * 
	 * @return
	 */
	public long getCurrentEndLoopPos() {
		return currentEndLoopPos;
	}

	private long selectionStart = -1;
	private long selectionEnd = -1;

	public boolean hasSelection() {
		return selectionStart != -1 && selectionEnd != -1;
	}

	public void setSelectionStart(long selectionStart) {
		this.selectionStart = selectionStart;
		repaint();
	}

	public void setSelectionEnd(long selectionEnd) {
		this.selectionEnd = selectionEnd;
		repaint();
	}

	public long getSelectionStart() {
		return selectionStart;
	}

	public long getSelectionEnd() {
		return selectionEnd;
	}

	public void clearSelection() {
		selectionStart = -1;
		selectionEnd = -1;
		repaint();
	}

	private void relaunchLoopViewIfValid() {

		// this.loopImageToShow.set(null);

		if (currentEndLoopPos != -1 && currentStartLoopPos != -1
				&& currentStartLoopPos <= currentEndLoopPos)
			launchNewLoopView(); // do the repaint();

		repaint();
	}

	private boolean isRunning() {

		LoopViewProcessor loopViewProcessor = this.referenceLoopViewProcessor
				.get();
		if (loopViewProcessor != null && loopViewProcessor.isRunning())
			return true;

		SampleViewProcessor referenceViewClass = this.referenceViewClass.get();
		if (referenceViewClass != null && referenceViewClass.isRunning())
			return true;

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Component#setBounds(int, int, int, int)
	 */
	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		launchNewSampleView();
		relaunchLoopViewIfValid();
	}

	@Override
	public void setBounds(Rectangle r) {
		super.setBounds(r);
		launchNewSampleView();
		relaunchLoopViewIfValid();
	}

	public double getScale() {
		return this.currentscale;
	}

	public void setScale(double newScale) {
		this.currentscale = newScale;
		launchNewSampleView();
		relaunchLoopViewIfValid();
	}

	public void setStart(long start) {
		if (start < 0)
			start = 0;
		this.currentStart = start;
		launchNewSampleView();
		relaunchLoopViewIfValid();
	}

	public long getStart() {
		return this.currentStart;
	}

	public long getFullLength() {
		RandomAccessStream ras = atomicReferenceRas.get();
		if (ras == null)
			return -1;
		return ras.getFileLength() / 2;
	}

	public void dispose() {
		if (atomicReferenceRas != null) {
			RandomAccessStream randomAccessStream = atomicReferenceRas.get();
			if (randomAccessStream != null) {
				randomAccessStream.dispose();
			}
			atomicReferenceRas = null;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		dispose();

		super.finalize();
	}

	/**
	 * Test method for the panel / frame ...
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		logger.debug("demarrage ...."); //$NON-NLS-1$
		JFrame frame = new JFrame();
		final JWavDisplayer wavDisplayer = new JWavDisplayer();
		wavDisplayer
				.displayAudioInputStream(new ManagedAudioInputStream(
						new ManagedAudioInputStream.NonManagedInputStream(
								AudioSystem
										.getAudioInputStream(new File(
												"contributions/enregistrement cottin/Archive1/do4.wav"))))); //$NON-NLS-1$

		// wavDisplayer.addMouseListener(new MouseAdapter() {
		// @Override
		// public void mouseReleased(MouseEvent e) {
		// long posFromScreen = wavDisplayer.getPosFromScreen(e.getX());
		// if (e.getButton() == MouseEvent.BUTTON1) {
		//
		// wavDisplayer.setCurrentStartLoopPos(posFromScreen);
		// } else if (e.getButton() == MouseEvent.BUTTON3) {
		// wavDisplayer.setCurrentEndLoopPos(posFromScreen);
		// }
		//
		// }
		// });

		wavDisplayer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				long posFromScreen = wavDisplayer.getPosFromScreen(e.getX());
				if (e.getButton() == MouseEvent.BUTTON1) {

					wavDisplayer.setSelectionStart(posFromScreen);
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					wavDisplayer.setSelectionEnd(posFromScreen);
				}

			}
		});

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(wavDisplayer, BorderLayout.CENTER);

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		JScrollBar start = new JScrollBar(JScrollBar.HORIZONTAL, 0, 100, 0,
				(int) wavDisplayer.getFullLength());
		start.addAdjustmentListener(new AdjustmentListener() {

			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (e.getValueIsAdjusting())
					return;
				JScrollBar start = (JScrollBar) e.getSource();
				long newstart = (long) start.getValue();
				wavDisplayer.setStart(newstart);
			}
		});
		p.add(start, BorderLayout.NORTH);

		JScrollBar sb = new JScrollBar(JScrollBar.HORIZONTAL, 100, 100, 1,
				10000);
		p.add(sb, BorderLayout.SOUTH);

		sb.addAdjustmentListener(new AdjustmentListener() {

			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (e.getValueIsAdjusting())
					return;
				JScrollBar sb = (JScrollBar) e.getSource();
				double newscale = (double) sb.getValue() / 10.0;
				wavDisplayer.setScale(newscale);
			}
		});

		frame.getContentPane().add(p, BorderLayout.SOUTH);

		frame.setSize(500, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

}
