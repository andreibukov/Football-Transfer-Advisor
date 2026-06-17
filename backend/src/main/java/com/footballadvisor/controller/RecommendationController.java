package com.footballadvisor.controller;

import com.footballadvisor.dto.RecommendationResponse;
import com.footballadvisor.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfer-needs/{transferNeedId}/recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public List<RecommendationResponse> getRecommendations(@PathVariable Long transferNeedId) {
        return recommendationService.getRecommendations(transferNeedId);
    }
}
