package com.pipelinepro.domain.port.out;

import com.pipelinepro.domain.NationalNumberAccessLog;

public interface NationalNumberAccessLogGateway {
    void logAccess(NationalNumberAccessLog accessLog);
}
