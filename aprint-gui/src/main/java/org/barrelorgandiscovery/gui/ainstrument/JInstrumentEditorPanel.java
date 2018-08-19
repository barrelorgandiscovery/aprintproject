package org.barrelorgandiscovery.gui.ainstrument;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.EditableInstrument;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.editableinstrument.InstrumentScript;
import org.barrelorgandiscovery.editableinstrument.ScaleListener;
import org.barrelorgandiscovery.editableinstrument.SoundSampleListListener;
import org.barrelorgandiscovery.gui.ainstrument.pianoroll.JPianoRollComponent;
import org.barrelorgandiscovery.gui.ainstrument.pianoroll.PianoRenderingNote;
import org.barrelorgandiscovery.gui.ascale.JScaleEditorPanel;
import org.barrelorgandiscovery.gui.ascale.ScaleComponent;
import org.barrelorgandiscovery.gui.ascale.ScaleEditorPrefs;
import org.barrelorgandiscovery.gui.ascale.ScaleHighlightListener;
import org.barrelorgandiscovery.instrument.SampleMapping;
import org.barrelorgandiscovery.instrument.sample.SoundSample;
import org.barrelorgandiscovery.instrument.sample.SoundSampleIO;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.prefs.DummyPrefsStorage;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.scale.ReferencedPercussion;
import org.barrelorgandiscovery.scale.ReferencedPercussionList;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.FileNameExtensionFilter;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.SwingUtils;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

import com.jeta.forms.components.image.ImageComponent;
import com.jeta.forms.components.panel.FormPanel;
import com.sun.media.sound.SF2Soundbank;

/**
 * Panel used for edit an instrument
 * 
 * @author Freydiere Patrice
 * 
 */
public class JInstrumentEditorPanel extends JPanel {

	/**
	 * serial number for class persistance
	 */
	private static final long serialVersionUID = -3093192779388777249L;

	private static Logger logger = Logger
			.getLogger(JInstrumentEditorPanel.class);

	private static int MAX_IMAGE_WIDTH = 300;
	private static int MAX_IMAGE_HEIGHT = 300;

	/**
	 * The current modified instrument
	 */
	private IEditableInstrument model;

	/**
	 * Object for playing the instrument ...
	 */
	private SBPlayer player;

	/**
	 * Object for loading and saving sound samples
	 */
	private SoundSampleIO ssio = new SoundSampleIO();

	/**
	 * Parent frame for modal dialogs
	 */
	private Frame parentFrame;

	public JInstrumentEditorPanel(Frame parentFrame) throws Exception {
		super();

		this.parentFrame = parentFrame;

		initComponents();

		player = new SBPlayer();
		
		setModel(new EditableInstrument());
		this.model.clearDirty();

		logger.debug("opening sbplayer ..."); //$NON-NLS-1$

		player.open();
	}

	/**
	 * Define the new instrument to edit
	 * 
	 * @param newModel
	 */
	public void setModel(IEditableInstrument newModel) {
		this.model = newModel;

		if (model != null) {
			model.addListener(new SoundSampleListListener() {
				public void soundSampleAdded(SoundSample sampleAdded,
						String pipeStopGroup) {

					soundSampleListChanged(getCurrentPipeStopGroup());
				}

				public void soundSampleRemoved(SoundSample sampleRemoved,
						String pipeStopGroup) {
					soundSampleListChanged(getCurrentPipeStopGroup());
				}

				public void hashChanged(HashMap<String, SoundSample> hash) {
					soundSampleListChanged(getCurrentPipeStopGroup());
				}
			});

			model.addListener(new ScaleListener() {
				public void ScaleChanged(Scale oldScale, Scale newScale) {
					scaleChanged();
				}

			});

			instrumentDescription.setText(newModel.getInstrumentDescription());
			Image i = newModel.getInstrumentPicture();
			if (i != null) {
				instrumentImage.setIcon(new ImageIcon(newModel
						.getInstrumentPicture()));
				instrumentImage.revalidate();
				instrumentImage.repaint();
			} else {
				instrumentImage.setIcon(null);
			}

			instrumentName.setText(newModel.getName());

		}

		updatePipeStopGroupCombo();
		updateScale();
		updateSoundListForCurrentPipeStopGroup(getCurrentPipeStopGroup());
		updateScripts();
		updatePianoRoll();
		updateCurrentSoundBank();
	}

	/**
	 * Get the instrument currently edited
	 * 
	 * @return
	 */
	public IEditableInstrument getModel() {

		panelScripting.commitProperties();
		updateModelWithScaleInformations();

		return this.model;

	}

	private JPianoRollComponent pianoroll;
	private ScaleComponent scalePreview;

	private JButton addSound;
	private JButton modifySound;
	private JButton removeSound;
	private JButton removeSoundMapping;

	private JButton clearSoundSampleSelection;

	private JComboBox pipeStopGroupCombo;

	private JTable listSounds = new JTable();

	private class SoundListRenderer extends DefaultTableCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5849635301564539640L;
		private ImageIcon icon = new ImageIcon(
				SoundListRenderer.class.getResource("arts.png")); //$NON-NLS-1$

		public SoundListRenderer() {
			super();
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {

			SoundSample ss = (SoundSample) value;

			super.getTableCellRendererComponent(table, ss.getName(),
					isSelected, hasFocus, row, column);

			setIcon(icon);

			return this;

		}

	}

	private class SoundListEditor extends AbstractCellEditor implements
			TableCellEditor {

		/**
		 * 
		 */
		private static final long serialVersionUID = 357556014021504292L;

		private SoundSample ss;

		public SoundListEditor() {
			super();
		}

		public Object getCellEditorValue() {
			ss.setName(tf.getText());
			logger.debug("getCellEditorValue : " + ss); //$NON-NLS-1$
			return ss;
		}

		private JTextField tf = new JTextField();

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {

			final SoundSample ss = (SoundSample) value;
			this.ss = ss;

			tf.setText(ss.getName());
			return tf;
		}

	}

	private StatedPianoRollMouseHandler pianorollMouseHandler = new PianoRollMouseHandler();

	private abstract class StatedPianoRollMouseHandler implements
			MouseListener, MouseMotionListener {

	}

	private class PianoRollMouseHandler extends StatedPianoRollMouseHandler {

		public void mouseMoved(MouseEvent e) {

			PianoRenderingNote currentSelectedNote2 = pianoroll
					.getCurrentSelectedNote();

			if (currentSelectedNote2 != null) {
				if (currentSelectedNote2.getPolygon().contains(e.getX(),
						e.getY()))
					// nothing to do ...
					return;
			}

			PianoRenderingNote n = pianoroll.searchForKey(e.getX(), e.getY());

			pianoroll.setCurrentSelectedNote(n);

		}

