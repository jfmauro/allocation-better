package com.pipelinepro.adapter.in.web.mapper;

import com.pipelinepro.adapter.in.web.v1.dto.response.MatchResultResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.ProposalCreationResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.StructuredMatchResponse;
import com.pipelinepro.domain.port.in.result.MatchPaymentResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Optional;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MatchingWebMapper {

    @Mapping(target = "paymentId", source = "paymentId")
    MatchResultResponse toMatchResultResponse(UUID paymentId, MatchPaymentResult result);

    @Mapping(target = "paymentId", source = "paymentId")
    StructuredMatchResponse toStructuredMatchResponse(UUID paymentId, MatchPaymentResult result);

    @Mapping(target = "paymentId", source = "paymentId")
    ProposalCreationResponse toProposalCreationResponse(UUID paymentId, MatchPaymentResult result);

    default <T> T unwrap(Optional<T> value) {
        return value == null ? null : value.orElse(null);
    }
}
