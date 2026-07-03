package com.pipelinepro.adapter.in.web.mapper;

import com.pipelinepro.adapter.in.web.v1.dto.request.ReceivePaymentRequest;
import com.pipelinepro.adapter.in.web.v1.dto.response.PaymentDetailsResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.PaymentResponse;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.port.in.command.ReceivePaymentCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface PaymentWebMapper {

    default ReceivePaymentCommand toReceivePaymentCommand(
            ReceivePaymentRequest request,
            UUID paymentId) {
        Instant receivedAt = resolveReceivedAt(request);
        String freeCommunication = resolveFreeCommunication(request);
        String payerIbanMasked = resolvePayerIbanMasked(request);
        return new ReceivePaymentCommand(
                paymentId,
                request.bankTransactionReference(),
                request.executionDate(),
                request.valueDate(),
                request.amount(),
                request.currency(),
                request.structuredCommunication(),
                freeCommunication,
                request.rawBankMessage(),
                request.payerName(),
                payerIbanMasked,
                receivedAt);
    }

    private static Instant resolveReceivedAt(ReceivePaymentRequest request) {
        if (request.receivedAt() != null) {
            return request.receivedAt();
        }
        if (request.valueDate() != null) {
            return request.valueDate();
        }
        if (request.executionDate() != null) {
            return request.executionDate();
        }
        return Instant.now();
    }

    private static String resolveFreeCommunication(ReceivePaymentRequest request) {
        if (request.freeCommunication() != null && !request.freeCommunication().isBlank()) {
            return request.freeCommunication();
        }
        return request.rawBankMessage();
    }

    private static String resolvePayerIbanMasked(ReceivePaymentRequest request) {
        if (request.payerIbanMasked() != null && !request.payerIbanMasked().isBlank()) {
            return request.payerIbanMasked();
        }
        if (request.payerIban() == null || request.payerIban().isBlank()) {
            return null;
        }
        String normalized = request.payerIban().replaceAll("\\s+", "");
        if (normalized.length() <= 4) {
            return "****";
        }
        return normalized.substring(0, 4) + "****";
    }

    @Mapping(target = "id", expression = "java(payment.id())")
    @Mapping(target = "bankTransactionReference", expression = "java(payment.bankTransactionReference())")
    @Mapping(target = "amount", expression = "java(payment.amount())")
    @Mapping(target = "remainingAmount", expression = "java(payment.remainingAmount())")
    @Mapping(target = "currency", expression = "java(payment.currency())")
    @Mapping(target = "status", expression = "java(payment.status())")
    @Mapping(target = "createdAt", expression = "java(payment.createdAt())")
    @Mapping(target = "updatedAt", expression = "java(payment.updatedAt())")
    PaymentResponse toPaymentResponse(Payment payment);

    @Mapping(target = "id", expression = "java(payment.id())")
    @Mapping(target = "bankTransactionReference", expression = "java(payment.bankTransactionReference())")
    @Mapping(target = "amount", expression = "java(payment.amount())")
    @Mapping(target = "remainingAmount", expression = "java(payment.remainingAmount())")
    @Mapping(target = "currency", expression = "java(payment.currency())")
    @Mapping(target = "executionDate", ignore = true)
    @Mapping(target = "valueDate", ignore = true)
    @Mapping(target = "status", expression = "java(payment.status())")
    @Mapping(target = "structuredCommunication", expression = "java(unwrap(payment.structuredCommunication()))")
    @Mapping(target = "freeCommunication", expression = "java(unwrap(payment.freeCommunication()))")
    @Mapping(target = "payerName", expression = "java(unwrap(payment.payerName()))")
    @Mapping(target = "payerIbanMasked", expression = "java(unwrap(payment.payerIbanMasked()))")
    @Mapping(target = "version", expression = "java(payment.version())")
    @Mapping(target = "createdAt", expression = "java(payment.createdAt())")
    @Mapping(target = "updatedAt", expression = "java(payment.updatedAt())")
    PaymentDetailsResponse toPaymentDetailsResponse(Payment payment);

    default <T> T unwrap(Optional<T> value) {
        return value == null ? null : value.orElse(null);
    }
}
