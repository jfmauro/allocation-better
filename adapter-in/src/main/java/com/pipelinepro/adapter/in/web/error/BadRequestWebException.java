package com.pipelinepro.adapter.in.web.error;

public class BadRequestWebException extends RuntimeException {

    public BadRequestWebException(String message) {
        super(message);
    }
}
