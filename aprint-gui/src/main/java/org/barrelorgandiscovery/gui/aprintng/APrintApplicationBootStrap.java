package org.barrelorgandiscovery.gui.aprintng;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ChildFirstClassLoader;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;

/**
 * boot strap the application
 * 
 * 
 * bootstrap needs some opens: --add-opens java.base/java.lang=ALL-UNNAMED
 * --add-opens java.desktop/javax.swing.text=ALL-UNNAMED --add-opens
 * java.desktop/javax.swing=ALL-UNNAMED --add-opens
 * java.base/java.util=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED
 * --add-opens java.desktop/java.awt=ALL-UNNAMED
 * 
 * 
 * 
 * 
 * 
 * 
 * @author patrice
 *
 */
public class APrintApplicationBootStrap {

	public static final String MAINFOLDER_SYSPROP = "mainfolder";
	public static String MAINFOLDER_VALUE = null;
	

	public static void main(String[] args) throws Exception {

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

		// setQuaquaLnf();

		// implementation 'com.formdev:flatlaf:2.6'

		// dump to stdout
		Properties sysprop = System.getProperties();
		sysprop.store(System.out, "");

		Logger.getRootLogger().setLevel(Level.DEBUG);

		String mainFolder = sysprop.getProperty(MAINFOLDER_SYSPROP, null);
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

		System.out.println("main folder :" + mainFolder);
		MAINFOLDER_VALUE = mainFolder;

		final APrintProperties prop = new APrintProperties("aprintstudio", //$NON-NLS-1$
				isbeta, mainFolder);

		// get all the jar in the aprintstudiofolder

		String cpseparator = System.getProperty("path.separator");

		String cp = sysprop.getProperty("java.class.path");
		String[] cplist = cp.split(cpseparator); // ";"

		ArrayList<File> syslist = new ArrayList<File>();
		for (int i = 0; i < cplist.length; i++) {
			String p = cplist[i];
			syslist.add(new File(p));
		}

		File[] libraryJars = prop.getAprintFolder().listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});

		syslist.addAll(Arrays.asList(libraryJars));

		ArrayList<URL> urls = new ArrayList<URL>();

		for (Iterator iterator = syslist.iterator(); iterator.hasNext();) {
			File file = (File) iterator.next();
			urls.add(file.toURL());
		}

		// logger.debug("Main class loader :"
		// + APrintApplicationBootStrap.class.getClassLoader());

		// isolation of the classes in a specific class loader
		// only Messages , lnf and aprint properties are loaded in he
		// main class loader
		URLClassLoader cl = new ChildFirstClassLoader(urls.toArray(new URL[0]),
				APrintApplicationBootStrap.class.getClass().getClassLoader());

		// logger.debug("App Class Loader :" + cl);

		Class aprintApplication = cl.loadClass("org.barrelorgandiscovery.gui.aprintng.APrintApplication");
		Method main = aprintApplication.getMethod("main", String[].class);

		Thread.currentThread().setContextClassLoader(cl);

		main.invoke(null, new Object[] { args });
	}
}
