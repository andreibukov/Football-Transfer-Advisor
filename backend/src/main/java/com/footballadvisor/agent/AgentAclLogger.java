package com.footballadvisor.agent;

import com.footballadvisor.config.SpringContext;
import com.footballadvisor.service.AgentLogService;
import jade.lang.acl.ACLMessage;

public final class AgentAclLogger {

    private AgentAclLogger() {
    }

    public static void log(String senderAgent, String receiverAgent, int performative, String messageContent) {
        try {
            AgentLogService agentLogService = SpringContext.getBean(AgentLogService.class);
            agentLogService.saveLog(
                    senderAgent,
                    receiverAgent,
                    ACLMessage.getPerformative(performative),
                    messageContent
            );
        } catch (Exception ex) {
            System.out.println("[ACL] Failed to persist agent log: " + ex.getMessage());
        }
    }
}
