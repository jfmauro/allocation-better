const test = require("node:test");
const assert = require("node:assert/strict");

const {
    buildDebtorsQuery,
    normalizeDebtorListResponse
} = require("../debtors-list.js");

test("should_build_debtors_query_when_supported_filters_are_provided", () => {
    const query = buildDebtorsQuery({
        query: "acme",
        debtorType: "ENTERPRISE",
        activeOnly: true
    });

    assert.equal(query.includes("query=acme"), true);
    assert.equal(query.includes("debtorType=ENTERPRISE"), true);
    assert.equal(query.includes("active=true"), true);
});

test("should_return_empty_query_when_filters_are_invalid_or_empty", () => {
    const query = buildDebtorsQuery({
        query: "   ",
        debtorType: "DROP TABLE",
        activeOnly: false
    });

    assert.equal(query, "");
});

test("should_url_encode_search_query_when_filter_contains_special_characters", () => {
    const query = buildDebtorsQuery({
        query: "<script>alert(1)</script>",
        debtorType: "ENTERPRISE",
        activeOnly: false
    });

    assert.equal(query.includes("query=%3Cscript%3Ealert%281%29%3C%2Fscript%3E"), true);
    assert.equal(query.includes("debtorType=ENTERPRISE"), true);
});

test("should_normalize_debtor_list_when_response_shape_varies", () => {
    const normalizedA = normalizeDebtorListResponse({
        debtors: [{
            id: "d-1",
            type: "ENTERPRISE",
            displayName: "Acme",
            nationalNumber: "0820501224",
            active: true
        }]
    });
    const normalizedB = normalizeDebtorListResponse([
        {
            id: "d-2",
            debtorType: "NATURAL_PERSON",
            displayName: "Alice",
            nationalNumber: "85073003328",
            active: false
        }
    ]);

    assert.equal(normalizedA.length, 1);
    assert.equal(normalizedA[0].id, "d-1");
    assert.equal(normalizedB.length, 1);
    assert.equal(normalizedB[0].type, "NATURAL_PERSON");
});

test("should_return_empty_list_when_debtor_response_does_not_contain_items", () => {
    const normalized = normalizeDebtorListResponse({ unexpected: [] });
    assert.deepEqual(normalized, []);
});
