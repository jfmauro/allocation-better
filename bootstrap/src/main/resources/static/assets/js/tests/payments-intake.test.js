const test = require("node:test");
const assert = require("node:assert/strict");

const {
    isValidPaymentReferenceAndAmount,
    buildPaymentIntakePayload,
    getPaymentIntakeValidationMessage,
    toDateTimeLocalValue,
    prefillValueDateInput
} = require("../payments-intake.js");

test("payments-intake validates reference and amount", () => {
    assert.equal(isValidPaymentReferenceAndAmount("REF-1", 100), true);
    assert.equal(isValidPaymentReferenceAndAmount("", 100), false);
    assert.equal(isValidPaymentReferenceAndAmount("REF-1", 0), false);
});

test("payments-intake builds sanitized payload including ISO valueDate", () => {
    const expectedIsoValueDate = new Date(2026, 7, 25, 14, 30, 0, 0).toISOString();
    const payload = buildPaymentIntakePayload({
        bankTransactionReference: "  REF-42 ",
        amount: "10.50",
        currency: "EUR",
        valueDate: "2026-08-25T14:30",
        structuredCommunication: "   ",
        freeCommunication: "  Invoice A ",
        payerName: "  Alice  ",
        payerIbanMasked: "  BE**1234 "
    });

    assert.deepEqual(payload, {
        bankTransactionReference: "REF-42",
        amount: 10.5,
        currency: "EUR",
        valueDate: expectedIsoValueDate,
        structuredCommunication: null,
        freeCommunication: "Invoice A",
        payerName: "Alice",
        payerIbanMasked: "BE**1234"
    });
});

test("payments-intake rejects missing or invalid valueDate in client validation", () => {
    const validPayload = {
        bankTransactionReference: "REF-1",
        amount: 25,
        valueDate: new Date(2026, 7, 25, 14, 30, 0, 0).toISOString()
    };

    assert.equal(getPaymentIntakeValidationMessage(validPayload), "");
    assert.equal(
        getPaymentIntakeValidationMessage({ ...validPayload, valueDate: null }),
        "Please provide a valid bank value date and time."
    );
    assert.equal(
        getPaymentIntakeValidationMessage({ ...validPayload, valueDate: "invalid-date" }),
        "Please provide a valid bank value date and time."
    );
});

test("payments-intake prefills valueDate without overriding an existing value", () => {
    const now = new Date(2026, 7, 25, 9, 5, 45, 0);
    const emptyInput = { value: "" };
    const prefilledInput = { value: "2026-08-25T10:30" };

    prefillValueDateInput(emptyInput, now);
    prefillValueDateInput(prefilledInput, now);

    assert.equal(emptyInput.value, toDateTimeLocalValue(now));
    assert.equal(prefilledInput.value, "2026-08-25T10:30");
});
