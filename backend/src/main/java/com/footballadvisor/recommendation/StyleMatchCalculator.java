package com.footballadvisor.recommendation;

import com.footballadvisor.entity.PlayerEntity;
import com.footballadvisor.ontology.OntologyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.OptionalInt;

@Component
@RequiredArgsConstructor
public class StyleMatchCalculator {

    private final OntologyQueryService ontologyQueryService;
    private final PlayerAttributeValueResolver playerAttributeValueResolver;

    public double calculate(String playingStyle, PlayerEntity player) {
        if (playingStyle == null || player == null) {
            return 0;
        }

        List<String> requiredAttributes = ontologyQueryService.findRelatedAttributes(
                playingStyle,
                "styleRequiresAttribute"
        );

        return average(player, requiredAttributes);
    }

    private double average(PlayerEntity player, List<String> attributeNames) {
        int sum = 0;
        int count = 0;

        for (String attributeName : attributeNames) {
            OptionalInt value = playerAttributeValueResolver.resolve(player, attributeName);

            if (value.isPresent()) {
                sum += value.getAsInt();
                count++;
            }
        }

        if (count == 0) {
            return 0;
        }

        return sum / (double) count;
    }
}
