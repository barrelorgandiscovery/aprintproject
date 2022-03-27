package org.barrelorgandiscovery.gui.aprintng;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.etl.IConsoleShowListener;
import org.barrelorgandiscovery.gui.etl.JModelEditorPanel;
import org.barrelorgandiscovery.gui.etl.template.ModelTemplateMarker;
import org.barrelorgandiscovery.gui.script.groovy.ASyncConsoleOutput;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.model.ContextVariables;
import org.barrelorgandiscovery.model.DefaultModelStepRegistry;
import org.barrelorgandiscovery.model.ModelStep;
import org.barrelorgandiscovery.model.ModelStepRegistry;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.noos.xing.mydoggy.ToolWindow;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;

/**
 * frame embedding the model into APrint Framework
 *
 * @author pfreydiere
 */
public class APrintNGModelFrame extends APrintNGInternalFrame {

	/** */
	private static final long serialVersionUID = 4580244042129834261L;

	private static Logger logger = Logger.getLogger(APrintNGModelFrame.class);

	private APrintNGGeneralServices services;

	private JModelEditorPanel modeleditor;

	private MyDoggyToolWindowManager toolWindowManager;

	private ASyncConsoleOutput aSyncConsoleOutput;

	private ToolWindow tConsolewindow;

	public APrintNGModelFrame(APrintProperties aprintproperties, APrintNGGeneralServices services) throws Exception {
		super(aprintproperties.getFilePrefsStorage(), "Model Editor", true, true, true, true); //$NON-NLS-1$
		assert services != null;
		this.services = services;
		setIconImage(ImageTools.loadImage(JModelEditorPanel.class, "model-editor.png")); //$NON-NLS-1$
		initComponents();
	}

	/** init components */
	protected void initComponents() throws Exception {

		// add the toolbar manager
		MyDoggyToolWindowManager myDoggyToolWindowManager = new MyDoggyToolWindowManager();
		this.toolWindowManager = myDoggyToolWindowManager;

		JPanel outputOfScript = new JPanel();
		outputOfScript.setLayout(new BorderLayout());

		JTextPane textArea = new JTextPane();
		JScrollPane textScrollPane = new JScrollPane(textArea);

		aSyncConsoleOutput = new ASyncConsoleOutput(textArea, null);

		outputOfScript.add(textScrollPane, BorderLayout.CENTER);
		JButton clearConsoleBtn = new JButton("Clear");
		clearConsoleBtn.addActionListener((action) -> {
			aSyncConsoleOutput.clearConsole();
		});
		JToolBar tbScriptOutputConsole = new JToolBar(JToolBar.HORIZONTAL);
		tbScriptOutputConsole.add(clearConsoleBtn);
		outputOfScript.add(tbScriptOutputConsole, BorderLayout.NORTH);

		// add console window
		tConsolewindow = toolWindowManager.registerToolWindow("console", // Id //$NON-NLS-1$
				"Execution Console", // Title //$NON-NLS-1$
				null, // Icon
				outputOfScript, // Component
				ToolWindowAnchor.BOTTOM);

		Map<String, Object> context = new HashMap<String, Object>();
		context.put(ContextVariables.CONTEXT_SERVICES, services);
		context.put(ContextVariables.CONTEXT_CONSOLE, aSyncConsoleOutput);

		modeleditor = new JModelEditorPanel(new ModelStepRegistry() {
			public List<ModelStep> getRegisteredModelStepList() throws Exception {
				try {
					DefaultModelStepRegistry reg = new DefaultModelStepRegistry();
					return reg.getRegisteredModelStepList();
				} catch (Exception ex) {
					logger.error("error in creating steps :" + ex.getMessage(), ex); //$NON-NLS-1$
					BugReporter.sendBugReport();
					throw new RuntimeException(ex.getMessage(), ex);
				}
			}
		}, services.getRepository(), services.getAsyncJobs(), context, prefixedNamePrefsStorage);

		modeleditor.defineOwner(this);

		modeleditor.setConsole(aSyncConsoleOutput);
		modeleditor.setConsoleShowListener(new IConsoleShowListener() {

			@Override
			public void showConsole() {
				try {
					tConsolewindow.setVisible(true);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}

			}
		});

		getContentPane().setLayout(new BorderLayout());

		// Made all tools available
		for (ToolWindow window : toolWindowManager.getToolWindows()) {
			window.setAvailable(true);
		}

		toolWindowManager.getContentManager().addContent("Main", //$NON-NLS-1$
				null, null, modeleditor);

		getContentPane().add(toolWindowManager, BorderLayout.CENTER);

		JMenuBar menu = new JMenuBar();

		JMenu jFileMenu = new JMenu(Messages.getString("APrintNGModelFrame.3")); //$NON-NLS-1$

		JMenu newMene = new JMenu(Messages.getString("APrintNGModelFrame.4")); //$NON-NLS-1$
		newMene.setIcon(new ImageIcon(APrintNGModelFrame.class.getResource("ark_new.png"))); //$NON-NLS-1$

		JMenuItem blank = new JMenuItem(new NewAction(Messages.getString("APrintNGModelFrame.6"), null)); //$NON-NLS-1$
		newMene.add(blank);

		newMene.addSeparator();

		JMenuItem loadMidi = new JMenuItem(new NewAction(Messages.getString("APrintNGModelFrame.7"), //$NON-NLS-1$
				ModelTemplateMarker.class.getResource("MidiLoadTemplate.model"))); //$NON-NLS-1$
		newMene.add(loadMidi);

		JMenuItem transformTemplate = new JMenuItem(new NewAction(Messages.getString("APrintNGModelFrame.9"), //$NON-NLS-1$
				ModelTemplateMarker.class.getResource("VirtualBookChange.model"))); //$NON-NLS-1$
		newMene.add(transformTemplate);

		jFileMenu.add(newMene);

		jFileMenu.addSeparator();

		JMenuItem openMenuItem = new JMenuItem(new LoadAction());

		jFileMenu.add(openMenuItem);
		JMenuItem saveMenuItem = new JMenuItem(new SaveAction());
		jFileMenu.add(saveMenuItem);

		jFileMenu.addSeparator();
		JMenuItem closeOption = jFileMenu.add(Messages.getString("APrintNGModelFrame.10")); //$NON-NLS-1$
		closeOption.addActionListener( (e) -> { dispose();} );
		
		menu.add(jFileMenu);

		add(menu, BorderLayout.NORTH);
	}

