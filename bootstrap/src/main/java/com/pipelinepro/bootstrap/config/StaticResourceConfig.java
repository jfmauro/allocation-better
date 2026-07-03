package com.pipelinepro.bootstrap.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(StaticResourceConfig.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("+++start addResourceHandlers+++");
        try {
            registry.addResourceHandler("/app/**")
                    .addResourceLocations("classpath:/static/");
        } finally {
            log.info("+++end addResourceHandlers+++");
        }
    }
}
