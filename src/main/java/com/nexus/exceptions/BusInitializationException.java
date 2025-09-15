package com.nexus.exceptions;

public class BusInitializationException extends NexusException {

	public BusInitializationException(String message) {
		super(message);
	}

	public BusInitializationException(String message, Throwable cause) {
		super(message, cause);
	}
}