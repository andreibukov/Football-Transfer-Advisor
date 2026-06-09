import apiClient from "./apiClient";
import { OntologyConcept, OntologyRelation } from "../types/ontology";

export const getAllOntologyConcepts = async () => {
    const response = await apiClient.get<OntologyConcept[]>("/ontology/concepts");
    return response.data;
};

export const getAllOntologyRelations = async () => {
    const response = await apiClient.get<OntologyRelation[]>("/ontology/relations");
    return response.data;
};

export const createOntologyConcept = async (concept: OntologyConcept) => {
    const response = await apiClient.post<OntologyConcept>(
        "/ontology/concepts",
        concept
    );

    return response.data;
};

export const createOntologyRelation = async (relation: OntologyRelation) => {
    const response = await apiClient.post<OntologyRelation>(
        "/ontology/relations",
        relation
    );

    return response.data;
};

export const getOntologyIndividualsByClass = async (className: string) => {
    const response = await apiClient.get<string[]>(
        `/ontology/individuals/${className}`
    );

    return response.data;
};

export const getRelatedOntologyTargets = async (
    concept: string,
    property: string
) => {
    const response = await apiClient.get<string[]>("/ontology/attributes", {
        params: { concept, property },
    });

    return response.data;
};
