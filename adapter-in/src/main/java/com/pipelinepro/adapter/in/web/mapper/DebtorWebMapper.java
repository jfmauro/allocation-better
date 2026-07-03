package com.pipelinepro.adapter.in.web.mapper;

import com.pipelinepro.adapter.in.web.v1.dto.request.CreateDebtorRequest;
import com.pipelinepro.adapter.in.web.v1.dto.response.DebtorResponse;
import com.pipelinepro.domain.Debtor;
import com.pipelinepro.domain.port.in.command.CreateDebtorCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DebtorWebMapper {

    default CreateDebtorCommand toCreateDebtorCommand(CreateDebtorRequest request, String idempotencyKey, String correlationId) {
        return new CreateDebtorCommand(
                request.debtorType(),
                request.displayName(),
                request.nationalNumber(),
                request.enterpriseNumber(),
                idempotencyKey,
                correlationId);
    }

    @Mapping(target = "id", expression = "java(debtor.id())")
    @Mapping(target = "type", expression = "java(debtor.type())")
    @Mapping(target = "displayName", expression = "java(debtor.displayName())")
    @Mapping(target = "nationalNumber", expression = "java(unwrap(debtor.nationalNumber()))")
    @Mapping(target = "enterpriseNumber", expression = "java(unwrap(debtor.enterpriseNumber()))")
    @Mapping(target = "active", expression = "java(debtor.active())")
    @Mapping(target = "createdAt", expression = "java(debtor.createdAt())")
    DebtorResponse toDebtorResponse(Debtor debtor);

    default <T> T unwrap(java.util.Optional<T> value) {
        return value == null ? null : value.orElse(null);
    }
}
