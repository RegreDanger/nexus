package com.nexus.exceptions;

public class DependencyInstantiationException extends RuntimeException {
    public DependencyInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DependencyInstantiationException(String message) {
        this(message, null);
    }

}
