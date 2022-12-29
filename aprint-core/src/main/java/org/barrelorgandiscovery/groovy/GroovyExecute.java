package org.barrelorgandiscovery.groovy;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.exec.IConsoleLog;
import org.barrelorgandiscovery.exec.IExecute;

import groovy.lang.Binding;

/**
 * groovy execute object extending the previous Groovy Shell and provide
 * the IExecute interface
 * @author pfreydiere
 *
 */
public class GroovyExecute extends APrintGroovyShell implements IExecute {

	/**
	 * logger
	 */
	private Logger logger = Logger.getLogger(GroovyExecute.class);

	private File scriptFile;
	private String scriptContent;

	/**
	 * constructor
	 * 
	 * @param scriptFile
	 */
	public GroovyExecute(File scriptFile) {
		this.scriptFile = scriptFile;
	}

	/**
	 * constructor with script content
	 * 
	 * @param scriptContent
	 */
	public GroovyExecute(String scriptContent) {
		this.scriptContent = scriptContent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.exec.IExecute#execute(java.util.HashMap,
	 * org.barrelorgandiscovery.exec.IConsoleLog)
	 */
	@Override
	public Map<String, Object> execute(Map<String, Object> variables, IConsoleLog console) throws Exception {

		if (logger.isDebugEnabled()) {
			logger.debug("starting execute, with variables ");//$NON-NLS-1$
			for (Entry<String, Object> e : variables.entrySet()) {
				logger.debug("variable " + e.getKey() + " : " + e.getValue()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		//		HashMap<String, Object> m = new HashMap<String, Object>();
		//		if (variables != null) {
		//			for (Entry<String, Object> e : variables.entrySet()) {
		//				m.put(e.getKey(), e.getValue());
		//			}
		//		}

		Binding binding = new Binding();
		// binding for the output in the console ...
		binding.setProperty("out", new PrintStream(new OutputStream() { //$NON-NLS-1$

			@Override
			public void write(byte[] b, int off, int len) throws IOException {

				String s = new String(b, off, len);
				try {
					console.log(s);
				} catch (Exception ex) {
					ex.printStackTrace(System.err);
				}

			}

			@Override
			public void write(int b) throws IOException {
				try {
					console.log((char) b);
				} catch (Exception ex) {
					ex.printStackTrace(System.err);
				}
			}
		}));

		APrintGroovyShell gs = new APrintGroovyShell(binding);
		Object ret = null;
		if (scriptFile != null) {
			logger.debug("execute script file :" + scriptFile); //$NON-NLS-1$
			ret = gs.evaluate(scriptFile);
		} else if (scriptContent == null) {
			logger.debug("execute script content :" + scriptContent); //$NON-NLS-1$
			ret = gs.evaluate(scriptContent);
		} else {
			throw new Exception("unsupported input , null scriptfile , null scriptcontent"); //$NON-NLS-1$
		}

		logger.debug("execution finished"); //$NON-NLS-1$

		Map<String, Object> retHash = new HashMap<String, Object>();
		retHash.put(IExecute.MAIN_RETURN_NAME, ret);

		return retHash;
	}

}
