package org.barrelorgandiscovery.gui.ainstrument;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.JobEvent;
import org.barrelorgandiscovery.editableinstrument.InstrumentScript;
import org.barrelorgandiscovery.editableinstrument.InstrumentScript.InstrumentScriptLanguage;
import org.barrelorgandiscovery.editableinstrument.InstrumentScript.InstrumentScriptType;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.script.groovy.APrintGroovyConsolePanel;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.Disposable;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFile;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFilePropertyEditor;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.components.separator.TitledSeparator;
import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

/**
 * Panel responsible for scripting edition
 * 
 * @author Freydiere Patrice
 * 
 */
public class JInstrumentScriptingPanel extends JPanel implements Disposable,
		UpdateInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6368263252098577125L;

	private static Logger logger = Logger
			.getLogger(JInstrumentScriptingPanel.class);

	private AsyncJobsManager asyncJobsManager;

	/**
	 * Console for the current script ...
	 */
	private APrintGroovyConsolePanel scriptingPanel;

	/**
	 * Script list component
	 */
	private JList importerScriptList;

	/**
	 * Script default list model
	 */
	private DefaultListModel importerScriptListModel;

	/**
	 * Run button
	 */
	private JButton runButton;

	/**
	 * Define parameters button ...
	 */
	private JButton defineParameters;

	/**
	 * Clear console button
	 */
	private JButton clearConsoleButton;

	/**
	 * Delete script button
	 */
	private JButton deletescript;

	/**
	 * rename script button
	 */
	private JButton renamescript;

	/**
	 * new script button
	 */
	private JButton newscript;

	/**
	 * Async executed task
	 */
	private Future<Object> currentExecutedTask;

	/**
	 * Constructor
	 * 
	 * @throws Exception
	 */
	public JInstrumentScriptingPanel() throws Exception {

		initComponents();

		propertyMidiFile = new DefaultProperty();
		propertyMidiFile.setName("midifile"); //$NON-NLS-1$
		propertyMidiFile.setDisplayName(propertyMidiFile.getName());
		propertyMidiFile.setType(MidiFile.class);
		propertyMidiFile.setEditable(true);

		propertyScale = new DefaultProperty();
		propertyScale.setName("scale"); //$NON-NLS-1$
		propertyScale.setDisplayName(propertyScale.getName());
		propertyScale.setType(Scale.class);
		propertyScale.setEditable(false);

		asyncJobsManager = new AsyncJobsManager();

	}

	/**
	 * Signal the current scale for the scripting evaluation
	 * 
	 * @param s
	 */
	public void setCurrentScale(Scale s) {
		propertyScale.setValue(s);
	}

	/**
	 * init visual beans
	 * 
	 * @throws Exception
	 */
	private void initComponents() throws Exception {
		FormPanel panelScripting = null;
		try {

			InputStream is = getClass().getResourceAsStream(
					"instrumenteditorpanelimporterscripts.jfrm"); //$NON-NLS-1$
			if (is == null)
				throw new Exception("form not found"); //$NON-NLS-1$
			panelScripting = new FormPanel(is);

		} catch (Exception ex) {
			logger.error("panel construction", ex); //$NON-NLS-1$
			throw new Exception(ex.getMessage(), ex);
		}

		scriptingPanel = new APrintGroovyConsolePanel();

		panelScripting.getFormAccessor().replaceBean(
				panelScripting.getComponentByName("scriptconsole"), //$NON-NLS-1$
				scriptingPanel);

		// i18n issues ...

		TitledSeparator titleImporterScriptList = (TitledSeparator) panelScripting
				.getComponentByName("titleImporterScriptList"); //$NON-NLS-1$
		titleImporterScriptList.setText(Messages
				.getString("JInstrumentScriptingPanel.6")); //$NON-NLS-1$

		TitledSeparator titlescriptediting = (TitledSeparator) panelScripting
				.getComponentByName("titlescriptediting"); //$NON-NLS-1$
		titlescriptediting.setText(Messages
				.getString("JInstrumentScriptingPanel.8")); //$NON-NLS-1$

		importerScriptList = (JList) panelScripting
				.getComponentByName("importerScriptList");//$NON-NLS-1$

		importerScriptListModel = new DefaultListModel();

		importerScriptList.setModel(importerScriptListModel);

		importerScriptList
				.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {

						if (lastSelectedScript != null) {
							updateScriptIfDirty();
						}

						JList source = (JList) e.getSource();
						InstrumentScript currentScript = (InstrumentScript) source
								.getSelectedValue();
						if (currentScript != null) {

							setCursor(Cursor
									.getPredefinedCursor(Cursor.WAIT_CURSOR));
							try {

								scriptingPanel.clearConsole();

								scriptingPanel.setScriptContent(currentScript
										.getContent());

								scriptingPanel.clearDirty();

							} finally {
								setCursor(Cursor.getDefaultCursor());
							}

						}

						lastSelectedScript = currentScript;

						checkState();
					}
				});

		defineParameters = (JButton) panelScripting
				.getComponentByName("defineParameters");//$NON-NLS-1$
		defineParameters.setText(Messages
				.getString("JInstrumentScriptingPanel.9")); //$NON-NLS-1$
		defineParameters.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					JFrame f = new JFrame();
					PropertySheetPanel p = new PropertySheetPanel();
					p.setDescriptionVisible(true);
					p.setSortingCategories(true);

					PropertyEditorRegistry pr = new PropertyEditorRegistry();
					// pr.registerDefaults();
					pr.registerEditor(MidiFile.class,
							MidiFilePropertyEditor.class);

					p.setProperties(new Property[] { propertyMidiFile,
							propertyScale });
					p.setEditorFactory(pr);

					f.getContentPane().add(p, BorderLayout.CENTER);
					f.setSize(800, 600);
					f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					f.setVisible(true);

				} catch (Throwable ex) {
					logger
							.error(
									"error in defining parameters :" + ex.getMessage(), ex); //$NON-NLS-1$
				}
			}
		});

		runButton = (JButton) panelScripting.getComponentByName("runButton"); //$NON-NLS-1$
		runButton.setText(Messages.getString("JInstrumentScriptingPanel.10")); //$NON-NLS-1$
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {

					try {

						logger.debug("setting midifile property"); //$NON-NLS-1$
						scriptingPanel.getCurrentBindingRef().setProperty(
								propertyMidiFile.getName(),
								propertyMidiFile.getValue());

						logger.debug("setting the scale parameter"); //$NON-NLS-1$
						scriptingPanel.getCurrentBindingRef().setProperty(
								propertyScale.getName(),
								propertyScale.getValue());

						Future<Object> rf = scriptingPanel.run();

						asyncJobsManager.submitAlreadyExecutedJobToTrack(rf, new JobEvent() {
							public void jobAborted() {
								logger.info("job aborted");

							}

							public void jobError(Throwable ex) {
								try {
									scriptingPanel.appendOutput(ex);

								} catch (Exception _ex) {
									logger.error(_ex.getMessage(), _ex);
								}
							}

							public void jobFinished(Object result) {
								final Object finalresult = result;
								try {
									if (logger.isDebugEnabled())
										logger
												.debug("result :" //$NON-NLS-1$
														+ (result == null ? "null" : result //$NON-NLS-1$
																		.toString()));

									scriptingPanel
											.appendOutputNl(
													" script output>>" //$NON-NLS-1$
															+ (result == null ? "null" : result.toString()), //$NON-NLS-1$
													null);

									if (result instanceof VirtualBook) {

										scriptingPanel.appendOutputNl(
												"\n", null); //$NON-NLS-1$

										logger
												.debug("adding virtual book to the console ... "); //$NON-NLS-1$
										final JVirtualBookScrollableComponent c = new JVirtualBookScrollableComponent();
										c.setPreferredSize(new Dimension(600,
												480));
										c
												.setVirtualBook((VirtualBook) finalresult);

										Runnable r = new Runnable() {
											public void run() {
												try {
													scriptingPanel
															.appendOutput(c,
																	null);
													scriptingPanel
															.appendOutputNl(
																	"Done",
																	null);
												} catch (Exception ex) {
													logger.error("error :"
															+ ex.getMessage(),
															ex);
												}
											};

										};

										if (!SwingUtilities
												.isEventDispatchThread()) {
											SwingUtilities.invokeAndWait(r);
										} else {
											r.run();
										}

									}
								} catch (Exception ex) {
									logger
											.error(
													"error in executing script :" + ex.getMessage(), ex); //$NON-NLS-1$
								}
							}

						});

					} catch (Throwable ex) {

						scriptingPanel.appendOutput(ex);
						return;
					}

				} catch (Throwable t) {
					logger.error(
							"error in executing script :" + t.getMessage(), t); //$NON-NLS-1$
					try {
						scriptingPanel.appendOutputNl(t.getMessage(), null);
					} catch (Exception ex) {
						logger.error("error in output :" + ex.getMessage(), ex); //$NON-NLS-1$
					}
				}

			}
		});

		clearConsoleButton = (JButton) panelScripting
				.getComponentByName("clearConsoleButton"); //$NON-NLS-1$
		clearConsoleButton.setText(Messages
				.getString("JInstrumentScriptingPanel.15")); //$NON-NLS-1$
		clearConsoleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scriptingPanel.clearConsole();
			}
		});

		deletescript = (JButton) panelScripting
				.getComponentByName("deletescript"); //$NON-NLS-1$
		deletescript
				.setText(Messages.getString("JInstrumentScriptingPanel.16")); //$NON-NLS-1$
		deletescript.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					updateScriptIfDirty();

					lastSelectedScript = null;

					importerScriptListModel.removeElement(importerScriptList
							.getSelectedValue());

					fireScriptsChanged();
				} catch (Throwable t) {
					logger.error("error in removing the script : " //$NON-NLS-1$
							+ t.getMessage(), t);
				}
			}
		});

		renamescript = (JButton) panelScripting
				.getComponentByName("renamescript"); //$NON-NLS-1$
		renamescript
				.setText(Messages.getString("JInstrumentScriptingPanel.17")); //$NON-NLS-1$
		renamescript.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					InstrumentScript s = (InstrumentScript) importerScriptList
							.getSelectedValue();

					String result = JOptionPane.showInputDialog(Messages
							.getString("JInstrumentScriptingPanel.11")); //$NON-NLS-1$
					if (result != null) {
						logger.debug("rename the script ... "); //$NON-NLS-1$

						updateScriptIfDirty();
						lastSelectedScript = null;

						InstrumentScript newScript = new InstrumentScript(
								result, s.getScriptLanguage(), s.getType(), s
										.getContent());
						importerScriptListModel
								.removeElement(importerScriptList
										.getSelectedValue());

						importerScriptListModel.addElement(newScript);

						fireScriptsChanged();

					}

				} catch (Exception ex) {
					logger.error(
							"error in renaming script :" + ex.getMessage(), ex); //$NON-NLS-1$
				}

			}
		});

		newscript = (JButton) panelScripting.getComponentByName("newscript"); //$NON-NLS-1$
		newscript.setText(Messages.getString("JInstrumentScriptingPanel.19")); //$NON-NLS-1$
		newscript.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					String result = JOptionPane.showInputDialog(Messages
							.getString("JInstrumentScriptingPanel.20")); //$NON-NLS-1$
					if (result != null) {
						logger.debug("creating a new script ... "); //$NON-NLS-1$

						// Type de script ....

						Object rselectiontype = JOptionPane
								.showInputDialog(
										JInstrumentScriptingPanel.this,
										"Select the script type",
										"Choose script type",
										JOptionPane.QUESTION_MESSAGE,
										null,
										new Object[] {
												InstrumentScriptType.IMPORTER_SCRIPT,
												InstrumentScriptType.MIDI_OUTPUT_SCRIPT },
										InstrumentScriptType.IMPORTER_SCRIPT);

						if (rselectiontype != null) {

							logger.debug("selected script type :"
									+ rselectiontype);
							InstrumentScriptType isc = (InstrumentScriptType) rselectiontype;

							InstrumentScript newScript = new InstrumentScript(
									result, InstrumentScriptLanguage.GROOVY,
									isc, ""); //$NON-NLS-1$

							importerScriptListModel.addElement(newScript);

							fireScriptsChanged();
						}
					}

				} catch (Exception ex) {
					logger.error("error in creating a new script ... " //$NON-NLS-1$
							+ ex.getMessage(), ex);
				}
			}
		});

		setLayout(new BorderLayout());
		add(panelScripting, BorderLayout.CENTER);

		checkState();

	}

	protected void updateScriptIfDirty() {

		if (scriptingPanel.isDirty()) {

			if (lastSelectedScript != null) {

				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					logger.debug("script to change :" //$NON-NLS-1$
							+ lastSelectedScript);

					lastSelectedScript.changeContent(scriptingPanel
							.getScriptContent());
					// signal change things ...

					fireScriptsChanged();
					scriptingPanel.clearDirty();
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}

			}
		}
	}

	/**
	 * set the instrument script list
	 */
	public void setInstrumentScripts(InstrumentScript[] scripts) {

		List<InstrumentScript> scriptsList = Arrays.asList(scripts);

		this.importerScriptListModel.clear();

		for (Iterator iterator = scriptsList.iterator(); iterator.hasNext();) {
			InstrumentScript instrumentScript = (InstrumentScript) iterator
					.next();

			importerScriptListModel.addElement(instrumentScript);
		}

	}

	/**
	 * Get the instruments script list ...
	 * 
	 * @return
	 */
	public InstrumentScript[] getInstrumentScripts() {

		InstrumentScript[] retvalue = new InstrumentScript[importerScriptListModel
				.size()];
		for (int i = 0; i < retvalue.length; i++) {
			retvalue[i] = (InstrumentScript) importerScriptListModel.get(i);
		}

		return retvalue;
	}

	private Vector<ScriptsChangedListener> scriptsChangedListeners = new Vector<ScriptsChangedListener>();

	private InstrumentScript lastSelectedScript = null;

	/**
	 * property for specify the midi file associated to the importer script
	 */
	private DefaultProperty propertyMidiFile;

	/**
	 * property for specify the scale of the imported script
	 */
	private DefaultProperty propertyScale;

	/**
	 * add a script change listener
	 * 
	 * @param listener
	 */
	public void addScriptsChangedListener(ScriptsChangedListener listener) {
		scriptsChangedListeners.add(listener);
	}

	/**
	 * Remove a script changed listener
	 * 
	 * @param listener
	 */
	public void removeScriptsChangedListener(ScriptsChangedListener listener) {
		scriptsChangedListeners.remove(listener);
	}

	/**
	 * fire an event for a script change
	 */
	protected void fireScriptsChanged() {

		InstrumentScript[] scripts = getInstrumentScripts();

		for (Iterator iterator = scriptsChangedListeners.iterator(); iterator
				.hasNext();) {
			ScriptsChangedListener l = (ScriptsChangedListener) iterator.next();

			if (logger.isDebugEnabled())
				logger.debug("fire scripts changed to listener :" + l);
			l.scriptsChanged(scripts);
		}

	}

	/**
	 * check the visual state of buttons / panels ...
	 */
	private void checkState() {
		if (importerScriptList.getSelectedIndex() == -1) {

			logger.debug("disable scriptingpanel"); //$NON-NLS-1$

			scriptingPanel.setEnabled(false);
			runButton.setEnabled(false);
			clearConsoleButton.setEnabled(false);
			renamescript.setEnabled(false);
			deletescript.setEnabled(false);

		} else {

			logger.debug("enable scriptingpanel"); //$NON-NLS-1$

			scriptingPanel.setEnabled(true);
			runButton.setEnabled(true);
			clearConsoleButton.setEnabled(true);
			renamescript.setEnabled(true);
			deletescript.setEnabled(true);
		}
	}

	public void dispose() {
		updateScriptIfDirty();
		asyncJobsManager.dispose();
	}

	public void commitProperties() {
		updateScriptIfDirty();
	}

	/**
	 * Test routine for the panel ...
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		JInstrumentScriptingPanel sp = new JInstrumentScriptingPanel();

		sp.setCurrentScale(Scale.getGammeMidiInstance());

		InstrumentScript test = new InstrumentScript(
				"monscript", //$NON-NLS-1$
				InstrumentScriptLanguage.GROOVY,
				InstrumentScriptType.IMPORTER_SCRIPT, "def toto = 4"); //$NON-NLS-1$

		InstrumentScript test2 = new InstrumentScript(
				"monscript2", //$NON-NLS-1$
				InstrumentScriptLanguage.GROOVY,
				InstrumentScriptType.IMPORTER_SCRIPT, "def test2 = 5"); //$NON-NLS-1$

		sp.setInstrumentScripts(new InstrumentScript[] { test, test2 });

		JFrame f = new JFrame();
		f.setSize(800, 600);
		f.getContentPane().add(sp, BorderLayout.CENTER);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);

	}

}
