package com.footballadvisor.recommendation;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationScore {

    private double positionMatch;
    private double styleMatch;
    private double roleMatch;
    private double budgetMatch;
    private double ageMatch;
    private double totalScore;
}
