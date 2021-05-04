package org.barrelorgandiscovery.recognition.gui.books.steps;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.barrelorgandiscovery.images.books.tools.IFileFamilyTiledImage;
import org.barrelorgandiscovery.recognition.gui.books.BackgroundTileImageProcessingThread;
import org.barrelorgandiscovery.recognition.gui.books.BookReadProcessor;
import org.barrelorgandiscovery.recognition.gui.books.states.EdgesStates;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.Disposable;

public class ProcessEdges implements Disposable {

	private BackgroundTileImageProcessingThread<Void> backProcessingView;

	@Override
	public void dispose() {
		if (backProcessingView != null) {
			backProcessingView.cancel();

			backProcessingView.dispose();
		}
	}

	/**
	 * process tile images
	 * 
	 * @param origin          the original image
	 * @param edgesState      the edges states
	 * @param instrumentScale destination instrument scale
	 * @param finalTiView     the final output
	 */
	public void processImageEdges(IFileFamilyTiledImage origin, EdgesStates edgesState, Scale instrumentScale,
			IFileFamilyTiledImage finalTiView) {

		assert origin != null;

		final IFileFamilyTiledImage finalOrigin = origin;

		////////////////////////////////////////////////////////////////////////////
		// viewing processing
		{

			BackgroundTileImageProcessingThread<Void> t2 = new BackgroundTileImageProcessingThread<>(origin,
					new BackgroundTileImageProcessingThread.TiledProcessedListener() {
						@Override
						public <T> void tileProcessed(int index, T result) {

//							SwingUtilities.invokeLater(new Runnable() {
//								@Override
//								public void run() {
//									editableVirtualbookComponent.repaint();
//								}
//							});

						}

						@Override
						public void errorInProcessingTile(String errormsg) {
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									JOptionPane.showMessageDialog(null, errormsg);
								}

							});
						}
					}, 1);

			if (backProcessingView != null) {
				backProcessingView.cancel();
			}

			backProcessingView = t2;

			t2.start(new BackgroundTileImageProcessingThread.TileProcessing<Void>() {
				@Override
				public Void process(int index, BufferedImage tile) throws Exception {

					File output = finalTiView.constructImagePath(index, finalTiView.getCurrentImageFamilyDisplay());

					BufferedImage result = BookReadProcessor.correctImage(tile, index * finalOrigin.getHeight(),
							finalOrigin.getHeight(), edgesState.top, edgesState.bottom, finalOrigin.getHeight(),
							edgesState.viewInverted ^ instrumentScale.isPreferredViewedInversed());

					ImageIO.write(result, "JPEG", output);

					return null;
				}
			});

		}
	}
}
