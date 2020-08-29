package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.lazer.mock;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControlListener;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.Command;

public class MockLazerMachineControl implements MachineControl {

	@Override
	public void setMachineControlListener(MachineControlListener listener) {
		
		
	}

	@Override
	public void sendCommand(Command command) throws Exception {
		Thread.sleep(100);
	}

	@Override
	public void close() throws Exception {
		
	}

	@Override
	public void flushCommands() throws Exception {
		
	}

	@Override
	public void reset() throws Exception {
		
	}

	@Override
	public void prepareForWork() throws Exception {
		
	}
	
	@Override
	public void endingForWork() throws Exception {
		
	}
	
}
