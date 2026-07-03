const PROPOSALS_VALIDATE_ALLOCATABLE_STATUSES = new Set(["OPEN", "PARTIALLY_PAID"]);

function isStatusAllocatable(status, allocatableStatuses = PROPOSALS_VALIDATE_ALLOCATABLE_STATUSES) {
    const normalizedStatus = String(status || "").trim();
    if (!normalizedStatus) {
        return false;
    }
    return allocatableStatuses.has(normalizedStatus);
}

function isValidAllocationAmount(amount, paymentRemainingAmount) {
    const normalizedAmount = Number(amount);
    if (!Number.isFinite(normalizedAmount) || normalizedAmount <= 0) {
        return false;
    }

    const normalizedRemainingAmount = Number(paymentRemainingAmount);
    if (!Number.isFinite(normalizedRemainingAmount)) {
        return false;
    }
    if (normalizedAmount > normalizedRemainingAmount) {
        return false;
    }

    return true;
}

function normalizeCandidate(candidate, allocatableStatuses = PROPOSALS_VALIDATE_ALLOCATABLE_STATUSES) {
    const debtId = String(candidate?.debtId || candidate?.debt?.id || "");
    const debtStatus = String(candidate?.debt?.status || candidate?.debtStatus || candidate?.status || "");
    const debt = {
        ...(candidate?.debt || {}),
        id: candidate?.debt?.id || debtId || null,
        status: debtStatus || candidate?.debt?.status || ""
    };

    return {
        ...candidate,
        debtId,
        debt,
        isAllocatable: isStatusAllocatable(debt.status, allocatableStatuses)
    };
}

function enrichCandidatesFromData(candidates, allocatableStatuses = PROPOSALS_VALIDATE_ALLOCATABLE_STATUSES) {
    return (Array.isArray(candidates) ? candidates : []).map((candidate) => normalizeCandidate(candidate, allocatableStatuses));
}

