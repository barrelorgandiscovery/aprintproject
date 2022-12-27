package aprintextensions.fr.freydierepatrice.perfo.gerard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionPoint;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.extensions.SimpleExtensionPoint;
import org.barrelorgandiscovery.extensionsng.perfo.gui.PunchLayer;
import org.barrelorgandiscovery.gui.CancelTracker;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.ImportersExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InformCurrentVirtualBookExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InformRepositoryExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InitExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.LayersExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.OptionMenuItemsExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.ToolbarAddExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.VisibilityLayerButtonsExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
import org.barrelorgandiscovery.issues.IssueLayer;
import org.barrelorgandiscovery.optimizers.model.Punch;
import org.barrelorgandiscovery.repository.Repository;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.VFSTools;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractMidiImporter;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractTransformation;
import org.barrelorgandiscovery.virtualbook.transformation.LinearTransposition;

/**
 * Extension de per�age pour la machine � percer de g�rard
 * 
 * @author Freydiere Patrice
 * 
 */
public class PerfoExtension implements IExtension, InitExtensionPoint,
		LayersExtensionPoint, InformCurrentVirtualBookExtensionPoint,
		VisibilityLayerButtonsExtensionPoint, ActionListener,
		ImportersExtensionPoint, InformRepositoryExtensionPoint,
		ToolbarAddExtensionPoint {

	private static Logger logger = Logger.getLogger(PerfoExtension.class);

	// points d'extension ...

	private IssueLayer issuesPunchLayer = null;

	private PunchLayer resultPunchLayer = null;

	private PerfoPunchConverter perfoconverter = null;

	private PerfoExtensionParameters parameters = new PerfoExtensionParameters();

	public PerfoExtension() {
		this.issuesPunchLayer = new IssueLayer();
		this.resultPunchLayer = new PunchLayer();
		this.resultPunchLayer.setOrigin(resultPunchLayer.ORIGIN_LEFT_MIDDLE);
	}

	public ExtensionPoint[] getExtensionPoints() {
		try {
			return new ExtensionPoint[] {
					new SimpleExtensionPoint(InitExtensionPoint.class,
							PerfoExtension.this),
					new SimpleExtensionPoint(
							OptionMenuItemsExtensionPoint.class,
							PerfoExtension.this),
					new SimpleExtensionPoint(LayersExtensionPoint.class,
							PerfoExtension.this),
					new SimpleExtensionPoint(
							InformCurrentVirtualBookExtensionPoint.class,
							PerfoExtension.this),

					new SimpleExtensionPoint(
							InformRepositoryExtensionPoint.class,
							PerfoExtension.this),
					new SimpleExtensionPoint(ImportersExtensionPoint.class,
							PerfoExtension.this),
					new SimpleExtensionPoint(ToolbarAddExtensionPoint.class,
							PerfoExtension.this) };
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return new ExtensionPoint[0];
		}
	}

	public String getName() {
		return "Perforation Extension";
	}

	// cycle de vie

	private APrintNG aprintref;

	public void init(APrintNG f) {

		assert f != null;

		logger.debug("Initialisation de l'extension de perforation");
		this.aprintref = f;
	}

	// tuning de l'interface ...

	private JButton mi = null;

	private JButton createOptionButton() {
		mi = new JButton("Options de per�age ...");
		// largeur du poin�on ...
		// hauteur du poin�on ...
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				PerfoExtensionParameters p = JPerfoExtensionParameters
						.editParameters(aprintref, parameters);
				if (p != null) {
					parameters = p;
					// rechargement du carton
					if (currentVirtualBook != null)
						informCurrentVirtualBook(currentVirtualBook);
					aprintref.repaint();
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

	public void informCurrentVirtualBook(VirtualBook vb) {

		this.currentVirtualBook = vb;

		// V�rification des contraintes ...
		perfoconverter = new PerfoPunchConverter(vb);

		perfoconverter.convertToPunchPage(parameters.poinconsize,
				parameters.poinconheight, parameters.page_size,
				parameters.avancement, parameters.minimum_length_for_two_punch);

		issuesPunchLayer.setIssueCollection(null, vb);

		if (perfoconverter.hasErrors()) {
			// raz des layers ...

			issuesPunchLayer.setIssueCollection(perfoconverter.getIssues(), vb);

		}

		// sinon

		resultPunchLayer.setOptimizedObject(perfoconverter.getPunchesCopy());
		resultPunchLayer.setPunchHeight(parameters.poinconheight);
		resultPunchLayer.setPunchWidth(parameters.poinconsize);

	}

	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();

		if ("OPTIMIZE".equals(actionCommand)) {
			if (perfoconverter == null) {
				JMessageBox.showMessage(aprintref,
						"Vous devez charger un carton");
				return;
			}

			if (perfoconverter.hasErrors()) {
				JMessageBox
						.showMessage(aprintref,
								"des erreurs subsistes dans le carton, le per�age ne reflete pas l'�coute");
			}

			// lancement de l'optimisation ...

			Thread t = new Thread(new Runnable() {

				public void run() {

					CancelTracker ct = new CancelTracker();
					aprintref.infiniteStartWait("Optimisation du trac�", ct);
					try {

						perfoconverter.optimize(aprintref, ct);
						if (!ct.isCanceled()) {
							resultPunchLayer.setOptimizedObject(perfoconverter
									.getPunchesCopy());
						}
						aprintref.repaint();
					} finally {
						aprintref.infiniteEndWait();
					}
				}
			});
			t.start();

		} else if ("SAVE".equals(actionCommand)) {

			if (this.currentVirtualBook == null) {
				JMessageBox.showMessage(aprintref, "Pas de carton virtuel");
				return;
			}

			if (this.perfoconverter == null) {
				JMessageBox.showMessage(aprintref, "Pas de carton");
				return;
			}

			if (this.perfoconverter.hasErrors()) {
				JMessageBox
						.showMessage(aprintref,
								"Des erreurs subsistent dans le carton, corrigez les erreurs");
				return;
			}

			// S�lection du fichier ..
			// demande du fichier � sauvegarder ...
			APrintFileChooser choose = new APrintFileChooser();

			choose.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

			choose.setFileFilter(new VFSFileNameExtensionFilter(
					"Fichier XYU", "xyu")); //$NON-NLS-1$ //$NON-NLS-2$

			if (choose.showSaveDialog(aprintref) == APrintFileChooser.APPROVE_OPTION) {

				AbstractFileObject savedfile = choose.getSelectedFile();
				if (savedfile == null) {
					JMessageBox.showMessage(aprintref,
							"pas de fichier s�lectionn�");
					return;
				}

				try {

					OutputStream outStream = VFSTools.transactionalWrite( savedfile);
					Writer fw = new OutputStreamWriter(outStream);
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

					JMessageBox.showMessage(aprintref, "Fichier sauvegard�");

				} catch (Throwable ex) {
					JMessageBox.showMessage(aprintref,
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
		optimize = new JButton("Optimiser le trac�");
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
		return new JToolBar[] { createPerfoToolBar(), createExportDXFToolBar() };
	}

	private JToolBar createPerfoToolBar() {
		JToolBar tb = new JToolBar("Perfo");
		addVisibilityLayerButtons(tb);
		JButton createOptionButton = createOptionButton();
		tb.add(createOptionButton);
		addPerfoButtonsInToolBar(tb);
		return tb;
	}

	private JToolBar createExportDXFToolBar() {
		JToolBar tb = new JToolBar("Dxf");

		savedxf = new JButton("exporter en DXF");
		savedxf.setIcon(new ImageIcon(getClass().getResource("misc.png")));
		savedxf.setToolTipText("Exporter le carton dans un fichier DXF pour per�age laser");
		savedxf.setActionCommand("SAVEDXF");
		savedxf.addActionListener(this);

		tb.add(savedxf);

		return tb;
	}

	public void removeButtons(JToolBar tb) {
		tb.remove(optimize);
		tb.remove(saveasxyu);
		tb.remove(savedxf);
		tb.remove(mi);
	}

	private JCheckBox perfo = null;

	public void addVisibilityLayerButtons(JToolBar tb) {
		perfo = new JCheckBox("Perforateur");
		perfo.setSelected(true);

		perfo.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox c = (JCheckBox) e.getSource();
				boolean checked = c.isSelected();

				issuesPunchLayer.setVisible(checked);
				resultPunchLayer.setVisible(checked);

				aprintref.repaint();
			}
		});

		tb.add(perfo);
	}

	public void removeVisibilityLayerButtons(JToolBar tb) {
		tb.remove(perfo);

	}

	private Repository rep = null;

	public void informRepository(Repository repository) {
		this.rep = repository;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.gui.aprint.extensionspoints.ImportersExtensionPoint
	 * #getExtensionImporterInstance(fr.freydierepatrice.scale.Scale)
	 */
	public ArrayList<AbstractMidiImporter> getExtensionImporterInstance(
			Scale destinationscale) {

		logger.debug("getExtensionImporterInstance");

		// get midi correspondance ...
		if (rep == null) {
			logger.warn("Rep is null ...");
			return null;
		}

		Scale gmidi = Scale.getGammeMidiInstance();
		ArrayList<AbstractTransformation> trans = rep.getTranspositionManager()
				.findTransposition(gmidi, destinationscale);

		if (trans == null || trans.size() == 0) {
			logger.warn("no midi transposition for " + destinationscale);
			return null;
		}

		ArrayList<AbstractMidiImporter> l = new ArrayList<AbstractMidiImporter>();

		for (Iterator<AbstractTransformation> iterator = trans.iterator(); iterator
				.hasNext();) {
			AbstractTransformation abstractTransformation = iterator.next();

			if (abstractTransformation instanceof LinearTransposition) {
				LinearTransposition lt = (LinearTransposition) abstractTransformation;
				l.add(new PerfoMidiImporter(getName(), destinationscale, lt,
						parameters.poinconsize));
			}
		}

		return l;
	}

}
