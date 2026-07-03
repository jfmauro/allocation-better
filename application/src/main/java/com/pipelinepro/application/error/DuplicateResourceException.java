package com.pipelinepro.application.error;

public final class DuplicateResourceException extends IllegalStateException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
