const test = require("node:test");
const assert = require("node:assert/strict");

const { escapeHtml } = require("../common.js");

test("render safety escapes HTML special characters", () => {
    const unsafe = `<img src=x onerror='alert(1)'><script>alert(2)</script>`;
    const escaped = escapeHtml(unsafe);

    assert.equal(escaped.includes("<script>"), false);
    assert.equal(/<[^>]+onerror=/i.test(escaped), false);
    assert.equal(escaped.includes("<img"), false);
    assert.equal(escaped.includes("&lt;script&gt;"), true);
    assert.equal(escaped.includes("&lt;img"), true);
});
