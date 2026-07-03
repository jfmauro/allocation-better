const DEBTOR_LIST_SUPPORTED_TYPES = new Set(["NATURAL_PERSON", "ENTERPRISE"]);

function buildDebtorsQuery(filters) {
    const params = new URLSearchParams();
    const query = String(filters?.query || "").trim();
    const debtorType = String(filters?.debtorType || "").trim();
    const activeOnly = Boolean(filters?.activeOnly);

    if (query) {
        params.set("query", query);
    }
    if (DEBTOR_LIST_SUPPORTED_TYPES.has(debtorType)) {
        params.set("debtorType", debtorType);
    }
    if (activeOnly) {
        params.set("active", "true");
    }

    return params.toString();
}

function normalizeDebtorListResponse(response) {
    let source = [];
    if (Array.isArray(response)) {
        source = response;
    } else if (Array.isArray(response?.debtors)) {
        source = response.debtors;
    } else if (Array.isArray(response?.items)) {
        source = response.items;
    } else if (Array.isArray(response?.results)) {
        source = response.results;
    } else if (Array.isArray(response?.content)) {
        source = response.content;
    } else if (Array.isArray(response?.data)) {
        source = response.data;
    }

        return source.map((item) => ({
            id: item?.id || null,
            type: item?.type || item?.debtorType || null,
            displayName: item?.displayName || null,
            enterpriseNumber: item?.enterpriseNumber || null,
            nationalNumber: item?.nationalNumber || null,
            active: item?.active,
            createdAt: item?.createdAt || null
        }));
}

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        const queryInput = byId("search-query");
        const typeFilter = byId("debtor-type-filter");
        const activeOnlyCheckbox = byId("active-only");
        const searchButton = byId("search-debtors");
        const banner = byId("debtors-banner");
        const tableContainer = byId("debtors-table");

        function renderMuted(message) {
            tableContainer.replaceChildren();
            const paragraph = document.createElement("p");
            paragraph.className = "muted";
            paragraph.textContent = message;
            tableContainer.appendChild(paragraph);
        }

        function renderDebtorTable(debtors) {
            tableContainer.replaceChildren();

            if (!debtors.length) {
                renderMuted("No debtors found for the selected filters.");
                return;
            }

            const table = document.createElement("table");
            const caption = document.createElement("caption");
            caption.className = "sr-only";
            caption.textContent = "Debtor search results";
            table.appendChild(caption);

            const head = document.createElement("thead");
            const headRow = document.createElement("tr");
            ["Debtor ID", "Display name", "Type", "Enterprise number", "National hash", "Active", "Created"].forEach((title) => {
                const th = document.createElement("th");
                th.scope = "col";
                th.textContent = title;
                headRow.appendChild(th);
            });
            head.appendChild(headRow);

            const body = document.createElement("tbody");
            debtors.forEach((debtor) => {
                const row = document.createElement("tr");

                const idCell = document.createElement("td");
                const idLink = document.createElement("a");
                idLink.href = `/app/debts/create.html?debtorId=${encodeURIComponent(String(debtor.id || ""))}`;
                idLink.textContent = displayText(debtor.id);
                idCell.appendChild(idLink);

                const displayNameCell = document.createElement("td");
                displayNameCell.textContent = displayText(debtor.displayName);

                const typeCell = document.createElement("td");
                typeCell.textContent = displayText(debtor.type);

                const enterpriseCell = document.createElement("td");
                enterpriseCell.textContent = displayText(debtor.enterpriseNumber);

                const nationalCell = document.createElement("td");
                nationalCell.textContent = displayText(debtor.nationalNumber);

                const activeCell = document.createElement("td");
                activeCell.textContent = debtor.active === true ? "Yes" : debtor.active === false ? "No" : "—";

                const createdAtCell = document.createElement("td");
                createdAtCell.textContent = formatDateTime(debtor.createdAt);

                row.append(idCell, displayNameCell, typeCell, enterpriseCell, nationalCell, activeCell, createdAtCell);
                body.appendChild(row);
            });

            table.append(head, body);
            tableContainer.appendChild(table);
        }

        renderMuted("Use the search criteria to load debtor master data.");

        searchButton?.addEventListener("click", async () => {
            const query = buildDebtorsQuery({
                query: queryInput?.value,
                debtorType: typeFilter?.value,
                activeOnly: activeOnlyCheckbox?.checked
            });

            searchButton.disabled = true;
            setBanner(banner, "info", "Loading debtors...");

            try {
                const endpoint = query ? `/debtors?${query}` : "/debtors";
                const response = await fetchJson(getApiUrl(endpoint));
                const debtors = normalizeDebtorListResponse(response);
                renderDebtorTable(debtors);
                setBanner(banner, debtors.length ? "success" : "warning", debtors.length
                    ? "Debtors loaded."
                    : "No debtors found.");
            } catch (error) {
                renderMuted("Unable to load debtors at the moment.");
                setBanner(banner, "error", toSafeErrorMessage(error?.message));
            } finally {
                searchButton.disabled = false;
            }
        });
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        DEBTOR_LIST_SUPPORTED_TYPES,
        buildDebtorsQuery,
        normalizeDebtorListResponse
    };
}
