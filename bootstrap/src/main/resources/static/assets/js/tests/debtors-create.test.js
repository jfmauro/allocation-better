const test = require("node:test");
const assert = require("node:assert/strict");

const {
    buildDebtorPayload,
    isValidDebtorPayload,
    createDefaultRequestHeaders
} = require("../debtors-create.js");

test("should_build_sanitized_debtor_payload_when_raw_values_include_whitespace", () => {
    const payload = buildDebtorPayload({
        debtorType: " ENTERPRISE ",
        displayName: "  Acme Corp  ",
        enterpriseNumber: "  BE0123456789 ",
        nationalNumber: "  "
    });

    assert.deepEqual(payload, {
        debtorType: "ENTERPRISE",
        displayName: "Acme Corp",
        nationalNumber: null,
        enterpriseNumber: "BE0123456789"
    });
});

test("should_validate_debtor_payload_when_rules_depend_on_debtor_type", () => {
    assert.equal(isValidDebtorPayload({ debtorType: "ENTERPRISE", displayName: "Acme", enterpriseNumber: "BE1" }), true);
    assert.equal(isValidDebtorPayload({ debtorType: "ENTERPRISE", displayName: "Acme", enterpriseNumber: "" }), false);
    assert.equal(isValidDebtorPayload({ debtorType: "NATURAL_PERSON", displayName: "Alice", nationalNumber: "85073003328" }), true);
    assert.equal(isValidDebtorPayload({ debtorType: "NATURAL_PERSON", displayName: "Alice" }), false);
});

test("should_reject_debtor_payload_when_required_values_are_missing_or_invalid", () => {
    assert.equal(isValidDebtorPayload({ debtorType: "", displayName: "Acme" }), false);
    assert.equal(isValidDebtorPayload({ debtorType: "ENTERPRISE", displayName: "", enterpriseNumber: "BE1" }), false);
    assert.equal(isValidDebtorPayload({ debtorType: "NATURAL_PERSON", displayName: "Alice", nationalNumber: "  " }), false);
});

test("should_create_safe_default_request_headers_when_generating_debtor_headers", () => {
    const headers = createDefaultRequestHeaders();
    assert.equal(String(headers.idempotencyKey).startsWith("debtor-"), true);
    assert.equal(String(headers.correlationId).startsWith("corr-"), true);
    assert.equal(/[\r\n]/.test(String(headers.idempotencyKey)), false);
    assert.equal(/[\r\n]/.test(String(headers.correlationId)), false);
    assert.equal(String(headers.idempotencyKey).trim(), String(headers.idempotencyKey));
    assert.equal(String(headers.correlationId).trim(), String(headers.correlationId));
});
