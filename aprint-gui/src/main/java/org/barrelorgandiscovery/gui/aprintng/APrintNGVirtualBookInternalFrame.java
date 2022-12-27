package org.barrelorgandiscovery.gui.aprintng;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Soundbank;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.JobEvent;
import org.barrelorgandiscovery.bookimage.BookImage;
import org.barrelorgandiscovery.bookimage.ZipBookImage;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository2Adapter;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.editableinstrument.InstrumentScript;
import org.barrelorgandiscovery.editableinstrument.InstrumentScript.InstrumentScriptType;
import org.barrelorgandiscovery.extensions.ExtensionPointProvider;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.gui.APrintConstants;
import org.barrelorgandiscovery.gui.CancelTracker;
import org.barrelorgandiscovery.gui.ProgressIndicator;
import org.barrelorgandiscovery.gui.aedit.CreationTool;
import org.barrelorgandiscovery.gui.aedit.GlobalVirtualBookUndoOperation;
import org.barrelorgandiscovery.gui.aedit.HoleSelectTool;
import org.barrelorgandiscovery.gui.aedit.IVirtualBookChangedListener;
import org.barrelorgandiscovery.gui.aedit.ImageAndHolesVisualizationLayer;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.aedit.PipeSetGroupLayer;
import org.barrelorgandiscovery.gui.aedit.RegistrationSectionLayer;
import org.barrelorgandiscovery.gui.aedit.ScaleHolesTool;
import org.barrelorgandiscovery.gui.aedit.SpaceCreatorTool;
import org.barrelorgandiscovery.gui.aedit.TempoChangedLayer;
import org.barrelorgandiscovery.gui.aedit.TimeBookLayer;
import org.barrelorgandiscovery.gui.aedit.UndoStack;
import org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayer;
import org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayerName;
import org.barrelorgandiscovery.gui.aedit.VirtualBookMesureLayer;
import org.barrelorgandiscovery.gui.aedit.markers.MarkerCreateTool;
import org.barrelorgandiscovery.gui.aedit.markers.MarkerDeleteTool;
import org.barrelorgandiscovery.gui.aedit.markers.MarkerLayer;
import org.barrelorgandiscovery.gui.aedit.markers.MarkerMoveTool;
import org.barrelorgandiscovery.gui.aedit.toolbar.JVBToolingToolbar;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprint.CartonVirtuelPrintDocument;
import org.barrelorgandiscovery.gui.aprint.MeasureTool;
import org.barrelorgandiscovery.gui.aprint.PanTool;
import org.barrelorgandiscovery.gui.aprint.PrintPreview;
import org.barrelorgandiscovery.gui.aprint.SequencerTools;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InformCurrentInstrumentExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InformCurrentVirtualBookExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.LayersExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.ToolbarAddExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.VirtualBookToolbarButtonsExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.VisibilityLayerButtonsExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.markers.IMarkerChangedListener;
import org.barrelorgandiscovery.gui.aprint.markers.JMarkerComponents;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.InformVirtualBookFrameExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.InitNGExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.VirtualBookFrameToolRegister;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.VirtualBookIssueRevalidation;
import org.barrelorgandiscovery.gui.aprintng.transferable.HolesListTransferable;
import org.barrelorgandiscovery.gui.issues.IssueRevalidateHook;
import org.barrelorgandiscovery.gui.issues.IssueSelector;
import org.barrelorgandiscovery.gui.issues.JIssuePresenter;
import org.barrelorgandiscovery.gui.script.groovy.APrintGroovyConsolePanel;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
import org.barrelorgandiscovery.images.books.tools.ITiledImage;
import org.barrelorgandiscovery.images.books.tools.RecognitionTiledImage;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.issues.IssueLayer;
import org.barrelorgandiscovery.listeningconverter.EcouteConverter;
import org.barrelorgandiscovery.listeningconverter.MIDIListeningConverter;
import org.barrelorgandiscovery.listeningconverter.VirtualBookToMidiConverter;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.movies.MovieConverter;
import org.barrelorgandiscovery.movies.MovieConverterParameters;
import org.barrelorgandiscovery.playsubsystem.ASyncPreparePlayin;
import org.barrelorgandiscovery.playsubsystem.IPlaySubSystemFeedBack;
import org.barrelorgandiscovery.playsubsystem.NeedInstrument;
import org.barrelorgandiscovery.playsubsystem.NeedMidiListeningConverter;
import org.barrelorgandiscovery.playsubsystem.PlayControl;
import org.barrelorgandiscovery.playsubsystem.PlaySubSystem;
import org.barrelorgandiscovery.playsubsystem.prepared.IPreparedCapableSubSystem;
import org.barrelorgandiscovery.playsubsystem.prepared.IPreparedPlaying;
import org.barrelorgandiscovery.prefs.PrefixedNamePrefsStorage;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Collection;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.BeanAsk;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.MP3Tools;
import org.barrelorgandiscovery.tools.OGGTools;
import org.barrelorgandiscovery.tools.SwingUtils;
import org.barrelorgandiscovery.tools.VFSTools;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.ui.tools.ToolWindowTools;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.VirtualBookMetadata;
import org.barrelorgandiscovery.virtualbook.tools.HoleTools;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFile;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFileIO;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO;
import org.noos.xing.mydoggy.DockedTypeDescriptor;
import org.noos.xing.mydoggy.PersistenceDelegate;
import org.noos.xing.mydoggy.ToolWindow;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.ToolWindowType;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;

import com.l2fprod.common.demo.BeanBinder;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

import groovy.lang.Binding;
import groovy.ui.GroovyMain;

/**
 * Internal frame for the virtual book displaying and working
 *
 * @author Freydiere Patrice
 */
