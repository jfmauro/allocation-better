function isValidPaymentReferenceAndAmount(reference, amount) {
    return Boolean(String(reference || "").trim()) && Number.isFinite(Number(amount)) && Number(amount) > 0;
}

const ISO_INSTANT_PATTERN = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d{3})?(?:Z|[+-]\d{2}:\d{2})$/;

function isIsoInstant(valueDate) {
    const normalizedValueDate = String(valueDate || "").trim();
    if (!ISO_INSTANT_PATTERN.test(normalizedValueDate)) {
        return false;
    }
    return !Number.isNaN(new Date(normalizedValueDate).getTime());
}

function toIsoInstantFromDateTimeLocal(valueDate) {
    const normalizedValueDate = String(valueDate || "").trim();
    if (!normalizedValueDate) {
        return null;
    }

    const match = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2})(?:\.(\d{1,3}))?)?$/.exec(normalizedValueDate);
    if (!match) {
        return null;
    }

    const year = Number(match[1]);
    const monthIndex = Number(match[2]) - 1;
    const day = Number(match[3]);
    const hour = Number(match[4]);
    const minute = Number(match[5]);
    const second = Number(match[6] || 0);
    const millisecond = Number(String(match[7] || "0").padEnd(3, "0"));

    if (
        monthIndex < 0 || monthIndex > 11 ||
        day < 1 || day > 31 ||
        hour < 0 || hour > 23 ||
        minute < 0 || minute > 59 ||
        second < 0 || second > 59 ||
        millisecond < 0 || millisecond > 999
    ) {
        return null;
    }

    const localDate = new Date(year, monthIndex, day, hour, minute, second, millisecond);
    if (Number.isNaN(localDate.getTime())) {
        return null;
    }

    if (
        localDate.getFullYear() !== year ||
        localDate.getMonth() !== monthIndex ||
        localDate.getDate() !== day ||
        localDate.getHours() !== hour ||
        localDate.getMinutes() !== minute ||
        localDate.getSeconds() !== second ||
        localDate.getMilliseconds() !== millisecond
    ) {
        return null;
    }

    return localDate.toISOString();
}

function toDateTimeLocalValue(date) {
    if (!(date instanceof Date) || Number.isNaN(date.getTime())) {
        return "";
    }

    const year = String(date.getFullYear());
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hour = String(date.getHours()).padStart(2, "0");
    const minute = String(date.getMinutes()).padStart(2, "0");

    return `${year}-${month}-${day}T${hour}:${minute}`;
}

function prefillValueDateInput(valueDateInput, now = new Date()) {
    if (!valueDateInput || String(valueDateInput.value || "").trim()) {
        return;
    }

    const defaultValueDate = toDateTimeLocalValue(now);
    if (defaultValueDate) {
        valueDateInput.value = defaultValueDate;
    }
}

function getPaymentIntakeValidationMessage(payload) {
    if (!isValidPaymentReferenceAndAmount(payload?.bankTransactionReference, payload?.amount)) {
        return "Please provide a valid reference and amount.";
    }
    if (!isIsoInstant(payload?.valueDate)) {
        return "Please provide a valid bank value date and time.";
    }
    return "";
}

function buildPaymentIntakePayload(rawValues) {
    return {
        bankTransactionReference: String(rawValues?.bankTransactionReference || "").trim(),
        amount: Number(rawValues?.amount),
        currency: String(rawValues?.currency || "EUR"),
        valueDate: toIsoInstantFromDateTimeLocal(rawValues?.valueDate),
        structuredCommunication: String(rawValues?.structuredCommunication || "").trim() || null,
        freeCommunication: String(rawValues?.freeCommunication || "").trim() || null,
        payerName: String(rawValues?.payerName || "").trim() || null,
        payerIbanMasked: String(rawValues?.payerIbanMasked || "").trim() || null
    };
}

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        const form = byId("payment-intake-form");
        const banner = byId("intake-banner");
        const result = byId("intake-result");
        const submitButton = byId("submit-intake");
        const valueDateInput = byId("value-date");

        prefillValueDateInput(valueDateInput);

        form?.addEventListener("submit", async (event) => {
            event.preventDefault();
            clearBanner(banner);

            const formData = new FormData(form);
            const payload = buildPaymentIntakePayload({
                bankTransactionReference: formData.get("bankTransactionReference"),
                amount: formData.get("amount"),
                currency: formData.get("currency"),
                valueDate: formData.get("valueDate"),
                structuredCommunication: formData.get("structuredCommunication"),
                freeCommunication: formData.get("freeCommunication"),
                payerName: formData.get("payerName"),
                payerIbanMasked: formData.get("payerIbanMasked")
            });

            const validationMessage = getPaymentIntakeValidationMessage(payload);
            if (validationMessage) {
                setBanner(banner, "error", validationMessage);
                return;
            }

            submitButton.disabled = true;
            setBanner(banner, "info", "Submitting payment intake...");

            try {
                const response = await fetchJson(getApiUrl("/payments"), {
                    method: "POST",
                    body: JSON.stringify(payload)
                });
                setBanner(banner, "success", "Payment intake completed.");
                result.textContent = JSON.stringify(response, null, 2);
                if (response?.id) {
                    updateQueryParam("paymentId", response.id);
                }
            } catch {
                setBanner(banner, "error", "Unable to submit payment right now.");
                result.textContent = "No successful response available.";
            } finally {
                submitButton.disabled = false;
            }
        });
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        isValidPaymentReferenceAndAmount,
        isIsoInstant,
        toIsoInstantFromDateTimeLocal,
        toDateTimeLocalValue,
        prefillValueDateInput,
        getPaymentIntakeValidationMessage,
        buildPaymentIntakePayload
    };
}
