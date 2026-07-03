const test = require("node:test");
const assert = require("node:assert/strict");

const {
    buildDebtPayload,
    isUuidValue,
    isValidDebtPayload,
    normalizeDebtorOptions,
    createDefaultRequestHeaders
} = require("../debts-create.js");

test("should_build_sanitized_debt_payload_when_raw_values_include_whitespace", () => {
    const payload = buildDebtPayload({
        debtorId: " 123e4567-e89b-12d3-a456-426614174000 ",
        reference: "  REF-001 ",
        originalAmount: "10.50",
        currency: "eur",
        openingStatus: "OPEN",
        dueDate: ""
    });

    assert.deepEqual(payload, {
        debtorId: "123e4567-e89b-12d3-a456-426614174000",
        reference: "REF-001",
        originalAmount: 10.5,
        currency: "EUR",
        openingStatus: "OPEN",
        dueDate: null
    });
});

test("should_validate_debt_payload_when_opening_status_and_debtor_id_are_valid", () => {
    const validPayload = {
        debtorId: "123e4567-e89b-12d3-a456-426614174000",
        reference: "REF-002",
        originalAmount: 100,
        currency: "EUR",
        openingStatus: "PARTIALLY_PAID"
    };

    assert.equal(isUuidValue(validPayload.debtorId), true);
    assert.equal(isValidDebtPayload(validPayload), true);
    assert.equal(isValidDebtPayload({ ...validPayload, openingStatus: "PAID" }), false);
    assert.equal(isValidDebtPayload({ ...validPayload, debtorId: "not-a-uuid" }), false);
});

test("should_reject_debt_payload_when_debtor_id_is_empty_or_malformed", () => {
    assert.equal(isUuidValue(""), false);
    assert.equal(isUuidValue("../../etc/passwd"), false);
    assert.equal(isValidDebtPayload({
        debtorId: "",
        reference: "REF-003",
        originalAmount: 50,
        currency: "EUR",
        openingStatus: "OPEN"
    }), false);
});

test("should_normalize_debtor_selector_options_when_response_contains_incomplete_rows", () => {
    const options = normalizeDebtorOptions({
        debtors: [
            { id: "d-1", displayName: "Acme", active: true },
            { id: null, displayName: "Missing" }
        ]
    });

    assert.equal(options.length, 1);
    assert.equal(options[0].id, "d-1");
    assert.equal(options[0].displayName, "Acme");
});

test("should_create_safe_default_request_headers_when_generating_debt_headers", () => {
    const headers = createDefaultRequestHeaders();
    assert.equal(String(headers.idempotencyKey).startsWith("debt-"), true);
    assert.equal(String(headers.correlationId).startsWith("corr-"), true);
    assert.equal(/[\r\n]/.test(String(headers.idempotencyKey)), false);
    assert.equal(/[\r\n]/.test(String(headers.correlationId)), false);
    assert.equal(String(headers.idempotencyKey).trim(), String(headers.idempotencyKey));
    assert.equal(String(headers.correlationId).trim(), String(headers.correlationId));
});
