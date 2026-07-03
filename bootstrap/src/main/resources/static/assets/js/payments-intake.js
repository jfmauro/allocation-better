function isValidPaymentReferenceAndAmount(reference, amount) {
    return Boolean(String(reference || "").trim()) && Number.isFinite(Number(amount)) && Number(amount) > 0;
}

function buildPaymentIntakePayload(rawValues) {
    return {
        bankTransactionReference: String(rawValues?.bankTransactionReference || "").trim(),
        amount: Number(rawValues?.amount),
        currency: String(rawValues?.currency || "EUR"),
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

        form?.addEventListener("submit", async (event) => {
            event.preventDefault();
            clearBanner(banner);

            const formData = new FormData(form);
            const payload = buildPaymentIntakePayload({
                bankTransactionReference: formData.get("bankTransactionReference"),
                amount: formData.get("amount"),
                currency: formData.get("currency"),
                structuredCommunication: formData.get("structuredCommunication"),
                freeCommunication: formData.get("freeCommunication"),
                payerName: formData.get("payerName"),
                payerIbanMasked: formData.get("payerIbanMasked")
            });

            if (!isValidPaymentReferenceAndAmount(payload.bankTransactionReference, payload.amount)) {
                setBanner(banner, "error", "Please provide a valid reference and amount.");
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
        buildPaymentIntakePayload
    };
}
