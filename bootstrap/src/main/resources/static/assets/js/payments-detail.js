document.addEventListener("DOMContentLoaded", () => {
    const paymentIdInput = byId("payment-id");
    const loadButton = byId("load-payment");
    const banner = byId("payment-banner");
    const stateContainer = byId("payment-state");
    const proposalContainer = byId("proposal-container");
    const matchingResult = byId("matching-result");

    function renderMuted(container, message) {
        container.replaceChildren();
        const paragraph = document.createElement("p");
        paragraph.className = "muted";
        paragraph.textContent = message;
        container.appendChild(paragraph);
    }

    function createKpiCell(label, valueNodeOrText) {
        const cell = document.createElement("div");
        const labelElement = document.createElement("p");
        labelElement.className = "kpi-label";
        labelElement.textContent = label;
        const valueElement = document.createElement("p");
        valueElement.className = "kpi-value";
        if (valueNodeOrText instanceof Node) {
            valueElement.appendChild(valueNodeOrText);
        } else {
            valueElement.textContent = String(valueNodeOrText);
        }
        cell.append(labelElement, valueElement);
        return cell;
    }

    function createStatusChip(status) {
        const chip = document.createElement("span");
        chip.className = "status-chip";
        chip.dataset.state = displayText(status, "");
        chip.textContent = displayText(status);
        return chip;
    }

    const initialPaymentId = getQueryParam("paymentId");
    if (initialPaymentId) {
        paymentIdInput.value = initialPaymentId;
        loadPaymentDetails(initialPaymentId);
    }

    loadButton?.addEventListener("click", async () => {
        const paymentId = paymentIdInput.value.trim();
        if (!paymentId) {
            setBanner(banner, "error", "Payment ID is required.");
            return;
        }
        await loadPaymentDetails(paymentId);
    });

    document.querySelectorAll("[data-action]").forEach((button) => {
        button.addEventListener("click", async () => {
            const paymentId = paymentIdInput.value.trim();
            if (!paymentId) {
                setBanner(banner, "error", "Load a payment before running match actions.");
                return;
            }
            const safePaymentId = encodePathSegment(paymentId);

            const action = button.getAttribute("data-action");
            const endpoint = action === "match"
                ? `/payments/${safePaymentId}/match`
                : `/payments/${safePaymentId}/match/${action}`;

            try {
                matchingResult.textContent = "Running matching action...";
                const response = await fetchJson(getApiUrl(endpoint), { method: "POST" });
                matchingResult.textContent = JSON.stringify(response, null, 2);
                setBanner(banner, "success", `Match action '${action}' completed.`);
                await loadProposals(paymentId);
            } catch {
                setBanner(banner, "error", "Match action failed. Please retry.");
                matchingResult.textContent = "No successful action response.";
            }
        });
    });

    async function loadPaymentDetails(paymentId) {
        loadButton.disabled = true;
        setBanner(banner, "info", "Loading payment details...");
        try {
            const safePaymentId = encodePathSegment(paymentId);
            const payment = await fetchJson(getApiUrl(`/payments/${safePaymentId}`));
            renderPaymentState(payment);
            updateQueryParam("paymentId", paymentId);
            setBanner(banner, "success", "Payment loaded.");
            await loadProposals(paymentId);
        } catch {
            renderMuted(stateContainer, "No payment data available.");
            renderMuted(proposalContainer, "No proposal data available.");
            setBanner(banner, "error", "Unable to load payment right now.");
        } finally {
            loadButton.disabled = false;
        }
    }

    async function loadProposals(paymentId) {
        try {
            const safePaymentId = encodePathSegment(paymentId);
            const proposalResponse = await fetchJson(getApiUrl(`/payments/${safePaymentId}/proposals`));
            renderProposalTable(proposalResponse.proposals || []);
        } catch {
            renderMuted(proposalContainer, "No proposals found for this payment.");
        }
    }

    function renderPaymentState(payment) {
        stateContainer.replaceChildren();
        stateContainer.append(
            createKpiCell("Status", createStatusChip(payment.status)),
            createKpiCell("Amount", formatMoney(payment.amount, payment.currency)),
            createKpiCell("Remaining", formatMoney(payment.remainingAmount, payment.currency)),
            createKpiCell("Reference", displayText(payment.bankTransactionReference)),
            createKpiCell("Payer name", displayText(payment.payerName)),
            createKpiCell("Payer IBAN", maskValue(payment.payerIbanMasked))
        );
    }

    function renderProposalTable(proposals) {
        if (!proposals.length) {
            renderMuted(proposalContainer, "No proposals linked to this payment.");
            return;
        }
        proposalContainer.replaceChildren();
        const table = document.createElement("table");
        const head = document.createElement("thead");
        const headRow = document.createElement("tr");
        ["ID", "Status", "Method", "Reason", "Updated"].forEach((title) => {
            const th = document.createElement("th");
            th.textContent = title;
            headRow.appendChild(th);
        });
        head.appendChild(headRow);

        const body = document.createElement("tbody");
        proposals.forEach((proposal) => {
            const row = document.createElement("tr");

            const idCell = document.createElement("td");
            const link = document.createElement("a");
            link.href = `/app/proposals/validate.html?proposalId=${encodeURIComponent(String(proposal.id || ""))}`;
            link.textContent = displayText(proposal.id);
            idCell.appendChild(link);

            const statusCell = document.createElement("td");
            statusCell.appendChild(createStatusChip(proposal.status));

            const methodCell = document.createElement("td");
            methodCell.textContent = displayText(proposal.matchingMethod);

            const reasonCell = document.createElement("td");
            reasonCell.textContent = displayText(proposal.reason);

            const updatedCell = document.createElement("td");
            updatedCell.textContent = formatDateTime(proposal.updatedAt);

            row.append(idCell, statusCell, methodCell, reasonCell, updatedCell);
            body.appendChild(row);
        });

        table.append(head, body);
        proposalContainer.appendChild(table);
    }
});
