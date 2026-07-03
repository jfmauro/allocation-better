package com.pipelinepro.adapter.in.web.error;

public class ForbiddenWebException extends RuntimeException {

    public ForbiddenWebException(String message) {
        super(message);
    }
}
