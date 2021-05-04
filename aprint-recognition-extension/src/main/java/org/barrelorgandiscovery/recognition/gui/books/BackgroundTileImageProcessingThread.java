package org.barrelorgandiscovery.recognition.gui.books;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.images.books.tools.IFileFamilyTiledImage;
import org.barrelorgandiscovery.tools.Disposable;

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

	public BackgroundTileImageProcessingThread(IFileFamilyTiledImage image, TiledProcessedListener l, int nbThreads) {
		this.image = image;
		this.l = l;
		exec = Executors.newFixedThreadPool(nbThreads); // Runtime.getRuntime().availableProcessors()
	}

	/**
	 * contains the tiles to process
	 */
	private ArrayList<Integer> tilesToProcess = new ArrayList<>();

	/**
	 * contains the tiles currently in process
	 */
	private HashSet<Integer> currentlyInProcess = new HashSet<>();
	private AtomicInteger processedCount = new AtomicInteger(0);

	private synchronized Integer getNextToProcess() {
		if (tilesToProcess.size() == 0) {
			return null;
		}
		Integer t = tilesToProcess.remove(0);
		currentlyInProcess.add(t);
		return t;
	}

	private synchronized boolean isFinished() {
		return tilesToProcess.size() == 0 && currentlyInProcess.size() == 0;
	}

	private synchronized void informProcessed(Integer tile) {
		assert tile != null;
		currentlyInProcess.remove(tile);
		processedCount.addAndGet(1);
	}

	public void sortProcessingQueue(Comparator<Integer> tileOrdering) {
		if (tileOrdering != null) {
			tilesToProcess.sort(tileOrdering);
		}
	}

	public <T> void start(TileProcessing<T> p) {
		start(p, null);
	}

	public <T> void start(TileProcessing<T> p, Comparator<Integer> initialTileProcessingSort) {

		assert exec != null;

		aborted = false;

		tilesToProcess.clear();
		currentlyInProcess.clear();
		int imageCount = image.getImageCount();

		for (int i = 0; i < imageCount; i++) {
			tilesToProcess.add(i);
		}

		if (initialTileProcessingSort != null) {
			sortProcessingQueue(initialTileProcessingSort);
		}

		for (int i = 0; i < imageCount; i++) {

			Runnable c = new Runnable() {
				@Override
				public void run() {
					try {
						if (aborted)
							return;

						Integer current = getNextToProcess();
						if (current == null) {
							// no more to process
							return;
						}

						try {
							if (!(image instanceof IFileFamilyTiledImage)) {
								throw new Exception(
										"image " + image + " does not implements " + IFileFamilyTiledImage.class);
							}

							// take the root image

							BufferedImage bi = ((IFileFamilyTiledImage) image).loadImage(current, null);
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

							}
						} finally {
							informProcessed(current);
						}

					} catch (Throwable ex) {
						logger.error(ex.getMessage(), ex);
						try {
							l.errorInProcessingTile("error in processing tile :" + ex.getMessage());
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

		if (!isFinished()) {
			return true;
		}

		return false;
	}

	public double currentProgress() {
		if (aborted)
			return 1.0;

		return processedCount.get() * 1.0 / image.getImageCount();
	}

	@Override
	public void dispose() {
		cancel();
	}

}
