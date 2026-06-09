package com.footballadvisor.dto;

import com.footballadvisor.entity.PlayerEntity;

import java.util.List;

public record RecommendationResponse(
        Long id,
        PlayerEntity player,
        Double positionMatch,
        Double styleMatch,
        Double roleMatch,
        Double budgetMatch,
        Double ageMatch,
        Double totalScore,
        List<String> reasons
) {
}
