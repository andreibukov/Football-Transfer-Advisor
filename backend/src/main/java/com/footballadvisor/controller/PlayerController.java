package com.footballadvisor.controller;

import com.footballadvisor.entity.PlayerEntity;
import com.footballadvisor.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping
    public PlayerEntity createPlayer(@RequestBody PlayerEntity player) {
        return playerService.createPlayer(player);
    }

    @GetMapping
    public List<PlayerEntity> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    @GetMapping("/{id}")
    public PlayerEntity getPlayerById(@PathVariable Long id) {
        return playerService.getPlayerById(id);
    }
}