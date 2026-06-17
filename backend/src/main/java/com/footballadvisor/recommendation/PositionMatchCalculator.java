package com.footballadvisor.recommendation;

import com.footballadvisor.ontology.OntologyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PositionMatchCalculator {

    private final OntologyQueryService ontologyQueryService;

    public double calculate(String neededPosition, String playerPosition) {
        if (neededPosition == null || playerPosition == null) {
            return 0;
        }

        if (neededPosition.equalsIgnoreCase(playerPosition)) {
            return 100;
        }

        if (ontologyQueryService.areRelatedByObjectProperty(
                neededPosition,
                playerPosition,
                "isSimilarPositionTo"
        )) {
            return 75;
        }

        if (ontologyQueryService.shareDirectParentClass(neededPosition, playerPosition)) {
            return 60;
        }

        return 0;
    }
}
