package org.barrelorgandiscovery.extensionsng.scanner.scan;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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

import com.github.sarxos.webcam.Webcam;
import com.jeta.forms.components.panel.FormPanel;

/**
 * this component permit to choose a webcam and preview the result
 * 
 * @author pfreydiere
 *
 */
public class JChooseWebCam extends JPanel implements Disposable {

	private static Logger logger = Logger.getLogger(JChooseWebCam.class);

	public JChooseWebCam() throws Exception {
		initComponents();
	}

	private JLabel previewPanel;

	private JComboBox combo;

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
			return label + (d != null ? " - " + d.toString() : "");
		}
	}

	protected void initComponents() throws Exception {

		InputStream is = getClass().getResourceAsStream("choosewebcam.jfrm");
		assert is != null;
		FormPanel fp = new FormPanel(is);

		previewPanel = fp.getLabel("preview");
		previewPanel.setText("");

		combo = fp.getComboBox("cbwebcam");

		List<Webcam> webcams = Webcam.getWebcams();
		logger.debug("cams :" + webcams);

		ArrayList<WebCamConfig> webcamdisplay = new ArrayList<>();
		webcamdisplay.add(new WebCamConfig(null, null, "<Not Selected>"));
		webcams.forEach((w) -> {
			Dimension[] sizes = w.getViewSizes();
			for (Dimension d : sizes) {
				webcamdisplay.add(new WebCamConfig(w, d, w.toString()));
			}
		});

		combo.setModel(new DefaultComboBoxModel<>(webcamdisplay.toArray()));

		combo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				logger.debug("item changed listener " + e);
				if (e.getStateChange() == ItemEvent.SELECTED) {
					WebCamConfig i = (WebCamConfig) e.getItem();
					try {
						openWebcam(i);
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
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
				w.setViewSize(webcamdisplay.d);
			}
			w.open();
			WebCamPictureTake wt = new WebCamPictureTake(w, (i, t) -> {
				SwingUtilities.invokeLater(() -> {
					previewPanel.setIcon(new ImageIcon(i));
					previewPanel.repaint();
				});
			});
			s = Executors.newSingleThreadScheduledExecutor();
			s.scheduleWithFixedDelay(wt, 0, 100, TimeUnit.MILLISECONDS);

			this.current = w;
		}
	}

	@Override
	public void dispose() {
		if (s != null) {
			s.shutdownNow();
			s = null;
		}
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
		logger.debug("cams :" + webcams);

		JFrame f = new JFrame();
		f.setSize(800, 600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(new JChooseWebCam(), BorderLayout.CENTER);
		f.setVisible(true);
	}
}
