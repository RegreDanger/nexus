package com.nexus.exceptions;

/**
 * Base unchecked exception for the Nexus library.
 */
public class NexusException extends RuntimeException {
	public NexusException(String message) {
		super(message);
	}
	public NexusException(String message, Throwable cause) {
		super(message, cause);
	}
	public NexusException(Throwable cause) {
		super(cause);
	}
}
