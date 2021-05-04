package org.barrelorgandiscovery.gui.aedit;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.sound.midi.Sequence;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.editableinstrument.EditableInstrument;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManager;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository;
import org.barrelorgandiscovery.gui.aedit.markers.MarkerCreateTool;
import org.barrelorgandiscovery.gui.aedit.markers.MarkerLayer;
import org.barrelorgandiscovery.gui.ainstrument.SBRegistersPlay;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
import org.barrelorgandiscovery.listeningconverter.VirtualBookToMidiConverter;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Collection;
import org.barrelorgandiscovery.repository.Repository2Factory;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.virtualbook.Fragment;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.io.MidiIO;
import org.barrelorgandiscovery.virtualbook.transformation.LinearTransposition;
import org.barrelorgandiscovery.virtualbook.transformation.TranspositionResult;
import org.barrelorgandiscovery.virtualbook.transformation.Transpositor;

public class AEdit extends JFrame implements ActionListener {

	private static final Logger logger = Logger.getLogger(AEdit.class);

	private static final String PROPERTIESFILE = "aedit.properties";

	private static final String GAMMETRANSPOSITION = "gammetranspositionfolder";

	/**
	 * serial associé au composant
	 */
	private static final long serialVersionUID = 3156455473917085009L;

	private JMenuBar menubar = new JMenuBar();

	private JToolBar toolbar = new JToolBar();

	private JEditableVirtualBookComponent jcarton = new JEditableVirtualBookComponent();

	private JPanel mainpanel = new JPanel();

	// Constructeurs

	private Repository2 rep;

	/**
	 * a sound rendering for the play tool
	 */
	private SBRegistersPlay sbrp = null;

	public AEdit(Repository2 repository) throws Exception {
		super();

		assert repository != null;
		this.rep = repository;

		init();

		sbrp = new SBRegistersPlay();
		sbrp.open();

	}

