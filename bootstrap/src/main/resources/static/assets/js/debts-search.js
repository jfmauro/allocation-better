const DEBTS_SEARCH_ALLOWED_STATUSES = new Set(["OPEN", "PARTIALLY_PAID"]);

function filterAllocatableDebts(debts, allowedStatuses = DEBTS_SEARCH_ALLOWED_STATUSES) {
    return (Array.isArray(debts) ? debts : []).filter((debt) => allowedStatuses.has(String(debt?.status || "")));
}

function getSelectedAllocatableStatuses(values, allowedStatuses = DEBTS_SEARCH_ALLOWED_STATUSES) {
    return (Array.isArray(values) ? values : []).filter((status) => allowedStatuses.has(String(status || "")));
}

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        const debtorIdInput = byId("debtor-id");
        const searchButton = byId("search-debts");
        const banner = byId("debt-banner");
        const tableContainer = byId("debt-table");
        const detailContainer = byId("debt-detail");

        function renderMuted(container, message) {
            container.replaceChildren();
            const paragraph = document.createElement("p");
            paragraph.className = "muted";
            paragraph.textContent = message;
            container.appendChild(paragraph);
        }

        searchButton?.addEventListener("click", async () => {
            const debtorId = debtorIdInput.value.trim();
            if (!debtorId) {
                setBanner(banner, "error", "Debtor ID is required.");
                return;
            }

            const selectedStatuses = getSelectedAllocatableStatuses(
                Array.from(document.querySelectorAll("input[name='status']:checked")).map((element) => element.value)
            );

            if (!selectedStatuses.length) {
                setBanner(banner, "error", "Select at least one allocatable status: OPEN or PARTIALLY_PAID.");
                return;
            }

            const query = new URLSearchParams();
            selectedStatuses.forEach((status) => query.append("status", status));

            searchButton.disabled = true;
            setBanner(banner, "info", "Loading debtor debts...");
            try {
                const safeDebtorId = encodePathSegment(debtorId);
                const response = await fetchJson(getApiUrl(`/debtors/${safeDebtorId}/debts?${query.toString()}`));
                const debts = filterAllocatableDebts(response?.debts || []);
                renderDebtTable(debts);
                setBanner(banner, "success", "Debts loaded.");
            } catch {
                renderMuted(tableContainer, "No debts found.");
                detailContainer.textContent = "No debt detail loaded.";
                setBanner(banner, "error", "Unable to search debts at the moment.");
            } finally {
                searchButton.disabled = false;
            }
        });

        function renderDebtTable(debts) {
            tableContainer.replaceChildren();

            if (!debts.length) {
                renderMuted(tableContainer, "No debts match the current criteria.");
                return;
            }

            const table = document.createElement("table");
            const head = document.createElement("thead");
            const headRow = document.createElement("tr");
            ["Debt ID", "Status", "Remaining", "Due date", "Updated"].forEach((title) => {
                const th = document.createElement("th");
                th.textContent = title;
                headRow.appendChild(th);
            });
            head.appendChild(headRow);

            const body = document.createElement("tbody");
            debts.forEach((debt) => {
                const row = document.createElement("tr");

                const debtCell = document.createElement("td");
                const debtButton = document.createElement("button");
                debtButton.type = "button";
                debtButton.className = "btn btn-ghost";
                debtButton.dataset.debtId = String(debt.id || "");
                debtButton.textContent = displayText(debt.id);
                debtButton.addEventListener("click", async () => {
                    await loadDebtDetail(debtButton.dataset.debtId || "");
                });
                debtCell.appendChild(debtButton);

                const statusCell = document.createElement("td");
                const statusChip = document.createElement("span");
                statusChip.className = "status-chip";
                statusChip.dataset.state = String(debt.status || "");
                statusChip.textContent = displayText(debt.status);
                statusCell.appendChild(statusChip);

                const remainingCell = document.createElement("td");
                remainingCell.textContent = formatMoney(debt.remainingAmount, debt.currency);

                const dueDateCell = document.createElement("td");
                dueDateCell.textContent = displayText(debt.dueDate);

                const updatedCell = document.createElement("td");
                updatedCell.textContent = formatDateTime(debt.updatedAt);

                row.append(debtCell, statusCell, remainingCell, dueDateCell, updatedCell);
                body.appendChild(row);
            });

            table.append(head, body);
            tableContainer.appendChild(table);
        }

        async function loadDebtDetail(debtId) {
            try {
                const safeDebtId = encodePathSegment(debtId);
                const debt = await fetchJson(getApiUrl(`/debts/${safeDebtId}`));

                detailContainer.replaceChildren();
                const grid = document.createElement("div");
                grid.className = "kpi-grid";

                const fields = [
                    ["Debt ID", displayText(debt.id)],
                    ["Debtor ID", displayText(debt.debtorId)],
                    ["Status", displayText(debt.status)],
                    ["Remaining", formatMoney(debt.remainingAmount, debt.currency)],
                    ["Structured communication", displayText(debt.structuredCommunication)],
                    ["Free communication", displayText(debt.freeCommunication)]
                ];

                fields.forEach(([label, value]) => {
                    const cell = document.createElement("div");
                    const labelElement = document.createElement("p");
                    labelElement.className = "kpi-label";
                    labelElement.textContent = label;

                    const valueElement = document.createElement("p");
                    valueElement.className = "kpi-value";

                    if (label === "Status") {
                        const chip = document.createElement("span");
                        chip.className = "status-chip";
                        chip.dataset.state = String(debt.status || "");
                        chip.textContent = value;
                        valueElement.appendChild(chip);
                    } else {
                        valueElement.textContent = value;
                    }

                    cell.append(labelElement, valueElement);
                    grid.appendChild(cell);
                });

                detailContainer.appendChild(grid);
            } catch {
                detailContainer.textContent = "Unable to load debt detail at the moment.";
            }
        }
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        DEBTS_SEARCH_ALLOWED_STATUSES,
        filterAllocatableDebts,
        getSelectedAllocatableStatuses
    };
}
