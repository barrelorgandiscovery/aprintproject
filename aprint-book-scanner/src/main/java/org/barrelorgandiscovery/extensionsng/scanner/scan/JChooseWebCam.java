package org.barrelorgandiscovery.extensionsng.scanner.scan;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.ImageTools;

import com.github.sarxos.webcam.Webcam;
import com.jeta.forms.components.panel.FormPanel;

/**
 * this component permit to choose a webcam and preview the result
 * 
 * @author pfreydiere
 *
 */
public class JChooseWebCam extends JPanel implements Disposable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2121928714524074850L;

	private static Logger logger = Logger.getLogger(JChooseWebCam.class);

	public JChooseWebCam() throws Exception {
		initComponents();
	}

	private JLabel previewPanel;

	private JComboBox combo;

	private IChooseWebCamListener chooseWebCamListener;
	
	public void setChooseWebCamListener(IChooseWebCamListener chooseWebCamListener) {
		this.chooseWebCamListener = chooseWebCamListener;
	}

	public static class WebCamConfig {
		public Webcam webcam;
		public String label;
		public Dimension d;

		public WebCamConfig(Webcam webcam, Dimension d, String label) {
			this.webcam = webcam;
			this.label = label;
			this.d = d;
		}

		@Override
		public String toString() {
			return label + (d != null ? " - "//$NON-NLS-1$ 
						+ d.toString() : "");//$NON-NLS-1$
		}
	}

	protected void initComponents() throws Exception {

		InputStream is = getClass().getResourceAsStream("choosewebcam.jfrm");//$NON-NLS-1$
		assert is != null;
		FormPanel fp = new FormPanel(is);

		previewPanel = fp.getLabel("preview");//$NON-NLS-1$
		previewPanel.setText("");

		JLabel lblchooseWebCam = fp.getLabel("lblchoosewebcam");//$NON-NLS-1$
		lblchooseWebCam.setText("Choose Webcam ..");

		combo = fp.getComboBox("cbwebcam");//$NON-NLS-1$

		List<Webcam> webcams = Webcam.getWebcams();
		logger.debug("cams :" + webcams);//$NON-NLS-1$

		ArrayList<WebCamConfig> webcamdisplay = new ArrayList<>();
		webcamdisplay.add(new WebCamConfig(null, null, "<Not Selected>"));
		webcams.forEach((w) -> {
			// for each webcam, add the view sizes
			Dimension[] sizes = w.getViewSizes();
			for (Dimension d : sizes) {
				webcamdisplay.add(new WebCamConfig(w, d, w.toString()));
			}
			
			
			webcamdisplay.add(new WebCamConfig(w, new Dimension(3000,3000), w.toString()));
			
			
		});

		combo.setModel(new DefaultComboBoxModel<>(webcamdisplay.toArray()));

		combo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				logger.debug("item changed listener " + e);//$NON-NLS-1$
				if (e.getStateChange() == ItemEvent.SELECTED) {
					WebCamConfig i = (WebCamConfig) e.getItem();
					try {
						openWebcam(i);
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
					if (chooseWebCamListener != null) {
						try {
							chooseWebCamListener.choosedWebCamChanged(i);
						} catch (Exception ex) {
							logger.error("error in choosewebcam listener :" + ex.getMessage(), ex);//$NON-NLS-1$
						}
					}
				}
			}
		});
		setLayout(new BorderLayout());
		add(fp, BorderLayout.CENTER);
	}

	private Webcam current = null;

	private ScheduledExecutorService s = null;

	private void openWebcam(WebCamConfig webcamdisplay) throws Exception {
		dispose();

		if (webcamdisplay != null && webcamdisplay.webcam != null) {
			Webcam w = webcamdisplay.webcam;
			if (webcamdisplay != null) {
				w.getDevice().setResolution(webcamdisplay.d);
				//  w.setViewSize(webcamdisplay.d);
			}
			w.open();

			this.current = w;
			startPreview();
		}
	}

	public void startPreview() {
		if (this.current == null) {
			return;
		}
		assert this.current != null;
		assert this.current.isOpen();
		WebCamPictureTake wt = new WebCamPictureTake(current, (i, t) -> {
			logger.debug("image size :" + i.getWidth() + "x" + i.getHeight());
			// thumbnail
			final BufferedImage transformed = ImageTools.crop(300, 300, i);

			SwingUtilities.invokeLater(() -> {
				previewPanel.setIcon(new ImageIcon(transformed));
				previewPanel.repaint();
			});
		});
		s = Executors.newSingleThreadScheduledExecutor();
		s.scheduleWithFixedDelay(wt, 0, 100, TimeUnit.MILLISECONDS);

	}

	public void stopPreview() {
		if (s != null) {
			s.shutdownNow();
			s = null;
		}

	}

	@Override
	public void dispose() {
		stopPreview();
		if (current != null) {
			current.close();
			current = null;
		}
	}

	/**
	 * get webcam config
	 *
	 * @return
	 */
	public WebCamConfig getSelectedWebCamConfig() {
		return (WebCamConfig) combo.getSelectedItem();
	}

	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure(new LF5Appender());

		List<Webcam> webcams = Webcam.getWebcams();
		logger.debug("cams :" + webcams);//$NON-NLS-1$

		JFrame f = new JFrame();
		f.setSize(800, 600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(new JChooseWebCam(), BorderLayout.CENTER);
		f.setVisible(true);
	}
}
