package com.pipelinepro.bootstrap.config;

import io.micrometer.common.KeyValue;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

final class IntakeObservabilityFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    private static final Pattern SAFE_CORRELATION_ID_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{1,64}$");

    private final ObservationRegistry observationRegistry;
    private final Counter createDebtorCounter;
    private final Counter createDebtCounter;

    IntakeObservabilityFilter(ObservationRegistry observationRegistry, MeterRegistry meterRegistry) {
        this.observationRegistry = observationRegistry;
        this.createDebtorCounter = meterRegistry.counter("pipelinepro.intake.requests", "operation", "create_debtor");
        this.createDebtCounter = meterRegistry.counter("pipelinepro.intake.requests", "operation", "create_debt");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String operation = resolveOperation(request);
        if (operation == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        boolean hasSafeCorrelationId = correlationId != null && SAFE_CORRELATION_ID_PATTERN.matcher(correlationId).matches();
        if (hasSafeCorrelationId) {
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
        }

        Observation observation = Observation.start("pipelinepro.intake.request", observationRegistry)
                .lowCardinalityKeyValue(KeyValue.of("operation", operation));

        try (Observation.Scope ignored = observation.openScope()) {
            counterForOperation(operation).increment();
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            observation.error(exception);
            throw exception;
        } finally {
            observation.lowCardinalityKeyValue(KeyValue.of("status", String.valueOf(response.getStatus())));
            observation.stop();
            if (hasSafeCorrelationId) {
                MDC.remove(CORRELATION_ID_MDC_KEY);
            }
        }
    }

    private Counter counterForOperation(String operation) {
        return "create_debtor".equals(operation) ? createDebtorCounter : createDebtCounter;
    }

    private String resolveOperation(HttpServletRequest request) {
        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            return null;
        }
        String requestUri = request.getRequestURI();
        if ("/debtors".equals(requestUri)) {
            return "create_debtor";
        }
        if ("/debts".equals(requestUri)) {
            return "create_debt";
        }
        return null;
    }
}
