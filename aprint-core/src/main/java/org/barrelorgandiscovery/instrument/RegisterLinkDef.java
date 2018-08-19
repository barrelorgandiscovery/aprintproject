package org.barrelorgandiscovery.instrument;

public class RegisterLinkDef {

	public String pipeStopSet = null;
	public String pipeStop = null;
	public int instrumentNumber = 0;

	public RegisterLinkDef(String pipeStopSet, String pipeStop,
			int instrumentNumber) {
		this.pipeStopSet = pipeStopSet;
		this.pipeStop = pipeStop;
		this.instrumentNumber = instrumentNumber;
	}

}
