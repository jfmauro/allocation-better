const GRAPH_URL_CANDIDATE_PATHS = ["../graphify-out/graph.json", "/graphify-out/graph.json", "./graphify-out/graph.json"];
const GRAPH_FETCH_TIMEOUT_MS = 12000;
const GRAPH_MODE_DEFAULT = "embedded";
const GRAPH_MODE_REMOTE = "remote";
const EMBEDDED_GRAPH_SOURCE = "graphify-out/graph.json";

const GRAPHIFY_GLOBAL_STATS = {
  nodes: 1533,
  edges: 3909,
  communities: 82,
  extractedRate: 87,
  inferredRate: 13
};

const CANVAS = {
  width: 1800,
  height: 1000,
  margin: { top: 118, right: 64, bottom: 56, left: 64 }
};

const PRIMARY_LAYERS = ["adapter-in", "application", "domain", "adapter-out", "bootstrap"];
const LAYER_ORDER = [...PRIMARY_LAYERS, "other"];

const LAYER_LABELS = {
  "adapter-in": "adapter-in",
  application: "application",
  domain: "domain",
  "adapter-out": "adapter-out",
  bootstrap: "bootstrap",
  other: "other"
};

const RELATION_COLORS = {
  imports: "#06b6d4",
  references: "#4f46e5",
  implements: "#f97316",
  calls: "#f59e0b",
  contains: "#6366f1",
  method: "#94a3b8",
  extends: "#f97316"
};

const NODE_SELECTORS = [
  {
    key: "payment",
    label: "Payment",
    id: "domain_src_main_java_com_pipelinepro_domain_payment_payment",
    sourceIncludes: "domain/src/main/java/com/pipelinepro/domain/Payment.java",
    mandatory: true
  },
  {
    key: "debt",
    label: "Debt",
    id: "domain_src_main_java_com_pipelinepro_domain_debt_debt",
    sourceIncludes: "domain/src/main/java/com/pipelinepro/domain/Debt.java",
    mandatory: true
  },
  {
    key: "debtor",
    label: "Debtor",
    id: "domain_src_main_java_com_pipelinepro_domain_debtor_debtor",
    sourceIncludes: "domain/src/main/java/com/pipelinepro/domain/Debtor.java",
    mandatory: true
  },
  {
    key: "allocation-proposal",
    label: "AllocationProposal",
    id: "domain_src_main_java_com_pipelinepro_domain_allocationproposal_allocationproposal",
    sourceIncludes: "domain/src/main/java/com/pipelinepro/domain/AllocationProposal.java",
    mandatory: true
  },
  {
    key: "payment-allocation",
    label: "PaymentAllocation",
    id: "domain_src_main_java_com_pipelinepro_domain_paymentallocation_paymentallocation",
    sourceIncludes: "domain/src/main/java/com/pipelinepro/domain/PaymentAllocation.java",
    mandatory: true
  },
  {
    key: "payment-matching-app-service",
    label: "PaymentMatchingApplicationService",
    id: "application_src_main_java_com_pipelinepro_application_paymentmatchingapplicationservice_paymentmatchingapplicationservice",
    sourceIncludes: "application/src/main/java/com/pipelinepro/application/PaymentMatchingApplicationService.java",
    mandatory: true
  },
  {
    key: "proposal-lifecycle-app-service",
    label: "ProposalLifecycleApplicationService",
    id: "application_src_main_java_com_pipelinepro_application_proposallifecycleapplicationservice_proposallifecycleapplicationservice",
    sourceIncludes: "application/src/main/java/com/pipelinepro/application/ProposalLifecycleApplicationService.java",
    mandatory: true
  },
  {
    key: "allocation-exec-app-service",
    label: "AllocationExecutionApplicationService",
    id: "application_src_main_java_com_pipelinepro_application_allocationexecutionapplicationservice_allocationexecutionapplicationservice",
    sourceIncludes: "application/src/main/java/com/pipelinepro/application/AllocationExecutionApplicationService.java",
    mandatory: true
  },
  {
    key: "query-app-service",
    label: "QueryApplicationService",
    id: "application_src_main_java_com_pipelinepro_application_queryapplicationservice_queryapplicationservice",
    sourceIncludes: "application/src/main/java/com/pipelinepro/application/QueryApplicationService.java",
    mandatory: true
  },
  {
    key: "payment-controller",
    label: "PaymentController",
    id: "adapter_in_src_main_java_com_pipelinepro_adapter_in_web_v1_paymentcontroller_paymentcontroller",
    sourceIncludes: "adapter-in/src/main/java/com/pipelinepro/adapter/in/web/v1/PaymentController.java",
    mandatory: true
  },
  {
    key: "allocation-proposal-controller",
    label: "AllocationProposalController",
    id: "adapter_in_src_main_java_com_pipelinepro_adapter_in_web_v1_allocationproposalcontroller_allocationproposalcontroller",
    sourceIncludes: "adapter-in/src/main/java/com/pipelinepro/adapter/in/web/v1/AllocationProposalController.java",
    mandatory: true
  },
  {
    key: "proposal-web-mapper",
    label: "ProposalWebMapper",
    id: "adapter_in_src_main_java_com_pipelinepro_adapter_in_web_mapper_proposalwebmapper_proposalwebmapper",
    sourceIncludes: "adapter-in/src/main/java/com/pipelinepro/adapter/in/web/mapper/ProposalWebMapper.java",
    mandatory: true
  },
  {
    key: "application-service-config",
    label: "ApplicationServiceConfig",
    id: "bootstrap_src_main_java_com_pipelinepro_bootstrap_config_applicationserviceconfig_applicationserviceconfig",
    sourceIncludes: "bootstrap/src/main/java/com/pipelinepro/bootstrap/config/ApplicationServiceConfig.java",
    mandatory: true
  },

  /* Ports / use cases */
  {
    key: "match-payment-usecase",
    label: "MatchPaymentUseCase",
    id: "domain_src_main_java_com_pipelinepro_domain_port_in_matchpaymentusecase_matchpaymentusecase",
    sourceIncludes: "domain/src/main/java/com/pipelinepro/domain/port/in/MatchPaymentUseCase.java"
  },
  {
    key: "proposal-lifecycle-usecase",
    label: "ProposalLifecycleUseCase",
    id: "domain_src_main_java_com_pipelinepro_domain_port_in_proposallifecycleusecase_proposallifecycleusecase",
    sourceIncludes: "domain/src/main/java/com/pipelinepro/domain/port/in/ProposalLifecycleUseCase.java"
  },
  {
    key: "execute-allocation-usecase",
    label: "ExecuteAllocationUseCase",
    id: "domain_src_main_java_com_pipelinepro_domain_port_in_executeallocationusecase_executeallocationusecase",
    sourceIncludes: "domain/src/main/java/com/pipelinepro/domain/port/in/ExecuteAllocationUseCase.java"
  },
  {
    key: "query-payment-usecase",
    label: "QueryPaymentUseCase",
    id: "domain_src_main_java_com_pipelinepro_domain_port_in_querypaymentusecase_querypaymentusecase",
    sourceIncludes: "domain/src/main/java/com/pipelinepro/domain/port/in/QueryPaymentUseCase.java"
  },
  {
    key: "receive-payment-usecase",
    label: "ReceivePaymentUseCase",
    id: "domain_src_main_java_com_pipelinepro_domain_port_in_receivepaymentusecase_receivepaymentusecase",
    sourceIncludes: "domain/src/main/java/com/pipelinepro/domain/port/in/ReceivePaymentUseCase.java"
  },
  {
    key: "query-debt-usecase",
    label: "QueryDebtUseCase",
    id: "domain_src_main_java_com_pipelinepro_domain_port_in_querydebtusecase_querydebtusecase",
    sourceIncludes: "domain/src/main/java/com/pipelinepro/domain/port/in/QueryDebtUseCase.java"
  },
  {
    key: "query-debtor-usecase",
    label: "QueryDebtorUseCase",
    id: "domain_src_main_java_com_pipelinepro_domain_port_in_querydebtorusecase_querydebtorusecase",
    sourceIncludes: "domain/src/main/java/com/pipelinepro/domain/port/in/QueryDebtorUseCase.java"
  },
  {
    key: "get-allocation-proposal-details-usecase",
    label: "GetAllocationProposalDetailsUseCase",
    id: "domain_src_main_java_com_pipelinepro_domain_port_in_getallocationproposaldetailsusecase_getallocationproposaldetailsusecase",
    sourceIncludes: "domain/src/main/java/com/pipelinepro/domain/port/in/GetAllocationProposalDetailsUseCase.java"
  },

  /* Ports / worker / repositories imported by ApplicationServiceConfig */
  {
    key: "payment-repository-port",
    label: "PaymentRepository",
    id: "bootstrap_src_main_java_com_pipelinepro_bootstrap_config_applicationserviceconfig_java_paymentrepository"
  },
  {
    key: "debt-repository-port",
    label: "DebtRepository",
    id: "bootstrap_src_main_java_com_pipelinepro_bootstrap_config_applicationserviceconfig_java_debtrepository"
  },
  {
    key: "debtor-repository-port",
    label: "DebtorRepository",
    id: "bootstrap_src_main_java_com_pipelinepro_bootstrap_config_applicationserviceconfig_java_debtorrepository"
  },
  {
    key: "allocation-proposal-repository-port",
    label: "AllocationProposalRepository",
    id: "bootstrap_src_main_java_com_pipelinepro_bootstrap_config_applicationserviceconfig_java_allocationproposalrepository"
  },
  {
    key: "payment-allocation-repository-port",
    label: "PaymentAllocationRepository",
    id: "bootstrap_src_main_java_com_pipelinepro_bootstrap_config_applicationserviceconfig_java_paymentallocationrepository"
  },
  {
    key: "allocation-worker-port",
    label: "AllocationTransactionalWorker",
    id: "bootstrap_src_main_java_com_pipelinepro_bootstrap_config_applicationserviceconfig_java_allocationtransactionalworker"
  },
  {
    key: "audit-event-gateway-port",
    label: "AuditEventGateway",
    id: "bootstrap_src_main_java_com_pipelinepro_bootstrap_config_applicationserviceconfig_java_auditeventgateway"
  },

  /* JPA / SpringData components surfaced in this Graphify snapshot */
  {
    key: "jpa-payment-intake-worker",
    label: "JpaPaymentIntakeTransactionalWorker",
    id: "jpapaymentintaketransactionalworker"
  },
  {
    key: "jpa-debtor-intake-worker",
    label: "JpaDebtorIntakeTransactionalWorker",
    id: "jpadebtorintaketransactionalworker"
  },
  {
    key: "jpa-debt-intake-worker",
    label: "JpaDebtIntakeTransactionalWorker",
    id: "jpadebtintaketransactionalworker"
  },
  {
    key: "springdata-payment-repository",
    label: "SpringDataPaymentRepository",
    id: "bootstrap_src_test_java_com_pipelinepro_bootstrap_allocationconcurrencyhardeningbootintegrationtest_java_springdatapaymentrepository"
  },
  {
    key: "springdata-debt-repository",
    label: "SpringDataDebtRepository",
    id: "bootstrap_src_test_java_com_pipelinepro_bootstrap_allocationconcurrencyhardeningbootintegrationtest_java_springdatadebtrepository"
  },
  {
    key: "springdata-debtor-repository",
    label: "SpringDataDebtorRepository",
    id: "bootstrap_src_test_java_com_pipelinepro_bootstrap_allocationconcurrencyhardeningbootintegrationtest_java_springdatadebtorrepository"
  },
  {
    key: "springdata-allocation-proposal-repository",
    label: "SpringDataAllocationProposalRepository",
    id: "bootstrap_src_test_java_com_pipelinepro_bootstrap_allocationconcurrencyhardeningbootintegrationtest_java_springdataallocationproposalrepository"
  },
  {
    key: "springdata-payment-allocation-repository",
    label: "SpringDataPaymentAllocationRepository",
    id: "bootstrap_src_test_java_com_pipelinepro_bootstrap_allocationconcurrencyhardeningbootintegrationtest_java_springdatapaymentallocationrepository"
  }
];

