package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.mock;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.prefs.IPrefsStorage;

public class MockMachineParameters extends AbstractMachineParameters {

	@Override
	public String getLabelName() {
		return "MockMachine";
	}

	@Override
	public void saveParameters(IPrefsStorage ps) throws Exception {

	}

	@Override
	public void loadParameters(IPrefsStorage ps) throws Exception {

	}

}
