package com.pipelinepro.application.error;

public final class ApplicationBadRequestException extends RuntimeException {

    public ApplicationBadRequestException(String message) {
        super(message);
    }
}
