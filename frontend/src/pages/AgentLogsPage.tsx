import { useCallback, useEffect, useMemo, useState } from "react";
import { AgentLog } from "../types/agentLog";
import { getAllAgentLogs } from "../services/agentLogService";

const formatDateTime = (value: string) =>
    new Intl.DateTimeFormat("en-GB", {
        dateStyle: "medium",
        timeStyle: "medium",
    }).format(new Date(value));

const uniqueValues = (values: string[]) =>
    Array.from(new Set(values.filter(Boolean))).sort();

const agentProfiles: Record<string, { role: string; responsibility: string }> = {
    BackendSenderAgent: {
        role: "Request coordinator",
        responsibility: "Starts the transfer analysis from the web application.",
    },
    ClubAnalysisAgent: {
        role: "Club analyst",
        responsibility: "Interprets the selected club need before recommendation.",
    },
    OntologyManagerAgent: {
        role: "Ontology specialist",
        responsibility: "Uses ontology knowledge such as positions, styles, and rules.",
    },
    TransferRecommendationAgent: {
        role: "Recommendation agent",
        responsibility: "Produces candidate suggestions based on the analysed need.",
    },
};

const performativeLabels: Record<string, { label: string; color: string }> = {
    REQUEST: { label: "Request", color: "#2563eb" },
    INFORM: { label: "Information", color: "#16a34a" },
    PROPOSE: { label: "Proposal", color: "#7c3aed" },
    FAILURE: { label: "Failure", color: "#dc2626" },
    REFUSE: { label: "Refused", color: "#ea580c" },
};

const getAgentDisplayName = (agent: string) => agent.replace(/@.*$/, "");

const getAgentProfile = (agent: string) =>
    agentProfiles[getAgentDisplayName(agent)] ?? {
        role: "JADE agent",
        responsibility: "Participates in the ACL communication flow.",
    };

const getPerformativeMeta = (performative: string) =>
    performativeLabels[performative] ?? {
        label: performative,
        color: "#475569",
    };

const getStepTitle = (log: AgentLog) => {
    const sender = getAgentDisplayName(log.senderAgent);
    const receiver = getAgentDisplayName(log.receiverAgent);

    if (sender === "BackendSenderAgent" && receiver === "ClubAnalysisAgent") {
        return "1. Transfer need submitted";
    }

    if (sender === "ClubAnalysisAgent" && receiver === "OntologyManagerAgent") {
        return "2. Club context requested";
    }

    if (
        sender === "OntologyManagerAgent" &&
        receiver === "ClubAnalysisAgent"
    ) {
        return "3. Ontology context returned";
    }

    if (
        sender === "ClubAnalysisAgent" &&
        receiver === "TransferRecommendationAgent"
    ) {
        return "4. Recommendation requested";
    }

    if (
        sender === "TransferRecommendationAgent" &&
        receiver === "ClubAnalysisAgent"
    ) {
        return "5. Recommendation result returned";
    }

    return `${sender} communicated with ${receiver}`;
};

const summarizeMessage = (message: string) => {
    const normalized = message.replace(/\s+/g, " ").trim();

    if (normalized.length <= 140) {
        return normalized;
    }

    return `${normalized.slice(0, 137)}...`;
};

