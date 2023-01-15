package org.barrelorgandiscovery.gui.ainstrument;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.InputStream;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.instrument.sample.ManagedAudioInputStream;
import org.barrelorgandiscovery.instrument.sample.SoundSample;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.FFTTools;
import org.barrelorgandiscovery.tools.HashCodeUtils;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.MidiHelper;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

import com.jeta.forms.components.border.TitledBorderLabel;
import com.jeta.forms.components.panel.FormPanel;

/**
 * Panel for editing a bunch of samples ...
 * 
 * @author Freydiere Patrice
 * 
 */
public class JSoundSampleEditorPanel extends JPanel implements Disposable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6808772337256177743L;

	static class LocalizedNoteChoose {
		private int noteCode;
		private String localizedNote;

		public LocalizedNoteChoose(int noteCode) {
			this.noteCode = noteCode;
			this.localizedNote = MidiHelper.getLocalizedMidiNote(noteCode);

		}

		public int getNoteCode() {
			return noteCode;
		}

		@Override
		public String toString() {
			return localizedNote;
		}

		@Override
		public boolean equals(Object obj) {

			if (obj == null)
				return false;

			if (obj.getClass() == this.getClass()
					|| obj instanceof LocalizedNoteChoose) {
				return noteCode == ((LocalizedNoteChoose) obj).noteCode;
			}

			return false;
		}

		@Override
		public int hashCode() {
			return HashCodeUtils.hash(HashCodeUtils.SEED, noteCode);
		}

	}

	
	private static Logger logger = Logger
			.getLogger(JSoundSampleEditorPanel.class);

	public JSoundSampleEditorPanel() {
		initComponents();
	}

	private JWavDisplayer wavDisplayer;
	private JButton play;
	private JButton playWithLoop;
	private JLabel loopStart;
	private JLabel loopEnd;
	private JComboBox comboRootKey;
	private JSpinner spinnerOctave;

	private JScrollBar scrollBar;

	private JButton resetLoop;

	/**
	 * Toggle button for the selection
	 */
	private JToggleButton selectionMode;

	/**
	 * Toggle button for the loop mode
	 */
	private JToggleButton loopMode;

	private JTextField sampleName;

	private String currentSoundSampleName = null;

	private void initComponents() {

		FormPanel wavPanel = null;
		try {

			InputStream is = getClass().getResourceAsStream(
					"patchwaveditor.jfrm"); //$NON-NLS-1$
			if (is == null)
				throw new Exception("form not found"); //$NON-NLS-1$
			wavPanel = new FormPanel(is);

			wavDisplayer = new JWavDisplayer();
			wavPanel.getFormAccessor().replaceBean(
					wavPanel.getComponentByName("wavdisplayer"), wavDisplayer); //$NON-NLS-1$

			sampleName = wavPanel.getTextField("sampleName"); //$NON-NLS-1$

			play = (JButton) wavPanel.getButton("buttonplaysample"); //$NON-NLS-1$
			assert play != null;
			play.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					play();
				}
			});
			play.setIcon(new ImageIcon(getClass().getResource("player_play.png"))); //$NON-NLS-1$
			play.setToolTipText("Play");
			play.setText(null);

			
			playWithLoop = (JButton) wavPanel.getButton("buttonplaywithloop"); //$NON-NLS-1$
			assert playWithLoop != null;
			playWithLoop.setIcon(new ImageIcon(getClass().getResource("player_play_loop.png"))); //$NON-NLS-1$
			
			playWithLoop.setToolTipText(Messages
					.getString("JSoundSampleEditorPanel.4")); //$NON-NLS-1$
			playWithLoop.setText(null);
			
			loopStart = (JLabel) wavPanel.getLabel("valueloopstart"); //$NON-NLS-1$
			assert loopStart != null;
			loopStart.setText(Messages.getString("JSoundSampleEditorPanel.6")); //$NON-NLS-1$

			loopEnd = (JLabel) wavPanel.getLabel("valueloopend"); //$NON-NLS-1$
			assert loopEnd != null;
			loopEnd.setText(Messages.getString("JSoundSampleEditorPanel.8")); //$NON-NLS-1$

			comboRootKey = wavPanel.getComboBox("rootkey"); //$NON-NLS-1$
			assert comboRootKey != null;

			spinnerOctave = wavPanel.getSpinner("spinneroctave"); //$NON-NLS-1$
			assert spinnerOctave != null;

			// wavborderlabel

			TitledBorderLabel tbl = (TitledBorderLabel) wavPanel
					.getComponentByName("wavborderlabel"); //$NON-NLS-1$
			tbl.setText(Messages.getString("JSoundSampleEditorPanel.0")); //$NON-NLS-1$

			TitledBorderLabel tblproperties = (TitledBorderLabel) wavPanel
					.getComponentByName("wavpropertiestitleborder"); //$NON-NLS-1$
			tblproperties.setText(Messages
					.getString("JSoundSampleEditorPanel.35")); //$NON-NLS-1$

			JLabel labelrootkey = wavPanel.getLabel("labelcomborootkey"); //$NON-NLS-1$
			labelrootkey.setText(Messages
					.getString("JSoundSampleEditorPanel.37")); //$NON-NLS-1$

			Vector<LocalizedNoteChoose> ln = new Vector<LocalizedNoteChoose>();
			for (int i = 0; i < 12; i++) {
				ln.add(new LocalizedNoteChoose(i));
			}
			DefaultComboBoxModel dcbm = new DefaultComboBoxModel(ln);
			comboRootKey.setModel(dcbm);
			comboRootKey.setMaximumRowCount(12);

			spinnerOctave.setModel(new SpinnerNumberModel(4, 0, 10, 1));

		} catch (Exception ex) {
			logger.error("panel construction", ex); //$NON-NLS-1$
		}

		if (wavPanel == null)
			return;

		wavDisplayer.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (currentTool == 0) {
					if ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0) {
						wavDisplayer.setSelectionEnd(wavDisplayer
								.getPosFromScreen(e.getX()));
					} else {
						wavDisplayer.setSelectionStart(wavDisplayer
								.getPosFromScreen(e.getX()));
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {

				if (currentTool == 1) {
					long posFromScreen = wavDisplayer.getPosFromScreen(e.getX());
					if (e.getButton() == MouseEvent.BUTTON1) {
						setStartLoop(posFromScreen);

					} else if (e.getButton() == MouseEvent.BUTTON3) {
						setEndLoop(posFromScreen);
					}
				}
			}
		});

		wavDisplayer.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (currentTool == 0) {
					wavDisplayer.setSelectionEnd(wavDisplayer
							.getPosFromScreen(e.getX()));
				}
			}

		});

		selectionMode = (JToggleButton) wavPanel.getButton("selectmode"); //$NON-NLS-1$
		loopMode = (JToggleButton) wavPanel.getButton("loopMode"); //$NON-NLS-1$

		JButton zoomplus = (JButton) wavPanel.getButton("zoomplus"); //$NON-NLS-1$
		zoomplus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				wavDisplayer.setScale(wavDisplayer.getScale() / 2.0);
			}
		});
		zoomplus.setIcon(new ImageIcon(getClass().getResource("viewmag+.png"))); //$NON-NLS-1$
		zoomplus.setToolTipText(Messages
				.getString("JSoundSampleEditorPanel.15")); //$NON-NLS-1$
		zoomplus.setText(null);

		JButton zoommoins = (JButton) wavPanel.getButton("zoommoins"); //$NON-NLS-1$
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

		JButton zoomtout = (JButton) wavPanel.getButton("zoomtout"); //$NON-NLS-1$
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

		JButton mVolume = (JButton) wavPanel.getButton("modifyVolume");
		mVolume.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				modifyVolume();

			}
		});
		mVolume.setIcon(new ImageIcon(getClass().getResource("mix_volume.png")));
		mVolume.setToolTipText("Adjust the sample volume");
		mVolume.setText(null);

		selectionMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setTool(0);
			}
		});
		selectionMode.setIcon(new ImageIcon(getClass()
				.getResource("pencil.png"))); //$NON-NLS-1$
		selectionMode.setToolTipText(Messages
				.getString("JSoundSampleEditorPanel.23")); //$NON-NLS-1$
		selectionMode.setText(null);

		loopMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setTool(1);
			}
		});
		loopMode.setIcon(new ImageIcon(getClass().getResource(
				"kaboodleloop.png"))); //$NON-NLS-1$
		loopMode.setToolTipText(Messages
				.getString("JSoundSampleEditorPanel.25")); //$NON-NLS-1$
		loopMode.setText(null);

		setTool(0);

		JButton crop = (JButton) wavPanel.getButton("crop"); //$NON-NLS-1$
		crop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (wavDisplayer.hasSelection()) {
						wavDisplayer.crop(wavDisplayer.getSelectionStart(),
								wavDisplayer.getSelectionEnd());
						wavDisplayer.clearSelection();
					}
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		});
		crop.setIcon(new ImageIcon(getClass().getResource("editcut.png"))); //$NON-NLS-1$
		crop.setToolTipText(Messages.getString("JSoundSampleEditorPanel.28")); //$NON-NLS-1$
		crop.setText(null);

		scrollBar = new JScrollBar(JScrollBar.HORIZONTAL);

		wavPanel.getFormAccessor().replaceBean(
				wavPanel.getComponentByName("scroll"), scrollBar); //$NON-NLS-1$
		scrollBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				int value = e.getValue();
				JScrollBar source = (JScrollBar) e.getSource();

				double pos = 1.0 * value / source.getMaximum();

				wavDisplayer.setStart((long) (wavDisplayer.getFullLength() * pos));
			}
		});

		resetLoop = (JButton) wavPanel.getButton("resetLoopButton"); //$NON-NLS-1$
		resetLoop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetLoopParameters();
			}
		});

		setLayout(new BorderLayout());
		add(wavPanel, BorderLayout.CENTER);

		playWithLoop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				WavPlayer wavPlayer = currentWavPlayer.get();

				if (wavPlayer != null && wavPlayer.isPlaying()) {
					logger.debug("cancel playing ... "); //$NON-NLS-1$
					wavPlayer.cancelPlay();
					return;
				}

				logger.debug("playing with loop ...."); //$NON-NLS-1$

				playWithLoop();
			}
		});

		JButton findNote = (JButton) wavPanel.getButton("findnote"); //$NON-NLS-1$
		findNote.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {

					int n = tryFindNote();
					if (n != -1) {
						String labelNote = MidiHelper.getLocalizedMidiNote(n)
								+ " " + MidiHelper.getOctave(n);

						logger.debug("note found : " + labelNote);

						if (JOptionPane.showConfirmDialog(null, "Note "
								+ labelNote + " found, accept ?") == JOptionPane.YES_OPTION) {
							changeMidiRootNote(n);
						}
					}

				} catch (Exception ex) {
					logger.error("error finding note :" + ex.getMessage(), ex);
				}
			}
		});

	}

	private int currentTool = 0; // selection

	private void setTool(int tool) {

		this.currentTool = tool;

		if (tool == 0) {
			selectionMode.setSelected(true);
			loopMode.setSelected(false);
		} else {
			selectionMode.setSelected(false);
			loopMode.setSelected(true);
		}

	}

	private void modifyVolume() {
		try {

			String retvalue = JOptionPane.showInputDialog("Volume Factor", 1.0);
			if (retvalue == null)
				return;

			float factor = 1.0f;
			try {
				factor = Float.parseFloat(retvalue);
			} catch (Exception ex) {
				logger.error("error reading input value " + ex.getMessage(), ex);
			}
			wavDisplayer.adjust(factor);
			logger.debug("volume adjusted");

		} catch (Exception ex) {
			logger.error("error in scaling the volume :" + ex.getMessage(), ex);
			JMessageBox.showMessage(this, "Error while adjusting the volume :"
					+ ex.getMessage());
			BugReporter.sendBugReport();
		}
	}

	/**
	 * define the currently visible sample
	 * 
	 * @param s
	 *            the sample to view
	 * @throws Exception
	 */
	public void setCurrentlyEditedSoundSample(SoundSample s) throws Exception {
		if (s == null) {
			wavDisplayer.displayAudioInputStream(null);
			wavDisplayer.resetLoopParameters();
			currentSoundSampleName = null;
			return;
		}

		ManagedAudioInputStream ais = s.getManagedAudioInputStream();

		wavDisplayer.displayAudioInputStream(ais);

		long sampleStartLoop = s.getLoopStart();
		long sampleEndLoop = s.getLoopEnd();

		if (sampleStartLoop == sampleEndLoop) {
			resetLoopParameters();
		} else {
			setStartLoop(sampleStartLoop);
			setEndLoop(sampleEndLoop);
		}

		// setting root note

		int rootNote = s.getMidiRootNote();

		changeMidiRootNote(rootNote);

		this.currentSoundSampleName = s.getName();

		this.sampleName.setText(s.getName());

		this.scrollBar.setMaximum((int) ais.getFrameLength());
		this.scrollBar.setMinimum(0);

	}

	/**
	 * @param rootNote
	 */
	protected void changeMidiRootNote(int rootNote) {
		int midiRootNote = rootNote;
		int note = MidiHelper.extractNoteFromMidiCode(midiRootNote);
		int octave = MidiHelper.getOctave(midiRootNote);

		spinnerOctave.setValue(octave);

		DefaultComboBoxModel m = (DefaultComboBoxModel) comboRootKey.getModel();
		for (int i = 0; i < m.getSize(); i++) {
			LocalizedNoteChoose cn = (LocalizedNoteChoose) m.getElementAt(i);
			if (cn.getNoteCode() == note) {
				logger.debug("note found " + note); //$NON-NLS-1$
				comboRootKey.setSelectedItem(cn);
				break;
			}
		}
	}

	private void setEndLoop(long sampleEndLoop) {
		wavDisplayer.setCurrentEndLoopPos(sampleEndLoop);
		loopEnd.setText("" + sampleEndLoop); //$NON-NLS-1$
	}

	private void setStartLoop(long sampleStartLoop) {
		wavDisplayer.setCurrentStartLoopPos(sampleStartLoop);
		loopStart.setText("" + sampleStartLoop); //$NON-NLS-1$
	}

	private void resetLoopParameters() {
		wavDisplayer.resetLoopParameters();
		loopEnd.setText(Messages.getString("JSoundSampleEditorPanel.32")); //$NON-NLS-1$
		loopStart.setText(Messages.getString("JSoundSampleEditorPanel.33")); //$NON-NLS-1$
	}

	/**
	 * Construct a sound sample objet from what is displayed ...
	 * 
	 * @return
	 * @throws Exception
	 */
	public SoundSample constructSoundSample() throws Exception {

		if (currentSoundSampleName == null)
			return null;

		LocalizedNoteChoose localizedSelectedNoteItem = (LocalizedNoteChoose) comboRootKey
				.getSelectedItem();
		int midinote = MidiHelper.computeMidiCodeFromNoteAndOctave(
				localizedSelectedNoteItem.getNoteCode(),
				(Integer) spinnerOctave.getValue());

		SoundSample s = new SoundSample(sampleName.getText(), midinote,
				wavDisplayer.getAudioStream());
		s.setLoopStart(wavDisplayer.getCurrentStartLoopPos());
		s.setLoopEnd(wavDisplayer.getCurrentEndLoopPos());
		return s;
	}

	private AtomicReference<WavPlayer> currentWavPlayer = new AtomicReference<WavPlayer>(
			null);

	private void play() {
		try {
			final ManagedAudioInputStream audioStream = wavDisplayer
					.getAudioStream();
			audioStream.reset();

			final WavPlayer wp = new WavPlayer();

			Runnable r = new Runnable() {
				public void run() {
					try {
						wp.playSound(audioStream, new WavPlayerListener() {
							public void playStateChanged(long pos) {
								logger.debug("time frame : " + pos); //$NON-NLS-1$
								wavDisplayer.setHightLight(pos);
							}

							public void playStopped() {
								wavDisplayer.clearHightLight();
							}

							public void startPlaying() {

							}
						});
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
				}
			};

			setNewCurrentWavPlayerAndStopPlayingOldOne(wp);

			new Thread(r).start();

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	private void setNewCurrentWavPlayerAndStopPlayingOldOne(final WavPlayer wp) {
		WavPlayer oldPlayer = currentWavPlayer.getAndSet(wp);
		if (oldPlayer != null)
			oldPlayer.cancelPlay();
	}

	private void playWithLoop() {
		try {
			final ManagedAudioInputStream audioStream = wavDisplayer
					.getAudioStream();
			audioStream.reset();

			final WavPlayer wp = new WavPlayer();

			Runnable r = new Runnable() {
				public void run() {
					try {
						wp.playSoundWithLoops(audioStream,
								new WavPlayerListener() {
									public void playStateChanged(long pos) {
										logger.debug("time frame : " + pos); //$NON-NLS-1$
										wavDisplayer.setHightLight(pos);
									}

									public void playStopped() {
										wavDisplayer.clearHightLight();
									}

									public void startPlaying() {

									}
								}, new LoopParameterProvider() {
									public long getEndLoop() {

										return wavDisplayer
												.getCurrentEndLoopPos();
									}

									public long getStartLoop() {

										return wavDisplayer
												.getCurrentStartLoopPos();
									}
								});
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
				}
			};

			setNewCurrentWavPlayerAndStopPlayingOldOne(wp);

			new Thread(r).start();

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	public void dispose() {
		logger.debug("stop the current play ....");
		setNewCurrentWavPlayerAndStopPlayingOldOne(null);
	}

	/**
	 * try to find the note from the sample
	 * 
	 * @return the midi code, or -1 if not found
	 * @throws Exception
	 */
	private int tryFindNote() throws Exception {
		ManagedAudioInputStream mas = wavDisplayer.getAudioStream();
		if (mas == null) {
			return -1;
		}

		return FFTTools.findMidiNote(mas);

	}

	/**
	 * Test procedure ...
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		// set the look and feel
		// try {
		//
		// javax.swing.UIManager
		// .setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel");
		// //$NON-NLS-1$
		// LiquidLookAndFeel.setLiquidDecorations(true, "mac"); //$NON-NLS-1$
		// // LiquidLookAndFeel.setStipples(false);
		// LiquidLookAndFeel.setToolbarFlattedButtons(true);
		//
		// } catch (Exception e) {
		// e.printStackTrace(System.err);
		// }
		//

		final JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(800, 600);
		f.getContentPane().setLayout(new BorderLayout());

		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				try {
					SoundSample soundSample = GUIInstrumentTools
							.loadWavFile(new File(
									"contributions\\Peter Griffith\\samples\\bass d.wav")); //$NON-NLS-1$

					soundSample.setMidiRootNote(34);

					JSoundSampleEditorPanel ssp = new JSoundSampleEditorPanel();
					f.getContentPane().add(ssp, BorderLayout.CENTER);

					ssp.setCurrentlyEditedSoundSample(soundSample);

					f.setVisible(true);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					// System.exit(1);
				}
			}
		});

	}

}
