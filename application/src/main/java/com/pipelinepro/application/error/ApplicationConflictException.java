package com.pipelinepro.application.error;

public final class ApplicationConflictException extends RuntimeException {

    public ApplicationConflictException(String message) {
        super(message);
    }
}
