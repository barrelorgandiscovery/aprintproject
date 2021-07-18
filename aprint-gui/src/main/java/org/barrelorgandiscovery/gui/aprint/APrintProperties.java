package org.barrelorgandiscovery.gui.aprint;

import java.awt.Dimension;
import java.io.File;
import java.util.Locale;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;

/**
 * Classe contenant les propriétés associées au programme APrint
 * 
 * 
 * @author Freydiere Patrice
 */
public class APrintProperties {

	private static final String DISPLAYKEYNAMEFORTRANSLATION = "displaykeynamefortranslation"; //$NON-NLS-1$

	private static final String APRINT_HEIGHT = "aprint_height"; //$NON-NLS-1$

	private static final String APRINT_WIDTH = "aprint_width"; //$NON-NLS-1$

	private static final String TIME_LAYER_VISIBLE = "time_layer_visible"; //$NON-NLS-1$

	private static final String ERRORS_VISIBLE_LAYER = "errors_visible_layer"; //$NON-NLS-1$

	private static final String REPERTOIRE_GAMME = "repertoire_gamme"; //$NON-NLS-1$

	private static final String LAST_MIDI_FILE = "last_midi_file"; //$NON-NLS-1$

	private static final String LAST_GROOVYSCRIPT_FILE = "last_groovyscript_file"; //$NON-NLS-1$

	private static final String LAST_VIRTUALBOOK_FILE = "last_virtualbook_file"; //$NON-NLS-1$

	private static final String LOOK_AND_FEEL = "looknfeel"; //$NON-NLS-1$

	private static final String WEB_REPOSITORY_USER = "webrepositoryuser"; //$NON-NLS-1$

	private static final String WEB_REPOSITORY_PASSWORD = "webrepositorypassword"; //$NON-NLS-1$

	private static final String WEB_REPOSITORY_URL = "webrepositoryurl"; //$NON-NLS-1$

	private static final String FIRST_TIME_OPEN_WEBREPOSITORY = "firsttimeopenrepository"; //$NON-NLS-1$

	private static final String QUICKSCRIPT_FOLDER = "quickscriptfolder"; //$NON-NLS-1$

	private static final String SEARCH_FOLDER = "searchfolder"; //$NON-NLS-1$

	private static final String APRINTNG_VB_HEIGHT = "aprintvbheight"; //$NON-NLS-1$

	private static final String APRINTNG_VB_WIDTH = "aprintvbwidth"; //$NON-NLS-1$

	private static final String APRINT_ADDITIONAL_WEB_REPOSITORIES_PREFIX = "aprintadditionalwebrepositories"; //$NON-NLS-1$

	private static final String LAST_SELECTED_INSTRUMENT = "lastselectedinstrument"; //$NON-NLS-1$

	private static final String LAST_INSTRUMENTBUNDLEFILE_FOLDER = "lastinstrumentbundlefilefolder"; //$NON-NLS-1$

	private static final String LAST_MOV_FILE = "lastmovfile";
	
	private static final String FORCED_LOCAL = "forcedlocal";

	private static final String TOOLBARVISIBILITYPREFIX = "toolbarvisibility";

	private boolean isbeta = false;

	private File aprintfolder;

	private IPrefsStorage ps;

	public APrintProperties(String softwareName, boolean isbeta) throws Exception {
		this(softwareName, isbeta, null);
	}

