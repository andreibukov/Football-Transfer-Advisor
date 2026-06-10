package com.footballadvisor.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FinancialViabilityService {

    private final ScoringWeightProvider scoringWeightProvider;

    public boolean isViable(RecommendationScore score) {
        return score.getBudgetMatch() >= scoringWeightProvider.getMinimumBudgetMatchForRecommendation();
    }
}
