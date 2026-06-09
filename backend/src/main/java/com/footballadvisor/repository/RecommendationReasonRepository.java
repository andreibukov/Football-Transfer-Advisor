package com.footballadvisor.repository;

import com.footballadvisor.entity.RecommendationReasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationReasonRepository extends JpaRepository<RecommendationReasonEntity, Long> {

    List<RecommendationReasonEntity> findByRecommendationId(Long recommendationId);
}