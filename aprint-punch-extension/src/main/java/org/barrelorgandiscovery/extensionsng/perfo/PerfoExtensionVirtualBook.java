package org.barrelorgandiscovery.extensionsng.perfo;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionPoint;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.extensions.SimpleExtensionPoint;
import org.barrelorgandiscovery.gui.CancelTracker;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.ainstrument.InstrumentAssociatedParameters;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.ImportersExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InformCurrentInstrumentExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InformCurrentVirtualBookExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InformRepositoryExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.LayersExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.OptionMenuItemsExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.ToolbarAddExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.VirtualBookToolbarButtonsExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.VisibilityLayerButtonsExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.InformVirtualBookFrameExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.InitNGExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.VirtualBookFrameToolRegister;
import org.barrelorgandiscovery.gui.atrace.Punch;
import org.barrelorgandiscovery.gui.atrace.PunchLayer;
import org.barrelorgandiscovery.gui.issues.JIssuePresenter;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.issues.IssueLayer;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.tools.FileNameExtensionFilter;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;

import aprintextensions.fr.freydierepatrice.perfo.gerard.BeanAsk;
import aprintextensions.fr.freydierepatrice.perfo.gerard.JPerfoExtensionParameters;
import aprintextensions.fr.freydierepatrice.perfo.gerard.PerfoExtensionParameters;
import aprintextensions.fr.freydierepatrice.perfo.gerard.PerfoPunchConverter;

/**
 * Extension de perçage pour la machine à percer de gérard
 * 
 * @author Freydiere Patrice
 * 
 */
