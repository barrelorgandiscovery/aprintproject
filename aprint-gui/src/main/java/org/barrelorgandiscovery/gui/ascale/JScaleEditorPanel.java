package org.barrelorgandiscovery.gui.ascale;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashMap;

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
import javax.swing.JToolBar;
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

public class JScaleEditorPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8484645796626431335L;

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
	private FormPanel generalProperties = null;
	private JTabbedPane tab = null;

	private FormPanel scalePanel = null;

	private JComboBox bookType;

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

		Component oldComp = leftPanel.getComponentByName("generalProperties"); //$NON-NLS-1$

		oldComp.getParent().add(generalProperties);

		// leftPanel.getFormAccessor().replaceBean(oldComp, generalProperties);

		// generalProperties =
		// (FormPanel)generalProperties.getComponentByName("generalProperties");

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

		JLabel labelPreferredViewInverted = (JLabel) generalProperties
				.getComponentByName("labelInvertReference"); //$NON-NLS-1$
		labelPreferredViewInverted.setText(Messages.getString("ScaleEditor.0")); //$NON-NLS-1$
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
				100.0, 0.1));

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
		spinnerentreaxepiste.setModel(new SpinnerNumberModel(5.0, 0.0, 30.0,
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

		JLabel labelScaleName = (JLabel) generalProperties
				.getComponentByName("labelScaleDescription"); //$NON-NLS-1$
		labelScaleName.setText(Messages.getString("JScaleEditorPanel.18")); //$NON-NLS-1$

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

		JLabel labelBookType = (JLabel) generalProperties
				.getComponentByName("labelBookType"); //$NON-NLS-1$
		labelBookType.setText(Messages.getString("JScaleEditorPanel.28")); //$NON-NLS-1$

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

		// ==== Scale Viewer ====

		scalecomponentscrollpane = new JScrollPane(scalecomponent,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scalecomponentscrollpane.setAutoscrolls(true);

		final double scalefactor = 1.3;

		JToolBar tb = new JToolBar("tools"); //$NON-NLS-1$

		final int SLIDER_MAX_VALUE = 30;
		final int SLIDER_MIN_VALUE = 2;

		final JSlider slider = new JSlider(JSlider.HORIZONTAL,
				SLIDER_MIN_VALUE, SLIDER_MAX_VALUE, 10);

		JButton zoomplus = new JButton(Messages.getString("GammeEditor.47")); //$NON-NLS-1$
		zoomplus.setIcon(new ImageIcon(APrintNG.class.getResource("viewmag.png"))); //$NON-NLS-1$
		zoomplus.setToolTipText(Messages.getString("GammeEditor.49")); //$NON-NLS-1$
		zoomplus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scalecomponent.setScale(scalecomponent.getScale() * scalefactor);
				scalecomponent.revalidate();
				scalecomponentscrollpane.revalidate();
				scalecomponentscrollpane.repaint();

				int sliderpos = (int) (scalecomponent.getScale() * 10);
				if (sliderpos < SLIDER_MIN_VALUE)
					sliderpos = SLIDER_MIN_VALUE;

				if (sliderpos > SLIDER_MAX_VALUE)
					sliderpos = SLIDER_MAX_VALUE;
				slider.setValue(sliderpos);
				slider.repaint();
			}
		});
		tb.add(zoomplus);
		JButton zoommoins = new JButton(Messages.getString("GammeEditor.50")); //$NON-NLS-1$
		zoommoins
				.setIcon(new ImageIcon(APrintNG.class.getResource("viewmag.png"))); //$NON-NLS-1$
		zoommoins.setToolTipText(Messages.getString("GammeEditor.52")); //$NON-NLS-1$
		zoommoins.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				scalecomponent.setScale(scalecomponent.getScale() / scalefactor);
				scalecomponent.revalidate();
				scalecomponentscrollpane.revalidate();
				scalecomponentscrollpane.repaint();

				int sliderpos = (int) (scalecomponent.getScale() * 10);
				if (sliderpos < SLIDER_MIN_VALUE)
					sliderpos = SLIDER_MIN_VALUE;

				if (sliderpos > SLIDER_MAX_VALUE)
					sliderpos = SLIDER_MAX_VALUE;
				slider.setValue(sliderpos);
				slider.repaint();
			}
		});
		tb.add(zoommoins);

		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider) e.getSource();
				int value = slider.getValue();
				scalecomponent.setScale((1.0 * value) / 10.0);

				scalecomponent.revalidate();
				scalecomponentscrollpane.revalidate();
				scalecomponentscrollpane.repaint();
			}
		});

		tb.add(slider);

		// Toolbar for modifying the scale
		JToolBar modifierTb = new JToolBar();

		JButton addTrack = new JButton(
				Messages.getString("JScaleEditorPanel.10000")); //$NON-NLS-1$
		addTrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int selectedTrackDef = scalecomponent.getSelectedTrackDef();
				if (selectedTrackDef != -1) {
					scalecomponent.shiftTracksDown(selectedTrackDef);
					scalecomponent.setSelectedTrackDef(selectedTrackDef); // refresh
				}

			}
		});
		modifierTb.add(addTrack);
		JButton removeTrack = new JButton(
				Messages.getString("JScaleEditorPanel.10001")); //$NON-NLS-1$

		removeTrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedTrackDef = scalecomponent.getSelectedTrackDef();
				if (selectedTrackDef != -1) {
					scalecomponent.shiftTracksUp(selectedTrackDef);
					scalecomponent.setSelectedTrackDef(selectedTrackDef); // refresh
				}
			}
		});
		modifierTb.add(removeTrack);

		JButton insererNotes = new JButton(Messages.getString("JScaleEditorPanel.1010")); //$NON-NLS-1$
		insererNotes.setToolTipText(Messages.getString("JScaleEditorPanel.1011")); //$NON-NLS-1$
		insererNotes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					insertFollowingNotes();

				} catch (Throwable t) {
					logger.error("error in inserting notes " + t.getMessage(), //$NON-NLS-1$
							t);
				}

			}
		});

		modifierTb.add(insererNotes);

		JPanel toolbarPanel = new JPanel();
		toolbarPanel.setLayout(new BorderLayout());
		toolbarPanel.add(tb, BorderLayout.NORTH);
		toolbarPanel.add(modifierTb, BorderLayout.SOUTH);

		JPanel panelWithToolbarsAndScaleComponent = new JPanel(
				new BorderLayout());
		panelWithToolbarsAndScaleComponent.add(scalecomponentscrollpane,
				BorderLayout.CENTER);
		panelWithToolbarsAndScaleComponent
				.add(toolbarPanel, BorderLayout.NORTH);

		this.scalePanel.getFormAccessor().replaceBean(
				scalePanel.getComponentByName("scalecomponent"), //$NON-NLS-1$
				panelWithToolbarsAndScaleComponent);

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

		JSplitPane globalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				scalePanel, leftPanel);

		globalSplit.setResizeWeight(1.0);

		generalPanel.add(globalSplit, BorderLayout.CENTER);

		tabbededitors.setBorder(new TitledBorder(Messages
				.getString("GammeEditor.24"))); //$NON-NLS-1$

		panelWithToolbarsAndScaleComponent.add(tabbededitors,
				BorderLayout.SOUTH);

		infostextarea = (JTextArea) generalProperties
				.getComponentByName("generalInformationsNotes"); //$NON-NLS-1$

		JLabel labelInformationNotes = (JLabel) generalProperties
				.getComponentByName("labelGeneralInformationsNotes"); //$NON-NLS-1$
		labelInformationNotes.setText(Messages.getString("GammeEditor.53")); //$NON-NLS-1$

		infostextarea.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				scalecomponent.changeInfos(infostextarea.getText());
			}
		});

		this.setLayout(new BorderLayout());

		this.add(generalPanel, BorderLayout.CENTER);

		// pack();

		// setLocationRelativeTo(getOwner());

		// setSize(new Dimension(1024, 768));

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
