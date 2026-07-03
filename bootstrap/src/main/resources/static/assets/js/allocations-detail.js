document.addEventListener("DOMContentLoaded", () => {
    const allocationIdInput = byId("allocation-id");
    const loadButton = byId("load-allocation");
    const banner = byId("allocation-banner");
    const summaryContainer = byId("allocation-summary");
    const contextContainer = byId("allocation-context");
    const balanceDeltaContainer = byId("allocation-balance-delta");
    const auditSummaryContainer = byId("allocation-audit-summary");

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

    function createTable(headers, rows) {
        const table = document.createElement("table");
        const head = document.createElement("thead");
        const headRow = document.createElement("tr");
        headers.forEach((header) => {
            const th = document.createElement("th");
            th.textContent = header;
            headRow.appendChild(th);
        });
        head.appendChild(headRow);

        const body = document.createElement("tbody");
        rows.forEach((values) => {
            const row = document.createElement("tr");
            values.forEach((value) => {
                const cell = document.createElement("td");
                if (value instanceof Node) {
                    cell.appendChild(value);
                } else {
                    cell.textContent = String(value);
                }
                row.appendChild(cell);
            });
            body.appendChild(row);
        });

        table.append(head, body);
        return table;
    }

    const initialAllocationId = getQueryParam("allocationId");
    if (initialAllocationId) {
        allocationIdInput.value = initialAllocationId;
        loadAllocation(initialAllocationId);
    }

    loadButton?.addEventListener("click", async () => {
        const allocationId = allocationIdInput.value.trim();
        if (!allocationId) {
            setBanner(banner, "error", "Allocation ID is required.");
            return;
        }
        await loadAllocation(allocationId);
    });

    async function loadAllocation(allocationId) {
        loadButton.disabled = true;
        setBanner(banner, "info", "Loading allocation detail...");
        try {
            const safeAllocationId = encodePathSegment(allocationId);
            const allocation = await fetchJson(getApiUrl(`/allocations/${safeAllocationId}`));
            const [payment, debt, proposal] = await Promise.all([
                fetchJson(getApiUrl(`/payments/${encodePathSegment(allocation.paymentId)}`)).catch(() => null),
                fetchJson(getApiUrl(`/debts/${encodePathSegment(allocation.debtId)}`)).catch(() => null),
                allocation.proposalId
                    ? fetchJson(getApiUrl(`/allocation-proposals/${encodePathSegment(allocation.proposalId)}`)).catch(() => null)
                    : Promise.resolve(null)
            ]);

            renderSummary(allocation);
            renderContext(allocation, payment, debt);
            renderBalanceDelta(allocation, payment, debt);
            renderAuditSummary(allocation, payment, debt, proposal);
            updateQueryParam("allocationId", allocationId);
            setBanner(banner, "success", "Allocation loaded.");
        } catch {
            renderMuted(summaryContainer, "No allocation data available.");
            renderMuted(contextContainer, "No context data available.");
            renderMuted(balanceDeltaContainer, "No balance delta available.");
            renderMuted(auditSummaryContainer, "No audit trace summary available.");
            setBanner(banner, "error", "Unable to load allocation right now.");
        } finally {
            loadButton.disabled = false;
        }
    }

    function renderSummary(allocation) {
        summaryContainer.replaceChildren();
        summaryContainer.append(
            createKpiCell("Allocation ID", displayText(allocation.id)),
            createKpiCell("Status", createStatusChip(allocation.status)),
            createKpiCell("Allocated amount", formatMoney(allocation.amount)),
            createKpiCell("Created by", displayText(allocation.createdBy))
        );
    }

    function renderContext(allocation, payment, debt) {
        contextContainer.replaceChildren();
        const table = createTable(
            ["Field", "Allocation", "Payment", "Debt"],
            [
                ["ID", displayText(allocation.id), displayText(payment?.id), displayText(debt?.id)],
                ["Reference", displayText(allocation.commandId), displayText(payment?.bankTransactionReference), "—"],
                [
                    "Remaining amount",
                    "—",
                    formatMoney(payment?.remainingAmount, payment?.currency || "EUR"),
                    formatMoney(debt?.remainingAmount, debt?.currency || "EUR")
                ],
                ["Proposal ID", displayText(allocation.proposalId), "—", "—"],
                ["Created at", formatDateTime(allocation.createdAt), formatDateTime(payment?.updatedAt), formatDateTime(debt?.updatedAt)]
            ]
        );
        contextContainer.appendChild(table);
    }

    function renderBalanceDelta(allocation, payment, debt) {
        const allocatedAmount = Number(allocation.amount);
        const paymentRemaining = Number(payment?.remainingAmount);
        const debtRemaining = Number(debt?.remainingAmount);

        const paymentVsDebtDelta = Number.isFinite(paymentRemaining) && Number.isFinite(debtRemaining)
            ? paymentRemaining - debtRemaining
            : null;
        const paymentVsAllocationDelta = Number.isFinite(paymentRemaining) && Number.isFinite(allocatedAmount)
            ? paymentRemaining - allocatedAmount
            : null;
        const debtVsAllocationDelta = Number.isFinite(debtRemaining) && Number.isFinite(allocatedAmount)
            ? debtRemaining - allocatedAmount
            : null;

        balanceDeltaContainer.replaceChildren();
        balanceDeltaContainer.append(
            createKpiCell("Allocated amount", formatMoney(allocation.amount, payment?.currency || debt?.currency || "EUR")),
            createKpiCell("Payment remaining", formatMoney(payment?.remainingAmount, payment?.currency || "EUR")),
            createKpiCell("Debt remaining", formatMoney(debt?.remainingAmount, debt?.currency || "EUR")),
            createKpiCell("Payment - debt delta", formatMoney(paymentVsDebtDelta, payment?.currency || debt?.currency || "EUR")),
            createKpiCell("Payment - allocation delta", formatMoney(paymentVsAllocationDelta, payment?.currency || "EUR")),
            createKpiCell("Debt - allocation delta", formatMoney(debtVsAllocationDelta, debt?.currency || "EUR"))
        );
    }

    function renderAuditSummary(allocation, payment, debt, proposal) {
        auditSummaryContainer.replaceChildren();
        const table = createTable(
            ["Audit field", "Value"],
            [
                ["Allocation ID", displayText(allocation.id)],
                ["Command ID", displayText(allocation.commandId)],
                ["Idempotency key", displayText(allocation.idempotencyKey)],
                ["Created by", displayText(allocation.createdBy)],
                ["Created at", formatDateTime(allocation.createdAt)],
                ["Proposal ID", displayText(allocation.proposalId)],
                ["Proposal status", displayText(proposal?.status)],
                ["Proposal validated by", displayText(proposal?.validatedBy)],
                ["Proposal validated at", formatDateTime(proposal?.validatedAt)],
                ["Payment updated at", formatDateTime(payment?.updatedAt)],
                ["Debt updated at", formatDateTime(debt?.updatedAt)]
            ]
        );
        auditSummaryContainer.appendChild(table);
    }
});
