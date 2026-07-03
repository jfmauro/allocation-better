package com.pipelinepro.application.error;

public final class ReferencedResourceNotFoundException extends RuntimeException {

    public ReferencedResourceNotFoundException(String message) {
        super(message);
    }

    public ReferencedResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
