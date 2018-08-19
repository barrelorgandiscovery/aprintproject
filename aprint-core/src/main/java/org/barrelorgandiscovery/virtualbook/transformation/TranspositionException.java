package org.barrelorgandiscovery.virtualbook.transformation;

/**
 * transposition exception
 * 
 * @author Freydiere Patrice
 * 
 */
public class TranspositionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4901542332070910977L;

	public TranspositionException() {

	}

	public TranspositionException(String message) {
		super(message);
	}

	public TranspositionException(Throwable cause) {
		super(cause);
	}

	public TranspositionException(String message, Throwable cause) {
		super(message, cause);
	}

}