public class APrintNGVirtualBookInternalFrame extends APrintNGInternalFrame
		implements ActionListener, APrintNGVirtualBookFrame, ClipboardOwner {

	public static final String BACKGROUNDLAYER_INTERNALNAME = "BACKGROUNDLAYER";

	private static final String QUICK_SCRIPT_EXECUTE = Messages.getString("APrintNGVirtualBookInternalFrame.2001"); //$NON-NLS-1$

	/** */
	private static final long serialVersionUID = -6330679425485407354L;

	static Logger logger = Logger.getLogger(APrintNGVirtualBookInternalFrame.class);

	private class SetBackGroundAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6972644892906915997L;

		public SetBackGroundAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {

				APrintFileChooser aPrintFileChooser = new APrintFileChooser();
				aPrintFileChooser.addFileFilter(
						new VFSFileNameExtensionFilter("Images PNG", new String[] { "png", "jpg", "jpeg" }));

				aPrintFileChooser.addFileFilter(new VFSFileNameExtensionFilter("Book image",
						new String[] { BookImage.BOOKIMAGE_EXTENSION_WITHOUT_DOT }));

				int returnedValue = aPrintFileChooser.showOpenDialog(APrintNGVirtualBookInternalFrame.this);
				if (returnedValue == APrintFileChooser.APPROVE_OPTION) {
					AbstractFileObject selectedFile = aPrintFileChooser.getSelectedFile();
					if (selectedFile != null) {
						File f = VFSTools.convertToFile(selectedFile);

						if (f.getName().toLowerCase().endsWith(BookImage.BOOKIMAGE_EXTENSION)) {

							ZipBookImage zbook = new ZipBookImage(f);

							imageBackGroundLayer.setTiledBackgroundimage(zbook);
						} else if (f.isDirectory()) {

							RecognitionTiledImage tiledImage = new RecognitionTiledImage(
									new File(f.getParentFile(), f.getName()));
							tiledImage.setCurrentImageFamilyDisplay("renormed");

							imageBackGroundLayer.setTiledBackgroundimage(tiledImage);
						} else {
							logger.error("cannot display image " + f);
						}
					}
				}

			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	private Instrument instrument = null;

	private JToolBar exportToolbar;

	private Vector<JDialog> associatedFrames = new Vector<JDialog>();

	/** Panneau contenant le choix de la transposition */
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

	private JPanel pianorollpanel = new JPanel();

	private JPanel panelPrincipal = new JPanel();

	// private JIssuePresenter issuePresenter;

	private JToolBar buttonActionToolbar = new JToolBar();

	private JCheckBox showTimeLayerCheckBox;

	private JCheckBox showErrorsLayerCheckBox;

	private int lastSavedVirtualBookHash = -1;

	private void clearVirtualBookState() {
		lastSavedVirtualBookHash = computeVirtualBookHashCode();
		updateTitle();
	}

	/** */
	private int computeVirtualBookHashCode() {
		int lastSavedVirtualBookHash = -1;
		VirtualBook virtualBook = getVirtualBook();
		if (virtualBook != null)
			lastSavedVirtualBookHash = virtualBook.hashCode();
		return lastSavedVirtualBookHash;
	}

	private boolean isVirtualBookDirty() {
		return lastSavedVirtualBookHash != computeVirtualBookHashCode();
	}

	/** Virtual book component rendering */
	private JEditableVirtualBookComponent pianoroll = new JEditableVirtualBookComponent();

	/** Layer contenant les problemes de transposition */
	private IssueLayer il = new IssueLayer();

	private TimeBookLayer tbl = new TimeBookLayer();

	private PipeSetGroupLayer psgl = new PipeSetGroupLayer();

	private RegistrationSectionLayer rsl = new RegistrationSectionLayer();

	private VirtualBookMesureLayer vbml = new VirtualBookMesureLayer();

	private TempoChangedLayer tcl = new TempoChangedLayer();

	private ImageAndHolesVisualizationLayer imageBackGroundLayer = new ImageAndHolesVisualizationLayer();

	private MarkerLayer markerLayer = new MarkerLayer();

	private IAPrintWait waitininterface;

	private JMarkerComponents markerComponents = new JMarkerComponents();

	private APrintProperties aprintproperties;

	private IPlaySubSystemManager playsubsystem;

	private IExtension[] exts;

	private APrintPageFormat aprintpageformat;

	private APrintNGGeneralServices services;

	private AsyncJobsManager asyncJobsManager;

	private QuickScriptManager scriptManager;

	// manage the precompute of the virtualbook listening

	/** Remembered saved file ... */
	private AbstractFileObject currentSavedFile = null;

	/**
	 * tool window manager
	 */
	private MyDoggyToolWindowManager toolWindowManager;

	private JButton lengthButton = new JButton();

	private JSlider tempoFactorSlider = new JSlider();

	private float crossThreadTempoSlider = 1.0f;

	public APrintNGVirtualBookInternalFrame(VirtualBook vb, AbstractFileObject virtualBookFile, IssueCollection ic,
			Instrument instrument, APrintProperties aprintproperties, IPlaySubSystemManager playsubSystem,
			IExtension[] exts, APrintPageFormat pageformat, APrintNGGeneralServices services,
			AsyncJobsManager jobManager, QuickScriptManager scriptManager) throws Exception {

		super(aprintproperties.getFilePrefsStorage(), Messages.getString("APrintNGVirtualBookInternalFrame.1") + " " //$NON-NLS-2$
				+ vb.getName() + "," //$NON-NLS-1$
				+ " " //$NON-NLS-1$
				+ Messages.getString("APrintNGVirtualBookInternalFrame.2000")//$NON-NLS-1$
				+ instrument.getName(), true, true, true, true);
		assert vb != null;

		this.waitininterface = this;
		this.aprintproperties = aprintproperties;
		this.playsubsystem = playsubSystem;
		this.exts = exts;
		this.aprintpageformat = pageformat;
		this.services = services;
		this.scriptManager = scriptManager;
		this.currentSavedFile = virtualBookFile;
		this.asyncJobsManager = jobManager;

		aSyncPreparePlayin = new ASyncPreparePlayin();

		internalDefineInstrument(instrument);

		bookPropertiesPropertySheetPanel = new PropertySheetPanel();
		bookPropertiesPropertySheetPanel.setBeanInfo(new VirtualBookMetadataBeanInfo());
		bookPropertiesPropertySheetPanel.addPropertySheetChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String name = getVirtualBook().getMetadata().getName();
				if (evt.getSource() instanceof Property) {
					if ("name".equals(((Property) evt.getSource()).getName())) {//$NON-NLS-1$
						name = "" + evt.getNewValue();//$NON-NLS-1$
					}
				}
				getVirtualBook().setName(name);
				toggleDirty();
				updateTitle();
			}
		});

		// book properties panel must be defined before
		internalChangeVirtualBook(vb);

		layerPreferences = new PrefixedNamePrefsStorage("layervisibilitypreferences", //$NON-NLS-1$
				aprintproperties.getFilePrefsStorage());

		psslistener = new IPlaySubSystemManagerListener() {
			public void playStopped() {
				try {
					checkState();
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					BugReporter.sendBugReport();
				}
			}

			public void startPlaying() {
				try {
					checkState();
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					BugReporter.sendBugReport();
				}
			}
		};

		playsubSystem.addPlaySubSystemManagerListener(psslistener);

		InitNGExtensionPoint[] extpoints = ExtensionPointProvider.getAllPoints(InitNGExtensionPoint.class, exts);
		for (int i = 0; i < extpoints.length; i++) {
			InitNGExtensionPoint vbextpoints = extpoints[i];
			try {
				logger.debug("init " + vbextpoints); //$NON-NLS-1$
				vbextpoints.init((APrintNG) services);
				logger.debug("done "); //$NON-NLS-1$

			} catch (Throwable t) {
				logger.error("Extension " + vbextpoints // $NON-NLS-1$
						+ " throw an exception", t); // $NON-NLS-1$
				BugReporter.sendBugReport();
			}
		}

		// instrument first, some parameters might be necessary for the
		// virtualbook
		InformCurrentInstrumentExtensionPoint[] iep = ExtensionPointProvider
				.getAllPoints(InformCurrentInstrumentExtensionPoint.class, exts);
		for (int i = 0; i < iep.length; i++) {
			InformCurrentInstrumentExtensionPoint vbextpoints = iep[i];
			try {
				vbextpoints.informCurrentInstrument(instrument);
			} catch (Throwable t) {
				logger.error("Extension "//$NON-NLS-1$
						+ vbextpoints + " throw an exception", //$NON-NLS-1$
						t);
				BugReporter.sendBugReport();
			}
		}

		// virtual book secondly
		InformCurrentVirtualBookExtensionPoint[] ivb = ExtensionPointProvider
				.getAllPoints(InformCurrentVirtualBookExtensionPoint.class, exts);
		for (int i = 0; i < ivb.length; i++) {
			InformCurrentVirtualBookExtensionPoint vbextpoints = ivb[i];
			try {
				vbextpoints.informCurrentVirtualBook(vb);
			} catch (Throwable t) {
				logger.error("Extension "//$NON-NLS-1$
						+ vbextpoints + " throw an exception", //$NON-NLS-1$
						t);
				BugReporter.sendBugReport();
			}
		}

		InformVirtualBookFrameExtensionPoint[] vbfiep = ExtensionPointProvider
				.getAllPoints(InformVirtualBookFrameExtensionPoint.class, exts);
		for (int i = 0; i < vbfiep.length; i++) {
			InformVirtualBookFrameExtensionPoint virtualBookFrameInformExtensionPoint = vbfiep[i];
			try {
				virtualBookFrameInformExtensionPoint.informVirtualBookFrame(this);
			} catch (Throwable t) {
				logger.error("Extension "//$NON-NLS-1$
						+ virtualBookFrameInformExtensionPoint + " throw an exception", //$NON-NLS-1$
						t);
				BugReporter.sendBugReport();
			}
		}

		initComponents(ic);

		addWindowListener(new WindowListener() {

			public void windowOpened(WindowEvent e) {
			}

			public void windowIconified(WindowEvent e) {
			}

			public void windowDeiconified(WindowEvent e) {
			}

			public void windowDeactivated(WindowEvent e) {
			}

			public void windowClosing(WindowEvent arg0) {

				try {
					// PlaySubSystem current = playsubsystem.getCurrent();
					// if (current.isPlaying()) {
					// if (current.getOwner() ==
					// APrintNGVirtualBookInternalFrame.this)
					// current.stop();
					// }
					//
					// playsubsystem
					// .removePlaySubSystemManagerListener(psslistener);

					// dispose();

				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					BugReporter.sendBugReport();
				}
			}

			public void windowClosed(WindowEvent arg0) {
			}

			public void windowActivated(WindowEvent arg0) {
				if (logger.isDebugEnabled()) {
					logger.debug("virtualBookFrame window activated"); //$NON-NLS-1$
				}
			}
		});

		pianoroll.addVirtualBookChangedListener(new IVirtualBookChangedListener() {
			public void virtualBookChanged(VirtualBook newVirtualBook) {
				try {
					logger.debug("Frame received the event"); //$NON-NLS-1$
					touchVirtualBook();
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					BugReporter.sendBugReport();
				}
			}
		});

		pianoroll.fitToScreen();

		tempoFactorSlider.setToolTipText("Play Tempo Change");

		clearVirtualBookState();
		clearDirty();
	}

	protected void internalDefineInstrument(Instrument instrument) {
		this.instrument = instrument;

		PlaySubSystem currentPlaySubSystem = playsubsystem.getCurrent();

		if (currentPlaySubSystem instanceof NeedInstrument) {
			NeedInstrument ni = (NeedInstrument) currentPlaySubSystem;
			ni.setCurrentInstrument(instrument);
		}

		preparePlayin(pianoroll.getVirtualBook());
	}

	/** @param vb */
	protected void internalChangeVirtualBook(VirtualBook vb) {
		pianoroll.setVirtualBook(vb);

		VirtualBookMetadata metadata = vb.getMetadata();
		if (metadata == null) {
			metadata = new VirtualBookMetadata();
			vb.setMetadata(metadata);
		}
		new BeanBinder(metadata, bookPropertiesPropertySheetPanel, new VirtualBookMetadataBeanInfo());
		metadata.setName(vb.getName());

		touchVirtualBook();
	}

	/**
	 * Define the current file in File System, for proposing the save ...
	 *
	 * @param currentSavedFile
	 */
	public void setCurrentSavedFile(File currentSavedFile) throws Exception {
		this.currentSavedFile = APrintFileChooser.convertToFileObject(currentSavedFile);
		updateTitle();
	}

	/**
	 * define the background image using a tiled image
	 * 
	 * @param tiledImage
	 */
	public void setBackGroundImage(ITiledImage tiledImage) {
		imageBackGroundLayer.setTiledBackgroundimage(tiledImage);
	}

	/**
	 * init the visual components
	 *
	 * @throws Exception
	 */
	private void initComponents(IssueCollection ic) throws Exception {

		VirtualBook virtualBook = getVirtualBook();

		JMenuBar mb = new JMenuBar();
		JMenu files = new JMenu(Messages.getString("APrintNGVirtualBookInternalFrame.2")); //$NON-NLS-1$

		JMenuItem save = files.add(Messages.getString("APrintNGVirtualBookInternalFrame.2002")); //$NON-NLS-1$
		save.setActionCommand("SAVE"); //$NON-NLS-1$
		save.addActionListener(this);
		save.setMnemonic('s');
		save.setAccelerator(KeyStroke.getKeyStroke("control S")); //$NON-NLS-1$
		files.add(save);

		JMenuItem saveas = files.add(Messages.getString("APrintNGVirtualBookInternalFrame.2103")); //$NON-NLS-1$
		saveas.setActionCommand("SAVEAS"); //$NON-NLS-1$
		saveas.addActionListener(this);

		files.addSeparator();
		JMenuItem saveAs2010 = files.add(Messages.getString("APrintNGVirtualBookInternalFrame.2110")); //$NON-NLS-1$
		saveAs2010.addActionListener(this);
		saveAs2010.setActionCommand("SAVEAS2010"); //$NON-NLS-1$

		files.addSeparator();
		JMenuItem exit = files.add(Messages.getString("APrintNGVirtualBookInternalFrame.5")); //$NON-NLS-1$
		exit.setActionCommand("EXIT"); //$NON-NLS-1$
		exit.addActionListener(this);

		mb.add(files);

		JMenu editMenu = new JMenu("Edit"); //$NON-NLS-1$

		JMenuItem undo = new JMenuItem("Undo"); //$NON-NLS-1$
		undo.setMnemonic('z'); // $NON-NLS-1$
		undo.setAccelerator(KeyStroke.getKeyStroke("control Z")); //$NON-NLS-1$
		undo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					UndoStack undoStack = pianoroll.getUndoStack();
					undoStack.undoLastOperation();

				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					BugReporter.sendBugReport();
				}
			}
		});
		editMenu.add(undo);

		editMenu.addSeparator();

		JMenuItem copy = new JMenuItem(Messages.getString("APrintNGVirtualBookInternalFrame.2013")); //$NON-NLS-1$
		copy.setMnemonic('c');
		copy.setAccelerator(KeyStroke.getKeyStroke("control C")); //$NON-NLS-1$
		copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					copySelectionToClipBoard();
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					BugReporter.sendBugReport();
				}
			}
		});
		editMenu.add(copy);

		JMenuItem paste = new JMenuItem(Messages.getString("APrintNGVirtualBookInternalFrame.2014")); //$NON-NLS-1$
		paste.setMnemonic('v');
		paste.setAccelerator(KeyStroke.getKeyStroke("control V")); //$NON-NLS-1$
		paste.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {

					paste();

					pianoroll.repaint();

				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					BugReporter.sendBugReport();
				}
			}
		});
		editMenu.add(paste);

		editMenu.addSeparator();
		JMenuItem selectAll = new JMenuItem("Selected All");
		selectAll.setMnemonic('a');
		selectAll.setAccelerator(KeyStroke.getKeyStroke("control a"));//$NON-NLS-1$
		selectAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {

					selectAll();

					pianoroll.repaint();

				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					BugReporter.sendBugReport();
				}

			}
		});
		editMenu.add(selectAll);

		JMenuItem selectNone = new JMenuItem("Selected None");
		selectNone.setAccelerator(KeyStroke.getKeyStroke("control shift a"));//$NON-NLS-1$
		selectNone.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {

					selectNone();

					pianoroll.repaint();

				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					BugReporter.sendBugReport();
				}

			}
		});
		editMenu.add(selectNone);

		editMenu.addSeparator();

		editMenu.add(new SetBackGroundAction("D�finir l'image de fond du carton .."));

		mb.add(editMenu);

		toolbarMenu = new JMenu(Messages.getString("APrintNGVirtualBookInternalFrame.1130")); //$NON-NLS-1$
		mb.add(toolbarMenu);

		mb.add(Box.createHorizontalGlue());

		setJMenuBar(mb);

		tbl.setVisible(false);

		psgl.setVisible(false);

		rsl.setVisible(false);

		vbml.setVisible(false);

		logger.debug("init components"); //$NON-NLS-1$

		jouer = new JButton();

		jouer.setIcon(new ImageIcon(getClass().getResource("noatunplay.png"), //$NON-NLS-1$
				Messages.getString("APrint.80"))); //$NON-NLS-1$
		jouer.setActionCommand("JOUER"); //$NON-NLS-1$
		jouer.addActionListener(this);
		jouer.setToolTipText(Messages.getString("APrint.129")); //$NON-NLS-1$

		rewind = new JButton();
		rewind.setIcon(new ImageIcon(getClass().getResource("2leftarrow.png"))); //$NON-NLS-1$
		rewind.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pianoroll.clearHightlight();
				pianoroll.setXoffset(-100.0);
				pianoroll.repaint();
			}
		});
		rewind.setToolTipText(Messages.getString("APrint.286")); //$NON-NLS-1$

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

		exportAsWav.setActionCommand("EXPORTTOWAV"); //$NON-NLS-1$
		exportAsWav.addActionListener(this);
		exportAsWav.setIcon(new ImageIcon(getClass().getResource("kdat.png"))); //$NON-NLS-1$
		exportAsWav.setToolTipText(Messages.getString("APrint.139")); //$NON-NLS-1$

		exportAsMp3.setActionCommand("EXPORTTOMP3"); //$NON-NLS-1$
		exportAsMp3.addActionListener(this);
		exportAsMp3.setIcon(new ImageIcon(getClass().getResource("kdat.png"))); //$NON-NLS-1$
		exportAsMp3.setToolTipText(Messages.getString("APrint.0")); //$NON-NLS-1$

		exportAsOgg.setActionCommand("EXPORTTOOGG"); //$NON-NLS-1$
		exportAsOgg.addActionListener(this);
		exportAsOgg.setIcon(new ImageIcon(getClass().getResource("kdat.png"))); //$NON-NLS-1$
		exportAsOgg.setToolTipText(Messages.getString("APrint.220")); //$NON-NLS-1$

		exportAsMidi.setIcon(new ImageIcon(getClass().getResource("kdat.png"))); //$NON-NLS-1$
		exportAsMidi.setToolTipText("Enregistre l'�coute du carton dans un nouveau fichier midi"); //$NON-NLS-1$
		exportAsMidi.setActionCommand("EXPORTTOMID"); //$NON-NLS-1$
		exportAsMidi.addActionListener(this);

		exportAsMovie.setIcon(new ImageIcon(getClass().getResource("movie.png"))); //$NON-NLS-1$
		exportAsMovie.setText(Messages.getString("APrint.307")); //$NON-NLS-1$
		exportAsMovie.setToolTipText(Messages.getString("APrint.308")); //$NON-NLS-1$
		exportAsMovie.addActionListener(this);
		exportAsMovie.setActionCommand("EXPORTMOV"); //$NON-NLS-1$

		buttonActionToolbar.add(rewind);

		// we use pooling between
		tempoFactorSlider.setMinimum(0);
		tempoFactorSlider.setMaximum(200);
		tempoFactorSlider.setValue(100);
		tempoFactorSlider.setPreferredSize(new Dimension(300, 20));
		tempoFactorSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider s = (JSlider) e.getSource();
				crossThreadTempoSlider = s.getValue() / 100f;
			}
		});

		buttonActionToolbar.add(jouer);
		buttonActionToolbar.add(tempoFactorSlider);
		buttonActionToolbar.add(preview);
		buttonActionToolbar.add(imprimer);
		// buttonActionToolbar.add(tracer);

		// calling extensions for toolbars
		VirtualBookToolbarButtonsExtensionPoint[] allToolbarPoints = ExtensionPointProvider
				.getAllPoints(VirtualBookToolbarButtonsExtensionPoint.class, exts);
		for (int i = 0; i < allToolbarPoints.length; i++) {
			VirtualBookToolbarButtonsExtensionPoint addToolbarButtons = allToolbarPoints[i];
			try {
				addToolbarButtons.addButtons(buttonActionToolbar);
			} catch (Throwable t) {
				logger.error("error adding toolbars :" + t.getMessage(), t); //$NON-NLS-1$
				BugReporter.sendBugReport();
			}
		}

		// ajout de la couche contenant les probl�mes de transposition
		pianoroll.addLayer(psgl);
		pianoroll.addLayer(il);
		pianoroll.addLayer(tbl);
		pianoroll.addLayer(rsl);
		pianoroll.addLayer(vbml);
		pianoroll.addLayer(tcl);
		pianoroll.addLayer(markerLayer);
		pianoroll.addLayer(imageBackGroundLayer);

		imageBackGroundLayer.setLayerInternalName(BACKGROUNDLAYER_INTERNALNAME);

		logger.debug("add layers from extensions"); //$NON-NLS-1$
		LayersExtensionPoint[] allLayersPoints = ExtensionPointProvider.getAllPoints(LayersExtensionPoint.class, exts);

		for (int i = 0; i < allLayersPoints.length; i++) {
			LayersExtensionPoint addLayers = allLayersPoints[i];
			try {
				addLayers.addLayers(pianoroll);
			} catch (Throwable t) {
				logger.error("error adding layer :" + t.getMessage(), t); //$NON-NLS-1$
				BugReporter.sendBugReport();
			}
		}

		// pianorollpanel.setBorder(new TitledBorder(Messages.getString("APrint.52")));
		// //$NON-NLS-1$
		pianorollpanel.setLayout(new BorderLayout());
		pianoroll.setPreferredSize(new Dimension(700, 300));
		pianoroll.setUseFastDrawing(true);

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

				pianoroll.requestFocusInWindow();

				logger.debug("mouse pressed :" + e.getButton()); //$NON-NLS-1$

				if (e.getButton() == MouseEvent.BUTTON2) {
					posx = e.getX();

					origineX = pianoroll.getXoffset();

					pan = true;
					logger.debug("pan started"); //$NON-NLS-1$

					pianoroll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

				} else {

					// normal click on the book

					// r�cup�ration du click sur le carton ...
					pianoroll.setHightlight(pianoroll.convertScreenXToCarton(e.getX()));

					pianoroll.repaint();

				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {

				pan = false;
				pianoroll.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

				if (e.getButton() == MouseEvent.BUTTON1) {

				}
			}
		}

		// pan on the book
		MouseEvents m = new MouseEvents();
		pianoroll.addMouseListener(m);
		pianoroll.addMouseMotionListener(m);

		pianoroll.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
						Set<Hole> selectionCopy = pianoroll.getSelectionCopy();
						if (selectionCopy.size() > 0) {
							UndoStack undoStack = pianoroll.getUndoStack();

							VirtualBook virtualBook2 = getVirtualBook();

							undoStack.push(new GlobalVirtualBookUndoOperation(virtualBook2,
									Messages.getString("APrintNGVirtualBookInternalFrame.1100"), pianoroll)); // $NON-NLS-1$

							pianoroll.startEventTransaction();
							try {
								for (Iterator iterator = selectionCopy.iterator(); iterator.hasNext();) {
									Hole hole = (Hole) iterator.next();
									virtualBook2.removeHole(hole);
								}
								pianoroll.clearSelection();
							} finally {
								pianoroll.endEventTransaction();
							}
						}
					}
					super.keyReleased(e);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					BugReporter.sendBugReport();
				}
			}
		});

		JIssuePresenter issuePresenter = new JIssuePresenter(this);
		issuePresenter.setIssueLayer(il);
		issuePresenter.setVirtualBook(getVirtualBook());

		issuePresenter.setIssueRevalidateHook(new IssueRevalidateHook() {

			public void addAdditionalChecks(VirtualBook vb, IssueCollection resultIssueCollection) {

				VirtualBookIssueRevalidation[] allIssueRevalidation = ExtensionPointProvider
						.getAllPoints(VirtualBookIssueRevalidation.class, exts);
				for (int i = 0; i < allIssueRevalidation.length; i++) {
					VirtualBookIssueRevalidation issueRevalidation = allIssueRevalidation[i];
					try {
						issueRevalidation.fillIssuesForVirtualBook(vb, resultIssueCollection);
					} catch (Exception ex) {
						logger.error("error in revalidation :" + ex.getMessage(), ex); //$NON-NLS-1$
						BugReporter.sendBugReport();
					}
				}
			}
		});

		issuePresenter.setIssueSelectionListener(new IssueSelector(il, pianoroll));

		issuePresenter.setToolTipText(Messages.getString("APrint.288")); //$NON-NLS-1$

		issuePresenter.setMinimumSize(new Dimension(200, 200));

		markerComponents.addMarkerChangedListener(new IMarkerChangedListener() {

			public void markersChanged() {
				pianoroll.repaint();
			}
		});

		markerComponents.setEditableComponent(pianoroll);

		MyDoggyToolWindowManager myDoggyToolWindowManager = new MyDoggyToolWindowManager();
		this.toolWindowManager = myDoggyToolWindowManager;

		// issue window
		ToolWindow tw = toolWindowManager.registerToolWindow(
				Messages.getString("APrintNGVirtualBookInternalFrame.2100"), // Id //$NON-NLS-1$
				Messages.getString("APrintNGVirtualBookInternalFrame.2101"), // Title //$NON-NLS-1$
				null, // Icon
				issuePresenter, // Component
				ToolWindowAnchor.RIGHT); // Anchor

		ToolWindowTools.defineProperties(tw);

		logger.debug("register marker tool window");
		// change width
		DockedTypeDescriptor desc = (DockedTypeDescriptor) tw.getTypeDescriptor(ToolWindowType.DOCKED);
		desc.setDockLength(400);

		tw = toolWindowManager.registerToolWindow(Messages.getString("APrintNGVirtualBookInternalFrame.3101"), //$NON-NLS-1$
				Messages.getString("APrintNGVirtualBookInternalFrame.3102"), //$NON-NLS-1$
				null, // Icon
				markerComponents, // Component
				ToolWindowAnchor.RIGHT); // Anchor
		ToolWindowTools.defineProperties(tw);

		JPanel bookPropertiesPanel = new JPanel();
		bookPropertiesPanel.setLayout(new BorderLayout());

		bookPropertiesPanel.add(bookPropertiesPropertySheetPanel, BorderLayout.CENTER);

		logger.debug("register book properties window");
		tw = toolWindowManager.registerToolWindow("Propriétés du carton", "Propriétés du carton", null,
				bookPropertiesPanel, ToolWindowAnchor.LEFT);

		// change width
		desc = (DockedTypeDescriptor) tw.getTypeDescriptor(ToolWindowType.DOCKED);
		desc.setDockLength(480);

		logger.debug("add extensions register tool windows");
		VirtualBookFrameToolRegister[] allVBTool = ExtensionPointProvider
				.getAllPoints(VirtualBookFrameToolRegister.class, exts);

		for (int i = 0; i < allVBTool.length; i++) {
			VirtualBookFrameToolRegister virtualBookFrameToolRegister = allVBTool[i];
			try {
				virtualBookFrameToolRegister.registerToolWindow(toolWindowManager);
			} catch (Exception ex) {
				logger.error("error registering toolwindow " //$NON-NLS-1$
						+ virtualBookFrameToolRegister.getClass().getName() + " " //$NON-NLS-1$
						+ ex.getMessage(), ex);
				BugReporter.sendBugReport();
			}
		}

		// Made all tools available
		for (ToolWindow window : toolWindowManager.getToolWindows()) {
			window.setAvailable(true);
			window.setAutoHide(false);
			window.setLockedOnAnchor(true);
		}

		toolWindowManager.getContentManager().addContent(Messages.getString("APrintNGVirtualBookInternalFrame.2102"), //$NON-NLS-1$
				null, null, pianoroll);

		pianorollpanel.add(toolWindowManager, BorderLayout.CENTER);

		// boutons de visualisation du pianoroll ...

		JVBToolingToolbar pianorolltb = new JVBToolingToolbar(pianoroll, pianoroll.getUndoStack(),
				pianoroll.getSnappingEnvironment());

		pianorollbutton = new JPanel();
		BoxLayout bl_pianorollbutton = new BoxLayout(pianorollbutton, BoxLayout.X_AXIS);
		pianorollbutton.setLayout(bl_pianorollbutton);

		createLayerToolbarOptions(pianorolltb, pianorollbutton);

		pianorolltb.add(lengthButton);

		pianorollbutton.add(buttonActionToolbar);

		toolbarPanel = new JPanel();
		WrappingLayout wrappingLayout = new WrappingLayout(WrappingLayout.LEFT, 1, 1);

		toolbarPanel.setLayout(wrappingLayout);
		toolbarPanel.add(pianorollbutton);
		pianorollbutton.setVisible(aprintproperties.getToolbarVisibility(pianorollbutton.getName()));

		toolbarPanel.setMinimumSize(new Dimension(10, 50));

		ToolbarAddExtensionPoint[] allToolBars = ExtensionPointProvider.getAllPoints(ToolbarAddExtensionPoint.class,
				exts);
		for (int i = 0; i < allToolBars.length; i++) {
			ToolbarAddExtensionPoint tep = allToolBars[i];
			try {
				JToolBar[] tbs = tep.addToolBars();
				if (tbs != null) {
					for (int j = 0; j < tbs.length; j++) {
						logger.debug("adding toolbar"); //$NON-NLS-1$
						if (tbs[j] != null) {
							toolbarPanel.add(tbs[j]);
							tbs[j].setVisible(aprintproperties.getToolbarVisibility(tbs[j].getName()));
						}
					}
				}
			} catch (Throwable t) {
				logger.error("fail to add toolbar :" + tep, t); //$NON-NLS-1$
				BugReporter.sendBugReport();
			}
		}

		//////////////////////////////////////////////////////////////////////////
		// export Toolbar ...

		exportToolbar = new JToolBar(Messages.getString("APrintNGVirtualBookInternalFrame.8")); //$NON-NLS-1$
		exportToolbar.add(exportAsWav);
		// buttonActionToolbar.add(exportAsMp3);
		exportToolbar.add(exportAsOgg);
		exportToolbar.add(exportAsMidi);
		exportToolbar.add(exportAsMovie);

		toolbarPanel.add(exportToolbar);
		exportToolbar.setVisible(aprintproperties.getToolbarVisibility(exportToolbar.getName()));

		// poncif toolbar
		APrintNGToolAwareToolbar poncifToolbar = new APrintNGToolAwareToolbar(
				Messages.getString("APrintNGVirtualBookInternalFrame.3103"), pianoroll); //$NON-NLS-1$
		MarkerCreateTool markerCreateTool = new MarkerCreateTool(pianoroll);
		poncifToolbar.addTool(markerCreateTool, "", // Messages.getString("APrintNGVirtualBookInternalFrame.3104"), //$NON-NLS-1$
				ImageTools.loadIcon(MarkerDeleteTool.class, "applix.png"), //$NON-NLS-1$
				Messages.getString("APrintNGVirtualBookInternalFrame.3106")); //$NON-NLS-1$

		MarkerMoveTool markerMoveTool = new MarkerMoveTool(pianoroll);
		poncifToolbar.addTool(markerMoveTool, "", // Messages.getString("APrintNGVirtualBookInternalFrame.3107"), //$NON-NLS-1$
				ImageTools.loadIcon(MarkerMoveTool.class, "applixmove.png"), //$NON-NLS-1$
				Messages.getString("APrintNGVirtualBookInternalFrame.3109")); //$NON-NLS-1$

		MarkerDeleteTool markerDeleteTool = new MarkerDeleteTool(pianoroll);
		poncifToolbar.addTool(markerDeleteTool, "", // Messages.getString("APrintNGVirtualBookInternalFrame.3110"), //$NON-NLS-1$
				ImageTools.loadIcon(MarkerDeleteTool.class, "applixdelete.png"), //$NON-NLS-1$
				Messages.getString("APrintNGVirtualBookInternalFrame.3112")); //$NON-NLS-1$

		toolbarPanel.add(poncifToolbar);

		/////////////////////////////////////////////////////////////////////////
		// Editing toolbar

		APrintNGToolAwareToolbar editingToolbar = new APrintNGToolAwareToolbar(
				Messages.getString("APrintNGVirtualBookInternalFrame.9"), pianoroll); //$NON-NLS-1$

		CreationTool tool = new CreationTool(pianoroll, pianoroll.getUndoStack(), pianoroll.getSnappingEnvironment());

		editingToolbar.addTool(tool, "", //
				new ImageIcon(getClass().getResource("edit.png")), //$NON-NLS-1$
				Messages.getString("APrintNGVirtualBookInternalFrame.10")); //$NON-NLS-1$

		editingToolbar.addSeparator();

		JButton undoButton = editingToolbar.addButton(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					UndoStack undoStack = pianoroll.getUndoStack();
					undoStack.undoLastOperation();

				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					BugReporter.sendBugReport();
				}
			}
		}, "", // , //$NON-NLS-1$
				new ImageIcon(getClass().getResource("undo.png"))); //$NON-NLS-1$
		undoButton.setToolTipText(Messages.getString("APrintNGVirtualBookInternalFrame.11"));//$NON-NLS-1$

		JButton redoButton = editingToolbar.addButton(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					UndoStack undoStack = pianoroll.getUndoStack();
					undoStack.redoLastOperation();

				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					BugReporter.sendBugReport();
				}
			}
		}, "", // , //$NON-NLS-1$
				new ImageIcon(getClass().getResource("redo.png"))); //$NON-NLS-1$
		redoButton.setToolTipText(Messages.getString("APrintNGVirtualBookInternalFrame.12"));//$NON-NLS-1$

		editingToolbar.addSeparator();

		HoleSelectTool hs = new HoleSelectTool(pianoroll, pianoroll.getUndoStack());

		editingToolbar.addTool(hs, "", // ,
				new ImageIcon(getClass() // $NON-NLS-1$
						.getResource("selection.png")),
				Messages.getString("APrintNGVirtualBookInternalFrame.13")); //$NON-NLS-1$

		JButton clearSelection = editingToolbar.addButton(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pianoroll.clearSelection();
				pianoroll.repaint();
			}
		}, "", //
				new ImageIcon(getClass().getResource( // $NON-NLS-1$
						"clearselection.png"))); // $NON-NLS-1$
		clearSelection.setToolTipText(Messages.getString("APrintNGVirtualBookInternalFrame.15"));
		editingToolbar.addSeparator();

		SpaceCreatorTool spaceCreatorTool = new SpaceCreatorTool(pianoroll);
		editingToolbar.addTool(spaceCreatorTool, "", // Messages.getString("APrintNGVirtualBookInternalFrame.3113"), //$NON-NLS-1$
				ImageTools.loadIcon(SpaceCreatorTool.class, "space.png"), //$NON-NLS-1$
				Messages.getString("APrintNGVirtualBookInternalFrame.3130") //$NON-NLS-1$
		); // $NON-NLS-1$

		editingToolbar.addTool(new ScaleHolesTool(pianoroll), "", //$NON-NLS-1$
				ImageTools.loadIcon(getClass(), "scale-holes.png"), //$NON-NLS-1$
				"Interactive Scale Tool");

		editingToolbar.addSeparator();
		JButton groovyButton = editingToolbar.addButton(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showScriptConsoleAssociatedWithVirtualBook();
			}
		}, "", // "Groovy",//$NON-NLS-1$
				new ImageIcon(ImageTools.loadImageAndCrop(GroovyMain.class.getResource("ConsoleIcon.png"), //$NON-NLS-1$
						22, 22)));

		groovyButton.setToolTipText("Open Script Console");

		// JButton modify = new JButton("Modify");
		// modify.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		//
		//
		//
		// pianoroll.setCurrentTool(tool);
		//
		// }
		// });
		//
		// editingToolbar.add(modify);

		editingToolbar.add(createQuickScriptAccess());

		toolbarPanel.add(editingToolbar);
		editingToolbar.setVisible(aprintproperties.getToolbarVisibility(editingToolbar.getName()));

		// adding the measure Tool ...

		measureTool = new MeasureTool(pianoroll);

		panTool = new PanTool(pianoroll);

		pianorollpanel.add(toolbarPanel, BorderLayout.NORTH);

		panelPrincipal.setLayout(new BorderLayout());

		setLayout(new BorderLayout());
		getContentPane().add(pianorollpanel, BorderLayout.CENTER);

		pianoroll.setVirtualBook(virtualBook);
		pianoroll.setMinimumSize(new Dimension(400, 200));

		int width = getWidth();

		logger.debug("width :" + width); //$NON-NLS-1$

		il.setIssueCollection(ic, virtualBook);

		pianoroll.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {

				if (e.getKeyChar() == ' ') {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							PlaySubSystem cps = playsubsystem.getCurrent();
							String cmd = "JOUER"; //$NON-NLS-1$
							try {
								if (cps != null && cps.isPlaying())
									cmd = "STOP"; //$NON-NLS-1$
							} catch (Exception ex) {
								logger.error("exception in getting isplaying action :" //$NON-NLS-1$
										+ ex.getMessage(), ex);
							}

							actionPerformed(new ActionEvent(this, 0, cmd));
						}
					});
					e.consume();
					return;
				}

				super.keyTyped(e);
			}
		});

		// user preferences, when all events are installed

		// ne montre pas les erreurs par d�faut
		il.setVisible(aprintproperties.isErrorsVisible());

		// getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
		// "PLAYBOOK");//$NON-NLS-1$
		// getActionMap().put("PLAYBOOK", jouer.getAction());//$NON-NLS-1$

		refreshToolbarMenu();
	}

	private void loadToolbarPreferences() {
		try {
			PersistenceDelegate pdelegate = toolWindowManager.getPersistenceDelegate();
			File f = constructToolWindowFile();
			if (f.exists()) {
				FileInputStream fis = new FileInputStream(f);
				try {
					// pdelegate.apply(fis);
					pdelegate.merge(fis, PersistenceDelegate.MergePolicy.UNION);
				} finally {
					fis.close();
				}
			}
		} catch (Exception ex) {
			logger.info("fail to read the tool windows prefs :" + ex.getMessage(), ex); //$NON-NLS-1$
		}
	}

	private File constructToolWindowFile() {
		return new File(aprintproperties.getAprintFolder(), "toolwindowlocations.serial"); //$NON-NLS-1$
	}

	/** Menu for the toolbars */
	private JMenu toolbarMenu;

	/** panel containing the toolbars */
	private JPanel toolbarPanel;

	/** Measure Tool */
	private MeasureTool measureTool;

	/** Pan Tool */
	private PanTool panTool;

	protected void refreshToolbarMenu() {
		// clear all the items
		toolbarMenu.removeAll();
		toolbarMenu.setArmed(false);
		toolbarMenu.setFocusable(false);

		for (int i = 0; i < toolbarPanel.getComponentCount(); i++) {
			Component c = toolbarPanel.getComponent(i);
			if (c instanceof JToolBar) {
				final JToolBar tb = (JToolBar) c;
				final JCheckBoxMenuItem chk = new JCheckBoxMenuItem(tb.getName(), tb.isVisible());
				chk.setFocusable(false);
				chk.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						JCheckBoxMenuItem c = (JCheckBoxMenuItem) e.getSource();
						boolean checked = c.isSelected();

						tb.setVisible(checked);
						aprintproperties.setToolbarVisibility(tb.getName(), checked);

						toolbarPanel.invalidate();
						// toolbarPanel.repaint();
						// refreshToolbarMenu();
					}
				});

				toolbarMenu.add(chk);
			}
			toolbarMenu.repaint();
		}
	}

	/** inform by development that the virtual book has been modified */
	protected void touchVirtualBook() {

		logger.debug("touchVirtualBook"); //$NON-NLS-1$

		String caption = Messages.getString("APrintNGVirtualBookInternalFrame.2022"); //$NON-NLS-1$

		VirtualBook virtualBook = getVirtualBook();

		// bookPropertiesPropertySheetPanel.set

		if (virtualBook != null) {
			long length = virtualBook.getLength();

			double timeToMM = virtualBook.getScale().timeToMM(length);

			timeToMM = timeToMM / 1000.0;

			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2); // la tu auras au plus 2 chiffres
			// apres la virgule
			nf.setMinimumFractionDigits(2); // maintenant tout tes nombres
			// auront 2 chiffres après la
			// virgule

			caption += nf.format(timeToMM) + " m"; //$NON-NLS-1$
		}

		if (virtualBook != null && instrument != null) {
			updateTitle();
		}

		lengthButton.setText(caption);

		preparePlayin(virtualBook);
	}

	/** @param virtualBook */
	protected void preparePlayin(VirtualBook virtualBook) {
		if (virtualBook == null) {
			return;
		}
		PlaySubSystem pss = playsubsystem.getCurrent();
		if (pss instanceof IPreparedCapableSubSystem) {
			try {
				aSyncPreparePlayin.signalVirtualBookChanged((IPreparedCapableSubSystem) pss, virtualBook);
			} catch (Exception ex) {
				logger.error("error in prepared play'in " + ex.getMessage(), ex); //$NON-NLS-1$
			}
		}
	}

	/** */
	protected void updateTitle() {

		String displayInstrumentName = (instrument == null ? Messages.getString("APrintNGVirtualBookInternalFrame.2024") //$NON-NLS-1$
				: instrument.getName());
		if (displayInstrumentName != null) {
			displayInstrumentName = displayInstrumentName.replace("\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}

		VirtualBook virtualBook = getVirtualBook();

		String vbName = (virtualBook == null ? Messages.getString("APrintNGVirtualBookInternalFrame.2025") //$NON-NLS-1$
				: (virtualBook.getName() != null ? virtualBook.getName()
						: Messages.getString("APrintNGVirtualBookInternalFrame.2025"))); //$NON-NLS-1$
		if (vbName != null) {
			vbName = vbName.replace("\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}

		String filePathSuffix = (currentSavedFile != null ? " - " //$NON-NLS-1$
				+ currentSavedFile.getName().toString() : ""); //$NON-NLS-1$

		setTitle(Messages.getString("APrintNGVirtualBookInternalFrame.1") //$NON-NLS-1$
				+ " " //$NON-NLS-1$
				+ vbName + (isDirty() ? "*" : "") + "," //$NON-NLS-1$
				+ " " //$NON-NLS-1$
				+ Messages.getString("APrintNGVirtualBookInternalFrame.2000") //$NON-NLS-1$
				+ displayInstrumentName + filePathSuffix + (isVirtualBookDirty() ? "*" : "")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void createLayerToolbarOptions(JToolBar pianorolltb, JPanel pianorollbutton) {

		final JButton lb = new JButton(Messages.getString("APrintNGVirtualBookInternalFrame.1002")); //$NON-NLS-1$

		class LayerVisibleAction implements ActionListener {

			JPopupMenu m = createMenuLayer();

			public LayerVisibleAction() {
			}

			public void actionPerformed(ActionEvent e) {

				logger.debug("isShowing ?" + m.isShowing()); //$NON-NLS-1$
				logger.debug("isVisible ?" + m.isVisible()); //$NON-NLS-1$

				m.show(lb, 0, lb.getHeight());
			}
		}

		lb.addActionListener(new LayerVisibleAction());
		lb.setIcon(new ImageIcon(getClass().getResource("tool_dock.png"))); //$NON-NLS-1$

		pianorolltb.add(lb);

		// call extension points
		VisibilityLayerButtonsExtensionPoint[] allVisibilityButtonsPoints = ExtensionPointProvider
				.getAllPoints(VisibilityLayerButtonsExtensionPoint.class, exts);
		for (int i = 0; i < allVisibilityButtonsPoints.length; i++) {
			VisibilityLayerButtonsExtensionPoint visibilityLayerButtons = allVisibilityButtonsPoints[i];
			try {
				visibilityLayerButtons.addVisibilityLayerButtons(pianorolltb);
			} catch (Throwable t) {
				logger.error(
						"error in adding visibility from extension :" + visibilityLayerButtons + " " + t.getMessage(), // $NON-NLS-1$
																														// //$NON-NLS-2$
						t);
			}
		}

		pianorollbutton.add(pianorolltb);
	}

	// /////////////////////////////////////////////////////////////////
	// Manage layer preferences

	private PrefixedNamePrefsStorage layerPreferences;

	private IPlaySubSystemManagerListener psslistener;

	private ASyncPreparePlayin aSyncPreparePlayin;

	private IScriptManagerListener scriptRefreeshListener;

	/**
	 * propery bean for virtual book properties
	 */
	private PropertySheetPanel bookPropertiesPropertySheetPanel;

	private JPanel pianorollbutton;

	/**
	 * Touch layer visibility from existing preferences
	 *
	 * @param c    component layer
	 * @param name
	 * 
	 */
	private void touchLayerVisibilityFromPreferences(VirtualBookComponentLayer c, String name) {
		if (c == null)
			return;

		String dn = name;

		if (c instanceof VirtualBookComponentLayerName) {
			VirtualBookComponentLayerName ln = (VirtualBookComponentLayerName) c;

			dn = ln.getDisplayName();

		} else {
			logger.debug("preferences for layer " + c + " cannot be applied"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (dn != null) {
			c.setVisible(layerPreferences.getBooleanProperty(dn, c.isVisible()));
		}
	}

	private void touchPreferencesVisibilityForLayer(VirtualBookComponentLayer c, String name) {
		if (c == null)
			return;
		String dn = name;
		if (c instanceof VirtualBookComponentLayerName) {
			VirtualBookComponentLayerName ln = (VirtualBookComponentLayerName) c;

			name = ln.getDisplayName();

		} else {
			logger.debug("preferences for layer " + c + " cannot be applied"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (dn != null) {
			layerPreferences.setBooleanProperty(dn, c.isVisible());
		}
	}

	private JPopupMenu createMenuLayer() {

		final JPopupMenu menuLayer = new JPopupMenu("Layers"); //$NON-NLS-1$

		showErrorsLayerCheckBox = new JCheckBox(Messages.getString("APrint.142"), il // $NON-NLS-1$
				.isVisible());
		showErrorsLayerCheckBox.setToolTipText(Messages.getString("APrint.143")); //$NON-NLS-1$

		touchLayerVisibilityFromPreferences(il, "issuelayer"); //$NON-NLS-1$

		showErrorsLayerCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox c = (JCheckBox) e.getSource();
				boolean checked = c.isSelected();
				il.setVisible(checked);
				touchPreferencesVisibilityForLayer(il, "issuelayer"); //$NON-NLS-1$
				repaint();
			}
		});

		touchLayerVisibilityFromPreferences(tbl, "timelayer"); //$NON-NLS-1$
		showTimeLayerCheckBox = new JCheckBox(Messages.getString("APrint.202"), //$NON-NLS-1$
				tbl.isVisible());
		showTimeLayerCheckBox.setToolTipText(Messages.getString(Messages.getString("APrint.201"))); //$NON-NLS-1$

		showTimeLayerCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox c = (JCheckBox) e.getSource();
				boolean checked = c.isSelected();
				tbl.setVisible(checked);
				touchPreferencesVisibilityForLayer(tbl, "timelayer"); //$NON-NLS-1$
				repaint();
			}
		});

		touchLayerVisibilityFromPreferences(psgl, "spg"); //$NON-NLS-1$

		JCheckBox showPipeStops = new JCheckBox(Messages.getString("APrint.298"), psgl.isVisible()); //$NON-NLS-1$
		showPipeStops.setToolTipText(Messages.getString("APrint.299")); //$NON-NLS-1$
		showPipeStops.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox c = (JCheckBox) e.getSource();
				boolean checked = c.isSelected();
				psgl.setVisible(checked);
				touchPreferencesVisibilityForLayer(psgl, "spg"); //$NON-NLS-1$
				repaint();
			}
		});

		touchLayerVisibilityFromPreferences(rsl, "rsl"); //$NON-NLS-1$
		JCheckBox showRegisters = new JCheckBox(Messages.getString("APrintNGVirtualBookInternalFrame.2004"), //$NON-NLS-1$
				rsl.isVisible());
		showRegisters.setToolTipText(Messages.getString("APrintNGVirtualBookInternalFrame.2005")); //$NON-NLS-1$
		showRegisters.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox c = (JCheckBox) e.getSource();
				boolean checked = c.isSelected();
				rsl.setVisible(checked);
				touchPreferencesVisibilityForLayer(rsl, "rsl"); //$NON-NLS-1$
				repaint();
			}
		});

		touchLayerVisibilityFromPreferences(vbml, "vbml"); //$NON-NLS-1$
		JCheckBox showMeasures = new JCheckBox(Messages.getString("APrintNGVirtualBookInternalFrame.110"), //$NON-NLS-1$
				vbml.isVisible());
		showMeasures.setToolTipText(Messages.getString("APrintNGVirtualBookInternalFrame.111")); //$NON-NLS-1$
		showMeasures.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox c = (JCheckBox) e.getSource();
				boolean checked = c.isSelected();
				vbml.setVisible(checked);
				touchPreferencesVisibilityForLayer(vbml, "vbml"); //$NON-NLS-1$
				repaint();
			}
		});

		touchLayerVisibilityFromPreferences(tcl, "tcl"); //$NON-NLS-1$
		JCheckBox showTempo = new JCheckBox(Messages.getString("APrintNGVirtualBookInternalFrame.112"), //$NON-NLS-1$
				tcl.isVisible());
		showTempo.setToolTipText(Messages.getString("APrintNGVirtualBookInternalFrame.113")); //$NON-NLS-1$
		showTempo.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox c = (JCheckBox) e.getSource();
				boolean checked = c.isSelected();
				tcl.setVisible(checked);
				touchPreferencesVisibilityForLayer(tcl, "tcl"); //$NON-NLS-1$
				repaint();
			}
		});

		touchLayerVisibilityFromPreferences(markerLayer, "markerlayer"); //$NON-NLS-1$
		JCheckBox showMarkerLayer = new JCheckBox(Messages.getString("APrintNGVirtualBookInternalFrame.3115"), //$NON-NLS-1$
				tcl.isVisible());
		showTempo.setToolTipText(Messages.getString("APrintNGVirtualBookInternalFrame.3116")); //$NON-NLS-1$
		showTempo.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox c = (JCheckBox) e.getSource();
				boolean checked = c.isSelected();
				markerLayer.setVisible(checked);
				touchPreferencesVisibilityForLayer(markerLayer, "markerlayer"); //$NON-NLS-1$
				repaint();
			}
		});

		menuLayer.add(showErrorsLayerCheckBox);
		menuLayer.add(showTimeLayerCheckBox);
		menuLayer.add(showPipeStops);
		menuLayer.add(showRegisters);
		menuLayer.add(showMeasures);
		menuLayer.add(showTempo);

		VirtualBookComponentLayer[] layers = pianoroll.getLayers();
		if (layers != null) {
			for (int i = 0; i < layers.length; i++) {
				final VirtualBookComponentLayer vbl = layers[i];
				if (vbl instanceof VirtualBookComponentLayerName) {
					VirtualBookComponentLayerName vbcln = (VirtualBookComponentLayerName) vbl;

					if (vbcln.getDisplayName() != null && !vbcln.getDisplayName().isEmpty()) {
						JCheckBox layerToggle = new JCheckBox(vbcln.getDisplayName(), vbl.isVisible());
						touchLayerVisibilityFromPreferences(vbl, null);

						layerToggle.addChangeListener(new ChangeListener() {
							public void stateChanged(ChangeEvent e) {
								JCheckBox c = (JCheckBox) e.getSource();
								boolean checked = c.isSelected();
								vbl.setVisible(checked);
								touchPreferencesVisibilityForLayer(vbl, null);

								repaint();
							}
						});

						menuLayer.add(layerToggle);
					}
				}
			}
		}

		return menuLayer;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		try {

			if ("EXIT".equals(e.getActionCommand())) { //$NON-NLS-1$
				dispose();
			} else if ("SAVE".equals(e.getActionCommand())) { //$NON-NLS-1$
				saveBook();
			} else if ("SAVEAS".equals(e.getActionCommand())) { //$NON-NLS-1$
				saveAsBook();
			} else if ("SAVEAS2010".equals(e.getActionCommand())) { //$NON-NLS-1$
				saveAsFormat2010();
			} else if ("IMPRIMER".equals(e.getActionCommand())) { //$NON-NLS-1$

				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					printCarton();
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}

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

				play();

			} else if ("STOP".equals(e.getActionCommand())) { //$NON-NLS-1$

				stop();

			} else if ("PREVIEW".equals(e.getActionCommand())) { //$NON-NLS-1$
				launchPrintPreview();
			}

		} catch (Exception ex) {
			logger.error("actionPerformed", ex); //$NON-NLS-1$

			BugReporter.sendBugReport();
			JMessageBox.showMessage(this, Messages.getString("APrint.42") + ex.getMessage()); //$NON-NLS-1$
		}
	}

	/** @throws Exception */
	public void stop() throws Exception {
		synchronized (this) {
			PlaySubSystem currentPlaySubSystem = playsubsystem.getCurrent();

			currentPlaySubSystem.stop();
		}
	}

	public void play() throws Exception {
		final PlaySubSystem currentPlaySubSystem = this.playsubsystem.getCurrent();

		if (currentPlaySubSystem.isPlaying()) {
			currentPlaySubSystem.stop();
		}

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		if (currentPlaySubSystem instanceof NeedInstrument) {
			NeedInstrument ni = (NeedInstrument) currentPlaySubSystem;
			ni.setCurrentInstrument(instrument);
		}

		if (currentPlaySubSystem instanceof NeedMidiListeningConverter
				&& ((NeedMidiListeningConverter) currentPlaySubSystem).isSupportMidiListeningConverter()) {

			InstrumentScript script = null;

			MIDIListeningConverter cconverter = null;

			Repository2 repository = services.getRepository();

			if (repository != null && repository instanceof Repository2Collection) {
				Repository2Collection rcol = (Repository2Collection) repository;
				Repository2 instrumentRepository = rcol.findRepositoryAssociatedTo(instrument);

				logger.debug("instrument repository :" + instrumentRepository); //$NON-NLS-1$

				if (instrumentRepository instanceof EditableInstrumentManagerRepository2Adapter) {
					EditableInstrumentManagerRepository2Adapter eir = (EditableInstrumentManagerRepository2Adapter) instrumentRepository;

					String eName = eir.findAssociatedEditableInstrument(instrument.getName());

					IEditableInstrument ei = eir.getEditableInstrumentManager().loadEditableInstrument(eName);

					logger.debug("editable instrument :" + ei); //$NON-NLS-1$

					InstrumentScript[] scripts = ei.findScriptsByType(InstrumentScriptType.MIDI_OUTPUT_SCRIPT);

					if (scripts.length > 0) {

						logger.debug("adding a default transform"); //$NON-NLS-1$

						Object[] choice = new Object[scripts.length + 1];
						choice[0] = Messages.getString("APrintNGVirtualBookInternalFrame.2104"); //$NON-NLS-1$
						for (int i = 0; i < scripts.length; i++) {
							choice[i + 1] = scripts[i];
						}

						Object sel = JOptionPane.showInputDialog((Component) this,
								Messages.getString("APrintNGVirtualBookInternalFrame.2007"), //$NON-NLS-1$
								Messages.getString("APrintNGVirtualBookInternalFrame.2008"), //$NON-NLS-1$
								JOptionPane.QUESTION_MESSAGE, (Icon) null, choice, (Object) null);

						if (sel != null && sel instanceof InstrumentScript)
							script = (InstrumentScript) sel;

						if (script != null) {
							// construct a specific transform from it groovy
							// script

							GroovyScriptVirtualBookToMidiConverter groovyScriptVirtualBookToMidiConverter = new GroovyScriptVirtualBookToMidiConverter(
									script);

							cconverter = groovyScriptVirtualBookToMidiConverter;

						} else {
							((NeedMidiListeningConverter) currentPlaySubSystem).setCurrentMidiListeningConverter(null);
						}
					}
				}
			}

			((NeedMidiListeningConverter) currentPlaySubSystem).setCurrentMidiListeningConverter(cconverter);
		}

		final long stop = pianoroll.getVirtualBook().getLength();

		// debut du jeu ...

		long startPlayPos = 0;

		if (pianoroll.hasHightlight()) {

			startPlayPos = pianoroll.MMToTime(pianoroll.getHightlight());
			// si on est à la fin, on redémarre

			if (startPlayPos - 10000 >= stop)
				startPlayPos = 0;
		}

		// start At ...

		APrintVirtualBookFramePlaySubSystemFeedback fb = new APrintVirtualBookFramePlaySubSystemFeedback(stop);

		try {

			if (currentPlaySubSystem instanceof IPreparedCapableSubSystem) {
				IPreparedCapableSubSystem pp = (IPreparedCapableSubSystem) currentPlaySubSystem;
				IPreparedPlaying p = aSyncPreparePlayin.getComputedPreparedPlayin(pp);
				if (p != null) {
					pp.playPrepared(APrintNGVirtualBookInternalFrame.this, p, fb, startPlayPos);
					return;
				}
			}

			// else
			PlayControl control = currentPlaySubSystem.play(APrintNGVirtualBookInternalFrame.this, getVirtualBook(), fb,
					startPlayPos);

			fb.setControl(control);

		} catch (Exception ex) {
			logger.error("jouer", ex); //$NON-NLS-1$
			JMessageBox.showMessage(this,
					Messages.getString("APrint.74") + ex.getMessage() + Messages.getString("APrint.75")); //$NON-NLS-2$
		}
	}

	private void saveBook() throws Exception {

		if (currentSavedFile == null) {
			saveAsBook();
			return;
		}

		saveBook(currentSavedFile);
	}

	private void saveAsFormat2010() throws Exception, FileNotFoundException {

		APrintFileChooser choose = new APrintFileChooser();

		choose.setFileFilter(new VFSFileNameExtensionFilter("Virtual book file 2010", //$NON-NLS-1$
				new String[] { APrintConstants.BOOK })); // $NON-NLS-1$

		choose.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

		if (currentSavedFile != null) {
			choose.setSelectedFile(currentSavedFile);
		}

		if (choose.showSaveDialog((Component) this) == APrintFileChooser.APPROVE_OPTION) {
			// Récupération du nom de fichier
			AbstractFileObject result = choose.getSelectedFile();
			String filename = result.getName().getBaseName();
			if (!filename.toLowerCase().endsWith("." + APrintConstants.BOOK)) //$NON-NLS-1$
				result = (AbstractFileObject) result.getFileSystem()
						.resolveFile(result.getName().toString() + "." + APrintConstants.BOOK); //$NON-NLS-1$

			OutputStream stream = VFSTools.transactionalWrite(result);
			VirtualBookXmlIO.write_2010(stream, getVirtualBook(), instrument.getName());

			logger.debug("virtual book 2010 written"); //$NON-NLS-1$

			currentSavedFile = result;

			JMessageBox.showMessage(this, Messages.getString("APrintNGVirtualBookInternalFrame.44") + result.getName() // $NON-NLS-1$
					+ Messages.getString("APrintNGVirtualBookInternalFrame.45")); //$NON-NLS-1$
		}
	}

	private void saveAsBook() throws Exception, FileNotFoundException {

		APrintFileChooser choose = new APrintFileChooser();

		choose.setFileFilter(new VFSFileNameExtensionFilter("Virtual book file", //$NON-NLS-1$
				new String[] { APrintConstants.BOOK })); // $NON-NLS-1$

		choose.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

		if (currentSavedFile != null) {
			choose.setSelectedFile(currentSavedFile);
		}

		if (choose.showSaveDialog((Component) this) == APrintFileChooser.APPROVE_OPTION) {
			// Récupération du nom de fichier
			AbstractFileObject result = choose.getSelectedFile();
			String filename = result.getName().getBaseName();
			if (!filename.toLowerCase().endsWith("." + APrintConstants.BOOK)) //$NON-NLS-1$
				result = (AbstractFileObject) result.getFileSystem()
						.resolveFile(result.getName().toString() + "." + APrintConstants.BOOK); //$NON-NLS-1$

			saveBook(result);
		}
	}

	private void saveBook(AbstractFileObject result) throws Exception {

		OutputStream outputStream = VFSTools.transactionalWrite(result);
		assert outputStream != null;
		try {
			// ensure properties are properly getted
			if (getVirtualBook().getMetadata() != null) {
				getVirtualBook().setName(getVirtualBook().getMetadata().getName());
			}

			VirtualBookXmlIO.write(outputStream, getVirtualBook(), instrument.getName());

			logger.debug("virtual book written"); //$NON-NLS-1$

			currentSavedFile = result;
			clearDirty();
			clearVirtualBookState();
			updateTitle();
		} finally {
			outputStream.close();
		}
	}

	/** Launch the print preview */
	private void launchPrintPreview() {

		// prévisualisation ...
		try {

			CartonVirtuelPrintDocument d = new CartonVirtuelPrintDocument(getVirtualBook(), 72);

			PrintPreview p = new PrintPreview(d, aprintpageformat.getLastPageFormat());

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}

	/** Run the print */
	private void printCarton() {

		// choix de l'imprimante, puis impression ...
		try {

			PrinterJob pjob = PrinterJob.getPrinterJob();

			PageFormat pageformat = aprintpageformat.getLastPageFormat();
			if (pageformat == null) {
				pageformat = pjob.defaultPage();
			}

			PageFormat newpageformat = pjob.pageDialog(pageformat);

			// pb avec les 300 dpi, si l'on met 300 dpi, ca devrai passer avec
			// les imprimantes ... non actuellement
			// testé, mais en PDF, c'est pas bon !!

			CartonVirtuelPrintDocument d = new CartonVirtuelPrintDocument(getVirtualBook(), 72);

			// if (newpageformat != pageformat) {
			pjob.setPrintable(d, newpageformat);

			aprintpageformat.setLastPageFormat(newpageformat);

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

			// } // sinon l'utilisateur a click� sur annuler ...

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}

	// ///////////////////////////////////////////////////////////
	// Gestion des actions de l'interface ...

	private void executeScript(final InstrumentScript script, final Map<String, Object> properties,
			final JobConsoleEvent jobEvent) throws Exception {

		// run it !!

		logger.debug("run the script :" + script.getName()); //$NON-NLS-1$

		JDialog groovyFrame = new JDialog((Frame) this, Messages.getString("APrintNGVirtualBookInternalFrame.17") //$NON-NLS-1$
				+ pianoroll.getVirtualBook().getName());
		groovyFrame.setModal(false);

		SwingUtils.center(groovyFrame);

		associatedFrames.add(groovyFrame);

		final APrintGroovyConsolePanel p = new APrintGroovyConsolePanel();

		try {

			for (Iterator iterator = properties.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, Object> e = (Entry<String, Object>) iterator.next();

				p.appendOutputNl(">>  " + e.getKey(), null); //$NON-NLS-1$
			}

		} catch (Exception ex) {
			logger.error("fail to output variables in console", ex); //$NON-NLS-1$
		}

		// loading a panel, setting the properties
		// run script and ask for closing the script console ...

		groovyFrame.getContentPane().setLayout(new BorderLayout());
		groovyFrame.getContentPane().add(p, BorderLayout.CENTER);

		groovyFrame.setPreferredSize(new Dimension(800, 600));
		groovyFrame.pack();

		groovyFrame.setVisible(true);

		groovyFrame.paintAll(groovyFrame.getGraphics());

		// for displaying the console ...
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				Binding b = p.getCurrentBindingRef();

				for (Iterator iterator = properties.entrySet().iterator(); iterator.hasNext();) {
					Entry<String, Object> e = (Entry<String, Object>) iterator.next();
					b.setProperty(e.getKey(), e.getValue());
				}

				p.clearConsole();

				try {

					final StringBuffer scLoading = new StringBuffer();

					p.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					try {

						scLoading.append(script.getContent());

						p.setScriptPanelEnabled(false);

					} finally {
						p.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}

					p.setScriptContent(scLoading.toString());
					p.paintAll(p.getGraphics());

					logger.debug("make the undo operation for the script"); //$NON-NLS-1$
					GlobalVirtualBookUndoOperation gvb = new GlobalVirtualBookUndoOperation(pianoroll.getVirtualBook(),
							Messages.getString("APrintNGVirtualBookInternalFrame.31"), pianoroll); // $NON-NLS-1$

					pianoroll.getUndoStack().push(gvb);

					Future f = p.run();

					JobEvent je = new JobEvent() {
						public void jobAborted() {
							try {
								jobEvent.jobAborted(p);
							} catch (Throwable t) {
								logger.error(t.getMessage(), t);
							}
						}

						public void jobError(Throwable t) {
							try {

								jobEvent.jobError(p, t);

							} catch (Exception x) {
								logger.debug(x.getMessage(), x);
							}
						}

						public void jobFinished(Object result) {
							try {

								jobEvent.jobFinished(p, result);

							} catch (Exception ex) {
								logger.error("error in executing script :" //$NON-NLS-1$
										+ ex.getMessage(), ex);
							}
						}
					};

					asyncJobsManager.submitAlreadyExecutedJobToTrack(f, je);

				} catch (Throwable t) {
					logger.error("error while executing script :" //$NON-NLS-1$
							+ t.getMessage(), t);
					try {
						p.appendOutput(t);
					} catch (Exception ex) {
						logger.error("exception in output " + ex.getMessage(), //$NON-NLS-1$
								ex);
					}
				}
			}
		});
	}

	private void saveAsMidiFile() throws Exception {

		logger.debug("saveAsMidiFile"); //$NON-NLS-1$

		// check if there are some exporter on the intruments

		InstrumentScript script = null;

		Repository2 repository = services.getRepository();

		if (repository != null && repository instanceof Repository2Collection) {
			Repository2Collection rcol = (Repository2Collection) repository;
			Repository2 instrumentRepository = rcol.findRepositoryAssociatedTo(instrument);

			logger.debug("instrument repository :" + instrumentRepository); //$NON-NLS-1$

			if (instrumentRepository instanceof EditableInstrumentManagerRepository2Adapter) {
				EditableInstrumentManagerRepository2Adapter eir = (EditableInstrumentManagerRepository2Adapter) instrumentRepository;

				String eName = eir.findAssociatedEditableInstrument(instrument.getName());

				IEditableInstrument ei = eir.getEditableInstrumentManager().loadEditableInstrument(eName);

				logger.debug("editable instrument :" + ei); //$NON-NLS-1$

				InstrumentScript[] scripts = ei.findScriptsByType(InstrumentScriptType.MIDI_OUTPUT_SCRIPT);

				if (scripts.length > 0) {

					logger.debug("adding a default transform"); //$NON-NLS-1$

					Object[] choice = new Object[scripts.length + 1];
					choice[0] = Messages.getString("APrintNGVirtualBookInternalFrame.2107"); //$NON-NLS-1$
					for (int i = 0; i < scripts.length; i++) {
						choice[i + 1] = scripts[i];
					}

					Object sel = JOptionPane.showInputDialog((Component) this,
							Messages.getString("APrintNGVirtualBookInternalFrame.2007"), //$NON-NLS-1$
							Messages.getString("APrintNGVirtualBookInternalFrame.2008"), JOptionPane.QUESTION_MESSAGE, // $NON-NLS-1$
							(Icon) null, choice, (Object) null);

					script = (InstrumentScript) sel;

					if (script == null)
						return;

					if (script instanceof InstrumentScript) {

						HashMap<String, Object> m = new HashMap<String, Object>();
						m.put("virtualbook", pianoroll.getVirtualBook()); //$NON-NLS-1$

						executeScript(script, m, new JobConsoleEvent() {

							public void jobAborted(APrintGroovyConsolePanel p) throws Exception {
								p.appendOutput("Aborted", null); //$NON-NLS-1$
							}

							public void jobError(APrintGroovyConsolePanel p, Throwable t) throws Exception {
								p.appendOutput(t);
							}

							public void jobFinished(APrintGroovyConsolePanel p, Object result) throws Exception {

								if (result != null && result instanceof MidiFile) {
									// demande du fichier à sauvegarder ...
									APrintFileChooser choose = new APrintFileChooser();

									choose.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

									choose.setFileFilter(new VFSFileNameExtensionFilter("Fichier Midi", "mid")); //$NON-NLS-1$ //$NON-NLS-2$

									if (choose.showSaveDialog(
											APrintNGVirtualBookInternalFrame.this) == APrintFileChooser.APPROVE_OPTION) {

										AbstractFileObject selfile = choose.getSelectedFile();

										String filename = selfile.getName().getBaseName();

										if (!filename.toLowerCase().endsWith(".mid")) { //$NON-NLS-1$
											selfile = (AbstractFileObject) selfile.getFileSystem()
													.resolveFile(selfile.getName().toString() + ".mid"); //$NON-NLS-1$
										}

										OutputStream midi0outputStream = VFSTools.transactionalWrite(selfile);
										try {
											MidiFileIO.write_midi_0((MidiFile) result, midi0outputStream);
										} finally {
											midi0outputStream.close();
										}
										JMessageBox.showMessage(this, "" //$NON-NLS-1$
												+ choose.getSelectedFile() + ""); //$NON-NLS-1$
									}

								} else {
									logger.debug("script result is not a midifile object ..."); //$NON-NLS-1$
								}
							}
						});

						return;
					}
				}
			}
		}

		logger.debug("default save ..."); //$NON-NLS-1$

		// demande du fichier à sauvegarder ...
		APrintFileChooser choose = new APrintFileChooser();

		choose.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

		choose.setFileFilter(new VFSFileNameExtensionFilter("Fichier Midi", "mid")); //$NON-NLS-1$ //$NON-NLS-2$

		if (choose.showSaveDialog(APrintNGVirtualBookInternalFrame.this) == APrintFileChooser.APPROVE_OPTION) {

			// lancement script ...

			Sequence seq = EcouteConverter.convert(getVirtualBook(), 120);

			AbstractFileObject selfile = choose.getSelectedFile();
			String filename = selfile.getName().getBaseName();
			if (!filename.toLowerCase().endsWith(".mid")) { //$NON-NLS-1$
				selfile = (AbstractFileObject) selfile.getFileSystem()
						.resolveFile(selfile.getName().toString() + ".mid"); //$NON-NLS-1$
			}

			OutputStream midioutputStream = VFSTools.transactionalWrite(selfile);
			try {
				MidiFileIO.writeMidi(seq, 0, midioutputStream);
			} finally {
				midioutputStream.close();
			}
			JMessageBox.showMessage(this, "" //$NON-NLS-1$
					+ choose.getSelectedFile() + ""); //$NON-NLS-1$
		}
	}

	private void saveWAV() throws InvalidMidiDataException, IOException, Exception {

		// demande du fichier à sauvegarder ...
		APrintFileChooser choose = new APrintFileChooser();

		choose.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

		choose.setFileFilter(new VFSFileNameExtensionFilter(Messages.getString("APrint.123"), //$NON-NLS-1$
				"wav")); //$NON-NLS-1$

		if (choose.showSaveDialog(this) == APrintFileChooser.APPROVE_OPTION) {

			waitininterface.infiniteStartWait(""); //$NON-NLS-1$

			// Chargement de l'instrument ...

			Soundbank sb = null;

			if (instrument != null) {
				// play with custom sound ...

				org.barrelorgandiscovery.instrument.Instrument ins = instrument;

				sb = ins.openSoundBank();
				if (sb == null) {
					throw new Exception("Fail to load instrument"); //$NON-NLS-1$
				}
			}

			AbstractFileObject choosenfile = choose.getSelectedFile();
			String choosenFileFileName = choosenfile.getName().getBaseName();
			if (!choosenFileFileName.toLowerCase().endsWith(".wav")) { //$NON-NLS-1$

				choosenfile = (AbstractFileObject) choosenfile.getFileSystem()
						.resolveFile(choosenfile.getName().toString() + ".wav"); //$NON-NLS-1$
			}

			final AbstractFileObject finalChoosenFile = choosenfile;
			final Soundbank sbfinal = sb;

			asyncJobsManager.submitAndExecuteJob(new Callable<Void>() {
				public Void call() throws Exception {

					VirtualBookToMidiConverter converter = new VirtualBookToMidiConverter(instrument);
					final Sequence seq = converter.convert(getVirtualBook());

					OutputStream os = VFSTools.transactionalWrite(finalChoosenFile);
					try {
						SequencerTools.render(sbfinal, seq, os, false);
					} finally {
						os.close();
					}
					waitininterface.infiniteChangeText(Messages.getString("APrint.231")); //$NON-NLS-1$

					return null;
				}
			}, new JobEvent() {

				public void jobFinished(Object result) {
					waitininterface.infiniteEndWait();

					JMessageBox.showMessage(getOwnerForDialog(), Messages.getString("APrint.125") //$NON-NLS-1$
							+ " " //$NON-NLS-1$
							+ finalChoosenFile + " " //$NON-NLS-1$
							+ Messages.getString("APrint.126")); //$NON-NLS-1$
				}

				public void jobError(Throwable ex) {
					waitininterface.infiniteEndWait();
					logger.error("export as wav", ex); //$NON-NLS-1$
					JMessageBox.showError(getOwnerForDialog(), ex);

					BugReporter.sendBugReport();
				}

				public void jobAborted() {
					waitininterface.infiniteEndWait();
				}
			});
		}
	}

	private void savetoMOV() throws Exception {

		// demande du fichier � sauvegarder ...
		APrintFileChooser choose = new APrintFileChooser();

		choose.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

		choose.setFileFilter(new VFSFileNameExtensionFilter(Messages.getString("APrintNGVirtualBookInternalFrame.310"), //$NON-NLS-1$
				"mov")); //$NON-NLS-1$

		if (choose.showSaveDialog(this) == APrintFileChooser.APPROVE_OPTION) {

			AbstractFileObject choosenfile = choose.getSelectedFile();
			String filename = choosenfile.getName().getBaseName();
			if (!filename.toLowerCase().endsWith(".mov")) { //$NON-NLS-1$
				choosenfile = (AbstractFileObject) choosenfile.getFileSystem()
						.resolveFile(choosenfile.getName().toString() + ".mov"); //$NON-NLS-1$
			}

			// ask for parameters ...

			BeanAsk b = new BeanAsk((Frame) this);
			final MovieConverterParameters params = (MovieConverterParameters) b.askForParameters((Frame) this,
					Messages.getString("APrintNGVirtualBookInternalFrame.1006"), //$NON-NLS-1$
					new MovieConverterParameters());

			if (params == null)
				return;

			// Chargement de l'instrument ...

			Soundbank sb = null;

			if (instrument != null) {
				// play with custom sound ...

				org.barrelorgandiscovery.instrument.Instrument ins = instrument;

				sb = ins.openSoundBank();
				if (sb == null) {
					throw new Exception("Fail to load instrument"); //$NON-NLS-1$
				}
			}

			final File finalwrittenFile = VFSTools.convertToFile(choosenfile);

			final ProgressIndicator p = new ProgressIndicator() {
				public void progress(double progress, String message) {

					waitininterface.infiniteChangeText("" + ((int) (progress * 100)) + " % " //$NON-NLS-2$
							+ (message == null ? "" : " -  " + message)); //$NON-NLS-1$ //$NON-NLS-2$
				}
			};

			final CancelTracker ct = new CancelTracker();

			waitininterface.infiniteStartWait(Messages.getString("APrint.315"), ct); //$NON-NLS-1$

			asyncJobsManager.submitAndExecuteJob(new Callable<Void>() {
				public Void call() throws Exception {

					MovieConverter.convertToMovie(getVirtualBook(), instrument, finalwrittenFile, p, ct, params);

					return null;
				}
			}, new JobEvent() {

				public void jobFinished(Object result) {
					waitininterface.infiniteEndWait();
					JMessageBox.showMessage(getOwnerForDialog(), "File " + " " //$NON-NLS-2$
							+ finalwrittenFile + " " //$NON-NLS-1$
							+ "successfully exported"); //$NON-NLS-1$
				}

				public void jobError(Throwable ex) {
					waitininterface.infiniteEndWait();

					logger.error("Error while exporting to movie :" //$NON-NLS-1$
							+ ex.getMessage(), ex);
					JMessageBox.showMessage(getOwnerForDialog(), Messages.getString("APrint.6") //$NON-NLS-1$
							+ ex.getMessage());
				}

				public void jobAborted() {
					waitininterface.infiniteEndWait();
				}
			});
		}
	}

	private void savetoMP3() throws InvalidMidiDataException, IOException, Exception {
		// demande du fichier � sauvegarder ...
		APrintFileChooser choose = new APrintFileChooser();

		choose.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

		choose.setFileFilter(new VFSFileNameExtensionFilter("Fichier MP3", "mp3")); //$NON-NLS-1$ //$NON-NLS-2$

		if (choose.showSaveDialog(this) == APrintFileChooser.APPROVE_OPTION) {

			Sequence seq = EcouteConverter.convert(getVirtualBook());

			// Chargement de l'instrument ...

			Soundbank sb = null;

			if (instrument != null) {
				// play with custom sound ...

				org.barrelorgandiscovery.instrument.Instrument ins = instrument;

				sb = ins.openSoundBank();
				if (sb == null) {
					throw new Exception("Fail to load instrument"); //$NON-NLS-1$
				}
			}

			AbstractFileObject choosenfile = choose.getSelectedFile();

			String filename = choosenfile.getName().getBaseName();

			if (!filename.toLowerCase().endsWith(".mp3")) { //$NON-NLS-1$
				choosenfile = (AbstractFileObject) choosenfile.getFileSystem()
						.resolveFile(choosenfile.getName().toString() + ".mp3"); //$NON-NLS-1$
			}

			File tmpfile = File.createTempFile("temp", "wav"); //$NON-NLS-1$ //$NON-NLS-2$

			SequencerTools.render(sb, seq, tmpfile, true);

			MP3Tools.convert(tmpfile.getAbsolutePath(), choosenfile.getName().toString());

			JMessageBox.showMessage(this, Messages.getString("APrint.125") //$NON-NLS-1$
					+ " " //$NON-NLS-1$
					+ choosenfile + " " //$NON-NLS-1$
					+ Messages.getString("APrint.126")); //$NON-NLS-1$
		}
	}

	private void savetoOGG() throws InvalidMidiDataException, IOException, Exception {
		// demande du fichier � sauvegarder ...
		APrintFileChooser choose = new APrintFileChooser();

		choose.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

		choose.setFileFilter(new VFSFileNameExtensionFilter("Fichier OGG", "ogg")); //$NON-NLS-1$ //$NON-NLS-2$

		if (choose.showSaveDialog(this) == APrintFileChooser.APPROVE_OPTION) {

			// Chargement de l'instrument ...

			Soundbank sb = null;

			if (instrument != null) {
				// play with custom sound ...

				org.barrelorgandiscovery.instrument.Instrument ins = instrument;

				sb = ins.openSoundBank();
				if (sb == null) {
					throw new Exception("Fail to load instrument"); //$NON-NLS-1$
				}
			}

			AbstractFileObject choosenfile = choose.getSelectedFile();
			String filename = choosenfile.getName().getBaseName();
			if (!filename.toLowerCase().endsWith(".ogg")) { //$NON-NLS-1$
				choosenfile = (AbstractFileObject) choosenfile.getFileSystem()
						.resolveFile(choosenfile.getName().toString() + ".ogg"); //$NON-NLS-1$
			}

			final AbstractFileObject writtenfile = choosenfile;
			final File tmpfile = File.createTempFile("temp", ".wav"); //$NON-NLS-1$ //$NON-NLS-2$
			tmpfile.deleteOnExit();

			final Soundbank sbfinal = sb;

			waitininterface.infiniteStartWait(Messages.getString("APrint.230")); //$NON-NLS-1$

			asyncJobsManager.submitAndExecuteJob(new Callable<Void>() {
				public Void call() throws Exception {

					VirtualBookToMidiConverter converter = new VirtualBookToMidiConverter(instrument);
					final Sequence seq = converter.convert(getVirtualBook());

					SequencerTools.render(sbfinal, seq, tmpfile, false);

					waitininterface.infiniteChangeText(Messages.getString("APrint.231")); //$NON-NLS-1$

					OGGTools.convert(tmpfile, VFSTools.convertToFile(writtenfile));

					return null;
				}
			}, new JobEvent() {

				public void jobFinished(Object result) {
					waitininterface.infiniteEndWait();

					JMessageBox.showMessage(getOwnerForDialog(), Messages.getString("APrint.125") //$NON-NLS-1$
							+ " " //$NON-NLS-1$
							+ writtenfile + " " //$NON-NLS-1$
							+ Messages.getString("APrint.126")); //$NON-NLS-1$
				}

				public void jobError(Throwable ex) {
					waitininterface.infiniteEndWait();
					logger.error("export as ogg", ex); //$NON-NLS-1$
					JMessageBox.showError(this, ex);

					BugReporter.sendBugReport();
				}

				public void jobAborted() {
					waitininterface.infiniteEndWait();
				}
			});
		}
	}

	/** Verifie l'�tat de l'interface en fonction de l'�tat de l'IHM */
	private void checkState() throws Exception {

		// le carton a �t� transpos� / transform�

		// on regarde si on est en train de jouer
		// le morceau ....

		PlaySubSystem currentPlaySubSystem = this.playsubsystem.getCurrent();

		if (currentPlaySubSystem.isPlaying()) {
			// on d�sactive le bouton imprimer
			rewind.setEnabled(false);
			imprimer.setEnabled(false);
			preview.setEnabled(false);
			// tracer.setEnabled(false);
			exportAsWav.setEnabled(false);
			exportAsMidi.setEnabled(false);
			exportAsOgg.setEnabled(false);
			exportAsMovie.setEnabled(false);

		} else {
			// sinon on active tout ...

			SwingUtils.recurseSetEnable(buttonActionToolbar, true);
			SwingUtils.recurseSetEnable(exportToolbar, true);
		}

		repaint();
	}

	private void recurseDisposeComponent(Component component) {
		try {
			if (component == null)
				return;

			if (component != null && component instanceof Disposable) {
				logger.debug("disposing component " + component); //$NON-NLS-1$
				((Disposable) component).dispose();
			}
			if (component instanceof Container) {
				Container c = (Container) component;
				for (Component comp : c.getComponents()) {
					recurseDisposeComponent(comp);
				}
			}

		} catch (Exception ex) {
			logger.debug("error disposing component :" + component, ex); //$NON-NLS-1$
		}
	}

	@Override
	public void dispose() {

		logger.debug("dispose virtual book frame"); //$NON-NLS-1$
		clearVirtualBookState(); // mark the virtual book as not dirty
		// to be able to close properly the window

		// save toolbar
		saveToolbarPreferences();

		if (this.associatedFrames != null) {
			for (JDialog d : associatedFrames) {
				try {
					logger.debug("dispose dialog " + d); //$NON-NLS-1$
					d.dispose();
				} catch (Exception ex) {
					logger.debug("error in disposing the dialog " + d); //$NON-NLS-1$
				}
			}
			this.associatedFrames.clear();
			this.associatedFrames = null;
		}

		recurseDisposeComponent(this.getContentPane());

		Container c = toolWindowManager.getMainContainer();

		recurseDisposeComponent(c);

		logger.debug("dispose the play subsystem"); //$NON-NLS-1$
		if (playsubsystem != null) {
			PlaySubSystem current = playsubsystem.getCurrent();
			if (current != null) {
				// stop the play
				if (current.getOwner() == this) {
					try {
						current.stop();
					} catch (Exception ex) {
						logger.warn("exception in stopping the play ..." //$NON-NLS-1$
								+ ex.getMessage(), ex);
					}
				}
			}
		}
		// stop the listening
		if (psslistener != null && playsubsystem != null) {
			logger.debug("unregister the events"); //$NON-NLS-1$
			playsubsystem.removePlaySubSystemManagerListener(psslistener);
		}

		if (scriptManager != null) {
			scriptManager.removeScriptManagerListener(scriptRefreeshListener);
		}

		scriptManager = null;

		aSyncPreparePlayin.dispose();

		playsubsystem = null;

		// dispose extensions

		if (this.exts != null) {

			for (IExtension ext : exts) {
				try {
					if (ext instanceof Disposable) {
						logger.debug("dispose extension");
						((Disposable) ext).dispose();
						logger.debug("done");
					}
				} catch (Throwable t) {
					logger.error("error while disposing " + ext);
				}
			}
			this.exts = null;
		}

		super.dispose();
	}

	private void saveToolbarPreferences() {
		// save the tool windows prefs
		try {
			PersistenceDelegate pdelegate = toolWindowManager.getPersistenceDelegate();

			File finalToolPerfsFile = constructToolWindowFile();

			File backupToolsPerfsFile = new File(finalToolPerfsFile.getParent(), finalToolPerfsFile.getName() + "_tmp"); //$NON-NLS-1$

			File aleatmpf = new File(finalToolPerfsFile.getParentFile(),
					finalToolPerfsFile.getName() + "" + Math.random()); //$NON-NLS-1$

			// save in tmpf
			FileOutputStream fos = new FileOutputStream(aleatmpf);
			try {
				pdelegate.save(fos);
			} finally {
				fos.close();
			}

			if (backupToolsPerfsFile.exists())
				backupToolsPerfsFile.delete();

			if (finalToolPerfsFile.exists()) {

				if (finalToolPerfsFile.renameTo(backupToolsPerfsFile)) {
					aleatmpf.renameTo(finalToolPerfsFile);
					backupToolsPerfsFile.delete();
				}
				// final file can't be renamed

				// don't save
			} else {
				// ok this work

				if (!aleatmpf.renameTo(finalToolPerfsFile)) {
					// reverse back
					backupToolsPerfsFile.renameTo(finalToolPerfsFile);
				} else {
					// this work
					// remove the files
					backupToolsPerfsFile.delete();
				}
			}

		} catch (Exception ex) {
			logger.info("fail to save tool window preferences :" + ex.getMessage(), ex); //$NON-NLS-1$
		}
	}

	private void showScriptConsoleAssociatedWithVirtualBook() {

		JDialog groovyFrame = new VirtualBookScriptConsole((Frame) this,
				Messages.getString("APrintNGVirtualBookInternalFrame.17") //$NON-NLS-1$
						+ pianoroll.getVirtualBook().getName(),
				pianoroll, toolbarPanel, asyncJobsManager, services, instrument, aprintproperties, scriptManager);

		groovyFrame.setModal(false);

		SwingUtils.center(groovyFrame);

		associatedFrames.add(groovyFrame);

		groovyFrame.setVisible(true);
	}

	private class APrintVirtualBookFramePlaySubSystemFeedback implements IPlaySubSystemFeedBack {

		private final long stop;

		long lastpos = -1;
		long nanoDiplayTime = 0;

		private APrintVirtualBookFramePlaySubSystemFeedback(long stop) {
			this.stop = stop;
		}

		private PlayControl control = null;

		public void setControl(PlayControl control) {
			this.control = control;
		}

		public void playStarted() {

			setCursor(Cursor.getDefaultCursor());

			logger.debug("play started"); //$NON-NLS-1$

			float v = crossThreadTempoSlider;
			if (control != null) {
				control.setTempo(v);
			}

			jouer.setText(Messages.getString("APrint.28")); //$NON-NLS-1$
			jouer.setActionCommand("STOP"); //$NON-NLS-1$
			jouer.setIcon(new ImageIcon(getClass().getResource("noatunstop.png"))); //$NON-NLS-1$
			try {
				checkState();
			} catch (Throwable t) {
				logger.error("error in start playing :" //$NON-NLS-1$
						+ t.getMessage(), t);
			}
			// pianoroll.setUseFastDrawing(true);

		}

		public void playStopped() {

			setCursor(Cursor.getDefaultCursor());
			try {

				logger.debug("play stopped"); //$NON-NLS-1$
				Runnable runnable = new Runnable() {
					public void run() {

						// pianoroll.setUseFastDrawing(false);

						jouer.setText(Messages.getString("APrint.31")); //$NON-NLS-1$
						jouer.setActionCommand("JOUER"); //$NON-NLS-1$
						jouer.setIcon(new ImageIcon(getClass().getResource("noatunplay.png"), //$NON-NLS-1$
								Messages.getString("APrint.80"))); //$NON-NLS-1$

						try {
							checkState();
						} catch (Throwable t) {
							logger.error("error in stopping playing :" //$NON-NLS-1$
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

		public long informCurrentPlayPosition(final long pos) {

			try {

				float v = crossThreadTempoSlider;
				if (control != null) {
					control.setTempo(v);
				}

				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						// not executed in swing thread in the call
						// ...

						double posx = pianoroll.getVirtualBook().getScale().getSpeed() * (((double) pos) / 1000000);

						if (pos != lastpos) {

							// change the selection :-)
							// query the holes under the hightlight

							ArrayList<Hole> s = pianoroll.getVirtualBook().findHoles(pos, 0);

							pianoroll.clearSelection();
							for (Iterator<Hole> iterator = s.iterator(); iterator.hasNext();) {
								Hole hole = iterator.next();
								pianoroll.addToSelection(hole);
							}

							lastpos = pos;
						}

						Dimension d = pianoroll.getSize();

						// highlight invoke repaint()
						pianoroll.setHightlight(posx);
						// pour le d�placement du carton dans
						// l'interface
						// inderlying repaint ..
						pianoroll.setXoffset(posx - pianoroll.pixelToMM(d.width / 2));

//                if (pos >= stop - 10000)
//                  SwingUtilities.invokeLater(
//                      new Runnable() {
//                        public void run() {
//                          actionPerformed(new ActionEvent(this, 0, "STOP")); //$NON-NLS-1$
//                        }
//                      });

						nanoDiplayTime = pianoroll.getDisplayNanos();
					}
				});

			} catch (Exception ex) {
				logger.error("error in playstopped :" //$NON-NLS-1$
						+ ex.getMessage(), ex);
			}

			return nanoDiplayTime;
		}
	}

	/**
	 * quick script element
	 * 
	 * @author pfreydiere
	 *
	 */
	public static class QuickScriptElement {
		private File f;

		public QuickScriptElement(File f) {
			this.f = f;
		}

		@Override
		public String toString() {
			return f.getName();
		}
	}

	/**
	 * Create the quick script fast access combo box
	 *
	 * @param folder
	 * @return
	 * @throws Exception
	 */
	private JComboBox createQuickScriptAccess() throws Exception {

		final JComboBox qscriptExecuteCombo = new JComboBox();

		reloadScriptList(qscriptExecuteCombo);

		qscriptExecuteCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {

				try {

					Object item = e.getItem();
					if (item != null && e.getStateChange() == ItemEvent.SELECTED
							&& ((String) item) != QUICK_SCRIPT_EXECUTE) {

						final String scriptToRun = (String) item;

						qscriptExecuteCombo.setSelectedIndex(0);

						// run it !!

						logger.debug(Messages.getString("APrintNGVirtualBookInternalFrame.2009") + scriptToRun); // $NON-NLS-1$

						final JDialog groovyFrame = new JDialog((Frame) APrintNGVirtualBookInternalFrame.this,
								Messages.getString("APrintNGVirtualBookInternalFrame.17") //$NON-NLS-1$
										+ pianoroll.getVirtualBook().getName());
						groovyFrame.setModal(false);

						SwingUtils.center(groovyFrame);

						associatedFrames.add(groovyFrame);

						final APrintGroovyConsolePanel p = new APrintGroovyConsolePanel();

						try {
							p.appendOutputNl(">>  " + "virtualbook" + " " //$NON-NLS-2$ //$NON-NLS-3$
									+ Messages.getString("APrintNGVirtualBookInternalFrame.21"), null); // $NON-NLS-1$
							p.appendOutputNl(">>  " //$NON-NLS-1$
									+ "pianoroll" //$NON-NLS-1$
									+ " " //$NON-NLS-1$
									+ Messages.getString("APrintNGVirtualBookInternalFrame.25"), //$NON-NLS-1$
									null);
							p.appendOutputNl(">>  " + "currentinstrument" + " " //$NON-NLS-3$ //$NON-NLS-2$
																				// //$NON-NLS-3$
									+ " instrument associated to the book view", null); // $NON-NLS-1$
							p.appendOutputNl(">>  " + "toolbarspanel" + " " //$NON-NLS-3$ //$NON-NLS-2$
							// //$NON-NLS-3$
									+ " toolbar panel", null); // $NON-NLS-1$

						} catch (Exception ex) {
							logger.error("fail to output variables in console", ex); //$NON-NLS-1$
						}

						// loading a panel, setting the properties
						// run script and ask for closing the script console ...

						groovyFrame.getContentPane().setLayout(new BorderLayout());
						groovyFrame.getContentPane().add(p, BorderLayout.CENTER);

						groovyFrame.setPreferredSize(new Dimension(800, 600));
						groovyFrame.pack();

						groovyFrame.setVisible(true);

						groovyFrame.paintAll(groovyFrame.getGraphics());

						// for displaying the console ...
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {

								Binding b = p.getCurrentBindingRef();
								b.setProperty("virtualbook", pianoroll // $NON-NLS-1$
										.getVirtualBook());
								b.setProperty("pianoroll", pianoroll); //$NON-NLS-1$
								b.setProperty("services", services); //$NON-NLS-1$
								b.setProperty("currentinstrument", instrument); //$NON-NLS-1$
								// toolbars panel, permit to add new JToolbars
								b.setProperty("toolbarspanel", pianorollbutton); //$NON-NLS-1$
								p.clearConsole();

								try {

									final StringBuffer scLoading = new StringBuffer();

									p.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
									try {

										scLoading.append(scriptManager.loadScript(scriptToRun));

										p.setScriptPanelEnabled(false);

									} finally {
										p.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
									}

									p.setScriptContent(scLoading.toString());
									p.paintAll(p.getGraphics());

									logger.debug("make the undo operation for the script"); //$NON-NLS-1$
									GlobalVirtualBookUndoOperation gvb = new GlobalVirtualBookUndoOperation(
											pianoroll.getVirtualBook(),
											Messages.getString("APrintNGVirtualBookInternalFrame.31"), pianoroll); // $NON-NLS-1$

									pianoroll.getUndoStack().push(gvb);

									Future f = p.run();

									asyncJobsManager.submitAlreadyExecutedJobToTrack(f, new JobEvent() {
										public void jobAborted() {
											groovyFrame.setVisible(false);
											groovyFrame.dispose();
										}

										public void jobError(Throwable t) {
											try {

												logger.error("error while executing script :" //$NON-NLS-1$
														+ t.getMessage(), t);
												p.appendOutput(t);

												// keep the dialog opened
											} catch (Exception x) {
												logger.debug(x);
											}
										}

										public void jobFinished(Object result) {
											try {
												p.appendOutputNl(result == null ? "null" //$NON-NLS-1$
														: result.toString(), null);

												pianoroll.touchBook();
												pianoroll.repaint();

												groovyFrame.setVisible(false);
												groovyFrame.dispose();

											} catch (Exception ex) {
												logger.error("error in executing script :" //$NON-NLS-1$
														+ ex.getMessage(), ex);
											}
										}
									});

								} catch (Throwable t) {
									logger.error("error while executing script :" //$NON-NLS-1$
											+ t.getMessage(), t);
									try {
										p.appendOutput(t);
									} catch (Exception ex) {
										logger.error("exception in output " //$NON-NLS-1$
												+ ex.getMessage(), ex);
									}
								}
							}
						});
					} // if

				} catch (Exception ex) {
					logger.error("error in executing script : " //$NON-NLS-1$
							+ ex.getMessage(), ex);
					JMessageBox.showMessage(this,
							Messages.getString("APrintNGVirtualBookInternalFrame.2010") + ex.getMessage()); // $NON-NLS-1$
				}
			}
		});

		scriptRefreeshListener = new IScriptManagerListener() {
			public void scriptChanged(String scriptname) {
			}

			public void scriptListChanged(String[] newScriptList) {
				reloadScriptList(qscriptExecuteCombo);
			}
		};
		scriptManager.addScriptManagerListener(scriptRefreeshListener);

		return qscriptExecuteCombo;
	}

	private void reloadScriptList(JComboBox qscriptExecuteCombo) {
		DefaultComboBoxModel dcbModel = new DefaultComboBoxModel();
		String[] qs = scriptManager.listQuickScripts();

		dcbModel.addElement(QUICK_SCRIPT_EXECUTE);

		for (int i = 0; i < qs.length; i++) {
			String name = qs[i];
			dcbModel.addElement(name);
		}

		qscriptExecuteCombo.setModel(dcbModel);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame#
	 * getVirtualBook ()
	 */
	public VirtualBook getVirtualBook() {
		return pianoroll.getVirtualBook();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame#
	 * getPianoRoll ()
	 */
	public JVirtualBookScrollableComponent getPianoRoll() {

		return pianoroll;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame#
	 * getCurrentInstrument()
	 */
	public Instrument getCurrentInstrument() {
		return instrument;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame#
	 * getWaitInterface()
	 */
	public IAPrintWait getWaitInterface() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame#
	 * getOwnerForDialog()
	 */
	public Object getOwnerForDialog() {
		return this;
	}

	@Override
	public boolean isDirty() {
		return super.isDirty() || isVirtualBookDirty();
	}

	/////////////////////////////////////////////////////////////////////////////////
	// copy paste implementation

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

	private void copySelectionToClipBoard() throws Exception {

		Set<Hole> selectionCopy = pianoroll.getSelectionCopy();

		HolesListTransferable transferable = new HolesListTransferable(pianoroll.getVirtualBook().getScale(),
				selectionCopy);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(transferable, this);
	}

	/**
	 * past clip board to virtual book
	 *
	 * @throws Exception
	 */
	public void paste() throws Exception {

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		// odd: the Object param of getContents is not currently used
		Transferable contents = clipboard.getContents(null);

		boolean hasTransferableBinaryData = (contents != null)
				&& contents.isDataFlavorSupported(HolesListTransferable.createDataFlavorBinary());

		if (hasTransferableBinaryData) {
			try {
				HolesListTransferable.Data result = (HolesListTransferable.Data) contents
						.getTransferData(HolesListTransferable.createDataFlavorBinary());

				// check scale
				Scale associatedScale = result.associatedScale;
				if (associatedScale != null) {

					VirtualBook currentBook = pianoroll.getVirtualBook();
					Scale currentBookScale = currentBook.getScale();

					if (currentBookScale != null && associatedScale.getTrackNb() == currentBookScale.getTrackNb()) {
						logger.debug("copy the holes at the selection"); //$NON-NLS-1$

						UndoStack us = pianoroll.getUndoStack();
						us.push(new GlobalVirtualBookUndoOperation(currentBook,
								Messages.getString("APrintNGVirtualBookInternalFrame.2015"), pianoroll)); // $NON-NLS-1$

						// ok
						currentBook.addHole(
								HoleTools.moveHolesAt(result.holes, pianoroll.MMToTime(pianoroll.getHightlight())));
						pianoroll.touchBook();
					}
				}

			} catch (Exception ex) {
				logger.error("error :" + ex.getMessage(), ex); //$NON-NLS-1$
				JMessageBox.showError(this, ex);
			}
		}
	}

	public void selectAll() throws Exception {
		VirtualBook currentBook = pianoroll.getVirtualBook();
		currentBook.getOrderedHolesCopy().stream().forEach((h) -> pianoroll.addToSelection(h));
	}

	public void selectNone() throws Exception {
		pianoroll.clearSelection();
	}

	/**
	 * this method permit to find extension points (used to transmit context in the
	 * frame, when an external element is working with this)
	 * 
	 * @param clazz
	 * @return
	 */
	public <T> T[] getExtensionPoints(Class<T> clazz) {
		// calling extensions for toolbars
		T[] allPoints = ExtensionPointProvider.getAllPoints(clazz, exts);
		return allPoints;
	}

}
