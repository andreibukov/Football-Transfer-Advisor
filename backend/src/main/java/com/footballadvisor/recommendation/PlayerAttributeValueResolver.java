package com.footballadvisor.recommendation;

import com.footballadvisor.entity.PlayerEntity;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.OptionalInt;

@Component
public class PlayerAttributeValueResolver {

    public OptionalInt resolve(PlayerEntity player, String attributeName) {
        if (player == null || attributeName == null) {
            return OptionalInt.empty();
        }

        Integer value = switch (attributeName.toLowerCase(Locale.ROOT)) {
            case "speed" -> player.getSpeed();
            case "acceleration" -> player.getAcceleration();
            case "passing" -> player.getPassing();
            case "vision" -> player.getVision();
            case "ballcontrol" -> player.getBallControl();
            case "finishing" -> player.getFinishing();
            case "dribbling" -> player.getDribbling();
            case "stamina" -> player.getStamina();
            case "strength" -> player.getStrength();
            case "tackling" -> player.getTackling();
            case "positioning" -> player.getPositioning();
            case "workrate" -> player.getWorkRate();
            default -> null;
        };

        return value == null ? OptionalInt.empty() : OptionalInt.of(value);
    }
}
