package com.pipelinepro.domain.value;

import java.util.Objects;

public final class StructuredCommunication {
    private final String digits;

    private StructuredCommunication(String digits) {
        this.digits = digits;
    }

    public static StructuredCommunication of(String rawValue) {
        String digits = normalizeDigits(rawValue, "structuredCommunication", 12);
        long base = Long.parseLong(digits.substring(0, 10));
        int checksum = Integer.parseInt(digits.substring(10, 12));
        int expected = (int) (base % 97);
        if (expected == 0) {
            expected = 97;
        }
        if (checksum != expected) {
            throw new IllegalArgumentException("structuredCommunication checksum is invalid");
        }
        return new StructuredCommunication(digits);
    }

    public String digits() {
        return digits;
    }

    public String formatted() {
        return "+++" + digits.substring(0, 3) + "/" + digits.substring(3, 7) + "/" + digits.substring(7) + "+++";
    }

    private static String normalizeDigits(String rawValue, String fieldName, int expectedLength) {
        Objects.requireNonNull(rawValue, fieldName);
        String digits = rawValue.replaceAll("\\D", "");
        if (digits.length() != expectedLength) {
            throw new IllegalArgumentException(fieldName + " must contain exactly " + expectedLength + " digits");
        }
        return digits;
    }
}
