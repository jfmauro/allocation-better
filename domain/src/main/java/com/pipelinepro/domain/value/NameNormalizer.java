package com.pipelinepro.domain.value;

import java.text.Normalizer;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NameNormalizer {
    private static final Logger log = LoggerFactory.getLogger(NameNormalizer.class);

    public String normalize(String rawName) {
        log.info("+++start normalize+++ ");
        Objects.requireNonNull(rawName, "rawName");
        String decomposed = Normalizer.normalize(rawName, Normalizer.Form.NFD);
        String withoutAccents = decomposed.replaceAll("\\p{M}+", "");
        String cleaned = withoutAccents
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("rawName cannot normalize to blank");
        }
        log.info("+++end normalize+++ ");
        return cleaned;
    }
}
