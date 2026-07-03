package com.pipelinepro.domain.value;

import java.util.Objects;

public final class VatNumber {
    private final String countryCode;
    private final String digits;

    private VatNumber(String countryCode, String digits) {
        this.countryCode = countryCode;
        this.digits = digits;
    }

    public static VatNumber belgian(String rawValue) {
        Objects.requireNonNull(rawValue, "vatNumber");
        String compact = rawValue.trim().toUpperCase();
        if (compact.startsWith("BE")) {
            compact = compact.substring(2);
        }
        EnterpriseNumber enterpriseNumber = EnterpriseNumber.of(compact);
        return new VatNumber("BE", enterpriseNumber.digits());
    }

    public String countryCode() {
        return countryCode;
    }

    public String digits() {
        return digits;
    }

    public String formatted() {
        return countryCode + digits;
    }
}
