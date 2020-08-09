package org.barrelorgandiscovery.extensionsng.scanner.scan;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.barrelorgandiscovery.tools.ImageTools;

/**
 * components showing an images stack
 * 
 * @author use
 *
 */
public class JImagePreviewStack extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5661371358507862578L;
	
	private static final int MAX_IMAGES = 5;
	private List<JLabel> images = new ArrayList<JLabel>();

	public JImagePreviewStack() {
	}

	private void refreshImages() {
		Component[] components = getComponents();
		for (Component c : components) {
			remove(c);
		}
		for (JLabel l : images) {
			add(l);
		}
		revalidate();
		repaint();
	}

	public void addImage(Image image) {
		BufferedImage thumbnail = ImageTools.crop(100, 100, image);
		JLabel l = new JLabel();

		l.setIcon(new ImageIcon(thumbnail));
		while (images.size() >= MAX_IMAGES) {
			images.remove(0);
		}

		images.add(l);
		refreshImages();
	}
	
	public void clearImages() {
		images.clear();
		refreshImages();
	}

	public static void main(String[] args) throws Exception {

		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());
		JImagePreviewStack scanPanel = new JImagePreviewStack();

		File folder = new File("C:\\projets\\APrint\\contributions\\patrice\\2018_numerisation_josephine");//$NON-NLS-1$
		assert folder.exists() && folder.isDirectory();

		ScheduledExecutorService scheduleExecutor = Executors.newSingleThreadScheduledExecutor();

		for (int i = 0; i < 10; i++) {
			final File imageFile = new File(folder, "scan_image_"//$NON-NLS-1$ 
												+ i + ".jpg");//$NON-NLS-1$
			assert imageFile.exists();
			
			scheduleExecutor.execute(() -> {
				try {

					Thread.sleep(1000);
					SwingUtilities.invokeAndWait(() -> {
						try {

							scanPanel.addImage(ImageTools.loadImage(imageFile.toURL()));

						} catch (Exception ex) {
							ex.printStackTrace();
						}
					});

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			});

		}

		f.getContentPane().add(scanPanel);
		f.setSize(800, 600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);

	}

}