const EMBEDDED_SELECTOR_SOURCE_OVERRIDES = {
  "payment-repository-port": "domain/src/main/java/com/pipelinepro/domain/port/out/PaymentRepository.java",
  "debt-repository-port": "domain/src/main/java/com/pipelinepro/domain/port/out/DebtRepository.java",
  "debtor-repository-port": "domain/src/main/java/com/pipelinepro/domain/port/out/DebtorRepository.java",
  "allocation-proposal-repository-port":
    "domain/src/main/java/com/pipelinepro/domain/port/out/AllocationProposalRepository.java",
  "payment-allocation-repository-port":
    "domain/src/main/java/com/pipelinepro/domain/port/out/PaymentAllocationRepository.java",
  "allocation-worker-port": "domain/src/main/java/com/pipelinepro/domain/port/out/AllocationTransactionalWorker.java",
  "audit-event-gateway-port": "domain/src/main/java/com/pipelinepro/domain/port/out/AuditEventGateway.java",
  "jpa-payment-intake-worker":
    "adapter-out/src/main/java/com/pipelinepro/adapter/out/persistence/JpaPaymentIntakeTransactionalWorker.java",
  "jpa-debtor-intake-worker":
    "adapter-out/src/main/java/com/pipelinepro/adapter/out/persistence/JpaDebtorIntakeTransactionalWorker.java",
  "jpa-debt-intake-worker":
    "adapter-out/src/main/java/com/pipelinepro/adapter/out/persistence/JpaDebtIntakeTransactionalWorker.java",
  "springdata-payment-repository":
    "adapter-out/src/main/java/com/pipelinepro/adapter/out/persistence/repository/SpringDataPaymentRepository.java",
  "springdata-debt-repository":
    "adapter-out/src/main/java/com/pipelinepro/adapter/out/persistence/repository/SpringDataDebtRepository.java",
  "springdata-debtor-repository":
    "adapter-out/src/main/java/com/pipelinepro/adapter/out/persistence/repository/SpringDataDebtorRepository.java",
  "springdata-allocation-proposal-repository":
    "adapter-out/src/main/java/com/pipelinepro/adapter/out/persistence/repository/SpringDataAllocationProposalRepository.java",
  "springdata-payment-allocation-repository":
    "adapter-out/src/main/java/com/pipelinepro/adapter/out/persistence/repository/SpringDataPaymentAllocationRepository.java"
};

