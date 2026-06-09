package com.footballadvisor.recommendation;

import com.footballadvisor.entity.PlayerEntity;
import com.footballadvisor.ontology.OntologyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.OptionalInt;

@Component
@RequiredArgsConstructor
public class RoleMatchCalculator {

    private final OntologyQueryService ontologyQueryService;
    private final PlayerAttributeValueResolver playerAttributeValueResolver;

    public double calculate(String preferredRole, PlayerEntity player) {
        if (preferredRole == null || preferredRole.isBlank() || player == null) {
            return 0;
        }

        List<String> requiredAttributes = ontologyQueryService.findRelatedAttributes(
                preferredRole,
                "requiresAttribute"
        );

        int sum = 0;
        int count = 0;

        for (String attributeName : requiredAttributes) {
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
