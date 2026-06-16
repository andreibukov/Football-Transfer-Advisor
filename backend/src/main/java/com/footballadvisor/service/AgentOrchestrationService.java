package com.footballadvisor.service;

import com.footballadvisor.agent.BackendSenderAgent;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgentOrchestrationService {

    private final AgentContainer jadeContainer;

    public String startTransferAnalysis(Long transferNeedId) {
        String content = "TRANSFER_NEED_ID|" + transferNeedId;

        return startAclFlow(content);
    }

    private String startAclFlow(String content) {
        try {
            AgentController senderAgent = jadeContainer.createNewAgent(
                    "BackendSenderAgent-" + System.currentTimeMillis(),
                    BackendSenderAgent.class.getName(),
                    new Object[]{content}
            );

            senderAgent.start();

            return "ACL flow started successfully for: " + content;

        } catch (Exception ex) {
            return "Failed to start ACL flow: " + ex.getMessage();
        }
    }
}
