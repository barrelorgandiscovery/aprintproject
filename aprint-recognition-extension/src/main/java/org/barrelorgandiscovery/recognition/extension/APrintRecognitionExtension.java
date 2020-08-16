package org.barrelorgandiscovery.recognition.extension;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.instrumentchoice.IInstrumentChoiceListener;
import org.barrelorgandiscovery.gui.aprint.instrumentchoice.JCoverFlowInstrumentChoice;
import org.barrelorgandiscovery.gui.aprint.instrumentchoice.JInstrumentChoice;
import org.barrelorgandiscovery.gui.aprint.instrumentchoice.shelf.JInstrumentChoiceShelf;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.WelcomeExtensionExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.helper.BaseExtension;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.prefs.DummyPrefsStorage;
import org.barrelorgandiscovery.recognition.RecognitionProject;
import org.barrelorgandiscovery.recognition.gui.JRecognitionProjectWindow;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

import com.jeta.forms.project.ProjectManager;
import com.l2fprod.common.swing.JDirectoryChooser;
import com.l2fprod.common.swing.plaf.DirectoryChooserUI;

public class APrintRecognitionExtension extends BaseExtension {

	private static final String PREF_FOLDER = "folder";
	private static Logger logger = Logger
			.getLogger(APrintRecognitionExtension.class);

	public APrintRecognitionExtension() throws Exception {
		super();
		this.defaultAboutAuthor = "Patrice Freydiere";
		this.defaultAboutVersion = "0.5_beta";
	}

	@Override
	public String getName() {
		return "APrint Recognition Extension";
	}

	@Override
	protected void setupExtensionPoint(List<ExtensionPoint> initExtensionPoints)
			throws Exception {
		super.setupExtensionPoint(initExtensionPoints);

		WelcomeExtensionExtensionPoint wbe = new WelcomeExtensionExtensionPoint() {

			public void addComponentInPanel(JPanel p) {
				p.add(createNewProjectButton());
				p.add(createOpenProjectButton());
			}
		};

		initExtensionPoints.add(createExtensionPoint(
				WelcomeExtensionExtensionPoint.class, wbe));

	}

	private JButton createNewProjectButton() {
		JButton btn = new JButton("New Recognition Project ...");
		btn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					newProject();
				} catch (Throwable t) {
					logger.error(
							"error creating new project :" + t.getMessage(), t);
					BugReporter.sendBugReport();
					JMessageBox.showError(
							APrintRecognitionExtension.this.application
									.getOwnerForDialog(), t);
				}
			}
		});
		return btn;
	}

	private JButton createOpenProjectButton() {
		JButton btn = new JButton("Open Recognition Project ...");
		btn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					openProject();
				} catch (Throwable t) {
					logger.error(
							"error creating new project :" + t.getMessage(), t);
					BugReporter.sendBugReport();
					JMessageBox.showError(
							APrintRecognitionExtension.this.application
									.getOwnerForDialog(), t);
				}
			}
		});
		return btn;
	}

	private void openProject() throws Exception {

		// ask for the folder + instrument

		final JDirectoryChooser dc = new JDirectoryChooser();

		File fPrefs = this.extensionPreferences.getFileProperty(PREF_FOLDER,
				null);

		dc.setSelectedFile(fPrefs);
		dc.setDialogTitle("Choisissez le répertoire contenant les photos ...");
		if (dc.showOpenDialog((Component) application.getOwnerForDialog()) == JDirectoryChooser.APPROVE_OPTION) {
			this.extensionPreferences.setFileProperty(PREF_FOLDER,
					dc.getSelectedFile());

			logger.debug("directory :" + dc.getSelectedFile().getAbsolutePath());
			RecognitionProject p = new RecognitionProject(dc.getSelectedFile(),
					application.getRepository(), application.getProperties()
							.getAprintFolder());

			JRecognitionProjectWindow rpw = new JRecognitionProjectWindow(
					new DummyPrefsStorage(), p);

			rpw.setSize(800, 600);
			rpw.setVisible(true);

		}

	}

	private void newProject() throws Exception {

		// ask for the folder + instrument

		final JDirectoryChooser dc = new JDirectoryChooser();

		File fPrefs = this.extensionPreferences.getFileProperty(PREF_FOLDER,
				null);

		dc.setSelectedFile(fPrefs);
		dc.setDialogTitle("Choisissez le répertoire contenant les photos ...");
		if (dc.showOpenDialog((Component) application.getOwnerForDialog()) == JDirectoryChooser.APPROVE_OPTION) {
			this.extensionPreferences.setFileProperty(PREF_FOLDER,
					dc.getSelectedFile());
			final JDialog d = new JDialog(
					(Frame) application.getOwnerForDialog());
			Container cp = d.getContentPane();
			cp.setLayout(new BorderLayout());

			final JCoverFlowInstrumentChoice ic = new JCoverFlowInstrumentChoice(
					application.getRepository(),
					new IInstrumentChoiceListener() {

						public void instrumentChanged(Instrument newInstrument) {
							// TODO Auto-generated method stub

						}
					}

			);
			cp.add(ic, BorderLayout.CENTER);
			JButton ok = new JButton("OK");
			ok.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						d.dispose();
						logger.debug("directory :"
								+ dc.getSelectedFile().getAbsolutePath());
						RecognitionProject p = new RecognitionProject(dc
								.getSelectedFile(), ic.getCurrentInstrument(),
								application.getProperties().getAprintFolder());

						JRecognitionProjectWindow rpw = new JRecognitionProjectWindow(
								new DummyPrefsStorage(),
								p);

						rpw.setSize(800, 600);
						rpw.setVisible(true);

					} catch (Exception ex) {
						logger.error(
								"error in creating the project :"
										+ ex.getMessage(), ex);
						BugReporter.sendBugReport();
						JMessageBox.showError(application.getOwnerForDialog(),
								ex);
					}
				}
			});
			cp.add(ok, BorderLayout.SOUTH);

			d.setSize(800, 600);
			d.setVisible(true);
		}

	}
}
