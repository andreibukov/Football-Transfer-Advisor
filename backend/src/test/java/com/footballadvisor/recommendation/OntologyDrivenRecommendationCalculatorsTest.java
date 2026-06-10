package com.footballadvisor.recommendation;

import com.footballadvisor.entity.ClubEntity;
import com.footballadvisor.entity.OntologyConceptEntity;
import com.footballadvisor.entity.OntologyRelationEntity;
import com.footballadvisor.entity.PlayerEntity;
import com.footballadvisor.entity.TransferNeedEntity;
import com.footballadvisor.ontology.OntologyLoader;
import com.footballadvisor.ontology.OntologyQueryService;
import com.footballadvisor.repository.OntologyConceptRepository;
import com.footballadvisor.repository.OntologyRelationRepository;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OntologyDrivenRecommendationCalculatorsTest {

    private static final String BASE_IRI = "http://www.footballadvisor.com/ontology/football#";

    private final OntologyQueryService ontologyQueryService = new OntologyQueryService(new OntologyLoader());
    private final PositionMatchCalculator positionMatchCalculator = new PositionMatchCalculator(ontologyQueryService);
    private final StyleMatchCalculator styleMatchCalculator = new StyleMatchCalculator(
            ontologyQueryService,
            new PlayerAttributeValueResolver()
    );
    private final ScoringWeightProvider scoringWeightProvider = new ScoringWeightProvider(ontologyQueryService);
    private final BudgetMatchCalculator budgetMatchCalculator = new BudgetMatchCalculator();
    private final AgeMatchCalculator ageMatchCalculator = new AgeMatchCalculator(scoringWeightProvider);
    private final CandidateEligibilityService candidateEligibilityService = new CandidateEligibilityService();

    @Test
    void calculatesRelatedPositionFromOntologyParentClass() {
        double score = positionMatchCalculator.calculate("Striker", "LeftWinger");

        assertThat(score).isEqualTo(60);
    }

    @Test
    void calculatesStyleMatchFromOntologyRequiredAttributes() {
        PlayerEntity player = PlayerEntity.builder()
                .passing(90)
                .vision(80)
                .ballControl(70)
                .build();

        double score = styleMatchCalculator.calculate("Possession", player);

        assertThat(score).isEqualTo(80);
    }

    @Test
    void loadsUiOptionsFromOntologyIndividuals() {
        assertThat(ontologyQueryService.findIndividualsByClass("Position"))
                .contains("Striker", "LeftWinger", "DefensiveMidfielder");

        assertThat(ontologyQueryService.findIndividualsByClass("PlayingStyle"))
                .contains("Possession", "CounterAttack", "TikiTaka", "Gegenpressing");
    }

    @Test
    void loadsPlayerRolesAndRoleAttributesFromOntology() {
        assertThat(ontologyQueryService.findIndividualsByClass("PlayerRole"))
                .contains(
                        "DeepLyingPlaymaker",
                        "BallWinningMidfielder",
                        "TargetForward",
                        "FalseNine",
                        "WingBack",
                        "SweeperKeeper"
                );

        assertThat(ontologyQueryService.findRelatedAttributes("DefensiveMidfielder", "hasRole"))
                .contains("DeepLyingPlaymaker", "BallWinningMidfielder", "AnchorMan");

        assertThat(ontologyQueryService.findRelatedAttributes("Striker", "hasRole"))
                .contains("TargetForward", "PressingForward", "Poacher", "FalseNine");

        assertThat(ontologyQueryService.findRelatedAttributes("RightBack", "hasRole"))
                .contains("WingBack", "InvertedFullBack", "AttackingFullBack");

        assertThat(ontologyQueryService.findRelatedAttributes("Goalkeeper", "hasRole"))
                .containsExactly("SweeperKeeper");

        assertThat(ontologyQueryService.findRelatedAttributes("DeepLyingPlaymaker", "requiresAttribute"))
                .containsExactly("Passing", "Positioning", "Vision");

        assertThat(ontologyQueryService.findRelatedAttributes("FalseNine", "requiresAttribute"))
                .containsExactly("BallControl", "Passing", "Vision");
    }

    @Test
    void loadsScoringWeightsFromOntologyProfile() {
        ScoringWeights weights = scoringWeightProvider.getDefaultWeights();

        assertThat(weights.positionMatchWeight()).isEqualTo(0.40);
        assertThat(weights.styleMatchWeight()).isEqualTo(0.30);
        assertThat(weights.budgetMatchWeight()).isEqualTo(0.15);
        assertThat(weights.ageMatchWeight()).isEqualTo(0.15);
        assertThat(scoringWeightProvider.getAgePenaltyPerYear()).isEqualTo(10);
    }

    @Test
    void ontologyContainsReasoningAxiomsAndPropertySemantics() {
        OWLOntology ontology = new OntologyLoader().loadOntology();
        OWLDataFactory dataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();

        OWLClass goalkeeper = dataFactory.getOWLClass(IRI.create(BASE_IRI + "Goalkeeper"));
        OWLClass defender = dataFactory.getOWLClass(IRI.create(BASE_IRI + "Defender"));
        OWLClass midfielder = dataFactory.getOWLClass(IRI.create(BASE_IRI + "Midfielder"));
        OWLClass forward = dataFactory.getOWLClass(IRI.create(BASE_IRI + "Forward"));
        OWLClass highPressingForwardCandidate = dataFactory.getOWLClass(
                IRI.create(BASE_IRI + "HighPressingForwardCandidate")
        );

        OWLObjectProperty belongsToClub = dataFactory.getOWLObjectProperty(IRI.create(BASE_IRI + "belongsToClub"));
        OWLObjectProperty hasPlayer = dataFactory.getOWLObjectProperty(IRI.create(BASE_IRI + "hasPlayer"));
        OWLDataProperty hasAgeMatchWeight = dataFactory.getOWLDataProperty(IRI.create(BASE_IRI + "hasAgeMatchWeight"));

        assertThat(ontology.getAxioms(AxiomType.DISJOINT_CLASSES))
                .anySatisfy(axiom -> assertThat(axiom.getClassesInSignature())
                        .contains(goalkeeper, defender, midfielder, forward));

        assertThat(ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES))
                .anySatisfy(axiom -> assertThat(axiom.getClassesInSignature())
                        .contains(highPressingForwardCandidate, forward));

        assertThat(ontology.getAxioms(AxiomType.INVERSE_OBJECT_PROPERTIES))
                .anySatisfy(axiom -> assertThat(axiom.getObjectPropertiesInSignature())
                        .contains(belongsToClub, hasPlayer));

        assertThat(ontology.getAxioms(AxiomType.FUNCTIONAL_OBJECT_PROPERTY))
                .anySatisfy(axiom -> assertThat(axiom.getObjectPropertiesInSignature()).contains(belongsToClub));

        assertThat(ontology.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN))
                .anySatisfy(axiom -> assertThat(axiom.getDataPropertiesInSignature()).contains(hasAgeMatchWeight));

        assertThat(ontology.getAxioms(AxiomType.DATA_PROPERTY_RANGE))
                .anySatisfy(axiom -> assertThat(axiom.getDataPropertiesInSignature()).contains(hasAgeMatchWeight));
    }

    @Test
    void decreasesBudgetMatchGraduallyWhenPlayerIsAboveBudget() {
        double score = budgetMatchCalculator.calculate(
                BigDecimal.valueOf(80_000_000),
                BigDecimal.valueOf(85_000_000)
        );

        assertThat(score).isEqualTo(93.75);
    }

    @Test
    void decreasesAgeMatchGraduallyInBothDirectionsFromPreferredAge() {
        assertThat(ageMatchCalculator.calculate(25, 25)).isEqualTo(100);
        assertThat(ageMatchCalculator.calculate(25, 21)).isEqualTo(60);
        assertThat(ageMatchCalculator.calculate(25, 29)).isEqualTo(60);
    }

    @Test
    void excludesPlayersFromTheRequestingClub() {
        TransferNeedEntity transferNeed = TransferNeedEntity.builder()
                .club(ClubEntity.builder().name("Manchester City").build())
                .build();

        PlayerEntity currentClubPlayer = PlayerEntity.builder()
                .currentClub("manchester city")
                .build();

        PlayerEntity externalPlayer = PlayerEntity.builder()
                .currentClub("Arsenal")
                .build();

        assertThat(candidateEligibilityService.isEligible(transferNeed, currentClubPlayer)).isFalse();
        assertThat(candidateEligibilityService.isEligible(transferNeed, externalPlayer)).isTrue();
    }

    @Test
    void combinesOwlKnowledgeWithUserDefinedOntologyOverlay() {
        OntologyConceptRepository conceptRepository = mock(OntologyConceptRepository.class);
        OntologyRelationRepository relationRepository = mock(OntologyRelationRepository.class);

        when(conceptRepository.findByConceptTypeIgnoreCase("PlayingStyle"))
                .thenReturn(List.of(OntologyConceptEntity.builder()
                        .conceptName("DirectFootball")
                        .conceptType("PlayingStyle")
                        .build()));

        when(relationRepository.findBySourceConceptIgnoreCaseAndRelationTypeIgnoreCase(
                "DirectFootball",
                "styleRequiresAttribute"
        )).thenReturn(List.of(OntologyRelationEntity.builder()
                .sourceConcept("DirectFootball")
                .relationType("styleRequiresAttribute")
                .targetConcept("Speed")
                .build()));

        OntologyQueryService overlayQueryService = new OntologyQueryService(
                new OntologyLoader(),
                conceptRepository,
                relationRepository
        );

        assertThat(overlayQueryService.findIndividualsByClass("PlayingStyle"))
                .contains("DirectFootball", "Possession");

        assertThat(overlayQueryService.findRelatedAttributes("DirectFootball", "styleRequiresAttribute"))
                .containsExactly("Speed");
    }
}
