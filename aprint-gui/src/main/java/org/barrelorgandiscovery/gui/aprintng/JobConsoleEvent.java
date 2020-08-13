package org.barrelorgandiscovery.gui.aprintng;

import org.barrelorgandiscovery.gui.script.groovy.APrintGroovyConsolePanel;

public interface JobConsoleEvent {

	public void jobAborted(APrintGroovyConsolePanel p) throws Exception;
	
	public void jobError(APrintGroovyConsolePanel p, Throwable t) throws Exception;

	public void jobFinished(APrintGroovyConsolePanel p, Object result) throws Exception ;

	
}
