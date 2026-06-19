package com.footballadvisor.ontology;

import lombok.RequiredArgsConstructor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OntologyFileMutationService {

    private static final String BASE_IRI = "http://www.footballadvisor.com/ontology/football#";
    private static final String ONTOLOGY_RESOURCE = "ontology/football-ontology.owl";
    private static final Path SOURCE_ONTOLOGY_PATH =
            Paths.get("src/main/resources/ontology/football-ontology.owl");
    private static final Path REPOSITORY_ONTOLOGY_PATH =
            Paths.get("backend/src/main/resources/ontology/football-ontology.owl");

    private final OntologyLoader ontologyLoader;

    public synchronized void addConcept(String conceptName, String conceptType) {
        if (isBlank(conceptName) || isBlank(conceptType)) {
            return;
        }

        mutate(ontology -> {
            OWLOntologyManager manager = ontology.getOWLOntologyManager();
            OWLDataFactory dataFactory = manager.getOWLDataFactory();

            OWLClass conceptClass = dataFactory.getOWLClass(toIri(conceptType));
            OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(
                    toIri(toIndividualName(conceptName, conceptType))
            );

            OWLDeclarationAxiom classDeclaration = dataFactory.getOWLDeclarationAxiom(conceptClass);
            OWLDeclarationAxiom individualDeclaration = dataFactory.getOWLDeclarationAxiom(individual);
            OWLClassAssertionAxiom classAssertion =
                    dataFactory.getOWLClassAssertionAxiom(conceptClass, individual);

            manager.addAxiom(ontology, classDeclaration);
            manager.addAxiom(ontology, individualDeclaration);
            manager.addAxiom(ontology, classAssertion);
        });
    }

    public synchronized void addRelation(String sourceConcept, String relationType, String targetConcept) {
        if (isBlank(sourceConcept) || isBlank(relationType) || isBlank(targetConcept)) {
            return;
        }

        mutate(ontology -> {
            OWLOntologyManager manager = ontology.getOWLOntologyManager();
            OWLDataFactory dataFactory = manager.getOWLDataFactory();

            OWLNamedIndividual source = dataFactory.getOWLNamedIndividual(
                    toIri(resolveIndividualName(ontology, sourceConcept))
            );
            OWLNamedIndividual target = dataFactory.getOWLNamedIndividual(
                    toIri(resolveIndividualName(ontology, targetConcept))
            );
            OWLObjectProperty property = dataFactory.getOWLObjectProperty(toIri(relationType));

            manager.addAxiom(ontology, dataFactory.getOWLDeclarationAxiom(source));
            manager.addAxiom(ontology, dataFactory.getOWLDeclarationAxiom(target));
            manager.addAxiom(ontology, dataFactory.getOWLDeclarationAxiom(property));

            OWLObjectPropertyAssertionAxiom assertion =
                    dataFactory.getOWLObjectPropertyAssertionAxiom(property, source, target);
            manager.addAxiom(ontology, assertion);
        });
    }

    private void mutate(OntologyMutation mutation) {
        try {
            Path primaryOntologyPath = resolvePrimaryOntologyPath();
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(primaryOntologyPath.toFile());

            mutation.apply(ontology);

            for (Path savePath : resolveSaveTargets(primaryOntologyPath)) {
                Files.createDirectories(savePath.getParent());
                manager.saveOntology(
                        ontology,
                        new RDFXMLDocumentFormat(),
                        IRI.create(savePath.toUri())
                );
            }

            ontologyLoader.clearCache();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to update OWL ontology file", ex);
        }
    }

    private Path resolvePrimaryOntologyPath() throws Exception {
        URL resource = getClass().getClassLoader().getResource(ONTOLOGY_RESOURCE);

        if (resource != null && "file".equalsIgnoreCase(resource.getProtocol())) {
            return Paths.get(resource.toURI());
        }

        Optional<Path> sourcePath = resolveSourcePath();

        if (sourcePath.isPresent()) {
            return sourcePath.get();
        }

        throw new IllegalStateException("Writable ontology file was not found.");
    }

    private Set<Path> resolveSaveTargets(Path primaryOntologyPath) {
        Set<Path> targets = new LinkedHashSet<>();
        targets.add(primaryOntologyPath);
        resolveSourcePath().ifPresent(targets::add);
        return targets;
    }

    private Optional<Path> resolveSourcePath() {
        if (Files.exists(SOURCE_ONTOLOGY_PATH)) {
            return Optional.of(SOURCE_ONTOLOGY_PATH);
        }

        if (Files.exists(REPOSITORY_ONTOLOGY_PATH)) {
            return Optional.of(REPOSITORY_ONTOLOGY_PATH);
        }

        return Optional.empty();
    }

    private String resolveIndividualName(OWLOntology ontology, String conceptName) {
        String sanitizedName = sanitizeName(conceptName);

        return ontology.getIndividualsInSignature()
                .stream()
                .map(individual -> shortName(individual.getIRI()))
                .filter(individualName -> normalizeIndividualName(individualName).equalsIgnoreCase(sanitizedName))
                .findFirst()
                .orElseGet(() -> sanitizedName);
    }

    private String toIndividualName(String conceptName, String conceptType) {
        String sanitizedName = sanitizeName(conceptName);

        if (sanitizedName.endsWith("Option") || sanitizedName.endsWith("Instance")) {
            return sanitizedName;
        }

        String normalizedType = conceptType.trim().toLowerCase(Locale.ROOT);

        if (normalizedType.equals("position")
                || normalizedType.equals("playerattribute")
                || normalizedType.equals("playingstyle")) {
            return sanitizedName + "Option";
        }

        return sanitizedName;
    }

    private String normalizeIndividualName(String individualName) {
        if (individualName.endsWith("Option")) {
            return individualName.substring(0, individualName.length() - "Option".length());
        }

        if (individualName.endsWith("Instance")) {
            return individualName.substring(0, individualName.length() - "Instance".length());
        }

        return individualName;
    }

    private IRI toIri(String shortName) {
        return IRI.create(BASE_IRI + sanitizeName(shortName));
    }

    private String sanitizeName(String value) {
        String sanitized = value.trim().replaceAll("[^A-Za-z0-9_]", "");

        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("Ontology names must contain at least one letter or digit.");
        }

        return sanitized;
    }

    private String shortName(IRI iri) {
        String value = iri.toString();
        return value.substring(value.indexOf("#") + 1);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @FunctionalInterface
    private interface OntologyMutation {
        void apply(OWLOntology ontology);
    }
}
