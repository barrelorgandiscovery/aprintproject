package org.barrelorgandiscovery.gui.ainstrument;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
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
import javax.swing.border.Border;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.EditableInstrument;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.editableinstrument.InstrumentScript;
import org.barrelorgandiscovery.editableinstrument.ScaleListener;
import org.barrelorgandiscovery.editableinstrument.SoundSampleListListener;
import org.barrelorgandiscovery.gui.ainstrument.pianoroll.JPianoRollComponent;
import org.barrelorgandiscovery.gui.ainstrument.pianoroll.PianoRenderingNote;
import org.barrelorgandiscovery.gui.ainstrument.pianoroll.PianoRollRangeEditListener;
import org.barrelorgandiscovery.gui.ainstrument.pianoroll.PianoRenderingNote;
import org.barrelorgandiscovery.gui.ascale.JScaleEditorPanel;
import org.barrelorgandiscovery.gui.ascale.ScaleComponent;
import org.barrelorgandiscovery.gui.ascale.ScaleEditorPrefs;
import org.barrelorgandiscovery.gui.ascale.ScaleHighlightListener;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
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
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.SwingUtils;
import org.barrelorgandiscovery.tools.VFSTools;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

import com.jeta.forms.components.image.ImageComponent;
import com.jeta.forms.components.panel.FormPanel;

