package com.footballadvisor.service;

import com.footballadvisor.entity.PlayerEntity;
import com.footballadvisor.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final FootballKnowledgeSyncService footballKnowledgeSyncService;

    public PlayerEntity createPlayer(PlayerEntity player) {
        PlayerEntity savedPlayer = playerRepository.save(player);
        footballKnowledgeSyncService.syncPlayer(savedPlayer);

        return savedPlayer;
    }

    public List<PlayerEntity> getAllPlayers() {
        return playerRepository.findAll();
    }

    public PlayerEntity getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
    }
}
