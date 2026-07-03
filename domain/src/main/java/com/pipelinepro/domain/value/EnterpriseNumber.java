package com.pipelinepro.domain.value;

import java.util.Objects;

public final class EnterpriseNumber {
    private final String digits;

    private EnterpriseNumber(String digits) {
        this.digits = digits;
    }

    public static EnterpriseNumber of(String rawValue) {
        String digits = normalizeDigits(rawValue, "enterpriseNumber", 10);
        char leadingDigit = digits.charAt(0);
        if (leadingDigit != '0' && leadingDigit != '1') {
            throw new IllegalArgumentException("enterpriseNumber must start with 0 or 1");
        }
        long firstEightDigits = Long.parseLong(digits.substring(0, 8));
        int checksum = Integer.parseInt(digits.substring(8, 10));
        int expected = (int) (97 - (firstEightDigits % 97));
        if (expected == 0) {
            expected = 97;
        }
        if (checksum != expected) {
            throw new IllegalArgumentException("enterpriseNumber checksum is invalid");
        }
        return new EnterpriseNumber(digits);
    }

    public String digits() {
        return digits;
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
