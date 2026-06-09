package com.footballadvisor.controller;

import com.footballadvisor.entity.OntologyConceptEntity;
import com.footballadvisor.entity.OntologyRelationEntity;
import com.footballadvisor.service.OntologyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.footballadvisor.ontology.OntologyQueryService;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/ontology")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class OntologyController {

    private final OntologyService ontologyService;
    private final OntologyQueryService ontologyQueryService;

    @GetMapping("/concepts")
    public List<OntologyConceptEntity> getAllConcepts() {
        return ontologyService.getAllConcepts();
    }

    @PostMapping("/concepts")
    public OntologyConceptEntity addConcept(@RequestBody OntologyConceptEntity concept) {
        return ontologyService.addConcept(concept);
    }

    @GetMapping("/concepts/search")
    public List<OntologyConceptEntity> searchConcepts(@RequestParam String name) {
        return ontologyService.searchConcepts(name);
    }

    @GetMapping("/concepts/type/{type}")
    public List<OntologyConceptEntity> getConceptsByType(@PathVariable String type) {
        return ontologyService.getConceptsByType(type);
    }

    @GetMapping("/relations")
    public List<OntologyRelationEntity> getAllRelations() {
        return ontologyService.getAllRelations();
    }

    @PostMapping("/relations")
    public OntologyRelationEntity addRelation(@RequestBody OntologyRelationEntity relation) {
        return ontologyService.addRelation(relation);
    }

    @GetMapping("/relations/source/{sourceConcept}")
    public List<OntologyRelationEntity> getRelationsBySourceConcept(@PathVariable String sourceConcept) {
        return ontologyService.getRelationsBySourceConcept(sourceConcept);
    }

    @GetMapping("/relations/type/{relationType}")
    public List<OntologyRelationEntity> getRelationsByType(@PathVariable String relationType) {
        return ontologyService.getRelationsByType(relationType);
    }
    @GetMapping("/attributes")
    public List<String> getRelatedAttributes(
            @RequestParam String concept,
            @RequestParam String property
    ) {
        return ontologyQueryService.findRelatedAttributes(concept, property);
    }

    @GetMapping("/individuals/{className}")
    public List<String> getIndividualsByClass(@PathVariable String className) {
        return ontologyQueryService.findIndividualsByClass(className);
    }
}
