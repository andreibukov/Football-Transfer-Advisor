package com.footballadvisor.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendationViabilityService {

    private final ScoringWeightProvider scoringWeightProvider;

    public boolean isViable(RecommendationScore score) {
        return isFinanciallyViable(score) && isAgeProfileViable(score);
    }

    private boolean isFinanciallyViable(RecommendationScore score) {
        return score.getBudgetMatch() >= scoringWeightProvider.getMinimumBudgetMatchForRecommendation();
    }

    private boolean isAgeProfileViable(RecommendationScore score) {
        return score.getAgeMatch() >= scoringWeightProvider.getMinimumAgeMatchForRecommendation();
    }
}