function selectInitialCandidate(candidates, preferredDebtId) {
    const normalizedPreferredDebtId = String(preferredDebtId || "");
    const visibleCandidates = Array.isArray(candidates)
        ? candidates.filter((candidate) => candidate && (candidate.debtId || candidate.debt?.id))
        : [];
    return visibleCandidates.find((candidate) => String(candidate.debtId) === normalizedPreferredDebtId)
        || visibleCandidates[0]
        || null;
}

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        const proposalIdInput = byId("proposal-id");
        const actorInput = byId("actor");
        const debtIdInput = byId("selected-debt-id");
        const amountInput = byId("allocation-amount");
        const reasonInput = byId("reason");
        const loadButton = byId("load-proposal");
        const candidateList = byId("candidate-list");
        const banner = byId("validate-banner");
        const actionResult = byId("proposal-action-result");
        const paymentInfo = byId("payment-info");
        const debtorInfo = byId("debtor-info");
        const debtInfo = byId("debt-info");
        const fullNationalNumberButton = byId("request-full-national-number");
        const hideNationalNumberButton = byId("hide-full-national-number");
        const accessReasonInput = byId("national-number-access-reason");
        const nationalNumberState = byId("national-number-state");

        let currentProposal = null;
        let currentPayment = null;
        let currentCandidates = [];
        let currentVisibleCandidates = [];
        let currentDebtById = new Map();
        let selectedDebtId = null;
        let selectedDebtor = null;
        let fullNationalNumber = "";
        let fullNationalNumberVisible = false;
        let sensitiveMaskTimeoutId = null;

        const initialProposalId = getQueryParam("proposalId");
        if (initialProposalId) {
            proposalIdInput.value = initialProposalId;
            loadProposal(initialProposalId);
        }

        loadButton?.addEventListener("click", async () => {
            const proposalId = proposalIdInput.value.trim();
            if (!proposalId) {
                setBanner(banner, "error", "Proposal ID is required.");
                return;
            }
            await loadProposal(proposalId);
        });

        byId("validate-btn")?.addEventListener("click", () => submitAction("validate"));
        byId("reject-btn")?.addEventListener("click", () => submitAction("reject"));
        byId("select-debt-btn")?.addEventListener("click", () => submitAction("select-debt"));
        byId("mark-unmatched-btn")?.addEventListener("click", () => submitAction("mark-unmatched"));
        byId("investigation-btn")?.addEventListener("click", () => submitAction("request-investigation"));
        fullNationalNumberButton?.addEventListener("click", requestFullNationalNumberView);
        hideNationalNumberButton?.addEventListener("click", hideFullNationalNumberView);
        window.addEventListener("beforeunload", hideFullNationalNumberView);
        document.addEventListener("visibilitychange", () => {
            if (document.hidden) {
                hideFullNationalNumberView();
            }
        });

        function renderMuted(container, message, tagName = "p") {
            container.replaceChildren();
            const element = document.createElement(tagName);
            element.className = "muted";
            element.textContent = message;
            container.appendChild(element);
        }

        async function loadProposal(proposalId) {
            loadButton.disabled = true;
            setBanner(banner, "info", "Loading proposal details...");
            try {
                const safeProposalId = encodePathSegment(proposalId);
                const proposal = await fetchJson(getApiUrl(`/allocation-proposals/${safeProposalId}`));
                currentProposal = proposal;
                currentPayment = await fetchJson(getApiUrl(`/payments/${encodePathSegment(proposal.paymentId)}`)).catch(() => null);

                const enrichedCandidates = await hydrateCandidates(proposal.candidates || []);
                currentCandidates = enrichedCandidates;
                currentVisibleCandidates = enrichedCandidates.filter((candidate) => candidate.debtId || candidate.debt?.id);
                currentDebtById = new Map(currentCandidates.map((candidate) => [String(candidate.debtId), candidate.debt]));

                const preferredDebtId = proposal.selectedDebtId ? String(proposal.selectedDebtId) : "";
                const initialCandidate = selectInitialCandidate(currentVisibleCandidates, preferredDebtId);

                selectedDebtId = initialCandidate ? String(initialCandidate.debtId) : null;
                selectedDebtor = initialCandidate?.debtor || initialCandidate?.debt?.debtor || null;
                fullNationalNumber = "";
                fullNationalNumberVisible = false;
                nationalNumberState.textContent = "National number is masked by default.";
                debtIdInput.value = selectedDebtId || "";
                if (initialCandidate) {
                    amountInput.value = initialCandidate.suggestedAmount || "";
                }

                renderCandidates(currentVisibleCandidates);
                renderInformationBlocks(initialCandidate);
                updateQueryParam("proposalId", proposalId);
                setBanner(banner, "success", `Proposal loaded (${proposal.status}).`);
            } catch {
                renderMuted(candidateList, "No candidate debts available.", "li");
                renderMuted(paymentInfo, "No payment information available.");
                renderMuted(debtorInfo, "No debtor information available.");
                renderMuted(debtInfo, "No debt information available.");
                setBanner(banner, "error", "Unable to load proposal at the moment.");
            } finally {
                loadButton.disabled = false;
            }
        }

        async function hydrateCandidates(candidates) {
            const normalizedCandidates = enrichCandidatesFromData(candidates);
            return Promise.all(normalizedCandidates.map(async (candidate) => {
                const [debt, debtor] = await Promise.all([
                    candidate.debt?.id ? Promise.resolve(candidate.debt) : fetchDebt(candidate.debtId),
                    candidate.debtor?.id ? Promise.resolve(candidate.debtor) : fetchDebtor(candidate.debtorId)
                ]);
                const hydratedDebt = debt ? { ...debt, debtor } : debt;
                return normalizeCandidate({
                    ...candidate,
                    debt: hydratedDebt,
                    debtor
                });
            }));
        }

        async function fetchDebt(debtId) {
            if (!debtId) {
                return null;
            }
            try {
                return await fetchJson(getApiUrl(`/debts/${encodePathSegment(debtId)}`));
            } catch {
                return null;
            }
        }

        async function fetchDebtor(debtorId) {
            if (!debtorId) {
                return null;
            }
            try {
                const response = await fetchJson(getApiUrl(`/debtors?query=${encodeURIComponent(String(debtorId))}`));
                const debtors = Array.isArray(response) ? response : (response?.debtors || response?.items || []);
                return debtors.find((debtor) => String(debtor?.id || "") === String(debtorId)) || null;
            } catch {
                return null;
            }
        }

        function renderCandidates(candidates) {
            candidateList.replaceChildren();

            if (!candidates.length) {
                renderMuted(candidateList, "No candidate debts found for this proposal.", "li");
                return;
            }

            candidates.forEach((candidate) => {
                const item = document.createElement("li");
                item.className = "candidate-item";

                const button = document.createElement("button");
                button.type = "button";
                button.className = "btn btn-ghost";
                button.dataset.debtId = String(candidate.debtId || "");
                button.dataset.amount = String(candidate.suggestedAmount || "");
                const allocatableLabel = candidate.isAllocatable ? "allocatable" : "not allocatable";
                button.textContent = `Debt ${displayText(candidate.debtId)} • ${displayText(candidate.debt?.status)} • ${displayText(candidate.confidence)} • ${allocatableLabel} • Suggested ${formatMoney(candidate.suggestedAmount)}`;

                button.addEventListener("click", () => {
                    candidateList.querySelectorAll(".candidate-item").forEach((row) => row.classList.remove("selected"));
                    item.classList.add("selected");
                    selectedDebtId = button.dataset.debtId || "";
                    debtIdInput.value = selectedDebtId;
                    amountInput.value = button.dataset.amount || "";
                    const selectedCandidate = currentCandidates.find((entry) => String(entry.debtId) === selectedDebtId) || null;
                    selectedDebtor = selectedCandidate?.debtor || selectedCandidate?.debt?.debtor || null;
                    fullNationalNumber = "";
                    fullNationalNumberVisible = false;
                    nationalNumberState.textContent = "National number is masked by default.";
                    renderInformationBlocks(selectedCandidate);
                });

                item.appendChild(button);
                candidateList.appendChild(item);
            });

            if (selectedDebtId) {
                candidateList.querySelectorAll("[data-debt-id]").forEach((button) => {
                    if (button.getAttribute("data-debt-id") === selectedDebtId) {
                        button.closest(".candidate-item")?.classList.add("selected");
                    }
                });
            }
        }

        function renderDetailsList(container, entries) {
            container.replaceChildren();
            const list = document.createElement("dl");

            entries.forEach(([label, value]) => {
                const wrapper = document.createElement("div");
                const term = document.createElement("dt");
                const definition = document.createElement("dd");

                term.textContent = label;
                definition.textContent = displayValue(value);

                wrapper.append(term, definition);
                list.appendChild(wrapper);
            });

            container.appendChild(list);
        }

        function renderInformationBlocks(selectedCandidate) {
            const selectedDebt = selectedCandidate?.debt || null;
            selectedDebtor = selectedCandidate?.debtor || selectedDebt?.debtor || selectedDebtor || null;
            const maskedNationalNumber = maskNationalNumber(selectedDebtor?.nationalNumberMasked || fullNationalNumber || selectedDebtor?.nationalNumber || "");
            const displayedNationalNumber = fullNationalNumberVisible
                ? formatFullNationalNumber(fullNationalNumber)
                : maskedNationalNumber;

            renderDetailsList(paymentInfo, [
                ["paymentId", currentProposal?.paymentId],
                ["bankTransactionReference", currentPayment?.bankTransactionReference],
                ["executionDate", formatDateTime(currentPayment?.executionDate)],
                ["valueDate", formatDateTime(currentPayment?.valueDate)],
                ["amount", formatMoney(currentPayment?.amount, currentPayment?.currency || "EUR")],
                ["remainingAmount", formatMoney(currentPayment?.remainingAmount, currentPayment?.currency || "EUR")],
                ["currency", currentPayment?.currency],
                ["payerName", currentPayment?.payerName],
                ["masked payerIban", maskValue(currentPayment?.payerIbanMasked || "")],
                ["structuredCommunication", currentPayment?.structuredCommunication],
                ["freeCommunication", currentPayment?.freeCommunication],
                ["matchingMethod", currentProposal?.matchingMethod],
                ["matchingConfidence", selectedCandidate?.confidence]
            ]);

            renderDetailsList(debtorInfo, [
                ["debtorId", selectedCandidate?.debtorId || selectedDebt?.debtorId || selectedDebtor?.id],
                ["type", selectedDebtor?.type],
                ["displayName", selectedDebtor?.displayName],
                ["nationalNumber", selectedDebtor?.nationalNumber],
                ["enterpriseNumber", selectedDebtor?.enterpriseNumber],
                [fullNationalNumberVisible ? "full nationalNumber" : "masked nationalNumber", displayedNationalNumber],
                ["active", selectedDebtor?.active],
                ["createdAt", formatDateTime(selectedDebtor?.createdAt)]
            ]);

            renderDetailsList(debtInfo, [
                ["debtId", selectedDebt?.id || selectedCandidate?.debtId],
                ["debtorId", selectedDebt?.debtorId || selectedCandidate?.debtorId],
                ["reference", selectedDebt?.reference],
                ["originalAmount", formatMoney(selectedDebt?.originalAmount, selectedDebt?.currency || "EUR")],
                ["remainingAmount", formatMoney(selectedDebt?.remainingAmount, selectedDebt?.currency || "EUR")],
                ["currency", selectedDebt?.currency],
                ["dueDate", selectedDebt?.dueDate],
                ["status", selectedDebt?.status],
                ["structuredCommunication", selectedDebt?.structuredCommunication],
                ["freeCommunication", selectedDebt?.freeCommunication]
            ]);

            if (selectedCandidate && !selectedCandidate.isAllocatable) {
                setBanner(banner, "warning", "Candidate details are visible, but this debt cannot be validated or selected because its status is not allocatable.");
            }
        }

        function displayValue(value) {
            if (value === null || value === undefined || value === "") {
                return "—";
            }
            return String(value);
        }

        function formatAddress(address) {
            if (!address) {
                return "—";
            }
            if (typeof address === "string") {
                return address;
            }
            const parts = [address.street, address.number, address.postalCode, address.city, address.country]
                .filter((part) => part);
            return parts.length ? parts.join(", ") : "—";
        }

        function maskNationalNumber(value) {
            if (!value) {
                return "—";
            }
            if (String(value).includes("*")) {
                return String(value);
            }
            const digits = String(value).replace(/\D/g, "");
            if (!digits) {
                return "—";
            }
            if (digits.length <= 6) {
                return "******";
            }
            return `${digits.slice(0, 6)}*****`;
        }

        function formatFullNationalNumber(value) {
            const digits = String(value || "").replace(/\D/g, "");
            if (!digits) {
                return "—";
            }
            return digits;
        }

        async function requestFullNationalNumberView() {
            const accessReason = accessReasonInput?.value.trim() || "";
            const debtorId = String(selectedDebtor?.id || selectedDebtor?.debtorId || currentProposal?.candidateDebtorId || "").trim();
            if (!accessReason) {
                setBanner(banner, "error", "Access reason is required before full display.");
                return;
            }
            if (!debtorId) {
                setBanner(banner, "error", "Select a candidate debtor before requesting full display.");
                return;
            }

            let securedNationalNumber;
            try {
                const sensitivePayload = await fetchJson(getApiUrl(`/debtors/${encodePathSegment(debtorId)}/national-number/full-view`), {
                    method: "POST",
                    body: JSON.stringify({
                        paymentId: currentProposal?.paymentId || null,
                        accessReason
                    })
                });
                securedNationalNumber = formatFullNationalNumber(
                    sensitivePayload?.nationalNumber
                    || sensitivePayload?.value
                    || sensitivePayload?.fullNationalNumber
                    || ""
                );
            } catch {
                setBanner(banner, "error", "Full display was denied or unavailable. Check permission and reason.");
                return;
            }

            if (securedNationalNumber === "—") {
                setBanner(banner, "error", "Full national number was not returned by the secured endpoint.");
                return;
            }

            try {
                await fetchJson(getApiUrl("/audit/access-logs"), {
                    method: "POST",
                    body: JSON.stringify({
                        paymentId: currentProposal?.paymentId || null,
                        debtorId,
                        accessReason,
                        eventType: "NATIONAL_NUMBER_FULL_VIEWED"
                    })
                });
            } catch {
                setBanner(banner, "error", "Unable to grant full display because access logging is unavailable.");
                return;
            }

            fullNationalNumber = securedNationalNumber;
            fullNationalNumberVisible = true;
            if (sensitiveMaskTimeoutId) {
                window.clearTimeout(sensitiveMaskTimeoutId);
            }
            sensitiveMaskTimeoutId = window.setTimeout(() => {
                hideFullNationalNumberView();
                setBanner(banner, "info", "Full national number was automatically masked after timeout.");
            }, 60000);
            nationalNumberState.textContent = "Full national number displayed after permission, reason, and audit logging.";
            renderInformationBlocks(currentCandidates.find((entry) => String(entry.debtId) === selectedDebtId) || null);
            setBanner(banner, "success", "Full national number access granted and logged.");
        }

        function hideFullNationalNumberView() {
            if (sensitiveMaskTimeoutId) {
                window.clearTimeout(sensitiveMaskTimeoutId);
                sensitiveMaskTimeoutId = null;
            }
            fullNationalNumberVisible = false;
            fullNationalNumber = "";
            nationalNumberState.textContent = "National number is masked by default.";
            renderInformationBlocks(currentCandidates.find((entry) => String(entry.debtId) === selectedDebtId) || null);
        }

        async function submitAction(action) {
            const proposalId = proposalIdInput.value.trim();
            const actor = actorInput.value.trim();
            if (!proposalId || !actor) {
                setBanner(banner, "error", "Proposal ID and actor are required.");
                return;
            }

            const reason = reasonInput.value.trim();
            const amount = Number(amountInput.value);
            const debtId = debtIdInput.value.trim();

            let payload;
            if (action === "validate") {
                if (!debtId || !reason) {
                    setBanner(banner, "error", "Debt ID and reason are required for validation.");
                    return;
                }
                if (!Number.isFinite(Number(currentPayment?.remainingAmount))) {
                    setBanner(banner, "error", "Payment remaining amount is unavailable. Reload proposal before validation.");
                    return;
                }
                if (!isValidAllocationAmount(amount, currentPayment?.remainingAmount)) {
                    setBanner(banner, "error", "Allocation amount must be greater than 0 and not exceed payment remaining amount.");
                    return;
                }
                if (!isStatusAllocatable(currentDebtById.get(debtId)?.status)) {
                    setBanner(banner, "error", "Only debts with OPEN or PARTIALLY_PAID status can be validated.");
                    return;
                }
                payload = { debtId, amount, actor, reason };
            } else if (action === "select-debt") {
                if (!debtId) {
                    setBanner(banner, "error", "Debt ID is required to select a debt.");
                    return;
                }
                if (!isStatusAllocatable(currentDebtById.get(debtId)?.status)) {
                    setBanner(banner, "error", "Only debts with OPEN or PARTIALLY_PAID status can be selected.");
                    return;
                }
                payload = { debtId, actor };
            } else {
                if (!reason) {
                    setBanner(banner, "error", "Reason is required for this action.");
                    return;
                }
                payload = { actor, reason };
            }

            setBanner(banner, "info", `Submitting '${action}' action...`);
            try {
                const safeProposalId = encodePathSegment(proposalId);
                const response = await fetchJson(getApiUrl(`/allocation-proposals/${safeProposalId}/${action}`), {
                    method: "POST",
                    body: JSON.stringify(payload)
                });
                actionResult.textContent = JSON.stringify(response, null, 2);
                setBanner(banner, "success", `Action '${action}' completed.`);
                await loadProposal(proposalId);
            } catch {
                setBanner(banner, "error", "Action failed. Please retry in a moment.");
            }
        }
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        PROPOSALS_VALIDATE_ALLOCATABLE_STATUSES,
        isStatusAllocatable,
        isValidAllocationAmount,
        normalizeCandidate,
        enrichCandidatesFromData,
        selectInitialCandidate
    };
}
