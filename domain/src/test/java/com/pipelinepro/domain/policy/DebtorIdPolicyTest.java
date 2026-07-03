package com.pipelinepro.domain.policy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebtorIdPolicyTest {

    @Test
    void should_generate_uuid_v4_debtor_id() {
        UUID debtorId = DebtorIdPolicy.newServerGeneratedId();

        assertThat(debtorId.version()).isEqualTo(4);
        assertThat(debtorId.toString()).isEqualTo(debtorId.toString().toLowerCase());
    }

    @Test
    void should_accept_canonical_lowercase_uuid_v4() {
        UUID debtorId = DebtorIdPolicy.newServerGeneratedId();

        UUID parsed = DebtorIdPolicy.requireCanonicalLowercaseV4(debtorId.toString());

        assertThat(parsed).isEqualTo(debtorId);
    }

    @Test
    void should_reject_non_canonical_or_non_v4_uuid() {
        UUID v4 = DebtorIdPolicy.newServerGeneratedId();

        assertThatThrownBy(() -> DebtorIdPolicy.requireCanonicalLowercaseV4(v4.toString().toUpperCase()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("canonical lowercase");

        assertThatThrownBy(() -> DebtorIdPolicy.requireCanonicalLowercaseV4("f47ac10b-58cc-11cf-ae64-08002b2c3d09"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UUID v4");
    }

    @Test
    void should_enforce_debtor_id_immutability() {
        UUID debtorId = DebtorIdPolicy.newServerGeneratedId();

        DebtorIdPolicy.requireImmutable(debtorId, debtorId);

        assertThatThrownBy(() -> DebtorIdPolicy.requireImmutable(debtorId, DebtorIdPolicy.newServerGeneratedId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("immutable");
    }
}
