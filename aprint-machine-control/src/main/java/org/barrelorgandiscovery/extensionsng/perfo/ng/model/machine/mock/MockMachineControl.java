package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.mock;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControlListener;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineStatus;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.Command;

public class MockMachineControl implements MachineControl {

	private MachineControlListener listener;

	@Override
	public void setMachineControlListener(MachineControlListener listener) {
		this.listener = listener;
	}

	@Override
	public void sendCommand(Command command) throws Exception {
		assert command != null;
		if (listener != null) {
			listener.rawElementSent(command.toString() + "\n");
		}

		long millis = (long) (Math.random() * 1000);
		Thread.sleep(millis);

		if (listener != null) {
			listener.rawElementReceived("OK\n");
		}
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

	@Override
	public MachineStatus getStatus() {
		return MachineStatus.CONNECTED;
	}

}
