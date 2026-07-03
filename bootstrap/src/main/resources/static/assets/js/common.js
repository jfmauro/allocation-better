const appConfig = {
    apiBaseUrl: ""
};

function getApiUrl(path) {
    return `${appConfig.apiBaseUrl}${path}`;
}

function byId(id) {
    if (typeof document === "undefined") {
        return null;
    }
    return document.getElementById(id);
}

function setBanner(target, type, message) {
    if (!target) {
        return;
    }
    target.className = `status-banner status-${type}`;
    target.textContent = message;
    target.hidden = false;
}

function escapeHtml(value) {
    if (value === null || value === undefined) {
        return "";
    }
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/\"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function displayText(value, fallback = "—") {
    if (value === null || value === undefined || value === "") {
        return fallback;
    }
    return String(value);
}

function toSafeErrorMessage(defaultMessage) {
    return defaultMessage || "Something went wrong. Please try again.";
}

function clearBanner(target) {
    if (!target) {
        return;
    }
    target.hidden = true;
    target.textContent = "";
    target.className = "status-banner status-info";
}

function formatMoney(amount, currency = "EUR") {
    if (amount === null || amount === undefined || Number.isNaN(Number(amount))) {
        return "—";
    }
    return new Intl.NumberFormat("en-BE", { style: "currency", currency }).format(Number(amount));
}

function formatDateTime(value) {
    if (!value) {
        return "—";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return "—";
    }
    return new Intl.DateTimeFormat("en-BE", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(date);
}

function maskValue(value) {
    if (!value) {
        return "—";
    }
    if (value.length <= 4) {
        return "****";
    }
    return `${value.slice(0, 2)}***${value.slice(-2)}`;
}

function mapHttpStatusToSafeMessage(status) {
    if (status === 400) {
        return "The request is invalid. Please verify the input and try again.";
    }
    if (status === 401 || status === 403) {
        return "You are not authorized to perform this action.";
    }
    if (status === 404) {
        return "The requested resource could not be found.";
    }
    if (status >= 500) {
        return "The service is temporarily unavailable. Please try again later.";
    }
    return "The request could not be completed. Please try again.";
}

function encodePathSegment(value) {
    return encodeURIComponent(String(value || ""));
}

async function fetchJson(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        }
    });

    if (!response.ok) {
        throw new Error(mapHttpStatusToSafeMessage(response.status));
    }

    if (response.status === 204) {
        return null;
    }
    return response.json();
}

function getQueryParam(name) {
    const params = new URLSearchParams(window.location.search);
    return params.get(name);
}

function updateQueryParam(name, value) {
    const url = new URL(window.location.href);
    if (value) {
        url.searchParams.set(name, value);
    } else {
        url.searchParams.delete(name);
    }
    window.history.replaceState({}, "", url);
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        escapeHtml,
        displayText,
        mapHttpStatusToSafeMessage,
        encodePathSegment
    };
}
