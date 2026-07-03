document.addEventListener("DOMContentLoaded", () => {
    const actorInput = byId("filter-actor");
    const reasonInput = byId("filter-reason");
    const fromInput = byId("filter-from");
    const toInput = byId("filter-to");
    const loadButton = byId("load-access-logs");
    const clearButton = byId("clear-log-filters");
    const banner = byId("audit-banner");
    const tableContainer = byId("audit-table");

    function renderMuted(container, message) {
        container.replaceChildren();
        const paragraph = document.createElement("p");
        paragraph.className = "muted";
        paragraph.textContent = message;
        container.appendChild(paragraph);
    }

    loadButton?.addEventListener("click", async () => {
        setBanner(banner, "info", "Loading access log entries...");
        loadButton.disabled = true;

        const params = new URLSearchParams();
        if (actorInput.value.trim()) {
            params.set("actor", actorInput.value.trim());
        }
        if (reasonInput.value.trim()) {
            params.set("reason", reasonInput.value.trim());
        }
        if (fromInput.value) {
            params.set("from", fromInput.value);
        }
        if (toInput.value) {
            params.set("to", toInput.value);
        }

        const query = params.toString() ? `?${params.toString()}` : "";
        try {
            const response = await fetchJson(getApiUrl(`/audit/access-logs${query}`));
            const items = Array.isArray(response.items) ? response.items : [];
            renderTable(items);
            setBanner(banner, items.length ? "success" : "warning", items.length
                ? "Access logs loaded."
                : "No access logs match current filters.");
        } catch {
            renderMuted(tableContainer, "Audit log endpoint is unavailable in the current backend build.");
            setBanner(banner, "warning", "Unable to fetch access logs right now.");
        } finally {
            loadButton.disabled = false;
        }
    });

    clearButton?.addEventListener("click", () => {
        actorInput.value = "";
        reasonInput.value = "";
        fromInput.value = "";
        toInput.value = "";
        clearBanner(banner);
        renderMuted(tableContainer, "Apply filters, then load access logs.");
    });

    renderMuted(tableContainer, "Apply filters, then load access logs.");

    function renderTable(items) {
        if (!items.length) {
            renderMuted(tableContainer, "No access events found.");
            return;
        }
        tableContainer.replaceChildren();
        const table = document.createElement("table");
        const head = document.createElement("thead");
        const headRow = document.createElement("tr");
        ["Timestamp", "Actor", "Debtor ID", "Payment ID", "Reason"].forEach((title) => {
            const th = document.createElement("th");
            th.textContent = title;
            headRow.appendChild(th);
        });
        head.appendChild(headRow);

        const body = document.createElement("tbody");
        items.forEach((item) => {
            const row = document.createElement("tr");

            const timestampCell = document.createElement("td");
            timestampCell.textContent = formatDateTime(item.createdAt);

            const actorCell = document.createElement("td");
            actorCell.textContent = displayText(item.userId || item.actor);

            const debtorCell = document.createElement("td");
            debtorCell.textContent = displayText(item.debtorId);

            const paymentCell = document.createElement("td");
            paymentCell.textContent = displayText(item.paymentId);

            const reasonCell = document.createElement("td");
            reasonCell.textContent = displayText(item.reason);

            row.append(timestampCell, actorCell, debtorCell, paymentCell, reasonCell);
            body.appendChild(row);
        });

        table.append(head, body);
        tableContainer.appendChild(table);
    }
});
