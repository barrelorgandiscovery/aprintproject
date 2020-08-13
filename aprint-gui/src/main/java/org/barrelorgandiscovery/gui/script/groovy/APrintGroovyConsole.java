package org.barrelorgandiscovery.gui.script.groovy;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.concurrent.Future;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.text.SimpleAttributeSet;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.JobEvent;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.JMessageBox;

import groovy.lang.Binding;

/**
 * APrint adapted console for book stuff manipulating
 * 
 * @author Freydiere Patrice
 * 
 */
public class APrintGroovyConsole extends JFrame {

	public static final String APRINTGROOVYSCRIPTEXTENSION = "aprintgroovyscript"; //$NON-NLS-1$

	/**
	 * serial number ..
	 */
	private static final long serialVersionUID = 2065506802918343102L;

	/**
	 * Logger
	 */
	private static Logger logger = Logger.getLogger(APrintGroovyConsole.class);

	/**
	 * Console panel ..
	 */
	private APrintGroovyConsolePanel consolePanel;

	private APrintProperties props = null;

	/**
	 * The current opened file ...
	 */
	private AbstractFileObject openedFile = null;

	/**
	 * Async job manager
	 */
	private AsyncJobsManager asyncJobsManager;

	private Future currentJob = null;

	private JButton run;

	/**
	 * Constructor
	 * 
	 * @throws Exception
	 */
	public APrintGroovyConsole(Frame ref, APrintProperties props, AsyncJobsManager asyncJobsManager) throws Exception {
		// super(ref);

		setAlwaysOnTop(true);

		// setIconImage(APrint.getAPrintApplicationIcon());

		setOpenedFile(null);
		this.props = props;
		this.asyncJobsManager = asyncJobsManager;

		initComponents();
	}

