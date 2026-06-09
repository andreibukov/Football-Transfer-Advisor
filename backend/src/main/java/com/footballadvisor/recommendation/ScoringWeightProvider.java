package com.footballadvisor.recommendation;

import com.footballadvisor.ontology.OntologyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScoringWeightProvider {

    private static final String DEFAULT_PROFILE = "DefaultScoringProfile";

    private final OntologyQueryService ontologyQueryService;

    public ScoringWeights getDefaultWeights() {
        ScoringWeights weights = new ScoringWeights(
                readWeight("hasPositionMatchWeight"),
                readWeight("hasStyleMatchWeight"),
                readWeight("hasBudgetMatchWeight"),
                readWeight("hasAgeMatchWeight")
        );

        validateWeights(weights);

        return weights;
    }

    public double getAgePenaltyPerYear() {
        return readWeight("hasAgePenaltyPerYear");
    }

    private double readWeight(String propertyName) {
        return ontologyQueryService.findNumericDataProperty(DEFAULT_PROFILE, propertyName)
                .orElseThrow(() -> new IllegalStateException(
                        "Missing scoring weight in ontology: " + propertyName
                ));
    }

    private void validateWeights(ScoringWeights weights) {
        double totalWeight = weights.positionMatchWeight()
                + weights.styleMatchWeight()
                + weights.budgetMatchWeight()
                + weights.ageMatchWeight();

        if (Math.abs(totalWeight - 1.0) > 0.0001) {
            throw new IllegalStateException("Scoring weights must add up to 1.0");
        }
    }
}