	/**
	 * Initialisation de la fenetre
	 */
	private void init() throws Exception {

		logger.debug("Demarrage de l'application");

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		jcarton.setUseFastDrawing(true);

		// adding the layer for displaying the registers ...
		jcarton.addLayer(new RegistrationSectionLayer());

		jcarton.addLayer(new PipeSetGroupLayer());

		MarkerLayer ml = new MarkerLayer();
		ml.setVisible(true);
		jcarton.addLayer(ml);

		// remplissage des menus ...
		logger.debug("Remplissage des menus");
		JMenu menufichier = new JMenu("Fichier");
		menubar.add(menufichier);

		JMenuItem nouveau = new JMenuItem("Nouveau ...");
		nouveau.setActionCommand("NOUVEAU");
		nouveau.addActionListener(this);
		menufichier.add(nouveau);

		JMenuItem ouvrir = new JMenuItem("Ouvrir ...");
		ouvrir.setActionCommand("OUVRIR");
		ouvrir.addActionListener(this);
		menufichier.add(ouvrir);

		JMenuItem sauversous = new JMenuItem("Sauvegarder sous ...");
		sauversous.setActionCommand("SAUVERSOUS");
		sauversous.addActionListener(this);
		menufichier.add(sauversous);

		menufichier.addSeparator();

		JMenuItem itemimporter = new JMenuItem("Importer un fichier Midi ...");
		itemimporter.setActionCommand("IMPORTER_MIDI");
		itemimporter.addActionListener(this);
		menufichier.add(itemimporter);

		menufichier.addSeparator();

		JMenuItem quitter = new JMenuItem("Quitter");
		quitter.setActionCommand("QUITTER");
		quitter.addActionListener(this);
		menufichier.add(quitter);

		setJMenuBar(menubar);

		// remplissage des barres d'outils ...

		logger.debug("Remplissage de la barre d'outils");
		JButton btnselection = new JButton("Selection");
		btnselection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jcarton.setCurrentTool(new SelectionTool(jcarton));
			}
		});
		toolbar.add(btnselection);

		JButton btnplay = new JButton("Jouer");
		btnplay.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (jcarton.getVirtualBook() == null)
					return;

				Fragment s = jcarton.getBlockSelection();
				if (s != null) {
					ArrayList<Hole> notes = jcarton.getVirtualBook().findHoles(
							s.start, s.length);
					VirtualBook c = new VirtualBook(jcarton.getVirtualBook()
							.getScale(), notes);
					Sequence seq = null;
					try {
						c.shift(-c.getFirstHoleStart());

						VirtualBookToMidiConverter virtualBookToMidiConverter = new VirtualBookToMidiConverter(
								sbrp.getCurrentInstrumentMapping());

						seq = virtualBookToMidiConverter.convert(c);

					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
						JMessageBox.showMessage(AEdit.this,
								"Erreur lors de la transformation en ecoute");
						return;
					}

					try {
						sbrp.playSequence(seq);

					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
						JMessageBox.showMessage(AEdit.this,
								"Erreur lors du jeu de la sequence");
					}
				}

			}
		});

		toolbar.add(btnplay);

		JButton undoBtn = new JButton("Undo");
		undoBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				logger.debug("undo operation");
				jcarton.getUndoStack().undoLastOperation();

				jcarton.repaint();
			}
		});

		toolbar.add(undoBtn);

		JButton btncreation = new JButton("Modifier le carton");
		btncreation.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					CreationTool c = new CreationTool(jcarton, jcarton
							.getUndoStack(), jcarton.getSnappingEnvironment());
					jcarton.setCurrentTool(c);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}

		});

		toolbar.add(btncreation);

		JButton btnPlayLocated = new JButton("Play");
		btnPlayLocated.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PlayTool pt = new PlayTool(sbrp, jcarton);
				jcarton.setCurrentTool(pt);
			}
		});

		toolbar.add(btnPlayLocated);

		JButton btnCreateMarker = new JButton("Create Marker");
		btnCreateMarker.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					MarkerCreateTool cm = new MarkerCreateTool(jcarton);
					jcarton.setCurrentTool(cm);
				} catch (Exception ex) {
					logger.error(
							"error in creating cursor :" + ex.getMessage(), ex);
				}
			}
		});

		toolbar.add(btnCreateMarker);

		logger.debug("Construction de la fenetre");
		mainpanel.setLayout(new BorderLayout());

		mainpanel.add(toolbar, BorderLayout.NORTH);

		mainpanel.add(jcarton, BorderLayout.CENTER);

		getContentPane().add(mainpanel);

		setBounds(0, 0, 800, 600);

	}
	
	
	private void saveCarton(AbstractFileObject vfsFile) {
		try {
			OutputStream fos = vfsFile.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			oos.writeObject(jcarton.getVirtualBook());
		} catch (IOException ex) {
			JMessageBox.showMessage(this, ex.getMessage());
			logger.error(ex);
		}		
	}

	private void saveCarton(File fichier) {

		try {
			FileOutputStream fos = new FileOutputStream(fichier);
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			oos.writeObject(jcarton.getVirtualBook());
		} catch (IOException ex) {
			JMessageBox.showMessage(this, ex.getMessage());
			logger.error(ex);
		}

	}

	private void loadCarton(AbstractFileObject fichier) {
		try {
			InputStream fis = fichier.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(fis);

			VirtualBook c = (VirtualBook) ois.readObject();

			jcarton.setVirtualBook(c);
			jcarton.touchBook();
			jcarton.repaint();

		} catch (Exception ex) {
			JMessageBox.showMessage(this, ex.getMessage());
			logger.error(ex);
		}		
	}

	
	private void loadCarton(File fichier) {

		try {
			FileInputStream fis = new FileInputStream(fichier);
			ObjectInputStream ois = new ObjectInputStream(fis);

			VirtualBook c = (VirtualBook) ois.readObject();

			jcarton.setVirtualBook(c);
			jcarton.touchBook();
			jcarton.repaint();

		} catch (Exception ex) {
			JMessageBox.showMessage(this, ex.getMessage());
			logger.error(ex);
		}
	}

	// ////////////////////////////////////////////////////////
	// Traitement des actions du menu

	public void actionPerformed(ActionEvent e) {
		try {
			if ("QUITTER".equals(e.getActionCommand())) {
				System.exit(0);
			} else if ("IMPORTER_MIDI".equals(e.getActionCommand())) {
				try {
					importMidiFile();
				} catch (AEditException ex) {
					logger.error("ex");
					JMessageBox.showMessage(this, ex.getMessage());
				}
			} else if ("NOUVEAU".equals(e.getActionCommand())) {

				if (rep instanceof Repository2Collection) {

					Repository2Collection rc = (Repository2Collection) rep;
					for (int i = 0; i < rc.getRepositoryCount(); i++) {

						if (rc.getRepository(i) instanceof EditableInstrumentManagerRepository) {

							EditableInstrumentManagerRepository repm = (EditableInstrumentManagerRepository) rc
									.getRepository(i);
							EditableInstrumentManager editableInstrumentManager = repm
									.getEditableInstrumentManager();

							String[] listEditableInstruments = editableInstrumentManager
									.listEditableInstruments();

							String ret = (String) JOptionPane.showInputDialog(
									this, "Choix de l'instrument",
									"Choix Instrument",
									JOptionPane.QUESTION_MESSAGE, null,
									listEditableInstruments, null);

							EditableInstrument ins = (EditableInstrument) editableInstrumentManager
									.loadEditableInstrument(ret);

							sbrp.changeInstrument(ins);
							VirtualBook vb = new VirtualBook(ins.getScale());

							jcarton.setVirtualBook(vb);
							jcarton.touchBook();
							repaint();

							break;
						}
					}
				}

			} else if ("OUVRIR".equals(e.getActionCommand())) {
				APrintFileChooser choose = new APrintFileChooser();
				choose.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

				choose.setFileFilter(new VFSFileNameExtensionFilter(
						"Carton virtuel", "cv"));
				if (choose.showOpenDialog(this) == APrintFileChooser.APPROVE_OPTION) {
					loadCarton(choose.getSelectedFile());
				}

			} else if ("SAUVERSOUS".equals(e.getActionCommand())) {
				APrintFileChooser choose = new APrintFileChooser();
				choose.setFileSelectionMode(APrintFileChooser.FILES_ONLY);
				choose.setFileFilter(new VFSFileNameExtensionFilter(
						"Carton virtuel", "cv"));
				if (choose.showSaveDialog(this) == APrintFileChooser.APPROVE_OPTION) {
					saveCarton(choose.getSelectedFile());
				}
			}
		} catch (Exception ex) {
			logger.error("Error in ActionPerformed " + ex.getMessage(), ex);
		}
	}

	/**
	 * Cette fonction déclenche l'import du fichier midi dans le progiciel
	 * 
	 */
	private void importMidiFile() throws AEditException {

		// choix du fichier midi
		APrintFileChooser choose = new APrintFileChooser();
		choose.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

		choose.setFileFilter(new VFSFileNameExtensionFilter("Midi File", "mid"));

		if (choose.showOpenDialog(this) == APrintFileChooser.APPROVE_OPTION) {
			// Récupération du nom de fichier
			AbstractFileObject result = choose.getSelectedFile();

			if (result != null) {
				// Chargement du carton virtuel ..

				VirtualBook c;
				try {
					FileName filename = result.getName();
					c = MidiIO.readCarton(result.getInputStream(), filename.getBaseName());
				} catch (Exception ex) {
					throw new AEditException(ex);
				}
				// on charge les transpositions et les gammes ...
				JChoixGammeEtTransposition j = new JChoixGammeEtTransposition(
						rep, rep);

				j.setModal(true);
				j.setVisible(true);

				LinearTransposition t = j.getTransposition();

				TranspositionResult tr = Transpositor.transpose(c, t);

				jcarton.setVirtualBook(tr.virtualbook);

			}

		}

	}

	// ///////////////////////////////////////////////////////////////////////
	// Gestion des propriétés associée à l'application

	/**
	 * Cette méthode lit une propriété associée au programme
	 * 
	 * @param propertyname
	 *            le nom de la propriété
	 * @return
	 */
	private String readProperty(String propertyname) {

		String userfolder = System.getProperty("user.home");
		if (userfolder == null || !new File(userfolder).exists()) {
			logger.error("le repertoire de l'utilisateur n'a pas été trouvé");
			return null;
		}

		// existance du fichier aprint.property ?
		File propfile = new File(new File(userfolder), PROPERTIESFILE);

		if (!propfile.exists()) {
			logger.error("impossible de trouver le fichier de propriétés");
			return null;
		}

		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(propfile));
		} catch (IOException ex) {
			logger.error("error while loading " + PROPERTIESFILE);
			return null;
		}

		// lecture de la propriété

		return prop.getProperty(propertyname);
	}

	private void writeProperty(String propertyname, String property)
			throws AEditException {

		String userfolder = System.getProperty("user.home");
		if (userfolder == null || !new File(userfolder).exists()) {
			logger.error("le repertoire de l'utilisateur n'a pas été trouvé");
			throw new AEditException(
					"le repertoire de l'utilisateur user.home n'a pas été trouvé");
		}

		// existance du fichier aprint.property ?
		File propfile = new File(new File(userfolder), PROPERTIESFILE);

		Properties prop = new Properties();

		if (propfile.exists()) {
			try {
				prop.load(new FileInputStream(propfile));
			} catch (IOException ex) {
				logger.error("error while loading " + PROPERTIESFILE);
				throw new AEditException(ex);
			}
		}

		prop.setProperty(propertyname, property);

		// Ecriture du fichier ...
		try {
			prop.store(new FileOutputStream(propfile), null);
		} catch (Exception ex) {
			logger.error(ex);
			throw new AEditException(ex);
		}
	}

	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		Properties p = new Properties();
		p.setProperty("repositorytype", "folder");
		p.setProperty("folder", "C:\\Users\\use\\aprintstudio\\private");

		APrintProperties props = new APrintProperties(true);
		AEdit e = new AEdit(Repository2Factory.create(p, props));

		e.setVisible(true);
	}

}
