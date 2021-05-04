package org.barrelorgandiscovery.gui.script.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.ui.ConsoleTextEditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.groovy.APrintGroovyShell;
import org.barrelorgandiscovery.tools.Dirtyable;
import org.barrelorgandiscovery.tools.SwingUtils;
import org.barrelorgandiscovery.tools.TimeUtils;

/**
 * Groovy Console panel for evauating some script elements ...
 * 
 * @author Freydiere Patrice
 * 
 */
public class APrintGroovyConsolePanel extends JPanel implements IScriptConsole,
		Dirtyable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4193594806153304682L;

	/**
	 * logger
	 */
	private static Logger logger = Logger
			.getLogger(APrintGroovyConsolePanel.class);

	private ConsoleTextEditor cte;
	private JTextPane outputArea;
	private Binding binding;

	private ClassLoader loaderForScripts = APrintGroovyConsolePanel.class
			.getClassLoader();

	/**
	 * Constructor
	 */
	public APrintGroovyConsolePanel() {

		initComponents();
	}

	private ASyncConsoleOutput console;

	/**
	 * Construct visual beans
	 */
	private void initComponents() {
		cte = new ConsoleTextEditor();

		cte.getTextEditor().getDocument()
				.addDocumentListener(new DocumentListener() {
					public void changedUpdate(DocumentEvent e) {
						toggleDirty();
					}

					public void insertUpdate(DocumentEvent e) {
						toggleDirty();
					}

					public void removeUpdate(DocumentEvent e) {
						toggleDirty();
					}
				});

		binding = new Binding();
		// binding for the output in the console ...
		binding.setProperty("console", this);
		binding.setProperty("out", new PrintStream(new OutputStream() { //$NON-NLS-1$

					@Override
					public void write(byte[] b, int off, int len)
							throws IOException {

						String s = new String(b, off, len);
						try {
							console.appendOutput(s, console.defaultAttributeSet);
						} catch (Exception ex) {
							ex.printStackTrace(System.err);
						}

					}

					@Override
					public void write(int b) throws IOException {
						try {
							console.appendOutput(
									"" + (char) b, console.defaultAttributeSet); //$NON-NLS-1$
						} catch (Exception ex) {
							ex.printStackTrace(System.err);
						}
					}
				}));

		outputArea = new JTextPane();
		JScrollPane sp = new JScrollPane(outputArea);
		outputArea.setEditable(false);

		console = new ASyncConsoleOutput(outputArea,
				new IScriptContentGetter() {
					public String getScriptContent() {
						return APrintGroovyConsolePanel.this.getScriptContent();
					}
				});

		setLayout(new BorderLayout());

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cte,
				sp);

		add(splitPane, BorderLayout.CENTER);
		splitPane.setDividerLocation(200);

	}

	/**
	 * Set script content
	 * 
	 * @param scriptContent
	 */
	public void setScriptContent(String scriptContent) {

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {

			cte.getTextEditor().setText(scriptContent);
			cte.getTextEditor().setCaretPosition(0);

		} finally {
			setCursor(Cursor.getDefaultCursor());
		}
	}

	/**
	 * get script content
	 * 
	 * @return
	 */
	public String getScriptContent() {
		return cte.getTextEditor().getText();
	}

	/**
	 * Get current binding for the evaluation ..
	 * 
	 * @return
	 */
	public Binding getCurrentBindingRef() {
		return binding;
	}

	private ExecutorService e = Executors.newSingleThreadExecutor();

	private Callable<Object> currentTasks = null;

	/**
	 * Run the script
	 * 
	 * @return
	 */
	public Future<Object> run() {

		final APrintGroovyShell gs = new APrintGroovyShell(loaderForScripts,
				binding);

		Future<Object> execution = e.submit(new Callable<Object>() {

			public Object call() throws Exception {

				long scriptStartTime = System.currentTimeMillis();

				SimpleAttributeSet simpleAttributeSet = new SimpleAttributeSet();
				StyleConstants.setForeground(simpleAttributeSet, Color.BLUE);

				console.appendOutputNl(">> start script execution" + "\n",
						simpleAttributeSet);

				Object resultEvaluate = gs.evaluate(getScriptContent());

				long executedTime = System.currentTimeMillis()
						- scriptStartTime;

				console.appendOutputNl("\n" + " " + "Script executed in" + " "
						+ TimeUtils.toMinSecs(executedTime * 1000),
						simpleAttributeSet);

				return resultEvaluate;

			};

		});

		return execution;

	}

	public void setScriptPanelEnabled(boolean enabled) {
		SwingUtils.recurseSetEnable(cte.getTextEditor(), enabled);
	}

	public boolean getScriptPanelEnabled() {
		return cte.getTextEditor().isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		outputArea.setEnabled(enabled);
		outputArea.setEditable(enabled);
		cte.setEnabled(enabled);
		cte.getTextEditor().setEnabled(enabled);
		cte.getTextEditor().setEditable(enabled);

	}

	public void appendOutput(Component component, AttributeSet style)
			throws Exception {
		console.appendOutput(component, style);

	}

	public void appendOutput(Icon icon, AttributeSet style) throws Exception {
		console.appendOutput(icon, style);

	}

	public void appendOutput(String text, AttributeSet style) throws Exception {
		console.appendOutput(text, style);

	}

	public void appendOutput(Throwable t) throws Exception {
		console.appendOutput(t);

	}

	public void appendOutputNl(String text, AttributeSet style)
			throws Exception {
		console.appendOutputNl(text, style);

	}

	public void clearConsole() {
		console.clearConsole();

	}

	private boolean dirty;

	public void clearDirty() {
		dirty = false;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void toggleDirty() {
		dirty = true;
	}

	public void setLoaderForScripts(ClassLoader loaderForScripts) {
		this.loaderForScripts = loaderForScripts;
	}

	public ClassLoader getLoaderForScripts() {
		return loaderForScripts;
	}

}
