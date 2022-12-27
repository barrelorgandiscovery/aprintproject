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
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprint.PrintPreview;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.VFSTools;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.virtualbook.transformation.LinearTransposition;
import org.barrelorgandiscovery.virtualbook.transformation.TranspositionIO;

/**
 * @author Patrice Freydiere
 */
public class ScaleEditor extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8484645796626431335L;

	private static Logger logger = Logger.getLogger(ScaleEditor.class);

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
	public ScaleEditor(ScaleEditorPrefs prefs, Repository2 repository) throws Exception {
		super(Messages.getString("GammeEditor.0")); //$NON-NLS-1$
		assert prefs != null;
		this.prefs = prefs;

		assert repository != null;
		this.repository = repository;

		initComponents();

	}

	/**
	 * Constructeur, utilisant une fenetre modale
	 * 
	 * @param owner
	 */
	public ScaleEditor(Frame owner, ScaleEditorPrefs prefs, Repository2 repository) throws Exception {
		this(prefs, repository);

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
				JMenuItem newScale = new JMenuItem(Messages.getString("ScaleEditor.13")); //$NON-NLS-1$
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

				menuFile.addSeparator();

				JMenuItem exportAsImage = menuFile.add(Messages.getString("ScaleEditor.15")); //$NON-NLS-1$
				exportAsImage.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {

							BufferedImage scalePicture = ScaleComponent.createScaleImage(scaleEditorPanel.getScale());

							APrintFileChooser fileChooser = new APrintFileChooser();
							fileChooser.setFileFilter(
									new VFSFileNameExtensionFilter(Messages.getString("ScaleEditor.16"), "png")); //$NON-NLS-1$ //$NON-NLS-2$
							fileChooser.setMultiSelectionEnabled(false);

							if (fileChooser.showSaveDialog(ScaleEditor.this) == APrintFileChooser.APPROVE_OPTION) {
								AbstractFileObject f = fileChooser.getSelectedFile();

								String filename = f.getName().getBaseName();

								if (!filename.endsWith(".png")) { //$NON-NLS-1$
									f = (AbstractFileObject) f.getFileSystem()
											.resolveFile(f.getName().toString() + ".png"); //$NON-NLS-1$
								}

								logger.debug("saving file :" //$NON-NLS-1$
										+ f.getName().toString());

								OutputStream fileOutputStream = VFSTools.transactionalWrite(f);
								try {
									ImageIO.write(scalePicture, "PNG", fileOutputStream); //$NON-NLS-1$
								} finally {
									fileOutputStream.close();
								}
								JMessageBox.showMessage(ScaleEditor.this,
										Messages.getString("ScaleEditor.22") + f.getName().toString() //$NON-NLS-1$
												+ Messages.getString("ScaleEditor.23")); //$NON-NLS-1$
							}

						} catch (Exception ex) {
							logger.error("error in saving image of the scale ...", //$NON-NLS-1$
									ex);
							BugReporter.sendBugReport();

							JMessageBox.showMessage(ScaleEditor.this, Messages.getString("ScaleEditor.25") //$NON-NLS-1$
									+ ex.getMessage());
						}
					}
				});

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
						JMessageBox.showMessage(ScaleEditor.this, Messages.getString("GammeEditor.16")); //$NON-NLS-1$
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

	private JScaleEditorPanel scaleEditorPanel;

	/**
	 * empty the current scale
	 */
	private void newScale() {
		try {

			scaleEditorPanel.newScale();

		} catch (Exception ex) {
			logger.error("newScale", ex); //$NON-NLS-1$
			JMessageBox.showMessage(this, Messages.getString("ScaleEditor.14") + ex.getMessage()); //$NON-NLS-1$
			BugReporter.sendBugReport();
		}
	}

	/**
	 * open a scale from the repository
	 */
	private void openGamme() {
		try {

			// liste des gammes du repository ...

			ScaleChooserDialog d = new ScaleChooserDialog(repository, this, true);
			d.setVisible(true);

			Scale returnscale = d.getSelectedScale();

			if (returnscale != null) {
				lastLoadedOrSavedScale = returnscale;
				loadGamme(returnscale);
			}

		} catch (Exception ex) {
			logger.error("openGamme", ex); //$NON-NLS-1$
			JMessageBox.showMessage(this, Messages.getString("ScaleEditor.6") //$NON-NLS-1$
					+ ex.getMessage());
			BugReporter.sendBugReport();
		}
	}

	private void saveGamme() {
		try {

			logger.debug("saveGamme"); //$NON-NLS-1$

			// save the scale without any user feedbacks
			logger.debug("save new scale ... "); //$NON-NLS-1$

			Scale newscaletosave = scaleEditorPanel.getScale();

			repository.saveScale(newscaletosave);

			logger.debug("save default transposition"); //$NON-NLS-1$
			LinearTransposition t = TranspositionIO.createDefaultMidiTransposition(newscaletosave);

			repository.saveTransformation(t);

			logger.debug("default transposition saved ..."); //$NON-NLS-1$

			lastLoadedOrSavedScale = newscaletosave;

			logger.debug("save done"); //$NON-NLS-1$

		} catch (Exception ex) {
			logger.error("saveGamme", ex); //$NON-NLS-1$
			JMessageBox.showMessage(this, Messages.getString("ScaleEditor.12") //$NON-NLS-1$
					+ ex.getMessage());
			BugReporter.sendBugReport();
		}

	
	}

	private void rememberDefaultFolderForFile(File selectedFile) {
		File new_default_folder = selectedFile.getParentFile();
		prefs.setLastGammeFolder(new_default_folder);
	}

	private void setupDefaultFolderForChooser(APrintFileChooser choose) {
		File default_folder = prefs.getLastGammeFolder();
		if (default_folder != null && default_folder.exists() && default_folder.isDirectory())
			choose.setCurrentDirectory(default_folder);
	}

	private void previewGamme() {
		try {

			logger.debug("scal check ..."); //$NON-NLS-1$

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
			logger.debug("scale check ..."); //$NON-NLS-1$
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
