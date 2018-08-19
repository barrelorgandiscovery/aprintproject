package org.barrelorgandiscovery.gui.aprintng;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionPointProvider;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.WelcomeExtensionExtensionPoint;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.BareBonesBrowserLaunch;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;

/**
 * Hello frame
 * 
 * @author Freydiere Patrice
 * 
 */
@Deprecated
public class APrintNGWelcomeFrame extends APrintNGInternalFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5447654633578740242L;

	private static Logger logger = Logger.getLogger(APrintNGWelcomeFrame.class);

	
	private APrintNG aprintng;
	
	private IExtension[] extensions;

	public APrintNGWelcomeFrame(APrintNG aprintng, IExtension[] extensions)
			throws Exception {
		super(aprintng.getProperties().getFilePrefsStorage(),
		Messages.getString("APrintNGWelcomeFrame.0"), true, true, false, false); //$NON-NLS-1$
		this.aprintng = aprintng;
		this.extensions = extensions;
		initComponents();
	}
	
	@Override
	protected String getInternalFrameNameForPreferences() {
		return "aprintngwelcomepage";
	}

	private ImageIcon resize(String resourcename) throws Exception {
		return new ImageIcon(ImageTools.loadImageAndCrop(getClass()
				.getResource(resourcename), 40, 40));
	}

	private void initComponents() throws Exception {

		FormPanel fp = new FormPanel(getClass().getResourceAsStream(
				"welcome.jfrm")); //$NON-NLS-1$

		JButton openbook = (JButton) fp.getButton("openbookfile"); //$NON-NLS-1$
		openbook.addActionListener(aprintng);
		openbook.setActionCommand("LOAD"); //$NON-NLS-1$
		openbook.setIcon(resize("welcomeopen.png")); //$NON-NLS-1$
		openbook.setText(Messages.getString("APrintNGWelcomeFrame.201")); //$NON-NLS-1$

		JButton searchBook = (JButton) fp.getButton("searchbook"); // //$NON-NLS-1$
		searchBook.addActionListener(aprintng);
		searchBook.setActionCommand("SEARCH"); //$NON-NLS-1$
		searchBook.setIcon(resize("kghostview.png"));//$NON-NLS-1$
		searchBook.setText(Messages.getString("APrintNGWelcomeFrame.301")); //$NON-NLS-1$

		JButton transformmidi = (JButton) fp.getButton("transformmidi"); //$NON-NLS-1$
		transformmidi.addActionListener(aprintng);
		transformmidi.setActionCommand("IMPORT"); //$NON-NLS-1$
		transformmidi.setIcon(resize("welcomeimportmidi.png")); //$NON-NLS-1$
		transformmidi.setText(Messages.getString("APrintNGWelcomeFrame.202")); //$NON-NLS-1$

		JButton manageinstruments = (JButton) fp.getButton("manageinstruments"); //$NON-NLS-1$
		manageinstruments.addActionListener(aprintng);
		manageinstruments.setActionCommand("INSTRUMENTREPOSITORY"); //$NON-NLS-1$
		manageinstruments.setIcon(resize("logomanageinstruments.png")); //$NON-NLS-1$
		manageinstruments.setText(Messages
				.getString("APrintNGWelcomeFrame.200")); //$NON-NLS-1$

		JButton gobarrelOrganDiscovery = (JButton) fp
				.getButton("gobarrelorgandiscovery");//$NON-NLS-1$
		gobarrelOrganDiscovery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					BareBonesBrowserLaunch
							.openURL("http://www.barrel-organ-discovery.org"); //$NON-NLS-1$
				} catch (Throwable t) {
					logger.error(t.getMessage(), t);
					BugReporter.sendBugReport();
				}
			}
		});
		gobarrelOrganDiscovery.setIcon(resize("welcomemanageinstrument.png")); //$NON-NLS-1$
		gobarrelOrganDiscovery.setText(Messages
				.getString("APrintNGWelcomeFrame.400")); //$NON-NLS-1$
		gobarrelOrganDiscovery.setToolTipText(Messages
				.getString("APrintNGWelcomeFrame.401")); //$NON-NLS-1$

		JButton close = (JButton) fp.getButton("close"); //$NON-NLS-1$
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				APrintNGWelcomeFrame.this.setVisible(false);
			}
		});
		close.setText(Messages.getString("APrintNGWelcomeFrame.300")); //$NON-NLS-1$

		JButton help = (JButton) fp.getButton("help"); //$NON-NLS-1$
		help.addActionListener(aprintng);
		help.setActionCommand("HELP");//$NON-NLS-1$
		help.setText(Messages.getString("APrintNGWelcomeFrame.100")); //$NON-NLS-1$
		help.setIcon(new ImageIcon(getClass().getResource("help.png"))); //$NON-NLS-1$

		JLabel aprintbanner = (JLabel) fp.getComponentByName("aprintbanner"); //$NON-NLS-1$
		aprintbanner.setText(Messages.getString("APrintNGWelcomeFrame.13")); //$NON-NLS-1$
		JLabel welcomemessage = (JLabel) fp
				.getComponentByName("welcomemessage"); //$NON-NLS-1$
		welcomemessage.setText(Messages.getString("APrintNGWelcomeFrame.15")); //$NON-NLS-1$

		// adding extension buttons
		JLabel extensionlabel = (JLabel) fp
				.getComponentByName("labelextension");//$NON-NLS-1$
		extensionlabel.setText("Extensions :");

		JPanel panelExtension = new JPanel();
		FormAccessor formAccessor = fp.getFormAccessor();
		formAccessor.replaceBean(
				fp.getComponentByName("extensionplace"), panelExtension);//$NON-NLS-1$

		WelcomeExtensionExtensionPoint[] allPoints = ExtensionPointProvider
				.getAllPoints(WelcomeExtensionExtensionPoint.class, extensions);

		for (int i = 0; i < allPoints.length; i++) {
			WelcomeExtensionExtensionPoint welcomeExtensionExtensionPoint = allPoints[i];
			if (welcomeExtensionExtensionPoint != null) {
				try {
					welcomeExtensionExtensionPoint
							.addComponentInPanel(panelExtension);
					logger.debug("extension for :"
							+ welcomeExtensionExtensionPoint + " added");
				} catch (Throwable t) {
					logger.error(
							"error adding extension point "
									+ welcomeExtensionExtensionPoint + ":"
									+ t.getMessage(), t);
					BugReporter.sendBugReport();
				}
			}
		}

		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(fp, BorderLayout.CENTER);

	}
}
