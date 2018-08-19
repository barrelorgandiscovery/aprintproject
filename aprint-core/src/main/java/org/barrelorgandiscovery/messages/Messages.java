package org.barrelorgandiscovery.messages;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.barrelorgandiscovery.tools.ProfilingCondition;

/**
 * Message bundle for the framework
 * 
 * @author Freydiere Patrice
 */
public class Messages {

	private static final String BUNDLE_NAME = Messages.class.getPackage()
			.getName()
			+ ".messages"; // "fr.freydierepatrice.messages.messages";
							// //$NON-NLS-1$

	private static ResourceBundle OVERRIDE_BUNDLE = null;
	private static ResourceBundle RESOURCE_BUNDLE;

	private static File aprintBaseFolder = null;

	private static boolean displayKeyNameInApplication = false;

	private Messages() {

	}

	public static void initLocale(File aprintBaseFolder) {
		initLocale(aprintBaseFolder, false);
	}

	public static void initLocale(File aprintBaseFolder,
			boolean displayKeyNamesInApplication) {
		try {
			
			if (ProfilingCondition.isProfiling())
			{
				RESOURCE_BUNDLE = getEnglishBundle();
				return;
			}

			if (aprintBaseFolder != null ) {

				Messages.aprintBaseFolder = aprintBaseFolder;
				Messages.displayKeyNameInApplication = displayKeyNamesInApplication;

				String p = "file:/" + aprintBaseFolder.getAbsolutePath() + "/";
				System.out.println("define classloader ... with " + p);

				File f = getOverrideLocalizedMessageFile();
				if (f.exists())
					OVERRIDE_BUNDLE = new PropertyResourceBundle(
							new FileInputStream(f));

			}

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}

		RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	}

	public static File getOverrideLocalizedMessageFile() {

		String[] split = BUNDLE_NAME.split("\\.");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < split.length; i++) {
			if (i > 0)
				sb.append("/");
			sb.append(split[i]);
		}
		sb.append('_').append(Locale.getDefault().getLanguage()).append(
				".properties");

		return new File(aprintBaseFolder, sb.toString());
	}

	private static void initBaseMessages() {
		if (RESOURCE_BUNDLE != null)
			return;

		try {
			if (ProfilingCondition.isProfiling()) {
				RESOURCE_BUNDLE = getEnglishBundle();
			} else {
				RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}

	}

	public static String getString(String key) {
		try {
			if (RESOURCE_BUNDLE == null) {
				initBaseMessages();
			}

			assert RESOURCE_BUNDLE != null;

			if (OVERRIDE_BUNDLE != null) {
				try {
					String v = OVERRIDE_BUNDLE.getString(key);
					return displayKeyNameInApplication ? "!" + key + "!" + v
							: v;
				} catch (MissingResourceException e) {

				}
			}

			if (displayKeyNameInApplication) {
				return "!" + key + "!" + RESOURCE_BUNDLE.getString(key);
			}

			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public static String getStringWithoutKey(String key) {
		try {
			if (RESOURCE_BUNDLE == null) {
				return null;
			}

			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public static ResourceBundle getEnglishBundle() {
		try {
			String[] split = BUNDLE_NAME.split("\\.");
			String base = split[split.length - 1];
			return new PropertyResourceBundle(Messages.class
					.getResourceAsStream(base + ".properties"));
		} catch (Exception ex) {
			System.out.println("fail to load english properties");
			return null;
		}
	}
}
