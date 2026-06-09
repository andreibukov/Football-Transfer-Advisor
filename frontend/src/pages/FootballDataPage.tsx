import { useEffect, useMemo, useState } from "react";
import { Club } from "../types/club";
import { Player } from "../types/player";
import { createClub, getAllClubs } from "../services/clubService";
import { createPlayer, getAllPlayers } from "../services/playerService";
import { getOntologyIndividualsByClass } from "../services/ontologyService";

const attributeFields: Array<{ key: keyof Player; label: string }> = [
    { key: "speed", label: "Speed" },
    { key: "acceleration", label: "Acceleration" },
    { key: "passing", label: "Passing" },
    { key: "vision", label: "Vision" },
    { key: "ballControl", label: "Ball Control" },
    { key: "finishing", label: "Finishing" },
    { key: "dribbling", label: "Dribbling" },
    { key: "stamina", label: "Stamina" },
    { key: "strength", label: "Strength" },
    { key: "tackling", label: "Tackling" },
    { key: "positioning", label: "Positioning" },
    { key: "workRate", label: "Work Rate" },
];

const defaultClub: Club = {
    name: "",
    league: "",
    budget: 50000000,
    preferredStyle: "",
};

const defaultPlayer: Player = {
    name: "",
    age: 25,
    currentClub: "",
    league: "",
    position: "",
    marketValue: 50000000,
    speed: 75,
    acceleration: 75,
    passing: 75,
    vision: 75,
    ballControl: 75,
    finishing: 75,
    dribbling: 75,
    stamina: 75,
    strength: 75,
    tackling: 75,
    positioning: 75,
    workRate: 75,
};

const formatCurrency = (value: number) => `EUR ${value.toLocaleString()}`;

type Feedback = {
    type: "success" | "error" | "warning";
    message: string;
};

