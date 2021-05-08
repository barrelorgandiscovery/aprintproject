package org.barrelorgandiscovery.movies;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.media.MediaLocator;
import javax.sound.midi.Sequence;
import javax.sound.midi.Soundbank;
import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.gui.ProgressIndicator;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.PipeSetGroupLayer;
import org.barrelorgandiscovery.gui.aedit.RegistrationSectionLayer;
import org.barrelorgandiscovery.gui.aedit.TimeBookLayer;
import org.barrelorgandiscovery.gui.aprint.SequencerTools;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.listeningconverter.EcouteConverter;
import org.barrelorgandiscovery.listeningconverter.VirtualBookToMidiConverter;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class MovieConverter {

	private static final String LISTENING_WAV = "listening.wav"; //$NON-NLS-1$
	private static final String IMAGES_MOV = "images.mov"; //$NON-NLS-1$
	private static Logger logger = Logger.getLogger(MovieConverter.class);

	private static int createImages(VirtualBook c, File f,
			ProgressIndicator progress, ICancelTracker cancelTracker,
			MovieConverterParameters parameters) throws Exception, IOException {

		if (progress == null)
			progress = new ProgressIndicator() {
				public void progress(double progress, String message) {
					logger.debug("progress :" + progress + ", message :" //$NON-NLS-1$ //$NON-NLS-2$
							+ message);
				}
			};

		JVirtualBookComponent comp = new JVirtualBookComponent();
		comp.setUseFastDrawing(parameters.isFastRendering());

		comp.setDisplayTracksLimits(false);

		comp.setVirtualBook(c);

		if (parameters.isShowTime()) {
			logger.debug("adding time layer ...");
			TimeBookLayer tbl = new TimeBookLayer();
			comp.addLayer(tbl);
		}

		if (parameters.isShowComposition()) {
			logger.debug("show composition");
			PipeSetGroupLayer psgl = new PipeSetGroupLayer();
			comp.addLayer(psgl);
		}

		if (parameters.isShowRegistration()) {
			logger.debug("show registration");
			RegistrationSectionLayer rl = new RegistrationSectionLayer();
			comp.addLayer(rl);
		}

		Dimension dimension = new Dimension(parameters.getWidth(),
				parameters.getHeight());
		comp.setPreferredSize(dimension);
		comp.setSize(dimension);

		comp.fitToComponentSize();

		int imageNumber = 0;

		double framerate = 25;

		Vector images = new Vector();

		long videoEndLength = c.getLength() + 3000000; // + 3s

		int maxFrames = (int) (videoEndLength / 1000000.0 * framerate);

		progress.progress(0.0, Messages.getString("MovieConverter.4")); //$NON-NLS-1$

		while (imageNumber < maxFrames) {

			if (imageNumber % 50 == 0) {
				progress.progress(imageNumber / (maxFrames * 1.0),
						Messages.getString("MovieConverter.5")); //$NON-NLS-1$
			}

			if (cancelTracker != null && cancelTracker.isCanceled())
				throw new InterruptedException("aborted by user"); //$NON-NLS-1$

			long pos = (long) (1000000 * (imageNumber * 1 / framerate));

			comp.setHightlight(comp.timestampToMM(pos));
			ArrayList<Hole> findHoles = comp.getVirtualBook().findHoles(pos, 0);
			comp.clearSelection();
			for (Iterator iterator = findHoles.iterator(); iterator.hasNext();) {
				Hole hole = (Hole) iterator.next();
				comp.addToSelection(hole);
			}
			comp.setXoffset(comp.getHightlight() - comp.pixelToMm(400) / 2.0);

			if (logger.isDebugEnabled() && (imageNumber % 100) == 0) {
				logger.debug("creating image :" + imageNumber); //$NON-NLS-1$
			}

			images.add(createJPEGSnapShot(f, "img" + imageNumber + ".jpg", //$NON-NLS-1$ //$NON-NLS-2$
					comp));

			imageNumber++;

		}
		imageNumber--;

		progress.progress(1.0, Messages.getString("MovieConverter.10")); //$NON-NLS-1$

		logger.debug(imageNumber + " images created"); //$NON-NLS-1$

		return imageNumber;
	}

	/**
	 * Create a jpeg snapshot of a component, 
	 * @param folder the result folder
	 * @param filename the file name (with jpg extension)
	 * @param comp the component to render, it use the preferred size for the size of the image
	 * @return the absolute file path is returned
	 * @throws Exception
	 */
	public static String createJPEGSnapShot(File folder, String filename,
			JComponent comp) throws Exception {

		BufferedImage bi = createImage(comp);

		File file = new File(folder, filename);
		ImageTools.saveJpeg(bi, file); //$NON-NLS-1$

		return file.getAbsolutePath();
	}

	/**
	 * create an image of a component, the size of the image is defined by the
	 * preferredsize of the component
	 * 
	 * @param comp
	 *            the component to render
	 * @return the associated buffered image
	 */
	public static BufferedImage createImage(JComponent comp) {
		Dimension size = comp.getPreferredSize();
		comp.setSize(size);

		// Create a picture of the scale ...
		BufferedImage bi = new BufferedImage(size.width, size.height,
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = bi.createGraphics();
		try {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
			g.setColor(Color.white);
			g.fillRect(0, 0, size.width, size.height);
			comp.paint(g);

		} finally {
			g.dispose();
		}
		return bi;
	}

	/**
	 * Convert a virtual book rendering to movie file ..
	 * 
	 * @param vb
	 *            the virtual book
	 * @param ins
	 *            the instrument associated to it
	 * @param resultFile
	 *            the result file ...
	 * @throws Exception
	 */
	public static void convertToMovie(VirtualBook vb, Instrument ins,
			File resultFile, ProgressIndicator progress,
			ICancelTracker cancelTracker, MovieConverterParameters parameters)
			throws Exception {

		if (vb == null || resultFile == null)
			throw new IllegalArgumentException("bad parameters"); //$NON-NLS-1$

		logger.debug("creating output temp folder"); //$NON-NLS-1$

		if (progress == null)
			progress = new ProgressIndicator() {
				public void progress(double progress, String message) {
					logger.debug("progress " + progress + " message " //$NON-NLS-1$ //$NON-NLS-2$
							+ message);
				}
			};

		File tmpfolder = File.createTempFile("video", "tmp"); //$NON-NLS-1$ //$NON-NLS-2$
		tmpfolder.delete();

		if (!tmpfolder.mkdirs())
			throw new Exception("fail to create temp folder :" //$NON-NLS-1$
					+ tmpfolder.getAbsolutePath());

		int imageNumber = createImages(vb, tmpfolder, progress, cancelTracker,
				parameters);

		Vector<String> images = new Vector<String>();
		for (int i = 0; i < imageNumber; i++) {
			images.add(new File(tmpfolder, "img" + i + ".jpg") //$NON-NLS-1$ //$NON-NLS-2$
					.getAbsolutePath());
		}

		progress.progress(0.0, Messages.getString("MovieConverter.22")); //$NON-NLS-1$

		JpegImageToMovie jp = new JpegImageToMovie();
		jp.doIt(parameters.getWidth(), parameters.getHeight(), 25, images,
				new MediaLocator(new File(tmpfolder, IMAGES_MOV).toURL()
						.toString()));

		progress.progress(0.3, Messages.getString("MovieConverter.23")); //$NON-NLS-1$

		VirtualBookToMidiConverter converter = new VirtualBookToMidiConverter(
				ins);

		// EcouteConverter.convert(vb); // old

		Sequence seq = converter.convert(vb);

		File wavFile = new File(tmpfolder, LISTENING_WAV);

		Soundbank sb = null;
		if (ins != null)
			sb = ins.openSoundBank();

		SequencerTools.render(sb, seq, wavFile, false);

		progress.progress(0.8, Messages.getString("MovieConverter.24")); //$NON-NLS-1$

		new Merge(new String[] {
				new File(tmpfolder, IMAGES_MOV).toURL().toString(),
				new File(tmpfolder, LISTENING_WAV).toURL().toString(), "-o", //$NON-NLS-1$
				resultFile.toURL().toString() });

		progress.progress(1.0, Messages.getString("MovieConverter.26")); //$NON-NLS-1$

	}
}
