package org.barrelorgandiscovery.gui.etl;

import org.barrelorgandiscovery.model.ModelRunner;

public interface ModelExecutionListener {

	
	void executed(ModelRunner modelRunner) throws Exception;
	
	
}
