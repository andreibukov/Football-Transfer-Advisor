package com.footballadvisor.repository;

import com.footballadvisor.entity.AgentLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentLogRepository extends JpaRepository<AgentLogEntity, Long> {
}