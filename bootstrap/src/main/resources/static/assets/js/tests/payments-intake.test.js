const test = require("node:test");
const assert = require("node:assert/strict");

const {
    isValidPaymentReferenceAndAmount,
    buildPaymentIntakePayload
} = require("../payments-intake.js");

test("payments-intake validates reference and amount", () => {
    assert.equal(isValidPaymentReferenceAndAmount("REF-1", 100), true);
    assert.equal(isValidPaymentReferenceAndAmount("", 100), false);
    assert.equal(isValidPaymentReferenceAndAmount("REF-1", 0), false);
});

test("payments-intake builds sanitized payload", () => {
    const payload = buildPaymentIntakePayload({
        bankTransactionReference: "  REF-42 ",
        amount: "10.50",
        currency: "EUR",
        structuredCommunication: "   ",
        freeCommunication: "  Invoice A ",
        payerName: "  Alice  ",
        payerIbanMasked: "  BE**1234 "
    });

    assert.deepEqual(payload, {
        bankTransactionReference: "REF-42",
        amount: 10.5,
        currency: "EUR",
        structuredCommunication: null,
        freeCommunication: "Invoice A",
        payerName: "Alice",
        payerIbanMasked: "BE**1234"
    });
});
