const test = require("node:test");
const assert = require("node:assert/strict");

const common = require("../../main/resources/static/assets/js/common.js");
const paymentsIntake = require("../../main/resources/static/assets/js/payments-intake.js");
const proposalsValidate = require("../../main/resources/static/assets/js/proposals-validate.js");
const debtsSearch = require("../../main/resources/static/assets/js/debts-search.js");

test("escapeHtml escapes dangerous markup", () => {
    const unsafe = '<img src=x onerror="alert(1)">';
    assert.equal(common.escapeHtml(unsafe), "&lt;img src=x onerror=&quot;alert(1)&quot;&gt;");
});

test("encodePathSegment neutralizes path traversal characters", () => {
    assert.equal(common.encodePathSegment("abc/../x y"), "abc%2F..%2Fx%20y");
});

test("payment intake payload is normalized", () => {
    const expectedValueDate = new Date(2026, 7, 25, 12, 34, 0, 0).toISOString();

    const payload = paymentsIntake.buildPaymentIntakePayload({
        bankTransactionReference: "  TX-001  ",
        amount: "120.50",
        currency: "EUR",
        valueDate: "2026-08-25T12:34",
        structuredCommunication: "  +++123/1234/12345+++  ",
        freeCommunication: "  note  ",
        payerName: "  Jane Doe  ",
        payerIbanMasked: "  BE12**********34  "
    });

    assert.deepEqual(payload, {
        bankTransactionReference: "TX-001",
        amount: 120.5,
        currency: "EUR",
        valueDate: expectedValueDate,
        structuredCommunication: "+++123/1234/12345+++",
        freeCommunication: "note",
        payerName: "Jane Doe",
        payerIbanMasked: "BE12**********34"
    });
});

test("payment intake validation rejects invalid reference/amount", () => {
    assert.equal(paymentsIntake.isValidPaymentReferenceAndAmount("", 10), false);
    assert.equal(paymentsIntake.isValidPaymentReferenceAndAmount("REF", 0), false);
    assert.equal(paymentsIntake.isValidPaymentReferenceAndAmount("REF", 10), true);
});

test("proposal candidates are normalized and allocatable status is enforced", () => {
    const candidates = proposalsValidate.enrichCandidatesFromData([
        { debtId: "d1", debtStatus: "OPEN", suggestedAmount: 50 },
        { debtId: "d2", debtStatus: "CLOSED", suggestedAmount: 50 },
        { debtId: "d3", debt: { status: "PARTIALLY_PAID" }, suggestedAmount: 25 }
    ]);

    assert.equal(candidates[0].isAllocatable, true);
    assert.equal(candidates[1].isAllocatable, false);
    assert.equal(candidates[2].isAllocatable, true);
    assert.equal(proposalsValidate.isStatusAllocatable(""), false);
});

test("proposal validate screen selects visible candidates regardless of allocatable status", () => {
    const selected = proposalsValidate.selectInitialCandidate([
        { debtId: "d1", debt: { id: "d1", status: "CLOSED" }, isAllocatable: false },
        { debtId: "d2", debt: { id: "d2", status: "OPEN" }, isAllocatable: true }
    ]);

    assert.equal(selected.debtId, "d1");
});

test("validation amount must be positive and within payment remaining amount", () => {
    assert.equal(proposalsValidate.isValidAllocationAmount(0, 100), false);
    assert.equal(proposalsValidate.isValidAllocationAmount(50, 40), false);
    assert.equal(proposalsValidate.isValidAllocationAmount(40, 40), true);
    assert.equal(proposalsValidate.isValidAllocationAmount(20, undefined), false);
});

test("debt search only keeps allowed statuses", () => {
    const filtered = debtsSearch.filterAllocatableDebts([
        { id: "a", status: "OPEN" },
        { id: "b", status: "PARTIALLY_PAID" },
        { id: "c", status: "PAID" },
        { id: "d", status: "CLOSED" }
    ]);

    assert.deepEqual(filtered.map((item) => item.id), ["a", "b", "c"]);

    const selected = debtsSearch.getSelectedAllocatableStatuses(["OPEN", "CLOSED", "PAID", "PARTIALLY_PAID"]);
    assert.deepEqual(selected, ["OPEN", "PAID", "PARTIALLY_PAID"]);
});
