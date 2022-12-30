package org.barrelorgandiscovery.gui.ainstrument;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

import javax.sound.sampled.AudioInputStream;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.gui.JNoteSelectorPanel;
import org.barrelorgandiscovery.instrument.sample.ManagedAudioInputStream;
import org.barrelorgandiscovery.instrument.sample.SoundSample;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

import com.jeta.forms.components.panel.FormPanel;

/**
 * Panel for cropping sounds from a wav file.
 * 
 * @author pfreydiere
 * 
 */
public class JCreateSoundSampleFromCropping extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -932573486302251888L;

	private static Logger logger = Logger
			.getLogger(JCreateSoundSampleFromCropping.class);

	private JWavDisplayer wavDisplayer;

	private JTextField samplename;

	private JNoteSelectorPanel rootNote;

	public JCreateSoundSampleFromCropping() throws Exception {
		initComponents();
	}

	private void initComponents() throws Exception {
		FormPanel wavCropPanel = null;
		try {

			InputStream is = getClass()
					.getResourceAsStream("cropfileform.jfrm"); //$NON-NLS-1$
			if (is == null)
				throw new Exception("form not found"); //$NON-NLS-1$

			wavCropPanel = new FormPanel(is);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw new Exception(ex.getMessage(), ex);
		}

		assert wavCropPanel != null;

		JLabel lhelp = (JLabel) wavCropPanel.getComponentByName("help"); //$NON-NLS-1$
		lhelp.setText(Messages.getString("JCreateSoundSampleFromCropping.1100")); //$NON-NLS-1$

		wavDisplayer = new JWavDisplayer();
		wavCropPanel.getFormAccessor().replaceBean(
				wavCropPanel.getComponentByName("wavdisplayer"), wavDisplayer); //$NON-NLS-1$

		rootNote = new JNoteSelectorPanel();
		wavCropPanel.getFormAccessor("createsszone").replaceBean( //$NON-NLS-1$
				wavCropPanel.getComponentByName("notechooser"), rootNote); //$NON-NLS-1$

		logger.debug("adding mouse events for displayer ..."); //$NON-NLS-1$
		wavDisplayer.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {

				if ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0) {
					wavDisplayer.setSelectionEnd(wavDisplayer
							.getPosFromScreen(e.getX()));
				} else {
					wavDisplayer.setSelectionStart(wavDisplayer
							.getPosFromScreen(e.getX()));
				}

			}

		});

		wavDisplayer.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {

				wavDisplayer.setSelectionEnd(wavDisplayer.getPosFromScreen(e
						.getX()));
			}

		});

		JButton zoomplus = (JButton) wavCropPanel.getButton("zoomplus"); //$NON-NLS-1$
		zoomplus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				wavDisplayer.setScale(wavDisplayer.getScale() / 2.0);
			}
		});
		zoomplus.setIcon(new ImageIcon(getClass().getResource("viewmag+.png"))); //$NON-NLS-1$
		zoomplus.setToolTipText(Messages
				.getString("JSoundSampleEditorPanel.15")); //$NON-NLS-1$
		zoomplus.setText(null);

		JButton zoommoins = (JButton) wavCropPanel.getButton("zoommoins"); //$NON-NLS-1$
		zoommoins.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				wavDisplayer.setScale(wavDisplayer.getScale() * 2.0);
			}
		});
		zoommoins
				.setIcon(new ImageIcon(getClass().getResource("viewmag-.png"))); //$NON-NLS-1$
		zoommoins.setToolTipText(Messages
				.getString("JSoundSampleEditorPanel.18")); //$NON-NLS-1$
		zoommoins.setText(null);

		JButton zoomtout = (JButton) wavCropPanel.getButton("zoomtout"); //$NON-NLS-1$
		zoomtout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				long l = wavDisplayer.getFullLength();
				wavDisplayer.setScale(1.0 * l / wavDisplayer.getWidth());
			}
		});
		zoomtout.setIcon(new ImageIcon(getClass().getResource("viewmagfit.png"))); //$NON-NLS-1$
		zoomtout.setToolTipText(Messages
				.getString("JSoundSampleEditorPanel.21")); //$NON-NLS-1$
		zoomtout.setText(null);

		JButton playbutton = (JButton) wavCropPanel.getButton("play"); //$NON-NLS-1$
		playbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playSelection();
			}
		});

		JButton createPatch = (JButton) wavCropPanel.getButton("createpatch"); //$NON-NLS-1$
		createPatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					sendCurrentSoundSample();
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					JOptionPane.showMessageDialog(
							JCreateSoundSampleFromCropping.this,
							Messages.getString("JCreateSoundSampleFromCropping.3") + ex.getMessage()); //$NON-NLS-1$
					BugReporter.sendBugReport();
				}
			}

		});

		JButton keeponlyselection = (JButton) wavCropPanel
				.getButton("keeponlyselection"); //$NON-NLS-1$
		keeponlyselection.setText(null);
		keeponlyselection.setToolTipText(Messages
				.getString("JCreateSoundSampleFromCropping.5")); //$NON-NLS-1$
		keeponlyselection.setIcon(new ImageIcon(getClass().getResource(
				"edit.png"))); //$NON-NLS-1$
		keeponlyselection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					if (!wavDisplayer.hasSelection())
						return;

					wavDisplayer.crop(wavDisplayer.getSelectionStart(),
							wavDisplayer.getSelectionEnd());

				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					JOptionPane.showMessageDialog(
							JCreateSoundSampleFromCropping.this,
							Messages.getString("JCreateSoundSampleFromCropping.7") + ex.getMessage()); //$NON-NLS-1$
					BugReporter.sendBugReport();
				}
			}
		});

		this.samplename = wavCropPanel.getTextField("samplename"); //$NON-NLS-1$
		assert samplename != null;

		setLayout(new BorderLayout());
		add(wavCropPanel, BorderLayout.CENTER);

	}

	private SoundSample displayedSample;

	public void setDisplayedSample(SoundSample displayedSample)
			throws Exception {
		this.displayedSample = displayedSample;
		if (displayedSample != null) {
			wavDisplayer.displayAudioInputStream(displayedSample
					.getManagedAudioInputStream());
		} else {
			wavDisplayer.displayAudioInputStream(null);
		}

		wavDisplayer.setScale(10.0);

	}

	public SoundSample getDisplayedSample() {
		return displayedSample;
	}

	private void playSelection() {
		try {

			if (!wavDisplayer.hasSelection()) {
				logger.debug("no selection"); //$NON-NLS-1$
				return;

			}

			final AudioInputStream audioStream = wavDisplayer.getAudioStream();
			audioStream.reset();

			final long start = wavDisplayer.getSelectionStart();
			final long end = wavDisplayer.getSelectionEnd();

			final WavPlayer wp = new WavPlayer();

			Runnable r = new Runnable() {
				public void run() {
					try {

						logger.debug("crop stream ..."); //$NON-NLS-1$

						ManagedAudioInputStream croppedStream = GUIInstrumentTools
								.crop(audioStream, start, end);

						wp.playSound(croppedStream, new WavPlayerListener() {
							public void playStateChanged(long pos) {
								final long p = pos;
								logger.debug("time frame : " + pos); //$NON-NLS-1$
								try {
									SwingUtilities
											.invokeAndWait(new Runnable() {
												public void run() {
													wavDisplayer
															.setHightLight(p
																	+ start);
												}
											});
								} catch (Exception ie) {
									logger.error(ie.getMessage(), ie);
								}
							}

							public void playStopped() {
								try {
									SwingUtilities
											.invokeAndWait(new Runnable() {
												public void run() {
													wavDisplayer
															.clearHightLight();
												}
											});
								} catch (Exception ie) {
									logger.error(ie.getMessage(), ie);
								}
							}

							public void startPlaying() {

							}
						});
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
				}
			};

			new Thread(r).start();

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	protected void sendCurrentSoundSample() throws Exception {

		if (!wavDisplayer.hasSelection()) {
			logger.debug("no selection"); //$NON-NLS-1$
			return;
		}
		String sn = samplename.getText();
		if (sn == null || "".equals(sn)) { //$NON-NLS-1$
			JOptionPane.showMessageDialog(JCreateSoundSampleFromCropping.this,
					Messages.getString("JCreateSoundSampleFromCropping.13")); //$NON-NLS-1$
			return;
		}

		ManagedAudioInputStream audioStream = wavDisplayer.getAudioStream();
		audioStream.reset();

		SoundSample s = new SoundSample(sn, rootNote.getNote(),
				new ManagedAudioInputStream(GUIInstrumentTools.crop(
						audioStream, wavDisplayer.getSelectionStart(),
						wavDisplayer.getSelectionEnd())));

		for (Iterator<SoundSampleListener> it = soundSampleListeners.iterator(); it
				.hasNext();) {
			SoundSampleListener l = it.next();
			try {
				if (l != null) {
					logger.debug("sending sound sample " + s); //$NON-NLS-1$
					l.soundSampleReceived(s);
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}

	}

	private Vector<SoundSampleListener> soundSampleListeners = new Vector<SoundSampleListener>();

	public void addListener(SoundSampleListener l) {
		if (l != null)
			soundSampleListeners.add(l);
	}

	public void removeListener(SoundSampleListener l) {
		if (l != null)
			soundSampleListeners.remove(l);
	}

	// test method
	public static void main(String[] args) throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				try {
					BasicConfigurator.configure(new LF5Appender());

					JFrame f = new JFrame();
					f.setSize(500, 500);

					SoundSample soundSample = GUIInstrumentTools
							.loadWavFile(new File(
									"C:/Documents and Settings/Freydiere Patrice/Bureau/Projets/Musique Mécanique/Enregistrement 50 limonaire/gamme-50-limonaire_egalise.wav")); //$NON-NLS-1$

					JCreateSoundSampleFromCropping c = new JCreateSoundSampleFromCropping();
					c.setDisplayedSample(soundSample);

					f.getContentPane().add(c, BorderLayout.CENTER);
					f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					f.setVisible(true);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					System.exit(1);
				}
			}
		});

	}

}
