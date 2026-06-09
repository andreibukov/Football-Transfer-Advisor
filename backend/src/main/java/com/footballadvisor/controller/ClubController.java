package com.footballadvisor.controller;

import com.footballadvisor.entity.ClubEntity;
import com.footballadvisor.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ClubController {

    private final ClubService clubService;

    @PostMapping
    public ClubEntity createClub(@RequestBody ClubEntity club) {
        return clubService.createClub(club);
    }

    @GetMapping
    public List<ClubEntity> getAllClubs() {
        return clubService.getAllClubs();
    }
}
