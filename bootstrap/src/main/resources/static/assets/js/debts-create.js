const DEBT_OPENING_STATUSES = new Set(["OPEN", "PARTIALLY_PAID"]);

function buildDebtPayload(rawValues) {
    return {
        debtorId: String(rawValues?.debtorId || "").trim(),
        reference: String(rawValues?.reference || "").trim(),
        originalAmount: Number(rawValues?.originalAmount),
        currency: String(rawValues?.currency || "EUR").trim().toUpperCase(),
        openingStatus: String(rawValues?.openingStatus || "").trim(),
        dueDate: String(rawValues?.dueDate || "").trim() || null
    };
}

function isUuidValue(value) {
    return /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(String(value || ""));
}

function isValidDebtPayload(payload) {
    if (!isUuidValue(payload?.debtorId)) {
        return false;
    }
    if (!String(payload?.reference || "").trim()) {
        return false;
    }
    if (!Number.isFinite(Number(payload?.originalAmount)) || Number(payload?.originalAmount) <= 0) {
        return false;
    }
    if (!String(payload?.currency || "").trim()) {
        return false;
    }
    return DEBT_OPENING_STATUSES.has(String(payload?.openingStatus || "").trim());
}

function normalizeDebtorOptions(response) {
    const source = Array.isArray(response)
        ? response
        : Array.isArray(response?.debtors)
            ? response.debtors
            : Array.isArray(response?.items)
                ? response.items
                : Array.isArray(response?.results)
                    ? response.results
                    : Array.isArray(response?.content)
                        ? response.content
                        : Array.isArray(response?.data)
                            ? response.data
                            : [];

    return source
        .map((item) => ({
            id: item?.id || null,
            displayName: item?.displayName || null,
            active: item?.active
        }))
        .filter((item) => item.id);
}

function createDefaultRequestHeaders() {
    const randomPart = typeof crypto !== "undefined" && typeof crypto.randomUUID === "function"
        ? crypto.randomUUID()
        : `${Date.now()}-${Math.random().toString(16).slice(2)}`;
    return {
        idempotencyKey: `debt-${randomPart}`,
        correlationId: `corr-${randomPart}`
    };
}

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        const form = byId("debt-create-form");
        const banner = byId("debt-create-banner");
        const result = byId("debt-create-result");
        const submitButton = byId("debt-create-submit");
        const debtorQueryInput = byId("debtor-query");
        const loadDebtorsButton = byId("load-debtor-options");
        const debtorSelect = byId("debtor-select");
        const debtorIdInput = byId("debtor-id");
        const idempotencyInput = byId("idempotency-key");
        const correlationInput = byId("correlation-id");

        if (!form) {
            return;
        }

        function renderDebtorOptions(debtors) {
            debtorSelect.replaceChildren();
            const defaultOption = document.createElement("option");
            defaultOption.value = "";
            defaultOption.textContent = debtors.length ? "Select a debtor" : "No debtors loaded";
            debtorSelect.appendChild(defaultOption);

            debtors.forEach((debtor) => {
                const option = document.createElement("option");
                option.value = String(debtor.id);
                option.textContent = `${displayText(debtor.displayName, "Unnamed debtor")} (${displayText(debtor.id)})`;
                debtorSelect.appendChild(option);
            });
        }

        const defaultHeaders = createDefaultRequestHeaders();
        if (idempotencyInput && !idempotencyInput.value) {
            idempotencyInput.value = defaultHeaders.idempotencyKey;
        }
        if (correlationInput && !correlationInput.value) {
            correlationInput.value = defaultHeaders.correlationId;
        }

        const initialDebtorId = getQueryParam("debtorId");
        if (initialDebtorId && debtorIdInput) {
            debtorIdInput.value = initialDebtorId;
        }

        loadDebtorsButton?.addEventListener("click", async () => {
            loadDebtorsButton.disabled = true;
            setBanner(banner, "info", "Loading debtor options...");

            try {
                const query = String(debtorQueryInput?.value || "").trim();
                const endpoint = query ? `/debtors?query=${encodeURIComponent(query)}` : "/debtors";
                const response = await fetchJson(getApiUrl(endpoint));
                const debtors = normalizeDebtorOptions(response);
                renderDebtorOptions(debtors);
                setBanner(banner, debtors.length ? "success" : "warning", debtors.length
                    ? "Debtor options loaded."
                    : "No debtor options found.");
            } catch (error) {
                renderDebtorOptions([]);
                setBanner(banner, "error", toSafeErrorMessage(error?.message));
            } finally {
                loadDebtorsButton.disabled = false;
            }
        });

        debtorSelect?.addEventListener("change", () => {
            if (debtorIdInput) {
                debtorIdInput.value = debtorSelect.value;
            }
        });

        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            clearBanner(banner);

            const formData = new FormData(form);
            const payload = buildDebtPayload({
                debtorId: formData.get("debtorId"),
                reference: formData.get("reference"),
                originalAmount: formData.get("originalAmount"),
                currency: formData.get("currency"),
                openingStatus: formData.get("openingStatus"),
                dueDate: formData.get("dueDate")
            });

            const idempotencyKey = String(formData.get("idempotencyKey") || "").trim();
            const correlationId = String(formData.get("correlationId") || "").trim();

            if (!idempotencyKey || !correlationId) {
                setBanner(banner, "error", "Idempotency-Key and X-Correlation-Id are required.");
                return;
            }

            if (!isValidDebtPayload(payload)) {
                setBanner(banner, "error", "Provide a valid debtor ID, positive amount, and opening status OPEN or PARTIALLY_PAID.");
                return;
            }

            submitButton.disabled = true;
            setBanner(banner, "info", "Submitting debt intake...");

            try {
                const response = await fetchJson(getApiUrl("/debts"), {
                    method: "POST",
                    headers: {
                        "Idempotency-Key": idempotencyKey,
                        "X-Correlation-Id": correlationId
                    },
                    body: JSON.stringify(payload)
                });

                setBanner(banner, "success", "Debt created successfully.");
                result.textContent = JSON.stringify(response, null, 2);
            } catch (error) {
                setBanner(banner, "error", toSafeErrorMessage(error?.message));
            } finally {
                submitButton.disabled = false;
            }
        });

        form.addEventListener("reset", () => {
            clearBanner(banner);
            result.textContent = "No debt submitted yet.";
        });
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        DEBT_OPENING_STATUSES,
        buildDebtPayload,
        isUuidValue,
        isValidDebtPayload,
        normalizeDebtorOptions,
        createDefaultRequestHeaders
    };
}
