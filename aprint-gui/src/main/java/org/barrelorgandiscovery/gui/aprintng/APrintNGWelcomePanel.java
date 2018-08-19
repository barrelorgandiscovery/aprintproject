package org.barrelorgandiscovery.gui.aprintng;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionPointProvider;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.WelcomeExtensionExtensionPoint;
import org.barrelorgandiscovery.gui.etl.JModelEditorPanel;
import org.barrelorgandiscovery.gui.script.groovy.APrintGroovyConsole;
import org.barrelorgandiscovery.gui.script.groovy.FileScriptExecutionActionPerformed;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.BareBonesBrowserLaunch;
import org.barrelorgandiscovery.tools.FileNameExtensionFilter;
import org.barrelorgandiscovery.tools.FileTools;
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
public class APrintNGWelcomePanel extends JPanel {

	private static final String MAIN_EXTENSION_SCRIPTS = "extensionscripts"; //$NON-NLS-1$

	/**
	 * 
	 */
	private static final long serialVersionUID = 5447654633578740242L;

	private APrintNG aprintng;

	private IExtension[] extensions;

	private static Logger logger = Logger.getLogger(APrintNGWelcomePanel.class);

	public APrintNGWelcomePanel(APrintNG aprintng, IExtension[] extensions) throws Exception {
		this.aprintng = aprintng;
		this.extensions = extensions;
		initComponents();
	}

	private ImageIcon resize(String resourcename) throws Exception {
		return new ImageIcon(ImageTools.loadImageAndCrop(getClass().getResource(resourcename), 40, 40));
	}

