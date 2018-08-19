package org.barrelorgandiscovery.gui.ascale;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
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
import org.barrelorgandiscovery.gui.aprint.APrint;
import org.barrelorgandiscovery.gui.ascale.constraints.ConstraintListChangeListener;
import org.barrelorgandiscovery.gui.ascale.constraints.ConstraintPanel;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.ConstraintList;
import org.barrelorgandiscovery.scale.PipeStopGroupList;
import org.barrelorgandiscovery.scale.ReferencedState;
import org.barrelorgandiscovery.scale.Scale;


@Deprecated
public class OLDJScaleEditorPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8484645796626431335L;

	private static Logger logger = Logger.getLogger(OLDJScaleEditorPanel.class);

	private JPanel panelparameters;
	private JPanel panelparametrespistes;
	private JLabel label6;

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
	private JScrollPane scalecrollpane;
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
	private JPanel parametreregistres;

	
	private InstrumentPipeStopDescriptionComponent registrecomponent;

	private AbstractGlobalTrackDefComponent tabbededitors;

	private ConstraintPanel constraintPanel;

	private JCheckBox preferredViewInverted;

	private ScaleEditorPrefs prefs = null;

	private PropertyHashEditor propertyHashEditor;
	
	

	/**
	 * Constructeur
	 */
	public OLDJScaleEditorPanel(ScaleEditorPrefs prefs) {
		super();
		assert prefs != null;
		this.prefs = prefs;

		initComponents();
		updateGammeComponent();
	}

	/**
	 * Initialisation de composants
	 */
	private void initComponents() {

		panelparameters = new JPanel();

		panelparametrespistes = new JPanel();
		label6 = new JLabel();

		labellargeurcarton = new JLabel();
		spinnerlargeurcarton = new JSpinner(new SpinnerNumberModel(200.0, 0.0,
				500.0, 1.0));

		spinnerlargeurcarton.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				scalecomponent.changeLargeurCarton(((Number) s.getValue())
						.doubleValue());
			}
		});

		preferredViewInverted = new JCheckBox(Messages
				.getString("ScaleEditor.0")); //$NON-NLS-1$
		preferredViewInverted.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox jch = (JCheckBox) e.getSource();
				scalecomponent.changePreferredViewInverted(jch.isSelected());

			}
		});

		labelaxepremierepiste = new JLabel();
		spinneraxepremierepiste = new JSpinner(new SpinnerNumberModel(10.0,
				0.0, 100.0, 0.1));
		spinneraxepremierepiste.setEditor(new JSpinner.NumberEditor(
				spinneraxepremierepiste, "0.00")); //$NON-NLS-1$

		spinneraxepremierepiste.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				scalecomponent.changePremierePiste(((Number) s.getValue())
						.doubleValue());
			}
		});

		labelaxepiste = new JLabel();
		spinnerentreaxepiste = new JSpinner(new SpinnerNumberModel(5.0, 0.0,
				10.0, 0.1));
		spinnerentreaxepiste.setEditor(new JSpinner.NumberEditor(
				spinnerentreaxepiste, "0.00")); //$NON-NLS-1$

		spinnerentreaxepiste.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				scalecomponent.changeEntrePiste(((Number) s.getValue())
						.doubleValue());
			}
		});

		labellargeurpiste = new JLabel();
		spinnerlargeurpiste = new JSpinner(new SpinnerNumberModel(5.0, 0.0,
				10.0, 0.1));
		spinnerlargeurpiste.setEditor(new JSpinner.NumberEditor(
				spinnerlargeurpiste, "0.00")); //$NON-NLS-1$

		spinnerlargeurpiste.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				scalecomponent.changeLargeurPiste(((Number) s.getValue())
						.doubleValue());
			}
		});

		labelvitesse = new JLabel();

		spinnerspeed = new JSpinner(
				new SpinnerNumberModel(60.0, 0.0, 1000.0, 1));
		spinnerspeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				scalecomponent.changeVitesse(((Number) s.getValue())
						.doubleValue());
			}
		});

		scalename = new JTextField();
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

		labelcontact = new JLabel();
		labelcontact.setText(Messages.getString("GammeEditor.44")); //$NON-NLS-1$

		contact = new JTextField();
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

		labelstate = new JLabel();
		labelstate.setText(Messages.getString("GammeEditor.45")); //$NON-NLS-1$

		combostate = new JComboBox();
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

		labelnbpistes = new JLabel();
		labelnbpistes.setText(Messages.getString("GammeEditor.2")); //$NON-NLS-1$

		spinnernbpistes = new JSpinner(new SpinnerNumberModel(30, 0, 300, 1));
		spinnernbpistes.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				scalecomponent
						.changeNbPiste(((Number) s.getValue()).intValue());
			}
		});

		parametreregistres = new JPanel();
		parametreregistres.setBorder(new TitledBorder(Messages
				.getString("GammeEditor.3"))); //$NON-NLS-1$

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

		JTabbedPane tabbedPaneScaleProperties = new JTabbedPane();

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

		tabbedPaneScaleProperties.addTab(
				Messages.getString("ScaleEditor.4"), registrecomponent); //$NON-NLS-1$
		tabbedPaneScaleProperties.addTab(
				Messages.getString("ScaleEditor.5"), constraintPanel); //$NON-NLS-1$
		propertyHashEditor = new PropertyHashEditor();
		propertyHashEditor
				.addPropertyHashEditorChangedListener(new PropertyHashEditorChangedListener() {
					public void hashChanged(HashMap<String, String> newHash) {
						scalecomponent.changeProperties(newHash);
					}
				});
		tabbedPaneScaleProperties.addTab("Properties", propertyHashEditor);

		// hello world !!! a bath to make to the last child, Yann !!! :-)

		tabbededitors
				.setTrackDefComponentListener(new TrackDefComponentListener() {

					public void trackDefChanged(AbstractTrackDef td) {
						int selected = scalecomponent.getSelectedTrackDef();
						if (selected != -1)
							scalecomponent.changePisteDef(selected, td);

					}
				});

		// ======== this ========
		Container contentPane = this;
		contentPane.setLayout(new GridBagLayout());
		((GridBagLayout) contentPane.getLayout()).columnWidths = new int[] { 0,
				0, 0 };
		((GridBagLayout) contentPane.getLayout()).rowHeights = new int[] { 0,
				0, 0 };
		((GridBagLayout) contentPane.getLayout()).columnWeights = new double[] {
				1.0, 1.0, 1.0E-4 };
		((GridBagLayout) contentPane.getLayout()).rowWeights = new double[] {
				0.0, 0.0, 0.5, 0.5 };

		// ==== Scale Viewer ====

		scalecrollpane = new JScrollPane(scalecomponent,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scalecrollpane.setAutoscrolls(true);

		final double scalefactor = 1.3;

		JToolBar tb = new JToolBar("tools"); //$NON-NLS-1$

		final int SLIDER_MAX_VALUE = 30;
		final int SLIDER_MIN_VALUE = 2;

		final JSlider slider = new JSlider(JSlider.HORIZONTAL,
				SLIDER_MIN_VALUE, SLIDER_MAX_VALUE, 10);

		JButton zoomplus = new JButton(Messages.getString("GammeEditor.47")); //$NON-NLS-1$
		zoomplus
				.setIcon(new ImageIcon(APrint.class.getResource("viewmag.png"))); //$NON-NLS-1$
		zoomplus.setToolTipText(Messages.getString("GammeEditor.49")); //$NON-NLS-1$
		zoomplus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scalecomponent
						.setScale(scalecomponent.getScale() * scalefactor);
				scalecomponent.revalidate();
				scalecrollpane.revalidate();
				scalecrollpane.repaint();

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
				.setIcon(new ImageIcon(APrint.class.getResource("viewmag.png"))); //$NON-NLS-1$
		zoommoins.setToolTipText(Messages.getString("GammeEditor.52")); //$NON-NLS-1$
		zoommoins.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				scalecomponent
						.setScale(scalecomponent.getScale() / scalefactor);
				scalecomponent.revalidate();
				scalecrollpane.revalidate();
				scalecrollpane.repaint();

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
				scalecrollpane.revalidate();
				scalecrollpane.repaint();
			}
		});

		tb.add(slider);

		JPanel gammepanel = new JPanel(new BorderLayout());
		gammepanel.add(scalecrollpane, BorderLayout.CENTER);
		gammepanel.add(tb, BorderLayout.NORTH);

		contentPane.add(gammepanel, new GridBagConstraints(0, 0, 1, 4, 10.0,
				10.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

		// ======== panel1 ========
		{
			panelparameters.setBorder(new TitledBorder(Messages
					.getString("GammeEditor.17"))); //$NON-NLS-1$
			panelparameters.setLayout(new GridBagLayout());
			((GridBagLayout) panelparameters.getLayout()).columnWidths = new int[] {
					0, 0 };
			((GridBagLayout) panelparameters.getLayout()).rowHeights = new int[] {
					0, 0, 0, 0 };
			((GridBagLayout) panelparameters.getLayout()).columnWeights = new double[] {
					1.0, 1.0E-4 };
			((GridBagLayout) panelparameters.getLayout()).rowWeights = new double[] {
					0.0, 0.0, 0.0, 1.0E-4 };

			// ======== panel2 ========
			{
				panelparametrespistes.setLayout(new GridBagLayout());
				((GridBagLayout) panelparametrespistes.getLayout()).columnWidths = new int[] {
						0, 118, 0 };
				((GridBagLayout) panelparametrespistes.getLayout()).rowHeights = new int[] {
						0, 0, 0, 0, 0, 0, 0, 0 };
				((GridBagLayout) panelparametrespistes.getLayout()).columnWeights = new double[] {
						0.0, 0.0, 1.0E-4 };
				((GridBagLayout) panelparametrespistes.getLayout()).rowWeights = new double[] {
						0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4 };

				// ---- label6 ----
				label6.setText(Messages.getString("GammeEditor.18")); //$NON-NLS-1$
				panelparametrespistes.add(label6, new GridBagConstraints(0, 0,
						1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
				panelparametrespistes.add(scalename, new GridBagConstraints(1,
						0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

				// nb pistes

				panelparametrespistes.add(labelnbpistes,
						new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER,
								GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));
				panelparametrespistes.add(spinnernbpistes,
						new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER,
								GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

				// ---- label2 ----
				labellargeurcarton
						.setText(Messages.getString("GammeEditor.19")); //$NON-NLS-1$
				panelparametrespistes.add(labellargeurcarton,
						new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER,
								GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));
				panelparametrespistes.add(spinnerlargeurcarton,
						new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER,
								GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

				// ---- label3 ----
				labelaxepremierepiste.setText(Messages
						.getString("GammeEditor.20")); //$NON-NLS-1$
				panelparametrespistes.add(labelaxepremierepiste,
						new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER,
								GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));
				panelparametrespistes.add(spinneraxepremierepiste,
						new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER,
								GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

				// ---- label4 ----
				labelaxepiste.setText(Messages.getString("GammeEditor.21")); //$NON-NLS-1$
				panelparametrespistes.add(labelaxepiste,
						new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER,
								GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));
				panelparametrespistes.add(spinnerentreaxepiste,
						new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER,
								GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

				// ---- label7 ----
				labellargeurpiste.setText(Messages.getString("GammeEditor.22")); //$NON-NLS-1$
				panelparametrespistes.add(labellargeurpiste,
						new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER,
								GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));
				panelparametrespistes.add(spinnerlargeurpiste,
						new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER,
								GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

				// ---- label5 ----
				labelvitesse.setText(Messages.getString("GammeEditor.23")); //$NON-NLS-1$
				panelparametrespistes.add(labelvitesse, new GridBagConstraints(
						0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
				panelparametrespistes.add(spinnerspeed, new GridBagConstraints(
						1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

				panelparametrespistes.add(preferredViewInverted,
						new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER,
								GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 0), 0, 0));

				// --- label etat
				panelparametrespistes.add(labelstate, new GridBagConstraints(0,
						8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
				panelparametrespistes.add(combostate, new GridBagConstraints(1,
						8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

				// label contact

				panelparametrespistes.add(labelcontact, new GridBagConstraints(
						0, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
				panelparametrespistes.add(contact, new GridBagConstraints(1, 9,
						1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			}

			panelparameters.add(panelparametrespistes, new GridBagConstraints(
					0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
		}

		contentPane.add(panelparameters, new GridBagConstraints(1, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		parametreregistres.setLayout(new BorderLayout());

		tabbedPaneScaleProperties.setMinimumSize(new Dimension(50, 100));
		parametreregistres.add(tabbedPaneScaleProperties, BorderLayout.CENTER);
		parametreregistres.invalidate();

		contentPane.add(parametreregistres, new GridBagConstraints(1, 1, 1, 2,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		tabbededitors.setBorder(new TitledBorder(Messages
				.getString("GammeEditor.24"))); //$NON-NLS-1$

		gammepanel.add(tabbededitors, BorderLayout.SOUTH);

		infostextarea = new JTextArea();
		infostextarea.setBorder(new TitledBorder(Messages
				.getString("GammeEditor.53"))); //$NON-NLS-1$
		infostextarea.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				scalecomponent.changeInfos(infostextarea.getText());
			}
		});
		contentPane.add(infostextarea, new GridBagConstraints(1, 3, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		// pack();

		// setLocationRelativeTo(getOwner());

		setSize(new Dimension(1024, 768));

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

	private void rememberDefaultFolderForFile(File selectedFile) {
		File new_default_folder = selectedFile.getParentFile();
		prefs.setLastGammeFolder(new_default_folder);
	}

	private void setupDefaultFolderForChooser(JFileChooser choose) {
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