	public APrintProperties(String softwareName, boolean isbeta, String mainFolder) throws Exception {
		// Récupération de l'emplacement du répertoire de l'utilisateur ...

		this.isbeta = isbeta;

		String userfolder = mainFolder;

		if (userfolder == null) {
			userfolder = System.getProperty("user.home"); //$NON-NLS-1$
		} else {
			// mkdirs
			new File(mainFolder).mkdirs();
		}
		
		if (userfolder == null || !new File(userfolder).exists())
			throw new Exception(Messages.getString("APrintProperties.1")); //$NON-NLS-1$

		aprintfolder = new File(new File(userfolder), softwareName // $NON-NLS-1$
				+ (isbeta ? "-beta" : "")); //$NON-NLS-1$ //$NON-NLS-2$

		if (!aprintfolder.exists())
			if (!aprintfolder.mkdir()) {
				throw new Exception("Fail to create " + softwareName + " directory"); //$NON-NLS-1$
			}

		File propertiesfile = new File(new File(userfolder), softwareName + (isbeta ? "-beta" : "") + ".properties"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
																														// //$NON-NLS-4$

		ps = new FilePrefsStorage(propertiesfile);

		if (propertiesfile.exists())
			ps.load();
	}

	/**
	 * Construction de l'objet, en prenant pour fichier de propriété le fichier
	 * aprint.properties dans le répertoire de l'utilisateur
	 */
	public APrintProperties(boolean isbeta) throws Exception {
		this("aprint", isbeta);
	}

	public IPrefsStorage getFilePrefsStorage() {
		return ps;
	}

	/**
	 * Récupère le répertoire de gammes et de fichiers de transposition
	 * 
	 * @return
	 */
	public File getGammeAndTranlation() {
		return ps.getFileProperty(REPERTOIRE_GAMME, null);
	}

	public void setGammeAndTranlation(File gammeAndTranlation) {
		ps.setFileProperty(REPERTOIRE_GAMME, gammeAndTranlation);
	}

	public boolean isErrorsVisible() {
		return ps.getBooleanProperty(ERRORS_VISIBLE_LAYER, false);
	}

	public void setErrorsVisible(boolean visible) {
		ps.setBooleanProperty(ERRORS_VISIBLE_LAYER, visible);
	}

	public boolean isTimeLayerVisible() {
		return ps.getBooleanProperty(TIME_LAYER_VISIBLE, false);
	}

	public void setTimeLayerVisible(boolean visible) {
		ps.setBooleanProperty(TIME_LAYER_VISIBLE, visible);
	}

	public File getExtensionFolder() {
		return aprintfolder;
	}

	public Dimension getAPrintFrameSize() {
		return new Dimension(ps.getIntegerProperty(APRINT_WIDTH, 800), ps.getIntegerProperty(APRINT_HEIGHT, 600));
	}

	public void setAPrintFrameSize(Dimension d) {
		ps.setIntegerProperty(APRINT_HEIGHT, d.height);
		ps.setIntegerProperty(APRINT_WIDTH, d.width);
	}

	private String APRINT_PIANOROLL_DIVIDER_LOCATION = "pianoroll_divider"; //$NON-NLS-1$

	public double getPianorollDividerLocation() {
		return ps.getDoubleProperty(APRINT_PIANOROLL_DIVIDER_LOCATION, 0.5);
	}

	public void setPianorollDividerLocation(double divider) {
		ps.setDoubleProperty(APRINT_PIANOROLL_DIVIDER_LOCATION, divider);
	}

	public void setDisplayKeyNamesForTranslation(boolean display) {
		ps.setBooleanProperty(DISPLAYKEYNAMEFORTRANSLATION, display);
	}

	public boolean getDisplayKeyNamesForTranslation() {
		return ps.getBooleanProperty(DISPLAYKEYNAMEFORTRANSLATION, false);
	}

	public String getLookAndFeel() {
		return ps.getStringProperty(LOOK_AND_FEEL, null); //$NON-NLS-1$
	}

	public void setLookAndFeel(String lookandfeelclass) {
		ps.setStringProperty(LOOK_AND_FEEL, lookandfeelclass);
	}

	public boolean isBeta() {
		return isbeta;
	}

	public File getAprintFolder() {
		return aprintfolder;
	}

	public String getWebRepositoryUser() {
		return ps.getStringProperty(WEB_REPOSITORY_USER, null);
	}

	public String getWebRepositoryPassword() {
		return ps.getStringProperty(WEB_REPOSITORY_PASSWORD, null);
	}

	public String getWebRepositoryURL() {

		String defaultRepositoryURL = "http://aprintrepository.appspot.com/rest";//$NON-NLS-1$
		String repositoryURL = ps.getStringProperty(WEB_REPOSITORY_URL, defaultRepositoryURL);

		if ("http://aprintrepository.appspot.com".equalsIgnoreCase(repositoryURL))//$NON-NLS-1$
		{
			repositoryURL = defaultRepositoryURL;
		}

		return repositoryURL;
	}

	public void setWebRepositoryUser(String user) {
		ps.setStringProperty(WEB_REPOSITORY_USER, user);
	}

	public void setWebRepositoryPassword(String password) {
		ps.setStringProperty(WEB_REPOSITORY_PASSWORD, password);
	}

	public void setWebRepositoryURL(String url) {
		ps.setStringProperty(WEB_REPOSITORY_URL, url);
	}

	public boolean getFirstTimeOpenAPrintAndGetWebInstruments() {
		return ps.getBooleanProperty(FIRST_TIME_OPEN_WEBREPOSITORY, true);
	}

	public void setFirstTimeOpenAPrintAndGetWebInstruments(boolean f) {
		ps.setBooleanProperty(FIRST_TIME_OPEN_WEBREPOSITORY, f);
	}

	public File getBookQuickScriptFolder() {
		return ps.getFileProperty(QUICKSCRIPT_FOLDER, new File(aprintfolder, "quickscripts"));
	}

	public void setBookQuickScriptFolder(File folder) {
		ps.setFileProperty(QUICKSCRIPT_FOLDER, folder);
	}

	public File getSearchFolder() {
		return ps.getFileProperty(SEARCH_FOLDER, null);
	}

	public void setSearchFolder(File searchFolder) {
		ps.setFileProperty(SEARCH_FOLDER, searchFolder);
	}

	public int getAdditionalWebRepositoriesCount() {
		return ps.getIntegerProperty(APRINT_ADDITIONAL_WEB_REPOSITORIES_PREFIX + ".count", 0);
	}

	public void setAdditionalWebRepositoriesCount(int newcount) {
		ps.setIntegerProperty(APRINT_ADDITIONAL_WEB_REPOSITORIES_PREFIX + ".count", newcount);
	}

	public String getAdditionalWebRepositoriesName(int i) {
		return ps.getStringProperty(APRINT_ADDITIONAL_WEB_REPOSITORIES_PREFIX + "." + i + ".name", null);
	}

	public void setAdditionalWebRepositoriesName(int i, String name) {
		ps.setStringProperty(APRINT_ADDITIONAL_WEB_REPOSITORIES_PREFIX + "." + i + ".name", name);
	}

	/*
	 * public String getAdditionalWebRepositoriesFolderName(int i) { return
	 * ps.getStringProperty(APRINT_ADDITIONAL_WEB_REPOSITORIES_PREFIX + "." + i
	 * + ".foldername", null); }
	 * 
	 * public void setAdditionalWebRepositoriesFolderName(int i, String
	 * foldername) {
	 * ps.setStringProperty(APRINT_ADDITIONAL_WEB_REPOSITORIES_PREFIX + "." + i
	 * + ".foldername", foldername); }
	 */
	public String getAdditionalWebRepositoriesUrl(int i) {
		return ps.getStringProperty(APRINT_ADDITIONAL_WEB_REPOSITORIES_PREFIX + "." + i + ".url", null);
	}

	public void setAdditionalWebRepositoriesUrl(int i, String url) {
		ps.setStringProperty(APRINT_ADDITIONAL_WEB_REPOSITORIES_PREFIX + "." + i + ".url", url);
	}

	public void setLastSelectedInstrument(String lastSelectedInstrument) {
		ps.setStringProperty(LAST_SELECTED_INSTRUMENT, lastSelectedInstrument);
	}

	public String getLastSelectedInstrument() {
		return ps.getStringProperty(LAST_SELECTED_INSTRUMENT, null);
	}

	public void setForcedLocal(Locale locale) {
		String value = "CURRENT";

		if (locale != null) {
			value = locale.getLanguage();
		}

		ps.setStringProperty(FORCED_LOCAL, value);
	}

	public Locale getForcedLocal() {
		String v = ps.getStringProperty(FORCED_LOCAL, "CURRENT");
		if (v != null && !"CURRENT".equals(v)) {
			return new Locale(v);
		}

		return Locale.getDefault();
	}

	public boolean getToolbarVisibility(String name) {
		return ps.getBooleanProperty(TOOLBARVISIBILITYPREFIX + "." + name, true);
	}

	public void setToolbarVisibility(String name, boolean visibility) {
		ps.setBooleanProperty(TOOLBARVISIBILITYPREFIX + "." + name, visibility);
	}

}
