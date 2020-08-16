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
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.WelcomeExtensionExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.helper.BaseExtension;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.prefs.DummyPrefsStorage;
import org.barrelorgandiscovery.recognition.RecognitionProject;
import org.barrelorgandiscovery.recognition.gui.JRecognitionProjectWindow;
import org.barrelorgandiscovery.recognition.messages.Messages;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

import com.l2fprod.common.swing.JDirectoryChooser;

public class APrintRecognitionExtension extends BaseExtension {

	private static final String PREF_FOLDER = "folder"; //$NON-NLS-1$
	private static Logger logger = Logger
			.getLogger(APrintRecognitionExtension.class);

	public APrintRecognitionExtension() throws Exception {
		super();
		this.defaultAboutAuthor = "Patrice Freydiere"; //$NON-NLS-1$
		this.defaultAboutVersion = "0.5_beta"; //$NON-NLS-1$
	}

	@Override
	public String getName() {
		return Messages.getString("APrintRecognitionExtension.3"); //$NON-NLS-1$
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
		JButton btn = new JButton(Messages.getString("APrintRecognitionExtension.4")); //$NON-NLS-1$
		btn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					newProject();
				} catch (Throwable t) {
					logger.error(
							"error creating new project :" + t.getMessage(), t); //$NON-NLS-1$
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
		JButton btn = new JButton(Messages.getString("APrintRecognitionExtension.6")); //$NON-NLS-1$
		btn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					openProject();
				} catch (Throwable t) {
					logger.error(
							"error creating new project :" + t.getMessage(), t); //$NON-NLS-1$
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
		dc.setDialogTitle(Messages.getString("APrintRecognitionExtension.8")); //$NON-NLS-1$
		if (dc.showOpenDialog((Component) application.getOwnerForDialog()) == JDirectoryChooser.APPROVE_OPTION) {
			this.extensionPreferences.setFileProperty(PREF_FOLDER,
					dc.getSelectedFile());

			logger.debug(Messages.getString("APrintRecognitionExtension.9") + dc.getSelectedFile().getAbsolutePath()); //$NON-NLS-1$
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
		dc.setDialogTitle(Messages.getString("APrintRecognitionExtension.10")); //$NON-NLS-1$
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
					
						}
					}

			);
			cp.add(ic, BorderLayout.CENTER);
			JButton ok = new JButton(Messages.getString("APrintRecognitionExtension.11")); //$NON-NLS-1$
			ok.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						d.dispose();
						logger.debug("directory :" //$NON-NLS-1$
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
								"error in creating the project :" //$NON-NLS-1$
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