const EMBEDDED_RELATION_SELECTORS = [
  ["payment-controller", "receive-payment-usecase", "calls", "EXTRACTED"],
  ["payment-controller", "query-payment-usecase", "calls", "EXTRACTED"],
  ["payment-controller", "query-debt-usecase", "calls", "EXTRACTED"],
  ["payment-controller", "query-debtor-usecase", "calls", "EXTRACTED"],
  ["allocation-proposal-controller", "proposal-lifecycle-usecase", "calls", "EXTRACTED"],
  ["allocation-proposal-controller", "execute-allocation-usecase", "calls", "EXTRACTED"],
  ["allocation-proposal-controller", "get-allocation-proposal-details-usecase", "calls", "EXTRACTED"],
  ["allocation-proposal-controller", "query-payment-usecase", "calls", "INFERRED"],
  ["proposal-web-mapper", "allocation-proposal-controller", "references", "EXTRACTED"],

  ["payment-matching-app-service", "match-payment-usecase", "implements", "EXTRACTED"],
  ["payment-matching-app-service", "payment", "calls", "EXTRACTED"],
  ["payment-matching-app-service", "debt", "calls", "EXTRACTED"],
  ["payment-matching-app-service", "debtor", "calls", "EXTRACTED"],
  ["payment-matching-app-service", "payment-repository-port", "calls", "EXTRACTED"],
  ["payment-matching-app-service", "debt-repository-port", "calls", "EXTRACTED"],
  ["payment-matching-app-service", "debtor-repository-port", "calls", "INFERRED"],
  ["payment-matching-app-service", "allocation-proposal-repository-port", "calls", "EXTRACTED"],

  ["proposal-lifecycle-app-service", "proposal-lifecycle-usecase", "implements", "EXTRACTED"],
  ["proposal-lifecycle-app-service", "allocation-proposal", "calls", "EXTRACTED"],
  ["proposal-lifecycle-app-service", "allocation-proposal-repository-port", "calls", "EXTRACTED"],
  ["proposal-lifecycle-app-service", "payment-allocation", "calls", "INFERRED"],

  ["allocation-exec-app-service", "execute-allocation-usecase", "implements", "EXTRACTED"],
  ["allocation-exec-app-service", "allocation-worker-port", "calls", "EXTRACTED"],
  ["allocation-exec-app-service", "audit-event-gateway-port", "calls", "EXTRACTED"],
  ["allocation-exec-app-service", "allocation-proposal", "calls", "EXTRACTED"],
  ["allocation-exec-app-service", "payment-allocation", "calls", "EXTRACTED"],

  ["query-app-service", "query-payment-usecase", "implements", "EXTRACTED"],
  ["query-app-service", "query-debt-usecase", "implements", "EXTRACTED"],
  ["query-app-service", "query-debtor-usecase", "implements", "EXTRACTED"],
  ["query-app-service", "get-allocation-proposal-details-usecase", "implements", "EXTRACTED"],
  ["query-app-service", "payment-repository-port", "calls", "INFERRED"],
  ["query-app-service", "debt-repository-port", "calls", "INFERRED"],
  ["query-app-service", "debtor-repository-port", "calls", "INFERRED"],
  ["query-app-service", "allocation-proposal-repository-port", "calls", "EXTRACTED"],

  ["application-service-config", "payment-matching-app-service", "imports", "EXTRACTED"],
  ["application-service-config", "proposal-lifecycle-app-service", "imports", "EXTRACTED"],
  ["application-service-config", "allocation-exec-app-service", "imports", "EXTRACTED"],
  ["application-service-config", "query-app-service", "imports", "EXTRACTED"],
  ["application-service-config", "allocation-worker-port", "imports", "EXTRACTED"],
  ["application-service-config", "payment-repository-port", "imports", "EXTRACTED"],
  ["application-service-config", "debt-repository-port", "imports", "EXTRACTED"],
  ["application-service-config", "debtor-repository-port", "imports", "EXTRACTED"],
  ["application-service-config", "allocation-proposal-repository-port", "imports", "EXTRACTED"],
  ["application-service-config", "payment-allocation-repository-port", "imports", "EXTRACTED"],

  ["jpa-payment-intake-worker", "payment-repository-port", "implements", "EXTRACTED"],
  ["jpa-payment-intake-worker", "payment", "calls", "INFERRED"],
  ["jpa-debtor-intake-worker", "debtor-repository-port", "implements", "EXTRACTED"],
  ["jpa-debtor-intake-worker", "debtor", "calls", "INFERRED"],
  ["jpa-debt-intake-worker", "debt-repository-port", "implements", "EXTRACTED"],
  ["jpa-debt-intake-worker", "debt", "calls", "INFERRED"],

  ["springdata-payment-repository", "payment-repository-port", "implements", "EXTRACTED"],
  ["springdata-payment-repository", "payment", "references", "EXTRACTED"],
  ["springdata-debt-repository", "debt-repository-port", "implements", "EXTRACTED"],
  ["springdata-debt-repository", "debt", "references", "EXTRACTED"],
  ["springdata-debtor-repository", "debtor-repository-port", "implements", "EXTRACTED"],
  ["springdata-debtor-repository", "debtor", "references", "EXTRACTED"],
  ["springdata-allocation-proposal-repository", "allocation-proposal-repository-port", "implements", "EXTRACTED"],
  ["springdata-allocation-proposal-repository", "allocation-proposal", "references", "EXTRACTED"],
  ["springdata-payment-allocation-repository", "payment-allocation-repository-port", "implements", "EXTRACTED"],
  ["springdata-payment-allocation-repository", "payment-allocation", "references", "EXTRACTED"]
];

const state = {
  rawNodes: [],
  rawLinks: [],
  nodeMap: new Map(),
  outboundLinks: new Map(),
  classToFile: new Map(),
  fileToPrimaryClass: new Map(),
  adjacency: new Map(),
  degrees: new Map(),

  selectedNodes: [],
  selectedNodeMap: new Map(),
  selectedByLabel: new Map(),
  architectureEdges: [],

  relationTypes: [],
  relationFilters: new Set(),
  layerFilters: new Set(PRIMARY_LAYERS),

  visibleNodes: [],
  visibleEdges: [],
  layoutByNodeId: new Map(),
  selectedNodeId: null,

  transform: { x: 0, y: 0, k: 1 },
  dragging: false,
  dragPointer: null,

  graphMode: GRAPH_MODE_DEFAULT,
  loadedUrl: "",
  missingMandatoryLabels: [],
  loadDiagnostics: []
};

const dom = {};

async function init() {
  cacheDom();
  bindControls();
  bindGraphNavigation();
  state.graphMode = resolveGraphMode();

  try {
    const graph =
      state.graphMode === GRAPH_MODE_REMOTE
        ? await loadGraphJson()
        : buildEmbeddedGraphFromSelectors();

    clearGraphError();
    prepareGraphIndexes(graph);
    buildArchitectureSelection();
    buildArchitectureEdges();
    renderLayerFilters();
    renderRelationFilters();
    updateStats();
    updateSnapshotNote();
    removeStaticSnapshot();
    applyFiltersAndRender();
  } catch (error) {
    const fallbackMessage = buildGraphLoadDiagnosticMessage(error);
    showGraphError(fallbackMessage);

    const fallbackGraph = buildEmbeddedGraphFromSelectors();
    prepareGraphIndexes(fallbackGraph);
    buildArchitectureSelection();
    buildArchitectureEdges();
    renderLayerFilters();
    renderRelationFilters();
    updateStats();
    updateSnapshotNote();
    removeStaticSnapshot();
    applyFiltersAndRender();
    updateSelectionSummary("Remote mode failed. Embedded Graphify snapshot is displayed.");
  } finally {
    showLoading(false);
  }
}

function resolveGraphMode() {
  const params = new URLSearchParams(window.location.search);
  const requestedMode = String(params.get("graphMode") || params.get("graphSource") || "").trim().toLowerCase();
  return requestedMode === GRAPH_MODE_REMOTE ? GRAPH_MODE_REMOTE : GRAPH_MODE_DEFAULT;
}

function cacheDom() {
  dom.graphSvg = document.getElementById("graph-svg");
  dom.viewport = document.getElementById("viewport");
  dom.layerZones = document.getElementById("layer-zones");
  dom.layerFlows = document.getElementById("layer-flows");
  dom.edgeLayer = document.getElementById("edge-layer");
  dom.nodeLayer = document.getElementById("node-layer");

  dom.loading = document.getElementById("graph-loading");
  dom.error = document.getElementById("graph-error");

  dom.searchForm = document.getElementById("search-form");
  dom.searchInput = document.getElementById("search-input");
  dom.searchFeedback = document.getElementById("search-feedback");
  dom.nodeSuggestions = document.getElementById("node-suggestions");
  dom.resetButton = document.getElementById("reset-btn");

  dom.layerFilters = document.getElementById("layer-filters");
  dom.relationFilters = document.getElementById("relation-filters");
  dom.selectionSummary = document.getElementById("selection-summary");

  dom.detailsEmpty = document.getElementById("details-empty");
  dom.detailsList = document.getElementById("details-list");
  dom.detailLabel = document.getElementById("detail-label");
  dom.detailLayer = document.getElementById("detail-layer");
  dom.detailSource = document.getElementById("detail-source");
  dom.detailLine = document.getElementById("detail-line");
  dom.detailCommunity = document.getElementById("detail-community");
  dom.detailDegree = document.getElementById("detail-degree");
  dom.neighborList = document.getElementById("neighbor-list");

  dom.statNodes = document.getElementById("stat-nodes");
  dom.statEdges = document.getElementById("stat-edges");
  dom.statCommunities = document.getElementById("stat-communities");
  dom.statExtracted = document.getElementById("stat-extracted");
  dom.statInferred = document.getElementById("stat-inferred");
  dom.statsNote = document.getElementById("stats-note");
  dom.snapshotNote = document.getElementById("snapshot-note");
}

function bindControls() {
  dom.searchForm.addEventListener("submit", onSearchSubmit);
  dom.resetButton.addEventListener("click", onResetClick);
}

function bindGraphNavigation() {
  dom.graphSvg.addEventListener("wheel", onWheelZoom, { passive: false });
  dom.graphSvg.addEventListener("pointerdown", onPointerDown);
  window.addEventListener("pointermove", onPointerMove);
  window.addEventListener("pointerup", onPointerUp);
  dom.graphSvg.addEventListener("keydown", onGraphKeyboardNav);
}

