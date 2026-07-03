package com.pipelinepro.adapter.out.persistence.mapper;

import com.pipelinepro.adapter.out.persistence.entity.NationalNumberAccessLogEntity;
import com.pipelinepro.domain.NationalNumberAccessLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NationalNumberAccessLogEntityMapper {

    @Mapping(target = "id", expression = "java(accessLog.id())")
    @Mapping(target = "paymentId", expression = "java(accessLog.paymentId())")
    @Mapping(target = "debtorId", expression = "java(accessLog.debtorId())")
    @Mapping(target = "userId", expression = "java(accessLog.userId())")
    @Mapping(target = "reason", expression = "java(accessLog.reason())")
    @Mapping(target = "createdAt", expression = "java(accessLog.createdAt())")
    NationalNumberAccessLogEntity toEntity(NationalNumberAccessLog accessLog);

    default NationalNumberAccessLog toDomain(NationalNumberAccessLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return new NationalNumberAccessLog(
                entity.getId(),
                entity.getPaymentId(),
                entity.getDebtorId(),
                entity.getUserId(),
                entity.getReason(),
                entity.getCreatedAt());
    }
}
