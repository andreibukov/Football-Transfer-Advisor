package com.footballadvisor.recommendation;

public record ScoringWeights(
        double positionMatchWeight,
        double styleMatchWeight,
        double budgetMatchWeight,
        double ageMatchWeight
) {
}
