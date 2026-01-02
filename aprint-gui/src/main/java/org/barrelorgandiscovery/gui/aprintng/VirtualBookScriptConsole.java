package org.barrelorgandiscovery.gui.aprintng;

import groovy.lang.Binding;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.concurrent.Future;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.JobEvent;
import org.barrelorgandiscovery.gui.aedit.GlobalVirtualBookUndoOperation;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.script.groovy.APrintGroovyConsolePanel;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

public class VirtualBookScriptConsole extends JDialog {

	private static Logger logger = Logger
			.getLogger(VirtualBookScriptConsole.class);

	private JEditableVirtualBookComponent pianoroll;

	private AsyncJobsManager asyncJobsManager;

	private APrintNGGeneralServices services;

	private APrintProperties aPrintProperties;

	private QuickScriptManager scriptManager;

	private Instrument currentInstrument;
	
	private JPanel toolbarsPanel;

	/**
	 * Current edited script
	 */
	private String currentEditedScript = null;

	private APrintGroovyConsolePanel p;
	
	private JLabel statusLabel;
	private JButton executeButton;

	public VirtualBookScriptConsole(Frame owner, String title,
			JEditableVirtualBookComponent pianoroll,
			JPanel toolbarsPanel,
			AsyncJobsManager asyncJobsManager,
			APrintNGGeneralServices services, Instrument instrument,
			APrintProperties aPrintProperties, QuickScriptManager scriptManager)
			throws HeadlessException {
		super(owner, title, false); // Non-modal dialog

		this.pianoroll = pianoroll;
		this.toolbarsPanel = toolbarsPanel;
		this.asyncJobsManager = asyncJobsManager;
		this.services = services;
		this.currentInstrument = instrument;

		this.aPrintProperties = aPrintProperties;
		this.scriptManager = scriptManager;

		initComponents();
		setupDialogProperties();
	}

	private void initComponents() {

		p = new APrintGroovyConsolePanel();
		try {
			p.appendOutputNl(
					">>  "	+ "virtualbook" + " " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ Messages
									.getString("APrintNGVirtualBookInternalFrame.21"), null); //$NON-NLS-1$
			p.appendOutputNl(
					">>  " //$NON-NLS-1$
							+ "pianoroll" //$NON-NLS-1$
							+ " " //$NON-NLS-1$
							+ Messages
									.getString("APrintNGVirtualBookInternalFrame.25"), //$NON-NLS-1$
					null);
			p.appendOutputNl(">>  " //$NON-NLS-1$
					+ "currentinstrument" //$NON-NLS-1$
					+ " " //$NON-NLS-1$
					+ "current used instrument", //$NON-NLS-1$
					null);
			p.appendOutputNl(">>  " //$NON-NLS-1$
					+ "toolbarspanel" //$NON-NLS-1$
					+ " " //$NON-NLS-1$
					+ "panel containing the toolbars", //$NON-NLS-1$
					null);

		} catch (Exception ex) {
			logger.error("fail to output variables in console", ex); //$NON-NLS-1$
		}

		// Main toolbar panel with better spacing
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new WrappingLayout(WrappingLayout.LEFT, 8, 8));
		buttonPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

		JToolBar scriptToolbar = new JToolBar();
		scriptToolbar.setFloatable(false);
		scriptToolbar.setBorderPainted(true);
		scriptToolbar.setBorder(new EmptyBorder(4, 4, 4, 4));

