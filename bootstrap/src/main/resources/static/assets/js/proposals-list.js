document.addEventListener("DOMContentLoaded", () => {
    const paymentIdInput = byId("payment-id");
    const statusFilter = byId("status-filter");
    const loadButton = byId("load-proposals");
    const sortButton = byId("sort-updated");
    const banner = byId("proposal-banner");
    const kpiContainer = byId("queue-kpis");
    const tableContainer = byId("proposal-table");

    let currentProposals = [];

    function renderMuted(container, message) {
        container.replaceChildren();
        const paragraph = document.createElement("p");
        paragraph.className = "muted";
        paragraph.textContent = message;
        container.appendChild(paragraph);
    }

    function createKpiCell(label, value) {
        const cell = document.createElement("div");
        const labelElement = document.createElement("p");
        labelElement.className = "kpi-label";
        labelElement.textContent = label;
        const valueElement = document.createElement("p");
        valueElement.className = "kpi-value";
        valueElement.textContent = String(value);
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
    }

    loadButton?.addEventListener("click", async () => {
        const paymentId = paymentIdInput.value.trim();
        if (!paymentId) {
            setBanner(banner, "error", "Payment ID is required.");
            return;
        }
        await loadProposals(paymentId);
    });

    statusFilter?.addEventListener("change", () => render());
    sortButton?.addEventListener("click", () => {
        currentProposals.sort((a, b) => new Date(b.updatedAt || 0).getTime() - new Date(a.updatedAt || 0).getTime());
        render();
    });

    async function loadProposals(paymentId) {
        loadButton.disabled = true;
        setBanner(banner, "info", "Loading proposal queue...");
        try {
            const safePaymentId = encodePathSegment(paymentId);
            const response = await fetchJson(getApiUrl(`/payments/${safePaymentId}/proposals`));
            currentProposals = Array.isArray(response.proposals) ? response.proposals : [];
            updateQueryParam("paymentId", paymentId);
            render();
            setBanner(banner, currentProposals.length ? "success" : "warning", currentProposals.length
                ? "Proposal queue loaded."
                : "No proposals found.");
        } catch {
            currentProposals = [];
            render();
            setBanner(banner, "error", "Unable to load proposals right now.");
        } finally {
            loadButton.disabled = false;
        }
    }

    function render() {
        const filter = statusFilter.value;
        const proposals = filter === "ALL"
            ? [...currentProposals]
            : currentProposals.filter((proposal) => proposal.status === filter);

        const counters = {
            total: proposals.length,
            proposed: proposals.filter((proposal) => proposal.status === "PROPOSED").length,
            investigation: proposals.filter((proposal) => String(proposal.status).includes("INVESTIGATION")).length,
            rejected: proposals.filter((proposal) => proposal.status === "REJECTED").length
        };

        kpiContainer.replaceChildren();
        kpiContainer.append(
            createKpiCell("Total", counters.total),
            createKpiCell("Proposed", counters.proposed),
            createKpiCell("Investigation", counters.investigation),
            createKpiCell("Rejected", counters.rejected)
        );

        if (!proposals.length) {
            renderMuted(tableContainer, "No proposals for the selected filter.");
            return;
        }

        tableContainer.replaceChildren();
        const table = document.createElement("table");
        const head = document.createElement("thead");
        const headRow = document.createElement("tr");
        ["Proposal", "Status", "Method", "Reason", "Created", "Updated"].forEach((title) => {
            const th = document.createElement("th");
            th.textContent = title;
            headRow.appendChild(th);
        });
        head.appendChild(headRow);

        const body = document.createElement("tbody");
        proposals.forEach((proposal) => {
            const row = document.createElement("tr");

            const proposalCell = document.createElement("td");
            const link = document.createElement("a");
            link.href = `/app/proposals/validate.html?proposalId=${encodeURIComponent(String(proposal.id || ""))}`;
            link.textContent = displayText(proposal.id);
            proposalCell.appendChild(link);

            const statusCell = document.createElement("td");
            statusCell.appendChild(createStatusChip(proposal.status));

            const methodCell = document.createElement("td");
            methodCell.textContent = displayText(proposal.matchingMethod);

            const reasonCell = document.createElement("td");
            reasonCell.textContent = displayText(proposal.reason);

            const createdCell = document.createElement("td");
            createdCell.textContent = formatDateTime(proposal.createdAt);

            const updatedCell = document.createElement("td");
            updatedCell.textContent = formatDateTime(proposal.updatedAt);

            row.append(proposalCell, statusCell, methodCell, reasonCell, createdCell, updatedCell);
            body.appendChild(row);
        });

        table.append(head, body);
        tableContainer.appendChild(table);
    }
});
