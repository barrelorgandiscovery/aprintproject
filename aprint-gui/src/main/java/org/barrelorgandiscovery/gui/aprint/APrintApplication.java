package org.barrelorgandiscovery.gui.aprint;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.SwingUtils;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

import com.birosoft.liquid.LiquidLookAndFeel;
import com.easynth.lookandfeel.EaSynthLookAndFeel;
import com.nilo.plaf.nimrod.NimRODLookAndFeel;


/**
 * Application Launch bootstrap, this one manage the properties file (in the
 * user folder)
 * 
 * @author Freydiere Patrice
 * 
 */
public class APrintApplication {

	private static Logger logger = Logger.getLogger(APrintApplication.class);

	/**
	 * main function, that is called from command line
	 * 
	 * @param args
	 */
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public static void main(String[] args) throws Exception {
		BugReporter.init("APrint"); //$NON-NLS-1$
		try {

			// Locale.setDefault(Locale.ENGLISH); // for checking localization

			System.out.println(Messages.getString("APrintApplication.0")); //$NON-NLS-1$
			logger.debug(Messages.getString("APrintApplication.0")); //$NON-NLS-1$

			System.out.println("Current Local :" //$NON-NLS-1$
					+ Locale.getDefault().toString());
			logger.debug("Current Local :" + Locale.getDefault().toString()); //$NON-NLS-1$

			boolean isbeta = false;
			boolean isdevelop = false;

			if (args.length > 0) //$NON-NLS-1$
			{
				if (args[0].equals("develop")) { //$NON-NLS-1$
					isdevelop = true;
					isbeta = true;
				}

				if (args[0].equals("beta")) //$NON-NLS-1$
					isbeta = true;

				if (isdevelop) {
					LF5Appender lf5 = new LF5Appender();
					BasicConfigurator.configure(lf5);
				}
			}

			if (!isdevelop)
			{
				Logger.getRootLogger().setLevel(Level.INFO);
			}
			
			// setQuaquaLnf();

			Properties sysprop = System.getProperties();
			// Logger.getRootLogger().setLevel(Level.INFO);

			Logger logger = Logger.getLogger(APrintApplication.class);
			if (logger.isInfoEnabled()) {
				sysprop.list(System.out);
				try {
					Set<Entry<Object, Object>> entrySet = sysprop.entrySet();
					for (Iterator iterator = entrySet.iterator(); iterator
							.hasNext();) {
						Entry<Object, Object> entry = (Entry<Object, Object>) iterator
								.next();

						logger.info("" + entry.getKey() + "=" //$NON-NLS-1$ //$NON-NLS-2$
								+ entry.getValue());
					}
				} catch (Exception ex) {
					logger.error("ex", ex); //$NON-NLS-1$
				}
			}

			logger.debug("set securitymanager"); //$NON-NLS-1$
			System.setSecurityManager(null);

			final APrintProperties prop = new APrintProperties(isbeta);

			// init the localization process ...
			Messages.initLocale(prop.getExtensionFolder(), prop
					.getDisplayKeyNamesForTranslation());

			// Vérification du répertoire de gammes ...
			File f = prop.getGammeAndTranlation();
			if (f != null && !f.exists()) {
				JMessageBox.showMessage(null, Messages
						.getString("APrintApplication.3") //$NON-NLS-1$
						+ f.getAbsolutePath()
						+ Messages.getString("APrintApplication.4")); //$NON-NLS-1$
				prop.setGammeAndTranlation(null);
			}

			boolean restart = true;

			while (restart) {

				// isolation of the classes in a specific class loader
				// only Messages , lnf and aprint properties are loaded in he
				// main class loader
				URLClassLoader cl = new URLClassLoader(new URL[] {},
						APrintApplication.class.getClassLoader());

				// setLiquidLnf();

				try {

					String llnfcn = LiquidLookAndFeel.class.getName();
					if (!isLookAndFeelInstalled(llnfcn))
						javax.swing.UIManager.installLookAndFeel(
								"Liquid Look And Feel", llnfcn); //$NON-NLS-1$

					String rodlnfclassname = NimRODLookAndFeel.class.getName();
					if (!isLookAndFeelInstalled(rodlnfclassname)) {
						javax.swing.UIManager.installLookAndFeel(
								"NimROD Look And Feel", rodlnfclassname); //$NON-NLS-1$
					}
					
					String easynthlnf =EaSynthLookAndFeel.class.getName();
					if (!isLookAndFeelInstalled(easynthlnf))
					{
						javax.swing.UIManager.installLookAndFeel(
								"EaSynth Look And Feel", easynthlnf); //$NON-NLS-1$
					}

					LiquidLookAndFeel.setLiquidDecorations(true, "mac"); //$NON-NLS-1$
					// LiquidLookAndFeel.setStipples(false);
					LiquidLookAndFeel.setToolbarFlattedButtons(true);

				} catch (Throwable t) {

				}

				logger.debug("loading the look and feel ..."); //$NON-NLS-1$

				try {

					String lnf = prop.getLookAndFeel();
					if (lnf == null || "swing".equals(lnf)) { //$NON-NLS-1$
						javax.swing.UIManager
								.setLookAndFeel(javax.swing.UIManager
										.getCrossPlatformLookAndFeelClassName());

					} else {
						Class<?> lnfclass = cl.loadClass(lnf);
						javax.swing.UIManager
								.setLookAndFeel((LookAndFeel) lnfclass
										.newInstance());
					}

				} catch (Throwable t) {
					logger.error("fail to load look and feel ...", t); //$NON-NLS-1$
				}

				Class aprintclass = cl.loadClass(APrint.class.getName());

				Constructor constructor = aprintclass
						.getConstructor(APrintProperties.class);
				APrint p = (APrint) constructor.newInstance(prop);
				p.setLocationByPlatform(true);
				SwingUtils.center(p);

				p.setVisible(true);

				// has some instruments in the web repository ?? and is it the
				// first time we access it ??
				if (prop.getFirstTimeOpenAPrintAndGetWebInstruments()) {

					if (p.hasInstrumentsInWebRepository()) {

						if (JOptionPane
								.showConfirmDialog(
										p,
										Messages.getString("APrintApplication.6"), //$NON-NLS-1$
										Messages.getString("APrintApplication.7"), //$NON-NLS-1$
										JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							logger
									.debug("getting the first time, the web instruments ... "); //$NON-NLS-1$

							try {
								p.synchronizeWebRepositories();
							} catch (Throwable t) {
								logger.error(
										Messages.getString("APrintApplication.9") //$NON-NLS-1$
												+ t.getMessage(), t);
								JMessageBox
										.showMessage(p,
												Messages.getString("APrintApplication.10")); //$NON-NLS-1$
								BugReporter.sendBugReport();
							}
							logger.debug("synchronization done ..."); //$NON-NLS-1$
						}

					}

					prop.setFirstTimeOpenAPrintAndGetWebInstruments(false);
				}

				while (p.isVisible()) {
					Thread.sleep(100);
				}

				if (p.getTerminateState() != APrint.NEED_RESTART) {
					p.dispose();
					p = null;

					// restart the application
					System.gc();

					System.exit(0); // user must restart to take effect

				}

			}

		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
			ex.printStackTrace(System.err);
			BugReporter.sendBugReport();
			JMessageBox.showMessage(null, Messages
					.getString("APrintApplication.5") //$NON-NLS-1$
					+ ex.getMessage() + "\n ->" + ex.toString()); //$NON-NLS-1$
		}

	}

	private static void setQuaquaLnf() {
		// set system properties here that affect Quaqua
		// for example the default layout policy for tabbed
		// panes:
		// System.setProperty(
		// "Quaqua.tabLayoutPolicy","wrap"
		//
		// );

		// set the Quaqua Look and Feel in the UIManager
		try {
			UIManager
					.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel"); //$NON-NLS-1$
			// set UI manager properties here that affect Quaqua

		} catch (Exception e) {
			// take an appropriate action here

		}
	}

	private static void installLookAndFeelProperties() {
		LiquidLookAndFeel.setLiquidDecorations(true, "mac"); //$NON-NLS-1$
		// LiquidLookAndFeel.setStipples(false);
		LiquidLookAndFeel.setToolbarFlattedButtons(true);
	}

	private static void setLiquidLnf() {
		// set the look and feel
		try {
			// PlasticLookAndFeel lf = new PlasticLookAndFeel();
			// lf.setPlasticTheme(new SkyPink());
			// UIManager.setLookAndFeel(lf);

			javax.swing.UIManager
					.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel"); //$NON-NLS-1$
			LiquidLookAndFeel.setLiquidDecorations(true, "mac"); //$NON-NLS-1$
			// LiquidLookAndFeel.setStipples(false);
			LiquidLookAndFeel.setToolbarFlattedButtons(true);

			// javax.swing.UIManager.setLookAndFeel("com.nilo.plaf.nimrod.NimRODLookAndFeel");

		} catch (Exception e) {
		}
	}

	private static boolean isLookAndFeelInstalled(String classname) {
		if (classname == null)
			return false;

		assert classname != null;
		LookAndFeelInfo[] installedLookAndFeels = javax.swing.UIManager
				.getInstalledLookAndFeels();
		for (int i = 0; i < installedLookAndFeels.length; i++) {
			LookAndFeelInfo lookAndFeelInfo = installedLookAndFeels[i];
			if (classname.equals(lookAndFeelInfo.getClassName()))
				return true;
		}

		return false;
	}

}
