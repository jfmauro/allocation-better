package com.pipelinepro.adapter.in.web.mapper;

import com.pipelinepro.adapter.in.web.v1.dto.request.CreateDebtRequest;
import com.pipelinepro.adapter.in.web.v1.dto.response.DebtListResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.DebtResponse;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.value.StructuredCommunication;
import com.pipelinepro.domain.port.in.command.CreateDebtCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DebtWebMapper {

    default CreateDebtCommand toCreateDebtCommand(CreateDebtRequest request, String idempotencyKey, String correlationId) {
        return new CreateDebtCommand(
                request.debtorId(),
                request.reference(),
                request.originalAmount(),
                request.currency(),
                request.openingStatus(),
                request.dueDate(),
                idempotencyKey,
                correlationId);
    }

    @Mapping(target = "id", expression = "java(debt.id())")
    @Mapping(target = "debtorId", expression = "java(debt.debtorId())")
    @Mapping(target = "remainingAmount", expression = "java(debt.remainingAmount())")
    @Mapping(target = "currency", expression = "java(debt.currency())")
    @Mapping(target = "status", expression = "java(debt.status())")
    @Mapping(target = "dueDate", expression = "java(unwrap(debt.dueDate()))")
    @Mapping(target = "structuredCommunication", expression = "java(resolveStructuredCommunication(debt))")
    @Mapping(target = "freeCommunication", expression = "java(resolveFreeCommunication(debt))")
    @Mapping(target = "version", expression = "java(debt.version())")
    @Mapping(target = "createdAt", expression = "java(debt.createdAt())")
    @Mapping(target = "updatedAt", expression = "java(debt.updatedAt())")
    DebtResponse toDebtResponse(Debt debt);

    default DebtListResponse toDebtListResponse(UUID debtorId, List<Debt> debts) {
        return new DebtListResponse(debtorId, debts.stream().map(this::toDebtResponse).toList());
    }

    default <T> T unwrap(Optional<T> value) {
        return value == null ? null : value.orElse(null);
    }

    default String resolveStructuredCommunication(Debt debt) {
        try {
            return StructuredCommunication.of(debt.reference()).formatted();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    default String resolveFreeCommunication(Debt debt) {
        return resolveStructuredCommunication(debt) == null ? debt.reference() : null;
    }
}
