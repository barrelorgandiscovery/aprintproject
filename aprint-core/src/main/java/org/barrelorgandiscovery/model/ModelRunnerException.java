package org.barrelorgandiscovery.model;

public class ModelRunnerException extends Exception {

	private ModelStep errorStep;

	public ModelRunnerException(ModelStep ms, Throwable cause) {
		super("Error in executing the element " + cause.getMessage() + " in " + ms, cause);
		this.errorStep = ms;
	}

	public ModelStep getModelStep() {
		return errorStep;
	}

}
