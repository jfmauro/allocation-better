const DEBTOR_TYPES = new Set(["NATURAL_PERSON", "ENTERPRISE"]);

function buildDebtorPayload(rawValues) {
    return {
        debtorType: String(rawValues?.debtorType || "").trim(),
        displayName: String(rawValues?.displayName || "").trim(),
        nationalNumber: String(rawValues?.nationalNumber || "").trim() || null,
        enterpriseNumber: String(rawValues?.enterpriseNumber || "").trim() || null
    };
}

function isValidDebtorPayload(payload) {
    const debtorType = String(payload?.debtorType || "").trim();
    if (!DEBTOR_TYPES.has(debtorType)) {
        return false;
    }

    if (!String(payload?.displayName || "").trim()) {
        return false;
    }

    if (debtorType === "ENTERPRISE") {
        return Boolean(String(payload?.enterpriseNumber || "").trim());
    }

    return Boolean(String(payload?.nationalNumber || "").trim());
}

function createDefaultRequestHeaders() {
    const randomPart = typeof crypto !== "undefined" && typeof crypto.randomUUID === "function"
        ? crypto.randomUUID()
        : `${Date.now()}-${Math.random().toString(16).slice(2)}`;
    return {
        idempotencyKey: `debtor-${randomPart}`,
        correlationId: `corr-${randomPart}`
    };
}

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        const form = byId("debtor-create-form");
        const banner = byId("debtor-create-banner");
        const result = byId("debtor-create-result");
        const submitButton = byId("debtor-create-submit");
        const debtorTypeInput = byId("debtor-type");
        const enterpriseField = byId("enterprise-number-field");
        const nationalField = byId("national-number-field");

        if (!form) {
            return;
        }

        const refreshFieldVisibility = () => {
            const debtorType = String(debtorTypeInput?.value || "").trim();
            const isEnterprise = debtorType === "ENTERPRISE";
            if (enterpriseField) {
                enterpriseField.hidden = !isEnterprise;
            }
            if (nationalField) {
                nationalField.hidden = isEnterprise;
            }
        };

        debtorTypeInput?.addEventListener("change", refreshFieldVisibility);
        refreshFieldVisibility();

        const defaultHeaders = createDefaultRequestHeaders();
        if (form) {
            form.dataset.idempotencyKey = defaultHeaders.idempotencyKey;
            form.dataset.correlationId = defaultHeaders.correlationId;
        }

        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            clearBanner(banner);

            const formData = new FormData(form);
            const payload = buildDebtorPayload({
                debtorType: formData.get("debtorType"),
                displayName: formData.get("displayName"),
                nationalNumber: formData.get("nationalNumber"),
                enterpriseNumber: formData.get("enterpriseNumber")
            });

            const idempotencyKey = String(form.dataset.idempotencyKey || "").trim();
            const correlationId = String(form.dataset.correlationId || "").trim();

            if (!idempotencyKey || !correlationId) {
                setBanner(banner, "error", "Idempotency-Key and X-Correlation-Id are required.");
                return;
            }

            if (!isValidDebtorPayload(payload)) {
                setBanner(banner, "error", "Please provide valid debtor details for the selected debtor type.");
                return;
            }

            submitButton.disabled = true;
            setBanner(banner, "info", "Submitting debtor intake...");

            try {
                const response = await fetchJson(getApiUrl("/debtors"), {
                    method: "POST",
                    headers: {
                "Idempotency-Key": idempotencyKey,
                "X-Correlation-Id": correlationId
                    },
                    body: JSON.stringify(payload)
                });

                setBanner(banner, "success", "Debtor created successfully.");
                result.textContent = JSON.stringify(response, null, 2);
                updateQueryParam("debtorId", response?.id || "");
            } catch (error) {
                setBanner(banner, "error", toSafeErrorMessage(error?.message));
            } finally {
                submitButton.disabled = false;
            }
        });

        form.addEventListener("reset", () => {
            clearBanner(banner);
            result.textContent = "No debtor submitted yet.";
        });
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        DEBTOR_TYPES,
        buildDebtorPayload,
        isValidDebtorPayload,
        createDefaultRequestHeaders
    };
}
