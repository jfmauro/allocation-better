package com.pipelinepro.adapter.out.persistence.mapper;

import com.pipelinepro.domain.AllocationProposal;
import com.pipelinepro.domain.AllocationStatus;
import com.pipelinepro.domain.Debt;
import com.pipelinepro.domain.MatchingMethod;
import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.PaymentAllocation;
import com.pipelinepro.domain.PaymentStatus;
import com.pipelinepro.domain.ProposalStatus;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

final class DomainObjectReflectionFactory {

    private static final Constructor<Payment> PAYMENT_CONSTRUCTOR = constructor(
            Payment.class,
            UUID.class,
            String.class,
            BigDecimal.class,
            String.class,
            PaymentStatus.class,
            String.class,
            String.class,
            String.class,
            String.class,
            BigDecimal.class,
            Long.class,
            Instant.class,
            Instant.class);

    private static final Constructor<AllocationProposal> ALLOCATION_PROPOSAL_CONSTRUCTOR = constructor(
            AllocationProposal.class,
            UUID.class,
            UUID.class,
            ProposalStatus.class,
            MatchingMethod.class,
            String.class,
            String.class,
            Instant.class,
            UUID.class,
            Long.class,
            Instant.class,
            Instant.class);

    private static final Constructor<PaymentAllocation> PAYMENT_ALLOCATION_CONSTRUCTOR = constructor(
            PaymentAllocation.class,
            UUID.class,
            UUID.class,
            UUID.class,
            UUID.class,
            BigDecimal.class,
            AllocationStatus.class,
            String.class,
            String.class,
            String.class,
            Instant.class);

    private static final Field DEBTOR_ID_FIELD = field(Debt.class, "debtorId");
    private static final Field REFERENCE_FIELD = field(Debt.class, "reference");
    private static final Field ORIGINAL_AMOUNT_FIELD = field(Debt.class, "originalAmount");

    private DomainObjectReflectionFactory() {
    }

    static Payment payment(
            UUID id,
            String bankTransactionReference,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            String structuredCommunication,
            String freeCommunication,
            String payerName,
            String payerIbanMasked,
            BigDecimal remainingAmount,
            Long version,
            Instant createdAt,
            Instant updatedAt) {
        return instantiate(
                PAYMENT_CONSTRUCTOR,
                id,
                bankTransactionReference,
                amount,
                currency,
                status,
                structuredCommunication,
                freeCommunication,
                payerName,
                payerIbanMasked,
                remainingAmount,
                version,
                createdAt,
                updatedAt);
    }

    static AllocationProposal allocationProposal(
            UUID id,
            UUID paymentId,
            ProposalStatus status,
            MatchingMethod matchingMethod,
            String reason,
            String validatedBy,
            Instant validatedAt,
            UUID selectedDebtId,
            Long version,
            Instant createdAt,
            Instant updatedAt) {
        return instantiate(
                ALLOCATION_PROPOSAL_CONSTRUCTOR,
                id,
                paymentId,
                status,
                matchingMethod,
                reason,
                validatedBy,
                validatedAt,
                selectedDebtId,
                version,
                createdAt,
                updatedAt);
    }

    static PaymentAllocation paymentAllocation(
            UUID id,
            UUID paymentId,
            UUID debtId,
            UUID proposalId,
            BigDecimal amount,
            AllocationStatus status,
            String idempotencyKey,
            String commandId,
            String createdBy,
            Instant createdAt) {
        return instantiate(
                PAYMENT_ALLOCATION_CONSTRUCTOR,
                id,
                paymentId,
                debtId,
                proposalId,
                amount,
                status,
                idempotencyKey,
                commandId,
                createdBy,
                createdAt);
    }

    static UUID debtDebtorId(Debt debt) {
        return readField(DEBTOR_ID_FIELD, debt, UUID.class);
    }

    static String debtReference(Debt debt) {
        return readField(REFERENCE_FIELD, debt, String.class);
    }

    static BigDecimal debtOriginalAmount(Debt debt) {
        return readField(ORIGINAL_AMOUNT_FIELD, debt, BigDecimal.class);
    }

    private static <T> Constructor<T> constructor(Class<T> type, Class<?>... parameterTypes) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to resolve constructor for " + type.getSimpleName(), exception);
        }
    }

    private static Field field(Class<?> type, String name) {
        try {
            Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to resolve field " + name + " for " + type.getSimpleName(), exception);
        }
    }

    private static <T> T instantiate(Constructor<T> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to instantiate domain object", exception);
        }
    }

    private static <T> T readField(Field field, Object source, Class<T> expectedType) {
        try {
            Object value = field.get(source);
            return expectedType.cast(value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to read field " + field.getName(), exception);
        }
    }
}
