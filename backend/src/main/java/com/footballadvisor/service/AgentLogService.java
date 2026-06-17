package com.footballadvisor.service;

import com.footballadvisor.entity.AgentLogEntity;
import com.footballadvisor.repository.AgentLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentLogService {

    private final AgentLogRepository agentLogRepository;

    public AgentLogEntity saveLog(
            String senderAgent,
            String receiverAgent,
            String performative,
            String messageContent
    ) {
        AgentLogEntity log = AgentLogEntity.builder()
                .senderAgent(senderAgent)
                .receiverAgent(receiverAgent)
                .performative(performative)
                .messageContent(messageContent)
                .build();

        return agentLogRepository.save(log);
    }

    public List<AgentLogEntity> getAllLogs() {
        return agentLogRepository.findAll();
    }

    public void clearLogs() {
        agentLogRepository.deleteAll();
    }
}
