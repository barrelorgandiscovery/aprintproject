package org.barrelorgandiscovery.extensionsng.scanner;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ChildFirstClassLoader;
import org.barrelorgandiscovery.extensions.ExtensionPoint;
import org.barrelorgandiscovery.extensionsng.scanner.tools.VersionTools;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InformRepositoryExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.APrintApplicationBootStrap;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.WelcomeExtensionExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.helper.BaseExtension;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.repository.Repository;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.RepositoryAdapter;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

/**
 * APrint NG extension
 *
 * @author pfreydiere
 */
public class ScannerExtension extends BaseExtension {

	private static Logger logger = Logger.getLogger(ScannerExtension.class);

	private Repository2 repository;

	public ScannerExtension() throws Exception {
		super();
		this.defaultAboutAuthor = "Patrice Freydiere"; //$NON-NLS-1$
		this.defaultAboutVersion = VersionTools.getVersion();
	}

	@Override
	public String getName() {
		return "Scanner Extension"; //$NON-NLS-1$
	}

	@Override
	protected void setupExtensionPoint(List<ExtensionPoint> initExtensionPoints) throws Exception {
		super.setupExtensionPoint(initExtensionPoints);

		WelcomeExtensionExtensionPoint wbe = new WelcomeExtensionExtensionPoint() {

			public void addComponentInPanel(JPanel p) {
				p.add(createNewProjectButton());
			}
		};

		initExtensionPoints.add(createExtensionPoint(WelcomeExtensionExtensionPoint.class, wbe));
		initExtensionPoints
				.add(createExtensionPoint(InformRepositoryExtensionPoint.class, new InformRepositoryExtensionPoint() {

					@Override
					public void informRepository(Repository repository) {
						if (repository instanceof RepositoryAdapter) {
							ScannerExtension.this.repository = ((RepositoryAdapter) repository).getRepository2();
						} else if (repository instanceof Repository2) {
							ScannerExtension.this.repository = (Repository2) repository;
						} else {
							logger.error("repository2 is not accessible, extension will not work"); //$NON-NLS-1$
							throw new RuntimeException("repository2 is not accessible, extension will not work"); //$NON-NLS-1$
						}
					}
				}));
	}

	private JButton createNewProjectButton() {
		JButton btn = new JButton(Messages.getString("ScannerExtension.4")); //$NON-NLS-1$
		try {
			btn.setIcon(ImageTools.loadIcon(ScannerExtension.class, "scanner.png")); //$NON-NLS-1$
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		}
		btn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					newProject();
				} catch (Throwable t) {
					logger.error("error creating new project :" + t.getMessage(), t); //$NON-NLS-1$
					BugReporter.sendBugReport();
					JMessageBox.showError(ScannerExtension.this.application.getOwnerForDialog(), t);
				}
			}
		});
		return btn;
	}

	private void newProject() throws Exception {

		// ask for the folder + instrument
		assert repository != null;
		if (repository == null) {
			throw new Exception("repository is not defined, waiting for Repository2 object type"); //$NON-NLS-1$
		}

		APrintProperties aprintproperties = application.getProperties();
		File aprintFolder = aprintproperties.getAprintFolder();

		ClassLoader cl = getClass().getClassLoader();
		
		String propertyNoLazy = System.getProperty("nolazy");
		if (propertyNoLazy == null || propertyNoLazy.isEmpty()) {
			File lazyExtension = new File(aprintFolder, "aprint-book-scanner-all.extensionlazy");
			if (!lazyExtension.exists()) {
				throw new Exception("cannot load extension, file " + lazyExtension + " does not exists");
			}

			cl = new ChildFirstClassLoader(new URL[] { lazyExtension.toURL() }, getClass().getClassLoader());
		}
		Class<?> clazz = cl.loadClass(getClass().getPackage().getName() + ".ScannerLazyLoad");
		// IPrefsStorage extensionPreferences, Repository2 repository, APrintNG
		// application
		Method m = clazz.getMethod("lazyLoadScanner",
				new Class[] { IPrefsStorage.class, Repository2.class, APrintNG.class });
		m.invoke(null, new Object[] { extensionPreferences, repository, application });

	}
}
