package org.barrelorgandiscovery.recognition.extension;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.aprintng.APrintNGInternalFrame;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.WelcomeExtensionExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.helper.BaseExtension;
import org.barrelorgandiscovery.recognition.gui.books.JBookRecognition;
import org.barrelorgandiscovery.recognition.gui.disks.JDiskRecognition;
import org.barrelorgandiscovery.recognition.messages.Messages;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.VersionTools;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

/**
 * extension for recognizing disks, and export to DXF
 * 
 * @author pfreydiere
 * 
 */
public class APrintDiskRecognitionExtension extends BaseExtension {

	private static final String CURRENT_DIRECTORY_PREF = "currentDirectory"; //$NON-NLS-1$
	private static Logger logger = Logger.getLogger(APrintDiskRecognitionExtension.class);

	public APrintDiskRecognitionExtension() throws Exception {
		super();

		this.defaultAboutAuthor = "Patrice Freydiere"; //$NON-NLS-1$
		this.defaultAboutVersion = "0.5_beta"; //$NON-NLS-1$
		
		if (VersionTools.getVersion().compareToIgnoreCase("2017.") < 0 ) { //$NON-NLS-1$
			throw new Exception("this extension cannot be run on an old version of APrint, 2016 minimum is required"); //$NON-NLS-1$
		}
	}

	@Override
	public String getName() {
		return "APrint Disks Recognition Extension"; //$NON-NLS-1$
	}

	@Override
	protected void setupExtensionPoint(List<ExtensionPoint> initExtensionPoints) throws Exception {
		super.setupExtensionPoint(initExtensionPoints);

		// add a recognition button in the front panel
		WelcomeExtensionExtensionPoint wbe = new WelcomeExtensionExtensionPoint() {
			public void addComponentInPanel(JPanel p) {
				p.add(createDiskRecognitionButton());
			}
		};

		initExtensionPoints.add(createExtensionPoint(WelcomeExtensionExtensionPoint.class, wbe));

		WelcomeExtensionExtensionPoint wbebook = new WelcomeExtensionExtensionPoint() {
			@Override
			public void addComponentInPanel(JPanel p) {
				p.add(createBookRecognitionButton());
			}
		};

		initExtensionPoints.add(createExtensionPoint(WelcomeExtensionExtensionPoint.class, wbebook));

	}

	private JButton createBookRecognitionButton() {
		JButton btn = new JButton(Messages.getString("APrintDiskRecognitionExtension.6"));  //$NON-NLS-1$
		btn.setIcon(new ImageIcon(getClass().getResource("imagebook.png")));//$NON-NLS-1$
		btn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					startRecognizeBook();
				} catch (Throwable t) {
					logger.error("error creating new project :" + t.getMessage(), t); //$NON-NLS-1$
					BugReporter.sendBugReport();
					JMessageBox.showError(APrintDiskRecognitionExtension.this.application.getOwnerForDialog(), t);
				}
			}
		});
		return btn;
	}

	private JButton createDiskRecognitionButton() {
		JButton btn = new JButton(Messages.getString("APrintDiskRecognitionExtension.4")); //$NON-NLS-1$
		btn.setIcon(new ImageIcon(getClass().getResource("imagedisqueariston.png"))); //$NON-NLS-1$
		btn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {

					startRecognize();

				} catch (Throwable t) {
					logger.error("error creating new project :" + t.getMessage(), t); //$NON-NLS-1$
					BugReporter.sendBugReport();
					JMessageBox.showError(APrintDiskRecognitionExtension.this.application.getOwnerForDialog(), t);
				}
			}
		});
		return btn;
	}

	private void startRecognizeBook() throws Exception {

		// frame for remembering the preferences
		APrintNGInternalFrame f = new APrintNGInternalFrame(extensionPreferences) {
		};
	
		JBookRecognition dr = new JBookRecognition(application.getRepository(),  extensionPreferences,
				application, f);

		f.setTitle("Book Recognition wizard"); //$NON-NLS-1$


		f.setLayout(new BorderLayout());
		f.getContentPane().add(dr, BorderLayout.CENTER);

		f.setVisible(true);

	}

	private void startRecognize() throws Exception {
		// open the wizard

		// frame for remembering the preferences
		APrintNGInternalFrame f = new APrintNGInternalFrame(extensionPreferences) {
		};

		JDiskRecognition dr = new JDiskRecognition(application.getRepository(), extensionPreferences, application,
				f);

		f.setTitle(Messages.getString("APrintDiskRecognitionExtension.0")); //$NON-NLS-1$

		// this should not be done like this, as the internal frame has it's own
		// mechanisme
		// f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		f.setLayout(new BorderLayout());
		f.getContentPane().add(dr, BorderLayout.CENTER);

		f.setVisible(true);

	
	}

}