public class PerfoExtensionVirtualBook implements IExtension,
		InitNGExtensionPoint, LayersExtensionPoint,
		InformCurrentVirtualBookExtensionPoint,
		InformCurrentInstrumentExtensionPoint,
		VisibilityLayerButtonsExtensionPoint, ActionListener,
		ToolbarAddExtensionPoint, VirtualBookFrameToolRegister,
		InformVirtualBookFrameExtensionPoint {

	private static final String POINCONSIZE = "poinconsize";

	private static final String POINCONHEIGHT = "poinconheight";

	private static final String PAGE_SIZE = "page_size";

	private static final String MINIMUM_LENGTH_FOR_TWO_PUNCH = "minimum_length_for_two_punch";

	private static final String AVANCEMENT = "avancement";

	private static Logger logger = Logger
			.getLogger(PerfoExtensionVirtualBook.class);

	// points d'extension ...

	private IssueLayer issuesPunchLayer = null;

	private JIssuePresenter issuesPresenter = new JIssuePresenter(null);

	private PunchLayer resultPunchLayer = null;

	private PerfoPunchConverter perfoconverter = null;

	private PerfoExtensionParameters parameters = new PerfoExtensionParameters();

	private Instrument instrument = null;

	public PerfoExtensionVirtualBook() {
		this.issuesPunchLayer = new IssueLayer();
		this.resultPunchLayer = new PunchLayer();
		this.resultPunchLayer.setOrigin(resultPunchLayer.ORIGIN_LEFT_MIDDLE);
	}

	public ExtensionPoint[] getExtensionPoints() {

		try {
			return new ExtensionPoint[] {
					new SimpleExtensionPoint(InitNGExtensionPoint.class,
							PerfoExtensionVirtualBook.this),
					new SimpleExtensionPoint(LayersExtensionPoint.class,
							PerfoExtensionVirtualBook.this),
					new SimpleExtensionPoint(
							InformCurrentVirtualBookExtensionPoint.class,
							PerfoExtensionVirtualBook.this),
					new SimpleExtensionPoint(ToolbarAddExtensionPoint.class,
							PerfoExtensionVirtualBook.this),
					new SimpleExtensionPoint(
							InformCurrentInstrumentExtensionPoint.class,
							PerfoExtensionVirtualBook.this),
					new SimpleExtensionPoint(
							InformVirtualBookFrameExtensionPoint.class,
							PerfoExtensionVirtualBook.this),
					new SimpleExtensionPoint(
							VirtualBookFrameToolRegister.class,
							PerfoExtensionVirtualBook.this)

			};
		} catch (Exception ex) {
			logger.error("error in declaring the extension points, "
					+ ex.getMessage());
			return new ExtensionPoint[0];
		}
	}

	public String getName() {
		return "Perforation Extension";
	}

	// cycle de vie

	private APrintNG aprintref;

	private IPrefsStorage instrumentPrefsStorage;

	public void init(APrintNG f) {

		assert f != null;

		logger.debug("Initialisation de l'extension de perforation");
		this.aprintref = f;

		// loading preferences associated to the application ...

		logger.debug("loading properties ... ");
		APrintProperties properties = aprintref.getProperties();
		File aprintFolder = properties.getAprintFolder();
		if (!aprintFolder.exists()) {
			logger.debug("aprintfolder doesn't exist, create it .. ");
			aprintFolder.mkdir();
		}
		this.instrumentPrefsStorage = new FilePrefsStorage(new File(
				aprintFolder, "perfoextension.properties"));

		logger.debug("properties for perfo created ... ");

	}

	// tuning de l'interface ...

	private JButton mi = null;

	private JButton createOptionButton() {
		mi = new JButton("Options de perçage ...");
		// largeur du poinçon ...
		// hauteur du poinçon ...
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				PerfoExtensionParameters p = JPerfoExtensionParameters
						.editParameters(aprintref, parameters);
				if (p != null) {

					parameters = p;

					logger.debug("saving current instrument associated properties ... ");

					savePerfoPrefs(p);

					// rechargement du carton
					if (currentVirtualBook != null)
						informCurrentVirtualBook(currentVirtualBook);
					frame.getPianoRoll().repaint();

				}

			}
		});
		return mi;
	}

	public void addLayers(JVirtualBookScrollableComponent c) {
		c.addLayer(issuesPunchLayer);
		c.addLayer(resultPunchLayer);
	}

	public void removeLayers(JVirtualBookScrollableComponent c) {
		c.removeLayer(issuesPunchLayer);
		c.removeLayer(resultPunchLayer);
	}

	private VirtualBook currentVirtualBook = null;

	public void informCurrentInstrument(Instrument instrument) {
		logger.debug("current instrument :" + instrument);
		this.instrument = instrument;

		logger.debug("creating associated parameter .. to instrument .. ");

		cps = new InstrumentAssociatedParameters(this.instrumentPrefsStorage)
				.getInstrumentPrefsStorage(instrument);

		try {
			loadPerfoPrefs(parameters);
		} catch (Throwable t) {
			logger.warn(
					"error when loading prefs parameters :" + t.getMessage(), t);
		}
		logger.debug("associated prefs storage created ... ");
	}

	public void informCurrentVirtualBook(VirtualBook vb) {

		this.currentVirtualBook = vb;

		// Vérification des contraintes ...
		perfoconverter = new PerfoPunchConverter(vb);

		perfoconverter.convertToPunchPage(parameters.poinconsize,
				parameters.poinconheight, parameters.page_size,
				parameters.avancement, parameters.minimum_length_for_two_punch);

		issuesPunchLayer.setIssueCollection(null, vb);

		if (perfoconverter.hasErrors()) {
			// raz des layers ...

			issuesPunchLayer.setIssueCollection(perfoconverter.getIssues(), vb);

		}

		issuesPresenter.setIssueLayer(issuesPunchLayer);

		// sinon

		resultPunchLayer.setPunch(perfoconverter.getPunchesCopy());
		resultPunchLayer.setPunchHeight(parameters.poinconheight);
		resultPunchLayer.setPunchWidth(parameters.poinconsize);

	}

	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();

		if ("OPTIMIZE".equals(actionCommand)) {
			if (perfoconverter == null) {
				JMessageBox.showMessage(aprintref.getOwnerForDialog(),
						"Vous devez charger un carton");
				return;
			}

			if (perfoconverter.hasErrors()) {
				JMessageBox
						.showMessage(aprintref.getOwnerForDialog(),
								"des erreurs subsistes dans le carton, le perçage ne reflete pas l'écoute");
			}

			// lancement de l'optimisation ...

			Thread t = new Thread(new Runnable() {

				public void run() {

					CancelTracker ct = new CancelTracker();

					frame.getWaitInterface().infiniteStartWait(
							"Optimisation du tracé", ct);
					try {

						perfoconverter.optimize(frame.getWaitInterface(), ct);
						resultPunchLayer.setPunch(perfoconverter
								.getPunchesCopy());
						frame.getPianoRoll().repaint();
					} finally {
						frame.getWaitInterface().infiniteEndWait();
					}
				}
			});
			t.start();

		}

		else if ("SAVE".equals(actionCommand)) {

			if (this.currentVirtualBook == null) {
				JMessageBox.showMessage(aprintref.getOwnerForDialog(),
						"Pas de carton virtuel");
				return;
			}

			if (this.perfoconverter == null) {
				JMessageBox.showMessage(aprintref.getOwnerForDialog(),
						"Pas de carton");
				return;
			}

			if (this.perfoconverter.hasErrors()) {
				JMessageBox
						.showMessage(aprintref.getOwnerForDialog(),
								"Des erreurs subsistent dans le carton, corrigez les erreurs");
				return;
			}

			// Sélection du fichier ..
			// demande du fichier à sauvegarder ...
			JFileChooser choose = new JFileChooser();

			choose.setFileSelectionMode(JFileChooser.FILES_ONLY);

			choose.setFileFilter(new FileNameExtensionFilter(
					"Fichier XYU", "xyu")); //$NON-NLS-1$ //$NON-NLS-2$

			if (choose.showSaveDialog(aprintref) == JFileChooser.APPROVE_OPTION) {

				File savedfile = choose.getSelectedFile();
				if (savedfile == null) {
					JMessageBox.showMessage(aprintref.getOwnerForDialog(),
							"pas de fichier sélectionné");
					return;
				}

				try {

					FileWriter fw = new FileWriter(savedfile, false);
					try {
						// Ecriture de l'entete ...

						write(fw, 1, 0); // 1, longueur en mm du carton

						for (int i = 0; i < perfoconverter.getPageCount(); i++) {

							if (i > 0) {
								// changement de page
								write(fw, 0, 0);
								write(fw, 2, i - 1);
							}

							// ecriture des punch de la page
							double decalage = i * perfoconverter.getPagesize();

							ArrayList<Punch> page = perfoconverter.getPage(i);
							for (Iterator iterator = page.iterator(); iterator
									.hasNext();) {
								Punch punch = (Punch) iterator.next();

								// calcul des pas ...
								double x = punch.x - decalage;
								double mmparpas = (25 * 4.2) / (4599 - 400); // calculs
								// issus
								// d'un
								// fichier
								// d'essai

								int pasx = (int) (x / mmparpas);
								int pasy = (int) (punch.y / mmparpas);

								write(fw, pasy, pasx);

							}

						}

						write(fw, 0, 0);
						write(fw, 2, perfoconverter.getPageCount() - 1);

						write(fw, 3, 320); // espace entre morceaux
						write(fw, 4, 0); // fin de parcours

					} finally {
						fw.close();
					}

					JMessageBox.showMessage(aprintref.getOwnerForDialog(),
							"Fichier sauvegardé");

				} catch (Throwable ex) {
					JMessageBox.showMessage(aprintref.getOwnerForDialog(),
							"Erreur dans la sauvegarde du fichier");
					logger.error("save", ex);

					new Thread(new Runnable() {
						public void run() {
							BugReporter.sendBugReport();
						}
					}).start();
				}

			}
			// sauvegarde du fichier ...

		}
	}

	private static void write(Writer os, int i1, int i2) throws IOException {
		os.write(formatInt(i1) + " " + formatInt(i2) + "\n");
	}

	private static String formatInt(int i1) {
		String s = "" + i1;
		while (s.length() < 6)
			s = " " + s;
		return s;
	}

	private JButton optimize = null;
	private JButton saveasxyu = null;
	private JButton savedxf = null;

	private void addPerfoButtonsInToolBar(JToolBar tb) {
		optimize = new JButton("Optimiser le tracé");
		optimize.setIcon(new ImageIcon(getClass().getResource("misc.png")));
		optimize.setToolTipText("Optimisation du parcours du poincons pour l'extention perfo");
		optimize.setActionCommand("OPTIMIZE");
		optimize.addActionListener(this);

		tb.add(optimize);

		saveasxyu = new JButton("Sauvegarder le fichier xyu");
		saveasxyu.setIcon(new ImageIcon(getClass().getResource("misc.png")));
		saveasxyu.setToolTipText("Sauvegarder en tant que fichier xyu");
		saveasxyu.setActionCommand("SAVE");
		saveasxyu.addActionListener(this);

		tb.add(saveasxyu);

	}

	public JToolBar[] addToolBars() {
		return new JToolBar[] { createPerfoToolBar() };
	}

	private JToolBar createPerfoToolBar() {
		JToolBar tb = new JToolBar("Perfo");
		addVisibilityLayerButtons(tb);
		JButton createOptionButton = createOptionButton();
		tb.add(createOptionButton);
		addPerfoButtonsInToolBar(tb);
		return tb;
	}

	public void removeButtons(JToolBar tb) {
		tb.remove(optimize);
		tb.remove(saveasxyu);
		tb.remove(savedxf);
		tb.remove(mi);
	}

	private JCheckBox perfo = null;

	private IPrefsStorage cps = null;

	public void addVisibilityLayerButtons(JToolBar tb) {
		perfo = new JCheckBox("Perforateur");
		perfo.setSelected(true);

		perfo.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				try {
					JCheckBox c = (JCheckBox) e.getSource();
					boolean checked = c.isSelected();

					issuesPunchLayer.setVisible(checked);
					resultPunchLayer.setVisible(checked);

					frame.getPianoRoll().repaint();
				} catch (Throwable t) {
					logger.error("error in throwing " + t.getMessage(), t);
				}
			}
		});

		tb.add(perfo);
	}

	public void removeVisibilityLayerButtons(JToolBar tb) {
		tb.remove(perfo);

	}

	private void savePerfoPrefs(PerfoExtensionParameters p) {
		if (cps == null) {
			logger.debug("no instrument preferences");
			return;
		}

		cps.setDoubleProperty(AVANCEMENT, p.avancement);
		cps.setDoubleProperty(MINIMUM_LENGTH_FOR_TWO_PUNCH,
				p.minimum_length_for_two_punch);
		cps.setDoubleProperty(PAGE_SIZE, p.page_size);
		cps.setDoubleProperty(POINCONHEIGHT, p.poinconheight);
		cps.setDoubleProperty(POINCONSIZE, p.poinconsize);

		cps.save();
	}

	private void loadPerfoPrefs(PerfoExtensionParameters p) {

		if (cps == null) {
			logger.debug("no instrument preferences");
			return;
		}

		try {
			cps.load();

			PerfoExtensionParameters defaultp = new PerfoExtensionParameters();
			p.avancement = cps.getDoubleProperty(AVANCEMENT,
					defaultp.avancement);
			p.minimum_length_for_two_punch = cps.getDoubleProperty(
					MINIMUM_LENGTH_FOR_TWO_PUNCH,
					defaultp.minimum_length_for_two_punch);
			p.page_size = cps.getDoubleProperty(PAGE_SIZE, defaultp.page_size);
			p.poinconheight = cps.getDoubleProperty(POINCONHEIGHT,
					defaultp.poinconheight);
			p.poinconsize = cps.getDoubleProperty(POINCONSIZE,
					defaultp.poinconsize);

		} catch (Exception ex) {
			logger.error("error in loading prefs :" + ex.getMessage(), ex);
		}

	}

	private APrintNGVirtualBookFrame frame;

	public void informVirtualBookFrame(APrintNGVirtualBookFrame frame) {
		this.frame = frame;

	}

	public void registerToolWindow(MyDoggyToolWindowManager manager) {

		// Register a Tool.
		manager.registerToolWindow("Erreurs sur le perçage", // Id //$NON-NLS-1$
				"Perfo Window", // Title //$NON-NLS-1$
				null, // Icon
				issuesPresenter, // Component
				ToolWindowAnchor.RIGHT); // Anchor

	}

}