		private int state = 0;
		private int firstPos = -1;

		public void mouseDragged(MouseEvent e) {

			mouseMoved(e);

			if (state == 1)
				return;

			PianoRenderingNote searchForKey = pianoroll.searchForKey(e.getX(),
					e.getY());

			if (getCurrentSelectedSoundSample() == null) {
				// play the note ...
				if (searchForKey != null
						&& searchForKey.getMidicode() != player
								.getCurrentPlayedNote())
					player.playNote(searchForKey.getMidicode());
				return;
			}

			pianoroll.setCursor(Cursor
					.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));

			if (searchForKey == null)
				return;

			logger.debug("start " + searchForKey.getMidicode()); //$NON-NLS-1$
			state = 1;
			firstPos = searchForKey.getMidicode();

		}

		public void mouseClicked(MouseEvent e) {

		}

		public void mouseEntered(MouseEvent e) {

		}

		public void mouseExited(MouseEvent e) {
			logger.debug("exited"); //$NON-NLS-1$

		}

		public void mousePressed(MouseEvent e) {
			try {
				logger.debug("pressed"); //$NON-NLS-1$

				if (getCurrentSelectedSoundSample() == null) {
					PianoRenderingNote searchForKey = pianoroll.searchForKey(
							e.getX(), e.getY());
					if (searchForKey != null) {
						player.playNote(searchForKey.getMidicode());
					}
				}

			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}

		public void mouseReleased(MouseEvent e) {

			logger.debug("released"); //$NON-NLS-1$
			if (state == 1) {
				logger.debug("end of "); //$NON-NLS-1$
				pianoroll.setCursor(Cursor.getDefaultCursor());

				PianoRenderingNote searchForKey = pianoroll.searchForKey(
						e.getX(), e.getY());
				if (searchForKey == null) {
					state = 0;
					return;
				}

				// change the mapping ...

				model.setSampleMapping(getCurrentPipeStopGroup(),
						getCurrentSelectedSoundSample(), firstPos,
						searchForKey.getMidicode());

				sampleMappingChanged();

				state = 0;
			} else {
				try {

					player.stopNote();

				} catch (Exception ex) {
					logger.error(ex);
				}
			}
		}
	}

	private JTabbedPane tabbedPane;

