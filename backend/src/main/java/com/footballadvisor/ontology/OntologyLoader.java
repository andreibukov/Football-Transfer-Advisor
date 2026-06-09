package com.footballadvisor.ontology;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class OntologyLoader {

    private OWLOntology ontology;

    public OWLOntology loadOntology() {
        try {
            if (ontology != null) {
                return ontology;
            }

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

            InputStream inputStream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("ontology/football-ontology.owl");

            if (inputStream == null) {
                throw new RuntimeException("Ontology file not found: ontology/football-ontology.owl");
            }

            ontology = manager.loadOntologyFromOntologyDocument(inputStream);

            System.out.println("[OWL] Ontology loaded successfully.");
            System.out.println("[OWL] Classes count: " + ontology.getClassesInSignature().size());
            System.out.println("[OWL] Object properties count: " + ontology.getObjectPropertiesInSignature().size());
            System.out.println("[OWL] Individuals count: " + ontology.getIndividualsInSignature().size());

            return ontology;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load ontology", e);
        }
    }
}