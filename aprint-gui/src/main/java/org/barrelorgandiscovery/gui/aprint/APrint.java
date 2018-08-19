package org.barrelorgandiscovery.gui.aprint;

import groovy.ui.GroovyMain;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.beans.BeanInfo;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Soundbank;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManager;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository;
import org.barrelorgandiscovery.extensions.ExtensionManager;
import org.barrelorgandiscovery.extensions.ExtensionPointProvider;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.gaerepositoryclient.GAESynchronizedRepository2;
import org.barrelorgandiscovery.gaerepositoryclient.SynchronizationFeedBack;
import org.barrelorgandiscovery.gaerepositoryclient.synchroreport.SynchroElement;
import org.barrelorgandiscovery.gaerepositoryclient.synchroreport.SynchronizationReport;
import org.barrelorgandiscovery.gui.CancelTracker;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.gui.ProgressIndicator;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.aedit.PipeSetGroupLayer;
import org.barrelorgandiscovery.gui.aedit.TimeBookLayer;
import org.barrelorgandiscovery.gui.ainstrument.JInstrumentEditor;
import org.barrelorgandiscovery.gui.aprint.extensions.CustomImporterParameters;
import org.barrelorgandiscovery.gui.aprint.extensions.ImporterParameters;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.HelpMenuItemsExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.ImportersExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InformCurrentVirtualBookExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InformRepositoryExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InitExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.LayersExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.OptionMenuItemsExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.ToolbarAddExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.VirtualBookToolbarButtonsExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.VisibilityLayerButtonsExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.instrumentchoice.IInstrumentChoice;
import org.barrelorgandiscovery.gui.aprint.instrumentchoice.IInstrumentChoiceListener;
import org.barrelorgandiscovery.gui.aprint.instrumentchoice.JCoverFlowInstrumentChoice;
import org.barrelorgandiscovery.gui.aprintng.IAPrintWait;
import org.barrelorgandiscovery.gui.ascale.ScaleEditor;
import org.barrelorgandiscovery.gui.ascale.ScaleEditorPrefs;
import org.barrelorgandiscovery.gui.ascale.ScalePrintDocument;
import org.barrelorgandiscovery.gui.atrace.ATrace;
import org.barrelorgandiscovery.gui.atrace.OptimizerResult;
import org.barrelorgandiscovery.gui.gaerepositoryclient.GaeRepositoryClientConnection;
import org.barrelorgandiscovery.gui.issues.IssueSelectionListener;
import org.barrelorgandiscovery.gui.issues.JIssuePresenter;
import org.barrelorgandiscovery.gui.script.groovy.APrintGroovyConsole;
import org.barrelorgandiscovery.issues.AbstractIssue;
import org.barrelorgandiscovery.issues.AbstractSpatialIssue;
import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.issues.IssueLayer;
import org.barrelorgandiscovery.listeningconverter.EcouteConverter;
import org.barrelorgandiscovery.messages.JTranslator;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.movies.MovieConverter;
import org.barrelorgandiscovery.movies.MovieConverterParameters;
import org.barrelorgandiscovery.playsubsystem.GervillPlaySubSystem;
import org.barrelorgandiscovery.playsubsystem.IPlaySubSystemFeedBack;
import org.barrelorgandiscovery.playsubsystem.MidiDevicePlaySubSystem;
import org.barrelorgandiscovery.playsubsystem.NeedInstrument;
import org.barrelorgandiscovery.playsubsystem.PlaySubSystem;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Collection;
import org.barrelorgandiscovery.repository.Repository2Factory;
import org.barrelorgandiscovery.repository.RepositoryAdapter;
import org.barrelorgandiscovery.repository.RepositoryChangedListener;
import org.barrelorgandiscovery.repository.RepositoryException;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.ScaleManager;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.FileNameExtensionFilter;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.MP3Tools;
import org.barrelorgandiscovery.tools.OGGTools;
import org.barrelorgandiscovery.tools.SwingUtils;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.tracetools.ga.GeneticOptimizer;
import org.barrelorgandiscovery.ui.animation.InfiniteProgressPanel;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.Region;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.checker.Checker;
import org.barrelorgandiscovery.virtualbook.checker.CheckerFactory;
import org.barrelorgandiscovery.virtualbook.checker.OverlappingHole;
import org.barrelorgandiscovery.virtualbook.checker.TranspositionIssueConverter;
import org.barrelorgandiscovery.virtualbook.io.MidiIO;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractMidiImporter;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractTransformation;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractTransposeVirtualBook;
import org.barrelorgandiscovery.virtualbook.transformation.TransformationManager;
import org.barrelorgandiscovery.virtualbook.transformation.TranspositionResult;
import org.barrelorgandiscovery.virtualbook.transformation.Transpositor;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiConversionResult;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFileIO;

import com.l2fprod.common.demo.BeanBinder;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.swing.JDirectoryChooser;

