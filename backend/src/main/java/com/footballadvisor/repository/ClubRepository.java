package com.footballadvisor.repository;

import com.footballadvisor.entity.ClubEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<ClubEntity, Long> {

    boolean existsByNameIgnoreCase(String name);
}
