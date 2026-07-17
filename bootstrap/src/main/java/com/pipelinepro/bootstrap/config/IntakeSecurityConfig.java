package com.pipelinepro.bootstrap.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.annotation.Order;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class IntakeSecurityConfig {

    public static final String VIEW_DEBTOR_MASTER_DATA = "VIEW_DEBTOR_MASTER_DATA";
    public static final String CREATE_DEBTOR = "CREATE_DEBTOR";
    public static final String VIEW_DEBT_MASTER_DATA = "VIEW_DEBT_MASTER_DATA";
    public static final String CREATE_DEBT = "CREATE_DEBT";
    public static final String ACCOUNTING_READ = "ACCOUNTING_READ";

    private static final Logger log = LoggerFactory.getLogger(IntakeSecurityConfig.class);
    private final Environment environment;

    public IntakeSecurityConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain intakeSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        log.info("+++start intakeSecurityFilterChain+++");
        try {
            if (environment.acceptsProfiles(Profiles.of("local"))) {
                httpSecurity.csrf(csrf -> csrf.disable());
            }
            httpSecurity
                    .securityMatcher("/debtors", "/debtors/**", "/debts", "/debts/**")
                    .httpBasic(Customizer.withDefaults())
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers(HttpMethod.POST, "/debtors").hasAuthority(CREATE_DEBTOR)
                            .requestMatchers(HttpMethod.GET, "/debtors", "/debtors/*").hasAuthority(VIEW_DEBTOR_MASTER_DATA)
                            .requestMatchers(HttpMethod.POST, "/debts").hasAuthority(CREATE_DEBT)
                            .requestMatchers(HttpMethod.GET, "/debts", "/debts/*", "/debtors/*/debts")
                            .hasAuthority(VIEW_DEBT_MASTER_DATA)
                            .anyRequest().denyAll());
            return httpSecurity.build();
        } finally {
            log.info("+++end intakeSecurityFilterChain+++");
        }
    }

    @Bean
    @Order(2)
    public SecurityFilterChain h2ConsoleSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        log.info("+++start h2ConsoleSecurityFilterChain+++");
        try {
            if (environment.acceptsProfiles(Profiles.of("local"))) {
                httpSecurity.csrf(csrf -> csrf.disable());
            }
            httpSecurity
                    .securityMatcher("/h2-console/**")
                    .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                    .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
            return httpSecurity.build();
        } finally {
            log.info("+++end h2ConsoleSecurityFilterChain+++");
        }
    }

    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        log.info("+++start defaultSecurityFilterChain+++");
        try {
            if (environment.acceptsProfiles(Profiles.of("local"))) {
                httpSecurity.csrf(csrf -> csrf.disable());
            }
            httpSecurity
                    .httpBasic(Customizer.withDefaults())
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers("/app/**").permitAll()
                            .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                            .requestMatchers(HttpMethod.GET, "/accounting-entries", "/accounting-entries/**")
                            .hasAuthority(ACCOUNTING_READ)
                            .anyRequest().authenticated());
            return httpSecurity.build();
        } finally {
            log.info("+++end defaultSecurityFilterChain+++");
        }
    }
}
