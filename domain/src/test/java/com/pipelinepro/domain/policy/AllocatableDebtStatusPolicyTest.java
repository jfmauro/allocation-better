package com.pipelinepro.domain.policy;

import com.pipelinepro.domain.DebtStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllocatableDebtStatusPolicyTest {

    @Test
    void should_allow_only_open_and_partially_paid_statuses() {
        assertThat(AllocatableDebtStatusPolicy.isAllocatable(DebtStatus.OPEN)).isTrue();
        assertThat(AllocatableDebtStatusPolicy.isAllocatable(DebtStatus.PARTIALLY_PAID)).isTrue();
        assertThat(AllocatableDebtStatusPolicy.supportedStatuses())
                .containsExactlyInAnyOrder(DebtStatus.OPEN, DebtStatus.PARTIALLY_PAID);
    }

    @Test
    void should_reject_non_allocatable_statuses() {
        assertThat(AllocatableDebtStatusPolicy.isAllocatable(DebtStatus.PAID)).isFalse();
        assertThat(AllocatableDebtStatusPolicy.isAllocatable(DebtStatus.CANCELLED)).isFalse();

        assertThatThrownBy(() -> AllocatableDebtStatusPolicy.requireAllocatable(DebtStatus.PAID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OPEN or PARTIALLY_PAID");
    }
}
