package com.footballadvisor.recommendation;

import com.footballadvisor.entity.PlayerEntity;
import com.footballadvisor.entity.TransferNeedEntity;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class CandidateEligibilityService {

    public boolean isEligible(TransferNeedEntity transferNeed, PlayerEntity player) {
        if (transferNeed == null || player == null) {
            return false;
        }

        if (transferNeed.getClub() == null) {
            return true;
        }

        return !normalize(transferNeed.getClub().getName())
                .equals(normalize(player.getCurrentClub()));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}
