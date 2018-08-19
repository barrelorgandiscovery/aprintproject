package org.barrelorgandiscovery.gui.script.groovy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprintng.APrintNGGeneralServices;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.StringTools;

public class FileScriptExecutionActionPerformed implements ActionListener {

	private static Logger logger = Logger.getLogger(FileScriptExecutionActionPerformed.class);

	private File scriptFile;
	private APrintNGGeneralServices aprint;

	public FileScriptExecutionActionPerformed(File scriptFile, APrintNGGeneralServices aprint) {
		this.scriptFile = scriptFile;
		this.aprint = aprint;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			logger.debug("read the file content");

			APrintGroovyConsolePanel gcp = new APrintGroovyConsolePanel();
			
			StringBuffer sb = StringTools.loadUTF8FileContent(scriptFile);			
			gcp.setScriptContent(sb.toString());
			gcp.getCurrentBindingRef().setVariable("services", aprint);
			gcp.run();

		} catch (Exception ex) {
			logger.error("error in executing the script");
			JMessageBox.showError(null, ex);
		}
	}

}
