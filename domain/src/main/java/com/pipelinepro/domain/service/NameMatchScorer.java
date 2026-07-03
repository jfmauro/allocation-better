package com.pipelinepro.domain.service;

import com.pipelinepro.domain.MatchConfidence;
import com.pipelinepro.domain.value.NameNormalizer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NameMatchScorer {
    private static final Logger log = LoggerFactory.getLogger(NameMatchScorer.class);
    private final NameNormalizer normalizer;

    public NameMatchScorer(NameNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    public MatchConfidence score(String leftName, String rightName) {
        log.info("+++start score+++ ");
        String left = normalizer.normalize(leftName);
        String right = normalizer.normalize(rightName);
        MatchConfidence confidence;
        if (left.equals(right)) {
            confidence = MatchConfidence.HIGH;
        } else if (left.contains(right) || right.contains(left)) {
            confidence = MatchConfidence.MEDIUM;
        } else {
            double jaccard = tokenJaccard(left, right);
            confidence = jaccard >= 0.50d ? MatchConfidence.MEDIUM : MatchConfidence.LOW;
        }
        log.info("+++end score+++ ");
        return confidence;
    }

    private static double tokenJaccard(String left, String right) {
        Set<String> leftTokens = tokens(left);
        Set<String> rightTokens = tokens(right);
        Set<String> intersection = new HashSet<>(leftTokens);
        intersection.retainAll(rightTokens);
        Set<String> union = new HashSet<>(leftTokens);
        union.addAll(rightTokens);
        if (union.isEmpty()) {
            return 0.0d;
        }
        return (double) intersection.size() / union.size();
    }

    private static Set<String> tokens(String value) {
        Set<String> tokens = new HashSet<>();
        Arrays.stream(value.split(" "))
                .filter(token -> !token.isBlank())
                .forEach(tokens::add);
        return tokens;
    }
}
