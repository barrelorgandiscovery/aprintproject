package org.barrelorgandiscovery.recognition;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;

/**
 * class for reading the holes in a book
 * @author pfreydiere
 *
 */
public class ImageHoleReader {

	private static Logger logger = Logger.getLogger(ImageHoleReader.class);

	private interface HoleEmitter {
		void emit(int track, int pixelstart, int pixelend);
	}

	/**
	 * manage the state of a bunch of holes recognized
	 * @author pfreydiere
	 *
	 */
	private static class ScannerState {

		private int[] startedAt;
		private boolean[] previousStates;
		private boolean[] states;

		private HoleEmitter holeEmitter;

		private int current = 0;

		private int tracknb;

		public ScannerState(int tracknb, HoleEmitter holeEmitter) {
			this.tracknb = tracknb;
			resetCurrentState();
			previousStates = states;
			resetCurrentState();

			startedAt = new int[tracknb];

			for (int i = 0; i < startedAt.length; i++) {
				startedAt[i] = -1; // not started
			}

			assert holeEmitter != null;
			this.holeEmitter = holeEmitter;
		}

		public void resetCurrentState() {
			states = new boolean[tracknb];
			for (int i = 0; i < states.length; i++) {
				states[i] = false;
			}
		}

		public void endLine() {

			assert current != -1;

			// check changed states
			for (int i = 0; i < states.length; i++) {

				if (states[i] != previousStates[i]) {
					if (states[i]) {

						assert !previousStates[i];
						// start a new hole
						assert startedAt[i] == -1;
						startedAt[i] = current;

					} else {
						// end of a hole

						assert startedAt[i] > -1;

						holeEmitter.emit(i, startedAt[i], (current - 1));
						// System.out.println("hole on track " + i
						// + " started at " + startedAt[i] + " to "
						// + (current - 1));
						startedAt[i] = -1;

					}

				}

			}

			previousStates = states;
			resetCurrentState();

			// System.out.println("end line " + current);
			current++;
		}

		public void set(int i, boolean state) throws Exception {

			if (i < 0 || i >= states.length) {
				return;
			}

			states[i] = state;
		}

	}

	/**
	 * read an image and extract the holes
	 * 
	 * @param bi
	 *            the image, properly aligned with the scale
	 * @param scale
	 *            the scale of the instrument
	 * @return the decoded holes
	 * @throws Exception
	 */
	public static List<Hole> readHoles(BufferedImage bi, final Scale scale)
			throws Exception {

		assert bi != null;
		logger.debug("image size :" + bi.getWidth() + "x" + bi.getHeight());

		final ArrayList<Hole> holes = new ArrayList<Hole>();

		final double res = scale.getWidth() / bi.getHeight();

		ScannerState s = new ScannerState(scale.getTrackNb(),
				new HoleEmitter() {
					public void emit(int track, int pixelstart, int pixelend) {

						holes.add(new Hole(track, scale.mmToTime(pixelstart
								* res), scale
								.mmToTime((pixelend - pixelstart + 1) * res)));

					}
				});

		for (int i = 0; i < bi.getWidth(); i++) {
			int j = 0;
			try {

				while (true) {

					int rgb = bi.getRGB(i, j);
					// black
					while (!isHole(rgb) && j < bi.getHeight()) {
						j++;
						if (j < bi.getHeight())
							rgb = bi.getRGB(i, j);
					}

					// we are on hole or out of bounds

					if (j >= bi.getHeight()) {
						s.endLine();
						break;

					} else {

						assert j < bi.getHeight();
						assert isHole(rgb);

						// search for the end of the hole

						// j on white pixel, compute height
						int start = j;
						// j++;
						rgb = bi.getRGB(i, j);
						while (isHole(rgb) && j < bi.getHeight()) {
							j++;
							if (j < bi.getHeight())
								rgb = bi.getRGB(i, j);
						}

						// two cases :
						// 1 - out of bound
						// 2 - on black

						int end = j;

						if (j >= bi.getHeight()) {
							end = bi.getHeight() - 1;
						}

						// white range [start, end], included
						int trackspixelsize = (int) (scale.getTrackWidth()
								/ scale.getWidth() * bi.getHeight());
						assert trackspixelsize > 0;

						int nbtrackswidth = (end - start) / trackspixelsize;
						if (nbtrackswidth == 0)
							nbtrackswidth = 1;

						int midfirst = start + trackspixelsize / 2;

						double pos = (midfirst * 1.0) / bi.getHeight();
						double lowerbound = (scale.getFirstTrackAxis() - scale
								.getIntertrackHeight() / 2) / scale.getWidth();

						if (scale.isPreferredViewedInversed()) {
							pos = 1.0 - pos;
						}

						int track = (int) ((pos - lowerbound) / (scale
								.getIntertrackHeight() / scale.getWidth()));
						for (int k = 0; k < nbtrackswidth; k++) {
							s.set(track + k, true);
						}

						if (j >= bi.getHeight()) {
							s.endLine();
							break;
						}

					} // else
				} // while

			} catch (Exception ex) {
				logger.error("error in reading holes x:" + i + " y:" + j
						+ " -> " + ex.getMessage(), ex);
				throw ex;
			}

		}

		return holes;

	}

	private static boolean isHole(int rgb) {
		return (rgb & 0x00FF00) == 0;
	}
}
