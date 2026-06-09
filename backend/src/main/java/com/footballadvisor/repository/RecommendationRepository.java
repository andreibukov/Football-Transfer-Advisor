package com.footballadvisor.repository;

import com.footballadvisor.entity.RecommendationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<RecommendationEntity, Long> {

    List<RecommendationEntity> findByTransferNeedIdOrderByTotalScoreDesc(Long transferNeedId);
}