package org.barrelorgandiscovery.repository;

public class RepositoryException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4138277750696635559L;

	public RepositoryException() {
	}

	public RepositoryException(String message) {
		super(message);
	}

	public RepositoryException(Throwable cause) {
		super(cause);
	}

	public RepositoryException(String message, Throwable cause) {
		super(message, cause);
	}

}
