package com.pipelinepro.domain.port.out;

import com.pipelinepro.domain.AuditEvent;

public interface AuditEventGateway {
    void append(AuditEvent event);
}
