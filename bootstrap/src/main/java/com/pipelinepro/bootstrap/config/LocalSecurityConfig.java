package com.pipelinepro.bootstrap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@Profile("local")
public class LocalSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder.encode("admin"))
                .authorities(
                        IntakeSecurityConfig.VIEW_DEBTOR_MASTER_DATA,
                        IntakeSecurityConfig.CREATE_DEBTOR,
                        IntakeSecurityConfig.VIEW_DEBT_MASTER_DATA,
                        IntakeSecurityConfig.CREATE_DEBT,
                        IntakeSecurityConfig.ACCOUNTING_READ
                )
                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}
