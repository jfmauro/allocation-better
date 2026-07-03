package com.pipelinepro.domain.port.out;

import com.pipelinepro.domain.port.out.command.PublishIntakeAuditEventCommand;

public interface IntakeAuditEventGateway {
    void publish(PublishIntakeAuditEventCommand command);
}
