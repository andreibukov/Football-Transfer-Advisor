package com.footballadvisor.repository;

import com.footballadvisor.entity.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {

    boolean existsByNameIgnoreCase(String name);
}
