package com.footballadvisor.ontology;

import org.semanticweb.owlapi.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.footballadvisor.entity.OntologyConceptEntity;
import com.footballadvisor.entity.OntologyRelationEntity;
import com.footballadvisor.repository.OntologyConceptRepository;
import com.footballadvisor.repository.OntologyRelationRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

@Service
public class OntologyQueryService {

    private final OntologyLoader ontologyLoader;
    private final OntologyConceptRepository ontologyConceptRepository;
    private final OntologyRelationRepository ontologyRelationRepository;

    public OntologyQueryService(OntologyLoader ontologyLoader) {
        this(ontologyLoader, null, null);
    }

    @Autowired
    public OntologyQueryService(
            OntologyLoader ontologyLoader,
            OntologyConceptRepository ontologyConceptRepository,
            OntologyRelationRepository ontologyRelationRepository
    ) {
        this.ontologyLoader = ontologyLoader;
        this.ontologyConceptRepository = ontologyConceptRepository;
        this.ontologyRelationRepository = ontologyRelationRepository;
    }

    public List<String> findRelatedAttributes(String conceptName, String propertyName) {
        OWLOntology ontology = ontologyLoader.loadOntology();
        List<String> results = new ArrayList<>();

        Optional<OWLNamedIndividual> concept = findIndividual(ontology, conceptName);

        concept.ifPresent(individual -> {
            for (OWLObjectPropertyAssertionAxiom axiom :
                    ontology.getObjectPropertyAssertionAxioms(individual)) {

                String currentPropertyName = getShortName(axiom.getProperty().asOWLObjectProperty().getIRI());

                if (currentPropertyName.equalsIgnoreCase(propertyName)) {
                    String targetName = normalizeIndividualName(
                            getShortName(axiom.getObject().asOWLNamedIndividual().getIRI())
                    );
                    results.add(targetName);
                }
            }
        });

        if (ontologyRelationRepository != null) {
            ontologyRelationRepository
                    .findBySourceConceptIgnoreCaseAndRelationTypeIgnoreCase(conceptName, propertyName)
                    .stream()
                    .map(OntologyRelationEntity::getTargetConcept)
                    .forEach(results::add);
        }

        return results.stream()
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    public boolean shareDirectParentClass(String firstClassName, String secondClassName) {
        if (firstClassName == null || secondClassName == null) {
            return false;
        }

        OWLOntology ontology = ontologyLoader.loadOntology();
        Set<String> firstParents = findDirectParentClasses(ontology, firstClassName);
        Set<String> secondParents = findDirectParentClasses(ontology, secondClassName);

        firstParents.remove("Position");
        secondParents.remove("Position");

        firstParents.retainAll(secondParents);

        return !firstParents.isEmpty();
    }

    public boolean areRelatedByObjectProperty(
            String firstConceptName,
            String secondConceptName,
            String propertyName
    ) {
        if (firstConceptName == null || secondConceptName == null || propertyName == null) {
            return false;
        }

        return hasObjectPropertyRelation(firstConceptName, secondConceptName, propertyName)
                || hasObjectPropertyRelation(secondConceptName, firstConceptName, propertyName);
    }

    public List<String> findIndividualsByClass(String className) {
        OWLOntology ontology = ontologyLoader.loadOntology();
        List<String> results = new ArrayList<>();

        for (OWLNamedIndividual individual : ontology.getIndividualsInSignature()) {
            boolean belongsToClass = ontology.getClassAssertionAxioms(individual)
                    .stream()
                    .filter(axiom -> !axiom.getClassExpression().isAnonymous())
                    .map(axiom -> getShortName(axiom.getClassExpression().asOWLClass().getIRI()))
                    .anyMatch(assertedClassName -> isClassOrSubclassOf(ontology, assertedClassName, className));

            if (belongsToClass) {
                results.add(normalizeIndividualName(getShortName(individual.getIRI())));
            }
        }

        if (ontologyConceptRepository != null) {
            ontologyConceptRepository.findByConceptTypeIgnoreCase(className)
                    .stream()
                    .map(OntologyConceptEntity::getConceptName)
                    .forEach(results::add);
        }

        return results.stream()
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    public OptionalDouble findNumericDataProperty(String individualName, String propertyName) {
        OWLOntology ontology = ontologyLoader.loadOntology();
        Optional<OWLNamedIndividual> concept = findIndividual(ontology, individualName);

        if (concept.isEmpty()) {
            return OptionalDouble.empty();
        }

        for (OWLDataPropertyAssertionAxiom axiom : ontology.getDataPropertyAssertionAxioms(concept.get())) {
            String currentPropertyName = getShortName(axiom.getProperty().asOWLDataProperty().getIRI());

            if (!currentPropertyName.equalsIgnoreCase(propertyName)) {
                continue;
            }

            try {
                return OptionalDouble.of(Double.parseDouble(axiom.getObject().getLiteral()));
            } catch (NumberFormatException ex) {
                return OptionalDouble.empty();
            }
        }

        return OptionalDouble.empty();
    }

    private boolean hasObjectPropertyRelation(
            String sourceConceptName,
            String targetConceptName,
            String propertyName
    ) {
        return findRelatedAttributes(sourceConceptName, propertyName)
                .stream()
                .anyMatch(relatedConceptName -> relatedConceptName.equalsIgnoreCase(targetConceptName));
    }

    private Set<String> findDirectParentClasses(OWLOntology ontology, String className) {
        Set<String> parentNames = new HashSet<>();
        Optional<OWLClass> owlClass = findClass(ontology, className);

        owlClass.ifPresent(currentClass -> {
            for (OWLSubClassOfAxiom axiom : ontology.getSubClassAxiomsForSubClass(currentClass)) {
                if (!axiom.getSuperClass().isAnonymous()) {
                    parentNames.add(getShortName(axiom.getSuperClass().asOWLClass().getIRI()));
                }
            }
        });

        return parentNames;
    }

    private boolean isClassOrSubclassOf(OWLOntology ontology, String className, String targetClassName) {
        if (className.equalsIgnoreCase(targetClassName)) {
            return true;
        }

        return findDirectParentClasses(ontology, className)
                .stream()
                .anyMatch(parentClassName -> isClassOrSubclassOf(ontology, parentClassName, targetClassName));
    }

    private Optional<OWLClass> findClass(OWLOntology ontology, String className) {
        return ontology.getClassesInSignature()
                .stream()
                .filter(owlClass -> getShortName(owlClass.getIRI()).equalsIgnoreCase(className))
                .findFirst();
    }

    private Optional<OWLNamedIndividual> findIndividual(OWLOntology ontology, String individualName) {
        Optional<OWLNamedIndividual> exactMatch = ontology.getIndividualsInSignature()
                .stream()
                .filter(individual -> getShortName(individual.getIRI()).equalsIgnoreCase(individualName))
                .findFirst();

        if (exactMatch.isPresent()) {
            return exactMatch;
        }

        Optional<OWLNamedIndividual> instanceMatch = ontology.getIndividualsInSignature()
                .stream()
                .filter(individual -> getShortName(individual.getIRI()).equalsIgnoreCase(individualName + "Instance"))
                .findFirst();

        if (instanceMatch.isPresent()) {
            return instanceMatch;
        }

        return ontology.getIndividualsInSignature()
                .stream()
                .filter(individual -> getShortName(individual.getIRI()).equalsIgnoreCase(individualName + "Option"))
                .findFirst();
    }

    private String normalizeIndividualName(String individualName) {
        if (individualName.endsWith("Instance")) {
            return individualName.substring(0, individualName.length() - "Instance".length());
        }

        if (individualName.endsWith("Option")) {
            return individualName.substring(0, individualName.length() - "Option".length());
        }

        return individualName;
    }

    private String getShortName(IRI iri) {
        String value = iri.toString();

        if (value.contains("#")) {
            return value.substring(value.indexOf("#") + 1);
        }

        return value.substring(value.lastIndexOf("/") + 1);
    }
}