async function loadGraphJson() {
  const candidateUrls = resolveGraphJsonCandidates();
  const diagnostics = [];

  for (const candidateUrl of candidateUrls) {
    const startedAt = performance.now();

    try {
      const response = await fetchWithTimeout(candidateUrl, GRAPH_FETCH_TIMEOUT_MS, { cache: "no-store" });
      if (!response.ok) {
        const errorBody = await safeReadErrorBody(response);
        const bodyDetail = errorBody ? ` · ${truncate(errorBody, 220)}` : "";
        throw new Error(`HTTP ${response.status} ${response.statusText}${bodyDetail}`.trim());
      }

      const json = await response.json();
      const links = Array.isArray(json.links) ? json.links : json.edges;

      if (!Array.isArray(json.nodes) || !Array.isArray(links)) {
        throw new Error("Structure JSON inattendue");
      }

      const elapsedMs = Math.round(performance.now() - startedAt);
      state.loadedUrl = candidateUrl;
      state.loadDiagnostics = [`OK · ${candidateUrl} · ${elapsedMs} ms`];
      return {
        nodes: json.nodes,
        links
      };
    } catch (error) {
      const elapsedMs = Math.round(performance.now() - startedAt);
      diagnostics.push({
        url: candidateUrl,
        elapsedMs,
        reason: describeFetchError(error, GRAPH_FETCH_TIMEOUT_MS)
      });
    }
  }

  const aggregateError = new Error("Chargement impossible");
  aggregateError.name = "GraphLoadError";
  aggregateError.diagnostics = diagnostics;
  aggregateError.attemptedUrls = candidateUrls;
  state.loadDiagnostics = diagnostics.map((entry) => `ECHEC · ${entry.url} · ${entry.reason} · ${entry.elapsedMs} ms`);
  throw aggregateError;
}

function buildEmbeddedGraphFromSelectors() {
  const nodes = NODE_SELECTORS.map((selector, index) => {
    const sourceFile = resolveEmbeddedSourceFile(selector);
    return {
      id: selector.id,
      label: selector.label,
      source_file: sourceFile,
      source_location: "L1",
      community: `embedded-${inferEmbeddedLayer(selector, sourceFile)}`,
      order: index
    };
  });

  const idByKey = new Map(NODE_SELECTORS.map((selector) => [selector.key, selector.id]));
  const links = EMBEDDED_RELATION_SELECTORS.map(([sourceKey, targetKey, relation, confidence]) => ({
    source: idByKey.get(sourceKey),
    target: idByKey.get(targetKey),
    relation,
    confidence
  })).filter((link) => Boolean(link.source && link.target));

  state.loadedUrl = `embedded://${EMBEDDED_GRAPH_SOURCE}`;
  state.loadDiagnostics = [
    "Embedded snapshot loaded synchronously (no runtime fetch).",
    `Generated from NODE_SELECTORS and architecture rules, source baseline: ${EMBEDDED_GRAPH_SOURCE}`
  ];

  return {
    nodes,
    links
  };
}

function resolveEmbeddedSourceFile(selector) {
  if (selector.sourceIncludes) {
    return selector.sourceIncludes;
  }

  if (EMBEDDED_SELECTOR_SOURCE_OVERRIDES[selector.key]) {
    return EMBEDDED_SELECTOR_SOURCE_OVERRIDES[selector.key];
  }

  return `embedded/${selector.key}.java`;
}

function inferEmbeddedLayer(selector, sourceFile) {
  const selectorId = String(selector.id || "");
  const syntheticNode = {
    id: selectorId,
    label: selector.label,
    source_file: sourceFile
  };
  return inferLayer(syntheticNode);
}

function resolveGraphJsonCandidates() {
  const candidates = [];
  const seen = new Set();

  const tryPush = (value) => {
    if (!value) {
      return;
    }
    const normalized = String(value);
    if (!seen.has(normalized)) {
      seen.add(normalized);
      candidates.push(normalized);
    }
  };

  for (const candidatePath of GRAPH_URL_CANDIDATE_PATHS) {
    tryPush(new URL(candidatePath, import.meta.url).href);
  }

  for (const candidatePath of GRAPH_URL_CANDIDATE_PATHS) {
    try {
      tryPush(new URL(candidatePath, window.location.href).href);
    } catch {
      // Ignore malformed document URL edge cases.
    }
  }

  return candidates;
}

async function fetchWithTimeout(url, timeoutMs, options = {}) {
  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => {
    controller.abort(`Timeout ${timeoutMs}ms`);
  }, timeoutMs);

  try {
    return await fetch(url, {
      ...options,
      signal: controller.signal
    });
  } finally {
    window.clearTimeout(timeoutId);
  }
}

async function safeReadErrorBody(response) {
  try {
    return await response.text();
  } catch {
    return "";
  }
}

function describeFetchError(error, timeoutMs) {
  if (!error) {
    return "Erreur inconnue";
  }

  if (error.name === "AbortError") {
    return `Timeout après ${timeoutMs} ms`;
  }

  const message = String(error.message || error);

  if (message.includes("Failed to fetch")) {
    return "Échec réseau (fetch impossible depuis le navigateur)";
  }

  return message;
}

function buildGraphLoadDiagnosticMessage(error) {
  const diagnostics = Array.isArray(error?.diagnostics) ? error.diagnostics : [];
  const attempted = diagnostics.length > 0
    ? diagnostics.map((entry) => `- ${entry.url} → ${entry.reason} (${entry.elapsedMs} ms)`).join("\n")
    : `- ${resolveGraphJsonCandidates().join("\n- ")}`;

  const onlineStatus = navigator.onLine ? "en ligne" : "hors ligne";

  return [
    "Impossible de charger le snapshot Graphify distant (graphify-out/graph.json).",
    `État réseau navigateur: ${onlineStatus}.`,
    `URL(s) tentée(s):\n${attempted}`,
    "Le mode local reste disponible : snapshot Graphify embarqué."
  ].join("\n\n");
}

function prepareGraphIndexes(graph) {
  state.rawNodes = graph.nodes
    .map((node) => ({
      ...node,
      id: normalizeEndpoint(node.id || node.label),
      label: String(node.label ?? node.id ?? "Nœud inconnu")
    }))
    .filter((node) => node.id);

  state.nodeMap = new Map(state.rawNodes.map((node) => [node.id, node]));

  state.rawLinks = graph.links
    .map((link, index) => ({
      index,
      relation: normalizeRelation(link.relation),
      confidence: normalizeConfidence(link.confidence),
      sourceId: normalizeEndpoint(link.source),
      targetId: normalizeEndpoint(link.target)
    }))
    .filter((link) => link.sourceId && link.targetId);

  state.outboundLinks = new Map();
  for (const link of state.rawLinks) {
    if (!state.outboundLinks.has(link.sourceId)) {
      state.outboundLinks.set(link.sourceId, []);
    }
    state.outboundLinks.get(link.sourceId).push(link);
  }

  buildContainmentIndexes();
  buildAdjacencyIndexes();
}

function buildContainmentIndexes() {
  state.classToFile = new Map();
  state.fileToPrimaryClass = new Map();

  for (const link of state.rawLinks) {
    if (link.relation !== "contains") {
      continue;
    }

    const sourceNode = state.nodeMap.get(link.sourceId);
    const targetNode = state.nodeMap.get(link.targetId);

    if (!sourceNode || !targetNode) {
      continue;
    }

    if (!isFileNode(sourceNode) || isFileNode(targetNode)) {
      continue;
    }

    state.classToFile.set(targetNode.id, sourceNode.id);

    if (!state.fileToPrimaryClass.has(sourceNode.id) && isClassLikeLabel(targetNode.label)) {
      state.fileToPrimaryClass.set(sourceNode.id, targetNode.id);
    }
  }
}

function buildAdjacencyIndexes() {
  state.adjacency = new Map();

  for (const link of state.rawLinks) {
    appendNeighbor(link.sourceId, {
      neighborId: link.targetId,
      relation: link.relation,
      confidence: link.confidence,
      direction: "out"
    });

    appendNeighbor(link.targetId, {
      neighborId: link.sourceId,
      relation: link.relation,
      confidence: link.confidence,
      direction: "in"
    });
  }

  state.degrees = new Map();
  for (const [nodeId, entries] of state.adjacency.entries()) {
    state.degrees.set(nodeId, new Set(entries.map((entry) => entry.neighborId)).size);
  }
}

