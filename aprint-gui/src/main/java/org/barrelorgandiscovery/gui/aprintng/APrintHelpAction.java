package org.barrelorgandiscovery.gui.aprintng;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.BareBonesBrowserLaunch;
import org.barrelorgandiscovery.tools.VersionTools;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

public class APrintHelpAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1421330127747487329L;
	
	private static Logger logger = Logger.getLogger(APrintHelpAction.class);
	
	public APrintHelpAction() {
		super(Messages.getString("APrint.102"),new ImageIcon(APrintHelpAction.class.getResource("help.png"), //$NON-NLS-1$
				Messages.getString("APrint.104")) );
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			
			String mainVersion = VersionTools.getMainVersion();
			BareBonesBrowserLaunch.openURL("http://www.barrel-organ-discovery.org/site/doc/" + mainVersion); //$NON-NLS-1$
		
		} catch(Exception ex)
		{
			logger.error("error showing help :" + ex.getMessage(), ex);
			BugReporter.sendBugReport();
		}
	}

}
