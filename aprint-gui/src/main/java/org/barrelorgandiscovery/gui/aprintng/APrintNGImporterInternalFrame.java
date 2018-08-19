package org.barrelorgandiscovery.gui.aprintng;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.BeanInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionPointProvider;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprint.extensions.CustomImporterParameters;
import org.barrelorgandiscovery.gui.aprint.extensions.ImporterParameters;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.ImportersExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InformCurrentVirtualBookExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.instrumentchoice.IInstrumentChoice;
import org.barrelorgandiscovery.gui.aprint.instrumentchoice.IInstrumentChoiceListener;
import org.barrelorgandiscovery.gui.aprint.instrumentchoice.JCoverFlowInstrumentChoice;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.issues.AbstractIssue;
import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.repository.Repository;
import org.barrelorgandiscovery.repository.RepositoryAdapter;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.FileNameExtensionFilter;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.SwingUtils;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.checker.Checker;
import org.barrelorgandiscovery.virtualbook.checker.CheckerFactory;
import org.barrelorgandiscovery.virtualbook.checker.OverlappingHole;
import org.barrelorgandiscovery.virtualbook.checker.TranspositionIssueConverter;
import org.barrelorgandiscovery.virtualbook.io.MidiIO;
import org.barrelorgandiscovery.virtualbook.io.MidiIOResult;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractMidiImporter;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractTransformation;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractTransposeVirtualBook;
import org.barrelorgandiscovery.virtualbook.transformation.TransformationManager;
import org.barrelorgandiscovery.virtualbook.transformation.TranspositionResult;
import org.barrelorgandiscovery.virtualbook.transformation.Transpositor;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiConversionProblem;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiConversionResult;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFileIO;
import org.barrelorgandiscovery.virtualbook.x2010.impl.VirtualBookImpl;

import com.jeta.forms.components.panel.FormPanel;
import com.l2fprod.common.demo.BeanBinder;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

/**
 * Internal frame for importing a new virtual book
 * 
 * @author Freydiere Patrice
 * 
 */