function appendNeighbor(nodeId, entry) {
  if (!state.adjacency.has(nodeId)) {
    state.adjacency.set(nodeId, []);
  }
  state.adjacency.get(nodeId).push(entry);
}

function buildArchitectureSelection() {
  const selected = [];
  const usedIds = new Set();
  const missingMandatory = [];

  for (const selector of NODE_SELECTORS) {
    const node = resolveNodeSelector(selector);

    if (!node) {
      if (selector.mandatory) {
        missingMandatory.push(selector.label);
      }
      continue;
    }

    if (usedIds.has(node.id)) {
      continue;
    }

    usedIds.add(node.id);
    selected.push({
      ...node,
      layer: inferLayer(node),
      selectorKey: selector.key
    });
  }

  selected.sort((a, b) => {
    const layerDelta = LAYER_ORDER.indexOf(a.layer) - LAYER_ORDER.indexOf(b.layer);
    if (layerDelta !== 0) {
      return layerDelta;
    }
    return a.label.localeCompare(b.label, "fr", { sensitivity: "base" });
  });

  state.selectedNodes = selected;
  state.selectedNodeMap = new Map(selected.map((node) => [node.id, node]));
  state.selectedByLabel = new Map();

  for (const node of selected) {
    if (!state.selectedByLabel.has(node.label)) {
      state.selectedByLabel.set(node.label, node.id);
    }
  }

  state.missingMandatoryLabels = missingMandatory;
}

function resolveNodeSelector(selector) {
  if (selector.id && state.nodeMap.has(selector.id)) {
    return state.nodeMap.get(selector.id);
  }

  const candidates = state.rawNodes.filter((node) => node.label === selector.label);
  if (candidates.length === 0) {
    return null;
  }

  const best = [...candidates]
    .map((node) => ({
      node,
      score: scoreNodeCandidate(node, selector)
    }))
    .sort((a, b) => b.score - a.score)[0];

  return best?.node ?? null;
}

function scoreNodeCandidate(node, selector) {
  const source = String(node.source_file || "");
  let score = 0;

  if (selector.id && node.id === selector.id) {
    score += 200;
  }

  if (selector.sourceIncludes && source.includes(selector.sourceIncludes)) {
    score += 120;
  }

  if (source.includes("/src/main/")) {
    score += 35;
  }

  if (source.includes("/src/test/")) {
    score -= 35;
  }

  if (source) {
    score += 10;
  } else {
    score += 4;
  }

  return score;
}

function buildArchitectureEdges() {
  const selectedIds = new Set(state.selectedNodes.map((node) => node.id));
  const edgesByKey = new Map();

  const addEdge = (sourceId, targetId, relation, confidence, derived = false) => {
    if (!selectedIds.has(sourceId) || !selectedIds.has(targetId) || sourceId === targetId) {
      return;
    }

    const normalizedRelation = normalizeRelation(relation);
    const normalizedConfidence = normalizeConfidence(confidence);
    const key = `${sourceId}|${targetId}|${normalizedRelation}|${normalizedConfidence}|${derived ? 1 : 0}`;

    if (edgesByKey.has(key)) {
      return;
    }

    edgesByKey.set(key, {
      id: key,
      source: sourceId,
      target: targetId,
      relation: normalizedRelation,
      confidence: normalizedConfidence,
      derived
    });
  };

  for (const link of state.rawLinks) {
    const sourceId = remapToSelectedNode(link.sourceId, selectedIds);
    const targetId = remapToSelectedNode(link.targetId, selectedIds);
    if (sourceId && targetId) {
      addEdge(sourceId, targetId, link.relation, link.confidence, false);
    }
  }

  for (const node of state.selectedNodes) {
    const fileId = state.classToFile.get(node.id);
    if (!fileId) {
      continue;
    }

    const fileLinks = state.outboundLinks.get(fileId) ?? [];
    for (const link of fileLinks) {
      const targetId = remapToSelectedNode(link.targetId, selectedIds);
      if (targetId) {
        addEdge(node.id, targetId, link.relation, link.confidence, true);
      }
    }
  }

  state.architectureEdges = [...edgesByKey.values()];
  state.relationTypes = [...new Set(state.architectureEdges.map((edge) => edge.relation))].sort();
  state.relationFilters = new Set(state.relationTypes);
}

function remapToSelectedNode(rawId, selectedIds) {
  if (!rawId) {
    return null;
  }

  if (selectedIds.has(rawId)) {
    return rawId;
  }

  const classFromFile = state.fileToPrimaryClass.get(rawId);
  if (classFromFile && selectedIds.has(classFromFile)) {
    return classFromFile;
  }

  const rawNode = state.nodeMap.get(rawId);
  if (!rawNode) {
    return null;
  }

  const mappedByLabel = state.selectedByLabel.get(rawNode.label);
  if (mappedByLabel && selectedIds.has(mappedByLabel)) {
    return mappedByLabel;
  }

  return null;
}

function renderLayerFilters() {
  dom.layerFilters.replaceChildren();

  const layerCounts = new Map();
  for (const node of state.selectedNodes) {
    layerCounts.set(node.layer, (layerCounts.get(node.layer) || 0) + 1);
  }

  const layersToRender = [...PRIMARY_LAYERS];
  if ((layerCounts.get("other") || 0) > 0) {
    layersToRender.push("other");
  }

  state.layerFilters = new Set(layersToRender.filter((layer) => (layerCounts.get(layer) || 0) > 0));

  for (const layer of layersToRender) {
    const count = layerCounts.get(layer) || 0;
    const checked = state.layerFilters.has(layer);
    const chip = createFilterChip(`layer-${layer}`, `${LAYER_LABELS[layer]} (${count})`, checked, layer, "layer");
    dom.layerFilters.append(chip);
  }
}

function renderRelationFilters() {
  dom.relationFilters.replaceChildren();

  for (const relation of state.relationTypes) {
    const label = `${humanizeRelation(relation)} (${countEdgesByRelation(relation)})`;
    const chip = createFilterChip(
      `relation-${relation}`,
      label,
      state.relationFilters.has(relation),
      relation,
      "relation"
    );
    dom.relationFilters.append(chip);
  }
}

function createFilterChip(inputId, text, checked, value, type) {
  const wrapper = document.createElement("label");
  wrapper.className = "filter-chip";

  const input = document.createElement("input");
  input.type = "checkbox";
  input.id = inputId;
  input.value = value;
  input.checked = checked;
  input.dataset.filterType = type;
  input.addEventListener("change", onFilterChanged);

  const span = document.createElement("span");
  span.textContent = text;

  wrapper.append(input, span);
  return wrapper;
}

function onFilterChanged() {
  const layerChecked = dom.layerFilters.querySelectorAll('input[type="checkbox"]:checked');
  state.layerFilters = new Set([...layerChecked].map((input) => input.value));

  const relationChecked = dom.relationFilters.querySelectorAll('input[type="checkbox"]:checked');
  state.relationFilters = new Set([...relationChecked].map((input) => input.value));

  applyFiltersAndRender();
}

function applyFiltersAndRender() {
  const visibleNodes = state.selectedNodes.filter((node) => state.layerFilters.has(node.layer));
  const visibleNodeIds = new Set(visibleNodes.map((node) => node.id));

  const visibleEdges = state.architectureEdges.filter(
    (edge) =>
      visibleNodeIds.has(edge.source) &&
      visibleNodeIds.has(edge.target) &&
      state.relationFilters.has(edge.relation)
  );

  state.visibleNodes = visibleNodes;
  state.visibleEdges = visibleEdges;
  state.layoutByNodeId = computeNodeLayout(visibleNodes);

  renderGraph();
  renderDetails();
  updateSelectionSummary();
  refreshSearchSuggestions();
}

