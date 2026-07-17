package com.pipelinepro.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SourceAggregateTypeTest {

    @Test
    void should_expose_extension_vocabulary_values() {
        assertThat(SourceAggregateType.values())
                .containsExactly(SourceAggregateType.DEBT, SourceAggregateType.PAYMENT, SourceAggregateType.ALLOCATION);
    }

    @Test
    void should_resolve_allocation_value() {
        assertThat(SourceAggregateType.valueOf("ALLOCATION")).isEqualTo(SourceAggregateType.ALLOCATION);
    }

    @Test
    void should_reject_legacy_payment_allocation_value() {
        assertThatThrownBy(() -> SourceAggregateType.valueOf("PAYMENT_ALLOCATION"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
