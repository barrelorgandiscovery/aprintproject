package org.barrelorgandiscovery.recognition.gui.books;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.bookimage.IFamilyImageSeeker;
import org.barrelorgandiscovery.images.books.tools.IFileBasedTiledImage;
import org.barrelorgandiscovery.images.books.tools.IFileFamilyTiledImage;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.ImageTools;

/**
 * background processing images tile
 * 
 * @author pfreydiere
 *
 */
public class BackgroundTileImageProcessingThread<T> implements Disposable {

	private static final Logger logger = Logger.getLogger(BackgroundTileImageProcessingThread.class);

	public interface TileProcessing<T> {
		T process(int index, BufferedImage tile) throws Exception;
	}

	public interface TiledProcessedListener {
		<T> void tileProcessed(int index, T result);

		void errorInProcessingTile(String errormsg);
	}

	private IFileFamilyTiledImage image;
	private TiledProcessedListener l;
	private ExecutorService exec;

	private boolean aborted = false;
	private AtomicInteger currentImageProcessed = new AtomicInteger(0);

	public BackgroundTileImageProcessingThread(IFileFamilyTiledImage image, TiledProcessedListener l, int nbThreads) {
		this.image = image;
		this.l = l;
		exec = Executors.newFixedThreadPool(nbThreads); // Runtime.getRuntime().availableProcessors()
	}

	public <T> void start(TileProcessing<T> p) {

		assert exec != null;

		aborted = false;
		currentImageProcessed.set(0);

		for (int i = 0; i < image.getImageCount(); i++) {
			final int current = i;
			Runnable c = new Runnable() {
				@Override
				public void run() {
					try {
						if (aborted)
							return;

						if (!(image instanceof IFileFamilyTiledImage)) {
							throw new Exception("image " + image + " does not implements " + IFileFamilyTiledImage.class);
						} 
						
						// take the root image
						
						BufferedImage bi = ((IFileFamilyTiledImage)image).loadImage(current, null);
						if (bi == null) {
							return;
						}
						
						T result = p.process(current, bi);
						if (l != null) {
							try {
								l.tileProcessed(current, result);
							} catch (Exception ex) {
								logger.error(ex.getMessage(), ex);
							}
							currentImageProcessed.addAndGet(1);
						}
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
						try {
							l.errorInProcessingTile("error in processing tile " + current + " :" + ex.getMessage());
						} catch (Exception exerr) {
							logger.error("error in processin error feedback " + exerr.getMessage(), exerr);
						}
					}
				}
			};
			exec.submit(c);
		}
	}

	public void cancel() {
		aborted = true;
		if (exec != null)
			exec.shutdownNow();
		exec = null;
	}

	public boolean isRunning() {
		if (aborted)
			return false;

		if (image.getImageCount() != currentImageProcessed.get()) {
			return true;
		}

		return false;
	}

	public double currentProgress() {
		if (aborted)
			return 1.0;

		return currentImageProcessed.get() * 1.0 / image.getImageCount();
	}

	@Override
	public void dispose() {
		cancel();
	}

}
