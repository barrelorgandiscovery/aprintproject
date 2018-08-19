package org.barrelorgandiscovery.gui.aprint;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Locale;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.extensions.ExtensionManager;
import org.barrelorgandiscovery.extensions.ExtensionRepository;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

import com.jeta.forms.components.border.TitledBorderLabel;
import com.jeta.forms.components.image.ImageComponent;
import com.jeta.forms.components.label.JETALabel;
import com.jeta.forms.components.panel.FormPanel;

import fr.pfreydiere.extensions.extensions.Description;
import fr.pfreydiere.extensions.extensions.ExtensionRef;

/**
 * frame for the extension management in APrint
 * 
 * @author Freydiere Patrice
 * 
 */
public class APrintExtensionRepository extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 752735919886549715L;

	/**
	 * default extension repository URL ...
	 */
	private static final String DEFAULT_REPOSITORY_URL = "http://pfreydiere.free.fr/aprint/extensions/repository.xml"; //$NON-NLS-1$

	private static Logger logger = Logger
			.getLogger(APrintExtensionRepository.class);

	private APrint aprintref;
	private ExtensionManager extensionmanager;

	public APrintExtensionRepository(APrint aprintref,
			ExtensionManager extensionmanager) throws Exception {

		assert aprintref != null;
		assert extensionmanager != null;

		this.aprintref = aprintref;
		this.extensionmanager = extensionmanager;

		initComponents();

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setIconImage(APrint.getAPrintApplicationIcon());

	}

	private JTextField urlrepository;
	private JButton reloadbutton;
	private JList listextensions;
	private JTextPane extensionDescription;

	private JButton buttoninstallorupdateselectedextensions;
	private JButton buttonupdateallinstalledextension;

	private void initComponents() throws Exception {

		FormPanel fp = new FormPanel(getClass().getResourceAsStream(
				"extensionrepositoryfrom.jfrm")); //$NON-NLS-1$

		ImageComponent imageComponent = (ImageComponent) fp
				.getComponentByName("socketImage");//$NON-NLS-1$
		imageComponent.setIcon(new ImageIcon(getClass().getResource(
				"socket.png")));//$NON-NLS-1$

		TitledBorderLabel labelextensionrepository = (TitledBorderLabel) fp
				.getComponentByName("labelextensionrepository"); //$NON-NLS-1$
		labelextensionrepository.setText(Messages
				.getString("APrintExtensionRepository.1")); //$NON-NLS-1$

		JETALabel labelextensionrepositoryurl = (JETALabel) fp
				.getComponentByName("labelextensionrepositoryurl"); //$NON-NLS-1$
		labelextensionrepositoryurl.setText(Messages
				.getString("APrintExtensionRepository.3")); //$NON-NLS-1$

		TitledBorderLabel labelextensionlist = (TitledBorderLabel) fp
				.getComponentByName("labelextensionlist"); //$NON-NLS-1$
		labelextensionlist.setText(Messages
				.getString("APrintExtensionRepository.5")); //$NON-NLS-1$

		urlrepository = fp.getTextField("repositoryurl"); //$NON-NLS-1$
		assert urlrepository != null;

		urlrepository.setText(DEFAULT_REPOSITORY_URL);

		reloadbutton = (JButton) fp.getButton("buttonreloadrepository"); //$NON-NLS-1$
		assert reloadbutton != null;

		reloadbutton.setText(Messages.getString("APrintExtensionRepository.8")); //$NON-NLS-1$

		reloadbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					reloadNewRepositoryXMLDef(urlrepository.getText());
				} catch (Exception ex) {
					logger.error("buttonreloadrepository", ex); //$NON-NLS-1$
					BugReporter.sendBugReport();
					JMessageBox.showMessage(APrintExtensionRepository.this,
							Messages.getString("APrintExtensionRepository.10") //$NON-NLS-1$
									+ ex.getMessage());
				}
			}
		});

		TitledBorderLabel labelselectedextensiondescription = (TitledBorderLabel) fp
				.getComponentByName("labelselectedextensiondescription"); //$NON-NLS-1$
		labelselectedextensiondescription.setText(Messages
				.getString("APrintExtensionRepository.12")); //$NON-NLS-1$

		listextensions = fp.getList("listextension"); //$NON-NLS-1$

		listextensions.setCellRenderer(new ListRenderer());

		listextensions.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				try {

					DisplayExtensionRef d = (DisplayExtensionRef) listextensions
							.getSelectedValue();
					logger.debug("selected element ..." + d); //$NON-NLS-1$

					String description = null;
					Description[] descriptionArray = d.getExtensionref()
							.getDescriptionArray();

					for (int i = 0; i < descriptionArray.length; i++) {
						Description description2 = descriptionArray[i];

						String lang = description2.getLang();
						if (description2.isSetLang() && lang != null) {
							if (Locale.getDefault().getLanguage().equals(lang)) {
								logger
										.debug("we found the language locale ... "); //$NON-NLS-1$
								description = description2.getDescription();
								break;
							}
						}

						if (!description2.isSetLang()) {
							description = description2.getDescription();
						}

					}

					extensionDescription.setText(description);
				} catch (Throwable t) {
					logger.error("selectExtension", t); //$NON-NLS-1$
				}
			}
		});

		extensionDescription = (JTextPane) fp
				.getComponentByName("textextensiondescription"); //$NON-NLS-1$
		extensionDescription.setEditable(false);

		buttoninstallorupdateselectedextensions = (JButton) fp
				.getButton("buttoninstallorupdateselectedextensions"); //$NON-NLS-1$
		buttoninstallorupdateselectedextensions.setText(Messages
				.getString("APrintExtensionRepository.18")); //$NON-NLS-1$

		buttoninstallorupdateselectedextensions
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						installOrUpdateSelectedExtensions();
					}
				});

		buttonupdateallinstalledextension = (JButton) fp
				.getButton("buttonupdateallinstalledextension"); //$NON-NLS-1$
		buttonupdateallinstalledextension.setText(Messages
				.getString("APrintExtensionRepository.20")); //$NON-NLS-1$
		buttonupdateallinstalledextension
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						updateAllInstalledExtensions();
					}
				});

		getContentPane().add(fp, BorderLayout.CENTER);

		getRootPane().setDefaultButton(reloadbutton);

		setSize(900, 500);
	}

	private void installOrUpdateSelectedExtensions() {
		try {

			logger.debug("get selected extensions ... "); //$NON-NLS-1$
			Object[] selectedValues = listextensions.getSelectedValues();

			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < selectedValues.length; i++) {
				Object object = selectedValues[i];
				DisplayExtensionRef d = (DisplayExtensionRef) object;
				ExtensionRef extensionRef = d.getExtensionref();
				String url = extensionRef.getUrl();
				try {
					extensionmanager.downloadExtension(url);
					sb
							.append(
									Messages
											.getString("APrintExtensionRepository.22")).append(extensionRef.getName()) //$NON-NLS-1$
							.append(" ").append(Messages.getString("APrintExtensionRepository.24")).append( //$NON-NLS-1$ //$NON-NLS-2$
									"\n"); //$NON-NLS-1$
				} catch (Exception ex) {
					logger.error("fail to update " + extensionRef, ex); //$NON-NLS-1$
					JMessageBox.showMessage(this, Messages
							.getString("APrintExtensionRepository.27") //$NON-NLS-1$
							+ extensionRef.getName());
				}
			}

			if (sb.length() > 0) {
				JMessageBox.showMessage(this, sb.toString());

				JMessageBox.showMessage(this, Messages
						.getString("APrintExtensionRepository.48")); //$NON-NLS-1$
				aprintref.relaunch();
				setVisible(false);

			}
		} catch (Throwable t) {
			logger.error("installOrUpdateSelectedExtensions", t); //$NON-NLS-1$
			BugReporter.sendBugReport();
			JMessageBox.showMessage(this, Messages
					.getString("APrintExtensionRepository.29") //$NON-NLS-1$
					+ t.getMessage());
		}

	}

	private void updateAllInstalledExtensions() {
		try {

			ExtensionRef[] listExtensionsThatShouldBeUpdated = extensionRepository
					.listExtensionsThatShouldBeUpdated(extensionmanager);

			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < listExtensionsThatShouldBeUpdated.length; i++) {
				ExtensionRef extensionRef = listExtensionsThatShouldBeUpdated[i];
				logger.debug("updating :" + extensionRef); //$NON-NLS-1$
				try {
					extensionmanager.downloadExtension(extensionRef.getUrl());
					sb
							.append(
									Messages
											.getString("APrintExtensionRepository.31")).append(extensionRef.getName()) //$NON-NLS-1$
							.append(" ").append(Messages.getString("APrintExtensionRepository.33")).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

				} catch (Exception ex) {
					logger.error("fail to update " + extensionRef, ex); //$NON-NLS-1$
					JMessageBox.showMessage(this, Messages
							.getString("APrintExtensionRepository.36") //$NON-NLS-1$
							+ extensionRef.getName());
				}
			}

			if (sb.length() > 0) {
				JMessageBox.showMessage(this, sb.toString());

				JMessageBox.showMessage(this, Messages
						.getString("APrintExtensionList.48")); //$NON-NLS-1$

				aprintref.relaunch();
				setVisible(false);
			}
		} catch (Throwable t) {
			logger.error("updateAllInstalledExtensions", t); //$NON-NLS-1$
			BugReporter.sendBugReport();
			JMessageBox.showMessage(this, Messages
					.getString("APrintExtensionRepository.38") //$NON-NLS-1$
					+ t.getMessage());
		}
	}

	/**
	 * current extension repository ...
	 */
	private ExtensionRepository extensionRepository = null;

	private void reloadNewRepositoryXMLDef(String url) {
		if (url == null || "".equals(url)) //$NON-NLS-1$
			return;

		logger.debug("load New Repository XMLDef .. " + url); //$NON-NLS-1$

		try {
			ExtensionRepository er = new ExtensionRepository(new URL(url));

			logger.debug("OK repository loaded ... "); //$NON-NLS-1$

			this.extensionRepository = er;

			logger.debug("refresh the gui ..."); //$NON-NLS-1$
			updateIHM();

		} catch (Exception ex) {
			logger.error("reloadNewRepositoryXMLDef", ex); //$NON-NLS-1$
			JMessageBox.showMessage(this, Messages
					.getString("APrintExtensionRepository.44") + url + " :" //$NON-NLS-1$ //$NON-NLS-2$
					+ ex.getMessage());
			BugReporter.sendBugReport();
		}
	}

	static class ListRenderer extends DefaultListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			try {

				Component c = super.getListCellRendererComponent(list, value,
						index, isSelected, cellHasFocus);

				DisplayExtensionRef r = (DisplayExtensionRef) value;

				JLabel label = (JLabel) c;

				String imagename = null;
				if (r.isAlreadyInstalled()) {
					imagename = "extension_installed.png"; //$NON-NLS-1$
				} else {
					imagename = "extension_notinstalled.png"; //$NON-NLS-1$
				}

				URL image = APrintExtensionRepository.class
						.getResource(imagename);
				if (image == null)
					throw new Exception(imagename);
				label.setIcon(new ImageIcon(image));

				return label;

			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
				throw new RuntimeException(ex.getMessage(), ex);
			}

		}

	}

	/**
	 * class for the display in the JList ..
	 * 
	 * @author Freydiere Patrice
	 * 
	 */
	static class DisplayExtensionRef {

		private boolean alreadyInstalled = false;

		public DisplayExtensionRef(ExtensionRef ref, boolean alreadyInstalled) {
			this.extensionref = ref;
			this.alreadyInstalled = alreadyInstalled;
		}

		private ExtensionRef extensionref;

		public ExtensionRef getExtensionref() {
			return extensionref;
		}

		public boolean isAlreadyInstalled() {
			return alreadyInstalled;
		}

		@Override
		public String toString() {
			return extensionref.getName()
					+ Messages.getString("APrintExtensionRepository.46") //$NON-NLS-1$
					+ extensionref.getVersion();
		}
	}

	private void updateDescription(ExtensionRef selectedExtensionRef) {

	}

	/**
	 * Update the GUI from the repository ...
	 */
	private void updateIHM() throws Exception {

		logger.debug("updateIHM"); //$NON-NLS-1$

		DefaultListModel dm = new DefaultListModel();
		listextensions.setModel(dm);

		if (extensionRepository == null) {
			logger.debug("no extensionrepository"); //$NON-NLS-1$
			return;
		}

		logger.debug("get the extension list .... "); //$NON-NLS-1$

		ExtensionRef[] listExtensions2 = extensionRepository.listExtensions();
		ExtensionRef[] installedExtensions = extensionRepository
				.listExtensionInstalled(extensionmanager);

		// merge the list with the already installed extensions ....

		for (int i = 0; i < listExtensions2.length; i++) {
			ExtensionRef extensionRef = listExtensions2[i];

			logger.debug("extensionref name " + extensionRef.getName()); //$NON-NLS-1$
			logger.debug("extension url ... " + extensionRef.getUrl()); //$NON-NLS-1$

			boolean installed = false;
			for (int j = 0; j < installedExtensions.length; j++) {
				if (installedExtensions[j] == extensionRef) {
					installed = true;
					break;
				}
			}

			dm.addElement(new DisplayExtensionRef(extensionRef, installed));
		}

		listextensions.revalidate();

	}

	public static void main(String[] args) throws Exception {

		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure(new LF5Appender());

		APrintExtensionRepository ar = new APrintExtensionRepository(null, null);
		ar.setVisible(true);

	}

}
