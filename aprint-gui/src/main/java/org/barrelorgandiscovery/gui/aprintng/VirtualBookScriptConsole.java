package org.barrelorgandiscovery.gui.aprintng;

import groovy.lang.Binding;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.Future;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.JobEvent;
import org.barrelorgandiscovery.gui.aedit.GlobalVirtualBookUndoOperation;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
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

	public VirtualBookScriptConsole(Frame owner, String title,
			JEditableVirtualBookComponent pianoroll,
			JPanel toolbarsPanel,
			AsyncJobsManager asyncJobsManager,
			APrintNGGeneralServices services, Instrument instrument,
			APrintProperties aPrintProperties, QuickScriptManager scriptManager)
			throws HeadlessException {
		super(owner, title);

		this.pianoroll = pianoroll;
		this.toolbarsPanel = toolbarsPanel;
		this.asyncJobsManager = asyncJobsManager;
		this.services = services;
		this.currentInstrument = instrument;

		this.aPrintProperties = aPrintProperties;
		this.scriptManager = scriptManager;

		initComponents();
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

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new WrappingLayout());

		JToolBar scriptToolbar = new JToolBar();

		JButton execute = new JButton(
				Messages.getString("APrintNGVirtualBookInternalFrame.27")); //$NON-NLS-1$
		execute.setIcon(new ImageIcon(getClass().getResource("misc.png")));
		execute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					Binding b = p.getCurrentBindingRef();
					b.setProperty("virtualbook", pianoroll //$NON-NLS-1$
							.getVirtualBook());
					b.setProperty("pianoroll", pianoroll); //$NON-NLS-1$
					b.setProperty("services", services); //$NON-NLS-1$
					b.setProperty("currentinstrument", currentInstrument); //$NON-NLS-1$
					b.setProperty("toolbarspanel", toolbarsPanel); //$NON-NLS-1$

					p.clearConsole();

					try {

						logger.debug("make the undo operation for the script"); //$NON-NLS-1$
						GlobalVirtualBookUndoOperation gvb = new GlobalVirtualBookUndoOperation(
								pianoroll.getVirtualBook(),
								Messages.getString("APrintNGVirtualBookInternalFrame.31"), pianoroll); //$NON-NLS-1$

						pianoroll.getUndoStack().push(gvb);

						Future f = p.run();
						pianoroll.startEventTransaction();
						asyncJobsManager.submitAlreadyExecutedJobToTrack(f,
								new JobEvent() {
									public void jobAborted() {
										// TODO Auto-generated method stub
										pianoroll.endEventTransaction();
									}

									public void jobError(Throwable t) {
										try {

											logger.error(
													"error while executing script :" //$NON-NLS-1$
															+ t.getMessage(), t);
											p.appendOutput(t);

										} catch (Exception x) {
											logger.debug(x);
										}
										pianoroll.endEventTransaction();
									}

									public void jobFinished(Object result) {
										try {
											p.appendOutputNl(
													result == null ? "null" //$NON-NLS-1$
															: result.toString(),
													null);

										} catch (Exception ex) {
											logger.error(
													"error in executing script :" //$NON-NLS-1$
															+ ex.getMessage(),
													ex);
										}
										pianoroll.endEventTransaction();
									}

								});

					} catch (Throwable t) {
						logger.error("error while executing script :" //$NON-NLS-1$
								+ t.getMessage(), t);
						p.appendOutput(t);
					}

				} catch (Exception ex) {
					logger.error("error while executing script :" //$NON-NLS-1$
							+ ex.getMessage(), ex);
					JMessageBox.showMessage(services.getOwnerForDialog(),
							"error while executing script :" //$NON-NLS-1$
									+ ex.getMessage());
				}

			}
		});

		scriptToolbar.add(execute);

		JButton clear = new JButton(
				Messages.getString("APrintNGVirtualBookInternalFrame.36")); //$NON-NLS-1$
		clear.setIcon(new ImageIcon(getClass().getResource("ark_new.png")));//$NON-NLS-1$
		clear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				p.clearConsole();
			}
		});

		scriptToolbar.add(clear);

		buttonPanel.add(scriptToolbar);

		// quick scripts ...

		JToolBar quickScriptToolbar = new JToolBar();

		final JButton quickScript = new JButton(
				Messages.getString("APrintNGVirtualBookInternalFrame.1007")); //$NON-NLS-1$
		quickScript.setIcon(new ImageIcon(getClass().getResource(
				"tool_dock.png"))); //$NON-NLS-1$
		quickScript.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {

					File quickScriptFolder = aPrintProperties
							.getBookQuickScriptFolder();
					assert quickScriptFolder != null;

					if (!quickScriptFolder.exists())
						quickScriptFolder.mkdir();

					String[] scripts = scriptManager.listQuickScripts();

					JPopupMenu scriptsMenu = new JPopupMenu();
					for (int i = 0; i < scripts.length; i++) {
						final String scriptName = scripts[i];
						try {
							// sb contains the script content ...

							JMenuItem item = new JMenuItem(scriptName);
							item.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {

									try {
										StringBuffer scriptContent = scriptManager
												.loadScript(scriptName);

										// ask for save ?

										p.setScriptContent(scriptContent
												.toString());
										p.clearDirty();

										currentEditedScript = scriptName;
										logger.debug("current script :"
												+ scriptName);

									} catch (Exception ex) {
										logger.error(
												"fail to load script "
														+ scriptName + ":"
														+ ex.getMessage(), ex);
										JMessageBox.showMessage(
												services.getOwnerForDialog(),
												"error while loading script :"
														+ scriptName + "\n"
														+ ex.getMessage());
									}
								};
							});

							scriptsMenu.add(item);

						} catch (Exception ex) {
							logger.error("error in reading script :" //$NON-NLS-1$
									+ scriptName + " skipped"); //$NON-NLS-1$
						}
					}

					scriptsMenu.show(quickScript, 0, quickScript.getHeight());

				} catch (Exception ex) {
					logger.error(
							"error in reading scripts :" + ex.getMessage(), ex); //$NON-NLS-1$
					BugReporter.sendBugReport();
					JMessageBox.showMessage(
							services.getOwnerForDialog(),
							Messages.getString("APrintNGVirtualBookInternalFrame.1013")); //$NON-NLS-1$
				}

			}
		});

		quickScriptToolbar.add(quickScript);

		JButton saveQuickScript = new JButton(
				Messages.getString("APrintNGVirtualBookInternalFrame.1014") + " ..."); //$NON-NLS-1$
		saveQuickScript.setIcon(new ImageIcon(getClass().getResource(
				"filesave.png"))); //$NON-NLS-1$
		saveQuickScript.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		});

		JButton saveCurrentEditedQuickScript = new JButton("Save Script"); //$NON-NLS-1$
		saveCurrentEditedQuickScript.setIcon(new ImageIcon(getClass()
				.getResource("filesave.png"))); //$NON-NLS-1$
		saveCurrentEditedQuickScript.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});

		quickScriptToolbar.add(saveQuickScript);

		quickScriptToolbar.add(saveCurrentEditedQuickScript);

		JButton removeQuickScript = new JButton(
				Messages.getString("APrintNGVirtualBookInternalFrame.1020")); //$NON-NLS-1$
		removeQuickScript.setIcon(new ImageIcon(getClass().getResource(
				"stop.png"))); //$NON-NLS-1$
		removeQuickScript.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					String[] scripts = scriptManager.listQuickScripts();

					Object o = JOptionPane.showInputDialog(
							VirtualBookScriptConsole.this,
							Messages.getString("APrintNGVirtualBookInternalFrame.1021"), Messages.getString("APrintNGVirtualBookInternalFrame.1022"), //$NON-NLS-1$ //$NON-NLS-2$
							JOptionPane.QUESTION_MESSAGE, null, scripts,
							(Object) null);

					if (o != null) {
						logger.debug("removing script");
						scriptManager.deleteScript((String) o);
					}

				} catch (Exception ex) {
					logger.error("error deleting quick scripts :" //$NON-NLS-1$
							+ ex.getMessage(), ex);
					BugReporter.sendBugReport();
					JMessageBox.showMessage(
							services.getOwnerForDialog(),
							Messages.getString("APrintNGVirtualBookInternalFrame.1024")); //$NON-NLS-1$
				}
			}
		});

		quickScriptToolbar.add(removeQuickScript);

		buttonPanel.add(quickScriptToolbar);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(buttonPanel, BorderLayout.NORTH);
		getContentPane().add(p, BorderLayout.CENTER);

		setPreferredSize(new Dimension(800, 600));
		pack();
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

}
