package com.footballadvisor.service;

import com.footballadvisor.entity.OntologyConceptEntity;
import com.footballadvisor.entity.OntologyRelationEntity;
import com.footballadvisor.repository.OntologyConceptRepository;
import com.footballadvisor.repository.OntologyRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OntologyService {

    private final OntologyConceptRepository ontologyConceptRepository;
    private final OntologyRelationRepository ontologyRelationRepository;

    public List<OntologyConceptEntity> getAllConcepts() {
        return ontologyConceptRepository.findAll();
    }

    public OntologyConceptEntity addConcept(OntologyConceptEntity concept) {
        return ontologyConceptRepository.save(concept);
    }

    public List<OntologyConceptEntity> searchConcepts(String name) {
        return ontologyConceptRepository.findByConceptNameContainingIgnoreCase(name);
    }

    public List<OntologyConceptEntity> getConceptsByType(String type) {
        return ontologyConceptRepository.findByConceptTypeIgnoreCase(type);
    }

    public List<OntologyRelationEntity> getAllRelations() {
        return ontologyRelationRepository.findAll();
    }

    public OntologyRelationEntity addRelation(OntologyRelationEntity relation) {
        return ontologyRelationRepository.save(relation);
    }

    public List<OntologyRelationEntity> getRelationsBySourceConcept(String sourceConcept) {
        return ontologyRelationRepository.findBySourceConceptIgnoreCase(sourceConcept);
    }

    public List<OntologyRelationEntity> getRelationsByType(String relationType) {
        return ontologyRelationRepository.findByRelationTypeIgnoreCase(relationType);
    }
}