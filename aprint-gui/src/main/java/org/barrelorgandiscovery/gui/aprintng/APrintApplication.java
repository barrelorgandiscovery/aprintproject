package org.barrelorgandiscovery.gui.aprintng;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.gui.SplashScreenWindow;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.JavaSoundInfos;
import org.barrelorgandiscovery.tools.ProfilingCondition;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

import com.birosoft.liquid.LiquidLookAndFeel;
import com.easynth.lookandfeel.EaSynthLookAndFeel;
import com.formdev.flatlaf.FlatLightLaf;
import com.googlecode.vfsjfilechooser2.accessories.bookmarks.Bookmarks;
import com.googlecode.vfsjfilechooser2.accessories.bookmarks.TitledURLEntry;
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

	private static APrintNG p;

	/**
	 * main function, that is called from command line, this method should be
	 * bootstrap for child first class loading
	 * 
	 * @param args
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public static void main(final String[] args) throws Exception {

		BugReporter.init("APrint"); //$NON-NLS-1$

		launch(args);

	}

	/**
	 * @param args
	 */
	private static void launch(String[] args) {
		try {

			boolean isbeta = false;
			boolean isdevelop = false;

			org.apache.commons.cli.CommandLineParser parser = new DefaultParser();
			final Options options = new Options();

			options.addOption(new Option("d", "develop", false, "turn on develop"));
			options.addOption(new Option("b", "beat", false, "turn on beta"));
			options.addOption(new Option("m", "mainfolder", true, "main folder argument"));

			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption('d')) {
				isdevelop = true;
				isbeta = true;
			}
			if (cmd.hasOption('b')) {
				isbeta = true;
			}

			// Dump system properties

			Properties sysprop = System.getProperties();
			String mainFolder = sysprop.getProperty(APrintApplicationBootStrap.MAINFOLDER_SYSPROP, null);
			if (mainFolder == null) {
				mainFolder = sysprop.getProperty("jpackage.app-path", null);
				if (mainFolder != null) {
					File root = new File(mainFolder).getParentFile().getParentFile();
					mainFolder = new File(root, "lib/app").getAbsolutePath();
				}
			}

			String commandline_mainfolderValue = cmd.getOptionValue('m');
			if (commandline_mainfolderValue != null && !commandline_mainfolderValue.isEmpty()) {
				mainFolder = commandline_mainfolderValue;
			}

			if (isdevelop && (!ProfilingCondition.isProfiling())) {
				// remove the log if profiling condition
				LF5Appender lf5 = new LF5Appender();
				BasicConfigurator.configure(lf5);
			}

			if (!ProfilingCondition.isProfiling() && !isdevelop) {
				Logger.getRootLogger().setLevel(Level.ERROR);
			} else {
				Logger.getRootLogger().setLevel(Level.DEBUG);
			}

			Logger logger = Logger.getLogger(APrintApplicationBootStrap.class);
			if (logger.isInfoEnabled()) {
				sysprop.list(System.out);
				try {
					Set<Entry<Object, Object>> entrySet = sysprop.entrySet();
					for (Iterator iterator = entrySet.iterator(); iterator.hasNext();) {
						Entry<Object, Object> entry = (Entry<Object, Object>) iterator.next();

						logger.info("" + entry.getKey() + "=" //$NON-NLS-1$ //$NON-NLS-2$
								+ entry.getValue());
					}
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex); // $NON-NLS-1$
				}

			}

			final APrintProperties prop = new APrintProperties("aprintstudio", //$NON-NLS-1$
					isbeta, mainFolder);


			File aprintFolder = prop.getAprintFolder();
			// check write permission in aprint folder
			try {
				
				File writeTestFile = new File(aprintFolder, "writeTest.file");
				try(var fw = new FileWriter(writeTestFile)) {
					fw.write("testwrite");
				}
				
			} catch(Exception ex) {
				logger.error("error checking the write permissions :" + ex.getMessage(), ex);
				JOptionPane.showConfirmDialog(null, "Fail to write in " + aprintFolder + ", check folder permissions");
				return;
			}	
			
			
			// splash
			showSplash();

			System.out.println(Messages.getString("APrintApplication.0")); //$NON-NLS-1$
			logger.info(Messages.getString("APrintApplication.0")); //$NON-NLS-1$

			System.out.println("Current Local :" //$NON-NLS-1$
					+ Locale.getDefault().toString());
			logger.info("Current Local :" + Locale.getDefault().toString()); //$NON-NLS-1$

			JavaSoundInfos.dumpSourceDataLines();

			logger.debug("set securitymanager"); //$NON-NLS-1$
			System.setSecurityManager(null);

			logger.debug("adding native library folder");
			
			if (aprintFolder != null) {
				File nl = new File(aprintFolder, "pluginnativelibraries");
				if (!nl.exists()) {
					nl.mkdirs();
				}

				addToJavaLibraryPath(nl);

			}

			initLanguageWithProperties(prop);

			// Verification du repertoire de gammes ...
			File f = prop.getGammeAndTranlation();
			if (f != null && !f.exists()) {
				JMessageBox.showMessage(null, Messages.getString("APrintApplication.3") //$NON-NLS-1$
						+ f.getAbsolutePath() + Messages.getString("APrintApplication.4")); //$NON-NLS-1$
				prop.setGammeAndTranlation(null);
			}

			boolean restart = true;
			boolean first = true;

			while (restart) {

				// setLiquidLnf();

				check_lnf();

				// check book marks

				try {
					Bookmarks b = new Bookmarks();
					boolean found = false;
					for (int i = 0; i < b.getSize(); i++) {
						String title = b.getTitle(i);
						if ("Web Library".equals(title)) {
							found = true;
							break;
						}
					}

					if (!found) {
						b.add(new TitledURLEntry("Web Library",
								"bod://www.barrel-organ-discovery.org:80/READONLY_WEB_SITE_CONTENT"));
						b.save();
					}

				} catch (Throwable t) {
					logger.error("fail to check bookmarks :" + t.getMessage(), t);
				}

				logger.debug("loading the look and feel ..."); //$NON-NLS-1$

				try {

					String lnf = prop.getLookAndFeel();
					if (lnf == null || lnf.isBlank()) {
						javax.swing.UIManager
								.setLookAndFeel((LookAndFeel) com.formdev.flatlaf.FlatLightLaf.class.newInstance());
					} else if ("swing".equals(lnf)) { //$NON-NLS-1$
						javax.swing.UIManager
								.setLookAndFeel(javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());

					} else {
						Class<?> lnfclass = APrintApplication.class.getClassLoader().loadClass(lnf);
						javax.swing.UIManager.setLookAndFeel((LookAndFeel) lnfclass.newInstance());
					}

				} catch (Throwable t) {
					logger.error("fail to load look and feel ...", t); //$NON-NLS-1$
				}

				p = new APrintNG(prop);

				// has some instruments in the web repository ?? and is it the
				// first time we access it ??
				if (prop.getFirstTimeOpenAPrintAndGetWebInstruments()) {

					if (!p.hasInstrumentsInWebRepository()) {

						if (JOptionPane.showConfirmDialog(p, Messages.getString("APrintApplication.6"), //$NON-NLS-1$
								Messages.getString("APrintApplication.7"), //$NON-NLS-1$
								JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							logger.debug("getting the first time, the web instruments ... "); //$NON-NLS-1$

							try {
								p.synchronizeWebRepositories();
							} catch (Throwable t) {
								logger.error(Messages.getString("APrintApplication.9") //$NON-NLS-1$
										+ t.getMessage(), t);
								JMessageBox.showMessage(p, Messages.getString("APrintApplication.10")); //$NON-NLS-1$
								BugReporter.sendBugReport();
							}
							logger.debug("synchronization done ..."); //$NON-NLS-1$
						}

					}

					prop.setFirstTimeOpenAPrintAndGetWebInstruments(false);
				} else {
					logger.debug("external instruments has already been downloaded");
				}
				
				if (first) {
					
					List<String> filesToOpen = cmd.getArgList();
					// handle file opening from command line
					for (String s: filesToOpen) {
						try {
						p.handleDropFileTarget(new File(s));
						} catch (Exception ex) {
							JMessageBox.showMessage(p, "Fail to open " + s + ": " + ex.getMessage());
							ex.printStackTrace();
						}
					}
					
					first = false;
				}
				

				// main loop
				while (p.isVisible()) {
					Thread.sleep(100);
				}

				if (p.getTerminateState() != APrintNG.NEED_RESTART) {
					p.dispose();
					p = null;

					// restart the application
					System.gc(); // make sure all the opened files has been closed (if not explicitely closed)
					System.exit(0); // user must restart to take effect
				}

				// reinit the language if the exit is linked to language change
				initLanguageWithProperties(prop);

			}

		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
			ex.printStackTrace(System.err);
			BugReporter.sendBugReport();
			JMessageBox.showMessage(null, Messages.getString("APrintApplication.5") //$NON-NLS-1$
					+ ex.getMessage() + "\n ->" + ex.toString()); //$NON-NLS-1$
		}
	}

	// check look and feel
	private static void check_lnf() {
		try {

			String llnfcn = LiquidLookAndFeel.class.getName();
			if (!isLookAndFeelInstalled(llnfcn))
				javax.swing.UIManager.installLookAndFeel("Liquid Look And Feel", llnfcn); //$NON-NLS-1$

			String rodlnfclassname = NimRODLookAndFeel.class.getName();
			if (!isLookAndFeelInstalled(rodlnfclassname)) {
				javax.swing.UIManager.installLookAndFeel("NimROD Look And Feel", rodlnfclassname); //$NON-NLS-1$
			}

			String easynthlnf = EaSynthLookAndFeel.class.getName();
			if (!isLookAndFeelInstalled(easynthlnf)) {
				javax.swing.UIManager.installLookAndFeel("EaSynth Look And Feel", easynthlnf); //$NON-NLS-1$
			}

			String flatlaf = FlatLightLaf.class.getName();
			if (!isLookAndFeelInstalled(flatlaf)) {
				javax.swing.UIManager.installLookAndFeel("FlatLaf And Feel", flatlaf); //$NON-NLS-1$
			}

			LiquidLookAndFeel.setLiquidDecorations(true, "mac"); //$NON-NLS-1$
			// LiquidLookAndFeel.setStipples(false);
			LiquidLookAndFeel.setToolbarFlattedButtons(true);

			try {
				String windowslnf = com.jgoodies.looks.windows.WindowsLookAndFeel.class.getName();
				if (!isLookAndFeelInstalled(windowslnf)) {
					UIManager.installLookAndFeel("JGoodies Windows lnf", windowslnf);
				}

				String plsticlnf = com.jgoodies.looks.plastic.PlasticLookAndFeel.class.getName();
				if (!isLookAndFeelInstalled(plsticlnf)) {
					UIManager.installLookAndFeel("JGoodies Plastic lnf", plsticlnf);
				}

			} catch (Throwable t) {

			}

		} catch (Throwable t) {

		}
	}

	/**
	 * Show Splash
	 * 
	 * @throws IOException
	 */
	private static void showSplash() throws IOException {
		BufferedImage image = ImageIO.read(APrintApplication.class.getResourceAsStream("splash.jpg")); //$NON-NLS-1$
		final SplashScreenWindow s = new SplashScreenWindow(image);
		s.setVisible(true);

		new Timer(3000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				s.dispose();
			}
		}).start();
	}

	/**
	 * @param prop
	 */
	protected static void initLanguageWithProperties(final APrintProperties prop) {
		try {
			Locale.setDefault(prop.getForcedLocal()); // for checking
														// localization

		} catch (Throwable t) {
			System.out.println("ERROR setting locale");
			t.printStackTrace();
		}

		// init the localization process ...
		Messages.initLocale(prop.getExtensionFolder(), prop.getDisplayKeyNamesForTranslation());
	}

	private static void setQuaquaLnf() {
		// set the Quaqua Look and Feel in the UIManager
		try {
			UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel"); //$NON-NLS-1$
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

			javax.swing.UIManager.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel"); //$NON-NLS-1$
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
		LookAndFeelInfo[] installedLookAndFeels = javax.swing.UIManager.getInstalledLookAndFeels();
		for (int i = 0; i < installedLookAndFeels.length; i++) {
			LookAndFeelInfo lookAndFeelInfo = installedLookAndFeels[i];
			if (classname.equals(lookAndFeelInfo.getClassName()))
				return true;
		}

		return false;
	}

	/**
	 * Ajoute un nouveau repertoire dans le java.library.path.
	 * 
	 * @param dir Le nouveau repertoire a ajouter.
	 */
	private static void addToJavaLibraryPath(File dir) {
		final String LIBRARY_PATH = "java.library.path";
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(dir + " is not a directory.");
		}
		String javaLibraryPath = System.getProperty(LIBRARY_PATH);
		System.setProperty(LIBRARY_PATH, javaLibraryPath + File.pathSeparatorChar + dir.getAbsolutePath());

		String msg = "Library path :" + System.getProperty(LIBRARY_PATH);
		logger.info(msg);
		System.out.println(msg);

		// resetJavaLibraryPath();
	}

}