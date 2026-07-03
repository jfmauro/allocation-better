package com.pipelinepro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PipelineProApplication {

    private static final Logger log = LoggerFactory.getLogger(PipelineProApplication.class);

    public static void main(String[] args) {
        log.info("+++start main+++");
        try {
            SpringApplication.run(PipelineProApplication.class, args);
        } finally {
            log.info("+++end main+++");
        }
    }
}
