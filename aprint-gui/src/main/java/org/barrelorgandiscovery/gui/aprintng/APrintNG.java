package org.barrelorgandiscovery.gui.aprintng;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.Callable;

import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.JobEvent;
import org.barrelorgandiscovery.VersionOnlineChecker;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentConstants;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManager;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository2Adapter;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentStorage;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.extensions.ExtensionManager;
import org.barrelorgandiscovery.extensions.ExtensionPointProvider;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.gaerepositoryclient.GAESynchronizedRepository2;
import org.barrelorgandiscovery.gaerepositoryclient.SynchronizationFeedBack;
import org.barrelorgandiscovery.gaerepositoryclient.synchroreport.MessageSynchroElement;
import org.barrelorgandiscovery.gaerepositoryclient.synchroreport.SynchroElement;
import org.barrelorgandiscovery.gaerepositoryclient.synchroreport.SynchronizationReport;
import org.barrelorgandiscovery.gui.APrintConstants;
import org.barrelorgandiscovery.gui.ainstrument.InstrumentSelectedListener;
import org.barrelorgandiscovery.gui.ainstrument.JInstrumentTileViewerPanel;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprint.AboutFrame;
import org.barrelorgandiscovery.gui.aprint.PrintPreview;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.HelpMenuItemsExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InformRepositoryExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.OptionMenuItemsExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.InitNGExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.ToolMenuExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.VirtualBookFrameExtensionPoints;
import org.barrelorgandiscovery.gui.ascale.JEquivalentScaleChooserPanel;
import org.barrelorgandiscovery.gui.ascale.ScaleEditorPrefs;
import org.barrelorgandiscovery.gui.ascale.ScalePrintDocument;
import org.barrelorgandiscovery.gui.ascale.StandAloneScaleEditor;
import org.barrelorgandiscovery.gui.etl.JModelEditorPanel;
import org.barrelorgandiscovery.gui.repository2.JRepositoryTileFrame;
import org.barrelorgandiscovery.gui.script.groovy.APrintGroovyConsole;
import org.barrelorgandiscovery.gui.script.groovy.APrintGroovyInnerConsole;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.listeningconverter.MIDIListeningConverter;
import org.barrelorgandiscovery.messages.JTranslator;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.playsubsystem.GervillPlaySubSystemWithRegisterInstruments;
import org.barrelorgandiscovery.playsubsystem.IPlaySubSystemFeedBack;
import org.barrelorgandiscovery.playsubsystem.MidiDevicePlaySubSystem;
import org.barrelorgandiscovery.playsubsystem.NeedInstrument;
import org.barrelorgandiscovery.playsubsystem.NeedMidiListeningConverter;
import org.barrelorgandiscovery.playsubsystem.PlayControl;
import org.barrelorgandiscovery.playsubsystem.PlaySubSystem;
import org.barrelorgandiscovery.playsubsystem.prepared.IPreparedCapableSubSystem;
import org.barrelorgandiscovery.playsubsystem.prepared.IPreparedPlaying;
import org.barrelorgandiscovery.playsubsystem.prepared.ISubSystemPlayParameters;
import org.barrelorgandiscovery.playsubsystem.registry.IPlaySubSystemRegistryExtensionPoint;
import org.barrelorgandiscovery.playsubsystem.registry.PlaySubSystemDef;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Collection;
import org.barrelorgandiscovery.repository.Repository2Factory;
import org.barrelorgandiscovery.repository.RepositoryAdapter;
import org.barrelorgandiscovery.repository.RepositoryChangedListener;
import org.barrelorgandiscovery.repository.RepositoryException;
import org.barrelorgandiscovery.repository.httpxmlrepository.HttpXmlRepository;
import org.barrelorgandiscovery.repository.httpxmlrepository.HttpXmlRepository.InstrumentDefinition;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.ScaleManager;
import org.barrelorgandiscovery.scale.comparator.ScaleComparator;
import org.barrelorgandiscovery.scale.comparator.TracksAndPositionedNoteComparator;
import org.barrelorgandiscovery.search.BookIndexing;
import org.barrelorgandiscovery.tools.Dirtyable;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.StreamsTools;
import org.barrelorgandiscovery.tools.StringTools;
import org.barrelorgandiscovery.tools.SwingUtils;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.tools.html.HTMLParsingTools;
import org.barrelorgandiscovery.ui.tools.VFSTools;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.transformation.TransformationManager;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO;

import com.l2fprod.common.swing.JDirectoryChooser;

import groovy.lang.Binding;
import groovy.ui.GroovyMain;

