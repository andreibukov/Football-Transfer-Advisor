package com.footballadvisor.service;

import com.footballadvisor.dto.RecommendationResponse;
import com.footballadvisor.entity.*;
import com.footballadvisor.ontology.OntologyQueryService;
import com.footballadvisor.recommendation.CandidateEligibilityService;
import com.footballadvisor.recommendation.RecommendationEngine;
import com.footballadvisor.recommendation.RecommendationScore;
import com.footballadvisor.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final TransferNeedRepository transferNeedRepository;
    private final PlayerRepository playerRepository;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationReasonRepository recommendationReasonRepository;
    private final RecommendationEngine recommendationEngine;
    private final CandidateEligibilityService candidateEligibilityService;
    private final OntologyQueryService ontologyQueryService;

    public List<RecommendationResponse> generateRecommendations(Long transferNeedId) {
        TransferNeedEntity transferNeed = transferNeedRepository.findById(transferNeedId)
                .orElseThrow(() -> new RuntimeException("Transfer need not found with id: " + transferNeedId));

        List<PlayerEntity> players = playerRepository.findAll();

        recommendationRepository
                .findByTransferNeedIdOrderByTotalScoreDesc(transferNeedId)
                .forEach(recommendation -> {
                    recommendationReasonRepository
                            .findByRecommendationId(recommendation.getId())
                            .forEach(recommendationReasonRepository::delete);

                    recommendationRepository.delete(recommendation);
                });

        for (PlayerEntity player : players) {
            if (!candidateEligibilityService.isEligible(transferNeed, player)) {
                continue;
            }

            RecommendationScore score = recommendationEngine.calculate(transferNeed, player);

            if (score.getTotalScore() <= 0) {
                continue;
            }

            RecommendationEntity recommendation = RecommendationEntity.builder()
                    .transferNeed(transferNeed)
                    .player(player)
                    .positionMatch(score.getPositionMatch())
                    .styleMatch(score.getStyleMatch())
                    .roleMatch(score.getRoleMatch())
                    .budgetMatch(score.getBudgetMatch())
                    .ageMatch(score.getAgeMatch())
                    .totalScore(score.getTotalScore())
                    .build();

            RecommendationEntity savedRecommendation = recommendationRepository.save(recommendation);

            saveReasons(savedRecommendation, score, transferNeed, player);
        }

        return getRecommendations(transferNeedId);
    }

    public List<RecommendationResponse> getRecommendations(Long transferNeedId) {
        return recommendationRepository.findByTransferNeedIdOrderByTotalScoreDesc(transferNeedId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private RecommendationResponse toResponse(RecommendationEntity recommendation) {
        List<String> reasons = recommendationReasonRepository
                .findByRecommendationId(recommendation.getId())
                .stream()
                .map(RecommendationReasonEntity::getReasonText)
                .toList();

        return new RecommendationResponse(
                recommendation.getId(),
                recommendation.getPlayer(),
                recommendation.getPositionMatch(),
                recommendation.getStyleMatch(),
                recommendation.getRoleMatch(),
                recommendation.getBudgetMatch(),
                recommendation.getAgeMatch(),
                recommendation.getTotalScore(),
                reasons
        );
    }

    private void saveReasons(
            RecommendationEntity recommendation,
            RecommendationScore score,
            TransferNeedEntity transferNeed,
            PlayerEntity player
    ) {
        if (score.getPositionMatch() == 100) {
            saveReason(recommendation, "Position Match: exact match. "
                    + player.getName()
                    + " plays as "
                    + player.getPosition()
                    + ", which matches the requested "
                    + transferNeed.getNeededPosition()
                    + ".");
        } else if (score.getPositionMatch() == 60) {
            saveReason(recommendation, "Position Match: related position. "
                    + player.getPosition()
                    + " is ontologically related to "
                    + transferNeed.getNeededPosition()
                    + ".");
        } else {
            saveReason(recommendation, "Position Match: weak fit. "
                    + player.getPosition()
                    + " is not directly related to "
                    + transferNeed.getNeededPosition()
                    + ".");
        }

        List<String> styleAttributes = ontologyQueryService.findRelatedAttributes(
                transferNeed.getPlayingStyle(),
                "styleRequiresAttribute"
        );

        if (score.getStyleMatch() >= 80) {
            saveReason(recommendation, "Style Match: strong fit for "
                    + transferNeed.getPlayingStyle()
                    + ". The ontology links this style to "
                    + String.join(", ", styleAttributes)
                    + ", and the player's average attribute score is "
                    + formatScore(score.getStyleMatch())
                    + "%.");
        } else if (score.getStyleMatch() >= 60) {
            saveReason(recommendation, "Style Match: partial fit for "
                    + transferNeed.getPlayingStyle()
                    + ". Relevant ontology attributes: "
                    + String.join(", ", styleAttributes)
                    + ". Player score: "
                    + formatScore(score.getStyleMatch())
                    + "%.");
        } else if (styleAttributes.isEmpty()) {
            saveReason(recommendation, "Style Match: no score because "
                    + transferNeed.getPlayingStyle()
                    + " has no styleRequiresAttribute relation in the ontology overlay.");
        } else {
            saveReason(recommendation, "Style Match: low fit for "
                    + transferNeed.getPlayingStyle()
                    + ". Relevant ontology attributes: "
                    + String.join(", ", styleAttributes)
                    + ". Player score: "
                    + formatScore(score.getStyleMatch())
                    + "%.");
        }

        if (transferNeed.getPreferredRole() != null && !transferNeed.getPreferredRole().isBlank()) {
            List<String> roleAttributes = ontologyQueryService.findRelatedAttributes(
                    transferNeed.getPreferredRole(),
                    "requiresAttribute"
            );

            if (roleAttributes.isEmpty()) {
                saveReason(recommendation, "Role Match: no score because "
                        + transferNeed.getPreferredRole()
                        + " has no requiresAttribute relation in the ontology overlay.");
            } else {
                saveReason(recommendation, "Role Match: "
                        + transferNeed.getPreferredRole()
                        + " requires "
                        + String.join(", ", roleAttributes)
                        + ". Player role fit is "
                        + formatScore(score.getRoleMatch())
                        + "% and is blended into the tactical Style Match score.");
            }
        }

        if (score.getBudgetMatch() == 100) {
            saveReason(recommendation, "Budget Match: within budget. Market value "
                    + formatCurrency(player.getMarketValue())
                    + " is not above the selected budget "
                    + formatCurrency(transferNeed.getMaxBudget())
                    + ".");
        } else if (score.getBudgetMatch() > 0) {
            saveReason(recommendation, "Budget Match: above budget but still comparable. Market value "
                    + formatCurrency(player.getMarketValue())
                    + " vs selected budget "
                    + formatCurrency(transferNeed.getMaxBudget())
                    + ", producing "
                    + formatScore(score.getBudgetMatch())
                    + "%.");
        } else {
            saveReason(recommendation, "Budget Match: too far above the selected budget.");
        }

        if (score.getAgeMatch() == 100) {
            saveReason(recommendation, "Age Match: exact preferred age. Player age "
                    + player.getAge()
                    + " matches "
                    + transferNeed.getMaxAge()
                    + ".");
        } else if (score.getAgeMatch() > 0) {
            int ageDifference = Math.abs(player.getAge() - transferNeed.getMaxAge());
            saveReason(recommendation, "Age Match: "
                    + ageDifference
                    + " years away from preferred age "
                    + transferNeed.getMaxAge()
                    + ", producing "
                    + formatScore(score.getAgeMatch())
                    + "%.");
        } else {
            saveReason(recommendation, "Age Match: too far from the preferred age.");
        }
    }

    private void saveReason(RecommendationEntity recommendation, String reasonText) {
        RecommendationReasonEntity reason = RecommendationReasonEntity.builder()
                .recommendation(recommendation)
                .reasonText(reasonText)
                .build();

        recommendationReasonRepository.save(reason);
    }

    private String formatScore(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "N/A";
        }

        return "EUR " + value
                .setScale(0, RoundingMode.HALF_UP)
                .toPlainString();
    }
}