import gervill.SF2Soundbank;

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

	private static Logger logger = Logger.getLogger(JInstrumentEditorPanel.class);

	private static int MAX_IMAGE_WIDTH = 300;
	private static int MAX_IMAGE_HEIGHT = 300;

	/** General tab: scroll viewport size for the instrument picture preview */
	private static final int GENERAL_TAB_IMAGE_VIEWPORT_W = 320;
	private static final int GENERAL_TAB_IMAGE_VIEWPORT_H = 280;

	/** Lazy placeholder when no instrument image is set (gray box + label) */
	private static ImageIcon noInstrumentPicturePlaceholder;

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
				public void soundSampleAdded(SoundSample sampleAdded, String pipeStopGroup) {

					soundSampleListChanged(getCurrentPipeStopGroup());
				}

				public void soundSampleRemoved(SoundSample sampleRemoved, String pipeStopGroup) {
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
			updateInstrumentPictureDisplay(newModel.getInstrumentPicture());

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
		private ImageIcon icon = InstrumentToolbarIcons.patchToolbarIcon(
				JInstrumentEditorPanel.class, "arts.png"); //$NON-NLS-1$

		public SoundListRenderer() {
			super();
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			SoundSample ss = (SoundSample) value;

			super.getTableCellRendererComponent(table, ss.getName(), isSelected, hasFocus, row, column);

			setIcon(icon);

			return this;

		}

	}

	private class SoundListEditor extends AbstractCellEditor implements TableCellEditor {

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

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {

			final SoundSample ss = (SoundSample) value;
			this.ss = ss;

			tf.setText(ss.getName());
			return tf;
		}

	}

	private StatedPianoRollMouseHandler pianorollMouseHandler = new PianoRollMouseHandler();

	private abstract class StatedPianoRollMouseHandler implements MouseListener, MouseMotionListener {

	}

	private class PianoRollMouseHandler extends StatedPianoRollMouseHandler {

		private boolean pianoRangeGesture = false;

		public void mouseMoved(MouseEvent e) {

			if (pianoroll.updateRangeHoverCursor(e)) {
				return;
			}

			PianoRenderingNote currentSelectedNote2 = pianoroll.getCurrentSelectedNote();

			if (currentSelectedNote2 != null) {
				if (currentSelectedNote2.getPolygon().contains(e.getX(), e.getY()))
					// nothing to do ...
					return;
			}

			PianoRenderingNote n = pianoroll.searchForKey(e.getX(), e.getY());

			pianoroll.setCurrentSelectedNote(n);

		}

		private int state = 0;
		private int firstPos = -1;

		public void mouseDragged(MouseEvent e) {

			if (pianoRangeGesture) {
				pianoroll.continueRangeGesture(e);
				return;
			}

			mouseMoved(e);

			if (state == 1)
				return;

			PianoRenderingNote searchForKey = pianoroll.searchForKey(e.getX(), e.getY());

			if (getCurrentSelectedSoundSample() == null) {
				// play the note ...
				if (searchForKey != null && searchForKey.getMidicode() != player.getCurrentPlayedNote())
					player.playNote(searchForKey.getMidicode());
				return;
			}

			pianoroll.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));

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
			pianoroll.setCursor(Cursor.getDefaultCursor());
		}

		public void mousePressed(MouseEvent e) {
			try {
				logger.debug("pressed"); //$NON-NLS-1$

				if (pianoroll.beginRangeGesture(e)) {
					pianoRangeGesture = true;
					return;
				}

				if (getCurrentSelectedSoundSample() == null) {
					PianoRenderingNote searchForKey = pianoroll.searchForKey(e.getX(), e.getY());
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
			if (pianoRangeGesture) {
				pianoroll.finishRangeGesture(e);
				pianoRangeGesture = false;
				pianoroll.setCursor(Cursor.getDefaultCursor());
				try {
					player.stopNote();
				} catch (Exception ex) {
					logger.error(ex);
				}
				return;
			}
			if (state == 1) {
				logger.debug("end of "); //$NON-NLS-1$
				pianoroll.setCursor(Cursor.getDefaultCursor());

				PianoRenderingNote searchForKey = pianoroll.searchForKey(e.getX(), e.getY());
				if (searchForKey == null) {
					state = 0;
					return;
				}

				// change the mapping ...

				model.setSampleMapping(getCurrentPipeStopGroup(), getCurrentSelectedSoundSample(), firstPos,
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

	private static final Preferences MAPPING_SPLIT_PREFS = Preferences.userNodeForPackage(JInstrumentEditorPanel.class);
	private static final String PREF_MAPPING_SPLIT_H = "mapping.split.horizontal.location"; //$NON-NLS-1$
	/** Prevents the mapping-tab scale preview from consuming the whole row on wide windows. */
	private static final int MAX_MAPPING_SCALE_PREVIEW_WIDTH = 520;

	private JScrollPane mappingPianoScroll;
	private JScrollPane mappingScaleScroll;
	private JScrollPane mappingSoundsScroll;
	private JSplitPane mappingListScaleSplit;
	private JPanel mappingTabRoot;
	private JLabel registrationLabel;
	private JButton addFromCropFileButton;
	private Component tabbedPreviousSelection;

	private void initComponents() throws Exception {

		setLayout(new BorderLayout());

		tabbedPane = new JTabbedPane();

		logger.debug("loading general properties panel"); //$NON-NLS-1$

		FormPanel panelGeneral = null;
		// load the form ...
		try {

			InputStream is = getClass().getResourceAsStream("instrumenteditorpanelgeneralinformations.jfrm"); //$NON-NLS-1$
			if (is == null)
				throw new Exception("form not found"); //$NON-NLS-1$
			panelGeneral = new FormPanel(is);

		} catch (Exception ex) {
			logger.error("panel construction", ex); //$NON-NLS-1$
			throw new Exception(ex.getMessage(), ex);
		}

		// Plain JPanel as the tab content — never removeAll/setLayout on the loaded
		// FormPanel (same JGoodies NPE as scale editor: "component has not been added
		// to the container").
		final JPanel instrumentGeneralTabRoot = new JPanel(new BorderLayout(12, 12));
		tabbedPane.add(instrumentGeneralTabRoot,
				Messages.getString("JInstrumentEditorPanel.1")); //$NON-NLS-1$

		logger.debug("adding the scale editor tab"); //$NON-NLS-1$
		ScaleEditorPrefs p = new ScaleEditorPrefs(new DummyPrefsStorage());

		scaleEditorPanel = new JScaleEditorPanel(parentFrame, p);
		tabbedPane.add(scaleEditorPanel, Messages.getString("JInstrumentEditorPanel.3")); //$NON-NLS-1$

		logger.debug("loading sound mapping panel"); //$NON-NLS-1$
		FormPanel panelMapping = null;
		try {

			InputStream is = getClass().getResourceAsStream("instrumenteditorpanelsoundmapping.jfrm"); //$NON-NLS-1$
			if (is == null)
				throw new Exception("form not found"); //$NON-NLS-1$
			panelMapping = new FormPanel(is);

		} catch (Exception ex) {
			logger.error("panel construction", ex); //$NON-NLS-1$
			throw new Exception(ex.getMessage(), ex);
		}

		FormPanel fpDrumSounds = null;
		try {

			InputStream is = getClass().getResourceAsStream("instrumenteditorpaneldrumssoundassociation.jfrm"); //$NON-NLS-1$
			if (is == null)
				throw new Exception("form not found"); //$NON-NLS-1$
			fpDrumSounds = new FormPanel(is);

		} catch (Exception ex) {
			logger.error("panel construction", ex); //$NON-NLS-1$
			throw new Exception(ex.getMessage(), ex);
		}

		drumsoundlist = (JTable) fpDrumSounds.getComponentByName("drumsoundlist"); //$NON-NLS-1$
		JButton addDrumSound = (JButton) fpDrumSounds.getButton("setwavassociation"); //$NON-NLS-1$

		addDrumSound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addDrumSound();
			}
		});

		JButton editDrumSound = (JButton) fpDrumSounds.getButton("update"); //$NON-NLS-1$
		editDrumSound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editDrumSoundSample();
			}
		});

		JButton removeDrumSound = (JButton) fpDrumSounds.getButton("resetwavassociation"); //$NON-NLS-1$
		removeDrumSound.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				removeDrumSound();
			}
		});

		JButton playDrumSound = (JButton) fpDrumSounds.getButton("playdrum"); //$NON-NLS-1$
		playDrumSound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playDrumSound();
			}
		});

		applyPatchToolbarButton(addDrumSound, "artsplus.png", //$NON-NLS-1$
				Messages.getString("JInstrumentEditorPanel.204")); //$NON-NLS-1$
		applyPatchToolbarButton(editDrumSound, "arts.png", //$NON-NLS-1$
				Messages.getString("JInstrumentEditorPanel.208")); //$NON-NLS-1$
		applyPatchToolbarButton(removeDrumSound, "artsmoins.png", //$NON-NLS-1$
				Messages.getString("JInstrumentEditorPanel.212")); //$NON-NLS-1$
		applyPatchToolbarButton(playDrumSound, "arts.png", //$NON-NLS-1$
				Messages.getString("JInstrumentEditorPanel.1001")); //$NON-NLS-1$

		detachFromParent(addDrumSound);
		detachFromParent(editDrumSound);
		detachFromParent(removeDrumSound);
		detachFromParent(playDrumSound);

		JToolBar drumToolbar = new JToolBar();
		drumToolbar.setFloatable(false);
		drumToolbar.setRollover(true);
		drumToolbar.add(addDrumSound);
		drumToolbar.add(editDrumSound);
		drumToolbar.add(removeDrumSound);
		drumToolbar.addSeparator();
		drumToolbar.add(playDrumSound);

		JPanel drumSoundsTab = new JPanel(new BorderLayout(0, 2));
		drumSoundsTab.add(drumToolbar, BorderLayout.NORTH);
		drumSoundsTab.add(fpDrumSounds, BorderLayout.CENTER);

		updateDrumSoundList();

		tabbedPane.add(drumSoundsTab, Messages.getString("JInstrumentEditorPanel.213")); //$NON-NLS-1$

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

					InstrumentScript modelInstrumentScript = model.findScript(instrumentScript.getName());
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

		tabbedPane.add(panelScripting, Messages.getString("JInstrumentEditorPanel.8")); //$NON-NLS-1$

		logger.debug("adding tab panels ... "); //$NON-NLS-1$
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				logger.debug("tab changed :" + e.getSource()); //$NON-NLS-1$
				JTabbedPane p = (JTabbedPane) e.getSource();
				Component selectedComponent = p.getSelectedComponent();
				logger.debug("selected component : " + selectedComponent); //$NON-NLS-1$

				if (tabbedPreviousSelection == mappingTabRoot) {
					saveMappingSplitLocations();
				}
				tabbedPreviousSelection = selectedComponent;

				if (selectedComponent != scaleEditorPanel) {
					updateModelWithScaleInformations();
					updateDrumSoundList();
				}

				panelScripting.commitProperties();

			}
		});

		logger.debug("adding the pianoroll component ..."); //$NON-NLS-1$

		pianoroll = new JPianoRollComponent();
		pianoroll.setRangeEditListener(new PianoRollRangeEditListener() {
			public void rangeBoundsChangeCommitted(SelectedRange range, Object clientTag, int start, int end) {
				if (clientTag instanceof SoundSample) {
					model.setSampleMapping(getCurrentPipeStopGroup(), (SoundSample) clientTag, start, end);
					sampleMappingChanged();
				}
			}
		});
		mappingPianoScroll = new JScrollPane(pianoroll);

		panelMapping.getFormAccessor().replaceBean(panelMapping.getComponentByName("pianoroll"), mappingPianoScroll); //$NON-NLS-1$

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

		mappingScaleScroll = new JScrollPane(scalePreview);
		panelMapping.getFormAccessor().replaceBean(panelMapping.getLabel("scalepreviewer"), //$NON-NLS-1$
				mappingScaleScroll);

		mappingSoundsScroll = new JScrollPane(listSounds);

		mappingSoundsScroll.setColumnHeader(null);
		mappingSoundsScroll.setMinimumSize(new Dimension(120, 200));

		listSounds.setShowGrid(true);

		listSounds.setRowHeight(20);

		listSounds.setRowSelectionAllowed(true);

		listSounds.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		listSounds.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				logger.debug("selected sound sample changed ..."); //$NON-NLS-1$

				if (e.getValueIsAdjusting()) {
					return;
				}
				int row = listSounds.getSelectedRow();
				if (row < 0 || row >= listSounds.getModel().getRowCount()) {
					return;
				}
				currentSelectedSoundSampleChanged((SoundSample) listSounds.getModel().getValueAt(row, 0));
			}
		});

		listSounds.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {

				if (e.getClickCount() == 2) {
					modifyCurrentSelectedSoundSampleInCurrentPipeStopGroup();

				} else if (e.getClickCount() == 1 && (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0) {
					int index = listSounds.getSelectionModel().getMinSelectionIndex();
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
				mappingSoundsScroll);

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
		applyPatchToolbarButton(addSound, "artsplus.png", //$NON-NLS-1$
				Messages.getString("JInstrumentEditor.14")); //$NON-NLS-1$

		modifySound = (JButton) panelMapping.getButton("modifysound"); //$NON-NLS-1$
		modifySound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modifyCurrentSelectedSoundSampleInCurrentPipeStopGroup();
			}
		});
		applyPatchToolbarButton(modifySound, "artsbuilder.png", //$NON-NLS-1$
				Messages.getString("JInstrumentEditor.16")); //$NON-NLS-1$

		removeSoundMapping = (JButton) panelMapping.getButton("removeSoundMapping"); //$NON-NLS-1$
		removeSoundMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeCurrentSelectedSampleSoundMapping();
			}
		});
		applyPatchToolbarButton(removeSoundMapping, "arts-remove-mapping.png", //$NON-NLS-1$
				Messages.getString("JInstrumentEditorPanel.16")); //$NON-NLS-1$

		removeSound = (JButton) panelMapping.getButton("removesound"); //$NON-NLS-1$
		removeSound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeSoundSampleInCurrentPipeStopGroup();
			}
		});
		applyPatchToolbarButton(removeSound, "artsmoins.png", //$NON-NLS-1$
				Messages.getString("JInstrumentEditor.18")); //$NON-NLS-1$

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

		addFromCropFileButton = (JButton) panelMapping.getButton("addfromcropfile"); //$NON-NLS-1$
		addFromCropFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addSampleMappingFromCroppedWav();
			}
		});
		applyPatchToolbarButton(addFromCropFileButton, "artsplus.png", //$NON-NLS-1$
				Messages.getString("JInstrumentEditor.79")); //$NON-NLS-1$

		loadsoundsample = (JButton) panelMapping.getButton("loadsoundsample");//$NON-NLS-1$
		loadsoundsample.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addLoadedSoundSampleInCurrentPipeStopGroup();
			}
		});
		applyPatchToolbarButton(loadsoundsample, "soundsampleload.png", //$NON-NLS-1$
				Messages.getString("JInstrumentEditorPanel.1002")); //$NON-NLS-1$

		savesoundsample = (JButton) panelMapping.getButton("savesoundsample");//$NON-NLS-1$
		savesoundsample.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveCurrentlySelectedSoundSample();
			}
		});
		applyPatchToolbarButton(savesoundsample, "soundsamplesave.png", //$NON-NLS-1$
				Messages.getString("JInstrumentEditorPanel.1003")); //$NON-NLS-1$

		pipeStopGroupCombo = panelMapping.getComboBox("pipestopgroupcombo"); //$NON-NLS-1$
		updatePipeStopGroupCombo();
		pipeStopGroupCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				pipeStopGroupChanged((String) pipeStopGroupCombo.getSelectedItem());
			}
		});

		clearSoundSampleSelection = (JButton) panelMapping.getButton("clearsoundsampleselection"); //$NON-NLS-1$
		clearSoundSampleSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listSounds.clearSelection();
			}
		});

		applyPatchToolbarButton(clearSoundSampleSelection, "artsmidimanager.png", //$NON-NLS-1$
				"<html><body width=\"320px\">" //$NON-NLS-1$
						+ Messages.getString("JInstrumentEditor.23") + "<br>" //$NON-NLS-1$ //$NON-NLS-2$
						+ Messages.getString("JInstrumentEditor.24") //$NON-NLS-1$
						+ "</body></html>"); //$NON-NLS-1$

		instrumentName = (JTextField) panelGeneral.getTextField("instrumentname"); //$NON-NLS-1$
		instrumentName.getDocument().addDocumentListener(new DocumentListener() {
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

		instrumentDescription = (JTextArea) panelGeneral.getComponentByName("instrumentdescription"); //$NON-NLS-1$
		instrumentDescription.getDocument().addDocumentListener(new DocumentListener() {
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

		instrumentImage = (ImageComponent) panelGeneral.getComponentByName("instrumentimage"); //$NON-NLS-1$

		JLabel labelinstrumentpicture = (JLabel) panelGeneral.getComponentByName("labelinstrumentpicture"); //$NON-NLS-1$
		labelinstrumentpicture.setText(Messages.getString("JInstrumentEditor.57")); //$NON-NLS-1$

		JLabel labelinstrumentdescription = (JLabel) panelGeneral.getComponentByName("labelinstrumentdescription"); //$NON-NLS-1$
		labelinstrumentdescription.setText(Messages.getString("JInstrumentEditor.59")); //$NON-NLS-1$

		JLabel labelinstrumentname = (JLabel) panelGeneral.getComponentByName("labelinstrumentname"); //$NON-NLS-1$
		labelinstrumentname.setText(Messages.getString("JInstrumentEditor.61")); //$NON-NLS-1$

		registrationLabel = (JLabel) panelMapping.getComponentByName("registrationlabel"); //$NON-NLS-1$
		registrationLabel.setText(Messages.getString("JInstrumentEditor.62")); //$NON-NLS-1$

		buttonchoicepicture = (JButton) panelGeneral.getButton("buttonchoicepicture"); //$NON-NLS-1$

		buttonchoicepicture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					APrintFileChooser f = new APrintFileChooser();
					f.setMultiSelectionEnabled(false);
					f.setFileFilter(new VFSFileNameExtensionFilter(Messages.getString("JInstrumentEditor.56"), //$NON-NLS-1$
							new String[] { "gif", "jpg", //$NON-NLS-1$ //$NON-NLS-2$
									"png" })); //$NON-NLS-1$

					if (f.showOpenDialog(JInstrumentEditorPanel.this) == APrintFileChooser.APPROVE_OPTION) {
						logger.debug("loading image ..."); //$NON-NLS-1$

						AbstractFileObject fimage = f.getSelectedFile();
						if (fimage != null) {
							InputStream stream = fimage.getInputStream();
							assert stream != null;
							try {
								BufferedImage biimage = ImageTools.loadImageAndCrop(stream, MAX_IMAGE_WIDTH,
										MAX_IMAGE_HEIGHT);

								model.setInstrumentPicture(biimage);
								updateInstrumentPictureDisplay(biimage);
							} finally {
								stream.close();
							}
						}
					}
				} catch (Exception ex) {
					logger.error("error when loading image ..."); //$NON-NLS-1$
				}
			}
		});

		applyGeneralTabLayout(instrumentGeneralTabRoot, panelGeneral,
				labelinstrumentname, labelinstrumentpicture,
				labelinstrumentdescription);

		mappingTabRoot = buildInstrumentMappingTab();
		tabbedPane.insertTab(Messages.getString("JInstrumentEditorPanel.5"), null, mappingTabRoot, null, 2); //$NON-NLS-1$

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				restoreMappingSplitLocations();
			}
		});

		add(tabbedPane, BorderLayout.CENTER);

		// install model events ....

		updateContextualButtons();

	}

	private static void detachFromParent(Component c) {
		if (c == null) {
			return;
		}
		Container p = c.getParent();
		if (p != null) {
			p.remove(c);
			p.revalidate();
			p.repaint();
		}
	}

	/**
	 * Icon-only patch toolbar buttons: green-shifted compact icon, tooltip, no
	 * text.
	 */
	private static void applyPatchToolbarButton(JButton b, String resourceName,
			String tooltipText) {
		b.setIcon(InstrumentToolbarIcons.patchToolbarIcon(
				JInstrumentEditorPanel.class, resourceName));
		b.setText(null);
		b.setToolTipText(tooltipText);
		b.setMargin(new Insets(2, 2, 2, 2));
	}

	private static synchronized ImageIcon getNoInstrumentPicturePlaceholderIcon() {
		if (noInstrumentPicturePlaceholder == null) {
			int w = Math.max(180, GENERAL_TAB_IMAGE_VIEWPORT_W - 40);
			int h = Math.max(140, GENERAL_TAB_IMAGE_VIEWPORT_H - 40);
			BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = bi.createGraphics();
			try {
				g.setColor(new Color(245, 245, 245));
				g.fillRect(0, 0, w, h);
				g.setColor(new Color(208, 208, 208));
				g.drawRect(0, 0, w - 1, h - 1);
				g.setColor(new Color(120, 120, 120));
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
						RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				Font f = g.getFont().deriveFont(Font.PLAIN, 13f);
				g.setFont(f);
				String msg = Messages.getString("JInstrumentEditor.105"); //$NON-NLS-1$
				FontMetrics fm = g.getFontMetrics();
				int tw = fm.stringWidth(msg);
				int x = Math.max(6, (w - tw) / 2);
				int y = (h + fm.getAscent()) / 2 - 2;
				g.drawString(msg, x, y);
			} finally {
				g.dispose();
			}
			noInstrumentPicturePlaceholder = new ImageIcon(bi);
		}
		return noInstrumentPicturePlaceholder;
	}

	/**
	 * Shows the given picture, or a neutral placeholder when {@code picture} is
	 * null.
	 */
	private void updateInstrumentPictureDisplay(Image picture) {
		if (instrumentImage == null) {
			return;
		}
		if (picture != null) {
			instrumentImage.setIcon(new ImageIcon(picture));
		} else {
			instrumentImage.setIcon(getNoInstrumentPicturePlaceholderIcon());
		}
		instrumentImage.revalidate();
		instrumentImage.repaint();
	}

	/**
	 * Replaces the JETA form grid with a clear layout: name row, then a split with
	 * a large picture preview and an editable description. Layout is applied to
	 * {@code host} (a plain JPanel). {@code formLoader} is only used for
	 * {@code getComponentByName("descriptionform")}; it must not be
	 * {@code removeAll}'d — that breaks Abeille/JGoodies FormLayout maps.
	 */
	private void applyGeneralTabLayout(JPanel host, FormPanel formLoader,
			JLabel labelinstrumentname, JLabel labelinstrumentpicture,
			JLabel labelinstrumentdescription) {
		JComponent descriptionForm = (JComponent) formLoader
				.getComponentByName("descriptionform"); //$NON-NLS-1$

		detachFromParent(instrumentName);
		detachFromParent(instrumentDescription);
		detachFromParent(instrumentImage);
		detachFromParent(labelinstrumentname);
		detachFromParent(labelinstrumentpicture);
		detachFromParent(labelinstrumentdescription);
		detachFromParent(buttonchoicepicture);
		detachFromParent(descriptionForm);

		host.removeAll();
		host.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

		instrumentDescription.setLineWrap(true);
		instrumentDescription.setWrapStyleWord(true);
		instrumentDescription.setRows(12);
		instrumentDescription.setTabSize(4);

		JScrollPane descScroll = new JScrollPane(instrumentDescription);
		descScroll.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		JPanel descColumn = new JPanel(new BorderLayout());
		Border descPad = BorderFactory.createEmptyBorder(8, 10, 10, 10);
		descColumn.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(
						BorderFactory.createEtchedBorder(),
						Messages.getString("JInstrumentEditor.64")), //$NON-NLS-1$
				descPad));
		descColumn.add(descScroll, BorderLayout.CENTER);
		descColumn.setMinimumSize(new Dimension(200, 120));

		instrumentImage.setMinimumSize(new Dimension(64, 64));
		JScrollPane imageScroll = new JScrollPane(instrumentImage);
		imageScroll.setPreferredSize(new Dimension(
				GENERAL_TAB_IMAGE_VIEWPORT_W, GENERAL_TAB_IMAGE_VIEWPORT_H));
		imageScroll.getViewport().setBackground(new Color(250, 250, 250));
		imageScroll.getVerticalScrollBar().setUnitIncrement(16);
		imageScroll.getHorizontalScrollBar().setUnitIncrement(16);

		JPanel pictureActions = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 4));
		pictureActions.setBorder(BorderFactory.createEmptyBorder(4, 10, 2, 10));
		buttonchoicepicture.setText(Messages.getString("JInstrumentEditor.103")); //$NON-NLS-1$
		buttonchoicepicture.setToolTipText(
				Messages.getString("JInstrumentEditor.104")); //$NON-NLS-1$
		pictureActions.add(buttonchoicepicture);

		String pictureSectionTitle = Messages.getString("JInstrumentEditor.57"); //$NON-NLS-1$
		if (pictureSectionTitle.endsWith(":")) { //$NON-NLS-1$
			pictureSectionTitle = pictureSectionTitle.substring(0,
					pictureSectionTitle.length() - 1).trim();
		}
		Border pictureInnerPad = BorderFactory.createEmptyBorder(10, 12, 8, 12);
		JPanel pictureColumn = new JPanel(new BorderLayout(0, 8));
		pictureColumn.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(
						BorderFactory.createEtchedBorder(), pictureSectionTitle),
				pictureInnerPad));
		pictureColumn.add(imageScroll, BorderLayout.CENTER);
		pictureColumn.add(pictureActions, BorderLayout.SOUTH);
		pictureColumn.setMinimumSize(new Dimension(180, 160));

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				pictureColumn, descColumn);
		split.setResizeWeight(0.48);
		split.setContinuousLayout(true);
		split.setOneTouchExpandable(true);
		split.setDividerSize(10);
		split.setBorder(BorderFactory.createEmptyBorder());

		JPanel nameRow = new JPanel(new BorderLayout(10, 0));
		nameRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		nameRow.add(labelinstrumentname, BorderLayout.WEST);
		nameRow.add(instrumentName, BorderLayout.CENTER);

		host.add(nameRow, BorderLayout.NORTH);
		host.add(split, BorderLayout.CENTER);

		if (model != null) {
			updateInstrumentPictureDisplay(model.getInstrumentPicture());
		} else {
			updateInstrumentPictureDisplay(null);
		}
	}

	private JPanel buildInstrumentMappingTab() {
		detachFromParent(registrationLabel);
		detachFromParent(pipeStopGroupCombo);
		detachFromParent(clearSoundSampleSelection);
		detachFromParent(addSound);
		detachFromParent(modifySound);
		detachFromParent(removeSoundMapping);
		detachFromParent(removeSound);
		detachFromParent(addFromCropFileButton);
		detachFromParent(loadsoundsample);
		detachFromParent(savesoundsample);

		detachFromParent(mappingPianoScroll);
		detachFromParent(mappingSoundsScroll);
		detachFromParent(mappingScaleScroll);

		JToolBar registrationToolbar = new JToolBar();
		registrationToolbar.setFloatable(false);
		registrationToolbar.setRollover(true);
		registrationToolbar.add(registrationLabel);
		registrationToolbar.addSeparator();
		registrationToolbar.add(pipeStopGroupCombo);

		JToolBar soundsToolbar = new JToolBar();
		soundsToolbar.setFloatable(false);
		soundsToolbar.setRollover(true);
		soundsToolbar.add(clearSoundSampleSelection);
		soundsToolbar.add(addSound);
		soundsToolbar.add(modifySound);
		soundsToolbar.add(removeSoundMapping);
		soundsToolbar.add(removeSound);
		soundsToolbar.addSeparator();
		soundsToolbar.add(addFromCropFileButton);
		soundsToolbar.add(loadsoundsample);
		soundsToolbar.add(savesoundsample);

		mappingSoundsScroll.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				Messages.getString("JInstrumentEditorPanel.mappingSoundSamples"))); //$NON-NLS-1$
		mappingScaleScroll.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				Messages.getString("JInstrumentEditorPanel.mappingScalePreview"))); //$NON-NLS-1$

		JPanel soundListColumn = new JPanel(new BorderLayout());
		soundListColumn.add(soundsToolbar, BorderLayout.NORTH);
		soundListColumn.add(mappingSoundsScroll, BorderLayout.CENTER);

		mappingSoundsScroll.setMinimumSize(new Dimension(120, 160));
		mappingScaleScroll.setMinimumSize(new Dimension(140, 140));

		JPanel mappingScalePreviewCap = new JPanel(new BorderLayout(0, 0)) {
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getMaximumSize() {
				Dimension d = super.getMaximumSize();
				return new Dimension(MAX_MAPPING_SCALE_PREVIEW_WIDTH, d.height);
			}
		};
		mappingScalePreviewCap.add(mappingScaleScroll, BorderLayout.CENTER);

		mappingListScaleSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, soundListColumn,
				mappingScalePreviewCap);
		mappingListScaleSplit.setResizeWeight(0.45);
		mappingListScaleSplit.setOneTouchExpandable(true);
		mappingListScaleSplit.setContinuousLayout(true);
		mappingListScaleSplit.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		Dimension pianoPref = pianoroll.getPreferredSize();
		int pianoH = (pianoPref != null ? pianoPref.height : PianoRenderingNote.KEYSIZE_Y) + 4;
		mappingPianoScroll.setPreferredSize(new Dimension(
				pianoPref != null ? pianoPref.width : 800, pianoH));
		mappingPianoScroll.setMinimumSize(new Dimension(80, pianoH));
		mappingPianoScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, pianoH));

		JPanel root = new JPanel(new BorderLayout());
		root.add(registrationToolbar, BorderLayout.NORTH);
		root.add(mappingListScaleSplit, BorderLayout.CENTER);
		root.add(mappingPianoScroll, BorderLayout.SOUTH);
		return root;
	}

	private void saveMappingSplitLocations() {
		if (mappingListScaleSplit == null) {
			return;
		}
		MAPPING_SPLIT_PREFS.putInt(PREF_MAPPING_SPLIT_H, mappingListScaleSplit.getDividerLocation());
	}

	private void restoreMappingSplitLocations() {
		if (mappingListScaleSplit == null) {
			return;
		}
		int h = MAPPING_SPLIT_PREFS.getInt(PREF_MAPPING_SPLIT_H, -1);
		if (h >= 0) {
			mappingListScaleSplit.setDividerLocation(h);
		}
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
					.findReferencedPercussionByMidiCode(percussionDef.getPercussion());

			String drumName = ReferencedPercussion.getLocalizedDrumLabel(r);

			SoundSample ss = model.getPercussionSoundSample(percussionDef);

			dlm.addRow(new Object[] { new DrumObjectReferenceDisplay(drumName, percussionDef),
					"" + (ss == null ? Messages.getString("JInstrumentEditorPanel.217") : " -> " + ss.getName()) }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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

	private void updateSoundListForCurrentPipeStopGroup(String currentPipeStopGroup) {

		if (model == null)
			return;

		List<SoundSample> soundSampleList = model.getSoundSampleList(currentPipeStopGroup);

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

		for (Iterator<SoundSample> iterator = soundSampleList.iterator(); iterator.hasNext();) {
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

			List<SoundSample> soundSampleList = model.getSoundSampleList(currentPipeStopGroup);
			ArrayList<SampleMapping> a = new ArrayList<SampleMapping>();
			for (Iterator<SoundSample> iterator = soundSampleList.iterator(); iterator.hasNext();) {
				SoundSample sampleMapping = iterator.next();

				SampleMapping sampleMapping2 = model.getSampleMapping(currentPipeStopGroup, sampleMapping);
				a.add(sampleMapping2);
			}

			SF2Soundbank soundBank = sb.createSimpleSoundBank(a.toArray(new SampleMapping[0]));

			logger.debug("sending the new sound bank ..."); //$NON-NLS-1$
			player.changeCurrentSoundBank(soundBank);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	private void updatePianoRoll() {

		logger.debug("update PianoRoll ..."); //$NON-NLS-1$

		assert model != null;

		List<SoundSample> soundSampleList = model.getSoundSampleList(getCurrentPipeStopGroup());

		pianoroll.clearSelectedRangeItem();
		pianoroll.removeAllSelectedRange();

		// get the mappings and create selectedRanges ....

		int cpt = 0;
		for (Iterator<SoundSample> iterator = soundSampleList.iterator(); iterator.hasNext();) {
			SoundSample soundSample = (SoundSample) iterator.next();
			logger.debug("treat soundSample " + soundSample); //$NON-NLS-1$

			SampleMapping sampleMapping = model.getSampleMapping(getCurrentPipeStopGroup(), soundSample);

			if (logger.isDebugEnabled())
				logger.debug("associated sound mapping " + sampleMapping); //$NON-NLS-1$

			if (sampleMapping != null) {
				logger.debug("has a mapping ..."); //$NON-NLS-1$

				SelectedRange r = new SelectedRange(sampleMapping.getFirstMidiCode(), sampleMapping.getLastMidiCode());

				pianoroll.addRange(r, soundSample);
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

	private AbstractFileObject lastOpenedFile = null;
	private AbstractFileObject lastOpenedSoundSampleFile = null;

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

			APrintFileChooser fc = new APrintFileChooser();
			fc.setFileFilter(new VFSFileNameExtensionFilter(Messages.getString("JInstrumentEditorPanel.1000"), //$NON-NLS-1$
					new String[] { SoundSampleIO.SOUNDSAMPLEEXTENSION })); // $NON-NLS-1$

			fc.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

			if (lastOpenedSoundSampleFile != null)
				fc.setSelectedFile(lastOpenedSoundSampleFile);

			if (fc.showSaveDialog(this) == APrintFileChooser.APPROVE_OPTION) {

				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				AbstractFileObject result = fc.getSelectedFile();
				try {
					String filename = result.getName().getBaseName();
					if (!filename.endsWith("." + SoundSampleIO.SOUNDSAMPLEEXTENSION)) { //$NON-NLS-1$
						result = (AbstractFileObject) result.getFileSystem()
								.resolveFile(result.getName().toString() + "." //$NON-NLS-1$
										+ SoundSampleIO.SOUNDSAMPLEEXTENSION);
					}

					lastOpenedSoundSampleFile = result;

					OutputStream fos = VFSTools.transactionalWrite(result);
					try {
						ssio.saveSample(ss, null, fos);

					} finally {
						fos.close();
					}
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
				JMessageBox.showMessage(this.parentFrame, Messages.getString("JInstrumentEditorPanel.1008") //$NON-NLS-1$
						+ result.getName() + " " + Messages.getString("JInstrumentEditorPanel.1010")); //$NON-NLS-1$ //$NON-NLS-2$

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

			APrintFileChooser fc = new APrintFileChooser();
			fc.setFileFilter(new VFSFileNameExtensionFilter(Messages.getString("JInstrumentEditorPanel.1012"), //$NON-NLS-1$
					new String[] { SoundSampleIO.SOUNDSAMPLEEXTENSION })); // $NON-NLS-1$

			fc.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

			if (lastOpenedSoundSampleFile != null)
				fc.setSelectedFile(lastOpenedSoundSampleFile);

			if (fc.showOpenDialog(this) == APrintFileChooser.APPROVE_OPTION) {

				final AbstractFileObject result = fc.getSelectedFile();
				lastOpenedSoundSampleFile = result;
				InputStream istream = result.getInputStream();
				try {
					SoundSample s = ssio.readSample(istream);

					if (s != null) {
						model.addSoundSample(s, getCurrentPipeStopGroup());

						int smidiroot = s.getMidiRootNote();
						if (smidiroot >= 0) {
							model.setSampleMapping(getCurrentPipeStopGroup(), s, smidiroot, smidiroot);
						}
					}
				} finally {
					istream.close();
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

			APrintFileChooser fc = new APrintFileChooser();
			fc.setFileFilter(new VFSFileNameExtensionFilter(Messages.getString("JInstrumentEditor.46"), //$NON-NLS-1$
					new String[] { "wav" })); //$NON-NLS-1$

			fc.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

			if (lastOpenedFile != null)
				fc.setSelectedFile(lastOpenedFile);

			if (fc.showOpenDialog(this) == APrintFileChooser.APPROVE_OPTION) {

				final AbstractFileObject result = fc.getSelectedFile();
				lastOpenedFile = result;

				InputStream istream = result.getInputStream();
				try {
					SoundSample s = GUIInstrumentTools.loadWavFile(istream, result.getName().getBaseName());

					SoundSample newConstructedSoundSample = showModalEditingSoundSampleEditor(s);

					if (newConstructedSoundSample != null)
						model.addSoundSample(newConstructedSoundSample, getCurrentPipeStopGroup());
				} finally {
					istream.close();
				}
			}

			soundSampleListChanged(getCurrentPipeStopGroup());

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			JMessageBox.showMessage(this.parentFrame, Messages.getString("JInstrumentEditor.75") //$NON-NLS-1$
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

	private SoundSample showModalEditingSoundSampleEditor(SoundSample s) throws Exception {

		SampleMapping mapping = model.getSampleMapping(getCurrentPipeStopGroup(), s);

		final JDialog d = new JDialog(this.parentFrame, Messages.getString("JInstrumentEditor.48")); //$NON-NLS-1$
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

		SoundSample editedSoundSample = soundSampleEditorPanel.constructSoundSample();

		if (editedSoundSample != null) {
			logger.debug("user has not cancelled the element ...."); //$NON-NLS-1$
			if (mapping == null) {
				// create a new mapping ..
				logger.debug("creating the new mapping"); //$NON-NLS-1$
				model.setSampleMapping(getCurrentPipeStopGroup(), editedSoundSample,
						editedSoundSample.getMidiRootNote(), editedSoundSample.getMidiRootNote());
			} else {
				// update the mapping ...
				logger.debug("update the mapping ..."); //$NON-NLS-1$
				model.setSampleMapping(getCurrentPipeStopGroup(), editedSoundSample, mapping.getFirstMidiCode(),
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

		model.removeSampleMapping(getCurrentPipeStopGroup(), soundSampleOnWhichRemoveTheMapping);
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

			APrintFileChooser fc = new APrintFileChooser();
			fc.setFileFilter(new VFSFileNameExtensionFilter(Messages.getString("JInstrumentEditor.46"), //$NON-NLS-1$
					new String[] { "wav" })); //$NON-NLS-1$

			fc.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

			if (lastOpenedFile != null)
				fc.setSelectedFile(lastOpenedFile);

			if (fc.showOpenDialog(this) == APrintFileChooser.APPROVE_OPTION) {

				final AbstractFileObject result = fc.getSelectedFile();
				lastOpenedFile = result;

				InputStream stream = result.getInputStream();
				try {
					SoundSample s = GUIInstrumentTools.loadWavFile(stream, result.getName().getBaseName());

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
								model.setSampleMapping(getCurrentPipeStopGroup(), s, s.getMidiRootNote(),
										s.getMidiRootNote());
								JMessageBox.showMessage(d,
										Messages.getString("JInstrumentEditorPanel.1014") + s.getName() //$NON-NLS-1$
												+ Messages.getString("JInstrumentEditorPanel.1015")); //$NON-NLS-1$
								logger.debug("sound sample " + s + "  added"); //$NON-NLS-1$ //$NON-NLS-2$
							} catch (Throwable t) {
								logger.error(t.getMessage(), t);
							}
						}
					});

					d.setVisible(true);

					logger.debug("finish showing the form ..."); //$NON-NLS-1$
				} finally {
					stream.close();
				}
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

			DrumObjectReferenceDisplay d = (DrumObjectReferenceDisplay) drumsoundlist.getModel().getValueAt(selRow, 0);

			if (d == null)
				return;

			PercussionDef pd = d.getPercussionDef();
			if (pd == null)
				return;

			SoundSample ss = model.getPercussionSoundSample(pd);

			if (ss == null)
				return;

			WavPlayer wp = new WavPlayer();
			wp.playSound(ss.getManagedAudioInputStream(), new WavPlayerListener() {
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
			JMessageBox.showMessage(parentFrame, "Error in playing the drum sound :" + ex.getMessage()); //$NON-NLS-1$
		}

	}

	protected void removeDrumSound() {
		try {

			int selRow = drumsoundlist.getSelectedRow();
			if (selRow == -1)
				throw new Exception("you must select a sample"); //$NON-NLS-1$

			DrumObjectReferenceDisplay d = (DrumObjectReferenceDisplay) drumsoundlist.getModel().getValueAt(selRow, 0);

			PercussionDef pd = d.getPercussionDef();

			model.setPercussionSoundSample(pd, null);

			updateDrumSoundList();

		} catch (Throwable ex) {
			logger.error("error in remove sound for drum :" + ex.getMessage(), //$NON-NLS-1$
					ex);
			JMessageBox.showMessage(parentFrame, Messages.getString("JInstrumentEditorPanel.221") + ex.getMessage()); //$NON-NLS-1$
		}
	}

	/**
	 * 
	 */
	protected void addDrumSound() {
		try {

			APrintFileChooser fc = new APrintFileChooser();
			fc.setFileFilter(new VFSFileNameExtensionFilter(Messages.getString("JInstrumentEditor.46"), //$NON-NLS-1$
					new String[] { "wav" })); //$NON-NLS-1$

			fc.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

			if (lastOpenedFile != null)
				fc.setSelectedFile(lastOpenedFile);

			if (fc.showOpenDialog(parentFrame) == APrintFileChooser.APPROVE_OPTION) {

				final AbstractFileObject result = fc.getSelectedFile();
				lastOpenedFile = result;
				InputStream stream = result.getInputStream();
				assert stream != null;
				try {
					SoundSample s = GUIInstrumentTools.loadWavFile(stream, result.getName().getBaseName());

					SoundSample newConstructedSoundSample = showModalEditingSoundSampleEditor(s);

					if (newConstructedSoundSample != null) {
						int selRow = drumsoundlist.getSelectedRow();
						if (selRow == -1)
							throw new Exception("you must select a sample"); //$NON-NLS-1$

						DrumObjectReferenceDisplay d = (DrumObjectReferenceDisplay) drumsoundlist.getModel()
								.getValueAt(selRow, 0);

						PercussionDef pd = d.getPercussionDef();

						newConstructedSoundSample.setMidiRootNote(pd.getPercussion());
						model.setPercussionSoundSample(pd, newConstructedSoundSample);

						updateDrumSoundList();
					}
				} finally {
					stream.close();
				}
			}

		} catch (Throwable ex) {
			logger.error("error in adding sound for drum :" + ex.getMessage(), //$NON-NLS-1$
					ex);
			JMessageBox.showMessage(parentFrame, Messages.getString("JInstrumentEditorPanel.224") + ex.getMessage()); //$NON-NLS-1$
		}
	}

	protected void editDrumSoundSample() {
		try {

			int selRow = drumsoundlist.getSelectedRow();
			if (selRow == -1)
				throw new Exception("you must select a sample"); //$NON-NLS-1$

			DrumObjectReferenceDisplay d = (DrumObjectReferenceDisplay) drumsoundlist.getModel().getValueAt(selRow, 0);

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