		executeButton = new JButton(
				Messages.getString("APrintNGVirtualBookInternalFrame.27")); //$NON-NLS-1$
		executeButton.setIcon(new ImageIcon(getClass().getResource("misc.png")));
		executeButton.setToolTipText("Execute the script (Ctrl+Enter)");
		// Add keyboard shortcut
		executeButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
			.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "execute");
		executeButton.getActionMap().put("execute", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				executeButton.doClick();
			}
		});
		executeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					Binding b = p.getCurrentBindingRef();
					b.setProperty("virtualbook", pianoroll //$NON-NLS-1$
							.getVirtualBook());
					b.setProperty("pianoroll", pianoroll); //$NON-NLS-1$
					b.setProperty("services", services); //$NON-NLS-1$
					b.setProperty("currentinstrument", currentInstrument); //$NON-NLS-1$
					b.setProperty("toolbarspanel", toolbarsPanel); //$NON-NLS-1$
					
					// Expose MCP context to scripts if available (using reflection to avoid compile-time dependency)
					try {
						Class<?> mcpExtensionClass = Class.forName("org.barrelorgandiscovery.mcp.MCPExtension");
						java.lang.reflect.Method getInstanceMethod = mcpExtensionClass.getMethod("getInstance");
						Object mcpExtension = getInstanceMethod.invoke(null);
						if (mcpExtension != null) {
							java.lang.reflect.Method getContextMethod = mcpExtensionClass.getMethod("getContext");
							Object mcpContext = getContextMethod.invoke(mcpExtension);
							if (mcpContext != null) {
								b.setProperty("mcpcontext", mcpContext); //$NON-NLS-1$
								logger.debug("MCP context exposed to script as 'mcpcontext'");
							}
						}
					} catch (ClassNotFoundException cnfe) {
						// MCP extension not available - this is OK
						logger.debug("MCP extension not found, context not exposed to script");
					} catch (Exception mcpEx) {
						logger.debug("Could not expose MCP context to script: " + mcpEx.getMessage());
					}

					// Update autocompletion with new binding variables
					p.updateAutocompletion();

					p.clearConsole();
					updateStatus("Executing script...");
					executeButton.setEnabled(false);

					try {

						logger.debug("make the undo operation for the script"); //$NON-NLS-1$
						GlobalVirtualBookUndoOperation gvb = new GlobalVirtualBookUndoOperation(
								pianoroll.getVirtualBook(),
								Messages.getString("APrintNGVirtualBookInternalFrame.31"), pianoroll); //$NON-NLS-1$

						pianoroll.getUndoStack().push(gvb);

						Future<?> f = p.run();
						pianoroll.startEventTransaction();
						asyncJobsManager.submitAlreadyExecutedJobToTrack(f,
								new JobEvent() {
									public void jobAborted() {
										// TODO Auto-generated method stub
										pianoroll.endEventTransaction();
										updateStatus("Script execution aborted");
										executeButton.setEnabled(true);
									}

									public void jobError(Throwable t) {
										try {

											logger.error(
													"error while executing script :" //$NON-NLS-1$
															+ t.getMessage(), t);
											p.appendOutput(t);
											updateStatus("Error: " + t.getMessage());

										} catch (Exception x) {
											logger.debug(x);
										}
										pianoroll.endEventTransaction();
										executeButton.setEnabled(true);
									}

									public void jobFinished(Object result) {
										try {
											p.appendOutputNl(
													result == null ? "null" //$NON-NLS-1$
															: result.toString(),
													null);
											updateStatus("Script executed successfully");

										} catch (Exception ex) {
											logger.error(
													"error in executing script :" //$NON-NLS-1$
															+ ex.getMessage(),
													ex);
											updateStatus("Error displaying result");
										}
										pianoroll.endEventTransaction();
										executeButton.setEnabled(true);
									}

								});

					} catch (Throwable t) {
						logger.error("error while executing script :" //$NON-NLS-1$
								+ t.getMessage(), t);
						p.appendOutput(t);
						updateStatus("Error: " + t.getMessage());
						executeButton.setEnabled(true);
					}

				} catch (Exception ex) {
					logger.error("error while executing script :" //$NON-NLS-1$
							+ ex.getMessage(), ex);
					JMessageBox.showMessage(services.getOwnerForDialog(),
							"error while executing script :" //$NON-NLS-1$
									+ ex.getMessage());
					updateStatus("Error: " + ex.getMessage());
					executeButton.setEnabled(true);
				}

			}
		});

		scriptToolbar.add(executeButton);

		JButton clear = new JButton(
				Messages.getString("APrintNGVirtualBookInternalFrame.36")); //$NON-NLS-1$
		clear.setIcon(new ImageIcon(getClass().getResource("ark_new.png")));//$NON-NLS-1$
		clear.setToolTipText("Clear the console output");
		clear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				p.clearConsole();
				updateStatus("Console cleared");
			}
		});

		scriptToolbar.add(clear);

		buttonPanel.add(scriptToolbar);
		
		// Add visual separator
		buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));

		// quick scripts ...

		JToolBar quickScriptToolbar = new JToolBar();
		quickScriptToolbar.setFloatable(false);
		quickScriptToolbar.setBorderPainted(true);
		quickScriptToolbar.setBorder(new EmptyBorder(4, 4, 4, 4));

		final JButton quickScript = new JButton(
				Messages.getString("APrintNGVirtualBookInternalFrame.1007")); //$NON-NLS-1$
		quickScript.setIcon(new ImageIcon(getClass().getResource(
				"tool_dock.png"))); //$NON-NLS-1$
		quickScript.setToolTipText("Open Script Manager (Load, Save, Delete, Rename scripts)");
		quickScript.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// Open script manager dialog
					ScriptManagerDialog dialog = new ScriptManagerDialog(
						VirtualBookScriptConsole.this,
						scriptManager,
						new ScriptManagerDialog.ScriptManagerCallback() {
							@Override
							public void onLoadScript(String scriptName) {
								try {
									StringBuffer scriptContent = scriptManager.loadScript(scriptName);
									p.setScriptContent(scriptContent.toString());
									p.clearDirty();
									currentEditedScript = scriptName;
									logger.debug("current script :" + scriptName);
									updateStatus("Loaded script: " + scriptName);
								} catch (Exception ex) {
									logger.error("fail to load script " + scriptName + ":" + ex.getMessage(), ex);
									JMessageBox.showMessage(services.getOwnerForDialog(),
										"error while loading script :" + scriptName + "\n" + ex.getMessage());
								}
							}
							
							@Override
							public boolean onSaveScript(String scriptName) {
								try {
									scriptManager.saveScript(scriptName, new StringBuffer(p.getScriptContent()));
									currentEditedScript = scriptName;
									updateStatus("Script saved: " + scriptName);
									return true;
								} catch (Exception ex) {
									logger.error("error writing quick scripts :" + ex.getMessage(), ex);
									JMessageBox.showMessage(services.getOwnerForDialog(),
										"error while saving script :" + scriptName + "\n" + ex.getMessage());
									return false;
								}
							}
							
							@Override
							public String getCurrentScriptContent() {
								return p.getScriptContent();
							}
							
							@Override
							public String getCurrentScriptName() {
								return currentEditedScript;
							}
						}
					);
					dialog.setVisible(true);
				} catch (Exception ex) {
					logger.error("error opening script manager :" + ex.getMessage(), ex);
					BugReporter.sendBugReport();
					JMessageBox.showMessage(services.getOwnerForDialog(),
						Messages.getString("APrintNGVirtualBookInternalFrame.1013")); //$NON-NLS-1$
				}
			}
		});

		quickScriptToolbar.add(quickScript);

		// Quick save button (still available for convenience)
		JButton saveCurrentEditedQuickScript = new JButton("Save Script"); //$NON-NLS-1$
		saveCurrentEditedQuickScript.setIcon(new ImageIcon(getClass()
				.getResource("filesave.png"))); //$NON-NLS-1$
		saveCurrentEditedQuickScript.setToolTipText("Save current script (Ctrl+S)");
		// Add keyboard shortcut
		saveCurrentEditedQuickScript.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
			.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "save");
		saveCurrentEditedQuickScript.getActionMap().put("save", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveCurrentEditedQuickScript.doClick();
			}
		});
		saveCurrentEditedQuickScript.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});

		quickScriptToolbar.add(saveCurrentEditedQuickScript);

		buttonPanel.add(quickScriptToolbar);

		// Status bar at the bottom
		statusLabel = new JLabel("Ready");
		statusLabel.setBorder(new EmptyBorder(4, 8, 4, 8));
		
		JPanel statusPanel = new JPanel(new BorderLayout());
		statusPanel.add(new JSeparator(), BorderLayout.NORTH);
		statusPanel.add(statusLabel, BorderLayout.WEST);
		statusPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

		// Main content layout
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(buttonPanel, BorderLayout.NORTH);
		getContentPane().add(p, BorderLayout.CENTER);
		getContentPane().add(statusPanel, BorderLayout.SOUTH);

		setPreferredSize(new Dimension(900, 650));
		setMinimumSize(new Dimension(600, 400));
	}
	
	/**
	 * Setup dialog properties for better user experience
	 */
	private void setupDialogProperties() {
		// Make dialog resizable
		setResizable(true);
		
		// Center on screen if no owner, or relative to owner
		if (getOwner() != null) {
			Point ownerLocation = getOwner().getLocation();
			Dimension ownerSize = getOwner().getSize();
			Dimension dialogSize = getPreferredSize();
			setLocation(
				ownerLocation.x + (ownerSize.width - dialogSize.width) / 2,
				ownerLocation.y + (ownerSize.height - dialogSize.height) / 2
			);
		} else {
			setLocationRelativeTo(null);
		}
		
		// Set default close operation
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		pack();
	}
	
	/**
	 * Update status message
	 */
	private void updateStatus(String message) {
		if (statusLabel != null) {
			SwingUtilities.invokeLater(() -> {
				statusLabel.setText(message);
			});
		}
	}

	@Override
	public void dispose() {

		super.dispose();
	}

	private void save() {

		logger.debug("save");

		if (currentEditedScript != null) {
			try {

				scriptManager.saveScript(currentEditedScript, new StringBuffer(
						p.getScriptContent()));

				JOptionPane.showMessageDialog(this, "Script saved");
				updateStatus("Script saved: " + currentEditedScript);

			} catch (Exception ex) {
				logger.error("error writing quick scripts :" //$NON-NLS-1$
						+ ex.getMessage(), ex);
				BugReporter.sendBugReport();
				JMessageBox.showMessage(services.getOwnerForDialog(), Messages
						.getString("APrintNGVirtualBookInternalFrame.1019")); //$NON-NLS-1$
			}

		} else {
			logger.debug("no current name, ask for the user ...");
			saveAs();
		}

	}

	private void saveAs() {
		try {
			String scriptContent = p.getScriptContent();

			String scriptName = JOptionPane.showInputDialog(Messages
					.getString("APrintNGVirtualBookInternalFrame.1015")); //$NON-NLS-1$
			if (scriptName != null) {

				scriptManager.saveScript(scriptName, new StringBuffer(
						scriptContent));

				currentEditedScript = scriptName;

				JOptionPane.showMessageDialog(this, "Script saved");
				updateStatus("Script saved: " + scriptName);

			}
		} catch (Exception ex) {
			logger.error("error writing quick scripts :" //$NON-NLS-1$
					+ ex.getMessage(), ex);
			BugReporter.sendBugReport();
			JMessageBox
					.showMessage(services.getOwnerForDialog(), Messages
							.getString("APrintNGVirtualBookInternalFrame.1019")); //$NON-NLS-1$
		}
	}
	
	/**
	 * Obtient le panel de console Groovy
	 * @return Le panel de console
	 */
	public APrintGroovyConsolePanel getConsolePanel() {
		return p;
	}
	
	/**
	 * Définit le contenu du script
	 * @param scriptContent Le contenu du script
	 */
	public void setScriptContent(String scriptContent) {
		if (p != null) {
			p.setScriptContent(scriptContent);
		}
	}
	
	/**
	 * Obtient le contenu du script
	 * @return Le contenu du script
	 */
	public String getScriptContent() {
		return p != null ? p.getScriptContent() : "";
	}

}
