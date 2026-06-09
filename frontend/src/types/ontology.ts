export interface OntologyConcept {
    id?: number;
    conceptName: string;
    conceptType: string;
    description: string;
    source?: string;
    createdAt?: string;
}

export interface OntologyRelation {
    id?: number;
    sourceConcept: string;
    relationType: string;
    targetConcept: string;
    createdAt?: string;
}