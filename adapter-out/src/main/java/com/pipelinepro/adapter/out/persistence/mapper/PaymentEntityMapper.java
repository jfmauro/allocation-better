package com.pipelinepro.adapter.out.persistence.mapper;

import com.pipelinepro.adapter.out.persistence.entity.PaymentEntity;
import com.pipelinepro.domain.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentEntityMapper {

    @Mapping(target = "id", expression = "java(payment.id())")
    @Mapping(target = "bankTransactionReference", expression = "java(payment.bankTransactionReference())")
    @Mapping(target = "amount", expression = "java(payment.amount())")
    @Mapping(target = "remainingAmount", expression = "java(payment.remainingAmount())")
    @Mapping(target = "currency", expression = "java(payment.currency())")
    @Mapping(target = "status", expression = "java(payment.status())")
    @Mapping(target = "structuredCommunication", expression = "java(payment.structuredCommunication().orElse(null))")
    @Mapping(target = "freeCommunication", expression = "java(payment.freeCommunication().orElse(null))")
    @Mapping(target = "payerName", expression = "java(payment.payerName().orElse(null))")
    @Mapping(target = "payerIbanMasked", expression = "java(payment.payerIbanMasked().orElse(null))")
    @Mapping(target = "version", expression = "java(payment.version())")
    @Mapping(target = "createdAt", expression = "java(payment.createdAt())")
    @Mapping(target = "updatedAt", expression = "java(payment.updatedAt())")
    PaymentEntity toEntity(Payment payment);

    default Payment toDomain(PaymentEntity entity) {
        if (entity == null) {
            return null;
        }
        return DomainObjectReflectionFactory.payment(
                entity.getId(),
                entity.getBankTransactionReference(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getStructuredCommunication(),
                entity.getFreeCommunication(),
                entity.getPayerName(),
                entity.getPayerIbanMasked(),
                entity.getRemainingAmount(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
