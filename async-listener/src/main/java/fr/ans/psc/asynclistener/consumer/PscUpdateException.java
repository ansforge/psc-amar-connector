/*
 * Copyright © 2022-2024 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
