package com.pipelinepro.adapter.in.web.mapper;

import com.pipelinepro.adapter.in.web.v1.dto.request.ValidateProposalRequest;
import com.pipelinepro.domain.port.in.command.ValidateProposalCommand;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProposalWebMapperTest {

    private final ProposalWebMapper mapper = Mappers.getMapper(ProposalWebMapper.class);

    @Test
    void toValidateProposalCommand_shouldKeepActorAndReasonUnchanged() {
        UUID proposalId = UUID.randomUUID();
        ValidateProposalRequest request = new ValidateProposalRequest(
                UUID.randomUUID(),
                new BigDecimal("10.00"),
                "john.doe",
                "Manual validation",
                Instant.parse("2026-07-03T12:00:00Z"));

        ValidateProposalCommand command = mapper.toValidateProposalCommand(
                proposalId,
                request,
                request.occurredAt());

        assertThat(command.actor()).isEqualTo("john.doe");
        assertThat(command.reason()).isEqualTo("Manual validation");
    }
}
