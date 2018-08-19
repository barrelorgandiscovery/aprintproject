package org.barrelorgandiscovery.gui.ascale;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FilenameFilter;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprint.PrintPreview;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.importer.MidiBoekGammeImporter;
import org.barrelorgandiscovery.scale.io.ScaleIO;
import org.barrelorgandiscovery.tools.FileNameExtensionFilter;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.virtualbook.transformation.LinearTransposition;
import org.barrelorgandiscovery.virtualbook.transformation.TranspositionIO;

/**
 * Standalone scale editor, for file based scales
 * 
 * @author Patrice Freydiere
 */
public class StandAloneScaleEditor extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8484645796626431335L;

	private static Logger logger = Logger
			.getLogger(StandAloneScaleEditor.class);

	private JMenuBar barredemenu;
	private JMenu menuFile;
	private JMenuItem menuOpenScale;
	private JMenuItem menuSave;
	private JMenuItem menuSaveAs;
	private JMenuItem menuQuitter;
	private JMenuItem menuImprimer;
	private JMenuItem menuPreview;

	private JPanel hSpacer1;
	private JMenu itemaide;
	private JMenuItem menuAideEnLigne;

	private ScaleEditorPrefs prefs = null;

	private Repository2 repository;

	/**
	 * Constructeur
	 */
	public StandAloneScaleEditor(ScaleEditorPrefs prefs) throws Exception {
		super(Messages.getString("GammeEditor.0")); //$NON-NLS-1$
		assert prefs != null;
		this.prefs = prefs;

		initComponents();

	}

	/**
	 * Constructeur, utilisant une fenetre modale
	 * 
	 * @param owner
	 */
	public StandAloneScaleEditor(Frame owner, ScaleEditorPrefs prefs)
			throws Exception {
		this(prefs);

		if (owner != null) {
			Image image = owner.getIconImage();
			setIconImage(image);
		}
	}

	/**
	 * Initialisation de composants
	 */
	private void initComponents() throws Exception {

		barredemenu = new JMenuBar();
		menuFile = new JMenu();
		menuOpenScale = new JMenuItem();
		menuSave = new JMenuItem();
		menuSaveAs = new JMenuItem();
		menuQuitter = new JMenuItem();
		menuImprimer = new JMenuItem();
		menuPreview = new JMenuItem();
		hSpacer1 = new JPanel(null);
		itemaide = new JMenu();
		menuAideEnLigne = new JMenuItem();

		scaleEditorPanel = new JScaleEditorPanel(this, this.prefs);

		// ======== menuBar1 ========
		{

			// ======== menuFichier ========
			{
				menuFile.setText(Messages.getString("GammeEditor.7")); //$NON-NLS-1$

				//
				JMenuItem newScale = new JMenuItem(Messages
						.getString("ScaleEditor.13")); //$NON-NLS-1$
				newScale.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						newScale();
					}
				});

				menuFile.add(newScale);

				// ---- menuOuvrirGamme ----
				menuOpenScale.setText(Messages.getString("GammeEditor.8")); //$NON-NLS-1$
				menuFile.add(menuOpenScale);

				menuOpenScale.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						openGamme();
					}
				});

				menuFile.addSeparator();

				// ---- menuItem2 ----
				menuSave.setText(Messages.getString("GammeEditor.9")); //$NON-NLS-1$
				menuSave.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						saveGamme();
					}
				});

				menuFile.add(menuSave);

				menuSaveAs.setText(Messages.getString("APrintNGVirtualBookInternalFrame.2103")); //$NON-NLS-1$
				menuSaveAs.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						saveAsGamme();
					}
				});

				menuFile.add(menuSaveAs);

				menuFile.addSeparator();

				JMenuItem exportAsImage = menuFile.add(Messages
						.getString("ScaleEditor.15")); //$NON-NLS-1$
				exportAsImage.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {

							BufferedImage scalePicture = ScaleComponent
									.createScaleImage(scaleEditorPanel
											.getScale());

							JFileChooser fileChooser = new JFileChooser();
							fileChooser
									.setFileFilter(new FileNameExtensionFilter(
											Messages
													.getString("ScaleEditor.16"), "png")); //$NON-NLS-1$ //$NON-NLS-2$
							fileChooser.setMultiSelectionEnabled(false);

							if (fileChooser
									.showSaveDialog(StandAloneScaleEditor.this) == JFileChooser.APPROVE_OPTION) {
								File f = fileChooser.getSelectedFile();
								if (!f.getName().endsWith(".png")) { //$NON-NLS-1$
									f = new File(f.getParentFile(), f.getName()
											+ ".png"); //$NON-NLS-1$
								}

								logger.debug("saving file :" //$NON-NLS-1$
										+ f.getAbsolutePath());

								ImageIO.write(scalePicture, "PNG", f); //$NON-NLS-1$

								JMessageBox
										.showMessage(
												StandAloneScaleEditor.this,
												Messages
														.getString("ScaleEditor.22") + f.getAbsolutePath() //$NON-NLS-1$
														+ Messages
																.getString("ScaleEditor.23")); //$NON-NLS-1$
							}

						} catch (Exception ex) {
							logger.error(
									"error in saving image of the scale ...", //$NON-NLS-1$
									ex);
							BugReporter.sendBugReport();

							JMessageBox.showMessage(StandAloneScaleEditor.this,
									Messages.getString("ScaleEditor.25") //$NON-NLS-1$
											+ ex.getMessage());
						}
					}
				});

				menuFile.addSeparator();

				JMenuItem importFromMidiBoek = new JMenuItem(
						Messages.getString("StandAloneScaleEditor.100")); //$NON-NLS-1$
				importFromMidiBoek.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						importFromMidiBoek();
					}
				});

				menuFile.add(importFromMidiBoek);
				menuFile.addSeparator();

				// ---- impression ----

				menuPreview.setText(Messages.getString("GammeEditor.11")); //$NON-NLS-1$
				menuPreview.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						previewGamme();
					}
				});

				menuFile.add(menuPreview);

				menuImprimer.setText(Messages.getString("GammeEditor.12")); //$NON-NLS-1$
				menuImprimer.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						printGamme();
					}
				});

				menuFile.add(menuImprimer);

				menuFile.addSeparator();

				// ---- menuQuitter ----
				menuQuitter.setText(Messages.getString("GammeEditor.13")); //$NON-NLS-1$
				menuFile.add(menuQuitter);
				menuQuitter.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});

			}
			barredemenu.add(menuFile);
			barredemenu.add(hSpacer1);

			// ======== menu1 ========
			{
				itemaide.setText(Messages.getString("GammeEditor.14")); //$NON-NLS-1$

				// ---- menuItem3 ----
				menuAideEnLigne.setText(Messages.getString("GammeEditor.15")); //$NON-NLS-1$
				menuAideEnLigne.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JMessageBox.showMessage(StandAloneScaleEditor.this,
								Messages.getString("GammeEditor.16")); //$NON-NLS-1$
					}
				});
				itemaide.add(menuAideEnLigne);
			}
			barredemenu.add(itemaide);
		}
		setJMenuBar(barredemenu);

		// pack();

		// setLocationRelativeTo(getOwner());

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(this.scaleEditorPanel, BorderLayout.CENTER);

		setSize(new Dimension(1024, 768));

	}

	/**
	 * This member remember the opened scale to ask the user their wishes.
	 */
	private Scale lastLoadedOrSavedScale = null;
	private File lastLoadedOrSavedFile = null;

	private JScaleEditorPanel scaleEditorPanel;

	/**
	 * empty the current scale
	 */
	private void newScale() {
		try {

			scaleEditorPanel.newScale();

		} catch (Exception ex) {
			logger.error("newScale", ex); //$NON-NLS-1$
			JMessageBox.showMessage(this,
					Messages.getString("ScaleEditor.14") + ex.getMessage()); //$NON-NLS-1$
			BugReporter.sendBugReport();
		}
	}

	/**
	 * open a scale from the repository
	 */
	private void openGamme() {
		try {

			// liste des gammes du repository ...

			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new FileNameExtensionFilter("Scale File", //$NON-NLS-1$
					ScaleIO.SCALE_FILE_EXTENSION));

			setupDefaultFolderForChooser(fileChooser);

			if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
				return;

			File selectedFile = fileChooser.getSelectedFile();
			if (selectedFile == null)
				return;

			Scale returnscale = ScaleIO.readGamme(selectedFile);

			if (returnscale != null) {
				lastLoadedOrSavedScale = returnscale;
				loadGamme(returnscale);
				lastLoadedOrSavedFile = selectedFile;
			}

		} catch (Exception ex) {
			logger.error("openGamme", ex); //$NON-NLS-1$
			JMessageBox.showMessage(this, Messages.getString("ScaleEditor.6") //$NON-NLS-1$
					+ ex.getMessage());
			BugReporter.sendBugReport();
		}
	}

	private void saveAsGamme() {
		try {

			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new FileNameExtensionFilter("Scale File", //$NON-NLS-1$
					ScaleIO.SCALE_FILE_EXTENSION));

			setupDefaultFolderForChooser(fileChooser);

			if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
				return;

			File choosedFile = fileChooser.getSelectedFile();
			if (choosedFile == null)
				return;

			if (!choosedFile.getName().endsWith("." + ScaleIO.SCALE_FILE_EXTENSION)) { //$NON-NLS-1$
				logger.debug("adjust the filename with scale"); //$NON-NLS-1$
				choosedFile = new File(choosedFile.getParentFile(), choosedFile
						.getName()
						+ "." + ScaleIO.SCALE_FILE_EXTENSION); //$NON-NLS-1$
			}
			logger.debug("vérification de la gamme ..."); //$NON-NLS-1$
			String checkGamme = scaleEditorPanel.checkScale();
			if (checkGamme != null) {
				JMessageBox.showMessage(this, checkGamme);
				return; // fin ..
			}

			Scale g = scaleEditorPanel.getScale();

			ScaleIO.writeGamme(g, choosedFile);

			JMessageBox.showMessage(this, Messages.getString("GammeEditor.31") //$NON-NLS-1$
					//$NON-NLS-1$
					+ choosedFile.getName()
					+ Messages.getString("GammeEditor.32")); //$NON-NLS-1$

			lastLoadedOrSavedFile = choosedFile;
			rememberDefaultFolderForFile(lastLoadedOrSavedFile);

		} catch (Exception ex) {

			JMessageBox.showMessage(this, Messages.getString("GammeEditor.31") //$NON-NLS-1$
			//$NON-NLS-1$
					+ Messages.getString("GammeEditor.32")); //$NON-NLS-1$

			logger.error("saveAsGamme :" + ex.getMessage(), ex); //$NON-NLS-1$
		}
	}

	private void saveGamme() {

		try {

			// s'il n'y a pas de fichier courant .. saveas ..
			if (lastLoadedOrSavedFile == null) {
				saveAsGamme();
				return;
			}

			logger.debug("vérification de la gamme ..."); //$NON-NLS-1$
			String checkGamme = scaleEditorPanel.checkScale();
			if (checkGamme != null) {
				JMessageBox.showMessage(this, checkGamme);
				return; // fin ..
			}

			Scale g = scaleEditorPanel.getScale();

			ScaleIO.writeGamme(g, lastLoadedOrSavedFile);

			JMessageBox.showMessage(this, Messages.getString("GammeEditor.31") //$NON-NLS-1$
					//$NON-NLS-1$
					+ lastLoadedOrSavedFile.getName()
					+ Messages.getString("GammeEditor.32")); //$NON-NLS-1$

		} catch (Exception ex) {
			logger.error("saveGamme", ex); //$NON-NLS-1$
		}
	}

	private void importFromMidiBoek() {
		try {
			logger.debug("import From MidiBoek"); //$NON-NLS-1$

			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new FileNameExtensionFilter(
					Messages.getString("StandAloneScaleEditor.101"), "gam")); //$NON-NLS-1$ //$NON-NLS-2$

			setupDefaultFolderForChooser(fileChooser);

			if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
				return;

			File choosedFile = fileChooser.getSelectedFile();
			if (choosedFile == null)
				return;

			Scale importScale = MidiBoekGammeImporter.importScale(choosedFile);

			lastLoadedOrSavedScale = importScale;
			loadGamme(importScale);

		} catch (Exception ex) {
			JMessageBox.showError(this, ex);
			logger.error("importFromMidiBoek", ex); //$NON-NLS-1$
		}
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

	private void previewGamme() {
		try {

			logger.debug("vérification de la gamme ..."); //$NON-NLS-1$

			String checkGamme = scaleEditorPanel.checkScale();

			if (checkGamme != null) {
				JMessageBox.showMessage(this, checkGamme);
				return; // fin ..
			}

			Scale g = scaleEditorPanel.getScale();

			// prévisualisation de la gamme ...
			new PrintPreview(new ScalePrintDocument(g));

		} catch (Exception ex) {
			logger.error("saveGamme", ex); //$NON-NLS-1$
		}
	}

	private void printGamme() {
		try {
			logger.debug("vérification de la gamme ..."); //$NON-NLS-1$
			String checkGamme = scaleEditorPanel.checkScale();
			if (checkGamme != null) {
				JMessageBox.showMessage(this, checkGamme);
				return; // fin ..
			}
			Scale g = scaleEditorPanel.getScale();

			ScalePrintDocument d = new ScalePrintDocument(g);

			PrinterJob pjob = PrinterJob.getPrinterJob();

			pjob.setPrintable(d, pjob.pageDialog(pjob.defaultPage()));

			// Affiche la boite de dialogue standard
			if (pjob.printDialog()) {
				pjob.print();
			}
		} catch (Exception ex) {
			logger.error("printGamme", ex); //$NON-NLS-1$
		}
	}

	/**
	 * Chargement d'une gamme
	 * 
	 * @param g
	 */
	public void loadGamme(Scale g) {

		scaleEditorPanel.loadScale(g);

	}

}
