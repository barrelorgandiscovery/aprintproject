package org.barrelorgandiscovery.gui.aprintng;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * Class for managing quick scripts, send events for changes on quick scripts ..
 * aso
 * 
 * @author Freydiere Patrice
 * 
 */
public class QuickScriptManager {

	private static Logger logger = Logger.getLogger(QuickScriptManager.class);

	public static final String APRINTBOOKGROOVYSCRIPTEXTENSION = ".aprintbookgroovyscript";
	private File folder;

	public QuickScriptManager(File scriptfolder) {
		this.folder = scriptfolder;
	}

	/**
	 * list the quickscripts in the folder passed in parameter
	 * 
	 * @param quickScriptFolder
	 *            the folder containing the quick scripts
	 * @return
	 */
	public String[] listQuickScripts() {
		File[] scripts = folder.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.isDirectory())
					return false;
				if (pathname.getName().toLowerCase()
						.endsWith(APRINTBOOKGROOVYSCRIPTEXTENSION))
					return true;
				return false;
			}
		});

		// create the list of the scripts
		ArrayList<String> retvalue = new ArrayList<String>();
		for (int i = 0; i < scripts.length; i++) {
			File file = scripts[i];
			String name = file.getName();
			name = name.substring(0, name.length()
					- APRINTBOOKGROOVYSCRIPTEXTENSION.length());
			retvalue.add(name);
		}

		// sort the list

		String[] result = retvalue.toArray(new String[0]);
		Arrays.sort(result);
		return result;
	}

	public void saveScript(String name, StringBuffer content) throws Exception {

		boolean exist = false;

		logger.debug("saveScript " + name);
		File scriptfile = new File(folder, name
				+ APRINTBOOKGROOVYSCRIPTEXTENSION);
		exist = scriptfile.exists();

		FileOutputStream fos = new FileOutputStream(scriptfile);
		try {
			fos.write(content.toString().getBytes("UTF-8"));

			logger.debug("save done !");
		} finally {
			fos.close();
		}

		if (exist)
			fireScriptChanged(name);
		else
			fireScriptListChanged(listQuickScripts());

	}

	/**
	 * Load the script managed by the script manager
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public StringBuffer loadScript(String name) throws Exception {

		File scriptfile = new File(folder, name
				+ APRINTBOOKGROOVYSCRIPTEXTENSION);

		logger.debug("loading script " + scriptfile.getAbsolutePath());

		StringBuffer sb = new StringBuffer();
		InputStreamReader fr = new InputStreamReader(new FileInputStream(
				scriptfile), "UTF-8"); //$NON-NLS-1$
		try {
			int cpt;
			char[] buffer = new char[2048];

			while ((cpt = fr.read(buffer)) != -1) {
				sb.append(buffer, 0, cpt);
			}

			return sb;
		} finally {
			fr.close();
		}

	}

	public void deleteScript(String name) throws Exception {
		File scriptfile = new File(folder, name
				+ APRINTBOOKGROOVYSCRIPTEXTENSION);

		scriptfile.delete();

		fireScriptListChanged(listQuickScripts());

	}

	private Vector<IScriptManagerListener> listeners = new Vector<IScriptManagerListener>();

	public void addScriptManagerListener(IScriptManagerListener listener) {
		listeners.add(listener);
	}

	public void removeScriptManagerListener(IScriptManagerListener listener) {
		listeners.remove(listener);
	}

	protected void fireScriptChanged(String name) {
		for (Iterator<IScriptManagerListener> iterator = listeners.iterator(); iterator
				.hasNext();) {
			IScriptManagerListener l = iterator.next();
			try {
				l.scriptChanged(name);
			} catch (Throwable t) {
				logger.error("error in firing event ScriptChanged " + name
						+ ", continue :" + t.getMessage(), t);
			}
		}
	}

	protected void fireScriptListChanged(String[] list) {
		for (Iterator<IScriptManagerListener> iterator = listeners.iterator(); iterator
				.hasNext();) {
			IScriptManagerListener l = iterator.next();
			try {
				l.scriptListChanged(list);
			} catch (Throwable t) {
				logger.error("error in firing event ScriptListChanged " + list
						+ ", continue :" + t.getMessage(), t);
			}
		}

	}

}
