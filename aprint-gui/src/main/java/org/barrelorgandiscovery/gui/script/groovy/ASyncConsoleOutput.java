package org.barrelorgandiscovery.gui.script.groovy;

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;

/**
 * Inner class for async output to the console, in swing thread ...
 * 
 * @author Freydiere Patrice
 * 
 */
public class ASyncConsoleOutput implements IScriptConsole {

	private static Logger logger = Logger.getLogger(ASyncConsoleOutput.class);

	public SimpleAttributeSet defaultAttributeSet = new SimpleAttributeSet();

	private JTextPane outputArea;

	// this may be null
	private IScriptContentGetter scriptContentGetter;

	private int maxLines = 1000;

	/**
	 * 
	 * @param outputArea
	 * @param scriptContentGetter may be null
	 */
	public ASyncConsoleOutput(JTextPane outputArea, IScriptContentGetter scriptContentGetter) {
		assert outputArea != null;
		this.outputArea = outputArea;
		this.scriptContentGetter = scriptContentGetter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.script.groovy.IScriptConsole#appendOutput(java
	 * .awt.Component, javax.swing.text.AttributeSet)
	 */
	public void appendOutput(Component component, AttributeSet style) throws Exception {
		SimpleAttributeSet sas = new SimpleAttributeSet(defaultAttributeSet);
		sas.addAttribute(StyleConstants.NameAttribute, "component"); //$NON-NLS-1$
		StyleConstants.setComponent(sas, component);
		appendOutput(component.toString(), sas);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.script.groovy.IScriptConsole#appendOutput(javax
	 * .swing.Icon, javax.swing.text.AttributeSet)
	 */
	public void appendOutput(final Icon icon, AttributeSet style) throws Exception {
		final SimpleAttributeSet sas = defaultAttributeSet;
		sas.addAttribute(StyleConstants.NameAttribute, "icon"); //$NON-NLS-1$
		StyleConstants.setIcon(sas, icon);
		appendOutput(icon.toString(), sas);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.script.groovy.IScriptConsole#appendOutput(java
	 * .lang.String, javax.swing.text.AttributeSet)
	 */
	public void appendOutput(final String text, final AttributeSet style) throws Exception {

		Runnable r = new Runnable() {
			public void run() {
				try {

					StyledDocument doc = outputArea.getStyledDocument();
					doc.insertString(doc.getLength(),text, style);
					outputArea.moveCaretPosition(doc.getLength());
					outputArea.repaint();
					
				} catch (Exception ex) {
					logger.error("outputError " + ex.getMessage(), ex);
				}
			}
		};

		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			SwingUtilities.invokeAndWait(r);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.script.groovy.IScriptConsole#appendOutputNl(
	 * java.lang.String, javax.swing.text.AttributeSet)
	 */
	public void appendOutputNl(final String text, final AttributeSet style) throws Exception {

		Runnable r = new Runnable() {
			public void run() {
				try {

					StyledDocument doc = outputArea.getStyledDocument();
					int len = doc.getLength();
					boolean alreadyNewLine = (len == 0 || doc.getText(len - 1, 1) == "\n"); //$NON-NLS-1$
					doc.insertString(doc.getLength(), " \n", style); //$NON-NLS-1$
					if (alreadyNewLine) {
						doc.remove(len, 2); // windows hack to fix
						// (improve?) line
						// spacing
					}
					appendOutput(text, style);

				} catch (Exception ex) {
					logger.error("outputError " + ex.getMessage(), ex);
				}
			}
		};

		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			SwingUtilities.invokeAndWait(r);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.script.groovy.IScriptConsole#appendOutput(java
	 * .lang.Throwable)
	 */
	public void appendOutput(Throwable t) throws Exception {

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		t.printStackTrace(pw);

		pw.close();

		SimpleAttributeSet s = new SimpleAttributeSet();
		StyleConstants.setForeground(s, Color.RED);

		// splitting the stack trace for getting the output lines associated
		// to the script reference

		appendOutputNl("--" + t.getMessage(), s);

		String stackTrace = sw.toString();
		String lineSplitPattern = "(\\n|\\r|\\r\\n|\\u0085|\\u2028|\\u2029)";
		String[] lines = stackTrace.split(lineSplitPattern);

		String scriptContent = null;

		if (scriptContentGetter != null) {
			scriptContent = scriptContentGetter.getScriptContent();
			String[] scriptLines = scriptContent.split(lineSplitPattern);

			String ji = "([\\p{Alnum}_\\$][\\p{Alnum}_\\$]*)";

			Pattern p = Pattern
					.compile("\\tat " + ji + "(\\." + ji + ")+\\(((" + ji + "(\\.(java|groovy))?):(\\d+))\\)");

			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];

				Matcher m = p.matcher(line);

				if (m.matches()) {
					String filename = m.group(6);
					String fileNameAndLineNumber = m.group(9);

					if (filename.startsWith("Script1")) {

						String scriptLine = "undefined";

						try {

							int errorline = Integer.parseInt(fileNameAndLineNumber);

							if (errorline + 1 < scriptLine.length())
								scriptLine = scriptLines[errorline + 1];
						} catch (Exception ex) {

						}

						appendOutputNl("error line " + fileNameAndLineNumber + ">> " + scriptLine, s);

					}
				}

			}
		}
		// appendOutputNl("Complete stack trace ------------------------>",
		// s);
		// appendOutputNl(sw.getBuffer().toString(), s);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.script.groovy.IScriptConsole#clearConsole()
	 */
	public void clearConsole() {
		outputArea.setText(""); //$NON-NLS-1$
	}

	public PrintStream getDefaultPrintWriter() {

		return new PrintStream(new OutputStream() {

			@Override
			public void write(byte[] b, int off, int len) throws IOException {

				String s = new String(b, off, len);
				try {
					appendOutput(s, defaultAttributeSet);
				} catch (Exception ex) {
					ex.printStackTrace(System.err);
				}

			}

			@Override
			public void write(int b) throws IOException {
				try {
					appendOutput("" + (char) b, defaultAttributeSet); //$NON-NLS-1$
				} catch (Exception ex) {
					ex.printStackTrace(System.err);
				}
			}
		});
	}

}