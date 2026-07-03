package com.pipelinepro.domain.value;

import java.util.Objects;

public final class NationalRegisterNumber {
    private final String digits;

    private NationalRegisterNumber(String digits) {
        this.digits = digits;
    }

    public static NationalRegisterNumber of(String rawValue) {
        String digits = normalizeDigits(rawValue, "nationalRegisterNumber", 11);
        long firstNineDigits = Long.parseLong(digits.substring(0, 9));
        int checksum = Integer.parseInt(digits.substring(9, 11));

        int expectedLegacy = expectedChecksum(firstNineDigits);
        int expectedAfter2000 = expectedChecksum(2_000_000_000L + firstNineDigits);
        if (checksum != expectedLegacy && checksum != expectedAfter2000) {
            throw new IllegalArgumentException("nationalRegisterNumber checksum is invalid");
        }
        return new NationalRegisterNumber(digits);
    }

    public String digits() {
        return digits;
    }

    private static int expectedChecksum(long base) {
        int result = (int) (97 - (base % 97));
        return result == 0 ? 97 : result;
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
