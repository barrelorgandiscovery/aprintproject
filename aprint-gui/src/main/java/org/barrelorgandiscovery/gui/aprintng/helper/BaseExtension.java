package org.barrelorgandiscovery.gui.aprintng.helper;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionPoint;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.extensions.SimpleExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.HelpMenuItemsExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.OptionMenuItemsExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.InitNGExtensionPoint;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.tools.StringTools;
import org.barrelorgandiscovery.tools.SwingUtils;

import com.jeta.forms.components.panel.FormPanel;

/**
 * Base extension for aprintng, provide the loading / plugin, about action
 * provide also a preference configuration for the extension.
 * 
 * @author pfreydiere
 * 
 */
public abstract class BaseExtension implements IExtension,
		HelpMenuItemsExtensionPoint, InitNGExtensionPoint,
		OptionMenuItemsExtensionPoint {

	private final static Logger logger = Logger.getLogger(BaseExtension.class);

	protected ExtensionPoint[] extspts;

	/**
	 * Application reference
	 */
	protected APrintNG application;

	/**
	 * Object permitting to save extension properties and user preferences,
	 * saving a lot of time for the user
	 */
	protected IPrefsStorage extensionPreferences;

	/**
	 * Constructor
	 * 
	 * @throws Exception
	 */
	public BaseExtension() throws Exception {

		List<ExtensionPoint> e = new ArrayList<ExtensionPoint>();

		setupExtensionPoint(e);

		extspts = e.toArray(new ExtensionPoint[0]);

	}

	/**
	 * At the Extension startup, this method is called for creating extension
	 * points
	 * 
	 * Method permitting to create new hook in derived classes
	 * 
	 * @param initExtensionPoints
	 */
	protected void setupExtensionPoint(List<ExtensionPoint> initExtensionPoints)
			throws Exception {

		ExtensionPoint[] pts = new ExtensionPoint[] {
				createExtensionPoint(HelpMenuItemsExtensionPoint.class),
				createExtensionPoint(InitNGExtensionPoint.class),
				createExtensionPoint(OptionMenuItemsExtensionPoint.class) };

		initExtensionPoints.addAll(Arrays.asList(pts));
	}

	/**
	 * Helper method for creating a new Hook Registration for the extension the
	 * implementation is the class itself
	 * 
	 * @param extensionPoint
	 * @return
	 * @throws Exception
	 */
	protected ExtensionPoint createExtensionPoint(Class extensionPoint)
			throws Exception {

		return createExtensionPoint(extensionPoint, this);
	}

	/**
	 * Helper method for creating a new hook,
	 * 
	 * @param extensionPoint
	 * @param classImplementing
	 * @return
	 * @throws Exception
	 */
	protected ExtensionPoint createExtensionPoint(Class extensionPoint,
			Object classImplementing) throws Exception {

		if (!(extensionPoint.isAssignableFrom(classImplementing.getClass()))) {
			throw new Exception("Implementation Error, Class "
					+ this.getClass() + " Must Implements "
					+ extensionPoint.getName());
		}

		return new SimpleExtensionPoint(extensionPoint, classImplementing);
	}

	public ExtensionPoint[] getExtensionPoints() {
		return extspts;
	}

	/**
	 * base implementation of the extension initialization, this implementation
	 * create a user preference store for the extension (permit the extension to
	 * store specific user parameters) ie : extensionPreferences
	 */
	public void init(APrintNG f) {
		
		this.application = f;
		APrintProperties aprintproperties = application.getProperties();

		logger.debug("creating properties file for extension");
		this.extensionPreferences = new FilePrefsStorage(new File(
				aprintproperties.getAprintFolder(),
				StringTools.convertToPhysicalName(getName()) + ".properties"));

		try {
			this.extensionPreferences.load();

		} catch (Exception ex) {
			logger.error(
					"error while reading the properties :" + ex.getMessage(),
					ex);
		}
		this.extensionPreferences.save();
	}

	/**
	 * Override this method for giving a name to the extension
	 */
	public abstract String getName();

	/**
	 * Override an add wished help Menu commands (about for example)
	 */
	public void addHelpMenuItem(JMenu helpMenu) {

		JMenuItem mih = helpMenu.add("About " + getName() + " ...");

		mih.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {

					final JDialog d = new JDialog((Frame) application
							.getOwner());
					d.setTitle("About ..");

					JPanel p = createAboutForm(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							d.dispose();
						}
					});

					d.getContentPane().add(p);

					d.setSize(260, 170);

					d.setModal(true);
					SwingUtils.center(d);
					d.setVisible(true);

				} catch (Exception ex) {
					logger.error(
							"Error in getting the about form about the extension",
							ex);
				}
			}
		});

	}

	/**
	 * Override this method for permitting adding elements in the option menu
	 */
	public void addOptionMenuItem(JMenu options) {

	}

	/**
	 * Default informations on the author
	 */
	protected String defaultAboutAuthor = "<unknown>";

	/**
	 * Default information on the version of the extension (for the about form)
	 */
	protected String defaultAboutVersion = "<unknown>";
	

	private void replaceLabelIfFound(FormPanel fp, String label, String text) {

		JLabel l = (JLabel) fp.getLabel(label);
		if (l != null)
			l.setText(text);

	}

	/**
	 * Default method for creating the about panel
	 * 
	 * @return
	 * @throws Exception
	 */
	protected JPanel createAboutForm(ActionListener okActionListener)
			throws Exception {

		FormPanel fp = new FormPanel(
				BaseExtension.class
						.getResourceAsStream("genericaboutform.jfrm"));

		replaceLabelIfFound(fp, "author", defaultAboutAuthor);
		replaceLabelIfFound(fp, "version", defaultAboutVersion);
		replaceLabelIfFound(fp, "extensionname", getName());

		JButton b = (JButton) fp.getButton("ok");
		b.addActionListener(okActionListener);

		return fp;
	}

}