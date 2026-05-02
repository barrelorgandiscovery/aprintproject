package org.barrelorgandiscovery.gui.ascale;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.MessageFormat;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.ascale.constraints.ConstraintListChangeListener;
import org.barrelorgandiscovery.gui.ascale.constraints.ConstraintPanel;
import org.barrelorgandiscovery.gui.swing.ComboBoxPopupSafeguards;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.ConstraintList;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.PipeStopGroupList;
import org.barrelorgandiscovery.scale.ReferencedState;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.virtualbook.rendering.VirtualBookRendering;
import org.barrelorgandiscovery.virtualbook.rendering.VirtualBookRenderingFactory;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;

public class JScaleEditorPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8484645796626431335L;

	/**
	 * Bean name in {@code scaleEditorLeftPart.jfrm} for the general tab. Matches
	 * Abeille strings and committed (working) code: {@code getComponentByName} +
	 * {@code getParent().add(generalProperties)}.
	 */
	private static final String GENERAL_PROPERTIES_BEAN = "generalProperties"; //$NON-NLS-1$

	/**
	 * Wider than the LAF default (~5–7px) so the diagram|properties and
	 * diagram|track-editor dividers are easier to grab and drag.
	 */
	private static final int SPLIT_DIVIDER_SIZE = 12;

	/**
	 * Low minimum for the diagram side so {@link JSplitPane} can move the
	 * divider; children with huge implicit mins would otherwise pin the sash.
	 */
	private static final int SPLIT_MIN_SCALE_SIDE_PX = 120;

	private static Logger logger = Logger.getLogger(JScaleEditorPanel.class);
	//
	// private JPanel panelparameters;
	// private JPanel panelparametrespistes;
	// private JLabel label6; // titre partie gauche

	private JLabel labellargeurcarton;
	private JSpinner spinnerlargeurcarton;
	private JLabel labelaxepremierepiste;
	private JSpinner spinneraxepremierepiste;
	private JLabel labelaxepiste;
	private JSpinner spinnerentreaxepiste;
	private JLabel labellargeurpiste;
	private JSpinner spinnerlargeurpiste;
	private JLabel labelvitesse;
	private JTextField scalename;

	private JSpinner spinnerspeed;
	private JScrollPane scalecomponentscrollpane;
	private ScaleComponent scalecomponent;

	/** Zoom slider (toolbar); kept for sync after programmatic scale changes */
	private JSlider zoomSlider;

	private JSpinner spinnernbpistes;
	private JLabel labelnbpistes;

	private JLabel labelcontact;
	private JTextField contact;

	private JComboBox combostate;
	private JLabel labelstate;

	/**
	 * Scale Informations
	 */
	private JTextArea infostextarea;

	/**
	 * Organ Description
	 */
	// private JPanel parametreregistres;
	private InstrumentPipeStopDescriptionComponent registrecomponent;

	private AbstractGlobalTrackDefComponent tabbededitors;

	private ConstraintPanel constraintPanel;

	private JCheckBox preferredViewInverted;

	private ScaleEditorPrefs prefs = null;

	private PropertyHashEditor propertyHashEditor;

	/**
	 * Left Panel
	 */
	private FormPanel leftPanel = null;
	/**
	 * General tab content from {@code scaleEditorGeneralProperties.jfrm}, added
	 * beside the {@link #GENERAL_PROPERTIES_BEAN} slot (same integration as git HEAD).
	 */
	private FormPanel generalProperties = null;
	private JTabbedPane tab = null;

	private FormPanel scalePanel = null;

	private JComboBox bookType;

	/** Labels from the form; kept for programmatic reflow of the General tab */
	private JLabel labelScaleDescription;
	private JLabel labelBookTypeCaption;
	private JLabel labelInvertReference;
	private JLabel labelInformationNotes;

	private Object owner;

	/**
	 * Constructeur
	 */
	public JScaleEditorPanel(Object owner, ScaleEditorPrefs prefs)
			throws Exception {
		super();
		assert prefs != null;

		this.owner = owner;
		this.prefs = prefs;

		leftPanel = new FormPanel(getClass().getResourceAsStream(
				"scaleEditorLeftPart.jfrm")); //$NON-NLS-1$
		tab = (JTabbedPane) leftPanel.getComponentByName("tab"); //$NON-NLS-1$
		// get the first grid view ...

		for (int i = 0; i < tab.getTabCount(); i++) {
			String tabName = tab.getTitleAt(i);

			if ("general".equals(tabName)) { //$NON-NLS-1$
				tabName = Messages.getString("JScaleEditorPanel.16"); //$NON-NLS-1$
			} else if ("organ composition".equals(tabName)) { //$NON-NLS-1$
				tabName = Messages.getString("JScaleEditorPanel.14"); //$NON-NLS-1$
			} else if ("constraints".equals(tabName)) { //$NON-NLS-1$
				tabName = Messages.getString("JScaleEditorPanel.12"); //$NON-NLS-1$
			} else if ("advancedProperties".equals(tabName)) { //$NON-NLS-1$
				tabName = Messages.getString("JScaleEditorPanel.10"); //$NON-NLS-1$
			}

			tab.setTitleAt(i, tabName);

		}

		generalProperties = new FormPanel(getClass().getResourceAsStream(
				"scaleEditorGeneralProperties.jfrm")); //$NON-NLS-1$

		// Identical to pre-refactor / git HEAD: first bean named generalProperties
		// (duplicate label+grid in jfrm), parent.add(full form). Works with extension
		// ClassLoader; replaceBean paths hit "Unable to find oldComp" there.
		Component oldComp = leftPanel.getComponentByName(GENERAL_PROPERTIES_BEAN);
		if (oldComp == null) {
			throw new IllegalStateException(
					"scaleEditorLeftPart: bean " + GENERAL_PROPERTIES_BEAN + " missing"); //$NON-NLS-1$
		}
		Container anchorParent = oldComp.getParent();
		if (anchorParent == null) {
			throw new IllegalStateException(
					"scaleEditorLeftPart: no parent for bean " //$NON-NLS-1$
							+ GENERAL_PROPERTIES_BEAN);
		}
		anchorParent.add(generalProperties);
		if (logger.isInfoEnabled()) {
			logger.info("general tab: legacy attach bean=" + GENERAL_PROPERTIES_BEAN //$NON-NLS-1$
					+ " formParent=" //$NON-NLS-1$
					+ generalProperties.getParent().getClass().getName());
		}

		scalePanel = new FormPanel(getClass().getResourceAsStream(
				"scaleEditorScaleEdit.jfrm")); //$NON-NLS-1$

		initComponents();
		updateGammeComponent();
	}

	/**
	 * Initialisation de composants
	 */
	private void initComponents() {

		labellargeurcarton = (JLabel) generalProperties
				.getComponentByName("labelBookWidth"); //$NON-NLS-1$
		spinnerlargeurcarton = (JSpinner) generalProperties
				.getComponentByName("bookWidth"); //$NON-NLS-1$
		spinnerlargeurcarton.setModel(new SpinnerNumberModel(200.0, 0.0, 500.0,
				1.0));
		spinnerlargeurcarton.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				scalecomponent.changeLargeurCarton(((Number) s.getValue())
						.doubleValue());
			}
		});

		labelInvertReference = (JLabel) generalProperties
				.getComponentByName("labelInvertReference"); //$NON-NLS-1$
		labelInvertReference.setText(Messages.getString("ScaleEditor.0")); //$NON-NLS-1$
		preferredViewInverted = (JCheckBox) generalProperties
				.getComponentByName("invertReference"); //$NON-NLS-1$
		preferredViewInverted.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox jch = (JCheckBox) e.getSource();
				scalecomponent.changePreferredViewInverted(jch.isSelected());

			}
		});

		labelaxepremierepiste = (JLabel) generalProperties
				.getComponentByName("labelFirstTrackAxis"); //$NON-NLS-1$

		spinneraxepremierepiste = (JSpinner) generalProperties
				.getComponentByName("firstTrackAxis"); //$NON-NLS-1$
		spinneraxepremierepiste.setModel(new SpinnerNumberModel(10.0, 0.0,
				1000.0, 0.1));

		spinneraxepremierepiste.setEditor(new JSpinner.NumberEditor(
				spinneraxepremierepiste, "0.000")); //$NON-NLS-1$

		spinneraxepremierepiste.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				scalecomponent.changePremierePiste(((Number) s.getValue())
						.doubleValue());
			}
		});

		labelaxepiste = (JLabel) generalProperties
				.getComponentByName("labelIntertrackWidth"); //$NON-NLS-1$

		spinnerentreaxepiste = (JSpinner) generalProperties
				.getComponentByName("intertrackWidth"); //$NON-NLS-1$
		spinnerentreaxepiste.setModel(new SpinnerNumberModel(5.0, 0.0, 100.0,
				0.1));

		spinnerentreaxepiste.setEditor(new JSpinner.NumberEditor(
				spinnerentreaxepiste, "0.000")); //$NON-NLS-1$

		spinnerentreaxepiste.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				scalecomponent.changeEntrePiste(((Number) s.getValue())
						.doubleValue());
			}
		});

		labellargeurpiste = (JLabel) generalProperties
				.getComponentByName("labelPunchHeight"); //$NON-NLS-1$

		spinnerlargeurpiste = (JSpinner) generalProperties
				.getComponentByName("punchHeight"); //$NON-NLS-1$
		spinnerlargeurpiste
				.setModel(new SpinnerNumberModel(5.0, 0.0, 30.0, 0.1));
		spinnerlargeurpiste.setEditor(new JSpinner.NumberEditor(
				spinnerlargeurpiste, "0.000")); //$NON-NLS-1$

		spinnerlargeurpiste.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				scalecomponent.changeLargeurPiste(((Number) s.getValue())
						.doubleValue());
			}
		});

		labelvitesse = (JLabel) generalProperties
				.getComponentByName("labelSpeed"); //$NON-NLS-1$

		spinnerspeed = (JSpinner) generalProperties.getComponentByName("speed"); //$NON-NLS-1$
		spinnerspeed.setModel(new SpinnerNumberModel(60.0, 0.0, 1000.0, 1));

		spinnerspeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				scalecomponent.changeVitesse(((Number) s.getValue())
						.doubleValue());
			}
		});

		labelScaleDescription = (JLabel) generalProperties
				.getComponentByName("labelScaleDescription"); //$NON-NLS-1$
		labelScaleDescription.setText(Messages.getString("JScaleEditorPanel.18")); //$NON-NLS-1$

		scalename = (JTextField) generalProperties
				.getComponentByName("scaleDescription"); //$NON-NLS-1$
		scalename.getDocument().addDocumentListener(new DocumentListener() {

			public void changedUpdate(DocumentEvent e) {
				update(e);

			}

			public void insertUpdate(DocumentEvent e) {
				update(e);
			}

			public void removeUpdate(DocumentEvent e) {
				update(e);
			}

			private void update(DocumentEvent e) {
				Document doc = e.getDocument();
				String t = ""; //$NON-NLS-1$
				try {
					t = doc.getText(0, doc.getLength());
				} catch (BadLocationException ex) {
				}

				scalecomponent.changeName(t);

			}
		});

		labelcontact = (JLabel) generalProperties
				.getComponentByName("labelScaleAuthor"); //$NON-NLS-1$
		labelcontact.setText(Messages.getString("GammeEditor.44")); //$NON-NLS-1$

		contact = (JTextField) generalProperties
				.getComponentByName("scaleAuthor"); //$NON-NLS-1$
		contact.getDocument().addDocumentListener(new DocumentListener() {

			public void changedUpdate(DocumentEvent e) {
				update(e);

			}

			public void insertUpdate(DocumentEvent e) {
				update(e);
			}

			public void removeUpdate(DocumentEvent e) {
				update(e);
			}

			private void update(DocumentEvent e) {
				Document doc = e.getDocument();
				String t = ""; //$NON-NLS-1$
				try {
					t = doc.getText(0, doc.getLength());
				} catch (BadLocationException ex) {
				}

				scalecomponent.changeContact(t);
			}
		});

		labelstate = (JLabel) generalProperties
				.getComponentByName("labelScaleState"); //$NON-NLS-1$
		labelstate.setText(Messages.getString("GammeEditor.45")); //$NON-NLS-1$

		combostate = (JComboBox) generalProperties
				.getComponentByName("scaleState"); //$NON-NLS-1$
		ReferencedState[] gammestates = ReferencedState.listReferencedState();
		for (ReferencedState st : gammestates) {
			combostate.addItem(st);
		}

		combostate.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if (e.getItem() != null)
						scalecomponent.changeState(((ReferencedState) e
								.getItem()).getName());
				}
			}
		});

		bookType = (JComboBox) generalProperties.getComponentByName("booktype"); //$NON-NLS-1$
		VirtualBookRendering[] renderingList = VirtualBookRenderingFactory
				.getRenderingList();
		for (VirtualBookRendering v : renderingList) {
			bookType.addItem(new VirtualBookRenderingDisplay(v));
		}

		bookType.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if (e.getItem() != null) {
						scalecomponent
								.changeRendering(((VirtualBookRenderingDisplay) e
										.getItem()).getRendering());
					}
				}
			}
		});

		labelnbpistes = (JLabel) generalProperties
				.getComponentByName("labelTrackNumber"); //$NON-NLS-1$
		labelnbpistes.setText(Messages.getString("GammeEditor.2")); //$NON-NLS-1$

		spinnernbpistes = (JSpinner) generalProperties
				.getComponentByName("trackNumber"); //$NON-NLS-1$
		spinnernbpistes.setModel(new SpinnerNumberModel(30, 0, 300, 1));
		spinnernbpistes.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				scalecomponent.changeNbPiste(((Number) s.getValue()).intValue());
			}
		});

		labelBookTypeCaption = (JLabel) generalProperties
				.getComponentByName("labelBookType"); //$NON-NLS-1$
		labelBookTypeCaption.setText(Messages.getString("JScaleEditorPanel.28")); //$NON-NLS-1$

		//
		// parametreregistres = new JPanel();
		// parametreregistres.setBorder(new TitledBorder(Messages
		// .getString("GammeEditor.3"))); //$NON-NLS-1$

		scalecomponent = new ScaleComponent();
		scalecomponent.setSpeedDraw(true);

		registrecomponent = new InstrumentPipeStopDescriptionComponent();

		tabbededitors = new TrackDefComboPane(registrecomponent);

		// new TrackDefTabbedPane(registrecomponent);

		// Comportement, lien entre l'édition de note et le carton ...

		scalecomponent.setGammeListener(new ScaleComponentListener() {
			public void trackClicked(int trackclicked) {
				logger.debug("receive track clicked"); //$NON-NLS-1$
				scalecomponent.setSelectedTrackDef(trackclicked);
				scalecomponent.repaint();
			}

			public void trackSelected(int selectedtrack) {
				logger.debug("received trackSelected " + selectedtrack); //$NON-NLS-1$
				if (selectedtrack != -1) {
					logger.debug("tabbedEditors edit track " + selectedtrack); //$NON-NLS-1$
					tabbededitors.edit(scalecomponent
							.getTrackDef(selectedtrack));
				}
			}
		});

		registrecomponent
				.addRegisterSetListChangeListener(new RegisterSetListChangeListener() {

					public void registerSetListChanged(PipeStopGroupList newlist) {
						try {
							scalecomponent.changeRegisterSetList(newlist);
						} catch (Exception ex) {
							logger.error("registerSetListChanged", ex); //$NON-NLS-1$
						}
					}
				});

		constraintPanel = new ConstraintPanel();
		constraintPanel
				.setConstraintListListener(new ConstraintListChangeListener() {

					public void constraintListChanged(
							ConstraintList newConstraintList) {
						scalecomponent.changeConstraintList(newConstraintList);
					}
				});

		// tabbedPaneScaleProperties.addTab(
		// Messages.getString("ScaleEditor.4"), registrecomponent);
		// //$NON-NLS-1$
		// tabbedPaneScaleProperties.addTab(
		// Messages.getString("ScaleEditor.5"), constraintPanel); //$NON-NLS-1$
		//

		propertyHashEditor = new PropertyHashEditor();
		propertyHashEditor
				.addPropertyHashEditorChangedListener(new PropertyHashEditorChangedListener() {
					public void hashChanged(HashMap<String, String> newHash) {
						scalecomponent.changeProperties(newHash);
					}
				});

		((JComponent) leftPanel.getComponentByName("advancedProperties")) //$NON-NLS-1$
				.add(propertyHashEditor);
		((JComponent) leftPanel.getComponentByName("constraints")) //$NON-NLS-1$
				.add(constraintPanel);
		((JComponent) leftPanel.getComponentByName("organComposition")) //$NON-NLS-1$
				.add(registrecomponent);

		// leftPanel.getFormAccessor().replaceBean(, propertyHashEditor);

		// tabbedPaneScaleProperties.addTab("Properties", propertyHashEditor);

		// hello world !!! a bath to make to the last child, Yann !!! :-)

		tabbededitors
				.setTrackDefComponentListener(new TrackDefComponentListener() {

					public void trackDefChanged(AbstractTrackDef td) {
						int selected = scalecomponent.getSelectedTrackDef();
						if (selected != -1)
							scalecomponent.changePisteDef(selected, td);

					}
				});

		tabbededitors.setBorder(new TitledBorder(Messages
				.getString("GammeEditor.24"))); //$NON-NLS-1$
		tabbededitors.setMinimumSize(new Dimension(0, 0));

		// ==== Scale Viewer ====

		scalecomponentscrollpane = new JScrollPane(scalecomponent,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scalecomponentscrollpane.setAutoscrolls(true);
		scalecomponentscrollpane.setMinimumSize(new Dimension(0, 0));

		final double scalefactor = 1.3;
		final int SLIDER_MAX_VALUE = 30;
		final int SLIDER_MIN_VALUE = 2;

		zoomSlider = new JSlider(JSlider.HORIZONTAL, SLIDER_MIN_VALUE,
				SLIDER_MAX_VALUE, 10);
		zoomSlider.setPreferredSize(new Dimension(220,
				zoomSlider.getPreferredSize().height));

		JPanel editorToolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
		editorToolBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

		JButton zoomplus = new JButton();
		zoomplus.setIcon(new ImageIcon(
				APrintNG.class.getResource("viewmagplus.png"))); //$NON-NLS-1$
		zoomplus.setToolTipText(Messages.getString("GammeEditor.49")); //$NON-NLS-1$
		zoomplus.addActionListener(e -> {
			scalecomponent.setScale(scalecomponent.getScale() * scalefactor);
			revalidateScaleViewAndSyncZoomSlider(SLIDER_MIN_VALUE,
					SLIDER_MAX_VALUE);
		});
		editorToolBar.add(zoomplus);

		JButton zoommoins = new JButton();
		zoommoins.setIcon(new ImageIcon(
				APrintNG.class.getResource("viewmagminus.png"))); //$NON-NLS-1$
		zoommoins.setToolTipText(Messages.getString("GammeEditor.52")); //$NON-NLS-1$
		zoommoins.addActionListener(e -> {
			scalecomponent.setScale(scalecomponent.getScale() / scalefactor);
			revalidateScaleViewAndSyncZoomSlider(SLIDER_MIN_VALUE,
					SLIDER_MAX_VALUE);
		});
		editorToolBar.add(zoommoins);

		editorToolBar.add(Box.createHorizontalStrut(8));
		editorToolBar.add(new JLabel(
				Messages.getString("JScaleEditorPanel.zoomLabel"))); //$NON-NLS-1$
		editorToolBar.add(zoomSlider);

		zoomSlider.addChangeListener(e -> {
			int value = zoomSlider.getValue();
			scalecomponent.setScale(value / 10.0);
			scalecomponent.revalidate();
			scalecomponentscrollpane.revalidate();
			scalecomponentscrollpane.repaint();
			zoomSlider.setToolTipText(MessageFormat.format(
					Messages.getString("JScaleEditorPanel.zoomFactorTooltip"), //$NON-NLS-1$
					scalecomponent.getScale()));
		});
		syncZoomSliderFromScale(SLIDER_MIN_VALUE, SLIDER_MAX_VALUE);

		editorToolBar.add(Box.createHorizontalStrut(8));

		JButton addTrack = new JButton(
				Messages.getString("JScaleEditorPanel.10000")); //$NON-NLS-1$
		addTrack.setToolTipText(Messages.getString("JScaleEditorPanel.10000")); //$NON-NLS-1$
		addTrack.addActionListener(e -> {
			int selectedTrackDef = scalecomponent.getSelectedTrackDef();
			if (selectedTrackDef != -1) {
				scalecomponent.shiftTracksDown(selectedTrackDef);
				scalecomponent.setSelectedTrackDef(selectedTrackDef);
			}
		});
		editorToolBar.add(addTrack);

		JButton removeTrack = new JButton(
				Messages.getString("JScaleEditorPanel.10001")); //$NON-NLS-1$
		removeTrack.setToolTipText(Messages.getString("JScaleEditorPanel.10001")); //$NON-NLS-1$
		removeTrack.addActionListener(e -> {
			int selectedTrackDef = scalecomponent.getSelectedTrackDef();
			if (selectedTrackDef != -1) {
				scalecomponent.shiftTracksUp(selectedTrackDef);
				scalecomponent.setSelectedTrackDef(selectedTrackDef);
			}
		});
		editorToolBar.add(removeTrack);

		JButton insererNotes = new JButton(
				Messages.getString("JScaleEditorPanel.1010")); //$NON-NLS-1$
		insererNotes.setToolTipText(Messages.getString("JScaleEditorPanel.1011")); //$NON-NLS-1$
		insererNotes.addActionListener(e -> {
			try {
				insertFollowingNotes();
			} catch (Throwable t) {
				logger.error("error in inserting notes " + t.getMessage(), //$NON-NLS-1$
						t);
			}
		});
		editorToolBar.add(insererNotes);

		JPanel diagramStack = new JPanel(new BorderLayout());
		diagramStack.setMinimumSize(new Dimension(0, 0));
		diagramStack.add(editorToolBar, BorderLayout.NORTH);
		diagramStack.add(scalecomponentscrollpane, BorderLayout.CENTER);

		JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				diagramStack, tabbededitors);
		verticalSplit.setResizeWeight(0.68);
		verticalSplit.setDividerSize(SPLIT_DIVIDER_SIZE);
		verticalSplit.setOneTouchExpandable(true);
		verticalSplit.setContinuousLayout(true);
		verticalSplit.setBorder(BorderFactory.createEmptyBorder());

		JPanel panelWithToolbarsAndScaleComponent = new JPanel(
				new BorderLayout());
		panelWithToolbarsAndScaleComponent.add(verticalSplit,
				BorderLayout.CENTER);

		this.scalePanel.getFormAccessor().replaceBean(
				scalePanel.getComponentByName("scalecomponent"), //$NON-NLS-1$
				panelWithToolbarsAndScaleComponent);
		scalePanel.setMinimumSize(new Dimension(SPLIT_MIN_SCALE_SIDE_PX, 80));

		JPanel generalPanel = new JPanel(new BorderLayout());

		// ======== panel1 ========

		// ---- label2 ----
		labellargeurcarton.setText(Messages.getString("GammeEditor.19")); //$NON-NLS-1$

		// ---- label3 ----
		labelaxepremierepiste.setText(Messages.getString("GammeEditor.20")); //$NON-NLS-1$

		// ---- label4 ----
		labelaxepiste.setText(Messages.getString("GammeEditor.21")); //$NON-NLS-1$

		// ---- label7 ----
		labellargeurpiste.setText(Messages.getString("GammeEditor.22")); //$NON-NLS-1$

		// ---- label5 ----
		labelvitesse.setText(Messages.getString("GammeEditor.23")); //$NON-NLS-1$

		JPanel generalPropertiesColumn = wrapGeneralPropertiesColumn(leftPanel);

		JSplitPane globalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				scalePanel, generalPropertiesColumn);

		globalSplit.setResizeWeight(0.58);
		globalSplit.setDividerSize(SPLIT_DIVIDER_SIZE);
		globalSplit.setOneTouchExpandable(true);
		globalSplit.setContinuousLayout(true);

		generalPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
		generalPanel.add(globalSplit, BorderLayout.CENTER);

		infostextarea = (JTextArea) generalProperties
				.getComponentByName("generalInformationsNotes"); //$NON-NLS-1$

		labelInformationNotes = (JLabel) generalProperties
				.getComponentByName("labelGeneralInformationsNotes"); //$NON-NLS-1$
		labelInformationNotes.setText(Messages.getString("GammeEditor.53")); //$NON-NLS-1$

		infostextarea.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				scalecomponent.changeInfos(infostextarea.getText());
			}
		});

		SwingUtilities.invokeLater(() -> {
			refreshSpinnerEditorsAfterLayout();
			if (logger.isInfoEnabled()) {
				logger.info("after init layout: generalProperties size=" //$NON-NLS-1$
						+ generalProperties.getSize() + " showing=" //$NON-NLS-1$
						+ generalProperties.isShowing());
			}
		});

		ComboBoxPopupSafeguards.installDeferredFlatComboPopupOnTreeLater(this);

		this.setLayout(new BorderLayout());

		this.add(generalPanel, BorderLayout.CENTER);

		// pack();

		// setLocationRelativeTo(getOwner());

		// setSize(new Dimension(1024, 768));

	}

	private JPanel wrapGeneralPropertiesColumn(FormPanel inner) {
		JPanel wrap = new JPanel(new BorderLayout(0, 0));
		wrap.add(inner, BorderLayout.CENTER);
		return wrap;
	}

	/**
	 * Rebuilds spinner editors and re-applies model values so text fields show
	 * correctly after layout (JDK / FlatLaf).
	 */
	private void refreshSpinnerEditorsAfterLayout() {
		resyncSpinnerWithNumberEditor(spinneraxepremierepiste, "0.000"); //$NON-NLS-1$
		resyncSpinnerWithNumberEditor(spinnerentreaxepiste, "0.000"); //$NON-NLS-1$
		resyncSpinnerWithNumberEditor(spinnerlargeurpiste, "0.000"); //$NON-NLS-1$
		resyncSpinnerPlain(spinnerlargeurcarton);
		resyncSpinnerPlain(spinnernbpistes);
		resyncSpinnerPlain(spinnerspeed);
	}

	private void resyncSpinnerWithNumberEditor(JSpinner spinner, String pattern) {
		Object v = spinner.getValue();
		spinner.setEditor(new JSpinner.NumberEditor(spinner, pattern));
		spinner.setValue(v);
		finishSpinnerResync(spinner);
	}

	private void resyncSpinnerPlain(JSpinner spinner) {
		Object v = spinner.getValue();
		spinner.setValue(v);
		finishSpinnerResync(spinner);
	}

	private void finishSpinnerResync(JSpinner spinner) {
		try {
			spinner.commitEdit();
		} catch (java.text.ParseException e) {
			// keep current model value
		}
		if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
			JSpinner.DefaultEditor de = (JSpinner.DefaultEditor) spinner
					.getEditor();
			JTextField tf = de.getTextField();
			tf.setColumns(Math.max(8, tf.getColumns()));
		}
		Dimension pref = spinner.getPreferredSize();
		spinner.setMinimumSize(
				new Dimension(Math.max(96, pref.width), pref.height));
		spinner.revalidate();
		spinner.repaint();
	}

	private void revalidateScaleViewAndSyncZoomSlider(int min, int max) {
		scalecomponent.revalidate();
		scalecomponentscrollpane.revalidate();
		scalecomponentscrollpane.repaint();
		syncZoomSliderFromScale(min, max);
	}

	private void syncZoomSliderFromScale(int min, int max) {
		if (zoomSlider == null) {
			return;
		}
		int v = (int) Math.round(scalecomponent.getScale() * 10);
		if (v < min) {
			v = min;
		}
		if (v > max) {
			v = max;
		}
		if (zoomSlider.getValue() != v) {
			zoomSlider.setValue(v);
		}
		zoomSlider.setToolTipText(MessageFormat.format(
				Messages.getString("JScaleEditorPanel.zoomFactorTooltip"), //$NON-NLS-1$
				scalecomponent.getScale()));
	}

	protected void insertFollowingNotes() {

		int selectedTrackDef = scalecomponent.getSelectedTrackDef();
		if (selectedTrackDef == -1) {
			JMessageBox.showMessage(owner,
					Messages.getString("JScaleEditorPanel.1013")); //$NON-NLS-1$
			return;
		}

		AbstractTrackDef trackDef = scalecomponent
				.getTrackDef(selectedTrackDef);
		if (!(trackDef instanceof NoteDef)) {
			JMessageBox.showMessage(owner, Messages.getString("JScaleEditorPanel.1014")); //$NON-NLS-1$
			return;
		}

		NoteDef nd = (NoteDef) trackDef;

		// ask for the number of following notes

		int evaluatedTrackLeft = scalecomponent.getTrackDefCount() - 1
				- selectedTrackDef;

		int numberToFollow = -1;
		while (numberToFollow < 0) {

			String r = JOptionPane.showInputDialog(Messages.getString("JScaleEditorPanel.1015"), //$NON-NLS-1$
					evaluatedTrackLeft);
			if (r == null || "".equals(r)) //$NON-NLS-1$
				return;

			try {
				numberToFollow = Integer.parseInt(r);
			} catch (NumberFormatException e) {
				JMessageBox.showMessage(owner, Messages.getString("JScaleEditorPanel.1017")); //$NON-NLS-1$
			}

		}

		assert numberToFollow > 0;

		for (int i = 0; i < numberToFollow; i++) {
			int index = selectedTrackDef + 1 + i;
			if (index > 0 && index < scalecomponent.getTrackDefCount()) {
				int newmidinote = nd.getMidiNote() + 1 + i;
				if (newmidinote > 127)
					continue;
				NoteDef n = new NoteDef(newmidinote, nd.getRegisterSetName());
				scalecomponent.changePisteDef(index, n);
			}
		}

	}

	/**
	 * empty the current scale
	 */
	public void newScale() {
		this.scalecomponent.newScale();
	}

	public Scale getScale() throws Exception {
		return scalecomponent.constructScale();
	}

	public String checkScale() {
		return scalecomponent.checkScale();
	}

	@SuppressWarnings(value = "unused")//$NON-NLS-1$
	private void rememberDefaultFolderForFile(File selectedFile) {
		File new_default_folder = selectedFile.getParentFile();
		prefs.setLastGammeFolder(new_default_folder);
	}

	@SuppressWarnings(value = "unused")//$NON-NLS-1$
	private void setupDefaultFolderForChooser(APrintFileChooser choose) {
		File default_folder = prefs.getLastGammeFolder();
		if (default_folder != null && default_folder.exists()
				&& default_folder.isDirectory())
			choose.setCurrentDirectory(default_folder);
	}

	/**
	 * Chargement d'une gamme
	 * 
	 * @param g
	 */
	public void loadScale(Scale g) {

		if (g == null) {
			scalecomponent.newScale();
			try {
				g = scalecomponent.constructScale();
			} catch (Exception ex) {
				logger.error("error in constructing defaut empty scale ", ex); //$NON-NLS-1$
			}
		} else {
			scalecomponent.loadScale(g);
		}

		scalename.setText(g.getName());
		spinneraxepremierepiste.setValue(new Double(g.getFirstTrackAxis()));
		spinnerlargeurcarton.setValue(new Double(g.getWidth()));
		spinnernbpistes.setValue(new Integer(g.getTrackNb()));
		spinnerentreaxepiste.setValue(new Double(g.getIntertrackHeight()));
		spinnerlargeurpiste.setValue(new Double(g.getTrackWidth()));
		spinnerspeed.setValue(new Double(g.getSpeed()));

		registrecomponent.setRegisterSetList(g.getPipeStopGroupList());

		constraintPanel.setConstraintList(g.getConstraints());

		infostextarea.setText((g.getInformations() == null ? "" : g //$NON-NLS-1$
				.getInformations()));

		combostate.setSelectedItem(ReferencedState.fromInternalValue(g
				.getState()));

		for (int i = 0; i < bookType.getItemCount(); i++) {
			VirtualBookRenderingDisplay d = (VirtualBookRenderingDisplay) bookType
					.getItemAt(i);
			if (d != null && g.getRendering() != null) {
				if (d.getRendering().getName()
						.equalsIgnoreCase(g.getRendering().getName())) {
					bookType.setSelectedItem(d);
					break;
				}
			}
		}

		contact.setText(g.getContact());

		preferredViewInverted.setSelected(g.isPreferredViewedInversed());

		propertyHashEditor.setHash(g.getAllProperties());

		SwingUtilities.invokeLater(() -> refreshSpinnerEditorsAfterLayout());
	}

	/**
	 * MAJ du composant de gamme à partir des autres composants
	 */
	private void updateGammeComponent() {

		scalecomponent.changeName(scalename.getText());
		scalecomponent
				.changePremierePiste(((SpinnerNumberModel) spinneraxepremierepiste
						.getModel()).getNumber().doubleValue());
		scalecomponent
				.changeLargeurCarton(((SpinnerNumberModel) spinnerlargeurcarton
						.getModel()).getNumber().doubleValue());
		scalecomponent.changeNbPiste(((SpinnerNumberModel) spinnernbpistes
				.getModel()).getNumber().intValue());
		scalecomponent
				.changeEntrePiste(((SpinnerNumberModel) spinnerentreaxepiste
						.getModel()).getNumber().doubleValue());
		scalecomponent
				.changeLargeurPiste(((SpinnerNumberModel) spinnerlargeurpiste
						.getModel()).getNumber().doubleValue());
		scalecomponent.changeVitesse(((SpinnerNumberModel) spinnerspeed
				.getModel()).getNumber().doubleValue());

		scalecomponent.changeRegisterSetList(registrecomponent
				.getRegisterSetList());
	}

}
