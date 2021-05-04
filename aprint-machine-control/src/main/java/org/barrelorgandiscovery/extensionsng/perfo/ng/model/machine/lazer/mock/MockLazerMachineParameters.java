package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.lazer.mock;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.prefs.IPrefsStorage;

public class MockLazerMachineParameters extends AbstractMachineParameters {

	@Override
	public String getLabelName() {
		return "MockLazerMachine";
	}

	@Override
	public void saveParameters(IPrefsStorage ps) throws Exception {

	}

	@Override
	public void loadParameters(IPrefsStorage ps) throws Exception {

	}

}
