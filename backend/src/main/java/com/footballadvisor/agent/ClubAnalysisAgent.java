package com.footballadvisor.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class ClubAnalysisAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("ClubAnalysisAgent started.");

        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage message = receive();

                if (message != null) {
                    String sender = message.getSender().getLocalName();
                    String performative = ACLMessage.getPerformative(message.getPerformative());

                    System.out.println("[ACL] ClubAnalysisAgent received from "
                            + sender
                            + " | "
                            + performative
                            + " | "
                            + message.getContent());

                    if (message.getPerformative() == ACLMessage.REQUEST) {
                        ACLMessage ontologyQuery = new ACLMessage(ACLMessage.QUERY_REF);
                        ontologyQuery.addReceiver(new AID("OntologyManagerAgent", AID.ISLOCALNAME));
                        ontologyQuery.setContent(message.getContent());
                        send(ontologyQuery);
                        AgentAclLogger.log(
                                getLocalName(),
                                "OntologyManagerAgent",
                                ontologyQuery.getPerformative(),
                                ontologyQuery.getContent()
                        );

                        System.out.println("[ACL] ClubAnalysisAgent -> OntologyManagerAgent | QUERY_REF | "
                                + ontologyQuery.getContent());

                        return;
                    }

                    if (message.getPerformative() == ACLMessage.INFORM
                            && sender.equals("OntologyManagerAgent")) {

                        ACLMessage recommendationRequest = new ACLMessage(ACLMessage.REQUEST);
                        recommendationRequest.addReceiver(new AID("TransferRecommendationAgent", AID.ISLOCALNAME));
                        recommendationRequest.setContent("Generate recommendations using ontology context: "
                                + message.getContent());
                        send(recommendationRequest);
                        AgentAclLogger.log(
                                getLocalName(),
                                "TransferRecommendationAgent",
                                recommendationRequest.getPerformative(),
                                recommendationRequest.getContent()
                        );

                        System.out.println("[ACL] ClubAnalysisAgent -> TransferRecommendationAgent | REQUEST | "
                                + recommendationRequest.getContent());

                        return;
                    }

                    if (message.getPerformative() == ACLMessage.INFORM
                            && sender.equals("TransferRecommendationAgent")) {

                        System.out.println("[ACL] ClubAnalysisAgent final result received | "
                                + message.getContent());

                        return;
                    }

                } else {
                    block();
                }
            }
        });
    }
}
