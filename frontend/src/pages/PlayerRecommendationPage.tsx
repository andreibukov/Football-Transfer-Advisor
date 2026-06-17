import { useEffect, useState } from "react";
import { Club } from "../types/club";
import { Recommendation } from "../types/recommendation";
import { TransferNeed } from "../types/transferNeed";
import { getAllClubs } from "../services/clubService";
import { createTransferNeed } from "../services/transferNeedService";
import { getRecommendations } from "../services/recommendationService";
import {
    getOntologyIndividualsByClass,
    getRelatedOntologyTargets,
} from "../services/ontologyService";
import { startAgentTransferAnalysis } from "../services/agentService";

const ages = Array.from({ length: 18 }, (_, index) => index + 18);

const corePositionOrder = [
    "Goalkeeper",
    "CenterBack",
    "LeftBack",
    "RightBack",
    "DefensiveMidfielder",
    "CentralMidfielder",
    "AttackingMidfielder",
    "LeftWinger",
    "RightWinger",
    "Striker",
];

const formatOntologyName = (value: string) =>
    value.replace(/([a-z])([A-Z])/g, "$1 $2");

const formatCurrency = (value?: number) =>
    value ? `EUR ${value.toLocaleString()}` : "N/A";

const formatScore = (value?: number) =>
    typeof value === "number" ? `${value.toFixed(2)}%` : "N/A";

type RecommendationDetails = Recommendation & {
    roleMatch?: number;
    reasons?: string[];
};

type Feedback = {
    type: "success" | "error" | "warning";
    message: string;
};

const uniqueOptions = (values: string[]) =>
    Array.from(new Set(values.filter(Boolean)));

const sortPositionOptions = (values: string[]) =>
    [...values].sort((firstPosition, secondPosition) => {
        const firstIndex = corePositionOrder.indexOf(firstPosition);
        const secondIndex = corePositionOrder.indexOf(secondPosition);

        if (firstIndex === -1 && secondIndex === -1) {
            return firstPosition.localeCompare(secondPosition);
        }

        if (firstIndex === -1) {
            return 1;
        }

        if (secondIndex === -1) {
            return -1;
        }

        return firstIndex - secondIndex;
    });

const resolveClubStyle = (
    preferredStyle: string | undefined,
    availableStyles: string[],
    fallbackStyle: string
) => {
    if (preferredStyle && availableStyles.includes(preferredStyle)) {
        return preferredStyle;
    }

    return fallbackStyle;
};

const wait = (milliseconds: number) =>
    new Promise((resolve) => window.setTimeout(resolve, milliseconds));

