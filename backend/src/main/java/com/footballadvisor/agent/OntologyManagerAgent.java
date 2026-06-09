package com.footballadvisor.agent;

import com.footballadvisor.config.SpringContext;
import com.footballadvisor.entity.TransferNeedEntity;
import com.footballadvisor.ontology.OntologyQueryService;
import com.footballadvisor.repository.TransferNeedRepository;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.util.List;

public class OntologyManagerAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("OntologyManagerAgent started.");

        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage message = receive();

                if (message != null) {
                    String sender = message.getSender().getLocalName();

                    System.out.println("[ACL] OntologyManagerAgent received from "
                            + sender
                            + " | "
                            + ACLMessage.getPerformative(message.getPerformative())
                            + " | "
                            + message.getContent());
                    AgentAclLogger.log(
                            sender,
                            getLocalName(),
                            message.getPerformative(),
                            message.getContent()
                    );

                    OntologyQueryService ontologyQueryService =
                            SpringContext.getBean(OntologyQueryService.class);

                    TransferNeedContext transferNeedContext = resolveTransferNeedContext(message.getContent());

                    List<String> positionAttributes =
                            ontologyQueryService.findRelatedAttributes(
                                    transferNeedContext.neededPosition(),
                                    "requiresAttribute"
                            );

                    List<String> styleAttributes =
                            ontologyQueryService.findRelatedAttributes(
                                    transferNeedContext.playingStyle(),
                                    "styleRequiresAttribute"
                            );

                    String ontologyContext = "Ontology context found: "
                            + transferNeedContext.neededPosition()
                            + " requires "
                            + String.join(", ", positionAttributes)
                            + ". "
                            + transferNeedContext.playingStyle()
                            + " requires "
                            + String.join(", ", styleAttributes)
                            + ". "
                            + transferNeedContext.payload();

                    ACLMessage reply = message.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(ontologyContext);
                    send(reply);
                    AgentAclLogger.log(
                            getLocalName(),
                            sender,
                            reply.getPerformative(),
                            reply.getContent()
                    );

                    System.out.println("[ACL] OntologyManagerAgent -> "
                            + sender
                            + " | INFORM | "
                            + reply.getContent());
                } else {
                    block();
                }
            }
        });
    }

    private TransferNeedContext resolveTransferNeedContext(String content) {
        if (content != null && content.startsWith("TRANSFER_NEED_ID|")) {
            Long transferNeedId = Long.parseLong(content.split("\\|")[1].trim());

            TransferNeedRepository transferNeedRepository =
                    SpringContext.getBean(TransferNeedRepository.class);

            TransferNeedEntity transferNeed = transferNeedRepository
                    .findById(transferNeedId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Transfer need not found with id: " + transferNeedId
                    ));

            return new TransferNeedContext(
                    transferNeed.getNeededPosition(),
                    transferNeed.getPlayingStyle(),
                    "TransferNeedId: " + transferNeedId
            );
        }

        String[] queryParts = content.split("\\|");

        return new TransferNeedContext(
                queryParts[0].trim(),
                queryParts[1].trim(),
                "TransferNeed: "
                        + queryParts[0].trim()
                        + "|"
                        + queryParts[1].trim()
                        + "|"
                        + queryParts[2].trim()
                        + "|"
                        + queryParts[3].trim()
        );
    }

    private record TransferNeedContext(
            String neededPosition,
            String playingStyle,
            String payload
    ) {
    }
}
