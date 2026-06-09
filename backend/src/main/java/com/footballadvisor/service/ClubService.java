package com.footballadvisor.service;

import com.footballadvisor.entity.ClubEntity;
import com.footballadvisor.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final FootballKnowledgeSyncService footballKnowledgeSyncService;

    public ClubEntity createClub(ClubEntity club) {
        ClubEntity savedClub = clubRepository.save(club);
        footballKnowledgeSyncService.syncClub(savedClub);

        return savedClub;
    }

    public List<ClubEntity> getAllClubs() {
        return clubRepository.findAll();
    }
}
