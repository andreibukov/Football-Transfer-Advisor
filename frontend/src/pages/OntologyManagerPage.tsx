import { useEffect, useMemo, useState } from "react";
import { OntologyConcept, OntologyRelation } from "../types/ontology";
import {
    createOntologyConcept,
    createOntologyRelation,
    getAllOntologyConcepts,
    getAllOntologyRelations,
    getOntologyIndividualsByClass,
} from "../services/ontologyService";

const conceptTypes = [
    "PlayingStyle",
    "PlayerRole",
    "Position",
    "PlayerAttribute",
    "League",
    "Club",
    "Player",
];

const relationTemplates = [
    {
        id: "styleRequiresAttribute",
        label: "Playing style requires player attribute",
        relationType: "styleRequiresAttribute",
        sourceType: "PlayingStyle",
        targetType: "PlayerAttribute",
        impact: "Affects Style Match in Recommend Player.",
    },
    {
        id: "hasRole",
        label: "Position has player role",
        relationType: "hasRole",
        sourceType: "Position",
        targetType: "PlayerRole",
        impact: "Populates the Preferred Role dropdown in Recommend Player.",
    },
    {
        id: "requiresAttribute",
        label: "Position or role requires player attribute",
        relationType: "requiresAttribute",
        sourceType: "PlayerRole",
        targetType: "PlayerAttribute",
        impact: "Defines which player attributes are important for a role.",
    },
    {
        id: "playsInLeague",
        label: "Club plays in league",
        relationType: "playsInLeague",
        sourceType: "Club",
        targetType: "League",
        impact: "Adds football knowledge for club and league context.",
    },
    {
        id: "playsForClub",
        label: "Player plays for club",
        relationType: "playsForClub",
        sourceType: "Player",
        targetType: "Club",
        impact: "Adds football knowledge for player and club context.",
    },
    {
        id: "prefersPlayingStyle",
        label: "Club prefers playing style",
        relationType: "prefersPlayingStyle",
        sourceType: "Club",
        targetType: "PlayingStyle",
        impact: "Explains a club's tactical preference.",
    },
    {
        id: "hasPosition",
        label: "Player has position",
        relationType: "hasPosition",
        sourceType: "Player",
        targetType: "Position",
        impact: "Adds football knowledge for player profile context.",
    },
];

const initialConceptForm: OntologyConcept = {
    conceptName: "",
    conceptType: "PlayingStyle",
    description: "",
};

const emptyRelationForm: OntologyRelation = {
    sourceConcept: "",
    relationType: relationTemplates[0].relationType,
    targetConcept: "",
};

const uniqueSorted = (values: string[]) =>
    Array.from(new Set(values.filter(Boolean))).sort((a, b) =>
        a.localeCompare(b)
    );

type Feedback = {
    type: "success" | "error" | "warning";
    message: string;
};

