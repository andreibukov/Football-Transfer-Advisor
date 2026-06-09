package com.footballadvisor.recommendation;

import com.footballadvisor.entity.PlayerEntity;
import com.footballadvisor.entity.TransferNeedEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendationEngine {

    private final PositionMatchCalculator positionMatchCalculator;
    private final StyleMatchCalculator styleMatchCalculator;
    private final RoleMatchCalculator roleMatchCalculator;
    private final BudgetMatchCalculator budgetMatchCalculator;
    private final AgeMatchCalculator ageMatchCalculator;
    private final ScoringWeightProvider scoringWeightProvider;

    public RecommendationScore calculate(TransferNeedEntity transferNeed, PlayerEntity player) {
        double positionMatch = positionMatchCalculator.calculate(
                transferNeed.getNeededPosition(),
                player.getPosition()
        );

        double styleMatch = styleMatchCalculator.calculate(
                transferNeed.getPlayingStyle(),
                player
        );

        double roleMatch = roleMatchCalculator.calculate(
                transferNeed.getPreferredRole(),
                player
        );

        double tacticalMatch = transferNeed.getPreferredRole() == null
                || transferNeed.getPreferredRole().isBlank()
                ? styleMatch
                : (styleMatch + roleMatch) / 2;

        double budgetMatch = budgetMatchCalculator.calculate(
                transferNeed.getMaxBudget(),
                player.getMarketValue()
        );

        double ageMatch = ageMatchCalculator.calculate(
                transferNeed.getMaxAge(),
                player.getAge()
        );

        ScoringWeights weights = scoringWeightProvider.getDefaultWeights();

        double totalScore =
                (positionMatch * weights.positionMatchWeight())
                        + (tacticalMatch * weights.styleMatchWeight())
                        + (budgetMatch * weights.budgetMatchWeight())
                        + (ageMatch * weights.ageMatchWeight());

        return RecommendationScore.builder()
                .positionMatch(positionMatch)
                .styleMatch(tacticalMatch)
                .roleMatch(roleMatch)
                .budgetMatch(budgetMatch)
                .ageMatch(ageMatch)
                .totalScore(totalScore)
                .build();
    }
}