public class APrintNGImporterInternalFrame extends APrintNGInternalFrame
		implements ActionListener {

	private static Logger logger = Logger
			.getLogger(APrintNGImporterInternalFrame.class);

	private boolean init = true;

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

	private JButton chargementmidi = new JButton(Messages.getString("APrint.1")); //$NON-NLS-1$

	
	private JLabel nomfichier = new JLabel(Messages.getString("APrint.43")); //$NON-NLS-1$


	private JComponent choixtransposition;


	private JComboBox listetransposition = new JComboBox();

	private JTextField searchTextField = new JTextField(20);
	private JButton searchButton = new JButton(Messages.getString("APrint.292")); //$NON-NLS-1$
	private JButton resetFilterButton = new JButton(
			Messages.getString("APrint.293")); //$NON-NLS-1$

	private AbstractTransformation getCurrentTransposition() {
		return (AbstractTransformation) listetransposition.getSelectedItem();
	}

	private IInstrumentChoice instrumentChoice;

	private IAPrintWait waitininterface;

	private RepositoryAdapter repository;

	private File midifile = null;

	// private VirtualBook readCarton;

	/**
	 * Gestionnaire de transposition
	 */
	private TransformationManager tm = null;

	private APrintProperties aprintproperties;

	private APrintNGGeneralServices services;

	private IExtension[] exts;

	public APrintNGImporterInternalFrame(IAPrintWait waitininterface,
			APrintRepositoryListener l, APrintProperties aprintproperties,
			IExtension[] exts, RepositoryAdapter startRepository,
			APrintNGGeneralServices services) throws Exception {

		super(
				aprintproperties.getFilePrefsStorage(),
				Messages.getString("APrintNGImporterInternalFrame.0"), true, true, true, true); //$NON-NLS-1$

		this.waitininterface = this;
		this.aprintproperties = aprintproperties;
		this.exts = exts;
		this.repository = startRepository;
		this.tm = repository.getTranspositionManager();
		this.services = services;

		initComponents();

		l.addAPrintRepositoryListener(new APrintRepositoryChangedListener() {
			public void repositoryChanged(Repository newRepository) {
				repository = (RepositoryAdapter) newRepository;
				tm = repository.getTranspositionManager();
				instrumentChoice
						.setRepository(((RepositoryAdapter) newRepository)
								.getRepository2());
			}
		});

		refreshTranspositions();

	}

	private void initComponents() throws Exception {

		getContentPane().setLayout(new BorderLayout());
		
		FormPanel pInstrument = new FormPanel(getClass().getResourceAsStream(
				"importerframe.jfrm"));//$NON-NLS-1$
		listetransposition = (JComboBox) pInstrument
				.getComponentByName("cbtransposition");
		JLabel labeltransposition = pInstrument
				.getLabel("lbltransposition");
		labeltransposition.setText(Messages.getString("APrint.11"));//$NON-NLS-1$

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
					JMessageBox.showMessage(services.getOwnerForDialog(),
							Messages.getString("APrint.55") //$NON-NLS-1$
									+ ex.getMessage());
				}
			}
		});

		chargementmidi = (JButton) pInstrument.getButton("browse"); //$NON-NLS-1$
		chargementmidi.setActionCommand("CHARGEMENTMIDI"); //$NON-NLS-1$
		chargementmidi.addActionListener(this);
		chargementmidi.setIcon(new ImageIcon(getClass().getResource(
				"folder_open.png"), Messages.getString("APrint.78"))); //$NON-NLS-1$ //$NON-NLS-2$
		chargementmidi.setToolTipText(Messages.getString("APrint.127")); //$NON-NLS-1$
		chargementmidi.setText(Messages.getString("APrint.1"));//$NON-NLS-1$
		
		
		nomfichier = pInstrument.getLabel("lblfilename");//$NON-NLS-1$
		nomfichier.setText(Messages.getString("APrint.43"));//$NON-NLS-1$

		JLabel lblnomfichier =  pInstrument.getLabel("lblmidiorkartoimport");//$NON-NLS-1$
		lblnomfichier.setText(Messages.getString("APrint.160"));//$NON-NLS-1$
		
		// /////////////////////////////////////////////////////////////////////////////
		// Panneau de choix de gamme, transposition et instruments

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
						APrintNGImporterInternalFrame.this.instrumentChanged();
					}
				});
		instrumentChoice = cfic;

	
		pInstrument.getFormAccessor().replaceBean(
				pInstrument.getComponentByName("instruments"), cfic);

	
		JLabel lblSearch = (JLabel)pInstrument.getComponentByName("lblfilterinstruments");//$NON-NLS-1$
		lblSearch.setText(Messages.getString("APrint.161"));//$NON-NLS-1$
		
		searchTextField = (JTextField)pInstrument.getComponentByName("txtfilter");//$NON-NLS-1$


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

		resetFilterButton = (JButton)pInstrument.getButton("reset"); //$NON-NLS-1$
		resetFilterButton.setText(Messages.getString("APrint.293")); //$NON-NLS-1$
		
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


		searchButton = (JButton)pInstrument.getComponentByName("filterinstruments"); //$NON-NLS-1$
		searchButton.addActionListener(searchListener);
		searchButton.setText(Messages.getString("APrint.292")); //$NON-NLS-1$
		
		
		
		JButton convertir = (JButton)pInstrument.getComponentByName("import"); //$NON-NLS-1$
		convertir.setText(Messages.getString("APrint.130")); //$NON-NLS-1$
		
		convertir.setToolTipText(Messages.getString("APrint.131")); //$NON-NLS-1$
		convertir.setIcon(new ImageIcon(getClass().getResource(
				"2rightarrow.png")));//$NON-NLS-1$
		convertir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					waitininterface.infiniteStartWait(Messages
							.getString("APrint.150")); //$NON-NLS-1$

					backgroundexecutor.execute(new Runnable() {

						public void run() {
							try {
								try {
									transposeCarton();
								} finally {
									waitininterface.infiniteEndWait();
								}
								SwingUtilities.invokeAndWait(new Runnable() {

									public void run() {
										try {

											checkState();
											
											// close the window, we don't need it any more
											
											dispose();
											
										} catch (Exception ex) {
											logger.error("transposeCarton", ex); //$NON-NLS-1$
											JMessageBox.showMessage(services
													.getOwnerForDialog(), ex
													.getMessage());
										}
									}
								});

							} catch (Throwable ex) {
								logger.error("transposeCarton", ex); //$NON-NLS-1$
								JMessageBox.showMessage(
										services.getOwnerForDialog(),
										ex.getMessage());
							}
						}
					});

				} catch (Exception ex) {
					logger.error("Convertir", ex); //$NON-NLS-1$
					JMessageBox.showMessage(services.getOwnerForDialog(),
							ex.getMessage());
				}
			}
		});

		
		choixtransposition = (JComponent)pInstrument.getComponentByName("choixtransposition");//$NON-NLS-1$

		getContentPane().add(pInstrument, BorderLayout.CENTER);

		setupLastSelectedInstrument();

		instrumentChanged(); // refresh

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				
				System.out.println(((JComponent)instrumentChoice).getSize());
				
				super.windowActivated(e);
			}
		});
		
		init = false;
	}


	private void setupLastSelectedInstrument() {
		String ln = aprintproperties.getLastSelectedInstrument();
		if (ln != null) {
			boolean elementSelected = instrumentChoice.selectInstrument(ln);
			logger.debug("found selected element :" + elementSelected);
		}

	}

	public void actionPerformed(ActionEvent e) {
		try {

			if ("CHARGEMENTMIDI".equals(e.getActionCommand())) { //$NON-NLS-1$
				loadMidi();

			}

		} catch (Exception ex) {
			logger.error("actionPerformed", ex); //$NON-NLS-1$

			BugReporter.sendBugReport();
			JMessageBox.showMessage(services.getOwnerForDialog(),
					Messages.getString("APrint.42") + ex.getMessage()); //$NON-NLS-1$

		}
	}

	/**
	 * Open the load dialog box and define the load midi file
	 */
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

			defineCurrentMidiFile(result);

		}

	}

	/**
	 * Define the current used midi file
	 * 
	 * @param midiFile
	 */
	public void defineCurrentMidiFile(final File midiFile) {
		aprintproperties.setLastMidiFile(midiFile);

		if (midiFile != null) {
			// Chargement du carton virtuel ..

			nomfichier.setText(midiFile.getName());
			nomfichier.setToolTipText(midiFile.getAbsolutePath());

			midifile = midiFile;

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
				if (logger.isDebugEnabled() && mi.size() == 0) {
					logger.debug("no scripts ..."); //$NON-NLS-1$
				}

				for (AbstractMidiImporter abstractMidiImporter : mi) {
					logger.debug("adding script :" + abstractMidiImporter); //$NON-NLS-1$
					listetransposition.addItem(abstractMidiImporter);
				}

				logger.debug("adding the extensions importers ..."); //$NON-NLS-1$

				ArrayList<AbstractMidiImporter> extension_importers = new ArrayList<AbstractMidiImporter>();
				ImportersExtensionPoint[] pts = ExtensionPointProvider
						.getAllPoints(ImportersExtensionPoint.class, exts);
				for (int i = 0; i < pts.length; i++) {
					ImportersExtensionPoint importersExtensionPoint = pts[i];
					try {
						logger.debug("get importer from extension :" + pts); //$NON-NLS-1$
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

	private MidiIOResult readMidiFile(File f) throws Exception {
		try {

			return MidiIO.readCartonWithError(f);

		} catch (Exception ex) {
			// Erreur de chargement du fichier

			// Affichage du problème ..
			logger.error("Chargement du carton", ex); //$NON-NLS-1$
			throw new Exception("Error while loading the midi file :"
					+ ex.getMessage(), ex);
		}

	}

	/**
	 * Methode de transposition du carton pour un orgue donné
	 * 
	 * @throws Exception
	 */
	private void transposeCarton() throws Exception {

		VirtualBook transposedCarton;

		AbstractTransformation at = getCurrentTransposition();

		assert at != null;

		MidiIOResult readMidiFile = readMidiFile(midifile);

		VirtualBook readCarton = readMidiFile.virtualBook;

		assert readCarton != null;

		//
		// Mémorisation des erreurs lors de la lecture ou transformation
		//
		IssueCollection issueCollection = new IssueCollection();

		List<AbstractIssue> issuesMidiFile = readMidiFile.issues;
		if (issuesMidiFile != null) {
			for (Iterator iterator = issuesMidiFile.iterator(); iterator
					.hasNext();) {
				AbstractIssue i = (AbstractIssue) iterator.next();
				issueCollection.add(i);
			}
		}

		if (at instanceof AbstractTransposeVirtualBook) {

			waitininterface
					.infiniteChangeText(Messages.getString("APrint.155")); //$NON-NLS-1$

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

				waitininterface.infiniteChangeText(Messages
						.getString("APrintNGImporterInternalFrame.4")); //$NON-NLS-1$

				// TODO
				// il.setIssueCollection(TranspositionIssueConverter.convert(tr,
				// readCarton.getScale()), tr.virtualbook);

				IssueCollection convertErrors = TranspositionIssueConverter
						.convert(tr, readCarton.getScale());
				issueCollection.addAll(convertErrors);

			} else {
				// il n'y a pas d'erreurs

				waitininterface.infiniteChangeText(Messages
						.getString("APrintNGImporterInternalFrame.5")); //$NON-NLS-1$

				// TODO
				// il.setIssueCollection(null, null);
			}

			transposedCarton = tr.virtualbook;

			// TODO Send result ...

		} else if (at instanceof AbstractMidiImporter) {

			waitininterface
					.infiniteChangeText(Messages.getString("APrint.156")); //$NON-NLS-1$

			AbstractMidiImporter mi = (AbstractMidiImporter) at;

			askForImportParametersIfExist(mi);

			waitininterface
					.infiniteChangeText(Messages.getString("APrint.252")); //$NON-NLS-1$

			logger.debug("read midi file ..."); //$NON-NLS-1$

			MidiConversionResult r = mi.convert(MidiFileIO.read(midifile));

			logger.debug("converting the errors ... "); //$NON-NLS-1$

			if (r.issues != null && r.issues.size() > 0) {
				JMessageBox.showMessage(services.getOwnerForDialog(),
						Messages.getString("APrint.33")); //$NON-NLS-1$

				for (Iterator iterator = r.issues.iterator(); iterator
						.hasNext();) {
					MidiConversionProblem mcp = (MidiConversionProblem) iterator
							.next();

					issueCollection.add(mcp.toIssue());
				}

			}

			logger.debug("get the result ..."); //$NON-NLS-1$
			transposedCarton = r.virtualbook;

		} else {
			throw new Exception("implementation error"); //$NON-NLS-1$
		}

		assert transposedCarton != null;

		waitininterface.infiniteChangeText(Messages.getString("APrint.157")); //$NON-NLS-1$

		// Dans les deux cas, on regarde les trous se téléscopant ...
		OverlappingHole oh = new OverlappingHole();
		IssueCollection ic = oh.check(transposedCarton);
		if (ic != null)
			issueCollection.addAll(ic);

		waitininterface.infiniteChangeText(Messages.getString("APrint.158")); //$NON-NLS-1$

		// Autres vérifications associées à la gamme ...
		Checker[] c = CheckerFactory.createCheckers(at.getScaleDestination());
		Checker composite = CheckerFactory.toComposite(c);
		
		try {
			IssueCollection compositeIssueCollection = composite.check(transposedCarton);
			ic.addAll(compositeIssueCollection);
			issueCollection.addAll(ic);
		} catch(Exception ex) {
			logger.error("fail to check constraints for holes:" + ex.getMessage(), ex);
		}
		

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

		services.newVirtualBook(transposedCarton, getSelectedInstrument(),
				issueCollection);

		repaint();

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

	// ////////////////////////////////////////////////////////////////////////
	// Gestion de l'état de l'interface ...

	private void instrumentChanged() {
		try {

			// transposedCarton = null; // raz de la transposition
			// pianoroll.setVirtualBook(null);
			//

			Instrument currentInstrument = instrumentChoice
					.getCurrentInstrument();
			if (currentInstrument != null && !init) {
				logger.debug("set preferences instrument :"
						+ currentInstrument.getName());

				aprintproperties.setLastSelectedInstrument(currentInstrument
						.getName());
			}
			refreshTranspositions();

			choixtransposition
					.setVisible(listetransposition.getItemCount() > 1);

			checkState();

		} catch (Exception ex) {
			// Traitement de l'exception
			logger.error("instrumentChanged " + ex.getMessage(), ex); //$NON-NLS-1$
		}
	}

	/**
	 * Verifie l'état de l'interface en fonction de l'état de l'IHM
	 */
	private void checkState() throws Exception {

		repaint();
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

			waitininterface
					.infiniteChangeText(Messages.getString("APrint.253")); //$NON-NLS-1$

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
								(JFrame) null, Messages.getString("APrint.239")); //$NON-NLS-1$

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

}
