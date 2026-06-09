package com.footballadvisor.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AgeMatchCalculator {

    private final ScoringWeightProvider scoringWeightProvider;

    public double calculate(Integer preferredAge, Integer playerAge) {
        if (preferredAge == null || playerAge == null) {
            return 0;
        }

        if (preferredAge <= 0 || playerAge <= 0) {
            return 0;
        }

        int ageDifference = Math.abs(playerAge - preferredAge);
        double agePenalty = ageDifference * scoringWeightProvider.getAgePenaltyPerYear();

        return Math.max(0, 100 - agePenalty);
    }
}
