package com.pipelinepro.domain.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdentifierValidatorsTest {

    @Test
    void should_validate_national_register_number() {
        NationalRegisterNumber niss = NationalRegisterNumber.of("85073003328");
        assertThat(niss.digits()).isEqualTo("85073003328");
    }

    @Test
    void should_validate_enterprise_and_vat_numbers() {
        EnterpriseNumber bce = EnterpriseNumber.of("0820501224");
        VatNumber vat = VatNumber.belgian("BE0820501224");

        assertThat(bce.digits()).isEqualTo("0820501224");
        assertThat(vat.formatted()).isEqualTo("BE0820501224");
    }

    @Test
    void should_reject_invalid_identifier_checksums() {
        assertThatThrownBy(() -> NationalRegisterNumber.of("85073003327"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("checksum is invalid");

        assertThatThrownBy(() -> EnterpriseNumber.of("0820501225"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("checksum is invalid");
    }
}
