package com.pipelinepro.domain.service;

import com.pipelinepro.domain.MatchConfidence;
import com.pipelinepro.domain.value.NameNormalizer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NameMatchingRulesTest {

    @Test
    void should_normalize_name() {
        NameNormalizer normalizer = new NameNormalizer();

        String normalized = normalizer.normalize("  Élodie   Van den-Broeck  ");

        assertThat(normalized).isEqualTo("ELODIE VAN DEN BROECK");
    }

    @Test
    void should_score_confidence_matrix() {
        NameMatchScorer scorer = new NameMatchScorer(new NameNormalizer());

        assertThat(scorer.score("Acme SA", "Acme SA")).isEqualTo(MatchConfidence.HIGH);
        assertThat(scorer.score("Acme Belgium", "Acme")).isEqualTo(MatchConfidence.MEDIUM);
        assertThat(scorer.score("Acme", "Globex Industries")).isEqualTo(MatchConfidence.LOW);
    }
}