public class APrint extends javax.swing.JFrame implements ActionListener,
		IAPrintWait {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5504312655158119463L;

	/**
	 * Loggeur
	 */
	private static final Logger logger = Logger.getLogger(APrint.class);

	// ///////////////////////////////////////////////////////////////////////////
	// Données interne du formulaire

	/**
	 * Mémorisation du nom du fichier midi (pour l'impression)
	 */
	private File midifile = null;
	/**
	 * Carton virtuel lu pour impression
	 */
	private VirtualBook readCarton = null;

	/**
	 * Carton transposé pour l'instrument donné ...
	 */
	private VirtualBook transposedCarton = null;

	/**
	 * Repository ...
	 */
	private RepositoryAdapter repository = null;

	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * Gestionnaire de gamme
	 */
	private ScaleManager gm = null;

	/**
	 * Gestionnaire de transposition
	 */
	private TransformationManager tm = null;

	private AsyncJobsManager asyncJobsManager;

	/**
	 * sequencer utilisé pour jouer le morceau
	 */
	// private Sequencer sequencer = null;
	/**
	 * Executor pour l'éxécution des taches longues
	 */
	private Executor backgroundexecutor = Executors
			.newCachedThreadPool(new ThreadFactory() {
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r);
					t.setPriority(Thread.MIN_PRIORITY);
					return t;
				}
			});

	// private Thread runningpreview = null;

	// ///////////////////////////////////////////////////////////////////////////
	// Interface IHM

	private JPanel panelPrincipal = new JPanel();

	private InfiniteProgressPanel infiniteprogresspanel = new InfiniteProgressPanel(
			null, 20, 0.5f, 0.5f);

	private JPanel northPanel = new JPanel();

	private JButton chargementmidi = new JButton(Messages.getString("APrint.1")); //$NON-NLS-1$

	private JPanel panelchoixfichier = new JPanel();

	private JLabel labelnomfichier = new JLabel(Messages.getString("APrint.2")); //$NON-NLS-1$

	private JLabel nomfichier = new JLabel(Messages.getString("APrint.43")); //$NON-NLS-1$

	private JPanel gammeTranspositionInstrumentPanel = new JPanel();

	private JComboBox listetransposition = new JComboBox();

	private IInstrumentChoice instrumentChoice;

	private JTextField searchTextField = new JTextField(20);
	private JButton searchButton = new JButton(Messages.getString("APrint.292")); //$NON-NLS-1$
	private JButton resetFilterButton = new JButton(
			Messages.getString("APrint.293")); //$NON-NLS-1$

	private JIssuePresenter issuePresenter;

	/**
	 * Panneau contenant le choix de la transposition
	 */
	private JPanel choixtransposition;

	private JButton jouer;

	private JButton rewind;

	// private JPanel buttonPanel = new JPanel();

	private JButton imprimer = new JButton();

	private JButton exportAsWav = new JButton();

	private JButton exportAsMp3 = new JButton();

	private JButton exportAsOgg = new JButton();

	private JButton exportAsMidi = new JButton();

	private JButton exportAsMovie = new JButton();

	private JButton preview = new JButton();

	// private JButton tracer = new JButton();

	private JMenu popupmenuImprimerGamme = new JMenu(
			Messages.getString("APrint.105")); //$NON-NLS-1$

	private APrintProperties aprintproperties;

	private JPanel pianorollpanel = new JPanel();

	private JVirtualBookScrollableComponent pianoroll = new JVirtualBookScrollableComponent();

	/**
	 * Layer contenant les problèmes de transposition
	 */
	private IssueLayer il = new IssueLayer();

	private TimeBookLayer tbl = new TimeBookLayer();

	private PipeSetGroupLayer psgl = new PipeSetGroupLayer();

	private ExtensionManager em = null;

	public APrint(APrintProperties properties) throws Exception,
			RepositoryException {
		super();

		if (properties == null)
			throw new IllegalArgumentException();

		aprintproperties = properties;

		// loading the extensions ... from class loader

		File extensionfolder = aprintproperties.getExtensionFolder();
		// jar
		// class
		// loader
		// ...

		if (extensionfolder != null) {

			em = new ExtensionManager(extensionfolder);

			em.deleteInvalidatedExtensions();

			exts = em.getExtensions();
			if (logger.isInfoEnabled()) {
				for (int i = 0; i < exts.length; i++) {
					logger.info("Extension " + exts[i].getName() + " loaded"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

		}

		InitExtensionPoint[] allInitPoints = ExtensionPointProvider
				.getAllPoints(InitExtensionPoint.class, exts);
		for (int i = 0; i < allInitPoints.length; i++) {
			InitExtensionPoint init = allInitPoints[i];
			init.init(this);
		}

		File rep = aprintproperties.getGammeAndTranlation();

		setTitle(Messages.getString("APrint.6") + " - V " + getVersion()); //$NON-NLS-1$ //$NON-NLS-2$
		setIconImage(getAPrintApplicationIcon()); //$NON-NLS-1$

		// Chargement des objets nécessaires

		defineNewGammeAndTranspositionFolder(rep);

		logger.debug("end of reading scales ad instrument definition"); //$NON-NLS-1$

		initHelpBroker();

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setName("aprint main window"); //$NON-NLS-1$

		chargementmidi.setName("boutonchargerfichiermidi"); //$NON-NLS-1$
		chargementmidi.setActionCommand("CHARGEMENTMIDI"); //$NON-NLS-1$
		chargementmidi.addActionListener(this);
		chargementmidi.setIcon(new ImageIcon(getClass().getResource(
				"fileopen.png"), Messages.getString("APrint.78"))); //$NON-NLS-1$ //$NON-NLS-2$
		chargementmidi.setToolTipText(Messages.getString("APrint.127")); //$NON-NLS-1$

		// chargementPanel.setBorder(new TitledBorder(Messages
		// .getString("APrint.8"))); //$NON-NLS-1$
		//
		// chargementPanel.add(labelchargementmidi);
		// chargementPanel.add(chargementmidi);

		// JLabel labelchargementmidi = new
		// JLabel(Messages.getString("APrint.0")); //$NON-NLS-1$

		TitledBorder fileChoosetitledBorder = new TitledBorder(
				Messages.getString("APrint.9")); //$NON-NLS-1$
		// fileChoosetitledBorder.setTitleJustification(TitledBorder.CENTER);

		panelchoixfichier.setBorder(fileChoosetitledBorder); //$NON-NLS-1$

		panelchoixfichier.add(labelnomfichier);
		panelchoixfichier.add(nomfichier);
		panelchoixfichier.add(chargementmidi);

		// ne montre pas les erreurs par défaut
		il.setVisible(aprintproperties.isErrorsVisible());

		tbl.setVisible(false);

		psgl.setVisible(false);

		// /////////////////////////////////////////////////////////////////////////////
		// Panneau de choix de gamme, transposition et instruments

		JPanel pInstrument = new JPanel();
		pInstrument.setLayout(new BorderLayout());

		// Panneau de choix de l'instrument ..
		// instrumentChoice = new JInstrumentChoice(repository.getRepository2(),
		// new IInstrumentChoiceListener() {
		// public void instrumentChanged(
		// fr.freydierepatrice.instrument.Instrument newInstrument) {
		// APrint.this.instrumentChanged();
		// }
		// });

		JCoverFlowInstrumentChoice cfic = new JCoverFlowInstrumentChoice(
				repository.getRepository2(), new IInstrumentChoiceListener() {
					public void instrumentChanged(
							org.barrelorgandiscovery.instrument.Instrument newInstrument) {
						APrint.this.instrumentChanged();
					}
				});
		instrumentChoice = cfic;

		cfic.setPreferredSize(new Dimension(700, 500));

		// JLabel lblInstrument = new JLabel(Messages.getString("APrint.13"));
		// //$NON-NLS-1$
		// pInstrument.add(lblInstrument);

		pInstrument.add((JComponent) instrumentChoice, BorderLayout.CENTER);

		JPanel searchPanel = new JPanel();

		searchPanel.add(searchTextField);

		final ActionListener searchListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String filter = searchTextField.getText();
					logger.debug("search criteria :" + filter); //$NON-NLS-1$
					instrumentChoice.setInstrumentFilter(filter);
					instrumentChoice.reloadInstruments();
				} catch (Exception ex) {
					logger.error("error in defining the instrument filter : " //$NON-NLS-1$
							+ ex.getMessage(), ex);
				}
			}
		};

		searchTextField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchListener.actionPerformed(null);
				}
			}

		});

		resetFilterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String filter = searchTextField.getText();
					logger.debug("old search criteria :" + filter); //$NON-NLS-1$
					instrumentChoice.setInstrumentFilter(null);
					instrumentChoice.reloadInstruments();
				} catch (Exception ex) {
					logger.error("error in resetting the instrument filter : " //$NON-NLS-1$
							+ ex.getMessage(), ex);
				}

			}
		});

		searchPanel.add(searchButton);
		searchPanel.add(resetFilterButton);

		pInstrument.add(searchPanel, BorderLayout.SOUTH);
		searchButton.addActionListener(searchListener);

		// transpositions

		choixtransposition = new JPanel();

		choixtransposition.add(new JLabel(Messages.getString("APrint.11"))); //$NON-NLS-1$
		choixtransposition.add(listetransposition);

		listetransposition.setToolTipText(Messages.getString("APrint.128")); //$NON-NLS-1$
		listetransposition.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				try {

					// transposedCarton = null;
					// pianoroll.setVirtualBook(null);
					//
					//
					checkState();
				} catch (Exception ex) {
					JMessageBox.showMessage(APrint.this,
							Messages.getString("APrint.55") //$NON-NLS-1$
									+ ex.getMessage());
				}
			}
		});
		panneauTranspositionEtConversion = new JPanel();
		panneauTranspositionEtConversion.add(choixtransposition);

		instrumentChanged();

		jouer = new JButton();

		jouer.setIcon(new ImageIcon(getClass().getResource("noatunplay.png"), //$NON-NLS-1$
				Messages.getString("APrint.80"))); //$NON-NLS-1$
		jouer.setActionCommand("JOUER"); //$NON-NLS-1$
		jouer.addActionListener(this);
		jouer.setToolTipText(Messages.getString("APrint.129")); //$NON-NLS-1$

		rewind = new JButton();
		rewind.setIcon(new ImageIcon(getClass().getResource("2leftarrow.png")));//$NON-NLS-1$
		rewind.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pianoroll.clearHightlight();
				pianoroll.setXoffset(0.0);
				pianoroll.repaint();
			}
		});
		rewind.setToolTipText(Messages.getString("APrint.286")); //$NON-NLS-1$

		JButton convertir = new JButton(Messages.getString("APrint.130")); //$NON-NLS-1$
		convertir.setToolTipText(Messages.getString("APrint.131")); //$NON-NLS-1$
		convertir.setIcon(new ImageIcon(getClass().getResource(
				"2rightarrow.png")));//$NON-NLS-1$
		convertir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					infiniteStartWait(Messages.getString("APrint.150")); //$NON-NLS-1$
					switchToPianoRollView();
					backgroundexecutor.execute(new Runnable() {

						public void run() {
							try {
								try {
									transposeCarton();
								} finally {
									infiniteEndWait();
								}
								SwingUtilities.invokeAndWait(new Runnable() {

									public void run() {
										try {

											checkState();
										} catch (Exception ex) {
											logger.error("transposeCarton", ex); //$NON-NLS-1$
											JMessageBox.showMessage(
													APrint.this,
													ex.getMessage());
										}
									}
								});

							} catch (Throwable ex) {
								logger.error("transposeCarton", ex); //$NON-NLS-1$
								JMessageBox.showMessage(APrint.this,
										ex.getMessage());
							}
						}
					});

				} catch (Exception ex) {
					logger.error("Convertir", ex); //$NON-NLS-1$
					JMessageBox.showMessage(APrint.this, ex.getMessage());
				}
			}
		});

		// instruments

		// refreshInstruments();

		panneauTranspositionEtConversion.add(convertir);

		gammeTranspositionInstrumentPanel.setBorder(new TitledBorder(Messages
				.getString("APrint.14"))); //$NON-NLS-1$
		gammeTranspositionInstrumentPanel.setLayout(new BorderLayout());

		gammeparameters = new JPanel();
		// BoxLayout gammeparametersbl = new BoxLayout(gammeparameters,
		// BoxLayout.Y_AXIS);
		gammeparameters.setLayout(new BorderLayout());

		gammeparameters.add(pInstrument, BorderLayout.CENTER);
		// gammeparameters.add(pecoute, BorderLayout.SOUTH);

		gammeTranspositionInstrumentPanel.add(gammeparameters,
				BorderLayout.CENTER);

		imprimer.setActionCommand("IMPRIMER"); //$NON-NLS-1$
		imprimer.setIcon(new ImageIcon(getClass().getResource("printmgr.png"), //$NON-NLS-1$
				Messages.getString("APrint.82"))); //$NON-NLS-1$
		imprimer.addActionListener(this);
		imprimer.setToolTipText(Messages.getString("APrint.136")); //$NON-NLS-1$

		preview.setActionCommand("PREVIEW"); //$NON-NLS-1$
		preview.addActionListener(this);
		preview.setIcon(new ImageIcon(getClass().getResource("preview.png"), //$NON-NLS-1$
				Messages.getString("APrint.84"))); //$NON-NLS-1$
		preview.setToolTipText(Messages.getString("APrint.137")); //$NON-NLS-1$
		//
		// tracer.setActionCommand("TRACER"); //$NON-NLS-1$
		// tracer.setIcon(new ImageIcon(getClass().getResource(
		// "tool_clipboard.png"))); //$NON-NLS-1$
		// tracer.addActionListener(this);
		// tracer.setToolTipText(Messages.getString("APrint.138"));
		// //$NON-NLS-1$

		exportAsWav.setActionCommand("EXPORTTOWAV"); //$NON-NLS-1$
		exportAsWav.addActionListener(this);
		exportAsWav.setIcon(new ImageIcon(getClass().getResource("kdat.png")));//$NON-NLS-1$
		exportAsWav.setToolTipText(Messages.getString("APrint.139")); //$NON-NLS-1$

		exportAsMp3.setActionCommand("EXPORTTOMP3"); //$NON-NLS-1$
		exportAsMp3.addActionListener(this);
		exportAsMp3.setIcon(new ImageIcon(getClass().getResource("kdat.png")));//$NON-NLS-1$
		exportAsMp3.setToolTipText(Messages.getString("APrint.0")); //$NON-NLS-1$

		exportAsOgg.setActionCommand("EXPORTTOOGG"); //$NON-NLS-1$
		exportAsOgg.addActionListener(this);
		exportAsOgg.setIcon(new ImageIcon(getClass().getResource("kdat.png")));//$NON-NLS-1$
		exportAsOgg.setToolTipText(Messages.getString("APrint.220")); //$NON-NLS-1$

		exportAsMidi.setIcon(new ImageIcon(getClass().getResource("kdat.png")));//$NON-NLS-1$
		exportAsMidi
				.setToolTipText("Enregistre l'écoute du carton dans un nouveau fichier midi"); //$NON-NLS-1$
		exportAsMidi.setActionCommand("EXPORTTOMID"); //$NON-NLS-1$
		exportAsMidi.addActionListener(this);

		exportAsMovie
				.setIcon(new ImageIcon(getClass().getResource("movie.png")));//$NON-NLS-1$
		exportAsMovie.setText(Messages.getString("APrint.307")); //$NON-NLS-1$
		exportAsMovie.setToolTipText(Messages.getString("APrint.308")); //$NON-NLS-1$
		exportAsMovie.addActionListener(this);
		exportAsMovie.setActionCommand("EXPORTMOV"); //$NON-NLS-1$

		buttonActionToolbar.add(rewind);
		buttonActionToolbar.add(jouer);
		buttonActionToolbar.add(preview);
		buttonActionToolbar.add(imprimer);
		// buttonActionToolbar.add(tracer);

		VirtualBookToolbarButtonsExtensionPoint[] allToolbarPoints = ExtensionPointProvider
				.getAllPoints(VirtualBookToolbarButtonsExtensionPoint.class,
						exts);
		for (int i = 0; i < allToolbarPoints.length; i++) {
			VirtualBookToolbarButtonsExtensionPoint addToolbarButtons = allToolbarPoints[i];
			addToolbarButtons.addButtons(buttonActionToolbar);
		}

		// buttonPanel.setLayout(new FlowLayout());
		// buttonPanel.add(buttonActionToolbar);

		// buttonPanel.add(jouer);
		// buttonPanel.add(preview); // Désactivation du preview
		// buttonPanel.add(exportAsWav);
		// buttonPanel.add(exportAsMidi);
		// buttonPanel.add(imprimer);
		// buttonPanel.add(tracer); // Désactivation de la fonction de trace

		// ////////////////////////////////////////////////////////////////////////////
		// Panneau de vision du carton ... :-)

		// Setup pianoRoll ...

		// ajout de la couche contenant les problèmes de transposition
		pianoroll.addLayer(psgl);
		pianoroll.addLayer(il);
		pianoroll.addLayer(tbl);

		logger.debug("add layers from extensions"); //$NON-NLS-1$
		LayersExtensionPoint[] allLayersPoints = ExtensionPointProvider
				.getAllPoints(LayersExtensionPoint.class, exts);
		for (int i = 0; i < allLayersPoints.length; i++) {
			LayersExtensionPoint addLayers = allLayersPoints[i];
			addLayers.addLayers(pianoroll);
		}

		pianorollpanel.setBorder(new TitledBorder(Messages
				.getString("APrint.52"))); //$NON-NLS-1$
		pianorollpanel.setLayout(new BorderLayout());
		pianoroll.setPreferredSize(new Dimension(700, 300));

		class MouseEvents extends MouseAdapter implements MouseMotionListener {

			private boolean pan = false;

			private double origineX;

			private int posx;

			public void mouseMoved(MouseEvent e) {

			}

			public void mouseDragged(MouseEvent e) {
				logger.debug("mouse dragged :" + e); //$NON-NLS-1$

				if (pan) {
					int x = e.getX();
					int y = e.getY();

					int delta = posx - x;

					logger.debug("pan to :"); //$NON-NLS-1$

					pianoroll.setXoffset(origineX + pianoroll.pixelToMM(delta));

					pianoroll.repaint();

				}
			}

			@Override
			public void mousePressed(MouseEvent e) {

				logger.debug("mouse pressed :" + e.getButton()); //$NON-NLS-1$

				if (e.getButton() == MouseEvent.BUTTON2) {
					posx = e.getX();

					origineX = pianoroll.getXoffset();

					pan = true;
					logger.debug("pan started"); //$NON-NLS-1$

					pianoroll.setCursor(Cursor
							.getPredefinedCursor(Cursor.HAND_CURSOR));

				} else {

					// normal click on the book

				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {

				pan = false;
				pianoroll.setCursor(Cursor
						.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

				if (e.getButton() == MouseEvent.BUTTON1) {

					// récupération du click sur le carton ...
					pianoroll.setHightlight(pianoroll.convertScreenXToCarton(e
							.getX()));

					pianoroll.repaint();
				}
			}

		}

		MouseEvents m = new MouseEvents();

		pianoroll.addMouseListener(m);
		pianoroll.addMouseMotionListener(m);

		issuePresenter = new JIssuePresenter(APrint.this);
		final JSplitPane pianorollsplit = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, pianoroll, issuePresenter);

		issuePresenter.setIssueSelectionListener(new IssueSelectionListener() {
			public void issueSelected(AbstractIssue issue) {
				if (issue == null) {
					il.resetSelectedIssues();
				} else {
					il.setSelectedIssues(new AbstractIssue[] { issue });
				}

				pianoroll.repaint();
				return;
			}

			public void issueDoubleClick(AbstractIssue issue) {

				if (issue == null)
					return;

				if (!(issue instanceof AbstractSpatialIssue))
					return;

				AbstractSpatialIssue asi = (AbstractSpatialIssue) issue;

				int width = pianoroll.getWidth();
				double length = pianoroll.pixelToMM(width);

				Region extent = asi.getExtent();

				double center = pianoroll
						.timeToMM((extent.start + extent.end) / 2);

				pianoroll.setXoffset(center - length / 2);

				showErrorsLayerCheckBox.setSelected(true);

				pianoroll.repaint();

			}

		});

		issuePresenter.setToolTipText(Messages.getString("APrint.288")); //$NON-NLS-1$

		pianorollpanel.add(pianorollsplit, BorderLayout.CENTER);
		pianorollsplit.setOneTouchExpandable(true);

		// boutons de visualisation du pianoroll ...

		JToolBar pianorolltb = new JToolBar();

		JPanel pianorollbutton = new JPanel();
		BoxLayout bl_pianorollbutton = new BoxLayout(pianorollbutton,
				BoxLayout.X_AXIS);
		pianorollbutton.setLayout(bl_pianorollbutton);

		// ajout des boutons
		JButton zoomplus = new JButton(); //$NON-NLS-1$
		zoomplus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pianoroll.setXfactor(pianoroll.getXfactor() / 2);
				pianoroll.repaint();
			}
		});
		zoomplus.setIcon(new ImageIcon(
				getClass().getResource("viewmag.png"), Messages.getString("APrint.95"))); //$NON-NLS-1$ //$NON-NLS-2$ 
		zoomplus.setToolTipText(Messages.getString("APrint.140")); //$NON-NLS-1$
		pianorolltb.add(zoomplus);

		JButton zoommoins = new JButton();
		zoommoins.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pianoroll.setXfactor(pianoroll.getXfactor() * 2);
				pianoroll.repaint();
			}
		});
		zoommoins.setIcon(new ImageIcon(getClass().getResource(
				"viewmagminus.png"), Messages.getString("APrint.97"))); //$NON-NLS-1$ //$NON-NLS-2$ 
		zoommoins.setToolTipText(Messages.getString("APrint.141")); //$NON-NLS-1$

		pianorolltb.add(zoommoins);
		showErrorsLayerCheckBox = new JCheckBox(
				Messages.getString("APrint.142"), il //$NON-NLS-1$
						.isVisible());
		showErrorsLayerCheckBox
				.setToolTipText(Messages.getString("APrint.143")); //$NON-NLS-1$
		showErrorsLayerCheckBox.setSelected(aprintproperties.isErrorsVisible());
		showErrorsLayerCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox c = (JCheckBox) e.getSource();
				boolean checked = c.isSelected();
				il.setVisible(checked);
				aprintproperties.setErrorsVisible(checked);
				repaint();
			}
		});
		showTimeLayerCheckBox = new JCheckBox(Messages.getString("APrint.202"), //$NON-NLS-1$
				tbl.isVisible());
		showTimeLayerCheckBox.setToolTipText(Messages.getString(Messages
				.getString("APrint.201"))); //$NON-NLS-1$

		showTimeLayerCheckBox
				.setSelected(aprintproperties.isTimeLayerVisible());

		showTimeLayerCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox c = (JCheckBox) e.getSource();
				boolean checked = c.isSelected();
				tbl.setVisible(checked);
				aprintproperties.setTimeLayerVisible(checked);
				repaint();
			}
		});

		JCheckBox showPipeStops = new JCheckBox(
				Messages.getString("APrint.298"), psgl.isVisible()); //$NON-NLS-1$
		showPipeStops.setToolTipText(Messages.getString("APrint.299")); //$NON-NLS-1$
		showPipeStops.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox c = (JCheckBox) e.getSource();
				boolean checked = c.isSelected();
				psgl.setVisible(checked);
				repaint();
			}
		});

		pianorolltb.add(showErrorsLayerCheckBox);
		pianorolltb.add(showTimeLayerCheckBox);
		pianorolltb.add(showPipeStops);

		VisibilityLayerButtonsExtensionPoint[] allVisibilityButtonsPoints = ExtensionPointProvider
				.getAllPoints(VisibilityLayerButtonsExtensionPoint.class, exts);
		for (int i = 0; i < allVisibilityButtonsPoints.length; i++) {
			VisibilityLayerButtonsExtensionPoint visibilityLayerButtons = allVisibilityButtonsPoints[i];
			visibilityLayerButtons.addVisibilityLayerButtons(pianorolltb);
		}

		pianorollbutton.add(pianorolltb);

		pianorollbutton.add(buttonActionToolbar);

		JPanel toolbarPanel = new JPanel();
		BoxLayout boxLayoutToolbars = new BoxLayout(toolbarPanel,
				BoxLayout.LINE_AXIS);

		toolbarPanel.setLayout(boxLayoutToolbars);

		toolbarPanel.add(pianorollbutton);

		ToolbarAddExtensionPoint[] allToolBars = ExtensionPointProvider
				.getAllPoints(ToolbarAddExtensionPoint.class, exts);
		for (int i = 0; i < allToolBars.length; i++) {
			ToolbarAddExtensionPoint tep = allToolBars[i];
			try {
				JToolBar[] tbs = tep.addToolBars();
				if (tbs != null) {
					for (int j = 0; j < tbs.length; j++) {
						logger.debug("adding toolbar"); //$NON-NLS-1$
						toolbarPanel.add(tbs[j]);
					}
				}
			} catch (Throwable t) {
				logger.error("fail to add toolbar :" + tep); //$NON-NLS-1$
			}

		}

		exportToolbar = new JToolBar("Export ...");
		exportToolbar.add(exportAsWav);
		// buttonActionToolbar.add(exportAsMp3);
		exportToolbar.add(exportAsOgg);
		exportToolbar.add(exportAsMidi);
		exportToolbar.add(exportAsMovie);

		toolbarPanel.add(exportToolbar);

		// adding the measure Tool ...

		MeasureTool mt = new MeasureTool(pianoroll);
		

		pianorollpanel.add(toolbarPanel, BorderLayout.NORTH);

		// north pour permettre le redimmensionnement de la partie centrale
		// BoxLayout bl = new BoxLayout(northPanel, BoxLayout.Y_AXIS);
		northPanel.setLayout(new BorderLayout());

		northPanel.add(panelchoixfichier, BorderLayout.NORTH);

		JScrollPane northScrollpane = new JScrollPane(
				gammeTranspositionInstrumentPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		northPanel.add(northScrollpane, BorderLayout.CENTER);

		northPanel.add(panneauTranspositionEtConversion, BorderLayout.SOUTH);

		panelPrincipal.setLayout(new BorderLayout());

		tabs = new JTabbedPane(JTabbedPane.TOP);
		tabs.addTab(Messages.getString("APrint.209"), northPanel); //$NON-NLS-1$
		tabs.addTab(Messages.getString("APrint.207"), //$NON-NLS-1$
				new ImageIcon(getClass().getResource("icon.jpg")), //$NON-NLS-1$
				pianorollpanel);

		changeBookTabTitle(Messages.getString("APrint.207")); //$NON-NLS-1$

		//
		// splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
		// new JScrollPane(northPanel), pianorollpanel);
		// splitpane.setOneTouchExpandable(true);
		//

		panelPrincipal.add(tabs, BorderLayout.CENTER);

		// panelPrincipal.add(northPanel, BorderLayout.NORTH);
		// panelPrincipal.add(pianorollpanel, BorderLayout.CENTER);

		setGlassPane(infiniteprogresspanel);

		getContentPane().add(panelPrincipal);

		// Définir le menu

		setJMenuBar(constructMenu());

		setSize(aprintproperties.getAPrintFrameSize());

		// pack();

		// définition de la hauteur du split pane par défaut ...
		// splitpane.setDividerLocation(0.8);
		logger.debug("divider location " //$NON-NLS-1$
				+ aprintproperties.getPianorollDividerLocation());

		addWindowListener(new WindowAdapter() {

			public void windowOpened(WindowEvent e) {
				pianorollsplit.setDividerLocation((int) (aprintproperties
						.getPianorollDividerLocation() * pianorollsplit
						.getWidth()));

			}
		});

		issuePresenter.addAncestorListener(new AncestorListener() {

			public void ancestorAdded(AncestorEvent event) {

			}

			public void ancestorMoved(AncestorEvent event) {
				try {
					if (pianorollsplit.getDividerLocation() < 0)
						return;

					aprintproperties.setPianorollDividerLocation(1.0
							* pianorollsplit.getDividerLocation()
							/ pianorollsplit.getWidth());
				} catch (Exception ex) {
					logger.error("divider storage", ex); //$NON-NLS-1$
				}

			}

			public void ancestorRemoved(AncestorEvent event) {

			}
		});

		checkState();

		refreshTranspositions();

		addComponentListener(new ComponentListener() {

			public void componentHidden(ComponentEvent e) {

			}

			public void componentMoved(ComponentEvent e) {

			}

			public void componentResized(ComponentEvent e) {
				APrint a = (APrint) e.getComponent();
				aprintproperties.setAPrintFrameSize(a.getSize());
			}

			public void componentShown(ComponentEvent e) {

			}
		});

		logger.debug("end of constructor"); //$NON-NLS-1$

		asyncJobsManager = new AsyncJobsManager();

		checkState();

	}

	public static Image getAPrintApplicationIcon() {
		return Toolkit.getDefaultToolkit().getImage(
				APrint.class.getResource("icon.jpg")); //$NON-NLS-1$
	}

	private void informExtensionsAboutRepository() {
		InformRepositoryExtensionPoint[] allPoints = ExtensionPointProvider
				.getAllPoints(InformRepositoryExtensionPoint.class, exts);
		for (int i = 0; i < allPoints.length; i++) {
			InformRepositoryExtensionPoint informRepositoryExtensionPoint = allPoints[i];
			try {
				informRepositoryExtensionPoint
						.informRepository(this.repository);
			} catch (Throwable t) {
				logger.error("Extension " + informRepositoryExtensionPoint //$NON-NLS-1$
						+ " throw an exception", t); //$NON-NLS-1$
			}
		}
	}

	private void changeBookTabTitle(String title) {
		tabs.setTitleAt(1, title);
	}

	/**
	 * Construction du menu associé à l'application
	 * 
	 * @return le menu bar associé
	 */
	private JMenuBar constructMenu() {
		JMenuBar menu = new JMenuBar();

		constructMenuFile(menu);
		constructMenuGamme(menu);
		constructInternetRepositoryMenu(menu);

		constructMenuOutils(menu);

		constructMenuOptions(menu);

		menu.add(Box.createHorizontalGlue());

		constructMenuHelp(menu);

		return menu;
	}

	private void constructMenuOutils(JMenuBar menu) {

		JMenu toolsMenu = new JMenu(Messages.getString("APrint.316")); //$NON-NLS-1$
		toolsMenu.setIcon(new ImageIcon(getClass().getResource(
				"ark_options.png"))); //$NON-NLS-1$

		// Editeur de gamme ...

		JMenuItem editeurgamme = toolsMenu
				.add(Messages.getString("APrint.117")); //$NON-NLS-1$
		editeurgamme.setIcon(new ImageIcon(getClass().getResource("kmid.png"))); //$NON-NLS-1$
		editeurgamme.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					// prefs for user ...

					ScaleEditor editor = new ScaleEditor(APrint.this,
							new ScaleEditorPrefs(aprintproperties
									.getFilePrefsStorage()), repository
									.getRepository2());
					editor.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					editor.setVisible(true);

				} catch (Exception ex) {
					logger.error("editeurgamme", ex); //$NON-NLS-1$
				}
			}
		});

		toolsMenu.addSeparator();

		JMenuItem groovyScriptConsole = new JMenuItem(
				Messages.getString("APrint.317")); //$NON-NLS-1$
		groovyScriptConsole.setIcon(new ImageIcon(GroovyMain.class
				.getResource("ConsoleIcon.png"))); //$NON-NLS-1$
		groovyScriptConsole.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					APrintGroovyConsole printGroovyConsole = new APrintGroovyConsole(
							APrint.this, aprintproperties, asyncJobsManager);
					printGroovyConsole.setVisible(true);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					JMessageBox.showMessage(APrint.this,
							Messages.getString("APrint.318") //$NON-NLS-1$
									+ ex.getMessage());
					BugReporter.sendBugReport();
				}
			}
		});

		toolsMenu.add(groovyScriptConsole);
		menu.add(toolsMenu);
	}

	/**
	 * Construction du menu aide en ligne
	 * 
	 * @param menu
	 */
	private void constructMenuHelp(JMenuBar menu) {
		JMenu helpmenu = new JMenu(Messages.getString("APrint.56")); //$NON-NLS-1$
		helpmenu.setIcon(new ImageIcon(getClass().getResource("help.png"))); //$NON-NLS-1$
		helpmenu.setMnemonic('h');//$NON-NLS-1$
		helpmenu.setHorizontalAlignment(SwingConstants.RIGHT);

		JMenuItem aide = helpmenu.add(Messages.getString("APrint.102")); //$NON-NLS-1$
		aide.setIcon(new ImageIcon(getClass().getResource("help.png"), //$NON-NLS-1$
				Messages.getString("APrint.104"))); //$NON-NLS-1$

		CSH.DisplayHelpFromSource displayHelpFromSource = new CSH.DisplayHelpFromSource(
				hb);
		aide.addActionListener(displayHelpFromSource);
		helpmenu.add(aide);

		JMenuItem apropos = helpmenu.add(Messages.getString("APrint.57")); //$NON-NLS-1$
		apropos.setIcon(new ImageIcon(getClass().getResource("help.png"), //$NON-NLS-1$
				Messages.getString("APrint.85"))); //$NON-NLS-1$
		apropos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// lecture du fichier de propriété

					AboutFrame af = new AboutFrame(APrint.this, Messages
							.getString("APrint.86"), true); //$NON-NLS-1$

					StringBuffer sb = new StringBuffer();
					sb.append("<p align=\"right\"><b>APrint Version</b>:" + getVersion() + "</p>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

					logger.debug("reading about file ... "); //$NON-NLS-1$

					af.setAboutContent(APrint.this.getClass()
							.getResourceAsStream("about.xml")); //$NON-NLS-1$

					af.setLocationByPlatform(true);
					SwingUtils.center(af);

					af.setVisible(true);

					//
					// JMessageBox.showMessage(APrint.this, Messages
					// .getString("APrint.59") //$NON-NLS-1$
					// + prop.getProperty("version")); //$NON-NLS-1$
					//

				} catch (Exception ex) {
					logger.error("APropos", ex); //$NON-NLS-1$
					JMessageBox.showMessage(APrint.this,
							Messages.getString("APrint.62") //$NON-NLS-1$
									+ ex.getMessage());
					BugReporter.sendBugReport();
				}
			}
		});

		logger.debug("adding extension help menu"); //$NON-NLS-1$

		HelpMenuItemsExtensionPoint[] helpExtensionsPoints = ExtensionPointProvider
				.getAllPoints(HelpMenuItemsExtensionPoint.class, exts);
		for (int i = 0; i < helpExtensionsPoints.length; i++) {
			HelpMenuItemsExtensionPoint helpMenuItemsExtensionPoint = helpExtensionsPoints[i];
			try {
				helpMenuItemsExtensionPoint.addHelpMenuItem(helpmenu);
			} catch (Throwable t) {
				logger.error("Extension " + helpMenuItemsExtensionPoint //$NON-NLS-1$
						+ " throw an exception", t); //$NON-NLS-1$
			}
		}

		helpmenu.addSeparator();

		if (aprintproperties.isBeta()) {
			helpmenu.addSeparator();
			JMenuItem reportsend = new JMenuItem(
					Messages.getString("APrint.244")); //$NON-NLS-1$
			reportsend
					.setIcon(new ImageIcon(getClass().getResource("bug.png"))); //$NON-NLS-1$
			reportsend.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BugReporter.sendBugReport();
					JMessageBox.showMessage(APrint.this,
							Messages.getString("APrint.257")); //$NON-NLS-1$
				}
			});

			helpmenu.add(reportsend);

		}

		menu.add(helpmenu);
	}

	/**
	 * Construction du menu options ..
	 * 
	 * @param menu
	 */
	private void constructMenuOptions(JMenuBar menu) {

		JMenu m_Options = new JMenu(Messages.getString("APrint.144")); //$NON-NLS-1$
		m_Options.setIcon(new ImageIcon(getClass().getResource(
				"package_settings.png"))); //$NON-NLS-1$
		m_Options.setMnemonic('o');

		JMenu repositoryMenu = constructMenuRepository(m_Options);

		m_Options.add(repositoryMenu);

		logger.debug("add option menu from extension"); //$NON-NLS-1$

		OptionMenuItemsExtensionPoint[] allOptionMenuPoints = ExtensionPointProvider
				.getAllPoints(OptionMenuItemsExtensionPoint.class, exts);
		for (int i = 0; i < allOptionMenuPoints.length; i++) {
			OptionMenuItemsExtensionPoint addOptionMenuItems = allOptionMenuPoints[i];
			try {
				addOptionMenuItems.addOptionMenuItem(m_Options);
			} catch (Throwable t) {
				logger.error("Extension " + addOptionMenuItems //$NON-NLS-1$
						+ " throw an exception", t); //$NON-NLS-1$
			}
		}

		m_Options.addSeparator();

		createMenuOptionsExtension(m_Options);

		m_Options.addSeparator();
		JMenuItem miTranslator = new JMenuItem(Messages.getString("APrint.242")); //$NON-NLS-1$
		miTranslator
				.setIcon(new ImageIcon(getClass().getResource("locale.png"))); //$NON-NLS-1$
		miTranslator.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					JTranslator t = new JTranslator(Messages
							.getOverrideLocalizedMessageFile(), Messages
							.getEnglishBundle(), aprintproperties);

					SwingUtils.center(t);

					t.setVisible(true);

				} catch (Exception ex) {
					String msg = Messages.getString("APrint.243") //$NON-NLS-1$
							+ ex.getMessage();
					logger.error(msg, ex);
					BugReporter.sendBugReport();
					JOptionPane.showMessageDialog(APrint.this, msg);
				}
			}
		});

		m_Options.add(miTranslator);

		JMenu lookandfeel = new JMenu(Messages.getString("APrint.248")); //$NON-NLS-1$
		lookandfeel
				.setIcon(new ImageIcon(getClass().getResource("wizard.png"))); //$NON-NLS-1$

		JMenuItem swinglnf = lookandfeel.add("Standard Java"); //$NON-NLS-1$
		swinglnf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				aprintproperties.setLookAndFeel("swing"); //$NON-NLS-1$
				relaunchWithMessageBox();
			}
		});

		LookAndFeelInfo[] installedLookAndFeels = UIManager
				.getInstalledLookAndFeels();
		LookAndFeel currentlnf = UIManager.getLookAndFeel();

		Set<String> alreadyPuttedInMenu = new TreeSet<String>();

		for (int i = 0; i < installedLookAndFeels.length; i++) {
			LookAndFeelInfo lookAndFeelInfo = installedLookAndFeels[i];
			final String lnfname = lookAndFeelInfo.getName();
			final String lnfclassname = lookAndFeelInfo.getClassName();

			if (!alreadyPuttedInMenu.contains(lnfclassname)) {

				JCheckBoxMenuItem installedlnf = new JCheckBoxMenuItem(lnfname);
				lookandfeel.add(installedlnf);

				if (currentlnf != null
						&& currentlnf.getClass().getName().equals(lnfclassname)) {
					installedlnf.setState(true);
				}

				installedlnf.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						aprintproperties.setLookAndFeel(lnfclassname); //$NON-NLS-1$
						relaunchWithMessageBox();
					}
				});
				alreadyPuttedInMenu.add(lnfclassname);
			}
		}

		m_Options.addSeparator();
		m_Options.add(lookandfeel);
		m_Options.addSeparator();
		m_Options.add(constructMenuOptionRendering());

		menu.add(m_Options);

	}

	private JMenu constructMenuOptionRendering() {
		final JMenu m = new JMenu(Messages.getString("APrint.300")); //$NON-NLS-1$

		final JCheckBoxMenuItem defaultRendering = new JCheckBoxMenuItem(
				Messages.getString("APrint.301")); //$NON-NLS-1$
		m.add(defaultRendering);

		defaultRendering.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				APrint.this.currentPlaySubSystem = new GervillPlaySubSystem();
				for (Component c : m.getMenuComponents()) {
					if (c instanceof JCheckBoxMenuItem) {
						JCheckBoxMenuItem mi = (JCheckBoxMenuItem) c;
						mi.setSelected(false);
					}
				}
				defaultRendering.setSelected(true);
			}
		});
		defaultRendering.setSelected(true);

		Info[] infos = MidiSystem.getMidiDeviceInfo();
		for (int i = 0; i < infos.length; i++) {
			final Info info = infos[i];
			final JCheckBoxMenuItem midiRendering = new JCheckBoxMenuItem(
					Messages.getString("APrint.302") + info.getName()); //$NON-NLS-1$
			m.add(midiRendering);

			midiRendering.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MidiDevicePlaySubSystem midiDevicePlaySubSystem = new MidiDevicePlaySubSystem();
					midiDevicePlaySubSystem.setCurrentMidiDevice(info);
					APrint.this.currentPlaySubSystem = midiDevicePlaySubSystem;
					for (Component c : m.getMenuComponents()) {
						if (c instanceof JCheckBoxMenuItem) {
							JCheckBoxMenuItem mi = (JCheckBoxMenuItem) c;
							mi.setSelected(false);
						}
					}
					midiRendering.setSelected(true);
				}
			});

		}

		return m;
	}

	private JMenu constructMenuRepository(JMenu m_Options) {
		JMenu repositoryMenu = new JMenu(Messages.getString("APrint.258")); //$NON-NLS-1$

		JMenuItem resetParametersgamme = repositoryMenu.add(Messages
				.getString("APrint.53")); //$NON-NLS-1$
		resetParametersgamme.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					aprintproperties.setGammeAndTranlation(null);
					defineNewGammeAndTranspositionFolder(null);
					repositoryChanged();
					refreshInstruments();

					refreshTranspositions();

					checkState();

				} catch (Exception ex) {
					logger.error("ResetParametersGamme", ex); //$NON-NLS-1$
					JMessageBox.showMessage(APrint.this,
							Messages.getString("APrint.55") //$NON-NLS-1$
									+ ex.getMessage());
				}
			}
		});

		m_Options.addSeparator();

		JMenuItem parametresgamme = repositoryMenu.add(Messages
				.getString("APrint.35")); //$NON-NLS-1$
		parametresgamme.setIcon(new ImageIcon(getClass()
				.getResource("misc.png"), Messages.getString("APrint.94"))); //$NON-NLS-1$ //$NON-NLS-2$
		parametresgamme.setActionCommand("CHOICEGAMMEFOLDER"); //$NON-NLS-1$
		parametresgamme.addActionListener(this);

		JMenuItem synchronize = repositoryMenu.add(Messages
				.getString("APrint.265")); //$NON-NLS-1$
		synchronize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				synchronizeWebRepositories();

			}

		});

		return repositoryMenu;
	}

	public void synchronizeWebRepositories() {
		infiniteStartWait(Messages.getString("APrint.266")); //$NON-NLS-1$

		Thread t = new Thread(new Runnable() {
			public void run() {

				final SynchronizationReport report = new SynchronizationReport();

				try {
					Repository2 repository2 = repository.getRepository2();
					if (repository2 != null) {
						if (repository2 instanceof Repository2Collection) {
							Repository2Collection rcollection = (Repository2Collection) repository2;
							for (int i = 0; i < rcollection
									.getRepositoryCount(); i++) {
								Repository2 r = rcollection.getRepository(i);
								if (r != null
										&& r instanceof GAESynchronizedRepository2) {
									GAESynchronizedRepository2 g = (GAESynchronizedRepository2) r;
									infiniteChangeText(Messages
											.getString("APrint.267") //$NON-NLS-1$
											+ g.getName());
									try {

										SynchronizationFeedBack sb = new SynchronizationFeedBack() {
											public void inform(String message,
													double progress) {

												infiniteChangeText(Messages
														.getString("APrint.268") //$NON-NLS-1$
														+ message
														+ " (" //$NON-NLS-1$
														+ ((int) (progress * 100))
														+ " % )"); //$NON-NLS-1$

											}
										};

										SynchronizationReport reportSynchro = g
												.synchronizeRepository(sb);

										report.addAll(reportSynchro);

									} catch (Exception ex) {
										final Exception fex = ex;

										logger.error(ex.getMessage(), ex);
										BugReporter.sendBugReport();

										try {
											SwingUtilities
													.invokeAndWait(new Runnable() {

														public void run() {

															JMessageBox
																	.showMessage(
																			APrint.this,
																			Messages.getString("APrint.303") //$NON-NLS-1$
																					+ fex.getMessage());

														}

													});
										} catch (Exception ex_) {
											logger.error(ex_.getMessage(), ex_);
										}

									}
								}
							}

						}
					}

				} finally {
					infiniteEndWait();
				}

				if (report != null && report.hasErrors()) {
					try {
						SwingUtilities.invokeAndWait(new Runnable() {

							public void run() {

								StringBuffer sb = new StringBuffer();

								for (Iterator<SynchroElement> it = report
										.iterator(); it.hasNext();) {
									SynchroElement e = it.next();

									if (e.getStatus() == SynchroElement.ERROR) {
										sb.append(
												Messages.getString("APrint.304") + e.getMessage()) //$NON-NLS-1$
												.append("\n"); //$NON-NLS-1$
									}
								}

								JMessageBox.showMessage(APrint.this,
										Messages.getString("APrint.306") //$NON-NLS-1$
												+ sb.toString());

							}

						});
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
						BugReporter.sendBugReport();
					}
				}

			}

		});

		t.start();
	}

	private void createMenuOptionsExtension(JMenu m_Options) {

		JMenu menuExtensions = new JMenu(Messages.getString("APrint.250")); //$NON-NLS-1$

		menuExtensions.setIcon(new ImageIcon(this.getClass().getResource(
				"connect_creating.png"))); //$NON-NLS-1$

		JMenuItem extensions = menuExtensions.add(Messages
				.getString("APrint.215")); //$NON-NLS-1$
		extensions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					APrintExtensionList ae = new APrintExtensionList(
							APrint.this, em);
					SwingUtils.center(ae);
					ae.setVisible(true);

				} catch (Exception ex) {
					logger.error("extension list", ex); //$NON-NLS-1$
					BugReporter.sendBugReport();
					JMessageBox.showMessage(APrint.this,
							Messages.getString("APrint.217") //$NON-NLS-1$
									+ ex.getMessage());
				}
			}
		});

		JMenuItem extensionRepository = menuExtensions.add(Messages
				.getString("APrint.245")); //$NON-NLS-1$
		extensionRepository.setIcon(new ImageIcon(this.getClass().getResource(
				"connect_creating.png"))); //$NON-NLS-1$
		extensionRepository.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					APrintExtensionRepository ae = new APrintExtensionRepository(
							APrint.this, em);
					SwingUtils.center(ae);
					ae.setVisible(true);

				} catch (Exception ex) {
					logger.error("extensionRepository ", ex); //$NON-NLS-1$
					BugReporter.sendBugReport();
					JMessageBox.showMessage(APrint.this,
							Messages.getString("APrint.217") //$NON-NLS-1$
									+ ex.getMessage());
				}
			}
		});

		m_Options.add(menuExtensions);
	}

	private JMenu scaleAndInstrumentEditor;

	/**
	 * Construction du menu gamme
	 * 
	 * @param menu
	 */
	private void constructMenuGamme(JMenuBar menu) {

		JMenu m_gamme = new JMenu(Messages.getString("APrint.287")); //$NON-NLS-1$
		m_gamme.setMnemonic('i');
		m_gamme.setIcon(new ImageIcon(APrint.getAPrintApplicationIcon()));

		m_gamme.add(popupmenuImprimerGamme);

		popupmenuImprimerGamme.setIcon(new ImageIcon(getClass().getResource(
				"frameprint.png"))); //$NON-NLS-1$

		populateImprimerGammeMenu(popupmenuImprimerGamme);

		m_gamme.addSeparator();

		scaleAndInstrumentEditor = new JMenu(Messages.getString("APrint.260")); //$NON-NLS-1$
		m_gamme.add(scaleAndInstrumentEditor);

		menu.add(m_gamme);

		m_gamme.addSeparator();

		if (repository.getRepository2() instanceof Repository2Collection) {

			Repository2Collection rc = (Repository2Collection) repository
					.getRepository2();
			for (int i = 0; i < rc.getRepositoryCount(); i++) {
				final Repository2 therep = rc.getRepository(i);

				if (therep instanceof EditableInstrumentManagerRepository) {
					final EditableInstrumentManager manager = ((EditableInstrumentManagerRepository) therep)
							.getEditableInstrumentManager();

					JMenuItem instrumentEditorInRepository = scaleAndInstrumentEditor
							.add(Messages.getString("APrint.259") + Messages.getString("APrint.272") //$NON-NLS-1$ //$NON-NLS-2$
									+ therep.getName() + " ... "); //$NON-NLS-1$

					instrumentEditorInRepository.setIcon(new ImageIcon(
							getClass().getResource("juk_dock.png"))); //$NON-NLS-1$

					instrumentEditorInRepository
							.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									try {
										JInstrumentEditor editor = new JInstrumentEditor(
												manager);

										editor.setIconImage(getIconImage());

										editor.setLocationByPlatform(true);
										editor.setSize(1024, 768);

										editor.setVisible(true);
										editor.setDefaultCloseOperation(editor.DO_NOTHING_ON_CLOSE);
									} catch (Exception ex) {
										logger.error("instrument edition on " //$NON-NLS-1$
												+ therep.getName() + ":" //$NON-NLS-1$
												+ ex.getMessage(), ex);
										BugReporter.sendBugReport();
									}
								}
							});
				}

			}

		}

	}

	/**
	 * Construction du menu fichier
	 * 
	 * @param menu
	 */
	private void constructMenuFile(JMenuBar menu) {
		JMenu m_fichier = new JMenu(Messages.getString("APrint.34")); //$NON-NLS-1$
		m_fichier
				.setIcon(new ImageIcon(getClass().getResource("filesave.png"))); //$NON-NLS-1$
		m_fichier.setMnemonic('f');

		JMenuItem ouvrirfichiermidi = m_fichier.add(Messages
				.getString("APrint.146")); //$NON-NLS-1$
		ouvrirfichiermidi.setIcon(new ImageIcon(getClass().getResource(
				"fileopen.png"))); //$NON-NLS-1$
		ouvrirfichiermidi.setActionCommand("CHARGEMENTMIDI"); //$NON-NLS-1$
		ouvrirfichiermidi.addActionListener(this);
		ouvrirfichiermidi.setAccelerator(KeyStroke.getKeyStroke("control O")); //$NON-NLS-1$

		m_fichier.addSeparator();

		JMenuItem formatpagedefault = m_fichier.add(Messages
				.getString("APrint.120")); //$NON-NLS-1$
		formatpagedefault.setIcon(new ImageIcon(getClass().getResource(
				"frameprint.png"))); //$NON-NLS-1$
		formatpagedefault.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				PrinterJob pj = PrinterJob.getPrinterJob();

				if (lastPrintPageFormat == null) {
					lastPrintPageFormat = pj.defaultPage();
				}

				PageFormat newone = pj.pageDialog(lastPrintPageFormat);
				if (newone != lastPrintPageFormat) {
					lastPrintPageFormat = newone;
				}

			}
		});

		m_fichier.add(formatpagedefault);

		m_fichier.addSeparator();

		JMenuItem quitter = m_fichier.add(Messages.getString("APrint.37")); //$NON-NLS-1$
		quitter.setActionCommand("QUITTER"); //$NON-NLS-1$
		quitter.addActionListener(this);
		quitter.setIcon(new ImageIcon(getClass().getResource("exit.png"))); //$NON-NLS-1$
		quitter.setAccelerator(KeyStroke.getKeyStroke("control Q")); //$NON-NLS-1$

		menu.add(m_fichier);
	}

	private void constructInternetRepositoryMenu(JMenuBar menu) {
		JMenu internetMenuBar = new JMenu(Messages.getString("APrint.278")); //$NON-NLS-1$
		internetMenuBar.setIcon(new ImageIcon(getClass().getResource(
				"network.png"))); //$NON-NLS-1$

		JMenuItem synchronize = internetMenuBar.add(Messages
				.getString("APrint.280")); //$NON-NLS-1$
		synchronize
				.setIcon(new ImageIcon(getClass().getResource("remote.png"))); //$NON-NLS-1$
		synchronize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchronizeWebRepositories();
			}
		});

		internetMenuBar.addSeparator();

		JMenuItem menuItemDefineWebOptions = internetMenuBar.add(Messages
				.getString("APrint.282")); //$NON-NLS-1$
		menuItemDefineWebOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					GaeRepositoryClientConnection c = new GaeRepositoryClientConnection(
							APrint.this, true);
					try {
						c.setLogin(aprintproperties.getWebRepositoryUser());
						c.setWebUrl(aprintproperties.getWebRepositoryURL());
						c.setPassword(aprintproperties
								.getWebRepositoryPassword());

						c.setSize(500, 300);

						c.setLocationByPlatform(true);

						c.setVisible(true);

						if (!c.isCanceled()) {
							aprintproperties.setWebRepositoryURL(c.getWebUrl());
							aprintproperties.setWebRepositoryUser(c.getLogin());
							aprintproperties.setWebRepositoryPassword(c
									.getPassword());

							Repository2 r2 = repository.getRepository2();
							if (r2 instanceof Repository2Collection) {
								Repository2Collection col = (Repository2Collection) r2;

								for (int i = 0; i < col.getRepositoryCount(); i++) {

									if (col.getRepository(i) instanceof GAESynchronizedRepository2) {
										GAESynchronizedRepository2 g2 = (GAESynchronizedRepository2) col
												.getRepository(i);

										g2.changeRepositoryConnectionInfos(
												new URL(aprintproperties
														.getWebRepositoryURL()),
												aprintproperties
														.getWebRepositoryUser(),
												aprintproperties
														.getWebRepositoryPassword());
										logger.debug("connections info changed"); //$NON-NLS-1$

									}

								}

							}

						}
					} finally {
						c.dispose();
					}

				} catch (Throwable ex) {
					logger.error("error while setting parameters : " //$NON-NLS-1$
							+ ex.getMessage(), ex);
					JMessageBox.showMessage(APrint.this,
							Messages.getString("APrint.285") + ex.getMessage()); //$NON-NLS-1$
					BugReporter.sendBugReport();
				}
			}
		});

		menu.add(internetMenuBar);

	}

	private JMenu addInMenuWithNext(JMenu menu, JMenuItem item) {
		if ((menu.getMenuComponentCount() + 1) % 15 == 0) {

			JMenu next = new JMenu(Messages.getString("APrint.291")); //$NON-NLS-1$
			menu.add(next);
			return next;

		} else {
			menu.add(item);
			return menu;
		}
	}

	/**
	 * Construire le menu d'impression des gammes ...
	 * 
	 * @param imprimergamme
	 */
	private void populateImprimerGammeMenu(JMenu imprimergamme) {
		imprimergamme.removeAll();

		Repository2 repository2 = repository.getRepository2();

		if (repository2 instanceof Repository2Collection) {
			Repository2Collection rc = (Repository2Collection) repository2;

			JMenu current = imprimergamme;

			for (int i = 0; i < rc.getRepositoryCount(); i++) {

				Repository2 r = rc.getRepository(i);

				String[] scaleNamesInRepository = r.getScaleNames();

				current = addScalesInMenu(current, scaleNamesInRepository,
						r.getName());

			}

		} else {

			String[] gammenames = gm.getScaleNames();

			addScalesInMenu(imprimergamme, gammenames, null);

		}
	}

	private JMenu addScalesInMenu(JMenu imprimergamme, String[] gammenames,
			String suffix) {
		Arrays.sort(gammenames, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});

		JMenu current = imprimergamme;

		for (final String g : gammenames) {

			if (!"Midi".equalsIgnoreCase(g)) { //$NON-NLS-1$
				JMenuItem mi = new JMenuItem(g
						+ (suffix == null ? "" : " - " + suffix)); //$NON-NLS-1$ //$NON-NLS-2$
				mi.setIcon(new ImageIcon(APrint.getAPrintApplicationIcon()));
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						PrintPreview pp = new PrintPreview(
								new ScalePrintDocument(gm.getScale(g)));
						pp.setDisplayScale(100);

					}
				});
				current = addInMenuWithNext(current, mi);
			}
		}
		return current;
	}

	/**
	 * cette méthode interne permet de rafraichir le liste des instruments en
	 * fonction de la soudbank chargée
	 */
	private void refreshInstruments() {

		instrumentChoice.reloadInstruments();

	}

	/**
	 * Récupère l'instrument sélectionné
	 * 
	 * @return
	 */
	private org.barrelorgandiscovery.instrument.Instrument getSelectedInstrument() {
		return instrumentChoice.getCurrentInstrument();
	}

	private PlaySubSystem currentPlaySubSystem = new GervillPlaySubSystem();

	// ///////////////////////////////////////////////////////////
	// Gestion des actions de l'interface ...

	public void actionPerformed(ActionEvent e) {
		try {

			if ("CHARGEMENTMIDI".equals(e.getActionCommand())) { //$NON-NLS-1$
				loadMidi();
			} else if ("IMPRIMER".equals(e.getActionCommand())) { //$NON-NLS-1$
				printCarton();
			} else if ("EXPORTTOMID".equals(e.getActionCommand())) { //$NON-NLS-1$

				saveAsMidiFile();

			} else if ("EXPORTTOWAV".equals(e.getActionCommand())) { //$NON-NLS-1$

				saveWAV();

			} else if ("EXPORTTOMP3".equals(e.getActionCommand())) { //$NON-NLS-1$

				savetoMP3();

			} else if ("EXPORTTOOGG".equals(e.getActionCommand())) { //$NON-NLS-1$
				savetoOGG();

			} else if ("EXPORTMOV".equals(e.getActionCommand())) { //$NON-NLS-1$

				savetoMOV();

			} else if ("JOUER".equals(e.getActionCommand())) { //$NON-NLS-1$

				if (currentPlaySubSystem instanceof NeedInstrument) {
					NeedInstrument ni = (NeedInstrument) currentPlaySubSystem;
					ni.setCurrentInstrument(getSelectedInstrument());
				}

				final long stop = pianoroll.getVirtualBook().getLength();

				// debut du jeu ...

				long startPlayPos = 0;

				if (pianoroll.hasHightlight()) {

					startPlayPos = pianoroll
							.MMToTime(pianoroll.getHightlight());
					// si on est à la fin, on redémarre

					if (startPlayPos - 10000 >= stop)
						startPlayPos = 0;

				}

				// start At ...

				IPlaySubSystemFeedBack fb = new IPlaySubSystemFeedBack() {

					long lastpos = -1;

					public void playStarted() {

						logger.debug("play started"); //$NON-NLS-1$

						jouer.setText(Messages.getString("APrint.28")); //$NON-NLS-1$
						jouer.setActionCommand("STOP"); //$NON-NLS-1$
						jouer.setIcon(new ImageIcon(getClass().getResource(
								"noatunstop.png"))); //$NON-NLS-1$
						try {
							checkState();
						} catch (Throwable t) {
							logger.error("error in start playing :" //$NON-NLS-1$
									+ t.getMessage(), t);
						}
						pianoroll.setUseFastDrawing(true);

					}

					public void playStopped() {

						try {

							logger.debug("play stopped"); //$NON-NLS-1$
							Runnable runnable = new Runnable() {
								public void run() {

									pianoroll.setUseFastDrawing(false);

									jouer.setText(Messages
											.getString("APrint.31")); //$NON-NLS-1$
									jouer.setActionCommand("JOUER"); //$NON-NLS-1$
									jouer.setIcon(new ImageIcon(getClass()
											.getResource("noatunplay.png"), //$NON-NLS-1$
											Messages.getString("APrint.80"))); //$NON-NLS-1$

									try {
										checkState();
									} catch (Throwable t) {
										logger.error(
												"error in stopping playing :" //$NON-NLS-1$
														+ t.getMessage(), t);
									}

								}

							};

							if (SwingUtilities.isEventDispatchThread()) {
								runnable.run();
							} else {
								SwingUtilities.invokeAndWait(runnable);
							}

							logger.debug("end of play stopped"); //$NON-NLS-1$

						} catch (Exception ex) {
							logger.error("error in playstopped :" //$NON-NLS-1$
									+ ex.getMessage(), ex);
						}
					}

					long nanoDiplayTime = 0;

					public long informCurrentPlayPosition(final long pos) {

						try {

							SwingUtilities.invokeAndWait(new Runnable() {
								public void run() {
									// not executed in swing thread in the call
									// ...

									double posx = pianoroll.getVirtualBook()
											.getScale().getSpeed()
											* (((double) pos) / 1000000);

									if (pos != lastpos) {

										// change the selection :-)
										// query the holes under the hightlight

										ArrayList<Hole> s = pianoroll
												.getVirtualBook().findHoles(
														pos, 0);

										pianoroll.clearSelection();
										for (Iterator<Hole> iterator = s
												.iterator(); iterator.hasNext();) {
											Hole hole = iterator.next();
											pianoroll.addToSelection(hole);
										}

										lastpos = pos;
									}

									Dimension d = pianoroll.getSize();

									// highlight invoke repaint()
									pianoroll.setHightlight(posx);
									// pour le déplacement du carton dans
									// l'interface
									// inderlying repaint ..
									pianoroll.setXoffset(posx
											- pianoroll.pixelToMM(d.width / 2));

									if (pos >= stop - 10000)
										SwingUtilities
												.invokeLater(new Runnable() {
													public void run() {
														actionPerformed(new ActionEvent(
																this, 0, "STOP")); //$NON-NLS-1$
													}
												});

									nanoDiplayTime = pianoroll
											.getDisplayNanos();

								}
							});

						} catch (Exception ex) {
							logger.error("error in playstopped :" //$NON-NLS-1$
									+ ex.getMessage(), ex);

						}

						return nanoDiplayTime;

					}
				};

				try {
					currentPlaySubSystem.play(APrint.this, transposedCarton,
							fb, startPlayPos);
				} catch (Exception ex) {
					logger.error("jouer", ex); //$NON-NLS-1$
					JMessageBox
							.showMessage(
									this,
									Messages.getString("APrint.74") + ex.getMessage() + Messages.getString("APrint.75")); //$NON-NLS-1$ //$NON-NLS-2$
				}

			} else if ("STOP".equals(e.getActionCommand())) { //$NON-NLS-1$

				synchronized (this) {
					currentPlaySubSystem.stop();
				}

			} else if ("PREVIEW".equals(e.getActionCommand())) { //$NON-NLS-1$
				launchPrintPreview();
			} else if ("QUITTER".equals(e.getActionCommand())) { //$NON-NLS-1$

				freeTemporaryResources();

				setTerminateState(TERMINATE);
				setVisible(false);

			} else if ("CHOICEGAMMEFOLDER".equals(e.getActionCommand())) { //$NON-NLS-1$
				changeGammeFolder();
			} else if ("TRACER".equals(e.getActionCommand())) { //$NON-NLS-1$
				launchTrace();
			}

		} catch (Exception ex) {
			logger.error("actionPerformed", ex); //$NON-NLS-1$

			BugReporter.sendBugReport();
			JMessageBox.showMessage(this,
					Messages.getString("APrint.42") + ex.getMessage()); //$NON-NLS-1$

		}
	}

	private void saveAsMidiFile() throws InvalidMidiDataException, IOException {
		// demande du fichier à sauvegarder ...
		JFileChooser choose = new JFileChooser();

		choose.setFileSelectionMode(JFileChooser.FILES_ONLY);

		choose.setFileFilter(new FileNameExtensionFilter("Fichier Midi", "mid")); //$NON-NLS-1$ //$NON-NLS-2$

		if (choose.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			Sequence seq = EcouteConverter.convert(transposedCarton, 120);
			File selfile = choose.getSelectedFile();

			if (!selfile.getAbsolutePath().toLowerCase().endsWith(".mid")) { //$NON-NLS-1$
				selfile = new File(selfile.getAbsolutePath() + ".mid"); //$NON-NLS-1$
			}

			MidiSystem.write(seq, 0, selfile);
			JMessageBox.showMessage(this, "" //$NON-NLS-1$
					+ choose.getSelectedFile() + ""); //$NON-NLS-1$
		}

	}

	private void saveWAV() throws InvalidMidiDataException, IOException,
			Exception {
		// demande du fichier à sauvegarder ...
		JFileChooser choose = new JFileChooser();

		choose.setFileSelectionMode(JFileChooser.FILES_ONLY);

		choose.setFileFilter(new FileNameExtensionFilter(Messages
				.getString("APrint.123"), //$NON-NLS-1$
				"wav")); //$NON-NLS-1$

		if (choose.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

			Sequence seq = EcouteConverter.convert(transposedCarton);

			// Chargement de l'instrument ...

			Soundbank sb = null;

			if (getSelectedInstrument() != null) {
				// play with custom sound ...

				org.barrelorgandiscovery.instrument.Instrument ins = getSelectedInstrument();

				sb = ins.openSoundBank();
				if (sb == null) {
					throw new Exception("Fail to load instrument"); //$NON-NLS-1$
				}

			}

			File choosenfile = choose.getSelectedFile();
			if (!choosenfile.getName().toLowerCase().endsWith(".wav")) { //$NON-NLS-1$
				choosenfile = new File(choosenfile.getAbsolutePath() + ".wav"); //$NON-NLS-1$
			}

			SequencerTools.render(sb, seq, choosenfile, true);

			JMessageBox.showMessage(this, Messages.getString("APrint.125") //$NON-NLS-1$
					+ " " //$NON-NLS-1$
					+ choosenfile + " " //$NON-NLS-1$
					+ Messages.getString("APrint.126")); //$NON-NLS-1$

		}
	}

	private void savetoMOV() throws Exception {
		// demande du fichier à sauvegarder ...
		JFileChooser choose = new JFileChooser();

		choose.setFileSelectionMode(JFileChooser.FILES_ONLY);

		choose.setFileFilter(new FileNameExtensionFilter("Fichier MOV", //$NON-NLS-1$
				"mov")); //$NON-NLS-1$

		if (choose.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

			// Chargement de l'instrument ...

			Soundbank sb = null;

			if (getSelectedInstrument() != null) {
				// play with custom sound ...

				org.barrelorgandiscovery.instrument.Instrument ins = getSelectedInstrument();

				sb = ins.openSoundBank();
				if (sb == null) {
					throw new Exception("Fail to load instrument"); //$NON-NLS-1$
				}

			}

			File choosenfile = choose.getSelectedFile();
			if (!choosenfile.getName().toLowerCase().endsWith(".mov")) { //$NON-NLS-1$
				choosenfile = new File(choosenfile.getAbsolutePath() + ".mov"); //$NON-NLS-1$
			}

			final File writtenFile = choosenfile;

			final ProgressIndicator p = new ProgressIndicator() {
				public void progress(double progress, String message) {
					infiniteChangeText("" + ((int) (progress * 100)) + " % " //$NON-NLS-1$ //$NON-NLS-2$
							+ (message == null ? "" : " -  " + message)); //$NON-NLS-1$ //$NON-NLS-2$
				}
			};

			final CancelTracker ct = new CancelTracker();

			Runnable r = new Runnable() {
				public void run() {
					try {
						APrint.this.infiniteStartWait(
								Messages.getString("APrint.315"), ct); //$NON-NLS-1$

						MovieConverterParameters parameters = new MovieConverterParameters();

						MovieConverter.convertToMovie(transposedCarton,
								getSelectedInstrument(), writtenFile, p, ct,
								parameters);

						JMessageBox.showMessage(APrint.this, "File " + " " //$NON-NLS-1$ //$NON-NLS-2$
								+ writtenFile + " " //$NON-NLS-1$
								+ "successfully exported"); //$NON-NLS-1$

						APrint.this.infiniteEndWait();
					} catch (InterruptedException ex) {
						logger.debug("interrupted exception"); //$NON-NLS-1$
						APrint.this.infiniteEndWait();
					} catch (Throwable ex) {
						logger.error("Error while exporting to movie :" //$NON-NLS-1$
								+ ex.getMessage(), ex);
						JMessageBox.showMessage(APrint.this,
								Messages.getString("APrint.6") //$NON-NLS-1$
										+ ex.getMessage());
						APrint.this.infiniteEndWait();
					}
				}
			};

			backgroundexecutor.execute(r);

		}

	}

	private void savetoMP3() throws InvalidMidiDataException, IOException,
			Exception {
		// demande du fichier à sauvegarder ...
		JFileChooser choose = new JFileChooser();

		choose.setFileSelectionMode(JFileChooser.FILES_ONLY);

		choose.setFileFilter(new FileNameExtensionFilter("Fichier MP3", "mp3")); //$NON-NLS-1$ //$NON-NLS-2$

		if (choose.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

			Sequence seq = EcouteConverter.convert(transposedCarton);

			// Chargement de l'instrument ...

			Soundbank sb = null;

			if (getSelectedInstrument() != null) {
				// play with custom sound ...

				org.barrelorgandiscovery.instrument.Instrument ins = getSelectedInstrument();

				sb = ins.openSoundBank();
				if (sb == null) {
					throw new Exception("Fail to load instrument"); //$NON-NLS-1$
				}

			}

			File choosenfile = choose.getSelectedFile();
			if (!choosenfile.getName().toLowerCase().endsWith(".mp3")) { //$NON-NLS-1$
				choosenfile = new File(choosenfile.getAbsolutePath() + ".mp3"); //$NON-NLS-1$
			}

			File tmpfile = File.createTempFile("temp", "wav"); //$NON-NLS-1$ //$NON-NLS-2$

			SequencerTools.render(sb, seq, tmpfile, true);

			MP3Tools.convert(tmpfile.getAbsolutePath(),
					choosenfile.getAbsolutePath());

			JMessageBox.showMessage(this, Messages.getString("APrint.125") //$NON-NLS-1$
					+ " " //$NON-NLS-1$
					+ choosenfile + " " //$NON-NLS-1$
					+ Messages.getString("APrint.126")); //$NON-NLS-1$

		}
	}

	private void savetoOGG() throws InvalidMidiDataException, IOException,
			Exception {
		// demande du fichier à sauvegarder ...
		JFileChooser choose = new JFileChooser();

		choose.setFileSelectionMode(JFileChooser.FILES_ONLY);

		choose.setFileFilter(new FileNameExtensionFilter("Fichier OGG", "ogg")); //$NON-NLS-1$ //$NON-NLS-2$

		if (choose.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

			// Chargement de l'instrument ...

			Soundbank sb = null;

			if (getSelectedInstrument() != null) {
				// play with custom sound ...

				org.barrelorgandiscovery.instrument.Instrument ins = getSelectedInstrument();

				sb = ins.openSoundBank();
				if (sb == null) {
					throw new Exception("Fail to load instrument"); //$NON-NLS-1$
				}

			}

			File choosenfile = choose.getSelectedFile();
			if (!choosenfile.getName().toLowerCase().endsWith(".ogg")) { //$NON-NLS-1$
				choosenfile = new File(choosenfile.getAbsolutePath() + ".ogg"); //$NON-NLS-1$
			}

			final File writtenfile = choosenfile;
			final File tmpfile = File.createTempFile("temp", ".wav"); //$NON-NLS-1$ //$NON-NLS-2$
			tmpfile.deleteOnExit();

			final Soundbank sbfinal = sb;
			final Sequence seq = EcouteConverter.convert(transposedCarton);

			infiniteStartWait(Messages.getString("APrint.230")); //$NON-NLS-1$

			Thread t = new Thread(new Runnable() {
				public void run() {
					try {

						SequencerTools.render(sbfinal, seq, tmpfile, true);

						APrint.this.infiniteChangeText(Messages
								.getString("APrint.231")); //$NON-NLS-1$

						OGGTools.convert(tmpfile, writtenfile);

						APrint.this.infiniteEndWait();

						JMessageBox.showMessage(APrint.this,
								Messages.getString("APrint.125") //$NON-NLS-1$
										+ " " //$NON-NLS-1$
										+ writtenfile + " " //$NON-NLS-1$
										+ Messages.getString("APrint.126")); //$NON-NLS-1$

					} catch (Exception ex) {
						logger.error("export as ogg", ex); //$NON-NLS-1$
						BugReporter.sendBugReport();
						APrint.this.infiniteEndWait();

					}
				}
			});

			t.start();

		}
	}

	private AbstractTransformation getCurrentTransposition() {
		return (AbstractTransformation) listetransposition.getSelectedItem();
	}

	/**
	 * Methode de transposition du carton pour un orgue donné
	 * 
	 * @throws Exception
	 */
	private void transposeCarton() throws Exception {

		AbstractTransformation at = getCurrentTransposition();

		assert at != null;

		assert readCarton != null;

		if (at instanceof AbstractTransposeVirtualBook) {

			infiniteChangeText(Messages.getString("APrint.155")); //$NON-NLS-1$

			TranspositionResult tr = Transpositor.transpose(readCarton,
					(AbstractTransposeVirtualBook) at);

			if (tr.untransposedholes != null
					&& tr.untransposedholes.size() != 0) {

				// JMessageBox.showMessage(this,
				// Messages.getString("APrint.33")); //$NON-NLS-1$
				//
				// APrintRapportNonTransposee r = new
				// APrintRapportNonTransposee(
				// this, tr.notesnontransposees, readCarton.getGamme());
				// SwingUtils.center(r);
				// // r.setModal(true);
				// r.setVisible(true);

				// ajout des problèmes détectés....

				// patch for issueCollection ...

				infiniteChangeText("get the errors ...");

				// TODO
				il.setIssueCollection(
						TranspositionIssueConverter.convert(tr,
								readCarton.getScale()), tr.virtualbook);

			} else {
				// il n'y a pas d'erreurs

				infiniteChangeText("no errors to get ...");

				il.setIssueCollection(null, null);
			}

			transposedCarton = tr.virtualbook;
			pianoroll.setVirtualBook(transposedCarton);
			pianoroll.touchBook();

		} else if (at instanceof AbstractMidiImporter) {

			infiniteChangeText(Messages.getString("APrint.156")); //$NON-NLS-1$

			AbstractMidiImporter mi = (AbstractMidiImporter) at;

			askForImportParametersIfExist(mi);

			infiniteChangeText(Messages.getString("APrint.252")); //$NON-NLS-1$

			logger.debug("read midi file ..."); //$NON-NLS-1$

			MidiConversionResult r = mi.convert(MidiFileIO.read(midifile));

			if (r.issues != null) {
				JMessageBox.showMessage(this, Messages.getString("APrint.33")); //$NON-NLS-1$
			}

			logger.debug("converting the errors ... ");
			// TODO convert the errors ...

			logger.debug("get the result ..."); //$NON-NLS-1$
			transposedCarton = r.virtualbook;
			il.setIssueCollection(null, null); // reset the errors
			pianoroll.setVirtualBook(transposedCarton);
			pianoroll.touchBook();

		} else {
			throw new Exception("implementation error"); //$NON-NLS-1$
		}

		changeBookTabTitle(Messages.getString("APrint.207") //$NON-NLS-1$
				+ (midifile != null ? " : " + this.midifile.getName() : "")); //$NON-NLS-1$ //$NON-NLS-2$

		assert transposedCarton != null;

		infiniteChangeText(Messages.getString("APrint.157")); //$NON-NLS-1$

		// Dans les deux cas, on regarde les trous se téléscopant ...
		OverlappingHole oh = new OverlappingHole();
		IssueCollection ic = oh.check(transposedCarton);

		infiniteChangeText(Messages.getString("APrint.158")); //$NON-NLS-1$

		// Autres vérifications associées à la gamme ...
		Checker[] c = CheckerFactory.createCheckers(at.getScaleDestination());
		Checker composite = CheckerFactory.toComposite(c);

		ic.addAll(composite.check(transposedCarton));

		IssueCollection tmpissuecollection = ic;
		if (il.getIssueCollection() != null) {
			IssueCollection tmp = il.getIssueCollection();
			tmpissuecollection.addAll(tmp);
		}

		il.setIssueCollection(tmpissuecollection, transposedCarton);

		issuePresenter.setIssueLayer(il);
		issuePresenter.setVirtualBook(transposedCarton);

		pianoroll.clearHightlight();

		// call extensions
		logger.debug("call extensions"); //$NON-NLS-1$
		InformCurrentVirtualBookExtensionPoint[] allPoints = ExtensionPointProvider
				.getAllPoints(InformCurrentVirtualBookExtensionPoint.class,
						exts);
		for (int i = 0; i < allPoints.length; i++) {
			logger.debug("call " + allPoints[i]); //$NON-NLS-1$
			InformCurrentVirtualBookExtensionPoint currentVirtualBook = allPoints[i];
			try {
				currentVirtualBook.informCurrentVirtualBook(transposedCarton);
			} catch (Throwable t) {
				logger.error("extension " + currentVirtualBook //$NON-NLS-1$
						+ " throw an exception ", t); //$NON-NLS-1$
			}
		}

		repaint();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.gui.aprint.IWaitPanelInterface#infiniteStartWait
	 * (java.lang.String, org.barrelorgandiscovery.gui.ICancelTracker)
	 */
	public void infiniteStartWait(String text, ICancelTracker cancelTracker) {

		assert !infiniteprogresspanel.isStarted();
		final String finalText = text;

		infiniteprogresspanel.setCancelTracker(cancelTracker);

		Runnable r = new Runnable() {
			public void run() {
				infiniteprogresspanel.start(finalText);
			}
		};

		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (Exception ex) {
				logger.error("infiniteStartWait :" + ex.getMessage(), ex); //$NON-NLS-1$
			}
		} else {
			r.run();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.gui.aprint.IWaitPanelInterface#infiniteStartWait
	 * (java.lang.String)
	 */
	public void infiniteStartWait(String text) {
		infiniteStartWait(text, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.gui.aprint.IWaitPanelInterface#infiniteEndWait()
	 */
	public void infiniteEndWait() {

		Runnable r = new Runnable() {
			public void run() {
				infiniteprogresspanel.stop();
			}
		};
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (Exception ex) {
				logger.error("infiniteEndWait :" + ex.getMessage(), ex); //$NON-NLS-1$
			}
		} else {
			r.run();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.gui.aprint.IWaitPanelInterface#infiniteChangeText
	 * (java.lang.String)
	 */
	public void infiniteChangeText(final String text) {

		Runnable r = new Runnable() {
			public void run() {
				infiniteprogresspanel.setText(text);
			}
		};

		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (Exception ex) {
				logger.error("infiniteChangeText :" + ex.getMessage(), ex); //$NON-NLS-1$
			}
		} else {
			r.run();
		}

	}

	private void switchToInstrumentChoice() {

		tabs.setSelectedIndex(0);

		// final int currentsize = splitpane.getDividerLocation();
		// final int finalsize = 0;
		// executor.execute(new Runnable() {
		// public void run() {
		// try {
		// for (int i = 0; i < 10; i++) {
		// splitpane.setDividerLocation((finalsize - currentsize)
		// / 10 * (i + 1) + currentsize);
		// repaint();
		// Thread.sleep(150);
		// }
		// } catch (InterruptedException ex) {
		//
		// }
		// }
		// });
	}

	private void switchToPianoRollView() {
		tabs.setSelectedIndex(1);
	}

	private void launchPrintPreview() {
		if (readCarton == null || listetransposition.getSelectedItem() == null)
			return;

		// prévisualisation ...
		try {

			CartonVirtuelPrintDocument d = new CartonVirtuelPrintDocument(
					transposedCarton, 72);

			PrintPreview p = new PrintPreview(d, lastPrintPageFormat);

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}

	private void changeGammeFolder() throws RepositoryException, IOException {
		// choix du répertoire de gamme ...
		JDirectoryChooser dc = new JDirectoryChooser(
				Messages.getString("APrintApplication.2")); //$NON-NLS-1$

		dc.setSelectedFile(aprintproperties.getGammeAndTranlation());

		int ret = dc
				.showDialog(null, Messages.getString("APrintApplication.3")); //$NON-NLS-1$

		if (ret != JDirectoryChooser.CANCEL_OPTION) //$NON-NLS-1$
		{
			File repertoire = dc.getSelectedFile();
			defineNewGammeAndTranspositionFolder(repertoire);
			repositoryChanged();

			// rafraichissement des transpositions ...
			refreshTranspositions();

			// mémorisation du répertoire contenant le repository
			aprintproperties.setGammeAndTranlation(repertoire);
		}
	}

	private void launchTrace() {
		// Transposition du carton

		try {
			TranspositionResult tr = Transpositor.transpose(readCarton,
					(AbstractTransposeVirtualBook) listetransposition
							.getSelectedItem());

			// Vérification de la traduction ...

			if (tr.untransposedholes != null
					&& tr.untransposedholes.size() != 0) {
				JMessageBox.showMessage(this, Messages.getString("APrint.33")); //$NON-NLS-1$
			}

			// Affichage des paramètres

			APrintTraceParameters tp = new APrintTraceParameters(this);
			tp.setModal(true);
			tp.setLocationByPlatform(true);
			tp.setVisible(true);

			if (tp.getValue() != Double.NaN) {
				// l'utilisateur a saisi une bonne valeur ...

				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				recurseSetEnable((JPanel) this.getContentPane(), false);
				update(this.getGraphics());

				ATrace t = new ATrace(tr.virtualbook);

				t.setTitle(Messages.getString("APrint.51")); //$NON-NLS-1$

				t.setSize(600, 600);
				t.setVisible(true);
				t.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				t.update(t.getGraphics());

				class RunOptimization implements Runnable {

					private GeneticOptimizer gop;

					private ATrace trace;

					private VirtualBook cv;

					public RunOptimization(GeneticOptimizer opt, ATrace trace,
							VirtualBook cv) {
						this.gop = opt;
						this.trace = trace;
						this.cv = cv;
					}

					public void run() {
						try {
							// optimisation du résultat ...
							OptimizerResult res = gop.optimize(cv);

							trace.setPunch(res.result);
							trace.setCursor(Cursor.getDefaultCursor());

						} catch (Exception ex) {
							logger.error("RunOptimization", ex); //$NON-NLS-1$
						}

						recurseSetEnable((JPanel) APrint.this.getContentPane(),
								true);

						APrint.this.setCursor(Cursor.getDefaultCursor());
					}

				}

				GeneticOptimizer op = new GeneticOptimizer();
				op.setPoinconsize(tp.getValue());
				RunOptimization ro = new RunOptimization(op, t, tr.virtualbook);

				logger.debug("Dispatch thread ? " //$NON-NLS-1$
						+ SwingUtilities.isEventDispatchThread());

				new Thread(ro).start();

			}
		} catch (Exception ex) {
			logger.error("launchTrace", ex); //$NON-NLS-1$
			JMessageBox.showMessage(this, ex.getMessage());
		}
	}

	private PageFormat lastPrintPageFormat = null;

	// private JSplitPane splitpane;

	private JCheckBox showTimeLayerCheckBox;

	private JCheckBox showErrorsLayerCheckBox;

	private IExtension[] exts;

	private JTabbedPane tabs;

	private JToolBar buttonActionToolbar = new JToolBar();

	private JPanel panneauTranspositionEtConversion;

	/**
	 * Routine de lancement de l'impression
	 */
	private void printCarton() {

		if (readCarton == null || listetransposition.getSelectedItem() == null)
			return;

		// choix de l'imprimante, puis impression ...
		try {

			PrinterJob pjob = PrinterJob.getPrinterJob();

			PageFormat pageformat = lastPrintPageFormat;
			if (pageformat == null) {
				pageformat = pjob.defaultPage();
			}

			PageFormat newpageformat = pjob.pageDialog(pageformat);

			// pb avec les 300 dpi, si l'on met 300 dpi, ca devrai passer avec
			// les imprimantes ... non actuellement
			// testé, mais en PDF, c'est pas bon !!

			CartonVirtuelPrintDocument d = new CartonVirtuelPrintDocument(
					transposedCarton, 72);

			// if (newpageformat != pageformat) {
			pjob.setPrintable(d, newpageformat);

			lastPrintPageFormat = newpageformat;

			// Affiche la boite de dialogue standard

			if (pjob.printDialog()) { // PF anciennement en commentaire
				// ...

				//
				// HashPrintRequestAttributeSet as = new
				// HashPrintRequestAttributeSet();

				// cette propriété ne fonctionne pas sur toutes les imprimantes
				// ...
				// as.add(new PrinterResolution(300, 300,
				// PrinterResolution.DPI));
				// pjob.print(as);

				pjob.print();

			} // PF anciennement en commentaire

			// } // sinon l'utilisateur a clické sur annuler ...

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}

	private void loadMidi() {
		JFileChooser choose = new JFileChooser();

		choose.setFileFilter(new FileNameExtensionFilter(Messages
				.getString("APrint.23"), //$NON-NLS-1$
				new String[] { "mid", "kar" })); //$NON-NLS-1$ //$NON-NLS-2$

		choose.setFileSelectionMode(JFileChooser.FILES_ONLY);

		if (aprintproperties.getLastMidiFile() != null) {
			choose.setSelectedFile(aprintproperties.getLastMidiFile());
		}

		if (choose.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			// Récupération du nom de fichier
			final File result = choose.getSelectedFile();

			aprintproperties.setLastMidiFile(result);

			if (result != null) {
				// Chargement du carton virtuel ..

				try {

					pianoroll.setVirtualBook(null);
					transposedCarton = null;

					backgroundexecutor.execute(new Runnable() {
						public void run() {
							try {

								infiniteStartWait(Messages
										.getString("APrint.159")); //$NON-NLS-1$

								infiniteChangeText(Messages
										.getString("APrint.160")); //$NON-NLS-1$

								SwingUtilities.invokeAndWait(new Runnable() {
									public void run() {
										switchToInstrumentChoice();
									}
								});

								readCarton = MidiIO.readCarton(result);
								nomfichier.setText(result.getName());
								midifile = result;

								SwingUtilities.invokeAndWait(new Runnable() {
									public void run() {
										try {
											checkState();
											repaint();

										} catch (Exception ex) {
											logger.error("load midi", ex); //$NON-NLS-1$
											JMessageBox.showMessage(
													APrint.this,
													ex.getMessage());
										}
									}
								});

								infiniteEndWait();

							} catch (Exception ex) {
								logger.error("load midi", ex); //$NON-NLS-1$
								JMessageBox.showMessage(APrint.this,
										ex.getMessage());
								infiniteEndWait();
							}
						}
					});

				} catch (Exception ex) {
					// Erreur de chargement du fichier

					// Affichage du problème ..
					logger.error("Chargement du carton", ex); //$NON-NLS-1$
				}

			}

		}

	}

	/**
	 * Rafraichit la liste des transposition en fonction de la gamme, ou de
	 * l'instrument sélectionné sélectionnée
	 */
	private void refreshTranspositions() {

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			// On récupère la gamme sélectionnée dans la liste

			Scale g = getSelectedGamme();

			// assert g != null;

			listetransposition.removeAllItems();
			if (g != null) {

				logger.debug("adding transpositions"); //$NON-NLS-1$
				ArrayList<AbstractTransformation> t = tm.findTransposition(
						Scale.getGammeMidiInstance(), g);

				for (int i = 0; i < t.size(); i++) {
					listetransposition.addItem(t.get(i));
				}

				logger.debug("Ajout des scripts .. "); //$NON-NLS-1$

				ArrayList<AbstractMidiImporter> mi = tm.findImporter(g);
				for (AbstractMidiImporter abstractMidiImporter : mi) {
					listetransposition.addItem(abstractMidiImporter);
				}

				logger.debug("adding the extensions importers ..."); //$NON-NLS-1$

				ArrayList<AbstractMidiImporter> extension_importers = new ArrayList<AbstractMidiImporter>();
				ImportersExtensionPoint[] pts = ExtensionPointProvider
						.getAllPoints(ImportersExtensionPoint.class, exts);
				for (int i = 0; i < pts.length; i++) {
					ImportersExtensionPoint importersExtensionPoint = pts[i];
					try {
						ArrayList<AbstractMidiImporter> result = importersExtensionPoint
								.getExtensionImporterInstance(g);
						if (result != null)
							extension_importers.addAll(result);
					} catch (Throwable th) {
						logger.error("extension " + importersExtensionPoint //$NON-NLS-1$
								+ " throw and exception", th); //$NON-NLS-1$
					}
				}

				for (Iterator<AbstractMidiImporter> iterator = extension_importers
						.iterator(); iterator.hasNext();) {
					AbstractMidiImporter abstractMidiImporter2 = iterator
							.next();
					logger.debug("adding " + abstractMidiImporter2); //$NON-NLS-1$
					listetransposition.addItem(abstractMidiImporter2);
				}

			}
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}
	}

	private class InternalRepositoryListenerClass implements
			RepositoryChangedListener {

		public void instrumentsChanged() {
			logger.debug("APrint receive an instrumentsChanged from repository"); //$NON-NLS-1$

		}

		public void scalesChanged() {
			logger.debug("APrint receive a scalesChanged from repository"); //$NON-NLS-1$
			try {
				Runnable runnable = new Runnable() {
					public void run() {

						populateImprimerGammeMenu(popupmenuImprimerGamme);

					}
				};

				if (SwingUtilities.isEventDispatchThread()) {
					runnable.run();

				} else {

					SwingUtilities.invokeAndWait(runnable);
				}

			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}

		}

		public void transformationAndImporterChanged() {
			logger.debug("APrint receive a transformationAndImporterChanged from repository"); //$NON-NLS-1$

		}

	}

	private InternalRepositoryListenerClass internalRepositoryListenerClass = new InternalRepositoryListenerClass();

	/**
	 * charge les nouvelles gammes contenues dans le répertoire
	 * 
	 * @param folder
	 *            le répertoire contenant des gammes
	 */
	private void defineNewGammeAndTranspositionFolder(File folder)
			throws RepositoryException {

		Properties repprop = new Properties();
		if (folder != null) {
			repprop.setProperty("folder", folder.getAbsolutePath()); //$NON-NLS-1$
		}

		RepositoryAdapter ra = new RepositoryAdapter(Repository2Factory.create(
				repprop, this.aprintproperties));

		if (repository != null) {
			// desabonnement sur les modifications du repository
			Repository2 r2 = repository.getRepository2();
			r2.removeRepositoryChangedListener(internalRepositoryListenerClass);
			if (r2 instanceof Disposable) {
				((Disposable) r2).dispose();
			}
		}

		// abonnement ...

		ra.getRepository2().addRepositoryChangedListener(
				internalRepositoryListenerClass);

		repository = ra;

		gm = repository.getScaleManager();
		tm = repository.getTranspositionManager();

		informExtensionsAboutRepository();

		populateImprimerGammeMenu(popupmenuImprimerGamme);
	}

	/**
	 * this function is called when the repository changed
	 */
	private void repositoryChanged() {
		instrumentChoice.setRepository(this.repository.getRepository2());
	}

	/**
	 * Récupère la gamme actuellement sélectionnée, null si celle ci n'est pas
	 * sélectionnée
	 * 
	 * @return
	 */
	private Scale getSelectedGamme() {

		// Récupération de l'instrument et de la gamme associée ...

		org.barrelorgandiscovery.instrument.Instrument ins = getSelectedInstrument();
		if (ins == null) {
			return null; // pas de gamme sélectionnée ...
		}

		return (Scale) ins.getScale();
	}

	/**
	 * internal function than ask for parameters in the translation ..
	 * 
	 * @param amimport
	 *            the importer ...
	 * @throws Exception
	 */
	private void askForImportParametersIfExist(
			final AbstractMidiImporter amimport) throws Exception {

		if (amimport instanceof CustomImporterParameters) {

			logger.debug("asking for custom parameters in translation"); //$NON-NLS-1$

			// on est invoké par un thread séparé
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {

					try {
						final CustomImporterParameters parametersinterface = (CustomImporterParameters) amimport;

						logger.debug("asking for custom parameters"); //$NON-NLS-1$
						parametersinterface.showCustomImporterParameters();

						logger.debug("done !"); //$NON-NLS-1$

					} catch (Throwable ex) {
						logger.error(
								"error in getting the importer parameters , " //$NON-NLS-1$
										+ ex.getMessage(), ex);

						BugReporter.sendBugReport();
					}
				}
			});

		} else if (amimport instanceof ImporterParameters) {

			infiniteChangeText(Messages.getString("APrint.253")); //$NON-NLS-1$

			// on est invoké par un thread séparé
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {

					try {
						final ImporterParameters parametersinterface = (ImporterParameters) amimport;

						final Object parameterbean = parametersinterface
								.getParametersInstanceBean();
						if (parameterbean == null)
							return;

						assert parameterbean != null;

						// test the bean info
						// To check if the bean info has been provided, if it
						// returns null,
						// no
						// bean info is provided

						// looking for the beaninfo associated to the parameters
						// ...

						ClassLoader cl = parameterbean.getClass()
								.getClassLoader();

						BeanInfo bi;

						String beaninfoclassname = parameterbean.getClass()
								.getName() + "BeanInfo"; //$NON-NLS-1$

						logger.debug("loading class " + beaninfoclassname //$NON-NLS-1$
								+ " in the extension classloader"); //$NON-NLS-1$
						try {

							bi = (BeanInfo) cl.loadClass(beaninfoclassname)
									.newInstance();

						} catch (Exception ex) {
							logger.error("no beaninfo for parameter bean " //$NON-NLS-1$
									+ parameterbean.toString(), ex);
							throw new Exception(
									"no parameter beaninfo for parameters", ex); //$NON-NLS-1$
						}

						if (bi == null) {
							throw new Exception(
									"no beaninfo for parameter bean " //$NON-NLS-1$
											+ parameterbean.toString());
						}

						final JDialog parameterDialog = new JDialog(
								APrint.this, Messages.getString("APrint.239")); //$NON-NLS-1$

						PropertySheetPanel p = new PropertySheetPanel();
						p.setDescriptionVisible(true);

						new BeanBinder(parameterbean, p, bi);

						Container contentPane = parameterDialog
								.getContentPane();
						contentPane.setLayout(new BorderLayout());
						contentPane.add(p, BorderLayout.CENTER);

						JButton ok = new JButton(Messages
								.getString("APrint.240")); //$NON-NLS-1$
						ok.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								parametersinterface
										.setParametersToUse(parameterbean);
								parameterDialog.setVisible(false);
								logger.debug(" - OK parameters modified - "); //$NON-NLS-1$
								parameterDialog.dispose();
							}
						});

						contentPane.add(ok, BorderLayout.SOUTH);
						parameterDialog.setSize(400, 600);
						SwingUtils.center(parameterDialog);

						parameterDialog.setModal(true);
						parameterDialog.setVisible(true);
					} catch (Exception ex) {
						logger.error("error while loading parameters", ex); //$NON-NLS-1$
						BugReporter.sendBugReport();
					}
				}
			});
		}
	}

	/**
	 * Verifie l'état de l'interface en fonction de l'état de l'IHM
	 */
	private void checkState() throws Exception {

		if (transposedCarton != null) {
			// le carton a été transposé / transformé

			// on regarde si on est en train de jouer
			// le morceau ....
			if (currentPlaySubSystem.isPlaying()) {
				// on désactive le bouton imprimer
				rewind.setEnabled(false);
				imprimer.setEnabled(false);
				preview.setEnabled(false);
				// tracer.setEnabled(false);
				exportAsWav.setEnabled(false);
				exportAsMidi.setEnabled(false);
				exportAsOgg.setEnabled(false);
				exportAsMovie.setEnabled(false);
				chargementmidi.setEnabled(false);
				recurseSetEnable(getJMenuBar(), false);
				recurseSetEnable(gammeTranspositionInstrumentPanel, false);

				recurseSetEnable(panneauTranspositionEtConversion, false);

			} else {
				// sinon on active tout ...

				recurseSetEnable(panelchoixfichier, true);
				recurseSetEnable(buttonActionToolbar, true);
				recurseSetEnable(exportToolbar, true);
				recurseSetEnable(gammeTranspositionInstrumentPanel, true);
				recurseSetEnable(panneauTranspositionEtConversion, true);
				recurseSetEnable(getJMenuBar(), true);
			}

		} else {
			if (readCarton == null) {
				// pas de carton chargé

				recurseSetEnable(panelchoixfichier, true);
				recurseSetEnable(buttonActionToolbar, false); // buttonPanel.setEnabled(false);
				recurseSetEnable(exportToolbar, false);
				recurseSetEnable(gammeTranspositionInstrumentPanel, false); // buttonPanel.setEnabled(false);
				recurseSetEnable(panneauTranspositionEtConversion, false);
			} else {
				// un carton a été chargé

				// on active que si la liste des instruments n'est pas vide ...
				recurseSetEnable(panelchoixfichier, true);
				recurseSetEnable(buttonActionToolbar, false); // buttonPanel.setEnabled(false);

				recurseSetEnable(exportToolbar, false);
				// on peut changer la transposition et l'instrument et
				// déclencher les actions
				recurseSetEnable(gammeTranspositionInstrumentPanel, true); // buttonPanel.setEnabled(false);
				recurseSetEnable(panneauTranspositionEtConversion, true);
			}
		}

		if (scaleAndInstrumentEditor != null) {

			if (repository != null && repository.getRepository2() != null
					&& !repository.getRepository2().isReadOnly()) {
				scaleAndInstrumentEditor.setEnabled(true);
			} else {
				scaleAndInstrumentEditor.setEnabled(false);
			}
		}
		repaint();
	}

	/**
	 * permet la désactivation/activation d'un ensemble de composans
	 * 
	 * @param comp
	 * @param enable
	 */
	private static void recurseSetEnable(JComponent comp, boolean enable) {
		if (comp == null)
			return;

		comp.setEnabled(enable);
		for (int i = 0; i < comp.getComponentCount(); i++) {
			Component c = comp.getComponent(i);
			if (c instanceof JComponent) {
				recurseSetEnable((JComponent) c, enable);
			}
		}
	}

	/**
	 * Retourne la version de l'outil ...
	 * 
	 * @return
	 */
	private String getVersion() {
		Properties prop = new Properties();

		try {
			prop.load(this.getClass().getClassLoader()
					.getResourceAsStream("version.properties")); //$NON-NLS-1$
		} catch (Exception ex) {
			return Messages.getString("APrint.115"); //$NON-NLS-1$
		}

		return prop.getProperty("version"); //$NON-NLS-1$
	}

	// ////////////////////////////////////////////////////////////////////////
	// Gestion de l'état de l'interface ...

	private void instrumentChanged() {
		try {

			// transposedCarton = null; // raz de la transposition
			// pianoroll.setVirtualBook(null);
			//

			refreshTranspositions();

			choixtransposition
					.setVisible(listetransposition.getItemCount() > 1);

			checkState();

		} catch (Exception ex) {
			// Traitement de l'exception
			logger.error("instrumentChanged", ex); //$NON-NLS-1$
		}
	}

	// gestion des extensions ...
	private void shutdownExtension(IExtension ext) {
		// terminaison des extensions ...

	}

	// manage the visibility of the Application

	public static final int TERMINATE = 0;
	public static final int NEED_RESTART = 1;

	private int terminate_state = TERMINATE;

	public void setTerminateState(int terminate_state) {
		this.terminate_state = terminate_state;
	}

	public int getTerminateState() {
		return this.terminate_state;
	}

	private void freeTemporaryResources() {
		if (repository != null) {
			Repository2 repository2 = repository.getRepository2();
			if (repository2 != null && repository2 instanceof Disposable) {
				((Disposable) repository2).dispose();
			}
		}
	}

	public void relaunch() {
		setTerminateState(NEED_RESTART);
		freeTemporaryResources();
		this.setVisible(false);
	}

	public void relaunchWithMessageBox() {
		JMessageBox.showMessage(this, Messages.getString("APrint.255")); //$NON-NLS-1$

		setTerminateState(NEED_RESTART);
		freeTemporaryResources();
		this.setVisible(false);
	}

	private HelpBroker hb = null;
	private HelpSet hs = null;

	private JPanel gammeparameters;

	private JToolBar exportToolbar;

	private void initHelpBroker() {

		logger.debug("initHelpBroker"); //$NON-NLS-1$

		hs = null;
		// Find the HelpSet file and create the HelpSet object:
		String helpHS = "APrintHelp.hs"; //$NON-NLS-1$
		ClassLoader cl = this.getClass().getClassLoader();
		try {

			String localsubfolder = "en"; //$NON-NLS-1$

			if ("fr".equals(Locale.getDefault().getLanguage())) //$NON-NLS-1$
				localsubfolder = "fr"; //$NON-NLS-1$

			URL hsURL = HelpSet.findHelpSet(cl,
					"doc/" + localsubfolder + "/" + helpHS); //$NON-NLS-1$ //$NON-NLS-2$
			hs = new HelpSet(null, hsURL);
		} catch (Exception ee) {
			// Say what the exception really is
			logger.error("HelpSet " + ee.getMessage()); //$NON-NLS-1$
			logger.error("HelpSet " + helpHS + Messages.getString("APrint.101")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		assert hs != null;

		// Create a HelpBroker object:
		hb = hs.createHelpBroker();

		hb.enableHelpKey(this.getRootPane(), "top", hs, //$NON-NLS-1$
				"javax.help.MainWindow", "mainSW"); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public boolean hasInstrumentsInWebRepository() {
		Repository2 repository2 = this.repository.getRepository2();
		if (repository2 instanceof Repository2Collection) {
			Repository2Collection r2c = (Repository2Collection) repository2;
			for (int i = 0; i < r2c.getRepositoryCount(); i++) {
				Repository2 r = r2c.getRepository(i);
				if (r instanceof GAESynchronizedRepository2) {
					org.barrelorgandiscovery.instrument.Instrument[] instruments = r
							.listInstruments();
					if (instruments == null)
						return false;
					return instruments.length == 0;
				}
			}
		}

		return false;

	}

	@Override
	public void dispose() {
		super.dispose();
		asyncJobsManager.dispose();
	}

}
