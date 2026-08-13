package com.nexus.exceptions;

public class InvalidListenerSignatureException extends NexusException {
    public InvalidListenerSignatureException(String message) {
        super(message);
    }

    public InvalidListenerSignatureException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
