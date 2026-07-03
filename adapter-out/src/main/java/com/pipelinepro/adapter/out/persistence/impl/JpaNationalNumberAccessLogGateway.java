package com.pipelinepro.adapter.out.persistence.impl;

import com.pipelinepro.adapter.out.persistence.mapper.NationalNumberAccessLogEntityMapper;
import com.pipelinepro.adapter.out.persistence.repository.SpringDataNationalNumberAccessLogRepository;
import com.pipelinepro.domain.NationalNumberAccessLog;
import com.pipelinepro.domain.port.out.NationalNumberAccessLogGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class JpaNationalNumberAccessLogGateway implements NationalNumberAccessLogGateway {

    private static final Logger log = LoggerFactory.getLogger(JpaNationalNumberAccessLogGateway.class);

    private final SpringDataNationalNumberAccessLogRepository springDataNationalNumberAccessLogRepository;
    private final NationalNumberAccessLogEntityMapper nationalNumberAccessLogEntityMapper;

    public JpaNationalNumberAccessLogGateway(
            SpringDataNationalNumberAccessLogRepository springDataNationalNumberAccessLogRepository,
            NationalNumberAccessLogEntityMapper nationalNumberAccessLogEntityMapper) {
        this.springDataNationalNumberAccessLogRepository = springDataNationalNumberAccessLogRepository;
        this.nationalNumberAccessLogEntityMapper = nationalNumberAccessLogEntityMapper;
    }

    @Override
    public void logAccess(NationalNumberAccessLog accessLog) {
        log.info("+++start logAccess+++");
        try {
            var accessLogEntity = nationalNumberAccessLogEntityMapper.toEntity(accessLog);
            accessLogEntity.setId(null);
            springDataNationalNumberAccessLogRepository.save(accessLogEntity);
        } finally {
            log.info("+++end logAccess+++");
        }
    }
}