	private void initComponents() throws Exception {

		FormPanel fp = new FormPanel(getClass().getResourceAsStream("welcomePanel.jfrm")); //$NON-NLS-1$


		JButton newBook = (JButton) fp.getButton("newbook") ;// $NON-NLS-1$
		newBook.addActionListener(aprintng);
		newBook.setActionCommand("NEW"); //$NON-NLS-1$
		newBook.setText(Messages.getString("APrintNG.4000") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		newBook.setIcon(new ImageIcon(APrintNG.getAPrintApplicationIcon()));
		
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

		FormAccessor toolsContainer = fp.getFormAccessor("toolscontainer"); //$NON-NLS-1$

		JButton manageinstruments = (JButton) toolsContainer.getButton("manageinstruments"); //$NON-NLS-1$
		manageinstruments.addActionListener(aprintng);
		manageinstruments.setActionCommand("INSTRUMENTREPOSITORY"); //$NON-NLS-1$
		manageinstruments.setIcon(resize("logomanageinstruments.png")); //$NON-NLS-1$
		manageinstruments.setText(Messages.getString("APrintNGWelcomeFrame.200")); //$NON-NLS-1$

		JButton modelEditor = (JButton) toolsContainer.getButton("modeleditor"); //$NON-NLS-1$
		modelEditor.addActionListener(aprintng);
		modelEditor.setActionCommand("MODELEDITOR"); //$NON-NLS-1$
		modelEditor.setIcon(new ImageIcon(JModelEditorPanel.class.getResource("model-editor.png"))); //$NON-NLS-1$
		modelEditor.setText("Model Editor");
		
		// deactivate the model editor for version 2017
		modelEditor.setEnabled(aprintng.ENABLE_MODELEDITOR);

		JButton standaloneScaleEditor = (JButton) toolsContainer.getButton("scaleditor");//$NON-NLS-1$
		standaloneScaleEditor.setIcon(resize("welcomescaaleeditor.png"));//$NON-NLS-1$
		standaloneScaleEditor.setText(Messages.getString("APrintNGWelcomePanel.30")); //$NON-NLS-1$
		standaloneScaleEditor.addActionListener(aprintng);
		standaloneScaleEditor.setActionCommand("SCALEDITOR"); //$NON-NLS-1$

		JButton gobarrelOrganDiscovery = (JButton) toolsContainer.getButton("gobarrelorgandiscovery");//$NON-NLS-1$
		gobarrelOrganDiscovery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					BareBonesBrowserLaunch.openURL("http://www.barrel-organ-discovery.org"); //$NON-NLS-1$
				} catch (Throwable t) {
					logger.error(t.getMessage(), t);
					BugReporter.sendBugReport();
				}
			}
		});
		gobarrelOrganDiscovery.setIcon(resize("welcomemanageinstrument.png")); //$NON-NLS-1$
		gobarrelOrganDiscovery.setText(Messages.getString("APrintNGWelcomeFrame.400")); //$NON-NLS-1$
		gobarrelOrganDiscovery.setToolTipText(Messages.getString("APrintNGWelcomeFrame.401")); //$NON-NLS-1$

		JLabel welcomemessage = (JLabel) fp.getComponentByName("welcomemessage"); //$NON-NLS-1$
		welcomemessage.setText(Messages.getString("APrintNGWelcomeFrame.15")); //$NON-NLS-1$

		JLabel toolslabel = (JLabel) fp.getComponentByName("lbltools");//$NON-NLS-1$
		toolslabel.setText(Messages.getString("APrintNGWelcomePanel.31")); //$NON-NLS-1$

		// adding extension buttons
		JLabel extensionlabel = (JLabel) fp.getComponentByName("labelextension");//$NON-NLS-1$
		extensionlabel.setText(Messages.getString("APrintNGWelcomePanelInclude.0")); //$NON-NLS-1$

		JLabel dragndrop = (JLabel) fp.getComponentByName("dragndrop"); //$NON-NLS-1$
		dragndrop.setText(Messages.getString("APrintNGWelcomePanelInclude.2")); //$NON-NLS-1$
		String tooltiptextdrag = "<html>" + Messages.getString("APrintNGWelcomePanelInclude.3") + "</html>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		dragndrop.setToolTipText(tooltiptextdrag);

		JPanel panelExtension = new JPanel();
		panelExtension.setLayout(new WrappingLayout());
		
		
		FormAccessor formAccessor = fp.getFormAccessor();
		formAccessor.replaceBean(fp.getComponentByName("extensionplace"),new JScrollPane( panelExtension, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));//$NON-NLS-1$

		/////////////////////////////////////// add extensions
		WelcomeExtensionExtensionPoint[] allPoints = ExtensionPointProvider
				.getAllPoints(WelcomeExtensionExtensionPoint.class, extensions);

		for (int i = 0; i < allPoints.length; i++) {
			WelcomeExtensionExtensionPoint welcomeExtensionExtensionPoint = allPoints[i];
			if (welcomeExtensionExtensionPoint != null) {
				try {
					welcomeExtensionExtensionPoint.addComponentInPanel(panelExtension);
					logger.debug("extension for :" //$NON-NLS-1$
							+ welcomeExtensionExtensionPoint + " added");//$NON-NLS-1$
				} catch (Throwable t) {
					logger.error("error adding extension point "//$NON-NLS-1$
							+ welcomeExtensionExtensionPoint + ":"//$NON-NLS-1$
							+ t.getMessage(), t);
				}
			}
		}

		// load the main scripts
		loadMainScriptsInPanel(panelExtension);

		setLayout(new BorderLayout());
		add(fp, BorderLayout.CENTER);
		doLayout();
	}

	private void loadMainScriptsInPanel(JPanel panelExtension) {
		APrintProperties aprintProperties = aprintng.getProperties();

		File aprintFolder = aprintProperties.getAprintFolder();
		File mainScriptFolder = new File(aprintFolder, MAIN_EXTENSION_SCRIPTS);

		if (!mainScriptFolder.exists()) {
			logger.info("No " + MAIN_EXTENSION_SCRIPTS + " folder, don't load the script as extension");
			return;
		}

		File[] extensionGroovyScript = mainScriptFolder.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (pathname == null)
					return false;

				if (pathname.getName().endsWith('.' + APrintGroovyConsole.APRINTGROOVYSCRIPTEXTENSION))
					return true;

				return false;
			}
		});

		if (extensionGroovyScript == null)
			return;

		for (File f : extensionGroovyScript) {
			logger.debug("load script " + f);

			Image icon = APrintNG.getAPrintApplicationIcon();
			File iconFile = new File(f.getParentFile(), f.getName() + ".png");
			if (iconFile.exists()) {
				try {
					icon = ImageTools.loadImage(iconFile.toURL());
				} catch (Exception ex) {
					logger.error("error loading image " + iconFile + " : " + ex.getMessage(), ex);
				}
			}

			String filename = f.getName();
			JButton btn = new JButton(
					filename.substring(0, filename.indexOf(APrintGroovyConsole.APRINTGROOVYSCRIPTEXTENSION) - 1));

			btn.setIcon(new ImageIcon(icon));
			final File finalFile = f;
			btn.addActionListener(new FileScriptExecutionActionPerformed(finalFile, aprintng));

			panelExtension.add(btn, null);
		}

	}

}
