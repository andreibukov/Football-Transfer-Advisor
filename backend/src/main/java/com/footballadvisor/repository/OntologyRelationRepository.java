package com.footballadvisor.repository;

import com.footballadvisor.entity.OntologyRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OntologyRelationRepository extends JpaRepository<OntologyRelationEntity, Long> {

    List<OntologyRelationEntity> findBySourceConceptIgnoreCase(String sourceConcept);

    List<OntologyRelationEntity> findByRelationTypeIgnoreCase(String relationType);

    List<OntologyRelationEntity> findBySourceConceptIgnoreCaseAndRelationTypeIgnoreCase(
            String sourceConcept,
            String relationType
    );

    boolean existsBySourceConceptIgnoreCaseAndRelationTypeIgnoreCaseAndTargetConceptIgnoreCase(
            String sourceConcept,
            String relationType,
            String targetConcept
    );
}