	private void initComponents() throws Exception {

		setLayout(new BorderLayout());

		tabbedPane = new JTabbedPane();

		logger.debug("loading general properties panel"); //$NON-NLS-1$

		FormPanel panelGeneral = null;
		// load the form ...
		try {

			InputStream is = getClass().getResourceAsStream(
					"instrumenteditorpanelgeneralinformations.jfrm"); //$NON-NLS-1$
			if (is == null)
				throw new Exception("form not found"); //$NON-NLS-1$
			panelGeneral = new FormPanel(is);

		} catch (Exception ex) {
			logger.error("panel construction", ex); //$NON-NLS-1$
			throw new Exception(ex.getMessage(), ex);
		}

		tabbedPane.add(panelGeneral,
				Messages.getString("JInstrumentEditorPanel.1")); //$NON-NLS-1$

		logger.debug("adding the scale editor tab"); //$NON-NLS-1$
		ScaleEditorPrefs p = new ScaleEditorPrefs(new DummyPrefsStorage());

		scaleEditorPanel = new JScaleEditorPanel(parentFrame, p);
		tabbedPane.add(scaleEditorPanel,
				Messages.getString("JInstrumentEditorPanel.3")); //$NON-NLS-1$

		logger.debug("loading sound mapping panel"); //$NON-NLS-1$
		FormPanel panelMapping = null;
		try {

			InputStream is = getClass().getResourceAsStream(
					"instrumenteditorpanelsoundmapping.jfrm"); //$NON-NLS-1$
			if (is == null)
				throw new Exception("form not found"); //$NON-NLS-1$
			panelMapping = new FormPanel(is);

		} catch (Exception ex) {
			logger.error("panel construction", ex); //$NON-NLS-1$
			throw new Exception(ex.getMessage(), ex);
		}

		tabbedPane.add(panelMapping,
				Messages.getString("JInstrumentEditorPanel.5")); //$NON-NLS-1$

		FormPanel fpDrumSounds = null;
		try {

			InputStream is = getClass().getResourceAsStream(
					"instrumenteditorpaneldrumssoundassociation.jfrm"); //$NON-NLS-1$
			if (is == null)
				throw new Exception("form not found"); //$NON-NLS-1$
			fpDrumSounds = new FormPanel(is);

		} catch (Exception ex) {
			logger.error("panel construction", ex); //$NON-NLS-1$
			throw new Exception(ex.getMessage(), ex);
		}

		drumsoundlist = (JTable) fpDrumSounds
				.getComponentByName("drumsoundlist"); //$NON-NLS-1$
		JButton addDrumSound = (JButton) fpDrumSounds
				.getButton("setwavassociation"); //$NON-NLS-1$
		addDrumSound.setText(Messages.getString("JInstrumentEditorPanel.202")); //$NON-NLS-1$
		addDrumSound.setIcon(new ImageIcon(getClass().getResource(
				"artsplus.png"))); //$NON-NLS-1$
		addDrumSound.setToolTipText(Messages
				.getString("JInstrumentEditorPanel.204")); //$NON-NLS-1$

		addDrumSound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addDrumSound();
			}
		});

		JButton editDrumSound = (JButton) fpDrumSounds.getButton("update"); //$NON-NLS-1$
		editDrumSound.setText(Messages.getString("JInstrumentEditorPanel.206")); //$NON-NLS-1$
		editDrumSound
				.setIcon(new ImageIcon(getClass().getResource("arts.png"))); //$NON-NLS-1$
		editDrumSound.setToolTipText(Messages
				.getString("JInstrumentEditorPanel.208")); //$NON-NLS-1$
		editDrumSound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editDrumSoundSample();
			}
		});

		JButton removeDrumSound = (JButton) fpDrumSounds
				.getButton("resetwavassociation"); //$NON-NLS-1$
		removeDrumSound.setText(Messages
				.getString("JInstrumentEditorPanel.210")); //$NON-NLS-1$
		removeDrumSound.setIcon(new ImageIcon(getClass().getResource(
				"artsmoins.png"))); //$NON-NLS-1$
		removeDrumSound.setToolTipText(Messages
				.getString("JInstrumentEditorPanel.212")); //$NON-NLS-1$
		removeDrumSound.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				removeDrumSound();
			}
		});

		JButton playDrumSound = (JButton) fpDrumSounds.getButton("playdrum"); //$NON-NLS-1$
		playDrumSound.setToolTipText(Messages.getString("JInstrumentEditorPanel.1001")); //$NON-NLS-1$
		playDrumSound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playDrumSound();
			}
		});
		playDrumSound
				.setIcon(new ImageIcon(getClass().getResource("arts.png"))); //$NON-NLS-1$

		updateDrumSoundList();

		tabbedPane.add(fpDrumSounds,
				Messages.getString("JInstrumentEditorPanel.213")); //$NON-NLS-1$

		logger.debug("adding scripting panel ... "); //$NON-NLS-1$

		panelScripting = new JInstrumentScriptingPanel();
		panelScripting.addScriptsChangedListener(new ScriptsChangedListener() {
			public void scriptsChanged(InstrumentScript[] scripts) {

				long start = System.currentTimeMillis();
				logger.debug("scriptChanged :" + scripts); //$NON-NLS-1$

				HashMap<String, InstrumentScript> hs = new HashMap<String, InstrumentScript>();

				for (int j = 0; j < scripts.length; j++) {
					InstrumentScript instrumentScript = scripts[j];
					hs.put(instrumentScript.getName(), instrumentScript);

					InstrumentScript modelInstrumentScript = model
							.findScript(instrumentScript.getName());
					if (modelInstrumentScript != null) {
						if (!modelInstrumentScript.equals(instrumentScript)) {
							model.removeScript(instrumentScript.getName());
							model.addScript(instrumentScript);
						}
					} else {
						// add the new script ...
						model.addScript(instrumentScript);
					}
				}

				// check for suppressed scripts ...

				InstrumentScript[] modelInstrumentScripts = model.getScripts();
				for (int i = 0; i < modelInstrumentScripts.length; i++) {
					InstrumentScript instrumentScript = modelInstrumentScripts[i];

					if (!hs.containsKey(instrumentScript.getName())) {
						model.removeScript(instrumentScript.getName());
					}

				}
				logger.debug("scripts changed ..." //$NON-NLS-1$
						+ (System.currentTimeMillis() - start));
			}
		});

		tabbedPane.add(panelScripting,
				Messages.getString("JInstrumentEditorPanel.8")); //$NON-NLS-1$

		logger.debug("adding tab panels ... "); //$NON-NLS-1$
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				logger.debug("tab changed :" + e.getSource()); //$NON-NLS-1$
				JTabbedPane p = (JTabbedPane) e.getSource();
				Component selectedComponent = p.getSelectedComponent();
				logger.debug("selected component : " + selectedComponent); //$NON-NLS-1$

				if (selectedComponent != scaleEditorPanel) {
					updateModelWithScaleInformations();
					updateDrumSoundList();
				}

				panelScripting.commitProperties();

			}
		});

		logger.debug("adding the pianoroll component ..."); //$NON-NLS-1$

		pianoroll = new JPianoRollComponent();
		JScrollPane scPianoroll = new JScrollPane(pianoroll);

		panelMapping.getFormAccessor().replaceBean(
				panelMapping.getComponentByName("pianoroll"), scPianoroll); //$NON-NLS-1$

		// register the events for the pianoroll ...

		pianoroll.addMouseListener(pianorollMouseHandler);
		pianoroll.addMouseMotionListener(pianorollMouseHandler);

		scalePreview = new ScaleComponent();
		// adding hightlight feedback

		scalePreview.addListener(new ScaleHighlightListener() {
			public void hightlightReseted() {
				pianoroll.clearCurrentSelectedNote();
			}

			public void trackIsHighlighted(AbstractTrackDef td) {
				if (td == null) {
					pianoroll.clearCurrentSelectedNote();
					return;
				}

				if (td instanceof NoteDef) {
					NoteDef c = (NoteDef) td;
					int midiNote = c.getMidiNote();

					pianoroll.setCurrentSelectedNote(midiNote);

				} else {
					pianoroll.clearCurrentSelectedNote();
				}
			}
		});

		scalePreview.setSpeedDraw(true);

		panelMapping.getFormAccessor().replaceBean(
				panelMapping.getLabel("scalepreviewer"), //$NON-NLS-1$
				new JScrollPane(scalePreview));

		Border bsoundsamplelist = ((JLabel) panelMapping.getFormAccessor(
				"patchpanel").getComponentByName("soundsamplelist")).getBorder(); //$NON-NLS-1$ //$NON-NLS-2$

		JScrollPane listSoundsScrollTable = new JScrollPane(listSounds);

		listSoundsScrollTable.setColumnHeader(null);
		listSoundsScrollTable.setMinimumSize(new Dimension(100, 200));

		listSounds.setShowGrid(true);

		listSounds.setRowHeight(20);

		listSounds.setRowSelectionAllowed(true);

		listSounds.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		listSounds.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						logger.debug("selected sound sample changed ..."); //$NON-NLS-1$

						int index = e.getFirstIndex();
						if (index != -1) {
							if (index < listSounds.getModel().getRowCount())
								currentSelectedSoundSampleChanged((SoundSample) listSounds
										.getModel().getValueAt(index, 0));
						}
					}
				});

		listSounds.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {

				if (e.getClickCount() == 2) {
					modifyCurrentSelectedSoundSampleInCurrentPipeStopGroup();

				} else if (e.getClickCount() == 1
						&& (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0) {
					int index = listSounds.getSelectionModel()
							.getMinSelectionIndex();
					if (index != -1) {
						listSounds.editCellAt(index, 0);
					}

				}

			}
		});

		// listSounds.setBorder(new TitledBorder(Messages
		// .getString("JInstrumentEditor.0")));//$NON-NLS-1$

		// JScrollPane spListSounds = new JScrollPane(listSounds);
		// spListSounds.setBorder(bsoundsamplelist);

		panelMapping.getFormAccessor("patchpanel").replaceBean( //$NON-NLS-1$
				panelMapping.getComponentByName("soundsamplelist"), //$NON-NLS-1$
				listSoundsScrollTable);

		updateScale();

		updateSoundListForCurrentPipeStopGroup(null);

		// //////////////////////////////////////////////////////////////////
		// buttons

		addSound = (JButton) panelMapping.getButton("addsound"); //$NON-NLS-1$
		addSound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addSoundSampleInCurrentPipeStopGroup();
			}
		});
		addSound.setToolTipText(Messages.getString("JInstrumentEditor.14")); //$NON-NLS-1$
		addSound.setIcon(new ImageIcon(getClass().getResource("artsplus.png"))); //$NON-NLS-1$
		addSound.setText(null);

		modifySound = (JButton) panelMapping.getButton("modifysound"); //$NON-NLS-1$
		modifySound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modifyCurrentSelectedSoundSampleInCurrentPipeStopGroup();
			}
		});
		modifySound.setToolTipText(Messages.getString("JInstrumentEditor.16")); //$NON-NLS-1$
		modifySound.setIcon(new ImageIcon(getClass().getResource(
				"artsbuilder.png"))); //$NON-NLS-1$
		modifySound.setText(null);

		removeSoundMapping = (JButton) panelMapping
				.getButton("removeSoundMapping"); //$NON-NLS-1$
		removeSoundMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeCurrentSelectedSampleSoundMapping();
			}
		});
		removeSoundMapping.setToolTipText(Messages
				.getString("JInstrumentEditorPanel.16")); //$NON-NLS-1$
		removeSoundMapping.setText(null);
		removeSoundMapping.setIcon(new ImageIcon(getClass().getResource(
				"arts-remove-mapping.png")));//$NON-NLS-1$

		removeSound = (JButton) panelMapping.getButton("removesound"); //$NON-NLS-1$
		removeSound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeSoundSampleInCurrentPipeStopGroup();
			}
		});
		removeSound.setText(null);
		removeSound.setToolTipText(Messages.getString("JInstrumentEditor.18")); //$NON-NLS-1$
		removeSound.setIcon(new ImageIcon(getClass().getResource(
				"artsmoins.png"))); //$NON-NLS-1$

		//
		// buttonModifyScale = (JButton) panelMapping
		// .getButton("buttonmodifyscale"); //$NON-NLS-1$
		// buttonModifyScale.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// modifyCurrentScale();
		// }
		// });
		// buttonModifyScale.setText(Messages.getString("JInstrumentEditor.76"));
		// //$NON-NLS-1$
		//

		JButton addfromcropfile = (JButton) panelMapping
				.getButton("addfromcropfile"); //$NON-NLS-1$
		addfromcropfile.setToolTipText(Messages
				.getString("JInstrumentEditor.79")); //$NON-NLS-1$
		addfromcropfile.setIcon(new ImageIcon(getClass().getResource(
				"artsplus.png"))); //$NON-NLS-1$
		addfromcropfile.setText(null);
		addfromcropfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addSampleMappingFromCroppedWav();
			}
		});

		loadsoundsample = (JButton) panelMapping.getButton("loadsoundsample");//$NON-NLS-1$
		loadsoundsample.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addLoadedSoundSampleInCurrentPipeStopGroup();
			}
		});
		loadsoundsample.setToolTipText(Messages.getString("JInstrumentEditorPanel.1002")); //$NON-NLS-1$
		loadsoundsample.setIcon(new ImageIcon(getClass().getResource(
				"soundsampleload.png")));//$NON-NLS-1$

		savesoundsample = (JButton) panelMapping.getButton("savesoundsample");//$NON-NLS-1$
		savesoundsample.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveCurrentlySelectedSoundSample();
			}
		});
		savesoundsample.setToolTipText(Messages.getString("JInstrumentEditorPanel.1003")); //$NON-NLS-1$
		savesoundsample.setIcon(new ImageIcon(getClass().getResource(
				"soundsamplesave.png")));//$NON-NLS-1$

		pipeStopGroupCombo = panelMapping.getComboBox("pipestopgroupcombo"); //$NON-NLS-1$
		updatePipeStopGroupCombo();
		pipeStopGroupCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				pipeStopGroupChanged((String) pipeStopGroupCombo
						.getSelectedItem());
			}
		});

		clearSoundSampleSelection = (JButton) panelMapping
				.getButton("clearsoundsampleselection"); //$NON-NLS-1$
		clearSoundSampleSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listSounds.clearSelection();
			}
		});

		clearSoundSampleSelection.setText(Messages
				.getString("JInstrumentEditor.23")); //$NON-NLS-1$
		clearSoundSampleSelection.setToolTipText(Messages
				.getString("JInstrumentEditor.24")); //$NON-NLS-1$
		clearSoundSampleSelection.setIcon(new ImageIcon(getClass().getResource(
				"artsmidimanager.png"))); //$NON-NLS-1$

		instrumentName = (JTextField) panelGeneral
				.getTextField("instrumentname"); //$NON-NLS-1$
		instrumentName.getDocument().addDocumentListener(
				new DocumentListener() {
					public void changedUpdate(DocumentEvent e) {
						update(e);
					}

					public void insertUpdate(DocumentEvent e) {
						update(e);
					}

					public void removeUpdate(DocumentEvent e) {
						update(e);
					}

					void update(DocumentEvent e) {
						Document doc = e.getDocument();
						String t = ""; //$NON-NLS-1$
						try {
							t = doc.getText(0, doc.getLength());
						} catch (BadLocationException ex) {
						}
						model.setName(t);
					}
				});

		instrumentDescription = (JTextArea) panelGeneral
				.getComponentByName("instrumentdescription"); //$NON-NLS-1$
		instrumentDescription.getDocument().addDocumentListener(
				new DocumentListener() {
					public void changedUpdate(DocumentEvent e) {
						update(e);
					}

					public void insertUpdate(DocumentEvent e) {
						update(e);
					}

					public void removeUpdate(DocumentEvent e) {
						update(e);
					}

					void update(DocumentEvent e) {
						Document doc = e.getDocument();
						String t = ""; //$NON-NLS-1$
						try {
							t = doc.getText(0, doc.getLength());
						} catch (BadLocationException ex) {
						}
						model.setInstrumentDescription(t);
					}
				});

		instrumentImage = (ImageComponent) panelGeneral
				.getComponentByName("instrumentimage"); //$NON-NLS-1$

		JLabel labelinstrumentpicture = (JLabel) panelGeneral
				.getComponentByName("labelinstrumentpicture"); //$NON-NLS-1$
		labelinstrumentpicture.setText(Messages
				.getString("JInstrumentEditor.57")); //$NON-NLS-1$

		JLabel labelinstrumentdescription = (JLabel) panelGeneral
				.getComponentByName("labelinstrumentdescription"); //$NON-NLS-1$
		labelinstrumentdescription.setText(Messages
				.getString("JInstrumentEditor.59")); //$NON-NLS-1$

		JLabel labelinstrumentname = (JLabel) panelGeneral
				.getComponentByName("labelinstrumentname"); //$NON-NLS-1$
		labelinstrumentname.setText(Messages.getString("JInstrumentEditor.61")); //$NON-NLS-1$

		JLabel registrationlabel = (JLabel) panelMapping
				.getComponentByName("registrationlabel"); //$NON-NLS-1$
		registrationlabel.setText(Messages.getString("JInstrumentEditor.62")); //$NON-NLS-1$

		JComponent componentByName = (JComponent) panelGeneral
				.getComponentByName("descriptionform"); //$NON-NLS-1$
		TitledBorder titledborder = (TitledBorder) componentByName.getBorder();
		titledborder.setTitle(Messages.getString("JInstrumentEditor.64")); //$NON-NLS-1$

		buttonchoicepicture = (JButton) panelGeneral
				.getButton("buttonchoicepicture"); //$NON-NLS-1$
		buttonchoicepicture.setText(Messages.getString("JInstrumentEditor.1")); //$NON-NLS-1$

		buttonchoicepicture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					JFileChooser f = new JFileChooser();
					f.setMultiSelectionEnabled(false);
					f.setFileFilter(new FileNameExtensionFilter(
							Messages.getString("JInstrumentEditor.56"), new String[] { "gif", "jpg", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
									"png" })); //$NON-NLS-1$

					if (f.showOpenDialog(JInstrumentEditorPanel.this) == JFileChooser.APPROVE_OPTION) {
						logger.debug("loading image ..."); //$NON-NLS-1$

						File fimage = f.getSelectedFile();
						if (fimage != null) {

							BufferedImage biimage = ImageTools
									.loadImageAndCrop(fimage, MAX_IMAGE_WIDTH,
											MAX_IMAGE_HEIGHT);
							model.setInstrumentPicture(biimage);
							instrumentImage.setIcon(new ImageIcon(biimage,
									"instrument picture")); //$NON-NLS-1$

							instrumentImage.revalidate();
							instrumentImage.repaint();

						}
					}
				} catch (Exception ex) {
					logger.error("error when loading image ..."); //$NON-NLS-1$
				}
			}
		});

		add(tabbedPane, BorderLayout.CENTER);

		// install model events ....

		updateContextualButtons();

	}

	/**
	 * Class for referencing a Drum association object
	 * 
	 * @author use
	 * 
	 */
	private static class DrumObjectReferenceDisplay {

		private String label = null;
		private PercussionDef d = null;

		public DrumObjectReferenceDisplay(String label, PercussionDef d) {
			this.label = label;
			this.d = d;
		}

		public PercussionDef getPercussionDef() {
			return this.d;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	/**
	 * Update Drum Element List
	 */
	private void updateDrumSoundList() {

		DefaultTableModel dlm = new DefaultTableModel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 4095663470668363266L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		dlm.addColumn(Messages.getString("JInstrumentEditorPanel.214")); //$NON-NLS-1$
		dlm.addColumn(Messages.getString("JInstrumentEditorPanel.215")); //$NON-NLS-1$

		if (model == null) {
			drumsoundlist.setModel(dlm);
			return;
		}

		Scale scale = model.getScale();
		PercussionDef[] ps = scale.findUniquePercussionDefs();
		for (int i = 0; i < ps.length; i++) {
			PercussionDef percussionDef = ps[i];
			ReferencedPercussion r = ReferencedPercussionList
					.findReferencedPercussionByMidiCode(percussionDef
							.getPercussion());

			String drumName = ReferencedPercussion.getLocalizedDrumLabel(r);

			SoundSample ss = model.getPercussionSoundSample(percussionDef);

			dlm.addRow(new Object[] {
					new DrumObjectReferenceDisplay(drumName, percussionDef),
					""		+ (ss == null ? Messages.getString("JInstrumentEditorPanel.217") : " -> " + ss.getName()) }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		}

		drumsoundlist.setModel(dlm);
		dlm.fireTableStructureChanged();

	}

	private JTextField instrumentName = null;
	private JTextArea instrumentDescription = null;
	private ImageComponent instrumentImage = null;
	private JButton buttonchoicepicture = null;

	private void updatePipeStopGroupCombo() {
		DefaultComboBoxModel dcbm = new DefaultComboBoxModel();
		if (model != null) {
			String[] pipeStopGroups = model.getPipeStopGroupsAndRegisterName();
			for (int i = 0; i < pipeStopGroups.length; i++) {
				dcbm.addElement(pipeStopGroups[i]);
			}
		}
		pipeStopGroupCombo.setModel(dcbm);
	}

	private void updateSoundListForCurrentPipeStopGroup(
			String currentPipeStopGroup) {

		if (model == null)
			return;

		List<SoundSample> soundSampleList = model
				.getSoundSampleList(currentPipeStopGroup);

		DefaultTableModel dtm = new DefaultTableModel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 4095663470668363266L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		dtm.addColumn(Messages.getString("JInstrumentEditor.95")); //$NON-NLS-1$

		for (Iterator<SoundSample> iterator = soundSampleList.iterator(); iterator
				.hasNext();) {
			SoundSample soundSample = (SoundSample) iterator.next();
			dtm.addRow(new Object[] { soundSample });
		}

		listSounds.setModel(dtm);

		DefaultTableColumnModel dtcm = new DefaultTableColumnModel();
		TableColumn tc = new TableColumn();
		tc.setModelIndex(0);
		tc.setHeaderValue(Messages.getString("JInstrumentEditor.96")); //$NON-NLS-1$
		tc.setCellRenderer(new SoundListRenderer());
		tc.setCellEditor(new SoundListEditor());

		dtcm.addColumn(tc);

		listSounds.setColumnModel(dtcm);

	}

	private void updateScale() {
		if (model == null)
			return;
		Scale currentScale = model.getScale();
		if (currentScale != null) {
			scalePreview.loadScale(currentScale);
			scaleEditorPanel.loadScale(currentScale);
		} else {
			scalePreview.newScale();
			scaleEditorPanel.newScale();

		}

		this.panelScripting.setCurrentScale(currentScale);

		logger.debug("update scale preview component ..."); //$NON-NLS-1$
		scalePreview.invalidate();
		scalePreview.repaint();
	}

	private void updateScripts() {
		if (model == null) {
			this.panelScripting.setInstrumentScripts(new InstrumentScript[0]);
		} else {
			this.panelScripting.setInstrumentScripts(model.getScripts());
		}
	}

	private void updateCurrentSoundBank() {

		logger.debug("update current sound bank"); //$NON-NLS-1$
		try {
			SBCreator sb = new SBCreator();
			String currentPipeStopGroup = getCurrentPipeStopGroup();

			List<SoundSample> soundSampleList = model
					.getSoundSampleList(currentPipeStopGroup);
			ArrayList<SampleMapping> a = new ArrayList<SampleMapping>();
			for (Iterator<SoundSample> iterator = soundSampleList.iterator(); iterator
					.hasNext();) {
				SoundSample sampleMapping = iterator.next();

				SampleMapping sampleMapping2 = model.getSampleMapping(
						currentPipeStopGroup, sampleMapping);
				a.add(sampleMapping2);
			}

			SF2Soundbank soundBank = sb.createSimpleSoundBank(a
					.toArray(new SampleMapping[0]));

			logger.debug("sending the new sound bank ..."); //$NON-NLS-1$
			player.changeCurrentSoundBank(soundBank);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	private void updatePianoRoll() {

		logger.debug("update PianoRoll ..."); //$NON-NLS-1$

		assert model != null;

		List<SoundSample> soundSampleList = model
				.getSoundSampleList(getCurrentPipeStopGroup());

		pianoroll.clearSelectedRangeItem();
		pianoroll.removeAllSelectedRange();

		// get the mappings and create selectedRanges ....

		int cpt = 0;
		for (Iterator<SoundSample> iterator = soundSampleList.iterator(); iterator
				.hasNext();) {
			SoundSample soundSample = (SoundSample) iterator.next();
			logger.debug("treat soundSample " + soundSample); //$NON-NLS-1$

			SampleMapping sampleMapping = model.getSampleMapping(
					getCurrentPipeStopGroup(), soundSample);

			if (logger.isDebugEnabled())
				logger.debug("associated sound mapping " + sampleMapping); //$NON-NLS-1$

			if (sampleMapping != null) {
				logger.debug("has a mapping ..."); //$NON-NLS-1$

				SelectedRange r = new SelectedRange(
						sampleMapping.getFirstMidiCode(),
						sampleMapping.getLastMidiCode());

				pianoroll.addRange(r);
				logger.debug("range added :" + r); //$NON-NLS-1$

				if (getCurrentSelectedSoundSample() == soundSample)
					pianoroll.setSelectedRangeItem(cpt);

				cpt++;
			}
		}

		// disable all notes, and activate notes from the scale ...

		pianoroll.unActivateAllNotes();

		// Note PF : some bug report show that the scale can be null ???
		// TODO check why
		Scale s = model.getScale();
		if (s != null) {
			AbstractTrackDef[] tracksDefinition = s.getTracksDefinition();
			for (int i = 0; i < tracksDefinition.length; i++) {
				AbstractTrackDef abstractTrackDef = tracksDefinition[i];
				if (abstractTrackDef != null) {
					if (abstractTrackDef instanceof NoteDef) {
						NoteDef note = (NoteDef) abstractTrackDef;
						pianoroll.activateNote(note.getMidiNote());
					}
				}
			}
		}
	}

	private void updateContextualButtons() {
		SoundSample currentSelectedSample = getCurrentSelectedSoundSample();
		if (currentSelectedSample == null) {
			removeSound.setEnabled(false);
			modifySound.setEnabled(false);
			removeSoundMapping.setEnabled(false);
			savesoundsample.setEnabled(false);
		} else {
			removeSound.setEnabled(true);
			modifySound.setEnabled(true);
			removeSoundMapping.setEnabled(true);
			savesoundsample.setEnabled(true);
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// internal controller for the form ...

	private String getCurrentPipeStopGroup() {
		return (String) pipeStopGroupCombo.getSelectedItem();
	}

	private SoundSample getCurrentSelectedSoundSample() {

		int sel = listSounds.getSelectedRow();
		if (sel == -1)
			return null;

		return (SoundSample) listSounds.getModel().getValueAt(sel, 0);

	}

	/**
	 * When the currently selected pipe stop group changed
	 */
	protected void pipeStopGroupChanged(String pipeStopGroup) {
		updateSoundListForCurrentPipeStopGroup(pipeStopGroup);
		updatePianoRoll();
		updateCurrentSoundBank();
	}

	/**
	 * When the current selected Sound Sample Changed
	 * 
	 * @param currentSelected
	 */
	protected void currentSelectedSoundSampleChanged(SoundSample currentSelected) {

		updatePianoRoll();
		updateContextualButtons();
		updateCurrentSoundBank();

	}

	/**
	 * When the soundsample list changed ..
	 * 
	 * @param pipeStopGroup
	 */
	protected void soundSampleListChanged(String pipeStopGroup) {

		updateSoundListForCurrentPipeStopGroup(pipeStopGroup);
		updatePianoRoll();
		updateCurrentSoundBank();

	}

	private File lastOpenedFile = null;
	private File lastOpenedSoundSampleFile = null;

	private JScaleEditorPanel scaleEditorPanel;

	private JInstrumentScriptingPanel panelScripting;

	private JTable drumsoundlist;

	private JButton loadsoundsample;

	private JButton savesoundsample;

	protected void saveCurrentlySelectedSoundSample() {
		try {
			SoundSample ss = getCurrentSelectedSoundSample();
			if (ss == null) {
				logger.debug("no sound sample selected"); //$NON-NLS-1$
				return;
			}

			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileNameExtensionFilter(Messages.getString("JInstrumentEditorPanel.1000"), //$NON-NLS-1$
					new String[] { SoundSampleIO.SOUNDSAMPLEEXTENSION })); //$NON-NLS-1$

			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

			if (lastOpenedSoundSampleFile != null)
				fc.setSelectedFile(lastOpenedSoundSampleFile);

			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				File result = fc.getSelectedFile();
				try {

					if (!result.getName().endsWith(
							"." + SoundSampleIO.SOUNDSAMPLEEXTENSION)) { //$NON-NLS-1$
						result = new File(result.getParentFile(),
								result.getName() + "." //$NON-NLS-1$
										+ SoundSampleIO.SOUNDSAMPLEEXTENSION);
					}

					lastOpenedSoundSampleFile = result;

					FileOutputStream fos = new FileOutputStream(result);
					try {
						ssio.saveSample(ss, null, fos);

					} finally {
						fos.close();
					}
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
				JMessageBox.showMessage(this.parentFrame,
						Messages.getString("JInstrumentEditorPanel.1008") + result.getName() + " " + Messages.getString("JInstrumentEditorPanel.1010")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			JMessageBox.showMessage(this.parentFrame,
					Messages.getString("JInstrumentEditorPanel.1011") + ex.getMessage()); //$NON-NLS-1$
			BugReporter.sendBugReport();
		}
	}

	protected void addLoadedSoundSampleInCurrentPipeStopGroup() {
		try {

			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileNameExtensionFilter(Messages.getString("JInstrumentEditorPanel.1012"), //$NON-NLS-1$
					new String[] { SoundSampleIO.SOUNDSAMPLEEXTENSION })); //$NON-NLS-1$

			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

			if (lastOpenedSoundSampleFile != null)
				fc.setSelectedFile(lastOpenedSoundSampleFile);

			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

				final File result = fc.getSelectedFile();
				lastOpenedSoundSampleFile = result;

				SoundSample s = ssio.readSample(new FileInputStream(result));

				if (s != null) {
					model.addSoundSample(s, getCurrentPipeStopGroup());

					int smidiroot = s.getMidiRootNote();
					if (smidiroot >= 0) {
						model.setSampleMapping(getCurrentPipeStopGroup(), s,
								smidiroot, smidiroot);
					}
				}
			}

			soundSampleListChanged(getCurrentPipeStopGroup());

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			JMessageBox.showMessage(this.parentFrame,
					Messages.getString("JInstrumentEditorPanel.1013") + ex.getMessage()); //$NON-NLS-1$
			BugReporter.sendBugReport();
		}
	}

	protected void addSoundSampleInCurrentPipeStopGroup() {
		try {

			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileNameExtensionFilter(Messages
					.getString("JInstrumentEditor.46"), //$NON-NLS-1$
					new String[] { "wav" })); //$NON-NLS-1$

			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

			if (lastOpenedFile != null)
				fc.setSelectedFile(lastOpenedFile);

			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

				final File result = fc.getSelectedFile();
				lastOpenedFile = result;

				SoundSample s = GUIInstrumentTools.loadWavFile(result);

				SoundSample newConstructedSoundSample = showModalEditingSoundSampleEditor(s);

				if (newConstructedSoundSample != null)
					model.addSoundSample(newConstructedSoundSample,
							getCurrentPipeStopGroup());
			}

			soundSampleListChanged(getCurrentPipeStopGroup());

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			JMessageBox.showMessage(this.parentFrame,
					Messages.getString("JInstrumentEditor.75") //$NON-NLS-1$
							+ ex.getMessage());
			BugReporter.sendBugReport();
		}
	}

	protected void modifyCurrentSelectedSoundSampleInCurrentPipeStopGroup() {
		try {

			SoundSample ss = getCurrentSelectedSoundSample();
			if (ss != null) {
				SoundSample result = showModalEditingSoundSampleEditor(ss);
				if (result != null) {

					// changed ...
					String currentPipeStopGroup = getCurrentPipeStopGroup();
					model.removeSoundSample(ss, currentPipeStopGroup);
					model.addSoundSample(result, currentPipeStopGroup);
					updatePianoRoll();

				}
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	private SoundSample showModalEditingSoundSampleEditor(SoundSample s)
			throws Exception {

		SampleMapping mapping = model.getSampleMapping(
				getCurrentPipeStopGroup(), s);

		final JDialog d = new JDialog(this.parentFrame,
				Messages.getString("JInstrumentEditor.48")); //$NON-NLS-1$
		final JSoundSampleEditorPanel soundSampleEditorPanel = new JSoundSampleEditorPanel();
		d.getContentPane().add(soundSampleEditorPanel, BorderLayout.CENTER);
		JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout());
		JButton ok = new JButton(Messages.getString("JInstrumentEditor.49")); //$NON-NLS-1$
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				d.setVisible(false);
			}
		});
		buttons.add(ok);

		JButton cancel = new JButton(Messages.getString("JInstrumentEditor.50")); //$NON-NLS-1$
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					soundSampleEditorPanel.setCurrentlyEditedSoundSample(null);
					d.setVisible(false);

				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		});

		buttons.add(cancel);

		d.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {

					// cancel ...
					soundSampleEditorPanel.setCurrentlyEditedSoundSample(null);

					d.setVisible(false);

				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		});

		d.getContentPane().add(buttons, BorderLayout.SOUTH);

		d.setSize(800, 500);
		soundSampleEditorPanel.setCurrentlyEditedSoundSample(s);

		d.setModal(true);
		SwingUtils.center(d);
		d.setVisible(true); // blocking ...

		// free the resources ...
		soundSampleEditorPanel.dispose();

		SoundSample editedSoundSample = soundSampleEditorPanel
				.constructSoundSample();

		if (editedSoundSample != null) {
			logger.debug("user has not cancelled the element ...."); //$NON-NLS-1$
			if (mapping == null) {
				// create a new mapping ..
				logger.debug("creating the new mapping"); //$NON-NLS-1$
				model.setSampleMapping(getCurrentPipeStopGroup(),
						editedSoundSample, editedSoundSample.getMidiRootNote(),
						editedSoundSample.getMidiRootNote());
			} else {
				// update the mapping ...
				logger.debug("update the mapping ..."); //$NON-NLS-1$
				model.setSampleMapping(getCurrentPipeStopGroup(),
						editedSoundSample, mapping.getFirstMidiCode(),
						mapping.getLastMidiCode());
			}
		}
		return editedSoundSample;
	}

	protected void removeSoundSampleInCurrentPipeStopGroup() {

		SoundSample toRemove = getCurrentSelectedSoundSample();
		if (toRemove == null)
			return;

		model.removeSoundSample(toRemove, getCurrentPipeStopGroup());
		model.removeSampleMapping(getCurrentPipeStopGroup(), toRemove);

	}

	protected void removeCurrentSelectedSampleSoundMapping() {
		SoundSample soundSampleOnWhichRemoveTheMapping = getCurrentSelectedSoundSample();
		if (soundSampleOnWhichRemoveTheMapping == null)
			return;

		model.removeSampleMapping(getCurrentPipeStopGroup(),
				soundSampleOnWhichRemoveTheMapping);
		updatePianoRoll();

	}

	private void sampleMappingChanged() {
		updatePianoRoll();
		updateCurrentSoundBank();
	}

	protected void scaleChanged() {

		Scale s = (model == null ? null : model.getScale());

		logger.debug("scaleChanged " //$NON-NLS-1$
				+ (model == null ? "null" : model.getScale())); //$NON-NLS-1$
		updateScale();
		updatePipeStopGroupCombo();
		updateSoundListForCurrentPipeStopGroup(getCurrentPipeStopGroup());
		updatePianoRoll();

		panelScripting.setCurrentScale(s);

	}

	/**
	 * Function for adding part of a wav as a Sound Mapping
	 * 
	 * @throws Exception
	 */
	protected void addSampleMappingFromCroppedWav() {
		try {
			logger.debug("addSampleMappingFromCroppedWav"); //$NON-NLS-1$

			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileNameExtensionFilter(Messages
					.getString("JInstrumentEditor.46"), //$NON-NLS-1$
					new String[] { "wav" })); //$NON-NLS-1$

			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

			if (lastOpenedFile != null)
				fc.setSelectedFile(lastOpenedFile);

			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

				final File result = fc.getSelectedFile();
				lastOpenedFile = result;

				SoundSample s = GUIInstrumentTools.loadWavFile(result);

				logger.debug("loading the wav in the tool window"); //$NON-NLS-1$

				final JDialog d = new JDialog(this.parentFrame, true);
				d.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				JCreateSoundSampleFromCropping c = new JCreateSoundSampleFromCropping();
				d.getContentPane().add(c, BorderLayout.CENTER);
				d.setSize(800, 500);
				d.setLocationByPlatform(true);

				c.setDisplayedSample(s);

				c.addListener(new SoundSampleListener() {
					public void soundSampleReceived(SoundSample s) {
						try {
							model.addSoundSample(s, getCurrentPipeStopGroup());
							model.setSampleMapping(getCurrentPipeStopGroup(),
									s, s.getMidiRootNote(), s.getMidiRootNote());
							JMessageBox.showMessage(d, Messages.getString("JInstrumentEditorPanel.1014") + s.getName() //$NON-NLS-1$
									+ Messages.getString("JInstrumentEditorPanel.1015")); //$NON-NLS-1$
							logger.debug("sound sample " + s + "  added"); //$NON-NLS-1$ //$NON-NLS-2$
						} catch (Throwable t) {
							logger.error(t.getMessage(), t);
						}
					}
				});

				d.setVisible(true);

				logger.debug("finish showing the form ..."); //$NON-NLS-1$

			}
		} catch (Exception ex) {
			logger.error("addSampleMappingFromCroppedWav", ex); //$NON-NLS-1$
		}

	}

	public void resetCurrentPipeStopGroup() {
		pipeStopGroupChanged(EditableInstrument.DEFAULT_PIPESTOPGROUPNAME);
	}

	private void updateModelWithScaleInformations() {
		try {

			logger.debug("try to change scale"); //$NON-NLS-1$

			Scale newScale = scaleEditorPanel.getScale();
			logger.debug("newscale :" + newScale); //$NON-NLS-1$

			Scale currentScale = model.getScale();
			logger.debug("current Scale :" + currentScale); //$NON-NLS-1$

			if (currentScale == null || !currentScale.equals(newScale)) {
				model.setScale(newScale);
				logger.debug("new scale defined ..."); //$NON-NLS-1$
				scaleChanged();
			}

		} catch (Exception ex) {
			logger.error("error in changing scale :" //$NON-NLS-1$
					+ ex.getMessage(), ex);
		}
	}

	protected void playDrumSound() {

		try {

			int selRow = drumsoundlist.getSelectedRow();
			if (selRow == -1)
				throw new Exception("you must select a sample"); //$NON-NLS-1$

			DrumObjectReferenceDisplay d = (DrumObjectReferenceDisplay) drumsoundlist
					.getModel().getValueAt(selRow, 0);

			if (d == null)
				return;

			PercussionDef pd = d.getPercussionDef();
			if (pd == null)
				return;

			SoundSample ss = model.getPercussionSoundSample(pd);

			if (ss == null)
				return;

			WavPlayer wp = new WavPlayer();
			wp.playSound(ss.getManagedAudioInputStream(),
					new WavPlayerListener() {
						public void playStateChanged(long pos) {
							// TODO Auto-generated method stub

						}

						public void playStopped() {
							// TODO Auto-generated method stub

						}

						public void startPlaying() {
							// TODO Auto-generated method stub

						}
					});

		} catch (Throwable ex) {
			logger.error("error in playing sound for drum :" + ex.getMessage(), //$NON-NLS-1$
					ex);
			JMessageBox.showMessage(parentFrame,
					"Error in playing the drum sound :" + ex.getMessage()); //$NON-NLS-1$
		}

	}

	protected void removeDrumSound() {
		try {

			int selRow = drumsoundlist.getSelectedRow();
			if (selRow == -1)
				throw new Exception("you must select a sample"); //$NON-NLS-1$

			DrumObjectReferenceDisplay d = (DrumObjectReferenceDisplay) drumsoundlist
					.getModel().getValueAt(selRow, 0);

			PercussionDef pd = d.getPercussionDef();

			model.setPercussionSoundSample(pd, null);

			updateDrumSoundList();

		} catch (Throwable ex) {
			logger.error("error in remove sound for drum :" + ex.getMessage(), //$NON-NLS-1$
					ex);
			JMessageBox
					.showMessage(
							parentFrame,
							Messages.getString("JInstrumentEditorPanel.221") + ex.getMessage()); //$NON-NLS-1$
		}
	}

	/**
	 * 
	 */
	protected void addDrumSound() {
		try {

			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileNameExtensionFilter(Messages
					.getString("JInstrumentEditor.46"), //$NON-NLS-1$
					new String[] { "wav" })); //$NON-NLS-1$

			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

			if (lastOpenedFile != null)
				fc.setSelectedFile(lastOpenedFile);

			if (fc.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {

				final File result = fc.getSelectedFile();
				lastOpenedFile = result;

				SoundSample s = GUIInstrumentTools.loadWavFile(result);

				SoundSample newConstructedSoundSample = showModalEditingSoundSampleEditor(s);

				if (newConstructedSoundSample != null) {
					int selRow = drumsoundlist.getSelectedRow();
					if (selRow == -1)
						throw new Exception("you must select a sample"); //$NON-NLS-1$

					DrumObjectReferenceDisplay d = (DrumObjectReferenceDisplay) drumsoundlist
							.getModel().getValueAt(selRow, 0);

					PercussionDef pd = d.getPercussionDef();

					newConstructedSoundSample.setMidiRootNote(pd
							.getPercussion());
					model.setPercussionSoundSample(pd,
							newConstructedSoundSample);

					updateDrumSoundList();
				}
			}

		} catch (Throwable ex) {
			logger.error("error in adding sound for drum :" + ex.getMessage(), //$NON-NLS-1$
					ex);
			JMessageBox
					.showMessage(
							parentFrame,
							Messages.getString("JInstrumentEditorPanel.224") + ex.getMessage()); //$NON-NLS-1$
		}
	}

	protected void editDrumSoundSample() {
		try {

			int selRow = drumsoundlist.getSelectedRow();
			if (selRow == -1)
				throw new Exception("you must select a sample"); //$NON-NLS-1$

			DrumObjectReferenceDisplay d = (DrumObjectReferenceDisplay) drumsoundlist
					.getModel().getValueAt(selRow, 0);

			PercussionDef pd = d.getPercussionDef();

			SoundSample ss = model.getPercussionSoundSample(pd);

			if (ss != null) {
				SoundSample result = showModalEditingSoundSampleEditor(ss);
				if (result != null) {

					model.setPercussionSoundSample(pd, result);

					updateDrumSoundList();
				}
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

	}

}