function computeNodeLayout(visibleNodes) {
  const layout = new Map();

  const laneCount = PRIMARY_LAYERS.length;
  const innerWidth = CANVAS.width - CANVAS.margin.left - CANVAS.margin.right;
  const laneWidth = innerWidth / laneCount;
  const top = CANVAS.margin.top;
  const usableHeight = CANVAS.height - CANVAS.margin.top - CANVAS.margin.bottom;

  const grouped = new Map();
  for (const layer of LAYER_ORDER) {
    grouped.set(layer, []);
  }

  for (const node of visibleNodes) {
    grouped.get(node.layer).push(node);
  }

  for (const layer of LAYER_ORDER) {
    const nodes = grouped.get(layer);
    if (!nodes || nodes.length === 0) {
      continue;
    }

    nodes.sort((a, b) => a.label.localeCompare(b.label, "fr", { sensitivity: "base" }));

    let laneIndex = PRIMARY_LAYERS.indexOf(layer);
    if (laneIndex < 0) {
      laneIndex = laneCount - 1;
    }

    const laneCenterX = CANVAS.margin.left + laneIndex * laneWidth + laneWidth / 2;
    const spacingY = usableHeight / (nodes.length + 1);

    nodes.forEach((node, index) => {
      const width = clamp(170, 350, 120 + node.label.length * 6);
      const height = 40;
      const y = top + spacingY * (index + 1);

      layout.set(node.id, {
        x: laneCenterX,
        y,
        width,
        height,
        layer: node.layer
      });
    });
  }

  return layout;
}

function renderGraph() {
  dom.layerZones.replaceChildren();
  dom.layerFlows.replaceChildren();
  dom.edgeLayer.replaceChildren();
  dom.nodeLayer.replaceChildren();

  renderLayerZones();
  renderLayerFlows();
  renderEdges();
  renderNodes();
  updateViewportTransform();
}

function renderLayerZones() {
  const laneCount = PRIMARY_LAYERS.length;
  const innerWidth = CANVAS.width - CANVAS.margin.left - CANVAS.margin.right;
  const laneWidth = innerWidth / laneCount;

  const zoneY = CANVAS.margin.top - 32;
  const zoneHeight = CANVAS.height - zoneY - CANVAS.margin.bottom + 16;

  PRIMARY_LAYERS.forEach((layer, index) => {
    const x = CANVAS.margin.left + index * laneWidth + 8;

    const zoneGroup = document.createElementNS("http://www.w3.org/2000/svg", "g");
    if (!state.layerFilters.has(layer)) {
      zoneGroup.classList.add("zone-muted");
    }

    const rect = document.createElementNS("http://www.w3.org/2000/svg", "rect");
    rect.setAttribute("x", String(x));
    rect.setAttribute("y", String(zoneY));
    rect.setAttribute("width", String(laneWidth - 16));
    rect.setAttribute("height", String(zoneHeight));
    rect.setAttribute("rx", "12");
    rect.setAttribute("ry", "12");
    rect.setAttribute("class", `zone-rect zone-${layer}`);

    const label = document.createElementNS("http://www.w3.org/2000/svg", "text");
    label.setAttribute("x", String(x + 14));
    label.setAttribute("y", String(CANVAS.margin.top - 44));
    label.setAttribute("class", "zone-label");
    label.textContent = layer;

    zoneGroup.append(rect, label);
    dom.layerZones.append(zoneGroup);
  });
}

function renderLayerFlows() {
  const laneCount = PRIMARY_LAYERS.length;
  const innerWidth = CANVAS.width - CANVAS.margin.left - CANVAS.margin.right;
  const laneWidth = innerWidth / laneCount;

  const layerCounts = new Map();
  for (const edge of state.visibleEdges) {
    const sourceNode = state.selectedNodeMap.get(edge.source);
    const targetNode = state.selectedNodeMap.get(edge.target);
    if (!sourceNode || !targetNode) {
      continue;
    }

    if (!PRIMARY_LAYERS.includes(sourceNode.layer) || !PRIMARY_LAYERS.includes(targetNode.layer)) {
      continue;
    }

    if (sourceNode.layer === targetNode.layer) {
      continue;
    }

    const key = `${sourceNode.layer}|${targetNode.layer}`;
    layerCounts.set(key, (layerCounts.get(key) || 0) + 1);
  }

  for (const [key, count] of layerCounts.entries()) {
    const [sourceLayer, targetLayer] = key.split("|");
    const sourceIndex = PRIMARY_LAYERS.indexOf(sourceLayer);
    const targetIndex = PRIMARY_LAYERS.indexOf(targetLayer);

    const x1 = CANVAS.margin.left + sourceIndex * laneWidth + laneWidth / 2;
    const x2 = CANVAS.margin.left + targetIndex * laneWidth + laneWidth / 2;
    const y = CANVAS.margin.top - 74;

    const path = document.createElementNS("http://www.w3.org/2000/svg", "path");
    const midX = (x1 + x2) / 2;
    const arcHeight = Math.max(10, Math.min(42, Math.abs(x2 - x1) / 5));
    path.setAttribute("class", "layer-flow");
    path.setAttribute("d", `M ${x1} ${y} Q ${midX} ${y - arcHeight} ${x2} ${y}`);

    const label = document.createElementNS("http://www.w3.org/2000/svg", "text");
    label.setAttribute("x", String(midX));
    label.setAttribute("y", String(y - arcHeight - 4));
    label.setAttribute("text-anchor", "middle");
    label.setAttribute("class", "layer-flow-label");
    label.textContent = `${sourceLayer} → ${targetLayer} (${count})`;

    dom.layerFlows.append(path, label);
  }
}

function renderEdges() {
  for (const edge of state.visibleEdges) {
    const sourcePos = state.layoutByNodeId.get(edge.source);
    const targetPos = state.layoutByNodeId.get(edge.target);
    if (!sourcePos || !targetPos) {
      continue;
    }

    const start = computeAnchor(sourcePos, targetPos);
    const end = computeAnchor(targetPos, sourcePos);

    const pathData = buildCurvePath(start, end);
    const strokeColor = RELATION_COLORS[edge.relation] || "#64748b";

    const path = document.createElementNS("http://www.w3.org/2000/svg", "path");
    path.setAttribute("d", pathData);
    path.setAttribute("class", `graph-edge${edge.confidence === "INFERRED" ? " is-inferred" : ""}`);
    path.setAttribute("stroke", strokeColor);
    path.setAttribute("color", strokeColor);

    const sourceLabel = state.selectedNodeMap.get(edge.source)?.label || edge.source;
    const targetLabel = state.selectedNodeMap.get(edge.target)?.label || edge.target;

    const title = document.createElementNS("http://www.w3.org/2000/svg", "title");
    title.textContent = `${sourceLabel} → ${targetLabel} · ${edge.relation} · ${edge.confidence}${
      edge.derived ? " · dérivé via fichier" : ""
    }`;

    path.append(title);
    dom.edgeLayer.append(path);
  }
}

function renderNodes() {
  const sortedNodes = [...state.visibleNodes].sort((a, b) => {
    const posA = state.layoutByNodeId.get(a.id);
    const posB = state.layoutByNodeId.get(b.id);
    if (!posA || !posB) {
      return 0;
    }
    return posA.y - posB.y;
  });

  for (const node of sortedNodes) {
    const pos = state.layoutByNodeId.get(node.id);
    if (!pos) {
      continue;
    }

    const group = document.createElementNS("http://www.w3.org/2000/svg", "g");
    group.setAttribute("transform", `translate(${pos.x} ${pos.y})`);
    group.setAttribute("class", `graph-node layer-${node.layer}${state.selectedNodeId === node.id ? " is-selected" : ""}`);
    group.setAttribute("role", "button");
    group.setAttribute("tabindex", "0");
    group.setAttribute("aria-label", `${node.label}, couche ${node.layer}`);

    const rect = document.createElementNS("http://www.w3.org/2000/svg", "rect");
    rect.setAttribute("x", String(-pos.width / 2));
    rect.setAttribute("y", String(-pos.height / 2));
    rect.setAttribute("width", String(pos.width));
    rect.setAttribute("height", String(pos.height));
    rect.setAttribute("rx", "9");
    rect.setAttribute("ry", "9");

    const text = document.createElementNS("http://www.w3.org/2000/svg", "text");
    text.setAttribute("text-anchor", "middle");
    text.setAttribute("dominant-baseline", "middle");
    text.textContent = truncate(node.label, 34);

    const title = document.createElementNS("http://www.w3.org/2000/svg", "title");
    title.textContent = `${node.label} (${node.layer})`;

    group.addEventListener("pointerdown", (event) => {
      event.stopPropagation();
    });

    group.addEventListener("click", (event) => {
      event.stopPropagation();
      state.selectedNodeId = node.id;
      renderGraph();
      renderDetails();
    });

    group.addEventListener("keydown", (event) => {
      if (event.key === "Enter" || event.key === " ") {
        event.preventDefault();
        state.selectedNodeId = node.id;
        renderGraph();
        renderDetails();
      }
    });

    group.append(rect, text, title);
    dom.nodeLayer.append(group);
  }
}

