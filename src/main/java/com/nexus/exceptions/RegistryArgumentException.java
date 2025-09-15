package com.nexus.exceptions;

public class RegistryArgumentException extends NexusException {
	public RegistryArgumentException(String message) {
		super(message);
	}
	public RegistryArgumentException(String message, Throwable cause) {
		super(message, cause);
	}
}
