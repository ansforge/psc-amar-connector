/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.asynclistener.consumer;

/**
 * The Class PscUpdateException.
 */
public class PscUpdateException extends Exception {

	/**
	 * Instantiates a new psc update exception.
	 */
	public PscUpdateException() {
		super();
	}

	/**
	 * Instantiates a new psc update exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 * @param enableSuppression the enable suppression
	 * @param writableStackTrace the writable stack trace
	 */
	public PscUpdateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Instantiates a new psc update exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public PscUpdateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new psc update exception.
	 *
	 * @param message the message
	 */
	public PscUpdateException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new psc update exception.
	 *
	 * @param cause the cause
	 */
	public PscUpdateException(Throwable cause) {
		super(cause);
	}

}