public class APrintNG extends APrintNGInternalFrame implements ActionListener, APrintRepositoryListener,
		APrintPageFormat, APrintNGGeneralServices, DropTargetListener {

	/** */
	private static final long serialVersionUID = 5504312655158119463L;

	/** Loggeur */
	static final Logger logger = Logger.getLogger(APrintNG.class);

	// ///////////////////////////////////////////////////////////////////////////
	// Donn�es interne du formulaire

	/** Repository ... */
	private RepositoryAdapter repository = null;

	// /////////////////////////////////////////////////////////////////////////////

	/** Gestionnaire de gamme */
	private ScaleManager gm = null;

	/** Gestionnaire de transposition */
	private TransformationManager tm = null;

	/** Async job manager */
	private AsyncJobsManager asyncJobsManager;

	private JMenu popupmenuImprimerGamme = new JMenu(Messages.getString("APrint.105")); //$NON-NLS-1$

	private APrintProperties aprintproperties;

	private ExtensionManager em = null;

	private BookIndexing bookIndexing = null;

	private PlaySubSystem currentPlaySubSystem = new GervillPlaySubSystemWithRegisterInstruments();

	// private final JDesktopPane desktopPane = new JDesktopPane();

	private QuickScriptManager scriptManager = null;

	static boolean ENABLE_MODELEDITOR = true;

	public APrintNG(APrintProperties properties) throws Exception, RepositoryException {
		super(properties.getFilePrefsStorage());

		VFSTools.getManager();
		
		checkWebVersion();

		System.out.println("ClassLoader :" + getClass().getClassLoader());

		final String LIBRARY_PATH = "java.library.path"; //$NON-NLS-1$
		String javaLibraryPath = System.getProperty(LIBRARY_PATH);
		logger.info("LibraryPath in application:" + javaLibraryPath); //$NON-NLS-1$

		if (properties == null)
			throw new IllegalArgumentException();

		aprintproperties = properties;

		// script manager

		File scripts = aprintproperties.getBookQuickScriptFolder();
		if (!scripts.exists())
			scripts.mkdirs();

		scriptManager = new QuickScriptManager(scripts);

		// indexing ...

		bookIndexing = new BookIndexing(aprintproperties);

		// drag and drop for external file loading

		setDropTarget(new DropTarget(this, this));

		// loading the extensions ... from class loader

		File extensionfolder = aprintproperties.getExtensionFolder();
		// jar
		// class
		// loader
		// ...

		if (extensionfolder != null) {

			em = new ExtensionManager(extensionfolder, "extensionsng.properties"); //$NON-NLS-1$

			em.deleteInvalidatedExtensions();

			exts = em.getExtensions();
			if (logger.isInfoEnabled()) {
				for (int i = 0; i < exts.length; i++) {
					logger.info("Extension " + exts[i].getName() + " loaded"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}

		InitNGExtensionPoint[] allInitPoints = ExtensionPointProvider.getAllPoints(InitNGExtensionPoint.class, exts);
		for (int i = 0; i < allInitPoints.length; i++) {
			InitNGExtensionPoint init = allInitPoints[i];
			try {
				init.init(this);
			} catch (Throwable t) {
				logger.error("fail to init :" + init.getClass().getName() + " " //$NON-NLS-2$
						+ t.getMessage(), t);
			}
		}

		File rep = aprintproperties.getGammeAndTranlation();

		updateFrameTitle();

		setIconImage(getAPrintApplicationIcon()); // $NON-NLS-1$

		// Chargement des objets n�cessaires

		defineNewGammeAndTranspositionFolder(rep);

		logger.debug("end of reading scales ad instrument definition"); //$NON-NLS-1$

		initHelpBroker();

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		setName("aprint main window"); //$NON-NLS-1$

		// D�finir le menu

		setJMenuBar(constructMenu());

		setSize(aprintproperties.getAPrintFrameSize());

		addComponentListener(new ComponentListener() {

			public void componentHidden(ComponentEvent e) {
			}

			public void componentMoved(ComponentEvent e) {
			}

			public void componentResized(ComponentEvent e) {
				APrintNG a = (APrintNG) e.getComponent();
				aprintproperties.setAPrintFrameSize(a.getSize());
			}

			public void componentShown(ComponentEvent e) {
			}
		});

		logger.debug("end of constructor"); //$NON-NLS-1$

		asyncJobsManager = new AsyncJobsManager();

		setVisible(true);

		// setLocationByPlatform(true);
		SwingUtils.center(this);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(new APrintNGWelcomePanel(this, exts), BorderLayout.CENTER);

		getContentPane().validate();
		repaint();
	}

	/** */
	protected void updateFrameTitle() {

		String newVersion = ""; //$NON-NLS-1$
		if (APrintNG.newWebVersionName != null) {
			newVersion = " ("//$NON-NLS-1$
					+ Messages.getString("APrintNG.106") //$NON-NLS-1$
					+ newWebVersionName + ")";//$NON-NLS-1$
		}

		setTitle("APrint Studio"//$NON-NLS-1$
				+ " - "//$NON-NLS-1$
				+ Messages.getString("APrintNG.20") //$NON-NLS-1$
				+ " "//$NON-NLS-1$
				+ " - V " //$NON-NLS-1$
				+ getVersion() + newVersion);

	}

	@Override
	protected boolean askForClose() {
		// check if all the savings have been done

		APrintNGInternalFrame[] listInternalFrames = listInternalFrames();
		boolean dirty = false;

		for (int i = 0; i < listInternalFrames.length; i++) {
			APrintNGInternalFrame f = listInternalFrames[i];
			if (f != null && f instanceof Dirtyable) {
				Dirtyable dirtyable = (Dirtyable) f;
				boolean windowDirty = dirtyable.isDirty();
				if (windowDirty) {
					logger.debug("window :" + f + " is dirty");
				}
				dirty |= windowDirty;
			}
		}

		if (dirty) {
			int result = JOptionPane.showConfirmDialog(getOwner(),
					"Some work has not been saved, do you really want to quit ?");
			if (result == JOptionPane.YES_OPTION) {
				dispose();
			} else {
				setVisible(true);
				return false;
			}

			// do nothing else
		} else {
			dispose();
		}
		return true;
	}

	private static String newWebVersionName = null;

	private void checkWebVersion() {
		Thread t = new Thread(new Runnable() {
			public void run() {

				try {

					String newVersion = VersionOnlineChecker.newVersion();
					if ((newVersion != null) && (newVersion.compareTo(getVersion()) > 0)) {
						newWebVersionName = newVersion;
						SwingUtilities.invokeAndWait(new Runnable() {
							public void run() {
								updateFrameTitle();
							};
						});
					}

				} catch (Exception ex) {
					logger.error("error in getting version :" + ex.getMessage(), ex);
				}
			}
		});
		t.start();
	}

	@Override
	protected String getInternalFrameNameForPreferences() {
		return "aprintngmainwindow";
	}

	/**
	 * Display the welcome frame
	 *
	 * @throws Exception
	 */
	private void showWelcomeFrame() throws Exception {

		if (printNGWelcomeFrame == null) {

			printNGWelcomeFrame = new APrintNGWelcomeFrame(this, exts);
			printNGWelcomeFrame.pack();

			// printNGWelcomeFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

			addNewInternalFrame(printNGWelcomeFrame);

			// SwingUtils.center(printNGWelcomeFrame, this.getBounds());
		}
		printNGWelcomeFrame.setVisible(true);
		printNGWelcomeFrame.toFront();
	}

	/**
	 * Get the aprint application icon
	 *
	 * @return
	 */
	public static Image getAPrintApplicationIcon() {
		return Toolkit.getDefaultToolkit().getImage(APrintNG.class.getResource("icon.jpg")); //$NON-NLS-1$
	}

	/** Internal method for signal repository to extensions */
	private void informExtensionsAboutRepository() {
		InformRepositoryExtensionPoint[] allPoints = ExtensionPointProvider
				.getAllPoints(InformRepositoryExtensionPoint.class, exts);
		for (int i = 0; i < allPoints.length; i++) {
			InformRepositoryExtensionPoint informRepositoryExtensionPoint = allPoints[i];
			try {
				logger.debug("inform extension " //$NON-NLS-1$
						+ informRepositoryExtensionPoint + " about repository"); //$NON-NLS-1$
				informRepositoryExtensionPoint.informRepository(this.repository);
			} catch (Throwable t) {
				logger.error("Extension " + informRepositoryExtensionPoint // $NON-NLS-1$
						+ " throw an exception", t); // $NON-NLS-1$
			}
		}
	}

	/**
	 * contruct the main application menu
	 *
	 * @return the constructed menu
	 */
	private JMenuBar constructMenu() {
		JMenuBar menu = new JMenuBar();

		constructMenuFile(menu);
		constructMenuGamme(menu);

		// constructInternetRepositoryMenu(menu);

		constructMenuOutils(menu);

		constructMenuOptions(menu);

		menu.add(Box.createHorizontalGlue());

		JMemoryProgressBar mpb = new JMemoryProgressBar();
		mpb.setMaximumSize(new Dimension(50, 20));
		menu.add(mpb);

		constructMenuHelp(menu);

		return menu;
	}

	/**
	 * Construct the tools menu
	 *
	 * @param menu
	 */
	private void constructMenuOutils(JMenuBar menu) {

		JMenu toolsMenu = new JMenu(Messages.getString("APrint.316")); //$NON-NLS-1$
		toolsMenu.setIcon(new ImageIcon(getClass().getResource("ark_options.png"))); //$NON-NLS-1$

		// Editeur de gamme ...

		JMenuItem editeurgamme = toolsMenu.add(Messages.getString("APrint.117")); //$NON-NLS-1$
		editeurgamme.setIcon(new ImageIcon(getClass().getResource("kmid.png"))); //$NON-NLS-1$
		editeurgamme.addActionListener(this);
		editeurgamme.setActionCommand("SCALEDITOR"); //$NON-NLS-1$

		toolsMenu.addSeparator();

		JMenuItem groovyScriptConsole = new JMenuItem(Messages.getString("APrint.317")); //$NON-NLS-1$
		groovyScriptConsole.setIcon(new ImageIcon(GroovyMain.class.getResource("ConsoleIcon.png"))); //$NON-NLS-1$
		groovyScriptConsole.setAccelerator(KeyStroke.getKeyStroke("control G")); //$NON-NLS-1$
		groovyScriptConsole.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					openGroovyScriptConsole();

				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					JMessageBox.showMessage(APrintNG.this, Messages.getString("APrint.318") //$NON-NLS-1$
							+ ex.getMessage());
					BugReporter.sendBugReport();
				}
			}
		});

		toolsMenu.add(groovyScriptConsole);

		JMenuItem modelEditor = new JMenuItem();
		modelEditor.setText("Model Editor");
		modelEditor.setIcon(new ImageIcon(JModelEditorPanel.class.getResource("model-editor.png")));
		modelEditor.addActionListener(e -> {
			try {
				openModelEditor();
			} catch (Exception ex) {
				JMessageBox.showMessage(APrintNG.this, "Error Opening model editor :" //$NON-NLS-1$
						+ ex.getMessage());
				BugReporter.sendBugReport();
			}
		});
		modelEditor.setEnabled(ENABLE_MODELEDITOR);
		toolsMenu.add(modelEditor);

		menu.add(toolsMenu);

		// call extensions
		ToolMenuExtensionPoint[] allToolMenuExtensionPoints = ExtensionPointProvider
				.getAllPoints(ToolMenuExtensionPoint.class, exts);
		for (int i = 0; i < allToolMenuExtensionPoints.length; i++) {
			ToolMenuExtensionPoint toolmenuExtenionPoint = allToolMenuExtensionPoints[i];
			try {
				toolmenuExtenionPoint.addMenuItem(toolsMenu);
			} catch (Throwable t) {
				logger.error("error in calling extension " //$NON-NLS-1$
						+ toolmenuExtenionPoint.getClass().getName(), t);
			}
		}
	}

	/**
	 * Construction du menu aide en ligne
	 *
	 * @param menu
	 */
	private void constructMenuHelp(JMenuBar menu) {
		JMenu helpmenu = new JMenu(Messages.getString("APrint.56")); //$NON-NLS-1$
		helpmenu.setIcon(new ImageIcon(getClass().getResource("help.png"))); //$NON-NLS-1$
		helpmenu.setMnemonic('h');
		helpmenu.setHorizontalAlignment(SwingConstants.RIGHT);

		JMenuItem aide = helpmenu.add(Messages.getString("APrint.102")); //$NON-NLS-1$
		aide.setIcon(new ImageIcon(getClass().getResource("help.png"), //$NON-NLS-1$
				Messages.getString("APrint.104"))); //$NON-NLS-1$
		aide.setAction(new APrintHelpAction());

		helpmenu.add(aide);

		JMenuItem apropos = helpmenu.add(Messages.getString("APrint.57")); //$NON-NLS-1$
		apropos.setIcon(new ImageIcon(getClass().getResource("help.png"), //$NON-NLS-1$
				Messages.getString("APrint.85"))); //$NON-NLS-1$
		apropos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// lecture du fichier de propri�t�

					AboutFrame af = new AboutFrame(APrintNG.this, Messages.getString("APrint.86"), true); //$NON-NLS-1$

					StringBuffer sb = new StringBuffer();
					sb.append("<p align=\"right\"><b>APrint Version</b>:" + getVersion() + "</p>"); //$NON-NLS-2$
					// //$NON-NLS-3$

					logger.debug("reading about file ... "); //$NON-NLS-1$

					af.setAboutContent(APrintNG.this.getClass().getResourceAsStream("about.xml")); //$NON-NLS-1$

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
					JMessageBox.showMessage(APrintNG.this, Messages.getString("APrint.62") //$NON-NLS-1$
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
				logger.error("Extension " + helpMenuItemsExtensionPoint // $NON-NLS-1$
						+ " throw an exception", t); // $NON-NLS-1$
			}
		}

		helpmenu.addSeparator();

		JMenuItem reportsend = new JMenuItem(Messages.getString("APrint.244")); //$NON-NLS-1$
		reportsend.setIcon(new ImageIcon(getClass().getResource("bug.png"))); //$NON-NLS-1$
		reportsend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.info("Asking sending a bug report"); //$NON-NLS-1$
				BugReporter.sendBugReport();
				JMessageBox.showMessage(APrintNG.this, Messages.getString("APrint.257")); //$NON-NLS-1$
			}
		});

		helpmenu.add(reportsend);

		JMenuItem saveLocalBugReport = new JMenuItem(Messages.getString("APrintNG.200")); //$NON-NLS-1$
		saveLocalBugReport.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					logger.debug("saving the bug report .."); //$NON-NLS-1$
					APrintFileChooser fc = new APrintFileChooser();
					if (fc.showSaveDialog(APrintNG.this) == APrintFileChooser.APPROVE_OPTION) {
						AbstractFileObject sel = fc.getSelectedFile();
						if (sel != null) {
							OutputStream os = sel.getOutputStream();
							try {
								BugReporter.saveBugReport(os);
							} finally {
								os.close();
							}
							JMessageBox.showMessage(APrintNG.this,
									Messages.getString("APrintNG.201") + sel.getName().toString() // $NON-NLS-1$
											+ " " + Messages.getString("APrintNG.202")); //$NON-NLS-2$
						}
					}
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		});
		helpmenu.add(saveLocalBugReport);

		menu.add(helpmenu);
	}

	/**
	 * Construct option menu
	 *
	 * @param menu
	 */
	private void constructMenuOptions(JMenuBar menu) {

		JMenu m_Options = new JMenu(Messages.getString("APrint.144")); //$NON-NLS-1$
		m_Options.setIcon(new ImageIcon(getClass().getResource("package_settings.png"))); //$NON-NLS-1$
		m_Options.setMnemonic('o');

		// JMenu repositoryMenu = constructMenuRepository(m_Options);
		//
		// m_Options.add(repositoryMenu);

		logger.debug("add option menu from extension"); //$NON-NLS-1$

		OptionMenuItemsExtensionPoint[] allOptionMenuPoints = ExtensionPointProvider
				.getAllPoints(OptionMenuItemsExtensionPoint.class, exts);
		for (int i = 0; i < allOptionMenuPoints.length; i++) {
			OptionMenuItemsExtensionPoint addOptionMenuItems = allOptionMenuPoints[i];
			try {
				addOptionMenuItems.addOptionMenuItem(m_Options);
			} catch (Throwable t) {
				logger.error("Extension " + addOptionMenuItems // $NON-NLS-1$
						+ " throw an exception", t); // $NON-NLS-1$
			}
		}

		m_Options.addSeparator();
		JMenuItem miTranslator = new JMenuItem(Messages.getString("APrint.242")); //$NON-NLS-1$
		miTranslator.setIcon(new ImageIcon(getClass().getResource("locale.png"))); //$NON-NLS-1$
		miTranslator.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					JTranslator t = new JTranslator(Messages.getOverrideLocalizedMessageFile(),
							Messages.getEnglishBundle(), aprintproperties);

					SwingUtils.center(t);

					t.setVisible(true);

				} catch (Exception ex) {
					String msg = Messages.getString("APrint.243") //$NON-NLS-1$
							+ ex.getMessage();
					logger.error(msg, ex);
					BugReporter.sendBugReport();
					JOptionPane.showMessageDialog(APrintNG.this, msg);
				}
			}
		});

		m_Options.add(miTranslator);

		JMenu forcedLocale = new JMenu(Messages.getString("APrintNG.30")); //$NON-NLS-1$
		m_Options.add(forcedLocale);

		forcedLocale.add(createLanguageMenu(Messages.getString("APrintNG.31"), null)); //$NON-NLS-1$
		forcedLocale.add(createLanguageMenu("English", Locale.ENGLISH)); //$NON-NLS-1$
		forcedLocale.add(createLanguageMenu("Fran�ais", Locale.FRENCH)); //$NON-NLS-1$
		forcedLocale.add(createLanguageMenu("Deutsch", Locale.GERMAN)); //$NON-NLS-1$
		forcedLocale.add(createLanguageMenu("Italiano", Locale.ITALIAN)); //$NON-NLS-1$
		forcedLocale.add(createLanguageMenu("Spanish", new Locale("es"))); //$NON-NLS-1$

		JMenu lookandfeel = new JMenu(Messages.getString("APrint.248")); //$NON-NLS-1$
		lookandfeel.setIcon(new ImageIcon(getClass().getResource("wizard.png"))); //$NON-NLS-1$

		JMenuItem swinglnf = lookandfeel.add("Standard Java"); //$NON-NLS-1$
		swinglnf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				aprintproperties.setLookAndFeel("swing"); //$NON-NLS-1$
				relaunchWithMessageBox();
			}
		});

		LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
		LookAndFeel currentlnf = UIManager.getLookAndFeel();

		Set<String> alreadyPuttedInMenu = new TreeSet<String>();

		for (int i = 0; i < installedLookAndFeels.length; i++) {
			LookAndFeelInfo lookAndFeelInfo = installedLookAndFeels[i];
			final String lnfname = lookAndFeelInfo.getName();
			final String lnfclassname = lookAndFeelInfo.getClassName();

			if (!alreadyPuttedInMenu.contains(lnfclassname)) {

				JCheckBoxMenuItem installedlnf = new JCheckBoxMenuItem(lnfname);
				lookandfeel.add(installedlnf);

				if (currentlnf != null && currentlnf.getClass().getName().equals(lnfclassname)) {
					installedlnf.setState(true);
				}

				installedlnf.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						aprintproperties.setLookAndFeel(lnfclassname); // $NON-NLS-1$
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

	/** */
	protected JMenuItem createLanguageMenu(String label, final Locale value) {
		JMenuItem miLanguage = new JMenuItem(label);
		miLanguage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				aprintproperties.setForcedLocal(value);
				relaunchWithMessageBox();
			}
		});
		return miLanguage;
	}

	private JMenu constructMenuOptionRendering() {
		final JMenu m = new JMenu(Messages.getString("APrint.300")); //$NON-NLS-1$

		final JCheckBoxMenuItem defaultRendering = new JCheckBoxMenuItem(Messages.getString("APrint.301")); //$NON-NLS-1$
		m.add(defaultRendering);

		defaultRendering.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				APrintNG.this.currentPlaySubSystem = new GervillPlaySubSystemWithRegisterInstruments();
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
					APrintNG.this.currentPlaySubSystem = midiDevicePlaySubSystem;
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

		IPlaySubSystemRegistryExtensionPoint[] allPoints = ExtensionPointProvider
				.getAllPoints(IPlaySubSystemRegistryExtensionPoint.class, exts);
		if (allPoints != null) {
			for (int i = 0; i < allPoints.length; i++) {
				IPlaySubSystemRegistryExtensionPoint iPlaySubSystemRegistryExtensionPoint = allPoints[i];

				PlaySubSystemDef[] pss = iPlaySubSystemRegistryExtensionPoint.getPlaySubSystems();
				if (pss != null) {
					for (int j = 0; j < pss.length; j++) {
						PlaySubSystemDef playSubSystemDef = pss[j];
						final String name = playSubSystemDef.name;
						final PlaySubSystem ps = playSubSystemDef.playSubSystem;

						if (name != null && ps != null) {
							logger.debug("adding play subsystem " + name // $NON-NLS-1$
									+ " -> " + ps); // $NON-NLS-1$

							final JCheckBoxMenuItem midiRendering = new JCheckBoxMenuItem(name);
							m.add(midiRendering);

							midiRendering.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									logger.debug("activate :" + ps); //$NON-NLS-1$
									APrintNG.this.currentPlaySubSystem = ps;
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
					}
				}
			}
		}

		return m;
	}

	// private JMenu constructMenuRepository(JMenu m_Options) {
	// JMenu repositoryMenu = new JMenu(Messages.getString("APrint.258"));
	// //$NON-NLS-1$
	//
	// JMenuItem resetParametersgamme = repositoryMenu.add(Messages
	// .getString("APrint.53")); //$NON-NLS-1$
	// resetParametersgamme.addActionListener(new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// try {
	// aprintproperties.setGammeAndTranlation(null);
	// defineNewGammeAndTranspositionFolder(null);
	// repositoryChanged();
	//
	// } catch (Exception ex) {
	// logger.error("ResetParametersGamme", ex); //$NON-NLS-1$
	// JMessageBox.showMessage(APrintNG.this, Messages
	// .getString("APrint.55") //$NON-NLS-1$
	// + ex.getMessage());
	// }
	// }
	// });
	//
	// m_Options.addSeparator();
	//
	// JMenuItem parametresgamme = repositoryMenu.add(Messages
	// .getString("APrint.35")); //$NON-NLS-1$
	// parametresgamme.setIcon(new ImageIcon(getClass()
	// .getResource("misc.png"), Messages.getString("APrint.94")));
	// //$NON-NLS-1$ //$NON-NLS-2$
	// parametresgamme.setActionCommand("CHOICEGAMMEFOLDER"); //$NON-NLS-1$
	// parametresgamme.addActionListener(this);
	//
	// JMenuItem synchronize = repositoryMenu.add(Messages
	// .getString("APrint.265")); //$NON-NLS-1$
	// synchronize.addActionListener(new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	//
	// synchronizeWebRepositories();
	//
	// }
	//
	// });
	//
	// return repositoryMenu;
	// }

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
							for (int i = 0; i < rcollection.getRepositoryCount(); i++) {
								Repository2 r = rcollection.getRepository(i);

								if (r != null) {

									SynchronizationFeedBack sb = new SynchronizationFeedBack() {
										public void inform(String message, double progress) {

											infiniteChangeText(Messages.getString("APrint.268") //$NON-NLS-1$
													+ message + " (" //$NON-NLS-1$
													+ ((int) (progress * 100)) + " % )"); //$NON-NLS-1$
										}
									};

									if (r instanceof GAESynchronizedRepository2) {
										GAESynchronizedRepository2 g = (GAESynchronizedRepository2) r;
										infiniteChangeText(Messages.getString("APrint.267") //$NON-NLS-1$
												+ g.getName());
										try {

											SynchronizationReport reportSynchro = g.synchronizeRepository(sb);

											report.addAll(reportSynchro);

										} catch (Exception ex) {
											final Exception fex = ex;

											logger.error(ex.getMessage(), ex);
											BugReporter.sendBugReport();

											try {
												SwingUtilities.invokeAndWait(new Runnable() {
													public void run() {
														JMessageBox.showMessage(APrintNG.this,
																Messages.getString("APrint.303") //$NON-NLS-1$
																		+ fex.getMessage());
													}
												});
											} catch (Exception ex_) {
												logger.error(ex_.getMessage(), ex_);
											}
										}
									} else if (r instanceof HttpXmlRepository) {

										HttpXmlRepository httprepo = (HttpXmlRepository) r;

										infiniteChangeText(Messages.getString("APrint.267") //$NON-NLS-1$
												+ r.getName());

										try {

											InstrumentDefinition[] instrumentList = httprepo.getInstruments();

											for (InstrumentDefinition id : instrumentList) {
												infiniteChangeText("Downloading " + id.label);
												try {
													httprepo.downloadInstruments(instrumentList);
													report.add(new MessageSynchroElement(SynchroElement.MESSAGE,
															"instrument " + id.label + " successfully downloaded"));

												} catch (Exception ex) {
													logger.error(
															"error downloading " + id.label + " from " + r.getName(),
															ex);
													report.add(new MessageSynchroElement(SynchroElement.ERROR,
															"error in downloading instrument " + id.label
																	+ " from repository " + r.getName()));
												}
											}

										} catch (Exception ex) {
											final Exception fex = ex;

											logger.error(ex.getMessage(), ex);
											BugReporter.sendBugReport();

											try {
												SwingUtilities.invokeAndWait(new Runnable() {
													public void run() {
														JMessageBox.showMessage(APrintNG.this,
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
					}

				} finally {
					infiniteEndWait();
				}

				if (report != null && report.hasErrors()) {
					try {
						SwingUtilities.invokeAndWait(new Runnable() {

							public void run() {

								StringBuffer sb = new StringBuffer();

								for (Iterator<SynchroElement> it = report.iterator(); it.hasNext();) {
									SynchroElement e = it.next();

									if (e.getStatus() == SynchroElement.ERROR) {
										sb.append(Messages.getString("APrint.304") + e.getMessage()) // $NON-NLS-1$
												.append("\n"); //$NON-NLS-1$
									}
								}

								JMessageBox.showMessage(APrintNG.this, Messages.getString("APrint.306") //$NON-NLS-1$
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

	private JMenu scaleAndInstrumentEditor;

	/**
	 * 
	 * @since 2020 : use vfs
	 * 
	 * @param im
	 * @param f
	 * @throws FileNotFoundException
	 * @throws Exception
	 * @throws IOException
	 */
	protected void importInstrumentToRepository(EditableInstrumentManager im, AbstractFileObject f)
			throws FileNotFoundException, Exception, IOException {
		EditableInstrumentStorage eis = new EditableInstrumentStorage();
		InputStream fis = f.getInputStream();
		infiniteStartWait(Messages.getString("APrintNG.203")); //$NON-NLS-1$
		try {
			IEditableInstrument ei = eis.load(fis, "importedinstrument"); //$NON-NLS-1$

			im.saveEditableInstrument(ei);

			logger.debug("instrument imported ..."); //$NON-NLS-1$

			JMessageBox.showMessage(getOwnerForDialog(), Messages.getString("APrintNG.101")); //$NON-NLS-1$

		} finally {
			fis.close();
			infiniteEndWait();
		}
	}

	/**
	 * @param editableRepository
	 * @return
	 */
	protected EditableInstrumentManagerRepository2Adapter findEditableInstrumentManagerRepository(
			List<EditableInstrumentManagerRepository2Adapter> editableRepository) {
		EditableInstrumentManagerRepository2Adapter eira = null;
		for (Iterator iterator = editableRepository.iterator(); iterator.hasNext();) {
			EditableInstrumentManagerRepository2Adapter editableInstrumentManagerRepository2Adapter = (EditableInstrumentManagerRepository2Adapter) iterator
					.next();

			logger.debug("evaluating " //$NON-NLS-1$
					+ editableInstrumentManagerRepository2Adapter.getName());
			if ((Repository2Factory.PERSONAL_EDITABLE_INSTRUMENTS + "private") //$NON-NLS-1$
					.equals(editableInstrumentManagerRepository2Adapter.getName())) {
				eira = editableInstrumentManagerRepository2Adapter;
				break;
			}
		}
		return eira;
	}

	/**
	 * Construction du menu gamme
	 *
	 * @param menu
	 */
	private void constructMenuGamme(JMenuBar menu) {

		JMenu m_gamme = new JMenu(Messages.getString("APrint.287")); //$NON-NLS-1$
		m_gamme.setMnemonic('i');
		m_gamme.setIcon(new ImageIcon(APrintNG.getAPrintApplicationIcon()));

		m_gamme.add(popupmenuImprimerGamme);

		popupmenuImprimerGamme.setIcon(new ImageIcon(getClass().getResource("frameprint.png"))); //$NON-NLS-1$

		populateImprimerGammeMenu(popupmenuImprimerGamme);

		m_gamme.addSeparator();

		JMenuItem shortCutImportInstrumentFromFile = m_gamme.add(Messages.getString("APrintNG.100")); //$NON-NLS-1$
		shortCutImportInstrumentFromFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				importInstrument();
			}
		});

		shortCutImportInstrumentFromFile.setIcon(new ImageIcon(getClass().getResource("link.png"))); //$NON-NLS-1$

		m_gamme.addSeparator();

		JMenuItem instrumentsAndScales = m_gamme.add(Messages.getString("APrintNG.1")); //$NON-NLS-1$
		instrumentsAndScales.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					showInstrumentEditor();

				} catch (Exception ex) {
					logger.error("error in showing the repositoryeditor :" //$NON-NLS-1$
							+ ex.getMessage(), ex);
					BugReporter.sendBugReport();
				}
			}
		});

		menu.add(m_gamme);
	}

	public void showInstrumentEditor() throws Exception {

		JRepositoryTileFrame f = new JRepositoryTileFrame(this, repository.getRepository2(), aprintproperties);

		f.setTitle(Messages.getString("APrintNG.2")); //$NON-NLS-1$

		f.setIconImage(getIconImage());

		f.setLocationByPlatform(true);
		f.setSize(1024, 768);

		SwingUtils.center(f);

		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	/**
	 * Construction du menu fichier
	 *
	 * @param menu
	 */
	private void constructMenuFile(JMenuBar menu) {

		JMenu m_fichier = new JMenu(Messages.getString("APrint.34")); //$NON-NLS-1$
		m_fichier.setIcon(new ImageIcon(getClass().getResource("filesave.png"))); //$NON-NLS-1$
		m_fichier.setMnemonic('f');

		JMenuItem ouvrirvb = m_fichier.add(Messages.getString("APrintNG.10")); //$NON-NLS-1$
		ouvrirvb.setActionCommand("LOAD"); //$NON-NLS-1$
		ouvrirvb.setAccelerator(KeyStroke.getKeyStroke("control O")); //$NON-NLS-1$
		ouvrirvb.addActionListener(this);

		m_fichier.addSeparator();

		JMenuItem ouvrirfichiermidi = m_fichier.add(Messages.getString("APrintNG.3")); //$NON-NLS-1$
		ouvrirfichiermidi.setIcon(new ImageIcon(getClass().getResource("fileopen.png"))); //$NON-NLS-1$
		ouvrirfichiermidi.setActionCommand("IMPORT"); //$NON-NLS-1$
		ouvrirfichiermidi.addActionListener(this);

		m_fichier.addSeparator();

		JMenuItem formatpagedefault = m_fichier.add(Messages.getString("APrint.120")); //$NON-NLS-1$
		formatpagedefault.setIcon(new ImageIcon(getClass().getResource("frameprint.png"))); //$NON-NLS-1$
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

	/**
	 * add menu with next button
	 *
	 * @param menu
	 * @param item
	 * @return
	 */
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

				current = addScalesInMenu(current, scaleNamesInRepository, r.getName());
			}

		} else {

			String[] gammenames = gm.getScaleNames();

			addScalesInMenu(imprimergamme, gammenames, null);
		}
	}

	private JMenu addScalesInMenu(JMenu imprimergamme, String[] gammenames, String suffix) {
		Arrays.sort(gammenames, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});

		JMenu current = imprimergamme;

		for (final String g : gammenames) {

			if (!"Midi".equalsIgnoreCase(g)) { //$NON-NLS-1$
				JMenuItem mi = new JMenuItem(g + (suffix == null ? "" : " - " + suffix)); //$NON-NLS-1$ //$NON-NLS-2$
				mi.setIcon(new ImageIcon(APrintNG.getAPrintApplicationIcon()));
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						PrintPreview pp = new PrintPreview(new ScalePrintDocument(gm.getScale(g)));
						pp.setDisplayScale(100);
					}
				});
				current = addInMenuWithNext(current, mi);
			}
		}
		return current;
	}

	private void changeGammeFolder() throws RepositoryException, IOException {
		// choix du r�pertoire de gamme ...
		JDirectoryChooser dc = new JDirectoryChooser(Messages.getString("APrintApplication.2")); //$NON-NLS-1$

		dc.setSelectedFile(aprintproperties.getGammeAndTranlation());

		int ret = dc.showDialog(null, Messages.getString("APrintApplication.3")); //$NON-NLS-1$

		if (ret != JDirectoryChooser.CANCEL_OPTION) // $NON-NLS-1$
		{
			File repertoire = dc.getSelectedFile();
			defineNewGammeAndTranspositionFolder(repertoire);
			repositoryChanged();

			// rafraichissement des transpositions ...

			// m�morisation du r�pertoire contenant le repository
			aprintproperties.setGammeAndTranlation(repertoire);
		}
	}

	private PageFormat lastPrintPageFormat = null;

	// private JSplitPane splitpane;

	private IExtension[] exts;

	private class InternalRepositoryListenerClass implements RepositoryChangedListener {

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
	 * charge les nouvelles gammes contenues dans le r�pertoire
	 *
	 * @param folder le r�pertoire contenant des gammes
	 */
	private void defineNewGammeAndTranspositionFolder(File folder) throws RepositoryException {

		Properties repprop = new Properties();
		if (folder != null) {
			repprop.setProperty("folder", folder.getAbsolutePath()); //$NON-NLS-1$
		}

		RepositoryAdapter ra = new RepositoryAdapter(Repository2Factory.create(repprop, this.aprintproperties));

		if (repository != null) {
			// desabonnement sur les modifications du repository
			Repository2 r2 = repository.getRepository2();
			r2.removeRepositoryChangedListener(internalRepositoryListenerClass);
			if (r2 instanceof Disposable) {
				((Disposable) r2).dispose();
			}
		}

		// abonnement ...

		ra.getRepository2().addRepositoryChangedListener(internalRepositoryListenerClass);

		repository = ra;

		gm = repository.getScaleManager();
		tm = repository.getTranspositionManager();

		informExtensionsAboutRepository();

		populateImprimerGammeMenu(popupmenuImprimerGamme);
	}

	/** this function is called when the repository changed */
	private void repositoryChanged() {

		for (Iterator iterator = repositorylistener.iterator(); iterator.hasNext();) {
			APrintRepositoryChangedListener l = (APrintRepositoryChangedListener) iterator.next();
			l.repositoryChanged(repository);
		}

		// instrumentChoice.setRepository(this.repository.getRepository2());
	}

	/**
	 * Retourne la version de l'outil ...
	 *
	 * @return
	 */
	public String getVersion() {
		Properties prop = new Properties();

		try {
			prop.load(this.getClass().getClassLoader().getResourceAsStream("aprintversion.properties")); //$NON-NLS-1$
		} catch (Exception ex) {
			return Messages.getString("APrint.115"); //$NON-NLS-1$
		}

		return prop.getProperty("version"); //$NON-NLS-1$
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

	@Override
	public void dispose() {
		if (asyncJobsManager != null) {
			this.asyncJobsManager.dispose();
		}

		System.exit(0);

		// the below function may block ???
		// reason is not yet understood, so we exit before
		// super.dispose();
	}

	public void relaunchWithMessageBox() {
		JMessageBox.showMessage(this, Messages.getString("APrint.255")); //$NON-NLS-1$

		setTerminateState(NEED_RESTART);
		freeTemporaryResources();
		this.setVisible(false);
	}

	private void initHelpBroker() {
	}

	/**
	 * test if there are instruments in the web repositories
	 *
	 * @return
	 */
	public boolean hasInstrumentsInWebRepository() {

		Repository2 repository2 = this.repository.getRepository2();
		if (repository2 instanceof Repository2Collection) {
			Repository2Collection r2c = (Repository2Collection) repository2;
			for (int i = 0; i < r2c.getRepositoryCount(); i++) {
				Repository2 r = r2c.getRepository(i);

				if (r instanceof GAESynchronizedRepository2 || r instanceof HttpXmlRepository) {

					org.barrelorgandiscovery.instrument.Instrument[] instruments = r.listInstruments();
					if (instruments == null)
						return false;

					return instruments.length != 0;
				}
			}
		}

		return true;
	}

	public void actionPerformed(ActionEvent e) {
		try {

			if ("NEW".equals(e.getActionCommand())) { //$NON-NLS-1$

				// create a new virtual book choosing an
				// instrument

				// instrument chooser ...

				APrintNGInternalFrame intf = new APrintNGInternalFrame(aprintproperties.getFilePrefsStorage(),
						Messages.getString("APrintNG.4000"), true); // $NON-NLS-1$
				intf.getContentPane().setLayout(new BorderLayout());

				JInstrumentTileViewerPanel p = new JInstrumentTileViewerPanel(

						new InstrumentSelectedListener() {

							@Override
							public void instrumentSelected(Instrument i) {
								try {
									newVirtualBook(i);
									intf.dispose();
								} catch (Exception ex) {
									logger.error("error while creating a new book with instrument " + i, ex); //$NON-NLS-1$
								}
							}

							@Override
							public void instrumentDoubleClicked(Instrument i) {
								try {
									newVirtualBook(i);
									intf.dispose();
								} catch (Exception ex) {
									logger.error("error while creating a new book with instrument " + i, ex); //$NON-NLS-1$
								}
							}

						}, Messages.getString("APrintNG.4000"), this.repository.getRepository2()); // $NON-NLS-1$

				intf.getContentPane().add(p, BorderLayout.CENTER);
				intf.setVisible(true);

			} else if ("SEARCH".equals(e.getActionCommand())) { //$NON-NLS-1$

				APrintNGSearchInternalFrame searchinternalframe = new APrintNGSearchInternalFrame(this.aprintproperties,
						this, this.bookIndexing);
				searchinternalframe.setSize(800, 600);

				addNewInternalFrame(searchinternalframe);

				searchinternalframe.setVisible(true);

			} else if ("LOAD".equals(e.getActionCommand())) { //$NON-NLS-1$

				logger.debug("load a virtualbookfile ... "); //$NON-NLS-1$

				APrintFileChooser choose = new APrintFileChooser();
				choose.setFileFilter(
						new VFSFileNameExtensionFilter("Virtual book File", new String[] { APrintConstants.BOOK })); //$NON-NLS-1$

				choose.setFileSelectionMode(APrintFileChooser.FILES_ONLY);
				
				File lastFileProperty = aprintproperties.getFilePrefsStorage()
						.getFileProperty("lastOpenedBookFileLocation", null); //$NON-NLS-1$

				if (lastFileProperty != null) {
						choose.setSelectedFile(lastFileProperty);
				}
				
				
				if (choose.showOpenDialog(this) == APrintFileChooser.APPROVE_OPTION) {
					// R�cup�ration du nom de fichier
					final AbstractFileObject result = choose.getSelectedFile();

					if (result.exists()) {
						try {
							
							saveLastOpenBookLocationFile(result);
							
							
							loadBookInNewFrame(result);
						} catch (Exception ex) {
							logger.error("error in loading book :" + ex.getMessage(), ex); //$NON-NLS-1$
							JMessageBox.showMessage(getOwnerForDialog(), Messages.getString("APrintNG.400")); //$NON-NLS-1$
						}
					}
				}

			} else if ("IMPORT".equals(e.getActionCommand())) { //$NON-NLS-1$

				openNewImportMidiFrame();

			} else if ("QUITTER".equals(e.getActionCommand())) { //$NON-NLS-1$

				freeTemporaryResources();

				setTerminateState(TERMINATE);
				setVisible(false);

			} else if ("CHOICEGAMMEFOLDER".equals(e.getActionCommand())) { //$NON-NLS-1$
				changeGammeFolder();
			} else if ("INSTRUMENTREPOSITORY".equals(e.getActionCommand())) { //$NON-NLS-1$
				showInstrumentEditor();
			} else if ("SCALEDITOR".equals(e.getActionCommand())) {

				// prefs for user ...

				StandAloneScaleEditor editor = new StandAloneScaleEditor(APrintNG.this,
						new ScaleEditorPrefs(aprintproperties.getFilePrefsStorage()));

				editor.setLocationByPlatform(true);

				SwingUtils.center(editor);

				editor.setVisible(true);

			} else if ("HELP".equals(e.getActionCommand())) { //$NON-NLS-1$
				new APrintHelpAction().actionPerformed(e);
				//
				// CSH.DisplayHelpFromSource displayHelpFromSource = new
				// CSH.DisplayHelpFromSource(
				// hb);
				//
				// displayHelpFromSource.actionPerformed(new ActionEvent(this,
				// 0,
				// "HELP")); //$NON-NLS-1$

			} else if ("MODELEDITOR".equals(e.getActionCommand())) { //$NON-NLS-1$
				openModelEditor();
			}

		} catch (Exception ex) {
			logger.error("actionPerformed", ex); //$NON-NLS-1$

			BugReporter.sendBugReport();
			JMessageBox.showMessage(getOwnerForDialog(), Messages.getString("APrint.42") + ex.getMessage()); //$NON-NLS-1$
		}
	}

	public void saveLastOpenBookLocationFile(final AbstractFileObject result) {
		try {
			File convertToFile = VFSTools.convertToFile(result);
			aprintproperties.getFilePrefsStorage()
			.setFileProperty("lastOpenedBookFileLocation", convertToFile); //$NON-NLS-1$
			aprintproperties.getFilePrefsStorage().save();
		} catch(Throwable t) {
		}
	}

	/**
	 * open a new Import Midi Frame
	 *
	 * @throws Exception
	 */
	private APrintNGImporterInternalFrame openNewImportMidiFrame() throws Exception {
		APrintNGImporterInternalFrame aprintNGImporterInternalFrame = new APrintNGImporterInternalFrame(this, this,
				aprintproperties, this.exts, this.repository, this);

		aprintNGImporterInternalFrame.setSize(800, 600);

		addNewInternalFrame(aprintNGImporterInternalFrame);

		aprintNGImporterInternalFrame.setVisible(true);

		return aprintNGImporterInternalFrame;
	}

	public void loadBookInNewFrame(AbstractFileObject result) throws Exception, FileNotFoundException {
		logger.debug("opening file " //$NON-NLS-1$
				+ result.getName().toString());

		infiniteStartWait(Messages.getString("APrintNG.204") + " " + result.getName()); //$NON-NLS-1$ //$NON-NLS-2$

		class ResultLoading {

			// this is the result of the read virtual book
			public VirtualBook vb;

			/**
			 * the loaded instrument, if the instrument is not found, this member is null.
			 */
			public Instrument ins;
		}

		asyncJobsManager.submitAndExecuteJob(new Callable<ResultLoading>() {

			public ResultLoading call() throws Exception {

				VirtualBookXmlIO.VirtualBookResult vbreadresult = null;

				InputStream stream = result.getInputStream();
				stream = new BufferedInputStream(stream);
				try {
					vbreadresult = VirtualBookXmlIO.read(stream);
				} finally {
					stream.close();
				}
				String preferredInstrumentName = vbreadresult.preferredInstrumentName;

				logger.debug("preferredInstrumentName :" //$NON-NLS-1$
						+ preferredInstrumentName);

				if (preferredInstrumentName == null || preferredInstrumentName.isEmpty()) {

					// try to get equivalent scale from existing instruments
					throw new ApplicationInstrumentNotFoundException(
							Messages.getString("APrintNG.8") + "(" + preferredInstrumentName + ")",
							vbreadresult.virtualBook // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					, vbreadresult.preferredInstrumentName);
				}

				assert preferredInstrumentName != null && !preferredInstrumentName.isEmpty();
				Instrument instrument = repository.getInstrumentManager().getInstrument(preferredInstrumentName);

				if (instrument == null) {
					throw new ApplicationInstrumentNotFoundException(
							Messages.getString("APrintNG.9") + " (" + preferredInstrumentName + ")",
							vbreadresult.virtualBook, vbreadresult.preferredInstrumentName); // $NON-NLS-1$
				}

				Scale newInstrumentScale = instrument.getScale();
				VirtualBook loadedVirtualBook = vbreadresult.virtualBook;

				VirtualBook vb = changeScale(newInstrumentScale, loadedVirtualBook);

				ResultLoading r = new ResultLoading();
				r.vb = vb;
				r.ins = instrument;

				return r;
			}
		}, new JobEvent() {

			public void jobFinished(Object objResult) {

				final ResultLoading r = (ResultLoading) objResult;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {

						try {
							internalOpenVirtualBookWindow(r.vb, result, r.ins, null);

						} catch (Throwable e) {
							logger.error(e.getMessage(), e);
							JMessageBox.showMessage(APrintNG.this.getOwnerForDialog(), e.getMessage());
						} finally {
							infiniteEndWait();
						}
					}
				});
			}

			public void jobError(final Throwable ex) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {

						Throwable raisedException = ex.getCause();

						if (raisedException instanceof ApplicationInstrumentNotFoundException) {

							// in case there is an issue of instrument loading
							// we present a frame for choosing the equivalent scale
							// and transform the book
							try {
								JDialog dialog = new JDialog(APrintNG.this);
								Container cp = dialog.getContentPane();
								cp.setLayout(new BorderLayout());
								ApplicationInstrumentNotFoundException appex = ((ApplicationInstrumentNotFoundException) raisedException);
								JEquivalentScaleChooserPanel p = new JEquivalentScaleChooserPanel(
										appex.getVirtualBook().getScale(), appex.getOptionalPreferredInstrument());

								ScaleComparator sc = new TracksAndPositionedNoteComparator();
								Instrument[] instruments = repository.getInstrumentManager().listInstruments();
								assert instruments != null;

								// look in the instruments having equivalent scales, using the scale comparator

								Map<String, Scale> equivalentScales = Arrays.stream(instruments)
										.filter((e) -> e != null)
										.filter((i) -> sc.compare(appex.getVirtualBook().getScale(), i.getScale()))
										.collect(HashMap<String, Scale>::new,
												(e, i) -> e.put(i.getName(), i.getScale()), (a, b) -> a.putAll(b));

								p.defineScales(equivalentScales);

								cp.add(p, BorderLayout.CENTER);

								JButton ok = new JButton("Change Instrument");
								cp.add(ok, BorderLayout.SOUTH);

								ok.addActionListener((l) -> {
									try {

										Scale selectedScale = p.getSelectedScale();
										if (selectedScale == null) {
											// nothing ??
											logger.debug("no selected scale choosed by the user");
											return;
										}

										Instrument[] ins = repository.getInstrumentManager()
												.getInstrument(selectedScale);
										assert ins != null;
										if (ins.length == 0) {
											logger.warn("no instrument found associated to scale " + selectedScale);
											return;
										}
										Instrument theretainedInstrument = ins[0];
										logger.debug("instrument retained :" + theretainedInstrument);

										VirtualBook newVB = changeScale(theretainedInstrument.getScale(),
												appex.getVirtualBook());

										dialog.setVisible(false);
										dialog.dispose();

										internalOpenVirtualBookWindow(newVB, result, theretainedInstrument, null);

										logger.debug("successfull converted to " + theretainedInstrument);

									} catch (Exception ex) {

										logger.error(ex.getMessage(), ex);
									}
								});

								dialog.setSize(700, 500);
								dialog.setVisible(true);

							} catch (Throwable ex) {
								logger.error(ex.getMessage(), ex);
								JMessageBox.showMessage(APrintNG.this.getOwnerForDialog(), ex.getMessage());
							}

						} else {

							logger.error(ex.getMessage(), ex);
							JMessageBox.showMessage(APrintNG.this.getOwnerForDialog(), ex.getMessage());
						}
						infiniteEndWait();
					}
				});
			}

			public void jobAborted() {

				infiniteEndWait();
			}
		});

	}

	public void loadBookInNewFrame(final File result) throws Exception, FileNotFoundException {
		loadBookInNewFrame(VFSTools.fromRegularFile(result));
	}

	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		APrintProperties printProperties = new APrintProperties(true);

		APrintNG printNG = new APrintNG(printProperties);

		printNG.setVisible(true);
	}

	private Vector<APrintRepositoryChangedListener> repositorylistener = new Vector<APrintRepositoryChangedListener>();

	public void addAPrintRepositoryListener(APrintRepositoryChangedListener listener) {
		repositorylistener.add(listener);
	}

	public void removeAPrintRepositoryListener(APrintRepositoryChangedListener listener) {
		repositorylistener.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.gui.aprintng.APrintPageFormat#getLastPageFormat
	 * ()
	 */
	public PageFormat getLastPageFormat() {
		return lastPrintPageFormat;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.gui.aprintng.APrintPageFormat#setLastPageFormat
	 * (java.awt.print.PageFormat)
	 */
	public void setLastPageFormat(PageFormat pageformet) {
		lastPrintPageFormat = pageformet;
	}

	public APrintNGVirtualBookFrame newVirtualBook(VirtualBook virtualBook, AbstractFileObject virtualBookFile,
			org.barrelorgandiscovery.instrument.Instrument instrument, IssueCollection collection) throws Exception {

		return internalOpenVirtualBookWindow(virtualBook, virtualBookFile, instrument, collection);
	}

	public APrintNGVirtualBookFrame newVirtualBook(VirtualBook virtualBook,
			org.barrelorgandiscovery.instrument.Instrument instrument, IssueCollection collection) throws Exception {

		return internalOpenVirtualBookWindow(virtualBook, null, instrument, collection);
	}

	private APrintNGVirtualBookInternalFrame internalOpenVirtualBookWindow(VirtualBook virtualBook,
			AbstractFileObject virtualBookFile, org.barrelorgandiscovery.instrument.Instrument instrument,
			IssueCollection collection) throws Exception {
		if (logger.isDebugEnabled())
			logger.debug("newVirtualBook " + virtualBook + " on instrument " //$NON-NLS-2$
					+ instrument);

		if (instrument == null)
			throw new Exception("null instrument passed"); //$NON-NLS-1$

		ArrayList<IExtension> virtualbookviewExtensionPoint = new ArrayList<IExtension>();

		VirtualBookFrameExtensionPoints[] helpExtensionsPoints = ExtensionPointProvider
				.getAllPoints(VirtualBookFrameExtensionPoints.class, exts);
		for (int i = 0; i < helpExtensionsPoints.length; i++) {
			VirtualBookFrameExtensionPoints vbextpoints = helpExtensionsPoints[i];
			try {
				IExtension e = vbextpoints.newExtension();

				logger.debug("adding extension " + e); //$NON-NLS-1$
				virtualbookviewExtensionPoint.add(e);
			} catch (Throwable t) {
				logger.error("Extension " + vbextpoints // $NON-NLS-1$
						+ " throw an exception", t); // $NON-NLS-1$
			}
		}

		APrintNGVirtualBookInternalFrame printNGVirtualBookInternalFrame = new APrintNGVirtualBookInternalFrame(
				virtualBook, virtualBookFile, collection, instrument, aprintproperties, currentPlaySubSystemManager,
				virtualbookviewExtensionPoint.toArray(new IExtension[0]), this, this, asyncJobsManager, scriptManager);

		// TODO perfs on resizing the window

		// printNGVirtualBookInternalFrame.setSize(aprintproperties
		// .getAPrintNGVirtualBookFrameSize());

		addNewInternalFrame(printNGVirtualBookInternalFrame);

		printNGVirtualBookInternalFrame.setVisible(true);

		return printNGVirtualBookInternalFrame;
	}

	class PlaySubSystemMockWithNeedInstrument
			implements PlaySubSystem, NeedInstrument, NeedMidiListeningConverter, IPreparedCapableSubSystem {
		PlaySubSystemManager m;

		public PlaySubSystemMockWithNeedInstrument(PlaySubSystemManager m) {
			this.m = m;
		}

		public PlayControl play(Object owner, VirtualBook vb, IPlaySubSystemFeedBack feedBack, long pos)
				throws Exception {
			try {
				return currentPlaySubSystem.play(owner, vb, feedBack, pos);
			} finally {
				m.fireStartPlaySubSystem();
			}
		}

		public boolean isPlaying() throws Exception {
			return currentPlaySubSystem.isPlaying();
		}

		public void stop() throws Exception {
			currentPlaySubSystem.stop();
			m.fireStopPlaySubSystem();
		}

		public Object getOwner() {
			return currentPlaySubSystem.getOwner();
		}

		public void setCurrentInstrument(Instrument ins) {
			if (currentPlaySubSystem instanceof NeedInstrument) {
				((NeedInstrument) currentPlaySubSystem).setCurrentInstrument(ins);
			} else {
				logger.info("play sub system " + currentPlaySubSystem // $NON-NLS-1$
						+ " doesn't support " + NeedInstrument.class.getName()); // $NON-NLS-1$
			}
		}

		public void setCurrentMidiListeningConverter(MIDIListeningConverter converter) {

			if (currentPlaySubSystem instanceof NeedMidiListeningConverter) {
				((NeedMidiListeningConverter) currentPlaySubSystem).setCurrentMidiListeningConverter(converter);
			}
		}

		public boolean isSupportMidiListeningConverter() {
			if (currentPlaySubSystem instanceof NeedMidiListeningConverter) {
				return ((NeedMidiListeningConverter) currentPlaySubSystem).isSupportMidiListeningConverter();
			}
			return false;
		}

		public ISubSystemPlayParameters createParameterInstance() throws Exception {
			if (currentPlaySubSystem instanceof IPreparedCapableSubSystem) {
				return ((IPreparedCapableSubSystem) currentPlaySubSystem).createParameterInstance();
			}
			return null;
		}

		public PlayControl playPrepared(Object owner, IPreparedPlaying pp, IPlaySubSystemFeedBack feedBack, long pos)
				throws Exception {
			if (currentPlaySubSystem instanceof IPreparedCapableSubSystem) {
				return ((IPreparedCapableSubSystem) currentPlaySubSystem).playPrepared(owner, pp, feedBack, pos);
			}

			return null;
		}

		public IPreparedPlaying preparePlaying(VirtualBook transposedVirtualBook, ISubSystemPlayParameters params)
				throws Exception {
			if (currentPlaySubSystem instanceof IPreparedCapableSubSystem) {
				return ((IPreparedCapableSubSystem) currentPlaySubSystem).preparePlaying(transposedVirtualBook, params);
			}
			return null;
		}
	}

	/** Inner class for the play subsystem */
	class PlaySubSystemManager implements IPlaySubSystemManager {

		private Vector<IPlaySubSystemManagerListener> listeners = new Vector<IPlaySubSystemManagerListener>();

		public PlaySubSystem getCurrent() {

			PlaySubSystem s = new PlaySubSystemMockWithNeedInstrument(this);

			return s;
		}

		public void addPlaySubSystemManagerListener(IPlaySubSystemManagerListener listener) {
			listeners.add(listener);
		}

		public void removePlaySubSystemManagerListener(IPlaySubSystemManagerListener listener) {
			listeners.remove(listener);
		}

		protected void fireStartPlaySubSystem() {
			for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
				IPlaySubSystemManagerListener l = (IPlaySubSystemManagerListener) iterator.next();
				try {
					l.startPlaying();
				} catch (Throwable t) {
					logger.error("error in fire start event :" + t.getMessage(), t); //$NON-NLS-1$
				}
			}
		}

		protected void fireStopPlaySubSystem() {
			for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
				IPlaySubSystemManagerListener l = (IPlaySubSystemManagerListener) iterator.next();
				try {
					l.playStopped();
				} catch (Throwable t) {
					logger.error("error in fire stop event :" + t.getMessage(), //$NON-NLS-1$
							t);
				}
			}
		}
	}

	private PlaySubSystemManager currentPlaySubSystemManager = new PlaySubSystemManager();

	private APrintNGWelcomeFrame printNGWelcomeFrame;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.gui.aprintng.APrintNGGeneralServices#
	 * newVirtualBook (org.barrelorgandiscovery.virtualbook.VirtualBook,
	 * org.barrelorgandiscovery.instrument.Instrument)
	 */
	public APrintNGVirtualBookFrame newVirtualBook(VirtualBook virtualBook,
			org.barrelorgandiscovery.instrument.Instrument instrument) throws Exception {
		return newVirtualBook(virtualBook, instrument, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.gui.aprintng.APrintNGGeneralServices#
	 * newVirtualBook(org.barrelorgandiscovery.instrument.Instrument)
	 */
	@Override
	public APrintNGVirtualBookFrame newVirtualBook(Instrument instrument) throws Exception {
		return newVirtualBook(new VirtualBook(instrument.getScale()), instrument);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.gui.aprintng.APrintNGGeneralServices#
	 * getRepository ()
	 */
	public Repository2 getRepository() {
		return repository.getRepository2();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.gui.aprintng.APrintNGGeneralServices#
	 * getOwnerForDialog()
	 */
	public Object getOwnerForDialog() {
		return this;
	}

	/**
	 * Get aprint properties
	 *
	 * @return
	 */
	public APrintProperties getProperties() {
		return aprintproperties;
	}

	/**
	 * Return the book indexing object
	 *
	 * @return
	 */
	public BookIndexing getBookIndexing() {
		return this.bookIndexing;
	}

	// /////////////////////////////////////////////////////////////////////////

	// weak references to the dialogs
	ArrayList<WeakReference<APrintNGInternalFrame>> internalFrames = new ArrayList<WeakReference<APrintNGInternalFrame>>();

	private void cleanUp() {
		int cpt = 0;
		while (cpt < internalFrames.size()) {
			WeakReference<APrintNGInternalFrame> f = internalFrames.get(cpt);
			if (f != null) {
				APrintNGInternalFrame ref = f.get();
				if (ref != null && ref.isDisposed()) {
					internalFrames.remove(cpt);
					continue; // next
				}
			}
			cpt++;
		}
	}

	public void addNewInternalFrame(APrintNGInternalFrame internalFrame) {
		cleanUp();
		if (internalFrame == null)
			return;

		internalFrames.add(new WeakReference<APrintNGInternalFrame>(internalFrame));
	}

	public APrintNGInternalFrame[] listInternalFrames() {
		cleanUp();

		ArrayList<APrintNGInternalFrame> ret = new ArrayList<APrintNGInternalFrame>();

		for (Iterator iterator = internalFrames.iterator(); iterator.hasNext();) {
			WeakReference<APrintNGInternalFrame> wr = (WeakReference<APrintNGInternalFrame>) iterator.next();
			APrintNGInternalFrame a = wr.get();
			if (a != null && !a.isDisposed()) {
				ret.add(a);
			}
		}

		return ret.toArray(new APrintNGInternalFrame[0]);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.gui.aprintng.APrintNGGeneralServices#
	 * getAsyncJobManager()
	 */
	public AsyncJobsManager getAsyncJobs() {
		return asyncJobsManager;
	}

	// /////////////////////////////////////////////////////////////////////
	// Drag and drop

	protected boolean acceptOrRejectDrag(DropTargetDragEvent dtde) {
		int dropAction = dtde.getDropAction();
		int sourceActions = dtde.getSourceActions();

		// Offering an acceptable operation: accept
		dtde.acceptDrag(dropAction);

		return true;
	}

	public void dragEnter(DropTargetDragEvent dtde) {
		acceptOrRejectDrag(dtde);
	}

	public void dragExit(DropTargetEvent dte) {
	}

	public void dragOver(DropTargetDragEvent dtde) {
		acceptOrRejectDrag(dtde);
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
		acceptOrRejectDrag(dtde);
	}

	/*
	 * Allow a file to be opened by dragging it onto the window
	 */
	public void drop(DropTargetDropEvent dtde) {
		try {
			// Get the object to be transferred
			Transferable tr = dtde.getTransferable();
			DataFlavor[] flavors = tr.getTransferDataFlavors();

			// If flavors is empty get flavor list from DropTarget
			flavors = (flavors.length == 0) ? dtde.getCurrentDataFlavors() : flavors;

			// Select best data flavor
			DataFlavor flavor = DataFlavor.selectBestTextFlavor(flavors);

			// Flavor will be null on Windows
			// In which case use the 1st available flavor
			flavor = (flavor == null) ? flavors[0] : flavor;

			// Flavors to check
			DataFlavor Linux = new DataFlavor("text/uri-list;class=java.io.Reader"); //$NON-NLS-1$
			DataFlavor Windows = DataFlavor.javaFileListFlavor;

			
			
			// On Linux (and OS X) file DnD is a reader
			if (flavor.equals(Linux)) {
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

				BufferedReader read = new BufferedReader(flavor.getReaderForText(tr));
				// Remove 'file://' from file name
				String fileName = VFSTools.decodeURIEncoding(read.readLine().substring(7)); //$NON-NLS-1$ //$NON-NLS-2$
				// Remove 'localhost' from OS X file names
				if (fileName.substring(0, 9).equals("localhost")) { //$NON-NLS-1$
					fileName = fileName.substring(9);
				}
				read.close();

				dtde.dropComplete(true);
				System.out.println("File Dragged:" + fileName); //$NON-NLS-1$

				File file = new File(fileName);
				if (fileName != null) {
					handleDropFileTarget(file);
				}
			}
			// On Windows file DnD is a file list
			else if (flavor.equals(Windows)) {
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				@SuppressWarnings("unchecked")
				List<File> list = (List<File>) tr.getTransferData(flavor);
				dtde.dropComplete(true);

				if (list.size() == 1) {
					System.out.println("File Dragged: " + list.get(0)); //$NON-NLS-1$

					File file = list.get(0);
					handleDropFileTarget(file);

				} else {
					for (Iterator iterator = list.iterator(); iterator.hasNext();) {
						File file = (File) iterator.next();
						try {
							handleDropFileTarget(file);
						} catch (Exception e) {
							logger.error("Error in handling file opening :" //$NON-NLS-1$
									+ file + " : " + e.getMessage(), e); // $NON-NLS-1$
						}
					}
				}
			} else if ("text".equals(flavor.getPrimaryType()) && "html".equals(flavor.getSubType())) {

				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

				// content contains an html fragment containing a a href
				// reference

				// internet link
				BufferedReader read = new BufferedReader(flavor.getReaderForText(tr));
				

				StringWriter sw = new StringWriter();

				
				char[] buffer = new char[200];
				int readchar = read.read(buffer, 0, buffer.length);
				
				while (readchar != -1) {
					sw.write(buffer, 0, readchar);
					logger.debug("content :" + buffer);
					readchar = read.read(buffer, 0, buffer.length);					
				}

				String[] links = HTMLParsingTools.parseHTMLAndExtractLinks(sw.getBuffer().toString());

				if (links == null || links.length == 0)
					return;

				for (String s : links) {
					logger.debug("handling link :" + s);
					URL u = new URL(s);
					String name = u.getFile();
					File fakeFileForParsing = new File(name);

					name = fakeFileForParsing.getName();

					logger.debug("file name :" + name);

					File df = File.createTempFile("download", ".tmp");
					df.delete();
					df.mkdir();

					logger.debug("download temporary folder :" + df);

					File dest = new File(df, name);

					InputStream is = u.openStream();
					try {
						FileOutputStream fos = new FileOutputStream(dest);
						try {
							logger.debug("copy stream");
							StreamsTools.copyStream(is, fos);
							logger.debug("stream copied");
						} finally {
							fos.close();
						}
					} finally {
						is.close();
					}

					logger.debug("handle file :" + dest.getAbsolutePath());

					handleDropFileTarget(dest);

					logger.debug("url " + u + "handled");
				}

			} else {
				logger.warn("unsupported flavor :" + flavor); //$NON-NLS-1$
				dtde.rejectDrop();
			}
		}
		// TODO: OS X Throws ArrayIndexOutOfBoundsException on first DnD
		catch (ArrayIndexOutOfBoundsException e) {
			logger.error("DnD not initalized properly, please try again."); //$NON-NLS-1$
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (UnsupportedFlavorException e) {
			logger.error(e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			JMessageBox.showMessage(this, Messages.getString("APrintNG.205")); //$NON-NLS-1$
		}
	}

	/**
	 * Handle file drop on the application
	 *
	 * @param file
	 * @throws Exception
	 */
	protected void handleDropFileTarget(final File file) throws Exception {
		if (file == null)
			return;

		String lowerCaseFileName = file.getName().toLowerCase();
		if (lowerCaseFileName.endsWith(".book")) { //$NON-NLS-1$
			logger.debug("loading the book"); //$NON-NLS-1$

			Callable<Boolean> c = new Callable<Boolean>() {

				public Boolean call() throws Exception {

					loadBookInNewFrame(file);
					
					saveLastOpenBookLocationFile(VFSTools.fromRegularFile(file));
					

					return null;
				}
			};

			asyncJobsManager.submitAndExecuteJob(c, new JobEvent() {

				public void jobFinished(Object result) {
				}

				public void jobError(Throwable ex) {
					JMessageBox.showMessage(APrintNG.this, Messages.getString("APrintNG.206") + file.getName()); //$NON-NLS-1$
				}

				public void jobAborted() {
				}
			});

		} else if (lowerCaseFileName.endsWith("." //$NON-NLS-1$
				+ EditableInstrumentConstants.INSTRUMENT_FILE_EXTENSION)) {

			Callable<Boolean> ft = new Callable<Boolean>() {
				public Boolean call() throws Exception {

					try {
						Repository2 rep2 = repository.getRepository2();
						if (rep2 instanceof Repository2Collection) {
							Repository2Collection col = (Repository2Collection) rep2;
							List<EditableInstrumentManagerRepository2Adapter> editableRepository = col
									.findRepository(EditableInstrumentManagerRepository2Adapter.class);

							EditableInstrumentManagerRepository2Adapter eira = findEditableInstrumentManagerRepository(
									editableRepository);

							if (eira != null) {
								EditableInstrumentManager im = eira.getEditableInstrumentManager();

								AbstractFileObject fso = VFSTools.fromRegularFile(file);

								importInstrumentToRepository(im, fso);
							}
						}

						return true;
					} catch (final Exception ex) {
						SwingUtilities.invokeLater(new Runnable() {

							public void run() {

								JMessageBox.showMessage(APrintNG.this, Messages.getString("APrintNG.207") //$NON-NLS-1$
										+ ex.getMessage());
							}
						});

						return false;
					}
				}
			};

			asyncJobsManager.submitAndExecuteJob(ft, null);

		} else if (lowerCaseFileName.endsWith("." //$NON-NLS-1$
				+ APrintGroovyConsole.APRINTGROOVYSCRIPTEXTENSION)) {
			logger.debug("opening aprint groovy script"); //$NON-NLS-1$
			APrintGroovyInnerConsole gc = openGroovyScriptConsole();
			AbstractFileObject fso = VFSTools.fromRegularFile(file);
			gc.openScript(fso);

		} else if (lowerCaseFileName.endsWith(".mid") || lowerCaseFileName.endsWith(".kar")) { //$NON-NLS-2$

			APrintNGImporterInternalFrame midiImportFrame = openNewImportMidiFrame();
			AbstractFileObject resolvedFile = VFSTools.fromRegularFile(file);
			midiImportFrame.defineCurrentMidiFile(resolvedFile);

		} else if (lowerCaseFileName.endsWith(QuickScriptManager.APRINTBOOKGROOVYSCRIPTEXTENSION)) {
			try {
				logger.debug("reading :" + file); //$NON-NLS-1$
				InputStreamReader ir = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")); //$NON-NLS-1$
				try {
					StringBuffer sb = new StringBuffer();

					int cpt;
					char[] buffer = new char[4096];

					while ((cpt = ir.read(buffer)) != -1) {
						sb.append(buffer, 0, cpt);
					}
					logger.debug("saving the quick script to the script manager"); //$NON-NLS-1$

					String scriptName = file.getName();
					scriptName = scriptName.substring(0, scriptName.lastIndexOf('.'));
					scriptManager.saveScript(scriptName, sb);

					JMessageBox.showMessage(APrintNG.this, "QuickScript " //$NON-NLS-1$
							+ file.getName() + " " + Messages.getString("APrintNG.208")); //$NON-NLS-2$

				} finally {
					ir.close();
				}

			} catch (Exception ex) {
				logger.error("error reading the script file", ex); //$NON-NLS-1$
				JMessageBox.showMessage(APrintNG.this, Messages.getString("APrintNG.209") + file.getName()); //$NON-NLS-1$
			}
		}
	}

	public APrintGroovyInnerConsole openGroovyScriptConsole(ClassLoader loaderForScripts) throws Exception {
		APrintGroovyInnerConsole printGroovyConsole = new APrintGroovyInnerConsole(APrintNG.this, aprintproperties,
				asyncJobsManager);

		if (loaderForScripts != null) {
			printGroovyConsole.setLoaderForScripts(loaderForScripts);
		}

		printGroovyConsole.setSize(800, 600);

		addNewInternalFrame(printGroovyConsole);

		Binding currentBindingByRef = printGroovyConsole.getCurrentBindingByRef();

		currentBindingByRef.setProperty("services", APrintNG.this); //$NON-NLS-1$
		// SwingUtils.center(printGroovyConsole);
		printGroovyConsole.setVisible(true);

		return printGroovyConsole;
	}

	/** @throws Exception */
	public APrintGroovyInnerConsole openGroovyScriptConsole() throws Exception {
		return openGroovyScriptConsole(null);
	}

	public APrintNGModelFrame openModelEditor() throws Exception {
		logger.debug("open model editor");
		APrintNGModelFrame f = new APrintNGModelFrame(getProperties(), this);

		f.setSize(800, 600);
		f.setVisible(true);
		addNewInternalFrame(f);

		return f;
	}

	/** */
	protected void importInstrument() {
		try {

			logger.debug("entering import instrument"); //$NON-NLS-1$

			Repository2 rep2 = repository.getRepository2();
			if (rep2 instanceof Repository2Collection) {
				Repository2Collection col = (Repository2Collection) rep2;
				List<EditableInstrumentManagerRepository2Adapter> editableRepository = col
						.findRepository(EditableInstrumentManagerRepository2Adapter.class);

				EditableInstrumentManagerRepository2Adapter eira = findEditableInstrumentManagerRepository(
						editableRepository);

				if (eira != null) {
					EditableInstrumentManager im = eira.getEditableInstrumentManager();

					APrintFileChooser fc = new APrintFileChooser();

					fc.setFileFilter(new VFSFileNameExtensionFilter("instrumentfile", //$NON-NLS-1$
							EditableInstrumentConstants.INSTRUMENT_FILE_EXTENSION));

					if (fc.showOpenDialog(getOwner()) == APrintFileChooser.APPROVE_OPTION) {
						AbstractFileObject f = fc.getSelectedFile();
						if (f != null) {

							logger.debug("loading instrument :" //$NON-NLS-1$
									+ f.toString());

							importInstrumentToRepository(im, f);
						}
					}

					// im.saveEditableInstrument(instrument);
				} else {
					logger.warn("cannot find \"private\" instrument manager"); //$NON-NLS-1$
				}

			} else {
				logger.warn("repository is not an editable instrument repository"); //$NON-NLS-1$
			}

		} catch (Throwable t) {
			logger.error("error while importing instrument from file " //$NON-NLS-1$
					+ t.getMessage(), t);
			BugReporter.sendBugReport();
			JMessageBox.showMessage(getOwnerForDialog(), Messages.getString("APrintNG.102") //$NON-NLS-1$
					+ t.getMessage());
		}
	}

	// /////////////////////////////////////////////////////////////////

	public IPrefsStorage getPrefsStorage(String name) {

		File aprintFolder = aprintproperties.getAprintFolder();
		File prefsFolder = new File(aprintFolder, "prefs");
		prefsFolder.mkdirs();
		File prefsFolderFile = new File(prefsFolder, StringTools.convertToPhysicalName(name) + ".prefs");

		FilePrefsStorage fsp = new FilePrefsStorage(prefsFolderFile);
		// load the properties

		if (fsp.getFile().exists()) {
			try {
				fsp.load();
			} catch (Exception ex) {
				logger.error("error while loading the preference properties");
			}
		}
		return fsp;
	}

	private VirtualBook changeScale(Scale newInstrumentScale, VirtualBook loadedVirtualBook) {
		VirtualBook vb = new VirtualBook(newInstrumentScale, loadedVirtualBook);
		vb.setMetadata(loadedVirtualBook.getMetadata());
		vb.setName(loadedVirtualBook.getName());
		return vb;
	}
	
	public IExtension[] getCurrentExtensions() {
		return this.exts;
	}
}