export default function AgentLogsPage() {
    const [logs, setLogs] = useState<AgentLog[]>([]);
    const [selectedAgent, setSelectedAgent] = useState("");
    const [showNewestFirst, setShowNewestFirst] = useState(true);
    const [autoRefresh, setAutoRefresh] = useState(true);
    const [expandedLogId, setExpandedLogId] = useState<number | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    const loadLogs = useCallback(async () => {
        setIsLoading(true);

        try {
            const data = await getAllAgentLogs();
            setLogs(data);
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        loadLogs();
    }, [loadLogs]);

    useEffect(() => {
        if (!autoRefresh) {
            return;
        }

        const intervalId = window.setInterval(loadLogs, 5000);

        return () => window.clearInterval(intervalId);
    }, [autoRefresh, loadLogs]);

    const agents = useMemo(
        () =>
            uniqueValues(
                logs.flatMap((log) => [
                    getAgentDisplayName(log.senderAgent),
                    getAgentDisplayName(log.receiverAgent),
                ])
            ),
        [logs]
    );

    const filteredLogs = useMemo(() => {
        return logs
            .filter((log) => {
                const sender = getAgentDisplayName(log.senderAgent);
                const receiver = getAgentDisplayName(log.receiverAgent);

                return (
                    selectedAgent === "" ||
                    sender === selectedAgent ||
                    receiver === selectedAgent
                );
            })
            .sort((firstLog, secondLog) => {
                const firstTime = new Date(firstLog.createdAt).getTime();
                const secondTime = new Date(secondLog.createdAt).getTime();

                return showNewestFirst
                    ? secondTime - firstTime
                    : firstTime - secondTime;
            });
    }, [logs, selectedAgent, showNewestFirst]);

    const latestLog = logs.reduce<AgentLog | null>((latest, log) => {
        if (!latest) {
            return log;
        }

        return new Date(log.createdAt).getTime() >
            new Date(latest.createdAt).getTime()
            ? log
            : latest;
    }, null);

    const clearFilters = () => {
        setSelectedAgent("");
    };

    return (
        <div>
            <div className="page-card">
                <h1 className="page-title">Agent ACL Timeline</h1>
                <p>
                    This page shows the agent conversation that happens after a
                    recommendation request. Use it as a simple audit trail: who
                    asked, who answered, and what information was exchanged.
                </p>
            </div>

            <section className="page-card">
                <h2 className="section-title">How to read the flow</h2>
                <div className="grid-auto">
                    <div className="metric-card">
                        <p className="metric-value">1</p>
                        <p className="metric-label">Backend sends transfer need</p>
                    </div>
                    <div className="metric-card">
                        <p className="metric-value">2</p>
                        <p className="metric-label">Club agent asks for context</p>
                    </div>
                    <div className="metric-card">
                        <p className="metric-value">3</p>
                        <p className="metric-label">Ontology agent returns context</p>
                    </div>
                    <div className="metric-card">
                        <p className="metric-value">4</p>
                        <p className="metric-label">Club agent asks recommendation agent</p>
                    </div>
                    <div className="metric-card">
                        <p className="metric-value">5</p>
                        <p className="metric-label">Recommendation agent returns result</p>
                    </div>
                </div>
            </section>

            <section
                style={{
                    display: "grid",
                    gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))",
                    gap: "16px",
                    marginBottom: "16px",
                }}
            >
                <div className="page-card">
                    <h2>{logs.length}</h2>
                    <p>Total ACL messages</p>
                </div>

                <div className="page-card">
                    <h2>{agents.length}</h2>
                    <p>Agents involved</p>
                </div>

                <div className="page-card">
                    <h2>{latestLog ? formatDateTime(latestLog.createdAt) : "-"}</h2>
                    <p>Latest activity</p>
                </div>
            </section>

            <section className="page-card">
                <div
                    style={{
                        display: "grid",
                        gridTemplateColumns:
                            "repeat(auto-fit, minmax(180px, 1fr))",
                        gap: "12px",
                        alignItems: "end",
                    }}
                >
                    <label>
                        Show messages for agent
                        <select
                            value={selectedAgent}
                            onChange={(event) =>
                                setSelectedAgent(event.target.value)
                            }
                            style={{ width: "100%", marginTop: "6px" }}
                        >
                            <option value="">All agents</option>
                            {agents.map((agent) => (
                                <option key={agent} value={agent}>
                                    {agent}
                                </option>
                            ))}
                        </select>
                    </label>

                    <label>
                        Order
                        <select
                            value={showNewestFirst ? "newest" : "oldest"}
                            onChange={(event) =>
                                setShowNewestFirst(event.target.value === "newest")
                            }
                            style={{ width: "100%", marginTop: "6px" }}
                        >
                            <option value="newest">Newest first</option>
                            <option value="oldest">Oldest first</option>
                        </select>
                    </label>

                    <label
                        style={{
                            display: "flex",
                            gap: "8px",
                            alignItems: "center",
                            marginBottom: "10px",
                        }}
                    >
                        <input
                            checked={autoRefresh}
                            onChange={(event) =>
                                setAutoRefresh(event.target.checked)
                            }
                            type="checkbox"
                            style={{ width: "16px", height: "16px" }}
                        />
                        Auto refresh
                    </label>

                    <button onClick={loadLogs} disabled={isLoading}>
                        {isLoading ? "Refreshing..." : "Refresh Logs"}
                    </button>

                    <button
                        disabled={selectedAgent === ""}
                        onClick={clearFilters}
                        type="button"
                        style={{ backgroundColor: "#475569" }}
                    >
                        Clear Filters
                    </button>
                </div>
            </section>

            {filteredLogs.length === 0 && (
                <section className="page-card">
                    <h2>No matching ACL messages</h2>
                    <p>
                        Generate recommendations from Recommend Player or clear the
                        current filters.
                    </p>
                </section>
            )}

            {filteredLogs.map((log) => (
                <article
                    key={log.id}
                    className="page-card"
                    style={{
                        borderLeft: `5px solid ${
                            getPerformativeMeta(log.performative).color
                        }`,
                    }}
                >
                    <div
                        style={{
                            display: "flex",
                            justifyContent: "space-between",
                            gap: "12px",
                            alignItems: "flex-start",
                            flexWrap: "wrap",
                        }}
                    >
                        <div>
                            <h2 style={{ margin: "0 0 8px" }}>
                                {getStepTitle(log)}
                            </h2>

                            <p style={{ marginTop: 0, color: "#475569" }}>
                                {getAgentDisplayName(log.senderAgent)} sends ACL{" "}
                                {getPerformativeMeta(log.performative).label} to{" "}
                                {getAgentDisplayName(log.receiverAgent)}
                            </p>
                            <p className="muted">
                                {getAgentProfile(log.senderAgent).responsibility}
                            </p>

                            <span
                                style={{
                                    display: "inline-block",
                                    padding: "4px 8px",
                                    borderRadius: "6px",
                                    backgroundColor: "#f8fafc",
                                    color: getPerformativeMeta(log.performative)
                                        .color,
                                    border: `1px solid ${
                                        getPerformativeMeta(log.performative).color
                                    }`,
                                    fontWeight: 700,
                                    fontSize: "12px",
                                }}
                            >
                                {log.performative}
                            </span>
                        </div>

                        <time style={{ color: "#64748b", fontSize: "14px" }}>
                            {formatDateTime(log.createdAt)}
                        </time>
                    </div>

                    <p>
                        <strong>Message summary:</strong>{" "}
                        {summarizeMessage(log.messageContent)}
                    </p>

                    <button
                        onClick={() =>
                            setExpandedLogId(
                                expandedLogId === log.id ? null : log.id
                            )
                        }
                        type="button"
                        style={{ backgroundColor: "#0f172a" }}
                    >
                        {expandedLogId === log.id
                            ? "Hide Raw ACL Message"
                            : "Show Raw ACL Message"}
                    </button>

                    {expandedLogId === log.id && (
                        <pre
                            style={{
                                whiteSpace: "pre-wrap",
                                overflowWrap: "anywhere",
                                backgroundColor: "#f8fafc",
                                border: "1px solid #e2e8f0",
                                borderRadius: "6px",
                                padding: "12px",
                                marginTop: "12px",
                                fontFamily: "inherit",
                                textAlign: "left",
                            }}
                        >
                            {log.messageContent}
                        </pre>
                    )}
                </article>
            ))}
        </div>
    );
}