export default function FootballDataPage() {
    const [clubs, setClubs] = useState<Club[]>([]);
    const [players, setPlayers] = useState<Player[]>([]);
    const [leagues, setLeagues] = useState<string[]>([]);
    const [positions, setPositions] = useState<string[]>([]);
    const [playingStyles, setPlayingStyles] = useState<string[]>([]);
    const [clubForm, setClubForm] = useState<Club>(defaultClub);
    const [playerForm, setPlayerForm] = useState<Player>(defaultPlayer);
    const [isSavingClub, setIsSavingClub] = useState(false);
    const [isSavingPlayer, setIsSavingPlayer] = useState(false);
    const [feedback, setFeedback] = useState<Feedback | null>(null);

    const loadData = async () => {
        try {
            const [
                clubsData,
                playersData,
                leagueData,
                positionData,
                playingStyleData,
            ] = await Promise.all([
                getAllClubs(),
                getAllPlayers(),
                getOntologyIndividualsByClass("League"),
                getOntologyIndividualsByClass("Position"),
                getOntologyIndividualsByClass("PlayingStyle"),
            ]);

            setClubs(clubsData);
            setPlayers(playersData);
            setLeagues(leagueData);
            setPositions(positionData);
            setPlayingStyles(playingStyleData);

            setClubForm((current) => ({
                ...current,
                league: current.league || leagueData[0] || "",
                preferredStyle:
                    current.preferredStyle || playingStyleData[0] || "",
            }));

            setPlayerForm((current) => ({
                ...current,
                currentClub: current.currentClub || clubsData[0]?.name || "",
                league:
                    current.league || clubsData[0]?.league || leagueData[0] || "",
                position: current.position || positionData[0] || "",
            }));
        } catch {
            setFeedback({
                type: "error",
                message:
                    "Could not load football data. Check that the backend is running.",
            });
        }
    };

    useEffect(() => {
        loadData();
    }, []);

    const clubsByLeague = useMemo(() => {
        return clubs.reduce<Record<string, number>>((accumulator, club) => {
            accumulator[club.league] = (accumulator[club.league] ?? 0) + 1;
            return accumulator;
        }, {});
    }, [clubs]);

    const handleClubSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        setFeedback(null);

        const clubName = clubForm.name.trim();

        if (clubs.some((club) => club.name.toLowerCase() === clubName.toLowerCase())) {
            setFeedback({
                type: "error",
                message: "A club with this name already exists.",
            });
            return;
        }

        if (clubForm.budget <= 0) {
            setFeedback({
                type: "error",
                message: "Club budget must be greater than zero.",
            });
            return;
        }

        setIsSavingClub(true);

        try {
            await createClub({ ...clubForm, name: clubName });
            setClubForm({
                ...defaultClub,
                league: clubForm.league,
                preferredStyle: clubForm.preferredStyle,
            });
            await loadData();
            setFeedback({
                type: "success",
                message:
                    "Club saved and ontology relations were created automatically.",
            });
        } catch {
            setFeedback({
                type: "error",
                message: "Could not save club. Check the form values.",
            });
        } finally {
            setIsSavingClub(false);
        }
    };

    const handlePlayerSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        setFeedback(null);

        const playerName = playerForm.name.trim();

        if (
            players.some(
                (player) => player.name.toLowerCase() === playerName.toLowerCase()
            )
        ) {
            setFeedback({
                type: "error",
                message: "A player with this name already exists.",
            });
            return;
        }

        if (playerForm.age < 15 || playerForm.age > 45) {
            setFeedback({
                type: "error",
                message: "Player age must be between 15 and 45.",
            });
            return;
        }

        if (playerForm.marketValue <= 0) {
            setFeedback({
                type: "error",
                message: "Market value must be greater than zero.",
            });
            return;
        }

        const invalidAttribute = attributeFields.find((attribute) => {
            const value = playerForm[attribute.key] as number;
            return value < 0 || value > 100;
        });

        if (invalidAttribute) {
            setFeedback({
                type: "error",
                message: `${invalidAttribute.label} must be between 0 and 100.`,
            });
            return;
        }

        setIsSavingPlayer(true);

        try {
            await createPlayer({ ...playerForm, name: playerName });
            setPlayerForm({
                ...defaultPlayer,
                currentClub: playerForm.currentClub,
                league: playerForm.league,
                position: playerForm.position,
            });
            await loadData();
            setFeedback({
                type: "success",
                message:
                    "Player saved and ontology relations were created automatically.",
            });
        } catch {
            setFeedback({
                type: "error",
                message: "Could not save player. Check the form values.",
            });
        } finally {
            setIsSavingPlayer(false);
        }
    };

    const handlePlayerClubChange = (clubName: string) => {
        const selectedClub = clubs.find((club) => club.name === clubName);

        setPlayerForm({
            ...playerForm,
            currentClub: clubName,
            league: selectedClub?.league ?? playerForm.league,
        });
    };

    return (
        <div>
            <header className="page-header">
                <div>
                    <p className="page-kicker">Database records</p>
                    <h1 className="page-title">Football Data</h1>
                    <p className="page-description">
                        Manage concrete clubs and players stored in the database.
                        Ontology still defines the valid football concepts used
                        by these records.
                    </p>
                </div>

                <span className="badge">
                    {clubs.length} clubs / {players.length} players
                </span>
            </header>

            {feedback && (
                <div className={`alert alert-${feedback.type}`}>
                    {feedback.message}
                </div>
            )}

            <section className="grid-auto" style={{ marginBottom: "16px" }}>
                <div className="metric-card">
                    <p className="metric-value">{clubs.length}</p>
                    <p className="metric-label">Clubs in database</p>
                </div>
                <div className="metric-card">
                    <p className="metric-value">{players.length}</p>
                    <p className="metric-label">Players in database</p>
                </div>
                <div className="metric-card">
                    <p className="metric-value">{leagues.length}</p>
                    <p className="metric-label">Ontology leagues</p>
                </div>
                <div className="metric-card">
                    <p className="metric-value">{positions.length}</p>
                    <p className="metric-label">Ontology positions</p>
                </div>
            </section>

            <div className="grid-two">
                <section className="page-card">
                    <h2 className="section-title">Add Club</h2>
                    <p className="section-subtitle">
                        Clubs are DB records. League and preferred style are
                        selected from ontology concepts. Saving a club also
                        creates ontology knowledge such as playsInLeague and
                        prefersPlayingStyle.
                    </p>

                    <form className="form" onSubmit={handleClubSubmit}>
                        <label>
                            Club Name
                            <input
                                value={clubForm.name}
                                onChange={(event) =>
                                    setClubForm({
                                        ...clubForm,
                                        name: event.target.value,
                                    })
                                }
                                placeholder="Example: Aston Villa"
                                required
                            />
                        </label>

                        <div className="form-grid">
                            <label>
                                League
                                <select
                                    value={clubForm.league}
                                    onChange={(event) =>
                                        setClubForm({
                                            ...clubForm,
                                            league: event.target.value,
                                        })
                                    }
                                    required
                                >
                                    {leagues.map((league) => (
                                        <option key={league} value={league}>
                                            {league}
                                        </option>
                                    ))}
                                </select>
                            </label>

                            <label>
                                Preferred Style
                                <select
                                    value={clubForm.preferredStyle}
                                    onChange={(event) =>
                                        setClubForm({
                                            ...clubForm,
                                            preferredStyle: event.target.value,
                                        })
                                    }
                                    required
                                >
                                    {playingStyles.map((style) => (
                                        <option key={style} value={style}>
                                            {style}
                                        </option>
                                    ))}
                                </select>
                            </label>
                        </div>

                        <label>
                            Budget
                            <input
                                min={1}
                                type="number"
                                value={clubForm.budget}
                                onChange={(event) =>
                                    setClubForm({
                                        ...clubForm,
                                        budget: Number(event.target.value),
                                    })
                                }
                                required
                            />
                        </label>

                        <button disabled={isSavingClub} type="submit">
                            {isSavingClub ? "Saving club..." : "Add Club"}
                        </button>
                    </form>
                </section>

                <section className="page-card">
                    <h2 className="section-title">Club Overview</h2>
                    <p className="section-subtitle">
                        Current database clubs grouped through their league field.
                    </p>

                    <div className="ontology-list">
                        {Object.entries(clubsByLeague).map(([league, count]) => (
                            <article key={league} className="ontology-item">
                                <h3 style={{ marginBottom: "8px" }}>{league}</h3>
                                <p className="muted">{count} clubs</p>
                            </article>
                        ))}
                    </div>
                </section>
            </div>

            <section className="page-card">
                <h2 className="section-title">Add Player</h2>
                <p className="section-subtitle">
                    Player attributes are the numeric values used by ontology
                    relations such as styleRequiresAttribute. Saving a player
                    also creates playsForClub and hasPosition ontology relations.
                </p>

                <form className="form" onSubmit={handlePlayerSubmit}>
                    <div className="form-grid">
                        <label>
                            Player Name
                            <input
                                value={playerForm.name}
                                onChange={(event) =>
                                    setPlayerForm({
                                        ...playerForm,
                                        name: event.target.value,
                                    })
                                }
                                placeholder="Example: Mohamed Salah"
                                required
                            />
                        </label>

                        <label>
                            Current Club
                            <select
                                value={playerForm.currentClub}
                                onChange={(event) =>
                                    handlePlayerClubChange(event.target.value)
                                }
                                required
                            >
                                {clubs.map((club) => (
                                    <option key={club.id} value={club.name}>
                                        {club.name}
                                    </option>
                                ))}
                            </select>
                        </label>

                        <label>
                            League
                            <input value={playerForm.league} readOnly />
                        </label>

                        <label>
                            Position
                            <select
                                value={playerForm.position}
                                onChange={(event) =>
                                    setPlayerForm({
                                        ...playerForm,
                                        position: event.target.value,
                                    })
                                }
                                required
                            >
                                {positions.map((position) => (
                                    <option key={position} value={position}>
                                        {position}
                                    </option>
                                ))}
                            </select>
                        </label>

                        <label>
                            Age
                            <input
                                max={45}
                                min={15}
                                type="number"
                                value={playerForm.age}
                                onChange={(event) =>
                                    setPlayerForm({
                                        ...playerForm,
                                        age: Number(event.target.value),
                                    })
                                }
                                required
                            />
                        </label>

                        <label>
                            Market Value
                            <input
                                min={1}
                                type="number"
                                value={playerForm.marketValue}
                                onChange={(event) =>
                                    setPlayerForm({
                                        ...playerForm,
                                        marketValue: Number(event.target.value),
                                    })
                                }
                                required
                            />
                        </label>
                    </div>

                    <h3 className="section-title">Player Attributes</h3>
                    <div className="form-grid">
                        {attributeFields.map((attribute) => (
                            <label key={attribute.key}>
                                {attribute.label}
                                <input
                                    max={100}
                                    min={0}
                                    type="number"
                                    value={playerForm[attribute.key] as number}
                                    onChange={(event) =>
                                        setPlayerForm({
                                            ...playerForm,
                                            [attribute.key]: Number(
                                                event.target.value
                                            ),
                                        })
                                    }
                                    required
                                />
                            </label>
                        ))}
                    </div>

                    <button disabled={isSavingPlayer} type="submit">
                        {isSavingPlayer ? "Saving player..." : "Add Player"}
                    </button>
                </form>
            </section>

            <section className="page-card">
                <h2 className="section-title">Players</h2>
                {players.length === 0 && (
                    <div className="empty-state">No players found.</div>
                )}

                <div className="recommendation-list">
                    {players.map((player) => (
                        <article key={player.id} className="recommendation-card">
                            <div className="rank-badge">
                                {player.name.slice(0, 2).toUpperCase()}
                            </div>
                            <div>
                                <h3 className="section-title">{player.name}</h3>
                                <p className="muted">
                                    {player.position} at {player.currentClub} in{" "}
                                    {player.league}
                                </p>
                                <ul className="info-list">
                                    <li>
                                        <span>Age</span>
                                        <strong>{player.age}</strong>
                                    </li>
                                    <li>
                                        <span>Market value</span>
                                        <strong>
                                            {formatCurrency(player.marketValue)}
                                        </strong>
                                    </li>
                                </ul>
                            </div>
                            <div className="score-panel">
                                <span className="score-value">
                                    {player.workRate}
                                </span>
                                <span className="score-label">Work rate</span>
                            </div>
                        </article>
                    ))}
                </div>
            </section>
        </div>
    );
}
