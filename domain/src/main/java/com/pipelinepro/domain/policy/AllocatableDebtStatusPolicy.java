package com.pipelinepro.domain.policy;

import com.pipelinepro.domain.DebtStatus;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AllocatableDebtStatusPolicy {
    private static final Logger log = LoggerFactory.getLogger(AllocatableDebtStatusPolicy.class);
    private static final Set<DebtStatus> ALLOCATABLE_STATUSES = Set.of(DebtStatus.OPEN, DebtStatus.PARTIALLY_PAID);

    private AllocatableDebtStatusPolicy() {
    }

    public static boolean isAllocatable(DebtStatus debtStatus) {
        log.info("+++start isAllocatable+++");
        if (debtStatus == null) {
            throw new IllegalArgumentException("debtStatus must be non-null");
        }
        boolean allocatable = ALLOCATABLE_STATUSES.contains(debtStatus);
        log.info("+++end isAllocatable+++");
        return allocatable;
    }

    public static void requireAllocatable(DebtStatus debtStatus) {
        log.info("+++start requireAllocatable+++");
        if (!isAllocatable(debtStatus)) {
            throw new IllegalArgumentException("debtStatus must be allocatable (OPEN or PARTIALLY_PAID)");
        }
        log.info("+++end requireAllocatable+++");
    }

    public static Set<DebtStatus> supportedStatuses() {
        log.info("+++start supportedStatuses+++");
        log.info("+++end supportedStatuses+++");
        return ALLOCATABLE_STATUSES;
    }
}