export default function OntologyManagerPage() {
    const [concepts, setConcepts] = useState<OntologyConcept[]>([]);
    const [relations, setRelations] = useState<OntologyRelation[]>([]);
    const [ontologyIndividuals, setOntologyIndividuals] = useState<
        Record<string, string[]>
    >({});

    const [conceptForm, setConceptForm] =
        useState<OntologyConcept>(initialConceptForm);
    const [selectedRelationTemplateId, setSelectedRelationTemplateId] = useState(
        relationTemplates[0].id
    );
    const [graphRelationFilter, setGraphRelationFilter] = useState("");
    const [relationForm, setRelationForm] =
        useState<OntologyRelation>(emptyRelationForm);
    const [feedback, setFeedback] = useState<Feedback | null>(null);

    const selectedTemplate =
        relationTemplates.find(
            (template) => template.id === selectedRelationTemplateId
        ) ?? relationTemplates[0];

    const loadConcepts = async () => {
        try {
            const data = await getAllOntologyConcepts();
            setConcepts(data);
        } catch {
            setFeedback({
                type: "error",
                message: "Could not load ontology concepts.",
            });
        }
    };

    const loadRelations = async () => {
        try {
            const data = await getAllOntologyRelations();
            setRelations(data);
        } catch {
            setFeedback({
                type: "error",
                message: "Could not load ontology relations.",
            });
        }
    };

    const loadOntologyIndividuals = async () => {
        try {
            const entries = await Promise.all(
                conceptTypes.map(async (conceptType) => [
                    conceptType,
                    await getOntologyIndividualsByClass(conceptType),
                ])
            );

            setOntologyIndividuals(Object.fromEntries(entries));
        } catch {
            setFeedback({
                type: "error",
                message: "Could not load ontology dropdown options.",
            });
        }
    };

    useEffect(() => {
        loadConcepts();
        loadRelations();
        loadOntologyIndividuals();
    }, []);

    const conceptsByType = useMemo(() => {
        return conceptTypes.reduce<Record<string, string[]>>(
            (accumulator, conceptType) => {
                const databaseConcepts = concepts
                    .filter((concept) => concept.conceptType === conceptType)
                    .map((concept) => concept.conceptName);

                accumulator[conceptType] = uniqueSorted([
                    ...(ontologyIndividuals[conceptType] ?? []),
                    ...databaseConcepts,
                ]);

                return accumulator;
            },
            {}
        );
    }, [concepts, ontologyIndividuals]);

    const sourceOptions = useMemo(
        () => conceptsByType[selectedTemplate.sourceType] ?? [],
        [conceptsByType, selectedTemplate.sourceType]
    );
    const targetOptions = useMemo(
        () => conceptsByType[selectedTemplate.targetType] ?? [],
        [conceptsByType, selectedTemplate.targetType]
    );

    useEffect(() => {
        setRelationForm({
            sourceConcept: sourceOptions[0] ?? "",
            relationType: selectedTemplate.relationType,
            targetConcept: targetOptions[0] ?? "",
        });
    }, [
        selectedTemplate.relationType,
        sourceOptions,
        targetOptions,
    ]);

    const handleConceptSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        setFeedback(null);

        const conceptName = conceptForm.conceptName.trim();

        if (
            concepts.some(
                (concept) =>
                    concept.conceptName.toLowerCase() ===
                        conceptName.toLowerCase() &&
                    concept.conceptType === conceptForm.conceptType
            )
        ) {
            setFeedback({
                type: "error",
                message:
                    "This ontology concept already exists for the selected type.",
            });
            return;
        }

        try {
            await createOntologyConcept({
                ...conceptForm,
                conceptName,
            });

            setConceptForm({
                ...initialConceptForm,
                conceptType: conceptForm.conceptType,
            });

            await loadConcepts();
            await loadOntologyIndividuals();
            setFeedback({
                type: "success",
                message: "Ontology concept added successfully.",
            });
        } catch {
            setFeedback({
                type: "error",
                message: "Could not add ontology concept.",
            });
        }
    };

    const handleRelationSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        setFeedback(null);

        if (relationForm.sourceConcept === "" || relationForm.targetConcept === "") {
            setFeedback({
                type: "error",
                message: "Select both source and target concept.",
            });
            return;
        }

        if (
            relations.some(
                (relation) =>
                    relation.sourceConcept === relationForm.sourceConcept &&
                    relation.relationType === relationForm.relationType &&
                    relation.targetConcept === relationForm.targetConcept
            )
        ) {
            setFeedback({
                type: "error",
                message: "This ontology relation already exists.",
            });
            return;
        }

        try {
            await createOntologyRelation(relationForm);
            await loadRelations();
            setFeedback({
                type: "success",
                message: "Ontology relation added successfully.",
            });
        } catch {
            setFeedback({
                type: "error",
                message: "Could not add ontology relation.",
            });
        }
    };

    const isStyleScoringRelation =
        selectedTemplate.relationType === "styleRequiresAttribute";

    const relationTypes = useMemo(
        () => uniqueSorted(relations.map((relation) => relation.relationType)),
        [relations]
    );

    const graphRelations = useMemo(() => {
        return relations
            .filter(
                (relation) =>
                    graphRelationFilter === "" ||
                    relation.relationType === graphRelationFilter
            )
            .slice(0, 12);
    }, [graphRelationFilter, relations]);

    return (
        <div>
            <header className="page-header">
                <div>
                    <p className="page-kicker">Knowledge base</p>
                    <h1 className="page-title">Ontology Manager</h1>
                    <p className="page-description">
                        Add controlled football concepts and relations without
                        guessing the correct ontology vocabulary.
                    </p>
                </div>

                <span className="badge">
                    {concepts.length} concepts / {relations.length} relations
                </span>
            </header>

            {feedback && (
                <div className={`alert alert-${feedback.type}`}>
                    {feedback.message}
                </div>
            )}

            <section className="page-card">
                <h2 className="section-title">How This Affects Recommendations</h2>
                <p className="section-subtitle">
                    A new playing style appears in Recommend Player when it is
                    added as a PlayingStyle concept. It receives a Style Match
                    score only after you connect it to one or more PlayerAttribute
                    targets with the styleRequiresAttribute relation.
                </p>
                <div className="grid-auto">
                    <div className="metric-card">
                        <p className="metric-value">1</p>
                        <p className="metric-label">Add PlayingStyle concept</p>
                    </div>
                    <div className="metric-card">
                        <p className="metric-value">2</p>
                        <p className="metric-label">Link required attributes</p>
                    </div>
                    <div className="metric-card">
                        <p className="metric-value">3</p>
                        <p className="metric-label">Use it in Recommend Player</p>
                    </div>
                </div>
            </section>

            <section className="page-card">
                <div className="page-header">
                    <div>
                        <h2 className="section-title">Ontology Relationship Graph</h2>
                        <p className="section-subtitle">
                            Visual view of ontology triples: source concept,
                            relation, and target concept.
                        </p>
                    </div>

                    <label style={{ minWidth: "240px" }}>
                        Relation Type
                        <select
                            value={graphRelationFilter}
                            onChange={(event) =>
                                setGraphRelationFilter(event.target.value)
                            }
                        >
                            <option value="">All relation types</option>
                            {relationTypes.map((relationType) => (
                                <option key={relationType} value={relationType}>
                                    {relationType}
                                </option>
                            ))}
                        </select>
                    </label>
                </div>

                {graphRelations.length === 0 && (
                    <div className="empty-state">
                        No ontology relations available for this filter.
                    </div>
                )}

                {graphRelations.length > 0 && (
                    <div className="relationship-graph">
                        {graphRelations.map((relation) => (
                            <article
                                key={`${relation.sourceConcept}-${relation.relationType}-${relation.targetConcept}-${relation.id}`}
                                className="relationship-row"
                            >
                                <div className="graph-node source-node">
                                    <span>Source</span>
                                    <strong>{relation.sourceConcept}</strong>
                                </div>

                                <div className="graph-edge">
                                    <span>{relation.relationType}</span>
                                </div>

                                <div className="graph-node target-node">
                                    <span>Target</span>
                                    <strong>{relation.targetConcept}</strong>
                                </div>
                            </article>
                        ))}
                    </div>
                )}
            </section>

            <div className="grid-two">
                <section className="page-card">
                    <h2 className="section-title">Add Ontology Concept</h2>
                    <p className="section-subtitle">
                        Concept Type is controlled so the value can be reused
                        safely by dropdowns and relation templates.
                    </p>

                    <form onSubmit={handleConceptSubmit} className="form">
                        <label>
                            Concept Type
                            <select
                                value={conceptForm.conceptType}
                                onChange={(event) =>
                                    setConceptForm({
                                        ...conceptForm,
                                        conceptType: event.target.value,
                                    })
                                }
                            >
                                {conceptTypes.map((conceptType) => (
                                    <option key={conceptType} value={conceptType}>
                                        {conceptType}
                                    </option>
                                ))}
                            </select>
                        </label>

                        <label>
                            Concept Name
                            <input
                                placeholder="Example: Gegenpressing"
                                value={conceptForm.conceptName}
                                onChange={(event) =>
                                    setConceptForm({
                                        ...conceptForm,
                                        conceptName: event.target.value,
                                    })
                                }
                                required
                            />
                        </label>

                        <label>
                            Description
                            <textarea
                                placeholder="Explain what this football concept means."
                                value={conceptForm.description}
                                onChange={(event) =>
                                    setConceptForm({
                                        ...conceptForm,
                                        description: event.target.value,
                                    })
                                }
                                required
                            />
                        </label>

                        <button type="submit">Add Concept</button>
                    </form>
                </section>

                <section className="page-card">
                    <h2 className="section-title">Add Ontology Relation</h2>
                    <p className="section-subtitle">
                        Pick a relation template, then choose valid source and
                        target concepts.
                    </p>

                    <form onSubmit={handleRelationSubmit} className="form">
                        <label>
                            Relation Template
                            <select
                                value={selectedRelationTemplateId}
                                onChange={(event) =>
                                    setSelectedRelationTemplateId(
                                        event.target.value
                                    )
                                }
                            >
                                {relationTemplates.map((template) => (
                                    <option key={template.id} value={template.id}>
                                        {template.label}
                                    </option>
                                ))}
                            </select>
                        </label>

                        <div className="empty-state" style={{ textAlign: "left" }}>
                            <strong>{selectedTemplate.sourceType}</strong>{" "}
                            {selectedTemplate.relationType}{" "}
                            <strong>{selectedTemplate.targetType}</strong>
                            <br />
                            {selectedTemplate.impact}
                        </div>

                        {(sourceOptions.length === 0 ||
                            targetOptions.length === 0) && (
                            <div className="alert alert-warning">
                                This template has no available source or target
                                options. Add the missing concept type first.
                            </div>
                        )}

                        <label>
                            Source Concept
                            <select
                                value={relationForm.sourceConcept}
                                onChange={(event) =>
                                    setRelationForm({
                                        ...relationForm,
                                        sourceConcept: event.target.value,
                                    })
                                }
                                required
                            >
                                {sourceOptions.map((sourceConcept) => (
                                    <option
                                        key={sourceConcept}
                                        value={sourceConcept}
                                    >
                                        {sourceConcept}
                                    </option>
                                ))}
                            </select>
                        </label>

                        <label>
                            Target Concept
                            <select
                                value={relationForm.targetConcept}
                                onChange={(event) =>
                                    setRelationForm({
                                        ...relationForm,
                                        targetConcept: event.target.value,
                                    })
                                }
                                required
                            >
                                {targetOptions.map((targetConcept) => (
                                    <option
                                        key={targetConcept}
                                        value={targetConcept}
                                    >
                                        {targetConcept}
                                    </option>
                                ))}
                            </select>
                        </label>

                        {isStyleScoringRelation && (
                            <div className="badge">
                                This relation will influence Style Match scoring
                            </div>
                        )}

                        <button
                            disabled={
                                relationForm.sourceConcept === "" ||
                                relationForm.targetConcept === ""
                            }
                            type="submit"
                        >
                            Add Relation
                        </button>
                    </form>
                </section>
            </div>

            <div className="grid-two">
                <section className="page-card">
                    <div className="page-header">
                        <div>
                            <h2 className="section-title">Concepts</h2>
                            <p className="section-subtitle">
                                User-defined and system-seeded football concepts.
                            </p>
                        </div>

                        <button className="ghost-button" onClick={loadConcepts}>
                            Refresh
                        </button>
                    </div>

                    {concepts.length === 0 && (
                        <div className="empty-state">
                            No ontology concepts found.
                        </div>
                    )}

                    <div className="ontology-list">
                        {concepts.map((concept) => (
                            <article key={concept.id} className="ontology-item">
                                <div className="button-row">
                                    <span className="badge">
                                        {concept.conceptType}
                                    </span>
                                    <span className="badge">
                                        {concept.source ?? "USER_DEFINED"}
                                    </span>
                                </div>
                                <h3 style={{ margin: "12px 0 6px" }}>
                                    {concept.conceptName}
                                </h3>
                                <p>{concept.description}</p>
                            </article>
                        ))}
                    </div>
                </section>

                <section className="page-card">
                    <div className="page-header">
                        <div>
                            <h2 className="section-title">Relations</h2>
                            <p className="section-subtitle">
                                Football facts and scoring rules used by the
                                application.
                            </p>
                        </div>

                        <button className="ghost-button" onClick={loadRelations}>
                            Refresh
                        </button>
                    </div>

                    {relations.length === 0 && (
                        <div className="empty-state">
                            No ontology relations found.
                        </div>
                    )}

                    <div className="ontology-list">
                        {relations.map((relation) => (
                            <article key={relation.id} className="ontology-item">
                                <h3 style={{ marginBottom: "8px" }}>
                                    {relation.sourceConcept}
                                </h3>
                                <p className="muted">
                                    <strong>{relation.relationType}</strong>
                                </p>
                                <p>{relation.targetConcept}</p>
                            </article>
                        ))}
                    </div>
                </section>
            </div>
        </div>
    );
}
