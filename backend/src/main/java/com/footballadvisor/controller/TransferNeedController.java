package com.footballadvisor.controller;

import com.footballadvisor.entity.TransferNeedEntity;
import com.footballadvisor.service.TransferNeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TransferNeedController {

    private final TransferNeedService transferNeedService;

    @PostMapping("/clubs/{clubId}/transfer-needs")
    public TransferNeedEntity createTransferNeed(
            @PathVariable Long clubId,
            @RequestBody TransferNeedEntity transferNeed
    ) {
        return transferNeedService.createTransferNeed(clubId, transferNeed);
    }

    @GetMapping("/transfer-needs/{id}")
    public TransferNeedEntity getTransferNeedById(@PathVariable Long id) {
        return transferNeedService.getTransferNeedById(id);
    }
}