function renderDetails() {
  if (!state.selectedNodeId) {
    showEmptyDetails("Cliquez sur un nœud pour afficher ses métadonnées Graphify.");
    return;
  }

  const node = state.selectedNodeMap.get(state.selectedNodeId);
  if (!node) {
    showEmptyDetails("Nœud non disponible dans la sélection courante.");
    return;
  }

  dom.detailsEmpty.hidden = true;
  dom.detailsList.hidden = false;

  dom.detailLabel.textContent = node.label;
  dom.detailLayer.textContent = node.layer;
  dom.detailSource.textContent = node.source_file || "Non renseigné";
  dom.detailLine.textContent = parseLine(node.source_location);
  dom.detailCommunity.textContent = node.community ?? "Non renseignée";
  dom.detailDegree.textContent = state.degrees.get(node.id) ?? 0;

  renderNeighbors(node.id);
}

function showEmptyDetails(message) {
  dom.detailsEmpty.hidden = false;
  dom.detailsEmpty.textContent = message;
  dom.detailsList.hidden = true;
  dom.neighborList.replaceChildren();
}

function renderNeighbors(nodeId) {
  const records = state.adjacency.get(nodeId) || [];
  const grouped = new Map();

  for (const record of records) {
    const key = record.neighborId;
    if (!grouped.has(key)) {
      grouped.set(key, {
        neighborId: key,
        label: state.nodeMap.get(key)?.label || key,
        inCount: 0,
        outCount: 0,
        relations: new Set(),
        confidences: new Set(),
        total: 0
      });
    }

    const bucket = grouped.get(key);
    if (record.direction === "in") {
      bucket.inCount += 1;
    } else {
      bucket.outCount += 1;
    }

    bucket.total += 1;
    bucket.relations.add(record.relation);
    bucket.confidences.add(record.confidence);
  }

  const neighbors = [...grouped.values()]
    .sort((a, b) => b.total - a.total || a.label.localeCompare(b.label, "fr", { sensitivity: "base" }))
    .slice(0, 16);

  dom.neighborList.replaceChildren();

  if (neighbors.length === 0) {
    const item = document.createElement("li");
    item.textContent = "Aucun voisin direct trouvé pour ce nœud.";
    dom.neighborList.append(item);
    return;
  }

  for (const neighbor of neighbors) {
    const item = document.createElement("li");
    const direction =
      neighbor.inCount > 0 && neighbor.outCount > 0
        ? "entrant/sortant"
        : neighbor.inCount > 0
          ? "entrant"
          : "sortant";

    const relationList = [...neighbor.relations].join(", ");
    const confidenceList = [...neighbor.confidences].join(", ");

    const strong = document.createElement("strong");
    strong.textContent = neighbor.label;

    const text = document.createTextNode(
      ` — ${direction} · ${neighbor.total} lien(s) · ${relationList} · ${confidenceList}`
    );

    item.append(strong, text);
    dom.neighborList.append(item);
  }
}

function refreshSearchSuggestions() {
  const labels = [...new Set(state.visibleNodes.map((node) => node.label))].sort((a, b) =>
    a.localeCompare(b, "fr", { sensitivity: "base" })
  );

  dom.nodeSuggestions.replaceChildren();
  for (const label of labels) {
    const option = document.createElement("option");
    option.value = label;
    dom.nodeSuggestions.append(option);
  }
}

function onSearchSubmit(event) {
  event.preventDefault();

  const query = dom.searchInput.value.trim().toLowerCase();
  if (!query) {
    dom.searchFeedback.textContent = "Saisissez un libellé pour lancer la recherche.";
    return;
  }

  const matchedNode = state.visibleNodes.find((node) => node.label.toLowerCase().includes(query));

  if (!matchedNode) {
    dom.searchFeedback.textContent = "Aucun nœud visible ne correspond à cette recherche.";
    return;
  }

  state.selectedNodeId = matchedNode.id;
  renderGraph();
  renderDetails();
  centerOnNode(matchedNode.id);

  dom.searchFeedback.textContent = `Nœud trouvé : ${matchedNode.label}`;
}

function onResetClick() {
  dom.searchInput.value = "";
  dom.searchFeedback.textContent = "";

  for (const checkbox of dom.layerFilters.querySelectorAll('input[type="checkbox"]')) {
    checkbox.checked = true;
  }

  for (const checkbox of dom.relationFilters.querySelectorAll('input[type="checkbox"]')) {
    checkbox.checked = true;
  }

  state.layerFilters = new Set(
    [...dom.layerFilters.querySelectorAll('input[type="checkbox"]')].map((input) => input.value)
  );
  state.relationFilters = new Set(state.relationTypes);
  state.selectedNodeId = null;

  resetZoom();
  applyFiltersAndRender();
}

function onPointerDown(event) {
  if (event.target.closest(".graph-node")) {
    return;
  }

  state.dragging = true;
  state.dragPointer = { x: event.clientX, y: event.clientY };
  dom.graphSvg.classList.add("is-dragging");
}

function onPointerMove(event) {
  if (!state.dragging || !state.dragPointer) {
    return;
  }

  const scaleX = CANVAS.width / Math.max(1, dom.graphSvg.clientWidth);
  const scaleY = CANVAS.height / Math.max(1, dom.graphSvg.clientHeight);

  const deltaX = (event.clientX - state.dragPointer.x) * scaleX;
  const deltaY = (event.clientY - state.dragPointer.y) * scaleY;

  state.transform.x += deltaX;
  state.transform.y += deltaY;
  state.dragPointer = { x: event.clientX, y: event.clientY };

  updateViewportTransform();
}

function onPointerUp() {
  if (!state.dragging) {
    return;
  }

  state.dragging = false;
  state.dragPointer = null;
  dom.graphSvg.classList.remove("is-dragging");
}

function onWheelZoom(event) {
  event.preventDefault();

  const zoomFactor = event.deltaY < 0 ? 1.1 : 0.9;
  const previousScale = state.transform.k;
  const nextScale = clamp(0.45, 3.8, previousScale * zoomFactor);

  if (nextScale === previousScale) {
    return;
  }

  const point = dom.graphSvg.createSVGPoint();
  point.x = event.clientX;
  point.y = event.clientY;

  const ctm = dom.graphSvg.getScreenCTM();
  if (!ctm) {
    return;
  }

  const cursor = point.matrixTransform(ctm.inverse());

  state.transform.x = cursor.x - ((cursor.x - state.transform.x) * nextScale) / previousScale;
  state.transform.y = cursor.y - ((cursor.y - state.transform.y) * nextScale) / previousScale;
  state.transform.k = nextScale;

  updateViewportTransform();
}

function onGraphKeyboardNav(event) {
  if (event.key === "0") {
    event.preventDefault();
    resetZoom();
    return;
  }

  if (event.key === "+" || event.key === "=") {
    event.preventDefault();
    zoomByFactor(1.12);
    return;
  }

  if (event.key === "-") {
    event.preventDefault();
    zoomByFactor(0.9);
  }
}

function zoomByFactor(factor) {
  const center = {
    x: CANVAS.width / 2,
    y: CANVAS.height / 2
  };

  const previousScale = state.transform.k;
  const nextScale = clamp(0.45, 3.8, previousScale * factor);
  if (nextScale === previousScale) {
    return;
  }

  state.transform.x = center.x - ((center.x - state.transform.x) * nextScale) / previousScale;
  state.transform.y = center.y - ((center.y - state.transform.y) * nextScale) / previousScale;
  state.transform.k = nextScale;

  updateViewportTransform();
}

function centerOnNode(nodeId) {
  const position = state.layoutByNodeId.get(nodeId);
  if (!position) {
    return;
  }

  const targetScale = Math.max(1.1, state.transform.k);
  state.transform.k = targetScale;
  state.transform.x = CANVAS.width / 2 - position.x * targetScale;
  state.transform.y = CANVAS.height / 2 - position.y * targetScale;

  updateViewportTransform();
}

function resetZoom() {
  state.transform = { x: 0, y: 0, k: 1 };
  updateViewportTransform();
}

