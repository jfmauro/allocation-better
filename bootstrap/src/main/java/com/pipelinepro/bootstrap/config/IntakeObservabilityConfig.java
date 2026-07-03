package com.pipelinepro.bootstrap.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.observation.ObservationRegistry;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IntakeObservabilityConfig {

    private static final Logger log = LoggerFactory.getLogger(IntakeObservabilityConfig.class);

    @Bean
    public MeterFilter intakeCommonTagsMeterFilter() {
        log.info("+++start intakeCommonTagsMeterFilter+++");
        try {
            return MeterFilter.commonTags(List.of(Tag.of("capability", "intake")));
        } finally {
            log.info("+++end intakeCommonTagsMeterFilter+++");
        }
    }

    @Bean
    public IntakeObservabilityFilter intakeObservabilityFilter(
            ObservationRegistry observationRegistry,
            MeterRegistry meterRegistry) {
        log.info("+++start intakeObservabilityFilter+++");
        try {
            return new IntakeObservabilityFilter(observationRegistry, meterRegistry);
        } finally {
            log.info("+++end intakeObservabilityFilter+++");
        }
    }

    @Bean
    public FilterRegistrationBean<IntakeObservabilityFilter> intakeObservabilityFilterRegistration(
            IntakeObservabilityFilter intakeObservabilityFilter) {
        log.info("+++start intakeObservabilityFilterRegistration+++");
        try {
            FilterRegistrationBean<IntakeObservabilityFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(intakeObservabilityFilter);
            registration.setOrder(10);
            registration.addUrlPatterns("/*");
            return registration;
        } finally {
            log.info("+++end intakeObservabilityFilterRegistration+++");
        }
    }
}