	private void setOpenedFile(AbstractFileObject f) {
		setTitle(Messages.getString("APrintGroovyConsole.1") + (f != null ? "-" + f.getName() : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.openedFile = f;
	}

	private void initComponents() throws Exception {

		this.consolePanel = new APrintGroovyConsolePanel();

		JButton loadScript = new JButton(Messages.getString("APrintGroovyConsole.2")); //$NON-NLS-1$
		loadScript.setIcon(new ImageIcon("folder.png"));//$NON-NLS-1$
		loadScript.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					APrintFileChooser fc = new APrintFileChooser();
					fc.setFileFilter(new VFSFileNameExtensionFilter("APrint Groovy Script", //$NON-NLS-1$
							APRINTGROOVYSCRIPTEXTENSION));

					fc.setFileSelectionMode(APrintFileChooser.FILES_ONLY);

					if (openedFile != null) {
						fc.setSelectedFile(openedFile);
					}

					if (fc.showOpenDialog(APrintGroovyConsole.this) == APrintFileChooser.APPROVE_OPTION) {

						AbstractFileObject result = fc.getSelectedFile();

						setOpenedFile(result);
						StringBuffer sb = new StringBuffer();
						InputStream istream = result.getInputStream();
						try {

							LineNumberReader r = new LineNumberReader(
									new InputStreamReader(istream, Charset.forName("UTF-8"))); //$NON-NLS-1$
							try {

								String s = r.readLine();
								while (s != null) {
									if (sb.length() > 0)
										sb.append("\n"); //$NON-NLS-1$
									sb.append(s);
									s = r.readLine();
								}
							} finally {
								r.close();
							}
						} finally {
							istream.close();
						}
						consolePanel.setScriptContent(sb.toString());

					}

				} catch (Exception ex) {
					logger.error("error in loading the script :" //$NON-NLS-1$
							+ ex.getMessage(), ex);
					JMessageBox.showMessage(null, Messages.getString("APrintGroovyConsole.7") + ex.getMessage()); //$NON-NLS-1$
				}

			}
		});

		JButton saveScript = new JButton(Messages.getString("APrintGroovyConsole.8")); //$NON-NLS-1$

		saveScript.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		});

		JButton save = new JButton(Messages.getString("APrintGroovyConsole.0")); //$NON-NLS-1$
		save.setIcon(new ImageIcon(getClass().getResource("filesave.png")));//$NON-NLS-1$
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});

		run = new JButton(Messages.getString("APrintGroovyConsole.16"));//$NON-NLS-1$
		run.setIcon(new ImageIcon(getClass().getResource("misc.png")));//$NON-NLS-1$
		run.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					consolePanel.appendOutput("\n", new SimpleAttributeSet()); //$NON-NLS-1$

					synchronized (this) {
						if (currentJob != null) {
							currentJob.cancel(true);
							guiFinishExec();

							currentJob = null;

							return;
						} else {
							guiStartExec();
						}
					}

					Future f = consolePanel.run();

					currentJob = f;

					asyncJobsManager.submitAlreadyExecutedJobToTrack(f, new JobEvent() {
						public void jobAborted() {
							try {
								consolePanel.appendOutputNl("Aborted", new SimpleAttributeSet());

								guiFinishExec();

								currentJob = null;
							} catch (Exception ex) {
								logger.error("error " + ex.getMessage(), ex);
							}
						}

						public void jobError(Throwable ex) {
							try {
								consolePanel.appendOutputNl("\n ERROR >> " //$NON-NLS-1$
										+ ex.getMessage(), new SimpleAttributeSet());

								consolePanel.appendOutput(ex);

								logger.debug(ex);

								guiFinishExec();
								currentJob = null;

							} catch (Throwable x) {
								logger.error("Error in logging exception :" + x.getMessage(), x);
							}
						}

						public void jobFinished(Object evaluate) {
							try {
								if (evaluate == null) {
									consolePanel.appendOutput("\n>>  (null) returned from evaluation", //$NON-NLS-1$
											new SimpleAttributeSet());
								} else {
									consolePanel.appendOutputNl("\n>>  " + evaluate.toString(), //$NON-NLS-1$
											new SimpleAttributeSet());
								}

								guiFinishExec();
								currentJob = null;

								logger.debug(evaluate);
							} catch (Throwable ex) {
								try {
									consolePanel.appendOutputNl("\n ERROR >> " //$NON-NLS-1$
											+ ex.getMessage(), new SimpleAttributeSet());

									consolePanel.appendOutput(ex);

								} catch (Exception _ex) {
									_ex.printStackTrace(System.err);
								}
								ex.printStackTrace(System.err);
							}
						}
					});

				} catch (Throwable ex) {

					try {
						consolePanel.appendOutputNl("\n ERROR >> " //$NON-NLS-1$
								+ ex.getMessage(), new SimpleAttributeSet());

						consolePanel.appendOutput(ex);

					} catch (Exception _ex) {
						_ex.printStackTrace(System.err);
					}
					ex.printStackTrace(System.err);
				}
			}
		});

		JButton clear = new JButton(Messages.getString("APrintGroovyConsole.21")); //$NON-NLS-1$
		clear.setIcon(new ImageIcon(getClass().getResource("ark_new.png")));//$NON-NLS-1$
		clear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				consolePanel.clearConsole();
			}
		});

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(loadScript);
		menuBar.add(save);
		menuBar.add(saveScript);
		menuBar.add(run);
		menuBar.add(clear);
		setJMenuBar(menuBar);

		setSize(800, 600);
		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		getContentPane().add(consolePanel, BorderLayout.CENTER);

	}

	public Binding getCurrentBindingByRef() {
		return consolePanel.getCurrentBindingRef();
	}

	private void save() {
		try {
			if (openedFile == null) {
				saveAs();
				return;
			}

			OutputStream ostream = openedFile.getOutputStream();
			try {
				Writer w = new OutputStreamWriter(ostream, Charset.forName("UTF-8")); //$NON-NLS-1$
				try {
					w.write(consolePanel.getScriptContent());
					logger.debug("file written ..."); //$NON-NLS-1$
				} finally {
					w.close();
				}
			} finally {
				ostream.close();
			}
		} catch (Exception ex) {
			logger.error("error in saving the script :" //$NON-NLS-1$
					+ ex.getMessage(), ex);
			JMessageBox.showMessage(null, Messages.getString("APrintGroovyConsole.15") + ex.getMessage()); //$NON-NLS-1$
		}
	}

	private void saveAs() {
		try {
			APrintFileChooser fc = new APrintFileChooser();
			fc.setFileFilter(new VFSFileNameExtensionFilter("APrint Groovy Script", //$NON-NLS-1$
					APRINTGROOVYSCRIPTEXTENSION));

			if (fc.showSaveDialog(APrintGroovyConsole.this) == APrintFileChooser.APPROVE_OPTION) {

				AbstractFileObject f = fc.getSelectedFile();
				if (f == null)
					return;

				String filename = f.getName().toString();
				if (!filename.toLowerCase().endsWith("." + APRINTGROOVYSCRIPTEXTENSION)) //$NON-NLS-1$
					f = (AbstractFileObject) f.getFileSystem().resolveFile(f.getName().toString() + "." //$NON-NLS-1$
							+ APRINTGROOVYSCRIPTEXTENSION);

				if (f.exists()) {
					if (JOptionPane.showConfirmDialog(APrintGroovyConsole.this,
							Messages.getString("APrintGroovyConsole.11")) != JOptionPane.YES_OPTION) { //$NON-NLS-1$
						return;
					}
				}

				OutputStream ostream = f.getOutputStream();
				try {
					Writer w = new OutputStreamWriter(ostream, Charset.forName("UTF-8")); //$NON-NLS-1$
					try {
						w.write(consolePanel.getScriptContent());
						logger.debug("file written ..."); //$NON-NLS-1$
					} finally {
						w.close();
					}
				} finally {
					ostream.close();
				}
				setOpenedFile(f);
			}

		} catch (Exception ex) {
			logger.error("error in saving the script :" //$NON-NLS-1$
					+ ex.getMessage(), ex);
			JMessageBox.showMessage(null, Messages.getString("APrintGroovyConsole.15") + ex.getMessage()); //$NON-NLS-1$
		}
	}

	private void guiStartExec() {
		run.setText("Cancel Script");
		run.setIcon(new ImageIcon(getClass().getResource("ajax-loader.gif")));

	}

	private void guiFinishExec() {
		run.setText(Messages.getString("APrintGroovyConsole.16"));
		run.setIcon(null);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		APrintGroovyConsole gc = new APrintGroovyConsole(null, new APrintProperties(true), new AsyncJobsManager());
		gc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gc.setVisible(true);

	}

}
