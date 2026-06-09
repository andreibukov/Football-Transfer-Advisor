package com.footballadvisor.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class BackendSenderAgent extends Agent {

    @Override
    protected void setup() {
        Object[] args = getArguments();

        String content = "Analyze test transfer need.";

        if (args != null && args.length > 0) {
            content = String.valueOf(args[0]);
        }

        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(new AID("ClubAnalysisAgent", AID.ISLOCALNAME));
        message.setContent(content);

        send(message);
        AgentAclLogger.log(
                getLocalName(),
                "ClubAnalysisAgent",
                message.getPerformative(),
                content
        );

        System.out.println("[ACL] BackendSenderAgent -> ClubAnalysisAgent | REQUEST | " + content);

        doDelete();
    }
}
