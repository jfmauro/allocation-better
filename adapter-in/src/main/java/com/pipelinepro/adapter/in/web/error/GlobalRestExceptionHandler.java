package com.pipelinepro.adapter.in.web.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalRestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalRestExceptionHandler.class);
    private static final AuthenticationTrustResolverImpl TRUST_RESOLVER = new AuthenticationTrustResolverImpl();

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        log.warn("+++start handleValidationException+++");
        try {
            String message = exception.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(this::formatFieldError)
                    .distinct()
                    .reduce((left, right) -> left + "; " + right)
                    .orElse("Validation failed");
            return buildProblemDetail(HttpStatus.BAD_REQUEST, message, request);
        } finally {
            log.warn("+++end handleValidationException+++");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException exception, HttpServletRequest request) {
        log.warn("+++start handleIllegalArgumentException+++");
        try {
            log.warn("+++illegal argument details kept server-side+++", exception);
            return buildProblemDetail(HttpStatus.BAD_REQUEST, "Bad request", request);
        } finally {
            log.warn("+++end handleIllegalArgumentException+++");
        }
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail handleRequestParsingException(Exception exception, HttpServletRequest request) {
        log.warn("+++start handleRequestParsingException+++");
        try {
            log.warn("+++request parsing details kept server-side+++", exception);
            return buildProblemDetail(HttpStatus.BAD_REQUEST, "Bad request", request);
        } finally {
            log.warn("+++end handleRequestParsingException+++");
        }
    }

    @ExceptionHandler(BadRequestWebException.class)
    public ProblemDetail handleBadRequestWebException(BadRequestWebException exception, HttpServletRequest request) {
        log.warn("+++start handleBadRequestWebException+++");
        try {
            log.warn("+++bad request details kept server-side+++", exception);
            return buildProblemDetail(HttpStatus.BAD_REQUEST, "Bad request", request);
        } finally {
            log.warn("+++end handleBadRequestWebException+++");
        }
    }

    @ExceptionHandler(NotFoundWebException.class)
    public ProblemDetail handleNotFoundWebException(NotFoundWebException exception, HttpServletRequest request) {
        log.warn("+++start handleNotFoundWebException+++");
        try {
            log.warn("+++not found details kept server-side+++", exception);
            return buildProblemDetail(HttpStatus.NOT_FOUND, "Resource not found", request);
        } finally {
            log.warn("+++end handleNotFoundWebException+++");
        }
    }

    @ExceptionHandler(ConflictWebException.class)
    public ProblemDetail handleConflictWebException(ConflictWebException exception, HttpServletRequest request) {
        log.warn("+++start handleConflictWebException+++");
        try {
            log.warn("+++conflict details kept server-side+++", exception);
            return buildProblemDetail(HttpStatus.CONFLICT, "Conflict detected", request);
        } finally {
            log.warn("+++end handleConflictWebException+++");
        }
    }

    @ExceptionHandler(ForbiddenWebException.class)
    public ProblemDetail handleForbiddenWebException(ForbiddenWebException exception, HttpServletRequest request) {
        log.warn("+++start handleForbiddenWebException+++");
        try {
            log.warn("+++forbidden details kept server-side+++", exception);
            return buildProblemDetail(HttpStatus.FORBIDDEN, "Forbidden", request);
        } finally {
            log.warn("+++end handleForbiddenWebException+++");
        }
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ProblemDetail handleAuthorizationDeniedException(AuthorizationDeniedException exception, HttpServletRequest request) {
        log.warn("+++start handleAuthorizationDeniedException+++");
        try {
            log.warn("+++authorization denied details kept server-side+++", exception);
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            HttpStatus status = authentication == null || TRUST_RESOLVER.isAnonymous(authentication)
                    ? HttpStatus.UNAUTHORIZED
                    : HttpStatus.FORBIDDEN;
            return buildProblemDetail(status, status == HttpStatus.UNAUTHORIZED ? "Unauthorized" : "Forbidden", request);
        } finally {
            log.warn("+++end handleAuthorizationDeniedException+++");
        }
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ProblemDetail handleAuthenticationCredentialsNotFoundException(AuthenticationCredentialsNotFoundException exception, HttpServletRequest request) {
        log.warn("+++start handleAuthenticationCredentialsNotFoundException+++");
        try {
            log.warn("+++authentication missing details kept server-side+++", exception);
            return buildProblemDetail(HttpStatus.UNAUTHORIZED, "Unauthorized", request);
        } finally {
            log.warn("+++end handleAuthenticationCredentialsNotFoundException+++");
        }
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException exception, HttpServletRequest request) {
        log.warn("+++start handleIllegalStateException+++");
        try {
            log.warn("+++illegal state details kept server-side+++", exception);
            return buildProblemDetail(HttpStatus.CONFLICT, "Conflict detected", request);
        } finally {
            log.warn("+++end handleIllegalStateException+++");
        }
    }

    @ExceptionHandler(SecurityException.class)
    public ProblemDetail handleSecurityException(SecurityException exception, HttpServletRequest request) {
        log.warn("+++start handleSecurityException+++");
        try {
            log.warn("+++security details kept server-side+++", exception);
            return buildProblemDetail(HttpStatus.FORBIDDEN, "Forbidden", request);
        } finally {
            log.warn("+++end handleSecurityException+++");
        }
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnhandledException(Exception exception, HttpServletRequest request) {
        log.error("+++start handleUnhandledException+++", exception);
        try {
            return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request);
        } finally {
            log.error("+++end handleUnhandledException+++");
        }
    }

    private ProblemDetail buildProblemDetail(HttpStatus status, String message, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("message", message);
        problemDetail.setProperty("path", request.getRequestURI());
        return problemDetail;
    }

    private String formatFieldError(FieldError fieldError) {
        String defaultMessage = fieldError.getDefaultMessage() == null ? "is invalid" : fieldError.getDefaultMessage();
        return fieldError.getField() + " " + defaultMessage;
    }

}
