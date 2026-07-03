package com.pipelinepro.domain.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StructuredCommunicationTest {

    @Test
    void should_validate_and_format_structured_communication() {
        StructuredCommunication communication = StructuredCommunication.of("+++123/4567/89002+++");

        assertThat(communication.digits()).isEqualTo("123456789002");
        assertThat(communication.formatted()).isEqualTo("+++123/4567/89002+++");
    }

    @Test
    void should_reject_invalid_checksum() {
        assertThatThrownBy(() -> StructuredCommunication.of("123456789099"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("checksum is invalid");
    }
}
