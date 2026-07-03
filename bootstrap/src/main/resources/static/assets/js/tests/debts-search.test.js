const test = require("node:test");
const assert = require("node:assert/strict");

const {
    filterAllocatableDebts,
    getSelectedAllocatableStatuses
} = require("../debts-search.js");

test("should_filter_allocatable_debts_when_status_is_open_or_partially_paid", () => {
    const debts = [
        { id: "1", status: "OPEN" },
        { id: "2", status: "PARTIALLY_PAID" },
        { id: "3", status: "PAID" }
    ];

    const result = filterAllocatableDebts(debts);
    assert.deepEqual(result.map((debt) => debt.id), ["1", "2"]);
});

test("should_keep_only_allowed_status_filters_when_selection_contains_non_allocatable_values", () => {
    const selected = getSelectedAllocatableStatuses(["OPEN", "PAID", "PARTIALLY_PAID"]);
    assert.deepEqual(selected, ["OPEN", "PARTIALLY_PAID"]);
});

test("should_return_empty_results_when_debt_input_or_filters_are_invalid", () => {
    assert.deepEqual(filterAllocatableDebts(null), []);
    assert.deepEqual(filterAllocatableDebts([{ id: "x", status: "closed" }]), []);
    assert.deepEqual(getSelectedAllocatableStatuses([]), []);
    assert.deepEqual(getSelectedAllocatableStatuses(["", "PAID", "  "]), []);
    assert.deepEqual(getSelectedAllocatableStatuses(null), []);
});