export default function PlayerRecommendationPage() {
    const [clubs, setClubs] = useState<Club[]>([]);
    const [positions, setPositions] = useState<string[]>([]);
    const [playingStyles, setPlayingStyles] = useState<string[]>([]);
    const [playerRoles, setPlayerRoles] = useState<string[]>([]);
    const [selectedLeague, setSelectedLeague] = useState<string>("");
    const [selectedClubId, setSelectedClubId] = useState<number>(0);
    const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [feedback, setFeedback] = useState<Feedback | null>(null);

    const [formData, setFormData] = useState<TransferNeed>({
        neededPosition: "DefensiveMidfielder",
        playingStyle: "Possession",
        preferredRole: "",
        maxBudget: 60000000,
        maxAge: 27,
    });

    useEffect(() => {
        const loadInitialData = async () => {
            try {
                const [clubsData, positionData, playingStyleData] =
                    await Promise.all([
                        getAllClubs(),
                        getOntologyIndividualsByClass("Position"),
                        getOntologyIndividualsByClass("PlayingStyle"),
                    ]);

                const positionOptions = sortPositionOptions(
                    uniqueOptions([...corePositionOrder, ...positionData])
                );
                const playingStyleOptions = uniqueOptions(playingStyleData);
                const initialClub = clubsData[0];
                const initialPlayingStyle = resolveClubStyle(
                    initialClub?.preferredStyle,
                    playingStyleOptions,
                    playingStyleOptions[0] ?? "Possession"
                );

                setClubs(clubsData);
                setPositions(positionOptions);
                setPlayingStyles(playingStyleOptions);

                if (clubsData.length > 0 && clubsData[0].id) {
                    setSelectedLeague(clubsData[0].league);
                    setSelectedClubId(clubsData[0].id);
                }

                setFormData((currentFormData) => ({
                    ...currentFormData,
                    neededPosition:
                        positionOptions[0] ?? currentFormData.neededPosition,
                    playingStyle: initialPlayingStyle,
                }));
            } catch {
                setFeedback({
                    type: "error",
                    message:
                        "Could not load clubs or ontology options. Check that the backend is running.",
                });
            }
        };

        loadInitialData();
    }, []);

    useEffect(() => {
        const loadRolesForPosition = async () => {
            if (!formData.neededPosition) {
                setPlayerRoles([]);
                return;
            }

            try {
                const roles = await getRelatedOntologyTargets(
                    formData.neededPosition,
                    "hasRole"
                );

                setPlayerRoles(roles);
                setFormData((currentFormData) => ({
                    ...currentFormData,
                    preferredRole: roles.includes(
                        currentFormData.preferredRole ?? ""
                    )
                        ? currentFormData.preferredRole
                        : roles[0] ?? "",
                }));
            } catch {
                setPlayerRoles([]);
            }
        };

        loadRolesForPosition();
    }, [formData.neededPosition]);

    const leagues = Array.from(new Set(clubs.map((club) => club.league)));

    const filteredClubs = clubs.filter(
        (club) => club.league === selectedLeague
    );

    const selectedClub = filteredClubs.find(
        (club) => club.id === selectedClubId
    );

    useEffect(() => {
        if (!selectedClub?.preferredStyle || playingStyles.length === 0) {
            return;
        }

        setFormData((currentFormData) => ({
            ...currentFormData,
            playingStyle: resolveClubStyle(
                selectedClub.preferredStyle,
                playingStyles,
                currentFormData.playingStyle
            ),
        }));
    }, [selectedClub?.id, selectedClub?.preferredStyle, playingStyles]);

    const handleLeagueChange = (league: string) => {
        setSelectedLeague(league);

        const firstClubInLeague = clubs.find((club) => club.league === league);

        if (firstClubInLeague?.id) {
            setSelectedClubId(firstClubInLeague.id);
        }
    };

    const handleGenerate = async (event: React.FormEvent) => {
        event.preventDefault();
        setFeedback(null);

        if (selectedClubId <= 0) {
            setFeedback({
                type: "error",
                message: "Select a buying club before generating recommendations.",
            });
            return;
        }

        if (formData.maxBudget <= 0) {
            setFeedback({
                type: "error",
                message: "Budget must be greater than zero.",
            });
            return;
        }

        setIsLoading(true);

        try {
            const createdTransferNeed = await createTransferNeed(
                selectedClubId,
                formData
            );

            await startAgentTransferAnalysis(createdTransferNeed.id!);

            let data: Recommendation[] = [];

            for (let attempt = 0; attempt < 10; attempt++) {
                data = await getRecommendations(createdTransferNeed.id!);

                if (data.length > 0) {
                    break;
                }

                await wait(500);
            }

            setRecommendations(data.slice(0, 3));
            setFeedback({
                type: data.length > 0 ? "success" : "warning",
                message:
                    data.length > 0
                        ? "Agent flow generated recommendations for this transfer need."
                        : "Agent flow started, but no eligible recommendations were available yet. Check Agent Logs for details.",
            });
        } catch {
            setFeedback({
                type: "error",
                message:
                    "Could not generate recommendations. Check the selected data and backend status.",
            });
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div>
            <header className="page-header">
                <div>
                    <p className="page-kicker">Decision support</p>
                    <h1 className="page-title">Recommend Player</h1>
                    <p className="page-description">
                        Build a transfer need, let JADE agents coordinate the
                        ontology and recommendation steps, and review the top
                        matches.
                    </p>
                </div>

                <span className="badge">OWL + JADE + scoring engine</span>
            </header>

            {feedback && (
                <div className={`alert alert-${feedback.type}`}>
                    {feedback.message}
                </div>
            )}

            {formData.neededPosition && playerRoles.length === 0 && (
                <div className="alert alert-warning">
                    No preferred roles are defined for{" "}
                    {formatOntologyName(formData.neededPosition)} in the OWL
                    ontology. Recommendations can still be generated with Any
                    role.
                </div>
            )}

            <div className="grid-two">
                <section className="page-card">
                    <h2 className="section-title">Recommendation Criteria</h2>
                    <p className="section-subtitle">
                        Choose the buying context and the player profile you
                        need.
                    </p>

                    <form onSubmit={handleGenerate} className="form">
                        <div className="form-grid">
                            <label>
                                League
                                <select
                                    value={selectedLeague}
                                    onChange={(event) =>
                                        handleLeagueChange(event.target.value)
                                    }
                                >
                                    {leagues.map((league) => (
                                        <option key={league} value={league}>
                                            {league}
                                        </option>
                                    ))}
                                </select>
                            </label>

                            <label>
                                Club
                                <select
                                    value={selectedClubId}
                                    onChange={(event) =>
                                        setSelectedClubId(
                                            Number(event.target.value)
                                        )
                                    }
                                >
                                    {filteredClubs.map((club) => (
                                        <option key={club.id} value={club.id}>
                                            {club.name}
                                        </option>
                                    ))}
                                </select>
                            </label>

                            <label>
                                Needed Position
                                <select
                                    value={formData.neededPosition}
                                    onChange={(event) =>
                                        setFormData({
                                            ...formData,
                                            neededPosition: event.target.value,
                                        })
                                    }
                                >
                                    {positions.map((position) => (
                                        <option key={position} value={position}>
                                            {formatOntologyName(position)}
                                        </option>
                                    ))}
                                </select>
                            </label>

                            <label>
                                Playing Style
                                <select
                                    value={formData.playingStyle}
                                    onChange={(event) =>
                                        setFormData({
                                            ...formData,
                                            playingStyle: event.target.value,
                                        })
                                    }
                                >
                                    {playingStyles.map((style) => (
                                        <option key={style} value={style}>
                                            {formatOntologyName(style)}
                                        </option>
                                    ))}
                                </select>
                                <span className="field-hint">
                                    Defaults to the club preferred style. Change
                                    it only if this specific transfer need has a
                                    different tactical requirement.
                                </span>
                            </label>

                            <label>
                                Preferred Role
                                <select
                                    value={formData.preferredRole ?? ""}
                                    onChange={(event) =>
                                        setFormData({
                                            ...formData,
                                            preferredRole: event.target.value,
                                        })
                                    }
                                >
                                    <option value="">Any role</option>
                                    {playerRoles.map((role) => (
                                        <option key={role} value={role}>
                                            {formatOntologyName(role)}
                                        </option>
                                    ))}
                                </select>
                            </label>

                            <label>
                                Maximum Budget
                                <input
                                    min={1}
                                    type="number"
                                    value={formData.maxBudget}
                                    onChange={(event) =>
                                        setFormData({
                                            ...formData,
                                            maxBudget: Number(event.target.value),
                                        })
                                    }
                                />
                            </label>

                            <label>
                                Preferred Age
                                <select
                                    value={formData.maxAge}
                                    onChange={(event) =>
                                        setFormData({
                                            ...formData,
                                            maxAge: Number(event.target.value),
                                        })
                                    }
                                >
                                    {ages.map((age) => (
                                        <option key={age} value={age}>
                                            {age}
                                        </option>
                                    ))}
                                </select>
                            </label>
                        </div>

                        <button type="submit" disabled={isLoading}>
                            {isLoading
                                ? "Running agent analysis..."
                                : "Run Agent Recommendation"}
                        </button>
                    </form>
                </section>

                <section className="page-card">
                    <h2 className="section-title">Current Search</h2>
                    <p className="section-subtitle">
                        A quick view of the selected transfer need.
                    </p>

                    <ul className="info-list">
                        <li>
                            <span>Buying club</span>
                            <strong>{selectedClub?.name ?? "N/A"}</strong>
                        </li>
                        <li>
                            <span>Position</span>
                            <strong>
                                {formatOntologyName(formData.neededPosition)}
                            </strong>
                        </li>
                        <li>
                            <span>Club preferred style</span>
                            <strong>
                                {selectedClub?.preferredStyle
                                    ? formatOntologyName(
                                          selectedClub.preferredStyle
                                      )
                                    : "N/A"}
                            </strong>
                        </li>
                        <li>
                            <span>Style</span>
                            <strong>
                                {formatOntologyName(formData.playingStyle)}
                            </strong>
                        </li>
                        <li>
                            <span>Role</span>
                            <strong>
                                {formData.preferredRole
                                    ? formatOntologyName(formData.preferredRole)
                                    : "Any role"}
                            </strong>
                        </li>
                        <li>
                            <span>Budget</span>
                            <strong>{formatCurrency(formData.maxBudget)}</strong>
                        </li>
                        <li>
                            <span>Preferred age</span>
                            <strong>{formData.maxAge}</strong>
                        </li>
                    </ul>

                    <h2 className="section-title" style={{ marginTop: "24px" }}>
                        Scoring Weights
                    </h2>
                    <div className="grid-auto">
                        <div className="metric-card">
                            <p className="metric-value">40%</p>
                            <p className="metric-label">Position</p>
                        </div>
                        <div className="metric-card">
                            <p className="metric-value">30%</p>
                            <p className="metric-label">Style</p>
                        </div>
                        <div className="metric-card">
                            <p className="metric-value">15%</p>
                            <p className="metric-label">Budget</p>
                        </div>
                        <div className="metric-card">
                            <p className="metric-value">15%</p>
                            <p className="metric-label">Age</p>
                        </div>
                    </div>
                </section>
            </div>

            <section className="page-card">
                <h2 className="section-title">Top 3 Recommended Players</h2>

                {recommendations.length === 0 && (
                    <div className="empty-state">
                        No recommendations generated yet.
                    </div>
                )}

                <div className="recommendation-list">
                    {recommendations.map((recommendation, index) => {
                        const recommendationDetails =
                            recommendation as RecommendationDetails;
                        const reasons = recommendationDetails.reasons ?? [];

                        return (
                            <article
                                key={recommendation.id ?? index}
                                className="recommendation-card"
                            >
                                <div className="rank-badge">#{index + 1}</div>

                                <div>
                                    <h2 className="section-title">
                                        {recommendation.player?.name ??
                                            "Recommended Player"}
                                    </h2>
                                    <p className="muted">
                                        {recommendation.player?.position ?? "N/A"}{" "}
                                        at{" "}
                                        {recommendation.player?.currentClub ??
                                            "N/A"}{" "}
                                        in {recommendation.player?.league ?? "N/A"}
                                    </p>

                                    <ul className="info-list">
                                        <li>
                                            <span>Age</span>
                                            <strong>
                                                {recommendation.player?.age ??
                                                    "N/A"}
                                            </strong>
                                        </li>
                                        <li>
                                            <span>Market value</span>
                                            <strong>
                                                {formatCurrency(
                                                    recommendation.player
                                                        ?.marketValue
                                                )}
                                            </strong>
                                        </li>
                                    </ul>

                                    <div className="score-grid">
                                        <div className="score-item">
                                            <span>Position</span>
                                            <strong>
                                                {formatScore(
                                                    recommendation.positionMatch
                                                )}
                                            </strong>
                                        </div>
                                        <div className="score-item">
                                            <span>Style</span>
                                            <strong>
                                                {formatScore(
                                                    recommendation.styleMatch
                                                )}
                                            </strong>
                                        </div>
                                        <div className="score-item">
                                            <span>Role</span>
                                            <strong>
                                                {formatScore(
                                                    recommendationDetails.roleMatch
                                                )}
                                            </strong>
                                        </div>
                                        <div className="score-item">
                                            <span>Budget</span>
                                            <strong>
                                                {formatScore(
                                                    recommendation.budgetMatch
                                                )}
                                            </strong>
                                        </div>
                                        <div className="score-item">
                                            <span>Age</span>
                                            <strong>
                                                {formatScore(
                                                    recommendation.ageMatch
                                                )}
                                            </strong>
                                        </div>
                                    </div>

                                    <div style={{ marginTop: "16px" }}>
                                        <h3 className="section-title">
                                            Why this player?
                                        </h3>
                                        {reasons.length === 0 && (
                                            <p className="muted">
                                                No explanation reasons were
                                                returned for this recommendation.
                                            </p>
                                        )}

                                        {reasons.length > 0 && (
                                            <ul className="explanation-list">
                                                {reasons.map((reason: string) => (
                                                    <li key={reason}>
                                                        {reason}
                                                    </li>
                                                ))}
                                            </ul>
                                        )}
                                    </div>
                                </div>

                                <div className="score-panel">
                                    <span className="score-value">
                                        {formatScore(recommendation.totalScore)}
                                    </span>
                                    <span className="score-label">
                                        Total score
                                    </span>
                                </div>
                            </article>
                        );
                    })}
                </div>
            </section>
        </div>
    );
}
