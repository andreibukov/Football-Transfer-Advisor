package com.footballadvisor.repository;

import com.footballadvisor.entity.OntologyConceptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OntologyConceptRepository extends JpaRepository<OntologyConceptEntity, Long> {

    List<OntologyConceptEntity> findByConceptNameContainingIgnoreCase(String conceptName);

    List<OntologyConceptEntity> findByConceptTypeIgnoreCase(String conceptType);

    boolean existsByConceptNameIgnoreCaseAndConceptTypeIgnoreCase(
            String conceptName,
            String conceptType
    );
}