function updateViewportTransform() {
  dom.viewport.setAttribute(
    "transform",
    `translate(${state.transform.x} ${state.transform.y}) scale(${state.transform.k})`
  );
}

function updateSelectionSummary(customText) {
  if (customText) {
    dom.selectionSummary.textContent = customText;
    return;
  }

  const missingHint =
    state.missingMandatoryLabels.length > 0
      ? ` · ${state.missingMandatoryLabels.length} composant(s) obligatoire(s) absent(s) du snapshot.`
      : "";

  const summary = `${state.visibleNodes.length} nœud(s) visibles sur ${state.selectedNodes.length} sélectionnés · ${state.visibleEdges.length} relation(s) visibles${missingHint}`;
  dom.selectionSummary.textContent = summary;
}

function updateStats() {
  dom.statNodes.textContent = formatInteger(GRAPHIFY_GLOBAL_STATS.nodes);
  dom.statEdges.textContent = formatInteger(GRAPHIFY_GLOBAL_STATS.edges);
  dom.statCommunities.textContent = formatInteger(GRAPHIFY_GLOBAL_STATS.communities);
  dom.statExtracted.textContent = `${GRAPHIFY_GLOBAL_STATS.extractedRate.toFixed(1)} %`;
  dom.statInferred.textContent = `${GRAPHIFY_GLOBAL_STATS.inferredRate.toFixed(1)} %`;

  const missing =
    state.missingMandatoryLabels.length > 0
      ? `Composants obligatoires absents dans ce snapshot : ${state.missingMandatoryLabels.join(", ")}. `
      : "Tous les composants obligatoires demandés ont été trouvés dans le graphe. ";

  const sourceNote = state.loadedUrl.startsWith("embedded://")
    ? `Mode actif: snapshot Graphify embarqué (source de génération: ${EMBEDDED_GRAPH_SOURCE}).`
    : `Mode actif: snapshot Graphify distant (${state.loadedUrl}).`;

  const diagnosticNote =
    state.loadDiagnostics.length > 0
      ? ` Diagnostic: ${state.loadDiagnostics.join(" | ")}.`
      : "";

  dom.statsNote.textContent =
    `${missing}${sourceNote} Les statistiques affichent le snapshot Graphify global connu, tandis que le diagramme représente une sélection embarquée issue de NODE_SELECTORS.${diagnosticNote}`;
}

function updateStatsFallback() {
  dom.statNodes.textContent = "—";
  dom.statEdges.textContent = "—";
  dom.statCommunities.textContent = "—";
  dom.statExtracted.textContent = "—";
  dom.statInferred.textContent = "—";
  dom.statsNote.textContent = "Statistiques indisponibles tant que le fichier graph.json n'est pas chargé.";
}

function showLoading(visible) {
  dom.loading.hidden = !visible;
}

function removeStaticSnapshot() {
  if (!dom.graphSvg) {
    return;
  }

  const staticElements = dom.graphSvg.querySelectorAll("[data-static-snapshot], [data-static-note]");
  for (const element of staticElements) {
    element.remove();
  }
}

function showGraphError(message) {
  dom.error.hidden = false;
  dom.error.textContent = message;
  dom.error.classList.remove("overlay");
}

// Si un runtime error empêche init de finir, on évite d'avoir une UI vide.
window.addEventListener("error", (evt) => {
  // Ne spam pas console, on laisse un message d'erreur visible.
  if (dom.error) {
    showGraphError("Interactive rendering failed. The static snapshot may still be used if JavaScript is disabled.");
  }
});

function clearGraphError() {
  dom.error.hidden = true;
  dom.error.textContent = "";
  dom.error.classList.add("overlay");
}

function updateSnapshotNote() {
  if (!dom.snapshotNote) {
    return;
  }

  const isEmbedded = state.loadedUrl.startsWith("embedded://");
  dom.snapshotNote.textContent = isEmbedded
    ? `Mode par défaut: snapshot Graphify embarqué. Source de génération: ${EMBEDDED_GRAPH_SOURCE}.`
    : `Mode optionnel distant actif: ${state.loadedUrl}. En cas d'échec, la page bascule vers le snapshot Graphify embarqué.`;
}

function normalizeEndpoint(endpoint) {
  if (endpoint === null || endpoint === undefined) {
    return "";
  }

  if (typeof endpoint === "string" || typeof endpoint === "number") {
    return String(endpoint);
  }

  if (typeof endpoint === "object" && endpoint.id !== undefined && endpoint.id !== null) {
    return String(endpoint.id);
  }

  return String(endpoint);
}

function normalizeRelation(relation) {
  const value = String(relation || "unknown").trim().toLowerCase();
  return value || "unknown";
}

function normalizeConfidence(confidence) {
  return String(confidence || "UNKNOWN").trim().toUpperCase() || "UNKNOWN";
}

function inferLayer(node) {
  const source = String(node.source_file || "");
  const id = String(node.id || "");
  const label = String(node.label || "");

  if (source.startsWith("adapter-in/") || id.includes("adapter_in")) {
    return "adapter-in";
  }

  if (source.startsWith("application/") || id.includes("application_")) {
    return "application";
  }

  if (source.startsWith("domain/") || id.includes("domain_")) {
    return "domain";
  }

  if (source.startsWith("adapter-out/") || id.includes("adapter_out")) {
    return "adapter-out";
  }

  if (source.startsWith("bootstrap/") || id.includes("bootstrap_")) {
    return "bootstrap";
  }

  if (/^Jpa/.test(label) || /^SpringData/.test(label)) {
    return "adapter-out";
  }

  if (/UseCase$/.test(label)) {
    return "domain";
  }

  if (/(Repository|Gateway|Worker|Adapter)$/.test(label) && !/UseCase$/.test(label)) {
    return "adapter-out";
  }

  if (/Controller$|WebMapper$/.test(label)) {
    return "adapter-in";
  }

  if (/ApplicationService$/.test(label)) {
    return "application";
  }

  if (/Config$/.test(label)) {
    return "bootstrap";
  }

  return "other";
}

function isFileNode(node) {
  return String(node.label || "").endsWith(".java") || String(node.label || "").endsWith(".py");
}

function isClassLikeLabel(label) {
  return /^[A-Z][A-Za-z0-9_$]*$/.test(String(label || ""));
}

function computeAnchor(from, to) {
  const dx = to.x - from.x;
  const dy = to.y - from.y;

  if (Math.abs(dx) > Math.abs(dy)) {
    return {
      x: from.x + Math.sign(dx || 1) * (from.width / 2),
      y: from.y
    };
  }

  return {
    x: from.x,
    y: from.y + Math.sign(dy || 1) * (from.height / 2)
  };
}

function buildCurvePath(start, end) {
  const deltaX = end.x - start.x;
  const deltaY = end.y - start.y;

  let c1x;
  let c1y;
  let c2x;
  let c2y;

  if (Math.abs(deltaX) >= Math.abs(deltaY)) {
    const offset = Math.max(40, Math.min(220, Math.abs(deltaX) * 0.35));
    c1x = start.x + Math.sign(deltaX || 1) * offset;
    c1y = start.y;
    c2x = end.x - Math.sign(deltaX || 1) * offset;
    c2y = end.y;
  } else {
    const offset = Math.max(30, Math.min(170, Math.abs(deltaY) * 0.45));
    c1x = start.x;
    c1y = start.y + Math.sign(deltaY || 1) * offset;
    c2x = end.x;
    c2y = end.y - Math.sign(deltaY || 1) * offset;
  }

  return `M ${start.x} ${start.y} C ${c1x} ${c1y} ${c2x} ${c2y} ${end.x} ${end.y}`;
}

function parseLine(sourceLocation) {
  if (!sourceLocation) {
    return "Non renseignée";
  }

  const value = String(sourceLocation);
  if (value.startsWith("L")) {
    return value.slice(1);
  }

  return value;
}

function humanizeRelation(relation) {
  return relation.replace(/_/g, " ");
}

function countEdgesByRelation(relation) {
  return state.architectureEdges.filter((edge) => edge.relation === relation).length;
}

function formatInteger(value) {
  return new Intl.NumberFormat("fr-BE").format(value);
}

function truncate(value, maxLength) {
  if (value.length <= maxLength) {
    return value;
  }
  return `${value.slice(0, maxLength - 1)}…`;
}

function clamp(min, max, value) {
  return Math.min(max, Math.max(min, value));
}

init();
