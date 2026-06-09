package com.footballadvisor.controller;

import com.footballadvisor.entity.AgentLogEntity;
import com.footballadvisor.service.AgentLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AgentLogController {

    private final AgentLogService agentLogService;

    @GetMapping
    public List<AgentLogEntity> getAllLogs() {
        return agentLogService.getAllLogs();
    }
}