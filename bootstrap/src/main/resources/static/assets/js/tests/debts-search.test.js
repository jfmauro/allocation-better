const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");

const {
    filterAllocatableDebts,
    getSelectedAllocatableStatuses,
    buildDebtSearchStatusQuery,
    getDebtTableHeaders,
    buildDebtDetailFields
} = require("../debts-search.js");

test("should_filter_allocatable_debts_when_status_is_open_partially_paid_or_paid", () => {
    const debts = [
        { id: "1", status: "OPEN" },
        { id: "2", status: "PARTIALLY_PAID" },
        { id: "3", status: "PAID" }
    ];

    const result = filterAllocatableDebts(debts);
    assert.deepEqual(result.map((debt) => debt.id), ["1", "2", "3"]);
});

test("should_keep_only_allowed_status_filters_when_selection_contains_non_supported_values", () => {
    const selected = getSelectedAllocatableStatuses(["OPEN", "PAID", "PARTIALLY_PAID", "CLOSED"]);
    assert.deepEqual(selected, ["OPEN", "PAID", "PARTIALLY_PAID"]);
});

test("should_return_empty_results_when_debt_input_or_filters_are_invalid", () => {
    assert.deepEqual(filterAllocatableDebts(null), []);
    assert.deepEqual(filterAllocatableDebts([{ id: "x", status: "closed" }]), []);
    assert.deepEqual(getSelectedAllocatableStatuses([]), []);
    assert.deepEqual(getSelectedAllocatableStatuses(["", "UNKNOWN", "  "]), []);
    assert.deepEqual(getSelectedAllocatableStatuses(null), []);
});

test("should_default_debt_search_to_open_and_partially_paid_only", () => {
    const debtSearchHtmlPath = path.join(__dirname, "../../../debts/search.html");
    const html = fs.readFileSync(debtSearchHtmlPath, "utf8");

    const checkedStatuses = Array.from(
        html.matchAll(/<input[^>]*name="status"[^>]*value="([^"]+)"[^>]*checked[^>]*>/g)
    ).map((match) => match[1]);

    assert.deepEqual(checkedStatuses, ["OPEN", "PARTIALLY_PAID"]);
    assert.equal(html.includes('value="PAID" checked'), false);
    assert.equal(buildDebtSearchStatusQuery(checkedStatuses), "status=OPEN&status=PARTIALLY_PAID");
});

test("should_show_original_amount_in_debtor_debts_table_and_keep_remaining_in_debt_detail", () => {
    global.displayText = (value) => (value == null ? "-" : String(value));
    global.formatMoney = (amount, currency) => `${currency} ${Number(amount).toFixed(2)}`;

    assert.deepEqual(getDebtTableHeaders(), ["Debt ID", "Status", "Original amount", "Due date", "Updated"]);

    const debt = {
        id: "debt-1",
        debtorId: "debtor-1",
        status: "PARTIALLY_PAID",
        originalAmount: 150,
        remainingAmount: 75,
        currency: "EUR",
        structuredCommunication: null,
        freeCommunication: "INV-001"
    };

    const detailFields = buildDebtDetailFields(debt);
    assert.deepEqual(detailFields.find(([label]) => label === "Remaining"), ["Remaining", "EUR 75.00"]);
    assert.equal(detailFields.some(([label]) => label === "Original amount"), false);

    delete global.displayText;
    delete global.formatMoney;
});
