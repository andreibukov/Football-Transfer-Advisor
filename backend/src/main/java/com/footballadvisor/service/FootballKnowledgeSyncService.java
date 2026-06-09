package com.footballadvisor.service;

import com.footballadvisor.entity.ClubEntity;
import com.footballadvisor.entity.OntologyConceptEntity;
import com.footballadvisor.entity.OntologyRelationEntity;
import com.footballadvisor.entity.PlayerEntity;
import com.footballadvisor.repository.OntologyConceptRepository;
import com.footballadvisor.repository.OntologyRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FootballKnowledgeSyncService {

    private static final String DATA_SOURCE = "DATA_SYNC";

    private final OntologyConceptRepository ontologyConceptRepository;
    private final OntologyRelationRepository ontologyRelationRepository;

    public void syncClub(ClubEntity club) {
        if (club == null) {
            return;
        }

        saveConcept(
                club.getName(),
                "Club",
                "Club created from Football Data."
        );
        saveConcept(
                club.getLeague(),
                "League",
                "League linked from Football Data."
        );
        saveRelation(club.getName(), "playsInLeague", club.getLeague());
        saveRelation(club.getName(), "prefersPlayingStyle", club.getPreferredStyle());
    }

    public void syncPlayer(PlayerEntity player) {
        if (player == null) {
            return;
        }

        saveConcept(
                player.getName(),
                "Player",
                "Player created from Football Data."
        );
        saveRelation(player.getName(), "playsForClub", player.getCurrentClub());
        saveRelation(player.getName(), "hasPosition", player.getPosition());
    }

    private void saveConcept(String conceptName, String conceptType, String description) {
        if (isBlank(conceptName) || isBlank(conceptType)) {
            return;
        }

        if (ontologyConceptRepository.existsByConceptNameIgnoreCaseAndConceptTypeIgnoreCase(
                conceptName,
                conceptType
        )) {
            return;
        }

        ontologyConceptRepository.save(OntologyConceptEntity.builder()
                .conceptName(conceptName)
                .conceptType(conceptType)
                .description(description)
                .source(DATA_SOURCE)
                .build());
    }

    private void saveRelation(String sourceConcept, String relationType, String targetConcept) {
        if (isBlank(sourceConcept) || isBlank(relationType) || isBlank(targetConcept)) {
            return;
        }

        if (ontologyRelationRepository
                .existsBySourceConceptIgnoreCaseAndRelationTypeIgnoreCaseAndTargetConceptIgnoreCase(
                        sourceConcept,
                        relationType,
                        targetConcept
                )) {
            return;
        }

        ontologyRelationRepository.save(OntologyRelationEntity.builder()
                .sourceConcept(sourceConcept)
                .relationType(relationType)
                .targetConcept(targetConcept)
                .build());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
