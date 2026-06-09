package com.footballadvisor.controller;

import com.footballadvisor.dto.TransferNeedRequest;
import com.footballadvisor.service.AgentOrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AgentController {

    private final AgentOrchestrationService agentOrchestrationService;

    @PostMapping("/transfer-analysis/{transferNeedId}")
    public String startTransferAnalysis(@PathVariable Long transferNeedId) {
        return agentOrchestrationService.startTransferAnalysis(transferNeedId);
    }

    @PostMapping("/test-flow")
    public String startTestFlow(@RequestBody TransferNeedRequest request) {
        return agentOrchestrationService.startTestFlow(request);
    }
}