	class SaveAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1905861931062174374L;

		public SaveAction() {
			super(Messages.getString("APrintNGModelFrame.11"), //$NON-NLS-1$
					new ImageIcon(APrintNGModelFrame.class.getResource("filesave.png"))); //$NON-NLS-1$
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//
			modeleditor.save();
		}
	}

	class LoadAction extends AbstractAction {
		/** */
		private static final long serialVersionUID = -1697497407011133001L;

		public LoadAction() {
			super(Messages.getString("APrintNGModelFrame.13"), //$NON-NLS-1$
					new ImageIcon(APrintNGModelFrame.class.getResource("fileopen.png"))); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			modeleditor.load();
		}
	}

	class NewAction extends AbstractAction {

		/** */
		private static final long serialVersionUID = -5727684345592418371L;

		private URL initialContent = null;

		public NewAction(String label, URL initialContent) {
			super(label, new ImageIcon(APrintNGModelFrame.class.getResource("ark_new.png"))); //$NON-NLS-1$
			this.initialContent = initialContent;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				logger.debug("create an empty model"); //$NON-NLS-1$
				modeleditor.newFromTemplate(initialContent);

				// lunch the model check to show the user actions
				// to make for having a fully functionnal element
				logger.debug("validate the constraints to show the elements"); //$NON-NLS-1$
				List<String> errors = modeleditor.validateState();

				if (errors != null && errors.size() > 0) {
					JMessageBox.showMessage(APrintNGModelFrame.this, Messages.getString("APrintNGModelFrame.18")); //$NON-NLS-1$
				}

				logger.debug("end of validation"); //$NON-NLS-1$

			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
				JMessageBox.showError(APrintNGModelFrame.this, ex);
				BugReporter.sendBugReport();
			}
		}
	}
}
