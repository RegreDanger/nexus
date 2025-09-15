package com.nexus.exceptions;

public class DependencyInstantiationException extends NexusException {
	public DependencyInstantiationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DependencyInstantiationException(String message) {
		this(message, null);
	}
}
