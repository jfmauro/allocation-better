const test = require("node:test");
const assert = require("node:assert/strict");

const {
    normalizeCandidate,
    enrichCandidatesFromData,
    isStatusAllocatable,
    selectInitialCandidate
} = require("../proposals-validate.js");

test("proposals-validate normalizes candidate using existing candidate data", () => {
    const normalized = normalizeCandidate({
        debtId: "d-1",
        debtStatus: "OPEN",
        confidence: "HIGH"
    });

    assert.equal(normalized.debt.id, "d-1");
    assert.equal(normalized.debt.status, "OPEN");
    assert.equal(normalized.isAllocatable, true);
});

test("proposals-validate marks unsupported statuses as non allocatable", () => {
    assert.equal(isStatusAllocatable("REJECTED"), false);
});

test("proposals-validate enriches all candidates without remote fetch dependency", () => {
    const enriched = enrichCandidatesFromData([
        { debtId: "a", debt: { status: "OPEN" } },
        { debtId: "b", debtStatus: "PARTIALLY_PAID" },
        { debtId: "c", debtStatus: "PAID" }
    ]);

    assert.equal(enriched.length, 3);
    assert.deepEqual(enriched.filter((candidate) => candidate.isAllocatable).map((candidate) => candidate.debtId), ["a", "b"]);
});

test("proposals-validate selects the first visible candidate even when it is not allocatable", () => {
    const selected = selectInitialCandidate([
        { debtId: "x", debt: { id: "x", status: "CLOSED" }, isAllocatable: false },
        { debtId: "y", debt: { id: "y", status: "OPEN" }, isAllocatable: true }
    ], "");

    assert.equal(selected.debtId, "x");
});

test("proposals-validate prefers the selected debt id when present", () => {
    const selected = selectInitialCandidate([
        { debtId: "x", debt: { id: "x", status: "CLOSED" }, isAllocatable: false },
        { debtId: "y", debt: { id: "y", status: "OPEN" }, isAllocatable: true }
    ], "y");

    assert.equal(selected.debtId, "y");
});
